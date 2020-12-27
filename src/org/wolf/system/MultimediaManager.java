/*
 * MultimediaManager.java
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

package org.wolf.system;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.acorns.data.PictureData;

/** Manage access to multimedia files */
public class MultimediaManager
{
    private static final int POSITION = 0, LENGTH = 1;

    private ArrayList<long[]> filePointers;
    private ArrayList<String> pictureList;
    private RandomAccessFile tempFile;
    private File temp;

    /** Copy multimedia objects from the temp file to the file saved output
     *
     * @param stream The stream for saving the file
     */
    public void writeMediaData(ObjectOutputStream stream) throws IOException
    {  if (tempFile==null) return;

       Object object;
       for (int i=0; i<filePointers.size(); i++)
       {  object = readObject(i);
          stream.writeObject(object);
       }
       stream.writeObject("End of File");
    }

    /** Read media objects from the stream and store in the temp file */
    public void readMediaData(ObjectInputStream stream) throws IOException
    {   makeTempFile();

        Object object;
        try
        {  while (true)
           {   object = stream.readObject();
           	   if (object instanceof PictureData)
           	   {
           		  PictureData picture = (PictureData)object;
           		  Vector<String> attributes = picture.getVector();
           		  if (attributes.size()>=3)
           		  {
           			  String path = attributes.get(2);
           			  String extension = getExtension(path);
           			  if (new File(path).exists() && isPicture(extension))
           				  pictureList.add(attributes.get(2));
           		  }
           	   }
               if (object instanceof String && object.equals("End of File"))
                    break;

               writeObject(-1, object);
           }
        }
        catch (EOFException e) {}
        catch (Exception e) 
        {  System.out.println(e);}
    }

    /** Create the temp file to manage media objects */
    private void makeTempFile() throws IOException
    {  close();

       filePointers = new ArrayList<long[]>();
       pictureList = new ArrayList<String>();
       String time = Long.toString(System.nanoTime());
       temp = File.createTempFile("wolf", time);
       temp.delete();
       
       tempFile = new RandomAccessFile(temp, "rw");
       temp.deleteOnExit();
    }

    /** Close the temporary multimedia file */
    public void close()
    {  try 
       { if (tempFile!=null) tempFile.close(); } catch (Exception e) {}

       if (temp!=null) temp.delete();
       tempFile = null;
    }

    /** Write an object to the temporary random access file
     *
     * @param object The object to write
     * @param the index number to write (-1 to append to end  of list)
     * @return the assigned index for this object
     * @throws IOException
     */
    public int writeObject(int index, Object object) throws IOException
    {  if (tempFile==null) makeTempFile();

       ByteArrayOutputStream bos = new ByteArrayOutputStream() ;

       ObjectOutput out = new ObjectOutputStream(bos);
       out.writeObject(object);
       out.close();
       byte[] serializedData = bos.toByteArray();

       if (object instanceof PictureData)
       {
    	   Vector<String> vector = ((PictureData)object).getVector();
    	   String path = vector.get(2);
    	   path = URLDecoder.decode(path, "UTF-8");
    	   pictureList.add(path);
       }

       long[] info = new long[2];
       info[POSITION] = tempFile.length();
       info[LENGTH] = serializedData.length;
       if (index<0 || index>=filePointers.size()) 
       {
    	   filePointers.add(info);
    	   index = filePointers.size()-1;
       }
       else  filePointers.set(index, info);

       tempFile.seek(info[POSITION]);
       tempFile.write(serializedData);
       
       return index;
    }

    /** Read object from the temporary file of multimedia objects
     *
     * @param index  Index into the array list of objects
     * @return The deserialized object
     */
    public Object readObject(int index)
    {   long[] pointers = filePointers.get(index);

        try
        {   tempFile.seek(pointers[POSITION]);
            byte[] bytes = new byte[(int)pointers[LENGTH]];
            tempFile.readFully(bytes);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object object = ois.readObject();
            ois.close();
            return object;
        }
        catch (Exception e) {}

        return null;
    }

    public String getPicturePath(String name)
    {
    	String extension = getExtension(name); 
    	if (!isPicture(extension)) return "";
    	
        name = name.substring(0,name.length()-3);
        String mediaName, shortName;
        try
        {
	    	for (int i=0; i<pictureList.size(); i++)
	    	{
	    		mediaName = pictureList.get(i);
	    		shortName = mediaName.substring(0,mediaName.length()-3);
	    		if (shortName.endsWith(name) && new File(mediaName).exists() ) 
	    			return new File(mediaName).toURI().toURL().toString();
	    	}
        } catch (Exception e) {}
    	return "";
    }
    
    private String getExtension(String name)
    {
    	if (name.length() >3) return name.substring(name.length()-3);
    	return "";
    }
    
    private boolean isPicture(String extension)
    {
    	if (extension.equalsIgnoreCase("jpg")) return true;
    	if (extension.equalsIgnoreCase("gif")) return true;
    	if (extension.equalsIgnoreCase("png")) return true;
    	return false;
    }
    
    /**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     * @param inputImagePath Path of the original image
     * @param outputImagePath Path to save the resized image
     * @param scaledWidth absolute width in pixels
     * @param scaledHeight absolute height in pixels
     * @throws IOException
     */
    public void resizePicture(URL inputImageURL, 
            String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {

    	// reads input image
        BufferedImage inputImage = ImageIO.read(inputImageURL);
        
        if (scaledWidth==-1)
        {
        	int imageWidth = inputImage.getWidth();
        	int imageHeight = inputImage.getHeight();
        	
        	double factor = 1. * scaledHeight / imageHeight ;
        	scaledWidth = (int)(imageWidth * factor);
        }
 
        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());
 
        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
 
        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);
 
        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
    


}   // Return MultimediaMangager class
