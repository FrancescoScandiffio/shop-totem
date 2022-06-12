package com.github.raffaelliscandiffio.repository.service;

import static com.mongodb.client.model.Filters.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionException;
import com.github.raffaelliscandiffio.transaction.mongo.TransactionManagerMongo;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

class ServiceMongoIT {

	private static final String DATABASE_NAME = "totem";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDERITEM_COLLECTION_NAME = "orderItem";

	private static final String PRODUCT_NAME_1 = "product_1";
	private static final int POSITIVE_QUANTITY = 5;
	private static final int NEGATIVE_QUANTITY = -3;
	private static final double POSITIVE_PRICE = 2.0;

	private MongoDatabase database;
	private MongoClient client;
	private ClientSession session;
	private MongoCollection<Document> productCollection;
	private MongoCollection<Document> stockCollection;

	private TransactionManagerMongo transactionManager;
	private ShoppingService serviceLayer;

	@BeforeEach()
	void setup() {
		String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
		client = MongoClients.create(uri);

		database = client.getDatabase(DATABASE_NAME);
		database.drop();
		database.createCollection(PRODUCT_COLLECTION_NAME);
		database.createCollection(STOCK_COLLECTION_NAME);
		database.createCollection(ORDER_COLLECTION_NAME);
		database.createCollection(ORDERITEM_COLLECTION_NAME);

		productCollection = database.getCollection(PRODUCT_COLLECTION_NAME);
		stockCollection = database.getCollection(STOCK_COLLECTION_NAME);

		session = client.startSession();
		transactionManager = new TransactionManagerMongo(client, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
				STOCK_COLLECTION_NAME, ORDER_COLLECTION_NAME, ORDERITEM_COLLECTION_NAME);
		serviceLayer = new ShoppingService(transactionManager);
	}

	@AfterEach
	void tearDown() {
		session.close();
		client.close();
	}

	@Test
	void testSaveProductAndStockIT() {
		Product expectedProduct = new Product(PRODUCT_NAME_1, POSITIVE_PRICE);

		serviceLayer.saveProductAndStock(PRODUCT_NAME_1, POSITIVE_PRICE, POSITIVE_QUANTITY);

		List<Product> products = getAllProducts();
		// assert only one and equal to expected, ignoring the null id
		assertThat(products).singleElement().usingRecursiveComparison().ignoringExpectedNullFields()
				.isEqualTo(expectedProduct);
		Product actualProduct = products.get(0);
		Stock expectedStock = new Stock(actualProduct, POSITIVE_QUANTITY);
		assertThat(getAllStocks()).singleElement().usingRecursiveComparison().ignoringExpectedNullFields()
				.isEqualTo(expectedStock);

	}

	@Test
	void testSaveProductAndStockWhenExceptionIsThrownIT() {
		assertThatThrownBy(() -> serviceLayer.saveProductAndStock(PRODUCT_NAME_1, POSITIVE_PRICE, NEGATIVE_QUANTITY))
				.isInstanceOf(TransactionException.class)
				.hasMessage("Quantity must be positive. Received: " + NEGATIVE_QUANTITY);
		assertThat(getAllProducts()).isEmpty();
		assertThat(getAllStocks()).isEmpty();
	}

	// Private utility methods

	private static final String _FIELD_ID = "_id";
	private static final String _FIELD_NAME = "name";
	private static final String _FIELD_PRICE = "price";
	private static final String _FIELD_PRODUCT = "product";
	private static final String _FIELD_QUANTITY = "quantity";

	private List<Product> getAllProducts() {
		return StreamSupport.stream(productCollection.find().spliterator(), false).map(productDocument -> {
			return fromDocumentToProduct(productDocument);
		}).collect(Collectors.toList());
	}

	private Product fromDocumentToProduct(Document d) {
		Product p = new Product(d.getString(_FIELD_NAME), d.getDouble(_FIELD_PRICE));
		p.setId(d.get(_FIELD_ID).toString());
		return p;
	}

	private List<Stock> getAllStocks() {
		return StreamSupport.stream(stockCollection.find().spliterator(), false).map(stockDocument -> {
			String productId = stockDocument.getString(_FIELD_PRODUCT);
			Document productDocument = productCollection.find(session, eq(_FIELD_ID, new ObjectId(productId))).first();
			Product product = fromDocumentToProduct(productDocument);
			Stock stock = new Stock(product, stockDocument.getInteger(_FIELD_QUANTITY));
			stock.setId(stockDocument.get(_FIELD_ID).toString());
			return stock;
		}).collect(Collectors.toList());
	}

}
