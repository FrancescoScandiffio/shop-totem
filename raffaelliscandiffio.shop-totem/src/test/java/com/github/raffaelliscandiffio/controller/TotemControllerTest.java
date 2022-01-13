package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.view.TotemView;

@ExtendWith(MockitoExtension.class)
class TotemControllerTest {

	@Mock
	private PurchaseBroker broker;

	@Mock
	private TotemView totemView;

	@Mock
	private Order order;

	@InjectMocks
	private TotemController totemController;

	@Nested
	@DisplayName("Test start shopping")
	class StartShoppingTest {

		@Test
		@DisplayName("Setup an empty order and show shopping view with 'startShopping'")
		void testStartShopping() {
			List<Product> allProducts = asList(new Product("name", 1));
			when(broker.getAllProducts()).thenReturn(allProducts);
			totemController.startShopping();
			assertThat(totemController.getOrder()).isNotNull();
			InOrder inOrder = inOrder(broker, totemView);
			inOrder.verify(totemView).showShopping();
			inOrder.verify(broker).getAllProducts();
			inOrder.verify(totemView).showAllProducts(allProducts);
		}

		@Test
		@DisplayName("Show shopping view after another shopping session has been cancelled")
		void testStartShoppingWhenOrderAlreadyExists() {
			List<Product> allProducts = asList(new Product("name", 1));
			when(broker.getAllProducts()).thenReturn(allProducts);
			totemController.setOrder(order);
			totemController.startShopping();
			assertThat(totemController.getOrder()).isEqualTo(order);
			InOrder inOrder = inOrder(broker, totemView);
			inOrder.verify(totemView).showShopping();
			inOrder.verify(broker).getAllProducts();
			inOrder.verify(totemView).showAllProducts(allProducts);
		}

	}

}