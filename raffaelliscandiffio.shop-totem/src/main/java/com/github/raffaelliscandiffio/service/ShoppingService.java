package com.github.raffaelliscandiffio.service;

import java.util.List;

import com.github.raffaelliscandiffio.exception.RepositoryException;
import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.model.Stock;
import com.github.raffaelliscandiffio.transaction.TransactionManager;

public class ShoppingService {

	private TransactionManager transactionManager;

	public ShoppingService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Order openNewOrder() {
		return transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
					Order order = new Order(OrderStatus.OPEN);
					orderRepository.save(order);
					return order;
				});
	}

	public List<Product> getAllProducts() {
		return transactionManager.runInTransaction(
				(productRepository, stockRepository, orderRepository, itemRepository) -> productRepository.findAll());
	}

	public void deleteOrder(String orderId) {
		transactionManager.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
			if (orderRepository.findById(orderId) != null) {
				for (OrderItem item : itemRepository.getListByOrderId(orderId)) {
					Stock stock = stockRepository.findByProductId(item.getProduct().getId());
					if (stock != null) {
						stock.setQuantity(stock.getQuantity() + item.getQuantity());
						stockRepository.update(stock);
					}
					itemRepository.delete(item.getId());
				}
				orderRepository.delete(orderId);
			}
			return null;
		});
	}

	public void closeOrder(String orderId) {
		transactionManager.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
			Order storedOrder = orderRepository.findById(orderId);
			if (storedOrder == null)
				throw new RepositoryException("Order not found: " + orderId);
			storedOrder.setStatus(OrderStatus.CLOSED);
			orderRepository.update(storedOrder);
			return null;
		});
	}

	public void deleteItem(OrderItem orderItem) {
		transactionManager.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
			OrderItem repositoryItem = itemRepository.findById(orderItem.getId());
			if (repositoryItem != null) {
				Stock stock = stockRepository.findByProductId(repositoryItem.getProduct().getId());
				if (stock != null) {
					stock.setQuantity(stock.getQuantity() + repositoryItem.getQuantity());
					stockRepository.update(stock);
				}
				itemRepository.delete(orderItem.getId());
			} else
				throw new RepositoryException("Item not found: " + orderItem.getId());
			return null;
		});
	}

	public OrderItem returnItem(OrderItem orderItem, int quantityToReturn) {
		return transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
					String itemId = orderItem.getId();
					OrderItem repositoryItem = itemRepository.findById(itemId);
					if (repositoryItem == null)
						throw new RepositoryException("Item not found: " + itemId);
					if (repositoryItem != orderItem)
						throw new RepositoryException("Stale data detected in OrderItem with id " + itemId);

					String productId = orderItem.getProduct().getId();
					Stock stock = stockRepository.findByProductId(productId);

					orderItem.setQuantity(orderItem.getQuantity() - quantityToReturn);
					itemRepository.update(orderItem);
					if (stock != null) {
						stock.setQuantity(stock.getQuantity() + quantityToReturn);
						stockRepository.update(stock);
					}
					return orderItem;
				});

	}

	public List<OrderItem> getOrderItems(String orderId) {
		return transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
					if (orderRepository.findById(orderId) == null)
						throw new RepositoryException("Order with id " + orderId + " not found.");
					return itemRepository.getListByOrderId(orderId);
				});
	}

	public OrderItem buyProduct(String orderId, String productId, int purchaseQuantity) {
		return transactionManager
				.runInTransaction((productRepository, stockRepository, orderRepository, itemRepository) -> {
					Order order = orderRepository.findById(orderId);
					if (order == null)
						throw new RepositoryException("Order not found: " + orderId);
					Product product = productRepository.findById(productId);
					if (product == null)
						throw new RepositoryException("Product not found: " + productId);
					Stock stock = stockRepository.findByProductId(productId);
					if (stock == null)
						throw new RepositoryException("Stock not found. Query by product: " + productId);
					if (purchaseQuantity > stock.getQuantity())
						throw new RepositoryException("Not enough quantity. Cannot buy product: " + product.getName());

					stock.setQuantity(stock.getQuantity() - purchaseQuantity);
					stockRepository.update(stock);
					OrderItem item = itemRepository.findByProductAndOrderId(productId, orderId);
					if (item != null) {
						item.setQuantity(item.getQuantity() + purchaseQuantity);
						itemRepository.update(item);
					} else {
						item = new OrderItem(product, order, purchaseQuantity);
						itemRepository.save(item);
					}
					return item;

				});
	}

}
