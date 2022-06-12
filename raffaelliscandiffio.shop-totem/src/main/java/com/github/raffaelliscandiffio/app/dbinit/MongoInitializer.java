package com.github.raffaelliscandiffio.app.dbinit;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.github.raffaelliscandiffio.transaction.mongo.TransactionManagerMongo;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoInitializer {

	private MongoClient client;
	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDERITEM_COLLECTION_NAME = "orderItem";

	private TransactionManager transactionManager;

	private final Logger logger = LogManager.getLogger(MongoInitializer.class);


	public void startDbConnection() {

		try {
			String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
			client = MongoClients.create(uri);

			MongoDatabase database = client.getDatabase(DATABASE_NAME);

			client.getDatabase(DATABASE_NAME).drop();
			database.createCollection(PRODUCT_COLLECTION_NAME);
			database.createCollection(STOCK_COLLECTION_NAME);
			database.createCollection(ORDER_COLLECTION_NAME);
			database.createCollection(ORDERITEM_COLLECTION_NAME);

			transactionManager = new TransactionManagerMongo(client, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
					STOCK_COLLECTION_NAME, ORDER_COLLECTION_NAME, ORDERITEM_COLLECTION_NAME);
			
		} catch (Exception e) {
			logger.log(Level.ERROR, "Mongo Exception", e);
		}
	}


	public void closeDbConnection() {
		logger.log(Level.INFO, "Close mongo client");
		client.close();
	}


	public TransactionManager getTransactionManager() {
		return transactionManager;
	}


}