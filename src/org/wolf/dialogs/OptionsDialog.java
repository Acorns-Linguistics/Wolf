/**
 * OptionsDialog.java
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.wolf.data.DictionaryData;

/** Set dictionary options */
public class OptionsDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private boolean         confirm;
    private JComboBox<String> fontCombo, sizeCombo;
    private JLabel          preview;
    private DictionaryData  dictionary;

    /** Dialog to set dictionary options
     *
     * @param root The root frame to which to attach this dialog
     * @param d The current dictionary object
     */
    public OptionsDialog(JFrame root, DictionaryData d)
    {  super(root, true);
       setModal(true);
       dictionary = d;
       
       confirm        = false;
        
       setTitle("Please enter the changes to your ACORNS dictionary options");
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
       preview = new JLabel("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz"
                        , JLabel.CENTER );
       previewPanel.add(Box.createHorizontalGlue());
       previewPanel.add(preview);
       previewPanel.add(Box.createHorizontalGlue());
    
       // Get the IPA font name and size
       Font IPAFont = dictionary.getIPAFont();
       String name = null;
       int size    = 12;
      
       if (IPAFont != null)
       {  name = IPAFont.getName();
          size = IPAFont.getSize();          
       }

       // Configure font size options
       String[] sizes = {"", "8", "10", "12", "14", "16", "18", "20", "24"};
       sizeCombo = new JComboBox<String>( sizes );
       setComboParams(sizeCombo, 50);
       sizeCombo.setSelectedItem(""+size);
       sizeCombo.addActionListener(
            new ActionListener() 
            {  public void actionPerformed(ActionEvent e) { previewFont(); }
            });

       // Configure font name options
       GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
       String[] fonts = genv.getAvailableFontFamilyNames();
       String[] comboFonts = new String[fonts.length + 1];
       comboFonts[0] = "Please select font";
       for (int i=0; i<fonts.length; i++) comboFonts[i+1] = fonts[i];
       fontCombo = new JComboBox<String>(comboFonts);
       setComboParams(fontCombo, 200);
       if (name == null) fontCombo.setSelectedIndex(0);
       else              fontCombo.setSelectedItem(name);
      
       fontCombo.addActionListener(
            new ActionListener() 
            {  public void actionPerformed(ActionEvent e) { previewFont(); }
            });
       
       // Create panel with user selections
       JPanel selections = new JPanel();
       selections.setLayout(new BoxLayout(selections, BoxLayout.X_AXIS));
       selections.setBackground(grey);
       selections.add(Box.createHorizontalGlue());
       selections.add(new JLabel("Choose the font to use for IPA input: "));
       selections.add(fontCombo);
       selections.add(Box.createHorizontalStrut(10));
       selections.add(new JLabel("Select an IPA font size: "));
       selections.add(sizeCombo);
       selections.add(Box.createHorizontalGlue());
       previewFont();  // Preview the initally selected font
  
       // Create panel of option buttons
       JButton accept = new JButton("Confirm");
       accept.setBackground(optionColor);
       accept.addActionListener(
           new ActionListener()
           {             
              public void actionPerformed(ActionEvent event)
              {   
                  confirm = true;
                  if (fontCombo.getSelectedIndex()>0)
                  {
                      String name = (String)fontCombo.getSelectedItem();
                      String sizeStr = (String)sizeCombo.getSelectedItem();
                      try
                      {
                          int size = Integer.parseInt(sizeStr);
                          Font IPAFont = new Font(name, Font.PLAIN, size);
                          dictionary.setIPAFont(IPAFont);
                      }
                      catch (Exception e) { confirm = false; }
                  }
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
        buttons.add(accept);
        buttons.add(Box.createHorizontalStrut(10));
        buttons.add(cancel);
        buttons.add(Box.createHorizontalStrut(10));
      
        // Create the main panel
        Container pane = getContentPane();
        pane.setBackground(grey);
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(selections);

        pane.add(Box.createVerticalGlue());
        pane.add(Box.createVerticalStrut(10));
        pane.add(previewPanel);
        pane.add(Box.createVerticalStrut(10));
        pane.add(buttons);
        pane.add(Box.createVerticalGlue());
      
        pack();
        Dimension screen = getToolkit().getScreenSize();
        Rectangle bounds = getBounds();
        setLocation((screen.width - bounds.width) / 2, (screen.height - bounds.height) / 2);
        setVisible(true);
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
   
   /** Preview a font and set the active language 
    *  @param dropDowns true if we should get the font information from drop downs.
    */
    private void previewFont()
    {
        String currentFont = (String)fontCombo.getSelectedItem();
        int size = 12;
        if (sizeCombo.getSelectedIndex()>0)
        size = Integer.parseInt((String)sizeCombo.getSelectedItem());

        Font font = new Font(currentFont, Font.PLAIN, size);
        preview.setFont(font);
    }
    
   /** Method to return whether the update was successful
    * 
    * @return true if successful, false otherwise
    */
    public boolean optionsConfirmed()   {  return confirm; }   
    
}  // End of OptionsDialog class
