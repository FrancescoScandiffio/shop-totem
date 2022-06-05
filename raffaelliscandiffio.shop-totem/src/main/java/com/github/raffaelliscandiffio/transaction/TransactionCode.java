package com.github.raffaelliscandiffio.transaction;

import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.github.raffaelliscandiffio.repository.ProductRepository;
import com.github.raffaelliscandiffio.repository.StockRepository;

@FunctionalInterface
public interface TransactionCode<T> {

	T apply(ProductRepository productRepository, StockRepository stockRepository, OrderRepository orderRepository);

}
