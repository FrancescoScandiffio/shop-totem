package com.github.raffaelliscandiffio.repository.mongo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.utils.LogUtility;
import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Document> productCollection;

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName) {
		productCollection = client.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(this::fromDocumentToProduct)
				.collect(Collectors.toList());
	}

	private Product fromDocumentToProduct(Document d) {
		return new Product(Long.valueOf("" + d.get("_id")), "" + d.get("name"), Double.valueOf("" + d.get("price")));
	}

	@Override
	public Product findById(long id) throws NoSuchElementException {
		Document d = productCollection.find(Filters.eq("_id", id)).first();
		if (d != null)
			return fromDocumentToProduct(d);
		else
			throw new NoSuchElementException(String.format("Product with id %d not found", id));
	}

	@Override
	public void save(Product product) {
		productCollection.insertOne(new Document().append("_id", product.getId()).append("name", product.getName())
				.append("price", product.getPrice()));
	}
}