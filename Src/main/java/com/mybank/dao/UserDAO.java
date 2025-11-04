package com.mybank.dao;

import com.mybank.DBConnection;
import com.mybank.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserDAO {

    /**
     * Find a user by username.
     */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find a user by id.
     */
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create a new user record.
     */
    public boolean create(User u) {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, address, photo, role, balance) VALUES (?,?,?,?,?,?,?,?,?)";
        String hashed = BCrypt.hashpw(u.getPassword(), BCrypt.gensalt(12));

        // Normalize role for MySQL ENUM('user','admin')
        String safeRole = (u.getRole() == null ||
                (!u.getRole().equalsIgnoreCase("admin") && !u.getRole().equalsIgnoreCase("user")))
                ? "user"
                : u.getRole().toLowerCase();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, hashed);
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setString(5, u.getPhone());
            ps.setString(6, u.getAddress());
            ps.setString(7, u.getPhoto());
            ps.setString(8, safeRole);
            ps.setDouble(9, u.getBalance());

            int affected = ps.executeUpdate();
            if (affected == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        u.setUserId(rs.getInt(1));
                    }
                }
                System.out.println("✅ User created successfully: " + u.getUsername());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error creating user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update profile details for a user.
     */
    public boolean updateProfile(User u) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ?, photo = ? WHERE user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getAddress());
            ps.setString(5, u.getPhoto());
            ps.setInt(6, u.getUserId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("❌ Error updating profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verify a user's password (using bcrypt).
     */
    public boolean verifyPassword(User u, String plainPassword) {
        if (u == null || u.getPassword() == null) return false;
        return BCrypt.checkpw(plainPassword, u.getPassword());
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password")); // hashed password
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setPhoto(rs.getString("photo"));
        u.setRole(rs.getString("role"));
        try {
            u.setBalance(rs.getDouble("balance"));
        } catch (SQLException ignore) {
            u.setBalance(0.0);
        }
        return u;
    }
}
