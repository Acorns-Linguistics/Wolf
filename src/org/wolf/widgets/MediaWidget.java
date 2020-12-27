/*
 * MediaWidget.java
 *    Class to maintain audio, video, and pictures dropped into the dictionary
 *
 *   @author  harveyd
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.acorns.data.PictureData;
import org.acorns.data.SoundData;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.MovieData;
import org.wolf.data.Unit;
import org.wolf.dialogs.MediaDialog;
import org.wolf.lib.Icons;
import org.wolf.system.Environment;
import org.wolf.system.MultimediaManager;

/** Handle multimedia entry and playback */
public class MediaWidget extends JSpinner 
                              implements MouseListener, MouseMotionListener
{   
	private static final long serialVersionUID = 1L;
	private static int ICON = 20;
    private static final int NONE = 0, AUDIO = 1, PICTURE = 2, MOVIE = 3;
    private static final int MAX_FILE_LENGTH = 40000000;

    private int         soundIndex;
    private int         pictureIndex;
    private int         movieIndex;

    private Environment environment;
    private String      category;

    private boolean     drag;  // Flag to determine if a drag is occurring
    
    /** Constructor to create the object holding multimedia icons
     *
     * @param env The application environment
     * @param category "Definition", "Word", or "Example"
     */
    public MediaWidget(Environment env, String category)
    {  
       this.category = category;

       soundIndex = movieIndex =  pictureIndex = -1;
       setEditor(new MediaEditor(env, this));
       environment = env;

       setModel(new MediaSpinnerModel(getMediaOptionValue()));
       setPreferredSize(new Dimension(40,25));
       setMaximumSize(getPreferredSize());
       setMinimumSize(getPreferredSize());
       setSize(getPreferredSize());
       setFocusable(true);
       addMouseListener(this);
       addMouseMotionListener(this);
   }

    /** Set MediaObject option value */
    public int getMediaOptionValue()
    {  int options = 0;
       if (soundIndex>=0)   options |= 1<<(AUDIO-1);
       if (pictureIndex>=0) options |= 1<<(PICTURE-1);
       if (movieIndex>=0)   options |= 1<<(MOVIE-1);
       return options;
    }

    /** Return cell with updated data */
    public Unit updateCell()
    { 
      Unit unit = new Unit(category);
      unit.setAudio(soundIndex);
      unit.setPicture(pictureIndex);
      unit.setMovie(movieIndex);

      MediaSpinnerModel model = (MediaSpinnerModel)getModel();
      int index = model.getIndex();
      unit.setIndex(index);
      return unit;
    }

    /** Format this cell with multimedia data */
    public void formatCell(Unit unit)
    {  
       soundIndex = unit.getAudio();
       pictureIndex = unit.getPicture();
       movieIndex = unit.getMovie();
       category = unit.getCategory();
       int index = unit.getIndex();

       MediaSpinnerModel model = (MediaSpinnerModel)getModel();
       int options = getMediaOptionValue();
       model.setOptions(options);
       model.setIndex(index);
    }
  
   /** Determine if an extension goes with a valid media type
     *
     * @param name of file to check
     * @return true if accepted, false otherwise
     */
    public boolean isMedia(File file)
    {   String extension = file.getName();
        extension = extension.substring(extension.lastIndexOf(".")+1);
        if (file.length()>MAX_FILE_LENGTH)
        {   getErr().setText("File exceeds maximum allowable size");
            return false;
        }
        if (environment.isAudio(extension))   return true;
        if (environment.isVideo(extension))   return true;
        if (environment.isPicture(extension)) return true;
        return false;
    }

    /** Method to handle drops of media files into the spinner object
     *
     * @param file The file object with the location of the
     * @throws IOException
     * @throws UnsupportedAudioFileException
     * @throws NoPlayerException
     */
    public void mediaDropped(File file)
          throws IOException, UnsupportedAudioFileException
    {
        URL url = file.toURI().toURL();
        if (url==null) throw new FileNotFoundException();

        MultimediaManager manager = getEnv().getMultimediaManager();

        int index = 0;
        String extension = file.getName();
        extension = extension.substring(extension.lastIndexOf(".")+1);

        Object[] mediaObjects = new Object[2];
        int[] mediaIndices = new int[2];

        if (environment.isAudio(extension))
        {  mediaIndices[0] = soundIndex;
           SoundData sound = new SoundData();
           sound.readFile(url);
           mediaObjects[1] = sound;

           index = AUDIO;
           mediaIndices[0] = soundIndex;
           mediaObjects[1] = sound;
           if (soundIndex>=0) mediaObjects[0] = manager.readObject(soundIndex);
           soundIndex = manager.writeObject(soundIndex, sound);
           mediaIndices[1] = soundIndex;
        }
        else if (environment.isPicture(extension))
        {  PictureData picture = new PictureData(url, null);
           index = PICTURE;
           mediaIndices[0] = pictureIndex;
           mediaObjects[1] = picture;
           if (pictureIndex>=0) mediaObjects[0] = manager.readObject(pictureIndex);
           pictureIndex = manager.writeObject(pictureIndex, picture);
           mediaIndices[1] = pictureIndex;
        }
        else if (environment.isVideo(extension))
        {  index = MOVIE;
           mediaIndices[0] = movieIndex;
           if (movieIndex>=0) mediaObjects[0] = manager.readObject(movieIndex);

           MovieData movie = new MovieData();
           movie.reset(file);
           mediaObjects[1] = movie;
           movieIndex = manager.writeObject(movieIndex, movie);
           mediaIndices[1] = movieIndex;
        }

        updateMedia(mediaObjects, mediaIndices);
        int options = getMediaOptionValue();
        MediaSpinnerModel model = (MediaSpinnerModel)getModel();
        model.setOptions(options);
        model.setIndex(index);

     }
    
    public void mousePressed(MouseEvent event)  {}
    public void mouseEntered(MouseEvent event)  {}

    /** Remove multimedia object when dragged away from the panel */
    public void mouseReleased(MouseEvent event) 
    {  if (drag==false) return;
       
       drag = false;
       if (getMousePosition()!=null) return;

       MediaSpinnerModel model = (MediaSpinnerModel)getModel();
       MultimediaManager manager = getEnv().getMultimediaManager();
       int index = model.getIndex();

       Object[] mediaObjects = new Object[2];
       int[] mediaIndices = new int[2];
       mediaIndices[1] = -1;
       mediaObjects[1] = null;

       switch (index)
       {   case AUDIO:
               mediaIndices[0] = soundIndex;
               if (soundIndex>=0)
               {   mediaObjects[0] = manager.readObject(soundIndex);
                   try { manager.writeObject(soundIndex, ""); }
                   catch(Exception e) {}
               }
               soundIndex = -1;
               break;

           case MOVIE:
               mediaIndices[0] = movieIndex;
               if (movieIndex>=0)
               {   mediaObjects[0] = manager.readObject(movieIndex);
                   try { manager.writeObject(movieIndex, ""); }
                   catch(Exception e) {}
               }
               movieIndex = -1;
               break;

           case PICTURE:
               mediaIndices[0] = pictureIndex;
               if (pictureIndex>=0)
               {   mediaObjects[0] = manager.readObject(pictureIndex);
                   try { manager.writeObject(pictureIndex, ""); }
                   catch(Exception e) {}
               }
               pictureIndex = -1;
               break;
       }
       updateMedia(mediaObjects, mediaIndices);
       int options = getMediaOptionValue();
       model.setOptions(options);
    }

    public void mouseExited(MouseEvent event)   {}

    /** Visual display of the multimedia object */
    public void mouseClicked(MouseEvent event)  
    {  MediaSpinnerModel model = (MediaSpinnerModel)getModel();
       int index = model.getIndex();
       MultimediaManager manager = getEnv().getMultimediaManager();
       MediaDialog dialog = null;

       Object[] mediaObjects = new Object[2];
       int[] mediaIndices = new int[2];
       
       if (soundIndex<0 && movieIndex<0 && pictureIndex<0) index = AUDIO;

       switch (index)
        {  case NONE:
           case AUDIO:
               mediaIndices[0] = soundIndex;
               SoundData sound = new SoundData();
               Object soundObject;
               if (soundIndex>=0)
               {  soundObject = manager.readObject(soundIndex);
               	  if (soundObject != null && soundObject instanceof SoundData) 
               		  sound = (SoundData)soundObject;
               	  else soundIndex = -1;
                  mediaObjects[0] = sound.clone();
               }
               dialog = new MediaDialog(environment, sound);
               if (!dialog.isChanged()) return;

               mediaObjects[1] = (SoundData)dialog.getMediaObject();
               try
               {  soundIndex = manager.writeObject(soundIndex, mediaObjects[1]);
                  mediaIndices[1] = soundIndex;
                  updateMedia(mediaObjects, mediaIndices);
               }
               catch (Exception e) {}
               break;

           case MOVIE:
               if (movieIndex>=0)
               {   mediaObjects[0] = manager.readObject(movieIndex);
                   mediaIndices[0] = movieIndex;

                   dialog = new MediaDialog(environment, mediaObjects[0]);
                   if (!dialog.isChanged()) return;

                   mediaObjects[1] = (MovieData)(dialog.getMediaObject());
                   try
                   {  movieIndex 
                              = manager.writeObject(movieIndex, mediaObjects[1]);
                      mediaIndices[1] = movieIndex;
                      updateMedia(mediaObjects, mediaIndices);
                   }
                   catch (Exception e) {}
               }
               break;

           case PICTURE:
               if (pictureIndex>=0)
               {   mediaObjects[0] = manager.readObject(pictureIndex);
                   mediaIndices[0] = pictureIndex;

                   dialog  = new MediaDialog(environment, mediaObjects[0]);
                   if (!dialog.isChanged()) return;

                   mediaObjects[1] = (PictureData)(dialog.getMediaObject());
                   try
                   {   pictureIndex = manager.writeObject
                                                (pictureIndex, mediaObjects[1]);
                       mediaIndices[1] = pictureIndex;
                       updateMedia(mediaObjects, mediaIndices);
                   }
                   catch (Exception e) {}
               }
       }

       int options = getMediaOptionValue();
       model.setOptions(options);
       model.setIndex(index);
    }       // End of mouseClicked()

    public void mouseMoved(MouseEvent event) {}
    public void mouseDragged(MouseEvent event)  {  drag = true; }

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

   /** Get the label for displaying errors */
    protected void updateMedia(Object[] media, int[] indices)
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        WordListWidget table = rootPanel.getWordTable();
        table.updateMedia(media, indices);
    }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

    /** Embedded class to handle the display of the spinner icons */
    class MediaEditor extends JLabel implements ChangeListener
    {   
		private static final long serialVersionUID = 1L;

		Environment environment;

        JSpinner spinner;
        Icon icon;

    public MediaEditor(Environment env, JSpinner s)
	{    
    	 super("", CENTER);

         environment = env;
         icon = Icons.getImageIcon("template.png", ICON);
         spinner = s;
	     spinner.addChangeListener(this);
	     setPreferredSize(new Dimension(ICON, ICON));
	     setMinimumSize(getPreferredSize());
	     setMaximumSize(getPreferredSize());
	}

	public void stateChanged(ChangeEvent ce)
	{  icon = (Icon) spinner.getValue();
	   setIcon(icon);
	}

	public JSpinner getSpinner()    { return spinner;  }
	public @Override Icon getIcon() { return icon; }
    } // End of MediaEditor class

    /** Embedded class to control which spinner icons are active */
    class MediaSpinnerModel extends SpinnerListModel
    {  
	   private static final long serialVersionUID = 1L;
	   private ArrayList<ImageIcon> icons;
       private boolean[] exists = {false, false, false, false};
       private int count = 1, index = 0;

       /** Constructor to initialize the active icons
        *
        * @param options AUDIO | MOVIE | PICTURE;
        */
       public MediaSpinnerModel(int options)
       {  String[] names
                    = {"template.png", "audio.png", "image.png", "video.png" };
          icons = new ArrayList<ImageIcon>();
          for (int i=0; i<names.length; i++)
          {  icons.add(Icons.getImageIcon(names[i], ICON));
             super.setList(icons);
          }
          setOptions(options);
       }

       public @Override Object getNextValue()
       {   Object value = icons.get(0);

           if (count!=0)
           {  for (int i=0; i<exists.length; i++)
              {  index = (index+1)%exists.length;
                 if (exists[index])  { value = icons.get(index); break; }
                 else if (index==NONE && !exists[AUDIO]) { break; }
              }
           }
           super.setValue(value);
           return value;
       }

       public @Override Object getPreviousValue()
       {  Object value = icons.get(0);
          if (count!=0)
          {  for (int i=0; i<exists.length; i++)
             {  index--;
                if (index<0) index = exists.length-1;
                if (exists[index])  { value = icons.get(index); break; }
                else if (index==NONE && !exists[AUDIO]) { break; }
             }
          }
          super.setValue(value);
          return value;
       }

       public @Override Object getValue()
       {   Object value = super.getValue();
           if (count!=0 && index == 0) return getNextValue();
           if (value!=icons.get(index) && exists[index])
               value = icons.get(index);
           super.setValue(value);
           return value;
       }

       /** Get index to media selection type */
       private int getIndex()  { return index; }
       public void setIndex(int index)
       { this.index = index;
         getValue();
       }

       /** Set count of objects */
       /** Method to set options based on the input parameter
        *  @param AUDIO | VIDEO | PICTURE
        *
        *  Note: if option is specified set it, otherwise remove it
        */
       public void setOptions(int options)
       {  count = 0;
          for (int i=1; i<exists.length; i++)
          {  if ( (options & 1) != 0)
             {   exists[i] = true;
                 count++;
             }
             else exists[i] = false;
             options /= 2;
          }
          if (count==0) {  index = 0; super.setValue(icons.get(0)); }
          else if (!exists[index]) getNextValue();
       }
    }  // End of MediaSpinnerModel class

}      // End of MediaWidget class
