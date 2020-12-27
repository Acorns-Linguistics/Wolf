 /* FileMakeMobileApp.java
 *
 * Created on September 22, 2011, 3:07 PM
 *
 *
 *   @author  HarveyD
 *   @version 7.00 Beta
 *
 *   Copyright 2011-2015, all rights reserved
 */
package org.wolf.data.makeApp;

import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.FormatData;
import org.wolf.data.Language;
import org.wolf.system.Environment;

//----------------------------------------------------------
// Class to export a file with a selected name.
//----------------------------------------------------------
public class MakeWebApp
{
	private final String[] icons 
       = { "search.png", "language.gif", "copyright.png", "author.png",
		   "reload.png", "prev.png", "next.png"};
	
	private final String[] audios
	  = {"beep.wav", "beep.ogg", "beep.mp3" };

	
	private ArrayList<Font> fonts;
	private File file;
	private ZipWebPage zip;
	private ByteArrayOutputStream stream = null;

	
	public MakeWebApp(File file, ArrayList<Font> fonts)
				throws IOException, FileNotFoundException
	{
		this.fonts = fonts;
		this.file  = file;
		zip = ZipWebPage.getZipWebPage(file);
	}
	
	//----------------------------------------------------------
	// Method to process export options.
	//----------------------------------------------------------
	public void makePage(Document xmlDocument, String document)  throws IOException
	{
		String path = file.getCanonicalPath();
		String directory = path.substring(0,path.lastIndexOf("."));
		String shortName = new File(directory).getName();
		  
		boolean mobile = path.endsWith(".acorns");
		File output = new File(directory);
		copyMobileFiles(output, mobile); 
		copyIcons(output, mobile);
		copyAudio(output, mobile);
		

		String page = directory.substring(0, directory.lastIndexOf(shortName)) 
				+ File.separator + shortName + ".html";
		
		FormatData templates = getDisplayTemplateObject();
		String json = templates.toString();
		
		makeHTMLFile(page, shortName, document, json, mobile);  
	}  // End of processOption().
		
	
	/** Copy a file from a URL address to an output destination
	 * 
	 * @param url The URL for the source file
	 * @param output The destination directory for the source file
	 * @param shortName relative path to destination
	 * @param mobile true if mobile app; false if web app
	 * @throws IOException File couldn't be written
	 */
	private void copyURLToFile(URL url, File outputDir, String shortName, boolean mobile)
						throws IOException
	{
	    BufferedInputStream inStream = null;
	    BufferedOutputStream outStream = null;
	    ByteArrayOutputStream stream = null;
	     
        int bufSize = 8192;
        inStream = new BufferedInputStream
    	  	   			( url.openConnection().getInputStream(), bufSize);
       
        if (mobile)
        {
        	stream = new ByteArrayOutputStream();
        	outStream = new BufferedOutputStream(stream);
        }
        else
        {
        	File file = new File(outputDir, shortName);
        	outStream = new BufferedOutputStream(new FileOutputStream(file), bufSize);
        }
          
        int read = -1;
        byte[] buf = new byte[bufSize];
        while ((read = inStream.read(buf, 0, bufSize)) >= 0) 
        {
           outStream.write(buf, 0, read);
        }
        outStream.flush();	      
        try { inStream.close(); }  catch (Exception ex) {}
        try { outStream.close(); } catch (Exception cioex) {}
	     
	     if (stream != null)
	     {
 			 byte[] bytes = stream.toByteArray();
			 zip.addFileBytes(shortName, bytes);
	     }
	}
	
	/** Copy the list of files to an output directory
	 * 
	 * @param files The list of file names
	 * @param outputDir The destination directory
	 * @param type "Icons" or "Audio"
	 * @return true if successful
	 * @param mobile true if mobile app; false if web app
	 * @throws IOException File couldn't be written
	 */
	private void copyFileList(String[] files, String type, File outputDir, boolean mobile)
						throws IOException
	{
		String fileName, shortName;
		for (int i=0; i<files.length; i++)
		{
			fileName = "/resources/" + files[i];
			shortName = ((mobile) ? outputDir.getName() + "/" : "") + "Assets/" + type + "/" + files[i];
            URL url = getClass().getResource(fileName);
            if (!mobile) 
            {
            	File fileDir = new File(outputDir, "/Assets/" + type);
				if (!fileDir.exists()) fileDir.mkdirs();
            }
            copyURLToFile(url, outputDir, shortName, mobile);
 		}
	}
	
	/** Copy icons needed for the web application
	 * 
	 * @param outputDir The path to the output directory
	 * @param mobile true if mobile app; false if web app
	 * @return true if successful
	 */
	private void copyIcons(File outputDir, boolean mobile)
						throws IOException
	{
		copyFileList(icons, "Icons", outputDir, mobile);
	}
	
	private void copyAudio(File outputDir, boolean mobile)
						throws IOException
	{
		copyFileList(audios, "Audio", outputDir, mobile);
	}
	
	
	/** Copy the executables needed for the mobile application
	 * @param outpuDir output directory (null if to compressed folder) 
	 * @param mobile true if mobile app; false if web app
	 * @throws IOException File couldn't be written
	 */
	private void copyMobileFiles(File outputDir, boolean mobile)
						throws IOException
	{
		String[] files = {"acorns.js", "styleDictionary.js", "style.css"};
		
		String shortName;
		URL url;
		
		for (int i=0; i<files.length; i++)
		{
			String fileName = "/resources/" + files[i];
			url = getClass().getResource(fileName);
			shortName = ((mobile) ? outputDir.getName() : "") + "/Assets/" + files[i];
			if (!mobile) 
			{
				File fileDir = new File(outputDir, "Assets");
				if (!fileDir.exists()) fileDir.mkdirs();
			}
	        copyURLToFile(url, outputDir, shortName, mobile);
		}
	}
	
	/** Create output stream for writing to a file or a zip folder 
	 * 
	 * @param page The destination filename
	 * @param mobile true if mobile app; false if web app
	 * @return the PrintWriter output stream
	 * @throws IOException
	 */
	private PrintWriter makeOutputStream(String page, boolean mobile)
					throws IOException
	{
		PrintWriter out;
		BufferedWriter bufWriter;
		if (mobile)
		{
			stream = new ByteArrayOutputStream();
			
			bufWriter = new BufferedWriter
				    (new OutputStreamWriter(stream,"UTF-8"));
			out = new PrintWriter(bufWriter, true);
		}
		else
		{
			bufWriter = new BufferedWriter
		      (new OutputStreamWriter
		    		  (new FileOutputStream(page),"UTF-8"));
	        out = new PrintWriter(bufWriter, true);
		}
		return out;
	}
	
	/** Copy the HTML file template to the destination 
	 * 
	 * @param page The destination filename
	 * @param shortName The name of the page without extension
	 * @param mobile true if mobile app; false if web app
	 * @param xmlString The xml string (dictionary structure) to include with the html
	 * @param jsonString The JSON string (display templates) to include with the html
	 */
	private void makeHTMLFile(String page, String shortName
			       , String xmlString, String jsonString, boolean mobile) throws IOException
	{
		String fileName = "/resources/" + "mobileTemplate.html";
		URL url = getClass().getResource(fileName);
		PrintWriter out = makeOutputStream(page, mobile);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String line = in.readLine();
		while (line!=null)
		{
			if (line.indexOf("FONTS")>=0)
			{
				out.println(createFontFace(shortName, fonts));
			}
			else if (line.indexOf("XML")>=0)
			{
				out.print("\t\tvar xml =\"");
				
		        xmlString = xmlString.replaceAll("\"", "\\\\\""); 
		        xmlString = xmlString.replaceAll("(\\r|\\n)+","\\\\n");
				out.print(xmlString);
				out.println("\";");
				
				String xmlFont = createEmbeddedList();
				out.println(xmlFont);
			}
			else if (line.indexOf("JSON")>=0)
			{
				out.print("\t\tvar json = '");
				
		        jsonString = jsonString.replaceAll("(\\r|\\n)+","\\\\n");
				out.print(jsonString);
				out.println("';");
				
			}
			else
			{
				line = line.replaceAll("HHHH", shortName);
				line = line.replaceAll("TTTT", shortName);
				out.println(line);
			}
			line = in.readLine();
		}
		if (mobile)
		{
			byte[] bytes = stream.toByteArray();
			zip.addFileBytes(shortName + ".html", bytes);
		}
		out.close();
		in.close();
	}

	/** Create JSON strings of keylayouts */
	private String createEmbeddedList()
	{
		StringBuilder buffer = new StringBuilder();

		ArrayList<String[]> fonts = Language.exportEmbeddedFonts();
		for (String[] xml: fonts)
		{
			buffer.append("\t\t" + xml[1] + "\n");
		}

		return buffer.toString();		
	}

	
	/** Copy embedded fonts needed for the web application
	 * 
	 * @param shortName The name of the directory containing assets
	 * @param fonts Array list of fonts to create faces for
	 * @return String of HTML font-face tags
	 * 
	 */
	public static String createFontFace(String shortName, ArrayList<Font> fonts)
	{
		Font font;
		String family, fontName;
		StringBuilder buffer = new StringBuilder();
		
	    for (int i=0; i<fonts.size(); i++)
	    {
	       font = fonts.get(i);
    	   fontName = font.getFontName();
    	   family = font.getFamily();
    	     
    	   buffer.append("\t\t@font-face  { 	font-family:\"");
    	   buffer.append(family);
    	   buffer.append("\";\n\t\t\t\t\t\t");

    	   buffer.append("src: ");
    	   buffer.append("local(\"" + family + "\"),");
    	   buffer.append("\n\t\t\t\t\t\t\t");
    	   
    	   buffer.append(" url(\"" + shortName);
    	   buffer.append("/Assets/Fonts/");
    	   buffer.append(fontName);
    	   buffer.append(".woff\") format(\"woff\"),\n\t\t\t\t\t\t\t");

    	   buffer.append(" url(\"");
    	   buffer.append(shortName);
    	   buffer.append("/Assets/Fonts/");
    	   buffer.append(fontName);
    	   buffer.append(".ttf\") format(\"truetype\");\n\t\t\t\t\t}\n\n");
	    }
		return buffer.toString();
	}
	
    /** Retrieve the object containing display templates */
    private FormatData getDisplayTemplateObject()
    {   
        Environment  env = getRootDictionaryPanel().getEnv();
        return env.getTemplateData();
    }
    
    private RootDictionaryPanel getRootDictionaryPanel()
    {
    	RootDictionaryPanel root = null;
    	PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        if (pcl.length>0) root = (RootDictionaryPanel)pcl[0];
        return root;
    }


 	
}   // end of MakeWebApp

