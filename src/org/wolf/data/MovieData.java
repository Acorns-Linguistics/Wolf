/**
 * MovieData.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.wolf.data;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.wolf.lib.Icons;
import org.wolf.system.Environment;

/** This class contains the data for MPEG video */
public class MovieData 
        implements Serializable, Cloneable, WindowListener
{
    /** Java file's serial version number */
    public static final long serialVersionUID=1L;
    
    private File   file;
    private byte[] bytes;

    private static MediaJFXPanel jfxPanel;
    private static JDialog mediaPanel;
    
    /** Constructor to create a MovieData object
     *
     * @param file The File object of the video file
     * @throws FileNotFoundException
     *
     * Note: using URLs are problematic. This is because for files,
     * file.toURI().toURL() doesn't work on windows systems. This is
     * because of the %20 characters which are valid files for Windows.
     *
     * file.toURL() works, but will it work on all systems? This is something
     * to check out in the future, especially if we want to create applets
     * that run on the Web.
     *
     * Copying files works because the Java File class handles the system
     * incompatibilities.
     */
    public MovieData(File file) throws FileNotFoundException
    {  reset(file);   }

    /** Constructor to initialize the object without a video */
    public MovieData()
    { file = null; }

    /** Read the video into memory for saving as part of the dictionary */
    private void readURL(URL url) throws IOException
    {   URLConnection connect = url.openConnection();
        connect.connect();

        InputStream in = connect.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final int BUF_SIZE = 1 << 8;
        byte[] buffer = new byte[BUF_SIZE];
        int bytesRead = -1;
        while((bytesRead = in.read(buffer)) > -1)
        { out.write(buffer, 0, bytesRead);  }
        in.close();
        bytes = out.toByteArray();
        out.close();
    }


    /** Write the video to a temp file (JMF likes it this way) */
    public void writeFile(File file) throws IOException
    {  BufferedOutputStream out
               = new BufferedOutputStream(new FileOutputStream(file));
       out.write(bytes);
       out.close();
    }

    /** Method to determine if the dialog panel is active */
    public boolean isVisible()
    {   return mediaPanel!=null && mediaPanel.isVisible();  }
    
    /** Method to alter the MediaPlayer URL
     *
     * @param file The video file object
     */
    public void reset(File file)
    { this.file = file;
      
      try {  readURL(file.toURI().toURL()); }
      catch(Exception e) {}
    }

    public void reset() { file = null; }

    /** Get the panel to hold the media */
    public JDialog getMediaPanel()
    {  
       if (mediaPanel==null)
       {  mediaPanel = new JDialog();
          mediaPanel.addWindowListener(this);
          
          String title = "Play movie ";
          if (file!=null) title += file.getName();
          mediaPanel.setTitle(title);
          
          Image imageIcon = Icons.getImageIcon("wolf.png", 30).getImage();
          mediaPanel.setIconImage(imageIcon);
       }
       return mediaPanel;
    }

    /** Get panel ready to play the video */
    public JDialog playVideo()
                      throws FileNotFoundException
    {  
       jfxPanel = new MediaJFXPanel(file);
       JDialog mediaPanel = getMediaPanel();
 
       Container container = mediaPanel.getContentPane();
       container.removeAll();
       container.add(jfxPanel);
       container.setPreferredSize(new Dimension(896,672));
       mediaPanel.pack();
       Point point = Environment.getRootFrame().getLocation();
       mediaPanel.setLocation(point);
       mediaPanel.setVisible(true);
       return mediaPanel;
    }

     /** Get the File for this movie object
     * 
     * @return FileL object
     */
    public File getFile()
    { return file; }

    /** Get the file name without the path */
    public String getName()  {  return file.getName();  }
    
    /** Method to stop the playback of a movie clip */
    public void stop()
    { 
    	if (jfxPanel!=null) 
    		jfxPanel.stopOperation();
    }

    /** Make an identical copy of this object 
     * 
     * @return The cloned Link object
     */
    public @Override MovieData clone()
    {   try
       {   MovieData newObject = (MovieData)super.clone();
           return (MovieData)newObject; 
       }
       catch (Exception e) 
       { 
    	      Frame root = JOptionPane.getRootFrame();
    	      JOptionPane.showMessageDialog
                 (root, "Could not clone MovieData object"); }
       return null;
    }

    /** Unused window event method */
    public void windowActivated(WindowEvent e) {}
    /** Unused window event method */
    public void windowClosed(WindowEvent e) {}
    /** Stop any video playback when the window closes */
    public void windowClosing(WindowEvent e)  
    { 
    	stop();
    }
    /** Unused window event method */
    public void windowDeactivated(WindowEvent e) {}
    /** Unused window event method */
    public void windowDeiconified(WindowEvent e) {}
    /** Unused window event method */
    public void windowIconified(WindowEvent e) {}
    /** Unused window event method */
    public void windowOpened(WindowEvent e) {}
    
	/** Create JFXPanel to play and control the video */
	private class MediaJFXPanel extends JFXPanel
	{
		private static final long serialVersionUID = 1L;
	    
	    private MediaPlayer  player;
	    private MediaView    view;
	    private MediaControl control;

	    /** Constructor: 
	     *    Instantiate panel with Media Player and controls
	     *    
	     *   @param file The video file   
	     */    
		public MediaJFXPanel(final File file) throws FileNotFoundException, MediaException
		{
	        Platform.runLater(new Runnable() 
	        {
	            @Override
	            public void run() 
	            {
            	   Scene scene = createScene(file);
	               setScene(scene);
	            }
	       });
		}
		
		private void stopOperation()
		{
			if (player!=null) player.stop();
		}
	
		/** Create the scene for the JFXPanel
		 * 
		 * @param file The video file to display
		 * @return The created scene
		 */
	    private Scene createScene(File file) 
	    {
	        Group  root  =  new  Group();
	        Scene  scene  =  new  Scene(root, Color.ALICEBLUE); 
	        scene.widthProperty().addListener(new ChangeListener<Number>() 
	        {
	            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) 
	            {
	           		if (player!= null) player.pause();
	            }
	
		    });
	        
	        scene.heightProperty().addListener(new ChangeListener<Number>() 
	        {
	            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) 
	            {	
	           		if (player!= null) player.pause();
	            }
	        });
	        
	        // create media player
	        String fileUri = file.toURI().toString();
	        
	        Media media;
	        try
	        {
		        media = new Media (fileUri);
		        player = new MediaPlayer(media);
	        }
	        catch (MediaException e) 
	        {
	        	Label label = new Label(e.toString());
	        	label.setAlignment(Pos.CENTER);
	        	label.setFont(new Font("Verdana", 12));
	        	label.setTextFill(Color.RED);
	        	scene.setRoot(label);
	        	return scene;
	        }

	        player.setAutoPlay(false);
	        //player.setMute(true);
	    	
	        view = new MediaView(player);
	        view.setFitWidth(800);
	        view.setFitHeight(600);

	        control = new MediaControl(player);
	    	control.setCenter(view);
	    	scene.setRoot(control);
	    	
	    	player.setOnPaused(new Runnable() {

				@Override
				public void run() 
				{
					player.setStopTime(player.getMedia().getDuration());
				} });
	        
	        return (scene);
	    }
	    
	} // End MediaJFXPanel class

}   // End of MovieData class
