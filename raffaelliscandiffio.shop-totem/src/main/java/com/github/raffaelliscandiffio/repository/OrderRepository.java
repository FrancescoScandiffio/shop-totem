package com.github.raffaelliscandiffio.repository;

import com.github.raffaelliscandiffio.model.Order;

public interface OrderRepository {

	void save(Order order);

	Order findById(String id);
}
