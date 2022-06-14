package com.github.raffaelliscandiffio.view.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.github.raffaelliscandiffio.model.Product;

public class ShoppingPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JButton btnCancelButton;

	private JList<Product> listProducts;
	private DefaultListModel<Product> listProductsModel;
	private JButton btnAddButton;
	private JLabel lblQuantity;
	private JScrollPane scrollPane;
	private JSpinner quantitySpinner;
	private JComponent editor;
	private JFormattedTextField tf;
	private JLabel lblMessage;
	private JButton btnCart;

	public ShoppingPanel() {

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 243, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		btnCancelButton = new JButton("Cancel Shopping");
		btnCancelButton.setName("shopBtnCancelShopping");
		btnCancelButton.setActionCommand("cancelShopping");
		btnCancelButton.setFocusPainted(false);

		GridBagConstraints gbc_btnCancelButton = new GridBagConstraints();
		gbc_btnCancelButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancelButton.gridx = 0;
		gbc_btnCancelButton.gridy = 0;
		add(btnCancelButton, gbc_btnCancelButton);

		btnCart = new JButton("Cart");
		btnCart.setName("cartButton");
		btnCart.setActionCommand("openCart");
		btnCart.setFocusPainted(false);
		GridBagConstraints gbc_btnCart = new GridBagConstraints();
		gbc_btnCart.insets = new Insets(0, 0, 5, 0);
		gbc_btnCart.gridx = 8;
		gbc_btnCart.gridy = 0;
		add(btnCart, gbc_btnCart);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 9;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		listProductsModel = new DefaultListModel<>();
		listProducts = new JList<>(listProductsModel);
		listProducts.addListSelectionListener(e -> {
			btnAddButton.setEnabled(listProducts.getSelectedIndex() != -1);
			quantitySpinner.setEnabled(listProducts.getSelectedIndex() != -1);
			lblMessage.setText(" ");
		});

		listProducts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listProducts.setName("productList");
		listProducts.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Product product = (Product) value;
				return super.getListCellRendererComponent(list, getDisplayRow(product), index, isSelected,
						cellHasFocus);
			}
		});
		scrollPane.setViewportView(listProducts);

		lblQuantity = new JLabel("Quantity:");
		GridBagConstraints gbc_lblQuantity = new GridBagConstraints();
		gbc_lblQuantity.gridwidth = 2;
		gbc_lblQuantity.insets = new Insets(0, 0, 5, 5);
		gbc_lblQuantity.anchor = GridBagConstraints.EAST;
		gbc_lblQuantity.gridx = 1;
		gbc_lblQuantity.gridy = 2;
		add(lblQuantity, gbc_lblQuantity);

		SpinnerModel quantitySpinnerModel = new SpinnerNumberModel(1, 1, null, 1);
		quantitySpinner = new JSpinner(quantitySpinnerModel);
		quantitySpinner.setEnabled(false);
		quantitySpinner.setName("quantitySpinner");
		GridBagConstraints gbc_quantitySpinner = new GridBagConstraints();
		gbc_quantitySpinner.anchor = GridBagConstraints.EAST;
		gbc_quantitySpinner.gridwidth = 5;
		gbc_quantitySpinner.insets = new Insets(0, 0, 5, 5);
		gbc_quantitySpinner.gridx = 3;
		gbc_quantitySpinner.gridy = 2;
		add(quantitySpinner, gbc_quantitySpinner);

		editor = quantitySpinner.getEditor();
		tf = ((JSpinner.DefaultEditor) editor).getTextField();
		tf.setColumns(4);
		tf.addCaretListener(e -> {
			boolean isValueValid = tf.getText().matches("^[1-9]\\d*");
			btnAddButton.setEnabled(isValueValid && listProducts.getSelectedIndex() != -1);
			if (!isValueValid) {
				lblMessage.setText("Invalid quantity");
				lblMessage.setForeground(Color.RED);
			} else {
				lblMessage.setText(" ");
			}
		});

		btnAddButton = new JButton("Add");
		btnAddButton.setEnabled(false);
		btnAddButton.setFocusPainted(false);
		btnAddButton.setName("addButton");

		GridBagConstraints gbc_btnAddButton = new GridBagConstraints();
		gbc_btnAddButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddButton.gridx = 8;
		gbc_btnAddButton.gridy = 2;
		add(btnAddButton, gbc_btnAddButton);

		lblMessage = new JLabel(" ");
		lblMessage.setName("messageLabel");
		lblMessage.setForeground(Color.BLACK);
		GridBagConstraints gbc_lblMessage = new GridBagConstraints();
		gbc_lblMessage.insets = new Insets(0, 0, 5, 0);
		gbc_lblMessage.gridwidth = 9;
		gbc_lblMessage.gridx = 0;
		gbc_lblMessage.gridy = 3;
		add(lblMessage, gbc_lblMessage);

	}

	private String getDisplayRow(Product product) {
		return product.getName() + " - Price: " + product.getPrice() + " €";
	}

	void addActionListener(ActionListener listener) {
		btnCancelButton.addActionListener(listener);
		btnCart.addActionListener(listener);
	}

	DefaultListModel<Product> getListProductsModel() {
		return listProductsModel;
	}

	JList<Product> getListProducts() {
		return listProducts;
	}

	JSpinner getQuantitySpinner() {
		return quantitySpinner;
	}

	JLabel getLblMessage() {
		return lblMessage;
	}

	JButton getAddProductButton() {
		return btnAddButton;
	}

}
