/*
 * UndoRedo.java
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

import java.awt.Toolkit;
import java.beans.PropertyChangeListener;

import org.wolf.application.DictionaryPanel;
import org.wolf.application.RootDictionaryPanel;

/** Definition of the Wolf undo/redo stack. */
public class UndoRedo
{   /** The maximum size of the ACORNS undo and redo stacks */
    public static final int MAX_UNDO = 5;
    
    int redoTop, undoTop;
    UndoRedoData[] redoStack, undoStack;
    
    /** Creates a new instance of UndoRedo using the default maximum size */
    public UndoRedo()  {  initialize(MAX_UNDO); }
    /** Creates a new instance with a specified maximum size */
    public UndoRedo(int max)  {  initialize(max); }
    
    private void initialize(int max)
    {  redoStack = new UndoRedoData[max];
       undoStack = new UndoRedoData[max];
       resetRedoUndo();
    }
    
    /** Reset the redo and undo stacks
    */
    public void resetRedoUndo()  
    { redoTop = undoTop = -1;
      setEnabled(DictionaryPanel.UNDO, false);
      setEnabled(DictionaryPanel.REDO, false);
    }
    
    /** Determine whether the redo stack is empty
    *
    *  @return true if redo stack is empty
    */
   public boolean isRedoEmpty()   { return (redoTop==-1); }
	
   /** Determine whether the undo stack is empty
    *
    *  @return true if undo stack is empty
    */
   public boolean isUndoEmpty()   { return (undoTop==-1); }

   /** Peek at the top of the redo stack */
   public UndoRedoData peekRedo()
   { if (isRedoEmpty()) return null;
     return redoStack[redoTop];
   }
	
   /** Peek at the top of the redo stack */
   public UndoRedoData peekUndo()
   { if (isUndoEmpty()) return null;
     return undoStack[undoTop];
   }

   /** Process a redo operation
    *
    * @param current The object's cucrrent state
    * @return The state after the redo operation
    */
   public UndoRedoData redo(UndoRedoData current)
   {
      if (isRedoEmpty()) return null;

      // Push current data into undo stack
      if (undoTop==undoStack.length - 1) undoStack = shift(undoStack);
      else undoTop++;
      
	     UndoRedoData data = redoStack[redoTop--];
      if (current==null) current = data;
      undoStack[undoTop] = current;
      setEnabled(DictionaryPanel.UNDO, true);
      setEnabled(DictionaryPanel.REDO, !isRedoEmpty());
      return data;
   }
	
   /** Process an undo operation
    *
    * @param current The current objects state
    * @return The state after the undo operation
    */
   public UndoRedoData undo(UndoRedoData current)
   {
      if (isUndoEmpty()) return null;
	
      // Remove from undo stack and add to redo stack.
      UndoRedoData data = undoStack[undoTop--];
      if (redoTop==redoStack.length-1) redoStack = shift(redoStack);
      else redoTop++;

      if (current==null) current = data;
      redoStack[redoTop] = current;
      setEnabled(DictionaryPanel.REDO, true);
      setEnabled(DictionaryPanel.UNDO, !isUndoEmpty());
      return data;
   }

   /** Replace top entry of stack
    *
    * @param current Current data object
    * @param undo true for undo stack, fals for redo stack
    */
   public void replaceUndoRedoTop(UndoRedoData current, boolean undo)
   {
      if (undo) undoStack[undoTop] = current;
      else      redoStack[redoTop] = current;
   }
	
   /** Push an undo command onto the undo stack 
    * @param data The object containing data to be pushed.
    */
   public void pushUndo(UndoRedoData data)
   {
      if (undoTop==undoStack.length-1) undoStack = shift(undoStack);
      else undoTop++;
      
      undoStack[undoTop] = data;
      redoTop = -1;
      setEnabled(DictionaryPanel.UNDO, true);
   }
   
   // Method to shift entries in the stacks if necessary.
   private UndoRedoData[] shift(UndoRedoData[] stack)
   {   for (int s=1; s<stack.length; s++)  stack[s-1] = stack[s];
       return stack;
   }

    /** Retrieve the root dictionary panel */
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

    /** Adjust the ghostable buttons in the dictionary panel */
    public void setEnabled(int type, boolean flag)
    {  RootDictionaryPanel panel = getRootDictionaryPanel();
       DictionaryPanel buttons = panel.getButtonPanel();
       buttons.enableButton(type, flag);
    }

   
}  // End of UndoRedo class.