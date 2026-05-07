package view;

import dao.*;
import model.*;
import utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main dashboard for Teacher users.
 * Fully resizable and opens maximized.
 */
public class TeacherDashboard extends JFrame {

    private final CourseDAO     courseDAO     = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final GradeDAO      gradeDAO      = new GradeDAO();
    private final StudentDAO    studentDAO    = new StudentDAO();

    private JTable            tblStudents;
    private DefaultTableModel mdlStudents;
    private JComboBox<String> cbCourse;
    private JTextField        tfSearch;

    private JTable            tblGrades;
    private DefaultTableModel mdlGrades;
    private JComboBox<String> cbGradeCourse;
    private JComboBox<String> cbSemester;

    private List<Course> allCourses;

    public TeacherDashboard() {
        allCourses = courseDAO.getAllCourses();
        initFrame();
    }

    private void initFrame() {
        setTitle("Student Performance Tracker – Teacher Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(860, 560));
        setResizable(true); // ← FULLY RESIZABLE
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmLogout(); }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);
        root.add(buildTopBar(),     BorderLayout.NORTH);
        root.add(buildTabbedPane(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.PRIMARY_DARK);
        bar.setBorder(new EmptyBorder(0, 18, 0, 18));
        bar.setPreferredSize(new Dimension(0, 56));

        JLabel title = new JLabel("🎓  Student Performance Tracking System  –  Teacher Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);
        String name = SessionManager.getInstance().getCurrentUser().getUsername();
        JLabel lblUser = new JLabel("👤 " + name + "   |   Teacher");
        lblUser.setFont(UITheme.FONT_BODY);
        lblUser.setForeground(new Color(200, 225, 255));
        JButton btnLogout = UITheme.createButton("Logout", UITheme.DANGER);
        btnLogout.addActionListener(e -> confirmLogout());
        right.add(lblUser); right.add(btnLogout);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UITheme.FONT_HEADING);
        tabs.setBackground(UITheme.BG_MAIN);
        tabs.addTab("👨‍🎓  My Students",  buildStudentsTab());
        tabs.addTab("📝  Grades",         buildGradesTab());
        tabs.addTab("📊  Grade Report",   buildReportTab());
        return tabs;
    }

    // ════════════════════════════════════════════════════════════════════════
    // STUDENTS TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildStudentsTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        cbCourse = UITheme.createCombo();
        cbCourse.addItem("All Courses");
        for (Course c : allCourses)
            cbCourse.addItem(c.getId() + " – " + c.getCourseCode() + " – " + c.getCourseName());

        tfSearch = UITheme.createField(18);
        tfSearch.setToolTipText("Search student by name");

        JButton btnFilter  = UITheme.primaryButton("🔍 Filter");
        JButton btnSearch  = UITheme.primaryButton("Search Name");
        JButton btnClear   = UITheme.neutralButton("Clear");
        JButton btnGrade   = UITheme.successButton("📝 Enter / Edit Grade");
        JButton btnRefresh = UITheme.neutralButton("↻ Refresh");

        toolbar.add(UITheme.createLabel("Course:")); toolbar.add(cbCourse); toolbar.add(btnFilter);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(UITheme.createLabel("Search:")); toolbar.add(tfSearch); toolbar.add(btnSearch);
        toolbar.add(btnClear);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnGrade); toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Enroll ID","Student Name","Email","Course","Code","Enrollment Date","Grade","Letter","Status"};
        mdlStudents = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStudents = new JTable(mdlStudents);
        UITheme.styleTable(tblStudents);
        tblStudents.getColumnModel().getColumn(0).setMaxWidth(80);

        panel.add(new JScrollPane(tblStudents), BorderLayout.CENTER);

        JLabel info = new JLabel("  💡 Double-click a row OR click 'Enter / Edit Grade' to grade a student.");
        info.setFont(UITheme.FONT_SMALL);
        info.setForeground(UITheme.TEXT_MUTED);
        panel.add(info, BorderLayout.SOUTH);

        loadStudentsByCourse(-1, null);

        btnFilter.addActionListener(e -> {
            int idx = cbCourse.getSelectedIndex();
            int cid = idx <= 0 ? -1 : allCourses.get(idx - 1).getId();
            loadStudentsByCourse(cid, null);
        });
        btnSearch.addActionListener(e -> {
            int idx = cbCourse.getSelectedIndex();
            int cid = idx <= 0 ? -1 : allCourses.get(idx - 1).getId();
            loadStudentsByCourse(cid, tfSearch.getText());
        });
        btnClear.addActionListener(e -> { cbCourse.setSelectedIndex(0); tfSearch.setText(""); loadStudentsByCourse(-1, null); });
        btnRefresh.addActionListener(e -> loadStudentsByCourse(-1, null));
        btnGrade.addActionListener(e -> openGradeDialog());
        tblStudents.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openGradeDialog();
            }
        });

        return panel;
    }

    private void loadStudentsByCourse(int courseId, String nameFilter) {
        mdlStudents.setRowCount(0);
        List<Enrollment> enrollments = (courseId > 0)
            ? enrollmentDAO.getEnrollmentsByCourse(courseId)
            : enrollmentDAO.getAllEnrollments();

        for (Enrollment en : enrollments) {
            if (nameFilter != null && !nameFilter.isEmpty() &&
                !en.getStudentName().toLowerCase().contains(nameFilter.toLowerCase())) continue;
            Grade g = gradeDAO.getGradeByEnrollmentId(en.getId());
            Student s = studentDAO.getStudentById(en.getStudentId());
            mdlStudents.addRow(new Object[]{
                en.getId(),
                en.getStudentName(),
                s != null ? s.getEmail() : "",
                en.getCourseName(),
                en.getCourseCode(),
                en.getEnrollmentDate() != null ? en.getEnrollmentDate().toString() : "",
                g != null ? g.getGradeValue()  : "—",
                g != null ? g.getGradeLetter() : "—",
                g != null ? g.getStatus()      : "Not Graded"
            });
        }
    }

    private void openGradeDialog() {
        int row = tblStudents.getSelectedRow();
        if (row < 0) { showError("Please select a student row first."); return; }
        int    enrollId    = (int)    mdlStudents.getValueAt(row, 0);
        String studentName = (String) mdlStudents.getValueAt(row, 1);
        String courseInfo  = mdlStudents.getValueAt(row, 4) + " – " + mdlStudents.getValueAt(row, 3);

        GradeEntryDialog dlg = new GradeEntryDialog(this, enrollId, studentName, courseInfo);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            int idx = cbCourse.getSelectedIndex();
            int cid = idx <= 0 ? -1 : allCourses.get(idx - 1).getId();
            loadStudentsByCourse(cid, tfSearch.getText());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // GRADES TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildGradesTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 6));
        panel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);

        cbGradeCourse = UITheme.createCombo();
        cbGradeCourse.addItem("All Courses");
        for (Course c : allCourses) cbGradeCourse.addItem(c.getId() + " – " + c.getCourseCode());

        cbSemester = UITheme.createCombo();
        cbSemester.addItem("All Semesters");
        cbSemester.addItem("Spring"); cbSemester.addItem("Fall"); cbSemester.addItem("Summer");

        JButton btnFilter  = UITheme.primaryButton("🔍 Filter");
        JButton btnAll     = UITheme.neutralButton("Show All");
        JButton btnEdit    = UITheme.primaryButton("✏️ Edit Grade");
        JButton btnDelete  = UITheme.dangerButton("🗑️ Delete Grade");

        toolbar.add(UITheme.createLabel("Course:")); toolbar.add(cbGradeCourse);
        toolbar.add(UITheme.createLabel("Semester:")); toolbar.add(cbSemester);
        toolbar.add(btnFilter); toolbar.add(btnAll);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnEdit); toolbar.add(btnDelete);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Student","Course","Code","Grade","Letter","Semester","Year","Status"};
        mdlGrades = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblGrades = new JTable(mdlGrades);
        UITheme.styleTable(tblGrades);
        tblGrades.getColumnModel().getColumn(0).setMaxWidth(55);
        panel.add(new JScrollPane(tblGrades), BorderLayout.CENTER);

        loadGrades(-1, null);

        btnFilter.addActionListener(e -> {
            int idx = cbGradeCourse.getSelectedIndex();
            int cid = idx <= 0 ? -1 : allCourses.get(idx - 1).getId();
            String sem = cbSemester.getSelectedIndex() <= 0 ? null : (String) cbSemester.getSelectedItem();
            loadGrades(cid, sem);
        });
        btnAll.addActionListener(e -> { cbGradeCourse.setSelectedIndex(0); cbSemester.setSelectedIndex(0); loadGrades(-1, null); });

        btnEdit.addActionListener(e -> {
            int row = tblGrades.getSelectedRow();
            if (row < 0) { showError("Select a grade to edit."); return; }
            int gradeId = (int) mdlGrades.getValueAt(row, 0);
            List<Grade> all = gradeDAO.getAllGrades();
            Grade target = all.stream().filter(g -> g.getId() == gradeId).findFirst().orElse(null);
            if (target == null) return;
            String student = (String) mdlGrades.getValueAt(row, 1);
            String course  = mdlGrades.getValueAt(row, 3) + " – " + mdlGrades.getValueAt(row, 2);
            GradeEntryDialog dlg = new GradeEntryDialog(this, target.getEnrollmentId(), student, course);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadGrades(-1, null);
        });

        btnDelete.addActionListener(e -> {
            int row = tblGrades.getSelectedRow();
            if (row < 0) { showError("Select a grade to delete."); return; }
            int gradeId = (int) mdlGrades.getValueAt(row, 0);
            String student = (String) mdlGrades.getValueAt(row, 1);
            if (JOptionPane.showConfirmDialog(this, "Delete grade for \"" + student + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION) {
                gradeDAO.deleteGrade(gradeId); loadGrades(-1, null);
            }
        });

        return panel;
    }

    private void loadGrades(int courseId, String semester) {
        mdlGrades.setRowCount(0);
        List<Grade> list = (courseId > 0)
            ? gradeDAO.getGradesByCourse(courseId, semester)
            : gradeDAO.getAllGrades();
        for (Grade g : list) {
            if (semester != null && !semester.isEmpty() && !semester.equals(g.getSemester())) continue;
            mdlGrades.addRow(new Object[]{ g.getId(), g.getStudentName(), g.getCourseName(),
                g.getCourseCode(), g.getGradeValue(), g.getGradeLetter(),
                g.getSemester(), g.getAcademicYear(), g.getStatus() });
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // REPORT TAB
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildReportTab() {
        JPanel panel = UITheme.mainPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(10, 8, 10, 8);
        gc.weightx = 1.0;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        JLabel heading = UITheme.createHeading("📊  Generate Grade Reports");
        heading.setFont(UITheme.FONT_TITLE);
        panel.add(heading, gc);

        gc.gridy = 1; gc.gridwidth = 1; gc.weightx = 0.3;
        panel.add(UITheme.createLabel("Select Course:"), gc);
        gc.gridx = 1; gc.weightx = 0.7;
        JComboBox<String> cbReport = UITheme.createCombo();
        for (Course c : allCourses)
            cbReport.addItem(c.getId() + " – " + c.getCourseCode() + " – " + c.getCourseName());
        panel.add(cbReport, gc);

        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0.3;
        panel.add(UITheme.createLabel("Semester (optional):"), gc);
        gc.gridx = 1; gc.weightx = 0.7;
        JComboBox<String> cbRepSem = UITheme.createCombo();
        cbRepSem.addItem("All Semesters"); cbRepSem.addItem("Spring");
        cbRepSem.addItem("Fall"); cbRepSem.addItem("Summer");
        panel.add(cbRepSem, gc);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2; gc.insets = new Insets(20, 8, 10, 8);
        JButton btnGen = UITheme.successButton("📄  Generate Grade Sheet");
        btnGen.setPreferredSize(new Dimension(280, 46));
        JPanel bw = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bw.setOpaque(false); bw.add(btnGen);
        panel.add(bw, gc);

        gc.gridy = 4; gc.insets = new Insets(10, 8, 10, 8);
        JPanel info = UITheme.cardPanel(new BorderLayout());
        JLabel txt = new JLabel("<html><b>Grade Sheet Report</b> lists all students enrolled in the selected course "
            + "with their numeric grade, letter, and Pass/Fail status. "
            + "Filter by semester if needed. Export as HTML or TXT.</html>");
        txt.setFont(UITheme.FONT_BODY); txt.setForeground(UITheme.TEXT_MUTED);
        txt.setBorder(new EmptyBorder(8, 8, 8, 8));
        info.add(txt);
        panel.add(info, gc);

        btnGen.addActionListener(e -> {
            if (allCourses.isEmpty()) { showError("No courses found."); return; }
            int idx = cbReport.getSelectedIndex();
            if (idx < 0) return;
            Course c = allCourses.get(idx);
            String sem = cbRepSem.getSelectedIndex() <= 0 ? null : (String) cbRepSem.getSelectedItem();
            List<Grade> grades = gradeDAO.getGradesByCourse(c.getId(), sem);
            if (grades.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No graded records found for this course/semester.",
                    "No Data", JOptionPane.INFORMATION_MESSAGE); return;
            }
            String[] headers = {"Student","Grade","Letter","Semester","Year","Status"};
            Object[][] rows = new Object[grades.size()][6];
            for (int i = 0; i < grades.size(); i++) {
                Grade g = grades.get(i);
                rows[i] = new Object[]{ g.getStudentName(), g.getGradeValue(), g.getGradeLetter(),
                    g.getSemester(), g.getAcademicYear(), g.getStatus() };
            }
            long pass = grades.stream().filter(g -> g.getGradeValue() >= 50).count();
            String title   = "Grade Sheet: " + c.getCourseCode() + " – " + c.getCourseName()
                           + (sem != null ? " (" + sem + ")" : "");
            String summary = "Total: " + grades.size() + "  |  Pass: " + pass
                           + "  |  Fail: " + (grades.size() - pass)
                           + "  |  Pass Rate: " + String.format("%.1f%%", pass * 100.0 / grades.size());
            new ReportViewer(this, title,
                ReportViewer.buildTableHTML(title, headers, rows, summary),
                ReportViewer.buildTableTXT(title,  headers, rows, summary)).setVisible(true);
        });

        return panel;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
