package com.github.raffaelliscandiffio.app.swing;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.github.raffaelliscandiffio.app.dbinit.MongoInitializer;
import com.github.raffaelliscandiffio.app.dbinit.MySqlInitializer;
import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionManager;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class App implements Callable<Void> {
	
	private static EntityManagerFactory entityManagerFactory = null;
    private static EntityManager entityManager = null;

    public static EntityManager getEntityManager(Map<String, String> settings) {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("mysql-production", settings);
            entityManager = entityManagerFactory.createEntityManager();
        }
        return entityManager;
    }

    public static void closeConnection() {
        if (entityManagerFactory != null) {
            entityManager.close();
            entityManagerFactory.close();
        }
    }

	private static final Logger LOGGER = LogManager.getLogger(App.class);

	@Option(names = { "--database" }, description = "Either 'mongo' or 'mysql'")
	private String databaseType = "mysql";

	public static void main(String[] args) {
		new CommandLine(new App()).execute(args);
	}

	TransactionManager transactionManager = null;
	
	// MySQL
	MySqlInitializer mySqlInitializer = null;
	EntityManager em = null;
	
	// Mongo
	MongoInitializer mongoInitializer = null;
	
	

	@Override
	public Void call() throws Exception {
		switch (databaseType) {
		case "mysql":
			Map<String, String> settings = new HashMap<>();
	        settings.put("javax.persistence.jdbc.url", "jdbc:mysql://" + "localhost" + ":" + 3306 + "/" + "totem");
	        settings.put("javax.persistence.jdbc.user", "root");
	        settings.put("javax.persistence.jdbc.password", "");

	        em = App.getEntityManager(settings);
	        
			break;
		case "mongo":
			
			break;

		default:
			LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
			System.exit(1);
		}
		

		EventQueue.invokeLater(() -> {
			try {

				switch (databaseType) {
				case "mysql":
					mySqlInitializer = new MySqlInitializer();
			        mySqlInitializer.setEntityManager(em);
			        mySqlInitializer.startDbConnection();
			        transactionManager = mySqlInitializer.getTransactionManager();
					break;
				case "mongo":
					mongoInitializer = new MongoInitializer();
					mongoInitializer.startDbConnection();
					transactionManager = mongoInitializer.getTransactionManager();
					break;

				default:
					LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
					System.exit(1);
				}


				TotemSwingView totemView = new TotemSwingView();
				ShoppingService shoppingService = new ShoppingService(transactionManager);
				TotemController totemController = new TotemController(shoppingService, totemView);
				shoppingService.saveProductAndStock("Bread", 1.40, 100);
				shoppingService.saveProductAndStock("Pizza", 2.20, 100);
				shoppingService.saveProductAndStock("Spaghetti", 0.80, 100);
				shoppingService.saveProductAndStock("Ice cream", 4, 100);

				totemView.setTotemController(totemController);
				totemView.setVisible(true);

			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception", e);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(App::closeConnection));
		return null;
	}

}