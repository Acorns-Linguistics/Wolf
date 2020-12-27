/*
 * Comment.java
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

import java.awt.Dimension;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wolf.lib.DeepCopy;

/**  Dictionary comment */
public class Comment extends Item implements Serializable, Cloneable
{   private static final long serialVersionUID = 1;

    String comment, phonetics, title;
    boolean expanded;

    public Comment() 
    { 
    	super(); 
    	comment = phonetics = title = ""; 
    }
    
    public Comment(String title, String comment)
    {
    	super();
        setTitle(title);
        setComment(comment);
        phonetics = "";
    	
    }
    
    public Comment(String title, Dimension size)
    {   super();
        setTitle(title);
        setSize(size);
        comment = phonetics = "";
    }
    public Comment(Comment commentObject)
    {  super(commentObject);
       setTitle(commentObject.title);
       setSize(commentObject.getSize());
       comment = phonetics = "";
    }

    public String getTitle() { return title; }
    public void setTitle(String t) { title = t; }
    public String getComment() { return comment; }
    public void setComment(String c) { comment = c; }
    public String getPhonetics() { return phonetics; }
    public void setPhonetics(String p) { phonetics = p; }
    public boolean isExpanded() { return expanded; }
    public void setExpanded(boolean e) { expanded = e;}

   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc, File file)
   {  ArrayList<String[]> attributes = new  ArrayList<String[]>();
      attributes.add(new String[]{"title", title.trim()});
      attributes.add(new String[]{"phonetics", phonetics.trim()} );
      Element node = makeNode(doc, "classification", attributes);
      exportStyle(node);
      node.setTextContent(comment.trim());
      return node;
   }

   /** Configure this dictionary element based on XML specifications */
   public void importXML(Element node, File file)
   {   importStyle(node);
       title = node.getAttribute("title").trim();
       phonetics = node.getAttribute("phonetics").trim();
       comment = node.getTextContent().trim();
   }


    public @Override Object clone()  {  return DeepCopy.copy(this);   }

   public @Override boolean equals(Object object)
   {   if (object instanceof Comment)
       {   Comment commentObject = (Comment)object;
           if (!super.isEqual(commentObject)) return false;
           if (!comment.equals(commentObject.comment)) return false;
           if (!phonetics.equals(commentObject.phonetics)) return false;
           if (!title.equals(commentObject.title)) return false;
           return true;
       }
       return false;
   }

   public @Override int hashCode()
   {  return (comment + phonetics + title).hashCode() + super.hashCode();  }
}
