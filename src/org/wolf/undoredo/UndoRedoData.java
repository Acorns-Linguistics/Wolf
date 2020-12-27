/**
 * UndoRedoData.java
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

import java.awt.Toolkit;
import java.beans.PropertyChangeListener;

import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.DictionaryData;
import org.wolf.system.Environment;
import org.wolf.widgets.WordListWidget;

/** Base class for objects pushed and popped onto the undo redo stack */
public abstract class UndoRedoData
{   public UndoRedoData() {}
    public abstract String undo(DictionaryData dictionary);
    public abstract String redo(DictionaryData dictionary);

        /** Get the environment  object */
    protected Environment getEnv()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getEnv();
    }

    /** Get the word list component object */
    protected WordListWidget getTable()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getWordTable();
    }

    /** Retrieve the root dictionary panel */
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

}   // End of UndoRedo class
