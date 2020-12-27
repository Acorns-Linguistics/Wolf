/**
 * LanguageCodes.java
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
package org.wolf.lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.acorns.language.LanguageText;

public class LanguageCodes 
{
   // The IPA 639-3 language code list
   static String[] codes;
   static JComboBox<String> combo;
   
   public static JComboBox<String> getJCombo()
   {
       Vector<String>codeVector = new Vector<String>();
       codeVector.add(LanguageText.getMessage("dictionary", 1));
       
       if (combo!=null) return combo;
       
       String fileName = "/resources/iso_Name_Index.tab";
       String line;
       try
       {
           URL url = LanguageCodes.class.getResource(fileName);
           BufferedReader read
                  = new BufferedReader(new InputStreamReader(url.openStream()));

           read.readLine();     // Skip header line.
           line = read.readLine().trim();
           String[] fields;
           while (line !=null)  
           {   fields = line.split("\t");
               if (fields.length==3)
               {  line = fields[0] + " " + fields[1];
                  if (line.length()>30) line = line.substring(0,30).trim();
                  codeVector.add(line);
               }
               line = read.readLine();
           }
           read.close();
           codes = (String[])codeVector.toArray(new String[codeVector.size()]);
       }
       catch (Exception e)
       { return null; }
       combo = new JComboBox<String>(codes);
       return combo;
   }

   /** Get the language name of a particular ISO language code */
   public static String getName(String code)
   {
	   JComboBox<String> comboBox = getJCombo();
	   
       ComboBoxModel<String> model = comboBox.getModel();
       int size = model.getSize();
       String element;
       
       for(int i=0;i<size;i++) 
       {
           element = model.getElementAt(i);
           if (element.substring(0, 3).equalsIgnoreCase(code))
        	   return element.substring(4);
       }
       return "";
   }
   
   
   
}   // End of LanguageCodes class
