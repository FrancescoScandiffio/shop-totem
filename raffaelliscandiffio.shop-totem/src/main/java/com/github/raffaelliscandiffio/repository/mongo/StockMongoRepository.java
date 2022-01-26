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
		else
			throw new NoSuchElementException(String.format("Stock with id %d not found", id));
	}

	@Override
	public void save(Stock stock) throws MongoWriteException {
		try {
			stockCollection
					.insertOne(new Document().append("_id", stock.getId()).append("quantity", stock.getQuantity()));
		} catch (MongoWriteException e) {
			LOGGER.log(Level.ERROR, "Stock with id {} already in database \n{}", stock.getId(),
					logUtil.getReducedStackTrace(e));
		}
	}

	@Override
	public void update(Stock stock) {
		// TODO Auto-generated method stub

	}

}
