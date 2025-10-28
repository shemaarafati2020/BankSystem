package com.mybank.view;

import com.mybank.model.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ProfileFrame extends JFrame {
    private final User user;
    private final DashboardFrame parent;

    public ProfileFrame(User user, DashboardFrame parent) {
        this.user = user;
        this.parent = parent;
        setTitle("MyBank Pro - Profile Settings");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField nameField = new JTextField(user.getFullName());
        JTextField emailField = new JTextField(user.getEmail());
        JTextField phoneField = new JTextField(user.getPhone());
        JLabel photoLabel = new JLabel(user.getPhotoPath() == null ? "No photo uploaded" : user.getPhotoPath());
        JButton browseBtn = new JButton("Upload Photo");

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Profile Photo:"));
        formPanel.add(photoLabel);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                user.setPhotoPath(file.getAbsolutePath());
                photoLabel.setText(file.getAbsolutePath());
            }
        });

        saveBtn.addActionListener(e -> {
            user.setFullName(nameField.getText());
            user.setEmail(emailField.getText());
            user.setPhone(phoneField.getText());
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());
    }
}
