package dao;

import db.DatabaseConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User authentication and management.
 * Supports admin, teacher, and student roles.
 */
public class UserDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Validates credentials and returns the matching User or null.
     * Loads the student_id field for student-role accounts.
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /** Returns all users ordered by role then username. */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, username";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Inserts a new user.
     * For student-role accounts, pass the matching students.id in user.getStudentId().
     * @return generated id, or -1 on failure.
     */
    public int addUser(User user) {
        String sql = "INSERT INTO users (username, password, role, student_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            if (user.getStudentId() > 0) ps.setInt(4, user.getStudentId());
            else                         ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Updates an existing user. Returns rows affected. */
    public int updateUser(User user) {
        String sql = "UPDATE users SET username=?, password=?, role=?, student_id=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            if (user.getStudentId() > 0) ps.setInt(4, user.getStudentId());
            else                         ps.setNull(4, Types.INTEGER);
            ps.setInt(5, user.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Deletes a user by id. Returns rows affected. */
    public int deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns true if the given username is already taken.
     * @param excludeId pass 0 for new users, or the user's own id when editing.
     */
    public boolean usernameExists(String username, int excludeId) {
        String sql = excludeId > 0
            ? "SELECT id FROM users WHERE username = ? AND id != ?"
            : "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            if (excludeId > 0) ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks whether a student already has a portal account linked.
     * @param studentId the students.id to check
     * @param excludeUserId pass 0 if creating, own user id if editing
     */
    public boolean studentAccountExists(int studentId, int excludeUserId) {
        String sql = excludeUserId > 0
            ? "SELECT id FROM users WHERE student_id = ? AND id != ?"
            : "SELECT id FROM users WHERE student_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            if (excludeUserId > 0) ps.setInt(2, excludeUserId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Row mapping ───────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role")
        );
        int sid = rs.getInt("student_id");
        u.setStudentId(rs.wasNull() ? 0 : sid);
        return u;
    }
}
