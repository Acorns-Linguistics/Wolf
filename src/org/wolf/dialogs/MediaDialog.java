/*
 * MediaDialog.java
 *     Class to display media data visually
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

package org.wolf.dialogs;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.URL;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.acorns.data.PictureData;
import org.acorns.data.SoundData;
import org.acorns.language.LanguageText;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.MovieData;
import org.wolf.lib.Icons;
import org.wolf.system.Environment;
import org.wolf.widgets.MediaDropTarget;

/** Display or playback audio, video, and pictures */
public class MediaDialog implements ActionListener
{
    private static final int PANEL_SIZE = 500;
    private static final int MAX_FILE_LENGTH = 40000000;

    private JButton recordButton, playButton, stopButton;
    private static JPanel audioPanel;
    private JPanel picturePanel;

    private Object  mediaObject;
    private SoundData sound;
    private boolean changed;

    private Environment environment;
    private static JDialog dialog;

    /** Constructor to visually display the media data
     *
     * @param media The audio, picture, or video object
     */
    public MediaDialog(Environment env, Object media)
    {   changed = false;

        if (media==null) media = new SoundData();
        mediaObject = media;
        environment = env;

        if (mediaObject instanceof SoundData)
        {   SoundData data = audioDialog();
           if (data!=null) mediaObject = data;
        }
        if (mediaObject instanceof MovieData)
        {  MovieData video = (MovieData)mediaObject;
           try
           {   Container player = video.getMediaPanel();
               if (player!=null)
               {   new MediaDropTarget(player, this);
                   video.playVideo();
               }
          }
           catch (Exception e)
           {   JOptionPane.showMessageDialog(Environment.getRootFrame(), e.toString()); }
        }
        if (mediaObject instanceof PictureData)
        {  PictureData picture = (PictureData)mediaObject;
           Frame root = Environment.getRootFrame();

           getPicturePanel();
           picture.getImage(picturePanel,new Rectangle(0,0,-1,-1));
           JDialog jd = getDialog(root, picturePanel);
           jd.setVisible(true);
           jd.setLocationRelativeTo(Environment.getRootFrame());
        }
    }

    private JDialog getDialog(Frame root, JPanel picturePanel)
    {   if (dialog==null) dialog = new JDialog(root);

        Container container = dialog.getContentPane();
        container.removeAll();
        container.add(picturePanel);
        dialog.pack();
        return dialog;
    }

    /** Method to return the media  object */
    public Object getMediaObject() { return mediaObject; }

    /* Handle recoding sounds with annotated gloss and native data
    *  @return SoundData object or null
    */
    public SoundData audioDialog()
    { sound = (SoundData)mediaObject;
      SoundData oldSound = sound.clone();

      Frame root = Environment.getRootFrame();
      JPanel  audioDialogPanel = getAudioPanel();
      String title = LanguageText.getMessage("commonHelpSets",49);
      
      int result  = JOptionPane.showConfirmDialog(root, audioDialogPanel, title
                    , JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
                    , null);

      // Stop recording and any playback that is active.
      if (sound.isActive()) sound.stopSound();

      // We're done if the user cancels.
      if (result<0 || result == JOptionPane.CANCEL_OPTION
                   || result==JOptionPane.NO_OPTION)
      { 
    	  mediaObject = oldSound;
    	  return oldSound;
      }
      return sound;
    } // End of audioDialog method.

   public boolean isChanged()  {  return changed;  }

   /** Method to respond to dialog buttons. */
   public void actionPerformed(ActionEvent event)
   {  if (event.getSource() == recordButton)
      {  if (sound.isActive()) sound.stopSound();
         sound.record(recordButton);
         changed = true;
      }

      if (event.getSource() == playButton)
      {  if (sound!=null && sound.isActive()) sound.stopSound();
         if (sound==null || !sound.playBack(null,0,-1))
         {  JOptionPane.showMessageDialog
                    (Environment.getRootFrame(), 
                    		LanguageText.getMessage("commonHelpSets", 27));
            return;
         }
      }

      if (event.getSource() == stopButton)  {  sound.stopSound();  }
   }     // End of actionPerformed()
   
   /** Method to get the panel for playback of audio objects */
   private JPanel getAudioPanel()
   {  audioPanel = new JPanel();
      new MediaDropTarget(audioPanel, this);
      BoxLayout box = new BoxLayout(audioPanel, BoxLayout.X_AXIS);
      audioPanel.setLayout(box);

      // Create a panel of buttons for the sound file.
      ImageIcon[] icons = new ImageIcon[4];
      icons[0] = Icons.getImageIcon("record.png", 0);
      icons[1] = Icons.getImageIcon("play.png",   0);
      icons[2] = Icons.getImageIcon("stop.png",   0);
      icons[3] = Icons.getImageIcon("browse.png", 0);

      audioPanel.add(Box.createHorizontalGlue());
      recordButton = new JButton(icons[0]);
      recordButton.addActionListener(this);
      String[] recordingText
              = LanguageText.getMessageList("commonHelpSets",57);
      recordButton.setToolTipText(recordingText[0]);
      audioPanel.add(recordButton);

      playButton   = new JButton(icons[1]);
      playButton.setToolTipText(recordingText[1]);
      playButton.addActionListener(this);
      audioPanel.add(playButton);

      stopButton   = new JButton(icons[2]);
      stopButton.setToolTipText(recordingText[2]);
      stopButton.addActionListener(this);
      audioPanel.add(stopButton);
      audioPanel.add(Box.createHorizontalGlue());
      return audioPanel;       
   }

   /** Method to get the panel for showing pictures */
   public JPanel getPicturePanel()
   {
       picturePanel = new JPanel()
       {
		private static final long serialVersionUID = 1L;

		public @Override void paintComponent(Graphics page)
          {   super.paintComponent(page);

              if (mediaObject==null) return;
              if (!(mediaObject instanceof PictureData)) return;

              // Draw the background
              Graphics2D graphics = (Graphics2D)page;
              graphics.setColor(new Color(80,80,80));
              graphics.fillRect(0, 0, PANEL_SIZE, PANEL_SIZE);
              graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING
                                       , RenderingHints.VALUE_ANTIALIAS_ON);
              graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                             , RenderingHints.VALUE_INTERPOLATION_BILINEAR);

              // Get the size of the picture to place in the button
              PictureData picture = (PictureData)mediaObject;
              Dimension size = picture.getSize();
              if (size == null) return;

              Rectangle center = new Rectangle(0,0, PANEL_SIZE, PANEL_SIZE);
              double scaleX = 1.0 * PANEL_SIZE / size.width;
              double scaleY = 1.0 * PANEL_SIZE / size.height;
              double scale  = 1.0;
              Dimension newSize = new Dimension(PANEL_SIZE, PANEL_SIZE);

              // Determine the coordinates and the scaling
              scale = scaleX;
              if (scaleX > scaleY) scale = scaleY;
              newSize.width  = (int)(size.width * scale);
              newSize.height = (int)(size.height * scale);

              center = new Rectangle(0, 0, newSize.width, newSize.height);

              int width = newSize.width;
              center.x = (PANEL_SIZE - width) / 2;

              int height = newSize.height;
              center.y = (PANEL_SIZE - height) / 2;

              BufferedImage image = picture.getImage(this, center);
              System.gc();
              graphics.drawImage(image, center.x, center.y, null);
          }};

       new MediaDropTarget(picturePanel, this);
       picturePanel.setPreferredSize(new Dimension(PANEL_SIZE, PANEL_SIZE));
       return picturePanel;
   }

   /** Determine if an extension goes with a valid media type
     *
     * @param name of file to check
     * @return true if accepted, false otherwise
     */
    public boolean isMedia(File file)
    {   String extension = file.getName();
        if (file.length()>MAX_FILE_LENGTH)
        {   getErr().setText("File exceeds maximum allowable size");
            return false;
        }

        extension = extension.substring(extension.lastIndexOf(".")+1);

        if (environment.isAudio(extension)&& mediaObject instanceof SoundData)
            return true;
        if (environment.isVideo(extension)&& mediaObject instanceof MovieData)
            return true;
        if (environment.isPicture(extension)
                                    && mediaObject instanceof PictureData) 
            return true;
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
          throws IOException, InvalidObjectException,
                 UnsupportedAudioFileException
    {
        URL url = file.toURI().toURL();
        if (url==null) throw new FileNotFoundException();
        
        String extension = file.getName();
        extension = extension.substring(extension.lastIndexOf(".")+1);

        if (environment.isAudio(extension))
        {  if (!(mediaObject instanceof SoundData))
               throw new InvalidObjectException("Wrong media type");
           sound = (SoundData)mediaObject;
           sound.readFile(url);
           changed = true;
        }
        if (environment.isPicture(extension))
        {  if (!(mediaObject instanceof PictureData))
               throw new InvalidObjectException("Wrong media type");

           PictureData picture = new PictureData(url, null);
           mediaObject = picture;
           picture.getImage(picturePanel,new Rectangle(0,0,-1,-1));
           picturePanel.repaint();
           changed = true;
        }

        if (environment.isVideo(extension))
        {   if (!(mediaObject instanceof MovieData))
               throw new InvalidObjectException("Wrong media type");

            MovieData movie = (MovieData)mediaObject;
            try  
            {   movie.stop();
                movie.reset(file);
                Container player = movie.getMediaPanel();
                new MediaDropTarget(player, this);
                movie.playVideo();
                changed = true;
            }
            catch (Exception e)
            {   movie.reset();
                JOptionPane.showMessageDialog(Environment.getRootFrame(), e.toString());
            }
        }
    }       // End of mediaDropped()

    /** Get the label for displaying errors */
    protected JLabel getErr()
    {   RootDictionaryPanel rootPanel = getRootDictionaryPanel();
        return rootPanel.getErrorLabel();
    }

    /** Retrieve the root dictionary panel */
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }
} 
