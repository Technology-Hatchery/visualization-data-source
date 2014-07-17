package com.mobinsight.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonReader;

@SuppressWarnings("serial")
public class AnswersServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(Survey.class.getName());
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String userIdStr = req.getParameter("userId");
        String latStr = req.getParameter("lat");
        String lngStr = req.getParameter("lng");
        Key userId = null;
        double lat = 0;
        double lng = 0;
        try {
            if (userIdStr != null) {
                userId = KeyFactory.stringToKey(userIdStr);
            }
            if (latStr != null) {
                lat = Float.parseFloat(latStr);
            }
            if (lngStr != null) {
                lng = Float.parseFloat(lngStr);
            }
        } catch (IllegalArgumentException e) {
        } finally {
            if (userId == null) {
                res.sendError(400);
                return;
            }
        }

        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction txn = em.getTransaction();
        JsonReader reader = new JsonReader(req.getReader());
        try {
            User user = em.find(User.class, userId);

            // Find the survey from which we should start.
            List<Key> allSurveyIds = Utils.getAllSurveysSortedByCreationTime();
            int surveyIndex = 0;
            if (user != null) {
            	log.severe(" User not null");
            } else {
            	log.severe(" User is null!");
            }
            Key lastSurveyAnswered = user.getLastSurveyAnswered();
        	log.severe(" lastSurveyAnswered " + lastSurveyAnswered);
            if (lastSurveyAnswered != null) {
                while (surveyIndex < allSurveyIds.size() && !lastSurveyAnswered.equals(allSurveyIds.get(surveyIndex))) {
                    ++surveyIndex;
                }
            }
            Key lastQuestionAnswered = user.getLastQuestionAnswered();
            reader.beginArray();
            // For each survey, save answers for all questions in that survey.. and do it until we reach EOF.
            while (surveyIndex < allSurveyIds.size() && reader.hasNext()) {
                Key surveyId = allSurveyIds.get(surveyIndex);
                Survey survey = em.find(Survey.class, surveyId);
                Key newLastQuestionAnswered = survey.addAnswersFromJson(reader, lastQuestionAnswered, em, user, lat, lng);
                if (newLastQuestionAnswered != null) {
                    user.setLastSurveyAnswered(surveyId);
                    user.setLastQuestionAnswered(newLastQuestionAnswered);
                }
                lastQuestionAnswered = null;
                ++surveyIndex;
            }
            reader.endArray();
            res.setStatus(200);
        } catch (IOException e) {
            res.sendError(400, e.getMessage());
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
            em.close();
        }
    }

}
