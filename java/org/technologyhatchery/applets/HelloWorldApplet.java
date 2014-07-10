package org.technologyhatchery.applets; /**
 * Created by Alfred on 7/8/2014.
 */
import javax.swing.*;
import java.awt.*;

//TODO I can't get this to display in the web browser

public class HelloWorldApplet extends JApplet {
    public static void main(String[] args) {

        // create and set up the applet
        HelloWorldApplet applet = new HelloWorldApplet();
        applet.setPreferredSize(new Dimension(500, 500));
        applet.init();

        // create a frame to host the applet, which is just another type of Swing Component
        JFrame mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add the applet to the frame and show it
        mainFrame.getContentPane().add(applet);
        mainFrame.pack();
        mainFrame.setVisible(true);

        // start the applet
        applet.start();
    }

    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JLabel label = new JLabel("Hello World!!");
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    add(label);
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
    }
}