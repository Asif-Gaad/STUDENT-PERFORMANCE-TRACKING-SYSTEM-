package dao;

import db.DatabaseConnection;
import model.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Enrollment operations (student ↔ course links).
 */
public class EnrollmentDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Read ────────────────────────────────────────────────────────────────────

    /** Returns all enrollments with student and course names joined. */
    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM enrollments e "
                   + "JOIN students s ON e.student_id = s.id "
                   + "JOIN courses  c ON e.course_id  = c.id "
                   + "ORDER BY s.name, c.course_name";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns enrollments for a specific student. */
    public List<Enrollment> getEnrollmentsByStudent(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM enrollments e "
                   + "JOIN students s ON e.student_id = s.id "
                   + "JOIN courses  c ON e.course_id  = c.id "
                   + "WHERE e.student_id = ? ORDER BY c.course_name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns enrollments for a specific course. */
    public List<Enrollment> getEnrollmentsByCourse(int courseId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM enrollments e "
                   + "JOIN students s ON e.student_id = s.id "
                   + "JOIN courses  c ON e.course_id  = c.id "
                   + "WHERE e.course_id = ? ORDER BY s.name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns the enrollment id for a student-course pair, or -1. */
    public int getEnrollmentId(int studentId, int courseId) {
        String sql = "SELECT id FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ── Create ──────────────────────────────────────────────────────────────────

    /** Enrolls a student in a course. Returns generated id or -1. */
    public int addEnrollment(Enrollment en) {
        String sql = "INSERT INTO enrollments (student_id, course_id, enrollment_date) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, en.getStudentId());
            ps.setInt(2, en.getCourseId());
            ps.setDate(3, en.getEnrollmentDate() != null
                    ? new Date(en.getEnrollmentDate().getTime())
                    : new Date(System.currentTimeMillis()));
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ── Delete ──────────────────────────────────────────────────────────────────

    public int deleteEnrollment(int id) {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Validation ──────────────────────────────────────────────────────────────

    /** Returns true if the student is already enrolled in the course. */
    public boolean isAlreadyEnrolled(int studentId, int courseId) {
        String sql = "SELECT id FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Returns total enrollment count. */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM enrollments";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Mapping ─────────────────────────────────────────────────────────────────

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setCourseId(rs.getInt("course_id"));
        e.setEnrollmentDate(rs.getDate("enrollment_date"));
        // Joined fields (may be null if raw query without JOIN)
        try { e.setStudentName(rs.getString("student_name")); } catch (SQLException ignored) {}
        try { e.setCourseName(rs.getString("course_name"));   } catch (SQLException ignored) {}
        try { e.setCourseCode(rs.getString("course_code"));   } catch (SQLException ignored) {}
        return e;
    }
}
