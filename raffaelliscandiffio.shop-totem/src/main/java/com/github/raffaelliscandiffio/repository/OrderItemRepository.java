package com.github.raffaelliscandiffio.repository;

import com.github.raffaelliscandiffio.model.OrderItem;

public interface OrderItemRepository {

	void save(OrderItem orderItem);

	OrderItem findById(String id);

	void delete(String id);

	void update(OrderItem orderItem);
}
