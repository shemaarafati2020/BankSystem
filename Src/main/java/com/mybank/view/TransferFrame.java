package com.mybank.view;

import com.mybank.DBConnection;
import com.mybank.dao.TransactionDAO;
import com.mybank.dao.UserDAO;
import com.mybank.model.Transaction;
import com.mybank.model.User;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransferFrame extends JFrame {
    private final User user;
    private final DashboardFrame parent;
    private JTextField targetUserField;
    private JTextField amountField;
    private JTextArea noteArea;
    private JComboBox<String> reasonCombo;
    private JTextField referenceField;
    private JLabel balanceLabel;

    public TransferFrame(User user, DashboardFrame parent) {
        this.user = user;
        this.parent = parent;
        setTitle("Transfer Funds");
        setSize(480, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        balanceLabel = new JLabel("Available balance: " + String.format("USh %,.2f", user.getBalance()));
        gbc.gridwidth = 2;
        p.add(balanceLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        p.add(new JLabel("Recipient username:"), gbc);
        gbc.gridx = 1;
        targetUserField = new JTextField();
        p.add(targetUserField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        p.add(new JLabel("Amount (USh):"), gbc);
        gbc.gridx = 1;
        amountField = new JTextField();
        p.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        p.add(new JLabel("Transfer reason:"), gbc);
        gbc.gridx = 1;
        reasonCombo = new JComboBox<>(new String[]{
                "Peer Transfer",
                "Family Support",
                "Business Payment",
                "Loan Settlement",
                "Other"
        });
        p.add(reasonCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        p.add(new JLabel("Reference:"), gbc);
        gbc.gridx = 1;
        referenceField = new JTextField();
        p.add(referenceField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        p.add(new JLabel("Notes:"), gbc);

        gbc.gridy++;
        noteArea = new JTextArea(4, 20);
        p.add(new JScrollPane(noteArea), gbc);

        gbc.gridy++;
        JPanel btns = new JPanel();
        JButton send = new JButton("Send");
        JButton cancel = new JButton("Cancel");
        btns.add(send);
        btns.add(cancel);
        p.add(btns, gbc);

        add(p);

        send.addActionListener(e -> doTransfer());
        cancel.addActionListener(e -> dispose());
    }

    private void doTransfer() {
        String targetUsername = targetUserField.getText().trim();
        String amtText = amountField.getText().trim();
        String note = noteArea.getText().trim();
        String reason = (String) reasonCombo.getSelectedItem();
        String reference = referenceField.getText().trim();

        if (targetUsername.isEmpty() || amtText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter recipient and amount.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid amount.");
            return;
        }

        UserDAO udao = new UserDAO();
        User recipient = udao.findByUsername(targetUsername);
        if (recipient == null) {
            JOptionPane.showMessageDialog(this, "Recipient not found.");
            return;
        }

        if (recipient.getUserId() == user.getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot transfer to yourself.");
            return;
        }

        String referenceText = reference.isEmpty() ? "" : (" | Ref: " + reference);
        String senderDescription = reason + " to " + recipient.getUsername() + referenceText + (note.isEmpty() ? "" : " - " + note);
        String recipientDescription = reason + " from " + user.getUsername() + referenceText + (note.isEmpty() ? "" : " - " + note);

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Lock sender balance
                try (PreparedStatement ps = c.prepareStatement("SELECT balance FROM users WHERE user_id = ? FOR UPDATE")) {
                    ps.setInt(1, user.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Sender account not found.");
                        BigDecimal senderBal = rs.getBigDecimal("balance");
                        if (senderBal.compareTo(amount) < 0) throw new Exception("Insufficient funds.");
                    }
                }

                // Lock recipient
                try (PreparedStatement ps = c.prepareStatement("SELECT balance FROM users WHERE user_id = ? FOR UPDATE")) {
                    ps.setInt(1, recipient.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Recipient account not found.");
                    }
                }

                Integer senderAccountId = null;
                BigDecimal senderAccountBalance = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT account_id, balance FROM accounts WHERE user_id = ? ORDER BY account_id LIMIT 1 FOR UPDATE")) {
                    ps.setInt(1, user.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            senderAccountId = rs.getInt("account_id");
                            senderAccountBalance = rs.getBigDecimal("balance");
                            if (senderAccountBalance == null) {
                                senderAccountBalance = BigDecimal.ZERO;
                            }
                        }
                    }
                }

                Integer recipientAccountId = null;
                BigDecimal recipientAccountBalance = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT account_id, balance FROM accounts WHERE user_id = ? ORDER BY account_id LIMIT 1 FOR UPDATE")) {
                    ps.setInt(1, recipient.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            recipientAccountId = rs.getInt("account_id");
                            recipientAccountBalance = rs.getBigDecimal("balance");
                            if (recipientAccountBalance == null) {
                                recipientAccountBalance = BigDecimal.ZERO;
                            }
                        }
                    }
                }

                try (PreparedStatement up1 = c.prepareStatement("UPDATE users SET balance = balance - ? WHERE user_id = ?")) {
                    up1.setBigDecimal(1, amount);
                    up1.setInt(2, user.getUserId());
                    up1.executeUpdate();
                }
                try (PreparedStatement up2 = c.prepareStatement("UPDATE users SET balance = balance + ? WHERE user_id = ?")) {
                    up2.setBigDecimal(1, amount);
                    up2.setInt(2, recipient.getUserId());
                    up2.executeUpdate();
                }

                if (senderAccountId != null && senderAccountBalance != null) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = ? WHERE account_id = ?")) {
                        ps.setBigDecimal(1, senderAccountBalance.subtract(amount));
                        ps.setInt(2, senderAccountId);
                        ps.executeUpdate();
                    }
                }
                if (recipientAccountId != null && recipientAccountBalance != null) {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE accounts SET balance = ? WHERE account_id = ?")) {
                        ps.setBigDecimal(1, recipientAccountBalance.add(amount));
                        ps.setInt(2, recipientAccountId);
                        ps.executeUpdate();
                    }
                }

                TransactionDAO tdao = new TransactionDAO();
                Transaction senderTx = new Transaction();
                senderTx.setUserId(user.getUserId());
                senderTx.setType("transfer");
                senderTx.setAmount(amount.negate());
                senderTx.setDescription(senderDescription);
                if (!tdao.create(c, senderTx)) {
                    throw new SQLException("Failed to record sender transfer");
                }

                Transaction recipientTx = new Transaction();
                recipientTx.setUserId(recipient.getUserId());
                recipientTx.setType("transfer");
                recipientTx.setAmount(amount);
                recipientTx.setDescription(recipientDescription);
                if (!tdao.create(c, recipientTx)) {
                    throw new SQLException("Failed to record recipient transfer");
                }

                c.commit();
                JOptionPane.showMessageDialog(this, "Transfer successful.");
                parent.refreshBalances();
                dispose();
            } catch (Exception ex) {
                c.rollback();
                JOptionPane.showMessageDialog(this, "Transfer failed: " + ex.getMessage());
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Transfer failed: " + ex.getMessage());
        }
    }
}
