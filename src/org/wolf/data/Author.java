/*
 *   class Author.java
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

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wolf.lib.DeepCopy;

/** This class defines an author that contributed to a particular dictionary */
public class Author implements Serializable, Cloneable
{
    private static final long serialVersionUID=1L;
    private static final String[] fieldKeys =
                    {"org", "url", "email", "langs", "initials", "name"};

    /** The author's organization or tribe */
    public final static int ORG    = 0;
    
    /** The URL of the author's web site */
    public final static int URL    = 1;
    
    /** The author's e-mail address */
    public final static int EMAIL  = 2;
    
    /** The list of languages (separated by commas) contriubed by this author */ 
    public final static int LANG   = 3;
    
    /* The author's initials */ 
    public final static int INIT   = 4;
    
    /** The authr's name */
    public final static int NAME   = 5;
    
    /** The number of fields in each author record */
    public final static int FIELDS = 6;
    
    private String[] fields;      // The list of author related information
    
    /** Constructor to initialize an author object */
    public Author()  { initFields(); }
    
    /** Constructor to initialice an author object and set all fields 
     * 
     * @param data the array of author's data to store
     */
    public Author( String[] data) {  setFields(data); }
 
    /** Method to set all Author object fields in a single call
     * 
     * @param data An array containing all author data
     */
    public void setFields(String[] data)
    {   initFields();
        if (data == null) return;
        
        int length = data.length;
        if (data.length > FIELDS) length = FIELDS;
        for (int f=0; f<length; f++) 
        {  if (fields[f]!=null) fields[f] = data[f]; }
    }
    
    /** Method to set a particular field
     * 
     * @param field  The data to store
     * @param fieldNo The number of the field to store
     */
    public void setField(String field, int fieldNo)
    {   if (fieldNo<0 || fieldNo>fields.length)
            throw new IndexOutOfBoundsException();
        if (field!=null) fields[fieldNo] = field;
        else             fields[fieldNo] = "";
    }
    
    /** Method to get a particular field
     * 
     * @param fieldNo The number of the field
     * @return The data corresponding to this field
     */
    public String getField(int fieldNo)   
    {  
        if (fieldNo<0 || fieldNo>fields.length) return null;
        return fields[fieldNo]; }
    
    /** Method to get all data for this author
     * 
     * @return A String array with all of the author's data
     */
    public String[] getFields() 
    {   String[] fieldData = new String[FIELDS];
        for (int i=0; i<fieldData.length; i++)   { fieldData[i] = fields[i]; }
        return fieldData; 
    }
    
    /** Method to determine if all the author fields are blank */
    public boolean isClear()
    {
    	for (int f=0; f<fields.length; f++)
    	{
    		if (fields[f].trim().length()!=0)
    			return false;
    	}
    	return true;
    }
    
   /** Make an identical copy of this object
     *
     * @return The cloned Example object
     */
   @Override public Object clone()  {   return DeepCopy.copy(this);  }

   @Override public String toString()
   {   String spaces = "                                                  ";
       String organization = (fields[ORG] + spaces).substring(0,40);
       String url          = fields[URL];
       String email        = (fields[EMAIL] + spaces).substring(0,30);
       String languages    = (fields[LANG] + spaces).substring(0,30);
       String name         = (fields[NAME] 
                           + "(" + fields[INIT] + ")" + spaces).substring(0,30);
       return name + " " + languages + " " 
                   + organization + " " + email + " " + url + " ";
   }
   
   /* Initialize the array of author fields */
   private void initFields()
   {   fields = new String[FIELDS]; 
       for (int f=0; f<FIELDS; f++) fields[f] = "";
   }

   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc)
   {  Element element = doc.createElement("author");
      for (int i=0; i<fields.length;  i++)
      {  element.setAttribute(fieldKeys[i], fields[i].trim());  }
      return element;
   }

   /** Configure this dictionary element based on XML specifications */
   public void importXML(Element node)
   {   for (int i=0; i<fields.length;  i++)
       {  fields[i] = node.getAttribute(fieldKeys[i]).trim();  }
   }
   
   public boolean equals(Object o)
   {
	   Author author = (Author)o;
	   for (int f=0; f<fields.length; f++)
	   {
		   if (!fields[f].equals(author.fields[f]))
			   return false;
	   }
	   return true;
   }

}   // End of Author class
