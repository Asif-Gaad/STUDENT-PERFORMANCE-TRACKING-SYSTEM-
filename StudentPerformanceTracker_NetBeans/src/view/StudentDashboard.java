package view;

import dao.*;
import model.*;
import utils.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  STUDENT DASHBOARD  –  Full Student Portal                      ║
 * ║                                                                  ║
 * ║  Tabs:                                                           ║
 * ║  🏠 Home        – Welcome card, stat summary, recent grades      ║
 * ║  📝 My Grades   – All grades, semester filter, GPA bar           ║
 * ║  📚 My Courses  – All enrolled courses with details              ║
 * ║  📄 Transcript  – Printable transcript, export HTML/TXT          ║
 * ║  👤 My Profile  – Edit phone / DOB / address                     ║
 * ║  🔑 Security    – Change login password                          ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public class StudentDashboard extends JFrame {

    // ── DAOs ──────────────────────────────────────────────────────────────────
    private final StudentDAO    studentDAO    = new StudentDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final GradeDAO      gradeDAO      = new GradeDAO();
    private final CourseDAO     courseDAO     = new CourseDAO();
    private final UserDAO       userDAO       = new UserDAO();

    // ── Session ───────────────────────────────────────────────────────────────
    private final User    currentUser;
    private       Student myStudent;

    // ── Grade tab ─────────────────────────────────────────────────────────────
    private DefaultTableModel mdlGrades;
    private JLabel            lblGradeSummary;
    private JComboBox<String> cbGradeSemester;

    // ── Courses tab ───────────────────────────────────────────────────────────
    private DefaultTableModel mdlCourses;

    // ── Home stat labels ──────────────────────────────────────────────────────
    private JLabel lblStatCourses, lblStatGraded, lblStatGPA, lblStatPassed;
    private DefaultTableModel mdlRecent;

    // ── Profile fields ────────────────────────────────────────────────────────
    private JTextField   tfPhone, tfDob, tfAddress;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final Color  STUDENT_GREEN = new Color(39, 174, 96);
    private static final Color  STUDENT_DARK  = new Color(27, 130, 70);

    // ══════════════════════════════════════════════════════════════════════════

    public StudentDashboard() {
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.myStudent   = (currentUser.getStudentId() > 0)
            ? studentDAO.getStudentById(currentUser.getStudentId())
            : null;
        initFrame();
    }

    private void initFrame() {
        String nameTitle = (myStudent != null) ? myStudent.getName() : currentUser.getUsername();
        setTitle("🎒 Student Portal – " + nameTitle);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 580));
        setResizable(true);
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
        bar.setBackground(STUDENT_GREEN);
        bar.setBorder(new EmptyBorder(0, 18, 0, 18));
        bar.setPreferredSize(new Dimension(0, 58));

        JLabel title = new JLabel("🎒  Student Portal  –  Student Performance Tracking System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        right.setOpaque(false);

        String name = (myStudent != null) ? myStudent.getName() : currentUser.getUsername();
        JLabel lblUser = new JLabel("🎒  " + name + "   |   Student");
        lblUser.setFont(UITheme.FONT_BODY);
        lblUser.setForeground(new Color(220, 255, 230));

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
        tabs.addTab("🏠  Home",        buildHomeTab());
        tabs.addTab("📝  My Grades",   buildGradesTab());
        tabs.addTab("📚  My Courses",  buildCoursesTab());
        tabs.addTab("📄  Transcript",  buildTranscriptTab());
        tabs.addTab("👤  My Profile",  buildProfileTab());
        tabs.addTab("🔑  Security",    buildSecurityTab());
        return tabs;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 🏠  HOME TAB
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildHomeTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Welcome card
        JPanel welcome = UITheme.cardPanel(new BorderLayout(18, 0));
        welcome.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 6, 0, 0, STUDENT_GREEN),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel avatarLbl = new JLabel("🎒");
        avatarLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 54));

        JPanel wText = new JPanel();
        wText.setOpaque(false);
        wText.setLayout(new BoxLayout(wText, BoxLayout.Y_AXIS));

        String dispName = (myStudent != null) ? myStudent.getName() : currentUser.getUsername();
        JLabel lblHi = new JLabel("Welcome back, " + dispName + " 👋");
        lblHi.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblHi.setForeground(UITheme.PRIMARY);

        String enrollInfo = (myStudent != null && myStudent.getEnrollmentDate() != null)
            ? "Enrolled: " + SDF.format(myStudent.getEnrollmentDate())
            : "Student Portal Account";
        JLabel lblEnroll = UITheme.createLabel(enrollInfo);
        lblEnroll.setForeground(UITheme.TEXT_MUTED);

        String emailInfo = (myStudent != null) ? "✉  " + myStudent.getEmail() : "";
        JLabel lblEmail = UITheme.createLabel(emailInfo);
        lblEmail.setForeground(UITheme.TEXT_MUTED);

        wText.add(lblHi);
        wText.add(Box.createVerticalStrut(5));
        wText.add(lblEnroll);
        wText.add(Box.createVerticalStrut(2));
        wText.add(lblEmail);

        welcome.add(avatarLbl, BorderLayout.WEST);
        welcome.add(wText, BorderLayout.CENTER);

        // Quick-action buttons in welcome card
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        quickActions.setOpaque(false);
        JButton btnViewGrades  = UITheme.createButton("📝 My Grades",    STUDENT_GREEN);
        JButton btnViewCourses = UITheme.createButton("📚 My Courses",   UITheme.SECONDARY);
        JButton btnTranscript  = UITheme.createButton("📄 Transcript",   new Color(142, 68, 173));
        quickActions.add(btnViewGrades);
        quickActions.add(btnViewCourses);
        quickActions.add(btnTranscript);
        welcome.add(quickActions, BorderLayout.EAST);

        panel.add(welcome, BorderLayout.NORTH);

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        lblStatCourses = new JLabel("–", SwingConstants.CENTER);
        lblStatGraded  = new JLabel("–", SwingConstants.CENTER);
        lblStatGPA     = new JLabel("–", SwingConstants.CENTER);
        lblStatPassed  = new JLabel("–", SwingConstants.CENTER);
        stats.add(wrapStatCard("📚  Enrolled",      lblStatCourses, UITheme.PRIMARY));
        stats.add(wrapStatCard("📝  Grades Received",lblStatGraded,  UITheme.SECONDARY));
        stats.add(wrapStatCard("⭐  Average Score",  lblStatGPA,     new Color(142, 68, 173)));
        stats.add(wrapStatCard("✅  Subjects Passed",lblStatPassed,  STUDENT_GREEN));

        // Recent grades table
        JPanel recentPanel = UITheme.cardPanel(new BorderLayout(0, 8));
        recentPanel.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel recentHdr = UITheme.createHeading("📋  Recent Grade Activity");
        recentPanel.add(recentHdr, BorderLayout.NORTH);

        String[] recentCols = {"Course Name","Code","Grade (%)","Letter","Semester","Status"};
        mdlRecent = new DefaultTableModel(recentCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblRecent = new JTable(mdlRecent);
        UITheme.styleTable(tblRecent);
        tblRecent.setPreferredScrollableViewportSize(new Dimension(0, 120));
        applyStatusRenderer(tblRecent, 5);
        JScrollPane sp = new JScrollPane(tblRecent);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        recentPanel.add(sp, BorderLayout.CENTER);

        JPanel centre = new JPanel(new GridLayout(2, 1, 0, 12));
        centre.setOpaque(false);
        centre.add(stats);
        centre.add(recentPanel);
        panel.add(centre, BorderLayout.CENTER);

        // Quick-action tab switching
        JTabbedPane[] tabRef = {null};
        panel.addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override public void ancestorAdded(javax.swing.event.AncestorEvent e) {
                Component c = panel.getParent();
                if (c instanceof JTabbedPane) tabRef[0] = (JTabbedPane) c;
            }
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
        btnViewGrades.addActionListener(e -> { if (tabRef[0] != null) tabRef[0].setSelectedIndex(1); });
        btnViewCourses.addActionListener(e -> { if (tabRef[0] != null) tabRef[0].setSelectedIndex(2); });
        btnTranscript.addActionListener(e -> { if (tabRef[0] != null) tabRef[0].setSelectedIndex(3); });

        refreshHomeStats();
        return panel;
    }

    private JPanel wrapStatCard(String title, JLabel valueLbl, Color color) {
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLbl.setForeground(color);
        JPanel card = UITheme.cardPanel(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, color),
            new EmptyBorder(12, 14, 12, 14)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        card.add(lbl,      BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        return card;
    }

    private void refreshHomeStats() {
        if (myStudent == null) return;
        int sid = myStudent.getId();
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(sid);
        List<Grade>      grades      = gradeDAO.getGradesByStudent(sid);

        lblStatCourses.setText(String.valueOf(enrollments.size()));
        lblStatGraded.setText(String.valueOf(grades.size()));

        double avg  = grades.stream().mapToDouble(Grade::getGradeValue).average().orElse(0);
        long   pass = grades.stream().filter(g -> g.getGradeValue() >= 50).count();
        lblStatGPA.setText(grades.isEmpty() ? "–" : String.format("%.1f%%", avg));
        lblStatPassed.setText(pass + " / " + grades.size());

        // Recent (up to 5)
        mdlRecent.setRowCount(0);
        int from = Math.max(0, grades.size() - 5);
        for (int i = from; i < grades.size(); i++) {
            Grade g = grades.get(i);
            mdlRecent.addRow(new Object[]{
                g.getCourseName(), g.getCourseCode(),
                String.format("%.1f", g.getGradeValue()),
                g.getGradeLetter(), g.getSemester(), g.getStatus()
            });
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 📝  MY GRADES TAB
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildGradesTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        cbGradeSemester = UITheme.createCombo();
        cbGradeSemester.addItem("All Semesters");
        cbGradeSemester.addItem("Spring");
        cbGradeSemester.addItem("Fall");
        cbGradeSemester.addItem("Summer");
        JButton btnFilter    = UITheme.primaryButton("🔍 Filter");
        JButton btnAll       = UITheme.neutralButton("Show All");
        JButton btnTranscript = UITheme.createButton("📄 Full Transcript", new Color(142, 68, 173));
        toolbar.add(UITheme.createLabel("Semester:"));
        toolbar.add(cbGradeSemester);
        toolbar.add(btnFilter);
        toolbar.add(btnAll);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnTranscript);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Course Name","Code","Credits","Grade (%)","Letter","Semester","Year","Status"};
        mdlGrades = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblGrades = new JTable(mdlGrades);
        UITheme.styleTable(tblGrades);
        applyStatusRenderer(tblGrades, 7);
        panel.add(new JScrollPane(tblGrades), BorderLayout.CENTER);

        // Summary bar
        lblGradeSummary = new JLabel("  Loading...");
        lblGradeSummary.setFont(UITheme.FONT_SMALL);
        lblGradeSummary.setForeground(UITheme.TEXT_MUTED);
        lblGradeSummary.setBorder(new EmptyBorder(4, 4, 4, 4));
        panel.add(lblGradeSummary, BorderLayout.SOUTH);

        loadMyGrades(null);

        btnFilter.addActionListener(e -> {
            String s = cbGradeSemester.getSelectedIndex() > 0 ? (String) cbGradeSemester.getSelectedItem() : null;
            loadMyGrades(s);
        });
        btnAll.addActionListener(e -> { cbGradeSemester.setSelectedIndex(0); loadMyGrades(null); });
        btnTranscript.addActionListener(e -> openTranscriptViewer(null));

        return panel;
    }

    private void loadMyGrades(String semFilter) {
        mdlGrades.setRowCount(0);
        if (myStudent == null) {
            lblGradeSummary.setText("  No student record linked to this account.");
            return;
        }
        List<Grade>      grades      = gradeDAO.getGradesByStudent(myStudent.getId());
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(myStudent.getId());

        double gradeSum = 0; int total = 0; int passed = 0;

        for (Grade g : grades) {
            if (semFilter != null && !semFilter.equals(g.getSemester())) continue;

            int credits = 3;
            for (Enrollment en : enrollments) {
                if (en.getId() == g.getEnrollmentId()) {
                    Course c = courseDAO.getCourseById(en.getCourseId());
                    if (c != null) credits = c.getCredits();
                    break;
                }
            }
            mdlGrades.addRow(new Object[]{
                g.getCourseName(), g.getCourseCode(), credits,
                String.format("%.1f", g.getGradeValue()),
                g.getGradeLetter(), g.getSemester(), g.getAcademicYear(), g.getStatus()
            });
            gradeSum += g.getGradeValue();
            total++;
            if (g.getGradeValue() >= 50) passed++;
        }

        if (total == 0) {
            lblGradeSummary.setText("  No grades found" + (semFilter != null ? " for " + semFilter : "") + ".");
        } else {
            lblGradeSummary.setText(String.format(
                "  Total: %d   |   ✅ Passed: %d   |   ❌ Failed: %d   |   Average: %.2f%%   |   GPA: %.1f / 100",
                total, passed, total - passed, gradeSum / total, gradeSum / total));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 📚  MY COURSES TAB
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildCoursesTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        JButton btnRefresh = UITheme.neutralButton("↻ Refresh");
        JLabel  info       = UITheme.createLabel("  All courses you are currently enrolled in.");
        info.setForeground(UITheme.TEXT_MUTED);
        toolbar.add(btnRefresh); toolbar.add(info);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"#","Course Name","Code","Credits","Teacher","Enrolled On","Grade","Letter","Status"};
        mdlCourses = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblCourses = new JTable(mdlCourses);
        UITheme.styleTable(tblCourses);
        tblCourses.getColumnModel().getColumn(0).setMaxWidth(40);
        applyStatusRenderer(tblCourses, 8);
        panel.add(new JScrollPane(tblCourses), BorderLayout.CENTER);

        // Summary footer
        JLabel lblCourseSummary = new JLabel("  Loading...");
        lblCourseSummary.setFont(UITheme.FONT_SMALL);
        lblCourseSummary.setForeground(UITheme.TEXT_MUTED);
        lblCourseSummary.setBorder(new EmptyBorder(4, 4, 4, 4));
        panel.add(lblCourseSummary, BorderLayout.SOUTH);

        loadMyCourses(lblCourseSummary);
        btnRefresh.addActionListener(e -> loadMyCourses(lblCourseSummary));
        return panel;
    }

    private void loadMyCourses(JLabel summary) {
        mdlCourses.setRowCount(0);
        if (myStudent == null) return;
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudent(myStudent.getId());
        int totalCredits = 0; int row = 1;
        for (Enrollment en : enrollments) {
            Course c     = courseDAO.getCourseById(en.getCourseId());
            Grade  grade = gradeDAO.getGradeByEnrollmentId(en.getId());
            int    creds = c != null ? c.getCredits() : 0;
            totalCredits += creds;
            mdlCourses.addRow(new Object[]{
                row++,
                en.getCourseName(),
                en.getCourseCode(),
                creds,
                c != null ? c.getTeacherName() : "–",
                en.getEnrollmentDate() != null ? en.getEnrollmentDate().toString() : "–",
                grade != null ? String.format("%.1f", grade.getGradeValue()) : "Not graded",
                grade != null ? grade.getGradeLetter() : "–",
                grade != null ? grade.getStatus() : "Pending"
            });
        }
        if (summary != null)
            summary.setText(String.format("  Total Enrolled: %d courses   |   Total Credits: %d",
                enrollments.size(), totalCredits));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 📄  TRANSCRIPT TAB
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildTranscriptTab() {
        JPanel panel = UITheme.mainPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Controls row
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        ctrl.setOpaque(false);
        JComboBox<String> cbYear = UITheme.createCombo();
        cbYear.addItem("All Years");
        for (int y = 2023; y <= 2026; y++) cbYear.addItem(String.valueOf(y));
        JButton btnGen  = UITheme.createButton("🔄 Generate / Refresh", new Color(142, 68, 173));
        JButton btnHTML = UITheme.primaryButton("💾 Save as HTML");
        JButton btnTXT  = UITheme.neutralButton("📃 Save as TXT");
        ctrl.add(UITheme.createLabel("Filter Year:"));
        ctrl.add(cbYear);
        ctrl.add(btnGen);
        ctrl.add(new JSeparator(SwingConstants.VERTICAL));
        ctrl.add(btnHTML);
        ctrl.add(btnTXT);
        panel.add(ctrl, BorderLayout.NORTH);

        // Viewer
        JEditorPane viewer = new JEditorPane("text/html", buildTranscriptHTML(null));
        viewer.setEditable(false);
        viewer.setBackground(Color.WHITE);
        viewer.setBorder(new EmptyBorder(10, 16, 10, 16));
        JScrollPane scroll = new JScrollPane(viewer);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scroll, BorderLayout.CENTER);

        btnGen.addActionListener(e -> {
            String yr = cbYear.getSelectedIndex() > 0 ? (String) cbYear.getSelectedItem() : null;
            viewer.setText(buildTranscriptHTML(yr));
            viewer.setCaretPosition(0);
        });
        btnHTML.addActionListener(e -> exportText(viewer.getText(), false));
        btnTXT.addActionListener(e  -> exportText(buildTranscriptTXT(null), true));

        return panel;
    }

    private void openTranscriptViewer(String yearFilter) {
        String html = buildTranscriptHTML(yearFilter);
        String txt  = buildTranscriptTXT(yearFilter);
        String name = (myStudent != null) ? myStudent.getName() : "Transcript";
        new ReportViewer(this, "Transcript – " + name, html, txt).setVisible(true);
    }

    // ── HTML transcript builder ────────────────────────────────────────────────

    private String buildTranscriptHTML(String yearFilter) {
        if (myStudent == null)
            return "<html><body style='font-family:Arial;padding:20px;'>"
                 + "<p style='color:red;'>No student record is linked to your account. "
                 + "Please contact an administrator.</p></body></html>";

        List<Grade> grades = gradeDAO.getGradesByStudent(myStudent.getId());
        double total = 0; int count = 0; int passed = 0;
        StringBuilder rows = new StringBuilder();

        for (Grade g : grades) {
            if (yearFilter != null && !yearFilter.equals(g.getAcademicYear())) continue;
            String sc = g.getGradeValue() >= 50
                ? "color:#27ae60;font-weight:bold;" : "color:#e74c3c;font-weight:bold;";
            rows.append("<tr>")
                .append("<td>").append(g.getCourseName()).append("</td>")
                .append("<td style='text-align:center;'>").append(g.getCourseCode()).append("</td>")
                .append("<td style='text-align:center;'>").append(String.format("%.1f", g.getGradeValue())).append("</td>")
                .append("<td style='text-align:center;font-weight:bold;'>").append(g.getGradeLetter()).append("</td>")
                .append("<td>").append(g.getSemester()).append(" ").append(g.getAcademicYear()).append("</td>")
                .append("<td style='").append(sc).append("text-align:center;'>").append(g.getStatus()).append("</td>")
                .append("</tr>");
            total += g.getGradeValue(); count++; if (g.getGradeValue() >= 50) passed++;
        }

        double avg = count > 0 ? total / count : 0;

        return "<html><head><style>"
            + "body{font-family:Arial,sans-serif;font-size:13px;margin:24px;color:#2c3e50;}"
            + ".header{border-bottom:3px solid #1a569f;padding-bottom:8px;margin-bottom:18px;}"
            + "h1{color:#1a569f;font-size:22px;margin:0 0 4px 0;}"
            + "h4{color:#666;font-weight:normal;margin:0;font-size:13px;}"
            + ".info{display:grid;grid-template-columns:1fr 1fr;gap:4px 24px;"
            +       "background:#f8f9fb;padding:10px 14px;margin:14px 0;border-radius:4px;}"
            + ".info span{font-size:12px;color:#555;}"
            + "table{border-collapse:collapse;width:100%;margin-top:10px;}"
            + "th{background:#1a569f;color:white;padding:9px 12px;text-align:left;font-size:13px;}"
            + "td{padding:8px 12px;border-bottom:1px solid #e5e9ef;font-size:13px;}"
            + "tr:nth-child(even){background:#f5f8ff;}"
            + ".summary{margin-top:18px;padding:12px 16px;background:#eaf4ff;"
            +          "border-left:5px solid #1a569f;border-radius:2px;}"
            + ".footer{margin-top:20px;color:#aaa;font-size:11px;}"
            + "</style></head><body>"
            + "<div class='header'>"
            + "<h1>🎓 Academic Transcript</h1>"
            + "<h4>Student Performance Tracking System &nbsp;·&nbsp; "
            +     "Dept. of Software Engineering, MUET Khairpur</h4>"
            + "</div>"
            + "<div class='info'>"
            + "<span><b>Name:</b>  " + myStudent.getName() + "</span>"
            + "<span><b>Email:</b> " + myStudent.getEmail() + "</span>"
            + "<span><b>Phone:</b> " + nvl(myStudent.getPhone()) + "</span>"
            + "<span><b>DOB:</b>   " + (myStudent.getDob() != null ? SDF.format(myStudent.getDob()) : "–") + "</span>"
            + "<span><b>Enrolled:</b> " + (myStudent.getEnrollmentDate() != null ? SDF.format(myStudent.getEnrollmentDate()) : "–") + "</span>"
            + "<span><b>Username:</b> " + currentUser.getUsername() + "</span>"
            + "</div>"
            + "<table>"
            + "<tr><th>Course Name</th><th>Code</th><th>Grade (%)</th><th>Letter</th><th>Period</th><th>Status</th></tr>"
            + rows
            + "</table>"
            + "<div class='summary'>"
            + "<b>Subjects:</b> " + count
            + " &nbsp;&nbsp; <b>Passed:</b> " + passed
            + " &nbsp;&nbsp; <b>Failed:</b> " + (count - passed)
            + " &nbsp;&nbsp; <b>Average:</b> " + String.format("%.2f%%", avg)
            + " &nbsp;&nbsp; <b>Overall GPA:</b> " + String.format("%.1f / 100", avg)
            + "</div>"
            + "<div class='footer'>Generated: " + new Date() + "</div>"
            + "</body></html>";
    }

    private String buildTranscriptTXT(String yearFilter) {
        if (myStudent == null) return "No student record linked to this account.";
        StringBuilder sb = new StringBuilder();
        sb.append("ACADEMIC TRANSCRIPT\n");
        sb.append("Student Performance Tracking System – MUET Khairpur\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append(String.format("Name     : %s%n", myStudent.getName()));
        sb.append(String.format("Email    : %s%n", myStudent.getEmail()));
        sb.append(String.format("Phone    : %s%n", nvl(myStudent.getPhone())));
        sb.append(String.format("Enrolled : %s%n",
            myStudent.getEnrollmentDate() != null ? SDF.format(myStudent.getEnrollmentDate()) : "–"));
        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("%-36s %-8s %-8s %-8s %-12s %-6s%n",
            "Course", "Code", "Grade", "Letter", "Semester", "Status"));
        sb.append("-".repeat(70)).append("\n");

        List<Grade> grades = gradeDAO.getGradesByStudent(myStudent.getId());
        double total = 0; int count = 0; int passed = 0;
        for (Grade g : grades) {
            if (yearFilter != null && !yearFilter.equals(g.getAcademicYear())) continue;
            sb.append(String.format("%-36s %-8s %-8.1f %-8s %-12s %-6s%n",
                trunc(g.getCourseName(), 34), g.getCourseCode(),
                g.getGradeValue(), g.getGradeLetter(), g.getSemester(), g.getStatus()));
            total += g.getGradeValue(); count++; if (g.getGradeValue() >= 50) passed++;
        }
        sb.append("-".repeat(70)).append("\n");
        if (count > 0)
            sb.append(String.format("Total: %d  |  Passed: %d  |  Failed: %d  |  Average: %.2f%%%n",
                count, passed, count - passed, total / count));
        sb.append("\nGenerated: ").append(new Date());
        return sb.toString();
    }

    private void exportText(String content, boolean asTxt) {
        JFileChooser fc = new JFileChooser();
        String safe = (myStudent != null ? myStudent.getName() : "Transcript").replaceAll("\\s+", "_");
        fc.setSelectedFile(new File(safe + "_Transcript." + (asTxt ? "txt" : "html")));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fc.getSelectedFile()))) {
                bw.write(content);
                JOptionPane.showMessageDialog(this,
                    "Saved: " + fc.getSelectedFile().getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 👤  MY PROFILE TAB
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildProfileTab() {
        JPanel outer = UITheme.mainPanel(new GridBagLayout());
        outer.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Centred card
        JPanel card = UITheme.cardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 36, 24, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 6, 8, 6);
        gc.weightx = 1.0;

        int row = 0;

        // Heading
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JLabel heading = UITheme.createHeading("👤  My Profile");
        heading.setFont(UITheme.FONT_TITLE);
        card.add(heading, gc);

        row++;
        gc.gridy = row;
        JLabel note = UITheme.createLabel("You can update your phone, date of birth, and address.");
        note.setForeground(UITheme.TEXT_MUTED);
        card.add(note, gc);

        row++;
        gc.gridy = row; card.add(new JSeparator(), gc);

        // Read-only fields
        row++;
        addProfileRow(card, gc, row++, "Full Name:",      readOnlyField(myStudent != null ? myStudent.getName() : "–"));
        addProfileRow(card, gc, row++, "Email:",          readOnlyField(myStudent != null ? myStudent.getEmail() : "–"));
        addProfileRow(card, gc, row++, "Enrollment Date:",readOnlyField(
            (myStudent != null && myStudent.getEnrollmentDate() != null)
                ? SDF.format(myStudent.getEnrollmentDate()) : "–"));

        // Editable fields
        tfPhone   = UITheme.createField(28); tfPhone.setToolTipText("0300-1234567");
        tfDob     = UITheme.createField(28); tfDob.setToolTipText("yyyy-MM-dd");
        tfAddress = UITheme.createField(28);

        if (myStudent != null) {
            tfPhone.setText(nvl(myStudent.getPhone()));
            tfDob.setText(myStudent.getDob() != null ? SDF.format(myStudent.getDob()) : "");
            tfAddress.setText(nvl(myStudent.getAddress()));
        }

        addProfileRow(card, gc, row++, "Phone:",        tfPhone);
        addProfileRow(card, gc, row++, "Date of Birth:", tfDob);
        addProfileRow(card, gc, row++, "Address:",      tfAddress);

        // Buttons
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        gc.insets = new Insets(18, 6, 6, 6);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btns.setOpaque(false);
        JButton btnSave    = UITheme.successButton("💾 Save Changes");
        JButton btnDiscard = UITheme.neutralButton("Discard");
        btns.add(btnSave); btns.add(btnDiscard);
        card.add(btns, gc);

        row++;
        gc.gridy = row; gc.insets = new Insets(4, 6, 4, 6);
        JLabel adminNote = new JLabel(
            "<html><i style='color:#aaa;font-size:10px;'>"
            + "Name and email can only be changed by an Administrator."
            + "</i></html>");
        adminNote.setFont(UITheme.FONT_SMALL);
        card.add(adminNote, gc);

        btnSave.addActionListener(e -> saveProfile());
        btnDiscard.addActionListener(e -> {
            if (myStudent != null) {
                tfPhone.setText(nvl(myStudent.getPhone()));
                tfDob.setText(myStudent.getDob() != null ? SDF.format(myStudent.getDob()) : "");
                tfAddress.setText(nvl(myStudent.getAddress()));
            }
        });

        // Centre card in outer
        GridBagConstraints oc = new GridBagConstraints();
        oc.fill    = GridBagConstraints.HORIZONTAL;
        oc.weightx = 1.0;
        oc.insets  = new Insets(0, 60, 0, 60);
        outer.add(card, oc);
        return outer;
    }

    private void addProfileRow(JPanel form, GridBagConstraints gc, int row,
                               String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0.28;
        gc.insets = new Insets(8, 6, 8, 6);
        JLabel lbl = UITheme.createLabel(label);
        lbl.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 0.72;
        form.add(field, gc);
    }

    private JTextField readOnlyField(String value) {
        JTextField tf = UITheme.createField(28);
        tf.setText(value);
        tf.setEditable(false);
        tf.setBackground(new Color(245, 246, 248));
        tf.setForeground(UITheme.TEXT_MUTED);
        return tf;
    }

    private void saveProfile() {
        if (myStudent == null) { showErr("No student record is linked to your account."); return; }

        String phone   = tfPhone.getText().trim();
        String dobStr  = tfDob.getText().trim();
        String address = tfAddress.getText().trim();

        if (!ValidationUtils.isValidPhone(phone)) { showErr("Phone format: 0300-1234567"); return; }

        Date dob = null;
        if (!dobStr.isEmpty()) {
            try { dob = SDF.parse(dobStr); }
            catch (Exception ex) { showErr("Date of Birth format: yyyy-MM-dd"); return; }
        }

        myStudent.setPhone(phone.isEmpty() ? null : phone);
        myStudent.setDob(dob);
        myStudent.setAddress(address.isEmpty() ? null : address);

        if (studentDAO.updateStudent(myStudent) > 0) {
            JOptionPane.showMessageDialog(this,
                "Profile updated successfully! ✅",
                "Saved", JOptionPane.INFORMATION_MESSAGE);
            refreshHomeStats();
        } else {
            showErr("Failed to update profile. Please try again.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 🔑  SECURITY / CHANGE PASSWORD TAB
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel buildSecurityTab() {
        JPanel outer = UITheme.mainPanel(new GridBagLayout());
        outer.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel card = UITheme.cardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 40, 28, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(9, 6, 9, 6);

        int row = 0;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JLabel heading = UITheme.createHeading("🔑  Change Password");
        heading.setFont(UITheme.FONT_TITLE);
        card.add(heading, gc);

        row++;
        gc.gridy = row;
        JLabel note = UITheme.createLabel("Choose a strong password with at least 6 characters.");
        note.setForeground(UITheme.TEXT_MUTED);
        card.add(note, gc);

        row++;
        gc.gridy = row; card.add(new JSeparator(), gc);

        row++;
        gc.gridwidth = 1; gc.weightx = 0.35;
        gc.gridx = 0; gc.gridy = row;
        card.add(bold("Current Password: *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        JPasswordField pfCurrent = UITheme.createPasswordField(22);
        card.add(pfCurrent, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35;
        card.add(bold("New Password: *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        JPasswordField pfNew = UITheme.createPasswordField(22);
        pfNew.setToolTipText("At least 6 characters");
        card.add(pfNew, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35;
        card.add(bold("Confirm New Password: *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        JPasswordField pfNewConfirm = UITheme.createPasswordField(22);
        card.add(pfNewConfirm, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        gc.insets = new Insets(20, 6, 6, 6);
        JButton btnChange = UITheme.createButton("🔑  Change Password", STUDENT_GREEN);
        btnChange.setPreferredSize(new Dimension(220, 42));
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnWrap.setOpaque(false);
        btnWrap.add(btnChange);
        card.add(btnWrap, gc);

        btnChange.addActionListener(e -> changePassword(
            new String(pfCurrent.getPassword()),
            new String(pfNew.getPassword()),
            new String(pfNewConfirm.getPassword()),
            pfCurrent, pfNew, pfNewConfirm
        ));
        pfNewConfirm.addActionListener(e -> btnChange.doClick());

        GridBagConstraints oc = new GridBagConstraints();
        oc.fill = GridBagConstraints.HORIZONTAL;
        oc.weightx = 1.0;
        oc.insets = new Insets(0, 80, 0, 80);
        outer.add(card, oc);
        return outer;
    }

    private void changePassword(String current, String newPass, String confirm,
                                JPasswordField pfCurrent, JPasswordField pfNew,
                                JPasswordField pfConfirm) {
        if (ValidationUtils.isEmpty(current)) { showErr("Enter your current password."); return; }
        if (newPass.length() < 6)             { showErr("New password must be at least 6 characters."); return; }
        if (!newPass.equals(confirm))          { showErr("New passwords do not match."); return; }
        if (newPass.equals(current))           { showErr("New password must be different from current password."); return; }

        // Verify current password
        if (!currentUser.getPassword().equals(current)) {
            showErr("Current password is incorrect."); return;
        }

        currentUser.setPassword(newPass);
        if (userDAO.updateUser(currentUser) > 0) {
            JOptionPane.showMessageDialog(this,
                "Password changed successfully! ✅\nUse your new password next time you log in.",
                "Password Updated", JOptionPane.INFORMATION_MESSAGE);
            pfCurrent.setText(""); pfNew.setText(""); pfConfirm.setText("");
        } else {
            showErr("Failed to update password. Please try again.");
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    /** Applies a colour renderer to the Status column index. */
    private void applyStatusRenderer(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    if (!sel) {
                        String s = v != null ? v.toString() : "";
                        if      ("Pass".equals(s) || "✅ Pass".equals(s))
                            setForeground(UITheme.ACCENT);
                        else if ("Fail".equals(s) || "❌ Fail".equals(s))
                            setForeground(UITheme.DANGER);
                        else if ("Pending".equals(s) || "Not graded".equals(s))
                            setForeground(UITheme.WARNING);
                        else
                            setForeground(UITheme.TEXT_MUTED);
                        setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                    }
                    setBorder(new EmptyBorder(0, 8, 0, 8));
                    return this;
                }
            });
    }

    private JLabel bold(String text) {
        JLabel l = UITheme.createLabel(text);
        l.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        return l;
    }

    private String nvl(String s) { return (s != null && !s.isEmpty()) ? s : "–"; }

    private String trunc(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.WARNING_MESSAGE);
    }

    private void confirmLogout() {
        if (JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
            == JOptionPane.YES_OPTION) {
            SessionManager.getInstance().logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
