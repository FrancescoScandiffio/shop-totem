package com.github.raffaelliscandiffio.app.dbinit;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;



public class MySqlInitializer implements DBInitializer {
	private EntityManagerFactory emf;
	private EntityManager entityManager;
	private TransactionManager transactionManager;
	
	private final Logger logger = LogManager.getLogger(MySqlInitializer.class);
	
	@Override
	public void startDbConnection() {
		try {
			emf = Persistence.createEntityManagerFactory("mysql-production");
			entityManager = emf.createEntityManager();

			transactionManager = new TransactionManagerMySql(entityManager);

		} catch (Exception e) {
			logger.log(Level.ERROR, "MySQL Exception", e);
		}
	}
	
	@Override
	public void closeDbConnection() {
		if (emf != null) {
			entityManager.close();
			emf.close();
			logger.log(Level.INFO, "Close entity manager and factory");
		}
	}
	
	@Override
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}

}