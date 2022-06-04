package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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
		List<Document> embeddedItems = orderItemSetToDocumentList(order.getItems());
		Document orderDocument = new Document().append("items", embeddedItems).append("status",
				order.getStatus().toString());
		orderCollection.insertOne(orderDocument);
		order.setId(orderDocument.get("_id").toString());
	}

	@Override
	public Order findById(String id) {
		Document orderDocument = findDocumentById(id);
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

	@Override
	public void update(Order order) {
		String orderId = order.getId();
		if (findDocumentById(orderId) == null)
			throw new NoSuchElementException("Order with id " + orderId + " not found.");
		List<Document> embeddedItems = orderItemSetToDocumentList(order.getItems());
		Bson update = combine(set("items", embeddedItems), set("status", order.getStatus().toString()));
		orderCollection.updateOne(eqFilter(orderId), update);
	}

	private Document findDocumentById(String orderId) {
		return orderCollection.find(eqFilter(orderId)).first();
	}

	private List<Document> orderItemSetToDocumentList(Set<OrderItem> items) {
		List<Document> embeddedItems = new ArrayList<>();
		items.forEach(item -> embeddedItems.add(
				new Document().append("product", item.getProduct().getId()).append("quantity", item.getQuantity())));
		return embeddedItems;
	}

	private Bson eqFilter(String id) {
		return eq("_id", new ObjectId(id));
	}

}
