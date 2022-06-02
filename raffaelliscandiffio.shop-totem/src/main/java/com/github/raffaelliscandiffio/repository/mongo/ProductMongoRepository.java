package com.github.raffaelliscandiffio.repository.mongo;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.mongodb.MongoClient;
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
		Product p = new Product(d.getString("name"), d.getDouble("price"));
		p.setId(d.getString("_id"));
		return p;
	}

	@Override
	public Product findById(String id) throws NoSuchElementException {
		Document d = productCollection.find(Filters.eq("_id", id)).first();
		if (d != null)
			return fromDocumentToProduct(d);
		return null;
	}

	@Override
	public void save(Product product) {
		productCollection.insertOne(new Document().append("_id", product.getId()).append("name", product.getName())
				.append("price", product.getPrice()));
	}
}