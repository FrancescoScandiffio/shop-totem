package com.github.raffaelliscandiffio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.view.TotemView;

/**
 * Status of the application according to the view shown. {@link #WELCOME}
 * {@link #SHOPPING}
 */
enum Status {
	WELCOME, SHOPPING
}

public class TotemController {

	private Order order;
	private List<OrderItem> orderItemList;
	private PurchaseBroker broker;
	private TotemView totemView;
	private Status status = null;

	/**
	 * Constructs a new TotemController with the specified broker, and view
	 * 
	 * @param broker    that handles the requests to the database for all the
	 *                  TotemController(s)
	 * @param totemView is the view shown to the user
	 */
	public TotemController(PurchaseBroker broker, TotemView totemView) {
		this.broker = broker;
		this.totemView = totemView;
	}

	/**
	 * Forwards products obtained from broker to the view. Also creates a new order
	 * object and initialize it with empty OrderItem list
	 *
	 * @returns List<Product> to populate the view
	 */
	public List<Product> startShopping() {
		this.orderItemList = new ArrayList<>();
		this.order = new Order(this.orderItemList);
		this.status = Status.SHOPPING;
		return this.broker.getAllProducts();
	}

	/**
	 * Returns product added to cart to the broker deleting the order.
	 */
	public void closeShopping() {

		this.broker.returnProducts(this.orderItemList);
		this.orderItemList = null;
		this.order = null;
		this.status = Status.WELCOME;
	}

	/**
	 * Request to add the product with given productId and quantity to the order.
	 * The request is forwarded to the broker that can return:
	 * <ul>
	 * <li>Exception if the product with Id is non-existing. TotemView is notified
	 * and product not inserted in order.</li>
	 * <li>(Product, orderedQuantity) where orderedQuantity is 0 if the product is
	 * out of stock. TotemView is notified and product not inserted in order.</li>
	 * <li>(Product, orderedQuantity) where orderedQuantity is < requestedQuantity.
	 * TotemView is notified and product inserted in order with partial
	 * quantity.</li>
	 * <li>(Product, orderedQuantity) where orderedQuantity is == requestedQuantity.
	 * TotemView is notified and product inserted in order with requested
	 * quantity.</li>
	 * </ul>
	 * 
	 * @param productId         the Id of the product that is requested to be added
	 *                          to order
	 * @param requestedQuantity of the product to add to order
	 */
	public void buyProductRequest(long productId, int requestedQuantity) {

		if (this.status == Status.SHOPPING) {
			Map.Entry<Product, Integer> orderProductQuantity;
			try {
				orderProductQuantity = this.broker.buy(productId, requestedQuantity);
			} catch (IllegalArgumentException e) {
				// the product is non existing
				this.totemView.notifyNoExistingProduct(productId);
				return;
			}

			int orderedQuantity = orderProductQuantity.getValue();

			if (orderedQuantity == 0) {
				// the product is out of stock
				this.totemView.notifyNoInsert(productId);
			} else {
				// requestedQuantity >= orderedQuantity
				this.order.insertItem(orderProductQuantity.getKey(), orderedQuantity);

				if (requestedQuantity == orderedQuantity) {
					// it is possible to buy the requested quantity
					this.totemView.notifyCorrectInsert(productId, orderedQuantity);
				} else {
					// not all the requested quantity was available
					this.totemView.notifyPartialInsert(productId, orderedQuantity);
				}
			}
		} else {
			throw new IllegalStateException(String.format("Status %s is not valid.", this.getStatus()));
		}

	}


	/**
	 * Returns the order
	 * 
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}
	
	/**
	 * Returns the list of orderItem owned by Order
	 * 
	 * @return the orderItems list
	 */
	public List<OrderItem> getOrderItemList() {
		return orderItemList;
	}
	

	/**
	 * Returns the status
	 * 
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	// for testing purposes
	void setStatus(Status status) {
		this.status = status;
	}

	// for testing purposes
	void initOrder(Order order) {
		this.order = order;
	}
	
	// for testing purposes
	void initOrderItemList(List<OrderItem> orderItemList) {
		this.orderItemList = orderItemList;
	}

}