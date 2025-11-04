package com.mybank.dao;

import com.mybank.DBConnection;
import com.mybank.model.Account;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public Account createAccount(int userId, String accountNumber, String type, BigDecimal initialBalance) {
        String sql = "INSERT INTO accounts (user_id, account_number, account_type, balance) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, accountNumber);
            ps.setString(3, type);
            ps.setBigDecimal(4, initialBalance);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getInt(1));
                    a.setUserId(userId);
                    a.setAccountNumber(accountNumber);
                    a.setAccountType(type);
                    a.setBalance(initialBalance);
                    return a;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Account> accountsForUser(int userId) {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        List<Account> out = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getInt("account_id"));
                    a.setUserId(rs.getInt("user_id"));
                    a.setAccountNumber(rs.getString("account_number"));
                    a.setAccountType(rs.getString("account_type"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    out.add(a);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    public Account findByAccountNumber(String accNum) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, accNum);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account a = new Account();
                    a.setAccountId(rs.getInt("account_id"));
                    a.setUserId(rs.getInt("user_id"));
                    a.setAccountNumber(rs.getString("account_number"));
                    a.setAccountType(rs.getString("account_type"));
                    a.setBalance(rs.getBigDecimal("balance"));
                    return a;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateBalance(Connection c, int accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() == 1;
        }
    }
}
