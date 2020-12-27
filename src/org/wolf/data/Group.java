/*
 * Group.java
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.lib.DeepCopy;

/** Data for definitions and words */
public class Group extends Item implements Serializable, Cloneable
{  private static final long serialVersionUID=1L;

   protected Unit media;              // Gloss, indigenous, multimedia
   protected ArrayList<Item>columns;  // Column (Ontology, comments, categories)
   protected ArrayList<Item>rows;     // Rows (examples, definitions, comments)

   /** Constructor to set up word unit components */
   public Group(String type)
   {  super();
   
   	  type = normalizeCase(type);
      columns = new ArrayList<Item>();
      rows    = new ArrayList<Item>();
      media = new Unit(type);
   }

   /** Constructor to instantiate an item with same structure as another */
   public Group(Group group)
   {  
	  super(group);
      media = new Unit(group.media);
      columns = new ArrayList<Item>();
      Item item;
      for (int i=0; i<group.columns.size(); i++)
      {  item = group.columns.get(i);
         if (item instanceof Ontology)
         {  Ontology ontology = (Ontology)item;
            item = new Ontology(ontology);
         }
         if (item instanceof Comment)
         {  Comment comment = (Comment)item;
            item = new Comment(comment);
         }
         if (item instanceof Reference)
         {  Reference reference = (Reference)item;
            item = new Reference(reference);
         }
         columns.add(item);
      }
      rows = new ArrayList<Item>();
      for (int i=0; i<group.rows.size(); i++)
      {  item = group.rows.get(i);
         if (item instanceof Comment)
         {  Comment comment = (Comment)item;
            item = new Comment(comment);
         }
         if (item instanceof Group)
         {  Group definition = (Group)item;
            item = new Group(definition);
         }
         if (item instanceof Unit)
         {  Unit example = (Unit)item;
            item = new Unit(example);
         }
         rows.add(item);
      }
   }

   /** Change the type of group (normally from Gloss to definition)
    * 
    * @param type The new type
    */
   public void setType(String type)
   {
	   media.setCategory(type);
   }

   public ArrayList<Item> getColumns() { return columns; }
   public void addColumn(Item column) 
   { 
	   columns.add(column); 
   }

   public ArrayList<Item> getRows() { return rows; }
   public void addRow(Item row) { rows.add(row); }

   public Unit getMedia() { return media; }
   public void setMedia(Unit m) { media = m; }

   public String getType() { return media.getCategory(); }
   
   public void setIndigenousData(String code, String data)
   { 
	   media.getTranslationData().setIndigenousData(code,  data);
   }
   
   public void setIndigenousData(Language lang, String data)
   {
	   media.getTranslationData().setIndigenousData(lang,  data);
   }

   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc, File file) throws IOException
   {  
	  Element node = media.exportXML(doc, file);
      if (!node.getTagName().equals("example")) exportStyle(node);
      node.setAttribute("width", "" + media.getSize().width);

      ArrayList<String[]> attributes = new  ArrayList<String[]>();
      Element element = makeNode(doc, "columns", attributes);
      for (int i=0; i<columns.size(); i++)
      {   element.appendChild(columns.get(i).exportXML(doc, file)); }
      node.appendChild(element);

      element = makeNode(doc, "rows", attributes);
      for (int i=0; i<rows.size(); i++)
      {   element.appendChild(rows.get(i).exportXML(doc, file)); }
      node.appendChild(element);
      return node;
   }

   /** Configure this dictionary element based on XML specifications */
   public void importXML(Element node, File file) throws IOException
   {   importStyle(node);
       media.importXML(node, file);

       NodeList list = node.getChildNodes();
       Element element;
       String name;
       int len = list.getLength();
       for (int i=0; i<len; i++)
       {  try
          {   element = (Element)list.item(i);
              name = element.getNodeName();
              if (name.equals("rows"))     
              {  importList(rows, element, file); }
              
              if (name.equals("columns"))  
              {  importList(columns, element, file);  }
          }
          catch (Exception e) {}
       }
   }

   /**  Import a list of row or column entities */
   private void importList(ArrayList<Item> items, Element node, File file)
                                                             throws IOException
   {   NodeList itemList = node.getChildNodes();
       Element element;
       Item item;
       String  itemName;
       int len = itemList.getLength();
       for (int i=0; i<len; i++)
       {  try
          {   element = (Element)itemList.item(i);
              itemName = element.getNodeName();

              if (itemName.equals("classification"))
              {   item = new Comment();
                  items.add(item);
                  item.importXML(element, file);
              }
              if (itemName.equals("definition"))
              {   item = new Group("Definition");
                  items.add(item);
                  item.importXML(element, file);
              }
              if (itemName.equals("example"))
              {   item = new Unit("Example");
                  items.add(item);
                  item.importXML(element, file);
              }
              if (itemName.equals("ontology"))
              {   item = new Ontology();
                  items.add(item);
                  item.importXML(element, file);
              }
              
              if (itemName.equals("translations"))
              {   item = new Reference(element.getAttribute("title"));
                  items.add(item);
                  item.importXML(element, file);
              }
          }   catch (Exception e) {}
       }
   }   // End of importList()


    /** Create deep copy of this object */
    public @Override Object clone()  
    { 
    	return DeepCopy.copy(this); 
    }

   public @Override boolean equals(Object object)
   {   if (object instanceof Group)
       {   Group group = (Group)object;
           if (!media.equals(group.media)) return false;
           if (!columns.equals(group.columns)) return false;
           if (!rows.equals(group.rows)) return false;
           return true;
       }
       return false;
   }

   public @Override int hashCode()
   {  return media.hashCode() + columns.hashCode() + rows.hashCode(); }

}   // End of Group class
