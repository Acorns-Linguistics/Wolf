/*
 * TemplateWidget.java
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
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.Constants;
import org.wolf.data.DictionaryData;
import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.system.Environment;

/** Base class for GUI components of a dictionary word */
public class TemplateWidget extends JPanel implements Constants
{
	private static final long serialVersionUID = 1L;
	protected String phoneticsText, indigenousText;

    private static final String[] refList 
  		= {	"Antonyms",   "Categories", "Compare",   "Lexical Function",
    		"Main Entry", "References", "Reversals", "Subentry",
    		"Synonyms",   "Thesaurus"
    	  };

    /** Constructor */
    public TemplateWidget()
    {   phoneticsText = indigenousText = "";
        setBackground(WIDGET_BACKGROUND);
        setForeground(WIDGET_FOREGROUND);
    }

    public TemplateWidget(String phoneticsText, String indigenousText)
    {   this.phoneticsText = phoneticsText;
        this.indigenousText = indigenousText;

        setBackground(WIDGET_BACKGROUND);
        setForeground(WIDGET_FOREGROUND);
    }

    /** Get the height of a text or label component based on the font */
    protected int getFieldHeight()
    {  FontMetrics metrics = getFontMetrics(getFont());
       return metrics.getHeight() + GAP;
    }

    /** Clear the phonetics and indigenous text */
    public void resetText(String i, String p)
    {  indigenousText = i;
       phoneticsText = p;
    }

    /** Get the phonetic or gloss text
     *
     * @param widget The calling component
     * @param phonetics phonetic text (true), gloss text (false)
     * @return The phonetic or gloss text string
     */
    public String getPhoneticText(TemplateWidget widget, boolean phonetics)
    {  JTextComponent component = getToggle();
       if (component==null) return "";

       if (isPhonetics()) phoneticsText = component.getText();
       else               indigenousText = component.getText();

       if (phonetics) return phoneticsText;
       else           return indigenousText;
    }

    /** Set the phonetic of gloss text
     *
     * @param widget The calling cocmponent
     */
    public void setPhoneticComponent(JTextComponent widget)
    {  DictionaryData dictionary = getDict();
       Font font;
       if (isPhonetics())
       {  widget.setText(phoneticsText);
          font = dictionary.getIPAFont();
          widget.setFont(font);
       }
       else
       {  widget.setText(indigenousText);
          Language language = dictionary.getActiveLanguage();
          font = new Font(null, Font.PLAIN, 12);

 	  	  if (language !=null && indigenousText!=null)
	  	  {
	  		  String[] split = indigenousText.split(" ");
	  		  if (split.length>0 && split.length>0 && split[0].endsWith(":"))
	  			  font = language.getFont();
    	      language.hookLanguage(widget);
	  	  }
       }
    }

    private JTextComponent getToggle()
    {  try
       {  Class<?> template = this.getClass();
          Method getToggleComponent = template.getMethod("getToggleComponent");
          return (JTextComponent)getToggleComponent.invoke(this, new Object[0]);
       }
       catch (Exception e) { }
       return null;
    }

    /** Get the environment  object */
    protected Environment getEnv()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getEnv();
    }

    /** Get the dictionary object */
    protected DictionaryData getDict()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getDictionaryData();
    }

    /** Get the label for displaying errors */
    protected JLabel getErr()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getErrorLabel();
    }

    protected boolean isPhonetics()
    {  RootDictionaryPanel rootPanel = getRootDictionaryPanel();
       return rootPanel.isPhonetics();
    }

    /** Get the widget that maintains the table of words */
    protected WordListWidget getTable()
    {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
        return rootPanel.getWordTable();
    }
    
    /** Attach right-click listener to appropriate components
     * 
     * @param text The text component to attach the listener
     * 			On right click, position the dictionary view to
     * 				the word indicated by the click position
     * @param type The type of widget
     */
    protected void linkListener(JTextComponent text, String type)
    {
    	MouseListener[] listeners = text.getMouseListeners();
    	String name;
    	for (int i=listeners.length - 1; i>=0; i--)
    	{
   			name = listeners[i].getClass().getName();
   			if (name.indexOf("TemplateWidget")>=0)
   				text.removeMouseListener(listeners[i]);
    	}
    	
    	boolean found = false;
    	for (String entry: refList)
    	{
    		if (entry.equalsIgnoreCase(type))
    		{
    			found = true;
    			break;
    		}
    	}
    	
    	// Only attach listener to appropriate types
    	if (!found) return;
    	
    	text.addMouseListener(new MouseListener()
    	{
    		@Override
    		public void mouseReleased(MouseEvent e)
    		{ 
    			if (e.isAltDown())      
    			{
    				JTextComponent component = (JTextComponent)e.getSource();
    	            int x = e.getX();
    	            int y = e.getY();
    	 
    				int position = component.viewToModel2D(new Point(x,y));
    				String text[] = component.getText().split(",| |;");
    				
    				int offset = 0, index = 0;
    				while (index < text.length)
    				{
    					offset += text[index].length() + 1;
    					if (position < offset) break;
    					index++;
    				}
    				
    				if (index>=text.length)
    				{
    					Toolkit.getDefaultToolkit().beep();
    					System.out.println(index + " " + "too large");
    					return;
    				}
    				
    				RootDictionaryPanel root = getRootDictionaryPanel();
    				WordListWidget wordList = root.getWordTable();
    				wordList.scrollToWord(text[index].trim());
    			}
    			
    		}

    		@Override
    		public void mouseClicked(MouseEvent e) {}

    		@Override
    		public void mousePressed(MouseEvent e) {}

    		@Override
    		public void mouseEntered(MouseEvent e) {}

    		@Override
    		public void mouseExited(MouseEvent e)  {}
    	});
    }

    /** Retrieve the root dictionary panel */
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

    /** Set the characteristics of this cell */
    public void storeCellCharacteristics(Item item)
    {   
    	item.setBackground(getBackground());
        item.setForeground(getForeground());
        item.setFont(getFont());
        item.setSize(getPreferredSize());
    }

    /** Load the characteristics of this cell */
    public void loadCellCharacteristics(Item item)
    {  
       setBackground(item.getBackground());
       setForeground(item.getForeground());
       setFont(item.getFont());
       setSize(new Dimension(item.getSize()));
       setPreferredSize(getSize());
       setMaximumSize(getSize());
    }
 
}       // End of TemplateWidget class
