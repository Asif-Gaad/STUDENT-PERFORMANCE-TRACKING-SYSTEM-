package view;

import controller.LoginController;
import controller.LoginController.LoginResult;
import utils.SessionManager;
import utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Login screen for all three roles: Admin, Teacher, Student.
 * Fully resizable. Routes to the correct dashboard after login.
 */
public class LoginFrame extends JFrame {

    private JTextField     tfUsername;
    private JPasswordField pfPassword;
    private JLabel         lblError;

    private final LoginController controller = new LoginController();

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Student Performance Tracker – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 640);
        setMinimumSize(new Dimension(420, 560));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // ── Header ────────────────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(UITheme.PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel icon = new JLabel("🎓");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Student Performance Tracking System");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("MUET – Dept. of Software Engineering, Khairpur");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(180, 210, 240));
        sub.setAlignmentX(CENTER_ALIGNMENT);

        // Role badge row
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        badges.setOpaque(false);
        badges.add(roleBadge("👑 Admin",    new Color(192, 57, 43)));
        badges.add(roleBadge("📖 Teacher",  new Color(41, 128, 185)));
        badges.add(roleBadge("🎒 Student",  new Color(39, 174, 96)));

        header.add(icon);
        header.add(Box.createVerticalStrut(8));
        header.add(appName);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);
        header.add(Box.createVerticalStrut(12));
        header.add(badges);
        root.add(header, BorderLayout.NORTH);

        // ── Login card ────────────────────────────────────────────────────────
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(UITheme.BG_MAIN);

        JPanel card = UITheme.cardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(28, 38, 28, 38));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets  = new Insets(7, 2, 7, 2);

        JLabel lblTitle = new JLabel("Sign In");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        card.add(lblTitle, gc);

        gc.gridy = 1;
        card.add(new JSeparator(), gc);

        gc.gridy = 2; gc.gridwidth = 1; gc.weightx = 0.3;
        card.add(UITheme.createLabel("Username:"), gc);
        tfUsername = UITheme.createField(22);
        gc.gridx = 1; gc.weightx = 0.7;
        card.add(tfUsername, gc);

        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0.3;
        card.add(UITheme.createLabel("Password:"), gc);
        pfPassword = UITheme.createPasswordField(22);
        gc.gridx = 1; gc.weightx = 0.7;
        card.add(pfPassword, gc);

        lblError = new JLabel(" ");
        lblError.setForeground(UITheme.DANGER);
        lblError.setFont(UITheme.FONT_SMALL);
        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2; gc.weightx = 1.0;
        card.add(lblError, gc);

        // Login button
        JButton btnLogin = UITheme.primaryButton("Login");
        btnLogin.setPreferredSize(new Dimension(100, 44));
        JPanel loginRow = new JPanel(new BorderLayout());
        loginRow.setOpaque(false);
        loginRow.add(btnLogin, BorderLayout.CENTER);
        gc.gridy = 5;
        card.add(loginRow, gc);

        // OR divider
        gc.gridy = 6; gc.insets = new Insets(14, 2, 14, 2);
        card.add(orDivider(), gc);

        // Sign-up button
        JButton btnSignUp = UITheme.successButton("Create New Account");
        btnSignUp.setPreferredSize(new Dimension(100, 44));
        JPanel signUpRow = new JPanel(new BorderLayout());
        signUpRow.setOpaque(false);
        signUpRow.add(btnSignUp, BorderLayout.CENTER);
        gc.gridy = 7; gc.insets = new Insets(0, 2, 0, 2);
        card.add(signUpRow, gc);

        // Hint
        JLabel hint = new JLabel(
            "<html><center style='color:#999;font-size:10px;'>"
            + "Admin: <b>admin / admin123</b> &nbsp;·&nbsp; Teacher: <b>teacher / teacher123</b><br/>"
            + "Student sample: <b>ali.hassan / ali123</b>"
            + "</center></html>",
            SwingConstants.CENTER);
        hint.setFont(UITheme.FONT_SMALL);
        gc.gridy = 8; gc.insets = new Insets(12, 2, 0, 2);
        card.add(hint, gc);

        GridBagConstraints cc = new GridBagConstraints();
        cc.fill    = GridBagConstraints.HORIZONTAL;
        cc.weightx = 1.0;
        cc.insets  = new Insets(22, 36, 22, 36);
        centre.add(card, cc);
        root.add(centre, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("© 2025 Dept. of Software Engineering, MUET Khairpur", SwingConstants.CENTER);
        footer.setFont(UITheme.FONT_SMALL);
        footer.setForeground(UITheme.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(6, 0, 6, 0));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        // ── Listeners ─────────────────────────────────────────────────────────
        btnLogin.addActionListener(e -> attemptLogin());
        tfUsername.addActionListener(e -> attemptLogin());
        pfPassword.addActionListener(e -> attemptLogin());
        btnSignUp.addActionListener(e -> openSignUp());
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    private void attemptLogin() {
        lblError.setText(" ");
        LoginResult result = controller.login(
            tfUsername.getText(), new String(pfPassword.getPassword()));

        switch (result) {
            case SUCCESS:
                routeToDashboard();
                break;
            case EMPTY_USERNAME:
                lblError.setText("Please enter your username.");
                tfUsername.requestFocus();
                break;
            case EMPTY_PASSWORD:
                lblError.setText("Please enter your password.");
                pfPassword.requestFocus();
                break;
            case INVALID_CREDENTIALS:
                lblError.setText("Incorrect username or password. Please try again.");
                pfPassword.setText("");
                shake();
                break;
        }
    }

    /** Routes to AdminDashboard, TeacherDashboard, or StudentDashboard. */
    private void routeToDashboard() {
        dispose();
        SwingUtilities.invokeLater(() -> {
            JFrame dash;
            if      (SessionManager.getInstance().isAdmin())   dash = new AdminDashboard();
            else if (SessionManager.getInstance().isTeacher()) dash = new TeacherDashboard();
            else                                               dash = new StudentDashboard();
            dash.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dash.setVisible(true);
        });
    }

    /** Opens the sign-up dialog; pre-fills username on success. */
    private void openSignUp() {
        SignUpDialog dlg = new SignUpDialog(this);
        dlg.setVisible(true);
        if (dlg.isRegistered()) {
            JOptionPane.showMessageDialog(this,
                "Account created successfully!\nYou can now log in with your new credentials.",
                "Welcome!", JOptionPane.INFORMATION_MESSAGE);
            tfUsername.setText(dlg.getCreatedUsername());
            pfPassword.setText("");
            pfPassword.requestFocus();
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private JLabel roleBadge(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        return lbl;
    }

    private JPanel orDivider() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.gridy = 0; g.gridx = 0;
        p.add(new JSeparator(), g);
        g.gridx = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JLabel or = new JLabel("  or  ");
        or.setFont(UITheme.FONT_SMALL); or.setForeground(UITheme.TEXT_MUTED);
        p.add(or, g);
        g.gridx = 2; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(new JSeparator(), g);
        return p;
    }

    private void shake() {
        final int[] n = {0};
        Timer t = new Timer(40, null);
        t.addActionListener(e -> {
            Point p = getLocation();
            setLocation(n[0]++ % 2 == 0 ? p.x + 8 : p.x - 8, p.y);
            if (n[0] >= 8) { t.stop(); setLocation(p.x, p.y); }
        });
        t.start();
    }
}
