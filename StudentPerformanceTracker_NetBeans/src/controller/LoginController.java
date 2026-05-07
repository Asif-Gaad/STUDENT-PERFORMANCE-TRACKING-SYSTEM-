package controller;

import dao.UserDAO;
import model.User;
import utils.SessionManager;
import utils.ValidationUtils;

/**
 * Controller handling login authentication logic.
 * Decouples the LoginFrame view from DAO and session management.
 */
public class LoginController {

    private final UserDAO userDAO;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    /**
     * Validates the supplied credentials and, if correct, stores the user
     * in the SessionManager.
     *
     * @param username entered username
     * @param password entered password
     * @return LoginResult enum describing the outcome
     */
    public LoginResult login(String username, String password) {

        // Basic input validation
        if (ValidationUtils.isEmpty(username)) return LoginResult.EMPTY_USERNAME;
        if (ValidationUtils.isEmpty(password)) return LoginResult.EMPTY_PASSWORD;

        User user = userDAO.authenticate(username.trim(), password.trim());
        if (user == null) return LoginResult.INVALID_CREDENTIALS;

        // Store in session
        SessionManager.getInstance().login(user);
        return LoginResult.SUCCESS;
    }

    /** Clears the current session. */
    public void logout() {
        SessionManager.getInstance().logout();
    }

    /** Outcome enum returned by login(). */
    public enum LoginResult {
        SUCCESS,
        EMPTY_USERNAME,
        EMPTY_PASSWORD,
        INVALID_CREDENTIALS
    }
}
