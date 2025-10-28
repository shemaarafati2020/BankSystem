package com.mybank.view;

import com.mybank.model.User;
import javax.swing.*;
import java.awt.*;

public class DepositFrame extends JFrame {
    private User user;

    public DepositFrame(User user) {
        this.user = user;
        setTitle("Deposit Funds - " + user.getFullName());
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Amount:"));
        JTextField amountField = new JTextField();
        panel.add(amountField);

        JButton submit = new JButton("Submit");
        JButton cancel = new JButton("Cancel");
        JPanel btns = new JPanel();
        btns.add(submit);
        btns.add(cancel);

        add(panel, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Deposit processed for $" + amountField.getText());
            dispose();
        });
    }
}

