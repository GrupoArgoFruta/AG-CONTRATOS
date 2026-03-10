package br.com.argo.contratos;

import java.math.BigDecimal;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;

public class NotificacaoUser {
	public  void notifUsu( String obs, String titulo)
			throws MGEModelException {
		JapeWrapper avisoDAO = JapeFactory.dao("AvisoSistema");
		try {
			
				@SuppressWarnings("unused")
				DynamicVO avisoVO = (DynamicVO) avisoDAO.create()
						.set("NUAVISO", null)
						.set("CODUSUREMETENTE", BigDecimal.valueOf(0))
						.set("CODUSU", BigDecimal.valueOf(145)) // Notificar usuário individualmente
//						.set("CODUSU", BigDecimal.valueOf(0)) // Notificar usuário individualmente
						.set("TITULO", titulo)
						.set("DESCRICAO", obs)
						.set("DHCRIACAO", TimeUtils.getNow())
						.set("IDENTIFICADOR", "PERSONALIZADO")
						.set("IMPORTANCIA", BigDecimal.valueOf(3))
						.set("SOLUCAO", null)  
						.set("TIPO", "P")
						.save();

			
		} catch (Exception e) {
			e.printStackTrace();
			MGEModelException.throwMe(e);
			System.out.println("Erro ao Executar Evento notifUsu" + e.getCause() + e.getMessage());
		}
	}
}
