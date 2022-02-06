package com.github.raffaelliscandiffio.view.swing;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.view.TotemView;

public class TotemSwingView extends JFrame implements TotemView {

	private static final long serialVersionUID = 1L;

	private WelcomePanel welcomePane;
	private ShoppingPanel shoppingPane;
	private CartPanel cartPane;
	private GoodbyePanel goodbyePane;

	private transient TotemController totemController;
	private CardLayout layout;

	public void setTotemController(TotemController totemController) {
		this.totemController = totemController;
	}

	public TotemSwingView() {
		setResizable(false);
		setTitle("Totem");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		layout = new CardLayout();
		getContentPane().setLayout(layout);
		welcomePane = new WelcomePanel();
		shoppingPane = new ShoppingPanel();
		cartPane = new CartPanel();
		goodbyePane = new GoodbyePanel();
		getContentPane().add(welcomePane, "welcome");
		getContentPane().add(shoppingPane, "shopping");
		getContentPane().add(cartPane, "cart");
		getContentPane().add(goodbyePane, "bye");

		welcomePane.addActionListener(e -> startShoppingAction());

		shoppingPane.getAddProductButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				buyProductAction();
			}
		});

		shoppingPane.addActionListener(e -> {
			String command = e.getActionCommand();
			if ("cancelShopping".equals(command))
				closeShoppingAction();
			else
				openCartAction();
		});

		cartPane.getBtnReturnQuantity().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				returnProductAction();
			}
		});

		cartPane.addActionListener(e -> {
			String command = e.getActionCommand();
			if ("openShopping".equals(command))
				openShoppingAction();
			else if ("cancelShopping".equals(command))
				closeShoppingAction();
			else if ("checkout".equals(command))
				confirmOrderAction();
			else
				removeItemAction();

		});

		goodbyePane.addActionListener(e -> startShoppingAction());
	}

	private void returnProductAction() {
		int spinnerValue = ((Integer) cartPane.getSpinner().getValue()).intValue();
		this.totemController.returnProduct(cartPane.getListOrderItems().getSelectedValue(), spinnerValue);
	}

	private void removeItemAction() {
		this.totemController.removeItem(getCartPane().getListOrderItems().getSelectedValue());
	}

	private void confirmOrderAction() {
		this.totemController.confirmOrder();
	}

	private void startShoppingAction() {
		this.totemController.startShopping();
	}

	private void openShoppingAction() {
		this.totemController.openShopping();
	}

	private void closeShoppingAction() {
		this.totemController.cancelShopping();
	}

	private void openCartAction() {
		this.totemController.openOrder();
	}

	private void buyProductAction() {
		this.totemController.buyProduct(getShoppingPane().getListProducts().getSelectedValue(),
				(Integer) getShoppingPane().getQuantitySpinner().getValue());
	}

	ShoppingPanel getShoppingPane() {
		return shoppingPane;
	}

	WelcomePanel getWelcomePane() {
		return welcomePane;
	}

	CartPanel getCartPane() {
		return cartPane;
	}

	GoodbyePanel getGoodbyePane() {
		return goodbyePane;
	}

	@Override
	public void showAllProducts(List<Product> products) {
		products.stream().forEach(getShoppingPane().getListProductsModel()::addElement);
	}

	@Override
	public void showShopping() {
		changePane("shopping");
	}

	@Override
	public void showWelcome() {
		changePane("welcome");
	}

	@Override
	public void showOrder() {
		changePane("cart");
	}

	@Override
	public void showGoodbye() {
		changePane("bye");
	}

	private void changePane(String pane) {
		this.layout.show(getContentPane(), pane);
	}

	@Override
	public void itemModified(OrderItem storedItem, OrderItem modifiedItem) {
		getCartPane().getListOrderItemsModel().setElementAt(modifiedItem,
				getCartPane().getListOrderItemsModel().indexOf(storedItem));
	}

	@Override
	public void itemAdded(OrderItem newItem) {
		getCartPane().getListOrderItemsModel().addElement(newItem);
	}

	@Override
	public void allItemsRemoved() {
		getCartPane().getListOrderItemsModel().clear();
	}

	@Override
	public void itemRemoved(OrderItem item) {
		getCartPane().getListOrderItemsModel().removeElement(item);

	}

	@Override
	public void showShoppingMessage(String msg) {
		setMessageWithColor(getShoppingLabel(), msg, Color.BLACK);
	}

	@Override
	public void showCartMessage(String msg) {
		setMessageWithColor(getCartLabel(), msg, Color.BLACK);
	}

	@Override
	public void showWarning(String msg) {
		setMessageWithColor(getShoppingLabel(), msg, Color.ORANGE);
	}

	@Override
	public void showShoppingErrorMessage(String msg) {
		setMessageWithColor(getShoppingLabel(), msg, Color.RED);
	}

	@Override
	public void showCartErrorMessage(String msg) {
		setMessageWithColor(getCartLabel(), msg, Color.RED);
	}

	@Override
	public void showErrorProductNotFound(String msg, Product product) {
		setMessageWithColor(getShoppingLabel(), msg, Color.RED);
		getShoppingPane().getListProductsModel().removeElement(product);
	}

	private JLabel getShoppingLabel() {
		return getShoppingPane().getLblMessage();
	}

	@Override
	public void showErrorItemNotFound(String msg, OrderItem item) {
		setMessageWithColor(getCartLabel(), msg, Color.RED);
		getCartPane().getListOrderItemsModel().removeElement(item);

	}

	@Override
	public void showErrorEmptyOrder(String msg) {
		setMessageWithColor(getCartLabel(), msg, Color.RED);
		getCartPane().getListOrderItemsModel().clear();
	}

	private JLabel getCartLabel() {
		return getCartPane().getMessageLabel();
	}

	private void setMessageWithColor(JLabel label, String msg, Color color) {
		label.setText(msg);
		label.setForeground(color);
	}

	CardLayout getCardLayout() {
		return layout;
	}

}
