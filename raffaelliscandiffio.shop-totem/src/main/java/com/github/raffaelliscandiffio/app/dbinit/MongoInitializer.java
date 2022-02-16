package com.github.raffaelliscandiffio.app.dbinit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.controller.PurchaseBroker;
import com.github.raffaelliscandiffio.repository.mongo.ProductMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.StockMongoRepository;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongoInitializer implements DBInitializer {

	private PurchaseBroker broker;
	private MongoClient client;
	private String dbName = "totem";
	
	private final Logger logger = LogManager.getLogger(MongoInitializer.class);
	
	@Override
	public void startDbConnection() {

		try {
			client = new MongoClient(new ServerAddress("localhost", 27017));
			// reset db at each start of the application
			client.getDatabase(dbName).drop();

			ProductMongoRepository productMongoRepository = new ProductMongoRepository(client, dbName, "product");
			StockMongoRepository stockMongoRepository = new StockMongoRepository(client, dbName, "stock");

			broker = new PurchaseBroker(productMongoRepository, stockMongoRepository);

		} catch (Exception e) {
			logger.log(Level.ERROR, "Mongo Exception", e);
		}
	}

	@Override
	public void closeDbConnection() {
		logger.log(Level.INFO, "Close mongo client");
		client.close();
	}

	@Override
	public PurchaseBroker getBroker() {
		return broker;
	}

}
