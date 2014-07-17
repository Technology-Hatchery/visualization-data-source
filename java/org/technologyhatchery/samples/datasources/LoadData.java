/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.technologyhatchery.samples.datasources;

import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonReader;
//import org.apache.commons.io.IOUtils;
import org.datanucleus.api.jpa.JPAEntityManager;
//import org.datanucleus.samples.jpa.tutorial.*;
import org.datanucleus.util.NucleusLogger;
//import org.datanucleus.jpa.EntityManagerFactoryImpl;
//import org.datanucleus.jpa.EntityManagerImpl;

import com.mobinsight.server.*;

import javax.persistence.*;
import java.util.*;
import java.io.*;

/**
 * Controlling application for the DataNucleus Tutorial using JPA.
 * Uses the "persistence-unit" called "Tutorial".
 */
public class LoadData
{
    private static String persistenceUnit = "MobinsightAlfred";

    //Does nothing constructor
    public LoadData() {

    }

    //Constructor that accepts a PrintWriter and assigns it to the internal PrintWriter
    public LoadData(PrintWriter printWriter) {
        out = printWriter;
    }

    static PrintWriter out = null;

    public static void main(String args[])
    {
        //Calls the internal functions when executing from the command lind
        String filePath = "C:\\Users\\Alfred\\Dropbox\\Technology Hatchery Inc\\technical\\Git\\visualization-data-source_testing\\json\\mobinsight\\";
        addDataFromJson(filePath + "Survey.json");
        List<Survey> results = retrieveSurvey("Registration branch");
        listSurveys(results, filePath + "listSurveys.json");
        listQuestions(results.get(0), filePath + "listQuestions.json");
    }

    public static void addDataFromJson(String filePath) {
        // Create an EntityManagerFactory for this "persistence-unit"
        // See the file "META-INF/persistence.xml"
        System.out.println("Create Entity Manager Factory<br />");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
        //LoadData ex = new LoadData();

        System.out.println("Starting to execute the statements<br />");
        System.out.println("=============================");
        System.out.println("CreateAdd<br />");


        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            //tx.begin();

            System.out.println("New Survey<br />");
            Survey survey = new Survey();
            System.out.println("New Question<br />");
            Question question = new Question();
            System.out.println("New Answer<br />");
            Answer answer = new Answer();

            //Read survey from a file
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            JsonReader jsonReader = new JsonReader(br);

            //File f = new File(filePath);
            //InputStream is = new FileInputStream(f);

            //System.out.println(convertStreamToString(is));
            System.out.println("Begin Reading in from File<br />");
            survey.readFromJson(em, jsonReader);
            System.out.println("End Reading in from File<br />");
            System.out.println("Replaying all the Questions:<br />");
            System.out.println(survey.getQuestions() + "<br />");

            System.out.println("<br />Here are the questions I read in from file 'Survey.json':<br />");
            for (Question q : survey.getQuestions()) {
                //This writes to the console
                System.out.println(q.getText() + "<br />");
            }

            System.out.println("Persisted has begun<br />");
            tx.begin();
            em.persist(survey);
            tx.commit();
            System.out.println("Persisted has completed<br />");

            //Product product = new Product("Sony Discman", "A standard discman from Sony", 200.00);
            //inv.getProducts().add(product);
            //Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien",
            //        "12345678", "MyBooks Factory");
            //inv.getProducts().add(book);

            //em.persist(survey);
            //em.persist(question);
            //em.persist(answer);
        } catch (Exception e) {
            NucleusLogger.GENERAL.error(">> Exception persisting data", e);
            System.err.println("Error persisting data : " + e.getMessage());
            return;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        emf.getCache().evictAll();
        System.out.println("");
    }

    /*public static void removeData(EntityManagerFactory emf) {

        // Clean out the database
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        //String output = "";

        try
        {
            tx.begin();

            System.out.println("Deleting all products from persistence");
            Inventory inv = (Inventory)em.find(Inventory.class, "My Inventory");

            System.out.println("Clearing out Inventory");
            inv.getProducts().clear();
            em.flush();

            System.out.println("Deleting Inventory");
            em.remove(inv);

            System.out.println("Deleting all products from persistence");
            Query q = em.createQuery("SELECT FROM Product p");
            List<Product> products = q.getResultList();
            int numDeleted = 0;
            for (Product prod : products)
            {
                em.remove(prod);
                numDeleted++;
            }
            System.out.println("Deleted " + numDeleted + " products");

            tx.commit();
        }
        catch (Exception e)
        {
            NucleusLogger.GENERAL.error(">> Exception in bulk delete of data", e);
            System.err.println("Error in bulk delete of data : " + e.getMessage());
            return;
        }
        finally
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            em.close();
        }

        System.out.println("");
        System.out.println("End of Tutorial");
        emf.close();
    }*/




    public static void listQuestions(Survey survey, String filePath) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
        EntityManager em = emf.createEntityManager();

        try {
            //Write response to a file
            PrintWriter fileWriter = new PrintWriter(filePath, "UTF-8");
            JsonWriter jsonWriter = new JsonWriter(fileWriter);    //Output gets written to a JSON writer

            //Write output to file
            jsonWriter.beginObject();
            jsonWriter.name("questions");
            jsonWriter.beginArray();
            for (Question item : survey.getQuestions()) {
                item.writeToJson(jsonWriter);
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
            jsonWriter.close();

        } catch (IOException e) {
            System.out.println("IO Exception.");
        } finally {
            em.close();
        }
    }

    public static List<Survey> retrieveSurvey(String name) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
        EntityManager em = emf.createEntityManager();
        TypedQuery<Survey> surveyquery;
        List<Survey> results;

        try {
            // List all survey names & ids
            surveyquery = em.createQuery(
                    "SELECT s FROM Survey s WHERE s.mName=:p1",
                    Survey.class);
            surveyquery.setParameter("p1", name);
            results = surveyquery.getResultList();

            /*
            //TODO-Alfred EntityGraph
            EntityGraph allGraph = em.getEntityGraph("allProps");
            Map hints = new HashMap();
            hints.put("javax.persistence.loadgraph", allGraph);
            inv = em.find(Inventory.class, "My Inventory", hints);*/

            return results;

        } catch (Exception e) {
            System.out.println("IO Exception.");
            return null;
        } finally {
            em.close();
        }
    }


    public static void listSurveys(List<Survey> surveys, String filePath) {

        try {
        //Write response to a file
        PrintWriter fileWriter = new PrintWriter(filePath, "UTF-8");
        JsonWriter jsonWriter = new JsonWriter(fileWriter);    //Output gets written to a JSON writer


        //Write output to file
        jsonWriter.beginObject();
        jsonWriter.name("surveys");
        jsonWriter.beginArray();
        for (Survey item : surveys) {
            item.writeToJsonShort(jsonWriter);
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
        jsonWriter.close();
        } catch (IOException e) {
            System.out.println("IO Exception.");
        }

    }

    /*public static void performQuery(EntityManagerFactory emf) {

        // Perform some query operations
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try
        {
            tx.begin();
            System.out.println("Executing Query for Products with price below 150.00");
            Query q = em.createQuery("SELECT p FROM Product p WHERE p.price < 150.00 ORDER BY p.price");
            List results = q.getResultList();
            Iterator iter = results.iterator();
            while (iter.hasNext())
            {
                Object obj = iter.next();
                System.out.println(">  " + obj);

                // Give an example of an update
                if (obj instanceof Book)
                {
                    Book b = (Book)obj;
                    b.setDescription(b.getDescription() + " REDUCED");
                }
            }

            System.out.println("Committing query");
            tx.commit();
        }
        catch (Exception e)
        {
            NucleusLogger.GENERAL.error(">> Exception querying data", e);
            System.err.println("Error querying data : " + e.getMessage());
            return;
        }
        finally
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            em.close();
        }
        System.out.println("");
    }*/

    /*public static void retrieveItems(EntityManagerFactory emf) {

        // Perform a retrieve of the Inventory and detach it (by closing the EM)
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        Survey survey = null;
        try
        {
            tx.begin();

            // Do a find() of the Survey
            // Set the EntityGraph as something pulling in all Products
            // Note : you could achieve the same by either
            // 1). access the Inventory.products field before commit
            // 2). set fetch=EAGER for the Inventory.products field
            System.out.println("Executing find() on Surveys");
            //System.out.println(em.getClass());
            // List all survey names & ids
            TypedQuery<Survey> surveyquery = em.createQuery(
                    "SELECT s FROM Survey s WHERE s.creator=:p1",
                    Survey.class);
            surveyquery.setParameter("p1", "Alfred");
            List<Survey> surveys = surveyquery.getResultList();
            //EntityGraph allGraph = ((JPAEntityManager)em).getEntityGraph("allProps");
            //Map hints = new HashMap();
            //hints.put("javax.persistence.loadgraph", allGraph);
            //inv = em.find(Inventory.class, "My Inventory", hints);
            //System.out.println("Retrieved Inventory as " + inv);

            tx.commit();
        }
        catch (Exception e)
        {
            NucleusLogger.GENERAL.error(">> Exception performing find() on data", e);
            System.err.println("Error performing find() on data : " + e.getMessage());
            return;
        }
        finally
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            em.close(); // This will detach all current managed objects
        }
        for (Product prod : inv.getProducts())
        {
            System.out.println(">> After Detach : Inventory has a product=" + prod);
        }
        System.out.println("");
    }*/

    public static void createAdd(EntityManagerFactory emf, PrintWriter out) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try
        {
            //tx.begin();

            Survey survey = new Survey();
            Question question = new Question();
            Answer answer = new Answer();

            //Read survey from a file
            String filePath = "C:\\Users\\Alfred\\Dropbox\\Technology Hatchery Inc\\technical\\Git\\visualization-data-source_testing\\webapps\\HelloServlet\\WEB-INF\\classes\\Survey.json";
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            JsonReader jsonReader = new JsonReader(br);

            //File f = new File(filePath);
            //InputStream is = new FileInputStream(f);

            //System.out.println(convertStreamToString(is));
            survey.readFromJson(em, jsonReader);
            System.out.println(survey.getQuestions() + "<br />");

            out.println("<br />Here are the questions I read in from file 'Survey.json':<br />");
            for ( Question q : survey.getQuestions() ) {
                //This writes to the webpage
                out.println(q.getText() + "<br />");
            }

            out.println("Persisted has begun<br />");
            tx.begin();
            em.persist(survey);
            tx.commit();
            out.println("Persisted has completed<br />");

            //Product product = new Product("Sony Discman", "A standard discman from Sony", 200.00);
            //inv.getProducts().add(product);
            //Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien",
            //        "12345678", "MyBooks Factory");
            //inv.getProducts().add(book);

            //em.persist(survey);
            //em.persist(question);
            //em.persist(answer);
            System.out.println("Items are pending persist");

            tx.commit();
            System.out.println("Items have been persisted");
        }
        catch (Exception e)
        {
            NucleusLogger.GENERAL.error(">> Exception persisting data", e);
            System.err.println("Error persisting data : " + e.getMessage());
            return;
        }
        finally
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            em.close();
        }
        emf.getCache().evictAll();
        System.out.println("");
    }

    private static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}