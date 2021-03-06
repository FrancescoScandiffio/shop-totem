package com.github.raffaelliscandiffio.view.swing;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.utils.ExcludeGeneratedFromCoverage;
import com.github.raffaelliscandiffio.view.TotemView;

public class TotemSwingView extends JFrame implements TotemView {

	private static final long serialVersionUID = 1L;

	private WelcomePanel welcomePane;
	private ShoppingPanel shoppingPane;
	private CartPanel cartPane;
	private GoodbyePanel goodbyePane;
	private String orderId;

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
		welcomePane.setName("welcomePane");
		getContentPane().add(shoppingPane, "shopping");
		shoppingPane.setName("shoppingPane");
		getContentPane().add(cartPane, "cart");
		cartPane.setName("cartPane");
		getContentPane().add(goodbyePane, "bye");
		goodbyePane.setName("byePane");

		welcomePane.addActionListener(e -> startShoppingAction());

		shoppingPane.getAddProductButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Thread t = new Thread(TotemSwingView.this::buyProductAction);
				t.start();
			}
		});

		shoppingPane.addActionListener(e -> {
			String command = e.getActionCommand();
			Thread t;
			if ("cancelShopping".equals(command))
				t = new Thread(TotemSwingView.this::closeShoppingAction);
			else
				t = new Thread(TotemSwingView.this::openCartAction);
			t.start();
		});

		cartPane.getBtnReturnQuantity().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				Thread t = new Thread(TotemSwingView.this::returnProductAction);
				t.start();
			}
		});

		cartPane.addActionListener(e -> {
			String command = e.getActionCommand();
			Thread t;
			if ("openShopping".equals(command))
				t = new Thread(TotemSwingView.this::openShoppingAction);
			else if ("cancelShopping".equals(command))
				t = new Thread(TotemSwingView.this::closeShoppingAction);
			else if ("checkout".equals(command))
				t = new Thread(TotemSwingView.this::confirmOrderAction);
			else
				t = new Thread(TotemSwingView.this::removeItemAction);
			t.start();
		});

		goodbyePane.addActionListener(e -> startShoppingAction());
	}

	private void returnProductAction() {
		if (cartPane.getBtnReturnQuantity().isEnabled()){
			SwingUtilities.invokeLater(() -> {
				int spinnerValue = ((Integer) cartPane.getSpinner().getValue()).intValue();
				this.totemController.returnItem(cartPane.getListOrderItems().getSelectedValue(), spinnerValue);
			});
		}
	}

	private void removeItemAction() {
		SwingUtilities.invokeLater(
				() -> this.totemController.removeItem(getCartPane().getListOrderItems().getSelectedValue()));
	}

	private void confirmOrderAction() {
		SwingUtilities.invokeLater(() -> this.totemController.checkout(this.getOrderId()));
	}

	private void startShoppingAction() {
		SwingUtilities.invokeLater(() -> this.totemController.startShopping());
	}

	private void openShoppingAction() {
		SwingUtilities.invokeLater(() -> this.totemController.openShopping());
	}

	private void closeShoppingAction() {
		SwingUtilities.invokeLater(() -> this.totemController.cancelShopping(this.getOrderId()));

	}

	private void openCartAction() {
		SwingUtilities.invokeLater(() -> this.totemController.openOrder());

	}

	private void buyProductAction() {
		if (shoppingPane.getAddProductButton().isEnabled()){
		SwingUtilities.invokeLater(() -> this.totemController.buyProduct(this.getOrderId(),
				getShoppingPane().getListProducts().getSelectedValue().getId(),
				(Integer) getShoppingPane().getQuantitySpinner().getValue()));
		}
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
		SwingUtilities.invokeLater(() -> {
			getShoppingPane().getListProductsModel().removeAllElements();
			products.stream().forEach(getShoppingPane().getListProductsModel()::addElement);
		});
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
		SwingUtilities.invokeLater(() -> {
			final DefaultListModel<OrderItem> listOrderItemsModel = getCartPane().getListOrderItemsModel();
			listOrderItemsModel.setElementAt(modifiedItem, listOrderItemsModel.indexOf(storedItem));
		});
	}

	@Override
	public void itemAdded(OrderItem newItem) {
		SwingUtilities.invokeLater(() -> getCartPane().getListOrderItemsModel().addElement(newItem));
	}

	@Override
	public void itemRemoved(OrderItem item) {
		SwingUtilities.invokeLater(() -> getCartPane().getListOrderItemsModel().removeElement(item));
	}

	@Override
	public void showShoppingMessage(String msg) {
		SwingUtilities.invokeLater(() -> setMessageWithColor(getShoppingLabel(), msg, Color.BLACK));
	}

	@Override
	public void showCartMessage(String msg) {
		SwingUtilities.invokeLater(() -> setMessageWithColor(getCartLabel(), msg, Color.BLACK));
	}

	@Override
	public void showShoppingErrorMessage(String msg) {
		SwingUtilities.invokeLater(() -> setMessageWithColor(getShoppingLabel(), msg, Color.RED));
	}

	@Override
	public void showCartErrorMessage(String msg) {
		SwingUtilities.invokeLater(() -> setMessageWithColor(getCartLabel(), msg, Color.RED));
	}

	private JLabel getShoppingLabel() {
		return getShoppingPane().getLblMessage();
	}

	@Override
	@ExcludeGeneratedFromCoverage
	public String getOrderId() {
		return this.orderId;
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

	@Override
	@ExcludeGeneratedFromCoverage
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public void resetView() {
		SwingUtilities.invokeLater(() -> {
			getShoppingPane().getListProductsModel().removeAllElements();
			getCartPane().getListOrderItemsModel().removeAllElements();
		});
	}

	@Override
	public void resetLabels() {
		SwingUtilities.invokeLater(() -> {
			this.showShoppingMessage(" ");
			this.showCartMessage(" ");
		});
	}

	@Override
	public void showAllOrderItems(List<OrderItem> allOrderItems) {
		SwingUtilities.invokeLater(() -> {
			getCartPane().getListOrderItemsModel().removeAllElements();
			allOrderItems.stream().forEach(getCartPane().getListOrderItemsModel()::addElement);
		});
	}

}
