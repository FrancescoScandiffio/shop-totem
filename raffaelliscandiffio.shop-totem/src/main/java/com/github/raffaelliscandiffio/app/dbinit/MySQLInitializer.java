package com.github.raffaelliscandiffio.app.dbinit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.controller.PurchaseBroker;
import com.github.raffaelliscandiffio.repository.mysql.ProductMySQLRepository;
import com.github.raffaelliscandiffio.repository.mysql.StockMySQLRepository;

public class MySQLInitializer implements DBInitializer {
	private EntityManagerFactory emf;
	private EntityManager entityManager;
	private PurchaseBroker broker;
	
	private final Logger logger = LogManager.getLogger(MySQLInitializer.class);
	
	@Override
	public void startDbConnection() {
		try {
			emf = Persistence.createEntityManagerFactory("mysql-production");
			entityManager = emf.createEntityManager();

			ProductMySQLRepository productMySQLRepository = new ProductMySQLRepository(entityManager);
			StockMySQLRepository stockMySQLRepository = new StockMySQLRepository(entityManager);
			broker = new PurchaseBroker(productMySQLRepository, stockMySQLRepository);

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
	public PurchaseBroker getBroker() {
		return broker;
	}
}
