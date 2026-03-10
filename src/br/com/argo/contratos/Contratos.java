package br.com.argo.contratos;

import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;
import com.sankhya.util.JdbcUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;

import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class Contratos implements ScheduledAction {

	@Override
	public void onTime(ScheduledActionContext sac) {
		// TODO Auto-generated method stub
		JdbcWrapper jdbc = null;
		NativeSql queryVoa = null;
		ResultSet rset = null;
		SessionHandle hnd = null;
		NotificacaoUser notificauser = new NotificacaoUser();
		EnvioEmail envioEmails = new EnvioEmail();
		String tituloEmail15 = "Vencimento de contrato 15 (dias)";
		String tituloEmail30 = "Vencimento de contrato 30 (dias)";
		String tituloNotificacao = "Vencimento de contrato hoje";
		try {

			// Abre uma sessão no banco de dados
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);
			// Obtém uma instância para interagir com o banco de dados
			EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();
			
			// Cria uma consulta SQL
			queryVoa = new NativeSql(jdbc);
			queryVoa.appendSql("SELECT CON.NUMCONTRATO, CON.DTCONTRATO,CON.DTTERMINO,CON.CODPARC,t.EMAIL,PAR.NOMEPARC \r\n"
					+ "					FROM TCSCON CON,TGFPAR PAR,TGFCTT t\r\n"
					+ "					WHERE CON.CODPARC = PAR.CODPARC \r\n"
					+ "					AND t.CODPARC = CON.CODPARC  AND t.CODCONTATO = CON.CODCONTATO");
			
			// Executa a consulta SQL e obtém o conjunto de resultados
			rset = queryVoa.executeQuery();
			while (rset.next()) {
				Calendar dataAtual = Calendar.getInstance();
				// Obtém os valores das colunas do resultado da consulta
				Long numeroContrato = rset.getLong("NUMCONTRATO");
				Date datainicio = rset.getDate("DTCONTRATO");
				Date datafim = rset.getDate("DTTERMINO");
				String nomeParc = rset.getString("NOMEPARC");
				String emailparc = rset.getString("EMAIL");
		
				String linkContrato = geraLinkAviso(numeroContrato); // Passa numeroContrato em vez de numcontrato
				// Converte as datas para o formato desejado
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				String dataInicioFormatada = sdf.format(datainicio);
				String dataFimFormatada = sdf.format(datafim);
				String strDataHoje = sdf.format(dataAtual.getTime());
				// Data atual do calendário
			
				// Remover horas, minutos, segundos e milissegundos da data atual
				zerarHorasMinutosSegundos(dataAtual);
				
				Calendar dataExpiracao15 = Calendar.getInstance();
				dataExpiracao15.setTime(datafim);
				dataExpiracao15.add(Calendar.DATE, -15); // Alteração aqui para subtrair 15 dias
				zerarHorasMinutosSegundos(dataExpiracao15);// Remover horas, minutos, segundos e milissegundos da data de vencimento
				
				Calendar dataExpiracao30 = Calendar.getInstance();
				dataExpiracao30.setTime(datafim);
				dataExpiracao30.add(Calendar.DATE, -30); // Notificados com 30 dias de atraso
				zerarHorasMinutosSegundos(dataExpiracao30); // Remover horas, minutos, segundos e milissegundos da data de vencimento
				
				
				// Verifica se a diferença entre a data de vencimento e a data atual é  exatamente de 30 e 15 dias
				
				long diferencaEmMillis15 = dataAtual.getTimeInMillis() - dataExpiracao15.getTimeInMillis();
				long diferencaEmDias15 = TimeUnit.DAYS.convert(diferencaEmMillis15, TimeUnit.MILLISECONDS);
				
				long diferencaEmMillis30 = dataAtual.getTimeInMillis() - dataExpiracao30.getTimeInMillis();
				long diferencaEmDias30 = TimeUnit.DAYS.convert(diferencaEmMillis30, TimeUnit.MILLISECONDS);
				
				if (diferencaEmDias15 == 0) {
				// Corpo do email com variáveis inseridas
				String corpoemail = "<!DOCTYPE html>\r\n"
						+ "<html>\r\n"
						+ "<head>\r\n"
						+ "<title>Lembrete da Argo Fruta</title>\r\n"
						+ "<link href=\"https://fonts.googleapis.com/css?family=Poppins:200,300,400,500,600,700\" rel=\"stylesheet\">\r\n"
						+ "</head>\r\n"
						+ "<body style=\"background-color: #f4f4f4; margin: 0; padding: 0; width: 100%; height: 100%; font-family: Poppins, sans-serif; color: rgba(0, 0, 0, .4);\">\r\n"
						+ "<table width=\"100%\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n"
						+ "    <tr>\r\n"
						+ "        <td align=\"center\" valign=\"top\" style=\"padding-top: 20px; padding-bottom: 20px;\">\r\n"
						+ "            <table width=\"600\" border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"background-color: white; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.1); min-height: 400px;\">\r\n"
						+ "                <tr>\r\n"
						+ "                    <td align=\"center\" style=\"margin-bottom: 20px; \">\r\n"
						+ "                        <img src=\"https://argofruta.com/wp-content/uploads/2021/05/Logo-text-green.png\" alt=\"Satya Code Logo\" width=\"250\" style=\"margin-top: 30px;\">\r\n"
						+ "                    </td>\r\n"
						+ "                </tr>\r\n"
						+ "                <tr>\r\n"
						+ "                    <td>\r\n"
						+ "                        <h2 style=\"font-family: Poppins, sans-serif; color: #000000; margin-top: 0; font-weight: 400;text-align: center; \">Lembrete da ArgoFruta</h2>\r\n"
						+ "                        <div style=\"  border: 1px solid rgba(0, 0, 0, .05); max-width: 80%;margin: 0 auto;padding: 2em;\">\r\n"
						+ "                            <p style=\"text-align: justify; font-size: 15px;\">Prezado " + nomeParc + ",<br>\r\n"
						+ "                            Identificamos que o(s) seu(s) contrato(s) nº " + numeroContrato + " expira em 15(dias).<br>\r\n"
						+ "                            Data de Início do Contrato: " + dataInicioFormatada + "<br>\r\n"
						+ "                            Data de Término do Contrato: " + dataFimFormatada + "<br>\r\n"
						+ "                            .</p>\r\n"
						+ "                        </div>\r\n"
						+ "                        <br>\r\n"
						+ "                    </td>\r\n"
						+ "                </tr>\r\n"
						+ "            </table>\r\n"
						+ "        </td>\r\n"
						+ "    </tr>\r\n"
						+ "</table>\r\n"
						+ "</body>\r\n"
						+ "</html>";

				
				String mensagemNotificacao = "<h4>Parceiro</h4>"
				        + "<p>" + nomeParc + ".</p>"
				        + "<hr>"
				        + "<h4>E-mail contato</h4>"
				        + "<p>" + emailparc + ".</p>"
				        + "<hr>"
				        + "<h4>Data de término do contrato</h4>"
				        + "<p>" + dataFimFormatada + "</p>"
				        + "<hr>"
				        + "<h4>Visualizar o contrato</h4>"
				        + "<p><a href='" + linkContrato + "'>Ver contrato</a></p>";



				// Enviar o email
				envioEmails.enviarEmail(tituloEmail15, corpoemail);
				notificauser.notifUsu( mensagemNotificacao, tituloEmail15);
				}else if (diferencaEmDias30 == 0) {
					// Corpo do email com variáveis inseridas
					String corpoemail = "<!DOCTYPE html>\r\n"
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "<title>Lembrete da Argo Fruta</title>\r\n"
							+ "<link href=\"https://fonts.googleapis.com/css?family=Poppins:200,300,400,500,600,700\" rel=\"stylesheet\">\r\n"
							+ "</head>\r\n"
							+ "<body style=\"background-color: #f4f4f4; margin: 0; padding: 0; width: 100%; height: 100%; font-family: Poppins, sans-serif; color: rgba(0, 0, 0, .4);\">\r\n"
							+ "<table width=\"100%\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n"
							+ "    <tr>\r\n"
							+ "        <td align=\"center\" valign=\"top\" style=\"padding-top: 20px; padding-bottom: 20px;\">\r\n"
							+ "            <table width=\"600\" border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"background-color: white; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.1); min-height: 400px;\">\r\n"
							+ "                <tr>\r\n"
							+ "                    <td align=\"center\" style=\"margin-bottom: 20px; \">\r\n"
							+ "                        <img src=\"https://argofruta.com/wp-content/uploads/2021/05/Logo-text-green.png\" alt=\"Satya Code Logo\" width=\"250\" style=\"margin-top: 30px;\">\r\n"
							+ "                    </td>\r\n"
							+ "                </tr>\r\n"
							+ "                <tr>\r\n"
							+ "                    <td>\r\n"
							+ "                        <h2 style=\"font-family: Poppins, sans-serif; color: #000000; margin-top: 0; font-weight: 400;text-align: center; \">Lembrete da ArgoFruta</h2>\r\n"
							+ "                        <div style=\"  border: 1px solid rgba(0, 0, 0, .05); max-width: 80%;margin: 0 auto;padding: 2em;\">\r\n"
							+ "                            <p style=\"text-align: justify; font-size: 15px;\">Prezado " + nomeParc + ",<br>\r\n"
							+ "                            Identificamos que o(s) seu(s) contrato(s) nº " + numeroContrato + " expira em 30(dias).<br>\r\n"
							+ "                            Data de Início do Contrato: " + dataInicioFormatada + "<br>\r\n"
							+ "                            Data de Término do Contrato: " + dataFimFormatada + "<br>\r\n"
							+ "                            .</p>\r\n"
							+ "                        </div>\r\n"
							+ "                        <br>\r\n"
							+ "                    </td>\r\n"
							+ "                </tr>\r\n"
							+ "            </table>\r\n"
							+ "        </td>\r\n"
							+ "    </tr>\r\n"
							+ "</table>\r\n"
							+ "</body>\r\n"
							+ "</html>";

					
					String mensagemNotificacao = "<h4>Parceiro</h4>"
					        + "<p>" + nomeParc + ".</p>"
					        + "<hr>"
					        + "<h4>E-mail contato</h4>"
					        + "<p>" + emailparc + ".</p>"
					        + "<hr>"
					        + "<h4>Data de término do contrato</h4>"
					        + "<p>" + dataFimFormatada + "</p>"
					        + "<hr>"
					        + "<h4>Visualizar o contrato</h4>"
					        + "<p><a href='" + linkContrato + "'>Ver contrato</a></p>";



					// Enviar o email
					envioEmails.enviarEmail(tituloEmail30, corpoemail);
					notificauser.notifUsu(mensagemNotificacao, tituloEmail30);
					
				}else if (dataFimFormatada.equals(strDataHoje)) {
					String corpoemail2 = "<!DOCTYPE html>\r\n"
							+ "<head>\r\n"
							+ "<title>Lembrete da Argo Fruta</title>\r\n"
							+ "<link href=\"https://fonts.googleapis.com/css?family=Poppins:200,300,400,500,600,700\" rel=\"stylesheet\">\r\n"
							+ "</head>\r\n"
							+ "<body style=\"background-color: #f4f4f4; margin: 0; padding: 0; width: 100%; height: 100%; font-family: Poppins, sans-serif; color: rgba(0, 0, 0, .4);\">\r\n"
							+ "<table width=\"100%\" height=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\r\n"
							+ "    <tr>\r\n"
							+ "        <td align=\"center\" valign=\"top\" style=\"padding-top: 20px; padding-bottom: 20px;\">\r\n"
							+ "            <table width=\"600\" border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"background-color: white; margin: auto; box-shadow: 0 0 10px rgba(0,0,0,0.1); min-height: 400px;\">\r\n"
							+ "                <tr>\r\n"
							+ "                    <td align=\"center\" style=\"margin-bottom: 20px; \">\r\n"
							+ "                        <img src=\"https://argofruta.com/wp-content/uploads/2021/05/Logo-text-green.png\" alt=\"Satya Code Logo\" width=\"250\" style=\"margin-top: 30px;\">\r\n"
							+ "                    </td>\r\n"
							+ "                </tr>\r\n"
							+ "                <tr>\r\n"
							+ "                    <td>\r\n"
							+ "                        <h2 style=\"font-family: Poppins, sans-serif; color: #000000; margin-top: 0; font-weight: 400;text-align: center; \">Lembrete da ArgoFruta</h2>\r\n"
							+ "                        <div style=\"  border: 1px solid rgba(0, 0, 0, .05); max-width: 80%;margin: 0 auto;padding: 2em;\">\r\n"
							+ "                            <p style=\"text-align: justify; font-size: 15px;\">Prezado " + nomeParc + ",<br>\r\n"
							+ "                            Identificamos que o(s) seu(s) contrato(s) nº " + numeroContrato + " .<br>\r\n"
							+ "                            Data de Início do Contrato: " + dataInicioFormatada + "<br>\r\n"
							+ "                            Data de Término do contrato expira em hoje : " + dataFimFormatada + "<br>\r\n"
							+ "                            Por favor, entre em contato para mais informações.</p>\r\n"
							+ "                        </div>\r\n"
							+ "                        <br>\r\n"
							+ "                    </td>\r\n"
							+ "                </tr>\r\n"
							+ "            </table>\r\n"
							+ "        </td>\r\n"
							+ "    </tr>\r\n"
							+ "</table>\r\n"
							+ "</body>\r\n"
							+ "</html>";
					
					String mensagemNotificacao = "<h4>Parceiro</h4>"
					        + "<p>" + nomeParc + ".</p>"
					        + "<hr>"
					        + "<h4>E-mail contato</h4>"
					        + "<p>" + emailparc + ".</p>"
					        + "<hr>"
					        + "<h4>Data de término do contrato hoje</h4>"
					        + "<p>" + dataFimFormatada + "</p>"
					        + "<hr>"
					        + "<h4>Visualizar o contrato</h4>"
					        + "<p><a href='" + linkContrato + "'>Ver contrato</a></p>";
				

					
					envioEmails.enviarEmail(tituloNotificacao, corpoemail2);
					notificauser.notifUsu( mensagemNotificacao, tituloNotificacao);
					
				}
			}

		} catch (Exception e) {
			// Exibir erro
			e.printStackTrace();
			sac.info("Erro Evento Programado Atualização de emails para contratos: " + e.getMessage());
		} finally {
			// Liberação de recursos e fechamento da sessão
			JdbcUtils.closeResultSet(rset);
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
			NativeSql.releaseResources(queryVoa);
		}
	}
	private void zerarHorasMinutosSegundos(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	public String geraLinkAviso(Long numeroContrato) {
	    // - Termos fixos para a montagem da URL
	    String prefixo = "#app";
	    String resourceIdTela = "br.com.sankhya.os.cad.contratos";
	    String mascara = "{\"NUMCONTRATO\":\"%d\"}";

	    // - Processamento dos parametros de entrada
	    String parametrosTela = String.format(mascara, numeroContrato);
	    
	    // - Montagem dos Base64
	    String tela = java.util.Base64.getEncoder().encodeToString(resourceIdTela.getBytes());
	    String parametros = java.util.Base64.getEncoder().encodeToString(parametrosTela.getBytes());
	    
	    // - Montagem final da URL
	    String link = prefixo + "/" + tela + "/" + parametros;
	    
	    // - Retorno da url final
	    return link;
	}

}	
