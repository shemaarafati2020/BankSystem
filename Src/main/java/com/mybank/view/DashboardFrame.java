package com.mybank.view;

import com.mybank.dao.TransactionDAO;
import com.mybank.dao.UserDAO;
import com.mybank.model.Transaction;
import com.mybank.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final User user;
    private JLabel balanceLabel;
    private JPanel recentTransactionsPanel;

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("BankSystem - Dashboard");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        // initial refresh
        refreshBalances();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x2F3E46));
        topBar.setPreferredSize(new Dimension(1100, 80));
        JLabel title = new JLabel("Welcome, " + (user.getFullName() == null ? user.getUsername() : user.getFullName()));
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        JLabel profilePic = new JLabel();
        profilePic.setPreferredSize(new Dimension(64, 64));
        profilePic.setHorizontalAlignment(SwingConstants.RIGHT);
        if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            try {
                Image img = ImageIO.read(new File(user.getPhoto())).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                profilePic.setIcon(new ImageIcon(makeCircular(img)));
            } catch (IOException e) {
                profilePic.setIcon(defaultProfileIcon());
            }
        } else {
            profilePic.setIcon(defaultProfileIcon());
        }

        topBar.add(title, BorderLayout.WEST);
        topBar.add(profilePic, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Sidebar
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new GridLayout(10, 1, 6, 6));
        sideBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        sideBar.setBackground(new Color(0xCAD2C5));

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        JButton statementsBtn = new JButton("Statements");
        JButton profileBtn = new JButton("Profile");
        JButton logoutBtn = new JButton("Logout");

        sideBar.add(depositBtn);
        sideBar.add(withdrawBtn);
        sideBar.add(transferBtn);
        sideBar.add(statementsBtn);
        sideBar.add(profileBtn);
        sideBar.add(logoutBtn);

        mainPanel.add(sideBar, BorderLayout.WEST);

        // Center
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Balance card
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        balancePanel.setOpaque(false);
        JLabel labelTitle = new JLabel("Available Balance:");
        labelTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        balanceLabel = new JLabel("Loading...");
        balanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        balancePanel.add(labelTitle);
        balancePanel.add(balanceLabel);

        // Recent transactions area
        recentTransactionsPanel = new JPanel();
        recentTransactionsPanel.setLayout(new BoxLayout(recentTransactionsPanel, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(recentTransactionsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));

        center.add(balancePanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        mainPanel.add(center, BorderLayout.CENTER);

        // Actions
        depositBtn.addActionListener(e -> new DepositFrame(user).setVisible(true));
        withdrawBtn.addActionListener(e -> new WithdrawFrame(user).setVisible(true));
        transferBtn.addActionListener(e -> new TransferFrame(user, this).setVisible(true));
        statementsBtn.addActionListener(e -> new TransactionHistoryFrame(user).setVisible(true));
        profileBtn.addActionListener(e -> new ProfileFrame(user, this).setVisible(true));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private ImageIcon defaultProfileIcon() {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.GRAY);
        g.fillOval(0, 0, 64, 64);
        g.setColor(Color.WHITE);
        g.drawString("U", 26, 40);
        g.dispose();
        return new ImageIcon(img);
    }

    private Image makeCircular(Image img) {
        int size = Math.min(img.getWidth(null), img.getHeight(null));
        BufferedImage mask = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mask.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(img, 0, 0, size, size, null);
        g2.dispose();
        return mask;
    }

    /**
     * Public method called by TransferFrame (and others) to refresh amounts & recent tx
     */
    public void refreshBalances() {
        // refresh user object from database
        UserDAO udao = new UserDAO();
        User fresh = udao.findByUsername(user.getUsername());
        if (fresh != null) {
            user.setBalance(fresh.getBalance());
            balanceLabel.setText(String.format("USh %.2f", user.getBalance()));
        }

        // refresh recent transactions
        TransactionDAO tdao = new TransactionDAO();
        List<Transaction> txs = tdao.findRecentByUser(user.getUserId());
        recentTransactionsPanel.removeAll();
        if (txs.isEmpty()) {
            recentTransactionsPanel.add(new JLabel("No recent transactions."));
        } else {
            for (Transaction t : txs) {
                JLabel lbl = new JLabel(String.format("%s | %s | %s", t.getCreatedAt(), t.getType(), t.getAmount()));
                lbl.setBorder(new EmptyBorder(6, 6, 6, 6));
                recentTransactionsPanel.add(lbl);
            }
        }
        recentTransactionsPanel.revalidate();
        recentTransactionsPanel.repaint();
    }
}
