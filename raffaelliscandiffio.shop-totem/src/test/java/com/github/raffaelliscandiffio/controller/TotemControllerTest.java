package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
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

import org.assertj.core.api.SoftAssertions;
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
import com.github.raffaelliscandiffio.repository.OrderRepository;
import com.github.raffaelliscandiffio.view.TotemView;

@ExtendWith(MockitoExtension.class)
class TotemControllerTest {

	@Mock
	private PurchaseBroker broker;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private TotemView totemView;

	@Mock
	private Order order;

	@InjectMocks
	private TotemController totemController;

	private static final int QUANTITY = 3;
	private static final int GREATER_QUANTITY = 5;

	@Nested
	@DisplayName("Test 'startShopping'")
	class StartShoppingTest {

		@Test
		@DisplayName("Setup an empty order, load all products and show the shopping view")
		void testStartShoppingWhenIsFirstLoading() {
			SoftAssertions softly = new SoftAssertions();
			List<Product> allProducts = asList(new Product(1, "pizza", 2.5));
			when(broker.retrieveProducts()).thenReturn(allProducts);
			totemController.startShopping();
			softly.assertThat(totemController.getOrder()).isNotNull();
			softly.assertThat(totemController.isFirstLoading()).isFalse();
			softly.assertAll();
			InOrder inOrder = inOrder(totemView);
			inOrder.verify(totemView).showAllProducts(allProducts);
			inOrder.verify(totemView).showShopping();
		}

		@Test
		@DisplayName("Do not reload products after the first call")
		void testStartShoppingWhenFirstLoadingIsFalse() {
			totemController.setFirstLoading(false);
			totemController.startShopping();
			verify(totemView).showShopping();
			verifyNoMoreInteractions(broker, totemView);

		}

		@Test
		@DisplayName("Do not replace the order when order is not null")
		void testStartShoppingWhenOrderIsNotNull() {
			totemController.setOrder(order);
			totemController.startShopping();
			assertThat(totemController.getOrder()).isEqualTo(order);

		}

	}

	@Nested
	@DisplayName("Test change view methods")
	class OpenShoppingTest {

		@Test
		@DisplayName("Show the shopping view")
		void testOpenShopping() {
			totemController.openShopping();
			verify(totemView).showShopping();
		}

		@Test
		@DisplayName("Show the order view")
		void testOpenOrder() {
			totemController.openOrder();
			verify(totemView).showOrder();
		}
	}

	@Nested
	@DisplayName("Test 'buyProduct'")
	class BuyProductTest {

		@Test
		@DisplayName("Show error when product does not exist")
		void testBuyProductWhenRequestedProductDoesNotExist() {
			Product product = new Product(1, "pizza", 2.5);
			when(broker.doesProductExist(anyLong())).thenReturn(false);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).showErrorProductNotFound("Product not found", product);
			verifyNoMoreInteractions(totemView, broker, order);
		}

		@Test
		@DisplayName("Show error when requested quantity is negative")
		void testBuyProductWhenRequestedQuantityIsNegative() {
			Product product = new Product(1, "pizza", 2.5);
			totemController.buyProduct(product, -3);
			verify(totemView).showErrorMessage("Buy quantity must be positive: received -3");
			verifyNoMoreInteractions(totemView, broker, order);
		}

		@Test
		@DisplayName("Show error when requested quantity is zero")
		void testBuyProductWhenRequestedQuantityIsZero() {
			Product product = new Product(1, "pizza", 2.5);
			totemController.buyProduct(product, 0);
			verify(totemView).showErrorMessage("Buy quantity must be positive: received 0");
			verifyNoMoreInteractions(totemView, broker, order);
		}

		@Test
		@DisplayName("Don't buy product when it is out of stock")
		void testBuyProductWhenRequestedProductIsOutOfStock() {
			Product product = new Product(1, "pizza", 2.5);
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
			Product product = new Product(1, "pizza", 2.5);
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
			Product product = new Product(1, "pizza", 2.5);
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
		@DisplayName("Show confirm message when requested quantity is available")
		void testBuyProductWhenRequestedQuantityIsAvailable() {
			Product product = new Product(1, "pizza", 2.5);
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
			Product product = new Product(1, "pizza", 2.5);
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
	@DisplayName("Test 'removeItem'")
	class RemoveItemTest {

		@Test
		@DisplayName("Remove item from order")
		void testRemoveItem() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem item = new OrderItem(product, QUANTITY);
			when(order.popItemById(item.getId())).thenReturn(item);
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
			OrderItem notExistingItem = new OrderItem(new Product(1, "pizza", 2.5), QUANTITY);
			when(order.popItemById(anyLong())).thenThrow(new NoSuchElementException());
			totemController.setOrder(order);
			totemController.removeItem(notExistingItem);
			verify(totemView).showErrorItemNotFound("Item not found", notExistingItem);
			verifyNoMoreInteractions(order, broker, totemView);
		}

	}

	@Nested
	@DisplayName("Test 'returnProduct'")
	class ReturnProductTest {

		@Test
		@DisplayName("Return a quantity of product")
		void testReturnProduct() {
			Product product = new Product(1, "pizza", 2.5);
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
			OrderItem notExistingItem = new OrderItem(new Product(1, "pizza", 2.5), QUANTITY);
			when(order.decreaseItem(anyLong(), anyInt())).thenThrow(new NoSuchElementException());
			totemController.setOrder(order);
			totemController.returnProduct(notExistingItem, QUANTITY);
			verify(totemView).showErrorItemNotFound("Item not found", notExistingItem);
			verifyNoMoreInteractions(order, broker, totemView);
		}

		@Test
		@DisplayName("Show error when order throws IllegalArgumentException")
		void testReturnProductWhenSelectedQuantityIsIllegalArgument() {
			OrderItem existingItem = new OrderItem(new Product(1, "pizza", 2.5), QUANTITY);
			String exceptionMessage = "Custom message";
			when(order.decreaseItem(anyLong(), anyInt())).thenThrow(new IllegalArgumentException(exceptionMessage));
			totemController.setOrder(order);
			totemController.returnProduct(existingItem, GREATER_QUANTITY);
			verify(totemView).showErrorMessage(exceptionMessage);
			verifyNoMoreInteractions(order, broker, totemView);
		}

	}

	@Nested
	@DisplayName("Test 'cancelShopping'")
	class CancelShoppingTest {

		@Test
		@DisplayName("Cancel shopping when no item is present")
		void testCancelShoppingWhenNoItemsArePresent() {
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(emptyList());
			totemController.cancelShopping();
			verify(totemView).showWelcome();
			verifyNoMoreInteractions(broker, order, totemView);
		}

		@Test
		@DisplayName("Cancel shopping when one item is present")
		void testCancelShoppingWhenOneItemIsPresent() {
			Product product = new Product(1, "foo", 1);
			OrderItem item = new OrderItem(product, QUANTITY);
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(asList(item));
			totemController.cancelShopping();
			verify(order).clear();
			verify(broker).returnProduct(product.getId(), QUANTITY);
			verify(totemView).allItemsRemoved();
			verify(totemView).showWelcome();

		}

		@Test
		@DisplayName("Cancel shopping when multiple items are present")
		void testCancelShoppingWhenSeveralItemArePresent() {
			Product product = new Product(1, "foo", 1);
			Product product_2 = new Product(2, "foo", 1);
			OrderItem item = new OrderItem(product, QUANTITY);
			OrderItem item_2 = new OrderItem(product_2, QUANTITY);

			totemController.setOrder(order);
			when(order.getItems()).thenReturn(asList(item, item_2));
			totemController.cancelShopping();
			verify(order).clear();
			verify(broker).returnProduct(product.getId(), QUANTITY);
			verify(broker).returnProduct(product_2.getId(), QUANTITY);
			verify(totemView).allItemsRemoved();
			verify(totemView).showWelcome();
		}

	}

	@Nested
	@DisplayName("Test 'confirmOrder'")
	class ConfirmOrderTest {

		@Test
		@DisplayName("Reset the order view, save the items and show goodbye view")
		void testConfirmOrderWhenOrderIsNotEmpty() {
			Product product = new Product(1, "foo", 1);
			OrderItem item = new OrderItem(product, QUANTITY);
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(asList(item));
			totemController.confirmOrder();
			InOrder inOrder = inOrder(totemView, orderRepository);
			inOrder.verify(orderRepository).save(order);
			inOrder.verify(totemView).showGoodbye();
			inOrder.verify(totemView).allItemsRemoved();
			assertThat(totemController.getOrder()).isNull();
		}

		@Test
		@DisplayName("Don't save order and show error when order is empty")
		void testConfirmOrderWhenOrderIsEmpty() {
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(emptyList());
			totemController.confirmOrder();
			verify(totemView).showErrorEmptyOrder("Cannot confirm an empty order");
			verifyNoMoreInteractions(totemView, order, orderRepository);
		}

	}

}