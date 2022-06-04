package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

public class OrderMongoRepository implements OrderRepository {

	private MongoCollection<Document> orderCollection;
	private MongoCollection<Document> productCollection;

	public OrderMongoRepository(MongoClient client, String databaseName, String orderCollectionName,
			String productCollectionName) {
		orderCollection = client.getDatabase(databaseName).getCollection(orderCollectionName);
		productCollection = client.getDatabase(databaseName).getCollection(productCollectionName);
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

	@Override
	public Order findById(String id) {
		Document orderDocument = orderCollection.find(eqFilter(id)).first();
		if (orderDocument == null)
			return null;
		List<Document> itemDocuments = orderDocument.getList("items", Document.class);
		List<OrderItem> items = new ArrayList<>();
		Document productDocument;
		for (Document itemDocument : itemDocuments) {
			String productId = itemDocument.getString("product");
			productDocument = productCollection.find(eqFilter(productId)).first();
			Product product = new Product(productDocument.getString("name"), productDocument.getDouble("price"));
			product.setId(productId);
			items.add(new OrderItem(product, itemDocument.getInteger("quantity")));
		}
		Order order = new Order(new LinkedHashSet<>(items), OrderStatus.valueOf(orderDocument.getString("status")));
		order.setId(orderDocument.get("_id").toString());
		return order;
	}

	@Override
	public void delete(Order order) {
		orderCollection.deleteOne(eqFilter(order.getId()));
	}

	private Bson eqFilter(String id) {
		return eq("_id", new ObjectId(id));
	}
}
