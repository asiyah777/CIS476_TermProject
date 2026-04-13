// =============================================
// Session helpers — token-based auth
// =============================================

function getToken() {
    return sessionStorage.getItem('dsToken') || '';
}

function getMyUserId() {
    return parseInt(sessionStorage.getItem('dsUserId') || '1');
}

/**
 * Drop-in replacement for fetch() that automatically attaches
 * the X-Session-Token header on every request.
 */
function apiFetch(url, options = {}) {
    const token = getToken();
    const headers = Object.assign({}, options.headers || {});
    if (token) headers['X-Session-Token'] = token;
    return fetch(url, Object.assign({}, options, { headers }));
}
