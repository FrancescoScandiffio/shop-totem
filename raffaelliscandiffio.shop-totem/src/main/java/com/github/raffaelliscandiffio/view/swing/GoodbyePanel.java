package com.github.raffaelliscandiffio.view.swing;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GoodbyePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JButton btnStartShopping;

	public GoodbyePanel() {
		setBorder(null);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 158, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblGoodbye = new JLabel("Goodbye!");
		lblGoodbye.setName("byeLabel");
		lblGoodbye.setFont(new Font("FreeSans", Font.BOLD, 30));
		GridBagConstraints gbc_lblGoodbye = new GridBagConstraints();
		gbc_lblGoodbye.insets = new Insets(0, 0, 5, 0);
		gbc_lblGoodbye.gridx = 0;
		gbc_lblGoodbye.gridy = 1;
		add(lblGoodbye, gbc_lblGoodbye);

		btnStartShopping = new JButton("Start shopping");
		btnStartShopping.setFocusPainted(false);
		btnStartShopping.setName("goodbyeStartShopping");
		GridBagConstraints gbc_btnStartShopping = new GridBagConstraints();
		gbc_btnStartShopping.insets = new Insets(0, 0, 5, 0);
		gbc_btnStartShopping.gridx = 0;
		gbc_btnStartShopping.gridy = 2;
		add(btnStartShopping, gbc_btnStartShopping);
	}

	public void addActionListener(ActionListener listener) {
		btnStartShopping.addActionListener(listener);

	}
}
