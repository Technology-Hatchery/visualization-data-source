package com.mobinsight.server;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.KeyFactory;

@SuppressWarnings("serial")
public class ImageServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String questionId = req.getParameter("question");
        if (questionId == null) {
            res.sendError(400);
            return;
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            Question question = em.find(Question.class, KeyFactory.stringToKey(questionId));
            if (question == null) {
                throw new IllegalArgumentException();
            }
            if(question.getImage() != null){
            	res.setContentType("image/jpeg");  // Hardcoded as this is a mobile product and jpeg is most efficient.
            	res.getOutputStream().write(question.getImage().getBytes());
            }
        } catch (IllegalArgumentException e) {
            res.sendError(404);
        } finally {
            em.close();
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String questionId = req.getParameter("question");
        if (questionId == null) {
            res.sendError(400);
            return;
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            Question question = em.find(Question.class, KeyFactory.stringToKey(questionId));
            ServletFileUpload upload = new ServletFileUpload();
            FileItemStream fis = upload.getItemIterator(req).next();
            String contentType = fis.getContentType();
            if (!contentType.equals("image/jpeg") || !contentType.equals("image/png") || !contentType.equals("image/jpg")) {
                res.sendError(400, "Please upload a JPEG image.");
                return;
            }
            Blob imageBlob = new Blob(IOUtils.toByteArray(fis.openStream()));
            question.setImage(imageBlob);
            em.persist(question);
            res.getWriter().write("Uploaded!");
        } catch (IllegalArgumentException e) {
            res.sendError(400);
        } catch (FileUploadException e) {
            res.sendError(400);
        } finally {
            em.close();
        }
    }

}
