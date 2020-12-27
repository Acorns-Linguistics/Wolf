/*
 * Reference.java
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

/** References to words in other languages */
public class Reference extends Item implements Serializable, Cloneable
{  private static final long serialVersionUID = 1;

   Hashtable<String, String> data;

   public Reference()
   { 
	 super();
     data = new Hashtable<String, String>();
     setTitle("References");
   }
   
   public Reference(String title)
   {
	   this();
	   if (title.length()>0) setTitle(title);
   }

   public Reference(Reference reference)
   {  super(reference);
      data = new Hashtable<String, String>();
      setTitle(reference.getTitle());

   }

   @Override
   public String getTitle()
   {
	   String title = data.get("title");
	   if (title==null) 
	   {
		   title = "References";
		   setTitle(title);
	   }
	   return data.get("title");
   }
   
   public void setTitle(String value)
   {
	   data.put("title", value);
   }

   /** Set indigenous translation */
   public void setIndigenousData(Language lang, String d)
   { 
	  String code = lang.getLanguageCode();
      data.put(code, d);
   }

   /** Store indigenous translation into the data 
    * 
    * @param code Lanaguage code
    * @param translation The translation data
    */
   @Override
   public void setIndigenousData(String code, String translation)
   {
	   data.put(code,  translation);
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

   
   /** Get indigenous translation */
   public ArrayList<String> getIndigenousData(ArrayList<Language> langs)
   {   Language language;
       String key;

       ArrayList<String> translation = new ArrayList<String>();
       for (int i=0; i<langs.size(); i++)
       {  language = langs.get(i);
          key = language.getLanguageCode();
          if (key!=null)   translation.add(data.get(key));
          else             translation.add("");
       }
       return translation;
   }

   /** Return the hash table of languages and codes */
   @Override
   public Hashtable<String, String> getIndigenousData()
   {
	   return new Hashtable<String,String>(data);
   }
   
   /** Store the translation data into the translation table */
   @Override
   public void setIndigenousData(Hashtable<String, String> data)
   {
	   this.data = new Hashtable<String,String>(data);
   }

   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc, File file)
   {  
	  ArrayList<String[]> attributes = new  ArrayList<String[]>();
      String title = data.get("title");
      if (title!= null && title.length()>0)
    	  attributes.add(new String[]{"title", title} );
 
      Element node = makeNode(doc, "translations", attributes);
      exportStyle(node);

      Enumeration<String> keys = data.keys();
      String key, value;
      Element translation;
      attributes = new ArrayList<String[]>();
      while (keys.hasMoreElements())
      {  key = keys.nextElement().trim();
         value = data.get(key).trim();
         if (key.equalsIgnoreCase("title")) continue;
         
         if (key.length()>0 && value.length()>0)
         {   attributes.clear();
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
   {   importStyle(node);

       NodeList list = node.getElementsByTagName("translation");
       String title = node.getAttribute("title");
       data.put("title", title);
       Element  element;
       String key, value;
       for (int i=0; i<list.getLength(); i++)
       {  try
          {   element = (Element)list.item(i);
              key = element.getAttribute("lang").trim();
              value = element.getTextContent().trim();
              if (key.length()>0) data.put(key, value);
          } catch (Exception e) {}
       }
   }

   public @Override Object clone()  {  return DeepCopy.copy(this);   }

   /** Compare two translation objects and return true if equal */
   public @Override boolean equals(Object object)
   {  if (object instanceof Reference)
      {   Reference reference = (Reference)object;

          if (!super.isEqual(reference)) return false;
          if (!data.equals(reference.data)) return false;
          return true;
      }
      return false;
   }


   public @Override int hashCode()
   {  return data.hashCode() + super.hashCode();  }


}  // End of Reference class
