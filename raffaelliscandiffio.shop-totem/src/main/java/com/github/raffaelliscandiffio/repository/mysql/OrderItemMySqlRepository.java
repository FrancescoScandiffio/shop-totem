package com.github.raffaelliscandiffio.repository.mysql;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.repository.OrderItemRepository;

public class OrderItemMySqlRepository implements OrderItemRepository {

	private EntityManager entityManager;

	public OrderItemMySqlRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public void save(OrderItem orderItem) {
		entityManager.persist(orderItem);
	}

	@Override
	public OrderItem findById(String id) {
		OrderItem item = entityManager.find(OrderItem.class, id);
		return initializeSubtotal(item);
	}

	@Override
	public void delete(String id) {
		entityManager.createQuery("DELETE FROM OrderItem where id=:item_id").setParameter("item_id", id)
				.executeUpdate();
	}

	@Override
	public void update(OrderItem orderItem) {
		entityManager.merge(orderItem);
	}

	@Override
	public List<OrderItem> getListByOrderId(String orderId) {
		TypedQuery<OrderItem> query = entityManager
				.createQuery("SELECT item FROM OrderItem item WHERE item.order.id = :orderId", OrderItem.class)
				.setParameter("orderId", orderId);
		List<OrderItem> items = query.getResultList();
		for (OrderItem item : items)
			item.setSubTotal(item.getQuantity() * item.getProduct().getPrice());
		return items;
	}

	@Override
	public OrderItem findByProductAndOrderId(String productId, String orderId) {
		TypedQuery<OrderItem> query = entityManager.createQuery(
				"SELECT item FROM OrderItem item WHERE item.product.id= :productId AND item.order.id = :orderId",
				OrderItem.class).setParameter("productId", productId).setParameter("orderId", orderId);
		OrderItem item = query.getResultList().stream().findFirst().orElse(null);
		return initializeSubtotal(item);
	}

	private OrderItem initializeSubtotal(OrderItem item) {
		if (item != null)
			item.setSubTotal(item.getQuantity() * item.getProduct().getPrice());
		return item;
	}

}
