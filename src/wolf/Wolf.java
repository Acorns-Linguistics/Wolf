/*
 *   class Main.java
 *
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2010, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package wolf;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.acorns.audio.SoundDefaults;
import org.wolf.application.DictionaryDisplayPanel;
import org.wolf.application.DictionaryPanels;
import org.wolf.data.DictionaryData;
import org.wolf.lib.Icons;
import org.wolf.system.Environment;

/** Wolf dictionary application */
public class Wolf extends JFrame implements WindowListener
{  
	private static final long serialVersionUID = 1L;

	private static final Dimension APP_SIZE = new Dimension(1200, 800);
   
    private static DictionaryPanels rootPanel;
    public Environment environment;

   public Wolf() throws Throwable
   {  
	  super("[W]ord [O]riented [L]inguistics [F]ramework");
	  
      SoundDefaults.setSandboxKey("org.acorns.wolf");
      String libraryFolder = "Wolf_lib";
      String userPath = System.getProperty("user.dir") + File.separator + libraryFolder;
 
      // Tell Java where the JavaFX media files reside
      String addition = System.getProperty("java.library.path") 
		  + File.pathSeparator 
		  + userPath;
	  
	  System.setProperty("java.library.path", addition);
	  
      // Read settings file or create it.
      environment = new Environment(this);
      
	  if (SoundDefaults.isSandboxed())
	  {
		  String libName = "SecurityScopedBookmarkLibrary";
		  System.loadLibrary(libName);
		  
		  boolean resetPaths = SoundDefaults.setBookmarkFolder();
	      if (resetPaths)
	      {
	    	  String data = SoundDefaults.getDataFolder();
	    	  Environment.setPaths(data);
	      }
	  }
	  
      fileAssociations();
      Image imageIcon = Icons.getImageIcon("wolf.png", 30).getImage();
      setIconImage(imageIcon);

      // Attach the window listener.
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      addWindowListener(this);

     // Create and add the sound panels.
     rootPanel = new DictionaryPanels(environment);
     getContentPane().add(rootPanel);
     new JavaAwtDesktop(environment, rootPanel);

     // Start the application.
     setSize(APP_SIZE);
     setVisible(true);
     setLocationRelativeTo(null);
   }
   
   public static void main(String[] args)
   {
	   System.setProperty("apple.laf.useScreenMenuBar", "true");

	   try
	   {
  	       JFrame root = new Wolf();
  	       
            if (args.length!=0 && args[0]!=null && !args[0].endsWith(".adct"))
	        {
	           JOptionPane.showMessageDialog(root, "Wolf: " + args[0] + " is an Illegal file type");
	           System.exit(1);
	        }
            
            if (args.length>0)
            {
            	File file = new File(args[0]);
            	DictionaryDisplayPanel display = rootPanel.getDisplayPanel();
            	if (display.isMedia(file))
            	{
            		display.mediaDropped(file);
            	}
            }
	   }
	   catch (Throwable t)
	   {
       		JOptionPane.showMessageDialog( null,
   			"Main: " + t.toString()); 
	   }
   }

   /** Check file associations */
   private void fileAssociations() throws Throwable
   {   
	   String os = System.getProperty("os.name");
       if (os.toLowerCase().indexOf("windows")>=0)
       {   String separator = System.getProperty("file.separator");
           String path = System.getProperty("user.dir");
           String file = path + separator + "wolf.exe";
           File   exe  = new File(file);
           if (exe.exists())
           {   try
               {  
        	      String command = "cmd /c assoc .adct=wolf";
                  Process p = Runtime.getRuntime().exec(command);
                  p.waitFor();

                  command = "cmd /c ftype wolf=\"" + file + "\" \"%1\"";
                  p = Runtime.getRuntime().exec(command);
                  p.waitFor();

               }  catch (Exception e) {}
           }   // End of if executable exists
       }       // End of if window operating system
   }           // End of file associations

   /** Listen for the closing of the frame window.	*/
   @Override public void windowClosing(WindowEvent event)
   {   DictionaryData data = rootPanel.getDictionaryData();
       if (data.checkDirty())
       {  environment.shutdown();
          Window  window = event.getWindow();
          window.dispose();
          System.exit(0);
       }
   }

   //--------------------------------------------------------------
   // Unused WindowListener methods.
   //--------------------------------------------------------------
   @Override public void windowDeactivated( WindowEvent event ) {}
   @Override public void windowActivated(   WindowEvent event ) {}
   @Override public void windowDeiconified( WindowEvent event ) {}
   @Override public void windowIconified(   WindowEvent event ) {}
   @Override public void windowClosed(      WindowEvent event ) {}
   @Override public void windowOpened(      WindowEvent event ) {}
   
 }  // End of Main class
