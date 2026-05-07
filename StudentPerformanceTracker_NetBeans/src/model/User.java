package model;

/**
 * Model class representing an application user (Admin / Teacher / Student).
 */
public class User {

    private int    id;
    private String username;
    private String password;
    private String role;          // "admin", "teacher", or "student"
    private int    studentId;     // only meaningful when role == "student"; 0 otherwise

    // ── Constructors ──────────────────────────────────────────────────────────

    public User() {}

    public User(int id, String username, String password, String role) {
        this.id       = id;
        this.username = username;
        this.password = password;
        this.role     = role;
        this.studentId = 0;
    }

    public User(int id, String username, String password, String role, int studentId) {
        this.id        = id;
        this.username  = username;
        this.password  = password;
        this.role      = role;
        this.studentId = studentId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getId()                   { return id; }
    public void   setId(int id)             { this.id = id; }

    public String getUsername()             { return username; }
    public void   setUsername(String u)     { this.username = u; }

    public String getPassword()             { return password; }
    public void   setPassword(String p)     { this.password = p; }

    public String getRole()                 { return role; }
    public void   setRole(String role)      { this.role = role; }

    public int    getStudentId()            { return studentId; }
    public void   setStudentId(int sid)     { this.studentId = sid; }

    // ── Role helpers ──────────────────────────────────────────────────────────

    public boolean isAdmin()   { return "admin".equalsIgnoreCase(role); }
    public boolean isTeacher() { return "teacher".equalsIgnoreCase(role); }
    public boolean isStudent() { return "student".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
