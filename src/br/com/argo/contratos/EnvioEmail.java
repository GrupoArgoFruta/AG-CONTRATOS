package br.com.argo.contratos;

import java.math.BigDecimal;

import com.sankhya.util.BigDecimalUtil;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

public class EnvioEmail {
	public  void enviarEmail(String titulo, String mensagem) throws Exception {
		SessionHandle hnd = null;
		try {
			hnd = JapeSession.open();
			JapeWrapper ordemServicoDAO = JapeFactory.dao(DynamicEntityNames.FILA_MSG);
			ordemServicoDAO.create()
			.set("EMAIL", "juridico@argofruta.com")
			.set("CODCON", BigDecimal.ZERO)
			.set("STATUS", "Pendente")
			.set("TIPOENVIO", "E")
			.set("MAXTENTENVIO", BigDecimalUtil.valueOf(3))
			.set("ASSUNTO", titulo)
			.set("MENSAGEM", mensagem.toCharArray())
			.save();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JapeSession.close(hnd);
		}
	}
}
