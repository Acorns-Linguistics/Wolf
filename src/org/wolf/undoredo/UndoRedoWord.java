/*
 * UndoRedoWord.java
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

package org.wolf.undoredo;

import javax.swing.table.DefaultTableModel;

import org.wolf.data.DictionaryData;
import org.wolf.data.Word;
import org.wolf.widgets.WordListWidget;

/** Class to undo and redo word changes */
public class UndoRedoWord extends UndoRedoData
{   private Word oldWord, newWord;
    int row;

    public UndoRedoWord(Word oldWord, Word newWord, int row)
    {  this.oldWord = oldWord;
       this.newWord = newWord;
       this.row = row;
    }
    public String redo(DictionaryData data) 
    {  return restore(oldWord, newWord, row);
    }
    public String undo(DictionaryData data) 
    {  return restore(newWord, oldWord, row);
    }

    private String restore(Word from, Word to, int row)
    {
        WordListWidget table  = getTable();
        DefaultTableModel model = (DefaultTableModel)table.getModel();

        if (from==null  && to==null) return "No change to restore";
        else if (from==null)
        {   Object[] words = new Object[1];
            words[0] = to;
            model.insertRow(row, words);
        }
        else if (to==null) { model.removeRow(row); }
        else  {  table.updateWord(to, row);  }
        return "";
    }

}  // End of UndoRedoWord class
