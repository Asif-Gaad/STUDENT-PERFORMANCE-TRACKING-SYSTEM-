package dao;

import db.DatabaseConnection;
import model.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Grade CRUD and report queries.
 */
public class GradeDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Read ────────────────────────────────────────────────────────────────────

    /** Returns all grades with student and course info joined. */
    public List<Grade> getAllGrades() {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM grades g "
                   + "JOIN enrollments en ON g.enrollment_id = en.id "
                   + "JOIN students   s  ON en.student_id    = s.id "
                   + "JOIN courses    c  ON en.course_id     = c.id "
                   + "ORDER BY s.name, c.course_name";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns grade by enrollment id, or null. */
    public Grade getGradeByEnrollmentId(int enrollmentId) {
        String sql = "SELECT g.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM grades g "
                   + "JOIN enrollments en ON g.enrollment_id = en.id "
                   + "JOIN students   s  ON en.student_id    = s.id "
                   + "JOIN courses    c  ON en.course_id     = c.id "
                   + "WHERE g.enrollment_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns grades for a specific course (used by Teacher grade-sheet report).
     * @param courseId course to report on
     * @param semester optional semester filter (null = all)
     */
    public List<Grade> getGradesByCourse(int courseId, String semester) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM grades g "
                   + "JOIN enrollments en ON g.enrollment_id = en.id "
                   + "JOIN students   s  ON en.student_id    = s.id "
                   + "JOIN courses    c  ON en.course_id     = c.id "
                   + "WHERE en.course_id = ?"
                   + (semester != null && !semester.isEmpty() ? " AND g.semester = ?" : "")
                   + " ORDER BY s.name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            if (semester != null && !semester.isEmpty()) ps.setString(2, semester);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns grades for a specific student. */
    public List<Grade> getGradesByStudent(int studentId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, s.name AS student_name, c.course_name, c.course_code "
                   + "FROM grades g "
                   + "JOIN enrollments en ON g.enrollment_id = en.id "
                   + "JOIN students   s  ON en.student_id    = s.id "
                   + "JOIN courses    c  ON en.course_id     = c.id "
                   + "WHERE en.student_id = ? ORDER BY c.course_name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Create / Update ─────────────────────────────────────────────────────────

    /** Inserts a new grade. Returns generated id or -1. */
    public int addGrade(Grade g) {
        String sql = "INSERT INTO grades (enrollment_id, grade_value, grade_letter, semester, academic_year) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getEnrollmentId());
            ps.setDouble(2, g.getGradeValue());
            ps.setString(3, g.getGradeLetter());
            ps.setString(4, g.getSemester());
            ps.setString(5, g.getAcademicYear());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Updates an existing grade record. Returns rows affected. */
    public int updateGrade(Grade g) {
        String sql = "UPDATE grades SET grade_value=?, grade_letter=?, semester=?, academic_year=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, g.getGradeValue());
            ps.setString(2, g.getGradeLetter());
            ps.setString(3, g.getSemester());
            ps.setString(4, g.getAcademicYear());
            ps.setInt(5, g.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Deletes grade by id. Returns rows affected. */
    public int deleteGrade(int id) {
        String sql = "DELETE FROM grades WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Returns total graded enrollments count. */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM grades";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Mapping ─────────────────────────────────────────────────────────────────

    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setId(rs.getInt("id"));
        g.setEnrollmentId(rs.getInt("enrollment_id"));
        g.setGradeValue(rs.getDouble("grade_value"));
        g.setGradeLetter(rs.getString("grade_letter"));
        g.setSemester(rs.getString("semester"));
        g.setAcademicYear(rs.getString("academic_year"));
        try { g.setStudentName(rs.getString("student_name")); } catch (SQLException ignored) {}
        try { g.setCourseName(rs.getString("course_name"));   } catch (SQLException ignored) {}
        try { g.setCourseCode(rs.getString("course_code"));   } catch (SQLException ignored) {}
        return g;
    }
}
