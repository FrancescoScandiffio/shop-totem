package com.github.raffaelliscandiffio.repository.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class OrderMongoRepository implements OrderRepository {

	private MongoCollection<Document> orderCollection;

	public OrderMongoRepository(MongoClient client, String databaseName, String orderCollectionName) {
		orderCollection = client.getDatabase(databaseName).getCollection(orderCollectionName);
	}

	public void save(Order order) {
		List<Document> embeddedItems = new ArrayList<>();
		order.getItems().forEach(item -> embeddedItems.add(
				new Document().append("product", item.getProduct().getId()).append("quantity", item.getQuantity())));
		Document orderDocument = new Document().append("items", embeddedItems).append("status",
				order.getStatus().toString());
		orderCollection.insertOne(orderDocument);
		order.setId(orderDocument.get("_id").toString());
	}

}
