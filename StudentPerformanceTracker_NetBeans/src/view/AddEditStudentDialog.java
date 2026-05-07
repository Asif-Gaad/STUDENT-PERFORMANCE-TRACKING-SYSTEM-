package view;

import dao.StudentDAO;
import model.Student;
import utils.UITheme;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Modal dialog for Adding or Editing a Student record.
 * Resizable. Pass null student for Add mode; pass existing Student for Edit mode.
 */
public class AddEditStudentDialog extends JDialog {

    private final Student    student;
    private final StudentDAO studentDAO;
    private       boolean    saved = false;

    private JTextField tfName, tfEmail, tfPhone, tfDob, tfAddress, tfEnrollDate;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    public AddEditStudentDialog(Window owner, Student student) {
        super(owner, student == null ? "Add New Student" : "Edit Student",
              ModalityType.APPLICATION_MODAL);
        this.student    = student;
        this.studentDAO = new StudentDAO();
        initComponents();
        if (student != null) populate();
    }

    private void initComponents() {
        setSize(500, 480);
        setMinimumSize(new Dimension(400, 420));
        setLocationRelativeTo(getOwner());
        setResizable(true); // ← RESIZABLE

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(UITheme.PRIMARY);
        JLabel title = new JLabel(student == null ? "➕  Add New Student" : "✏️  Edit Student");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel form = UITheme.cardPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 28, 20, 28));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(7, 4, 7, 4);
        gc.weightx = 0.35;

        int row = 0;
        addRow(form, gc, row++, "Full Name: *",       tfName      = UITheme.createField(22));
        addRow(form, gc, row++, "Email: *",            tfEmail     = UITheme.createField(22));
        addRow(form, gc, row++, "Phone:",              tfPhone     = UITheme.createField(22));
        tfPhone.setToolTipText("Format: 0300-1234567");
        addRow(form, gc, row++, "Date of Birth:",     tfDob       = UITheme.createField(22));
        tfDob.setToolTipText("Format: yyyy-MM-dd");
        addRow(form, gc, row++, "Address:",           tfAddress   = UITheme.createField(22));
        addRow(form, gc, row,   "Enrollment Date: *", tfEnrollDate = UITheme.createField(22));
        tfEnrollDate.setToolTipText("Format: yyyy-MM-dd");
        if (student == null) tfEnrollDate.setText(SDF.format(new Date()));

        root.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBackground(UITheme.BG_MAIN);
        JButton btnCancel = UITheme.neutralButton("Cancel");
        JButton btnSave   = UITheme.successButton(student == null ? "Add Student" : "Save Changes");
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());
        btnPanel.add(btnCancel); btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addRow(JPanel f, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0.35;
        f.add(UITheme.createLabel(label), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        f.add(field, gc);
    }

    private void populate() {
        tfName.setText(student.getName());
        tfEmail.setText(student.getEmail());
        tfPhone.setText(student.getPhone() != null ? student.getPhone() : "");
        tfDob.setText(student.getDob() != null ? SDF.format(student.getDob()) : "");
        tfAddress.setText(student.getAddress() != null ? student.getAddress() : "");
        tfEnrollDate.setText(student.getEnrollmentDate() != null
                ? SDF.format(student.getEnrollmentDate()) : SDF.format(new Date()));
    }

    private void save() {
        String name      = tfName.getText().trim();
        String email     = tfEmail.getText().trim();
        String phone     = tfPhone.getText().trim();
        String dobStr    = tfDob.getText().trim();
        String address   = tfAddress.getText().trim();
        String enrollStr = tfEnrollDate.getText().trim();

        if (ValidationUtils.isEmpty(name))            { showError("Full Name is required."); return; }
        if (!ValidationUtils.isValidEmail(email))     { showError("Enter a valid email address."); return; }
        if (!ValidationUtils.isValidPhone(phone))     { showError("Phone format: 0300-1234567"); return; }

        Date dob = null;
        if (!dobStr.isEmpty()) { dob = parseDate(dobStr); if (dob == null) { showError("DOB format: yyyy-MM-dd"); return; } }
        Date enrollDate = parseDate(enrollStr);
        if (enrollDate == null)                       { showError("Enrollment Date format: yyyy-MM-dd"); return; }

        int excludeId = (student != null) ? student.getId() : 0;
        if (studentDAO.emailExists(email, excludeId)) { showError("A student with this email already exists."); return; }

        Student s = (student != null) ? student : new Student();
        s.setName(name); s.setEmail(email);
        s.setPhone(phone.isEmpty() ? null : phone);
        s.setDob(dob); s.setAddress(address.isEmpty() ? null : address);
        s.setEnrollmentDate(enrollDate);

        if (student == null) {
            int id = studentDAO.addStudent(s);
            if (id > 0) { saved = true; dispose(); } else showError("Failed to save student.");
        } else {
            int rows = studentDAO.updateStudent(s);
            if (rows > 0) { saved = true; dispose(); } else showError("Failed to update student.");
        }
    }

    private Date parseDate(String s) {
        try { return SDF.parse(s); } catch (ParseException e) { return null; }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() { return saved; }
}
