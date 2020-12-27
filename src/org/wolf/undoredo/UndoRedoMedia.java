/*
 * UndoRedoMedia.java
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

import org.wolf.data.DictionaryData;
import org.wolf.data.Language;
import org.wolf.data.Word;
import org.wolf.system.MultimediaManager;
import org.wolf.widgets.WordListWidget;

/** Class to undo and redo multimedia media changes */
public class UndoRedoMedia extends UndoRedoData
{   private final static int OLD=0, NEW=1;
    
    Word[] words;
    Object[] objects;
    int[] indX;
    int   row;

    public UndoRedoMedia(Word words[], int row, Object[] objects, int[] indX)
    { this.words = words;
      this.row = row;
      this.objects = objects;
      this.indX= indX;
    }

    public String redo(DictionaryData data)
    {  return restore(data,row, words[NEW], objects[NEW], indX[OLD], indX[NEW]);
    }
    public String undo(DictionaryData data) 
    {  return restore(data,row, words[OLD], objects[OLD], indX[NEW], indX[OLD]);
    }

    /** Method to restore the word to its previous  state */
    private String restore( DictionaryData data, int row, Word word,
                        Object newMult, int oldX, int newX)
    {
        if (newX<0 && oldX<0) return "No media objects to restore";
        Language language = data.getActiveLanguage();
        if (language==null) return "Couldn't find active language";

        MultimediaManager manager = getEnv().getMultimediaManager();
        try
        {  // Remove new multimedia object
            if (newX<0)
            {  manager.writeObject(oldX, "");
            }
            // Replace or insert themultimedia object
            else
            {  manager.writeObject(newX, newMult);
            }

            WordListWidget table = getTable();
            table.updateWord(word, row);
        }
        catch (Exception e) { return e.toString(); }
        return "";
    }

}  // End of UndoRedoMedia class
