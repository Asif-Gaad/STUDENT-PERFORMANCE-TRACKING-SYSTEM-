package dao;

import db.DatabaseConnection;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student CRUD and search operations.
 */
public class StudentDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Read ────────────────────────────────────────────────────────────────────

    /** Returns all students ordered by name. */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY name";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns a single student by id, or null. */
    public Student getStudentById(int id) {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Searches students by name or email (case-insensitive, partial match).
     * @param keyword search term
     * @return matching students
     */
    public List<Student> searchStudents(String keyword) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE name LIKE ? OR email LIKE ? ORDER BY name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Filters students by enrollment date range.
     * @param from start date (inclusive)
     * @param to   end date   (inclusive)
     */
    public List<Student> filterByEnrollmentDate(Date from, Date to) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE enrollment_date BETWEEN ? AND ? ORDER BY enrollment_date";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Create ──────────────────────────────────────────────────────────────────

    /**
     * Inserts a new student.
     * @return generated id or -1 on failure
     */
    public int addStudent(Student s) {
        String sql = "INSERT INTO students (name, email, phone, dob, address, enrollment_date) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPhone());
            ps.setDate(4, s.getDob() != null ? new Date(s.getDob().getTime()) : null);
            ps.setString(5, s.getAddress());
            ps.setDate(6, s.getEnrollmentDate() != null ? new Date(s.getEnrollmentDate().getTime()) : new Date(System.currentTimeMillis()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ── Update ──────────────────────────────────────────────────────────────────

    /** Updates student record. Returns rows affected. */
    public int updateStudent(Student s) {
        String sql = "UPDATE students SET name=?, email=?, phone=?, dob=?, address=?, enrollment_date=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getPhone());
            ps.setDate(4, s.getDob() != null ? new Date(s.getDob().getTime()) : null);
            ps.setString(5, s.getAddress());
            ps.setDate(6, s.getEnrollmentDate() != null ? new Date(s.getEnrollmentDate().getTime()) : new Date(System.currentTimeMillis()));
            ps.setInt(7, s.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Delete ──────────────────────────────────────────────────────────────────

    /** Deletes student by id. Returns rows affected. */
    public int deleteStudent(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Validation helpers ──────────────────────────────────────────────────────

    /** Returns true if the given email is already used by a different student. */
    public boolean emailExists(String email, int excludeId) {
        String sql = "SELECT id FROM students WHERE email = ? AND id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Returns total count of students. */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Row mapping ─────────────────────────────────────────────────────────────

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDob(rs.getDate("dob"));
        s.setAddress(rs.getString("address"));
        s.setEnrollmentDate(rs.getDate("enrollment_date"));
        return s;
    }
}
