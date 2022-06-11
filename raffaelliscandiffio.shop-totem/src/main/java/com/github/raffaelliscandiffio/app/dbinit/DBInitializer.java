package com.github.raffaelliscandiffio.app.dbinit;

import com.github.raffaelliscandiffio.transaction.TransactionManager;

public interface DBInitializer {
	
	public void startDbConnection();

	public void closeDbConnection();
	
	public TransactionManager getTransactionManager();

}