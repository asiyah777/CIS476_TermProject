package com.driveshare.controller;

import com.driveshare.model.User;
import com.driveshare.patterns.singleton.SessionManager;

/**
 * Shared helper for all controllers.
 * Resolves the current user from the X-Session-Token header via SessionManager (Singleton).
 */
public abstract class BaseController {

    /** Returns the logged-in user for the given token, or a fallback user with id=1 for unauthenticated calls. */
    protected User resolveUser(String token) {
        User user = SessionManager.getInstance().getUser(token);
        if (user == null) {
            // Fallback so existing in-memory demo still works without a token
            User fallback = new User();
            fallback.setId(1L);
            return fallback;
        }
        return user;
    }

    protected Long resolveUserId(String token) {
        return resolveUser(token).getId();
    }
}
