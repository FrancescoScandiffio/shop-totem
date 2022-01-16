package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
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

	private static final int QUANTITY = 3;
	private static final int GREATER_QUANTITY = 5;

	@Nested
	@DisplayName("Tests for 'startShopping'")
	class StartShoppingTest {

		@Test
		@DisplayName("Setup an empty order and show shopping view")
		void testStartShopping() {
			List<Product> allProducts = asList(new Product("pizza", 2.5));
			when(broker.retrieveProducts()).thenReturn(allProducts);
			totemController.startShopping();
			assertThat(totemController.getOrder()).isNotNull();
			InOrder inOrder = inOrder(totemView);
			inOrder.verify(totemView).showShopping();
			inOrder.verify(totemView).showAllProducts(allProducts);
		}

		@Test
		@DisplayName("Show shopping view after another shopping session has been cancelled")
		void testStartShoppingWhenOrderAlreadyExists() {
			List<Product> allProducts = asList(new Product("pizza", 2.5));
			when(broker.retrieveProducts()).thenReturn(allProducts);
			totemController.setOrder(order);
			totemController.startShopping();
			assertThat(totemController.getOrder()).isEqualTo(order);
			InOrder inOrder = inOrder(totemView);
			inOrder.verify(totemView).showShopping();
			inOrder.verify(totemView).showAllProducts(allProducts);
		}

	}

	@Nested
	@DisplayName("Tests for 'buyProduct'")
	class BuyProductTest {

		@Test
		@DisplayName("Show error when product does not exist")
		void testBuyProductWhenRequestedProductDoesNotExist() {
			Product product = new Product("pizza", 2.5);
			when(broker.doesProductExist(anyLong())).thenReturn(false);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).showErrorProductNotFound("Product not found", product);
			verifyNoMoreInteractions(totemView, broker, order);
		}

		@Test
		@DisplayName("Show error when requested quantity is negative")
		void testBuyProductWhenRequestedQuantityIsNegative() {
			Product product = new Product("pizza", 2.5);
			totemController.buyProduct(product, -3);
			verify(totemView).showErrorMessage("Buy quantity must be positive: received -3");
			verifyNoMoreInteractions(totemView, broker, order);
		}

		@Test
		@DisplayName("Show error when requested quantity is zero")
		void testBuyProductWhenRequestedQuantityIsZero() {
			Product product = new Product("pizza", 2.5);
			totemController.buyProduct(product, 0);
			verify(totemView).showErrorMessage("Buy quantity must be positive: received 0");
			verifyNoMoreInteractions(totemView, broker, order);
		}

		@Test
		@DisplayName("Don't buy product when it is out of stock")
		void testBuyProductWhenRequestedProductIsOutOfStock() {
			Product product = new Product("pizza", 2.5);
			totemController.setOrder(order);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(eq(product.getId()), anyInt())).thenReturn(0);
			totemController.buyProduct(product, QUANTITY);
			verify(broker).takeAvailable(product.getId(), QUANTITY);
			verify(totemView).showWarning("Item out of stock: pizza");
			verifyNoMoreInteractions(order, totemView);
		}

		@Test
		@DisplayName("Insert new item when item does not already exist")
		void testBuyProductWhenItemDoesNotExist() {
			Product product = new Product("pizza", 2.5);
			OrderItem itemToAdd = new OrderItem(product, QUANTITY);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProductId(product.getId())).thenReturn(null);
			when(order.insertItem(product, QUANTITY)).thenReturn(itemToAdd);
			totemController.setOrder(order);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).itemAdded(itemToAdd);
		}

		@Test
		@DisplayName("Modify item when the requested product is already in order")
		void testBuyProductWhenProductIsAlreadyInOrder() {
			Product product = new Product("pizza", 2.5);
			OrderItem storedItem = new OrderItem(product, QUANTITY);
			OrderItem modifiedItem = new OrderItem(product, QUANTITY * 2);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProductId(product.getId())).thenReturn(storedItem);
			when(order.insertItem(product, QUANTITY)).thenReturn(modifiedItem);

			totemController.setOrder(order);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).itemModified(storedItem, modifiedItem);
		}

		@Test
		@DisplayName("Show message when requested quantity is available")
		void testBuyProductWhenRequestedQuantityIsAvailable() {
			Product product = new Product("pizza", 2.5);
			OrderItem itemToAdd = new OrderItem(product, QUANTITY);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProductId(product.getId())).thenReturn(null);
			when(order.insertItem(product, QUANTITY)).thenReturn(itemToAdd);

			totemController.setOrder(order);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).showMessage("Added " + QUANTITY + " pizza");
		}

		@Test
		@DisplayName("Show warning when provided quantity is not as much as requested")
		void testBuyProductWhenProvidedQuantityIsLessThanRequested() {
			Product product = new Product("pizza", 2.5);
			OrderItem itemToAdd = new OrderItem(product, QUANTITY);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), GREATER_QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProductId(product.getId())).thenReturn(null);
			when(order.insertItem(product, QUANTITY)).thenReturn(itemToAdd);

			totemController.setOrder(order);
			totemController.buyProduct(product, GREATER_QUANTITY);
			verify(totemView).showWarning("Not enough pizza in stock: added only " + QUANTITY);
		}

	}

	@Nested
	@DisplayName("Tests for 'removeItem'")
	class RemoveItemTest {

		@Test
		@DisplayName("Remove item from order")
		void testRemoveItem() {
			Product product = new Product("pizza", 2.5);
			OrderItem item = new OrderItem(product, QUANTITY);
			totemController.setOrder(order);
			totemController.removeItem(item);
			InOrder inOrder = inOrder(broker, order, totemView);
			inOrder.verify(order).popItemById(item.getId());
			inOrder.verify(broker).returnProduct(product.getId(), QUANTITY);
			inOrder.verify(totemView).itemRemoved(item);
			inOrder.verify(totemView).showMessage("Removed all pizza");
		}

		@Test
		@DisplayName("Show error when item is not found in order")
		void testRemoveItemWhenItemIsNotFound() {
			OrderItem notExistingItem = new OrderItem(new Product("pizza", 2.5), QUANTITY);
			when(order.popItemById(anyLong())).thenThrow(new NoSuchElementException());
			totemController.setOrder(order);
			totemController.removeItem(notExistingItem);
			verify(totemView).showErrorItemNotFound("Item not found", notExistingItem);
			verifyNoMoreInteractions(order, broker, totemView);
		}

	}

	@Nested
	@DisplayName("Tests for 'returnProduct'")
	class ReturnProductTest {

		@Test
		@DisplayName("Return a quantity of product")
		void testReturnProduct() {
			Product product = new Product("pizza", 2.5);
			OrderItem item = new OrderItem(product, GREATER_QUANTITY);
			OrderItem modifiedItem = new OrderItem(product, QUANTITY);
			when(order.decreaseItem(item.getId(), QUANTITY)).thenReturn(modifiedItem);
			totemController.setOrder(order);
			totemController.returnProduct(item, QUANTITY);
			InOrder inOrder = inOrder(broker, order, totemView);
			inOrder.verify(order).decreaseItem(item.getId(), QUANTITY);
			inOrder.verify(broker).returnProduct(product.getId(), QUANTITY);
			inOrder.verify(totemView).itemModified(item, modifiedItem);
			inOrder.verify(totemView).showMessage("Removed 3 pizza");
		}

		@Test
		@DisplayName("Show error when item is not found")
		void testReturnProductWhenItemIsNotFound() {
			OrderItem notExistingItem = new OrderItem(new Product("pizza", 2.5), QUANTITY);
			when(order.decreaseItem(anyLong(), anyInt())).thenThrow(new NoSuchElementException());
			totemController.setOrder(order);
			totemController.returnProduct(notExistingItem, QUANTITY);
			verify(totemView).showErrorItemNotFound("Item not found", notExistingItem);
			verifyNoMoreInteractions(order, broker, totemView);
		}

		@Test
		@DisplayName("Show error when order throws IllegalArgumentException")
		void testReturnProductWhenSelectedQuantityIsIllegalArgument() {
			OrderItem existingItem = new OrderItem(new Product("pizza", 2.5), QUANTITY);
			String exceptionMessage = "Custom message";
			when(order.decreaseItem(anyLong(), anyInt())).thenThrow(new IllegalArgumentException(exceptionMessage));
			totemController.setOrder(order);
			totemController.returnProduct(existingItem, GREATER_QUANTITY);
			verify(totemView).showErrorMessage(exceptionMessage);
			verifyNoMoreInteractions(order, broker, totemView);
		}

	}

}