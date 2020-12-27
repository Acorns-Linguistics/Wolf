/*
 * OntologyWidget.java
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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.wolf.data.Constants;
import org.wolf.data.Item;
import org.wolf.data.Ontology;
import org.wolf.data.OntologyData;
import org.wolf.data.OntologyNode;
import org.wolf.dialogs.OntologySelectionDialog;
import org.wolf.system.Environment;

/** Create GUI components to represent the GOLD ontology  */
public class OntologyWidget extends TemplateWidget 
                                            implements Constants, MouseListener
{
	private static final long serialVersionUID = 1L;

	private final static Dimension[] sizes
            = { new Dimension(100,1), new Dimension(150, 1),
                new Dimension(100,2), new Dimension(100, 2),
                new Dimension(250,2), new Dimension(100, 1),
              };

    private JLabel parent, value;
    private JTextField data;
    private String abrv;

    private int type;

    /** Constructor to parse the gold ontology and prepare for creating GUIs */
    public OntologyWidget(int type)
    {   super();
        this.type = type;

        GridBagLayout gridBag = new GridBagLayout();
        setLayout(gridBag);

        parent = new JLabel("");
        parent.addMouseListener(this);
        parent.setVerticalAlignment(SwingConstants.CENTER);
        parent.setHorizontalAlignment(SwingConstants.CENTER);
        parent.setBorder(makeBorder(0));

        value  = new JLabel("");
        value.addMouseListener(this);
        value.setVerticalAlignment(SwingConstants.CENTER);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        value.setBorder(makeBorder(0));

        data   = new JTextField("");
        data.setBorder(makeBorder(0));
        setBorder(makeBorder(3));
        setInitialWidgetSize(sizes[type], type);
        reset();
        initialize(type);

    }   // End of OntologyWidget constructor

    /** Method to initialize the widget */
    private void initialize(int type)
    {   GridBagLayout gridBag = (GridBagLayout)getLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridy = 0;
        c.gridx = 0;

        int height = getFieldHeight();
        Dimension size = new Dimension(sizes[type]);
        size.height *= height;
        switch (type)
        {   case ONTOLOGY_VALUE:
                gridBag.setConstraints(value, c);
                add(value);
                break;

            case ONTOLOGY_PARENTVALUE:
                gridBag.setConstraints(parent, c);
                add(parent);
                c.gridx = 1;
                gridBag.setConstraints(value, c);
                add(value);
                break;

            case ONTOLOGY_PARENT_VALUE:
                gridBag.setConstraints(parent, c);
                add(parent);
                c.anchor = GridBagConstraints.WEST;
                c.gridy = 1;
                gridBag.setConstraints(value, c);
                add(value);
                break;

            case ONTOLOGY_VALUE_DATA:
                gridBag.setConstraints(value, c);
                add(value);
                c.gridy = 1;
                gridBag.setConstraints(data, c);
                add(data);
                break;

            case ONTOLOGY_PARENTVALUE_DATA:
                gridBag.setConstraints(parent, c);
                add(parent);

                c.gridx = 1;
                gridBag.setConstraints(value, c);
                add(value);

                c.gridx = 0;
                c.gridy = 1;
                c.gridwidth = 2;
                gridBag.setConstraints(data, c);
                add(data);
                break;
                
            case ONTOLOGY_DATA:
                gridBag.setConstraints(data, c);
                add(data);
                break;
        }
        setFieldSizes();
    }

    /** Initialize the sizes of the ontology widget */
    private void setInitialWidgetSize(Dimension size, int height)
    {   size = new Dimension(size);
        size.height = getFieldHeight() * sizes[type].height;

        Insets insets = getInsets();
        size.width += (insets.left + insets.right);
        size.height += (insets.top + insets.bottom);
        setPreferredSize(size);
        setSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

    }

    /** Update the sizes of the components */
    private void setFieldSizes()
    {   
    	Insets insets = getInsets();
        Dimension size = getPreferredSize();
        size.width -= (insets.left + insets.right);
        size.height -= (insets.top + insets.bottom);
        Dimension dataSize = new Dimension(size);

        switch(type)
        {
            case ONTOLOGY_PARENTVALUE_DATA:
                size.width /=2;

            case ONTOLOGY_PARENT_VALUE:
            case ONTOLOGY_VALUE_DATA:
                size.height /= 2;
                dataSize.height /= 2;
                break;

            case ONTOLOGY_DATA:
            	dataSize.height *= 2;
            	break;

            case ONTOLOGY_PARENTVALUE:
                size.width /= 2;

            case ONTOLOGY_VALUE:
        }
        parent.setPreferredSize(size);
        value.setPreferredSize(size);
        data.setPreferredSize(dataSize);
    }

    /** Get the data after user interaction */
    public Item updateCell()
    {  
       Ontology ontology  = new Ontology();

       storeCellCharacteristics(ontology);
       ontology.setParent(parent.getText());
       ontology.setValue(value.getText());
       ontology.setData(getPhoneticText(this, false));
       ontology.setPhonetics(getPhoneticText(this, true));
       ontology.setAbbrev(abrv);
       ontology.setType(type);
       return ontology;
    }

    /** Format a cell for display */
    public void formatCell(Item item)
    {  Ontology ontology = (Ontology)item;

       Dimension size = item.getSize();
       type = ontology.getType();
       if (size==null)
       {  setInitialWidgetSize(sizes[type], type);
          item.setSize(getSize());
       }
       else if (size.height==-1)
       {  Dimension newSize = new Dimension(sizes[type]);
          newSize.width = size.width;
          setInitialWidgetSize(newSize, type);
          item.setSize(getSize());
       }
       loadCellCharacteristics(ontology);
       parent.setText(ontology.getParent());
       value.setText(ontology.getValue());
       resetText(ontology.getData(), ontology.getPhonetics());
       setPhoneticComponent(getToggleComponent());
       abrv = ontology.getAbbrev();
       removeAll();
       initialize(type);
    }

    /** Update the width of the cell */
    public void setCellWidth(Integer width)
    {  Dimension size = getPreferredSize();
       size.width = width;
       setPreferredSize(size);
       setFieldSizes();
    }

   /** Return the component that displays phonetics and indigenous text */
   public JTextField getToggleComponent() { return data; }

    /** Create a border containing insets */
    private Border makeBorder(int w)
    {   Border line = BorderFactory.createLineBorder(Color.GRAY);
        Border empty = BorderFactory.createEmptyBorder(w,w,w,w);
        return BorderFactory.createCompoundBorder(line, empty);
    }
    
   
    /** Method to reset ontology widget to its initial state */
    private void reset()
    {   String toolTip = "Invoke popup menu to select ontology";
        String dataToolTip = "Enter data associated with ontology selection";
        value.setText("");
        value.setToolTipText(toolTip);
        parent.setText("");
        parent.setToolTipText(toolTip);
        data.setText("");
        data.setToolTipText(dataToolTip);
        resetText("","");
        abrv = "";

    }

    /** Show dialog menu when clicked */
    public void mouseReleased(MouseEvent event)
    {  OntologyData ontology = getEnv().getOntologyData();
       JFrame root = Environment.getRootFrame();
       WordListWidget table = (WordListWidget)getTable();
       OntologySelectionDialog dialog 
               = new OntologySelectionDialog(root, ontology);
       OntologyNode[] nodes = dialog.getSelection();
       processOntologySelection(nodes);
       table.updateWord();
    }

    public void mouseClicked(MouseEvent event) {}
    public void mousePressed(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseEntered(MouseEvent event)  {}

    /** Respond to ontology selections (set the parent and child ontology names)
     *
     * @param event The event triggering the action
     */
    public void processOntologySelection(OntologyNode[] nodes)
    { 
      if (nodes==null) return;
      if (nodes[0]==null) {  reset(); return; }

      OntologyNode node = nodes[0];
      if (node != null)
      {  abrv = node.getAbbreviation();
         if (abrv.equals(node.getName()))
               parent.setText(node.getFormattedParent());
         else  parent.setText(abrv);
         parent.setToolTipText(node.getName());

         OntologyNode child = nodes[1];
         if (child!=null)
         {   abrv = child.getAbbreviation();
             if (abrv.equals(child.getName()))
                  value.setText(child.getFormattedChild());
             else value.setText(abrv);
             value.setToolTipText(child.getName());
         }

         // Adjust component size based on input
         Dimension size = new Dimension(sizes[type]);
         size.height *= getFieldHeight();

         FontMetrics metrics = parent.getFontMetrics(parent.getFont());
         int padding = metrics.getMaxAdvance();
         if (padding<0) padding = 24;
         int parentWidth = metrics.stringWidth(parent.getText()) + padding;
         int valueWidth = metrics.stringWidth(value.getText()) + padding;

         int width = valueWidth;
         if (width<parentWidth) width = parentWidth;
         switch (type)
         {   case ONTOLOGY_PARENTVALUE_DATA:
             case ONTOLOGY_PARENTVALUE:
                 width = 2 * width;
                 break;
             case ONTOLOGY_PARENT_VALUE:
                 if (valueWidth<parentWidth) width = parentWidth;
                 break;
             case ONTOLOGY_DATA:
            	 width = 2*width;
         }

         if (width > size.width) size.width = width;
         setPreferredSize(size);
         setMinimumSize(size);
         setMaximumSize(size);
         setSize(size);
      }
    }     // End of actionPerformed()
}         // End of OntologyWidget class
