package com.github.raffaelliscandiffio.repository.mysql;

import javax.persistence.EntityManager;

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
		return entityManager.find(OrderItem.class, id);
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

}
