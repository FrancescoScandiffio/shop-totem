package com.github.raffaelliscandiffio.controller;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static java.util.Arrays.asList;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.view.TotemView;

@ExtendWith(MockitoExtension.class)
class TotemControllerTest {

	@Mock
	private Order order;
	
	@Mock
	private OrderItem orderItem;

	@Mock
	private PurchaseBroker broker;

	@Mock
	private TotemView totemView;

	@InjectMocks
	private TotemController totemController;

	private static final int ZERO = 0;
	private static final int QUANTITY = 3;
	private static final int SMALLER_QUANTITY = 2;
	private Product product;

	@BeforeEach
	void setup() {
		product = new Product("name", 3);
	}

	@Test
	@DisplayName("StartShopping forwards list of products from broker to Totem View. Creates order and sets state SHOPPING.")
	void testStartShoppingForwardsBrokerProductsToTotemViewCreatesOrderAndSetsStateShopping() {

		List<Product> allProducts = asList(product);

		when(broker.getAllProducts()).thenReturn(allProducts);
		List<Product> returnedProducts = totemController.startShopping();
		
		assertThat(returnedProducts).isEqualTo(allProducts);
		assertThat(totemController.getOrderItemList()).isNotNull();
		assertThat(totemController.getOrder()).isNotNull();
		assertThat(totemController.getStatus()).isEqualTo(Status.SHOPPING);
	}

	@Test
	@DisplayName("CloseShopping forwards order products to the broker. Deletes order and sets state WELCOME.")
	void testCloseShoppingForwardsProductsToBrokerCancelsOrderAndSetsStateWelcome() {

		List<OrderItem> orderItems = new ArrayList<>();
		orderItems.add(orderItem);
		
		totemController.initOrder(order);
		totemController.initOrderItemList(orderItems);

		totemController.closeShopping();

		verify(broker).returnProducts(orderItems);
		assertThat(totemController.getOrderItemList()).isNull();
		assertThat(totemController.getOrder()).isNull();
		assertThat(totemController.getStatus()).isEqualTo(Status.WELCOME);
	}

	@Nested
	@DisplayName("BuyProductRequest tests")
	class BuyProductRequestTests {

		private Map.Entry<Product, Integer> orderProduct;

		private static final long PRODUCT_ID = 1;

		@BeforeEach
		void setup() {
			totemController.initOrder(order);
		}

		@Nested
		@DisplayName("Happy cases")
		class HappyCases {
			@Test
			@DisplayName("Requests broker to buy product with quantity. Broker returns product and quantity for order. When quantity returned is equals to requested quantity calls order insertItem with quantity and notifies totem view of correct insert.")
			void testBuyProductRequestShouldCallBrokerBuyWithRequestedQuantityAndWhenBrokerOrderedQuantityIsEqualsToRequestedQuantityCallOrderInsertItemWithRequestedQuantityAndNotifiesTotemViewOfCorrectInsert() {

				totemController.setStatus(Status.SHOPPING);
				orderProduct = new AbstractMap.SimpleEntry<Product, Integer>(product, QUANTITY);

				when(broker.buy(PRODUCT_ID, QUANTITY)).thenReturn(orderProduct);

				totemController.buyProductRequest(PRODUCT_ID, QUANTITY);
				InOrder inOrder = inOrder(order, totemView);
				inOrder.verify(order).insertItem(product, QUANTITY);
				inOrder.verify(totemView).notifyCorrectInsert(PRODUCT_ID, QUANTITY);

				verifyNoMoreInteractions(order);
			}
		}

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCases {
			@Test
			@DisplayName("Requests broker to buy product with quantity. Broker returns product and quantity for order. When quantity returned is less than requested quantity calls order insertItem with quantity and notifies totem view of partial insert.")
			void testBuyProductRequestShouldCallBrokerBuyWithRequestedQuantityAndWhenBrokerOrderedQuantityIsLessThanRequestedQuantityCallOrderInsertItemWithOrderedQuantityAndNotifiesTotemViewOfPartialInsert() {

				totemController.setStatus(Status.SHOPPING);
				orderProduct = new AbstractMap.SimpleEntry<Product, Integer>(product, SMALLER_QUANTITY);

				when(broker.buy(PRODUCT_ID, QUANTITY)).thenReturn(orderProduct);

				totemController.buyProductRequest(PRODUCT_ID, QUANTITY);
				InOrder inOrder = inOrder(order, totemView);
				inOrder.verify(order).insertItem(product, SMALLER_QUANTITY);
				inOrder.verify(totemView).notifyPartialInsert(PRODUCT_ID, SMALLER_QUANTITY);

				verifyNoMoreInteractions(order);
			}

			@Test
			@DisplayName("Requests the broker to buy product with quantity. Broker returns product and quantity for order. When quantity returned is zero notifies totem view of no insert.")
			void testBuyProductRequestShouldCallBrokerBuyWithRequestedQuantityAndWhenBrokerOrderedQuantityIsZeroNotifiesTotemViewOfNoInsert() {

				totemController.setStatus(Status.SHOPPING);
				orderProduct = new AbstractMap.SimpleEntry<Product, Integer>(product, ZERO);

				when(broker.buy(PRODUCT_ID, QUANTITY)).thenReturn(orderProduct);

				totemController.buyProductRequest(PRODUCT_ID, QUANTITY);

				verify(totemView).notifyNoInsert(PRODUCT_ID);
				verifyNoMoreInteractions(order);
			}

			@Test
			@DisplayName("Requests broker to buy product with quantity given an ID. When broker throws should notify totem view of no existing product given id.")
			void testBuyProductsShouldCallBrokerBuyAndIfBrokerThrowsShouldNotifyTotemViewOfNoExistingProduct() {

				totemController.setStatus(Status.SHOPPING);
				when(broker.buy(PRODUCT_ID, QUANTITY)).thenThrow(
						new IllegalArgumentException(String.format("Product with ID %d not found.", PRODUCT_ID)));

				totemController.buyProductRequest(PRODUCT_ID, QUANTITY);
				verify(totemView).notifyNoExistingProduct(PRODUCT_ID);
				verifyNoMoreInteractions(order);
			}

			@Test
			@DisplayName("The function throws exception if the status is WELCOME.")
			void testBuyProductsCalledOnStatusWelcomeShouldThrow() {

				totemController.setStatus(Status.WELCOME);

				assertThatThrownBy(() -> totemController.buyProductRequest(PRODUCT_ID, QUANTITY))
						.isInstanceOf(IllegalStateException.class)
						.hasMessage(String.format("Status WELCOME is not valid."));
			}
		}
	}
}