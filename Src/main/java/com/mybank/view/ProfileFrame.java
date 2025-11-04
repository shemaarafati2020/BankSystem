package com.mybank.view;

import com.mybank.dao.UserDAO;
import com.mybank.model.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ProfileFrame extends JFrame {
    private final User user;
    private final DashboardFrame parent;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JLabel photoLabel;
    private File selectedPhoto;
    private JLabel statusLabel;

    public ProfileFrame(User user, DashboardFrame parent) {
        this.user = user;
        this.parent = parent;
        setTitle("MyBank Pro - Profile Settings");
        setSize(520, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        nameField = new JTextField(user.getFullName());
        emailField = new JTextField(user.getEmail());
        phoneField = new JTextField(user.getPhone());
        addressField = new JTextField(user.getAddress());

        addRow(formPanel, gbc, "Full Name:", nameField);
        addRow(formPanel, gbc, "Email:", emailField);
        addRow(formPanel, gbc, "Phone:", phoneField);
        addRow(formPanel, gbc, "Address:", addressField);

        JButton browseBtn = new JButton("Upload Photo");
        photoLabel = new JLabel(user.getPhoto() == null || user.getPhoto().isEmpty() ? "No photo uploaded" : user.getPhoto());
        browseBtn.addActionListener(e -> choosePhoto());

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Profile Photo:"), gbc);
        gbc.gridx = 1;
        JPanel photoPanel = new JPanel(new BorderLayout(6, 6));
        photoPanel.add(browseBtn, BorderLayout.WEST);
        photoPanel.add(photoLabel, BorderLayout.CENTER);
        formPanel.add(photoPanel, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(0x2F3E46));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(statusLabel);
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        saveBtn.addActionListener(e -> saveProfile());
        cancelBtn.addActionListener(e -> dispose());
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
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedPhoto = chooser.getSelectedFile();
            photoLabel.setText(selectedPhoto.getAbsolutePath());
        }
    }

    private void saveProfile() {
        user.setFullName(nameField.getText());
        user.setEmail(emailField.getText());
        user.setPhone(phoneField.getText());
        user.setAddress(addressField.getText());
        if (selectedPhoto != null) {
            user.setPhoto(selectedPhoto.getAbsolutePath());
        }

        UserDAO dao = new UserDAO();
        boolean ok = dao.updateProfile(user);
        if (ok) {
            statusLabel.setText("Profile updated successfully.");
            parent.refreshBalances();
        } else {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Failed to update profile.");
        }
    }
}
