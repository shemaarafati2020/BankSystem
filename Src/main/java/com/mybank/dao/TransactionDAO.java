package com.mybank.dao;

import com.mybank.DBConnection;
import com.mybank.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final String INSERT_SQL =
            "INSERT INTO transactions (user_id, type, amount, description) VALUES (?,?,?,?)";

    /**
     * Persist a transaction using a fresh connection.
     */
    public boolean create(Transaction t) {
        try (Connection c = DBConnection.getConnection()) {
            return create(c, t);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Persist a transaction using an existing connection (for transactional flows).
     */
    public boolean create(Connection c, Transaction t) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getType());
            ps.setBigDecimal(3, t.getAmount());
            ps.setString(4, t.getDescription());
            int updated = ps.executeUpdate();
            if (updated == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        t.setTransactionId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Return the latest five transactions for a user (used on the dashboard).
     */
    public List<Transaction> findRecentByUser(int userId) {
        List<Transaction> out = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT 5";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Fetch the most recent transaction for a user, if any.
     */
    public Transaction findLatestForUser(int userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Full history for the user with optional type filtering.
     */
    public List<Transaction> forUser(int userId, String typeFilter) {
        List<Transaction> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE user_id = ?");
        boolean filterType = typeFilter != null && !typeFilter.equalsIgnoreCase("all") && !typeFilter.isBlank();
        if (filterType) {
            sql.append(" AND type = ?");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            ps.setInt(1, userId);
            if (filterType) {
                ps.setString(2, typeFilter.toLowerCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Sum the signed amounts for a user between start (inclusive) and end (exclusive).
     */
    public BigDecimal sumBetween(int userId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id = ? AND created_at >= ? AND created_at < ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal val = rs.getBigDecimal(1);
                    return val != null ? val : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Sum the signed amounts for a given transaction type within a period.
     */
    public BigDecimal sumByTypeBetween(int userId, String type, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id = ? AND type = ? AND created_at >= ? AND created_at < ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type.toLowerCase());
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal val = rs.getBigDecimal(1);
                    return val != null ? val : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setUserId(rs.getInt("user_id"));
        t.setType(rs.getString("type"));
        t.setAmount(rs.getBigDecimal("amount"));
        t.setDescription(rs.getString("description"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
