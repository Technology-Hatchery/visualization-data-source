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

import java.util.*;

import javax.persistence.*;

import org.datanucleus.api.jpa.JPAEntityManager;
import org.datanucleus.util.NucleusLogger;
import org.technologyhatchery.items.*;

/**
 * Controlling application for the DataNucleus Tutorial using JPA.
 * Uses the "persistence-unit" called "Tutorial".
 */
public class Example
{
    public Example() {
        //Does nothing constructor
    }

    public static void main(String args[])
    {
        System.out.println("DataNucleus Tutorial with JPA");
        System.out.println("=============================");

        /**
         * Create the objects: EntityManager and EntityTransaction
         */
        // Create an EntityManagerFactory for this "persistence-unit"
        // See the file "META-INF/persistence.xml"
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CloudSQL");

        // Persistence of a Product and a Book.
        //EntityManager em = emf.createEntityManager();
        //EntityTransaction tx = em.getTransaction();

        createAdd(emf);
        retrieveInventory(emf);
        performQuery(emf);
        removeData(emf);
    }

    public static void removeData(EntityManagerFactory emf) {

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
    }

    public static void performQuery(EntityManagerFactory emf) {

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
    }

    public static void retrieveInventory(EntityManagerFactory emf) {

        // Perform a retrieve of the Inventory and detach it (by closing the EM)
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        Inventory inv = null;
        try
        {
            tx.begin();

            // Do a find() of the Inventory
            // Set the EntityGraph as something pulling in all Products
            // Note : you could achieve the same by either
            // 1). access the Inventory.products field before commit
            // 2). set fetch=EAGER for the Inventory.products field
            System.out.println("Executing find() on Inventory");
            //System.out.println(em.getClass());
            EntityGraph allGraph = ((JPAEntityManager)em).getEntityGraph("allProps");
            Map hints = new HashMap();
            hints.put("javax.persistence.loadgraph", allGraph);
            inv = em.find(Inventory.class, "My Inventory", hints);
            System.out.println("Retrieved Inventory as " + inv);

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
    }

    public static void createAdd(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try
        {
            tx.begin();

            Inventory inv = new Inventory("My Inventory");
            Product product = new Product("Sony Discman", "A standard discman from Sony", 200.00);
            inv.getProducts().add(product);
            Book book = new Book("Lord of the Rings by Tolkien", "The classic story", 49.99, "JRR Tolkien",
                    "12345678", "MyBooks Factory");
            inv.getProducts().add(book);

            em.persist(inv);
            System.out.println("Product and Book are pending persisted");

            tx.commit();
            System.out.println("Product and Book have been persisted");
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
}