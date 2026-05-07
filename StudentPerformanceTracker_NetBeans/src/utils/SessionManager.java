package utils;

import model.User;

/**
 * Singleton session manager that holds the currently logged-in user.
 * Provides a simple way to share session state across the whole application.
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    /** Returns the singleton instance. */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /** Stores the authenticated user in the session. */
    public void login(User user) {
        this.currentUser = user;
    }

    /** Clears the session (logout). */
    public void logout() {
        this.currentUser = null;
    }

    /** Returns the currently logged-in user, or null if not authenticated. */
    public User getCurrentUser() {
        return currentUser;
    }

    /** Convenience method: returns true if a user is currently logged in. */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Convenience: true if current user has Admin role. */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /** Convenience: true if current user has Teacher role. */
    public boolean isTeacher() {
        return currentUser != null && currentUser.isTeacher();
    }

    /** Convenience: true if current user has Student role. */
    public boolean isStudent() {
        return currentUser != null && currentUser.isStudent();
    }
}
