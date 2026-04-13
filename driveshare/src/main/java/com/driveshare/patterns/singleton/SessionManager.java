package com.driveshare.patterns.singleton;

import com.driveshare.model.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton — one instance manages all active user sessions.
 * Each session is identified by a UUID token stored in the browser (sessionStorage).
 * Multiple users can be logged in simultaneously; each has their own token entry.
 */

public class SessionManager {

    private static SessionManager instance;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    // token → User  (thread-safe map so concurrent logins are safe)
    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    /** Creates a new session for the user and returns the session token. */
    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    /** Returns the user for the given token, or null if invalid/expired. */
    public User getUser(String token) {
        if (token == null || token.isBlank()) return null;
        return sessions.get(token);
    }

    /** Removes a session (logout). */
    public void removeSession(String token) {
        if (token != null) sessions.remove(token);
    }
}
