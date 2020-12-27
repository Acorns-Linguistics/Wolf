/*
 * TextAreaWidget.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import org.wolf.data.Comment;
import org.wolf.data.Item;

/** GUI objects the contain a title and a text area
 *  Could be free form entry, synonyms, antonyms, categories, spellings
 */
public class TextAreaWidget extends TemplateWidget implements MouseListener
{   
	private static final long serialVersionUID = 1L;

	private final static int ICON = 25;

    private JLabel expand;

    private JTextArea area;
    private JTextField field;
    private JScrollPane scroll;

    private String title;

    private Dimension scrollSize, fieldSize;
    private Font indigenousFont;

    /** Constructor for expandable text area with user no title */
    public TextAreaWidget(Dimension size)
    {  
       super();
       initialize(null, size);
    }

    public TextAreaWidget(Item item)
    {  
       super();
       
       Comment comment = (Comment)item;
       initialize(comment.getTitle(), item.getSize());
       formatCell(item);
    }

    /** Get the data after user interaction */
    public Item updateCell()
    {  
       Comment comment = new Comment();
       storeCellCharacteristics(comment);
       comment.setSize(scrollSize);
       comment.setFont(indigenousFont);
       comment.setComment(getPhoneticText(this, false));
       comment.setPhonetics(getPhoneticText(this, true));
       comment.setTitle(title);
       comment.setExpanded(isExpanded());
       return comment;
    }

    /** Format a cell for display */
    public void formatCell(Item item)
    {  
    	Comment comment = (Comment)item;

        Dimension size = comment.getSize();
        if (size.height == -1) size.height = size.width;

        title = comment.getTitle();
        setBorder(makeBorder(title)); 
        linkListener(area, title);
        linkListener(field, title);
        
        scrollSize = item.getSize();
        scrollSize.height = getFieldHeight() * 5;
 
        Insets insets = getInsets();
        
        fieldSize = new Dimension(scrollSize.width
                        , getFieldHeight()+insets.top+insets.bottom);
        loadCellCharacteristics(comment);

        String indigenous = comment.getComment();
        String phonetic = comment.getPhonetics();
        resetText(indigenous, phonetic);

        // Set the text area component
        setPhoneticComponent(area);

        // Now set the single line component
        String text = area.getText();
        int index = text.indexOf('\n');
        if (index>=0) text = text.substring(0, index);
        field.setText(text);
        field.setFont(area.getFont());
        
        int fieldHeight = getFieldHeight();
        if (fieldHeight < FIELD_HEIGHT )
     	   fieldHeight= FIELD_HEIGHT;
        
        Dimension fSize = new Dimension(scrollSize.width, fieldHeight);
        field.setSize(fSize);
        field.setPreferredSize(fSize);
        field.setMaximumSize(fSize);

        setIndigenousFont(item.getFont(), insets);
        if (isPhonetics())  
        { 
        	setCellFont(getDict().getIPAFont(), insets); 
        }
        
        setExpanded(comment.isExpanded());
        setAreaSize();
    }

    /** Configure the component for the constructors
     *
     * @param title JLabel or JTextPanel
     */
    private void initialize(String  t, Dimension size)
    {  title = t;
       scrollSize = new Dimension(size);

       setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       setBorder(makeBorder(t));

       Insets insets = getInsets();
       fieldSize
         = new Dimension(size.width, getFieldHeight()+insets.top+insets.bottom);

       expand = new JLabel("+");
       expand.setVerticalTextPosition(JLabel.TOP);
       //expand.setVerticalAlignment(SwingConstants.CENTER);
       expand.setHorizontalAlignment(SwingConstants.CENTER);
       expand.setBorder(makeBorder(null));
       expand.setAlignmentX(Component.LEFT_ALIGNMENT);
       expand.setAlignmentY(Component.TOP_ALIGNMENT);
       expand.setFont(new Font("monospaced", Font.BOLD, 14));
       expand.addMouseListener(this);
       expand.setPreferredSize(new Dimension(ICON,ICON));
       expand.setMaximumSize(new Dimension(ICON,ICON));
       add(expand);

       field = new JTextField("");
       field.setAlignmentY(Component.TOP_ALIGNMENT);
       field.setVisible(true);
       
       int fieldHeight = getFieldHeight();
       if (fieldHeight < FIELD_HEIGHT )
    	   fieldHeight= FIELD_HEIGHT;
       
       Dimension fSize = new Dimension(scrollSize.width, fieldHeight);
       field.setSize(fSize);
       field.setPreferredSize(fSize);
       field.setMaximumSize(fSize);
       
       add(field);

       area = new JTextArea();
       area.setLineWrap(true);
       area.setWrapStyleWord(true);
       
       scroll = new JScrollPane(area);
       scroll.setVerticalScrollBarPolicy
               (ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
       scroll.setAlignmentY(Component.TOP_ALIGNMENT);
       add(scroll);

       scroll.setVisible(false);
       setAreaSize();
    }  // End of initialize()

    /** Create a border containing insets */
    private Border makeBorder(String title)
    {   Border border;
        if (title!=null && title.length()!=0)
              border = BorderFactory.createTitledBorder(title);
        else  border = BorderFactory.createLineBorder(Color.GRAY);

        Border empty = BorderFactory.createEmptyBorder(10,2,1,2);
        if (title==null)
        	empty = BorderFactory.createEmptyBorder(0,0,0,0);
        else if (title.length()>0)
        	empty = BorderFactory.createEmptyBorder(2,2,2,2);
        return BorderFactory.createCompoundBorder(border, empty);
    }
    
    private String updateText()
    {
   	   String text = area.getText();
  	   int index = text.indexOf('\n');
       
  	   if (isExpanded()) 
  	   {
  		   if (index<0) field.setText(text);
  		   else field.setText(text.substring(0, index));
  	   }
  	   else
  	   {
     	  if (index>=0) 
     	  {
     		  text = field.getText() + text.substring(index);
     	  }
     	  else text = field.getText();
     	  area.setText(text);
       }
       return text;
    }

    /** Toggle display between text and phonetics */
    public JTextComponent getToggleComponent()
    {  
       updateText();  // Update the current text data
       return area;
    }  // End of toggleDisplay()

    /** Configure the font of this component */
    private void setCellFont(Font font, Insets insets)
    {   
    	String[] split = field.getText().split(" ");
    	String data = (split.length>0) ? split[0] : "";
    	if (data.endsWith(":")) return;
    	
    	if (field!=null) field.setFont(font);
        if (area!=null) area.setFont(font);
        setFont(font);

        //Insets insets = getInsets();
        fieldSize.height = getFieldHeight()+insets.top+insets.bottom;
        if (fieldSize.height<WIDGET_HEIGHT)
        	fieldSize.height = WIDGET_HEIGHT;
        setAreaSize();
    }
    
    /** Override the indigenous font */
    private void setIndigenousFont(Font font, Insets insets)
    {  indigenousFont = font;
       if (!isPhonetics()) setCellFont(font, insets);        
    }


    /** Update the width of the cell */
    public void setCellWidth(Integer width)
    {  Dimension size = getPreferredSize();
       scrollSize.width = width;
       fieldSize.width = width;
       size.width = width;
       setSize(new Dimension(size));
       setPreferredSize(getSize());
       setMaximumSize(getSize());
       
    }

    /** Configure the size of the component based on if expanded or not */
    private void setAreaSize()
    {  
       Dimension size = fieldSize;
       if (isExpanded())  
       {
    	   size = scrollSize;
    	   if (size.height < AREA_WIDGET_HEIGHT)
    		   size.height = AREA_WIDGET_HEIGHT;
       }
       setSize(size);
       setPreferredSize(size);
       setMinimumSize(size);
       setMaximumSize(size);
    }

    /** Determine if text area or text field component is showing */
    private boolean isExpanded()  {  return expand.getText().equals("-"); }

    /** Expand or contract the text field */
    private void setExpanded(boolean expandField)
    {  
       updateText();
       if (expandField) 
       { 
    	   expand.setText("-");
       }
       else  
       { 
    	   expand.setText("+");
       }
       scroll.setVisible(expandField);
       field.setVisible(!expandField);
       setAreaSize();
    }

    /** Expand and collapse text area display  */
    public void mouseReleased(MouseEvent event)
    {  setExpanded(!isExpanded());
       WordListWidget table = getTable();
       if (table!=null) table.updateWord();
    }
    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseEntered(MouseEvent event)  {}

 }         // End of TextAreaWidget class
