package com.github.raffaelliscandiffio.transaction.mongo;

import com.github.raffaelliscandiffio.repository.mongo.OrderItemMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.OrderMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.ProductMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.StockMongoRepository;
import com.github.raffaelliscandiffio.transaction.TransactionCode;
import com.github.raffaelliscandiffio.transaction.TransactionException;
import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;

public class TransactionManagerMongo implements TransactionManager {

	private MongoClient client;
	private final String mongoDatabaseName;
	private final String productCollectionName;
	private final String stockCollectionName;
	private final String orderCollectionName;
	private final String orderItemCollectionName;
	private ClientSession session;

	public TransactionManagerMongo(MongoClient client, String dbName, String productCollectionName,
			String stockCollectionName, String orderCollectionName, String orderItemCollectionName) {
		this.client = client;
		this.mongoDatabaseName = dbName;
		this.productCollectionName = productCollectionName;
		this.stockCollectionName = stockCollectionName;
		this.orderCollectionName = orderCollectionName;
		this.orderItemCollectionName = orderItemCollectionName;

	}

	@Override
	public <T> T runInTransaction(TransactionCode<T> code) {
		try {
			session = client.startSession();
			session.startTransaction();
			T result = code.apply(new ProductMongoRepository(client, session, mongoDatabaseName, productCollectionName),
					new StockMongoRepository(client, session, mongoDatabaseName, productCollectionName,
							stockCollectionName),
					new OrderMongoRepository(client, session, mongoDatabaseName, orderCollectionName,
							orderItemCollectionName),
					new OrderItemMongoRepository(client, session, mongoDatabaseName, productCollectionName,
							orderCollectionName, orderItemCollectionName));
			session.commitTransaction();
			return result;
		} catch (Exception e) {
			throw new TransactionException(e.getMessage());
		} finally {
			session.close();
		}
	}

	ClientSession getSession() {
		return session;
	}

}
