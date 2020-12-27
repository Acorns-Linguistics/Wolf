/*
 * Unit.java
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

package org.wolf.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import org.acorns.data.PictureData;
import org.acorns.data.SoundData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.data.makeApp.ZipWebPage;
import org.wolf.lib.DeepCopy;
import org.wolf.system.MultimediaManager;

/** Word and Definition dictionary units */
public class Unit extends Item implements Serializable, Cloneable
{   private static final long serialVersionUID = 1;

    private String category; // "Word", "Definition", "Linguistic function", or "Example"
    private int picture;  // Index to picture object
    private int sound;    // Index to audio object
    private int movie;    // Index to video object
    private Translation translation;  // Translations in other languages
    private int index;    // Index to selected spinner value

    private transient String directory; // Base directory for import/export
    private transient String extension; // Extension for inport/export (xml, html, or acorns)
    private transient ZipWebPage zip;	// Archive for creating mobile apps
    /** Default constructor
     *
     * @param c Category of unit ("Word", "Definition", "Example");
     */
    public Unit(String c)
    {  super();
       category = c;
       sound = picture = movie = -1;
       translation = new Translation();
       index = 0;
    }

    /** Create object with same structure as another */
    public Unit(Unit unit)
    {  super(unit);
       category = unit.category;
       sound = picture = movie = -1;
       translation = new Translation(unit.translation);
       index = 0;
    }
 
    /** Get the category for this media */
    public String getCategory() { return category; }
    
    @Override
    public String getTitle()    { return category; }
    
    @Override
    public void setTitle(String category) { this.category = category; }
    
    /** Set the category for this media */
    public void setCategory(String category)
    {
    	
    	this.category = category;
    }

    /** Get the picture object */
    public int getPicture() { return picture;
    }
    /** Set the picture object */
    public void setPicture(int p) { picture = p;
    }

    /** Get the audio object */
    public int getAudio() { return sound;  }
    /** Set the audio object */
    public void setAudio(int s) { sound = s; }

    /** Get the movie object */
    public int getMovie() { return movie; }
    /** Set the movie object */
    public void setMovie(int m) { movie = m; }

    /** Set Ontology data (ex: threw, ran, fell) for a language */
    public void setTranslationData(Translation t) { translation = t; }
    /** Set Ontology data (ex: threw, ran, fell) for a language */
    public Translation getTranslationData() { return translation; }
    
    /** Return the hash table of languages and codes */
    @Override
    public Hashtable<String, String> getIndigenousData()
    {
 	   return translation.getIndigenousData();
    }

    @Override
    public void updateLanguageCodes(ArrayList<String> languages)
    {
    	getTranslationData().updateLanguageCodes(languages);
    }

    
    /** Set the gloss for the primary language */
    public void setGloss(String data)
    {
    	translation.setGloss(data);
    }
    
    /** Return the gloss for the primary language */
    public String getGloss()
    {
    	return translation.getGloss();
    }
    
    /** Store the translation data into the translation table */
    @Override
    public void setIndigenousData(Hashtable<String, String> data)
    {
 	   translation.setIndigenousData(data);
    }
    
    public void setIndigenousData(String code, String data)
    {
    	translation.setIndigenousData(code, data);
    }
    
    /** Get the spinner index for viewing multimedia objects */
    public int getIndex() { return index; }
    /** Set the spinner index for viewing multimedia objects */
    public void setIndex(int i) { index = i; }

    /** Create deep copy of this object */
    public @Override Object clone()  { return DeepCopy.copy(this); }
 
    /** Insert Audio into the media object
     * 
     * @param path Path to the picture file
     * @return "" if OK, otherwise an error string
     */
    public String insertAudio(String path)
    {
    	try	
    	{
    		MultimediaManager manager = getEnv().getMultimediaManager();
    		SoundData soundData = new SoundData();
    		soundData.readFile
                (new File(path).toURI().toURL());
    		sound = manager.writeObject(sound, soundData);
    	}
    	catch (Exception e) 
    	{ 
    		return e.toString(); 
    	}
    	return "";
    }

    /** Insert picture into the media object
     * 
     * @param path Path to the picture file
     * @return "" if OK, otherwise an error string
     */
    public String insertPicture(String path)
    {
    	try	
    	{
    		MultimediaManager manager = getEnv().getMultimediaManager();
    		PictureData pictureData = new PictureData
                (new File(path).toURI().toURL(), null);
    		picture = manager.writeObject(picture, pictureData);
    	}
    	catch (Exception e) 
    	{ 
    		return e.toString(); 
    	}
    	return "";
    }

   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc, File file) throws IOException
   {  
	  setBaseDirectory(file);

      ArrayList<String[]> attributes = new  ArrayList<String[]>();
      String nodeName = category;
	  if (category==null || category.length()==0 || category.equalsIgnoreCase("Lexical Function"))
	  {
		  nodeName = "example";
		  attributes.add(new String[]{"title",category});
	  }

      Element node = makeNode(doc, nodeName.toLowerCase(), attributes);
      exportStyle(node);

      String name;
      attributes.clear();
      MultimediaManager manager = getEnv().getMultimediaManager();
      if (sound>=0)
      {  try
         {   
    	     SoundData soundData = (SoundData)manager.readObject(sound);
             name = soundData.getSoundText(SoundData.NAME).trim();
             if (name.length()==0) name = "audio" + sound;
             else name = new File(name).getName();
       
           	 writeAudio(soundData, name);
             attributes.add(new String[]{"audio", name + ".mp3"});
         } catch (Exception e) 
      	 {
        	 System.out.println(e.getMessage());
      	 }
      }
      if (picture>=0)
      {  PictureData pictureData = (PictureData)manager.readObject(picture);
         name = pictureData.getVector().get(0);
         if (!name.toLowerCase().endsWith(".gif"))
         {
        	 int index = name.lastIndexOf(".");
        	 name = name.substring(0, index+1) + "png";
         }
         try
         {  
        	writePicture(pictureData, name);
            attributes.add(new String[]{"picture", name});
         } catch (Exception e) 
         {
        	 System.out.println(e.getMessage());
         }
      }
      if (movie>=0)
      {  
    	 MovieData movieData = (MovieData)manager.readObject(movie);
         name = movieData.getName();
         try
         {  
            writeMovie(movieData, name);
            attributes.add(new String[]{"video", name});
         }  catch (Exception e) {}
      }
      if (attributes.size()>0)
          node.appendChild(makeNode(doc, "media", attributes));

      Element translationNode = translation.exportXML(doc, file);
      node.appendChild(translationNode);
      return node;
   }  // End of export XML()
   
   /** Get path to temporary acorns directory */
   private String getTempDirPath()
   {
	    String property = "java.io.tmpdir";
	    String tempPath = System.getProperty(property) + "acorns";
	    File tempDir = new File(tempPath);
	    if (!tempDir.exists())
	    {
	    	tempDir.mkdirs();
	    }
	    return tempPath + File.separator;
   }
   
   private void writeAudio(SoundData sound, String name)
   					throws Exception
   {
	   if (directory == null) return;
 
	   String root = directory + File.separator + name;
	   
	   if (extension.equals(".html") || extension.equalsIgnoreCase(".xml"))
	   {
		   if (!new File(root+".mp3").exists())
			   sound.writeFile(root + ".mp3");
	   }
	   else if (extension.equals(".acorns"))
	   {
		    name = new File(directory).getName() + "/" + name;
		    String tempPath = getTempDirPath();
		    String path = tempPath + "temp";
		    sound.writeFile(path + ".mp3");
		    zip.addFile(path + ".mp3", name + ".mp3");
	   }
   }
   
   private void writePicture(PictureData picture, String name)
   				throws IOException
   {
	   if (directory == null) return;
	   if (extension.equals(".acorns"))
	   {
		    String tempPath = getTempDirPath();
		    String path = tempPath + "temp";
		    String ext = name.substring(name.lastIndexOf("."));
		    picture.writePicture(new File(path + ext));
		    zip.addFile(path + ext, new File(directory).getName() + "/" + name);
	   }
	   else
	   {
       	   picture.writePicture(makeFileName(name));
	   }
	   
   }
   
   private void writeMovie(MovieData movie, String name)
   				throws IOException
   {
	   if (directory == null) return;
	   if (extension.equals(".acorns"))
	   {
		    String tempPath = getTempDirPath();
		    String path = tempPath + "temp";
		    String ext = name.substring(name.lastIndexOf("."));
		    movie.writeFile(new File(path + ext));
		    zip.addFile(path + ext, new File(directory).getName() + "/" + name);
	   }
	   else
	   {
       		movie.writeFile(makeFileName(name));
	   }
   }

   /** Configure this dictionary element based on XML specifications */
   public void importXML(Element node, File file) throws IOException
   {   
	   MultimediaManager manager = getEnv().getMultimediaManager();

       setBaseDirectory(file);
       importStyle(node);
       
       String title = node.getAttribute("title");
       if (title.equalsIgnoreCase("lexical function"))
       {
    	   category = "Lexical Function";
       }
       else if (title.length()>0)
       {
    	   category = title;
       }

       NodeList list = node.getChildNodes();
       Element element;
       String name;
       for (int i=0; i<list.getLength(); i++)
       {   try
           {   
    	       if (!(list.item(i) instanceof Element))
    	    	   continue;
    	       
    	   	   element = (Element)list.item(i);
               name = element.getNodeName();
               String attribute;
               if (name.equals("media"))
               {  attribute = element.getAttribute("audio");
                  try
                  {   if (attribute.length()>0)
                      {  SoundData soundData = new SoundData();
                         soundData.readFile(makeFileName(attribute));
                         sound = manager.writeObject(sound, soundData);
                      }
                      attribute = element.getAttribute("picture");
                      if (attribute.length()>0)
                      {  PictureData pictureData = new PictureData
                                (makeFileName(attribute).toURI().toURL(), null);
                         picture = manager.writeObject(picture, pictureData);

                      }
                      attribute = element.getAttribute("video");
                      if (attribute.length()>0)
                      {  MovieData movieData
                                       = new MovieData(makeFileName(attribute));
                         movie= manager.writeObject(movie, movieData);
                      }
                  } catch (Exception e) {}
               }
               if (name.equals("translations"))
               {  translation.importXML(element, file); }
           } catch (Exception e) 
       		 {
       		 }
       }
   }        // end of importXML()

    /** Set the directory for reading and writing media files */
    private void setBaseDirectory(File file)  throws IOException
    {  if (file==null) { directory = extension = null; return; }
    
       String fullName = file.getCanonicalPath();
       int lastIndex = fullName.lastIndexOf(".");
       if (lastIndex<0) throw new IOException("Illegal file name");

       directory = fullName.substring(0, lastIndex);
       extension = fullName.substring(lastIndex).toLowerCase();
       
   	   zip = ZipWebPage.getZipWebPage(file);
       
       // Don't create directory for mobile apps
       if (extension.equals(".acorns")) 
       { return; }
       
       File directoryFile = new File(directory);
       if (directoryFile.exists() && directoryFile.isDirectory()) return;
       directoryFile.mkdir();
    }

   /** Make file name for import and exports */
   private File makeFileName(String name)
   {  if (directory != null) name = directory + File.separator + name;
      return new File(name);
   }

   public @Override boolean equals(Object object)
   {   if (object instanceof Unit)
       {  Unit unit = (Unit)object;

          if (!super.isEqual(unit)) return false;
          if (picture != unit.picture) return false;
          if (sound != unit.sound) return false;
          if (movie != unit.movie) return false;
          if (!translation.equals(unit.translation)) return false;
          if (!category.equals(unit.category)) return false;
          return true;
       }
       return false;
   }

   public @Override int hashCode()
   {  return (translation.hashCode() + category.hashCode()
              + (((sound * 31) + movie)*31) + picture)*31 + super.hashCode();
   }
   

}   // End of Unit class
