package com.mybank.view;

import com.mybank.dao.AccountDAO;
import com.mybank.dao.TransactionDAO;
import com.mybank.dao.UserDAO;
import com.mybank.model.Account;
import com.mybank.model.Transaction;
import com.mybank.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final User user;
    private JLabel balanceLabel;
    private JLabel monthlyDepositLabel;
    private JLabel monthlyOutflowLabel;
    private JLabel insightsLabel;
    private JPanel recentTransactionsPanel;
    private JPanel accountsPanel;
    private JProgressBar savingsGoalBar;
    private JLabel savingsGoalLabel;
    private JProgressBar emergencyGoalBar;
    private JLabel emergencyGoalLabel;

    private static final BigDecimal SAVINGS_TARGET = new BigDecimal("500000.00");
    private static final BigDecimal EMERGENCY_TARGET = new BigDecimal("300000.00");

    public DashboardFrame(User user) {
        this.user = user;
        setTitle("BankSystem - Dashboard");
        setSize(1150, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        refreshBalances();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        JPanel topBar = buildTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);

        JPanel sideBar = buildSideBar();
        mainPanel.add(sideBar, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel overview = new JPanel();
        overview.setLayout(new BoxLayout(overview, BoxLayout.Y_AXIS));
        center.add(overview, BorderLayout.CENTER);

        JPanel statsRow = new JPanel(new GridLayout(1, 3, 16, 16));
        statsRow.add(createStatCard("Total Balance", balanceLabel = createValueLabel(), "Across all linked accounts"));
        statsRow.add(createStatCard("Monthly Inflows", monthlyDepositLabel = createValueLabel(), "Deposits & incoming transfers"));
        statsRow.add(createStatCard("Monthly Outflows", monthlyOutflowLabel = createValueLabel(), "Withdrawals & sent transfers"));
        overview.add(statsRow);
        overview.add(Box.createVerticalStrut(16));

        accountsPanel = new JPanel();
        accountsPanel.setLayout(new BoxLayout(accountsPanel, BoxLayout.Y_AXIS));
        JScrollPane accountsScroll = new JScrollPane(accountsPanel);
        accountsScroll.setBorder(BorderFactory.createEmptyBorder());
        accountsScroll.setPreferredSize(new Dimension(0, 160));
        overview.add(createSectionCard("Accounts", accountsScroll));
        overview.add(Box.createVerticalStrut(16));

        insightsLabel = new JLabel("Loading insights...");
        insightsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        overview.add(createSectionCard("Insights", insightsLabel));
        overview.add(Box.createVerticalStrut(16));

        overview.add(createSectionCard("Financial Goals", buildGoalsPanel()));
        overview.add(Box.createVerticalStrut(16));

        recentTransactionsPanel = new JPanel();
        recentTransactionsPanel.setLayout(new BoxLayout(recentTransactionsPanel, BoxLayout.Y_AXIS));
        JScrollPane recentScroll = new JScrollPane(recentTransactionsPanel);
        recentScroll.setBorder(BorderFactory.createEmptyBorder());
        recentScroll.setPreferredSize(new Dimension(0, 220));
        overview.add(createSectionCard("Recent Transactions", recentScroll));

        mainPanel.add(center, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x2F3E46));
        topBar.setPreferredSize(new Dimension(1100, 90));
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Welcome, " + (user.getFullName() == null ? user.getUsername() : user.getFullName()));
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topBar.add(title, BorderLayout.WEST);

        JLabel profilePic = new JLabel();
        profilePic.setPreferredSize(new Dimension(72, 72));
        profilePic.setHorizontalAlignment(SwingConstants.RIGHT);
        if (user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            try {
                Image img = ImageIO.read(new File(user.getPhoto())).getScaledInstance(72, 72, Image.SCALE_SMOOTH);
                profilePic.setIcon(new ImageIcon(makeCircular(img)));
            } catch (IOException e) {
                profilePic.setIcon(defaultProfileIcon());
            }
        } else {
            profilePic.setIcon(defaultProfileIcon());
        }
        topBar.add(profilePic, BorderLayout.EAST);
        return topBar;
    }

    private JPanel buildSideBar() {
        JPanel sideBar = new JPanel();
        sideBar.setLayout(new GridLayout(10, 1, 10, 10));
        sideBar.setBorder(new EmptyBorder(20, 20, 20, 20));
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

        depositBtn.addActionListener(e -> new DepositFrame(user, this).setVisible(true));
        withdrawBtn.addActionListener(e -> new WithdrawFrame(user, this).setVisible(true));
        transferBtn.addActionListener(e -> new TransferFrame(user, this).setVisible(true));
        statementsBtn.addActionListener(e -> new TransactionHistoryFrame(user).setVisible(true));
        profileBtn.addActionListener(e -> new ProfileFrame(user, this).setVisible(true));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        return sideBar;
    }

    private JPanel buildGoalsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 12, 12));
        savingsGoalBar = new JProgressBar(0, 100);
        savingsGoalBar.setStringPainted(true);
        savingsGoalLabel = new JLabel("--");
        panel.add(createGoalItem("Savings Cushion", savingsGoalBar, savingsGoalLabel));

        emergencyGoalBar = new JProgressBar(0, 100);
        emergencyGoalBar.setForeground(new Color(0xFF8C42));
        emergencyGoalBar.setStringPainted(true);
        emergencyGoalLabel = new JLabel("--");
        panel.add(createGoalItem("Emergency Fund", emergencyGoalBar, emergencyGoalLabel));
        return panel;
    }

    private JPanel createGoalItem(String title, JProgressBar bar, JLabel helper) {
        JPanel wrapper = new JPanel(new BorderLayout(6, 6));
        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(bar, BorderLayout.CENTER);
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        wrapper.add(helper, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xDADADA), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(0x5A5A5A));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(subLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel createValueLabel() {
        JLabel lbl = new JLabel("--");
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        return lbl;
    }

    private JPanel createSectionCard(String title, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE0E0E0), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public void refreshBalances() {
        UserDAO udao = new UserDAO();
        User fresh = udao.findById(user.getUserId());
        if (fresh != null) {
            user.setBalance(fresh.getBalance());
            user.setFullName(fresh.getFullName());
            user.setEmail(fresh.getEmail());
            user.setPhone(fresh.getPhone());
            user.setAddress(fresh.getAddress());
            user.setPhoto(fresh.getPhoto());
        }

        AccountDAO accountDAO = new AccountDAO();
        List<Account> accounts = accountDAO.accountsForUser(user.getUserId());
        accountsPanel.removeAll();
        BigDecimal aggregatedBalance = BigDecimal.ZERO;
        if (accounts.isEmpty()) {
            accountsPanel.add(new JLabel("No accounts provisioned yet."));
        } else {
            for (Account account : accounts) {
                aggregatedBalance = aggregatedBalance.add(account.getBalance());
                JPanel row = new JPanel(new BorderLayout());
                row.setBorder(new EmptyBorder(6, 0, 6, 0));
                JLabel left = new JLabel(account.getAccountType() + " • " + account.getAccountNumber());
                left.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                JLabel right = new JLabel(formatCurrency(account.getBalance()));
                right.setFont(new Font("Segoe UI", Font.BOLD, 14));
                row.add(left, BorderLayout.WEST);
                row.add(right, BorderLayout.EAST);
                accountsPanel.add(row);
            }
        }
        accountsPanel.revalidate();
        accountsPanel.repaint();

        user.setBalance(aggregatedBalance.doubleValue());
        balanceLabel.setText(formatCurrency(aggregatedBalance));

        TransactionDAO tdao = new TransactionDAO();
        YearMonth month = YearMonth.now();
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal depositSum = tdao.sumByTypeBetween(user.getUserId(), "deposit", start, end);
        BigDecimal withdrawSum = tdao.sumByTypeBetween(user.getUserId(), "withdraw", start, end);
        BigDecimal transferNet = tdao.sumByTypeBetween(user.getUserId(), "transfer", start, end);

        BigDecimal transferIn = transferNet.max(BigDecimal.ZERO);
        BigDecimal transferOut = transferNet.min(BigDecimal.ZERO).abs();
        BigDecimal inflow = depositSum.add(transferIn);
        BigDecimal outflow = withdrawSum.abs().add(transferOut);
        BigDecimal net = inflow.subtract(outflow);

        monthlyDepositLabel.setText(formatCurrency(inflow));
        monthlyOutflowLabel.setText(formatCurrency(outflow));

        Transaction latest = tdao.findLatestForUser(user.getUserId());
        String latestText = latest == null ? "No transactions yet." :
                String.format("%s • %s • %s", latest.getCreatedAt(), latest.getType().toUpperCase(), formatCurrency(latest.getAmount()));
        insightsLabel.setText(String.format("<html><b>Net cashflow:</b> %s &nbsp;&nbsp; <b>Latest:</b> %s</html>",
                formatCurrency(net), latestText));

        updateGoal(savingsGoalBar, savingsGoalLabel, inflow, SAVINGS_TARGET, "monthly inflow target");
        updateGoal(emergencyGoalBar, emergencyGoalLabel, aggregatedBalance, EMERGENCY_TARGET, "emergency reserve");

        List<Transaction> recent = tdao.findRecentByUser(user.getUserId());
        recentTransactionsPanel.removeAll();
        if (recent.isEmpty()) {
            recentTransactionsPanel.add(new JLabel("No recent transactions."));
        } else {
            for (Transaction t : recent) {
                JLabel lbl = new JLabel(String.format("%s | %s | %s", t.getCreatedAt(), t.getType(), formatCurrency(t.getAmount())));
                lbl.setBorder(new EmptyBorder(4, 0, 4, 0));
                recentTransactionsPanel.add(lbl);
            }
        }
        recentTransactionsPanel.revalidate();
        recentTransactionsPanel.repaint();
    }

    private void updateGoal(JProgressBar bar, JLabel label, BigDecimal current, BigDecimal target, String caption) {
        if (current == null) current = BigDecimal.ZERO;
        if (target.compareTo(BigDecimal.ZERO) <= 0) {
            bar.setValue(0);
            bar.setString("0%");
            label.setText("No goal defined");
            return;
        }
        BigDecimal ratio = current.divide(target, 4, RoundingMode.HALF_UP);
        int percent = ratio.compareTo(BigDecimal.ONE) >= 0 ? 100 : ratio.multiply(BigDecimal.valueOf(100)).intValue();
        bar.setValue(percent);
        bar.setString(percent + "%");
        label.setText(String.format("%s of %s %s", formatCurrency(current), formatCurrency(target), caption));
    }

    private ImageIcon defaultProfileIcon() {
        BufferedImage img = new BufferedImage(72, 72, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x4E4E4E));
        g.fillOval(0, 0, 72, 72);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g.drawString("U", 26, 46);
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

    private String formatCurrency(BigDecimal amount) {
        return String.format("USh %,.2f", amount);
    }

    private String formatCurrency(double amount) {
        return formatCurrency(BigDecimal.valueOf(amount));
    }
}
