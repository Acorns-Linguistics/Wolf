/**
 * AuthorDialog.java
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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.wolf.data.Author;
import org.wolf.data.DictionaryData;

public class AuthorDialog extends JDialog 
{
	private static final long serialVersionUID = 1L;
	private JList<String>    list;
    private JPanel           leftSide, rightSide;
    private Author[]         authors;
    private DictionaryData   dictionary;
    private DefaultListModel<String> model;
    private boolean          confirm;
    private JFrame			 root;
    
    public AuthorDialog(JFrame root, DictionaryData dictionaryData)
    {
        super(root, true);
        
        this.root = root;
        setModal(true);
        model        = new DefaultListModel<String>();
        confirm      = false;
        
        dictionary = dictionaryData;
        authors = dictionary.getAuthors();
       
        setTitle("Please enter the changes to your list of authors");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Dimension panelSize = new Dimension(1150,300);
        setSize(panelSize);
        setPreferredSize(panelSize);
        setMaximumSize(panelSize);
        setLocationRelativeTo(root);
        
        // Create panel with list of authors (top of center portion)
        model = new DefaultListModel<String>();
        list = new JList<String>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(
                new ListSelectionListener()
                {  public void valueChanged(ListSelectionEvent event) 
                   { loadAuthorData(); }}  );
        list.setBackground(new Color(208,208,208));
        list.setFont(new Font("Monospaced", Font.PLAIN, 12));

        for (int a=0; a<authors.length; a++) model.add(a, authors[a].toString());
        JScrollPane scroll = new JScrollPane(list);
        
        // Create panel for text entry (bottom of center portion)
        String[] leftLabels = {"Name:", "Initials:", "Language"};
        int[]    leftSizes  = {30, 3, 30};
        leftSide = makeGroup(leftLabels, leftSizes);
        
        String[] rightLabels = {"Organization:", "Email:", "URL:"};
        int[]    rightSizes  = {30, 30, 30};
        rightSide = makeGroup(rightLabels, rightSizes);
        
        JPanel groups = new JPanel();
        groups.setBackground(new Color(168,168,158));
        groups.setLayout(new BoxLayout(groups, BoxLayout.X_AXIS));
        groups.add(Box.createHorizontalGlue());
        groups.add(leftSide);
        groups.add(Box.createHorizontalGlue());
        groups.add(rightSide);
        groups.add(Box.createHorizontalGlue());
        
        JPanel center = new JPanel();
        center.setBackground(new Color(168,168,168));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(scroll);
        center.add(Box.createVerticalStrut(10));
        center.add(groups);
        
        // Create bottom portion to hold the buttons
        JButton add = new JButton("Add");
        add.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  addAuthor();  } });
           
        JButton modify = new JButton("Modify");
        modify.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  modifyAuthor(); } });
              
        JButton remove = new JButton("Remove");
        remove.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {  removeAuthor(); } });
        
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
        buttonPanel.add(add);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(modify);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(remove);
        buttonPanel.add(Box.createHorizontalStrut(50));
        buttonPanel.add(accept);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalGlue());

        Container entry = getContentPane();
        entry.setLayout(new BorderLayout());
        entry.add(center, BorderLayout.CENTER);
        entry.add(buttonPanel, BorderLayout.SOUTH);
        Point point = root.getLocation();
        setLocation(point);
        setVisible(true);
    }
    
    /** Method to get the updated author list
     * 
     * @return null if cancelled, string value if ok
     */
    public Author[] getAuthors()
    {   if (confirm) return authors; 
        else         return null;
    }
    
    /** Method to create a panel with a label and an entry field
    * 
    * @param label Text to go into the label
    * @param width The width of the text field
    * @return JPanel holding the lagel and text field
    */
   private JPanel makePanel(String label, int width)
   {
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
       JLabel jLabel = new JLabel(label);
       Dimension size = new Dimension(150, 20);
       jLabel.setPreferredSize(size);
       jLabel.setMinimumSize(size);
       jLabel.setMaximumSize(size);
       jLabel.setPreferredSize(size);        
        
       JTextField field = new JTextField(width);
       size = new Dimension(150, 20);
       field.setPreferredSize(size);
       field.setMinimumSize(size);
       field.setMaximumSize(size);
       field.setPreferredSize(size);        
       
       panel.add(jLabel);
       panel.add(Box.createHorizontalStrut(10));
       panel.add(field);
       panel.add(Box.createHorizontalGlue());
       
       size = new Dimension(400,20);
       panel.setPreferredSize(size);
       panel.setMaximumSize(size);
       panel.setMinimumSize(size);
       panel.setSize(size);
       panel.setBackground(new Color(168,168,158));
       return panel;
   }
   
   /** Method to create a vertical group of text fields with labels
    * 
    * @param labels Array of label text
    * @param sizes Array of text field width
    * @return The panel holding the group of text fields and labels
    */
   private JPanel makeGroup(String[] labels, int[] sizes)
   {
       JPanel panel = new JPanel(), labelPanel;
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.add(Box.createVerticalGlue());
       for (int i=0; i<labels.length; i++)
       {
           labelPanel = makePanel(labels[i], sizes[i]);
           panel.add(labelPanel);           
       }
       panel.add(Box.createVerticalGlue());
       panel.setBackground(new Color(168,168,158));
       return panel;
   }
   
   /** Method to extract text field data from the panel
    * 
    * @param panel Panel containing set of labels and text fields
    * @return array of strings
    */
   private String[] getFields(JPanel panel)
   {
       String[] fields = new String[3];
       
       JPanel component;
       JTextField field;
       for (int c=0; c<fields.length; c++)
       {
           component = (JPanel)panel.getComponent(c+1);
           field = (JTextField)(component.getComponent(2));
           fields[c] = field.getText();           
       }
       return fields;       
   }
   
   /** Set fields into a JPanel
    * 
    * @param panel The panel into which text fields are to be set
    * @param fields The array of text strings
    */
   private void setFields(JPanel panel, String[] fields)
   {
       JPanel component;
       JTextField field;
       for (int c=0; c<fields.length; c++)
       {
           component = (JPanel)panel.getComponent(c+1);
           field = (JTextField)(component.getComponent(2));
           field.setText(fields[c]);           
       }
   }
   
   /** Load author data into the JPanels */
   private void loadAuthorData()
   {
       int index = list.getSelectedIndex();
       if (index<0) return;
       
       String[] leftFields  = new String[3];
       String[] rightFields = new String[3];
       for (int i=0; i<3; i++)
       {  leftFields[i] = rightFields[i] = ""; }
       
       if (index>=0)
       {
          leftFields[0] = authors[index].getField(Author.NAME);
          leftFields[1] = authors[index].getField(Author.INIT);
          leftFields[2] = authors[index].getField(Author.LANG);
          rightFields[0] = authors[index].getField(Author.ORG);
          rightFields[1] = authors[index].getField(Author.EMAIL);
          rightFields[2] = authors[index].getField(Author.URL);
       }
       setFields(leftSide, leftFields);
       setFields(rightSide, rightFields);
   }
   
   /** Store author data into the authors array */
   private void storeAuthorData(int index)
   {
       String[] leftFields = getFields(leftSide);
       String[] rightFields = getFields(rightSide);
       
       authors[index].setField(leftFields[0], Author.NAME);
       authors[index].setField(leftFields[1], Author.INIT);
       authors[index].setField(leftFields[2], Author.LANG);
       authors[index].setField(rightFields[0], Author.ORG);
       authors[index].setField(rightFields[1], Author.EMAIL);
       authors[index].setField(rightFields[2], Author.URL);
   }
   
   /** Add a new author to the end */
   private void addAuthor()
   {
       Author[] newAuthors = new Author[authors.length + 1];
       
       int index = list.getSelectedIndex();
       if (index<0) index = model.getSize();
      
       for (int i=0; i<newAuthors.length; i++)
       {
            if (i<index)  newAuthors[i] = authors[i];
            if (i==index) newAuthors[i] = new Author();
            if (i>index)  newAuthors[i] = authors[i-1];
       }
       authors = newAuthors;
       storeAuthorData(index);
       model.add(index, authors[index].toString());
       list.setSelectedIndex(index);
   }
   
   /** Remove the selected author from the list */
   private void removeAuthor()
   {
       int index = list.getSelectedIndex();
       if (index<0)
       {
           JOptionPane.showMessageDialog(root, "Please select author to delete");
           return;
       }
       
       Author[] newAuthors = new Author[authors.length - 1];
       for (int a=0; a<authors.length; a++)
       {
           if (a<index)        newAuthors[a] = authors[a];
           else if (a > index) newAuthors[a-1] = authors[a];
       }
       authors = newAuthors;
       model.removeElementAt(index);
       list.setSelectedIndex(-1); 
   }
   
   /** Modify the selected author */
   private void modifyAuthor()
   {
       int index = list.getSelectedIndex();
       if (index<0)
       {
           JOptionPane.showMessageDialog(root, "Please select author to modify");
           return;
       }
       storeAuthorData(index);
       model.setElementAt(authors[index].toString(), index);
   }

}  // End of AuthorDialog class
