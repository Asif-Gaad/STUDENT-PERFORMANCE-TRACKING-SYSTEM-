package view;

import utils.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generic report viewer dialog – resizable, HTML display, export as HTML/TXT.
 */
public class ReportViewer extends JDialog {

    private final String reportTitle;
    private final String htmlContent;
    private final String txtContent;

    public ReportViewer(Window owner, String reportTitle, String htmlContent, String txtContent) {
        super(owner, "Report: " + reportTitle, ModalityType.APPLICATION_MODAL);
        this.reportTitle = reportTitle;
        this.htmlContent = htmlContent;
        this.txtContent  = txtContent;
        initComponents();
    }

    private void initComponents() {
        setSize(800, 650);
        setMinimumSize(new Dimension(600, 450));
        setLocationRelativeTo(getOwner());
        setResizable(true); // ← RESIZABLE

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(10, 16, 10, 16));
        JLabel lblTitle = new JLabel("📄  " + reportTitle);
        lblTitle.setFont(UITheme.FONT_HEADING); lblTitle.setForeground(Color.WHITE);
        JLabel lblDate = new JLabel("Generated: " + new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date()));
        lblDate.setFont(UITheme.FONT_SMALL); lblDate.setForeground(new Color(180, 210, 240));
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblDate,  BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Content
        JEditorPane editor = new JEditorPane("text/html", htmlContent);
        editor.setEditable(false);
        editor.setBackground(Color.WHITE);
        editor.setBorder(new EmptyBorder(12, 16, 12, 16));
        JScrollPane scroll = new JScrollPane(editor);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        btnPanel.setBackground(UITheme.BG_MAIN);
        JButton btnHTML  = UITheme.primaryButton("💾 Save as HTML");
        JButton btnTXT   = UITheme.neutralButton("📃 Save as TXT");
        JButton btnClose = UITheme.dangerButton("Close");
        btnHTML.addActionListener(e -> save(false));
        btnTXT.addActionListener(e  -> save(true));
        btnClose.addActionListener(e -> dispose());
        btnPanel.add(btnTXT); btnPanel.add(btnHTML); btnPanel.add(btnClose);
        root.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void save(boolean asTxt) {
        JFileChooser fc = new JFileChooser();
        String ext  = asTxt ? "txt" : "html";
        String safe = reportTitle.replaceAll("[^A-Za-z0-9_-]", "_");
        fc.setSelectedFile(new File(safe + "." + ext));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fc.getSelectedFile()), StandardCharsets.UTF_8))) {
                bw.write(asTxt ? txtContent : htmlContent);
                JOptionPane.showMessageDialog(this,
                    "Saved:\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Static builders ───────────────────────────────────────────────────────

    public static String buildTableHTML(String title, String[] headers, Object[][] rows, String summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>")
          .append("body{font-family:Arial,sans-serif;font-size:13px;margin:20px;color:#2c3e50;}")
          .append("h2{color:#1a569f;border-bottom:2px solid #1a569f;padding-bottom:6px;}")
          .append("table{border-collapse:collapse;width:100%;margin-top:10px;}")
          .append("th{background:#1a569f;color:white;padding:8px 10px;text-align:left;}")
          .append("td{padding:7px 10px;border-bottom:1px solid #ddd;}")
          .append("tr:nth-child(even){background:#eaf4ff;}")
          .append(".summary{margin-top:14px;font-weight:bold;color:#1a569f;}")
          .append(".pass{color:#27ae60;font-weight:bold;}.fail{color:#e74c3c;font-weight:bold;}")
          .append("</style></head><body>");
        sb.append("<h2>").append(title).append("</h2><table><tr>");
        for (String h : headers) sb.append("<th>").append(h).append("</th>");
        sb.append("</tr>");
        for (Object[] row : rows) {
            sb.append("<tr>");
            for (Object cell : row) {
                String v = cell != null ? cell.toString() : "";
                if ("Pass".equals(v))      sb.append("<td class='pass'>").append(v).append("</td>");
                else if ("Fail".equals(v)) sb.append("<td class='fail'>").append(v).append("</td>");
                else                       sb.append("<td>").append(v).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        if (summary != null && !summary.isEmpty()) sb.append("<p class='summary'>").append(summary).append("</p>");
        sb.append("<br/><small>Generated: ").append(new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()))
          .append("</small></body></html>");
        return sb.toString();
    }

    public static String buildTableTXT(String title, String[] headers, Object[][] rows, String summary) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("\n").append("=".repeat(title.length())).append("\n\n");
        int[] widths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) widths[i] = headers[i].length();
        for (Object[] row : rows)
            for (int i = 0; i < Math.min(row.length, widths.length); i++) {
                String v = row[i] != null ? row[i].toString() : "";
                widths[i] = Math.max(widths[i], v.length());
            }
        for (int i = 0; i < headers.length; i++) sb.append(pad(headers[i], widths[i] + 2));
        sb.append("\n");
        for (int w : widths) sb.append("-".repeat(w + 2));
        sb.append("\n");
        for (Object[] row : rows) {
            for (int i = 0; i < headers.length; i++) {
                String v = (i < row.length && row[i] != null) ? row[i].toString() : "";
                sb.append(pad(v, widths[i] + 2));
            }
            sb.append("\n");
        }
        if (summary != null && !summary.isEmpty()) sb.append("\n").append(summary).append("\n");
        sb.append("\nGenerated: ").append(new SimpleDateFormat("dd MMM yyyy HH:mm").format(new Date()));
        return sb.toString();
    }

    private static String pad(String s, int n) { return String.format("%-" + n + "s", s); }
}
