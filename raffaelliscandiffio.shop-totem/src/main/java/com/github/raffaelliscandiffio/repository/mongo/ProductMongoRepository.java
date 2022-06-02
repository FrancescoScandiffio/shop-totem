package com.github.raffaelliscandiffio.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;

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
	public void save(Product product) {
		Document productDocument = new Document().append("name", product.getName()).append("price", product.getPrice());
		productCollection.insertOne(productDocument);
		product.setId(productDocument.get("_id").toString());

	}

	@Override
	public List<Product> findAll() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(this::fromDocumentToProduct)
				.collect(Collectors.toList());
	}

	@Override
	public Product findById(String id) {
		Document d = productCollection.find(Filters.eq("_id", new ObjectId(id))).first();
		if (d != null)
			return fromDocumentToProduct(d);
		else
			return null;
	}

	private Product fromDocumentToProduct(Document d) {
		Product p = new Product(d.getString("name"), d.getDouble("price"));
		p.setId(d.get("_id").toString());
		return p;
	}
}