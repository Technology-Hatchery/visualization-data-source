package com.mobinsight.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.mortbay.log.Log;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mobinsight.user.GaeUser;
import com.mobinsight.user.GaeUserDAO;

@SuppressWarnings("serial")
public class SurveyServlet extends HttpServlet {
	private Logger LOG = Logger.getLogger(SurveyServlet.class.getSimpleName());
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction txn = em.getTransaction();
        try {
            Survey survey = new Survey();
            survey.readFromJson(em, new JsonReader(req.getReader()));
            txn.begin();
            em.persist(survey);
            txn.commit();
            JsonWriter writer = new JsonWriter(res.getWriter());
            survey.writeToJson(writer);
            writer.close();
//            txn.begin();
//            em.persist(survey);
//            txn.commit();
        } catch (IOException e) {
            res.sendError(400, e.getMessage());
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
            em.close();
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        EntityManager em = EMF.get().createEntityManager();
        JsonWriter writer = new JsonWriter(res.getWriter());    //Output gets written to a JSON writer
        try {
            String pathinfo = req.getPathInfo();
            if (pathinfo == null) {
            	
        		GaeUser user = null;
        		Subject currentUser = SecurityUtils.getSubject();
                if ( currentUser.isAuthenticated() ) {
	            	LOG.info("Username " + currentUser.getPrincipal());
	            	GaeUserDAO dao = new GaeUserDAO();
	            	user = dao.findUser(currentUser.getPrincipal().toString());
                }
                // List all survey names & ids
                TypedQuery<Survey> surveyquery = em.createQuery(
    					"SELECT s FROM Survey s WHERE s.creator=:p1",
    					Survey.class);

    			surveyquery.setParameter("p1", currentUser.getPrincipal().toString());
    			List<Survey> surveys = surveyquery.getResultList();
    			

                writer.beginArray();
                for (Survey item : surveys) {
                    item.writeToJsonShort(writer);
                }
                writer.endArray();
            } else {
                String id = pathinfo.substring(1);
                Survey survey = em.find(Survey.class, KeyFactory.stringToKey(id));
                survey.writeToJson(writer);
            }
            writer.close();
        } catch (IOException e) {
            res.sendError(400, e.getMessage());
        } finally {
            em.close();
        }
    }

}
