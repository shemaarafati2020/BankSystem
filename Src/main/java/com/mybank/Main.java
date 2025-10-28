package com.mybank;

import javax.swing.SwingUtilities;
import com.mybank.view.LoginFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
