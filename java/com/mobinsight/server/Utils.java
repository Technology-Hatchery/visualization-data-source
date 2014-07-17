package com.mobinsight.server;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/**
 * General collection of utilities.
 */
public class Utils {

    /**
     * Returns the host url where this application is running.
     */
    public static String getHostUrl() {
        String hostUrl; 
        String environment = System.getProperty("com.google.appengine.runtime.environment");
        if (environment.equals("Production")) {
            String applicationId = System.getProperty("com.google.appengine.application.id");
            String version = System.getProperty("com.google.appengine.application.version");
            hostUrl = "http://" + version + "." + applicationId + ".appspot.com";
        } else {
            hostUrl = "http://192.168.1.101:8080";
        }
        return hostUrl;
    }

    /**
     * Returns the list of all survey keys sorted by creation time.
     */
    public static List<Key> getAllSurveysSortedByCreationTime() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Survey")
                .addSort("mCreationTime", SortDirection.ASCENDING)
                .setKeysOnly();
        List<Entity> results = datastore.prepare(query)
                .asList(FetchOptions.Builder.withDefaults());
        ArrayList<Key> keys = new ArrayList<Key>();
        for (Entity entity : results) {
            keys.add(entity.getKey());
        }
        return keys;
    }

}
