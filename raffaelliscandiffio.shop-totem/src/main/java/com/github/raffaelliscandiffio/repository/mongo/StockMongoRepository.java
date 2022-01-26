package com.github.raffaelliscandiffio.repository.mongo;

import java.util.NoSuchElementException;

import org.bson.Document;

import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class StockMongoRepository implements StockRepository {

	private MongoCollection<Document> stockCollection;

	public StockMongoRepository(MongoClient client, String databaseName, String collectionName) {
		stockCollection = client.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public Stock findById(long id) throws NoSuchElementException {
		Document d = stockCollection.find(Filters.eq("id", id)).first();
		if (d != null)
			return new Stock(Long.valueOf("" + d.get("id")), Integer.valueOf("" + d.get("quantity")));
		else
			throw new NoSuchElementException(String.format("Stock with id %d not found", id));
	}

	@Override
	public void save(Stock stock) {
		stockCollection.insertOne(new Document().append("id", stock.getId()).append("quantity", stock.getQuantity()));
	}

	@Override
	public void update(Stock stock) {
		// TODO Auto-generated method stub
		
	}

}
