package com.mybank.view;

import com.mybank.DBConnection;
import com.mybank.dao.TransactionDAO;
import com.mybank.model.Transaction;
import com.mybank.model.User;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DepositFrame extends JFrame {
    private final User user;
    private final DashboardFrame parent;
    private JTextField amountField;
    private JComboBox<String> sourceCombo;
    private JTextArea noteArea;
    private JLabel balanceLabel;

    public DepositFrame(User user, DashboardFrame parent) {
        this.user = user;
        this.parent = parent;
        setTitle("Deposit Funds - " + (user.getFullName() == null ? user.getUsername() : user.getFullName()));
        setSize(460, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        form.add(new JLabel("Amount (USh):"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField();
        form.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        form.add(new JLabel("Funding source:"), gbc);
        gbc.gridx = 1;
        sourceCombo = new JComboBox<>(new String[]{
                "Cash Deposit",
                "Salary",
                "Mobile Money",
                "Cheque",
                "Wire Transfer"
        });
        form.add(sourceCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        form.add(new JLabel("Reference / Notes:"), gbc);

        gbc.gridy++;
        noteArea = new JTextArea(4, 20);
        form.add(new JScrollPane(noteArea), gbc);

        balanceLabel = new JLabel("Available balance: " + formatCurrency(user.getBalance()));
        balanceLabel.setForeground(new Color(0x2F3E46));

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(balanceLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel();
        JButton submit = new JButton("Process Deposit");
        JButton cancel = new JButton("Cancel");
        buttons.add(submit);
        buttons.add(cancel);
        footer.add(buttons, BorderLayout.EAST);

        content.add(form, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        add(content);

        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> processDeposit());
    }

    private void processDeposit() {
        String amtText = amountField.getText().trim();
        if (amtText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter an amount to deposit.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive amount.");
            return;
        }

        String source = (String) sourceCombo.getSelectedItem();
        String note = noteArea.getText().trim();
        String description = source + (note.isEmpty() ? "" : " - " + note);

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE users SET balance = balance + ? WHERE user_id = ?")) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, user.getUserId());
                    ps.executeUpdate();
                }

                Integer accountId = null;
                BigDecimal accountBalance = null;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT account_id, balance FROM accounts WHERE user_id = ? ORDER BY account_id LIMIT 1 FOR UPDATE")) {
                    ps.setInt(1, user.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            accountId = rs.getInt("account_id");
                            accountBalance = rs.getBigDecimal("balance");
                            if (accountBalance == null) {
                                accountBalance = BigDecimal.ZERO;
                            }
                        }
                    }
                }
                if (accountId != null && accountBalance != null) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = ? WHERE account_id = ?")) {
                        ps.setBigDecimal(1, accountBalance.add(amount));
                        ps.setInt(2, accountId);
                        ps.executeUpdate();
                    }
                }

                Transaction tx = new Transaction();
                tx.setUserId(user.getUserId());
                tx.setType("deposit");
                tx.setAmount(amount);
                tx.setDescription(description);
                if (!new TransactionDAO().create(c, tx)) {
                    throw new SQLException("Failed to record deposit transaction");
                }

                c.commit();
                JOptionPane.showMessageDialog(this, "Deposit recorded successfully.");
                parent.refreshBalances();
                dispose();
            } catch (Exception ex) {
                c.rollback();
                JOptionPane.showMessageDialog(this, "Failed to process deposit: " + ex.getMessage());
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to process deposit: " + ex.getMessage());
        }
    }

    private String formatCurrency(double amount) {
        return String.format("USh %,.2f", amount);
    }
}
