package com.github.raffaelliscandiffio.repository.mongo;

import java.util.Collections;
import java.util.List;

import org.bson.Document;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class ProductMongoRepository implements ProductRepository {

	private MongoCollection<Document> productCollection;

	public ProductMongoRepository(MongoClient client, String databaseName, String collectionName) {
		productCollection = client.getDatabase(databaseName).getCollection(collectionName);
	}

	@Override
	public List<Product> findAll() {
		return Collections.emptyList();
	}
	
	@Override
	public Product findById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
