package model;

import java.util.Date;

/**
 * Model class representing a Student entity.
 */
public class Student {

    private int    id;
    private String name;
    private String email;
    private String phone;
    private Date   dob;
    private String address;
    private Date   enrollmentDate;

    // ── Constructors ────────────────────────────────────────────────────────────

    public Student() {}

    public Student(int id, String name, String email, String phone,
                   Date dob, String address, Date enrollmentDate) {
        this.id             = id;
        this.name           = name;
        this.email          = email;
        this.phone          = phone;
        this.dob            = dob;
        this.address        = address;
        this.enrollmentDate = enrollmentDate;
    }

    // ── Getters & Setters ───────────────────────────────────────────────────────

    public int    getId()                      { return id; }
    public void   setId(int id)                { this.id = id; }

    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }

    public String getEmail()                   { return email; }
    public void   setEmail(String email)       { this.email = email; }

    public String getPhone()                   { return phone; }
    public void   setPhone(String phone)       { this.phone = phone; }

    public Date   getDob()                     { return dob; }
    public void   setDob(Date dob)             { this.dob = dob; }

    public String getAddress()                 { return address; }
    public void   setAddress(String address)   { this.address = address; }

    public Date   getEnrollmentDate()              { return enrollmentDate; }
    public void   setEnrollmentDate(Date d)        { this.enrollmentDate = d; }

    @Override
    public String toString() { return name; }
}
