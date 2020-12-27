/*
 * ReferenceWidget.java
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

package org.wolf.widgets;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.wolf.data.Constants;
import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.data.Reference;

/** Reference that links definitions to words in other languages */
public class ReferenceWidget extends TemplateWidget implements Constants
{   
	private static final long serialVersionUID = 1L;
	private ArrayList<JTextField> languageFields;
	private String title;


    /** Constructor to initialize the widget
    *
    * @param panelWidth width of widget in pixels
    */
   public ReferenceWidget(int panelWidth, String title)
   {  super("", "");

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      Insets insets = getInsets();
      panelWidth += (insets.left + insets.right);

      initializeFields(panelWidth);
      
      Border border = BorderFactory.createTitledBorder(title);
      setBorder(border);
   }
   
   /** Update the cell data after user interaction */
   public Item updateCell()
   {   
	   Border border = BorderFactory.createTitledBorder(title);
	      setBorder(border);
	      
	   Reference reference = new Reference();
       storeCellCharacteristics(reference);
       reference.setTitle(title);
       if (!languageFields.isEmpty()) 
     	  linkListener(languageFields.get(0), title);
       
       Language active = getDict().getActiveLanguage();
       ArrayList<Language> languages = getDict().getListOfLanguages();
       languages.add(0, active);
       String text;
       for (int i=0; i<languageFields.size(); i++)
       {   text = languageFields.get(i).getText();
           if (i>=languages.size()) break;
           reference.setIndigenousData(languages.get(i), text);
       }
       return reference;
   }

   /** Format the cell with user data */
   public void formatCell(Item item)
   {  
	  Reference reference = (Reference)item;
	  
      title = reference.getTitle();
      
      Border border = BorderFactory.createTitledBorder(title);
      setBorder(border);
      
      loadCellCharacteristics(reference);
      
      Language active = getDict().getActiveLanguage();
      ArrayList<Language> languages = getDict().getListOfLanguages();
      languages.add(0, active);
      ArrayList<String> languageData = reference.getIndigenousData(languages);

      initializeFields(getSize().width);
      for (int i=0; i<languageFields.size(); i++)
      {   languageFields.get(i).setText(languageData.get(i));  }
      
      if (!languageFields.isEmpty()) 
     	  linkListener(languageFields.get(0), title);
   }

   /** Format the widget language components
    *
    * @param panelWidth The width of the panel
    */
   private void initializeFields(int panelWidth)
   {   Language active = getDict().getActiveLanguage();
       ArrayList<Language> languages = getDict().getListOfLanguages();
       languages.add(0, active);
       if (languageFields==null)  languageFields = new ArrayList<JTextField>();

      // If too many text fields, remove the excess
      int count = languageFields.size();
      for (int i=languages.size(); i<count; i++)
      {   languageFields.remove(0); }

      // Make sure there are enough text fields
      for (int i=languageFields.size(); i<languages.size(); i++)
      {   languageFields.add(new JTextField("")); }

      // Configure the size, font and tooltip
      String tooltip;
      JTextField field;
      Font font;
      FontMetrics metrics;
      Language language;

      Dimension size = new Dimension(getSize().width, 0);
      int widgetHeight = 0;
      for (int i=0; i<languageFields.size(); i++)
      {   field = languageFields.get(i);
          tooltip = "language: " + languages.get(i).getLanguageCode();
          field.setToolTipText(tooltip);
          language = languages.get(i);
          font = languages.get(i).getFont();
          field.setFont(font);
          language.hookLanguage(field);
          metrics = field.getFontMetrics(field.getFont());
          size.height = metrics.getHeight() + GAP;
          widgetHeight += size.height;
          field.setPreferredSize(size);
      }

      // Add the components to the widget
      removeAll();
      for (int i=0; i<languageFields.size(); i++)
      {   add(languageFields.get(i));  }

      // Set the size of this widget
      size = new Dimension(panelWidth,widgetHeight);
      Insets insets = getInsets();
      size.height +=  (insets.top + insets.bottom);
      setSize(size);
      setPreferredSize(size);
      setMinimumSize(size);
      setMaximumSize(size);
   }

   /** Update the cell size based on the user interface */
   public void setCellWidth(Integer width)
   {  initializeFields(width); }


}     // End of ReferenceWidget class
