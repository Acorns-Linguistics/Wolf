/*
 * ChooseFileDialog.java
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

package org.wolf.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.wolf.application.RootDictionaryPanel;
import org.acorns.audio.SoundDefaults;
import org.acorns.language.LanguageText;
import org.acorns.lib.DialogFilter;
import org.wolf.system.Environment;


/**  Choose a file for export, import, save, or load */
 public class ChooseFileDialog implements ListSelectionListener
{
	 private static String fileName;
	 
     /** Loading a dictionary file */
     public final static int LOAD = 0;
     /** Saving a dictionary file */
     public final static int SAVE = 1;
     /** Importing a dictionary file */
     public final static int IMPORT = 2;
     /** Exporting a dictionary file */
     public final static int EXPORT = 3;
     /** Create a mobile application */
     public final static int MOBILE = 4;
     /** Create a web page */
     public final static int WEB = 5;

     private final static String[] titles
             = {  "load"
    	        , "save"
    	        , "import"
    	        , "export" 
    	        , "mobile app"
    	        , "web page"
    	       };
     private final static String[] filters
             = {  "dictionary files (adct)"
    	        , "dictionary files (adct)"
                , "import files (xml, db)"
                , "export files (xml)" 
                , "mobile application files (acorns)"
                , "web page files (html)"
               };
     private final static String[][] extensions
             = { {"adct"},
    	         {"adct"},
    	         {"xml", "db", "lift"},
    	         {"xml"},
    	         {"acorns"},
    	         {"html"}
    	       };
     
     private final static String[] additionalFilters
            = { "web page files (htm)", 
    	 		"rich text format (rtf)", 
    	 		"portable document format (pdf)", };
     
     private final static String[] additionalExtensions
            = { "htm", "rtf", "pdf" };

     private JFileChooser fc;

     private File file;

     
     /** Constructor to configure file choosers
      *
      * @param option 0=load, 1=save, 2=import, 3=export, 4=mobile ap
      * @throws FileNotFoundException If dialog fails
      * @throws IOException If canonical path is illegal
      */
     public ChooseFileDialog(JFrame root, int option)
              throws FileNotFoundException
     {
        boolean load = (option == LOAD || option == IMPORT);
        String extension = "." + extensions[option][0];

        int pathNum = Environment.SAVE_DICT;
        if (load) pathNum = Environment.LOAD_DICT;
        String path = getEnv().getPath(pathNum);
        
        String title = "Please select a file to " + titles[option];

        int size = extensions[option].length;
        if (option==EXPORT)
        	size += additionalExtensions.length;
        String[] totalExtensions = new String[size];
        System.arraycopy(extensions[option], 0
        		, totalExtensions, 0, extensions[option].length);
        
        if (option == EXPORT)
        {
        	System.arraycopy(additionalExtensions, 0
        		, totalExtensions, extensions[option].length, additionalExtensions.length);       
        }
        
        DialogFilter dialogFilter      
           = new DialogFilter(filters[option], totalExtensions);

        String osName = System.getProperty("os.name");
        if (!osName.contains("Mac")) 
        {  
	        fc = new JFileChooser();
	        fc.setCurrentDirectory(new File(path));
	        
	        if (fileName!=null && !load)
	        	fc.setSelectedFile(new File(fileName + extension));
	
	        fc.setDialogTitle(title);
	        fc.setFileFilter( dialogFilter );
	        
	        FileNameExtensionFilter filter;
	        if (option == EXPORT)
	        {
	        	for (int i=0; i<additionalFilters.length; i++)
	        	{
	               filter = new FileNameExtensionFilter(additionalFilters[i], additionalExtensions[i]);
	           	   fc.addChoosableFileFilter(filter);
	        	}
	        }
	        
	        JPanel panel = createAccessoryComponent(extension);
	        if (panel!=null) fc.setAccessory(panel);
	
	        int returnVal = JFileChooser.CANCEL_OPTION;
	
	        try
	        {   if (load) returnVal = fc.showOpenDialog(root);
	            else returnVal = fc.showSaveDialog(root);
	        }
	        catch (Exception e)
	        {  throw new FileNotFoundException("Concurrent execution rejected - try again"); }
	
	        if (returnVal == JFileChooser.APPROVE_OPTION)
	        {  // Add the dictionary extension if needed.
	           file     = fc.getSelectedFile();
	        }
        }
        else
        {

      	    int dialogOption = FileDialog.SAVE;
      	    if (load)
      		    dialogOption = FileDialog.LOAD;
      	  
            FileDialog fd = new FileDialog(new Dialog(root), title, dialogOption);
            
            fd.setDirectory("");
            fd.setDirectory(path);
            
            fd.setFilenameFilter(dialogFilter);
 
	        if (fileName!=null && !load)
	        	fd.setFile(new File(fileName + extension).getName());

            fd.setVisible(true);
            
            String fileName = fd.getFile();
            String directory = fd.getDirectory();
            String fullPath = directory + fileName;
            
            if (option!=LOAD && option!=SAVE && option!=MOBILE)
            {	
                if (!SoundDefaults.isValidForSandbox(fullPath))
	            {
	            	if (  !(option == IMPORT && fullPath.contains("Downloads"))  )
		            	throw new FileNotFoundException
		            	 (LanguageText.getMessage("acornsApplication", 161));
		            }
            }            
            
            if (fileName != null && fileName.length()!=0)
            {
                file = new File(fullPath);
            }
        }
        
        if (file==null)
        {
        	throw new FileNotFoundException("Chooser operation canceled");        	
        }
        
        String fullName;
    	
        try { fullName = file.getCanonicalPath(); }
        catch (Exception e) { throw new FileNotFoundException("Illegal file path"); }

        int lastIndex = -1;
        boolean found = false;
	    if (option==EXPORT)
	    {
		    if (fullName.endsWith(extension))
		    {
    		   found = true; 
		    }
		    else
		    {
		    	for (String ext: additionalExtensions)
		    	{
		    		if (fullName.endsWith(ext))
		    		{
		    			found = true;
		    			break;
		    		}
		    	}
		    }
	    }
    	else
    	{
           for (int i=0; i<extensions[option].length; i++)
           {
	    	   if (fullName.endsWith(extensions[option][i]))
        	   {
        		   found = true; 
        		   break;
        	   }
           }
	    }
	    
        // If not a valid extension, replace with the extension needed.
        if (!found)
        {
           if (SoundDefaults.isSandboxed() && !SoundDefaults.isValidForSandbox(fullName))
           {
           	   throw new FileNotFoundException(
           			 "Apple Store applications disallow adding extensions to selected files"); 
           }
           
    	   lastIndex = fullName.lastIndexOf(".");
    	   if (lastIndex > 0)
    		   fullName = fullName.substring(0, lastIndex);
    	   fullName = fullName + extension;
        }
       
        file = new File(fullName);

        // Verify that it is ok to replace an existing file.
        if (file.exists())
        {
	        if (load)
	        {
	           // Remove the extension
	    	   fileName = file.getName().replaceFirst("[.][^.]+$", "");
	        }
	        else if (!osName.contains("Mac"))
	        {  
	        	int answer = JOptionPane.showConfirmDialog(root,
	                        fullName + " already exists\n\n"
	                                 + "Do you want to replace it?"
	                                 , "Save File Dialog"
	                                 , JOptionPane.OK_OPTION);
	
	           if (answer != JOptionPane.OK_OPTION)
	           {   
	        	  throw new FileNotFoundException("Write operation canceled"); 
	           }
	
	           if (!file.delete())
	           { 
	        	  throw new FileNotFoundException("Couldn't delete "+fullName); 
	           }
	
	           if (option==EXPORT || option==WEB)
	           {  
	        	  if (fullName.endsWith(extension))
	              {  
	        		  lastIndex = fullName.lastIndexOf(extension);
	                  String directory = fullName.substring(0, lastIndex);
	                  deleteDirectory(new File(directory));
	              }
	
	           }
	       }
        }     // End if file.exists();
        else if (load && !file.exists())
        {   
        	throw new FileNotFoundException
                                  ("File " + file.getName() + " not found");
        }

        // Set the path to the file and the file name.
        getEnv().setPath(file, pathNum);
    }   // End of constructor

    public File getSelectedFile() { return file; }

    private JPanel createAccessoryComponent(String extension)
    {  
       Vector<String> files = getEnv().getRecentlyOpenedFiles(extension);
       if (files.size()==0) return null;

       JPanel panel = new JPanel();
       panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
       panel.setBackground(Color.WHITE);
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       JLabel label
         = new JLabel("<html>Recently Opened Files<br> </html>", JLabel.CENTER);
       label.setAlignmentX(Component.LEFT_ALIGNMENT);
       panel.add(label);

       JList<String> list = new JList<String>(files);
       list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       list.addListSelectionListener(this);
       list.setAlignmentX(Component.LEFT_ALIGNMENT);
       panel.add(list);
       panel.add(Box.createVerticalGlue());
       return panel;
    }

    /** Set the selected file based on the list selection */
    public void valueChanged(ListSelectionEvent event)
    {  JList<?> list = (JList<?>)event.getSource();
       String file = (String)list.getSelectedValue();
       fc.setSelectedFile(new File(file));
    }

     /** Get environment data */
    private Environment getEnv()
    {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
        return rootPanel.getEnv();
    }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

    /** Remove a directory that contains files */
    private boolean deleteDirectory(File path)
    {   if (path.exists())
        {  File[] files = path.listFiles();
           if (files!=null)
           {   for(int i=0; i<files.length; i++)
               {  if(files[i].isDirectory())
                  {  deleteDirectory(files[i]);  }
                  else { files[i].delete(); }
               }
           }
        }
        return( path.delete() );
    }
    
    /** Clear default file name when dictionary is closed */
    public static void resetFileName()
    {
    	fileName = null;
    }

}      // End of ChooseFileDialog


