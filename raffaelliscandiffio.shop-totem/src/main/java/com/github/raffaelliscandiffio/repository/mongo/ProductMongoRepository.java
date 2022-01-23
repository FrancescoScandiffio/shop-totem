package com.github.raffaelliscandiffio.repository.mongo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
		return StreamSupport.stream(productCollection.find().spliterator(), false)
				.map(d -> new Product(Long.valueOf("" + d.get("id")), "" + d.get("name"), Double.valueOf("" + d.get("price"))))
				.collect(Collectors.toList());
	}


	@Override
	public Product findById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

}