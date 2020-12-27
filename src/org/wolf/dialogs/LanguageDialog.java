/**
 * LanguageDialog.java
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
package org.wolf.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.acorns.language.LanguageFont;
import org.wolf.data.DictionaryData;
import org.wolf.data.Language;
import org.wolf.lib.LanguageCodes;
import org.wolf.system.Environment;

/** Maintain the languages in the dictionary */
public class LanguageDialog extends JDialog
{  
   private static final long serialVersionUID = 1L;
   private static final int FIELD_HEIGHT = 25;
   
   private Language[]       languages;
   private DictionaryData   dictionary;
   private boolean          confirm;
   private boolean          languageRemoved;
   
   private DefaultListModel<String>  model;
   private JList<String>      infoList;
   private JComboBox<String> sizeCombo, fontCombo, codeCombo;
   private JLabel            preview;
   private JTextField        languageText, variantText, sortText;
   private JFrame			 root;
   
   /** Create the language dialog
    *
    * @param root The root frame to which to attach this dialog
    * @param dictionaryData The current dictionary object
    */
   public LanguageDialog(JFrame root, DictionaryData dictionaryData)
   {  
      super(root, true);
      this.root = root;
      setModal(true);
       
      dictionary = dictionaryData;
      languages = dictionary.getLanguages();
       
      confirm = false;
      languageRemoved = false;
       
      setTitle("Please enter the changes to your list of languages");
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      Dimension panelSize = new Dimension(1000,300);
      setSize(panelSize);
      setPreferredSize(panelSize);
      setMaximumSize(panelSize);
      setLocationRelativeTo(root);
       
      // Create label for previewing the font
      Color grey = new Color(192,192,192);
      Color optionColor = new Color(220, 220, 220);

      JPanel previewPanel = new JPanel();
      previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.X_AXIS));
      previewPanel.setBackground(grey);
      preview = new JLabel
         ("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz",JLabel.CENTER);
      previewPanel.add(Box.createHorizontalGlue());
      previewPanel.add(preview);
      previewPanel.add(Box.createHorizontalGlue());

      // Configure font size options
      String[] sizes = {"", "8", "10", "12", "14", "16", "18", "20", "24"};
      sizeCombo = new JComboBox<String>( sizes );
      setComboParams(sizeCombo, 70);
      sizeCombo.setSelectedIndex(3);
      sizeCombo.addActionListener(
            new ActionListener() 
            {  public void actionPerformed(ActionEvent e) { previewFont(true); }
            });

      // Configure font name options
      fontCombo = createLanguageComboBox();
      setComboParams(fontCombo, 200);
      fontCombo.addActionListener(
            new ActionListener() 
            {  public void actionPerformed(ActionEvent e) { previewFont(true); }
            });
         
      // Create language code options
      codeCombo = LanguageCodes.getJCombo();
      setComboParams(codeCombo, 250);
      codeCombo.setSelectedIndex(0);
      codeCombo.addItemListener
              (   new ItemListener()
                  {  public void itemStateChanged(ItemEvent event)
                     {   changeLanguage();  }
          
                  }
              );  
      // Create language label and text field.
      JLabel languageLabel = new JLabel("Language: ");
      languageText = new JTextField(30);
      Dimension size = new Dimension(200, FIELD_HEIGHT);
      languageText.setMaximumSize(size);
      languageText.setPreferredSize(size);
      languageText.setSize(size);
      languageText.setToolTipText
              ("Enter language that applies to the selected font");
      languageText.setText("");

      // Create dialect label and text field.
      JLabel variantLabel = new JLabel("Variant: ");
      variantText = new JTextField(3);
      size = new Dimension(30, FIELD_HEIGHT);
      variantText.setMaximumSize(size);
      variantText.setPreferredSize(size);
      variantText.setSize(size);
      variantText.setToolTipText
              ("Enter variant - Alphabetic and maximum of two characters");
      variantText.setText("");

      
      // Create top panel to hold font, size dropdowns and the language box
      JPanel selections = new JPanel();
      selections.setLayout(new BoxLayout(selections, BoxLayout.X_AXIS));
      selections.setBackground(grey);
      selections.add(Box.createHorizontalStrut(50));
      selections.add(fontCombo);
      selections.add(Box.createHorizontalStrut(10));
      selections.add(sizeCombo);
      selections.add(Box.createHorizontalStrut(10));
      selections.add(languageLabel);
      selections.add(codeCombo);
      selections.add(Box.createHorizontalStrut(10));
      selections.add(languageText);
      selections.add(Box.createHorizontalStrut(10));
      selections.add(variantLabel);
      selections.add(variantText);
      
      // Create a Scrollable list of current selections
      model = new DefaultListModel<String>();
      infoList = new JList<String>(model);
      infoList.setCellRenderer(new InfoListRenderer());
      infoList.setFont(new Font(null, Font.PLAIN, 12));
      for (int i=0; i<languages.length; i++) 
      {  
    	  model.add(i, languages[i].getFontInfoDialect()); 
      }

      infoList.setSelectedIndex(-1); 
      infoList.addListSelectionListener(
            new ListSelectionListener() 
            {  public void valueChanged(ListSelectionEvent e) 
				           {   loadLanguageData(); }
            });
		
      JScrollPane scroll = new JScrollPane(infoList);
      JPanel scrollPanel = new JPanel();
      scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.X_AXIS));
      scrollPanel.setBackground(grey);
      scrollPanel.add(Box.createHorizontalGlue());
      scrollPanel.add(scroll);
      scrollPanel.add(Box.createHorizontalGlue());
      
      // Create the text field to define the sort order
      JPanel sortPanel = new JPanel();
      sortPanel.setOpaque(false);
      sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.X_AXIS));
      sortPanel.add(new JLabel("Custom Sort Order: "));
      sortText = new JTextField();
      sortText.setMaximumSize(new Dimension(2000,FIELD_HEIGHT));
      sortPanel.add(sortText);
        
      // Create the buttons to manage the list of languages
      JButton add = new JButton("Add");
      add.setBackground(optionColor);
      add.addActionListener(
          new ActionListener()
          {  public void actionPerformed(ActionEvent event)
             {  addLanguage();  } });
      
      JButton modify = new JButton("Modify");
      modify.setBackground(optionColor);
	
      modify.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  modifyLanguage(); } });
              
      JButton remove = new JButton("Remove");
      remove.setBackground(optionColor);
      remove.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  removeLanguage(); } });
        
      JButton accept = new JButton("Confirm");
      accept.setBackground(optionColor);
      accept.addActionListener(
           new ActionListener()
           {             
              public void actionPerformed(ActionEvent event)
              {   confirm = true;
                  setVisible(false);
                  dispose();
              }
           });
           
      JButton cancel = new JButton("Cancel");
      cancel.setBackground(optionColor);
      cancel.addActionListener(
           new ActionListener()
           {             
              public void actionPerformed(ActionEvent event)
              {   
                  confirm = false;
                  setVisible(false);
                  dispose();
              }
           });
             
      // Create panel of option buttons
      JPanel buttons = new JPanel();
      buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
      buttons.setBackground(grey);
      buttons.add(Box.createHorizontalGlue());
      buttons.add(add);
      buttons.add(Box.createHorizontalStrut(10));
      buttons.add(modify);
      buttons.add(Box.createHorizontalStrut(10));
      buttons.add(remove);
      buttons.add(Box.createHorizontalStrut(30));
      buttons.add(accept);
      buttons.add(Box.createHorizontalStrut(10));
      buttons.add(cancel);
      buttons.add(Box.createHorizontalStrut(10));
      
      // Now create the main panel
      Container pane = getContentPane();
      pane.setBackground(grey);
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(selections);
      pane.add(Box.createVerticalStrut(10));
      pane.add(scrollPanel);
      pane.add(Box.createVerticalStrut(10));
      pane.add(previewPanel);
      pane.add(Box.createVerticalStrut(10));
      pane.add(sortPanel);
      pane.add(buttons);
      
      pack(); 
      Dimension screen = getToolkit().getScreenSize();
      Rectangle bounds = getBounds();
      setLocation((screen.width - bounds.width) / 2, (screen.height - bounds.height) / 2);
      Point point = root.getLocation();
      setLocation(point);
      setVisible(true);           
   }
	
/** Preview a font and set the active language 
   *  @param dropDowns true if we should get the font information from drop downs.
   */
   private void previewFont(boolean dropDowns)
   {
      if (dropDowns)
      {
          String currentFont = (String)fontCombo.getSelectedItem();
          int size = 12;
          if (sizeCombo.getSelectedIndex()>0)
          size = Integer.parseInt((String)sizeCombo.getSelectedItem());

          Font font = new Font(currentFont, Font.PLAIN, size);
          preview.setFont(font);
          sortText.setFont(font);
          languageText.setFont(font);
      }
      else  // Get preview from list selection
      {  int index = infoList.getSelectedIndex();
         if (index>=0)
          {  Font font = languages[index].getFont();
             preview.setFont(font);
             sortText.setFont(font);
             languageText.setFont(font);
             fontCombo.setSelectedItem(font.getName());
             sizeCombo.setSelectedItem("" + font.getSize());
          }
      }
   }
	
   /** Add a new language to the end */
   private void addLanguage()
   {   // Validate the entry
       String font     = (String)fontCombo.getSelectedItem();
       String sizeStr  = (String)sizeCombo.getSelectedItem();
       String code     = (String)codeCombo.getSelectedItem();
       
       if (code.length()>3) code = code.substring(0,3);
       String language = languageText.getText();
       code  = getVariant(code);

       if (fontCombo.getSelectedIndex()<=0)
           JOptionPane.showMessageDialog
                   (root, "Please select an available font");
       else if (code == null)
           JOptionPane.showMessageDialog
                   (root, "Illegal variant - must be alphabetic and less than 3 characters");
       else if (sizeCombo.getSelectedIndex()<=0)
           JOptionPane.showMessageDialog
                       (root, "Please select an available font size");
       else if (codeCombo.getSelectedIndex()<=0)
           JOptionPane.showMessageDialog
                       (root, "Please select an available language code");
       else if (language==null || language.trim().length()==0)
           JOptionPane.showMessageDialog
                       (Environment.getRootFrame(), 
                    		   "Please enter the native language");
       else if (findLanguageIndex(code)>=0)
       {  JOptionPane.showMessageDialog(root, "Language is already there"); }
       else
       {  Language[] newLanguages = new Language[languages.length + 1];
          for (int lng=0; lng<languages.length; lng++)
          {  newLanguages[lng] = languages[lng]; }

          // Create the new language object
          int size = Integer.parseInt(sizeStr);
          LanguageFont newLanguage = new LanguageFont(font, size, language);

          String languageFields = getSort(code);
          newLanguages[languages.length] = new Language(languageFields, newLanguage);

          // Update the language list, and add to the display
          languages = newLanguages;
          model.addElement(languages[languages.length - 1].getFontInfoDialect());
          //infoList.setSelectedIndex(languages.length - 1); 
       }
   }
   
   /** Method to prevent a language code from twice appearing
    * 
    * @param code language search code
    * @return index if found, -1 otherwise
    */
   private int findLanguageIndex(String code)
   {   for (int c=0; c<languages.length; c++)
       { if (languages[c].getLanguageCode().equals(code)) return c;  }
       return -1;
   }
   
   /** Remove the selected language from the list */
   private void removeLanguage()
   {   int index = infoList.getSelectedIndex();
       if (index<0)
       {   JOptionPane.showMessageDialog
                      (root, "Please select the language you want to delete");
           return;
       }
       
       Language[] newLanguages = new Language[languages.length - 1];
       for (int lng=0; lng<languages.length; lng++)
       {   if (lng<index)        newLanguages[lng]   = languages[lng];
           else if (lng > index) newLanguages[lng-1] = languages[lng];
           else if (languages[lng].getWordCount()>0) languageRemoved = true;
       }
       languages = newLanguages;
       model.removeElementAt(index);
       infoList.setSelectedIndex(-1); 
       preview.setFont(null);
       sortText.setFont(null);
       languageText.setFont(null);
   }
   
   /** Modify the selected language */
   private void modifyLanguage()
   {   // Validate the entry
       int index = infoList.getSelectedIndex();
       if (index<0)
       {   JOptionPane.showMessageDialog
                        (root, "Please select language you want to modify");
           return;
       }

       String font     = (String)fontCombo.getSelectedItem();
       String sizeStr  = (String)sizeCombo.getSelectedItem();
       String code     = (String)codeCombo.getSelectedItem();
       if (code.length()>3) code = code.substring(0,3);
       String language = languageText.getText();
       code  = getVariant(code);
       
       int listIndex = findLanguageIndex(code);
       if (listIndex>0 && listIndex!=index)
          JOptionPane.showMessageDialog(root,"Modified language already exists");
       else if (fontCombo.getSelectedIndex()<=0)
          JOptionPane.showMessageDialog(root,"Please select an available font");
       else if (sizeCombo.getSelectedIndex()<=0)
           JOptionPane.showMessageDialog
                   (root, "Please select an available font size");
       else if (codeCombo.getSelectedIndex()<=0)
           JOptionPane.showMessageDialog
                   (root, "Please select an available language code");
       else if (language==null || language.trim().length()==0)
           JOptionPane.showMessageDialog
                   (root, "Please enter the native language");
       else
       {   
    	   code = getSort(code);
    	   languages[index].setLanguageFields(code);
           int size = Integer.parseInt(sizeStr);
           LanguageFont modifyLanguage = new LanguageFont(font, size, language);
           languages[index].setLanguageFont(modifyLanguage);
       }
       model.setElementAt(languages[index].getFontInfoDialect(), index);
   }
   
   /** Return the language key string in the correct format for the language class
    *      
    * @param code lang or lang/variant
    * @return lang or lang/variant or lang/variant/sort or lang//sort
    * 
    */
   private String getSort(String code)
   {
       String sortOrder = (String)sortText.getText();
       String languageFields = code;
       if (sortOrder.length() != 0)
       {
     	  if (code.indexOf('/') >= 0)  languageFields += "/" + sortOrder;
     	  else languageFields += "//" + sortOrder;
       }
       return languageFields;
   }
   
   /** Method to respond to changes in the language codes */
   private void changeLanguage()
   {   String code = (String)codeCombo.getSelectedItem();
       if (code==null) return;
       
       int index = code.indexOf(' ');
       if (index>0) code = code.substring(index + 1);
       
       languageText.setText(code);
   }
   
   /** Load language data after a selection changes */
   private void loadLanguageData()
   {   int index = infoList.getSelectedIndex();
       if (index<0) 
       { 
    	   preview.setFont(null); 
    	   sortText.setFont(null); 
    	   languageText.setFont(null);
    	   return; 
       }

       String code = languages[index].getLanguageCode();
       String[] splitCode = code.split("/");
       if (splitCode.length>1) variantText.setText(splitCode[1]);
       else variantText.setText("");

       String sortOrder = languages[index].getSortOrder();
       sortText.setText(sortOrder);

       int codeIndex = findLanguageCode(splitCode[0]);
       codeCombo.setSelectedIndex(codeIndex);
       
       LanguageFont languageFont = languages[index].getLanguageFont();
       languageText.setText(languageFont.getLanguage());
       
       Font font = languageFont.getFont();
       fontCombo.setSelectedItem(font.getFamily());
       sizeCombo.setSelectedItem(font.getSize());

       previewFont(false);
   }
   
   /** Set size of a combo component */
   private void setComboParams(JComboBox<String> combo, int comboWidth)
   {  Color optionColor = new Color(220, 220, 220);
      combo.setBackground(optionColor);
      combo.setEditable(false);
      
      Dimension size = new Dimension(comboWidth, 20);
      combo.setPreferredSize(size);
      combo.setSize(size);
      combo.setMinimumSize(size);
      combo.setMaximumSize(size);
   }

   /** Method to get the language variant
    *
    * @param code language code
    *
    * @return null if illegal; language code with variant if okay
    */
   private String getVariant(String code)
   {   String variant = variantText.getText();
       if (variant.length()== 0) return code;
       if (variant.length()>2) return null;

       Pattern pattern = Pattern.compile("\\w.*");
       Matcher match = pattern.matcher(variant);
       if (!match.find()) return null;
       return code += "/" + variant;
   }
   
   /** Method to get the updated language list
     * 
     * @return null if cancelled, string value if ok
     */
    public Language[] getLanguages()
    {   if (confirm) return languages; 
        else         return null;
    }

    /** Indicate if language was removed */
    public boolean isRemove()   {  return languageRemoved & confirm;   }

    /** Find language index
     *  @param code language code
     */
    public int findLanguageCode(String code)
    {  int top=0, bottom=codeCombo.getItemCount(), middle=(top+bottom)/2, cmp;
       String item;
       while(top+1<bottom)
       {  item = (String)codeCombo.getItemAt(middle);
          item = item.substring(0,3);
          cmp = item.compareTo(code);
          if (cmp==0) return middle;
          if (cmp<0) top = middle;
          else bottom = middle;
          middle = (top + bottom)/2;
       }
       return 0;
    }
    
    /** Create a combo box of languages
     * 
     * @param select The language to select or null to use the active language
     * @param all true if add all languages, false to add only registered languages
     * @return Created JComboBox
     */
    public JComboBox<String> createLanguageComboBox()
    {
 	    String[] fontList = getDisplayableFonts();
 	    if (fontList == null) fontList = new String[0];

 	    final JComboBox<String> fontCombo = new JComboBox<String>(fontList);
  
 	   fontCombo.setEditable(false);
       fontCombo.setSelectedIndex(0);

       Font selectedFont = new Font(null, Font.PLAIN, 12);

 	   fontCombo.setFont(selectedFont);
 	   fontCombo.setRenderer(new FontCellRenderer());
 	   fontCombo.addActionListener(
 	         new ActionListener() 
 	         {  public void actionPerformed(ActionEvent e) 
 	         	{ 
 	        	 	Font font;
 	        	 	if (fontCombo.getSelectedIndex()==0)
 	        	 		font = new Font(null, Font.PLAIN, 12);
 	        	 	else
 	        	 	{
 	        	 	    String fontName = (String)fontCombo.getSelectedItem();
 	        	 	    font = new Font(fontName, Font.PLAIN, 12);
 	        	 	}
        	 	 	fontCombo.setFont(font);
 	         	}
 	         });
 	   return fontCombo;
    }
    
    
    private String[] getDisplayableFonts()
    {
 	      GraphicsEnvironment env = 
 	              GraphicsEnvironment.getLocalGraphicsEnvironment();
 	      String[] allFonts = env.getAvailableFontFamilyNames();
 	      
 	      ArrayList<String> fontData = new ArrayList<String>();
 	      fontData.add("Please select font");

 	      Font font;
 	      for (int i=0; i<allFonts.length; i++)
 	      {
 	    	  font = new Font(allFonts[i], Font.PLAIN, 12);
 	    	  for (char c='a'; c<='z'; c++)
 	    	  {
 	    		  if (font.canDisplay(c))
 	    		  {
 	    			  fontData.add(allFonts[i]);
 	    			  break;
 	    				  
 	    		  }
 	    	  }
 	      }
 	      return fontData.toArray(new String[fontData.size()]);
    }

    /** Nested class to render cells of the language combo box */
    private static class FontCellRenderer 
 			implements ListCellRenderer<String> 
 	{
 	
 		private DefaultListCellRenderer renderer = null;
 		
 		public FontCellRenderer() {}
 		
 		protected DefaultListCellRenderer getRenderer()
 		{ 
 			if (renderer == null)
 			{	
 				renderer = new DefaultListCellRenderer();
 			}
 			return renderer;
 		}
 		
 		public Component getListCellRendererComponent(
 				JList<? extends String> list, String fontName, int index,
 				boolean isSelected, boolean cellHasFocus) 
 		{
 			final JLabel result = (JLabel)getRenderer().getListCellRendererComponent(
 			  list, fontName, index, isSelected, cellHasFocus);

 			Font font = null;
 			if (index == 0)
 				 font = new Font(null, Font.PLAIN, 12);
 			else font = new Font(fontName, Font.PLAIN, 12);
 			
 			result.setFont(font);
 			return result;
 		}
 	}		// End of FontCellRenderer
    
 
    /** Nested class to render cells of the list of languages */
    private static class InfoListRenderer
    					implements ListCellRenderer<String>
    {
 	   CustomRenderer renderer = null;
 	   
 	   protected CustomRenderer getRenderer()
 	   { 
 	       if (renderer == null)
 	       {	
 	    	   renderer = new CustomRenderer();
 	       }
 	       return renderer;
 	   }
 	   
 	   public Component getListCellRendererComponent(
 			   JList<? extends String> list, String languageData, int index,
 			    boolean isSelected, boolean cellHasFocus) 
 	   {
 		   final Component result = getRenderer().getListCellRendererComponent(
 				   list, languageData, index, isSelected, cellHasFocus);
 	    
 		   CustomRenderer renderer = (CustomRenderer)result;
 		   renderer.setText(languageData);
 		   return result;
 	   }
 	   private class CustomRenderer extends JPanel
 	   {
 		   private static final long serialVersionUID = 1L;
 		   private final static int HDR = 7;
 		   JLabel labels[] = new JLabel[4];
 		   int[] widths = {80, 250, 30, 250};
 		   
 		   public CustomRenderer()
 		   {
 			   setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 			   Dimension size;
 			   for (int i=0; i<labels.length; i++)
 			   {
 				   labels[i] = new JLabel();
 				   size = new Dimension(widths[i], 20);
 				   labels[i].setPreferredSize(size);
 				   labels[i].setMinimumSize(size);
 				   labels[i].setMaximumSize(size);
 				   labels[i].setSize(size);
 				   add(labels[i]);
 			   }
 		   }
 		   
 		   public Component getListCellRendererComponent
 		      (JList<? extends String> list, String language, int index, boolean isSelected, boolean cellHasFocus)
 		   {
 			  return this; 
 		   }
 		      
 		   public void setText(String font)
 		   {
 			   labels[0].setText(font.substring(0, HDR));
 			   
 			   String[] languageFields 
 		   		= LanguageFont.extractToString
 		   				(font.substring(HDR));
 			   
 			   labels[1].setText(languageFields[0]);
 			   labels[1].setFont(new Font(languageFields[0], Font.PLAIN, 12));
 			   labels[2].setText(languageFields[1]);
 			   labels[3].setText(languageFields[2]);
 			   labels[3].setFont(new Font(languageFields[0], Font.PLAIN, 12));
 		   }
 	   }
    }


   
}  // End of LanguageDialog class
