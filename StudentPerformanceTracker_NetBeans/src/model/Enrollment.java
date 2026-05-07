package model;

import java.util.Date;

/**
 * Model class representing the many-to-many link between Student and Course.
 */
public class Enrollment {

    private int    id;
    private int    studentId;
    private int    courseId;
    private Date   enrollmentDate;

    // Denormalized display fields (populated by JOIN queries)
    private String studentName;
    private String courseName;
    private String courseCode;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Enrollment() {}

    public Enrollment(int id, int studentId, int courseId, Date enrollmentDate) {
        this.id             = id;
        this.studentId      = studentId;
        this.courseId       = courseId;
        this.enrollmentDate = enrollmentDate;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public int    getId()                         { return id; }
    public void   setId(int id)                   { this.id = id; }

    public int    getStudentId()                  { return studentId; }
    public void   setStudentId(int studentId)     { this.studentId = studentId; }

    public int    getCourseId()                   { return courseId; }
    public void   setCourseId(int courseId)       { this.courseId = courseId; }

    public Date   getEnrollmentDate()             { return enrollmentDate; }
    public void   setEnrollmentDate(Date d)       { this.enrollmentDate = d; }

    public String getStudentName()                { return studentName; }
    public void   setStudentName(String s)        { this.studentName = s; }

    public String getCourseName()                 { return courseName; }
    public void   setCourseName(String c)         { this.courseName = c; }

    public String getCourseCode()                 { return courseCode; }
    public void   setCourseCode(String c)         { this.courseCode = c; }
}
