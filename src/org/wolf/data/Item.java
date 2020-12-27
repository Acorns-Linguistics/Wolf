/*
 * Item.java
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

package org.wolf.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.lib.DeepCopy;
import org.wolf.system.Environment;

/** Base class of dictionary items (Defines dictionary cell attributes) */
public abstract class Item  implements Constants, Serializable
{  private static final long serialVersionUID=1L;

   protected static final int NAME = 0, VALUE = 1;

   private Dimension size;
   private Color background, foreground;
   private String fontName;
   private int fontStyle;
   private int fontSize;

   public Item()
   {  background = Color.LIGHT_GRAY;
      foreground = Color.BLACK;
      fontName = null;
      fontStyle = Font.PLAIN;
      fontSize = 12;

      size = new Dimension(200, -1);
   }
   
   public Item(int width)
   {
	   this();
	   this.size = new Dimension(width, -1);
   }

   /** Create an item based on another item */
   public Item(Item item)
   {  
	  this();
	  if (item==null) return;
	  
	  background = item.background;
      foreground = item.foreground;
      fontName   = item.fontName;
      fontStyle  = item.fontStyle;
      fontSize   = item.fontSize;
      size = new Dimension(item.size);
   }

   // Polymophic methods overridden in child classes.
   public String getTitle() { return ""; }
   public void setTitle(String title) {}
   
   public Hashtable<String, String> getIndigenousData()
   {
	   return new Hashtable<String,String>();
   }
   
   public void setIndigenousData(Hashtable<String, String> data)
   {}
   
   public void setIndigenousData(String lang, String d)
   {}

   
   public void updateLanguageCodes(ArrayList<String> languages)
   {}
   
   public Dimension getSize() { return size; }
   public void setSize(Dimension s) 
   { size = s; }

   public Color getBackground() { return background; }
   public void setBackground( Color b) { background = b; }

   public Color getForeground() { return foreground; }
   public void setForeground( Color f) { foreground = f; }

   public Font getFont() { return new Font(fontName, fontStyle, fontSize); }
   public void setFont(Font f)
   {   fontName = f.getName();
       fontStyle = f.getStyle();
       fontSize = f.getSize();
   }

   /** Abstract methods for export and import */
   protected abstract Element exportXML
                                   (Document doc, File file) throws IOException;
   protected abstract void importXML
                                   (Element node, File file) throws IOException;

   /** Convert the first character to upper case */
   protected String normalizeCase(String string)
   {
   	if (string==null || string.length()==0)
   		return "";
   	
   	return string.substring(0,1).toUpperCase() + string.substring(1).toLowerCase();
   }

    /** Get the dictionary object */
    protected Environment getEnv()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getEnv();
    }

    /** Retrieve the root dictionary panel */
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }
    
    /** Make an identical copy of this object 
     * 
     * @return The cloned Example object
     */
    @Override public Object clone()  
    {   
    	return DeepCopy.copy(this);  
    }

    /** Method to create a node with its attributes
     *
     * @param doc The XML document object
     * @param node The name of the node to create
     * @param attributes Pairs of attribute names and values
     * @return The created element
     */
    protected Element makeNode
            (Document doc, String node, ArrayList<String[]> attributes)
    {
        Element element = doc.createElement(node);
        String[] attribute;
        for (int i=0; i<attributes.size(); i++)
        {   attribute = attributes.get(i);
            element.setAttribute(attribute[NAME], attribute[VALUE]);
        }
        return element;
    }

    /** Create the element for formatting on import and for XSLT hints */
    protected void exportStyle(Element node)
    {   if (isColor())
        { node.setAttribute("foreground", colorValue(foreground));
          node.setAttribute("background", colorValue(background));
        }

        if (isWidth()) node.setAttribute("width", ""+size.width);

        if (isFont())
        {  node.setAttribute("face", fontName);
           node.setAttribute("size", ""+fontSize);
        }
    }

    /** Set the style characteristics for this dictionary component */
    protected void importStyle(Element node)
    {   String value = node.getAttribute("forground");
        if (value.length()>0)
            try { foreground = getColor(value); } catch (Exception e) {}

        value = node.getAttribute("background");
        if (value.length()>0)
            try { background = getColor(value); } catch (Exception e) {}

        value = node.getAttribute("width");
        if (value.length()>0)
            try { size.width = Integer.parseInt(value); } catch (Exception e) {}
 
        value = node.getAttribute("face");
        if (value.length()>0) fontName = value;

        value = node.getAttribute("style");
        if (value.length()>0)
            try { fontStyle = Integer.parseInt(value); } catch (Exception e) {}

        value = node.getAttribute("size");
        if (value.length()>0)
            try { fontSize = Integer.parseInt(value); } catch (Exception e) {}
    }

    /** Does the dictionary element have a custom foreground/background color */
    private boolean isColor()
    {  if (!background.equals(Color.LIGHT_GRAY)) return true;
       if (!foreground.equals(Color.BLACK)) return true;
       return false;
    }

    /** Convert string from "r,g,b,a" to a color object */
    private Color getColor(String data)
    {   String[] rgb = data.split(",");
        int red = Integer.parseInt(rgb[0]);
        int green = Integer.parseInt(rgb[1]);
        int blue = Integer.parseInt(rgb[2]);
        int alpha = Integer.parseInt(rgb[3]);
        Color color = new Color(red, green, blue, alpha);
        return color;
    }

    /** Does the dictionary element have a custom width? */
    private boolean isWidth()  { return size.width != 200; }

    /** Does this cocmponent have a custom font? */
    private boolean isFont()
    {  if (!(this instanceof Comment)) return false;

       if (fontName != null) return true;
       if (fontSize != 12) return true;
       if (fontStyle != Font.PLAIN) return true;
       return false;
    }

    /** Get the color as a string "r,g,b,a" */
    private String colorValue(Color color)
    {  return color.getRed() + "," + color.getGreen() + ","
                             + color.getBlue() + "," + color.getAlpha();
    }

    /** Compare the cell parameters of two items */
    protected boolean isEqual(Item item)
    {  if (!background.equals(item.background)) return false;
       if (!foreground.equals(item.foreground)) return false;
       if (fontSize != item.fontSize) return false;
       if (fontStyle != item.fontStyle) return false;
       if (!size.equals(item.size)) return false;
       if (fontName==null && item.fontName==null) return true;
       if (fontName!=null && item.fontName==null) return false;
       if (fontName==null && item.fontName!=null) return false;
       if (!fontName.equals(item.fontName)) return false;
       return true;
    }
    
}   // End of Item class

