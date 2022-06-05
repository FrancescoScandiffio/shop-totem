package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.repository.StockRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;

public class StockMongoRepository implements StockRepository {

	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;

	public StockMongoRepository(MongoClient client, ClientSession session, String databaseName,
			String productCollectionName, String stockCollectionName) {
		productCollection = client.getDatabase(databaseName).getCollection(productCollectionName);
		stockCollection = client.getDatabase(databaseName).getCollection(stockCollectionName);
	}

	@Override
	public void save(Stock stock) {
		Document stockDocument = new Document().append("product", stock.getProduct().getId()).append("quantity",
				stock.getQuantity());
		stockCollection.insertOne(stockDocument);
		stock.setId(stockDocument.get("_id").toString());
	}

	@Override
	public Stock findById(String id) {
		Document stockDocument = stockCollection.find(eqFilter(id)).first();
		if (stockDocument == null)
			return null;
		String productId = stockDocument.getString("product");
		Document productDocument = productCollection.find(eqFilter(productId)).first();
		Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
		product.setId(productId);
		Stock stock = new Stock(product, stockDocument.getInteger("quantity"));
		stock.setId(id);
		return stock;
	}

	@Override
	public void update(Stock stock) {
		Document updateQuery = new Document("$set", new Document("quantity", stock.getQuantity()));
		stockCollection.updateOne(eqFilter(stock.getId()), updateQuery);
	}

	private Bson eqFilter(String id) {
		return eq("_id", new ObjectId(id));
	}
}