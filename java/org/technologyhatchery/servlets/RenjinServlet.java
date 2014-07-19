package org.technologyhatchery.servlets;

import org.renjin.appengine.AppEngineContextFactory;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Vector;

import javax.script.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.CharBuffer;
import java.util.Map;
import java.util.Scanner;

/**
 * Simple servlet that simply calls out the R Script in
 *
 */
public class RenjinServlet extends HttpServlet {

    /**
     * Maintain one instance of Renjin for each request thread.
     */
    private static final ThreadLocal<ScriptEngine> ENGINE = new ThreadLocal<ScriptEngine>();

    private ServletContext servletContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.servletContext = config.getServletContext();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        /*
        // This is another way to create a Renjin script engine
        // create a script engine manager
        ScriptEngineManager manager = new ScriptEngineManager();
        // create a Renjin engine:
        ScriptEngine engine = manager.getEngineByName("Renjin");
        // check if the engine has loaded correctly:
        if(engine == null) {
            throw new RuntimeException("Renjin Script Engine not found on the classpath.");
        }*/

        //TODO-Alfred Figure out how this code is able to get a Renjin engine
        ScriptEngine engine = ENGINE.get();
        if(engine == null) {
            // create a new engine for this thread (this calls the Renjin library)
            engine = AppEngineContextFactory.createScriptEngine(servletContext);
            if(engine == null) {
                throw new ServletException("Could not create Renjin ScriptEngine");
            }

            // load the application script
            String scriptPath = servletContext.getRealPath("R/app.R");
            Reader reader = new FileReader(scriptPath);
            
            try {
                engine.eval(reader);
            } catch (ScriptException e) {
                handleException(resp, e);
                return;
            }

            //Read data in from json file
            scriptPath = servletContext.getRealPath("json/sampleData.json");
            reader = new FileReader(scriptPath);

            CharBuffer charBuffer = CharBuffer.allocate(1000);
            int numChars = reader.read(charBuffer);
            char[] charArray = new char[numChars];
            charBuffer.flip();
            charBuffer.get(charArray);  //populate charArray
            String json = new String(charArray);
            //json = "{ one : 2, two : 3, three : \"four\" }";
            engine.put("json", json);
            ENGINE.set(engine);
        }

        // Create an R list object to hold the query parameters
        ListVector.NamedBuilder queryParams = ListVector.newNamedBuilder();
        for(Object param : req.getParameterMap().entrySet()) {
            Map.Entry<String, String[]> paramEntry = (Map.Entry)param;
            queryParams.add(paramEntry.getKey(), new StringArrayVector(paramEntry.getValue()));
        }

        // Invoke the doGet() handler in app.R, passing the URI and list of query
        // parameters as arguments
        Vector result;
        Invocable invokableEngine = (Invocable)engine;
        try {
            result = (Vector) invokableEngine.invokeFunction("doGet", req.getRequestURI(), queryParams.build());
        } catch (Exception e) {
            handleException(resp, e);
            return;
        }
        
        // simply print the result
        PrintStream out = new PrintStream(resp.getOutputStream());
        for(int i=0;i!=result.length();++i) {
            out.println(result.getElementAsString(i));
        }
        out.close();
    }

    private void handleException(HttpServletResponse resp, Exception e) throws IOException, ServletException {
        PrintStream out = new PrintStream(resp.getOutputStream());
        e.printStackTrace(out);
        out.flush();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
