package br.com.argo.contratos;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;

public class TelaHistoricoReajuste {
	public void lancarHistorico(BigDecimal codcontrato,BigDecimal frequencia,Timestamp datareajuste, Timestamp vlrDataOrig,
			BigDecimal vlrFrequenciaOrig, Timestamp dtAlteracao, BigDecimal codUsuProd, String usuarioLogadoNome, BigDecimal vlrcontrato, BigDecimal vlrcontratoOrig2) throws MGEModelException {
		// TODO Auto-generated method stub
		
		JapeSession.SessionHandle hnd = null;
		JapeWrapper hisDAO = JapeFactory.dao("AD_HISTORICOREAJUSTE");
		try {
			
			hnd = JapeSession.open(); // Abertura da sessão do JapeSession
			@SuppressWarnings("unused")
			DynamicVO histoVo = hisDAO.create()
				.set("CODCONTRATO", codcontrato)
				.set("FREQREAJ", frequencia)
				.set("DTBASEREAJ", datareajuste)
				.set("DTBASEREAJANT", vlrDataOrig)
				.set("FREQREAJANTI", vlrFrequenciaOrig)
				.set("DTALTERACAO", dtAlteracao)
				.set("NOMEUSER", usuarioLogadoNome)
				.set("CODUSER", codUsuProd)
				.set("AD_VLRCONTRATO", vlrcontrato)
				.set("AD_VLRCONTRATOHIST", vlrcontratoOrig2)
				.save();  	
			
		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
		}
		
	}
}
