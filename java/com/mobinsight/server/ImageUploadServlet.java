package com.mobinsight.server;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.stream.JsonWriter;
import com.mobinsight.user.GaeUser;
import com.mobinsight.user.GaeUserDAO;

@SuppressWarnings("serial")
@Singleton
public class ImageUploadServlet extends BaseServlet {
	
	Logger LOG = Logger.getLogger(ImageUploadServlet.class.getSimpleName());
		
	@Inject
    protected ImageUploadServlet(Provider<GaeUserDAO> daoProvider) {
		super(daoProvider);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String imageId = req.getParameter("imageId");
		GaeUser user = null;
		EntityManager em = EMF.get().createEntityManager();
//        if (questionId == null) {
//            res.sendError(400);
//            return;
//        }

		if(imageId == null) {
		Subject currentUser = SecurityUtils.getSubject();
        if ( currentUser.isAuthenticated() ) {
        	LOG.warning("Username " + currentUser.getPrincipal());
        	
        	GaeUserDAO dao = new GaeUserDAO();
        	user = dao.findUser(currentUser.getPrincipal().toString());
        	//issueJson(res, 200, "Message", "User " + currentUser.getPrincipal());

        }
            if (null != user) {
		        JsonWriter writer = new JsonWriter(res.getWriter());
		        // List all survey names & ids
		        List<Image> images = em.createQuery(
		                "SELECT images FROM Image images where userName='" + user.getName() + "'", Image.class).getResultList();
		        writer.beginArray();
		        for (Image item : images) {
		            item.writeToJsonShort(writer);
		        }
		        writer.endArray();
		        writer.close();
		    }
	        
		} else {
        
        
	        try {
	            Image image = em.find(Image.class, KeyFactory.stringToKey(imageId));
	            if (image == null) {
	                throw new IllegalArgumentException();
	            }
	            if(image.getImage() != null){
	            	res.setContentType("image/jpeg");  // Hardcoded as this is a mobile product and jpeg is most efficient.
	            	res.getOutputStream().write(((Blob) image.getImage()).getBytes());
	            }
	        } catch (IllegalArgumentException e) {
	            res.sendError(404);
	        } finally {
	            em.close();
	        }
		}
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String questionId = req.getParameter("question");
        GaeUser user = null;
		Subject currentUser = SecurityUtils.getSubject();
        if ( currentUser.isAuthenticated() ) {
        	LOG.severe("Username " + currentUser.getPrincipal());
        	
        	GaeUserDAO dao = new GaeUserDAO();
        	user = dao.findUser(currentUser.getPrincipal().toString());
        	issueJson(res, 200, "Message", "User " + currentUser.getPrincipal(), "second", "second value");

        } else {
        	LOG.severe("not authenticated");
        }

        EntityManager em = EMF.get().createEntityManager();
        try {
        	GaeUserDAO dao = new GaeUserDAO();
        	user = dao.findUser(currentUser.getPrincipal().toString());
        	LOG.severe("User " + user.getName());
            ServletFileUpload upload = new ServletFileUpload();
            FileItemStream fis = upload.getItemIterator(req).next();
            String contentType = fis.getContentType();
//            if (!contentType.equals("image/jpeg")) {
//                res.sendError(400, "Please upload a JPEG image.");
//                return;
//            }
            Blob imageBlob = new Blob(IOUtils.toByteArray(fis.openStream()));
            LOG.severe("Setting Image");
            Image image = new Image();
            
            image.setUserImage(imageBlob, user);
            em.persist(image);
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
