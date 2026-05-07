package utils;

import java.util.regex.Pattern;

/**
 * Utility class containing static validation helpers used across the application.
 */
public class ValidationUtils {

    // ── Regex patterns ──────────────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^(\\+92|0)[0-9]{9,10}$");

    private static final Pattern COURSE_CODE_PATTERN =
        Pattern.compile("^[A-Z]{2,4}[0-9]{3,4}$");

    // ── Text validation ─────────────────────────────────────────────────────────

    /**
     * Returns true if the string is null or blank after trimming.
     */
    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Returns true if the string is non-null and non-blank.
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    // ── Email validation ────────────────────────────────────────────────────────

    /**
     * Returns true if the email matches a valid format.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ── Phone validation ────────────────────────────────────────────────────────

    /**
     * Returns true if the phone number matches Pakistani format (0xxx-xxxxxxx).
     * Accepts with or without hyphens.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // phone is optional
        String cleaned = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    // ── Grade validation ────────────────────────────────────────────────────────

    /**
     * Returns true if the grade value is between 0 and 100 (inclusive).
     */
    public static boolean isValidGrade(double grade) {
        return grade >= 0 && grade <= 100;
    }

    /**
     * Parses a string to double for grade input.
     * @return the parsed value, or -1 if not parseable
     */
    public static double parseGrade(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ── Grade letter computation ─────────────────────────────────────────────────

    /**
     * Converts a numeric grade value to a letter grade.
     * Scale: A+ ≥ 95, A ≥ 90, B+ ≥ 85, B ≥ 80, B- ≥ 75,
     *        C ≥ 65, D ≥ 50, F < 50
     */
    public static String computeGradeLetter(double value) {
        if (value >= 95) return "A+";
        if (value >= 90) return "A";
        if (value >= 85) return "B+";
        if (value >= 80) return "B";
        if (value >= 75) return "B-";
        if (value >= 65) return "C";
        if (value >= 50) return "D";
        return "F";
    }

    // ── Course code validation ───────────────────────────────────────────────────

    /**
     * Returns true if the course code matches the expected pattern (e.g. CS101).
     */
    public static boolean isValidCourseCode(String code) {
        return code != null && COURSE_CODE_PATTERN.matcher(code.trim()).matches();
    }

    // ── Credits validation ───────────────────────────────────────────────────────

    /**
     * Returns true if credits is between 1 and 6.
     */
    public static boolean isValidCredits(int credits) {
        return credits >= 1 && credits <= 6;
    }

    /**
     * Parses credits from a string.
     * @return parsed value or -1 if invalid
     */
    public static int parseCredits(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
