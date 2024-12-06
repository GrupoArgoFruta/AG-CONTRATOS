package br.com.argo.contratos;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.sankhya.util.JdbcUtils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class Util {
	public Timestamp buscarDataReajuste(BigDecimal numcontrato) throws MGEModelException {
	    Timestamp res = null;
	    JdbcWrapper jdbc = null;
	    NativeSql sql = null;
	    ResultSet rset = null;
	    SessionHandle hnd = null;

	    try {
	        hnd = JapeSession.open();
	        hnd.setFindersMaxRows(-1);
	        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	        jdbc = entity.getJdbcWrapper();
	        jdbc.openSession();
	        sql = new NativeSql(jdbc);

	        sql.appendSql("SELECT DTBASEREAJ  FROM TCSCON WHERE NUMCONTRATO = :NUMCONTRATO");
	        sql.setNamedParameter("NUMCONTRATO", numcontrato);
	  
	        rset = sql.executeQuery();

	        while (rset.next()) {
	            res = rset.getTimestamp("DTBASEREAJ");
	        }
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	        JdbcUtils.closeResultSet(rset);
	        NativeSql.releaseResources(sql);
	        JdbcWrapper.closeSession(jdbc);
	        JapeSession.close(hnd);
	    }

	    return res;
	}
	public BigDecimal buscarFrequencia(BigDecimal numcontrato) throws MGEModelException {
	    BigDecimal res = null;
	    JdbcWrapper jdbc = null;
	    NativeSql sql = null;
	    ResultSet rset = null;
	    SessionHandle hnd = null;

	    try {
	        hnd = JapeSession.open();
	        hnd.setFindersMaxRows(-1);
	        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	        jdbc = entity.getJdbcWrapper();
	        jdbc.openSession();
	        sql = new NativeSql(jdbc);

	        sql.appendSql("SELECT FREQREAJ  FROM TCSCON WHERE NUMCONTRATO = :NUMCONTRATO");
	        sql.setNamedParameter("NUMCONTRATO", numcontrato);
	  
	        rset = sql.executeQuery();

	        while (rset.next()) {
	            res = rset.getBigDecimal("FREQREAJ");
	        }
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	        JdbcUtils.closeResultSet(rset);
	        NativeSql.releaseResources(sql);
	        JdbcWrapper.closeSession(jdbc);
	        JapeSession.close(hnd);
	    }

	    return res;
	}
	public BigDecimal buscarVlrcontrato(BigDecimal vlrcont) throws MGEModelException {
	    BigDecimal res = null;
	    JdbcWrapper jdbc = null;
	    NativeSql sql = null;
	    ResultSet rset = null;
	    SessionHandle hnd = null;

	    try {
	        hnd = JapeSession.open();
	        hnd.setFindersMaxRows(-1);
	        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
	        jdbc = entity.getJdbcWrapper();
	        jdbc.openSession();
	        sql = new NativeSql(jdbc);

	        sql.appendSql("SELECT AD_VLRCONTRATO  FROM TCSCON WHERE NUMCONTRATO = :NUMCONTRATO");
	        sql.setNamedParameter("NUMCONTRATO", vlrcont);
	  
	        rset = sql.executeQuery();

	        while (rset.next()) {
	            res = rset.getBigDecimal("AD_VLRCONTRATO");
	        }
	    } catch (Exception e) {
	        MGEModelException.throwMe(e);
	    } finally {
	        JdbcUtils.closeResultSet(rset);
	        NativeSql.releaseResources(sql);
	        JdbcWrapper.closeSession(jdbc);
	        JapeSession.close(hnd);
	    }

	    return res;
	}
}
