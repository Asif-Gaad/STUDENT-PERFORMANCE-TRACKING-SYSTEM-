package view;

import dao.CourseDAO;
import model.Course;
import utils.UITheme;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for Adding or Editing a Course. Resizable.
 */
public class AddEditCourseDialog extends JDialog {

    private final Course    course;
    private final CourseDAO courseDAO;
    private       boolean   saved = false;

    private JTextField tfCourseName, tfCourseCode, tfCredits, tfTeacherName;

    public AddEditCourseDialog(Window owner, Course course) {
        super(owner, course == null ? "Add New Course" : "Edit Course",
              ModalityType.APPLICATION_MODAL);
        this.course    = course;
        this.courseDAO = new CourseDAO();
        initComponents();
        if (course != null) populate();
    }

    private void initComponents() {
        setSize(460, 380);
        setMinimumSize(new Dimension(380, 340));
        setLocationRelativeTo(getOwner());
        setResizable(true); // ← RESIZABLE

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(UITheme.PRIMARY);
        JLabel title = new JLabel(course == null ? "➕  Add New Course" : "✏️  Edit Course");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel form = UITheme.cardPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 28, 20, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 4, 8, 4);

        int row = 0;
        addRow(form, gc, row++, "Course Name: *", tfCourseName  = UITheme.createField(22));
        addRow(form, gc, row++, "Course Code: *", tfCourseCode  = UITheme.createField(22));
        tfCourseCode.setToolTipText("Format: CS101");
        addRow(form, gc, row++, "Credits (1–6): *", tfCredits   = UITheme.createField(22));
        if (course == null) tfCredits.setText("3");
        addRow(form, gc, row,   "Teacher Name: *", tfTeacherName = UITheme.createField(22));

        root.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBackground(UITheme.BG_MAIN);
        JButton btnCancel = UITheme.neutralButton("Cancel");
        JButton btnSave   = UITheme.successButton(course == null ? "Add Course" : "Save Changes");
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
        tfCourseName.setText(course.getCourseName());
        tfCourseCode.setText(course.getCourseCode());
        tfCredits.setText(String.valueOf(course.getCredits()));
        tfTeacherName.setText(course.getTeacherName());
    }

    private void save() {
        String name    = tfCourseName.getText().trim();
        String code    = tfCourseCode.getText().trim().toUpperCase();
        String credStr = tfCredits.getText().trim();
        String teacher = tfTeacherName.getText().trim();

        if (ValidationUtils.isEmpty(name))    { showError("Course Name is required.");    return; }
        if (ValidationUtils.isEmpty(code))    { showError("Course Code is required.");    return; }
        if (ValidationUtils.isEmpty(teacher)) { showError("Teacher Name is required.");   return; }

        int credits = ValidationUtils.parseCredits(credStr);
        if (!ValidationUtils.isValidCredits(credits)) { showError("Credits must be 1–6."); return; }

        int excludeId = (course != null) ? course.getId() : 0;
        if (courseDAO.courseCodeExists(code, excludeId)) {
            showError("A course with code '" + code + "' already exists."); return;
        }

        Course c = (course != null) ? course : new Course();
        c.setCourseName(name); c.setCourseCode(code);
        c.setCredits(credits); c.setTeacherName(teacher);

        if (course == null) {
            int id = courseDAO.addCourse(c);
            if (id > 0) { saved = true; dispose(); } else showError("Failed to save course.");
        } else {
            int rows = courseDAO.updateCourse(c);
            if (rows > 0) { saved = true; dispose(); } else showError("Failed to update course.");
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isSaved() { return saved; }
}
