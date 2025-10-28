package com.mybank.view;

import com.mybank.dao.UserDAO;
import com.mybank.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("MyBank Pro - Login");
        setSize(420,230);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridLayout(3,2,6,6));
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.add(new JLabel("Username:"));
        userField = new JTextField();
        form.add(userField);
        form.add(new JLabel("Password:"));
        passField = new JPasswordField();
        form.add(passField);

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");
        JPanel btns = new JPanel();
        btns.add(login); btns.add(register);
        p.add(form, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        add(p);

        login.addActionListener(e -> doLogin());
        register.addActionListener(e -> new RegisterFrame().setVisible(true));
    }

    private void doLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter username and password."); return; }
        UserDAO ud = new UserDAO();
        User user = ud.findByUsername(u);
        if (user == null || !ud.verifyPassword(user, p)) { JOptionPane.showMessageDialog(this, "Invalid credentials."); return; }
        // open dashboard
        DashboardFrame df = new DashboardFrame(user);
        df.setVisible(true);
        this.dispose();
    }
}
