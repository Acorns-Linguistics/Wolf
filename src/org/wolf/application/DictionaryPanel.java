/*
 *   class DictionaryPanel.java
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
package org.wolf.application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.acorns.audio.SoundDefaults;
import org.acorns.visual.ColorScheme;
import org.wolf.components.ScrollableToolbar;
import org.wolf.data.FormatData;
import org.wolf.data.Language;
import org.wolf.lib.Icons;
import org.wolf.system.Environment;

public class DictionaryPanel extends JPanel 
{
    private final static int ICONSIZE  = 30;
    private final static int STRUTSIZE = 30;  // South panel size.

    /** Code for ghosting the undo button */
    public final static int UNDO = 0;
    /** Code for ghosting the redo button */
    public final static int REDO = 1;
    /** Code for ghosting the back button */
    public final static int BACK = 2;
    /** Code for ghosting the forward button */
    public final static int FORWARD = 3;
    /** Code for ghosting the close button */
    public final static int CLOSE = 4;
    /** Code for ghosting the Save button  */
    public final static int SAVE = 5;
    /** Code for ghosting the Export button */
    public final static int EXPORT = 6;
    /** Code for ghosting the Mobile application button */
    public final static int MOBILE = 7;
    /** Code for ghosting the Web page button */
    public final static int WEB = 8;
    /** Code for ghosting the Print button */
    public final static int PRINT = 9;
    /** Code for ghosting the Print preview button */
    public final static int PREVIEW = 10;
    /** Code for ghosting the Reload button */
    public final static int RELOAD = 11;
    /** Code for ghosting the Toggle button */
    public final static int TOGGLE = 12;
    /** Code for ghosting the Sort button */
    public final static int SORT = 13;
    /** Number of buttons that can be ghosted */
    
    public final static int ENABLABLE_BUTTONS = 14;
    
    private final static Dimension LABELSIZE = new Dimension(500, 20);
        
    private DictionaryListener      dictionaryListener;
    private RootDictionaryPanel     rootPanel;
    private DictionaryDisplayPanel  dictionaryPanel;
    private Language				searchLanguage;

    private JButton[] enablableButtons;
    private JLabel    searchLabel;
    private JTextField field;

    private static final long serialVersionUID=1L;
    
    /** Creates a new instance of DictionaryPanel 
     * 
     * @param annotate Type of dictionary (presently only 'd' supported)
     * @param colors   color scheme for the background color
     * @param panelSize desired size fot this panel
     */
     
    public DictionaryPanel
            (RootDictionaryPanel root, ColorScheme colors, Dimension panelSize)
    {   
    	enablableButtons = new JButton[ENABLABLE_BUTTONS];

        setLayout(new BorderLayout());
        setBackground(SoundDefaults.DARKBACKGROUND);
        if (panelSize!=null) setPreferredSize(panelSize);
        
        // Create the label for error messages.
        JLabel message = getProperties().getErrorLabel();
        message.setForeground(SoundDefaults.ERROR);
        message.setToolTipText
                ("Error and ACORNS Acknowledge Messages display Here");
        
        ScrollableToolbar toolbar = getProperties().getToolbar();
        dictionaryListener = new DictionaryListener(message, toolbar);

        
        // Create buttons to go into the north section of the panel.
        // First string is the text to show in the button.
        // Second string is the tooltip text.
        // Third string indicates the button group option and the button group.
        //    For example: "rf" indicates the reset command in the file group.
        // Fourth string indicates whether to create the button.
        //    "a" for dictionary mode, "s" for sound editing mode
        String[] north = 
        { "", "", "","d",
          "open", "open the indigenous dictionary", "lf", "d",
          "close", "close the open dictionary", "cf", "d"+ CLOSE,
          "import", "import the indigenous dictionary", "if", "d",
          "export", "export the indigenous dictionary", "ef", "d"+ EXPORT,
          "mobile", "create mobile app", "mf", "d"+ MOBILE,
          "web", "create web page", "wf", "d" + WEB,
          "save", "save the indegenous dictionary", "sf", "d" + SAVE,
          "", "", "","d",
          "print", "Print dictionary", "pp", "d" + PRINT,
          "printpreview", "View the dictionary", "vp", "d" + PREVIEW,
           "", "", "","d",
          "prev", "Go back to previous view", "bv", "d" + BACK,
          "next", "Go forward to next view", "fv", "d" + FORWARD,
          "reload", "Reload the dictionary's initial view","rv","d" + RELOAD,
          "","","","d",
       };
        JPanel northPanel = buttonPanel
                (north,BorderLayout.NORTH,BoxLayout.X_AXIS, 'd', null);

        Dimension  size  = new Dimension(400,20);
        field = new JTextField(40);
        field.setMaximumSize(size);
        field.setName("sw");
        field.addActionListener(dictionaryListener);
        northPanel.add(new JLabel("Search: "));
        northPanel.add(field);
        northPanel.add(Box.createHorizontalStrut(5));
        searchLabel = new JLabel("W", SwingConstants.CENTER);
        searchLabel.setToolTipText("Click to change search mode");
        searchLabel.setFont(new Font(null, Font.BOLD, 14));
        searchLabel.setBackground(Color.LIGHT_GRAY);
        searchLabel.setPreferredSize(new Dimension(30,30));
        searchLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        searchLabel.addMouseListener
                (  new MouseListener()
                   {  public void mousePressed(MouseEvent event)
                      {  Object component = event.getSource();
                         JLabel label = (JLabel)component;
                         String text = label.getText();
                         if (text.equals("W")) label.setText("C");
                         else if (text.equals("C")) label.setText("O");
                         else if (text.equals("O")) label.setText("W");
                         setSearchFont(searchLanguage);
                      }
                      public void mouseReleased(MouseEvent event) {}
                      public void mouseEntered(MouseEvent event) {}
                      public void mouseExited(MouseEvent event) {}
                      public void mouseClicked(MouseEvent event) {}
                   }
                );
        northPanel.add(searchLabel);
        northPanel.add(Box.createHorizontalStrut(STRUTSIZE));
        
        
        FormatData templates = getEnv().getTemplateData();
        northPanel.add(templates.getTemplateCombo());
        northPanel.add(Box.createHorizontalGlue());
        
        makeButton(northPanel, "edit", "Set dictionary options", "do", -1);
         
        // Create buttons to go into the west section of the panel.
        String[] west = 
        { "flip", "Toggle phonetics and indigenos text", "ts", "d" + TOGGLE,
          "sort", "Sort the dictionary alphabetically", "ss", "d" + SORT,
          "", "", "","d",
          "language", "Modify list of languages", "ls", "d",
          "copyright", "Enter copyright notice", "cs", "d",
          "author", "Modify list of authors", "as", "d",
          "document", "Templates for dictionary output format", "ds","d",
          "gold", "Customize the parsed GOLD ontology", "os", "d",
           "", "", "","d",
         };
        
        buttonPanel(west,BorderLayout.WEST,BoxLayout.Y_AXIS, 'd', null);
		      
        // Create dialog panel for the south section.
        JPanel sDialogPanel = new JPanel();
        sDialogPanel.setLayout(new BoxLayout(sDialogPanel, BoxLayout.X_AXIS));
        sDialogPanel.setBackground(SoundDefaults.BACKGROUND);
     
        // Create panel for the south section.
        JPanel sPanel = new JPanel();
        sPanel.setLayout(new BoxLayout(sPanel, BoxLayout.Y_AXIS));
        sPanel.setBackground(SoundDefaults.BACKGROUND);

        sDialogPanel.add(toolbar);
        sDialogPanel.add(Box.createHorizontalStrut(STRUTSIZE));
        sDialogPanel.add(Box.createHorizontalGlue());
        enablableButtons[UNDO] =
          makeButton(sDialogPanel, "undo", "undo the last operation", "uo", -1);
        enableButton(UNDO, false);
        enablableButtons[REDO] =
          makeButton(sDialogPanel, "redo", "redo the last operation", "ro", -1);
        enableButton(REDO, false);

        sPanel.add(sDialogPanel);
        
        // Create message panel and add it.
        JPanel mPanel = new JPanel();
        
        mPanel.setPreferredSize(LABELSIZE);
        mPanel.setMinimumSize(LABELSIZE);
        mPanel.setBackground(SoundDefaults.BACKGROUND);
        mPanel.add(message);
        sPanel.add(mPanel);
        
        // Add the south panel to the frame.
        add(sPanel, BorderLayout.SOUTH);
        
        // Find the previous and next buttons
        dictionaryPanel = new DictionaryDisplayPanel
           (dictionaryListener, message, colors);
        add(dictionaryPanel, BorderLayout.CENTER);
        root.setDictionaryDisplayPanel(dictionaryPanel);

    }   // End of constructor.


    // Method to create a panel of buttons.
    private JPanel buttonPanel(String[] gifs
            , String position, int layout, char option, int[] hots)
    {
        int hotKey;
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, layout));
        panel.setBackground(SoundDefaults.BACKGROUND);
        
        if (layout==BoxLayout.X_AXIS)  panel.add(Box.createHorizontalGlue());
        else                           panel.add(Box.createVerticalGlue());
        
        int spacer, ghostOffset;
        
        for (int i=0; i<gifs.length; i+=4)
        { 
        	ghostOffset = -1;
        	if (gifs[i+3].length() > 1)
        	{
        		ghostOffset = Integer.parseInt(gifs[i+3].substring(1));
        		gifs[i+3] = gifs[i+3].substring(0,1);
        	}
        	
            if (gifs[i+3].indexOf(option)>=0)
            {  if (gifs[i].equals(""))  
               {   if (layout==BoxLayout.X_AXIS)
                         panel.add(Box.createHorizontalGlue());
                   else  panel.add(Box.createVerticalGlue());
               }
               else if (gifs[i].equals("s"))
               {
                  try
                  {  spacer = Integer.parseInt(gifs[i+1]);
                     if (layout==BoxLayout.X_AXIS)
                           panel.add(Box.createHorizontalStrut(spacer));
                     else  panel.add(Box.createVerticalStrut(spacer));
                  }
                  catch (Exception e) {}
               }
               else   
               { 
                   if (hots==null) hotKey = -1;
                   else            hotKey = hots[i/4];
                   JButton button = makeButton(panel, gifs[i], gifs[i+1], gifs[i+2], hotKey);
                   if (ghostOffset>=0)
                   {
                	  enablableButtons[ghostOffset] = button;
                	  enableButton(ghostOffset, false);
                   }
                   
                   
               }
            }
        }    
        panel.setLayout(new BoxLayout(panel, layout));
        if (layout==BoxLayout.X_AXIS)  panel.add(Box.createHorizontalGlue());
        else                           panel.add(Box.createVerticalGlue());

        add(panel, position);
        return panel;
    }
    // Method to create a button with an Icon image and add it to a JPanel.
    private JButton makeButton(JPanel panel, String icon,
                                    String toolTip, String buttonName, int hotKey)    
    {    // Create the Help Button.
         ImageIcon image = Icons.getImageIcon(icon + ".png", ICONSIZE);
         JButton button = new JButton(image);
         button.setName(buttonName);
         button.setPreferredSize(new Dimension(ICONSIZE+1, ICONSIZE+1));
         button.setMaximumSize(new Dimension(ICONSIZE+1, ICONSIZE+1));
         button.setMinimumSize(new Dimension(ICONSIZE+1, ICONSIZE+1));
         button.setToolTipText(toolTip);
         if (hotKey!=-1) button.setMnemonic(hotKey);
         button.setBackground(SoundDefaults.BACKGROUND);
         button.addActionListener((ActionListener)dictionaryListener);
         panel.add(button);
         return button;
    }

    /** Method to ghost or enable buttons */
    public void enableButton(int type, boolean enable)
    {   enablableButtons[type].setEnabled(enable);   }
    
    /** Enable or disable all of the enablable buttons
     * 
     * @param flag true to enable, false to disable
     */
    public void enableButtons(boolean flag)
    {
    	for (int i=CLOSE; i<enablableButtons.length; i++)
    		enableButton(i, flag);
    }
    
    public void setSearchFont(Language language)
    {
    	this.searchLanguage = language;
    	if (language == null) return;
    	if (getSearchMode() == 'W' && language!=null)
      	  language.hookLanguage(field);
    	else
    	{
    	  language.unhookLanguage(field);
    	  field.setFont(new Font(null, Font.BOLD, 14));
    	}
    }

    public char getSearchMode() { return searchLabel.getText().charAt(0); }

    public void setSearchMode(char mode) { searchLabel.setText("" + mode); }
      
    /** Method to get the root sound panel property object  */
    public RootDictionaryPanel getProperties()
    {   if (rootPanel==null)
        {   // Get the ACORNS property change listener.
            PropertyChangeListener[] pcl 
              = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                               ("DictionaryListeners");
            if (pcl.length>0) rootPanel = (RootDictionaryPanel)pcl[0];
        }
        return rootPanel;
    }   // End of getProperties()

    /** Return the environment object */
    public Environment getEnv()
    {  RootDictionaryPanel root = getProperties();
       return root.getEnv();
    }
         
}   // End of DictionaryPanel class
