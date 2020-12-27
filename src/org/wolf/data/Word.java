/*
 * Word.java
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;

/** Dictionary word */
public class Word extends Group 
                   implements Serializable, Comparable<Word>, Cloneable, Transferable
{  private static final long serialVersionUID=1L;

   public static final DataFlavor WORD_FLAVOR
                                       = new DataFlavor(Word.class, "TreePath");

   /** Constructor to set up word unit components and key
    * 
    * @param key The word key for dictionary searching
    */
   public Word(String key)
   {
	   this();
	   setKey(key);
   }
   
   /** Constructor to set up word unit components */
   public Word()  
   {  
	   super("Word");  
   }

   /** Constructor to create a word with same structure as another */
   public Word(Word oldWord)
   {  super(oldWord);  }

   /** Get the key value for a dictionary word object */
   public String getKey()
   {  return media.getTranslationData().getGloss();  }

   /** Set the key value for a dictionary word */
   public void setKey(String key)
   { media.getTranslationData().setGloss(key); }
   
   /** Set the phonetics for a dictionary word */
   public void setPhonetics(String phonetics)
   { 
	   media.getTranslationData().setPhonetics(phonetics);
   }

    /** Compare two Word objects */
    public int compareTo(Word o)
    {  
       if (o instanceof Word)
       {   Word w = (Word)o;
           String key = getKey().toLowerCase();
           String wKey = w.getKey().toLowerCase();
           
           Language language 
                = getRootDictionaryPanel().getDictionaryData().getActiveLanguage();
           return language.compare(key, wKey);
       }
       return 0;
    }

    /** Get supported data flavors */
    public DataFlavor[] getTransferDataFlavors()
    {   DataFlavor[] flavors = {WORD_FLAVOR};
        return flavors;
    }

    /** Determine if the transferable data type is supported */
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {   return flavor.equals(WORD_FLAVOR); }

    /** Get transferable object
     *
     * @param flavor The transferable data type
     * @return The transferable object
     * @throws UnsupportedFlavorException
     */
    public Object getTransferData(DataFlavor flavor)
                                            throws UnsupportedFlavorException
    {   if (flavor.equals(WORD_FLAVOR))  return this;
        else throw new UnsupportedFlavorException(flavor);
    }

    public @Override boolean equals(Object object)
    {  if (object instanceof Word)  { return super.equals((Word)object);  }
       return false;
    }

    public @Override int hashCode()  {  return super.hashCode(); }

}  // End of Word class
