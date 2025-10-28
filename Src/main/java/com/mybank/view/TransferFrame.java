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

public class TransferFrame extends JFrame {
    private final User user;
    private final DashboardFrame parent;
    private JTextField targetUserField;
    private JTextField amountField;
    private JTextArea noteArea;

    public TransferFrame(User user, DashboardFrame parent) {
        this.user = user;
        this.parent = parent;
        setTitle("Transfer Funds");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel p = new JPanel(new GridLayout(7, 1, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        p.add(new JLabel("Recipient username:"));
        targetUserField = new JTextField();
        p.add(targetUserField);

        p.add(new JLabel("Amount:"));
        amountField = new JTextField();
        p.add(amountField);

        p.add(new JLabel("Note (optional):"));
        noteArea = new JTextArea(3, 20);
        p.add(new JScrollPane(noteArea));

        JButton send = new JButton("Send");
        JButton cancel = new JButton("Cancel");
        JPanel btns = new JPanel();
        btns.add(send); btns.add(cancel);
        p.add(btns);

        add(p);

        send.addActionListener(e -> doTransfer());
        cancel.addActionListener(e -> dispose());
    }

    private void doTransfer() {
        String targetUsername = targetUserField.getText().trim();
        String amtText = amountField.getText().trim();
        String note = noteArea.getText().trim();

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

        // find recipient
        UserDAO udao = new UserDAO();
        User recipient = udao.findByUsername(targetUsername);
        if (recipient == null) {
            JOptionPane.showMessageDialog(this, "Recipient not found.");
            return;
        }

        // perform DB transaction: check sender balance, update both balances, insert tx rows
        try (Connection c = DBConnection.getConnection()) {
            try {
                c.setAutoCommit(false);

                // Lock and read sender balance
                try (PreparedStatement ps = c.prepareStatement("SELECT balance FROM users WHERE user_id = ? FOR UPDATE")) {
                    ps.setInt(1, user.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Sender account not found.");
                        BigDecimal senderBal = rs.getBigDecimal("balance");
                        if (senderBal.compareTo(amount) < 0) throw new Exception("Insufficient funds.");
                    }
                }

                // Lock and read recipient balance
                BigDecimal recipientBal;
                try (PreparedStatement ps = c.prepareStatement("SELECT balance FROM users WHERE user_id = ? FOR UPDATE")) {
                    ps.setInt(1, recipient.getUserId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new Exception("Recipient account not found.");
                        recipientBal = rs.getBigDecimal("balance");
                    }
                }

                // Update balances
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

                // Insert transaction rows
                try (PreparedStatement it = c.prepareStatement("INSERT INTO transactions (user_id, type, amount, description) VALUES (?,?,?,?)")) {
                    // sender tx (negative amount)
                    it.setInt(1, user.getUserId());
                    it.setString(2, "transfer");
                    it.setBigDecimal(3, amount.negate());
                    it.setString(4, "To: " + recipient.getUsername() + (note.isEmpty() ? "" : " - " + note));
                    it.executeUpdate();

                    // recipient tx (positive amount)
                    it.setInt(1, recipient.getUserId());
                    it.setString(2, "transfer");
                    it.setBigDecimal(3, amount);
                    it.setString(4, "From: " + user.getUsername() + (note.isEmpty() ? "" : " - " + note));
                    it.executeUpdate();
                }

                c.commit();
                JOptionPane.showMessageDialog(this, "Transfer successful.");
                parent.refreshBalances();
                dispose();

            } catch (Exception ex) {
                c.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Transfer failed: " + ex.getMessage());
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Transfer failed: " + ex.getMessage());
        }
    }
}
