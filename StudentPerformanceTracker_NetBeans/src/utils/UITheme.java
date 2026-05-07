package utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Centralised UI theming constants and helpers.
 * Import this class to keep a consistent look across all views.
 */
public class UITheme {

    // ── Colour Palette ──────────────────────────────────────────────────────────
    public static final Color PRIMARY      = new Color(26,  86, 159);   // Deep Blue
    public static final Color PRIMARY_DARK = new Color(15,  57, 112);   // Darker Blue
    public static final Color SECONDARY    = new Color(52, 152, 219);   // Sky Blue
    public static final Color ACCENT       = new Color(46, 204, 113);   // Green
    public static final Color DANGER       = new Color(231, 76,  60);   // Red
    public static final Color WARNING      = new Color(243,156,  18);   // Orange
    public static final Color BG_MAIN      = new Color(236,240,245);   // Light Grey-Blue
    public static final Color BG_CARD      = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(44,  62,  80);  // Dark Slate
    public static final Color TEXT_MUTED   = new Color(127,140,141);   // Grey
    public static final Color TABLE_ALT    = new Color(235,245,255);   // Light blue row

    // ── Fonts ───────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD,  13);

    // ── Borders ─────────────────────────────────────────────────────────────────
    public static final Border BORDER_NONE = new EmptyBorder(0, 0, 0, 0);
    public static final Border BORDER_FORM = new EmptyBorder(8, 12, 8, 12);

    // ── Button factory ───────────────────────────────────────────────────────────

    /** Creates a styled button with the given background colour. */
    public static JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bg;
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    /** Primary blue button */
    public static JButton primaryButton(String text) {
        return createButton(text, PRIMARY);
    }

    /** Green success button */
    public static JButton successButton(String text) {
        return createButton(text, ACCENT);
    }

    /** Red danger button */
    public static JButton dangerButton(String text) {
        return createButton(text, DANGER);
    }

    /** Grey neutral button */
    public static JButton neutralButton(String text) {
        return createButton(text, TEXT_MUTED);
    }

    // ── Field factory ────────────────────────────────────────────────────────────

    /** Creates a styled JTextField. */
    public static JTextField createField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189,195,199), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    /** Creates a styled JPasswordField. */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_BODY);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189,195,199), 1, true),
            new EmptyBorder(6, 8, 6, 8)
        ));
        return pf;
    }

    /** Creates a styled JComboBox. */
    public static <T> JComboBox<T> createCombo() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    // ── Label factory ────────────────────────────────────────────────────────────

    public static JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel createHeading(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    // ── Table styling ────────────────────────────────────────────────────────────

    /**
     * Applies the application theme to the given JTable.
     * Call once after creating a JTable and its model.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setGridColor(new Color(220,225,230));
        table.setShowGrid(true);
        table.setSelectionBackground(SECONDARY);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Alternating row colours via custom renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? BG_CARD : TABLE_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADING);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(JLabel.LEFT);
    }

    // ── Panel helpers ────────────────────────────────────────────────────────────

    /** Returns a panel with BG_MAIN background and the given layout. */
    public static JPanel mainPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG_MAIN);
        return p;
    }

    /** Returns a white card panel with rounded border and padding. */
    public static JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,225,230), 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        return p;
    }

    // ── Global L&F ───────────────────────────────────────────────────────────────

    /**
     * Sets the Nimbus Look & Feel. Call once at application startup in main().
     */
    public static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    // Override Nimbus defaults
                    UIManager.put("control",          BG_MAIN);
                    UIManager.put("nimbusBase",       PRIMARY);
                    UIManager.put("nimbusBlueGrey",   new Color(180,195,210));
                    UIManager.put("nimbusFocus",      SECONDARY);
                    break;
                }
            }
        } catch (Exception e) {
            // Fall back to system default
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }
    }
}
