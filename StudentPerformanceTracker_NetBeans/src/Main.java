import utils.UITheme;
import view.LoginFrame;

import javax.swing.*;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║        Student Performance Tracking System                      ║
 * ║        Department of Software Engineering – MUET Khairpur       ║
 * ║        Course: SW121 – Object Oriented Programming              ║
 * ║        Instructor: Engr. Asmatullah Zubair                      ║
 * ║        Batch: K24SW                                             ║
 * ╚══════════════════════════════════════════════════════════════════╝
 *
 * Main class – application entry point.
 * Applies the Look & Feel and launches the LoginFrame on the EDT.
 */
public class Main {

    public static void main(String[] args) {

        // Apply Nimbus L&F before any Swing component is created
        UITheme.applyLookAndFeel();

        // Launch on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
