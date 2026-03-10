package br.com.argo.contratos;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.ws.ServiceContext;

public class DataReajuste implements  EventoProgramavelJava {

	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		BigDecimal codUsuProd = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUserID();
		String usuarioLogadoNome = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUsuVO().getNOMEUSU();
		TelaHistoricoReajuste telaHisto = new TelaHistoricoReajuste();
		NotificacaoUser notificar = new NotificacaoUser();
		DynamicVO vo = (DynamicVO) event.getVo();
		JapeWrapper contraDAO = JapeFactory.dao("Contrato");
		Timestamp dtAlteracao = new Timestamp(new Date().getTime());
		String tituloNotificacao = "Inclusão de histórico";
		telaHisto.lancarHistorico(vo.asBigDecimal("NUMCONTRATO"),vo.asBigDecimal("FREQREAJ"),vo.asTimestamp("DTBASEREAJ"),null , null, dtAlteracao, codUsuProd,usuarioLogadoNome,vo.asBigDecimal("AD_VLRCONTRATO"),null);
		String mensagemNotificacao = "<p><h4>Foi incluído o contrato com registro "+vo.asBigDecimal("NUMCONTRATO")+"</h4></p><hr>";
		notificar.notifUsu( mensagemNotificacao, tituloNotificacao);
	}

	@Override
	public void afterUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
//		BigDecimal codUsuProd = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUserID();
//		String usuarioLogadoNome = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUsuVO().getNOMEUSU();
//		TelaHistoricoReajuste telaHisto = new TelaHistoricoReajuste();
//		Util utl = new Util();
//		NotificacaoUser notificar = new NotificacaoUser();
//		DynamicVO vo = (DynamicVO) event.getVo();
//		JapeWrapper contraDAO = JapeFactory.dao("Contrato");
//		DynamicVO contrato = contraDAO.findByPK(vo.asBigDecimal("NUMCONTRATO"));
//		Timestamp dtAlteracao = new Timestamp(new Date().getTime());
//		String tituloNotificacao = "Inclusão de histórico";
//		Timestamp vlrDataOrig = utl.buscarDataReajuste(vo.asBigDecimal("NUMCONTRATO"));
//		BigDecimal vlrFrequenciaOrig = utl.buscarFrequencia(vo.asBigDecimal("NUMCONTRATO"));
//		if (event.getModifingFields().isModifing("FREQREAJ")|| event.getModifingFields().isModifing("DTBASEREAJ")) {
//			telaHisto.lancarHistorico(vo.asBigDecimal("NUMCONTRATO"), vo.asBigDecimal("FREQREAJ"),
//					vo.asTimestamp("DTBASEREAJ"), vlrDataOrig, vlrFrequenciaOrig, dtAlteracao, codUsuProd,
//					usuarioLogadoNome);
//			String mensagemNotificacao = "<p><h4>Foi incluído o contrato com registro "+vo.asBigDecimal("NUMCONTRATO")+"</h4></p><hr>";
//			
//			notificar.notifUsu( mensagemNotificacao, tituloNotificacao);
//
//	}
//		throw new Exception("afterUpdate");
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
//		BigDecimal codUsuProd = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUserID();
//		String usuarioLogadoNome = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUsuVO().getNOMEUSU();
//		TelaHistoricoReajuste telaHisto = new TelaHistoricoReajuste();
//		Util utl = new Util();
//		NotificacaoUser notificar = new NotificacaoUser();
//		DynamicVO vo = (DynamicVO) event.getVo();
//		JapeWrapper contraDAO = JapeFactory.dao("Contrato");
//		DynamicVO contrato = contraDAO.findByPK(vo.asBigDecimal("NUMCONTRATO"));
//		Timestamp dtAlteracao = new Timestamp(new Date().getTime());
//		String tituloNotificacao = "Inclusão de histórico";
//		Timestamp vlrDataOrig = utl.buscarDataReajuste(vo.asBigDecimal("NUMCONTRATO"));
//		BigDecimal vlrFrequenciaOrig = utl.buscarFrequencia(vo.asBigDecimal("NUMCONTRATO"));
//		
//			telaHisto.lancarHistorico(vo.asBigDecimal("NUMCONTRATO"), vo.asBigDecimal("FREQREAJ"),
//					vo.asTimestamp("DTBASEREAJ"),null , null, dtAlteracao, codUsuProd,
//					usuarioLogadoNome);
//			String mensagemNotificacao = "<p><h4>Foi incluído o contrato com registro "+vo.asBigDecimal("NUMCONTRATO")+"</h4></p><hr>";
//			
//			notificar.notifUsu( mensagemNotificacao, tituloNotificacao);

	
		
		
		
		
		
//		throw new Exception("beforeInsert");
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent event) throws Exception {
		// TODO Auto-generated method stub
		BigDecimal codUsuProd = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUserID();
		String usuarioLogadoNome = ((AuthenticationInfo) ServiceContext.getCurrent().getAutentication()).getUsuVO().getNOMEUSU();
		TelaHistoricoReajuste telaHisto = new TelaHistoricoReajuste();
		Util utl = new Util();
		NotificacaoUser notificar = new NotificacaoUser();
		DynamicVO vo = (DynamicVO) event.getVo();
		JapeWrapper contraDAO = JapeFactory.dao("Contrato");
		DynamicVO contrato = contraDAO.findByPK(vo.asBigDecimal("NUMCONTRATO"));
		Timestamp dtAlteracao = new Timestamp(new Date().getTime());
		String tituloNotificacao = "Inclusão de histórico";
		Timestamp vlrDataOrig = utl.buscarDataReajuste(vo.asBigDecimal("NUMCONTRATO"));
		BigDecimal vlrFrequenciaOrig = utl.buscarFrequencia(vo.asBigDecimal("NUMCONTRATO"));
		BigDecimal vlrcontratoOrig = utl.buscarVlrcontrato(vo.asBigDecimal("NUMCONTRATO"));
		if (event.getModifingFields().isModifing("FREQREAJ")|| event.getModifingFields().isModifing("DTBASEREAJ")|| event.getModifingFields().isModifing("AD_VLRCONTRATO")) {
			telaHisto.lancarHistorico(vo.asBigDecimal("NUMCONTRATO"), vo.asBigDecimal("FREQREAJ"),
					vo.asTimestamp("DTBASEREAJ"), vlrDataOrig, vlrFrequenciaOrig, dtAlteracao, codUsuProd,
					usuarioLogadoNome,vo.asBigDecimal("AD_VLRCONTRATO"),vlrcontratoOrig);
			String mensagemNotificacao = "<p><h4>Foi incluído o contrato com registro "+vo.asBigDecimal("NUMCONTRATO")+"</h4></p><hr>";
			
			notificar.notifUsu( mensagemNotificacao, tituloNotificacao);

	}
	}
}
