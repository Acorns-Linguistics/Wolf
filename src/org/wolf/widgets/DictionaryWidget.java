/*
 * DictionaryWidget.java
 *    This class represents components for displaying word, definition, and
 *        examples contained in a dictionary
 *
 *   @author  Harveyd
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.data.Translation;
import org.wolf.data.Unit;

/** GUI component for entering words, definitions, and examples */
public class DictionaryWidget extends TemplateWidget
{
	private static final long serialVersionUID = 1L;
	private JTextField   topField;
    private ArrayList<JTextField> languageFields;
    private MediaWidget  media;
    private String title;
    private JPanel centerPanel, topPanel;

   /** Create a dictionary word GUI object
    *
    * @param panelWidth desired width of this panel in pixels
    */
   public DictionaryWidget(int panelWidth, String title)
   {   
	   super("","");
   
       this.title = title;
       
       if (title!=null && !title.equals("Word") && !title.equals("Definition"))
            setBorder(BorderFactory.createTitledBorder(title));
       else setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

       setLayout(new BorderLayout());
       topPanel = new JPanel();
       topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
       media = new MediaWidget(getEnv(), title);
       topField = new JTextField("");
       topField.setName("translate");
       topPanel.add(topField);
       topPanel.add(media);

       add(topPanel, BorderLayout.NORTH);
       Insets insets = getInsets();
       panelWidth -= (insets.left + insets.right);
       centerPanel = new JPanel();
       centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
       add(centerPanel, BorderLayout.CENTER);
       initializeFields(panelWidth);
   }
   
   public String getTitle() { return title; }

   /** Update the cell data after user interaction */
   public Item updateCell()
   {
	   Unit unit = media.updateCell();
       storeCellCharacteristics(unit);

       Dimension size = getSize();
       Insets insets = getInsets();
       size.width -= (insets.left + insets.right);
       unit.setSize(size);

       Translation translation = unit.getTranslationData();
       translation.setGloss(getPhoneticText(this, false));
       translation.setPhonetics(getPhoneticText(this, true));

       String text;
       ArrayList<Language> languages = getDict().getListOfLanguages();
       for (int i=0; i<languageFields.size(); i++)
       {   
    	   text = languageFields.get(i).getText();
           if (i>=languages.size()) break;
           translation.setIndigenousData(languages.get(i), text);
       }
       return unit;
   }

   /** Format the cell with user data */
   public void formatCell(Item item)
   {  
	  Unit unit = (Unit)item;
	  
	  this.title = item.getTitle();
      if (title!=null && !title.equals("Word") && !title.equals("Definition"))
          setBorder(BorderFactory.createTitledBorder(title));
      else setBorder(BorderFactory.createEmptyBorder(0,0,20,0));

      setFont(item.getFont());
      item.setBackground(getBackground());
      item.setForeground(getForeground());
      media.formatCell(unit);


      Dimension size = item.getSize();
      initializeFields(size.width);

      Translation translation = unit.getTranslationData();
      resetText(translation.getGloss(), translation.getPhonetics());
      setPhoneticComponent(getToggleComponent());

      ArrayList<Language> languages = getDict().getListOfLanguages();
      ArrayList<String> languageData = translation.getIndigenousData(languages);
      JTextField field;
      Language language;
      int i = 0;
      for (i=0; i<languageFields.size(); i++)
      {   
    	  field = languageFields.get(i);
    	  language = languages.get(i);
    	  language.hookLanguage(field);
    	  field.setText(languageData.get(i)); 
      }      
   }
 
   /** return the component that displays phonetics and indigenous text */
   public JTextField getToggleComponent() { return topField; }

   /** Format the widget language components
    *  panelWidth desired width of the panel
    */
   private void initializeFields(int panelWidth)
   {  ArrayList<Language> languages = getDict().getListOfLanguages();
      if (languageFields==null)  languageFields = new ArrayList<JTextField>();

      // If too many text fields, remove the excess
      int count = languageFields.size();
      for (int i=languages.size(); i<count; i++)
      {   languageFields.remove(0); }

      // Make sure there are enough text fields
      for (int i=languageFields.size(); i<languages.size(); i++)
      {   languageFields.add(new JTextField("")); }

      // Configure the size, font and tool tip
      String tooltip;
      Language language;
      JTextField field;
      Font font;
      Dimension mediaSize = media.getPreferredSize();

      int widgetHeight = mediaSize.height;
      int fieldHeight = topField.getFontMetrics(topField.getFont()).getHeight() + 3;
      if (title.equals("Word"))
    	  fieldHeight += 1;
      if (widgetHeight<fieldHeight) widgetHeight = fieldHeight;
      Dimension fieldSize = new Dimension(panelWidth, fieldHeight);
      
      for (int i=0; i<languageFields.size(); i++)
      {   
    	  field = languageFields.get(i);
    	  language = languages.get(i);
          tooltip = "language: " + language.getLanguageCode();
          field.setToolTipText(tooltip);
          font = languages.get(i).getFont();
          field.setFont(font);
          language.hookLanguage(field);
          field.setSize(fieldSize);
          field.setPreferredSize(fieldSize);
          widgetHeight +=fieldHeight;
      }

       // Add the components to the widget
      count = getComponentCount();
      centerPanel.removeAll();
      for (int i=0; i<languageFields.size(); i++)
      {   centerPanel.add(languageFields.get(i));
      }
  
      // Adjust the size of this widget
      Insets insets = getInsets();

      panelWidth += insets.left + insets.right;
      widgetHeight += insets.top + insets.bottom;
      
      if (title.equalsIgnoreCase("Example"))
    	  widgetHeight -= 12;
 
      Dimension size = new Dimension(panelWidth, widgetHeight);
      setSize(size);
      setPreferredSize(size);
   }

   /** Update the cell size based on the user interface */
   public void setCellWidth(Integer width)
   {  initializeFields(width);

   }

   public boolean isMedia(File file)
   {  return media.isMedia(file); }

   public void mediaDropped(File file) throws Exception
   { media.mediaDropped(file); }

}  // End of DictionaryWidget class
