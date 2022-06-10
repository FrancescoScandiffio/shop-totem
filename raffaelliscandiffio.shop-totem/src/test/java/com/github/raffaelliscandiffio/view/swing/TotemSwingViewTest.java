package com.github.raffaelliscandiffio.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.SpinnerNumberModel;

import org.assertj.core.api.SoftAssertions;
import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JSpinnerFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.model.Order;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.OrderStatus;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.utils.GUITestExtension;

@DisplayName("Test Totem View")
@ExtendWith(GUITestExtension.class)
@ExtendWith(MockitoExtension.class)
class TotemSwingViewTest {

	private FrameFixture window;

	private TotemSwingView totemSwingView;

	private SoftAssertions softly;

	@Mock
	private TotemController totemController;

	@BeforeAll
	static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() {
		softly = new SoftAssertions();

		GuiActionRunner.execute(() -> {
			totemSwingView = new TotemSwingView();
			totemSwingView.setTotemController(totemController);
			return totemSwingView;
		});
		window = new FrameFixture(totemSwingView);
		window.show(); // shows the frame to test

	}

	@AfterEach
	void clean() throws Exception {
		window.cleanUp();
	}

	@Nested
	@DisplayName("Test the change of panels")
	class PanelShowTest {

		@Test
		@DisplayName("Method 'showShopping' should show the shopping panel")
		void testShowShoppingShouldChangePanelToShoppingPanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "welcome"));
			GuiActionRunner.execute(() -> totemSwingView.showShopping());
			softly.assertThat(totemSwingView.getShoppingPane().isShowing()).isTrue();
			softly.assertThat(totemSwingView.getWelcomePane().isShowing()).isFalse();
			softly.assertAll();
		}

		@Test
		@DisplayName("Method 'showWelcome' should show the welcome panel")
		void testShowWelcomeShouldChangePanelToWelcomePanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));

			GuiActionRunner.execute(() -> totemSwingView.showWelcome());
			softly.assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			softly.assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			softly.assertAll();
		}

		@Test
		@DisplayName("Method 'showCart' should show the cart panel")
		void testShowCartShouldChangePanelToCartPanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
			GuiActionRunner.execute(() -> totemSwingView.showOrder());
			softly.assertThat(totemSwingView.getCartPane().isShowing()).isTrue();
			softly.assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			softly.assertAll();
		}

		@Test
		@DisplayName("Method 'showGoodbye' should show the goodbye panel")
		void testShowGoodbyeShouldChangePanelToGoodbyePanel() {
			GuiActionRunner.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart"));
			GuiActionRunner.execute(() -> totemSwingView.showGoodbye());
			softly.assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
			softly.assertThat(totemSwingView.getGoodbyePane().isShowing()).isTrue();
			softly.assertAll();
		}
	}

	@Nested
	@DisplayName("Test Welcome panel")
	class WelcomePanelTest {

		@Test
		@DisplayName("Welcome panel should be the only visible panel at start")
		void testWelcomePanelShouldBeInitialPanel() {
			softly.assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			softly.assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			softly.assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
			softly.assertThat(totemSwingView.getGoodbyePane().isShowing()).isFalse();
			softly.assertAll();
		}

		@Test
		@GUITest
		@DisplayName("Welcome panel initial state")
		void testWelcomePanelInitialState() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "welcome"));
			window.button(JButtonMatcher.withName("welcomeStartShopping")).requireText("Start shopping")
					.requireEnabled();
		}

		@Test
		@DisplayName("Button 'Start Shopping' should delegate to TotemController 'startShopping'")
		void testStartShoppingButtonShouldDelegateToTotemControllerStartShopping() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "welcome"));
			window.button(JButtonMatcher.withName("welcomeStartShopping")).requireText("Start shopping").click();
			verify(totemController).startShopping();
		}

	}

	@Nested
	@DisplayName("Test Shopping panel")
	class ShoppingTest {

		@BeforeEach
		void setup() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
		}

		@Test
		@GUITest
		@DisplayName("Shopping panel inital state")
		void testShoppingPanelInitialState() {
			window.button(JButtonMatcher.withName("shopBtnCancelShopping")).requireText("Cancel Shopping")
					.requireEnabled();
			window.button(JButtonMatcher.withText("Cart")).requireEnabled();
			window.label("messageLabel").requireText(" ");
			window.list("productList");
			window.label(JLabelMatcher.withText("Quantity:"));
			window.spinner("quantitySpinner").requireDisabled();
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Method 'ShowAllProducts' should load the received products to shopping panel")
		void testShowAllProductsShouldLoadProductsFromTheListToShoppingPanel() {
			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);
			totemSwingView.showAllProducts(Arrays.asList(product1, product2));

			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €", "Product2 - Price: 3.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Method 'ShowAllProducts' should load the received products to shopping panel deleting old products")
		void testShowAllProductsShouldLoadProductsFromTheListToShoppingPanelDeletingOld() {
			Product productOld = new Product("ProductOld", 4);
			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);
			GuiActionRunner
					.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(productOld));

			totemSwingView.showAllProducts(Arrays.asList(product1, product2));

			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €", "Product2 - Price: 3.0 €");
		}

		@Test
		@DisplayName("Button 'Cancel Shopping' should delegate to TotemController 'cancelShopping' with orderId")
		void testCancelShoppingButtonShouldDelegateToTotemControllerCancelShoppingWithOrderId() {
			String orderId = "3";
			totemSwingView.setOrderId(orderId);
			window.button(JButtonMatcher.withName("shopBtnCancelShopping")).click();
			verify(totemController).cancelShopping(orderId);
		}

		@Test
		@DisplayName("Button 'Open Cart' should delegate to TotemController 'openOrder'")
		void testOpenCartButtonShouldDelegateToTotemControllerOpenOrder() {
			window.button(JButtonMatcher.withText("Cart")).click();
			verify(totemController).openOrder();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' and quantity spinner should be enabled only when a Product is selected")
		void testAddButtonAndQuantitySpinnerShouldBeEnabledOnlyWhenAProductIsSelected() {
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel()
					.addElement(new Product("Product1", 2)));

			window.list("productList").selectItem(0);
			JButtonFixture buttonAdd = window.button(JButtonMatcher.withText("Add"));
			JSpinnerFixture spinner = window.spinner("quantitySpinner");
			spinner.requireEnabled();
			buttonAdd.requireEnabled();

			window.list("productList").clearSelection();
			buttonAdd.requireDisabled();
			spinner.requireDisabled();
		}

		@GUITest
		@ParameterizedTest
		@ValueSource(strings = { "0", "01", "-1", "1 ", " ", "a", "1a", "1..", "1.1" })
		@DisplayName("Button 'Add' should be disabled when the value in spinner is not a positive integer or starts with zero")
		void testAddButtonShouldBeDisabledWhenValueInSpinnerIsNotAPositiveIntegerOrStartsWithZero(String input) {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText(input);
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is invalid and enabled when becomes a positive integer")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsInvalidAndEnabledWhenTurnsValid() {
			Product product = new Product("Product1", 2);
			JButtonFixture addButton = window.button(JButtonMatcher.withText("Add"));
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("text");
			GuiActionRunner.execute(() -> addButton.target().setEnabled(false));
			window.spinner("quantitySpinner").enterText("3");
			addButton.requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("Message label should display 'Invalid quantity' only when quantity inserted is invalid")
		void testMessageShouldDisplayInvalidQuantityErrorOnlyWhenQuantityIsInvalid() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("3");
			window.label("messageLabel").requireText(" ");
			window.spinner("quantitySpinner").enterText("-1");
			window.label("messageLabel").requireText("Invalid quantity");
			window.spinner("quantitySpinner").enterText("3");
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message label should not display 'Invalid quantity' after deselection if quantity inserted was invalid before")
		void testMessageShouldNotDisplayInvalidQuantityWhenProductIsNotSelected() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("-1");
			window.label("messageLabel").requireText("Invalid quantity");
			window.list("productList").clearSelection();
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@DisplayName("Button 'Add' should delegate to TotemController 'buyProduct'")
		void testAddButtonShouldDelegateToTotemControllerBuyProduct() {
			String orderId = "3";
			Product product = new Product("Product1", 2);
			totemSwingView.setOrderId(orderId);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));

			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterTextAndCommit("3");
			window.button(JButtonMatcher.withText("Add")).click();

			verify(totemController).buyProduct(orderId, product.getId(), 3);
		}

		@Test
		@GUITest
		@DisplayName("Method 'showShoppingErrorMessage' should show the error message in the shopping label")
		void testShowShoppingErrorMessageShouldShowTheErrorMessageInTheShoppingLabel() {
			totemSwingView.showShoppingErrorMessage("Error message");
			window.label("messageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showShoppingMessage' should show the message in the shopping label")
		void testShowShoppingMessageShouldShowTheMessageInTheShoppingLabel() {
			totemSwingView.showShoppingMessage("Message");
			window.label("messageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be reset to empty after Product deselection")
		void testMessageLabelShouldBeResetWhenProductIsDeselected() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));
			window.list("productList").clearSelection();
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be reset to empty after Product change of selection")
		void testMessageLabelShouldBeResetWhenProductIsChangedOfSelection() {
			Product product1 = new Product("Product1", 2);
			Product product2 = new Product("Product2", 3);
			totemSwingView.showAllProducts(Arrays.asList(product1, product2));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));
			window.list("productList").selectItem(1);
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be reset to empty after quantity change (to valid value)")
		void testMessageLabelShouldBeResetWhenQuantityIsChangedToValidValue() {
			Product product = new Product("Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));
			window.spinner("quantitySpinner").enterText("2");
			window.label("messageLabel").requireText(" ");
		}
	}

	@Nested
	@DisplayName("Test Cart panel")
	class CartPanelTest {

		@BeforeEach
		void setup() {
			GuiActionRunner.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart"));
		}

		@Test
		@GUITest
		@DisplayName("Cart panel inital state")
		void testCartPanelInitialState() {
			window.button(JButtonMatcher.withText("Continue Shopping")).requireEnabled();
			window.button(JButtonMatcher.withName("cartBtnCancelShopping")).requireText("Cancel Shopping")
					.requireEnabled();
			window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			window.button(JButtonMatcher.withText("Remove selected")).requireDisabled();
			window.button(JButtonMatcher.withText("Checkout")).requireDisabled();
			window.label("cartMessageLabel").requireText(" ");
			window.label(JLabelMatcher.withText("Remove selected item"));
			window.label(JLabelMatcher.withText("Quantity"));
			window.spinner("cartReturnSpinner").requireDisabled();
			window.list("cartList");
			window.spinner("cartReturnSpinner").requireDisabled();
			SpinnerNumberModel spinnerModel = (SpinnerNumberModel) (window.spinner("cartReturnSpinner").target()
					.getModel());
			assertThat(spinnerModel.getValue()).isEqualTo(1);
			assertThat((Integer) spinnerModel.getMinimum()).isEqualTo(1);
		}

		@Nested
		@DisplayName("Test swing interface methods")
		class TestSwingInterfaceImplementation {

			@Test
			@GUITest
			@DisplayName("Method 'itemAdded' should add the received OrderItem element to the cart list")
			void testItemAddedShouldAddTheOrderItemToTheCartList() {
				OrderItem orderItem1 = new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5);
				OrderItem orderItem2 = new OrderItem(new Product("Product2", 2.0), new Order(OrderStatus.OPEN), 4);

				GuiActionRunner
						.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(orderItem1));

				totemSwingView.itemAdded(orderItem2);
				String[] listContents = window.list("cartList").contents();
				assertThat(listContents).containsExactly("Product1 - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €",
						"Product2 - Quantity: 4 - Price: 2.0 € - Subtotal: 8.0 €");
			}

			@Test
			@GUITest
			@DisplayName("Method 'itemModified' should update the specified item in the cart list")
			void testItemModifiedShouldUpdateTheOldOrderItemWithTheNewOneInCartList() {
				Product product = new Product("Product2", 3.0);
				OrderItem oldItem = new OrderItem(product, new Order(OrderStatus.OPEN), 4);
				OrderItem updatedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);

				GuiActionRunner.execute(() -> {
					DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
					itemsModel.addElement(new OrderItem(new Product("Product1", 2.0), new Order(OrderStatus.OPEN), 5));
					itemsModel.addElement(oldItem);
				});
				totemSwingView.itemModified(oldItem, updatedItem);
				String[] listContents = window.list("cartList").contents();
				assertThat(listContents).containsExactly("Product1 - Quantity: 5 - Price: 2.0 € - Subtotal: 10.0 €",
						"Product2 - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €");
			}

			@Test
			@GUITest
			@DisplayName("Method 'showCartMessage' should show the message in the cart label")
			void testShowCartMessageShouldShowTheMessageInTheCartLabel() {
				totemSwingView.showCartMessage("Message");
				window.label("cartMessageLabel").requireText("Message");
			}

			@Test
			@GUITest
			@DisplayName("Method 'showCartErrorMessage' should show the error message in the cart label")
			void testShowCartErrorMessageShouldShowTheErrorMessageInTheCartLabel() {
				totemSwingView.showCartErrorMessage("Error message");
				window.label("cartMessageLabel").requireText("Error message");
			}

			@Test
			@GUITest
			@DisplayName("Method 'itemRemoved' should remove the specified OrderItem from the cart")
			void testItemRemovedShouldRemoveTheOrderItemFromTheCart() {
				OrderItem toRemove = new OrderItem(new Product("Product2", 2.0), new Order(OrderStatus.OPEN), 4);
				OrderItem existingOrderItem = new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN),
						5);

				GuiActionRunner.execute(() -> {
					DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
					itemsModel.addElement(existingOrderItem);
					itemsModel.addElement(toRemove);
				});
				totemSwingView.itemRemoved(toRemove);
				String[] listContents = window.list("cartList").contents();
				assertThat(listContents).containsExactly("Product1 - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €");
			}

			@Test
			@GUITest
			@DisplayName("Method 'resetView' should set orderId to null, empty products and cart")
			void testResetViewShouldSetOrderIdToNullAndEmptyLists() {

				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);

				GuiActionRunner.execute(() -> {
					totemSwingView.getShoppingPane().getListProductsModel().addElement(product);
					totemSwingView.getCartPane().getListOrderItemsModel().addElement(storedItem);
				});

				totemSwingView.resetView();

				GuiActionRunner.execute(
						() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
				String[] listContentsShopping = window.list("productList").contents();
				softly.assertThat(listContentsShopping).isEmpty();

				GuiActionRunner
						.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart"));
				String[] listContentsCart = window.list("cartList").contents();
				softly.assertThat(listContentsCart).isEmpty();

				softly.assertAll();
			}

			@Test
			@GUITest
			@DisplayName("Method 'resetLabels' should set labels to empty")
			void testResetLabelsShouldSetLabelsToEmpty() {

				GuiActionRunner.execute(() -> {
					totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping");
					window.label("messageLabel").target().setText("foo");

					totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart");
					window.label("cartMessageLabel").target().setText("foo");
				});

				totemSwingView.resetLabels();

				GuiActionRunner.execute(
						() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
				window.label("messageLabel").requireText(" ");
				GuiActionRunner
						.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart"));
				window.label("cartMessageLabel").requireText(" ");
			}

			@Test
			@GUITest
			@DisplayName("Method 'showAllOrderItems' should load the received order items to cart panel")
			void testshowAllOrderItemsShouldLoadOrderItemsFromTheListToCartPanel() {
				Product product = new Product("Product", 3.0);
				OrderItem orderItem1 = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem orderItem2 = new OrderItem(product, new Order(OrderStatus.OPEN), 3);
				totemSwingView.showAllOrderItems(Arrays.asList(orderItem1, orderItem2));

				String[] listContents = window.list("cartList").contents();
				assertThat(listContents).containsExactly("Product - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €",
						"Product - Quantity: 3 - Price: 3.0 € - Subtotal: 9.0 €");
			}

			@Test
			@GUITest
			@DisplayName("Method 'showAllOrderItems' should load the received order items to cart panel deleting old order items")
			void testShowAllOrderItemsShouldAddOrderItemsFromTheListToCartPanelAndDeleteOld() {
				Product product = new Product("Product", 3.0);
				OrderItem orderItem1 = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem orderItem2 = new OrderItem(product, new Order(OrderStatus.OPEN), 3);
				OrderItem orderItemOld = new OrderItem(product, new Order(OrderStatus.OPEN), 2);
				GuiActionRunner
						.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(orderItemOld));

				totemSwingView.showAllOrderItems(Arrays.asList(orderItem1, orderItem2));

				String[] listContents = window.list("cartList").contents();
				assertThat(listContents).containsExactly("Product - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €",
						"Product - Quantity: 3 - Price: 3.0 € - Subtotal: 9.0 €");
			}

		}

		@Nested
		@DisplayName("Test 'Continue Shopping' ")
		class TestContinueShopping {

			@Test
			@DisplayName("Button 'Continue Shopping' should delegate to TotemController 'openShopping'")
			void testContinueShoppingButtonShouldDelegateToTotemControllerOpenShopping() {
				window.button(JButtonMatcher.withText("Continue Shopping")).click();
				verify(totemController).openShopping();
			}
		}

		@Nested
		@DisplayName("Test 'Cancel Shopping'")
		class TestCancelShopping {

			@Test
			@DisplayName("Button 'Cancel Shopping' should delegate to TotemController 'cancelShopping' with orderId")
			void testCancelShoppingButtonShouldDelegateToTotemControllerCancelShoppingWithOrderId() {
				String orderId = "3";
				totemSwingView.setOrderId(orderId);
				window.button(JButtonMatcher.withName("cartBtnCancelShopping")).click();
				verify(totemController).cancelShopping(orderId);
			}
		}

		@Nested
		@DisplayName("Test 'Remove selected'")
		class TestRemoveSelected {

			@Test
			@GUITest
			@DisplayName("Button 'Remove selected' should be enabled only when an OrderItem is selected")
			void testRemoveSelectedButtonShouldBeEnabledOnlyWhenAnOrderItemIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				JButtonFixture buttonAdd = window.button(JButtonMatcher.withText("Remove selected"));
				buttonAdd.requireEnabled();
				window.list("cartList").clearSelection();
				buttonAdd.requireDisabled();
			}

			@Test
			@DisplayName("Button 'Remove selected' should delegate to TotemController 'removeItem'")
			void testRemoveSelectedButtonShouldDelegateToTotemControllerRemoveItem() {
				OrderItem item1 = new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5);
				OrderItem item2 = new OrderItem(new Product("Product2", 1.0), new Order(OrderStatus.OPEN), 4);
				JButtonFixture removeButton = window.button(JButtonMatcher.withText("Remove selected"));
				GuiActionRunner.execute(() -> {
					DefaultListModel<OrderItem> listItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
					listItemsModel.addElement(item1);
					listItemsModel.addElement(item2);
					removeButton.target().setEnabled(true);
				});
				window.list("cartList").selectItem(1);
				removeButton.click();
				verify(totemController).removeItem(item2);
			}

		}

		@Nested
		@DisplayName("Test Checkout")
		class TestCheckout {

			@Test
			@GUITest
			@DisplayName("Button 'Checkout' should be enabled when an item is added to the cart list")
			void testCheckoutButtonShouldBeEnabledWhenAnItemIsAddedToCartList() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.button(JButtonMatcher.withText("Checkout")).requireEnabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'Checkout' should be disabled when the cart list is empty")
			void testCheckoutButtonShouldBeDisabledWhenCartListIsEmpty() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().clear());
				window.button(JButtonMatcher.withText("Checkout")).requireDisabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'Checkout' should be enabled when several items are present and one is removed")
			void testCheckoutButtonShouldBeEnabledWhenSeveralItemsArePresentAndOneIsRemoved() {
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				JButtonFixture checkoutButton = window.button(JButtonMatcher.withText("Checkout"));
				GuiActionRunner.execute(() -> {
					itemsModel.addElement(new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5));
					itemsModel.addElement(new OrderItem(new Product("Product2", 3.0), new Order(OrderStatus.OPEN), 5));
					checkoutButton.target().setEnabled(true);
				});

				GuiActionRunner.execute(() -> itemsModel.removeElementAt(0));
				checkoutButton.requireEnabled();
			}

			@Test
			@DisplayName("Button 'Checkout' should delegate to TotemController 'confirmOrder' with OrderId and OrderItems")
			void testCheckoutButtonShouldDelegateToTotemControllerConfirmOrder() {
				String orderId = "2";
				JButtonFixture checkoutButton = window.button(JButtonMatcher.withText("Checkout"));
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				OrderItem orderItem1 = new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5);
				OrderItem orderItem2 = new OrderItem(new Product("Product2", 3.0), new Order(OrderStatus.OPEN), 5);
				totemSwingView.setOrderId(orderId);
				GuiActionRunner.execute(() -> {
					itemsModel.addElement(orderItem1);
					itemsModel.addElement(orderItem2);
				});

				GuiActionRunner.execute(() -> checkoutButton.target().setEnabled(true));
				checkoutButton.click();
				verify(totemController).checkout(orderId, Arrays.asList(orderItem1, orderItem2));
			}
		}

		@Nested
		@DisplayName("Test cart spinner")
		class TestCartSpinner {

			@Test
			@GUITest
			@DisplayName("The spinner should be enabled when an item is selected and the item quantity is greater than one")
			void testTheSpinnerShouldBeEnabledWhenAnItemWithQuantityGreaterThanOneIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").requireEnabled();
			}

			@Test
			@GUITest
			@DisplayName("The spinner should be disabled when an item is selected and the item quantity is one")
			void testTheSpinnerShouldBeDisabeldWhenAnItemWithQuantityEqualToOneIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 1)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").requireDisabled();
			}

			@Test
			@GUITest
			@DisplayName("The spinner should be disabled when an item is deselected")
			void testTheSpinnerShouldBeDisabeldWhenAnItemIsDeselected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product1", 3), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.list("cartList").clearSelection();
				window.spinner("cartReturnSpinner").requireDisabled();
			}

			@Test
			@DisplayName("The spinner value should be reset to one when the selection changes")
			void testTheSpinnerValueShouldBeResetToOneWhenAnItemIsSelected() {
				JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
				GuiActionRunner.execute(() -> {
					totemSwingView.getCartPane().getListOrderItemsModel()
							.addElement(new OrderItem(new Product("Product1", 3), new Order(OrderStatus.OPEN), 5));
					totemSwingView.getCartPane().getListOrderItemsModel()
							.addElement(new OrderItem(new Product("Product2", 3), new Order(OrderStatus.OPEN), 5));
				});
				window.list("cartList").selectItem(0);
				cartSpinner.enterTextAndCommit("2");
				window.list("cartList").selectItem(1);
				cartSpinner.requireValue(1);
			}

			@Test
			@DisplayName("The spinner value should be reset to one when an item is deselected")
			void testTheSpinnerValueShouldBeResetToOneWhenAnItemIsDeselected() {
				JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				cartSpinner.enterTextAndCommit("2");
				window.list("cartList").clearSelection();
				cartSpinner.requireValue(1);
			}

			@Test
			@DisplayName("The spinner value should be reset to one when an item is selected and the item quantity changes to one")
			void testTheSpinnerValueShouldBeResetToOneWhenAnItemIsSelectedAndTheItemQuantityChangesToOne() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 10);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
				GuiActionRunner.execute(() -> listOrderItemsModel.addElement(storedItem));
				window.list("cartList").selectItem(0);
				cartSpinner.enterText("2");
				GuiActionRunner.execute(() -> listOrderItemsModel.setElementAt(modifiedItem, 0));
				cartSpinner.requireValue(1);
			}

			@Test
			@DisplayName("The spinner value should not be reset to one when an item is selected and another item quantity changes to one")
			void testTheSpinnerValueShouldNotBeResetToOneWhenAnItemIsSelectedAndAnotherItemQuantityChangesToOne() {
				Product product = new Product("Product2", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
				GuiActionRunner.execute(() -> {
					listOrderItemsModel
							.addElement(new OrderItem(new Product("Product1", 3.0), new Order(OrderStatus.OPEN), 5));
					listOrderItemsModel.addElement(storedItem);
				});
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterTextAndCommit("3");
				GuiActionRunner.execute(() -> listOrderItemsModel.setElementAt(modifiedItem, 1));
				cartSpinner.requireValue(3);
			}

			@Test
			@DisplayName("The spinner should be disabled when an item is selected and the item quantity changes to one")
			void testTheSpinnerShouldBeDisabledWhenAnItemIsSelectedAndTheItemQuantityChangesToOne() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 10);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
				GuiActionRunner.execute(() -> listOrderItemsModel.addElement(storedItem));
				window.list("cartList").selectItem(0);
				cartSpinner.enterTextAndCommit("2");
				GuiActionRunner.execute(() -> listOrderItemsModel.setElementAt(modifiedItem, 0));
				cartSpinner.requireDisabled();
			}

			@Test
			@DisplayName("The spinner should be enabled when an item with value one is selected and the item quantity increases")
			void testTheSpinnerShouldBeEnabledWhenAnItemWithQuantityOneIsSelectedAndTheItemQuantityIncreases() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
				GuiActionRunner.execute(() -> listOrderItemsModel.addElement(storedItem));
				window.list("cartList").selectItem(0);
				GuiActionRunner.execute(() -> listOrderItemsModel.setElementAt(modifiedItem, 0));
				cartSpinner.requireEnabled();
			}
		}

		@Nested
		@DisplayName("Test button 'return quantity'")
		class TestReturnQuantityButton {

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should be enabled when an item is selected and the item quantity is greater than one")
			void testButtonReturnQuantityShouldBeEnabledWhenAnItemWithQuantityGreaterThanOneIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.button(JButtonMatcher.withText("Return quantity")).requireEnabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should be disabled when an item is selected and the item quantity is one")
			void testButtonReturnQuantityShouldBeDisabledWhenAnItemWithQuantityEqualToOneIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 1)));
				window.list("cartList").selectItem(0);
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should be disabled when no item is selected")
			void testButtonReturnQuantityShouldBeDisabledWhenNoItemIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.list("cartList").clearSelection();
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

			@GUITest
			@ParameterizedTest
			@ValueSource(strings = { "0", "01", "-1", " ", "a", "1a", "1.1" })
			@DisplayName("Button 'return quantity' should be disabled when the value in spinner is not a positive integer or starts with zero")
			void testReturnQuantityButtonShouldBeDisabledWhenValueInSpinnerIsNotAPositiveIntegerOrStartsWithZero(
					String input) {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText(input);
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should be enabled when an item is selected and the spinner text is less than the item quantity")
			void testReturnQuantityButtonShouldBeEnabledWhenAnItemIsSelectedAndSpinnerTextIsLessThanTheSelectedItemQuantity() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("4");
				window.button(JButtonMatcher.withText("Return quantity")).requireEnabled();
			}

			@ParameterizedTest
			@ValueSource(strings = { "5", "6" })
			@DisplayName("Button 'return quantity' should be disabled when an item is selected and the spinner text is equal to or greater than the item quantity")
			void testReturnQuantityButtonShouldBeDisabledWhenAnItemIsSelectedAndSpinnerTextIsEqualToOrGreaterThanTheSelectedItemQuantity(
					String input) {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText(input);
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

			@Test
			@DisplayName("Button 'return quantity' should delegate to TotemController 'returnProduct'")
			void testButtonReturnQuantityShouldDelegateToTotemControllerReturnQuantity() {
				OrderItem itemToReturn = new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5);
				GuiActionRunner
						.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(itemToReturn));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterTextAndCommit("3");
				window.button(JButtonMatcher.withText("Return quantity")).click();
				verify(totemController).returnItem(itemToReturn, 3);
			}

			@ParameterizedTest
			@ValueSource(strings = { "5", "6" })
			@GUITest
			@DisplayName("Button 'return quantity' should be disabled when an item is selected, the item quantity changes and the spinner value is equal to or greater than the new item quantity")
			void testReturnQuantityButtonShouldBeDisabledWhenTheQuantityOfTheSelectedItemChangesAndTheSpinnerValueIsEqualToTheSelectedItemQuantity() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 10);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner.execute(() -> listOrderItemsModel.addElement(storedItem));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("5");
				GuiActionRunner.execute(
						() -> listOrderItemsModel.setElementAt(modifiedItem, listOrderItemsModel.indexOf(storedItem)));
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should be enabled when an item is selected, the item quantity changes and the spinner value is less than the new item quantity")
			void testReturnQuantityButtonShouldBeEnabledWhenTheQuantityOfTheSelectedItemChangesAndTheSpinnerValueIsLessThanTheSelectedItemQuantity() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 10);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner.execute(() -> listOrderItemsModel.addElement(storedItem));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("7");
				GuiActionRunner.execute(
						() -> listOrderItemsModel.setElementAt(modifiedItem, listOrderItemsModel.indexOf(storedItem)));
				window.button(JButtonMatcher.withText("Return quantity")).requireEnabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should be disabled when an item is selected and the item quantity changes to one")
			void testReturnQuantityButtonShouldBeDisabledWhenTheQuantityOfTheSelectedItemChangesToOne() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner.execute(() -> listOrderItemsModel.addElement(storedItem));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("3");
				GuiActionRunner.execute(
						() -> listOrderItemsModel.setElementAt(modifiedItem, listOrderItemsModel.indexOf(storedItem)));
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

			@Test
			@GUITest
			@DisplayName("Button 'return quantity' should not change when an item is selected and another item quantity changes")
			void testReturnQuantityButtonShouldNotChangeWhenAnItemIsSelectedAndAnotherItemChange() {
				Product product = new Product("Product1", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner.execute(() -> {
					listOrderItemsModel
							.addElement(new OrderItem(new Product("Product2", 3.0), new Order(OrderStatus.OPEN), 5));
					listOrderItemsModel.addElement(storedItem);
				});
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("3");
				GuiActionRunner.execute(
						() -> listOrderItemsModel.setElementAt(modifiedItem, listOrderItemsModel.indexOf(storedItem)));
				window.button(JButtonMatcher.withText("Return quantity")).requireEnabled();
			}

			@Test
			@DisplayName("Reset button 'return quantity' should be disabled when the spinner is disabled")
			void testReturnQuantityButtonShouldBeDisabledWhenTheSpinnerIsDisabled() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("10");
				GuiActionRunner.execute(() -> window.spinner("cartReturnSpinner").target().setEnabled(false));
				window.button(JButtonMatcher.withText("Return quantity")).requireDisabled();
			}

		}

		@Nested
		@DisplayName("Test label messages")
		class TestLabelMessages {

			@Test
			@DisplayName("Reset the label message when an item is selected")
			void testResetTheLabelMessageWhenAnItemIsSelected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				GuiActionRunner.execute(() -> window.label("cartMessageLabel").target().setText("foo"));
				window.list("cartList").selectItem(0);
				window.label("cartMessageLabel").requireText(" ");
			}

			@Test
			@DisplayName("Reset the label when an item is deselected")
			void testResetTheLabelMessageWhenAnItemIsDeselected() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				GuiActionRunner.execute(() -> window.label("cartMessageLabel").target().setText("foo"));
				window.list("cartList").clearSelection();
				window.label("cartMessageLabel").requireText(" ");
			}

			@ParameterizedTest
			@ValueSource(strings = { "-1", "0", "01", " ", "a", "1.1" })
			@DisplayName("Show error message when the content of the spinner is not a positive integer or starts with zero")
			void testShowErrorMessageWhenTheContentOfTheSpinnerIsNotPositiveIntegerOrStartsWithZero(String input) {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText(input);
				window.label("cartMessageLabel")
						.requireText("Error: the input must be a positive integer. Received: " + input);
			}

			@Test
			@DisplayName("Reset label message when the spinner is disabled")
			void testResetLabelMessageWhenTheSpinnerIsDisabled() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("10");
				GuiActionRunner.execute(() -> window.spinner("cartReturnSpinner").target().setEnabled(false));
				window.label("cartMessageLabel").requireText(" ");
			}

			@Test
			@DisplayName("Reset label message when an item is selected and the spinner value is less than the selected item quantity")
			void testResetLabelMessageWhenAnItemIsSelectedAndTheSpinnerValueIsLessThanTheSelectedItemQuantity() {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("3");
				window.label("cartMessageLabel").requireText(" ");
			}

			@ParameterizedTest
			@ValueSource(strings = { "5", "6" })
			@DisplayName("Show error message when an item is selected and the spinner value is not less than the selected item quantity")
			void testShowErrorMessageWhenAnItemIsSelectedAndTheSpinnerValueIsNotLessThanTheSelectedItemQuantity(
					String input) {
				GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product("Product", 3.0), new Order(OrderStatus.OPEN), 5)));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText(input);
				window.label("cartMessageLabel")
						.requireText("Error: the input must be an integer in range [1,4]. Received: " + input);
			}

			@ParameterizedTest
			@ValueSource(strings = { "5", "6" })
			@DisplayName("Show error message when an item is selected, the item quantity changes and the spinner value is not less than the new item quantity")
			void testShowErrorMessageWhenTheQuantityOfTheSelectedItemChangesAndTheSpinnerValueIsNotLessThanTheSelectedItemQuantity(
					String input) {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 10);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner
						.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(storedItem));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText(input);
				GuiActionRunner.execute(
						() -> listOrderItemsModel.setElementAt(modifiedItem, listOrderItemsModel.indexOf(storedItem)));
				window.label("cartMessageLabel")
						.requireText("Error: the input must be an integer in range [1,4]. Received: " + input);
			}

			@Test
			@DisplayName("Reset label message when an item is selected, the item quantity changes and the spinner value is less than the new item quantity")
			void testResetLabelMessageWhenTheQuantityOfTheSelectedItemChangesAndTheSpinnerValueIsLessThanTheSelectedItemQuantity() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 10);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner
						.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(storedItem));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("7");
				GuiActionRunner.execute(() -> listOrderItemsModel.setElementAt(modifiedItem, 0));
				window.label("cartMessageLabel").requireText(" ");
			}

			@Test
			@DisplayName("Reset label message when an item is selected and the item quantity changes to one")
			void testResetLabelMessageWhenTheQuantityOfTheSelectedItemChangesToOne() {
				Product product = new Product("Product", 3.0);
				OrderItem storedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 5);
				OrderItem modifiedItem = new OrderItem(product, new Order(OrderStatus.OPEN), 1);
				DefaultListModel<OrderItem> listOrderItemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				GuiActionRunner
						.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel().addElement(storedItem));
				window.list("cartList").selectItem(0);
				window.spinner("cartReturnSpinner").enterText("7");
				GuiActionRunner.execute(() -> listOrderItemsModel.setElementAt(modifiedItem, 0));
				window.label("cartMessageLabel").requireText(" ");
			}
		}
	}

	@Nested
	@DisplayName("Test Goodbye panel")
	class GoodbyePanelTest {
		@BeforeEach
		void setup() {
			GuiActionRunner.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "bye"));
		}

		@Test
		@GUITest
		@DisplayName("Goodbye panel inital state")
		void testGoodbyePanelInitialState() {
			window.button(JButtonMatcher.withName("goodbyeStartShopping")).requireText("Start shopping")
					.requireEnabled();
			window.label("byeLabel").requireText("Goodbye!");
		}

		@Test
		@DisplayName("Button 'Start Shopping' should delegate to TotemController 'startShopping'")
		void testStartShoppingButtonShouldDelegateToTotemControllerStartShopping() {
			window.button(JButtonMatcher.withName("goodbyeStartShopping")).click();
			verify(totemController).startShopping();
		}

	}

}
