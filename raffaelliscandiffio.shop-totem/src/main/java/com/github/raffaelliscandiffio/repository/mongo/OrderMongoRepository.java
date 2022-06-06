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
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;

public class OrderMongoRepository implements OrderRepository {

	private static final String FIELD_ID = "_id";
	private static final String FIELD_ITEMS = "items";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_PRICE = "price";
	private static final String FIELD_PRODUCT = "product";
	private static final String FIELD_QUANTITY = "quantity";
	private static final String FIELD_STATUS = "status";

	private ClientSession session;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> orderCollection;

	public OrderMongoRepository(MongoClient client, ClientSession session, String databaseName,
			String productCollectionName, String orderCollectionName) {
		this.session = session;
		productCollection = client.getDatabase(databaseName).getCollection(productCollectionName);
		orderCollection = client.getDatabase(databaseName).getCollection(orderCollectionName);
	}

	public void save(Order order) {
		List<Document> embeddedItems = orderItemSetToDocumentList(order.getItems());
		Document orderDocument = new Document().append(FIELD_ITEMS, embeddedItems).append(FIELD_STATUS,
				order.getStatus().toString());
		orderCollection.insertOne(session, orderDocument);
		order.setId(orderDocument.get(FIELD_ID).toString());
	}

	@Override
	public Order findById(String id) {
		Document orderDocument = findDocumentById(id);
		if (orderDocument == null)
			return null;
		List<Document> itemDocuments = orderDocument.getList(FIELD_ITEMS, Document.class);
		List<OrderItem> items = new ArrayList<>();
		Document productDocument;
		for (Document itemDocument : itemDocuments) {
			String productId = itemDocument.getString(FIELD_PRODUCT);
			productDocument = productCollection.find(eqFilter(productId)).first();
			Product product = new Product(productDocument.getString(FIELD_NAME),
					productDocument.getDouble(FIELD_PRICE));
			product.setId(productId);
			items.add(new OrderItem(product, itemDocument.getInteger(FIELD_QUANTITY)));
		}
		Order order = new Order(new LinkedHashSet<>(items), OrderStatus.valueOf(orderDocument.getString(FIELD_STATUS)));
		order.setId(orderDocument.get(FIELD_ID).toString());
		return order;
	}

	@Override
	public void delete(Order order) {
		orderCollection.deleteOne(session, eqFilter(order.getId()));
	}

	@Override
	public void update(Order order) {
		String orderId = order.getId();
		if (findDocumentById(orderId) == null)
			throw new NoSuchElementException("Order with id " + orderId + " not found.");
		List<Document> embeddedItems = orderItemSetToDocumentList(order.getItems());
		Bson update = combine(set(FIELD_ITEMS, embeddedItems), set(FIELD_STATUS, order.getStatus().toString()));
		orderCollection.updateOne(session, eqFilter(orderId), update);
	}

	private Document findDocumentById(String orderId) {
		return orderCollection.find(session, eqFilter(orderId)).first();
	}

	private List<Document> orderItemSetToDocumentList(Set<OrderItem> items) {
		List<Document> embeddedItems = new ArrayList<>();
		items.forEach(item -> embeddedItems.add(new Document().append(FIELD_PRODUCT, item.getProduct().getId())
				.append(FIELD_QUANTITY, item.getQuantity())));
		return embeddedItems;
	}

	private Bson eqFilter(String id) {
		return eq(FIELD_ID, new ObjectId(id));
	}

}
