package com.mybank.view;

import com.mybank.dao.TransactionDAO;
import com.mybank.model.Transaction;
import com.mybank.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TransactionHistoryFrame extends JFrame {
    private final User user;
    private final TransactionDAO dao = new TransactionDAO();

    public TransactionHistoryFrame(User user) {
        this.user = user;
        setTitle("Transaction History - " + (user.getFullName() == null ? user.getUsername() : user.getFullName()));
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        String[] cols = {"ID", "Type", "Amount", "Description", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        List<Transaction> txs = dao.forAccount(user.getUserId());
        for (Transaction t : txs) {
            model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getType(),
                    t.getAmount(),
                    t.getDescription(),
                    t.getCreatedAt()
            });
        }

        add(sp, BorderLayout.CENTER);
    }
}
