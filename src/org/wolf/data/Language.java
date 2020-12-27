/*
 *   class Language.java
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

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.acorns.language.KeyboardFonts;
import org.acorns.language.LanguageFont;
import org.acorns.language.keyboards.lib.KeyLayoutLanguages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.application.DictionaryView;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.lib.DeepCopy;
import org.xml.sax.SAXException;

import org.wolf.system.Environment;

/** Class to define a language */
public class Language implements Serializable, Cloneable, Comparable<Language>
{
    private static final long serialVersionUID=1L;
    
    /** Offset of audio into url array */
    public static final int AUDIO = 0;
    /** Offset of pictures into url array */
    public static final int IMAGE = 1;
    /** Offset of movies into url array */
    public static final int MOVIE = 2;
    
    /** Offset of language code into language fields array */
    private static final int LANG = 0;
    /** Offset of dialect code into language fields array */
    private final static int DIALECT = 1;
    /** Offset of custorm sort order into language fields array */
    private final static int SORT_ORDER = 2;

    private String                  code;
    private LanguageFont            languageFont;
    private ArrayList<Word>         wordList;

    private transient ArrayList<Word> sortedList;
    private transient DictionaryView dictionaryView;
    private transient Hashtable<String, Word> hashWords;
    
    private transient HashMap<Character, Integer> sortMap;
    private transient String[] languageFields;
    
    private static KeyLayoutLanguages keylayouts = null;

    public Language()
    {  code = "";
       languageFont = new LanguageFont("", 12, "none");
       wordList = new ArrayList<Word>();
    }

    public Language(String code, LanguageFont languageFont)
    {
        setLanguageFields(code);
        this.languageFont = languageFont;    

        wordList = new ArrayList<Word>();
    }
    
    
    public void hookLanguage(Component component)
    {
    	if (sortMap == null) initializeData(code);
    	String languageName = languageFont.getLanguage();
   	 	KeyLayoutLanguages keyLayouts = getKeyLayoutLanguages();
    	keyLayouts.hookComponent(component, languageName);
    	Font font = languageFont.getFont();
    	component.setFont(font);
    }
    
    public void unhookLanguage(Component component)
    {
   	 	KeyLayoutLanguages keyLayouts = getKeyLayoutLanguages();
    	keyLayouts.unhookComponent(component);
    }
    
    /** Export Embedded fonts to an array of strings
     *    (called when a Lesson web-page is created)
     *
     * @param path Destination File
     */
    public static ArrayList<String[]> exportEmbeddedFonts()
    {  
       ArrayList<String[]>keyboards = new ArrayList<String[]>();
   	
	   KeyLayoutLanguages keyLayouts = getKeyLayoutLanguages();
 
       String[] languages = KeyboardFonts.getLanguageFonts().getLanguages();
       for (String language: languages)
       {
           String xmlString = keyLayouts.exportKeyLayout(language);
           if (xmlString != null)
           {
           	String[] data = new String[2];
           	data[0] = language;
           	data[1] = xmlString;
           	keyboards.add(data);
           }
       }
       return keyboards;
    }
    
    public static KeyLayoutLanguages getKeyLayoutLanguages()
    {
    	if (keylayouts == null)
    	{
       	 	URL url = Environment.getEmbeddedURL(false);
    		keylayouts = new KeyLayoutLanguages(url);
    	}
    	return keylayouts;
    }
    
    
    /** Get and set methods */
    
    /** Get language code and dialect */
    public String getLanguageCode() 
    { 
    	if (sortMap == null) initializeData(code);
    	
    	if (languageFields[DIALECT].length() == 0)
    	    return languageFields[LANG];
    	else return languageFields[LANG] + "/" + languageFields[DIALECT];
    }
    
    /** Get language sort order */
    public String getSortOrder()
    {
    	if (sortMap == null) initializeData(code);
    	return languageFields[SORT_ORDER];
    }
    
    /** Set language code, dialect, and sort order */
    public void setLanguageFields(String langCode) 
    { 
    	String[] fields = parseLanguage(langCode);
    	code = combineLanguageFields(fields);
    	initializeData(code);
    }
    
    public LanguageFont getLanguageFont() { return languageFont; }
    public void setLanguageFont( LanguageFont languageFont) 
    {
    	this.languageFont = languageFont; 
    }
    
    /** Method to get the native font for this language
     * 
     * @return The font for the language
     */
    public Font getFont( )
    {
        if (languageFont==null) return null;
        return languageFont.getFont();
    }

    public DictionaryView getView()
    {   if (dictionaryView==null)
        {   dictionaryView = new DictionaryView();  }
        return dictionaryView;
    }

   public void setView(String pattern)
   { if (dictionaryView==null) getView();
     dictionaryView.createView(pattern);  }
    
    /** Method to get the information for this font as a string
     * 
     * @return Font information string
     */
    public String getFontInfo()  
    {
    	
    	return languageFont.toString(getLanguageCode()); 
    }
    
    /** Method to get the font information with the code/dialect
     * 
     * @return Font information with language code/dialect
     */
    public String getFontInfoDialect()
    {
    	String code = getLanguageCode();
    	code += "       ";
    	code = code.substring(0,7);
    	return code + languageFont.toString(code);
    }

    /** Get all of the words in this dictionary */
    public ArrayList<Word> getWords() 
    { 
    	return wordList; 
    }

    /** Sort during an import process */
    public void sortWords()
    {
    	if (wordList==null) return;
    	final Language lang = this;
    	
		Collections.sort(wordList, new Comparator<Word>()
		{
			@Override
			public int compare(Word o1, Word o2) 
			{
				String k1 = o1.getKey();
				String k2 = o2.getKey();
				return lang.compare(k1, k2);
			}
		});
		
		sortedList = wordList;
    }
    
    /** Get the word at a specific index */
    public Word getWord(int row)
    {  if (dictionaryView==null) getView();
       int which = dictionaryView.mapViewIndex(row);
       if (which>=0 && which<wordList.size())  return wordList.get(which);
       return null;
    }
    
    /** Method to add a word to the list
     * 
     * @param row place to add the word
     * @param word word to add
     * @return "" if Successful, error message otherwise
     */
    public String addWord(int row, Word word)
    {   
    	if (dictionaryView==null) getView();
        String key = word.getKey();
        int spot = find(word);
        if (key != null && key.trim().length()>0 && spot>=0)
            return "The edited word is already on file";

        int index = dictionaryView.mapViewIndex(row);
        dictionaryView.insertViewIndex(row);

        wordList.add(index, word);
 	    Hashtable<String, Word> hash = getHash();
    	hash.put(key,  word);

        insert(word);
        return "";
    }
    
    /** Method to merge a word into the list */
    public void mergeWord(Word word)
    {
    	Hashtable<String, Word> hash = getHash();
    	String key = word.getKey();
    	Word oldWord = hash.get(key);
        if (oldWord != null)
        {
        	moveMedia(oldWord.getMedia(), word.getMedia());
        	
        	ArrayList<Item> oldItems = oldWord.getRows();
        	ArrayList<Item> items = word.getRows();
        	
        	for (Item oldItem: oldItems)
        	{
        		if (oldItem instanceof Comment)
        		{
        			continue;
        		}
        		
        		Unit unit;
        		if (oldItem instanceof Group)
        		{
        			Group group = (Group)oldItem;
        			unit = group.getMedia();
        		}
        		else
        		{
        			unit = (Unit)oldItem;
        		}
        		
        	    Unit matchUnit = findMatch(oldItem, items);
        		if (matchUnit != null)
        		{
        			moveMedia(unit, matchUnit);
        		}
        	}
        	return;
        }
    	
    	addWord(word);
    }

    /** Find matching item in a list - return item or null if not found */
    private Unit findMatch(Item item, ArrayList<Item> items)
    {
    	String key = "", value = "";
    	
    	Unit unit;
    	if (item instanceof Group)
    	{
    		unit = ((Group)item).getMedia();
    	}
    	else
    	{
    		unit = ((Unit)item);
    	}
    	
    	Hashtable<String, String> hash = unit.getIndigenousData();
    	key = hash.get(getLanguageCode());
    	if (key==null)
    		key = unit.getGloss();
    	
    	boolean same;
    	for (Item thisItem: items)
		{
    		same = thisItem.getClass().equals( item.getClass());
    		if (same)
    		{
    			if (thisItem instanceof Group)
    			{
    				unit = ((Group)thisItem).getMedia();
    			}
    			else
    			{
    				unit = (Unit)thisItem;
    			}

    	    	hash = unit.getIndigenousData();
    	    	value = hash.get(getLanguageCode());
    	    	if (value==null)
    	    		value = unit.getGloss();

    	    	if (value.equals(key))
				{
					return unit;
				}
    		}
 		}
    	return null;
    }
  
    /** Get the hastable of all the words in the dictionary */
    private Hashtable<String, Word> getHash()
    {
    	if (hashWords==null)
    	{
    		String key;
    		Word word;
    		hashWords = new Hashtable<String, Word>(1000, 0.5F);
    		
    		for (int i=0; i<wordList.size(); i++)
    		{
    			word = wordList.get(i);
    			key = word.getKey();
    			hashWords.put(key, word);
    		}
    	}
    	return hashWords;
    }
    
    /** Save audio, movie, picture items into new Item */
    private void moveMedia(Unit source, Unit destination)
    {
    	int picture = source.getPicture();
    	int movie = source.getMovie();
    	int audio = source.getAudio();
    	
    	if (picture==-1 && movie==-1 && audio==-1)
    		return;
    	
    	destination.setPicture(picture);
    	destination.setMovie(movie);
    	destination.setAudio(audio);
    }
    
    /** Remove word from dictionary based on its unsorted index
     *
     * @param row Displayed row in the word list table
     * @return "" if successful, error message otherwise
     */
    public String removeWord(int row)
    {  
    	
       if (dictionaryView==null) getView();
       if (row<0 || row>=dictionaryView.size()) { return "No such word on file"; }

       int index = dictionaryView.mapViewIndex(row);
       dictionaryView.removeViewIndex(row);

       Word word = wordList.get(index);
       wordList.remove(index);

	   Hashtable<String, Word> hash = getHash();
	   String key = word.getKey();
   	   hash.remove(key);


       index = find(word);
       if (index>=0) sortedList.remove(index);
       return "";
    }

        /** Method to modify a word in the list
     *
     * @param word Word to replace on file
     * @return "" if successful, error message otherwise
     */
    public String modifyWord(Word word, int row)
    {   
    	// More work required if the key value changes
        if (dictionaryView==null) getView();
        row = dictionaryView.mapViewIndex(row);

        Word newWord = (Word)DeepCopy.copy(word);
        String newKey = word.getKey();
        int index = find(newWord);

        Word oldWord = wordList.get(row);
        String oldKey = oldWord.getKey();

        Hashtable<String, Word> hash = getHash();
        String key;

        if (newKey.equals(oldKey))
        {  
           wordList.set(row, newWord);
        
           key = newWord.getKey();
           hash.put(key, newWord);
           return "";
        }

        if (newKey != null && newKey.trim().length()>0 && index>=0)
            return "The edited word " + newKey + " is already on file";

    	try
    	{
	        // Adjust the sorted list
	        index = find(oldWord);
	        sortedList.remove(index);  // Adjust the sorted list
	        insert(newWord);

	        // Adjust the hash table
	        key = oldWord.getKey();
	        hash.remove(key);
	        
	        key = newWord.getKey();
	        hash.put(key,  newWord);
	        
	        // Replace the word in the unsorted list
	        wordList.set(row, word);
	        return "";
    	}
    	catch (Exception e)
    	{
    		return e.getMessage();
    	}
   }

   /** Get the number of words on file. */
   public int getWordCount()
   {  if (dictionaryView==null) getView();
      return wordList.size(); }
   
   public int getSize()
   {
	   return wordList.size();
   }

   /** Get the number of words in the current view. */
   public int getViewCount()
   {   if (dictionaryView==null) getView();
       return dictionaryView.size();
   }
   
   /** Sort the language alphabetically */
   public void sortLanguage()
   {  if (dictionaryView==null) getView();
      sortKeys(true);
      try
      {   SwingUtilities.invokeAndWait(new Runnable()
          {  public void run()
             {  wordList = sortedList;
             	sortedList = null;
                dictionaryView.reloadView();
             }
          });
      }
      catch (Exception  e) {}
   }

    /** Make an identical copy of this object 
     * 
     * @return The cloned Example object
     */
    @Override public Object clone()  {   return DeepCopy.copy(this);  }
    
    /** Method to compare if two language objects have the same code */
    public boolean equals(Language lang) { return compareTo(lang) == 0; }
    
    /** Method to enable sorting and collections methods 
     * 
     * @param obj method to compare to
     * @return +1 if this > other, 0 if equal, -1 if less
     */
    public int compareTo(Language language)
    {
        return getLanguageCode().compareTo(language.getLanguageCode());
    }
    
    /** Create a sorted list if it doesn't exist */
    private void sortKeys(boolean always)
    {  
    	if (sortedList==null || always)
    	{
	    	sortedList = new ArrayList<Word>();
	    	for (int i=0; i<wordList.size(); i++)
	    	{
	    		sortedList.add((Word)wordList.get(i).clone());
	    	}
	        Collections.sort(sortedList);
    	}
    }

    /** Find the index of a word key in the dictionary
     * 
     * @param key The key for the word
     * @return The row index (or -1 if not found)
     */
    public int find(String key)
    {
    	Word word;
    	for (int w=0; w<wordList.size(); w++)
    	{
    		word = wordList.get(w);
    		if (word.getKey().equals(key))
    			return w;
    	}
    	return -1;
    }

    /** Find a word from the sorted list */
    private int find(Word key)
    {   
    	sortKeys(false);  // sort, but only if necessary.
        
        int top = -1, bottom = sortedList.size(), middle = (top + bottom)/2;
        Word node;
        while (top + 1 < bottom)
        {  
           node = sortedList.get(middle);
           int value = compareTo(node, key);
           if (value > 0) bottom = middle;
           else if (value < 0) top = middle;
           else return middle;
            
           middle = (top + bottom) / 2;
        }
        return -1;        
    }
    
    /** Compare two Word objects (return <0, 0, >0 if <, =, or >. */
    public int compareTo(Word source, Word word)
    {  
       String key = source.getKey().toLowerCase();
       String wKey = word.getKey().toLowerCase();
       return compare(key, wKey);
    }

    
    /** Insert a word into the sorted list */
    private void insert(Word word)
    {   
    	sortedList.add(word);
        Word node;
        int index = sortedList.size() - 2;
        while (index >= 0)
        {  node = sortedList.get(index);
           if (node.compareTo(word)<=0) 
           {  sortedList.set(index+1, word);
              return;
           }
           sortedList.set(index+1, node);
           index--;
        }
        sortedList.set(0, word);
    }

   /** Create the XML that represents this language */
   public Element exportXML(Document doc, File file, int count, int wordCount) throws IOException
   {
	   return exportXML(doc, file, false, count, wordCount);
   }

    /** Create the XML that represents this language
     * 
     * @param doc The XML document being created
     * @param file The file to which the file is being written
     * @param view false = entire dictionary, true = current view
     * @return The language XML element with all the children
     * @return The number of words exported so far
     * @return The total number of words to export
     * @throws IOException
     */
   public Element exportXML(Document doc, File file, boolean view, int count, int wordCount) throws IOException
   {  
	  Element element = doc.createElement("language");

      String splitCode[] = parseLanguage(code);

      String code = splitCode[LANG];
      if (splitCode[DIALECT].length() > 0)
      {
    	  code += "/" + splitCode[DIALECT];
      }
      element.setAttribute("lang", code);
      
      if (splitCode[DIALECT].length()>0) 
    	  element.setAttribute("variant", splitCode[DIALECT]);

      if (splitCode[SORT_ORDER].length()>0)
    	  element.setAttribute("sort", splitCode[SORT_ORDER]);
      
      String languageName = languageFont.getLanguage();
      element.setAttribute("name", languageName);

      Font font = languageFont.getFont();
      element.setAttribute("face", font.getName());
      element.setAttribute("size", "" + font.getSize());

      Element words = doc.createElement("words");
      element.appendChild(words);

      Word word;
      double percent = count * 100.0 / wordCount;
      String progress;
      for (int i=0; i<wordList.size(); i++)
      {  
    	 if (view) 
    	 {
    	     if (dictionaryView==null || !dictionaryView.isInView(i)) continue;

    		 word = getWord(i); // append if word is in the view
    		 if (word == null) continue;
    	 }
    	 else word = wordList.get(i);
         words.appendChild(word.exportXML(doc, file));
         
         percent = ++count * 100.0 / wordCount;
         progress = String.format("Progress = %7.3f", percent);
         if (i%10==0)
        	 getErr().setText(progress + "%");
      }
      return element;
   }

   /** Create the XML that represents this language
    * 
    * @param node The node containing a language element
    * @param file The file to which the file is being written
    * @return The number of words exported so far
    * @return The total number of words to export
    * @throws SAXException, IOException
    */
   public void importXML(Element node, File file, int count, int wordCount)
                                                throws SAXException, IOException
   {   
	   code = node.getAttribute("lang");
       if (code.length()==0) throw new SAXException();

       String variant = node.getAttribute("variant");
       String sortOrder = node.getAttribute("sort");
       
       if (code.length()>3 && !code.substring(4).equals(variant))
    	   throw new SAXException();
       else if (code.length()>3) 
    	   code = code.substring(0, 3);
       
       if (sortOrder.length()>0) code = code + "/" + variant + "/" + sortOrder;
       else if (variant.length()>0) code = code + "/" + variant;
       
       String face = node.getAttribute("face");
       String attSize = node.getAttribute("size");
       String name = node.getAttribute("name");
       int size = 12;
       if (attSize.length()>0) size = Integer.parseInt(attSize);
       languageFont = new LanguageFont(face, size, name);

       NodeList list = node.getElementsByTagName("word");
       Element element;
       Word word;
       double percent = count * 100.0 / wordCount;
       String progress;
       int len = list.getLength();
       for (int i=0; i<len; i++)
       {  try
          {   element = (Element)list.item(i);
              word = new Word();
              word.importXML(element, file);
              mergeWord(word);

              percent = ++count * 100.0 / wordCount;
              progress = String.format("Progress = %7.3f", percent);
              if (i%10==0)
             	 getErr().setText(progress + "%");
          }  catch(Exception e) {}
       }
   }
 
   /** add imported word to the word list
    * 
    * @param word The word object to insert
    */
   public void addWord(Word word)
   {
	   wordList.add(word);
	   
	   Hashtable<String, Word> hash = getHash();
	   String key = word.getKey();
   	   hash.put(key,  word);
   }
   
   /** Extract language, dialect, and sort order from code
    *  Format: lll/dd/sss... or lll//sss... or lll or lll/dd
    *  @param code String combining language, dialect, and sort order
    *  @return array [0] langguage, [1] dialect, [2] sort order
    */
   private String[] parseLanguage(String code)
   {
      String[] result = {"", "", ""};
      
      int index = code.indexOf("/");
      if (index == -1) {
    	 
         result[LANG] = (code.length() > 3) ? code.substring(0,3) : code;
         return result;
      }
      else result[LANG] = code.substring(0, (index>3) ? 3 : index);
      
      code = code.substring(index+1);
      index = code.indexOf("/");
      if (index == -1) {
         result[DIALECT] = (code.length() > 2) ? code.substring(0,2) : code;
         return result;
      }
      else result[DIALECT] = code.substring(0, (index>2) ? 2 : index);
      
      result[SORT_ORDER] = code.substring(index+1);
      return result;
   }
   
   /** Combine array of language, dialect, sort order
    *  @param fields [0] language, [1] dialect, [2] sort order
    *  @return String lll/dd/sss... or lll//sss... or lll or lll/dd
    */
   private String combineLanguageFields(String[] fields)
   {
      if (fields[LANG].length() == 0) return "";
      if (fields[SORT_ORDER].length() != 0)   
        return fields[LANG] + "/" + fields[DIALECT] + "/" + fields[SORT_ORDER];
      
      if (fields[DIALECT].length() !=0)
         return fields[LANG] + "/" + fields[DIALECT];
      
      return fields[LANG];
   }


   /** Initialize the language fields and sort order hash table
    * 
    * @param code Language, dialect, and custom sort order
    */
   private void initializeData(String code)
   {
	   if (sortMap == null)
	   {
		   sortMap = new HashMap<Character, Integer>();
	   }
	   else sortMap.clear();
	   
	   languageFields = parseLanguage(code);
	   
	   String sortOrder = languageFields[SORT_ORDER];
	   for (int i=0; i<sortOrder.length(); i++)
	   {
		   sortMap.put(sortOrder.charAt(i), i);
	   }
   }
   
   /** Compare two strings using the appropriate language sort order
    * 
    * @param first  The source string to compare
    * @param second The string to compare to
    * @return <0 if first<second, =0 if first=second, >0 if first>second
    */
   public int compare(String first, String second)
   {
      if (sortMap==null) initializeData(code);
      
      int size = Math.min(first.length(), second.length());
      Integer indexF, indexS;
      char charF, charS;
      for (int i=0; i<size; i++)
      {
         charF = first.charAt(i);
         charS = second.charAt(i);
         indexF = sortMap.get(charF);
         indexS = sortMap.get(charS);
         if (indexF == null && indexS == null)
         {
            int result = Character.toUpperCase(charF)
                  - Character.toUpperCase(charS);
            if (result != 0) return result;
         
            result = charF - charS;
            if (result == 0) continue;
            return result;
         }
         if (indexF == null) return +1;
         if (indexS == null) return -1;
         if (indexF != indexS)
            return (indexF.compareTo(indexS));
      }
      return first.length() - second.length();
   }
   
   /** Retrieve the root dictionary panel */
   private RootDictionaryPanel getRootDictionaryPanel()
   {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
               .getPropertyChangeListeners("DictionaryListeners");

       return (RootDictionaryPanel)pcl[0];
   }
   
  protected JLabel getErr()
   {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
       return rootPanel.getErrorLabel();
   }
  
  public boolean equals(Object o)
  {
	  if (o instanceof Language)
	  {
		  if (((Language) o).getLanguageCode().equals(getLanguageCode()))
			  return true;
	  }
	  return false;
  }


}       // End of Language class
