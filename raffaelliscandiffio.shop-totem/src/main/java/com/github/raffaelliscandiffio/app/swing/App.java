package com.github.raffaelliscandiffio.app.swing;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.controller.PurchaseBroker;
import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.repository.mongo.ProductMongoRepository;
import com.github.raffaelliscandiffio.repository.mongo.StockMongoRepository;
import com.github.raffaelliscandiffio.repository.mysql.ProductMySQLRepository;
import com.github.raffaelliscandiffio.repository.mysql.StockMySQLRepository;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class App implements Callable<Void> {

	private static final Logger LOGGER = LogManager.getLogger(App.class);

	@Option(names = { "--database" }, description = "Either 'mongo' or 'mysql'")
	private String databaseType = "mysql";

	public static void main(String[] args) {
		new CommandLine(new App()).execute(args);
	}

	@Override
	public Void call() throws Exception {

		EventQueue.invokeLater(() -> {
			PurchaseBroker broker = null;

			switch (databaseType) {
			case "mysql":
				EntityManagerFactory emf;
				EntityManager entityManager;

				try {
					emf = Persistence.createEntityManagerFactory("mysql-production");
					entityManager = emf.createEntityManager();

					ProductMySQLRepository productMySQLRepository = new ProductMySQLRepository(entityManager);
					StockMySQLRepository stockMySQLRepository = new StockMySQLRepository(entityManager);
					broker = new PurchaseBroker(productMySQLRepository, stockMySQLRepository);

				} catch (Exception e) {
					LOGGER.log(Level.ERROR, "MySQL Exception", e);
				}

				break;
			case "mongo":
				String dbName = "totem";
				try {
					MongoClient client = new MongoClient(new ServerAddress("localhost", 27017));
					// reset db at each start of the application
					client.getDatabase(dbName).drop();

					ProductMongoRepository productMongoRepository = new ProductMongoRepository(client, dbName,
							"product");
					StockMongoRepository stockMongoRepository = new StockMongoRepository(client, dbName, "stock");

					broker = new PurchaseBroker(productMongoRepository, stockMongoRepository);

				} catch (Exception e) {
					LOGGER.log(Level.ERROR, "Mongo Exception", e);
				}
				break;

			default:
				LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
				System.exit(1);
			}
			try {
				TotemSwingView totemView = new TotemSwingView();
				fillDB(broker);
				TotemController totemController = new TotemController(broker, totemView, null);
				totemView.setTotemController(totemController);
				totemView.setVisible(true);
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception", e);
			}

		});
		return null;
	}

	private void fillDB(PurchaseBroker broker) {
		// fill the database each time

		List<String[]> dataLines = new ArrayList<>();
		dataLines.add(new String[] { "1", "Pasta", "2.5", "300" });
		dataLines.add(new String[] { "2", "Pizza", "5.7", "700" });
		dataLines.add(new String[] { "3", "Broccoli", "1.5", "1000" });
		dataLines.add(new String[] { "4", "Tangerine", "1.1", "2000" });

		dataLines.stream().forEach(el -> {
			try {
				broker.saveNewProductInStock(Long.parseLong(el[0]), el[1], Double.parseDouble(el[2]),
						Integer.parseInt(el[3]));
			} catch (IllegalArgumentException ie) {
				LOGGER.log(Level.ERROR, "Illegal argument exception", ie);
			}
		});
	}
}