// To save as "<CATALINA_HOME>\webapps\helloservlet\WEB-INF\src\mypkg\HelloWorldExample.java"
package org.technologyhatchery.servlets;

//import com.google.gson.stream.JsonWriter;
import org.technologyhatchery.samples.datasources.LoadData;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.ResourceBundle;

public class SimpleServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // Set the response message's MIME type.
        response.setContentType("text/html;charset=UTF-8");
        // Allocate an output writer to write the response message into the network socket.
        PrintWriter out = response.getWriter();
        //JsonWriter writer = new JsonWriter(out);

        // Use a ResourceBundle for localized string in "LocalStrings_xx.properties" for i18n.
        // The request.getLocale() sets the locale based on the "Accept-Language" request header.
        ResourceBundle rb = ResourceBundle.getBundle("LocalStrings", request.getLocale());

        // Write the response message, in an HTML document.
        try {
            out.println("<!DOCTYPE html>");  // HTML 5
            out.println("<html><head>");
            out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
            String title = rb.getString("helloworld.title");
            out.println("<title>" + title + "</title></head>");
            out.println("<body>");

            Date date = new Date();
            out.println(String.valueOf(date));
            out.println("<br />");

            // ------------- Data Entries (Start) ---------------------
            // Create an EntityManagerFactory for this "persistence-unit"
            // See the file "META-INF/persistence.xml"
            out.println("Create Entity Manager Factory<br />");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("MobinsightAlfred");
            LoadData ex = new LoadData();

            out.println("Starting to execute the statements<br />");
            out.println("=============================");
            out.println("CreateAdd<br />");
            ex.createAdd(emf, out);
            //out.println("retrieveInventory<br />");
            //ex.retrieveItems(emf);
            //System.out.println("performQuery<br />");
            //out.println("performQuery<br />");
            //ex.performQuery(emf);
            //System.out.println("removeData<br />");
            //out.println("removeData<br />");
            //ex.removeData(emf);
            // ------------- Data Entries (End) ---------------------




            out.println("<h1>" + title + "</h1>");  // Prints "Hello, world!"
            // Set a hyperlink image to refresh this page
            out.println("<a href='" + request.getRequestURI() + "'><img src='images/return.gif'></a>");
            out.println("</body></html>");
        } finally {
            out.close();  // Always close the output writer
        }
    }
}