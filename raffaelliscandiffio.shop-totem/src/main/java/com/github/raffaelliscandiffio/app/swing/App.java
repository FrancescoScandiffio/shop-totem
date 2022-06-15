package com.github.raffaelliscandiffio.app.swing;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.TransactionException;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.github.raffaelliscandiffio.transaction.mongo.TransactionManagerMongo;
import com.github.raffaelliscandiffio.transaction.mysql.TransactionManagerMySql;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class App implements Callable<Void> {

	// shared
	private static final Logger LOGGER = LogManager.getLogger(App.class);
	private static final String DATABASE_NAME = "totem";

	// mysql
	private static final String MYSQL = "mysql";
	private static EntityManagerFactory entityManagerFactory = null;
	private static EntityManager entityManager = null;
	private TransactionManager transactionManager = null;
	private EntityManager em = null;

	// mongo
	private static final String MONGO = "mongo";
	private static final String PRODUCT_COLLECTION_NAME = "product";
	private static final String STOCK_COLLECTION_NAME = "stock";
	private static final String ORDER_COLLECTION_NAME = "order";
	private static final String ORDERITEM_COLLECTION_NAME = "orderItem";
	private static MongoClient client = null;
	private MongoClient mainClient = null;

	@Option(names = { "--database" }, description = "Either 'mongo' or 'mysql'")
	private String databaseType = MYSQL;

	public static void main(String[] args) {
		new CommandLine(new App()).execute(args);
	}

	@Override
	public Void call() throws Exception {
		switch (databaseType) {
		case MYSQL:

			Map<String, String> settings = new HashMap<>();
			settings.put("javax.persistence.jdbc.url",
					"jdbc:mysql://" + "localhost" + ":" + 3306 + "/" + DATABASE_NAME);
			settings.put("javax.persistence.jdbc.user", "root");
			settings.put("javax.persistence.jdbc.password", "");
			em = App.getEntityManager(settings);
			break;
		case MONGO:
			mainClient = App.getMongoClient();
			break;

		default:
			LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
			System.exit(1);
		}

		EventQueue.invokeLater(() -> {
			try {

				switch (databaseType) {
				case MYSQL:
					dropSqlDatabase();
					transactionManager = new TransactionManagerMySql(em);
					break;
				case MONGO:
					dropAndCreateMongoDb(Arrays.asList(PRODUCT_COLLECTION_NAME, STOCK_COLLECTION_NAME,
							ORDERITEM_COLLECTION_NAME, ORDER_COLLECTION_NAME));

					transactionManager = new TransactionManagerMongo(mainClient, DATABASE_NAME, PRODUCT_COLLECTION_NAME,
							STOCK_COLLECTION_NAME, ORDER_COLLECTION_NAME, ORDERITEM_COLLECTION_NAME);
					break;

				default:
					LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
					System.exit(1);
				}

				TotemSwingView totemView = new TotemSwingView();
				ShoppingService shoppingService = new ShoppingService(transactionManager);
				TotemController totemController = new TotemController(shoppingService, totemView);
				populateDatabase(shoppingService);

				totemView.setTotemController(totemController);
				totemView.setVisible(true);

			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception", e);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(App::closeConnection));
		return null;
	}

	public static EntityManager getEntityManager(Map<String, String> settings) {
		if (entityManagerFactory == null) {
			entityManagerFactory = Persistence.createEntityManagerFactory("mysql-production", settings);
			entityManager = entityManagerFactory.createEntityManager();
		}
		return entityManager;
	}

	public static MongoClient getMongoClient() {
		if (client == null) {
			String uri = "mongodb://localhost:27017,localhost:27018,localhost:27019/?replicaSet=rs0&readPreference=primary&ssl=false";
			client = MongoClients.create(uri);
		}
		return client;
	}

	public static void closeConnection() {
		if (entityManagerFactory != null) {
			entityManager.close();
			entityManagerFactory.close();
		} else if (client != null) {
			client.close();
		}
	}

	private void dropSqlDatabase() {
		try {
			entityManager.getTransaction().begin();
			entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
			entityManager.createQuery("DELETE FROM Stock").executeUpdate();
			entityManager.createQuery("DELETE FROM Product").executeUpdate();
			entityManager.createQuery("DELETE FROM Order").executeUpdate();
			entityManager.getTransaction().commit();
		} catch (RuntimeException e) {
			LOGGER.log(Level.ERROR, e.getMessage());
		}
	}

	private void dropAndCreateMongoDb(List<String> nameList) {
		try {
			MongoDatabase mongoDb = mainClient.getDatabase(DATABASE_NAME);
			mongoDb.drop();
			for (String name : nameList)
				mongoDb.createCollection(name);
		} catch (RuntimeException e) {
			LOGGER.log(Level.ERROR, e.getMessage());
		}
	}

	private void populateDatabase(ShoppingService shoppingService) {
		try {
			shoppingService.saveProductAndStock("Bread", 1.40, 100);
			shoppingService.saveProductAndStock("Pizza", 2.20, 100);
			shoppingService.saveProductAndStock("Spaghetti", 0.80, 100);
			shoppingService.saveProductAndStock("Ice cream", 4, 100);
		} catch (TransactionException e) {
			LOGGER.log(Level.ERROR, e.getMessage());
		}
	}

}