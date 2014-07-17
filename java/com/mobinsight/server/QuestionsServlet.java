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
import com.google.gson.stream.JsonWriter;


@SuppressWarnings("serial")
public class QuestionsServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(QuestionsServlet.class.getName());
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String userIdStr = req.getParameter("userId");
        String countStr = req.getParameter("count");
        int count = -1;
        Key userId = null;
        try {
            if (countStr != null && userIdStr != null) {
                count = Integer.parseInt(countStr);
                userId = KeyFactory.stringToKey(userIdStr);
            }
        } catch (NumberFormatException e) {
        } catch (IllegalArgumentException e) {
        } finally {
            if (count == -1 || userId == null) {
                res.sendError(400);
                return;
            }
        }

        // Returns the next set of questions for the given user. The last survey and question
        // returned are stored in the User object so we'll know where to start from when this
        // is invoked again in future.
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction txn = em.getTransaction();
        JsonWriter writer = new JsonWriter(res.getWriter());
        try {
            User user = em.find(User.class, userId);

            // Find the survey from which we should start.
            List<Key> allSurveyIds = Utils.getAllSurveysSortedByCreationTime();
            int surveyIndex = 0;
            if (user.getLastSurveyAnswered() != null) {
                Key lastSurveyAnswered = user.getLastSurveyAnswered();
                while (surveyIndex < allSurveyIds.size() && !lastSurveyAnswered.equals(allSurveyIds.get(surveyIndex))) {
                    ++surveyIndex;
                }
            }

            // Write as many questions from this survey as we can, then move to the next one.
            writer.beginArray();
            Key lastQuestionAnswered = user.getLastQuestionAnswered();
            log.info("lastQuestionAnswered: " + lastQuestionAnswered);
            while (surveyIndex < allSurveyIds.size() && count > 0) {
                Key surveyId = allSurveyIds.get(surveyIndex);
                Survey survey = em.find(Survey.class, surveyId);
                Survey.WriteToJsonQuestionsAfterReturn ret = survey.writeToJsonQuestionsAfter(
                        em, user, writer, count, lastQuestionAnswered);
                if (ret.questionsWritten > 0) {
                    count -= ret.questionsWritten;
                }
                lastQuestionAnswered = null;
                ++surveyIndex;
            }
            writer.endArray();
            writer.close();

            // Save the last survey & question sent so we start from there next time.
            txn.begin();
            em.persist(user);
            txn.commit();
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
