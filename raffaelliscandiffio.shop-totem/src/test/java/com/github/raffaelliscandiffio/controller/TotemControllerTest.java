package com.github.raffaelliscandiffio.controller;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
	@DisplayName("Test method 'buyProduct'")
	class BuyProductTest {

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
		@DisplayName("Add new product to the order when the product is not found in order")
		void testBuyProductWhenTheProductIsNotFoundInOrderShouldDelegateToOrder() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem itemToAdd = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProduct(product)).thenReturn(null);
			when(order.addNewProduct(product, QUANTITY)).thenReturn(itemToAdd);
			totemController.setOrder(order);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).itemAdded(itemToAdd);
		}

		@Test
		@DisplayName("Increase product quantity when the specified product is found in order")
		void testBuyProductWhenProductIsAlreadyInOrderShouldIncreaseItsQuantity() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem storedItem = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			OrderItem modifiedItem = new OrderItem(product, QUANTITY + GREATER_QUANTITY,
					2.5 * (QUANTITY + GREATER_QUANTITY));
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), GREATER_QUANTITY)).thenReturn(GREATER_QUANTITY);
			when(order.findItemByProduct(product)).thenReturn(storedItem);
			when(order.increaseProductQuantity(product, GREATER_QUANTITY)).thenReturn(modifiedItem);
			totemController.setOrder(order);
			totemController.buyProduct(product, GREATER_QUANTITY);
			verify(totemView).itemModified(storedItem, modifiedItem);
		}

		@Test
		@DisplayName("Show confirm message when requested quantity is available")
		void testBuyProductWhenRequestedQuantityIsAvailableShouldShowConfirmMessage() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem itemToAdd = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProduct(product)).thenReturn(null);
			when(order.addNewProduct(product, QUANTITY)).thenReturn(itemToAdd);
			totemController.setOrder(order);
			totemController.buyProduct(product, QUANTITY);
			verify(totemView).showShoppingMessage("Added " + QUANTITY + " pizza");
		}

		@Test
		@DisplayName("Show warning when provided quantity is not as much as requested")
		void testBuyProductWhenProvidedQuantityIsLessThanRequestedShouldShowWarning() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem itemToAdd = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			when(broker.doesProductExist(product.getId())).thenReturn(true);
			when(broker.takeAvailable(product.getId(), GREATER_QUANTITY)).thenReturn(QUANTITY);
			when(order.findItemByProduct(product)).thenReturn(null);
			when(order.addNewProduct(product, QUANTITY)).thenReturn(itemToAdd);
			totemController.setOrder(order);
			totemController.buyProduct(product, GREATER_QUANTITY);
			verify(totemView).showWarning("Not enough pizza in stock: added only " + QUANTITY);
		}

		@Nested
		@DisplayName("Error cases")
		class ErrorCasesTest {

			@Test
			@DisplayName("Show error when product does not exist in repository")
			void testBuyProductWhenRequestedProductDoesNotExistInRepository() {
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
				verify(totemView).showShoppingErrorMessage("Buy quantity must be positive: received -3");
				verifyNoMoreInteractions(totemView, broker, order);
			}

			@Test
			@DisplayName("Show error when requested quantity is zero")
			void testBuyProductWhenRequestedQuantityIsZero() {
				Product product = new Product(1, "pizza", 2.5);
				totemController.buyProduct(product, 0);
				verify(totemView).showShoppingErrorMessage("Buy quantity must be positive: received 0");
				verifyNoMoreInteractions(totemView, broker, order);
			}

			@Test
			@DisplayName("Show error when the specified product is null")
			void testBuyProductWhenTheSpecifiedProductIsNull() {
				totemController.buyProduct(null, QUANTITY);
				verify(totemView).showShoppingErrorMessage("Product not found");
				verifyNoMoreInteractions(totemView, broker, order);
			}

		}

	}

	@Nested
	@DisplayName("Test method 'removeItem'")
	class RemoveItemTest {

		@Test
		@DisplayName("Remove the specified item from Order")
		void testRemoveItemWhenTheLinkedProductIsFoundShouldRemoveTheItem() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem item = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			totemController.setOrder(order);
			totemController.removeItem(item);
			InOrder inOrder = inOrder(broker, order, totemView);
			inOrder.verify(order).removeProduct(product);
			inOrder.verify(broker).returnProduct(product.getId(), QUANTITY);
			inOrder.verify(totemView).itemRemoved(item);
			inOrder.verify(totemView).showCartMessage("Removed all pizza");
		}

		@Test
		@DisplayName("Show an error when the product linked to the specified item is not found in Order")
		void testRemoveItemWhenTheLinkedProductIsNotFoundShouldShowAnError() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem notExistingItem = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			doThrow(new NoSuchElementException()).when(order).removeProduct(product);
			totemController.setOrder(order);
			totemController.removeItem(notExistingItem);
			verify(totemView).showErrorItemNotFound("Item not found", notExistingItem);
			verifyNoMoreInteractions(order, broker, totemView);
		}

		@Test
		@DisplayName("Show an error when Order throws NullPointerException")
		void testRemoveItemWhenOrderThrowsNullPointerExceptionShouldShowAnError() {
			OrderItem item = new OrderItem(null, QUANTITY, 2.5 * QUANTITY);
			doThrow(new NoSuchElementException()).when(order).removeProduct(null);
			totemController.setOrder(order);
			totemController.removeItem(item);
			verify(totemView).showErrorItemNotFound("Item not found", item);
			verifyNoMoreInteractions(order, broker, totemView);
		}

	}

	@Nested
	@DisplayName("Test method 'returnProduct'")
	class ReturnProductTest {

		@Test
		@DisplayName("Return a quantity of a product from Order")
		void testReturnProduct() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem item = new OrderItem(product, GREATER_QUANTITY, 2.5 * GREATER_QUANTITY);
			int decreasedQuantity = GREATER_QUANTITY - QUANTITY;
			OrderItem modifiedItem = new OrderItem(product, decreasedQuantity, 2.5 * decreasedQuantity);
			when(order.decreaseProductQuantity(product, QUANTITY)).thenReturn(modifiedItem);
			totemController.setOrder(order);
			totemController.returnProduct(item, QUANTITY);
			InOrder inOrder = inOrder(broker, totemView);
			inOrder.verify(broker).returnProduct(product.getId(), QUANTITY);
			inOrder.verify(totemView).itemModified(item, modifiedItem);
			inOrder.verify(totemView).showCartMessage("Removed " + QUANTITY + " pizza");
		}

		@ParameterizedTest
		@ValueSource(ints = { -1, 0 })
		@DisplayName("Test return product when quantity is not positive")
		void testReturnProductWhenQuantityIsNotPositive(int input) {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem item = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			totemController.setOrder(order);
			totemController.returnProduct(item, input);
			verify(totemView).showCartErrorMessage("Input quantity must be positive: received " + input);
			verifyNoInteractions(order, broker);
		}

		@Nested
		@DisplayName("Exceptional cases")
		class ExceptionalCasesTest {

			@Test
			@DisplayName("Show error when the product to remove is not found")
			void testReturnProductWhenTheProductIsNotFoundShouldShowError() {
				Product product = new Product(1, "pizza", 2.5);
				OrderItem notExistingItem = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
				when(order.decreaseProductQuantity(product, QUANTITY)).thenThrow(new NoSuchElementException());
				totemController.setOrder(order);
				totemController.returnProduct(notExistingItem, QUANTITY);
				verify(totemView).showErrorItemNotFound("Item not found", notExistingItem);
				verifyNoMoreInteractions(order, broker, totemView);
			}

			@Test
			@DisplayName("Show error when Order throws IllegalArgumentException")
			void testReturnProductWhenOrderThrowsIllegalArgumentExceptionShouldShowAnError() {
				Product product = new Product(1, "pizza", 2.5);
				OrderItem item = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
				String exceptionMessage = "Custom exception message";
				when(order.decreaseProductQuantity(product, GREATER_QUANTITY))
						.thenThrow(new IllegalArgumentException(exceptionMessage));
				totemController.setOrder(order);
				totemController.returnProduct(item, GREATER_QUANTITY);
				verify(totemView).showCartErrorMessage(exceptionMessage);
				verifyNoMoreInteractions(order, broker, totemView);
			}

			@Test
			@DisplayName("Show error when Order throws NullPointerException")
			void testReturnProductWhenOrderThrowsNullPointerExceptionShouldShowAnError() {
				Product product = new Product(1, "pizza", 2.5);
				OrderItem item = new OrderItem(product, GREATER_QUANTITY, 2.5 * GREATER_QUANTITY);
				String exceptionMessage = "Custom exception message";
				when(order.decreaseProductQuantity(product, QUANTITY))
						.thenThrow(new NullPointerException(exceptionMessage));
				totemController.setOrder(order);
				totemController.returnProduct(item, QUANTITY);
				verify(totemView).showErrorItemNotFound(exceptionMessage, item);
				verifyNoMoreInteractions(order, broker, totemView);
			}

		}

	}

	@Nested
	@DisplayName("Test method 'cancelShopping'")
	class CancelShoppingTest {

		@Test
		@DisplayName("Cancel shopping when Order is empty")
		void testCancelShoppingWhenOrderIsEmpty() {
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(emptyList());
			totemController.cancelShopping();
			verify(totemView).showWelcome();
			verifyNoMoreInteractions(broker, order, totemView);
		}

		@Test
		@DisplayName("Cancel shopping when one item is in Order")
		void testCancelShoppingWhenOneItemIsInOrder() {
			Product product = new Product(1, "pizza", 2.5);
			OrderItem item = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(asList(item));
			totemController.cancelShopping();
			verify(order).clear();
			verify(broker).returnProduct(product.getId(), QUANTITY);
			verify(totemView).allItemsRemoved();
			verify(totemView).showWelcome();

		}

		@Test
		@DisplayName("Cancel shopping when multiple items are in Order")
		void testCancelShoppingWhenSeveralItemAreInOrder() {
			Product product1 = new Product(1, "pizza", 2.5);
			Product product2 = new Product(2, "pasta", 1.0);
			OrderItem item1 = new OrderItem(product1, QUANTITY, 2.5 * QUANTITY);
			OrderItem item2 = new OrderItem(product2, QUANTITY, 1.0 * QUANTITY);
			totemController.setOrder(order);
			when(order.getItems()).thenReturn(asList(item1, item2));
			totemController.cancelShopping();
			verify(order).clear();
			verify(broker).returnProduct(product1.getId(), QUANTITY);
			verify(broker).returnProduct(product2.getId(), QUANTITY);
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
			Product product = new Product(1, "pizza", 2.5);
			OrderItem item = new OrderItem(product, QUANTITY, 2.5 * QUANTITY);
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