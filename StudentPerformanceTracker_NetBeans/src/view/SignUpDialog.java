package view;

import dao.StudentDAO;
import dao.UserDAO;
import model.Student;
import model.User;
import utils.UITheme;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  SIGN-UP DIALOG                                             ║
 * ║                                                              ║
 * ║  Any visitor can register as:                               ║
 * ║   • Student  – fills in full personal details here.         ║
 * ║                Creates both a students row AND a users row.  ║
 * ║   • Teacher  – just username + password.                    ║
 * ║   • Admin    – just username + password.                    ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
public class SignUpDialog extends JDialog {

    private final UserDAO    userDAO    = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    // ── Common fields ─────────────────────────────────────────────────────────
    private JComboBox<String> cbRole;
    private JTextField        tfUsername;
    private JPasswordField    pfPassword, pfConfirm;

    // ── Student-only fields ───────────────────────────────────────────────────
    private JPanel     studentFieldsPanel;
    private JTextField tfFullName, tfEmail, tfPhone, tfDob, tfAddress;

    // ── Result ────────────────────────────────────────────────────────────────
    private boolean registered      = false;
    private String  createdUsername = "";

    // ── Constructor ───────────────────────────────────────────────────────────

    public SignUpDialog(Window owner) {
        super(owner, "Create New Account", ModalityType.APPLICATION_MODAL);
        initComponents();
    }

    private void initComponents() {
        setSize(540, 660);
        setMinimumSize(new Dimension(460, 580));
        setLocationRelativeTo(getOwner());
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        header.setBackground(new Color(39, 174, 96));
        JLabel ico   = new JLabel("📝");
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        JLabel title = new JLabel("  Create New Account");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(ico); header.add(title);
        root.add(header, BorderLayout.NORTH);

        // ── Scrollable form ───────────────────────────────────────────────────
        JPanel form = UITheme.cardPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 32, 20, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets  = new Insets(6, 4, 6, 4);

        int row = 0;

        // ── Role selector ─────────────────────────────────────────────────────
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0.32;
        form.add(bold("I am a: *"), gc);
        gc.gridx = 1; gc.weightx = 0.68;
        cbRole = UITheme.createCombo();
        cbRole.addItem("🎒  Student");
        cbRole.addItem("📖  Teacher");
        cbRole.addItem("👑  Admin");
        cbRole.setFont(UITheme.FONT_BODY);
        form.add(cbRole, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        form.add(new JSeparator(), gc);

        // ══ STUDENT PERSONAL DETAILS PANEL ═══════════════════════════════════
        row++;
        studentFieldsPanel = new JPanel(new GridBagLayout());
        studentFieldsPanel.setOpaque(false);
        GridBagConstraints sg = new GridBagConstraints();
        sg.fill = GridBagConstraints.HORIZONTAL;
        sg.insets = new Insets(5, 0, 5, 0);
        sg.weightx = 1.0;

        // Section label
        sg.gridx = 0; sg.gridy = 0; sg.gridwidth = 2;
        JLabel secLabel = new JLabel("  📋  Your Personal Details");
        secLabel.setFont(UITheme.FONT_HEADING);
        secLabel.setForeground(new Color(39, 174, 96));
        secLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(39, 174, 96)),
            new EmptyBorder(4, 8, 4, 0)
        ));
        studentFieldsPanel.add(secLabel, sg);

        sg.gridy = 1; sg.gridwidth = 1; sg.weightx = 0.32;
        studentFieldsPanel.add(bold("Full Name: *"), sg);
        sg.gridx = 1; sg.weightx = 0.68;
        tfFullName = UITheme.createField(22);
        tfFullName.setToolTipText("Your full legal name");
        studentFieldsPanel.add(tfFullName, sg);

        sg.gridx = 0; sg.gridy = 2; sg.weightx = 0.32;
        studentFieldsPanel.add(bold("Email: *"), sg);
        sg.gridx = 1; sg.weightx = 0.68;
        tfEmail = UITheme.createField(22);
        tfEmail.setToolTipText("example@email.com");
        studentFieldsPanel.add(tfEmail, sg);

        sg.gridx = 0; sg.gridy = 3; sg.weightx = 0.32;
        studentFieldsPanel.add(UITheme.createLabel("Phone:"), sg);
        sg.gridx = 1; sg.weightx = 0.68;
        tfPhone = UITheme.createField(22);
        tfPhone.setToolTipText("0300-1234567");
        studentFieldsPanel.add(tfPhone, sg);

        sg.gridx = 0; sg.gridy = 4; sg.weightx = 0.32;
        studentFieldsPanel.add(UITheme.createLabel("Date of Birth:"), sg);
        sg.gridx = 1; sg.weightx = 0.68;
        tfDob = UITheme.createField(22);
        tfDob.setToolTipText("yyyy-MM-dd  e.g. 2002-03-15");
        studentFieldsPanel.add(tfDob, sg);

        sg.gridx = 0; sg.gridy = 5; sg.weightx = 0.32;
        studentFieldsPanel.add(UITheme.createLabel("Address:"), sg);
        sg.gridx = 1; sg.weightx = 0.68;
        tfAddress = UITheme.createField(22);
        studentFieldsPanel.add(tfAddress, sg);

        // Separator below student fields
        sg.gridx = 0; sg.gridy = 6; sg.gridwidth = 2;
        sg.insets = new Insets(10, 0, 4, 0);
        studentFieldsPanel.add(new JSeparator(), sg);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 1.0;
        form.add(studentFieldsPanel, gc);

        // ══ ACCOUNT CREDENTIALS ═══════════════════════════════════════════════
        row++;
        gc.gridy = row;
        JLabel credLabel = new JLabel("  🔐  Account Credentials");
        credLabel.setFont(UITheme.FONT_HEADING);
        credLabel.setForeground(UITheme.PRIMARY);
        credLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.PRIMARY),
            new EmptyBorder(4, 8, 4, 0)
        ));
        form.add(credLabel, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1; gc.weightx = 0.32;
        form.add(bold("Username: *"), gc);
        gc.gridx = 1; gc.weightx = 0.68;
        tfUsername = UITheme.createField(20);
        tfUsername.setToolTipText("Letters, numbers, underscores – min 3 chars");
        form.add(tfUsername, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.32;
        form.add(bold("Password: *"), gc);
        gc.gridx = 1; gc.weightx = 0.68;
        pfPassword = UITheme.createPasswordField(20);
        pfPassword.setToolTipText("Minimum 6 characters");
        form.add(pfPassword, gc);

        row++;
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.32;
        form.add(bold("Confirm Password: *"), gc);
        gc.gridx = 1; gc.weightx = 0.68;
        pfConfirm = UITheme.createPasswordField(20);
        form.add(pfConfirm, gc);

        // Note
        row++;
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        gc.insets = new Insets(8, 4, 2, 4);
        JLabel noteLabel = new JLabel(
            "<html><i style='color:#888;font-size:10px;'>"
            + "Students: fill in personal details AND choose your login credentials above. "
            + "Teachers & Admins only need credentials."
            + "</i></html>");
        noteLabel.setFont(UITheme.FONT_SMALL);
        form.add(noteLabel, gc);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(BorderFactory.createEmptyBorder());
        scrollForm.getVerticalScrollBar().setUnitIncrement(12);
        root.add(scrollForm, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        btnPanel.setBackground(UITheme.BG_MAIN);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 225, 230)));
        JButton btnCancel   = UITheme.neutralButton("Cancel");
        JButton btnRegister = UITheme.successButton("✔  Create Account");
        btnRegister.setPreferredSize(new Dimension(160, 38));
        btnCancel.addActionListener(e -> dispose());
        btnRegister.addActionListener(e -> register());
        btnPanel.add(btnCancel);
        btnPanel.add(btnRegister);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);

        // ── Role switch: show/hide student fields ─────────────────────────────
        cbRole.addActionListener(e -> toggleStudentFields());
        toggleStudentFields(); // initial state

        pfConfirm.addActionListener(e -> register());
    }

    /** Shows student personal detail fields only when role = Student. */
    private void toggleStudentFields() {
        String sel = (String) cbRole.getSelectedItem();
        boolean isStudent = sel != null && sel.contains("Student");
        studentFieldsPanel.setVisible(isStudent);
        // Resize dialog to fit
        if (isStudent) {
            setSize(Math.max(getWidth(), 540), 680);
        } else {
            setSize(Math.max(getWidth(), 480), 440);
        }
        revalidate(); repaint();
    }

    // ── Registration logic ────────────────────────────────────────────────────

    private void register() {
        String sel      = (String) cbRole.getSelectedItem();
        boolean isStudent = sel != null && sel.contains("Student");
        String  role    = isStudent ? "student" : (sel != null && sel.contains("Teacher") ? "teacher" : "admin");
        String  uname   = tfUsername.getText().trim();
        String  pass    = new String(pfPassword.getPassword());
        String  confirm = new String(pfConfirm.getPassword());

        // ── Common credential validation ──────────────────────────────────────
        if (ValidationUtils.isEmpty(uname))       { err("Username is required."); return; }
        if (uname.length() < 3)                   { err("Username must be at least 3 characters."); return; }
        if (!uname.matches("[A-Za-z0-9_.]+"))     { err("Username: only letters, numbers, dots, underscores allowed."); return; }
        if (pass.length() < 6)                    { err("Password must be at least 6 characters."); return; }
        if (!pass.equals(confirm))                { err("Passwords do not match. Please re-enter."); return; }
        if (userDAO.usernameExists(uname, 0))     { err("Username \"" + uname + "\" is already taken.\nPlease choose a different one."); return; }

        int studentId = 0;

        if (isStudent) {
            // ── Student-specific validation ───────────────────────────────────
            String fullName = tfFullName.getText().trim();
            String email    = tfEmail.getText().trim();
            String phone    = tfPhone.getText().trim();
            String dobStr   = tfDob.getText().trim();
            String address  = tfAddress.getText().trim();

            if (ValidationUtils.isEmpty(fullName))    { err("Full Name is required."); return; }
            if (!ValidationUtils.isValidEmail(email)) { err("Please enter a valid email address."); return; }
            if (!ValidationUtils.isValidPhone(phone)) { err("Phone format should be: 0300-1234567"); return; }

            Date dob = null;
            if (!dobStr.isEmpty()) {
                try { dob = SDF.parse(dobStr); }
                catch (ParseException ex) { err("Date of Birth format: yyyy-MM-dd\n(e.g. 2002-03-15)"); return; }
            }

            // Email must be unique in students table
            if (studentDAO.emailExists(email, 0)) {
                err("A student with email \"" + email + "\" already exists.\n"
                  + "If you already have an account, please log in instead."); return;
            }

            // ── Create students record first ──────────────────────────────────
            Student newStudent = new Student();
            newStudent.setName(fullName);
            newStudent.setEmail(email);
            newStudent.setPhone(phone.isEmpty() ? null : phone);
            newStudent.setDob(dob);
            newStudent.setAddress(address.isEmpty() ? null : address);
            newStudent.setEnrollmentDate(new Date());

            studentId = studentDAO.addStudent(newStudent);
            if (studentId <= 0) {
                err("Failed to create student record. Please try again."); return;
            }
        }

        // ── Create users record ───────────────────────────────────────────────
        User newUser = new User(0, uname, pass, role, studentId);
        int uid = userDAO.addUser(newUser);

        if (uid > 0) {
            createdUsername = uname;
            registered = true;
            dispose();
        } else {
            // Roll back student record if user creation failed
            if (studentId > 0) studentDAO.deleteStudent(studentId);
            err("Account creation failed. Please try again.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JLabel bold(String text) {
        JLabel l = UITheme.createLabel(text);
        l.setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
        return l;
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Registration Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── Result accessors ──────────────────────────────────────────────────────

    public boolean isRegistered()       { return registered; }
    public String  getCreatedUsername() { return createdUsername; }
}
