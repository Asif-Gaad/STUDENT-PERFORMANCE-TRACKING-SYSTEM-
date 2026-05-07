package controller;

import dao.GradeDAO;
import dao.EnrollmentDAO;
import model.Grade;
import utils.ValidationUtils;

/**
 * Controller for Grade entry and update operations.
 * Performs business-level validation before delegating to GradeDAO.
 */
public class GradeController {

    private final GradeDAO      gradeDAO;
    private final EnrollmentDAO enrollmentDAO;

    public GradeController() {
        this.gradeDAO      = new GradeDAO();
        this.enrollmentDAO = new EnrollmentDAO();
    }

    /**
     * Saves a grade (insert or update) for the given enrollment.
     *
     * @param enrollmentId target enrollment
     * @param gradeValueStr numeric grade as a string (0–100)
     * @param semester       e.g. "Spring"
     * @param academicYear   e.g. "2024"
     * @return SaveResult describing the outcome
     */
    public SaveResult saveGrade(int enrollmentId, String gradeValueStr,
                                String semester, String academicYear) {

        // ── Input validation ────────────────────────────────────────────────────
        if (enrollmentId <= 0)                    return SaveResult.INVALID_ENROLLMENT;
        if (ValidationUtils.isEmpty(gradeValueStr)) return SaveResult.EMPTY_GRADE;
        if (ValidationUtils.isEmpty(semester))       return SaveResult.EMPTY_SEMESTER;
        if (ValidationUtils.isEmpty(academicYear))   return SaveResult.EMPTY_YEAR;

        double gradeValue = ValidationUtils.parseGrade(gradeValueStr);
        if (gradeValue < 0)                        return SaveResult.INVALID_GRADE_FORMAT;
        if (!ValidationUtils.isValidGrade(gradeValue)) return SaveResult.GRADE_OUT_OF_RANGE;

        String gradeLetter = ValidationUtils.computeGradeLetter(gradeValue);

        // ── Determine insert vs update ──────────────────────────────────────────
        Grade existing = gradeDAO.getGradeByEnrollmentId(enrollmentId);
        if (existing == null) {
            // INSERT
            Grade g = new Grade(0, enrollmentId, gradeValue, gradeLetter, semester, academicYear);
            int id = gradeDAO.addGrade(g);
            return id > 0 ? SaveResult.CREATED : SaveResult.DB_ERROR;
        } else {
            // UPDATE
            existing.setGradeValue(gradeValue);
            existing.setGradeLetter(gradeLetter);
            existing.setSemester(semester);
            existing.setAcademicYear(academicYear);
            int rows = gradeDAO.updateGrade(existing);
            return rows > 0 ? SaveResult.UPDATED : SaveResult.DB_ERROR;
        }
    }

    /** Deletes a grade by its id. */
    public boolean deleteGrade(int gradeId) {
        return gradeDAO.deleteGrade(gradeId) > 0;
    }

    // ── Result enum ─────────────────────────────────────────────────────────────

    public enum SaveResult {
        CREATED,
        UPDATED,
        INVALID_ENROLLMENT,
        EMPTY_GRADE,
        EMPTY_SEMESTER,
        EMPTY_YEAR,
        INVALID_GRADE_FORMAT,
        GRADE_OUT_OF_RANGE,
        DB_ERROR;

        /** User-friendly message for each result. */
        public String getMessage() {
            switch (this) {
                case CREATED:             return "Grade saved successfully!";
                case UPDATED:             return "Grade updated successfully!";
                case INVALID_ENROLLMENT:  return "Invalid enrollment selected.";
                case EMPTY_GRADE:         return "Please enter a grade value.";
                case EMPTY_SEMESTER:      return "Please enter the semester.";
                case EMPTY_YEAR:          return "Please enter the academic year.";
                case INVALID_GRADE_FORMAT:return "Grade must be a numeric value (e.g. 85.5).";
                case GRADE_OUT_OF_RANGE:  return "Grade must be between 0 and 100.";
                case DB_ERROR:            return "Database error. Please try again.";
                default:                  return "Unknown error.";
            }
        }

        public boolean isSuccess() {
            return this == CREATED || this == UPDATED;
        }
    }
}
