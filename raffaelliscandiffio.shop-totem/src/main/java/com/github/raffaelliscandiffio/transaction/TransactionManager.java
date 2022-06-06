package com.github.raffaelliscandiffio.transaction;

public interface TransactionManager {

	<T> T runInTransaction(TransactionCode<T> code);

}
