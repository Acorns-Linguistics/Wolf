/*
 * Environment.java
 *
 *   @author  Harveyd
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

package org.wolf.system;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Vector;

import javax.help.HelpSet;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.acorns.audio.SoundDefaults;
import org.acorns.language.KeyboardFonts;
import org.acorns.language.LanguageText;
import org.acorns.language.keyboards.data.Status;
import org.wolf.data.Constants;
import org.wolf.data.FormatData;
import org.wolf.data.OntologyData;
import org.wolf.data.OntologyNode;

/**
 *
 * @author Harveyd
 */
public class Environment implements Constants
{
    public final static int LOAD_DICT  = 0;
    public final static int SAVE_DICT  = 1;
    public final static int LOAD_IMG   = 2;
    public static final int SAVE_IMG   = 3;
    public static final int LOAD_MOVIE = 4;
    public static final int SAVE_MOVIE = 5;
    public static final int LOAD_AUDIO = 6;
    public static final int SAVE_AUDIO = 7;
    public static final int MAX_PATHS  = 8;	
	
    /** Maximum number of recent file list to remember */
    private static final int MAX_FILES = 9;
    
    /** Panel background color behind buttons */
    public static final Color BACKGROUND     = new Color(200,200,200);
    /** Panel Dark background in scroll bar viewport */
    public static final Color FOREGROUND     = Color.WHITE;
    
    private final String[] videoExtensions
            = {"mp4", "MP4", "mp1", "mp2", "mp3", "m4v", "flv", "FLV", "m4a", "M4a"};

    private boolean[] flags = {false};
    private HelpSet   helpSet;

    private static String[] pathNames;
    private File[] recentFiles;
    private static JFrame root;

    private OntologyData ontology = null;
    private FormatData templates;
    private MultimediaManager mediaManager;

    public Environment(JFrame frame)
    {   root = frame;
        startup();
        new LanguageText(getHelpSet(), 
        		new String[]{"dictionary"}, true );
    }

    public static JFrame getRootFrame() { return root; }
    
    /** Method to set oone of the toggle flags */
    public void setFlag(int which, boolean set) { flags[which] = set; }
    
    /** Method to determine if a toggle flag is set */
    public boolean getFlag(int which) { return flags[which]; }
    
    /** Determine if extension is for audio files
     *
     *  @param extension the extension to check
     *  @return true if yes, false otherwise
     */
    public boolean isAudio(String extension)
    {   if (extension.equalsIgnoreCase("ogg")) return true;
        if (extension.equalsIgnoreCase("mp3")) return true;

        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        for (int i=0; i<types.length; i++)
        {  if (extension.equals(types[i].getExtension())) return true; }
        return false;
    }

    /** Method to determine if file with given extension is a video object
     *
     * @param extension The extension of the file
     * @return true if yes, false otherwise
     */
    public boolean isVideo(String extension)
    {
       for (int i=0; i<videoExtensions.length; i++)
       {   if (videoExtensions[i].equals(extension)) return  true; }
       return  false;
    }

     /** Determine if an extension corresponds to a picture file
     *
     * @param extension The file's extension to check
     * @return true if an picture extension, false otherwise.
     */
    public boolean isPicture(String extension)
    {
        String[] imageArray = ImageIO.getReaderFormatNames();
        for (int i=0; i<imageArray.length; i++)
        {  if (extension.equals(imageArray[i])) return true; }
        return false;
    }

    /** Method to get the ACORNS help set */
    public HelpSet getHelpSet()
    {  if (helpSet!=null) return helpSet;
       try
       {  URL helpURL = Environment.class.getResource("/helpData/wolf.hs" );
          if (helpURL==null) throw new Exception();

          ClassLoader loader = Environment.class.getClassLoader();
          helpSet = new HelpSet(loader, helpURL);
          return helpSet;
       }
       catch (Throwable t) {}
       return null;
    }

    /** Method to read persistent application parameters from disk */
    private void startup()
    { 
    	ObjectInputStream ois = null;

      // Read font and keyboard status.
      Status.readStatus();
      
      // Path to user home/acorns for application specific data
      String dirName = SoundDefaults.getHomeDirectory();

      // Now read file path names.
      FileInputStream fis;
      BufferedInputStream bis;
      
      String sep = System.getProperty("file.separator");
      String defaultPath = SoundDefaults.getDataFolder();      

      try
      {  
    	 // Read settings file or create it.
         String settings = dirName + sep + "recentWolfFiles";
         fis = new FileInputStream(settings);
         bis = new BufferedInputStream(fis);
         ois = new ObjectInputStream(bis);

         recentFiles    = (File[])ois.readObject();
         pathNames      = (String[])ois.readObject();
                 
         if (pathNames.length < MAX_PATHS)
         {  
        	String[] newPathNames = new String[MAX_PATHS];
            for (int i=0; i<newPathNames.length; i++)
            { 
            	newPathNames[i] = defaultPath;   
            }
            for (int i=0; i<pathNames.length; i++)
            {  
            	newPathNames[i] = pathNames[i]; 
            }
            pathNames = newPathNames;
        }

         for (int i=0; i<recentFiles.length; i++)
         {  if (recentFiles[i]!=null)
            {   File file = recentFiles[i];
                if (!file.exists()) recentFiles[i] = null;
            }
         }

         int count = 0;
         for (int i=0; i<recentFiles.length; i++)
         {   if (recentFiles[i]!=null)
             { recentFiles[count] = recentFiles[i];
               if (count++ != i) recentFiles[i] = null;
             }
         }

         try
         {   
        	 // Upwards compatibility.
             ois.readObject();
         }
         catch (Exception e) {}
      }
      catch (Exception ioe)
      {  
    	  recentFiles = new File[MAX_FILES];
    		 for (int i=0; i<recentFiles.length; i++)  recentFiles[i] = null;

          pathNames = new String[MAX_PATHS];
          for (int i=0; i<pathNames.length; i++)  
        	  pathNames[i] = defaultPath;
      }
      try {ois.close(); }catch (Exception ex) {}

      // Attempt to read the Gold Ontology
      try
      {   
    	  String ontologyFile = dirName + sep + "ontology";
          fis = new FileInputStream(ontologyFile);
          bis = new BufferedInputStream(fis);
          ois = new ObjectInputStream(bis);
          ontology = (OntologyData)ois.readObject();
          ois.close();
      }
      catch (Exception e)   { }
      setOntologyData();
      
      // Attempt to read the display template file
      try
      {   
    	  String templateFile = dirName + sep + "templates";
          fis = new FileInputStream(templateFile);
          bis = new BufferedInputStream(fis);
          ois = new ObjectInputStream(bis);
          templates = (FormatData)ois.readObject();
          ois.close();
      }
      catch (Exception e)   { }
      
    }

    /** Method to shutdown wolf and write the persistent parameters to disk */
    public void shutdown()
    { 
    	try
        {  
    		 getMultimediaManager().close();
    		 String dirName = SoundDefaults.getHomeDirectory();
	         String sep = System.getProperty("file.separator");
	         String maps = dirName + sep + "keymaps";
	         KeyboardFonts.writeFonts(maps);
	
	         String settings = dirName + sep + "recentWolfFiles";
	         FileOutputStream     fos = new FileOutputStream(settings);
	         BufferedOutputStream bos = new BufferedOutputStream(fos);
	         ObjectOutputStream   oos = new ObjectOutputStream(bos);
	
	         oos.writeObject(recentFiles);
	         oos.writeObject(pathNames);
	
			 // Update SoundEditor path defaults
	         String[] audio = new String[2];
	         audio[0] = pathNames[LOAD_AUDIO];
	         audio[1] = pathNames[SAVE_AUDIO];
	         SoundDefaults.writeSoundDefaults(audio);
	
	         String language = KeyboardFonts.getLanguageFonts().getLanguage();
	         if (language==null) language = "English";
	         oos.writeObject(language);
	         oos.close();
	
	         // Attempt to write the Gold Ontology
	         String ontologyFile = dirName + sep + "ontology";
	         fos = new FileOutputStream(ontologyFile);
	         bos = new BufferedOutputStream(fos);
	         oos = new ObjectOutputStream(bos);
	         oos.writeObject(getOntologyData());
	         oos.close();
	         
	         // Attempt to write template data
	         String templateFile = dirName + sep + "templates";
	         fos = new FileOutputStream(templateFile);
	         bos = new BufferedOutputStream(fos);
	         oos = new ObjectOutputStream(bos);
	         oos.writeObject(getTemplateData());
	         oos.close();
         
       }
       catch (Exception exception)  {}
    }

    /** Method to get the URL to embedded keylayouts or fonts
    *
    * @param ttf true if url to fonts, false for keylayouts
    * @return URL or null if fail
    */
    public static URL getEmbeddedURL(boolean ttf)
    {   
		URL url = null;
		try
		{   
			File file = Status.getDefaultPath(ttf);
			url = file.toURI().toURL();
		}
		catch (Exception e) {}
		return url;
   }

    /** Set the Gold ontology object */
    public void setOntologyData()
    {   
    	String gold = "gold.xml";
        OntologyNode rootNode = null;
        if (ontology !=null) rootNode = ontology.getOntologyTree();
        ontology = new OntologyData(gold, rootNode);
        if (ontology.getOntologyTree()==null) ontology = null;
    }

    /** Get the Gold ontology data */
    public OntologyData getOntologyData()  { return ontology; }
    
    /** Get the dictionary display template data */
    public FormatData getTemplateData() 
    { 
    	if (templates==null)
    		templates = new FormatData();
    	return templates; 
    }

    /** Get the manager for reading and writing multimedia files */
    public MultimediaManager getMultimediaManager()
    {  if (mediaManager==null) mediaManager = new MultimediaManager();
       return mediaManager;
    }
	
    /**
     *  Get path name for file path for load and save options.
     *  @param option LOAD_DICT,  SAVE_DICT,  LOAD_IMG,   SAVE_IMG,
     *                LOAD_MOVIE, SAVE_MOVIE, LOAD_AUDIO, SAVE_AUDIO
     */
    public String getPath(int option)  { return pathNames[option];  }
  
    /** Reset the file paths after resetting the bookmark location */
    public static void setPaths(String dir)
    {
    	for (int i=0; i<pathNames.length; i++)
    	{
    		pathNames[i] = dir;
    	}
    }
    /**
     *  Get path name for file path for load and save options.
     *  @param option LOAD or SAVE
     */
    public void setPath(File file, int option)
    {   int lastIndex = 0;
        String fullName, fileName;
        setRecentFile(file);
        
        try
        {  
           fullName = file.getCanonicalPath();
           fileName = file.getName();
           lastIndex = fullName.lastIndexOf(fileName);
           if (lastIndex<0) throw new FileNotFoundException();           
        }
        catch (Exception e) 
        { 
        	JOptionPane.showMessageDialog(root, e.toString()); return; 
        }
        String path = fullName.substring(0,lastIndex-1);

        // Update the load and save path names.
        pathNames[option] = path;
        
        try 
        {  String name = file.getCanonicalPath();
           if (name.endsWith(".xml"))
           {  int index = name.lastIndexOf(".xml");
              file = new File(name.substring(0,index) + ".adct");
           }
           else return;
        }
        catch (Exception e) { return; }
        setRecentFile(file);
    }   // End of setPath()
    
    /** Update the recent file list */
    private void setRecentFile(File file)
    {  
    	File recentFile; 
       // Delete file if already in list
       for (int i=0; i<recentFiles.length; i++)
       {  recentFile = recentFiles[i];
          if (recentFile!=null && recentFile.equals(file))
              recentFiles[i] = null;
       }

       // Copy files up
       for (int i=recentFiles.length-1; i>0; i--)
       {  recentFiles[i] = recentFiles[i-1];
       }
       recentFiles[0] = file;

       // Eliminate null entries
       int count = 0;
       for (int i=0; i<recentFiles.length; i++)
       {   
    	   if (recentFiles[i]!=null) 
           { recentFiles[count] = recentFiles[i];
             if (count++ != i) recentFiles[i] = null;
           }
       }
    }

    /** Get the last file that was opened */
    public File getSelectedFile()   {  return recentFiles[0];  }

    /** Get recently opened files of a particular type */
    public Vector<String> getRecentlyOpenedFiles(String ext)
    {
    	Vector<String> files = getRecentlyOpenedFiles();
    	if (files.isEmpty()) return files;
    	
    	String fileName;
    	for (int i=files.size()-1; i>=0; i--)
    	{
    	    fileName = files.get(i);
    	    if (!fileName.endsWith(ext))
    	    	files.remove(i);
    	}
    	return files;
    }
    
    /** Get recently opened files */
    public Vector<String> getRecentlyOpenedFiles()
    {  
       Vector<String> files = new Vector<String>();
       for (int i=0; i<recentFiles.length; i++)
       {  
    	  try
          {   if (!recentFiles[i].exists())
              { recentFiles[i] = null; continue; }
              files.add(recentFiles[i].getCanonicalPath());
          }
          catch (Exception e) { recentFiles[i] = null;  }
       }
       return files;
    }
}       // End of Environment class
