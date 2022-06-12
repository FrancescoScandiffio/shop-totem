package com.github.raffaelliscandiffio.app.swing;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.app.dbinit.DBInitializer;
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

	private static final Logger LOGGER = LogManager.getLogger(App.class);

	@Option(names = { "--database" }, description = "Either 'mongo' or 'mysql'")
	private String databaseType = "mongo";

	public static void main(String[] args) {
		new CommandLine(new App()).execute(args);
	}

	TransactionManager transactionManager;
	DBInitializer dbInitializer;

	@Override
	public Void call() throws Exception {

		EventQueue.invokeLater(() -> {
			try {

				switch (databaseType) {
				case "mysql":
					dbInitializer = new MySqlInitializer();
					break;
				case "mongo":
					dbInitializer = new MongoInitializer();
					break;

				default:
					LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
					System.exit(1);
				}
				dbInitializer.startDbConnection();
				transactionManager = dbInitializer.getTransactionManager();

				TotemSwingView totemView = new TotemSwingView();
				ShoppingService shoppingService = new ShoppingService(transactionManager);
				TotemController totemController = new TotemController(shoppingService, totemView);
				totemView.setTotemController(totemController);
				totemView.setVisible(true);

			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception", e);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				dbInitializer.closeDbConnection();
			}
		});
		return null;
	}

}