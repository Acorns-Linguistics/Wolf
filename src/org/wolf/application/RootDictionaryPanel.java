/*
 *   class RootDictionaryPanel.java
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.wolf.components.ScrollableToolbar;
import org.wolf.data.DictionaryData;
import org.wolf.system.Environment;
import org.wolf.undoredo.UndoRedo;
import org.wolf.widgets.WordListWidget;

public abstract class RootDictionaryPanel extends JPanel 
                                       implements PropertyChangeListener
{
	private static final long serialVersionUID = 1L;
	private Environment environment;
    private DictionaryData dictionaryData;
    private ScrollableToolbar toolbar;
    private static JLabel error;
    private WordListWidget table;
    private boolean phonetics;
    private DictionaryDisplayPanel dictionaryDisplayPanel;

    private DictionaryPanel buttonPanel;
    private UndoRedo undoRedo;

    RootDictionaryPanel(Environment env)
    {  environment = env;
       toolbar = new ScrollableToolbar(JScrollBar.HORIZONTAL);
       phonetics = false;
    }
    
    /** Property Change Listener to draw vertical line during play back.
      *
      *  @param event property change "PlayBack" fired by the sound data object.
      */
     public void propertyChange(PropertyChangeEvent event)  {}

    /** Set the dictionary data object
     *  @param data dictionary data object
     */
    public void setDictionaryData(DictionaryData data) {dictionaryData = data;}

    /** Get the dictionary data object
     *  @return dictionary data object
     */
    public DictionaryData getDictionaryData()
    {  if (dictionaryData==null) dictionaryData = new DictionaryData();
       return dictionaryData;
    }

     /** Get the undo and redo stack (create if it doesn't exist)
      *  @return Object for undo and redo operations
      */
     public UndoRedo getUndoRedo()
     {   if (undoRedo==null) undoRedo = new UndoRedo(100);
         return undoRedo;
     }

     public Environment getEnv() { return environment; }
     public ScrollableToolbar getToolbar()
     { return toolbar; }
     
     public JLabel getErrorLabel()
     {
    	 return getLabel();
     }
     
     public static JLabel getLabel()
     {   if (error==null) error = new JLabel("");
         return error;
     }
     public void setWordTable(WordListWidget t) { table = t; }
     public WordListWidget getWordTable() { return table; }

     public void togglePhonetics() { phonetics = !phonetics; }
     public boolean isPhonetics()  { return phonetics; }

         /** Set wave display panel so listeners can become active.
      */
     public void setDictionaryDisplayPanel(DictionaryDisplayPanel panel)
     {  dictionaryDisplayPanel = panel;  }

    /** Method to return the SoundDisplayPanel object
     *
     * @return SoundDisplayPanel object
     */
    public DictionaryDisplayPanel getDisplayPanel()
    { return dictionaryDisplayPanel; }

    public DictionaryPanel getButtonPanel()
    {  return buttonPanel; }

    protected void setButtonPanel(DictionaryPanel panel)
    { buttonPanel  = panel; }

  }  // End of RootDictionaryPanel class

