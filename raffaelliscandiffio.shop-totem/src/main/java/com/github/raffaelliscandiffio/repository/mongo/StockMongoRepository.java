package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import java.util.NoSuchElementException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

public class StockMongoRepository implements StockRepository {

	private static final String FIELD_ID = "_id";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_PRICE = "price";
	private static final String FIELD_PRODUCT = "product";
	private static final String FIELD_QUANTITY = "quantity";

	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;
	private ClientSession session;

	public StockMongoRepository(MongoClient client, ClientSession session, String databaseName,
			String productCollectionName, String stockCollectionName) {
		productCollection = client.getDatabase(databaseName).getCollection(productCollectionName);
		stockCollection = client.getDatabase(databaseName).getCollection(stockCollectionName);
		this.session = session;
	}

	@Override
	public void save(Stock stock) {
		String productId = stock.getProduct().getId();
		if (findProductDocumentById(productId) == null)
			throw new NoSuchElementException("Referenced Product with id " + productId + " not found.");
		Document stockDocument = new Document().append(FIELD_PRODUCT, productId).append(FIELD_QUANTITY,
				stock.getQuantity());
		stockCollection.insertOne(session, stockDocument);
		stock.setId(stockDocument.get(FIELD_ID).toString());
	}

	@Override
	public Stock findById(String id) {
		Document stockDocument = stockCollection.find(session, eqFilter(id)).first();
		if (stockDocument == null)
			return null;
		String productId = stockDocument.getString(FIELD_PRODUCT);
		Document productDocument = findProductDocumentById(productId);
		Product product = new Product(productDocument.getString(FIELD_NAME), productDocument.getDouble(FIELD_PRICE));
		product.setId(productId);
		Stock stock = new Stock(product, stockDocument.getInteger(FIELD_QUANTITY));
		stock.setId(id);
		return stock;
	}

	@Override
	public void update(Stock stock) {
		Bson update = set(FIELD_QUANTITY, stock.getQuantity());
		String id = stock.getId();
		UpdateResult result = stockCollection.updateOne(session, eqFilter(id), update);
		if (result.getMatchedCount() == 0)
			throw new NoSuchElementException("Stock with id " + id + " not found.");
	}

	private Bson eqFilter(String id) {
		return eq(FIELD_ID, new ObjectId(id));
	}

	private Document findProductDocumentById(String productId) {
		return productCollection.find(session, eqFilter(productId)).first();
	}

}