package com.mobinsight.server;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@SuppressWarnings("serial")
public class UserServlet extends HttpServlet {

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Creates a new user.
        EntityManager em = EMF.get().createEntityManager();
        EntityTransaction txn = em.getTransaction();
        try {
            User user = User.readFromJson(new JsonReader(req.getReader()));
            User oldUser = user.getUserWithSameOSAndDeviceId(em);
            if (oldUser != null) {
                oldUser.copyFrom(user);
                user = oldUser;
            }

            txn.begin();
            em.persist(user);
            txn.commit();

            JsonWriter writer = new JsonWriter(res.getWriter());
            writer.beginObject();
            writer.name("id").value(user.getIdString());
            writer.endObject();
            writer.close();
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
        JsonWriter writer = new JsonWriter(res.getWriter());
        try {
            // List all user emails & ids
            List<User> users = em.createQuery(
                    "SELECT u FROM User u", User.class).getResultList();
            writer.beginArray();
            for (User user : users) {
                writer.beginObject()
                      .name("id").value(user.getIdString())
                      .name("email").value(user.getEmail())
                      .endObject();
            }
            writer.endArray();
            writer.close();
        } catch (IOException e) {
            res.sendError(400, e.getMessage());
        } finally {
            em.close();
        }
    }

}
