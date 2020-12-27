/*
 * DictionaryDisplayPanel.java
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
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.acorns.visual.ColorScheme;
import org.wolf.data.Constants;
import org.wolf.data.DictionaryData;
import org.wolf.data.Language;
import org.wolf.widgets.MediaDropTarget;
import org.wolf.widgets.WordListWidget;

/** Manage the dictionary work space */
public class DictionaryDisplayPanel extends JPanel implements Constants
{

	private static final long serialVersionUID = 1L;
	private boolean enabled,   view;
    private DictionaryData     dictionary;
    private JScrollPane        scroll;
    private JLabel             label;
    private WordListWidget     wordList;
    private DictionaryListener listener;

    public DictionaryDisplayPanel
            (DictionaryListener listener, JLabel error, ColorScheme colors)
    {
        this.listener = listener;

        new MediaDropTarget(this, this);

        label = new JLabel("", JLabel.CENTER);
        setLayout(new BorderLayout());
        label.setVerticalTextPosition(JLabel.CENTER);
        scroll = new JScrollPane();
    	scroll.getViewport().setBackground(Color.LIGHT_GRAY);
        add(scroll, BorderLayout.CENTER);
        enableDictionary(false);
        enableDictionary(true);
        view = false;
   }
 
    /** Determine if dictionary is present */
    public boolean isEnabled() { return enabled; }

    /** Method to enable or disable repainting of this component
     *
     * @param enable true if component is to be enabled
     */
    public synchronized void enableDictionary(boolean enable)
    {   if (enable == enabled) return;
        if (getRootDictionaryPanel() ==  null)
        {   label.setText("Couldn't find dictionary");
            scroll.setViewportView(label);
            view = false;
            wordList = null;
            return;
        }

        dictionary = getDict();
        if (dictionary==null || enable==false)
        {   if (dictionary!=null && dictionary.getActiveLanguage()!=null)
                stopEditing();

            label.setText("There is no dictionary loaded");
            dispatchDictionary();
            enabled  = enable;
            return;
        }

        Language active = dictionary.getActiveLanguage();
        if (active==null)
        {  label.setText(
              "<html><p align='center'>WOLF version " + version + "<br>" +
              "A Dictionary must contain at least " +
              "one language before this workspace can be active.<br><br>" +
              "Please click the add language icon to add those that " +
              "your dictionary will contain.<br>" +
              "This will cause an icon appear at the bottom of the display" +
              "<br><br>" +
              "When you finish adding languages, you need to make one of them" +
              " active.<br>Do this by clicking twice on its icon that " +
              "appears. (This will cause it to turn green.)<br>" +
              "Then click once on other languages (if any) that you wish " +
              "to cross reference (to turn their icons red).<br><br>" +
              "You can also optionally add a copyright statement and " +
              "the contact information for coontributing authors.<br>" +
              "Do this by clicking on the appropriate icons.<br><br>" +
              "After these preliminaries, you are ready to begin adding " +
              "words to your dictionary.</p></html>");
           dispatchDictionary();
           enabled = enable;
        }
        else
        {   if (SwingUtilities.isEventDispatchThread())
            {   int minWidth = scroll.getSize().width;
                wordList = new WordListWidget(dictionary, minWidth - 3);
                scroll.setViewportView(wordList);
                view = true;
            }
            else
            {  try
               {   SwingUtilities.invokeAndWait(new Runnable()
                   {   public void run()
                       {   int minWidth = scroll.getSize().width;
                           wordList = new WordListWidget(dictionary, minWidth - 3);
                           scroll.setViewportView(wordList);
                           view = true;
                       }
                   });
               }
               catch (InterruptedException e) {}
               catch (InvocationTargetException e) {}
           }
           enabled = enable;
        }
    }   // End of enableDictionary()

    /** Method to reset the dictionary display */
    private void dispatchDictionary()
    {   if (SwingUtilities.isEventDispatchThread())
        {   wordList = null;
            scroll.setViewportView(label);
            view = false;
        }
        else
        {   try
            {  SwingUtilities.invokeAndWait(new Runnable()
               {    public void run()
                    {   wordList = null;
                        scroll.setViewportView(label);
                        view = false;
                    }
               });
            }
            catch (InterruptedException e) {}
            catch (InvocationTargetException e) {}
       }
   }

    /** Override to draw the sound data object.
     *  @param graphics The graphics object for drawing to this panel.
     */
    public @Override void paintComponent(Graphics graphics)
    {   if (!enabled) return;
        super.paintComponent(graphics);
    }  // End PaintComponent();

    /** Load dictionary data into the work space */
    public void loadDictionary()
    {   enableDictionary(false);
        enableDictionary(true);
    }

    /** If table displayed, stop editing, clear selection, and redraw */
    public void reloadDictionary()
    {  dictionary.setSavedLanguage();
       if (dictionary.getActiveLanguage()==null || wordList==null)
       {   enableDictionary(false);
           enableDictionary(true);
           return;
       }

       if (wordList!=null)
       {    
    	    try
            {  SwingUtilities.invokeAndWait(new Runnable()
               {  public void run()
                  {  
            	     AbstractTableModel model
                             = (AbstractTableModel)wordList.getModel();
                     TableCellEditor editor = wordList.getCellEditor();
                     if (editor!=null) editor.stopCellEditing();
                     
                     
                     model.fireTableDataChanged();
                  }
               });
            }
            catch (InterruptedException e) {}
            catch (InvocationTargetException e) {}
       }
    }

    /** Stop any editing that is in progress */
    public void stopEditing()
    {   if (wordList!=null && view)
        {   
    		TableCellEditor editor = wordList.getCellEditor();
            if (editor!=null)  editor.stopCellEditing();
        }
    }

    public void forward() {}

    /** Determine if an extension goes with a valid media type
     *
     * @param name of file to check
     * @return true if accepted, false otherwise
     */
    public boolean isMedia(File file)
    {   if (wordList != null)  {  wordList.changeSelectedItem();  }
        String extension = file.getName();
        extension = extension.substring(extension.lastIndexOf(".")+1);

        if (extension.equals("adct")) return true;
        if (extension.equals("xml")) return true;
        if (extension.equals("db")) return true;
        return false;
    }

    /** Method to handle drops of media files into the spinner object
     *
     * @param file The file object with the location of the
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws NoPlayerException
     */
    public void mediaDropped(File  file)
    {
        enableDictionary(false);
        listener.dropDictionary(file);
        enableDictionary(true);
        
        RootDictionaryPanel root = getRootDictionaryPanel();
        DictionaryPanel buttons = root.getButtonPanel();
        buttons.enableButtons(true);
    }       // End of mediaDropped()

    /** Retrieve the root dictionary panel */
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }
    
    public boolean togglePhonetics()
    {
    	if (wordList == null) return false;

	    int rowIndex = wordList.getSelectedRow();
	    if (rowIndex<0)
	    {
	    	JViewport viewport = scroll.getViewport();
	    	Point p = viewport.getViewPosition();
	    	rowIndex = wordList.rowAtPoint(p);
	    }
	    
    	stopEditing();
	    getRootDictionaryPanel().togglePhonetics();
   		wordList.setRowSelectionInterval(rowIndex, rowIndex);

	    final AbstractTableModel model
        	= (AbstractTableModel)wordList.getModel();
	    model.addTableModelListener(new TableModelListener() 
	    {      
            private final int row=wordList.getSelectedRow();
            private final TableModelListener listener = this;
            
	        @Override
	        public void tableChanged(TableModelEvent e)
	        {
	            if (row<0) return;

	            SwingUtilities.invokeLater(new Runnable() {
	                @Override
	                public void run() 
	                {
	                	model.removeTableModelListener(listener);
	                	JViewport view = (JViewport)wordList.getParent();
	                    Rectangle rect = wordList.getCellRect(row, 0, true);
	                    view.setViewPosition(new Point(rect.x, rect.y));
	                }
	            });
	        }
	    });
	    
    	reloadDictionary();

    	wordList.setRowSelectionInterval(rowIndex, rowIndex);
        
        return getRootDictionaryPanel().isPhonetics();

    }

    /** Get the dictionary object */
    protected DictionaryData getDict()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getDictionaryData();
    }
}       // End of DicgtionaryDisplayPanel
