package wolf;



import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.acorns.audio.SoundDefaults;
import org.acorns.language.LanguageText;
import org.wolf.application.DictionaryDisplayPanel;
import org.wolf.application.DictionaryPanels;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.system.Environment;

public class JavaAwtDesktop 
{
	
	private static String os = System.getProperty("os.name").toLowerCase();

	DictionaryPanels rootPanels;
	ImageIcon icon;

    public JavaAwtDesktop(Environment environment, DictionaryPanels rootPanels) 
    {
    	this.rootPanels = rootPanels;
        Desktop desktop = Desktop.getDesktop();
    	JLabel label = RootDictionaryPanel.getLabel();

        if (os.indexOf("mac") >= 0) 
    	{
	        desktop.setAboutHandler(e -> {
	    		about();
	        });
	        
	        desktop.setPreferencesHandler(e -> {
	        	
	           if (!SoundDefaults.isSandboxed())
	           {
	        		Frame root = JOptionPane.getRootFrame();
	        		JOptionPane.showMessageDialog
	        			(root, LanguageText.getMessage("commonHelpSets",  99));
	        		return;
	           }
	        	
	  	      if (SoundDefaults.resetBookmarkFolder())
		      {
		  	      String data = SoundDefaults.getDataFolder();
		  	      Environment.setPaths(data);
		  	      
		  	      label.setText("File access permission granted");
		      }
	  	      
	        });
	        
	        desktop.setQuitHandler((e,r) -> {
	        	environment.shutdown();
	        	System.exit(0);
	        });
	        
	        desktop.setOpenFileHandler(e -> {
	            // Method to open an Acorns file (Called from Mac OS application listener).
	        	String path = "???";
	        	try
	            {  
	            	List<File> files = e.getFiles();
	            	for (File file: files)
	            	{
	            	   path = file.getCanonicalPath();
	                   openFile(path); 
	            	}
	            }
	            catch (Exception exception) 
	            { 
	            	JOptionPane.showMessageDialog( rootPanels,
	            			"Main: " + path + ":" + exception.toString()); 
	            }
	        });
	    }
    }		// End of constructor
    
    public void about()
    {
        // Create icon for the dialog window.
        URL url = getClass().getResource("/resources/wolf.png");
        if (url!=null)
        {
           Image image  = Toolkit.getDefaultToolkit().getImage(url);
 		   Image newImage = image.getScaledInstance(30, 30, Image.SCALE_REPLICATE);
 		   icon = new ImageIcon(newImage);
        }

        // Create label to hold the text.
        String[] text =
        {     "Version 2.2",
              "",
              "Copyright \u00a9 2019, Dan Harvey, all rights reserved",
              "Contact: harveyd@sou.edu, http://cs.sou.edu/cs/~harveyd",
              "","",
              "This product is freeware, but its intention is to support tribal language revitalization efforts.",
              "We hope that the software is useful, but provide no guarantees of its suitability for any purpose.",
              "It is not to be sold for profit or be reverse engineered. Use of this software implies agreement",
              "to abide by these terms. Please contact the author with questions or comments.",
              "",
         };

        JLabel[] labels = new JLabel[text.length];
        for (int i=0; i<text.length; i++)	{labels[i] = new JLabel(text[i]);}

        String title = "About WOLF ([W]ord [O]riented [L]inguistic [F]ramework)";
        JOptionPane.showMessageDialog
                (Environment.getRootFrame(), labels, title, JOptionPane.INFORMATION_MESSAGE, icon);
    }

    
    /** Methods to handle MacOs interfaces
    *
    * Method to open an Acorns file (Called from Mac OS application listener)
    */
   public void openFile(String path)
   {   try
       {   File file = new File(path);
           DictionaryDisplayPanel display = rootPanels.getDisplayPanel();
           if (!display.isMedia(file))
               throw new IOException("Illegal file type");
           display.mediaDropped(file);
       }
       catch (Exception e)
       { 
    	   JOptionPane.showMessageDialog(Environment.getRootFrame(), e.toString()); 
       }
   }
   
}

