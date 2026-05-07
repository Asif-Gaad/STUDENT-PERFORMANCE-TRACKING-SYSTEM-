package model;

/**
 * Model class representing a Course entity.
 */
public class Course {

    private int    id;
    private String courseName;
    private String courseCode;
    private int    credits;
    private String teacherName;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Course() {}

    public Course(int id, String courseName, String courseCode,
                  int credits, String teacherName) {
        this.id          = id;
        this.courseName  = courseName;
        this.courseCode  = courseCode;
        this.credits     = credits;
        this.teacherName = teacherName;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public int    getId()                           { return id; }
    public void   setId(int id)                     { this.id = id; }

    public String getCourseName()                   { return courseName; }
    public void   setCourseName(String courseName)  { this.courseName = courseName; }

    public String getCourseCode()                   { return courseCode; }
    public void   setCourseCode(String courseCode)  { this.courseCode = courseCode; }

    public int    getCredits()                      { return credits; }
    public void   setCredits(int credits)           { this.credits = credits; }

    public String getTeacherName()                  { return teacherName; }
    public void   setTeacherName(String t)          { this.teacherName = t; }

    @Override
    public String toString() { return courseCode + " - " + courseName; }
}
