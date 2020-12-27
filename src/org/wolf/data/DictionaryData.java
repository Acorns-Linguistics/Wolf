/*
 *   class DictionaryData.java
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


import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.application.DictionaryDisplayPanel;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.components.ScrollableToolbar;
import org.wolf.conversion.ExcelConversion;
import org.wolf.conversion.LiftConversion;
import org.wolf.conversion.MDFConversion;
import org.wolf.data.makeApp.MakeWebApp;
import org.wolf.data.makeApp.ZipWebPage;
import org.wolf.dialogs.ChooseFileDialog;
import org.wolf.lib.DeepCopy;
import org.wolf.system.Environment;
import org.wolf.system.MultimediaManager;
import org.wolf.undoredo.UndoRedo;
import org.wolf.undoredo.UndoRedoData;
import org.xml.sax.SAXException;

/** class for a language dictionary 
 *
 */
public class DictionaryData  implements Serializable, Cloneable, Constants
{
    private static final long serialVersionUID=1L;

    public static final int MAX_LANGUAGES = 100;
    public static final int MIN_LANGUAGES = 1;
    
    private String active;                 // The name of the active language
    private String[] selectedLanguages;    // List of selected languages
    
    private Vector<Language> languages;    // Languages in this dictionary
    private Vector<Author>   authors;      // Contributing authors
    private String           copyright;    // Copyright notice
    private Font             IPAFont;      // Phonetics font info
    
    private transient DictionaryData dictionaryObject = null;
    private transient boolean dirty = false;
    private transient RootDictionaryPanel root;
    private transient String[] codes;
    
    /** Data to handle redo and undo operations */
    private transient UndoRedo undoRedo;
    
    public DictionaryData()
    {
        languages = new Vector<Language>();
        authors   = new Vector<Author>();
        copyright = active = "";
    }
    
    /* Get copyright text */
    public String getCopyright() { return copyright; }
    /* Set copyright text */
    public void setCopyright(String copyright) { this.copyright = copyright; }
    
    /* Get list of contributing authors */
    public Author[] getAuthors() 
    { return (Author[])authors.toArray(new Author[authors.size()]); }
    
    /* Set list of contributing authors */
    public void setAuthors(Author[] newAuthors)
    {   authors = new Vector<Author>();
        if (newAuthors!=null) 
        { for (int a=0; a<newAuthors.length; a++) authors.add(newAuthors[a]); }        
    }

    /** Method to get the font to use for phonetics */
    public Font getIPAFont()  
    { 
    	if (IPAFont == null)
    		IPAFont = new Font("Times New Roman", Font.PLAIN, 12);
    	return IPAFont;  
    }

    /** Set a new font to use for phonetics */
    public void setIPAFont(Font font) { IPAFont = font; }

    /** Get the parsed gold ontology data */
    public OntologyData getOntologyData()
    {  Environment env = getEnv();
       return env.getOntologyData();
    }
    
    /** Get the file of display formats */
    public FormatData getTemplateData()
    {
    	Environment env = getEnv();
    	return env.getTemplateData(); 
    }

    /** Method to find the language object corresponding to a particular language
     *
     * @param language The language code
     * @return The language object or null if not found
     */
    public Language getLanguage(String key)
    {   Language language;
        for (int i=0; i<languages.size(); i++)
        {   language = languages.get(i);
            if (language.getLanguageCode().equals(key))
                return language;
        }
        return null;
    }

    /** Get a deep copy of the list of languages */
    public Language[] getLanguages()
    { 
    	Language[] languageArray = new Language[languages.size()];
    	for (int i=0; i<languageArray.length; i++)
    	{  
    		languageArray[i] = (Language)languages.get(i).clone(); 
    	}
    	return languageArray;
    }

    /** Method to return the languages currently selected
     *
     * @param active button list or previous
     * @return Vector of selected language objects
     */
    public ArrayList<Language> getListOfLanguages()
    {   
    	if (codes==null || codes.length==0) return new ArrayList<Language>();

        Language language;
        ArrayList<Language> languageVector = new ArrayList<Language>();

        for (int i=0; i<codes.length; i++)
        {   language = getLanguage(codes[i]);
            if (language!=null) languageVector.add(language);
        }
        return languageVector;
    }
    
    public void setActiveLanguage()
    {
    	active = getToolbar().getActiveButton();
    }
    
    public void setListOfLanguages()
    {
    	codes = getToolbar().getSelectedButtons();
    	setActiveLanguage();
    }

    public String[] getSelectedLanguages() { return selectedLanguages; }

    /** Set a modified language list */
    public void setLanguages(Language[] newLanguages)
    {   languages = new Vector<Language>();
        if (newLanguages!=null)
        { for (int lang=0; lang<newLanguages.length; lang++)
          {    languages.add(newLanguages[lang]); }
        }
    }
    
    /**Set a modified language list from an array list */
    public void setLanguages(ArrayList<Language> newLanguages)
    {	
    	languages = new Vector<Language>();
    	if (newLanguages.isEmpty()) return;
    	
    	Language language;
    	String code;
    	selectedLanguages = new String[newLanguages.size()-1]; 
    			
    	if (newLanguages!=null)
    	{ 
    		for (int lang=0; lang<newLanguages.size(); lang++)
    		{ 
    			language = newLanguages.get(lang);
    			code = language.getLanguageCode();
    			if (lang==0) active = code;
    			else selectedLanguages[lang-1] = code;
    			languages.add(newLanguages.get(lang)); 
    		}
    	}
    }
  
    /** Select only the languages that contain words */
    public void setActiveLanguages()
    {
    	if (languages==null || languages.isEmpty()) return;
    	
    	Language language;
    	String code;
    	ArrayList<String> activeLanguages = new ArrayList<String>();
    			
		for (int lang=1; lang<languages.size(); lang++)
		{ 
			language = languages.get(lang);
			code = language.getLanguageCode();
			if (language.getSize()>0)
				activeLanguages.add(code);
		}
		
		selectedLanguages = new String[activeLanguages.size()];
		selectedLanguages = activeLanguages.toArray(selectedLanguages);
    }
   
    /** Method to get the language that is active */
    public Language getActiveLanguage()
    {   Language language;
        if (active.equals("")) active = getToolbar().getActiveButton();
        if (active.equals("")) return null;

        for (int i=0; i<languages.size(); i++)
        {   language = languages.get(i);
            if (active.equals(language.getLanguageCode()))
                return language;
        }
        active = "";
        return null;
    }

    /** Get data for loading the dictionary */
    public String getSavedLanguage() { return active; }
    public void setSavedLanguage()
    { active = getToolbar().getActiveButton(); }
    public Vector<Language> getSavedLanguages() { return languages; }

    /** Determine if this file has changed since the last write */
    public boolean isDirty()  { return dirty; }
    
    
    /** Method to redo a modification to a word in the dictionary */
    public String redo(DictionaryDisplayPanel panel)
    {   UndoRedoData data = undoRedo.redo(null);
        return data.redo(this);
    }
    
    /** Method to undo a modification to a word in the dictionary */
    public String undo(DictionaryDisplayPanel panel)
    {   UndoRedoData data = undoRedo.undo(null);
        return data.undo(this);
    }
    
    public void push(UndoRedoData undoRedoData)
    {   // Get the undoRedo stack for the dictionary if it exists
        dirty = true;
        if (undoRedo==null) { undoRedo = root.getUndoRedo();  }
        undoRedo.pushUndo(undoRedoData);
    }
        
    /** Method to import a dictionary from a URL location
     * 
     * @param file the source file for importing
     * @return "" if successful, a message otherwise
     */
    public String importXML(File file) 
            throws SAXException, IOException, ParserConfigurationException
    {
    	dictionaryObject = this;
    	if (!languages.isEmpty())
    	{
    		JFrame root = Environment.getRootFrame();
        	
    		int ok = JOptionPane.showConfirmDialog(root
                    , "Do you want to merge with the existing dictionary file?"
                    , "Wolf Dictionary - Dictionary Exists"
                    , JOptionPane.YES_NO_OPTION
                    , JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION)
            {
                dictionaryObject = new DictionaryData();
            }
    	}
    	else
    	{
            dictionaryObject = new DictionaryData();
    	}

    	// Check if file is in MDF format (ToolBox)
    	if (file.getName().toLowerCase().endsWith(".db"))
    	{
    		MDFConversion mdf = new MDFConversion(file, dictionaryObject);
    		String result = mdf.convert(); 
    		if (result==null || result.length()==0)
    	        getRootDictionaryPanel().getButtonPanel().enableButtons(true);

    		return result;
    	}
    	
        // Parse the xml and create a DOM object.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(null);

        InputStream stream = file.toURI().toURL().openStream();
        Document document = builder.parse(stream);
        stream.close();
        System.gc();
        document.getDocumentElement().normalize();
        
        // Determine type of document
        Element rootNode = document.getDocumentElement();
        
        if (rootNode.getNodeName().toLowerCase().equals("workbook"))
        {
    		ExcelConversion excel = new ExcelConversion(rootNode, dictionaryObject);
    		String result = excel.convert(); 
    		if (result==null || result.length()==0)
    	        getRootDictionaryPanel().getButtonPanel().enableButtons(true);

    		return result;
        }
        
        if (rootNode.getNodeName().toLowerCase().equals("lift"))
        {
    		LiftConversion lift = new LiftConversion(rootNode, dictionaryObject, file);
    		String result = lift.convert(); 
    		if (result==null || result.length()==0)
    	        getRootDictionaryPanel().getButtonPanel().enableButtons(true);

    		return result;
        }
        FontData fonts = new FontData(document);
        String path = file.getCanonicalPath();
        path = path.substring(0,  path.lastIndexOf('.'));
        fonts.registerFonts(new File(path, "Assets/Fonts"));
        
        // Get the language name from the root element.
        String face = rootNode.getAttribute("face");
        String attSize = rootNode.getAttribute("size");
        int size = 12;
        if (attSize.length()>0) size = Integer.parseInt(attSize);
        dictionaryObject.IPAFont = new Font(face, Font.PLAIN, size);

        NodeList list  = rootNode.getElementsByTagName("copyright");
        if (list.getLength()>0)
           dictionaryObject.copyright = list.item(0).getTextContent();

        list = rootNode.getElementsByTagName("author");
        Author author;
        Element element;
        for (int i=0; i<list.getLength(); i++)
        {  try
           {   element = (Element)list.item(i);
               author = new Author();
               author.importXML(element);
               if (author.isClear())
            	   continue;
               
               if (dictionaryObject.authors.contains(author))
            	   continue;
               dictionaryObject.authors.add(author);
           } catch (Exception e) {}
        }

        list = rootNode.getElementsByTagName("language");
        Language language;

        int wordCount = 0, len = list.getLength(), counts[] = new int[len];
	   	getErr().setText("Progress =   0.000%");
	   	for (int i=0; i<len; i++)
	   	{
	   		element = (Element)list.item(i);
	        counts[i] = wordCount += element.getElementsByTagName("word").getLength();
	   	}

	   	String code;
	   	boolean exists;
        for (int i=0; i<len; i++)
        {  try
           {   element = (Element)list.item(i);
    	       code = element.getAttribute("lang");
    	   	   if (code.length()==0) throw new SAXException();
    	   	   
    	   	   language = getLanguage(code);
    	   	   exists = language != null;
    	   	   if (!exists)
    	   		   language = new Language();
    	   	   
               language.importXML(element, file, (i==0) ? 0 : counts[i-1], wordCount);
               if (!exists) dictionaryObject.languages.add(language);
           } catch (Exception e) {}
        }
        dirty = false;
        getRootDictionaryPanel().getButtonPanel().enableButtons(true);
        return "";
    };
    
    /** Method to export this dictionary to an XML file
     * 
     * @param file The file name for the dictionary
     * 		If ends with xml (export), html (web page), acorns (mobile app)
     * @return message if export fails or "" if succeeds
     */
    public String exportXML(File file) 
                 throws ParserConfigurationException, IOException, BadLocationException,
                        TransformerConfigurationException, TransformerException
    {   
    	getDisplayPanel().stopEditing();
    	
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        document.setXmlStandalone(true);
        
        Element rootNode = document.createElement("amdx");
        if (IPAFont!=null)
        {  rootNode.setAttribute("face", IPAFont.getName());
           rootNode.setAttribute("size", "" + IPAFont.getSize());
           rootNode.setAttribute("version", version);
        }
        document.appendChild(rootNode);
        
        Element node;
        if (copyright.length()>0)
        {    node = document.createElement("copyright");
             node.setTextContent(copyright);
             rootNode.appendChild(node);
        }
        
        Author author;
        if (authors.size()>0)
        {   
        	 node = document.createElement("authors");
             for (int i=0; i<authors.size(); i++)
             { 
            	 author = authors.get(i);
            	 if (author.isClear())
            		 continue;
            	 
            	 node.appendChild(author.exportXML(document)); 
             }
            rootNode.appendChild(node);
        }

        if (languages.size()>0)
        {    node = document.createElement("languages");
        
        	 int wordCount = 0, count = 0;
        	 getErr().setText("Progress =   0.000%");
        	 for (int i=0; i<languages.size(); i++)
        	 {
        		 wordCount += languages.get(i).getWordCount();
        	 }
        	 
             for (int i=0; i<languages.size(); i++)
             { 
            	 node.appendChild(languages.get(i).exportXML(document, file, file==null, count, wordCount));
            	 count += languages.get(i).getWordCount();
             }
             rootNode.appendChild(node);
        }
        
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        
        transformer.setOutputProperty
                  ("{http://xml.apache.org/xslt}indent-amount", "3");
        DOMSource source = new DOMSource(document);

        OutputStream stream;
    	FormatData format = getTemplateData();
    	format.setDirectory(file);

    	selectedLanguages = getToolbar().getSelectedButtons();
    	active = getToolbar().getActiveButton();

    	if (file==null) 
        { 
        	String output = format.toHTML(document, active, selectedLanguages);
        	if (output != null) return output;
        	
        	StringWriter streamWriter = new StringWriter();
        	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, new StreamResult(streamWriter));
            return streamWriter.toString();
        }
        
	    FontData fontData = new FontData(document);
        fontData.writeFont(file);
    	
    	if (file.getName().endsWith(".htm"))
        {
        	String output = format.toHTML(document, active, selectedLanguages);
        	if (output == null) throw new IllegalArgumentException();
        	
			BufferedWriter bufWriter = new BufferedWriter
				      (new OutputStreamWriter
				    		  (new FileOutputStream(file),"UTF-8"));
			PrintWriter out = new PrintWriter(bufWriter, true);
            try { out.write(output); }
            finally { out.close(); }
            return "";

        }
        else if (file.getName().endsWith(".rtf"))
        {
        	String output = format.toRTF(document, active, selectedLanguages);
        	if (output == null) throw new IllegalArgumentException();

        	BufferedWriter bufWriter = new BufferedWriter
				      (new OutputStreamWriter
				    		  (new FileOutputStream(file),"UTF-8"));
			PrintWriter out = new PrintWriter(bufWriter, true);
          try { out.write(output); }
          finally { out.close(); }
          return "";
        }
        else if (file.getName().endsWith(".pdf"))
        {
        	byte[] output = format.toPDF(document, active, selectedLanguages);
        	if (output == null) throw new IllegalArgumentException();

        	FileOutputStream out = new FileOutputStream(file);
        	try { out.write(output); }
        	finally { out.close(); }
        	return "";
        }
        else if (file.getName().endsWith(".acorns"))
        {
        	StringWriter streamWriter = new StringWriter();
            transformer.transform(source, new StreamResult(streamWriter));

            MakeWebApp webApp = new MakeWebApp(file, fontData.getFonts());
            webApp.makePage(document, streamWriter.toString());
        }
        else
        { 
        	StringWriter streamWriter = new StringWriter();
            transformer.transform(source, new StreamResult(streamWriter));

            MakeWebApp webApp = new MakeWebApp(file, fontData.getFonts());
            webApp.makePage(document, streamWriter.toString());
            
        	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
  
            String xml = file.getCanonicalPath();
            xml = xml.substring(0,  xml.lastIndexOf('.')) + ".xml";
            stream = new FileOutputStream(xml);
            StreamResult result = new StreamResult(stream);
            transformer.transform(source, result);
            stream.close();
        }
        
        ZipWebPage zip = ZipWebPage.getZipWebPage(file);
        zip.close();
        return "";
    }
    
    /** Close the open file */
    public String closeFile()
    {  getDisplayPanel().stopEditing();
       ScrollableToolbar toolbar = getToolbar();
       toolbar.reset();
       toolbar.repaint();
       
       
       return "";
    }
    
    /** Method to write dictionary file to disk
     * 
     * @return "" if ok, error message otherwise
     */
    public String writeFile(File file)
    {   
    	String path;
    	ObjectOutputStream oos = null;

    	try
    	{
        	// Replace extension
    		path = file.getCanonicalPath();
    	
	    	int index = path.lastIndexOf('.');
	    	if (index>=0)
	    	{
	    		path = path.substring(0,index);
	    	}
	    	file = new File(path + ".adct");
	    	
	    	
	    	// Update the active language before the write
	        getDisplayPanel().stopEditing();
	        active = getToolbar().getActiveButton();

	        // Update the list of selected languages
	        selectedLanguages = getToolbar().getSelectedButtons();

        	FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            MultimediaManager manager = getEnv().getMultimediaManager();
            manager.writeMediaData(oos);
            oos.close();
        }
        catch (IOException iox) 
        {   try { oos.close(); } catch(Exception e) {}
            return "Save operation failed " + iox.toString(); 
        }
        dirty = false;
        return "";          
    }   // End of write file.   

     /**  Read sound file from disk
      * 
      * @return "" if successful, messag otherwise
      */
     public String readFile(File file)
     {   ObjectInputStream ois = null;
         try
         {  FileInputStream fis     = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);
            dictionaryObject = (DictionaryData)ois.readObject();
            MultimediaManager manager = getEnv().getMultimediaManager();
            manager.readMediaData(ois);
            ois.close();
         }
         catch (Exception exception)
         {  try {ois.close();}
            catch(Exception e) { }
            return exception.toString();
         }
         dictionaryObject.dirty = false;
         return "";
     }   // end choseFile()
     
     /** Method to get loaded DictionaryData object after a read
      * 
      * @return DictioanryData object
      */
     public DictionaryData getDictionary() 
     { return dictionaryObject; }
     
     /** Method to insertion sort the Language objects */
     public void sortLanguages()
     {   Language temp;
         Language[] sortedLangs = getLanguages();
         
         for (int i=1; i<sortedLangs.length; i++)
         {
             temp = sortedLangs[i];
             int j = i;
             while (j>0)
             {
                 if (sortedLangs[j-1].compareTo(temp)<=0) break;
                 sortedLangs[j] = sortedLangs[j-1];
                 j--;
             }
             sortedLangs[j] = temp;
         }
         setLanguages(sortedLangs);
     }

    /** Get the scrollable toolbar */
    private ScrollableToolbar getToolbar()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getToolbar();
    }

    /** Get the environment  object */
    private DictionaryDisplayPanel getDisplayPanel()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getDisplayPanel();
    }

    /** Get the environment  object */
    private Environment getEnv()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getEnv();
    }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   if (root==null)
        {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

            if (pcl.length>0) root = (RootDictionaryPanel)pcl[0];
        }
        return root;
    }
    

    /** Get label to set messages with user information */
    protected JLabel getErr()
    {   
    	RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
        return rootPanel.getErrorLabel();
    }


    /** Determine if dirty file should be saved
     *
     * @return true if file close is complete, false to cancel the operation
     */
    public boolean checkDirty()
    {   
    	if (isDirty())
        {   
    		JFrame root = Environment.getRootFrame();
    	
    		int ok = JOptionPane.showConfirmDialog(root
                    , "Do you want to save the dictionary file?"
                    , "Wolf Dictionary - CLose file dialog"
                    , JOptionPane.YES_NO_CANCEL_OPTION
                    , JOptionPane.WARNING_MESSAGE);
            if (ok == JOptionPane.YES_OPTION)
            {   
       		     File file = getEnv().getSelectedFile();
           		 try
           		 {
                        ChooseFileDialog sDialog;
                        sDialog = new ChooseFileDialog
                                 (root, ChooseFileDialog.SAVE); 

                        file = sDialog.getSelectedFile();
            		 
           		 }
           		 catch(Exception e)
           		 {
           			 return true;
           		 }
                 writeFile(file); return true;
            }
            if (ok == JOptionPane.NO_OPTION)  {  return true; }
            return false;
        }
        return true;
    }

    /** Make an identical copy of this object
     *
     * @return The cloned Example object
     */
    public @Override Object clone()  {  return DeepCopy.copy(this); }

}   // End of DictionaryData class
