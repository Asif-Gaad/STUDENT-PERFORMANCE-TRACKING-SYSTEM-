package model;

/**
 * Model class representing a Grade entity linked to an Enrollment.
 */
public class Grade {

    private int    id;
    private int    enrollmentId;
    private double gradeValue;
    private String gradeLetter;
    private String semester;
    private String academicYear;

    // Denormalized display fields
    private String studentName;
    private String courseName;
    private String courseCode;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Grade() {}

    public Grade(int id, int enrollmentId, double gradeValue,
                 String gradeLetter, String semester, String academicYear) {
        this.id           = id;
        this.enrollmentId = enrollmentId;
        this.gradeValue   = gradeValue;
        this.gradeLetter  = gradeLetter;
        this.semester     = semester;
        this.academicYear = academicYear;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public int    getId()                           { return id; }
    public void   setId(int id)                     { this.id = id; }

    public int    getEnrollmentId()                 { return enrollmentId; }
    public void   setEnrollmentId(int e)            { this.enrollmentId = e; }

    public double getGradeValue()                   { return gradeValue; }
    public void   setGradeValue(double g)           { this.gradeValue = g; }

    public String getGradeLetter()                  { return gradeLetter; }
    public void   setGradeLetter(String g)          { this.gradeLetter = g; }

    public String getSemester()                     { return semester; }
    public void   setSemester(String s)             { this.semester = s; }

    public String getAcademicYear()                 { return academicYear; }
    public void   setAcademicYear(String a)         { this.academicYear = a; }

    public String getStudentName()                  { return studentName; }
    public void   setStudentName(String s)          { this.studentName = s; }

    public String getCourseName()                   { return courseName; }
    public void   setCourseName(String c)           { this.courseName = c; }

    public String getCourseCode()                   { return courseCode; }
    public void   setCourseCode(String c)           { this.courseCode = c; }

    /** Returns "Pass" if grade >= 50, "Fail" otherwise. */
    public String getStatus() { return gradeValue >= 50 ? "Pass" : "Fail"; }
}
