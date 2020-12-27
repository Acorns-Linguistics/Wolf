/*
 * DictionaryDropTarget.java
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

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.List;

/**  Drop dictionary files into the application */
public class DictionaryDropTarget implements DropTargetListener
{  Method mediaDroppedMethod, mediaIsMediaMethod;
   Object dropObject;

   public DictionaryDropTarget(Container component, Object dropObject)
   {  new DropTarget(component, this);
      this.dropObject = dropObject;
      Class<?> dropClass = dropObject.getClass();

      try
      {  mediaDroppedMethod = dropClass.getMethod
                  ("mediaDropped", new Class[]{File.class} );

         mediaIsMediaMethod = dropClass.getMethod
                  ("isMedia", new Class[]{File.class} );
      }
      catch (NoSuchMethodException ex) {}
   }

   /** Method to process drops into the panel
    *
    * @param dtde The event triggering this method to execute
    */
   public void drop(DropTargetDropEvent dtde)
    {   dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        File[] files = getTransferObjects(dtde.getTransferable());
        if (files == null || files.length==0)
        { Toolkit.getDefaultToolkit().beep();
          dtde.dropComplete(false);  return;
        }

        try
        { mediaDroppedMethod.invoke(dropObject, files[0]); }
        catch (Exception ex)
        {  Toolkit.getDefaultToolkit().beep();
           dtde.dropComplete(false);
           return;
        }
        dtde.dropComplete(true);
    }

    /** Method to degermine if drags to this object are okay
     *
     * @param dtde The triggering event
     */
    public void dragEnter (DropTargetDragEvent dtde)
    {
        if (!acceptIt(dtde.getTransferable())) dtde.rejectDrag();
        else
        {  File[] files = getTransferObjects(dtde.getTransferable());
           if (files == null || files.length==0) dtde.rejectDrag();
           else  dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
    }

    public void dragExit (DropTargetEvent dte) {}

    public void dragOver (DropTargetDragEvent dtde)
    {  if (!acceptIt(dtde.getTransferable())) dtde.rejectDrag();
       else
       {  File[] files = getTransferObjects(dtde.getTransferable());
           if (files == null || files.length==0) dtde.rejectDrag();
           else dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
       }
    }

    public void dropActionChanged (DropTargetDragEvent dtde)
    {   if (!acceptIt(dtde.getTransferable())) dtde.rejectDrag(); }

    /** Method to determine if drop type is correct
     *
     * @param transfer The transferable object
     * @return true if acceptible drop type.
     */
    private boolean acceptIt(Transferable transfer)
    {   DataFlavor[] flavors = transfer.getTransferDataFlavors();
        for (int i=0; i<flavors.length; i++)
        { if (flavors[i].getRepresentationClass() == List.class) return true;
           if (flavors[i].getRepresentationClass() == AbstractList.class)
                return true;
        }
        return false;
    }

    /** Method to get the transferable list of files
     *
     * @param transfer The transferable object
     * @return An array of file objects or (null if none)
     */
    private File[] getTransferObjects(Transferable transfer)
    {
        DataFlavor[] flavors = transfer.getTransferDataFlavors();
        File[] file = new File[1];

        DataFlavor listFlavor = null;
        AbstractList<?> list = null;

        for (int i=0; i<flavors.length; i++)
        {  if (flavors[i].getRepresentationClass() == List.class)
                listFlavor = flavors[i];
           if (flavors[i].getRepresentationClass() == AbstractList.class)
                listFlavor = flavors[i];
        }

        try
        {  if (listFlavor!=null)
           {   list = (AbstractList<?>)transfer.getTransferData(listFlavor);

               int size = list.size();
               file = new File[size];

               for (int i=0; i<size; i++)
               {  file[i] = (File)list.get(i);

                  try
                  {  mediaIsMediaMethod.invoke(dropObject, file[i]); }
                  catch (Exception  e) { return null; }
               }  // End for
           }
        }
        catch (Throwable e) { return null; }
        return file;
    }   // End acceptIt()

}  // End of DictionaryDropTarget class
