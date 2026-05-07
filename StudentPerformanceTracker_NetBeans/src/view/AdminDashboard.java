package view;

import dao.*;
import model.*;
import utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Main dashboard for Admin users.
 * Fully resizable and opens maximized.
 * Provides tabbed navigation for Students, Courses, Enrollments, Users, and Reports.
 */
public class AdminDashboard extends JFrame {

    private final StudentDAO    studentDAO    = new StudentDAO();
    private final CourseDAO     courseDAO     = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final GradeDAO      gradeDAO      = new GradeDAO();
    private final UserDAO       userDAO       = new UserDAO();

    // Student tab
    private JTable            tblStudents;
    private DefaultTableModel mdlStudents;
    private JTextField        tfStudentSearch;

    // Course tab
    private JTable            tblCourses;
    private DefaultTableModel mdlCourses;
    private JTextField        tfCourseSearch;

    // Enrollment tab
    private JTable            tblEnrollments;
    private DefaultTableModel mdlEnrollments;
    private JComboBox<String> cbStudentFilter;
    private JComboBox<String> cbCourseFilter;

    // Users tab
    private JTable            tblUsers;
    private DefaultTableModel mdlUsers;

    // Stat labels
    private JLabel lblStatStudents, lblStatCourses, lblStatEnrollments, lblStatGrades;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public AdminDashboard() {
        initFrame();
        refreshStats();
    }

    private void initFrame() {
        setTitle("Student Performance Tracker – Admin Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setResizable(true); // ← FULLY RESIZABLE
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmLogout(); }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);
        root.add(buildTopBar(), BorderLayout.NORTH);

        JPanel centre = new JPanel(new BorderLayout(0, 6));
        centre.setOpaque(false);
        centre.setBorder(new EmptyBorder(6, 0, 0, 0));
        centre.add(buildStatPanel(),  BorderLayout.NORTH);
        centre.add(buildTabbedPane(), BorderLayout.CENTER);
        root.add(centre, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY);
        bar.setBorder(new EmptyBorder(0, 18, 0, 18));
        bar.setPreferredSize(new Dimension(0, 56));

        JLabel title = new JLabel("🎓  Student Performance Tracking System  –  Admin Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);

        String name = SessionManager.getInstance().getCurrentUser().getUsername();
        JLabel lblUser = new JLabel("👤 " + name + "   |   Admin");
        lblUser.setFont(UITheme.FONT_BODY);
        lblUser.setForeground(new Color(200, 225, 255));

        JButton btnLogout = UITheme.createButton("Logout", UITheme.DANGER);
        btnLogout.addActionListener(e -> confirmLogout());

        right.add(lblUser);
        right.add(btnLogout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Stat cards ────────────────────────────────────────────────────────────

    private JPanel buildStatPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 16, 4, 16));

        lblStatStudents    = new JLabel("0", SwingConstants.CENTER);
        lblStatCourses     = new JLabel("0", SwingConstants.CENTER);
        lblStatEnrollments = new JLabel("0", SwingConstants.CENTER);
        lblStatGrades      = new JLabel("0", SwingConstants.CENTER);

        panel.add(wrapStat("👨‍🎓  Total Students",   lblStatStudents,    UITheme.PRIMARY));
        panel.add(wrapStat("📚  Total Courses",      lblStatCourses,     UITheme.SECONDARY));
        panel.add(wrapStat("📋  Enrollments",        lblStatEnrollments, UITheme.ACCENT));
        panel.add(wrapStat("📝  Graded Records",     lblStatGrades,      UITheme.WARNING));
        return panel;
    }

    private JPanel wrapStat(String title, JLabel valueLbl, Color color) {
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLbl.setForeground(color);
        JPanel card = UITheme.cardPanel(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, color),
            new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        card.add(lbl,      BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private void refreshStats() {
        lblStatStudents.setText(String.valueOf(studentDAO.getTotalCount()));
        lblStatCourses.setText(String.valueOf(courseDAO.getTotalCount()));
        lblStatEnrollments.setText(String.valueOf(enrollmentDAO.getTotalCount()));
        lblStatGrades.setText(String.valueOf(gradeDAO.getTotalCount()));
    }

    // ── Tabbed pane ───────────────────────────────────────────────────────────

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UITheme.FONT_HEADING);
        tabs.setBackground(UITheme.BG_MAIN);
        tabs.addTab("👨‍🎓  Students",   buildStudentTab());
        tabs.addTab("📚  Courses",     buildCourseTab());
        tabs.addTab("📋  Enrollments", buildEnrollmentTab());
        tabs.addTab("👤  Users",       buildUsersTab());
        tabs.addTab("📊  Reports",     buildReportsTab());
        return tabs;
    }

    // ════════════════════════════════════════════════════════════════════════
    // STUDENT TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildStudentTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        tfStudentSearch = UITheme.createField(22);
        tfStudentSearch.setToolTipText("Search by name or email");
        JButton btnSearch  = UITheme.primaryButton("🔍 Search");
        JButton btnClear   = UITheme.neutralButton("Clear");
        JTextField tfFrom  = UITheme.createField(10);
        JTextField tfTo    = UITheme.createField(10);
        tfFrom.setToolTipText("From date yyyy-MM-dd");
        tfTo.setToolTipText("To date yyyy-MM-dd");
        JButton btnDateFilter = UITheme.primaryButton("Filter by Date");
        JButton btnAdd     = UITheme.successButton("➕ Add Student");
        JButton btnEdit    = UITheme.primaryButton("✏️ Edit");
        JButton btnDelete  = UITheme.dangerButton("🗑️ Delete");
        JButton btnRefresh = UITheme.neutralButton("↻ Refresh");

        toolbar.add(tfStudentSearch); toolbar.add(btnSearch); toolbar.add(btnClear);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(UITheme.createLabel(" From:")); toolbar.add(tfFrom);
        toolbar.add(UITheme.createLabel("To:"));    toolbar.add(tfTo);
        toolbar.add(btnDateFilter);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnAdd); toolbar.add(btnEdit); toolbar.add(btnDelete); toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Name","Email","Phone","DOB","Address","Enrollment Date"};
        mdlStudents = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStudents = new JTable(mdlStudents);
        UITheme.styleTable(tblStudents);
        tblStudents.getColumnModel().getColumn(0).setMaxWidth(55);

        JScrollPane scroll = new JScrollPane(tblStudents);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        panel.add(scroll, BorderLayout.CENTER);

        loadStudents(null, null, null);

        btnSearch.addActionListener(e -> loadStudents(tfStudentSearch.getText(), null, null));
        btnClear.addActionListener(e -> { tfStudentSearch.setText(""); tfFrom.setText(""); tfTo.setText(""); loadStudents(null, null, null); });
        btnRefresh.addActionListener(e -> { loadStudents(null, null, null); refreshStats(); });

        btnDateFilter.addActionListener(e -> {
            java.sql.Date from = sqlDate(tfFrom.getText());
            java.sql.Date to   = sqlDate(tfTo.getText());
            if (from == null || to == null) { showError("Enter valid dates (yyyy-MM-dd)."); return; }
            loadStudents(null, from, to);
        });

        btnAdd.addActionListener(e -> {
            AddEditStudentDialog dlg = new AddEditStudentDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadStudents(null, null, null); refreshStats(); }
        });
        btnEdit.addActionListener(e -> {
            int row = tblStudents.getSelectedRow();
            if (row < 0) { showError("Select a student first."); return; }
            int id = (int) mdlStudents.getValueAt(row, 0);
            AddEditStudentDialog dlg = new AddEditStudentDialog(this, studentDAO.getStudentById(id));
            dlg.setVisible(true);
            if (dlg.isSaved()) loadStudents(null, null, null);
        });
        btnDelete.addActionListener(e -> {
            int row = tblStudents.getSelectedRow();
            if (row < 0) { showError("Select a student first."); return; }
            int id = (int) mdlStudents.getValueAt(row, 0);
            String nm = (String) mdlStudents.getValueAt(row, 1);
            if (confirmDelete("student \"" + nm + "\"")) { studentDAO.deleteStudent(id); loadStudents(null, null, null); refreshStats(); }
        });

        return panel;
    }

    private void loadStudents(String kw, java.sql.Date from, java.sql.Date to) {
        mdlStudents.setRowCount(0);
        List<Student> list;
        if (from != null && to != null) list = studentDAO.filterByEnrollmentDate(from, to);
        else if (kw != null && !kw.isEmpty()) list = studentDAO.searchStudents(kw);
        else list = studentDAO.getAllStudents();
        for (Student s : list)
            mdlStudents.addRow(new Object[]{ s.getId(), s.getName(), s.getEmail(), s.getPhone(),
                s.getDob() != null ? SDF.format(s.getDob()) : "",
                s.getAddress(),
                s.getEnrollmentDate() != null ? SDF.format(s.getEnrollmentDate()) : "" });
    }

    // ════════════════════════════════════════════════════════════════════════
    // COURSE TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildCourseTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        tfCourseSearch = UITheme.createField(22);
        JButton btnSearch  = UITheme.primaryButton("🔍 Search");
        JButton btnClear   = UITheme.neutralButton("Clear");
        JButton btnAdd     = UITheme.successButton("➕ Add Course");
        JButton btnEdit    = UITheme.primaryButton("✏️ Edit");
        JButton btnDelete  = UITheme.dangerButton("🗑️ Delete");
        JButton btnRefresh = UITheme.neutralButton("↻ Refresh");

        toolbar.add(tfCourseSearch); toolbar.add(btnSearch); toolbar.add(btnClear);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnAdd); toolbar.add(btnEdit); toolbar.add(btnDelete); toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Course Name","Code","Credits","Teacher"};
        mdlCourses = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCourses = new JTable(mdlCourses);
        UITheme.styleTable(tblCourses);
        tblCourses.getColumnModel().getColumn(0).setMaxWidth(55);
        panel.add(new JScrollPane(tblCourses), BorderLayout.CENTER);

        loadCourses(null);

        btnSearch.addActionListener(e -> loadCourses(tfCourseSearch.getText()));
        btnClear.addActionListener(e -> { tfCourseSearch.setText(""); loadCourses(null); });
        btnRefresh.addActionListener(e -> { loadCourses(null); refreshStats(); });

        btnAdd.addActionListener(e -> {
            AddEditCourseDialog dlg = new AddEditCourseDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadCourses(null); refreshStats(); }
        });
        btnEdit.addActionListener(e -> {
            int row = tblCourses.getSelectedRow();
            if (row < 0) { showError("Select a course first."); return; }
            int id = (int) mdlCourses.getValueAt(row, 0);
            AddEditCourseDialog dlg = new AddEditCourseDialog(this, courseDAO.getCourseById(id));
            dlg.setVisible(true);
            if (dlg.isSaved()) loadCourses(null);
        });
        btnDelete.addActionListener(e -> {
            int row = tblCourses.getSelectedRow();
            if (row < 0) { showError("Select a course first."); return; }
            int id = (int) mdlCourses.getValueAt(row, 0);
            String nm = (String) mdlCourses.getValueAt(row, 1);
            if (confirmDelete("course \"" + nm + "\"")) { courseDAO.deleteCourse(id); loadCourses(null); refreshStats(); }
        });

        return panel;
    }

    private void loadCourses(String kw) {
        mdlCourses.setRowCount(0);
        List<Course> list = (kw != null && !kw.isEmpty()) ? courseDAO.searchCourses(kw) : courseDAO.getAllCourses();
        for (Course c : list)
            mdlCourses.addRow(new Object[]{ c.getId(), c.getCourseName(), c.getCourseCode(), c.getCredits(), c.getTeacherName() });
    }

    // ════════════════════════════════════════════════════════════════════════
    // ENROLLMENT TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildEnrollmentTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        cbStudentFilter = UITheme.createCombo();
        cbCourseFilter  = UITheme.createCombo();
        JButton btnFilter  = UITheme.primaryButton("🔍 Filter");
        JButton btnAll     = UITheme.neutralButton("Show All");
        JButton btnEnroll  = UITheme.successButton("➕ Enroll Student");
        JButton btnDelete  = UITheme.dangerButton("🗑️ Remove");

        toolbar.add(UITheme.createLabel("Student:")); toolbar.add(cbStudentFilter);
        toolbar.add(UITheme.createLabel("Course:"));  toolbar.add(cbCourseFilter);
        toolbar.add(btnFilter); toolbar.add(btnAll);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnEnroll); toolbar.add(btnDelete);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Student","Course","Code","Enrollment Date"};
        mdlEnrollments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEnrollments = new JTable(mdlEnrollments);
        UITheme.styleTable(tblEnrollments);
        tblEnrollments.getColumnModel().getColumn(0).setMaxWidth(55);
        panel.add(new JScrollPane(tblEnrollments), BorderLayout.CENTER);

        populateEnrollmentFilters();
        loadEnrollments();

        btnFilter.addActionListener(e -> loadEnrollments());
        btnAll.addActionListener(e -> { cbStudentFilter.setSelectedIndex(0); cbCourseFilter.setSelectedIndex(0); loadEnrollments(); });
        btnEnroll.addActionListener(e -> showEnrollDialog());
        btnDelete.addActionListener(e -> removeEnrollment());

        return panel;
    }

    private void populateEnrollmentFilters() {
        cbStudentFilter.removeAllItems(); cbStudentFilter.addItem("All Students");
        for (Student s : studentDAO.getAllStudents()) cbStudentFilter.addItem(s.getId() + " - " + s.getName());
        cbCourseFilter.removeAllItems();  cbCourseFilter.addItem("All Courses");
        for (Course c : courseDAO.getAllCourses()) cbCourseFilter.addItem(c.getId() + " - " + c.getCourseName());
    }

    private void loadEnrollments() {
        mdlEnrollments.setRowCount(0);
        List<Enrollment> list;
        int sIdx = cbStudentFilter.getSelectedIndex();
        int cIdx = cbCourseFilter.getSelectedIndex();
        if (sIdx > 0) {
            int sid = Integer.parseInt(((String) cbStudentFilter.getSelectedItem()).split(" - ")[0]);
            list = enrollmentDAO.getEnrollmentsByStudent(sid);
        } else if (cIdx > 0) {
            int cid = Integer.parseInt(((String) cbCourseFilter.getSelectedItem()).split(" - ")[0]);
            list = enrollmentDAO.getEnrollmentsByCourse(cid);
        } else {
            list = enrollmentDAO.getAllEnrollments();
        }
        for (Enrollment en : list)
            mdlEnrollments.addRow(new Object[]{ en.getId(), en.getStudentName(), en.getCourseName(),
                en.getCourseCode(), en.getEnrollmentDate() != null ? SDF.format(en.getEnrollmentDate()) : "" });
    }

    private void showEnrollDialog() {
        List<Student> students = studentDAO.getAllStudents();
        List<Course>  courses  = courseDAO.getAllCourses();
        if (students.isEmpty() || courses.isEmpty()) { showError("Need at least one student and one course."); return; }

        String[] sNames = students.stream().map(s -> s.getId() + " – " + s.getName()).toArray(String[]::new);
        String[] cNames = courses.stream().map(c -> c.getId() + " – " + c.getCourseName()).toArray(String[]::new);

        JComboBox<String> cbS = new JComboBox<>(sNames);
        JComboBox<String> cbC = new JComboBox<>(cNames);
        cbS.setFont(UITheme.FONT_BODY); cbC.setFont(UITheme.FONT_BODY);

        JPanel p = new JPanel(new GridLayout(4, 1, 6, 6));
        p.add(UITheme.createLabel("Select Student:")); p.add(cbS);
        p.add(UITheme.createLabel("Select Course:"));  p.add(cbC);

        if (JOptionPane.showConfirmDialog(this, p, "Enroll Student",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            int sid = students.get(cbS.getSelectedIndex()).getId();
            int cid = courses.get(cbC.getSelectedIndex()).getId();
            if (enrollmentDAO.isAlreadyEnrolled(sid, cid)) { showError("Student is already enrolled in this course."); return; }
            int id = enrollmentDAO.addEnrollment(new Enrollment(0, sid, cid, new java.util.Date()));
            if (id > 0) { loadEnrollments(); refreshStats(); }
        }
    }

    private void removeEnrollment() {
        int row = tblEnrollments.getSelectedRow();
        if (row < 0) { showError("Select an enrollment first."); return; }
        int id = (int) mdlEnrollments.getValueAt(row, 0);
        String student = (String) mdlEnrollments.getValueAt(row, 1);
        String course  = (String) mdlEnrollments.getValueAt(row, 2);
        if (confirmDelete("enrollment of \"" + student + "\" from \"" + course + "\"")) {
            enrollmentDAO.deleteEnrollment(id); loadEnrollments(); refreshStats();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // USERS TAB  (Admin can manage all accounts)
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildUsersTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        JButton btnAdd     = UITheme.successButton("➕ Add User");
        JButton btnDelete  = UITheme.dangerButton("🗑️ Delete");
        JButton btnRefresh = UITheme.neutralButton("↻ Refresh");

        toolbar.add(btnAdd); toolbar.add(btnDelete); toolbar.add(btnRefresh);

        // Info label
        JLabel info = UITheme.createLabel("  Manage all system accounts. Admins can delete or add users.");
        info.setForeground(UITheme.TEXT_MUTED);
        toolbar.add(info);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Username","Role","Linked Student"};
        mdlUsers = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblUsers = new JTable(mdlUsers);
        UITheme.styleTable(tblUsers);
        tblUsers.getColumnModel().getColumn(0).setMaxWidth(55);
        tblUsers.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblUsers.getColumnModel().getColumn(3).setPreferredWidth(180);
        panel.add(new JScrollPane(tblUsers), BorderLayout.CENTER);

        loadUsers();

        btnAdd.addActionListener(e -> {
            SignUpDialog dlg = new SignUpDialog(this);
            dlg.setVisible(true);
            if (dlg.isRegistered()) loadUsers();
        });

        btnDelete.addActionListener(e -> {
            int row = tblUsers.getSelectedRow();
            if (row < 0) { showError("Select a user first."); return; }
            int id = (int) mdlUsers.getValueAt(row, 0);
            String uname = (String) mdlUsers.getValueAt(row, 1);
            // prevent deleting self
            int myId = SessionManager.getInstance().getCurrentUser().getId();
            if (id == myId) { showError("You cannot delete your own account."); return; }
            if (confirmDelete("user \"" + uname + "\"")) { userDAO.deleteUser(id); loadUsers(); }
        });

        btnRefresh.addActionListener(e -> loadUsers());
        return panel;
    }

    private void loadUsers() {
        mdlUsers.setRowCount(0);
        for (User u : userDAO.getAllUsers()) {
            String linked = "–";
            if (u.isStudent() && u.getStudentId() > 0) {
                Student s = studentDAO.getStudentById(u.getStudentId());
                linked = s != null ? s.getName() : ("Student ID: " + u.getStudentId());
            }
            mdlUsers.addRow(new Object[]{ u.getId(), u.getUsername(), u.getRole().toUpperCase(), linked });
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORTS TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildReportsTab() {
        JPanel panel = UITheme.mainPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.BOTH;
        gc.insets = new Insets(10, 10, 10, 10);
        gc.weightx = 0.5; gc.weighty = 0.5;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel heading = UITheme.createHeading("📊  Generate Reports");
        heading.setFont(UITheme.FONT_TITLE);
        panel.add(heading, gc);

        gc.gridwidth = 1; gc.gridy = 1;
        panel.add(reportCard("Student Enrollment Summary",
            "All students with total courses enrolled.",
            UITheme.PRIMARY, e -> generateStudentEnrollmentReport()), gc);

        gc.gridx = 1;
        panel.add(reportCard("Complete Grade Report",
            "All graded records: student, course, grade, pass/fail.",
            UITheme.SECONDARY, e -> generateCompleteGradeReport()), gc);

        gc.gridx = 0; gc.gridy = 2;
        panel.add(reportCard("Student Transcript",
            "Full grade transcript for a selected student.",
            UITheme.ACCENT, e -> generateTranscriptReport()), gc);

        gc.gridx = 1;
        panel.add(reportCard("Course Grade Sheet",
            "Grade sheet for a specific course with pass rate.",
            UITheme.WARNING, e -> generateCourseGradeSheet()), gc);

        return panel;
    }

    private JPanel reportCard(String title, String desc, Color color, ActionListener action) {
        JPanel card = UITheme.cardPanel(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, color),
            new EmptyBorder(16, 18, 16, 18)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_HEADING);
        lbl.setForeground(color);
        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(UITheme.FONT_SMALL);
        d.setForeground(UITheme.TEXT_MUTED);
        JButton btn = UITheme.createButton("Generate Report", color);
        btn.addActionListener(action);
        card.add(lbl, BorderLayout.NORTH);
        card.add(d,   BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);
        return card;
    }

    private void generateStudentEnrollmentReport() {
        List<Student> students = studentDAO.getAllStudents();
        String[] headers = {"ID","Student Name","Email","Phone","Enrollment Date","Courses Enrolled"};
        Object[][] rows = new Object[students.size()][6];
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            rows[i] = new Object[]{ s.getId(), s.getName(), s.getEmail(), s.getPhone(),
                s.getEnrollmentDate() != null ? SDF.format(s.getEnrollmentDate()) : "",
                enrollmentDAO.getEnrollmentsByStudent(s.getId()).size() };
        }
        String title = "Student Enrollment Summary";
        showReport(title, headers, rows, "Total Students: " + students.size());
    }

    private void generateCompleteGradeReport() {
        List<Grade> grades = gradeDAO.getAllGrades();
        String[] headers = {"Student","Course","Code","Grade","Letter","Semester","Year","Status"};
        Object[][] rows = new Object[grades.size()][8];
        for (int i = 0; i < grades.size(); i++) {
            Grade g = grades.get(i);
            rows[i] = new Object[]{ g.getStudentName(), g.getCourseName(), g.getCourseCode(),
                g.getGradeValue(), g.getGradeLetter(), g.getSemester(), g.getAcademicYear(), g.getStatus() };
        }
        long pass = grades.stream().filter(g -> g.getGradeValue() >= 50).count();
        showReport("Complete Grade Report", headers, rows,
            "Total: " + grades.size() + "  |  Pass: " + pass + "  |  Fail: " + (grades.size() - pass));
    }

    private void generateTranscriptReport() {
        List<Student> students = studentDAO.getAllStudents();
        if (students.isEmpty()) { showError("No students found."); return; }
        String[] names = students.stream().map(Student::getName).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Select student:", "Transcript",
            JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (sel == null) return;
        Student s = students.stream().filter(st -> st.getName().equals(sel)).findFirst().orElse(null);
        if (s == null) return;
        List<Grade> grades = gradeDAO.getGradesByStudent(s.getId());
        String[] headers = {"Course","Code","Grade","Letter","Semester","Year","Status"};
        Object[][] rows = new Object[grades.size()][7];
        for (int i = 0; i < grades.size(); i++) {
            Grade g = grades.get(i);
            rows[i] = new Object[]{ g.getCourseName(), g.getCourseCode(), g.getGradeValue(),
                g.getGradeLetter(), g.getSemester(), g.getAcademicYear(), g.getStatus() };
        }
        double avg = grades.stream().mapToDouble(Grade::getGradeValue).average().orElse(0);
        showReport("Transcript: " + s.getName(), headers, rows,
            "Courses Graded: " + grades.size() + "  |  Average: " + String.format("%.2f", avg));
    }

    private void generateCourseGradeSheet() {
        List<Course> courses = courseDAO.getAllCourses();
        if (courses.isEmpty()) { showError("No courses found."); return; }
        String[] names = courses.stream().map(c -> c.getCourseCode() + " - " + c.getCourseName()).toArray(String[]::new);
        String sel = (String) JOptionPane.showInputDialog(this, "Select course:", "Grade Sheet",
            JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        if (sel == null) return;
        Course c = courses.get(java.util.Arrays.asList(names).indexOf(sel));
        List<Grade> grades = gradeDAO.getGradesByCourse(c.getId(), null);
        String[] headers = {"Student","Grade","Letter","Semester","Year","Status"};
        Object[][] rows = new Object[grades.size()][6];
        for (int i = 0; i < grades.size(); i++) {
            Grade g = grades.get(i);
            rows[i] = new Object[]{ g.getStudentName(), g.getGradeValue(), g.getGradeLetter(),
                g.getSemester(), g.getAcademicYear(), g.getStatus() };
        }
        long pass = grades.stream().filter(g -> g.getGradeValue() >= 50).count();
        showReport("Grade Sheet: " + c.getCourseCode(), headers, rows,
            "Total: " + grades.size() + "  |  Pass: " + pass + "  |  Fail: " + (grades.size() - pass)
            + (grades.size() > 0 ? "  |  Pass Rate: " + String.format("%.1f%%", pass * 100.0 / grades.size()) : ""));
    }

    private void showReport(String title, String[] headers, Object[][] rows, String summary) {
        new ReportViewer(this, title,
            ReportViewer.buildTableHTML(title, headers, rows, summary),
            ReportViewer.buildTableTXT(title,  headers, rows, summary)).setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private java.sql.Date sqlDate(String s) {
        try { return new java.sql.Date(SDF.parse(s).getTime()); } catch (Exception e) { return null; }
    }

    private boolean confirmDelete(String what) {
        return JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + what + "?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
            == JOptionPane.YES_OPTION;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.WARNING_MESSAGE);
    }

    private void confirmLogout() {
        if (JOptionPane.showConfirmDialog(this, "Logout and return to login screen?", "Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
