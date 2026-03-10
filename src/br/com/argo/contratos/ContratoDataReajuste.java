package br.com.argo.contratos;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import com.sankhya.util.JdbcUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Evento programado para envio de notificações de reajuste de contrato.
 *
 * Dispara emails e notificações internas nos marcos de 30 dias, 15 dias
 * e no dia do vencimento da data base de reajuste (DTBASEREAJ).
 *
 * Utiliza a tabela AD_LOGNOTIFCONTRATO com constraint UNIQUE
 * (NUMCONTRATO, TIPONOTIF, DTBASEREAJ) para garantir envio único
 * por contrato/tipo/data de reajuste.
 *
 * Cron sugerido: 0 0 6 ? * * (todo dia às 06:00)  0 0 0 ? * *
 */
public class ContratoDataReajuste implements ScheduledAction {

	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

	// ========================== QUERY PRINCIPAL ==========================
	// Retorna apenas contratos que:
	// 1. Estão na janela de notificação (0, 15 ou 30 dias)
	// 2. Ainda NÃO foram notificados nesse ciclo (NOT EXISTS no log)
	// ====================================================================
	private static final String SQL_CONTRATOS_PENDENTES =
			"SELECT CON.NUMCONTRATO, CON.DTCONTRATO, CON.DTBASEREAJ, " +
					"       CON.CODPARC, T.EMAIL, PAR.NOMEPARC, " +
					"       TRUNC(CON.DTBASEREAJ) - TRUNC(SYSDATE) AS DIAS_RESTANTES " +
					"FROM TCSCON CON " +
					"JOIN TGFPAR PAR ON CON.CODPARC = PAR.CODPARC " +
					"JOIN TGFCTT T ON T.CODPARC = CON.CODPARC " +
					"  AND T.CODCONTATO = CON.CODCONTATO " +
					"WHERE CON.DTBASEREAJ IS NOT NULL " +
					"  AND TRUNC(CON.DTBASEREAJ) - TRUNC(SYSDATE) IN (0, 15, 30) " +
					"  AND NOT EXISTS ( " +
					"      SELECT 1 FROM AD_LOGNOTIFCONTRATO LOG " +
					"      WHERE LOG.NUMCONTRATO = CON.NUMCONTRATO " +
					"        AND LOG.DTBASEREAJ = TRUNC(CON.DTBASEREAJ) " +
					"        AND LOG.TIPONOTIF = CASE TRUNC(CON.DTBASEREAJ) - TRUNC(SYSDATE) " +
					"                                WHEN 15 THEN '15DIAS' " +
					"                                WHEN 30 THEN '30DIAS' " +
					"                                WHEN 0  THEN 'HOJE' " +
					"                            END " +
					"  )";

	@Override
	public void onTime(ScheduledActionContext sac) {

		JdbcWrapper jdbc = null;
		NativeSql query = null;
		ResultSet rset = null;
		SessionHandle hnd = null;

		NotificacaoUser notificaUser = new NotificacaoUser();
		EnvioEmail envioEmails = new EnvioEmail();

		int totalEnviados = 0;
		int totalErros = 0;

		try {
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);

			EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();

			query = new NativeSql(jdbc);
			query.appendSql(SQL_CONTRATOS_PENDENTES);
			rset = query.executeQuery();

			while (rset.next()) {
				Long numContrato = rset.getLong("NUMCONTRATO");
				Timestamp dataInicio = rset.getTimestamp("DTCONTRATO");
				Timestamp dataReajuste = rset.getTimestamp("DTBASEREAJ");
				String nomeParc = rset.getString("NOMEPARC");
				String emailParc = rset.getString("EMAIL");
				int diasRestantes = rset.getInt("DIAS_RESTANTES");

				String tipoNotif = resolverTipoNotif(diasRestantes);
				String tituloEmail = resolverTituloEmail(diasRestantes);

				String dataInicioFmt = SDF.format(dataInicio);
				String dataReajusteFmt = SDF.format(dataReajuste);
				String linkContrato = geraLinkAviso(numContrato);

				try {
					// Monta e envia email
					String corpoEmail = montarCorpoEmail(
							nomeParc, numContrato, dataInicioFmt, dataReajusteFmt, diasRestantes
					);
					envioEmails.enviarEmail(tituloEmail, corpoEmail);

					// Monta e envia notificação interna
					String mensagemNotif = montarNotificacao(
							nomeParc, emailParc, dataReajusteFmt, linkContrato, diasRestantes
					);
					notificaUser.notifUsu(mensagemNotif, tituloEmail);

					// Registra no log para não enviar novamente
					registrarLogEnvio( numContrato, tipoNotif, dataReajuste);
					totalEnviados++;

				} catch (Exception envioEx) {
					totalErros++;
					sac.info("Erro ao notificar contrato " + numContrato + ": " + envioEx.getMessage());
				}
			}

			sac.info("Job finalizado - Enviados: " + totalEnviados + " | Erros: " + totalErros);

		} catch (Exception e) {
			e.printStackTrace();
			sac.info("Erro Evento Programado Reajuste Contratos: " + e.getMessage());
		} finally {
			JdbcUtils.closeResultSet(rset);
			NativeSql.releaseResources(query);
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}
	}

	// ========================== REGISTRO DE LOG ==========================

	/**
	 * Insere registro na AD_LOGNOTIFCONTRATO após envio bem-sucedido.
	 * A constraint UK_NOTIF (NUMCONTRATO, TIPONOTIF, DTBASEREAJ)
	 * garante idempotência mesmo em caso de execução concorrente.
	 *
	 */
	private void registrarLogEnvio(Long numContrato, String tipoNotif, Timestamp dtBaseReaj) throws Exception {
		EntityFacade entityFacade = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbcLog = entityFacade.getJdbcWrapper();
		PreparedStatement pstmt = null;
		try {
			jdbcLog.openSession();
			String sql = "INSERT INTO AD_LOGNOTIFCONTRATO (IDLOG, NUMCONTRATO, TIPONOTIF, DTBASEREAJ, DTENVIO) " +
					"VALUES (SEQ_LOGNOTIFCONTRATO.NEXTVAL, ?, ?, ?, SYSDATE)";
			pstmt = jdbcLog.getPreparedStatement(sql);
			pstmt.setBigDecimal(1, new BigDecimal(numContrato));
			pstmt.setString(2, tipoNotif);
			pstmt.setTimestamp(3, dtBaseReaj);
			pstmt.executeUpdate();
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().contains("UK_NOTIF")) {
				// Já registrado, ignora
			} else {
				throw e;
			}
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			JdbcWrapper.closeSession(jdbcLog);
		}
	}

	// ========================== RESOLVERS ==========================

	private String resolverTipoNotif(int diasRestantes) {
		switch (diasRestantes) {
			case 30: return "30DIAS";
			case 15: return "15DIAS";
			case 0:  return "HOJE";
			default: return "OUTRO";
		}
	}

	private String resolverTituloEmail(int diasRestantes) {
		switch (diasRestantes) {
			case 30: return "Vencimento Data base reajuste 30 (dias)";
			case 15: return "Vencimento Data base reajuste 15 (dias)";
			case 0:  return "Vencimento Data base reajuste hoje";
			default: return "Vencimento Data base reajuste";
		}
	}

	// ========================== MONTAGEM DE EMAIL ==========================

	private String montarCorpoEmail(String nomeParc, Long numContrato, String dataInicio, String dataFim, int diasRestantes) {
		String mensagemPrazo;
		if (diasRestantes == 0) {
			mensagemPrazo = "Identificamos que o(s) seu(s) contrato(s) n&ordm; " + numContrato + " expira hoje.<br>"
					+ "Data de In&iacute;cio do Contrato: " + dataInicio + "<br>"
					+ "Data de T&eacute;rmino do Contrato: " + dataFim + "<br>"
					+ "Por favor, entre em contato para mais informa&ccedil;&otilde;es.";
		} else {
			mensagemPrazo = "Identificamos que o(s) seu(s) contrato(s) n&ordm; " + numContrato + " expira em " + diasRestantes + " (dias).<br>"
					+ "Data de In&iacute;cio do Contrato: " + dataInicio + "<br>"
					+ "Data de T&eacute;rmino do Contrato: " + dataFim;
		}

		return "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<title>Lembrete da Argo Fruta</title>\n"
				+ "<link href=\"https://fonts.googleapis.com/css?family=Poppins:200,300,400,500,600,700\" rel=\"stylesheet\">\n"
				+ "</head>\n"
				+ "<body style=\"background-color: #f4f4f4; margin: 0; padding: 0; width: 100%; height: 100%; font-family: Poppins, sans-serif; color: rgba(0, 0, 0, .4);\">\n"
				+ "<table width=\"100%\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n"
				+ "    <tr>\n"
				+ "        <td align=\"center\" valign=\"top\" style=\"padding-top: 20px; padding-bottom: 20px;\">\n"
				+ "            <table width=\"600\" border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"background-color: white; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.1); min-height: 400px;\">\n"
				+ "                <tr>\n"
				+ "                    <td align=\"center\" style=\"margin-bottom: 20px;\">\n"
				+ "                        <img src=\"https://argofruta.com/wp-content/uploads/2021/05/Logo-text-green.png\" alt=\"Argo Fruta Logo\" width=\"250\" style=\"margin-top: 30px;\">\n"
				+ "                    </td>\n"
				+ "                </tr>\n"
				+ "                <tr>\n"
				+ "                    <td>\n"
				+ "                        <h2 style=\"font-family: Poppins, sans-serif; color: #000000; margin-top: 0; font-weight: 400; text-align: center;\">Lembrete da ArgoFruta</h2>\n"
				+ "                        <div style=\"border: 1px solid rgba(0, 0, 0, .05); max-width: 80%; margin: 0 auto; padding: 2em;\">\n"
				+ "                            <p style=\"text-align: justify; font-size: 15px;\">Prezado " + nomeParc + ",<br>\n"
				+ "                            " + mensagemPrazo + "</p>\n"
				+ "                        </div>\n"
				+ "                        <br>\n"
				+ "                    </td>\n"
				+ "                </tr>\n"
				+ "            </table>\n"
				+ "        </td>\n"
				+ "    </tr>\n"
				+ "</table>\n"
				+ "</body>\n"
				+ "</html>";
	}

	// ========================== MONTAGEM DE NOTIFICAÇÃO ==========================

	private String montarNotificacao(String nomeParc, String emailParc, String dataReajuste, String linkContrato, int diasRestantes) {
		String tituloData = diasRestantes == 0 ? "Data base reajuste hoje" : "Data base reajuste";

		return "<h4>Parceiro</h4>"
				+ "<p>" + nomeParc + "</p>"
				+ "<hr>"
				+ "<h4>E-mail contato</h4>"
				+ "<p>" + emailParc + "</p>"
				+ "<hr>"
				+ "<h4>" + tituloData + "</h4>"
				+ "<p>" + dataReajuste + "</p>"
				+ "<hr>"
				+ "<h4>Visualizar o contrato</h4>"
				+ "<p><a href='" + linkContrato + "'>Ver contrato</a></p>";
	}

	// ========================== LINK DO CONTRATO ==========================

	public String geraLinkAviso(Long numeroContrato) {
		String prefixo = "#app";
		String resourceIdTela = "br.com.sankhya.os.cad.contratos";
		String mascara = "{\"NUMCONTRATO\":\"%d\"}";

		String parametrosTela = String.format(mascara, numeroContrato);

		String tela = java.util.Base64.getEncoder().encodeToString(resourceIdTela.getBytes());
		String parametros = java.util.Base64.getEncoder().encodeToString(parametrosTela.getBytes());

		return prefixo + "/" + tela + "/" + parametros;
	}
}
// Cron sugerido: 0 0 6 ? * *