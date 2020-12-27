package org.wolf.dialogs;

/**
 * TemplateDialog.java
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.wolf.data.DictionaryData;
import org.wolf.data.FormatData;

public class TemplateDialog extends JDialog 
{
	private static final int FIELD_SIZE =    150;
	private static final int FORMAT_SIZE =   100;
	private static final int SEPARATOR_SIZE =  90;
	private static final Dimension TEXT_SIZE = new Dimension(120, 20);
	private static final Dimension PANEL_SIZE = new Dimension(1024, 675);
	private static final Dimension SCROLL_SIZE = new Dimension(120, 675);
	
	private static final int GAP = 2;

	
	/** List of field types */
    private static final String[] fields = 
    {	"Select field",
    	"Annotations", 		 "Antonyms",   "Categories",  "Comments",    "Compare",
    	"Encyclopedic Info", "Etymology",  "Gloss",	      "Language Links", 
    	"Lexical Function",  "Main Entry", "Morphemes",   "Ontology",    "Refer To",       
    	"References",        "Reversals",  "Restrictions", "Spelling",   	
        "Subentry",          "Synonyms",   "Table",        "Thesaurus",     
        "Usage",             "Variants"
    };
    
	private static final int MAX_FIELDS = 8;

    /** Format for a particular field */
    private static final String[] formats = 
    {	"Normal", "Italic", "Bold",    "Black", "Blue", 
    	"Gold",   "Green",  "Magenta", "Red"  	
    };
    
    /** Possible ways to separate fields */
    private static final String[] separators = 
    {
    	"None", "()", "{}", "[]", "\"\"", "''", 
    	":",  "-",  "|",  ",",    ";", ":"
    };
    
    private static final String[] dividers =
   	{
   			",", ";", ":", "|", "-", "/"
   	};
  
    /** Indicate counting label for multiple entries */
    private static final String[] counts = 
    {
    	"None",
    	"#.", "#)", "#",
    	"a.", "a)", "a",
    	"A.", "A)", "A",
    	"i.", "i)", "i",
    	"I.", "I)", "I",
    };
    
	private static final long serialVersionUID = 1L;
	private JList<String>    list;
	private JTextField       keyField;
	private JLabel			 label;
    
    private FormatData       templates;
    private String[]		 templateKeys;
    
    private static final int C_LEN = FormatData.t.values().length;
    private JComponent[][][] components = new JComponent[C_LEN][][];
    private String[][][] values = new String[C_LEN][][];
    
    private DictionaryData   dictionary;
    private DefaultListModel<String> model;
    
    private JCheckBox definitionBox,  categoryBox, exampleBox;
    
    public TemplateDialog(JFrame root, DictionaryData dictionaryData)
    {
        super(root, true);
        setModal(true);
        
        model = new DefaultListModel<String>();
        
        dictionary = dictionaryData;
        templates = dictionary.getTemplateData();
        
        ArrayList<String> data = templates.getTemplateList();
        if (templateKeys == null) 
        	templateKeys = new String[data.size()];
        
        if (!data.isEmpty()) 
        	templateKeys = data.toArray(templateKeys);
       
        setTitle("Please enter your dictionary display formats");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     
        setSize(PANEL_SIZE);
        setPreferredSize(PANEL_SIZE);
        setMaximumSize(PANEL_SIZE);
        setLocationRelativeTo(root);
        
        // Create panel with list of file display templates
        model = new DefaultListModel<String>();
        list = new JList<String>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(
                new ListSelectionListener()
                {  public void valueChanged(ListSelectionEvent event) 
                   { 
                	 loadTemplate(); 
                   }
                }  );
        list.setBackground(new Color(208,208,208));
        list.setFont(new Font("Monospaced", Font.PLAIN, 12));

        if (templateKeys!=null)
        	for (int a=0; a<templateKeys.length; a++) model.add(a, templateKeys[a]);
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(SCROLL_SIZE);
        
        JPanel center = new JPanel();
        center.setBackground(new Color(168,168,168));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        
        components[FormatData.t.W_HDR.ordinal()] 
        		= new JComponent[1][FormatData.WORD_HEADERS.length];
        components[FormatData.t.W_FLD.ordinal()] 
        		= new JComponent[MAX_FIELDS][FormatData.FIELD_HEADERS.length];
        components[FormatData.t.D_HDR.ordinal()] 
        		= new JComponent[1][FormatData.CATEGORY_HEADERS.length];
        components[FormatData.t.D_FLD.ordinal()] 
        		= new JComponent[MAX_FIELDS][FormatData.FIELD_HEADERS.length];
        components[FormatData.t.E_HDR.ordinal()] 
        		= new JComponent[1][FormatData.CATEGORY_HEADERS.length];
        components[FormatData.t.E_FLD.ordinal()] 
        		= new JComponent[0][0];
        components[FormatData.t.C_HDR.ordinal()] 
        		= new JComponent[1][FormatData.CATEGORY_HEADERS.length];
        components[FormatData.t.C_FLD.ordinal()] 
        		= new JComponent[0][0];
        
        values[FormatData.t.W_HDR.ordinal()] 
        		= new String[1][FormatData.WORD_HEADERS.length];
        values[FormatData.t.W_FLD.ordinal()] 
        		= new String[MAX_FIELDS][FormatData.FIELD_HEADERS.length];
        values[FormatData.t.D_HDR.ordinal()] 
        		= new String[1][FormatData.CATEGORY_HEADERS.length];
        values[FormatData.t.D_FLD.ordinal()] 
        		= new String[MAX_FIELDS][FormatData.FIELD_HEADERS.length];
        values[FormatData.t.E_HDR.ordinal()] 
        		= new String[1][FormatData.CATEGORY_HEADERS.length];
        values[FormatData.t.E_FLD.ordinal()] 
        		= new String[0][0];
        values[FormatData.t.C_HDR.ordinal()] 
        		= new String[1][FormatData.CATEGORY_HEADERS.length];
        values[FormatData.t.C_FLD.ordinal()] 
        		= new String[0][0];
        
        JPanel category = makeWordPanel(MAX_FIELDS);
        center.add(category);
        JPanel definition = makeCategoryPanel("Definition", true, MAX_FIELDS, true);
        center.add(definition);
        JPanel example = makeCategoryPanel("Example", false, 0, true);
        center.add(example);
        JPanel comment = makeCategoryPanel("Comment", false, 0, false);
        center.add(comment);
        
        label = new JLabel();
        label.setForeground(new Color(255,128,128));
        label.setFont(new Font("Courier New", Font.PLAIN, 20));
        
        // Create bottom portion to hold the buttons
        JButton add = new JButton("Save");
        add.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  saveTemplate();  } });
           
              
        JButton remove = new JButton("Remove");
        remove.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  removeTemplate(); } });
 
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(80,80,80));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(label);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(add);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(remove);
        buttonPanel.add(Box.createHorizontalStrut(50));

        Container entry = getContentPane();
        entry.setLayout(new BorderLayout());
        entry.add(scroll, BorderLayout.WEST);
        entry.add(center, BorderLayout.CENTER);
        entry.add(buttonPanel, BorderLayout.SOUTH);
        
        clearComponents();
        Point point = root.getLocation();
        setLocation(point);
        setVisible(true);
    }
    
   /** Clear template data */
   private void clearComponents()
   {
	   String[] clearedData;
       for (int i=0; i<values.length; i++)
    	   for (int j=0; j<values[i].length; j++)
    	   {
    		   clearedData = new String[values[i][j].length];
    		   Arrays.fill(clearedData, "");
			   setComponentArray(components[i][j], clearedData);
    	   }
   }
   
   /** Load template data into the JPanels */
   private void loadTemplate()
   {
       String key = list.getSelectedValue();
       if (key==null) return;
       
       String[][][] template = templates.getTemplate(key);
       clearComponents();
       
   	   definitionBox.setSelected(template[FormatData.t.D_HDR.ordinal()].length != 0);
   	   categoryBox.setSelected(template[FormatData.t.C_HDR.ordinal()].length != 0);
   	   exampleBox.setSelected(template[FormatData.t.E_HDR.ordinal()].length != 0);
       
       keyField.setText(key);
       int len; 
       for (int i=0; i<template.length; i++)
       {   
    	   len = (template[i].length < components[i].length) ? template[i].length : components[i].length;
    	   for (int j=0; j<len; j++)
			   setComponentArray(components[i][j], template[i][j]);
       }
   }
   
   /** Get array of strings from array of components */
   private String[] getComponentArray(JComponent[] components)
   {
	   int len = components.length;
	   String[] values = new String[len];
	   for (int i=0; i<len; i++)
	   {
		   values[i] = getComponentValue(components[i]);
	   }
	   return values;
   }

   /** Set values into an array of components */
   private void setComponentArray(JComponent[] components, String[] values)
   {
	   int len = Math.min(components.length, values.length);
	   for (int i=0; i<len; i++)
	   {
		   setComponentValue(components[i], values[i]);
	   }
   }
   
   /** Set string from component */
   private String getComponentValue(JComponent component)
   {
	   if (component instanceof JTextField)
	   {
		   JTextField text = (JTextField)component;
		   return text.getText();
	   }
	   else if (component instanceof JCheckBox)
	   {
		   JCheckBox checkBox = (JCheckBox)component;
	   	   boolean value = checkBox.isSelected();
	   	   return (value) ? "true" : "false";
	   }
	   else if (component instanceof JComboBox)
	   {
		   @SuppressWarnings("unchecked")
		   JComboBox<String> comboBox = (JComboBox<String>)component;
		   
		  String value = (String)comboBox.getSelectedItem();
		  if (value == null) return "";
		  if (value.equals(fields[0])) return "";
		  if (value.length()>2) return value.toLowerCase();
		  return value;
	   }
	   return "";
   }

   /** Set string into component value */
   private void setComponentValue(JComponent component, String value)
   {
	   if (component instanceof JTextField)
	   {
		   JTextField text = (JTextField)component;
		   text.setText(value);
	   }
	   else if (component instanceof JCheckBox)
	   {
		   boolean selected = false;
		   if (value.equals("true")) selected = true;
		   
		   JCheckBox checkBox = (JCheckBox)component;
	   	   checkBox.setSelected(selected);
	   }
	   else if (component instanceof JComboBox)
	   {
		   @SuppressWarnings("unchecked")
		   JComboBox<String> comboBox = (JComboBox<String>)component;
		   if (value.length()>0)
		   {
			   value = value.substring(0,1).toUpperCase() + value.substring(1);
			   String[] values = value.split(" ");
			   if (values.length>1 && values[1].length()>0)
			   {
				   values[1] = values[1].substring(0, 1).toUpperCase() + values[1].substring(1);
				   value = values[0] + " " + values[1];
				   
			   }
			   comboBox.setSelectedItem(value);
		   }
		   else
			   comboBox.setSelectedIndex(0);
	   }
   }
   
   /** Store template data into the template array */
   private void saveTemplate()
   {
	   String template = keyField.getText();

	   int lenI = values.length, lenJ, lenK;
	   String[][][] newValues = new String[lenI][][];
	   for (int i=0; i<lenI; i++)
	   {
		   lenJ = values[i].length;
		   newValues[i] = new String[lenJ][];
		   for (int j=0; j<lenJ; j++)
		   {
			   lenK = values[i][j].length;
			   newValues[i][j] = new String[lenK];
			   for (int k=0; k<lenK; k++)
				   values[i][j][k] = values[i][j][k];
		   }
	   }
	   
	   if (template.length()==0)
	   {
		   label.setText("Please enter a valid template name");
		   return;
	   }
	   
	   // Update or store to the list of templates.
       for (int i=0; i<newValues.length; i++)
    	   for (int j=0; j<newValues[i].length; j++)
			   newValues[i][j] = getComponentArray(components[i][j]);
 
   	   if (!definitionBox.isSelected())
   	   {
   		   newValues[FormatData.t.D_HDR.ordinal()] 
   				   = newValues[FormatData.t.D_FLD.ordinal()] 
   						   = new String[0][0];
   	   }
   	   if (!categoryBox.isSelected())
   	   {
   		   newValues[FormatData.t.C_HDR.ordinal()] 
   				   = newValues[FormatData.t.C_FLD.ordinal()] 
   						   = new String[0][0];
   	   }
   	   if (!exampleBox.isSelected())
   	   {
   		   newValues[FormatData.t.E_HDR.ordinal()] 
   				   = newValues[FormatData.t.E_FLD.ordinal()] 
   						   = new String[0][0];
   	   }

       templates.setTemplate(template, newValues);

       if (!model.contains(template))
	   {
	       int size = templateKeys.length;
	       String[] newTemplates = new String[templateKeys.length + 1];
	       if (templateKeys.length != 0)
	    	   System.arraycopy(templateKeys, 0, newTemplates, 0, size);
	       
	       newTemplates[size] = template;
	       templateKeys = newTemplates;
	       model.add(size, templateKeys[size]);
	       list.setSelectedIndex(size);
	   }
       label.setText("Display format " + template + " saved");
   }
   
   

   /** Remove the selected template from the list */
   private void removeTemplate()
   {
	   String template = keyField.getText();
	   if (template.length()==0)
	   {
		   label.setText("Please enter template name");
		   return;
	   }
	   
	   int index = list.getSelectedIndex();
       if (index<0)  return;

	   if (!templates.removeTemplate(template))
	   {
		   label.setText("Couldn't remove default template");
		   return;
	   }

       String[] newTemplates = new String[templateKeys.length - 1];
       for (int a=0; a<templateKeys.length; a++)
       {
           if (a<index)        newTemplates[a] = templateKeys[a];
           else if (a > index) newTemplates[a-1] = templateKeys[a];
       }
       templateKeys = newTemplates;
       model.removeElementAt(index);
       
       clearComponents();
       list.setSelectedIndex(-1); 
       label.setText("Display format " + template + " removed");
   }
   
   
   private JPanel makeWordPanel(int maxFields)
   {
	   int type = FormatData.t.W_FLD.ordinal();
	   
	   JPanel panel = new JPanel();
	   panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
       panel.setBackground(new Color(168,168,168));

       panel.add(new JLabel("Word"));
       panel.add(Box.createHorizontalStrut(GAP));

       JCheckBox phonetics = new JCheckBox("Phonetics");
       components[FormatData.t.W_HDR.ordinal()][0][FormatData.W_PHONETICS] = phonetics;
       
	   phonetics.setToolTipText("Show Phonetics");
	   panel.add(phonetics);
	   panel.add(Box.createHorizontalStrut(GAP));

	   JLabel label = new JLabel("Format");
	   panel.add(label);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   JComboBox<String> format = new JComboBox<String>(formats);
       components[FormatData.t.W_HDR.ordinal()][0][FormatData.W_FORMAT] = format;
	   setComboParams(format, FORMAT_SIZE);
	   format.setSelectedItem("Normal");
       format.setToolTipText("Selection controls the word display format");
       panel.add(format);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   label = new JLabel("Media");
	   panel.add(label);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   JCheckBox picture = new JCheckBox("Show");
       components[FormatData.t.W_HDR.ordinal()][0][FormatData.W_SHOW_PICTURE] = picture;
       picture.setToolTipText("Show media in the output (if present)");
	   panel.add(picture);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   label = new JLabel("Language Codes");
	   panel.add(label);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   JCheckBox language = new JCheckBox("Exclude");
       components[FormatData.t.W_HDR.ordinal()][0][FormatData.W_SHOW_LANGUAGES] = language;
       language.setToolTipText("Exclude language codes in the output");
	   panel.add(language);
	   panel.add(Box.createHorizontalStrut(GAP));
       
	   label = new JLabel("Language Separator");
	   panel.add(label);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   JComboBox<String> divider = new JComboBox<String>(dividers);
       components[FormatData.t.W_HDR.ordinal()][0][FormatData.W_SEPARATOR] = divider;
       setComboParams(divider, 50);
       divider.setToolTipText("Character to separate language text and translations");
	   panel.add(divider);
	   panel.add(Box.createHorizontalStrut(GAP));
       panel.add(Box.createHorizontalGlue());

	   JPanel category = new JPanel();
	   category.setLayout(new BoxLayout(category, BoxLayout.Y_AXIS));
       category.setBackground(new Color(168,168,168));
       
       JPanel template = new JPanel();
	   template.setLayout(new BoxLayout(template, BoxLayout.X_AXIS));
       template.setBackground(new Color(168,168,168));

       template.add(new JLabel("Template Name"));
	   template.add(Box.createHorizontalStrut(GAP));
	   keyField = new JTextField();
	   keyField.setMinimumSize(TEXT_SIZE);
	   keyField.setMaximumSize(TEXT_SIZE);
	   keyField.setPreferredSize(TEXT_SIZE);
	   keyField.setToolTipText("Title of dictionary display template");
	   template.add(keyField);
	   template.add(Box.createHorizontalGlue());

	   category.add(template);
	   category.add(Box.createVerticalStrut(3*GAP));
	   
	   category.add(panel);
	   category.add(Box.createVerticalStrut(GAP));
	   
	   if (maxFields>0)
	   {
		   JPanel fieldPanel;
		   for (int r=0; r<maxFields; r++)
		   {
			   fieldPanel = makeFieldPanel(type, r);
			   category.add(fieldPanel);
			   category.add(Box.createVerticalStrut(GAP));
		   }
		   return category;
	   }
	   return category;
   }
 
   /** Method to create a panel to hold template options
    * 			for Words, Definitions, Examples, Comments
    * 
    * @param type "Definition", "Example", or "Comment"
    * @param position If true, create a drop down for display position among fields
    * @param maxFields Maximum number of fields
    * @param showPicture display picture if present
    * @return Created panel
    */
   private JPanel makeCategoryPanel(String type, boolean position, int maxFields, boolean showPicture)
   {
	   if (maxFields==0) position = false; // Position doesn't apply if no rows
	   
	   JCheckBox typeBox = new JCheckBox(type);
	   typeBox.setToolTipText("Check to include " + type + "s in output");
	  

	   int index = FormatData.t.C_HDR.ordinal();
	   int field = FormatData.t.C_FLD.ordinal();
	   categoryBox = typeBox;
	   
	   switch (type.toLowerCase().charAt(0))
	   {
	   		case 'd':
	   			index = FormatData.t.D_HDR.ordinal();
	   			field = FormatData.t.D_FLD.ordinal();
	   			definitionBox = typeBox;
	   			break;
	   		case 'e':
	   			index = FormatData.t.E_HDR.ordinal();
	   			field = FormatData.t.E_FLD.ordinal();
	   			exampleBox = typeBox;
	   			break;
	   }
	   
	   JPanel panel = new JPanel();
	   panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
       panel.setBackground(new Color(168,168,168));

 	   panel.add(typeBox);
	   panel.add(Box.createHorizontalStrut(GAP));
	     
	   panel.add(new JLabel("Order"));
	   panel.add(Box.createHorizontalStrut(GAP));

	   JComboBox<String> count = new JComboBox<String>(counts);
       components[index][0][FormatData.C_COUNT] = count;
	   setComboParams(count, SEPARATOR_SIZE);
       count.setToolTipText("Select how to order multiple entries");
	   panel.add(count);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   JCheckBox rowsOrColumns = new JCheckBox("Rows");
       components[index][0][FormatData.C_ROWS] = rowsOrColumns;
	   rowsOrColumns.setToolTipText("List vertically (rows) or horizontally (columns)");
	   panel.add(rowsOrColumns);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   panel.add(new JLabel("Title"));
	   panel.add(Box.createHorizontalStrut(GAP));

	   JTextField title = new JTextField();
       components[index][0][FormatData.C_TITLE] = title;
       title.setMinimumSize(TEXT_SIZE);
       title.setMaximumSize(TEXT_SIZE);
       title.setPreferredSize(TEXT_SIZE);
       title.setToolTipText("Title of field (Use \\n to skip lines)");
	   panel.add(title);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   panel.add(new JLabel("Format"));
	   panel.add(Box.createHorizontalStrut(GAP));

	   JComboBox<String> format = new JComboBox<String>(formats);
       components[index][0][FormatData.C_FORMAT] = format;
	   setComboParams(format, FORMAT_SIZE);
	   format.setSelectedItem("Normal");
       format.setToolTipText("Selection controls the " + type + " format");
       panel.add(format);
	   panel.add(Box.createHorizontalStrut(GAP));
       
	   JComboBox<String> separator = new JComboBox<String>(separators);
       components[index][0][FormatData.C_SEPARATOR] = separator;
	   setComboParams(separator, SEPARATOR_SIZE);
       separator.setToolTipText("Selection controls how field separates from other text");
	   panel.add(separator);
	   
	   if (showPicture)
	   {
		   JLabel label = new JLabel("Media");
		   panel.add(label);
		   panel.add(Box.createHorizontalStrut(GAP));
		   
		   panel.add(Box.createHorizontalStrut(GAP));
		   JCheckBox picture = new JCheckBox("Show");
	       components[index][0][FormatData.C_SHOW_PICTURE] = picture;
	       picture.setToolTipText("Show media in the output (if present)");
		   panel.add(picture);
		   panel.add(Box.createHorizontalStrut(GAP));
	   }
	   
	   panel.add(Box.createHorizontalGlue());

	   if (maxFields>0)
	   {
		   JPanel category = new JPanel();
		   category.setLayout(new BoxLayout(category, BoxLayout.Y_AXIS));
	       category.setBackground(new Color(168,168,168));

		   category.add(panel);
		   category.add(Box.createVerticalStrut(GAP));
		   
		   JPanel fieldPanel;
		   for (int r=0; r<maxFields; r++)
		   {
			   fieldPanel = makeFieldPanel(field, r);
			   category.add(fieldPanel);
			   category.add(Box.createVerticalStrut(GAP));
		   }
		   return category;
	   }
	   return panel;
   }
   
   /** Method to create a panel of field layouts
    * 	Field name drop down, title for the field, format, and separator type
    * 
    *  @param type index to word, definition, example, or comment
    *  @param index to which field to create
    * 
    * @return The created JPanel of field layouts
    */
   private JPanel makeFieldPanel(int type, int which)
   {
	   JPanel panel = new JPanel();
	   panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
       panel.setBackground(new Color(168,168,168));
	   panel.add(new JLabel("     "));
	   
	   panel.add(new JLabel("Field"));
	   panel.add(Box.createHorizontalStrut(GAP));

	   JComboBox<String> field = new JComboBox<String>(fields);
	   components[type][which][FormatData.F_FIELD] = field;
	   
	   setComboParams(field, FIELD_SIZE);
       field.setToolTipText("Select field to be included in output");
       field.setSelectedIndex(0);

	   panel.add(field);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   panel.add(new JLabel("Title"));
	   panel.add(Box.createHorizontalStrut(GAP));

	   JTextField title = new JTextField();
	   components[type][which][FormatData.F_TITLE] = title;
       title.setMinimumSize(TEXT_SIZE);
       title.setMaximumSize(TEXT_SIZE);
       title.setPreferredSize(TEXT_SIZE);
       title.setToolTipText("Title of field (Use \\n to skip lines)");
       panel.add(title);
	   panel.add(Box.createHorizontalStrut(GAP));
 	   
	   panel.add(new JLabel("Format"));
	   panel.add(Box.createHorizontalStrut(GAP));

	   JComboBox<String> format = new JComboBox<String>(formats);
	   components[type][which][FormatData.F_FORMAT] = format;
	   setComboParams(format, FORMAT_SIZE);
       format.setToolTipText("Selection controls the field format");
       format.setSelectedItem("Normal");
       panel.add(format);
	   panel.add(Box.createHorizontalStrut(GAP));
       
	   JComboBox<String> separator = new JComboBox<String>(separators);
	   components[type][which][FormatData.F_SEPARATOR] = separator;
	   setComboParams(separator, SEPARATOR_SIZE);
       separator.setToolTipText("Selection controls how field separates from other text");
	   panel.add(separator);
	   panel.add(Box.createHorizontalStrut(GAP));
	   
	   if (type==FormatData.t.D_FLD.ordinal())
	   {		   
		   JLabel label = new JLabel("Position Before");
		   panel.add(label);
		   panel.add(Box.createHorizontalStrut(GAP));
	
		   JCheckBox position = new JCheckBox();
	       components[type][which][FormatData.F_POSITION] = position;
	       
	       String tip = "Position field before or after Definition";
	    	   
	       position.setToolTipText(tip);
		   panel.add(position);
	   }
	   panel.add(Box.createHorizontalGlue());
	   return panel;
   }
   
   /** Set size of a combo component */
   private void setComboParams(JComboBox<String> combo, int comboWidth)
   {  
	  Color optionColor = new Color(220, 220, 220);
      combo.setBackground(optionColor);
      combo.setEditable(false);
      
      Dimension size = new Dimension(comboWidth, 20);
      combo.setPreferredSize(size);
      combo.setSize(size);
      combo.setMinimumSize(size);
      combo.setMaximumSize(size);
   }
   
}  // End of TemplateDialog class
