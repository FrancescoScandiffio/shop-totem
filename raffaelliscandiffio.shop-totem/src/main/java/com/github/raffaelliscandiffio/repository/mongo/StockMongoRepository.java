package com.github.raffaelliscandiffio.repository.mongo;

import org.bson.Document;

import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class StockMongoRepository implements StockRepository {
	
	private MongoCollection<Document> stockCollection;

	public StockMongoRepository(MongoClient client, String databaseName, String collectionName) {
		stockCollection = client.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public Stock findById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(Stock stock) {
		// TODO Auto-generated method stub
		
	}

}
