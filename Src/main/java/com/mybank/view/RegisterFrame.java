package com.mybank.view;

import com.mybank.dao.UserDAO;
import com.mybank.dao.AccountDAO;
import com.mybank.dao.TransactionDAO;
import com.mybank.model.User;
import com.mybank.model.Account;
import com.mybank.model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;

public class RegisterFrame extends JFrame {
    private JTextField userF, nameF, emailF, phoneF, addrF, initialDepositField;
    private JPasswordField passF;
    private JLabel photoLabel;
    private File selectedPhoto;

    public RegisterFrame() {
        setTitle("Register - MyBank Pro");
        setSize(540, 420);
        setLocationRelativeTo(null);
        init();
    }

    private void init() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        userF = new JTextField();
        passF = new JPasswordField();
        nameF = new JTextField();
        emailF = new JTextField();
        phoneF = new JTextField();
        addrF = new JTextField();
        initialDepositField = new JTextField("0.00");

        addRow(p, gbc, "Username:", userF);
        addRow(p, gbc, "Password:", passF);
        addRow(p, gbc, "Full name:", nameF);
        addRow(p, gbc, "Email:", emailF);
        addRow(p, gbc, "Phone:", phoneF);
        addRow(p, gbc, "Address:", addrF);
        addRow(p, gbc, "Initial deposit (USh):", initialDepositField);

        JButton choose = new JButton("Choose Photo");
        photoLabel = new JLabel("No photo chosen");
        choose.addActionListener(e -> choosePhoto());
        gbc.gridx = 0;
        gbc.gridy++;
        p.add(new JLabel("Profile photo:"), gbc);
        gbc.gridx = 1;
        JPanel photoPanel = new JPanel(new BorderLayout(6, 6));
        photoPanel.add(choose, BorderLayout.WEST);
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        p.add(photoPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton create = new JButton("Create Account");
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(cancel);
        buttonPanel.add(create);
        p.add(buttonPanel, gbc);

        add(p);

        create.addActionListener(e -> doCreate());
        cancel.addActionListener(e -> this.dispose());
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void choosePhoto() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedPhoto = fc.getSelectedFile();
            photoLabel.setText(selectedPhoto.getName());
        }
    }

    private void doCreate() {
        String username = userF.getText().trim();
        String pass = new String(passF.getPassword());
        if (username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password required");
            return;
        }
        UserDAO ud = new UserDAO();
        if (ud.findByUsername(username) != null) {
            JOptionPane.showMessageDialog(this, "User exists");
            return;
        }

        BigDecimal initialDeposit;
        try {
            initialDeposit = new BigDecimal(initialDepositField.getText().trim());
            if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Initial deposit must be a positive number");
            return;
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(pass);
        u.setFullName(nameF.getText());
        u.setEmail(emailF.getText());
        u.setPhone(phoneF.getText());
        u.setAddress(addrF.getText());
        u.setBalance(initialDeposit.doubleValue());

        if (selectedPhoto != null) {
            try {
                String ext = selectedPhoto.getName().contains(".") ?
                        selectedPhoto.getName().substring(selectedPhoto.getName().lastIndexOf('.')) : "";
                File dest = new File("data/profile_photos/" + username + ext);
                dest.getParentFile().mkdirs();
                Files.copy(selectedPhoto.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                u.setPhoto(dest.getPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        boolean ok = ud.create(u);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Failed to create user.");
            return;
        }

        AccountDAO ad = new AccountDAO();
        String accNum = "ACC" + (System.currentTimeMillis() % 1000000);
        Account account = ad.createAccount(u.getUserId(), accNum, "Savings", initialDeposit);

        if (initialDeposit.compareTo(BigDecimal.ZERO) > 0 && account != null) {
            Transaction initialTx = new Transaction();
            initialTx.setUserId(u.getUserId());
            initialTx.setType("deposit");
            initialTx.setAmount(initialDeposit);
            initialTx.setDescription("Initial deposit into account " + account.getAccountNumber());
            new TransactionDAO().create(initialTx);
        }

        JOptionPane.showMessageDialog(this, "Account created. You can login now.");
        this.dispose();
    }
}
