package com.github.raffaelliscandiffio.repository.mongo;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

import java.util.NoSuchElementException;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

public class OrderMongoRepository implements OrderRepository {

	private static final String FIELD_ID = "_id";
	private static final String FIELD_STATUS = "status";

	private ClientSession session;
	private MongoCollection<Document> orderCollection;
	private MongoCollection<Document> itemCollection;

	public OrderMongoRepository(MongoClient client, ClientSession session, String databaseName,
			String orderCollectionName, String itemCollection) {
		this.session = session;
		this.orderCollection = client.getDatabase(databaseName).getCollection(orderCollectionName);
		this.itemCollection = client.getDatabase(databaseName).getCollection(itemCollection);

	}

	@Override
	public void save(Order order) {
		Document orderDocument = new Document().append(FIELD_STATUS, order.getStatus().toString());
		orderCollection.insertOne(session, orderDocument);
		order.setId(orderDocument.get(FIELD_ID).toString());
	}

	@Override
	public Order findById(String id) {
		Document doc = orderCollection.find(session, eqFilter(id)).first();
		if (doc == null)
			return null;
		Order order = new Order(OrderStatus.valueOf(doc.getString(FIELD_STATUS)));
		order.setId(id);
		return order;
	}

	@Override
	public void delete(String id) {
		Document itemDoc = itemCollection.find(session, eq("order", id)).first();
		if (itemDoc != null)
			throw new IllegalStateException("Reference error: cannot delete Order with id " + id
					+ " because OrderItem with id " + itemDoc.get("_id").toString() + " has a reference to it.");
		orderCollection.deleteOne(session, eqFilter(id));
	}

	@Override
	public void update(Order order) {
		Bson update = set(FIELD_STATUS, order.getStatus().toString());
		String id = order.getId();
		UpdateResult result = orderCollection.updateOne(session, eqFilter(id), update);
		if (result.getMatchedCount() == 0)
			throw new NoSuchElementException("Order with id " + id + " not found.");

	}

	private Bson eqFilter(String id) {
		return eq(FIELD_ID, new ObjectId(id));
	}

}
