/*
 * DictionaryTableModel.java
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

package org.wolf.widgets.table;

import java.awt.Toolkit;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.DictionaryData;
import org.wolf.data.Language;
import org.wolf.data.Word;
import org.wolf.widgets.WordListWidget;

/** Class to control the rows and columns of the word list JTable */
public class DictionaryTableModel extends DefaultTableModel
{  
	private static final long serialVersionUID = 1L;
    Language active;
    DictionaryData dictionary;
    WordListWidget table;

  /** Prepare the table model with the list of dictionary words
   *
   * @param table The word list JTable
   * @param dictionary The dictionary data associated with this table
   * @param active The active language
   */
  public DictionaryTableModel
          (WordListWidget table, DictionaryData dictionary, Language active)
  {  this.active = active;
     this.dictionary = dictionary;
     this.table = table;
  }

  public @Override int getColumnCount() { return 1; }
  public @Override int getRowCount()
  {   if (dictionary==null) return 0;
      active = dictionary.getActiveLanguage();
      if (active==null) return 0;
      int count = active.getWordCount();
      if (count==0)
      {  Word word = new Word();
         active.addWord(0, word);
         fireTableRowsInserted(0, 0);
      }
      return active.getViewCount();
  }

  public @Override boolean isCellEditable(int row, int col)
  {   return true; }

  public @Override String getColumnName(int column) {  return "";  }

  public @Override Object getValueAt(int row, int column)
  {  return active.getWord(row);  }

  public @Override void setValueAt(Object value, int row, int column)
  {  
	 active = dictionary.getActiveLanguage(); 
     String message = "No active language";
     if (active!=null) message = active.modifyWord( (Word)value, row);

     JLabel label = getErr();
     if (message.length()!=0)  
     {  
    	 label.setText(message);   
     }
     fireTableCellUpdated(row, 0);
  }

  /** Remove a word from the dictionary */
  public @Override void removeRow(int row)
  {   int count = active.getWordCount();
      if (count<=1)
      {  getErr().setText("Can't remove the last word");
         return;
      }

      TableCellEditor editor = table.getCellEditor();
      if (editor!=null)  editor.stopCellEditing();
      table.clearSelection();

      Word word = (Word)getValueAt(row, 0);
      String message = active.removeWord(row);
      if (message.length()==0)
      {  getErr().setText(word.getKey() + " successfully removed");
         fireTableRowsDeleted(row, row);
      }
      else getErr().setText(message);
  }

  public @Override void insertRow(int row, Object[] data)
  {
      if (data==null)
      {  getErr().setText("Illegal insertion");
         return;
      }
      Word word = (Word)data[0];
      String key = word.getKey().trim();

      TableCellEditor editor = table.getCellEditor();
      if (editor!=null)  editor.stopCellEditing();
      table.clearSelection();

      String message = active.addWord(row, word);
      if (message.length()==0)
      {  if (key.length()==0)
              getErr().setText("Word successfully added");
         else getErr().setText( word.getKey() + " successfully added");
      }
      else getErr().setText(message);
      if (message.length()==0) fireTableRowsInserted(row, row);
  }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }


  /** Get the label for displaying errors */
  private JLabel getErr()
  {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
      return rootPanel.getErrorLabel();
  }

}    // End of embedded DictionaryTableModel class
