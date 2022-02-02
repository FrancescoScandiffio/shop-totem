package com.github.raffaelliscandiffio.repository.mongo;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.github.raffaelliscandiffio.utils.LogUtility;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

public class StockMongoRepository implements StockRepository {

	private MongoCollection<Document> stockCollection;
	private static final Logger LOGGER = LogManager.getLogger(StockMongoRepository.class);
	private static final LogUtility logUtil = new LogUtility();

	public StockMongoRepository(MongoClient client, String databaseName, String collectionName) {
		stockCollection = client.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public Stock findById(long id) throws NoSuchElementException {
		Document d = stockCollection.find(Filters.eq("_id", id)).first();
		if (d != null)
			return new Stock(Long.valueOf("" + d.get("_id")), Integer.valueOf("" + d.get("quantity")));
		return null;
	}

	@Override
	public void save(Stock stock) {
		try {
			stockCollection.insertOne(fromStockToDocument(stock));
		} catch (MongoWriteException e) {
			LOGGER.log(Level.ERROR, "Stock with id {} already in database \n{}", stock.getId(),
					logUtil.getReducedStackTrace(e));
		}
	}

	@Override
	public void update(Stock stock) throws NoSuchElementException{
		
		UpdateResult result = stockCollection.replaceOne(Filters.eq("_id", stock.getId()), fromStockToDocument(stock));

		if (result.getModifiedCount() == 0) {
			LOGGER.log(Level.ERROR, "Stock with id {} cannot be updated because not found in database",
					stock.getId());
			throw new NoSuchElementException(String.format("Stock with id %d cannot be updated because not found in database", stock.getId()));
		}
	}
	
	private Document fromStockToDocument(Stock stock) {
		return new Document().append("_id", stock.getId()).append("quantity", stock.getQuantity());
	}
}