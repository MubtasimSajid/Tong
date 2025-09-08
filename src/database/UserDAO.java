package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import models.User;

public class UserDAO {
    public static void insertUser(User user) {
        String sql = "INSERT INTO users (id, full_name, display_name, email_address, password, random_identifier) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user.id);
            stmt.setString(2, user.fullName);
            stmt.setString(3, user.displayName);
            stmt.setString(4, user.emailAddress);
            stmt.setString(5, user.password);
            stmt.setString(6, user.randomIdentifier);

            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("UserDAO : insertUser");
        }
    }

    public static User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.id = rs.getInt("id");
                    user.fullName = rs.getString("full_name");
                    user.displayName = rs.getString("display_name");
                    user.emailAddress = rs.getString("email_address");
                    user.password = rs.getString("password");
                    user.randomIdentifier = rs.getString("random_identifier");
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static User getUserByEmail(String address) {
        String sql = "SELECT * FROM users WHERE email_address = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, address);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.id = rs.getInt("id");
                    user.fullName = rs.getString("full_name");
                    user.displayName = rs.getString("display_name");
                    user.emailAddress = rs.getString("email_address");
                    user.password = rs.getString("password");
                    user.randomIdentifier = rs.getString("random_identifier");

                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("No user found");
            return null;
        }
        return null;
    }

    public static boolean updateUserPassword(String email, String newHashedPassword) {
        String sql = "UPDATE users SET password = ? WHERE email_address = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("UserDAO : updateUserPassword");
        }
        return false;
    }

    public static User getUserByRandomID(String randID) {
        String sql = "SELECT * FROM users WHERE random_identifier = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, randID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.id = rs.getInt("id");
                    user.fullName = rs.getString("full_name");
                    user.displayName = rs.getString("display_name");
                    user.emailAddress = rs.getString("email_address");
                    user.password = rs.getString("password");
                    user.randomIdentifier = rs.getString("random_identifier");
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean updateUserRandomID(String email, String newRandomID) {
        String sql = "UPDATE users SET random_identifier = ? WHERE email_address = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newRandomID);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("UserDAO : updateUserRandomID");
        }
        return false;
    }

    public static boolean updateUserDisplayName(String email, String newDisplayName) {
        String sql = "UPDATE users SET display_name = ? WHERE email_address = ?";
        try (Connection conn = DatabaseHelper.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDisplayName);
            stmt.setString(2, email);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("UserDAO : updateUserDisplayName");
        }
        return false;
    }
}
