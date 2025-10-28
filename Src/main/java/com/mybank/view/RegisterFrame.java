package com.mybank.view;

import com.mybank.dao.UserDAO;
import com.mybank.dao.AccountDAO;
import com.mybank.model.User;
import com.mybank.model.Account;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.math.BigDecimal;

public class RegisterFrame extends JFrame {
    private JTextField userF, nameF, emailF, phoneF, addrF;
    private JPasswordField passF;
    private JLabel photoLabel;
    private File selectedPhoto;

    public RegisterFrame() {
        setTitle("Register - MyBank Pro");
        setSize(520,360);
        setLocationRelativeTo(null);
        init();
    }

    private void init() {
        JPanel p = new JPanel(new GridLayout(7,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        p.add(new JLabel("Username:")); userF = new JTextField(); p.add(userF);
        p.add(new JLabel("Password:")); passF = new JPasswordField(); p.add(passF);
        p.add(new JLabel("Full name:")); nameF = new JTextField(); p.add(nameF);
        p.add(new JLabel("Email:")); emailF = new JTextField(); p.add(emailF);
        p.add(new JLabel("Phone:")); phoneF = new JTextField(); p.add(phoneF);
        p.add(new JLabel("Address:")); addrF = new JTextField(); p.add(addrF);

        JButton choose = new JButton("Choose Photo"); photoLabel = new JLabel("No photo chosen");
        choose.addActionListener(e -> choosePhoto());
        p.add(choose); p.add(photoLabel);

        JButton create = new JButton("Create Account"); JButton cancel = new JButton("Cancel");
        p.add(create); p.add(cancel);
        add(p);

        create.addActionListener(e -> doCreate());
        cancel.addActionListener(e -> this.dispose());
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
        if (username.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(this, "Username and password required"); return; }
        UserDAO ud = new UserDAO();
        if (ud.findByUsername(username) != null) { JOptionPane.showMessageDialog(this, "User exists"); return; }
        User u = new User();
        u.setUsername(username); u.setPassword(pass); u.setFullName(nameF.getText()); u.setEmail(emailF.getText()); u.setPhone(phoneF.getText()); u.setAddress(addrF.getText());
        String photoPath = null;
        if (selectedPhoto != null) {
            try {
                String ext = selectedPhoto.getName().substring(selectedPhoto.getName().lastIndexOf('.'));
                File dest = new File("data/profile_photos/" + username + ext);
                dest.getParentFile().mkdirs();
                Files.copy(selectedPhoto.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                photoPath = dest.getPath();
                u.setPhoto(photoPath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        boolean ok = ud.create(u);
        if (!ok) { JOptionPane.showMessageDialog(this, "Failed to create user."); return; }
        // create default account
        AccountDAO ad = new AccountDAO();
        String accNum = "ACC" + (System.currentTimeMillis() % 1000000);
        ad.createAccount(ud.findByUsername(username).getUserId(), accNum, "Savings", new BigDecimal("0.00"));
        JOptionPane.showMessageDialog(this, "Account created. You can login now."); this.dispose();
    }
}
