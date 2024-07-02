package mg.p16.Util;

import javax.servlet.http.HttpSession;
import java.util.Objects;

public class MySession {
    private HttpSession session;

    public MySession() {
    }

    public MySession(HttpSession session) {
        this.session = session;
    }

    public HttpSession getSession() {
        return this.session;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public MySession session(HttpSession session) {
        setSession(session);
        return this;
    }

    // Adding a session attribute
    public MySession add(String key, Object value) {
        Objects.requireNonNull(session, "HttpSession must not be null");
        session.setAttribute(key, value);
        return this;
    }

    // Getting a session attribute
    public Object get(String key) {
        Objects.requireNonNull(session, "HttpSession must not be null");
        return session.getAttribute(key);
    }

    // Removing a session attribute
    public MySession remove(String key) {
        Objects.requireNonNull(session, "HttpSession must not be null");
        session.removeAttribute(key);
        return this;
    }
}
