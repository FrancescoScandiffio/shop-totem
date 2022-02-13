package com.github.raffaelliscandiffio.app.dbinit;

import com.github.raffaelliscandiffio.controller.PurchaseBroker;

public interface DBInitializer {
	
	public void startDbConnection();

	public void closeDbConnection();

	public PurchaseBroker getBroker();
}
