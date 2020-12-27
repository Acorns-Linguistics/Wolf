/**
 * ScrollableToolbar.java
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

package org.wolf.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.acorns.language.LanguageText;
import org.wolf.lib.Icons;

/** This class creates a JScrollbar component with a variable number of buttons with icons in it
 * 
 */
public class ScrollableToolbar extends JPanel implements ActionListener
{
 
	private static final long serialVersionUID = 1L;
	private final static int   ICONSIZE       = 20;
    private final static int   SCROLLBARSPACE = 20;
    private final static int   THICKNESS      = 4;
    private final static Color BLANK          = new Color(80,80,80);
    private final static Color ON             = Color.RED;
    private final static Color GLOSS          = Color.GREEN;
     
    private JPanel          buttonPanel;
    
    JButton activeButton;     // The button set as primary
    
    /** Constructor to create the scrollable toolbar panel 
     * 
     * @param orientation JScrollbar.HORIZONTAL, or JScrollbar.VERTICAL
     */     
    public ScrollableToolbar(int orientation)
    {
        setLayout(new BorderLayout());
         
        buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(80,80,80));
        if (orientation == JScrollBar.VERTICAL)
        {  buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));}
        else
        {  buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));}

        JScrollPane scrollpane = new JScrollPane(buttonPanel);
        scrollpane.getVerticalScrollBar().setUnitIncrement(30);

        add(scrollpane);
        
        Dimension size = new Dimension(500, ICONSIZE+SCROLLBARSPACE);
        setPreferredSize(size);
    }
    
    /** Reset the panel of buttons */
    public void reset() { buttonPanel.removeAll(); }
    
    /** Get the name of the active button 
     *  @return get the button that is active
     */
    public String getActiveButton() 
    {  if (activeButton == null) return "";
       
       String name = activeButton.getName();
       if (name == null) name = "";
       return name; 
    }
    
    /** Method to set the active button
     * 
     * @param name name of the button to become active
     * @return true if successful, false otherwise
     */
    public boolean setActiveButton(String name) 
    {
        int index = findButton(name, false);
        if (index<0) return false;
        
        JButton oldActive = activeButton;
        if (activeButton!=null) 
        {
            activeButton = null;
            createBorder(oldActive, oldActive.isSelected());
        }
        activeButton = (JButton)buttonPanel.getComponent(index);
        activeButton.setSelected(true);
        createBorder(activeButton, true);
        fireActiveButtonProperty(oldActive, activeButton);
        return true; 
    }
    
    /** Purge the list of button components
     * 
     * @param codes The names of buttons still needed
     */
    public void purgeButtons(Vector<String> codes)
    {
        String name;
        boolean found;
        
        if (codes==null) return;
        int count = buttonPanel.getComponentCount();
        if (count==0)
        {
            buttonPanel.setVisible(false);
            buttonPanel.setVisible(true);
            return;
        }
        
        for (int c=count-1; c>=0; c--)
        {
            found = false;
            name = buttonPanel.getComponent(c).getName();
            for (int n=0; n<codes.size(); n++)
            {  if (name.equals(codes.get(n))) { found = true; break; }  }
            if (!found) removeButton(name);
        }        
    }
    
    /** Add a button to the scrollable toolbar 
     * 
     * @param iconLabel The label that goes with this button
     * @param iconName The name of the icon to go into the button
     * @return The button that was added.
     */
    public JButton addButton(String iconLabel, String iconName)
    { JButton button = null;
      ImageIcon  icon = Icons.getImageIcon(iconName , ICONSIZE);
      Color buttonColor = new Color(255,255,204);
      button = new JButton(iconLabel, icon);
      button.setVerticalTextPosition(SwingConstants.TOP);
      button.setBackground(buttonColor);
      button.setName(iconLabel);
      String onceForRed = LanguageText.getMessage("dictionary", 2);
      String twiceForGreen = LanguageText.getMessage("dictionary", 3);
      button.setToolTipText
                 ("<html>" + onceForRed + "<br>" + twiceForGreen + "</html>");
      button.addActionListener(this);
         
      int buttonNo = findButton(iconLabel, true);
      if (buttonNo>=0)
      {  buttonPanel.add(button, buttonNo);
         createBorder(button, false);
      }
      activeButton = null;
      buttonPanel.setVisible(false);
      buttonPanel.setVisible(true);
      return button;
    }    // End of addButton()
    
    /** Method to remove a language button from the button panel
     * 
     * @param iconLabel Name of the button
     * @return true if successful, false otherwise
     */
    public boolean removeButton(String iconLabel)
    {
        int buttonNo = findButton(iconLabel, false);
        if (buttonNo < 0) return false;
        
        JButton button = (JButton)buttonPanel.getComponent(buttonNo);
        buttonPanel.remove(buttonNo);
        if (button==activeButton) fireActiveButtonProperty(button, null);
        activeButton = null;
        buttonPanel.setVisible(false);
        buttonPanel.setVisible(true);
        return true;
        
    }   // End of removeButton()
    
   /** Method to 
    
  /** Method to create the correct border for a button
   * 
   * @param button The button to which to attach the border
   * @param select true if button should be selected
   */
   private void createBorder(JButton button, boolean select)
   {
       Border inner, outer, compound;
       
       if (button == activeButton)
       {      outer = BorderFactory.createLineBorder(GLOSS, THICKNESS);  }
       else { outer = BorderFactory.createLineBorder(BLANK, THICKNESS); }
       
       if (select) { inner = BorderFactory.createLineBorder(ON, THICKNESS); }
       else        { inner = BorderFactory.createLineBorder(BLANK, THICKNESS); }

       compound = BorderFactory.createCompoundBorder(outer, inner);
       button.setBorder(compound);
       
   }    // End of createBorder() method    
    
    /** Return the index where to insert a particular button
     * 
     * @param iconLabel The name of the button
     * @param insert true if inserting a new button
     * @return The buttons index or -1 if error
     */
    private int findButton(String iconLabel, boolean insert)
    {
        if (iconLabel==null) return -1;
        
        String componentLabel;
        int count = buttonPanel.getComponentCount();
        for (int c=0; c<count; c++)
        {
            componentLabel = buttonPanel.getComponent(c).getName();
            if (componentLabel.equals(iconLabel))
            {   if (insert) return -1;
                else        return c;
            }
            if (componentLabel.compareTo(iconLabel)>=0) 
            {   if (insert) return c; 
                else        return -1;
            }
        }
        if (insert) return count;
        else        return -1;
    }
    
    /** Method to change selected status */
    private void fireSelectedButtonProperty(JButton button)
    {
        boolean nowSelected = button.isSelected();
        firePropertyChange("select", !nowSelected, nowSelected);
    }
    /** Method to fire a property when the active button changes 
     * 
     * @param oldActive The button that was active
     * @param newActive The button that is now active
     */
    private void fireActiveButtonProperty(JButton oldActive, JButton newActive)
    {
        String prev = "", now = "";
        if (oldActive != null) prev = oldActive.getName();
        if (newActive != null) now  = newActive.getName();
        this.firePropertyChange("active", prev, now);        
    }
    
    public void actionPerformed(ActionEvent event)
    {
        JButton button = (JButton)event.getSource();
        JButton oldActive = activeButton;
        
        if (button.isSelected())
        {
            if (button == activeButton) 
            {
                activeButton = null;
                createBorder(button, false);
                button.setSelected(false);
                fireSelectedButtonProperty(button);
            }
            else 
            {
                if (oldActive!=null) 
                {
                    activeButton = null;
                    createBorder(oldActive, oldActive.isSelected());
                }
                activeButton = button;
                createBorder(button, true);
            } 
        }
        else
        {
            createBorder(button, true);
            button.setSelected(true);
            fireSelectedButtonProperty(button);
        }
        if (activeButton != oldActive)
            fireActiveButtonProperty(oldActive, activeButton);
    }
    
    /** Method to return the codes of those buttons selected, but not active
     * 
     * @return The string of selected button codes
     */
    public String[] getSelectedButtons()
    {
        Vector<String> selectedButtons = new Vector<String>();
        int count = buttonPanel.getComponentCount();
        JButton button;
        Component component;
        String code;
        
        for (int c=count-1; c>=0; c--)
        {
            component = buttonPanel.getComponent(c);
            if (component instanceof JButton)
            {
                button = (JButton)component;
                code   = button.getName();
                if (button!=activeButton && button.isSelected()) 
                { selectedButtons.add(0, code); }
            }
        }    
        return selectedButtons.toArray(new String[selectedButtons.size()]);        
    }

    /** Mark the list of buttons selected */
    public void setSelectedButtons(String[] buttonList)
    {   if (buttonList==null) return;
        
        int index;
        for (int i=0; i<buttonList.length; i++)
        {   index = findButton(buttonList[i], false);
            if (index<0) continue;

            JButton button = (JButton)buttonPanel.getComponent(index);
            createBorder(button, true);
            button.setSelected(true);
        }
    }

}       // End of ScrollableToolbar class
