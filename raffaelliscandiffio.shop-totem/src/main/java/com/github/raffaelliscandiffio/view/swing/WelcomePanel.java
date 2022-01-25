package com.github.raffaelliscandiffio.view.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class WelcomePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JButton btnStartShopping;

	public WelcomePanel() {

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_contentPane);

		btnStartShopping = new JButton("Start shopping");
		btnStartShopping.setName("welcomeStartShopping");
		btnStartShopping.setFocusPainted(false);

		GridBagConstraints gbc_btnStartShopping = new GridBagConstraints();
		gbc_btnStartShopping.gridx = 0;
		gbc_btnStartShopping.gridy = 0;
		this.add(btnStartShopping, gbc_btnStartShopping);
	}

	public void addActionListener(ActionListener listener) {
		btnStartShopping.addActionListener(listener);
	}

}
