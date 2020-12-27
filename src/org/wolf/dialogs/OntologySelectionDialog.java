/*
 * OntologySelectionDialog.java
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
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import org.wolf.data.OntologyData;
import org.wolf.data.OntologyNode;

/** Select ontology for a widget */
public class OntologySelectionDialog extends JDialog implements ActionListener
{   
	private static final long serialVersionUID = 1L;
	private final static int COLUMNS = 3;
    private final static int MAX_COL_SIZE = 25;

    private OntologyNode[]   selection;
    ArrayList<OntologyNode>  list;

    /** Constructor to set up the dialog panel
     * @param root The root frame to which to attach this dialog
     * @param ontology The object containing the GOLD ontology
     */
    public OntologySelectionDialog(JFrame root, OntologyData ontology)
    {   super(root, true);
        setModal(true);
        selection = null;

        setTitle("Please select your desirec ontology values");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocation(new Point(200,200));

        Container container = getContentPane();
        list = ontology.getOntologyList();
        JMenuBar panel = makeOntologyPanel(list);
        container.add(panel);
        pack();
        Point point = root.getLocation();
        setLocation(point);
        setVisible(true);
    }   // End of Constructor

    /** Create the popup menu for Gold ontology selections
     *
     * @param list A flat list of Ontology Nodes and leaf-children
     * @return The Created ontology popup menu
     */
    private JMenuBar makeOntologyPanel(ArrayList<OntologyNode> list)
    {   JMenuBar panel =  new JMenuBar();
        panel.setLayout(new GridLayout(0,COLUMNS,0,0));

        OntologyNode parentNode, childNode;
        JMenuItem item;
        JMenu menu;
        String childName, itemName;

        JMenuItem reset = new JMenuItem("Reset");
        reset.setBackground(Color.CYAN);
        reset.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        reset.addActionListener(this);
        panel.add(reset);

        ArrayList<OntologyNode> childrenNodes;
        ArrayList<JMenu> menus = new ArrayList<JMenu>();

        for (int i=0; i<list.size(); i++)
        {  parentNode = list.get(i);

           // Skip if this node disabled.
           if (parentNode.getDisabled()) continue;

           childrenNodes = parentNode.getChildren();

           int size = 0;
           for (int j=0; j<childrenNodes.size(); j++)
           {  childNode = childrenNodes.get(j);
              if (!childNode.getDisabled()) size++;
           }
           if (size==0) continue;  // Skip this parent if all children disabled

           menu = new JMenu(parentNode.getFormattedParent());
           menu.setName(parentNode.getFormattedParent());
           menu.addActionListener(this);
           menus.add(menu);

           int columns = size / MAX_COL_SIZE + 1;
           int sizePerCol = 0;
           sizePerCol = size / columns;
           int count = 0;
          

           for (int j=0; j<childrenNodes.size(); j++)
           {  childNode = childrenNodes.get(j);
              if (childNode.getDisabled()) continue;

              if (childNode.isLeaf())
              {    childName = childNode.getFormattedChild();
                   item = new JMenuItem(childNode.getFormattedChild());
                   item.setBackground(Color.CYAN);
                   item.setName(parentNode.getName()+";"+childNode.getName());
                   item.addActionListener(this);

                   if (count>0 && count%sizePerCol==0  &&  count/sizePerCol<columns)
                   {   itemName = parentNode.getFormattedParent()
                                     + " (Cont. from "
                                     + childName.split(" ")[0] + ")";
                       menu = new JMenu(itemName);
                       menu.addActionListener(this);
                       menus.add(menu);
                   }
                   count++;
                   menu.add(item);
              }
           }
        }

        // Force an even power of columns.
        int size = menus.size();
        int remainder = size - size / COLUMNS * COLUMNS;
        for (int i=0; i<COLUMNS - remainder; i++)
            menus.add(new JMenu(" "));

        size = menus.size();
        int span = menus.size() / COLUMNS;
        for (int i=0; i<span; i++)
        {  for (int c=span; c<=COLUMNS*span; c+=span)
           { panel.add(menus.get(c%(COLUMNS*span) + i)); }
        }
        return panel;
    }

    /** Get the ontology selection */
    public OntologyNode[] getSelection()  {  return selection;  }

    /** Respond to ontology selections (set the parent and child ontology names)
     *
     * @param event The event triggering the action
     */
    public void actionPerformed(ActionEvent event)
    { Object object = event.getSource();
      if (!(object instanceof JMenuItem)) return;

      selection = new OntologyNode[2];
      JMenuItem item = (JMenuItem)object;
      String name = item.getText();
      if (name.equals("Reset"))
      {  setVisible(false);
         dispose();
         return;
      }

      name = item.getName();
      String keys[] =  name.split(";");
      if (keys.length!=2) return;

      OntologyNode parent = find(keys[0]), child = null;
      if (parent!=null) child = findChild(parent, keys[1]);
      if (parent!=null && child!=null)
      {
          selection[0] = parent;
          selection[1] = child;
      }
      else selection = null;

      setVisible(false);
      dispose();
   }

    /** Perform a binary search to find the selected ontology node
     *
     * @param name The key of the node find
     * @return The OntologyNode object or null if not found
     */
    private OntologyNode find(String name)
    {   OntologyNode node;
        String parentName;
        int first = -1, last = list.size(), middle = (first + last)/2, compare;

        while (first + 1 < last)
        {  node = list.get(middle);
           parentName = node.getName();
           compare = name.compareTo(parentName);
           if (compare<0) last = middle;
           else if (compare>0) first = middle;
           else return node;
           middle = (first + last)/2;
        }
        return null;
    }   // End of find()

       /** Perform a binary search to find the selected ontology node
     *
     * @param parent OntologyNode object containing the child node
     * @param name The key of the child to find
     *
     * @return The OntologyNode object or null if not found
     */
    private OntologyNode findChild(OntologyNode parent, String name)
    {   String child;
        OntologyNode node;
        ArrayList<OntologyNode> children = parent.getChildren();
        int first=-1, last=children.size(), middle=(first+last)/2, compare;

        while (first + 1 < last)
        {  node = children.get(middle);
           child = node.getName();
           compare = name.compareTo(child);
           if (compare<0) last = middle;
           else if (compare>0) first = middle;
           else return node;
           middle = (first + last)/2;
        }
        return null;
    }   // End of findChild()
}       // End of OntologySelectionDialog class
