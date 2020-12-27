/*
 * Translation.java
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.lib.DeepCopy;

/** Gloss, phonetics, and arrays of indigenous text for dictionary components */
public class Translation extends Item implements Serializable, Cloneable
{  private static final long serialVersionUID = 1;

   public String gloss, phonetics;
   private Hashtable<String,String> data;

   /** Constructor to initialize gloss, phonetic, and translation data */
   public Translation(ArrayList<Language> langs)
   {  super();
      data = new Hashtable<String,String>();
      gloss = phonetics = "";

      for (int i=0; i<langs.size(); i++)
      {  
    	  setIndigenousData(langs.get(i), "");  
      }
   }

   public Translation(Translation translation)
   {  super(translation);
      data = new Hashtable<String,String>();
      gloss = phonetics = "";
   }

   /** Constructor to initialize gloss, phonetic, without translation data */
   public Translation()
   {  
	  data = new Hashtable<String,String>();
      gloss = phonetics = "";
   }
   
   /** Return the hash table of languages and codes */
   public Hashtable<String, String> getIndigenousData()
   {
	   return new Hashtable<String,String>(data);
   }
   
   /** Store the translation data into the translation table */
   public void setIndigenousData(Hashtable<String, String> data)
   {
	   this.data = new Hashtable<String,String>(data);
   }

   /** Get the gloss (primary language) data */
   public String getGloss() { return gloss; }

   /** Set  the gloss (primary language) data */
   public void setGloss(String g) { gloss = g; }

   /** Get the  phonetics pronunciation */
   public String getPhonetics() { return phonetics; }

   /** Set the phonetic pronunciation */
   public void setPhonetics(String p) 
   { 
	   phonetics = p; 
   }
   
   /** Set indigenous translation */
   public void setIndigenousData(String code, String d)
   { 
      data.put(code, d);
   }

   /** Set indigenous translation */
   public void setIndigenousData(Language lang, String d)
   {  String code = lang.getLanguageCode();
      data.put(code, d);
   }

   /** Replace temporary SIL markers with actual language codes
    * 
    * @param languages Array list of actual language codes
    */
   public void updateLanguageCodes(ArrayList<String> languages)
   {
	   String value, key;
	   int index;
	   
	   ArrayList<Map.Entry<String, String>> copy = new ArrayList<Map.Entry<String,String>>(data.entrySet());
	   for (Map.Entry<String, String> entry : copy)
	   {
		   key = entry.getKey();
		   value = entry.getValue();
		   index = SIL_CODES.indexOf(key.charAt(key.length()-1));

		   if (key.charAt(0) == '~')
		   {
			   data.remove(key);

			   if (index>=0 && languages.get(index).length() == LANGUAGE_CODE_SIZE)
				   data.put(languages.get(index), value);
		   }
	   }
   }
   

   /** Set indigenous translation */
   public ArrayList<String> getIndigenousData(ArrayList<Language> langs)
   {   Language language;
       String key, value;

       ArrayList<String> translation = new ArrayList<String>();
       for (int i=0; i<langs.size(); i++)
       {  language = langs.get(i);
          key = language.getLanguageCode();
          if (key!=null)   
          {   value = data.get(key);
              if (value==null) value = "";
              translation.add(value);
          }
          else             
          {
        	  translation.add("");
          }
       }
       return translation;
   }

   public @Override Object clone()  
   {  
	   return DeepCopy.copy(this);   
   }
   
   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc, File file)
   {  
	  ArrayList<String[]> attributes = new  ArrayList<String[]>();

      attributes.add(new String[]{"phonetics", phonetics} ); 
      String title = data.get("title");
      if (title!= null && title.length()>0)
    	  attributes.add(new String[]{"title", title} );

      Element node = makeNode(doc, "translations", attributes);
      exportStyle(node);
      node.appendChild(doc.createTextNode(gloss));
      
      Enumeration<String> keys = data.keys();
      String key, value;
      Element translation;
      attributes = new ArrayList<String[]>();
      while (keys.hasMoreElements())
      {  
    	 key = keys.nextElement().trim();
         value = data.get(key).trim();
         if (key.equals("title")) 
        	 continue;

         if (key.length()>0 && value.length()>0)
         {   
        	 attributes.clear();
             attributes.add(new String[]{"lang", key});
             translation = makeNode(doc, "translation", attributes);
             translation.setTextContent(value);
             node.appendChild(translation);  
         }
      }
      return node;
   }
   
   /** Configure this dictionary element based on XML specifications */
   public void importXML(Element node, File file)
   {   
	   importStyle(node);
       
       gloss = getTextContent(node).trim();
       if (gloss.endsWith(","))
    	   gloss = gloss.substring(0,gloss.length()-1);
       phonetics = node.getAttribute("phonetics");
       String title = node.getAttribute("title");
       data.put("title", title);
       
       NodeList list = node.getElementsByTagName("translation");
       Element  element;
       String key, value;
       for (int i=0; i<list.getLength(); i++)
       {  try
          {   element = (Element)list.item(i);
              key = element.getAttribute("lang").trim();
              value = element.getTextContent().trim();
              if (key.length()>0) data.put(key, value);
          }  catch (Exception e) {}
       }
   }


   /** Get the text content of an element (without traversing the child nodes */
   private String getTextContent(Element element)
   {  
	  NodeList list = element.getChildNodes();
      if (list.getLength()==0) return "";

      String value = list.item(0).getNodeValue();
      if (value==null) return "";
      return value;
   }

   /** Compare two translation objects and return true if equal */
   public @Override boolean equals(Object object)
   {  
	  if (object instanceof Translation)
      {   
		  Translation translation = (Translation)object;

          if (!super.isEqual(translation)) return false;
          if (!gloss.equals(translation.gloss)) return false;
          if (!phonetics.equals(translation.phonetics)) return false;
          if (!data.equals(translation.data)) return false;
          return true;
      }
      return false;
   }

   public @Override int hashCode()
   {   return (gloss+phonetics).hashCode() + data.hashCode() + super.hashCode();
   }

}      // End of Translation class
