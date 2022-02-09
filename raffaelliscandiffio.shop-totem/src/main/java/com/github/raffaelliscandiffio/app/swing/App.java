package com.github.raffaelliscandiffio.app.swing;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.controller.PurchaseBroker;
import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.repository.mysql.ProductMySQLRepository;
import com.github.raffaelliscandiffio.repository.mysql.StockMySQLRepository;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(mixinStandardHelpOptions = true)
public class App implements Callable<Void>{
	
	private static final Logger LOGGER = LogManager.getLogger(App.class);
	
	@Option(names = { "--database" }, description = "Either 'mongo' or 'mysql'")
	private String databaseType = "mysql";
	
	
	public static void main(String[] args) {
		new CommandLine(new App()).execute(args);
	}
	
	@Override
	public Void call() throws Exception {
      
		EventQueue.invokeLater(() -> {
			if(databaseType.equals("mysql")) {
				try {
					
					EntityManagerFactory emf;
					EntityManager entityManager;
				
			        emf = Persistence.createEntityManagerFactory("mysql-production");
			        entityManager = emf.createEntityManager();
					
					ProductMySQLRepository productMySQLRepository = new ProductMySQLRepository(entityManager);
					StockMySQLRepository stockMySQLRepository = new StockMySQLRepository(entityManager);
					
					TotemSwingView totemView = new TotemSwingView(); 
					PurchaseBroker broker = new PurchaseBroker(productMySQLRepository, stockMySQLRepository);
					// fill the database each time
					broker.saveNewProductInStock(1, "Pasta", 2.5, 300);
					broker.saveNewProductInStock(2, "Pizza", 5.7, 700);
					broker.saveNewProductInStock(3, "Broccoli", 2.3, 1000);
					broker.saveNewProductInStock(4, "Tangerine", 1.1, 2000);
					
					TotemController totemController = new TotemController(broker, totemView, null); 
					totemView.setTotemController(totemController); 
					totemView.setVisible(true);
					
				} catch (Exception e) {
					LOGGER.log(Level.ERROR, "Exception", e);
				}
			}
		});
		return null;
	}
}
