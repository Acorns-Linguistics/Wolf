/**
 * CopyrightDialog.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.wolf.data.DictionaryData;

/** Update the copyright notice */
public class CopyrightDialog extends JDialog
{   
	private static final long serialVersionUID = 1L;

	JTextArea copyright;
    boolean confirm = false;
    DictionaryData dictionary;

    /** Copyright notice dialog
     *
     * @param root The root frame to which to attach this dialog
     * @param dictionaryData The current dictionary object
     */
    public CopyrightDialog(JFrame root, DictionaryData dictionaryData)
    {   super(root, true);
        setModal(true);
        confirm = false;
        dictionary = dictionaryData;
        String description = dictionary.getCopyright();
       
        setTitle("Please enter your copyright text");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Dimension panelSize = new Dimension(800,300);
        setSize(panelSize);
        setPreferredSize(panelSize);
        setMaximumSize(panelSize);
        setLocationRelativeTo(root);
        
        copyright = new JTextArea(description, 1, 1);
        copyright.setLineWrap(true);
        copyright.setBackground(new Color(208,208,208));
        JScrollPane scroll = new JScrollPane(copyright);
                 
        JButton accept = new JButton("Confirm");
        accept.addActionListener(
           new ActionListener()
           {             
              public void actionPerformed(ActionEvent event)
              {   
                  confirm = true;
                  setVisible(false);
                  dispose();
              }
           });
           
        JButton cancel = new JButton("Cancel");
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
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(80,80,80));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(accept);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalGlue());

       Container entry = getContentPane();
       entry.setLayout(new BorderLayout());
       entry.add(scroll, BorderLayout.CENTER);
       entry.add(buttonPanel, BorderLayout.SOUTH);
       Point point = root.getLocation();
       setLocation(point);
       setVisible(true);
    }
    
    /** Method to get the entered description
     * 
     * @return null if cancelled, string value if ok
     */
    public String getDescription()
    {   if (confirm) return copyright.getText(); 
        else         return null;
    }
  
 
}  // End of CopyrightDialog class
