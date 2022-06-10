package com.github.raffaelliscandiffio.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.service.ShoppingService;
import com.github.raffaelliscandiffio.transaction.TransactionException;
import com.github.raffaelliscandiffio.view.TotemView;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class TotemControllerTest {

	@InjectMocks
	private TotemController totemController;

	@Mock
	private ShoppingService shoppingService;

	@Mock
	private TotemView totemView;


	@Nested
	@DisplayName("Test 'startShopping' method")
	class startShoppingTests {

		@Test
		@DisplayName("Should create an Order with status open, get the id from service layer and set it to view, also set products to view")
		void testStartShoppingShouldCreateOrderWithStatusOpenGetIdAndSetToViewGetProductsAndSetToView() {

			Order returnedOrder = new Order(OrderStatus.OPEN);
			String idReturned = "1";
			Product product = new Product("Pasta", 3);
			List<Product> productList = Arrays.asList(product);
			returnedOrder.setId(idReturned);

			ArgumentCaptor<Order> orderCaptorShoppingService = ArgumentCaptor.forClass(Order.class);
			when(shoppingService.saveOrder(any(Order.class))).thenReturn(returnedOrder);
			when(shoppingService.getAllProducts()).thenReturn(productList);

			totemController.startShopping();

			InOrder inOrder = inOrder(shoppingService, totemView);

			inOrder.verify(shoppingService, times(1)).saveOrder(orderCaptorShoppingService.capture());
			assertThat(orderCaptorShoppingService.getValue().getStatus()).isEqualTo(OrderStatus.OPEN);
			
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(totemView, times(1)).resetLabels();
			inOrder.verify(totemView, times(1)).setOrderId(idReturned);
			inOrder.verify(totemView, times(1)).showShopping();
			inOrder.verify(totemView, times(1)).showAllProducts(productList);
		}

		@Test
		@DisplayName("Should show an error and show shopping pane when save order throws exception")
		void testStartShoppingShouldShowErrorAndShowShoppingPaneWhenSaveOrderThrows() {

			String errorMessage = "Error message";

			doThrow(new TransactionException(errorMessage)).when(shoppingService).saveOrder(any(Order.class));

			totemController.startShopping();

			InOrder inOrder = Mockito.inOrder(totemView);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(totemView, times(1)).showShopping();
			inOrder.verify(totemView, times(1)).showShoppingErrorMessage(errorMessage);

			verifyNoMoreInteractions(shoppingService, totemView);
		}

		@Test
		@DisplayName("Should show an error and show shopping pane when get all products throws exception")
		void testStartShoppingShouldShowErrorAndShowShoppingPaneWhenGetAllProductsThrows() {

			String errorMessage = "Error message";

			doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();

			totemController.startShopping();

			InOrder inOrder = Mockito.inOrder(totemView);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(totemView, times(1)).showShopping();
			inOrder.verify(totemView, times(1)).showShoppingErrorMessage(errorMessage);

			verifyNoMoreInteractions(totemView);
		}
	}

	@Nested
	@DisplayName("Test 'openShopping' method")
	class openShoppingTests {
		
		@Test
		@DisplayName("Should getAllProducts from service layer, load to view and open shopping panel")
		void testOpenShoppingShouldGetAllProductsToViewAndShowShopping() {
			
			List<Product> productList = Arrays.asList(new Product("Pasta", 3));
			InOrder inOrder = Mockito.inOrder(totemView);
			when(shoppingService.getAllProducts()).thenReturn(productList);
			
			totemController.openShopping();
			
			inOrder.verify(totemView, times(1)).showShopping();
			inOrder.verify(totemView, times(1)).showAllProducts(productList);
		}
		
		@Test
		@DisplayName("Should show error message and show shopping panel when getAllProducts throws exception")
		void testOpenShoppingShouldShowErrorAndShowShoppingPanelWhenGeAllProductsThrows() {
			
			String errorMessage = "Error message";

			doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();
			
			totemController.openShopping();
			
			InOrder inOrder = Mockito.inOrder(totemView);
			inOrder.verify(totemView, times(1)).showShopping();
			inOrder.verify(totemView, times(1)).showShoppingErrorMessage(errorMessage);
			
			verifyNoMoreInteractions(totemView);
		}
	}
	
	@Nested
	@DisplayName("Test closing tests")
	class closingTests {
		
		@Test
		@DisplayName("'cancelShopping' should call delete on shopping service giving the order id, reset view, show welcome page")
		void testCancelShoppingShouldCallDeleteOnServiceLayerWithIdAndResetViewAndShowWelcome() {
			String orderId = "3";
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.cancelShopping(orderId);
			
			inOrder.verify(shoppingService, times(1)).deleteOrder(orderId);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(totemView, times(1)).resetLabels();
			inOrder.verify(totemView, times(1)).setOrderId(null);
			inOrder.verify(totemView, times(1)).showWelcome();
		}
		
		@Test
		@DisplayName("'cancelShopping' should show error when deleteOrder throws")
		void testCancelShoppingShouldShowErrorWhenDeleteOrderThrows() {
			String orderId = "3";
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			String errorMessage = "Error message";
			doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteOrder(orderId);
			
			totemController.cancelShopping(orderId);
			
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(shoppingService, totemView);
		}
		
		@Test
		@DisplayName("'checkout' should call closeOrder on shopping service giving the order id, reset view, show goodbye page")
		void testCheckoutShouldCallCloseOrderOnServiceLayerWithIdAndOrderItemsListAndResetViewAndShowGoodbye() {
			String orderId = "3";
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.checkout(orderId);
			
			inOrder.verify(shoppingService, times(1)).closeOrder(orderId);
			inOrder.verify(totemView, times(1)).setOrderId(null);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(totemView, times(1)).resetLabels();
			inOrder.verify(totemView, times(1)).showGoodbye();
		}
		
		@Test
		@DisplayName("'checkout' should set error on view when closeOrder throws")
		void testCheckoutShouldSetErrorMessageWhenCloseOrderThrows() {
			
			String errorMessage = "Error message";
			String orderId = "3";

			doThrow(new TransactionException(errorMessage)).when(shoppingService).closeOrder(any(String.class));
			
			totemController.checkout(orderId);
			
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			inOrder.verify(shoppingService, times(1)).closeOrder(orderId);
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(shoppingService, totemView);
			
		}
	}
	
	@Nested
	@DisplayName("Test returning methods")
	class ReturningMethodsTests {
		
		private String errorMessage;
		private OrderItem orderItem; 
		
		private static final int QUANTITY = 1;
		
		@BeforeEach
		void setup() {
			errorMessage = "Error message";
			orderItem = new OrderItem(new Product("pizza", 3.0), new Order(OrderStatus.OPEN), 5);

		}
		
		@Test
		@DisplayName("'removeItem' should call deleteItem with orderItem and call itemRemoved on view and show ok message")
		void testRemoveItemShouldCallDeleteItemWithOrderItemAndItemRemovedWithOrderItemAndShowMessage() {
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.removeItem(orderItem);
			
			inOrder.verify(shoppingService, times(1)).deleteItem(orderItem);
			inOrder.verify(totemView, times(1)).itemRemoved(orderItem);
			inOrder.verify(totemView, times(1)).showCartMessage("Removed all pizza");
		}
		
		@Test
		@DisplayName("'removeItem' when deleteItem throws should set error message and not delete orderItem from view")
		void testRemoveItemShouldSetErrorMessageAndNotRemoveItemFromViewWhenDeleteItemThrows() {
			String orderId = "1";
			List<Product> productList = Arrays.asList(new Product("pizza", 3));
			List<OrderItem> orderItemList = Arrays.asList(orderItem);
			
			doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteItem(any(OrderItem.class));
			when(shoppingService.getAllProducts()).thenReturn(productList);
			when(shoppingService.getOrderItems(orderId)).thenReturn(orderItemList);
			when(totemView.getOrderId()).thenReturn(orderId);
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.removeItem(orderItem);
			
			inOrder.verify(shoppingService, times(1)).deleteItem(orderItem);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(shoppingService, times(1)).getAllProducts();
			inOrder.verify(shoppingService, times(1)).getOrderItems(orderId);
			inOrder.verify(totemView, times(1)).showAllProducts(productList);
			inOrder.verify(totemView, times(1)).showAllOrderItems(orderItemList);
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView);
		}
		
		@Test
		@DisplayName("'removeItem' should resetView and set error message when deleteItem throws and getAllProducts throws")
		void testRemoveItemShouldResetViewAndSetErrorMessageWhenDeleteItemAndGetAllProductsThrows() {

			doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteItem(any(OrderItem.class));
			doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();
			
			totemController.removeItem(orderItem);
			
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(shoppingService, times(1)).getAllProducts();
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView, shoppingService);
		}
		
		@Test
		@DisplayName("'removeItem' should resetView and set error message when deleteItem throws and getOrderItems throws")
		void testRemoveItemShouldResetViewAndSetErrorMessageWhenDeleteItemAndGetOrderItemsThrows() {

			String orderId = "1";
			
			doThrow(new TransactionException(errorMessage)).when(shoppingService).deleteItem(any(OrderItem.class));
			doThrow(new TransactionException(errorMessage)).when(shoppingService).getOrderItems(orderId);
			when(totemView.getOrderId()).thenReturn(orderId);

			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.removeItem(orderItem);
			
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(shoppingService, times(1)).getAllProducts();
			inOrder.verify(shoppingService, times(1)).getOrderItems(orderId);
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView, shoppingService);
		}
		
		@Test
		@DisplayName("'returnItem' should call returnItem on shopping service with orderItem and call itemModified with new orderItem on view and show ok message")
		void testReturnItemShouldCallReturnItemOnShoppingServiceAndCallItemModifiedWithNewOrderItemAndShowMessage() {
			OrderItem orderItemModified = new OrderItem(new Product("pizza", 3.0), new Order(OrderStatus.OPEN), 3);
			
			when(shoppingService.returnItem(any(OrderItem.class), anyInt())).thenReturn(orderItemModified);
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.returnItem(orderItem, QUANTITY);
			
			inOrder.verify(shoppingService, times(1)).returnItem(orderItem, QUANTITY);
			inOrder.verify(totemView, times(1)).itemModified(orderItem, orderItemModified);
			inOrder.verify(totemView, times(1)).showCartMessage("Removed 1 pizza");
		}
		
		@Test
		@DisplayName("'returnItem' should resetView, getAllProducts, getAllOderItems, set error message when returnItem throws")
		void testReturnItemShouldResetViewGetAllProductsGetAllOrderItemsSetErrorWhenShoppingServiceReturnItemThrows() {
			String orderId = "1";
			List<Product> productList = Arrays.asList(new Product("pizza", 3));
			List<OrderItem> orderItemList = Arrays.asList(orderItem);

			
			doThrow(new TransactionException(errorMessage)).when(shoppingService).returnItem(any(OrderItem.class), anyInt());
			when(shoppingService.getAllProducts()).thenReturn(productList);
			when(shoppingService.getOrderItems(orderId)).thenReturn(orderItemList);
			when(totemView.getOrderId()).thenReturn(orderId);
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.returnItem(orderItem, QUANTITY);
			
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(shoppingService, times(1)).getAllProducts();
			inOrder.verify(shoppingService, times(1)).getOrderItems(orderId);
			inOrder.verify(totemView, times(1)).showAllProducts(productList);
			inOrder.verify(totemView, times(1)).showAllOrderItems(orderItemList);
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView);
		}
		
		@Test
		@DisplayName("'returnItem' should resetView and set error message when returnItem throws and getAllProducts throws")
		void testReturnItemShouldResetViewAndSetErrorMessageWhenReturnItemAndGetAllProductsThrows() {

			doThrow(new TransactionException(errorMessage)).when(shoppingService).returnItem(any(OrderItem.class), anyInt());
			doThrow(new TransactionException(errorMessage)).when(shoppingService).getAllProducts();
			
			totemController.returnItem(orderItem, QUANTITY);
			
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(shoppingService, times(1)).getAllProducts();
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView, shoppingService);
		}
		
		@Test
		@DisplayName("'returnItem' should resetView and set error message when returnItem throws and getOrderItems throws")
		void testReturnItemShouldResetViewAndSetErrorMessageWhenReturnItemAndGetOrderItemsThrows() {

			String orderId = "1";
			
			doThrow(new TransactionException(errorMessage)).when(shoppingService).returnItem(any(OrderItem.class), anyInt());
			doThrow(new TransactionException(errorMessage)).when(shoppingService).getOrderItems(orderId);
			when(totemView.getOrderId()).thenReturn(orderId);

			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			
			totemController.returnItem(orderItem, QUANTITY);
			
			inOrder.verify(totemView, times(1)).resetView();
			inOrder.verify(shoppingService, times(1)).getAllProducts();
			inOrder.verify(shoppingService, times(1)).getOrderItems(orderId);
			inOrder.verify(totemView, times(1)).showCartErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView, shoppingService);
		}
	}
	
	
	@Test
	@DisplayName("method 'openOrder' should call showOrder")
	void testOpenOrderShouldCallShowOrder() {
		totemController.openOrder();
		verify(totemView).showOrder();
	}
	
	@Nested
	@DisplayName("Test 'buyProduct method")
	class BuyProductTests {
		
		private static final String ORDER_ID = "1";
		private static final String PRODUCT_ID = "2";
		private static final int QUANTITY = 2;
		
		@Test
		@DisplayName("should call buyProduct on shopping service and call itemAdded with returned orderItem and show ok message")
		void testBuyProductShouldCallBuyProductOnShoppingServiceAndCallItemAddedWithReturnedOrderItemAndShowOkMessage() {

			OrderItem orderItem = new OrderItem(new Product("pizza", 3.0), new Order(OrderStatus.OPEN), 5);
			
			when(shoppingService.buyProduct(anyString(), anyString(), anyInt())).thenReturn(orderItem);
			
			totemController.buyProduct(ORDER_ID, PRODUCT_ID, QUANTITY);
			
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			inOrder.verify(shoppingService, times(1)).buyProduct(ORDER_ID, PRODUCT_ID, QUANTITY);
			inOrder.verify(totemView, times(1)).itemAdded(orderItem);
			inOrder.verify(totemView, times(1)).showShoppingMessage("Added 2 pizza");
		}
		
		@Test
		@DisplayName("should call buyProduct on shopping service and call show error message when buyProduct throws")
		void testBuyProductShouldShowErrorMessageWhenBuyProductThrows() {

			String errorMessage = "Error message";
			
			doThrow(new TransactionException(errorMessage)).when(shoppingService).buyProduct(anyString(), anyString(), anyInt());

			totemController.buyProduct(ORDER_ID, PRODUCT_ID, QUANTITY);
			
			InOrder inOrder = Mockito.inOrder(shoppingService, totemView);
			inOrder.verify(shoppingService, times(1)).buyProduct(ORDER_ID, PRODUCT_ID, QUANTITY);
			inOrder.verify(totemView, times(1)).showShoppingErrorMessage(errorMessage);
			verifyNoMoreInteractions(totemView);
		}
		
	}

}
