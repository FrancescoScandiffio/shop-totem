package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;

import java.util.NoSuchElementException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.repository.OrderItemRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;

public class OrderItemMongoRepository implements OrderItemRepository {

	private static final String FIELD_ID = "_id";
	private static final String FIELD_PRODUCT = "product";
	private static final String FIELD_ORDER = "order";
	private static final String FIELD_QUANTITY = "quantity";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_PRICE = "price";
	private static final String FIELD_STATUS = "status";

	private ClientSession session;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> orderCollection;
	private MongoCollection<Document> orderItemCollection;

	public OrderItemMongoRepository(MongoClient client, ClientSession session, String databaseName,
			String productCollectionName, String orderCollectionName, String orderItemCollectionName) {
		this.session = session;
		this.productCollection = client.getDatabase(databaseName).getCollection(productCollectionName);
		this.orderCollection = client.getDatabase(databaseName).getCollection(orderCollectionName);
		this.orderItemCollection = client.getDatabase(databaseName).getCollection(orderItemCollectionName);
	}

	@Override
	public void save(OrderItem orderItem) {
		String productId = orderItem.getProduct().getId();
		if (productCollection.find(eqFilter(productId)).first() == null)
			throw new NoSuchElementException(
					"Reference error, cannot save OrderItem: Product with " + productId + " not found.");

		String orderId = orderItem.getOrder().getId();
		if (orderCollection.find(session, eqFilter(orderId)).first() == null)
			throw new NoSuchElementException(
					"Reference error, cannot save OrderItem: Order with " + orderId + " not found.");

		Document doc = new Document().append(FIELD_PRODUCT, orderItem.getProduct().getId())
				.append(FIELD_ORDER, orderItem.getOrder().getId()).append(FIELD_QUANTITY, orderItem.getQuantity());
		orderItemCollection.insertOne(session, doc);
		orderItem.setId(doc.get(FIELD_ID).toString());
	}

	@Override
	public OrderItem findById(String id) {
		Document itemDocument = orderItemCollection.find(session, eqFilter(id)).first();
		if (itemDocument == null)
			return null;
		String productId = itemDocument.getString(FIELD_PRODUCT);
		String orderId = itemDocument.getString(FIELD_ORDER);
		Document productDocument = productCollection.find(session, eqFilter(productId)).first();
		Document orderDocument = orderCollection.find(session, eqFilter(orderId)).first();
		Product product = new Product(productDocument.getString(FIELD_NAME), productDocument.getDouble(FIELD_PRICE));
		product.setId(productId);
		Order order = new Order(OrderStatus.valueOf(orderDocument.getString(FIELD_STATUS)));
		order.setId(orderId);
		OrderItem orderItem = new OrderItem(product, order, itemDocument.getInteger(FIELD_QUANTITY));
		orderItem.setId(id);
		return orderItem;
	}

	@Override
	public void delete(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(OrderItem orderItem) {
		// TODO Auto-generated method stub

	}

	private Bson eqFilter(String id) {
		return eq(FIELD_ID, new ObjectId(id));
	}

}
