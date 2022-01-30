package com.github.raffaelliscandiffio.view.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.github.raffaelliscandiffio.model.OrderItem;

public class CartPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JList<OrderItem> listOrderItems;
	private DefaultListModel<OrderItem> listOrderItemsModel;
	private JScrollPane scrollPane;
	private JButton btnContinueShopping;
	private JButton btnCancelShopping;
	private JButton btnCheckout;
	private JLabel messageLabel;
	private JLabel lblRemoveSelectedItem;
	private JButton btnRemoveSelected;
	private JButton btnReturnQuantity;
	private Box horizontalBox;
	private JLabel lblQuantity;
	private JSpinner spinner;
	private Component horizontalStrut;
	private SpinnerNumberModel spinnerModel;

	public CartPanel() {
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 243, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0 };
		setLayout(gridBagLayout);

		btnContinueShopping = new JButton("Continue Shopping");
		btnContinueShopping.setActionCommand("openShopping");
		btnContinueShopping.setFocusPainted(false);
		GridBagConstraints gbc_btnBackToShopping = new GridBagConstraints();
		gbc_btnBackToShopping.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnBackToShopping.insets = new Insets(0, 0, 5, 5);
		gbc_btnBackToShopping.gridx = 0;
		gbc_btnBackToShopping.gridy = 0;
		add(btnContinueShopping, gbc_btnBackToShopping);

		btnCancelShopping = new JButton("Cancel Shopping");
		btnCancelShopping.setName("cartBtnCancelShopping");
		btnCancelShopping.setActionCommand("cancelShopping");
		btnCancelShopping.setFocusPainted(false);
		GridBagConstraints gbc_btnCancelShopping = new GridBagConstraints();
		gbc_btnCancelShopping.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancelShopping.insets = new Insets(0, 0, 5, 0);
		gbc_btnCancelShopping.gridx = 8;
		gbc_btnCancelShopping.gridy = 0;
		add(btnCancelShopping, gbc_btnCancelShopping);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 9;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		listOrderItemsModel = new DefaultListModel<>();
		listOrderItemsModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent e) {
				btnCheckout.setEnabled(!listOrderItemsModel.isEmpty());
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				btnCheckout.setEnabled(true);
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				if (listOrderItems.getSelectedIndex() != -1 && e.getIndex0() == listOrderItems.getSelectedIndex())
					updateCurrentUpperBound();
			}
		});

		listOrderItems = new JList<>(listOrderItemsModel);
		listOrderItems.addListSelectionListener(e -> {
			boolean isItemSelected = listOrderItems.getSelectedIndex() != -1;
			btnRemoveSelected.setEnabled(isItemSelected);
			spinner.setEnabled(isItemSelected);
			spinner.setValue(1);
			if (isItemSelected)
				updateCurrentUpperBound();
			else
				spinnerModel.setMaximum(null);
			messageLabel.setText(" ");
		});
		listOrderItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOrderItems.setName("cartList");
		listOrderItems.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				OrderItem orderItem = (OrderItem) value;
				return super.getListCellRendererComponent(list, getDisplayRow(orderItem), index, isSelected,
						cellHasFocus);
			}
		});
		scrollPane.setViewportView(listOrderItems);

		lblRemoveSelectedItem = new JLabel("Remove selected item");
		GridBagConstraints gbc_lblRemoveSelectedItem = new GridBagConstraints();
		gbc_lblRemoveSelectedItem.insets = new Insets(0, 0, 5, 5);
		gbc_lblRemoveSelectedItem.gridx = 0;
		gbc_lblRemoveSelectedItem.gridy = 2;
		add(lblRemoveSelectedItem, gbc_lblRemoveSelectedItem);

		horizontalBox = Box.createHorizontalBox();
		GridBagConstraints gbc_horizontalBox = new GridBagConstraints();
		gbc_horizontalBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_horizontalBox.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalBox.gridx = 0;
		gbc_horizontalBox.gridy = 3;
		add(horizontalBox, gbc_horizontalBox);

		lblQuantity = new JLabel("Quantity");
		horizontalBox.add(lblQuantity);
		horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut);

		spinnerModel = new SpinnerNumberModel(1, 1, null, 1);
		spinner = new JSpinner(spinnerModel);
		spinner.setName("cartReturnSpinner");
		horizontalBox.add(spinner);
		spinner.setEnabled(false);

		btnReturnQuantity = new JButton("Return quantity");
		btnReturnQuantity.setEnabled(false);
		btnReturnQuantity.setFocusPainted(false);
		btnReturnQuantity.setActionCommand("returnProduct");
		GridBagConstraints gbc_btnReturnQuantity = new GridBagConstraints();
		gbc_btnReturnQuantity.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnReturnQuantity.insets = new Insets(0, 0, 5, 5);
		gbc_btnReturnQuantity.gridx = 0;
		gbc_btnReturnQuantity.gridy = 4;
		add(btnReturnQuantity, gbc_btnReturnQuantity);

		spinner.addPropertyChangeListener(arg0 -> {
			if (!spinner.isEnabled())
				btnReturnQuantity.setEnabled(false);
		});

		JFormattedTextField tf = ((DefaultEditor) spinner.getEditor()).getTextField();
		tf.addCaretListener(e -> {
			boolean isValueValid = tf.isEditValid();
			btnReturnQuantity.setEnabled(isValueValid);
			if (!isValueValid) {
				messageLabel.setText("Error: the input must be an integer in range [1,"
						+ spinnerModel.getMaximum().toString() + "]. Received: " + tf.getText());
				messageLabel.setForeground(Color.RED);
			} else
				messageLabel.setText(" ");
		});

		btnCheckout = new JButton("Checkout");
		btnCheckout.setActionCommand("checkout");
		btnCheckout.setEnabled(false);
		btnCheckout.setFocusPainted(false);
		GridBagConstraints gbc_btnCheckout = new GridBagConstraints();
		gbc_btnCheckout.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCheckout.insets = new Insets(0, 0, 5, 0);
		gbc_btnCheckout.gridx = 8;
		gbc_btnCheckout.gridy = 4;
		add(btnCheckout, gbc_btnCheckout);

		btnRemoveSelected = new JButton("Remove selected");
		btnRemoveSelected.setEnabled(false);
		btnRemoveSelected.setActionCommand("removeSelected");
		btnRemoveSelected.setFocusPainted(false);
		GridBagConstraints gbc_btnRemoveSelected = new GridBagConstraints();
		gbc_btnRemoveSelected.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemoveSelected.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemoveSelected.gridx = 0;
		gbc_btnRemoveSelected.gridy = 5;
		add(btnRemoveSelected, gbc_btnRemoveSelected);

		messageLabel = new JLabel(" ");
		messageLabel.setName("cartMessageLabel");
		GridBagConstraints gbc_messageLabel = new GridBagConstraints();
		gbc_messageLabel.fill = GridBagConstraints.VERTICAL;
		gbc_messageLabel.gridwidth = 9;
		gbc_messageLabel.gridx = 0;
		gbc_messageLabel.gridy = 6;
		add(messageLabel, gbc_messageLabel);
	}

	private String getDisplayRow(OrderItem orderItem) {
		return orderItem.getProduct().getName() + " - Quantity: " + orderItem.getQuantity() + " - Price: "
				+ orderItem.getProduct().getPrice() + " € - Subtotal: " + orderItem.getSubTotal() + " €";
	}

	public void addActionListener(ActionListener listener) {
		btnContinueShopping.addActionListener(listener);
		btnCancelShopping.addActionListener(listener);
		btnCheckout.addActionListener(listener);
		btnRemoveSelected.addActionListener(listener);
		btnReturnQuantity.addActionListener(listener);
	}

	DefaultListModel<OrderItem> getListOrderItemsModel() {
		return listOrderItemsModel;
	}

	JList<OrderItem> getListOrderItems() {
		return listOrderItems;
	}

	public JLabel getMessageLabel() {
		return messageLabel;
	}

	private void updateCurrentUpperBound() {
		spinnerModel.setMaximum(listOrderItems.getSelectedValue().getQuantity() - 1);
	}

	JSpinner getSpinner() {
		return spinner;
	}

}
