/*
 * FontDialog.java
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

/** Dialog to choose a font and size */
public class FontDialog extends JDialog
{   
	private static final long serialVersionUID = 1L;

boolean confirm;

   private JComboBox<String>  sizeCombo, fontCombo;
   private JLabel     preview;

   /** Chose a font and size
    *
    * @param root The root frame to which to attach this dialog
    * @param font The current font to use as a default
    */
   public FontDialog(JFrame root, Font font)
   {   super(root, true);
       setModal(true);
       confirm = false;

       setTitle("Please select the desired font and size");
       setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
       Dimension panelSize = new Dimension(900,150);
       setSize(panelSize);
       setPreferredSize(panelSize);
       setMaximumSize(panelSize);
       setLocationRelativeTo(root);

       // Create label for previewing the font
       Color optionColor = new Color(220, 220, 220);

       JPanel previewPanel = new JPanel();
       previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.X_AXIS));
       preview = new JLabel
         ("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz",JLabel.CENTER);
       previewPanel.add(Box.createHorizontalGlue());
       previewPanel.add(preview);
       previewPanel.add(Box.createHorizontalGlue());

       // Configure font size options
       String[] sizes = {"", "8", "10", "12", "14", "16", "18", "20", "24"};
       sizeCombo = new JComboBox<String>( sizes );
       sizeCombo.setSelectedItem("" + font.getSize());
       setComboParams(sizeCombo, 70);
       sizeCombo.addActionListener(
            new ActionListener()
            {  public void actionPerformed(ActionEvent e) { previewFont(); }
            });

      // Configure font name options
      GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
      String[] fonts = genv.getAvailableFontFamilyNames();
      fontCombo = new JComboBox<String>(fonts);
      setComboParams(fontCombo, 200);
      fontCombo.setSelectedItem(font.getName());
      previewFont();
      fontCombo.addActionListener(
            new ActionListener()
            {  public void actionPerformed(ActionEvent e) { previewFont(); }
            });


      // Create top panel to hold font, size dropdowns and the language box
      JPanel selections = new JPanel();
      selections.setLayout(new BoxLayout(selections, BoxLayout.X_AXIS));
      selections.add(Box.createHorizontalStrut(20));
      selections.add(fontCombo);
      selections.add(Box.createHorizontalStrut(5));
      selections.add(sizeCombo);
      selections.add(Box.createHorizontalStrut(5));
      selections.add(Box.createHorizontalStrut(20));

      JButton accept = new JButton("Confirm");
      accept.setBackground(optionColor);
      accept.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {   confirm = true;
                  setVisible(false);
                  dispose();
              }
           });

      JButton cancel = new JButton("Cancel");
      cancel.setBackground(optionColor);
      cancel.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {   confirm = false;
                  setVisible(false);
                  dispose();
              }
           });

      // Create panel of option buttons
      JPanel buttons = new JPanel();
      buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
      buttons.add(Box.createHorizontalGlue());
      buttons.add(Box.createHorizontalStrut(3));
      buttons.add(accept);
      buttons.add(Box.createHorizontalStrut(3));
      buttons.add(cancel);
      buttons.add(Box.createHorizontalStrut(3));

      // Now create the main panel
      Container pane = getContentPane();
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(selections);
      pane.add(Box.createVerticalStrut(3));
      pane.add(previewPanel);
      pane.add(Box.createVerticalStrut(3));
      pane.add(buttons);

      pack();
      Dimension screen = getToolkit().getScreenSize();
      Rectangle bounds = getBounds();
      setLocation((screen.width-bounds.width)/2,(screen.height-bounds.height)/2);
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


   /** Preview a font that is selected */
   private void previewFont()
   {
       String currentFont = (String)fontCombo.getSelectedItem();
       int size = 12;
       if (sizeCombo.getSelectedIndex()>0)
       size = Integer.parseInt((String)sizeCombo.getSelectedItem());

       Font selectedFont = new Font(currentFont, Font.PLAIN, size);
       preview.setFont(selectedFont);
   }

   /** Return the selected font or null if operation canceled */
   public Font getSelectedFont()
   {   if (confirm) 
       {   String fontName = (String)fontCombo.getSelectedItem();
           int fontSize = Integer.parseInt((String)sizeCombo.getSelectedItem());
           return new Font(fontName, Font.PLAIN, fontSize);
       }
       return null;
   }
}       // End of FontDialog class
