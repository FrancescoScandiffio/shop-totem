package com.github.raffaelliscandiffio.view.swing;

import java.awt.CardLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JFrame;

import com.github.raffaelliscandiffio.controller.TotemController;
import com.github.raffaelliscandiffio.model.OrderItem;
import com.github.raffaelliscandiffio.model.Product;
import com.github.raffaelliscandiffio.view.TotemView;

public class TotemSwingView extends JFrame implements TotemView {

	private static final long serialVersionUID = 1L;

	private WelcomePanel welcomePane;
	private ShoppingPanel shoppingPane;
	private CartPanel cartPane;

	private TotemController totemController;
	private CardLayout layout;

	public void setTotemController(TotemController totemController) {
		this.totemController = totemController;
	}

	/**
	 * Launch the application.
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TotemSwingView frame = new TotemSwingView();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	*/
	 

	/**
	 * Create the frame.
	 */
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
		getContentPane().add(welcomePane, "welcome");
		getContentPane().add(shoppingPane, "shopping");
		getContentPane().add(cartPane, "cart");

		welcomePane.addActionListener(e -> {
			String command = e.getActionCommand();
			if ("startShopping".equals(command)) {
				startShoppingAction();
			}
		});

		shoppingPane.addActionListener(e -> {
			String command = e.getActionCommand();
			if ("cancelShopping".equals(command)) {
				closeShoppingAction();
			} else if ("buyProduct".equals(command)) {
				buyProductAction();
			} else if ("openCart".equals(command)) {
				openCartAction();
			}
		});
		
		cartPane.addActionListener(e -> {
			String command = e.getActionCommand();
			if ("openShopping".equals(command)) {
				openShoppingAction();
			} 
		});
		
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
		this.totemController.openCart();
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
	public void showCart() {
		changePane("cart");
	}

	private void changePane(String pane) {
		this.layout.show(getContentPane(), pane);
	}

	@Override
	public void showErrorMessage(String msg) {
		getShoppingPane().getLblMessage().setText(msg);
		getShoppingPane().getLblMessage().setForeground(Color.RED);
	}

	@Override
	public void showMessage(String msg) {
		getShoppingPane().getLblMessage().setText(msg);
		getShoppingPane().getLblMessage().setForeground(Color.BLACK);
	}

	@Override
	public void showErrorProductNotFound(String msg, Product product) {
		getShoppingPane().getLblMessage().setText(msg);
		getShoppingPane().getLblMessage().setForeground(Color.RED);
		getShoppingPane().getListProductsModel().removeElement(product);
	}

	@Override
	public void showWarning(String msg) {
		getShoppingPane().getLblMessage().setText(msg);
		getShoppingPane().getLblMessage().setForeground(Color.ORANGE);
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
	public void clearCart() {
		getCartPane().getListOrderItemsModel().clear();
	}

	@Override
	public void allItemsRemoved() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void itemRemoved(OrderItem item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showErrorItemNotFound(String msg, OrderItem item) {
		// TODO Auto-generated method stub
		
	}

}
