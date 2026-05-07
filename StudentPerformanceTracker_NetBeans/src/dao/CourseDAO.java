package dao;

import db.DatabaseConnection;
import model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course CRUD operations.
 */
public class CourseDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Read ────────────────────────────────────────────────────────────────────

    public List<Course> getAllCourses() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_name";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Course getCourseById(int id) {
        String sql = "SELECT * FROM courses WHERE id = ?";
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
     * Searches courses by name, code, or teacher (case-insensitive partial match).
     */
    public List<Course> searchCourses(String keyword) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE course_name LIKE ? OR course_code LIKE ? OR teacher_name LIKE ? ORDER BY course_name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Returns courses taught by a specific teacher. */
    public List<Course> getCoursesByTeacher(String teacherName) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE teacher_name = ? ORDER BY course_name";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, teacherName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Create ──────────────────────────────────────────────────────────────────

    public int addCourse(Course c) {
        String sql = "INSERT INTO courses (course_name, course_code, credits, teacher_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCourseName());
            ps.setString(2, c.getCourseCode());
            ps.setInt(3, c.getCredits());
            ps.setString(4, c.getTeacherName());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ── Update ──────────────────────────────────────────────────────────────────

    public int updateCourse(Course c) {
        String sql = "UPDATE courses SET course_name=?, course_code=?, credits=?, teacher_name=? WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, c.getCourseName());
            ps.setString(2, c.getCourseCode());
            ps.setInt(3, c.getCredits());
            ps.setString(4, c.getTeacherName());
            ps.setInt(5, c.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Delete ──────────────────────────────────────────────────────────────────

    public int deleteCourse(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Validation ──────────────────────────────────────────────────────────────

    public boolean courseCodeExists(String code, int excludeId) {
        String sql = "SELECT id FROM courses WHERE course_code = ? AND id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM courses";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Mapping ─────────────────────────────────────────────────────────────────

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
            rs.getInt("id"),
            rs.getString("course_name"),
            rs.getString("course_code"),
            rs.getInt("credits"),
            rs.getString("teacher_name")
        );
    }
}
