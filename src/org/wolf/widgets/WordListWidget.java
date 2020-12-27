/*
 * WordListWidget.java
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.DictionaryData;
import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.data.Word;
import org.wolf.undoredo.UndoRedoMedia;
import org.wolf.undoredo.UndoRedoWord;
import org.wolf.widgets.table.DictionaryTableModel;

/** User interface for creating dictionaries */
public class WordListWidget extends JTable
               implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	private static final int MIN_COMPONENT_WIDTH = 50;
	
	DictionaryData dictionary; 
    Word cutWord;
    boolean selectionEnabled;
    FocusPolicy traversalPolicy;

    public WordListWidget(DictionaryData dictionary, int minWidth)
    {   this.dictionary = dictionary;
        selectionEnabled = true;

        setTable(); // Set this as the word list table for varioous widgets

        setBackground(Color.LIGHT_GRAY);

        setEnabled(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultEditor(Object.class, new DictionaryCellEditor());
        setDefaultRenderer(Object.class, new DictionaryCellRenderer());
        setSurrendersFocusOnKeystroke(true);

        Language language = dictionary.getActiveLanguage();
        DictionaryTableModel tableModel 
                = new DictionaryTableModel(this, dictionary, language);
        setModel(tableModel);
        addMouseListener(this);
        addMouseMotionListener(this);
        setDragEnabled(true);
        new MediaDropTarget(this, this);

        TableColumn column = getColumnModel().getColumn(0);
        column.setMinWidth(minWidth);
        traversalPolicy = new FocusPolicy();
    }
    
    @Override
    protected boolean processKeyBinding(KeyStroke stroke, KeyEvent evt, int condition, boolean pressed) 
    {
	    Component owner 
			= KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
	   
	   
        if(!pressed) 
        {
            return super.processKeyBinding(stroke, evt, condition, pressed);
        }
        
		   
        int code = evt.getKeyCode();
        boolean shift = evt.isShiftDown();
        Component nextComponent;
        int row = getSelectedRow();
        
        switch (code)
        {
	        case KeyEvent.VK_UP:
	    		nextComponent = traversalPolicy.getPreviousRowComponent(this, owner);
	        	nextComponent.requestFocusInWindow();
	        	return true;
	        	
	        case KeyEvent.VK_DOWN:
	    		nextComponent = traversalPolicy.getNextRowComponent(this, owner);
	        	nextComponent.requestFocusInWindow();
	        	return true;
        	
        	case KeyEvent.VK_ENTER:
	        case KeyEvent.VK_TAB:
	        	if (shift)
	        	{
	        		nextComponent = traversalPolicy.getComponentBefore(this, owner);
	        	}
	        	else
	        		nextComponent = traversalPolicy.getComponentAfter(this, owner);
	        	
	        	nextComponent.requestFocusInWindow();
	        	return true;
	        
	        case KeyEvent.VK_PAGE_DOWN:
	            if(row < (getRowCount() - 1))
	            {
	            	scrollToRow(row+1);
	            } 
            	return true;
	        	
	        case KeyEvent.VK_PAGE_UP:
	        	if (row > 0)
	        	{
	        		scrollToRow(row-1);
	        	}
	        	return true;
        }
        
    	return super.processKeyBinding(stroke, evt, condition, pressed);
    }

    /** Handle drag and drops into cells */
    public boolean isMedia(File file)
    {  
    	changeSelectedItem();
        GroupWidget group = findDrop();
        if (group!=null) return group.isMedia(file);
        return false;
    }

    public void mediaDropped(File file) throws Exception
    {  changeSelectedItem();
       GroupWidget group = findDrop();
       if (group!=null) group.mediaDropped(file);
    }



    /** Find the drop target for media */
    private GroupWidget findDrop()
    {  
    	TableCellEditor editor = getCellEditor();
    	if (editor == null) return null;

    	int row = getEditingRow();
        Object value = editor.getCellEditorValue();
        GroupWidget group = (GroupWidget)editor.getTableCellEditorComponent
                                                   (this, value, true, row, 0);
        traversalPolicy.setPolicyComponents(group);
        
        JComponent component = (JComponent)traversalPolicy.getFirstComponent(group);
        component.requestFocusInWindow();
        return group;
    }
 
    /** Update media objects in the word */
    public void updateMedia(Object[] media, int[] indices)
    {   int row = getEditingRow();
        Language language = dictionary.getActiveLanguage();
        Word[] words = new Word[2];
        words[0] = language.getWord(row);

        if (row>=0)
        {   TableCellEditor editor = getCellEditor();
            if (editor!=null)  editor.stopCellEditing();
            changeSelection(row, 0, false, false);
            boolean success = editCellAt(row, 0);
            if (success) {  changeSelection(row, 0, true, false); }
        }
        words[1] = language.getWord(row);

        UndoRedoMedia undo = new UndoRedoMedia(words,row,media,indices);
        dictionary.push(undo);
    }

    /** Determine if selection changes allowed on mouse movement */
    public void setSelectionsEnabled(boolean flag) { selectionEnabled = flag;  }

    /** Update cell and restore focus */
    public void updateWord()
    {  int row = getEditingRow();
       if (row<0) row = rowAtPoint(getMousePosition());

       if (row>=0)
       {   Word oldWord = dictionary.getActiveLanguage().getWord(row);
           
           TableCellEditor editor = getCellEditor();
           if (editor!=null)  editor.stopCellEditing();
           changeSelection(row, 0, false, false);

           Word newWord = dictionary.getActiveLanguage().getWord(row);
           if (!oldWord.equals(newWord))
           {   UndoRedoWord undo = new UndoRedoWord(oldWord, newWord, row);
               dictionary.push(undo);
           }

           boolean success = editCellAt(row, 0);
           if (success) {  changeSelection(row, 0, true, false); }
           findDrop();
       }
    }

    /** Update cell with new word value and restore focus */
    public void updateWord(Word word, int row)
    {  DefaultTableModel model = (DefaultTableModel)getModel();
       TableCellEditor editor = getCellEditor();
       if (editor!=null) editor.stopCellEditing();
       model.setValueAt(word, row, 0);
    }


    /** Set the WordListTable to this object */
    private void setTable()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        rootPanel.setWordTable(this);
    }

    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event)  {}
    public void mousePressed(MouseEvent event)
    { 
    	if (getEditingRow()< 0)   
    	{   
    		changeSelectedItem();  
    	}
    }
    public void mouseReleased(MouseEvent event) 
    { 
    	if (getEditingRow()<0)  
    	{ 
    		changeSelectedItem();   
    	}
    }
    public void mouseClicked(MouseEvent event)  
    { 
    	if (getEditingRow()<0)  
    	{  
    		changeSelectedItem();  
    	}
    }
  
    /** Scroll to the desired word in the dictionary
     * 
     * @param key The word keyvalue
     */
    
    public void scrollToWord(String key)
    {
    	Language language = dictionary.getActiveLanguage();
    	int row = language.find(key);
    	if (row<0)
    	{
			Toolkit.getDefaultToolkit().beep();
			return;
    	}
	       	
        TableCellEditor editor = getCellEditor();
        if (editor!=null)
           editor.stopCellEditing();
        
	    setRowSelectionInterval(row, row);
	    scrollToRow(row);
	    
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() 
            {
            	int row = getSelectedRow();
            	JViewport view = (JViewport)getParent();
                Rectangle rect = getCellRect(row, 0, true);

                // The location of the viewport relative to the table
                Point pt = view.getViewPosition();

                // Translate the cell location so that it is relative
                // to the view, assuming the northwest corner of the
                // view is (0,0)
                rect.setLocation(rect.x-pt.x, rect.y-pt.y);
                view.scrollRectToVisible(rect);
            }
        });
    }

    /** Scroll to the desired word in the dictionary
     * 
     * @param row The table row contining the dictionary
     */
    public void scrollToRow(int row)
    {
    	Rectangle bounds = getCellRect(row, 0, true);
    	scrollRectToVisible(bounds);
    	changeSelectedItem(row);
    }

    public void mouseMoved(MouseEvent event) {  changeSelectedItem();  }
    public void mouseDragged(MouseEvent event) {}
    
    public synchronized void changeSelectedItem(int row)
    {
        int oldRow = getEditingRow();
        if (row==oldRow) return;
        if (oldRow>=0)
        {  
           TableCellEditor editor = getCellEditor();
           if (editor!=null)
           {   
        	   Word oldWord = dictionary.getActiveLanguage().getWord(oldRow);
               editor.stopCellEditing();

               Word newWord = dictionary.getActiveLanguage().getWord(oldRow);
               if (!oldWord.equals(newWord))
               {   UndoRedoWord undo = new UndoRedoWord(oldWord, newWord, oldRow);
                   dictionary.push(undo);
               }
           }
           clearSelection();
        }

        if (row>=0)
        {  
           setRowSelectionInterval(row, row);
           editCellAt(row, 0);
        }
        findDrop();
    }

    public synchronized void changeSelectedItem()
    {   
    	if (!selectionEnabled) return;
        Point point = getMousePosition(true);
        if (point==null) return;
 
       int row = rowAtPoint(point);
       changeSelectedItem(row);
    }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }
 
    /** OntologyTree node components */
    private class DictionaryTableCellObject
    {   private GroupWidget group;

        public DictionaryTableCellObject()
        { group = new GroupWidget(true);
        }

        /** Get the word updated by the user */
        public Word getWord()   { return (Word)group.updateCell(); }

        /** Get the cell component */
        public GroupWidget getPanel()    { return group; }

        /** Format the object for rendering or editing */
        public void formatCell(int row, Word word)
        {   
        	group.formatCell((Item)word);
            Dimension size = new Dimension(group.getPreferredSize());
            Dimension viewSize = getPreferredScrollableViewportSize();
            if (viewSize.width < size.width) viewSize.width = size.width;

            int rowHeight = getRowHeight(row);
            if (rowHeight != size.height) setRowHeight(row, size.height);
            Dimension tableSize = getSize();
            if (tableSize.width < size.width)
            {   tableSize.width = size.width;
                TableColumn column = getColumnModel().getColumn(0);
                column.setPreferredWidth(size.width);
            }
       }

       public void addMouseListener(MouseListener listener)
       {   group.addMouseListener(listener); }
    }

    /** Render Word objects appropriately in this list */
    class DictionaryCellRenderer implements TableCellRenderer
    {   DictionaryTableCellObject cellObject;
        DefaultTableCellRenderer defaultRenderer;

        public DictionaryCellRenderer()
        {   cellObject = new DictionaryTableCellObject();
            defaultRenderer = new DefaultTableCellRenderer();
        }

        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean selected, boolean focus, int row, int col)
        {
            Component returnComponent = null;
            if ((value != null) && (value instanceof Word))
            {  Word word = (Word) value;
               try
               {   cellObject.formatCell(row, word);
                   returnComponent = cellObject.getPanel();
               }
               catch (Exception e) {}
            }

            // Handle case where we couldn't render the entry.
            if (returnComponent == null)
            {  returnComponent = defaultRenderer.getTableCellRendererComponent
                    (table, value, selected, focus, row, col);
            }
            return returnComponent;
        }
    }       // End of embedded DictionaryCellRenderer class

    /** Enable editing of Words in the workspace */
    private class DictionaryCellEditor  extends AbstractCellEditor 
                                       implements TableCellEditor, MouseListener
    {   
    	private static final long serialVersionUID = 1L;
    	private DictionaryTableCellObject cellObject;

        /** Constructor to create the editor component and listener list */
        public DictionaryCellEditor()
        {  cellObject = new DictionaryTableCellObject();
           cellObject.addMouseListener(this);
        }

        /** Get the Tree cell component to edit a node */
        public synchronized Component getTableCellEditorComponent
                (JTable table, Object value, boolean selected, int row, int col)
        {   if (value instanceof Word)
            {   Word word = (Word)value;
                cellObject.formatCell(row, word);
            }
            return cellObject.getPanel();
        }

        /** Get the editing cell for updating the tree */
        public Object getCellEditorValue()
        {  Word word = cellObject.getWord();
           return word;
        }

        public void mouseEntered(MouseEvent event) 
        { Component parent = event.getComponent().getParent();
           if (parent !=null && parent instanceof WordListWidget)
           {   changeSelectedItem();  }
        }
        public void mouseExited(MouseEvent event) 
        {  Component parent = event.getComponent().getParent();
           if (parent !=null && parent instanceof WordListWidget)
           {   changeSelectedItem();  }
        }
        public void mousePressed(MouseEvent event) {}
        public void mouseReleased(MouseEvent event) {}
        public void mouseClicked(MouseEvent event) {}

    }   // End of embewdded DictionaryCellEditor class

	/** Focus on next word */
	public void nextWord()
	{
		int rows = getModel().getRowCount();
        int row = getEditingRow();
        if (row==rows) return;
        changeSelectedItem(row+1);
	}

	
	private class FocusPolicy extends FocusTraversalPolicy
	{
		ArrayList<JComponent> list;
		
		public void setPolicyComponents(GroupWidget component)
		{
			list = new ArrayList<JComponent>();
			findTextFields(component, list);
		}

		@Override
		public Component getComponentAfter(Container arg0, Component arg1) 
		{
			if (list == null || list.isEmpty()) return null;
			int index = (list.indexOf(arg1) + 1) % list.size();
			return list.get(index);
		}

		@Override
		public Component getComponentBefore(Container arg0, Component arg1) 
		{
			if (list == null || list.isEmpty()) return null;

			int index = list.indexOf(arg1);
			if (index<0) return list.get(0);
			
			if (index==0) index = list.size();
			return list.get(index - 1);
		}

		@Override
		public Component getDefaultComponent(Container arg0) 
		{
			if (list == null || list.isEmpty()) return null;
			return list.get(0);
		}

		@Override
		public Component getFirstComponent(Container arg0) 
		{
			if (list == null || list.isEmpty()) return null;
			return list.get(0);
		}

		@Override
		public Component getLastComponent(Container arg0) {
			if (list == null || list.isEmpty()) return null;
			return list.get(list.size() - 1);
		}
		
		public Component getPreviousRowComponent(Container arg0, Component arg1)
		{
			if (list == null || list.isEmpty()) return null;

			int index = list.indexOf(arg1);
			if (index<0) return list.get(0);

			Point p, spot = list.get(0).getLocationOnScreen();
			Component component;
			for (int i = (index - 1 + list.size())%list.size(); i!=index; i = (i-1 + list.size())%list.size() )
			{
				component = list.get(i);
				p = component.getLocationOnScreen();
				if (Math.abs(p.x - spot.x)<MIN_COMPONENT_WIDTH) 
				{
					return component;
				}
			}
			return list.get(0);
		}
		
		public Component getNextRowComponent(Container arg0, Component arg1)
		{
			if (list == null || list.isEmpty()) return null;

			int index = list.indexOf(arg1);
			if (index<0) return list.get(0);
			
			Point p, spot = list.get(0).getLocationOnScreen();
			Component component;
			for (int i = (index + 1)%list.size(); i!=index; i = (i+1)%list.size() )
			{
				component = list.get(i);
				p = component.getLocationOnScreen();
				if (Math.abs(p.x - spot.x) < MIN_COMPONENT_WIDTH)
				{
					return component;
				}
			} 
			return list.get(0);
		}
		
		private ArrayList<JComponent> findTextFields(JComponent panel, ArrayList<JComponent> fields)
		{
			synchronized (panel.getTreeLock())
			{
				if (panel instanceof GroupWidget)
				{
					ArrayList<JComponent> group = ((GroupWidget)panel).getColumns();
					for (int i=0; i<group.size(); i++)
					{
						findTextFields(group.get(i), fields);
					}
					
					group = ((GroupWidget)panel).getRows();
					for (int i=0; i<group.size(); i++)
					{
						findTextFields(group.get(i), fields);
					}
					return fields;
				}

				Component[] components = panel.getComponents();
				int count = components.length;
				for (int i=0; i<count; i++)
				{
					if (components[i] instanceof JTextField)
					{
						fields.add( (JComponent)components[i]);
					}
					
					if (components[i] instanceof JTextArea)
					{
						fields.add( (JComponent)components[i]);
					}
					
					if (components[i] instanceof JPanel 
							|| components[i] instanceof JLayeredPane)
					{
						findTextFields((JComponent)components[i], fields);
					}
				}
			}
			return fields;
		}
	
	}   // End of FocusPolicy class
    
    
}       // End of WordListWidget class
