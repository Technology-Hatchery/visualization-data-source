package com.mobinsight.server;

import java.io.IOException;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;
import javax.inject.Provider;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mobinsight.user.GaeUser;
import com.mobinsight.user.GaeUserDAO;

public class BaseServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Logger. */
	private static final Logger LOG = Logger.getLogger(BaseServlet.class.getName());

    protected Provider<GaeUserDAO> daoProvider;

    protected BaseServlet(Provider<GaeUserDAO> daoProvider) {
        this.daoProvider = daoProvider;
    }

    protected void issueJson(HttpServletResponse response, int status, String... args) throws IOException {
        Preconditions.checkArgument(args.length % 2 == 0, "There must be an even number of strings");
            try {
            JSONObject obj = new JSONObject();
            for (int i = 0; i < args.length; i += 2) {
                obj.put(args[i], args[i+1]);
            }
            issueJson(response, status, obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected void issueJson(HttpServletResponse response, int status, JSONObject obj) throws IOException {
        issue("application/json", status, obj.toString(), response);
    }
    
    protected void issue(String mimeType, int returnCode, String output, HttpServletResponse response) throws IOException {
        response.setContentType(mimeType);
        response.setStatus(returnCode);
        response.getWriter().println(output);
    }
    
    protected int intParameter(String name, HttpServletRequest request, int deflt) {
        String s = request.getParameter(name);
        return (s == null) ? deflt : Integer.parseInt(s);
    }

    protected boolean booleanParameter(String name, HttpServletRequest request, boolean deflt) {
        String s = request.getParameter(name);
        return (s == null) ? deflt : Boolean.parseBoolean(s);
    }
    
    /**
     * Login and make sure you then have a new session.  This helps prevent session fixation attacks.
     *
     * @param token
     * @param subject
     */
    protected static void loginWithNewSession(AuthenticationToken token, Subject subject) {
        Session originalSession = subject.getSession();

        Map<Object, Object> attributes = Maps.newLinkedHashMap();
        Collection<Object> keys = originalSession.getAttributeKeys();
        for(Object key : keys) {
            Object value = originalSession.getAttribute(key);
            if (value != null) {
                attributes.put(key, value);
            }
        }
        originalSession.stop();
        subject.login(token);

        Session newSession = subject.getSession();
        for(Object key : attributes.keySet() ) {
            newSession.setAttribute(key, attributes.get(key));
        }
    }

    protected boolean isCurrentUserAdmin() {
        Subject subject = SecurityUtils.getSubject();
        return subject.hasRole("admin");
    }

    @SuppressWarnings({"unchecked"})
    protected GaeUser getCurrentGaeUser() {
        Subject subject = SecurityUtils.getSubject();
        String email = (String)subject.getPrincipal();
        if (email == null) {
            return null;
        } else {
            GaeUserDAO dao = daoProvider.get();
            return dao.findUser(email);
        }
    }
}
