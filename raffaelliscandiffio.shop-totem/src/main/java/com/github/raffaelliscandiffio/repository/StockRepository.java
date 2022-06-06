package com.github.raffaelliscandiffio.repository;

import com.github.raffaelliscandiffio.model.Stock;

public interface StockRepository {

	public Stock findById(String id);

	public void save(Stock stock);

	public void update(Stock stock);
}
