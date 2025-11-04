package com.mybank.view;

import com.mybank.dao.TransactionDAO;
import com.mybank.model.Transaction;
import com.mybank.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class TransactionHistoryFrame extends JFrame {
    private final User user;
    private final TransactionDAO dao = new TransactionDAO();
    private DefaultTableModel model;
    private JComboBox<String> typeFilter;
    private JLabel inflowLabel;
    private JLabel outflowLabel;

    public TransactionHistoryFrame(User user) {
        this.user = user;
        setTitle("Transaction History - " + (user.getFullName() == null ? user.getUsername() : user.getFullName()));
        setSize(960, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        String[] cols = {"ID", "Type", "Amount", "Description", "Date"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(table);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by type:"));
        typeFilter = new JComboBox<>(new String[]{"All", "deposit", "withdraw", "transfer"});
        filterPanel.add(typeFilter);
        JButton refreshBtn = new JButton("Apply");
        JButton exportBtn = new JButton("Export CSV");
        inflowLabel = new JLabel("Inflow: --");
        outflowLabel = new JLabel("Outflow: --");
        filterPanel.add(refreshBtn);
        filterPanel.add(exportBtn);
        filterPanel.add(inflowLabel);
        filterPanel.add(outflowLabel);

        add(filterPanel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadData());
        typeFilter.addActionListener(e -> loadData());
        exportBtn.addActionListener(e -> exportToCsv());

        loadData();
    }

    private void loadData() {
        String filter = (String) typeFilter.getSelectedItem();
        List<Transaction> txs = dao.forUser(user.getUserId(), filter);
        model.setRowCount(0);
        BigDecimal inflow = BigDecimal.ZERO;
        BigDecimal outflow = BigDecimal.ZERO;
        for (Transaction t : txs) {
            model.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getType(),
                    formatCurrency(t.getAmount()),
                    t.getDescription(),
                    t.getCreatedAt()
            });
            if (t.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
                inflow = inflow.add(t.getAmount());
            } else {
                outflow = outflow.add(t.getAmount().abs());
            }
        }
        inflowLabel.setText("Inflow: " + formatCurrency(inflow));
        outflowLabel.setText("Outflow: " + formatCurrency(outflow));
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("transactions.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
            writer.write("id,type,amount,description,date\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                writer.write(model.getValueAt(i, 0) + ","
                        + model.getValueAt(i, 1) + ","
                        + model.getValueAt(i, 2) + ","
                        + escapeCsv(model.getValueAt(i, 3)) + ","
                        + model.getValueAt(i, 4) + "\n");
            }
            JOptionPane.showMessageDialog(this, "Transactions exported successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to export: " + ex.getMessage());
        }
    }

    private String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.contains(",") || str.contains("\"")) {
            str = '"' + str.replace("\"", "\"\"") + '"';
        }
        return str;
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("USh %,.2f", amount);
    }
}
