package com.github.raffaelliscandiffio.app.swing;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.raffaelliscandiffio.app.dbinit.DBInitializer;
import com.github.raffaelliscandiffio.app.dbinit.MongoInitializer;
import com.github.raffaelliscandiffio.app.dbinit.MySQLInitializer;
import com.github.raffaelliscandiffio.controller.PurchaseBroker;
import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.view.swing.TotemSwingView;

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

	DBInitializer dBInitializer;

	@Override
	public Void call() throws Exception {

		EventQueue.invokeLater(() -> {
			try {

				switch (databaseType) {
				case "mysql":

					dBInitializer = new MySQLInitializer();
					break;
				case "mongo":
					dBInitializer = new MongoInitializer();
					break;

				default:
					LOGGER.log(Level.ERROR, "--database must be either 'mysql' or 'mongo'");
					System.exit(1);
				}
				dBInitializer.startDbConnection();
				TotemSwingView totemView = new TotemSwingView();

				fillDB(dBInitializer.getBroker());
				
				// TODO: Create and inject Order object to TotemController
				TotemController totemController = new TotemController(dBInitializer.getBroker(), totemView, null);
				totemView.setTotemController(totemController);
				totemView.setVisible(true);
				
			} catch (Exception e) {
				LOGGER.log(Level.ERROR, "Exception", e);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				dBInitializer.closeDbConnection();
			}
		});
		return null;
	}

	private void fillDB(PurchaseBroker broker) {
		// fill the database each time

		try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/initDB.csv"))) {
			br.lines().skip(1).forEach(line -> {
				String[] values = line.split(",");
				insertProduct(broker, values);
			});
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "Exception reading file", e);
		}
	}

	private void insertProduct(PurchaseBroker broker, String[] values) {
		try {
			broker.saveNewProductInStock(Long.parseLong(values[0]), values[1], Double.parseDouble(values[2]),
					Integer.parseInt(values[3]));
		} catch (IllegalArgumentException ie) {
			LOGGER.log(Level.ERROR, "Illegal argument exception", ie);
		}
	}
}