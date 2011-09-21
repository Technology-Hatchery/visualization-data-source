To compile everything and install all jars in the maven repository on your local machine do this
at the top level directory

   mvn install

Then in the examples directory, you can run a stand-alone Jetty web server using mvn to start it:

   cd examples ; mvn jetty:run

Go to http://localhost:8080/all_examples.html to see the examples in action.

There is also a mySql based example.  The file examples/src/main/webapp/WEB-INF/jetty-env.xml defines
the access parameters for the mySql connection including host, port, database, user and password.
The parallel web.xml file then defines how that connection pool is injected into instances of the
com.mapr.metrics.DataServlet class.  When that class is invoked from any URL of the form
/metrics/$table where $table represents a mySql table name, you will get back data from that table
according to the query given when making the request.  The all_examples.html page has a link
to a mySql demo page that you can use if you have a valid mySql server configured.  The default values
in the jetty-env.xml are

    url      jdbc:mysql://10.250.1.58:3306/
    user     root
    password foobar

You can change these values to anything you like, but you probably will need to restart jetty after
doing  so.  All changes to the HTML pages or csv data can be made with the server live.

Note that the mysql example is not fully functional just now.  To make it so, I (or you!) need to 
add some directions for creating a table and fix up the query to make things nicer and give a better
demo.

For more information on the examples refer to: http://code.google.com/apis/visualization/documentation/dev/dsl_get_started.html
