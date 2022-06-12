package com.github.raffaelliscandiffio.app.dbinit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;



public class MySqlInitializer{
	private EntityManagerFactory emf;
	private EntityManager entityManager;
	private TransactionManager transactionManager;
	
	private final Logger logger = LogManager.getLogger(MySqlInitializer.class);
	

	public void startDbConnection() {
		try {
			transactionManager = new TransactionManagerMySql(entityManager);

		} catch (Exception e) {
			logger.log(Level.ERROR, "MySQL Exception", e);
		}
	}
	

	public void closeDbConnection() {
		if (emf != null) {
			entityManager.close();
			emf.close();
			logger.log(Level.INFO, "Close entity manager and factory");
		}
	}
	

	public TransactionManager getTransactionManager() {
		return transactionManager;
	}


	public void setEntityManager(EntityManager em) {
		entityManager = em;
		
	}

}