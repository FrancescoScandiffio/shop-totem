package com.github.raffaelliscandiffio.view.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.github.raffaelliscandiffio.model.OrderItem;


public class CartPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	JButton btnGoShopping;

	private JList<OrderItem> listOrderItems;
	private DefaultListModel<OrderItem> listOrderItemsModel;
	private JScrollPane scrollPane;

	public CartPanel() {

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 243, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		btnGoShopping = new JButton("Go to Shopping");
		btnGoShopping.setActionCommand("openShopping");

		GridBagConstraints gbc_btnGoShopping = new GridBagConstraints();
		gbc_btnGoShopping.insets = new Insets(0, 0, 5, 5);
		gbc_btnGoShopping.gridx = 0;
		gbc_btnGoShopping.gridy = 0;
		add(btnGoShopping, gbc_btnGoShopping);

		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 9;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		listOrderItemsModel = new DefaultListModel<>();
		listOrderItems = new JList<>(listOrderItemsModel);

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
	}

	private String getDisplayRow(OrderItem orderItem) {
		return orderItem.getProduct().getName() + " - Price: " + orderItem.getProduct().getPrice() + " € - Quantity: "
				+ orderItem.getQuantity();
	}

	public void addActionListener(ActionListener listener) {
		btnGoShopping.addActionListener(listener);
	}

	public DefaultListModel<OrderItem> getListOrderItemsModel() {
		return listOrderItemsModel;
	}
}
