/*
 * GroupWidget.java
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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.Comment;
import org.wolf.data.Constants;
import org.wolf.data.DictionaryData;
import org.wolf.data.Group;
import org.wolf.data.Item;
import org.wolf.data.Ontology;
import org.wolf.data.OntologyData;
import org.wolf.data.Reference;
import org.wolf.data.Unit;
import org.wolf.data.Word;
import org.wolf.dialogs.CellSizeDialog;
import org.wolf.dialogs.FontDialog;
import org.wolf.lib.Icons;
import org.wolf.system.Environment;
import org.wolf.undoredo.UndoRedoWord;
import org.wolf.widgets.table.DictionaryTableModel;

/** Swing Components for words and definitions.
 *
 * Consists of a column of GUI components that lay out
 * horizontally. It also consists of a RowWidget for GUI components that lay
 * out vertically.
 *
 * A Component listener adjusts the position when row and column layout changes
 */
public class GroupWidget extends JLayeredPane
          implements ComponentListener, MouseListener, MouseMotionListener,
                     ActionListener, PopupMenuListener, Constants
{
	private static final long serialVersionUID = 1L;

    private final static int COLUMN_GAP = 3, ROW_GAP = 3;
    private final static Integer TOP = JLayeredPane.POPUP_LAYER;
	
	private final static String[] textAreas =
    { 
    	"Annotations", "Antonyms",   "Categories", "Etymology", "Language Links", 
        "Main Entry",  "Morphemes",  "Refer To",   "Spelling",  "Subentry",    
        "Synonyms",    "Table",      "Thesaurus"
    };
    
    private final static String[] references = 
    { 	
    	"Compare",          "Encyclopedic Info", "Gloss", 
    	"Lexical Function", "References",        "Reversals",	
    	"Restrictions",    	"Usage",             "Variants"	
    };

    private ArrayList<JComponent> columns, rows;

    private JPopupMenu rowMenu, columnMenu, widgetMenu;
    private JMenuItem  rowPasteOption, columnPasteOption;

    private Point   mouseOffset;
    private boolean word;
    private boolean dragRow;
    private int dragComponentNo;

    private DictionaryWidget dictionaryWidget;
    private JComponent widgetCell;
    
    public GroupWidget(boolean word)
    {   
    	this.word = word;
        dragComponentNo = -1;
        
        if (word)
        {  setBorder(BorderFactory.createTitledBorder("Word"));
           dictionaryWidget = new DictionaryWidget(COLUMN_WIDGET_WIDTH, "Word");
        }
        else
        {  setBorder(BorderFactory.createTitledBorder("Definition"));
           dictionaryWidget = new DictionaryWidget(DEFINITION_WIDGET_WIDTH,"Definition");
        }
        dictionaryWidget.setEnabled(true);

        columns = new ArrayList<JComponent>();
        rows = new ArrayList<JComponent>();
        columns.add(dictionaryWidget);
        add(dictionaryWidget, TOP);

        rowMenu = makeRowMenu();
        columnMenu = makeColumnMenu();
        widgetMenu = makeWidgetMenu();
        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    } 

    /** Override the sizing methods with computed size */
    public @Override Dimension getSize()          {  return setDimension(); }
    public @Override Dimension getPreferredSize() {  return setDimension(); }
    public @Override Dimension getMinimumSize()   {  return setDimension(); }
    public @Override Dimension getMaximumSize()   {  return setDimension(); }

    public boolean isMedia(File file)
    {   try
        {
            DictionaryWidget widget = findDrop(this);
            if (widget==null) return false;
            return widget.isMedia(file);
        }
        catch (Exception e) { System.out.println(e.toString()); }
        return true;
    }

    public void mediaDropped(File file) throws Exception
    {   DictionaryWidget widget = findDrop(this);
        if (widget==null) return;
        widget.mediaDropped(file);
    }

    /**Find target of the drop from the parent group object */
    private DictionaryWidget findDrop(GroupWidget group)
    {
       Component child;
       DictionaryWidget media = null;
        for (int i=0; i<group.getComponentCount(); i++)
        {  child = group.getComponent(i);
           if (child.getMousePosition() !=null)
           {  if (child!=null && child instanceof GroupWidget)
              {  return findDrop((GroupWidget)child);  }
              if (child!=null && child instanceof DictionaryWidget)
              {  return (DictionaryWidget)child;  }
           }
        }
        return media;
    }

    /** Return updated word data */
    public Group updateCell()
    {   Group group;
        if (word)  group = new Word();
        else       group = new Group("Definition");
        storeCellCharacteristics(group);

        Unit unit = (Unit)dictionaryWidget.updateCell();
        group.setMedia(unit);
        
        JComponent widget;
        Item item;
        Method updateCell;
        Class<?> widgetClass;
        for (int i=1; i<columns.size(); i++)
        {  try
           {   widget = columns.get(i);
               widgetClass = widget.getClass();
               updateCell = widgetClass.getMethod("updateCell");
               item =  (Item)updateCell.invoke(widget, new Object[0]);
               group.addColumn(item); 
           }
           catch (Exception e) {}
        }
        for (int i=0; i<rows.size(); i++)
        {  try
           {   widget = rows.get(i);
               widgetClass = widget.getClass();
               updateCell = widgetClass.getMethod("updateCell");
               item =  (Item)updateCell.invoke(widget, new Object[0]);
               group.addRow(item);
           }
           catch (Exception e) {}
        }
        return group;
    }
    
    public ArrayList<JComponent> getColumns() { return columns; }
    public ArrayList<JComponent> getRows() { return rows; }

    /** Format this widget based on information in the word */
    public void formatCell(Item item)
    {  
    	if (item instanceof Word)
    		System.out.println( ((Word)item).getTitle());
       Group group = (Group)item;
       Unit unit = group.getMedia();

       dictionaryWidget.formatCell(unit);

       loadCellCharacteristics(group);

       JComponent component;
       int componentCount = getComponentCount();
       for (int i=componentCount-1; i>=0; i--)
       {   component = (JComponent)getComponent(i);
           if (component!=dictionaryWidget)
           {  removeComponent(component); 
              WidgetFactory.releaseComponent(component);
           }
       }

       ArrayList<Item> columnItems = group.getColumns();
       JComponent widget;
       Point point = new Point(99999,99999);
       for (int i=0; i<columnItems.size(); i++)
       {
          try
          {   
        	  widget = (JComponent)WidgetFactory.createWidget(columnItems.get(i));
              updateListeners(widget);
              insertComponent(false, point, widget);

          }
          catch (Exception e) 
          {
        	  getErr().setText(e.toString());
          }
       }
       ArrayList<Item> rowItems = group.getRows();
       for (int i=0; i<rowItems.size(); i++)
       {  try
          {   widget = (JComponent)WidgetFactory.createWidget(rowItems.get(i));
              updateListeners(widget);
              insertComponent(true, point, widget);
          }
          catch (Exception e) 
       	  { 
        	  getErr().setText(e.toString()); 
          }
       }
       setDimension(); 
    }
    
    /** Add listener to this object if necessary */
    private void updateListeners(JComponent widget)
    {   EventListener[] listeners = widget.getMouseListeners();
        boolean exists = false;
        for (int m=0; m<listeners.length; m++)
        {  if (listeners[m]==this) { exists = true; break; }  }
        if (!exists) widget.addMouseListener(this);

        listeners = widget.getMouseMotionListeners();
        exists = false;
        for (int m=0; m<listeners.length; m++)
        {   if (listeners[m]==this) { exists = true; break; } }
        if (!exists) widget.addMouseMotionListener(this);
    }
 
    /** Create a popup menu for adding GUI component rows */
    private JPopupMenu makeRowMenu()
    {  JPopupMenu menu = makeWordMenu(true);
       JMenuItem item = new JMenuItem("Comment");
       item.setName("RowComment");
       item.addActionListener(this);
       menu.add(item);

       if (word)
       {  item = new JMenuItem("Definition");
          item.setName("Definition");
          item.addActionListener(this);
          menu.add(item);
       }

       item = new JMenuItem("Example");
       item.setName("Example");
       item.addActionListener(this);
       menu.add(item);
       return menu;
    }

    /** Create the drop down menu for add GUI components to a column */
    private JPopupMenu makeColumnMenu()
    {  JPopupMenu menu = makeWordMenu(false);
       JMenuItem item;

       JMenu categorySubMenu = new JMenu("Category");
       JMenu referencesSubMenu = new JMenu("Translations");

       for (int i=0; i<textAreas.length; i++)
       {   
    	   item = new JMenuItem(textAreas[i]);
           item.setName(textAreas[i]);
           item.addActionListener(this);
           categorySubMenu.add(item);
       }
       menu.add(categorySubMenu);
       
       for (int i=0; i<references.length; i++)
       {
    	   item = new JMenuItem(references[i]);
    	   item.setName(references[i]);
    	   item.addActionListener(this);
    	   referencesSubMenu.add(item);
       }
       menu.add(referencesSubMenu);

       item = new JMenuItem("Comment");
       item.setName("ColumnComment");
       item.addActionListener(this);
       menu.add(item);
       
       ImageIcon icon;
       OntologyData ontology = getEnv().getOntologyData();
       if (ontology!=null)
       {   categorySubMenu = new JMenu("Ontology");
           for (int i=0; i<ONTOLOGY_NAMES.length; i++)
           {   icon = Icons.getImageIcon(ONTOLOGY_NAMES[i], -1);
               item = new JMenuItem(icon);
               item.addActionListener(this);
               item.setName(ONTOLOGY_NAMES[i]);
               categorySubMenu.add(item);
           }
       }
       menu.add(categorySubMenu);
       return menu;
    }

    /** Add word options to a popup menu
     *
     * @param menu The popup menu
     * @param true if this is for row options, false for column options
     * @return The edited popup menu
     */
    private JPopupMenu makeWordMenu(boolean row)
    {   JPopupMenu menu = new JPopupMenu();
        menu.addPopupMenuListener(this);
        if (!word) return  menu;

        JMenuItem item = new JMenuItem("Insert word");
        item.setName("Insert");
        item.addActionListener(this);
        menu.add(item);

        item = new JMenuItem("Remove word");
        item.setName("Remove");
        item.addActionListener(this);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Cut word");
        item.setName("Cut");
        item.addActionListener(this);
        menu.add(item);

        item = new JMenuItem("Paste word");
        item.setName("Paste");
        item.addActionListener(this);
        menu.add(item);

        if (row) rowPasteOption = item;
        else     columnPasteOption = item;

        menu.addSeparator();
        return menu;
    }

    /** Create menu for manipulating widget cells */
    private JPopupMenu makeWidgetMenu()
    {  JPopupMenu menu = new JPopupMenu();
       menu.addPopupMenuListener(this);

       JMenuItem item = new JMenuItem("Choose font");
       item.setName("Choose Font");
       item.addActionListener(this);
       menu.add(item);
           
       item = new JMenuItem("Set cell size");
       item.setName("Cell Size");
       item.addActionListener(this);
       menu.add(item);
       return menu;
    }

    /** Reenable table selections on mouse movement */
    public void popupMenuCanceled(PopupMenuEvent popupMenuEvent)
    {  WordListWidget table = (WordListWidget)getTable();
       table.setSelectionsEnabled(true);
    }

  public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {}

  public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {}

    public void componentHidden(ComponentEvent event) {}
    public void componentMoved(ComponentEvent event) {}
    public void componentShown(ComponentEvent event) {}

    /** Adjust the location of the components when page resizes */
    public void componentResized(ComponentEvent event)  {}
    /** Show popup menu when appropriate */
    public void mouseClicked(MouseEvent event)  {}

    public void mouseReleased(MouseEvent event)
    {  WordListWidget table = (WordListWidget)getTable();
       table.setSelectionsEnabled(true);

       if (event.isPopupTrigger())  { processEvent(event); return; }

       setCursor(Cursor.getDefaultCursor());
       if (dragRow && rows.size()<=dragComponentNo) dragComponentNo = -1;
       if (!dragRow && columns.size()<=dragComponentNo) dragComponentNo = -1;
       
       if (dragComponentNo>=0)
       {  JComponent component;
          if (dragRow) component = rows.get(dragComponentNo);
          else  component = columns.get(dragComponentNo);

          Point point = getMousePosition(true);

          boolean isRow = rows.contains(component);
          removeComponent(component);
          if (point!=null) insertComponent(isRow, point, component);
          if (table!=null) table.updateWord();
       }
       dragComponentNo = -1;
    }

    /** Start drag operation */
    public void mousePressed(MouseEvent event)
    {  if (event.isPopupTrigger())  { processEvent(event); }   }

    /** Process a right click event */
    private void processEvent(MouseEvent event)
    {   Point mousePosition = getMousePosition(true);
        if (mousePosition==null) return;

        widgetCell = findWidget(mousePosition);
        if (widgetCell!=null)
        {   JMenuItem item = (JMenuItem)widgetMenu.getComponent(0);
            item.setEnabled(widgetCell instanceof TextAreaWidget);

            item = (JMenuItem)widgetMenu.getComponent(1);
            Method setCellWidth = getCellWidthMethod(widgetCell);
            item.setEnabled(setCellWidth!=null);

            widgetMenu.show(event.getComponent(), event.getX(), event.getY());
            return;
        }
        mouseOffset       = mousePosition;
        Rectangle bounds  = columns.get(0).getBounds();

        JPopupMenu popup;
        boolean pastable = isPastable();
        if (mousePosition.y > (bounds.height + bounds.y))
        {  popup = rowMenu;
           if (rowPasteOption!=null) rowPasteOption.setEnabled(pastable);
        }
        else  
        {  popup = columnMenu;
           if (columnPasteOption!=null) columnPasteOption.setEnabled(pastable);
        }
        WordListWidget table = (WordListWidget)getTable();
        table.setSelectionsEnabled(false);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }

    /** Find which widget user right clicked over */
    private JComponent findWidget(Point mousePosition)
    {   Rectangle bounds;

        for (int i=0; i<columns.size(); i++)
        {   bounds = columns.get(i).getBounds();
            if (bounds.contains(mousePosition)) return columns.get(i);
        }
        for (int i=0; i<rows.size(); i++)
        {   bounds = rows.get(i).getBounds();
            if (bounds.contains(mousePosition)) return rows.get(i);
        }
        return  null;
    }
static int count;
    public void mouseExited(MouseEvent event)   
    {}
    public void mouseEntered(MouseEvent event)  
    {}
    public void mouseMoved(MouseEvent event)    {}

    public void mouseDragged(MouseEvent event)
    {   WordListWidget table = (WordListWidget)getTable();
        table.setSelectionsEnabled(false);
        
        if (!getCursor().equals(Cursor.getDefaultCursor())) return;
        dragComponentNo = -1;

       Object object = event.getSource();
       if (object==columns.get(0) || object==this) return;

       if (object instanceof JComponent)
       {   setCursor(new Cursor(Cursor.MOVE_CURSOR));

           dragRow = true;
           for (int i=0; i<rows.size(); i++)
           {   if (object==rows.get(i))
               {  dragComponentNo = i;
                  return;
               }
           }

           dragRow = false;
           for (int i=0; i<columns.size(); i++)
           {   if (object==columns.get(i))
               {  dragComponentNo = i;
                  return;
               }
           }
       }
    }       // End of mouseDragged()

    /** Respond to ontology selections (set the parent and child ontology names)
     *
     * @param event The event triggering the action
     */
    public void actionPerformed(ActionEvent event)
    { 
      Object object = event.getSource();
      if (!(object instanceof JMenuItem)) return;

      JMenuItem item = (JMenuItem)object;
      String name = item.getName();

      TemplateWidget widget = null;
      JComponent component = null;

      // Process word options
      WordListWidget table = (WordListWidget)getTable();
      table.setSelectionsEnabled(true);

      if (name.equals("Insert"))  { insertWord(); return; }
      if (name.equals("Remove"))  { removeWord(); return; }
      if (name.equals("Cut"))     { cutWord();    return; }
      if (name.equals("Paste"))   { pasteWord();  return; }

      // Process widget options
      JFrame root = Environment.getRootFrame();
      if (name.equals("Choose Font"))
      {   Font font = widgetCell.getFont();
          FontDialog fontChooser = new FontDialog(root, font);
          font = fontChooser.getSelectedFont();
          Method setIndigenousFont = getCellFontMethod(widgetCell);
          try { if (setIndigenousFont!=null)
                         setIndigenousFont.invoke(widgetCell, font); }
         catch (Exception e)
         {  
        	 getErr().setText(e.toString()); 
         }
         table.updateWord();
          return;
      }

      if (name.equals("Cell Size"))
      {  
    	 int width = widgetCell.getSize().width;
         CellSizeDialog cellChooser = new CellSizeDialog(root, width);
         width = cellChooser.getSelectedWidth();
         if (width<0) return;
         Method setCellWidth = getCellWidthMethod(widgetCell);
         try { if (setCellWidth!=null) setCellWidth.invoke(widgetCell, width); }
         catch (Exception e)
         {  
        	 getErr().setText(e.toString()); 
         }
         table.updateWord();
         return;
      }

      // Column Components
      Dimension size = new Dimension(COLUMN_WIDGET_WIDTH, AREA_WIDGET_HEIGHT);
      for (int i=0; i<textAreas.length; i++)
      {  if (name.equals(textAreas[i]))
         {  Comment comment = new Comment(name, size);
            widget = (TemplateWidget)WidgetFactory.createWidget(comment);
            break;
         }
      }

      if (widget==null)
	      for (int i=0; i<ONTOLOGY_NAMES.length; i++)
	      {   if (name.equals(ONTOLOGY_NAMES[i]))
	          {  Ontology ontology = new Ontology();
	             ontology.setType(i);
	             ontology.setSize(null);
	             widget = (TemplateWidget)WidgetFactory.createWidget(ontology);
	             break;
	          }
	      }
      
      if (widget==null && name.equals("ColumnComment"))
      {  
    	 Comment comment = new Comment("", size);
         widget = (TemplateWidget)WidgetFactory.createWidget(comment);
      }
      
      if (widget==null && name.equals("Lexical Function"))
      {
    	  Unit unit = new Unit(name);
          widget = (TemplateWidget)WidgetFactory.createWidget(unit);
      }

      if (widget==null)
      {
          for (int i=0; i<references.length; i++)
          {  if (name.equals(references[i]))
             {  
        	    Reference reference = new Reference(references[i]);
                widget = (TemplateWidget)WidgetFactory.createWidget(reference);
                break;
             }
          }

      }
      
      if (widget!=null)
      {
    	 insertComponent(false, mouseOffset, widget);
    	 this.addListenerToComponents(widget);
    	 widget.addMouseMotionListener(this);
    	 widget.addMouseListener(this); 	   
         setDimension();
         table.updateWord();
         return;
     }

      // Row components
      int rowWidth = DEFINITION_WIDGET_WIDTH;
      if (word) rowWidth = ROW_WIDGET_WIDTH;

      if (name.equals("RowComment"))
      {  
    	 size = new Dimension(rowWidth, AREA_WIDGET_HEIGHT);
         Comment comment = new Comment("", size);
         component = (TemplateWidget)WidgetFactory.createWidget(comment);
      }
      if (name.equals("Definition"))
      {  Group group = new Group("Definition");
         group.setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));
         size = group.getMedia().getSize();
         size.width = DEFINITION_WIDGET_WIDTH;
         group.getMedia().setSize(new Dimension(size));
         component = (JComponent)WidgetFactory.createWidget(group);
      }
      if (name.equals("Example"))
      {   
    	  Unit unit = new Unit("Example");
          unit.setSize(new Dimension(EXAMPLE_WIDGET_WIDTH, TEXT_HEIGHT));
          component = (TemplateWidget)WidgetFactory.createWidget(unit);
      }

      if (component!=null)
      {   insertComponent(true, mouseOffset, component);
          component.addMouseMotionListener(this);
          component.addMouseListener(this);
      }
      
      setDimension();
      if (component!=null && table!=null)
      {   table.updateWord();
      }
    }     // End of actionPerformed()

    /** Return the height needed for text */
    public int getRowHeight()
    {  FontMetrics metrics = getFontMetrics(getFont());
       return metrics.getHeight() + GAP;
    }

    /** Remove the column at the designated offset.
     *
     * @param offset The x position of the widget to remove
     */
    private void removeComponent(JComponent component)
    {  setVisible(false);
       columns.remove(component);
       rows.remove(component);
       remove(component);
       setVisible(true);
    }

    /** Insert a new widget at the designated offset
     *
     * @param row true if adding a row widget, false if adding a column widget
     * @param mouse The mouse position of the widget to insert
     * @param component The widget to insert
     */
    private void insertComponent(boolean row, Point mouse, JComponent component)
    {   ArrayList<JComponent> components = columns;
        int offset = mouse.x;

        setVisible(false);
        if (row)
        {  components = rows;
           offset = mouse.y;
        }

        Rectangle bounds;
        int spot;
        JComponent widget;
        boolean inserted = false;
        for (int i=(row)?0:1; i<components.size(); i++)
        {   widget = components.get(i);
            bounds = widget.getBounds();
            spot = bounds.x;
            if (row) spot = bounds.y;
            if (spot >=offset)
            {  components.add(i, component);
               inserted = true;
               break;
           }
       }
       if (!inserted) components.add(component);
       component.setFocusable(true);
       add(component);
       if (row) moveToBack(component);
       else     moveToFront(component);

       setVisible(true);
    }

    /** Remove the selected word from the dictionary */
    public void removeWord()
    {  WordListWidget table = (WordListWidget)getTable();
       Point position = table.getMousePosition(true);
       if (position==null)
       {  Toolkit.getDefaultToolkit().beep(); return; }

       int row = table.rowAtPoint(position);
       DictionaryTableModel model = (DictionaryTableModel)table.getModel();
       Word removeWord = (Word)model.getValueAt(row, 0);
       UndoRedoWord undo = new UndoRedoWord(removeWord, null, row);
       getDict().push(undo);
       model.removeRow(row);
    }

    /**  Cut a word so it can be stored in a different location */
    public void cutWord()
    {  WordListWidget table = (WordListWidget)getTable();
       Point position = table.getMousePosition(true);
       if (position==null)
       { Toolkit.getDefaultToolkit().beep(); return; }

       int row = table.rowAtPoint(position);
       Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
       DictionaryTableModel model = (DictionaryTableModel)table.getModel();
       Word cutWord = (Word)model.getValueAt(row, 0);
       clip.setContents(cutWord, null);
       UndoRedoWord undo = new UndoRedoWord(cutWord, null, row);
       getDict().push(undo);
       model.removeRow(row);
    }

    /** Add a new word to the dictionary */
    public void insertWord()
    {  WordListWidget table = (WordListWidget)getTable();
       Point position = table.getMousePosition(true);
       int row = table.getRowCount()-1;
       if (position!=null) row = table.rowAtPoint(position);

       DictionaryTableModel model = (DictionaryTableModel)table.getModel();
       Word addWord = null;
       if (row>=0)
    	   addWord = (Word)model.getValueAt(row, 0);
 
       addWord = new Word(addWord);
       
       Word[] words = {addWord};
       UndoRedoWord undo = new UndoRedoWord(null, addWord, row);
       getDict().push(undo);
       model.insertRow(row, words);

       table.setRowSelectionInterval(row, row);
       table.editCellAt(row, 0);
    }

    public void pasteWord()
    {  WordListWidget table = (WordListWidget)getTable();

       Point position = table.getMousePosition(true);
       int row = table.getRowCount();
       if (position!=null) row = table.rowAtPoint(position);

       Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
       try
       {  Word pasteWord 
               = (Word)clip.getContents(null).getTransferData(Word.WORD_FLAVOR);
          if (pasteWord==null)
          {   
        	  getErr().setText("The clipboard has no contents");
              return;
          }
          DictionaryTableModel model = (DictionaryTableModel)table.getModel();
          Word[] words = {pasteWord};
          UndoRedoWord undo = new UndoRedoWord(null, pasteWord, row);
          getDict().push(undo);
          model.insertRow(row, words);
          table.setRowSelectionInterval(row, row);
          table.editCellAt(row, 0);
       }
       catch (UnsupportedFlavorException e)
       {  
    	   getErr().setText("Illegal paste operation"); 
       }
       catch (Exception e)  
       {   
    	   getErr().setText(e.toString()); 
       }
    }

    /** return true if there is a word in the clipboard to paste */
    public boolean isPastable()
    {  Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
       try
       {  Word pasteWord
               = (Word)clip.getContents(null).getTransferData(Word.WORD_FLAVOR);
          if (pasteWord==null)  { return false; }
          return true;
       }
       catch (Exception e) { return false; }
    }


    /** Set the location and bounds of each column component */
    private Dimension setDimension()
    {   Dimension size = new Dimension(0, 0);
        try
        {
            Insets insets = getInsets(), wInsets;
            int x = insets.left, height = 0;
            JComponent widget;
            Rectangle bounds;
            for (int i=0; i<columns.size(); i++)
            {   widget = columns.get(i);

                bounds = new Rectangle
                        ( x, insets.top, widget.getWidth(),
                          widget.getHeight());
                widget.setLocation(x, insets.top);
                widget.setBounds(bounds);
                x += bounds.width + COLUMN_GAP;

                if (height<bounds.height) height = bounds.height;
            }

            int y = GAP + insets.top + columns.get(0).getHeight();
            int minHeight = 0;
            if (word) minHeight = getFieldHeight() * 3 + GAP;
            if (y < minHeight) y = minHeight;
            
            for (int i=0; i<rows.size(); i++)
            {  widget = rows.get(i);
               size = widget.getSize();
               wInsets = widget.getInsets();
               bounds = new Rectangle
                       (insets.left, y, size.width,
                        size.height + wInsets.bottom);
               widget.setLocation(insets.left, y);
               widget.setBounds(bounds);
               y += size.height + ROW_GAP;
               if (size.width+ insets.left > x)  x = size.width + insets.left;
            }

            if (y > height) height = y;

            size = new Dimension
                    (x + insets.left, height + insets.top + insets.bottom );
        }  catch (Exception e) { }
        return size;
    }    // End of setDimension()

   /** Add listeners to sub-components so we can reposition or delete */
   private void addListenerToComponents(JComponent component)
   {   component.addMouseListener(this);
       component.addMouseMotionListener(this);
       for (int i=0; i<component.getComponentCount(); i++)
       {   component.getComponent(i).addMouseListener(this);
           component.getComponent(i).addMouseMotionListener(this);
       }
   }

       /** Get the height of a text or label component based on the font */
    protected int getFieldHeight()
    {  FontMetrics metrics = getFontMetrics(getFont());
       return metrics.getHeight() + GAP;
    }

    /** Set the characteristics of this cell */
    public void storeCellCharacteristics(Item item)
    {   item.setBackground(getBackground());
        item.setForeground(getForeground());
        Font font = getFont();
        if (font==null) font = new Font(null, Font.PLAIN, 12);
        item.setFont(font);
        item.setSize(getPreferredSize());
    }

    /** Load the characteristics of this cell */
    public void loadCellCharacteristics(Item item)
    {  setBackground(item.getBackground());
       setForeground(item.getForeground());
       setFont(item.getFont());
       
       Dimension size = item.getSize();
       setSize(size);
       setPreferredSize(size);
       setMaximumSize(getSize());
    }

    /** Get the method in a component to alter the cell width
     * @param component component that might contain the setCellWidth method
     */
    protected Method getCellWidthMethod(JComponent component)
    {   Class<?>[] params = { Integer.class };
        Class<?> cellClass;
        try
         {  cellClass = component.getClass();
            Method setCellWidth = cellClass.getMethod("setCellWidth", params);
            return setCellWidth;
         }
         catch (Exception e) { return null; }
    }

    protected Method getCellFontMethod(JComponent component)
    {   Class<?>[] params = { Font.class };
        Class<?> cellClass;
        try
         {  cellClass = component.getClass();
            Method setIndigenousFont
                             = cellClass.getMethod("setIndigenousFont", params);
            return setIndigenousFont;
         }
         catch (Exception e) { return null; }
    }



    /** Get environment data */
    protected Environment getEnv()
    {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
        return rootPanel.getEnv();
    }

    /** Get the label for displaying errors */
    protected JLabel getErr()
    {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
        return rootPanel.getErrorLabel();
    }

    /** Get the dictionary word list table */
    protected JTable getTable()
    {  RootDictionaryPanel rootPanel = getRootDictionaryPanel();
       return rootPanel.getWordTable();
    }

    /** Get the active dictionary */
    protected DictionaryData getDict()
    {  RootDictionaryPanel rootPanel = getRootDictionaryPanel();
       return rootPanel.getDictionaryData();
    }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }
    
}       // End of GroupWidget class
