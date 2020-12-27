/*
 * OntologyDialog.java
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import org.wolf.data.DictionaryData;
import org.wolf.data.OntologyData;
import org.wolf.data.OntologyNode;

/** Component for displaying the ontology and entering abbreviations */
public class OntologyDialog extends JDialog implements WindowListener
{   
	private static final long serialVersionUID = 1L;

	boolean confirm;
    OntologyTree tree;

    /** Customize the GOLD ontology
     *
     * @param root The root frame to which to attach this dialog
     * @param dictionary The current dictionary object
     */
    public OntologyDialog(JFrame root, DictionaryData dictionary)
    {   super(root, true);
        setTitle("Please enter the changes to your ontology");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        tree = new OntologyTree(dictionary, false);
        JScrollPane scroll = new JScrollPane(tree);
        Dimension panelSize = new Dimension(500,700);
        scroll.setSize(panelSize);
        scroll.setPreferredSize(panelSize);
        scroll.setMaximumSize(panelSize);
        add(scroll);

        addWindowListener(this);
        pack();
        Point point = root.getLocation();
        setLocation(point);
        setVisible(true);
    }


    public void windowActivated(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowClosing(WindowEvent event)
    {  tree.getCellEditor().stopCellEditing();  }
    public void windowIconified(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}


   /** Method to determine if operation was successful
    *
    * @return true if successful, false otherwise
    */
    public boolean dialogConfirmed() { return confirm; }

    class OntologyTree extends JTree
    {   
		private static final long serialVersionUID = 1L;

	/** Constructor to create a JTree component
         *
         * @param environment The object containing dictionary data structures
         * @param all true if to build the complete tree, false if bottom level
         */
        public OntologyTree(DictionaryData dictionary, boolean all)
        {   OntologyData ontology = dictionary.getOntologyData();
            setCellRenderer(new OntologyCellRenderer());
            setCellEditor(new OntologyCellEditor(this));

            DefaultMutableTreeNode root;
            if (all)
            {   OntologyNode tree = ontology.getOntologyTree();
                root = makeTree(tree);
            }
            else
            {   ArrayList<OntologyNode> list = ontology.getOntologyList();
                root = new DefaultMutableTreeNode("");
                DefaultMutableTreeNode parent;
                ArrayList<OntologyNode> children;
                for (int i=0; i<list.size(); i++)
                {  parent = new DefaultMutableTreeNode(list.get(i));
                   root.add(parent);

                   children = list.get(i).getChildren();
                   for (int j=0; j<children.size(); j++)
                   {  parent.add(new DefaultMutableTreeNode(children.get(j)));  }
                }
            }

            DefaultTreeModel model  = (DefaultTreeModel)getModel();
            model.setRoot(root);
            model.nodeChanged(root);
            model.reload();

            for (int i = 0; i < getRowCount(); i++) { expandRow(i);  }
            setEditable(true);
        }

        /** Build the ontology tree in depth first manner */
        private DefaultMutableTreeNode makeTree(OntologyNode root)
        {   if (root==null) return null;

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(root);
            ArrayList<OntologyNode> children = root.getChildren();

            if (children==null) return node;

            OntologyNode child;
            DefaultMutableTreeNode childNode;
            for (int i=0; i<children.size(); i++)
            {   child = children.get(i);
                childNode = makeTree(child); // Recursive depth first call
                node.add(childNode);
            }
            return node;
        }

        /** OntologyTree node components */
        private class OntologyTreeCellObject
        {   private JLabel     label;
            private JTextField field;
            private JPanel     panel;
            private JCheckBox  check;

            public OntologyTreeCellObject()
            {   Dimension size = new Dimension(200, 20);
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
                panel.add(Box.createHorizontalGlue());

                label = new JLabel("");
                label.setPreferredSize(new Dimension(size));
                label.setMaximumSize(getPreferredSize());
                panel.add(label);
                panel.add(Box.createHorizontalStrut(5));

                field = new JTextField();
                field.setPreferredSize(new Dimension(size));
                field.setMaximumSize(getPreferredSize());
                panel.add(Box.createHorizontalStrut(5));

                check = new JCheckBox();
                panel.add(check);

                panel.add(field);
                panel.add(Box.createHorizontalGlue());

            }

            public String getAbbrev()     
            { return field.getText(); }
            public boolean getSelected()  { return check.isSelected(); }
            public JPanel getPanel()      { return panel; }
            public void addKeyListener(KeyListener listener)
            { field.addKeyListener(listener); }
            public void addItemListener(ItemListener listener)
            {check.addItemListener(listener); }

            /** Format the object for rendering or editing
             *
             * @param node The Ontology Node object
             */
            public void formatCell(OntologyNode node)
            {   OntologyNode parent = node.getParent();
                String formatName;
                if (parent!=null)
                     formatName = node.getFormattedChild();
                else formatName = node.getFormattedParent();
                label.setText(formatName);

                String name = node.getName();
                String abrv = node.getAbbreviation();
                if (name.equals(abrv))
                     field.setText(formatName);
                else field.setText(abrv);

                ItemListener[] listeners = check.getItemListeners();
                for (int i=0; i<listeners.length; i++)
                    check.removeItemListener(listeners[i]);
                check.setSelected(node.getDisabled());
                for (int i=0; i<listeners.length; i++)
                    check.addItemListener(listeners[i]);
            }
        }

        /** Class to enable editing of Ontology Tree Cells */
        private class OntologyCellEditor
                             implements TreeCellEditor, KeyListener, ItemListener
        {   private boolean escEntered;
            private OntologyTreeCellObject cellObject;
            private OntologyNode node;
            private ArrayList<CellEditorListener> listeners;


            /** Constructor to create the editor component and listener list */
            public OntologyCellEditor(JTree tree)
            {  
               escEntered = true;
               cellObject = new OntologyTreeCellObject();
               cellObject.addKeyListener(this);
               cellObject.addItemListener(this);
               listeners  = new ArrayList<CellEditorListener>();
            }

            /** Get the Tree cell component to edit a node */
            public Component getTreeCellEditorComponent(JTree tree, Object value,
                    boolean isSelected, boolean expanded, boolean leaf, int row)
            {   if (value instanceof DefaultMutableTreeNode)
                {  DefaultMutableTreeNode mutable = (DefaultMutableTreeNode)value;
                   Object object = mutable.getUserObject();
                   if (object instanceof OntologyNode)
                   {   node = (OntologyNode)object;
                       cellObject.formatCell(node);
                   }
                }
                return cellObject.getPanel();
            }

            /** Any cell edited */
            public boolean isCellEditable(EventObject anEvent) { return true; }

            /** Any cell can be selected */
            public boolean shouldSelectCell(EventObject event)
            {  escEntered = false;
               return true;
            }

            /** Always can stop editing a cell.
             *    When it does, update the OntologyNode object when stopping */
            public boolean stopCellEditing()
            {  if (node==null) return true;
               node.setAbbreviation(cellObject.getAbbrev());
               node.setDisabled(cellObject.getSelected());
               return true;
            }

            /** On cancel, we don't change the OntologyNode object */
            public void cancelCellEditing()  {}

            /** Add a new listener */
            public void addCellEditorListener(CellEditorListener listener)
            { listeners.add(listener);
            }

            /** Remove a listener */
            public void removeCellEditorListener(CellEditorListener listener)
            { listeners.remove(listener);
            }

            /** Method to fire editing stopped events
                   For example when the user types the enter key
             */
            protected void fireEditingStopped()
            {   if (listeners.size() > 0)
                {  ChangeEvent ce = new ChangeEvent(this);
                   for (int i= listeners.size()-1; i>=0; i--)
                   { listeners.get(i).editingStopped(ce); }
               }
            }

            /** Get the editing cell for updating the tree */
            public Object getCellEditorValue()
            {  if (!escEntered)
               {  node.setAbbreviation(cellObject.getAbbrev());
                  node.setDisabled(cellObject.getSelected());
               }
               return node;
            }

            /** Listen for check box changes (update the node) */
            public void itemStateChanged(ItemEvent event)
            {  fireEditingStopped();  }

            /** Listen for escapes */
            public void keyTyped(KeyEvent event)
            { handleEvent(event);  }

            /** Listen for escapes */
            public void keyPressed(KeyEvent event)
            { handleEvent(event);  }

            /** Listen for escapes */
            public void keyReleased(KeyEvent event)
            { handleEvent(event);  }

            /** Handle key press events in a common manner */
            private void handleEvent(KeyEvent event)
            {   int code = event.getKeyCode();
                escEntered = (code ==KeyEvent.VK_ESCAPE);
                if (code==KeyEvent.VK_ENTER) fireEditingStopped();
            }
        }

        /** Render JTree OntologyNode components */
        private class OntologyCellRenderer implements TreeCellRenderer
        {  private OntologyTreeCellObject cellObject;
           private DefaultTreeCellRenderer defaultRenderer;

           /** Constructor to create the renderer component */
           public OntologyCellRenderer()
           {    cellObject = new OntologyTreeCellObject();
                defaultRenderer = new DefaultTreeCellRenderer();
           }  // End of constructor

           /** Render the OntologyNode component, showing the abbreviation */
           public Component getTreeCellRendererComponent
                   (JTree tree, Object value, boolean selected, boolean expanded,
                                boolean leaf, int row, boolean hasFocus)
           {
              Component returnComponent = null;
              if ((value != null) && (value instanceof DefaultMutableTreeNode))
              {  DefaultMutableTreeNode rendNode = (DefaultMutableTreeNode) value;
                 Object object = rendNode.getUserObject();

                 if (object instanceof OntologyNode)
                 {  OntologyNode node = (OntologyNode)object;
                    cellObject.formatCell(node);
                    return cellObject.getPanel();
                 }
              }  // End if

              // Handle case where we couldn't render the entry.
              if (returnComponent == null)
              {  returnComponent = defaultRenderer.getTreeCellRendererComponent
                      (tree, value, selected, expanded, leaf, row, hasFocus);
              }
              return returnComponent;
           }    // End of getTreeCellRendererComponent
        }       // End of OntologyCellRenderer
    }       // End of OntologyTree class
}           // End of OntologyDialog class
