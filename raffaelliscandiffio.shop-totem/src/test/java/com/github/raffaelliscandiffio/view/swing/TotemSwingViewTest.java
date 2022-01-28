package com.github.raffaelliscandiffio.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.utils.GUITestExtension;

@DisplayName("Test Totem View")
@ExtendWith(GUITestExtension.class)
@ExtendWith(MockitoExtension.class)
class TotemSwingViewTest {

	private FrameFixture window;

	private TotemSwingView totemSwingView;

	@Mock
	private TotemController totemController;

	@BeforeAll
	static void setUpOnce() {
		FailOnThreadViolationRepaintManager.install();
	}

	@BeforeEach
	void setup() {
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
			assertThat(totemSwingView.getShoppingPane().isShowing()).isTrue();
			assertThat(totemSwingView.getWelcomePane().isShowing()).isFalse();
		}

		@Test
		@DisplayName("Method 'showWelcome' should show the welcome panel")
		void testShowWelcomeShouldChangePanelToWelcomePanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
			GuiActionRunner.execute(() -> totemSwingView.showWelcome());
			assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
		}

		@Test
		@DisplayName("Method 'showCart' should show the cart panel")
		void testShowCartShouldChangePanelToCartPanel() {
			GuiActionRunner
					.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "shopping"));
			GuiActionRunner.execute(() -> totemSwingView.showOrder());
			assertThat(totemSwingView.getCartPane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
		}

		@Test
		@DisplayName("Method 'showGoodbye' should sho the goodbye panel")
		void testShowGoodbyeShouldChangePanelToGoodbyePanel() {
			GuiActionRunner.execute(() -> totemSwingView.getCardLayout().show(totemSwingView.getContentPane(), "cart"));
			GuiActionRunner.execute(() -> totemSwingView.showGoodbye());
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
			assertThat(totemSwingView.getGoodbyePane().isShowing()).isTrue();
		}
	}

	@Nested
	@DisplayName("Test Welcome panel")
	class WelcomePanelTest {

		@Test
		@DisplayName("Welcome panel should be the only visible panel at start")
		void testWelcomePanelShouldBeInitialPanel() {
			assertThat(totemSwingView.getWelcomePane().isShowing()).isTrue();
			assertThat(totemSwingView.getShoppingPane().isShowing()).isFalse();
			assertThat(totemSwingView.getCartPane().isShowing()).isFalse();
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
		@GUITest
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
		@DisplayName("Method 'ShowAllProducts' should add the received products to shopping panel")
		void testShowAllProductsShouldAddProductsFromTheListToShoppingPanel() {
			Product product1 = new Product(1, "Product1", 2);
			Product product2 = new Product(1, "Product2", 3);
			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));

			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €", "Product2 - Price: 3.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Button 'Cancel Shopping' should delegate to TotemController 'cancelShopping'")
		void testCancelShoppingButtonShouldDelegateToTotemControllerCancelShopping() {
			window.button(JButtonMatcher.withName("shopBtnCancelShopping")).click();
			verify(totemController).cancelShopping();
		}

		@Test
		@GUITest
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
					.addElement(new Product(1, "Product1", 2)));
			window.list("productList").selectItem(0);
			JButtonFixture buttonAdd = window.button(JButtonMatcher.withText("Add"));
			JSpinnerFixture spinner = window.spinner("quantitySpinner");
			spinner.requireEnabled();
			buttonAdd.requireEnabled();

			window.list("productList").clearSelection();
			buttonAdd.requireDisabled();
			spinner.requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is a negative number")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsNegative() {
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("-1");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is not a number")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsNaN() {
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("text");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is zero")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsZero() {
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("0");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should be disabled when quantity in spinner is invalid and enabled when becomes valid")
		void testAddButtonShouldBeDisabledWhenQuantityInSpinnerIsInvalidAndEnabledWhenTurnsValid() {
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("text");
			window.button(JButtonMatcher.withText("Add")).requireDisabled();
			window.spinner("quantitySpinner").enterText("2");
			window.button(JButtonMatcher.withText("Add")).requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("Quantity spinner shown value should be reset to last valid value if an invalid value is inserted and the product is deselected")
		void testQuantitySpinnerShouldBeResetToLastValidValueWhenQuantityIsInvalidAndProductIsDeselected() {
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			// gets the spinner text field
			JFormattedTextField tf = ((JSpinner.DefaultEditor) window.spinner("quantitySpinner").target().getEditor())
					.getTextField();
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("2");
			window.spinner("quantitySpinner").enterText("text");
			window.list("productList").clearSelection();
			// asserts on the spinner current shown value
			assertThat(tf.getText()).isEqualTo("2");
		}

		@Test
		@GUITest
		@DisplayName("Message label should display 'Invalid quantity' only when quantity inserted is invalid")
		void testMessageShouldDisplayInvalidQuantityErrorOnlyWhenQuantityIsInvalid() {
			Product product = new Product(1, "Product1", 2);
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
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterText("-1");
			window.label("messageLabel").requireText("Invalid quantity");
			window.list("productList").clearSelection();
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Button 'Add' should delegate to TotemController 'buyProduct'")
		void testAddButtonShouldDelegateToTotemControllerBuyProduct() {
			Product product = new Product(1, "Product1", 2);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getListProductsModel().addElement(product));
			window.list("productList").selectItem(0);
			window.spinner("quantitySpinner").enterTextAndCommit("3");
			window.button(JButtonMatcher.withText("Add")).click();
			verify(totemController).buyProduct(product, 3);
		}

		@Test
		@GUITest
		@DisplayName("Method 'showShoppingErrorMessage' should show the error message in the shopping label")
		void testShowShoppingErrorMessageShouldShowTheErrorMessageInTheShoppingLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showShoppingErrorMessage("Error message"));
			window.label("messageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showShoppingMessage' should show the message in the shopping label")
		void testShowShoppingMessageShouldShowTheMessageInTheShoppingLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showShoppingMessage("Message"));
			window.label("messageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showErrorProductNotFound' should set error message label and delete product from list")
		void testShowErrorProductNotFoundShouldSetMessageLabelAndDeleteProductFromList() {
			Product product1 = new Product(1, "Product1", 2);
			Product product2 = new Product(1, "Product2", 3);
			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));
			GuiActionRunner.execute(() -> totemSwingView.showErrorProductNotFound("Error message", product2));
			String[] listContents = window.list("productList").contents();
			assertThat(listContents).containsExactly("Product1 - Price: 2.0 €");
			window.label("messageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showWarning' should show the warning message in the shopping label")
		void testShowWarningShouldShowTheWarningMessageInTheShoppingLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showWarning("Message"));
			window.label("messageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be reset to empty after Product deselection")
		void testMessageLabelShouldBeResetWhenProductIsDeselected() {
			Product product = new Product(1, "Product1", 2);
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
			Product product1 = new Product(1, "Product1", 2);
			Product product2 = new Product(1, "Product2", 3);
			GuiActionRunner.execute(() -> totemSwingView.showAllProducts(Arrays.asList(product1, product2)));
			window.list("productList").selectItem(0);
			GuiActionRunner.execute(() -> totemSwingView.getShoppingPane().getLblMessage().setText("Error message"));
			window.list("productList").selectItem(1);
			window.label("messageLabel").requireText(" ");
		}

		@Test
		@GUITest
		@DisplayName("Message label should be reset to empty after quantity change (to valid value)")
		void testMessageLabelShouldBeResetWhenQuantityIsChangedToValidValue() {
			Product product = new Product(1, "Product1", 2);
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

		@Test
		@GUITest
		@DisplayName("Button 'Continue Shopping' should delegate to TotemController 'openShopping'")
		void testContinueShoppingButtonShouldDelegateToTotemControllerOpenShopping() {
			window.button(JButtonMatcher.withText("Continue Shopping")).click();
			verify(totemController).openShopping();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Cancel Shopping' should delegate to TotemController 'cancelShopping'")
		void testCancelShoppingButtonShouldDelegateToTotemControllerCancelShopping() {
			window.button(JButtonMatcher.withName("cartBtnCancelShopping")).click();
			verify(totemController).cancelShopping();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Remove selected' should be enabled only when an OrderItem is selected")
		void testRemoveSelectedButtonShouldBeEnabledOnlyWhenAnOrderItemIsSelected() {
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product(1, "Product1", 3), 5)));
			window.list("cartList").selectItem(0);
			JButtonFixture buttonAdd = window.button(JButtonMatcher.withText("Remove selected"));
			buttonAdd.requireEnabled();
			window.list("cartList").clearSelection();
			buttonAdd.requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Remove selected' should delegate to TotemController 'removeItem'")
		void testRemoveSelectedButtonShouldDelegateToTotemControllerRemoveItem() {
			OrderItem item1 = new OrderItem(new Product(1, "Product1", 3), 5);
			OrderItem item2 = new OrderItem(new Product(2, "Product2", 1), 4);
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

		@Test
		@GUITest
		@DisplayName("Button 'Checkout' should be enabled when an item is added to the cart list")
		void testCheckoutButtonShouldBeEnabledWhenAnItemIsAddedToCartList() {
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product(1, "Product1", 3), 5)));
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
				itemsModel.addElement(new OrderItem(new Product(1, "Product1", 3), 5));
				itemsModel.addElement(new OrderItem(new Product(2, "Product2", 3), 5));
				checkoutButton.target().setEnabled(true);
			});

			GuiActionRunner.execute(() -> itemsModel.removeElementAt(0));
			checkoutButton.requireEnabled();
		}

		@Test
		@GUITest
		@DisplayName("Button 'Checkout' should delegate to TotemController 'confirmOrder'")
		void testCheckoutButtonShouldDelegateToTotemControllerConfirmOrder() {
			JButtonFixture checkoutButton = window.button(JButtonMatcher.withText("Checkout"));
			GuiActionRunner.execute(() -> checkoutButton.target().setEnabled(true));
			checkoutButton.click();
			verify(totemController).confirmOrder();
		}

		@Test
		@GUITest
		@DisplayName("Method 'itemAdded' should add the received OrderItem element to the cart list")
		void testItemAddedShouldAddTheOrderItemToTheCartList() {
			OrderItem newItem = new OrderItem(new Product(2, "Product2", 2), 4);
			GuiActionRunner.execute(() -> totemSwingView.getCartPane().getListOrderItemsModel()
					.addElement(new OrderItem(new Product(1, "Product1", 3), 5)));

			GuiActionRunner.execute(() -> totemSwingView.itemAdded(newItem));
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).containsExactly("Product1 - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €",
					"Product2 - Quantity: 4 - Price: 2.0 € - Subtotal: 8.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Method 'itemRemoved' should remove the specified OrderItem from the cart")
		void testItemRemovedShouldRemoveTheOrderItemFromTheCart() {
			OrderItem toRemove = new OrderItem(new Product(2, "Product2", 2), 4);
			GuiActionRunner.execute(() -> {
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				itemsModel.addElement(new OrderItem(new Product(1, "Product1", 3), 5));
				itemsModel.addElement(toRemove);
			});
			GuiActionRunner.execute(() -> totemSwingView.itemRemoved(toRemove));
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).containsExactly("Product1 - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Method 'itemModified' should update the specified item in the cart list")
		void testItemModifiedShouldUpdateTheOldOrderItemWithTheNewOneInCartList() {
			Product product = new Product(2, "Product2", 3);
			OrderItem oldItem = new OrderItem(product, 4);
			OrderItem updatedItem = new OrderItem(product, 5);
			GuiActionRunner.execute(() -> {
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				itemsModel.addElement(new OrderItem(new Product(1, "Product1", 2), 5));
				itemsModel.addElement(oldItem);
			});
			GuiActionRunner.execute(() -> totemSwingView.itemModified(oldItem, updatedItem));
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).containsExactly("Product1 - Quantity: 5 - Price: 2.0 € - Subtotal: 10.0 €",
					"Product2 - Quantity: 5 - Price: 3.0 € - Subtotal: 15.0 €");

		}

		@Test
		@GUITest
		@DisplayName("Method 'allItemsRemoved' should remove all items from the cart list")
		void testAllItemsRemovedShouldClearTheCartList() {
			GuiActionRunner.execute(() -> {
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				itemsModel.addElement(new OrderItem(new Product(1, "Product1", 2), 5));
				itemsModel.addElement(new OrderItem(new Product(1, "Product1", 2), 4));
			});
			GuiActionRunner.execute(() -> totemSwingView.allItemsRemoved());
			String[] listContents = window.list("cartList").contents();
			assertThat(listContents).isEmpty();
		}

		@Test
		@GUITest
		@DisplayName("Method 'showErrorItemNotFound' should set error message label and delete the item from the cart list")
		void testShowErrorItemNotFoundShouldSetMessageLabelAndDeleteItemFromCartList() {
			OrderItem item1 = new OrderItem(new Product(1, "Product1", 3), 5);
			OrderItem item2 = new OrderItem(new Product(2, "Product2", 1), 4);
			GuiActionRunner.execute(() -> {
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				itemsModel.addElement(item1);
				itemsModel.addElement(item2);
			});
			GuiActionRunner.execute(() -> totemSwingView.showErrorItemNotFound("error message", item1));
			window.label("cartMessageLabel").requireText("error message");
			assertThat(window.list("cartList").contents())
					.containsExactly("Product2 - Quantity: 4 - Price: 1.0 € - Subtotal: 4.0 €");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showErrorEmptyOrder' should set error message label and clear the cart list")
		void testShowErrorEmptyOrderShouldSetMessageLabelAndClearTheCartList() {
			OrderItem item1 = new OrderItem(new Product(1, "Product1", 3), 5);
			OrderItem item2 = new OrderItem(new Product(2, "Product2", 1), 4);
			GuiActionRunner.execute(() -> {
				DefaultListModel<OrderItem> itemsModel = totemSwingView.getCartPane().getListOrderItemsModel();
				itemsModel.addElement(item1);
				itemsModel.addElement(item2);
			});
			GuiActionRunner.execute(() -> totemSwingView.showErrorEmptyOrder("error message"));
			window.label("cartMessageLabel").requireText("error message");
			assertThat(window.list("cartList").contents()).isEmpty();
		}

		@Test
		@GUITest
		@DisplayName("Method 'showCartMessage' should show the message in the cart label")
		void testShowCartMessageShouldShowTheMessageInTheCartLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showCartMessage("Message"));
			window.label("cartMessageLabel").requireText("Message");
		}

		@Test
		@GUITest
		@DisplayName("Method 'showCartErrorMessage' should show the error message in the cart label")
		void testShowCartErrorMessageShouldShowTheErrorMessageInTheCartLabel() {
			GuiActionRunner.execute(() -> totemSwingView.showCartErrorMessage("Error message"));
			window.label("cartMessageLabel").requireText("Error message");
		}

		@Test
		@GUITest
		@DisplayName("The spinner should be enabled only when an item is selected")
		void testTheSpinnerShouldBeEnabledOnlyWhenAnItemIsSelected() {
			JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
			GuiActionRunner.execute(() -> {
				totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product(1, "Product1", 3), 5));
				cartSpinner.target().setEnabled(false);
			});
			window.list("cartList").selectItem(0);
			cartSpinner.requireEnabled();
			window.list("cartList").clearSelection();
			cartSpinner.requireDisabled();
		}

		@Test
		@GUITest
		@DisplayName("The spinner value should be reset to one when the selection changes")
		void testTheSpinnerValueShouldBeResetToOneWhenAnItemIsSelected() {
			JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
			GuiActionRunner.execute(() -> {
				totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product(1, "Product1", 3), 5));
				totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product(1, "Product1", 3), 5));
			});
			window.list("cartList").selectItem(0);
			cartSpinner.enterTextAndCommit("2");
			window.list("cartList").selectItem(1);
			cartSpinner.requireValue(1);
		}

		@Test
		@GUITest
		@DisplayName("The spinner value should be reset to one when an item is deselected")
		void testTheSpinnerValueShouldBeResetToOneWhenAnItemIsDeselected() {
			JSpinnerFixture cartSpinner = window.spinner("cartReturnSpinner");
			GuiActionRunner.execute(() -> {
				totemSwingView.getCartPane().getListOrderItemsModel()
						.addElement(new OrderItem(new Product(1, "Product1", 3), 5));
			});
			window.list("cartList").selectItem(0);
			cartSpinner.enterTextAndCommit("2");
			window.list("cartList").clearSelection();
			cartSpinner.requireValue(1);
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
		@GUITest
		@DisplayName("Button 'Start Shopping' should delegate to TotemController 'startShopping'")
		void testStartShoppingButtonShouldDelegateToTotemControllerStartShopping() {
			window.button(JButtonMatcher.withName("goodbyeStartShopping")).click();
			verify(totemController).startShopping();
		}

	}

}
