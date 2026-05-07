package view;

import controller.GradeController;
import controller.GradeController.SaveResult;
import dao.GradeDAO;
import model.Grade;
import utils.UITheme;
import utils.ValidationUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for entering or updating a student grade. Resizable.
 */
public class GradeEntryDialog extends JDialog {

    private final GradeController gradeController = new GradeController();
    private final GradeDAO        gradeDAO        = new GradeDAO();

    private boolean saved = false;

    private final int    preEnrollmentId;
    private JLabel       lblGradeLetter, lblStatus;
    private JTextField   tfGradeValue, tfSemester, tfAcademicYear;

    public GradeEntryDialog(Window owner, int enrollmentId, String studentName, String courseInfo) {
        super(owner, "Grade Entry", ModalityType.APPLICATION_MODAL);
        this.preEnrollmentId = enrollmentId;
        initComponents(studentName, courseInfo);
        loadExisting();
    }

    private void initComponents(String studentName, String courseInfo) {
        setSize(460, 440);
        setMinimumSize(new Dimension(380, 400));
        setLocationRelativeTo(getOwner());
        setResizable(true); // ← RESIZABLE

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        header.setBackground(UITheme.PRIMARY_DARK);
        JLabel title = new JLabel("📝  Grade Entry / Update");
        title.setFont(UITheme.FONT_HEADING);
        title.setForeground(Color.WHITE);
        header.add(title);
        root.add(header, BorderLayout.NORTH);

        JPanel form = UITheme.cardPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 28, 18, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(7, 4, 7, 4);

        // Student info (read-only)
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Student:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        JLabel lblStudent = new JLabel(studentName);
        lblStudent.setFont(UITheme.FONT_BODY); lblStudent.setForeground(UITheme.PRIMARY);
        form.add(lblStudent, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Course:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        JLabel lblCourse = new JLabel(courseInfo);
        lblCourse.setFont(UITheme.FONT_BODY); lblCourse.setForeground(UITheme.PRIMARY);
        form.add(lblCourse, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(new JSeparator(), gc);
        gc.gridwidth = 1;

        // Grade value
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Grade (0–100): *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        tfGradeValue = UITheme.createField(10);
        tfGradeValue.setToolTipText("Numeric grade, e.g. 85.5");
        form.add(tfGradeValue, gc);

        // Live grade letter
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Grade Letter:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        lblGradeLetter = new JLabel("—");
        lblGradeLetter.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGradeLetter.setForeground(UITheme.PRIMARY);
        form.add(lblGradeLetter, gc);

        // Status
        gc.gridx = 0; gc.gridy = 5; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Status:"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        lblStatus = new JLabel("—");
        lblStatus.setFont(UITheme.FONT_BODY);
        form.add(lblStatus, gc);

        // Semester
        gc.gridx = 0; gc.gridy = 6; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Semester: *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        tfSemester = UITheme.createField(12);
        tfSemester.setToolTipText("Spring / Fall / Summer");
        form.add(tfSemester, gc);

        // Academic year
        gc.gridx = 0; gc.gridy = 7; gc.weightx = 0.35;
        form.add(UITheme.createLabel("Academic Year: *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        tfAcademicYear = UITheme.createField(8);
        tfAcademicYear.setText("2024");
        form.add(tfAcademicYear, gc);

        root.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBackground(UITheme.BG_MAIN);
        JButton btnCancel = UITheme.neutralButton("Cancel");
        JButton btnSave   = UITheme.successButton("Save Grade");
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());
        btnPanel.add(btnCancel); btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);

        // Live update as user types
        tfGradeValue.addCaretListener(e -> updateDisplay());
    }

    private void loadExisting() {
        Grade g = gradeDAO.getGradeByEnrollmentId(preEnrollmentId);
        if (g != null) {
            tfGradeValue.setText(String.valueOf(g.getGradeValue()));
            tfSemester.setText(g.getSemester());
            tfAcademicYear.setText(g.getAcademicYear());
            updateDisplay();
        }
    }

    private void updateDisplay() {
        double val = ValidationUtils.parseGrade(tfGradeValue.getText());
        if (val >= 0 && val <= 100) {
            String letter = ValidationUtils.computeGradeLetter(val);
            lblGradeLetter.setText(letter);
            boolean pass = val >= 50;
            lblStatus.setText(pass ? "✅  Pass" : "❌  Fail");
            lblStatus.setForeground(pass ? UITheme.ACCENT : UITheme.DANGER);
        } else {
            lblGradeLetter.setText("—");
            lblStatus.setText("—");
            lblStatus.setForeground(UITheme.TEXT_MUTED);
        }
    }

    private void save() {
        SaveResult result = gradeController.saveGrade(
            preEnrollmentId, tfGradeValue.getText(), tfSemester.getText(), tfAcademicYear.getText());
        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
            saved = true; dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isSaved() { return saved; }
}
