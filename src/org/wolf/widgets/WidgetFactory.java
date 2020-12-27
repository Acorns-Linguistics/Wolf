/*
 * WidgetFactory.java
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
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.wolf.data.Comment;
import org.wolf.data.Group;
import org.wolf.data.Item;
import org.wolf.data.Ontology;
import org.wolf.data.Reference;
import org.wolf.data.Unit;
import org.wolf.lib.DeepCopy;

/** Maintain a list of widgets to use in the dictionary workspacce */
public class WidgetFactory
{
   public final static int GROUP     = 0;
   public final static int ONTOLOGY  = 1;
   public final static int REFERENCE = 2;
   public final static int TEXTAREA  = 3;
   public final static int UNIT      = 4;
   public final static int SIZE      = 5;

   private static ArrayList<JComponent>[] componentLists;

   /** Method to create a desired type of widget
    *
    * @param item The object containing data defining the widget
    * @return Return the created component
    */
   public static JComponent createWidget(Item item)
   {   createWidgetList();
       Dimension size = item.getSize();
       String title;

       JComponent component = null;
       int listNo = -1;

       // References to words in alternate languages
       if (item instanceof Reference)
       {  
    	  listNo = REFERENCE;
       	  title = ((Reference)item).getTitle();
          if (componentLists[REFERENCE].isEmpty())
          { 
        	  ReferenceWidget referenceWidget = new ReferenceWidget(size.width, title);
              componentLists[REFERENCE].add(referenceWidget);
          }
       }
       
       // Gold ontology widgets
       if (item instanceof Ontology)
       {  listNo = ONTOLOGY;
          Ontology ontology = (Ontology)item;
          if (componentLists[ONTOLOGY].isEmpty())
          {  OntologyWidget ontologyWidget
                              = new OntologyWidget(ontology.getType());
             componentLists[ONTOLOGY].add(ontologyWidget);
          }
       }

       // Column and row widgets
       if (item instanceof Comment)
       {  listNo = TEXTAREA;
          if (componentLists[TEXTAREA].isEmpty())
          {   component = new TextAreaWidget(item);  }
       }

       // Definition widgets
       if (item instanceof Group)
       {  listNo = GROUP;
          if (componentLists[GROUP].isEmpty())
          {   GroupWidget group = new GroupWidget(false);
              componentLists[GROUP].add(group);
          }
       }

       // Example widgets
       if (item instanceof Unit)
       {  
    	  listNo = UNIT;
          if (componentLists[UNIT].isEmpty())
          {    Unit unit = (Unit)item;
               DictionaryWidget dictionaryWidget
                       = new DictionaryWidget(size.width, unit.getCategory());
               componentLists[UNIT].add(dictionaryWidget);
          }
       }

       Class<?> widgetClass;
       Method formatCell;
       Class<?>[] params = new Class[1];
       params[0] = Item.class;
       try
       {   if (component==null && listNo>=0)
           {  component = componentLists[listNo].get(0);
              widgetClass = component.getClass();
              formatCell = widgetClass.getMethod("formatCell", params);
              formatCell.invoke(component, DeepCopy.copy(item));

              componentLists[listNo].remove(0);
           }
       }  catch (Exception e) 
       {
    	   e.printStackTrace();
       }
       return component;
   }

   /** Make a released Widget available  for reallocation */
   public static void releaseComponent(JComponent component)
   {   int listNo = -1;
       if (component instanceof GroupWidget)      listNo = 0;
       if (component instanceof OntologyWidget)   listNo = 1;
       if (component instanceof ReferenceWidget)  listNo = 2;
       if (component instanceof TextAreaWidget)   listNo = 3;
       if (component instanceof DictionaryWidget) listNo = 4;

       if (listNo>=0) componentLists[listNo].add(component);
   } 

   /** Create the array of available widgets */
   @SuppressWarnings("unchecked")
   private static void createWidgetList()
   {  if (componentLists==null)
      {  componentLists = new ArrayList[SIZE];
         for (int  i=0; i<SIZE; i++)
         {  componentLists[i] = new ArrayList<JComponent>();   }
      }
   }
}           // End of WidgetFactory class
