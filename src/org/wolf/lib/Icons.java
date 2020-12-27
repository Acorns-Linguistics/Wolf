/*
 * Icons.java
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

package org.wolf.lib;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.wolf.system.Environment;

/** Read icons from disk */
public class Icons
{    public static final int ICON_SIZE = 30;

    /** Method to retrieve an acorn for display
     *
     * @param iconName the name of the icon to load
     * @param size icon size (use default if size = 0, if <0, don't scale)
     */
     public static ImageIcon getImageIcon(String iconName, int size)
     {  iconName = "/resources/" + iconName;
        ImageIcon image = null;

        if (size==0) size = ICON_SIZE;
        try
        {  URL url = Icons.class.getResource(iconName);
           Image newImage  = Toolkit.getDefaultToolkit().getImage(url);

           if (size>0)  newImage = newImage.getScaledInstance
                                            (size, size, Image.SCALE_REPLICATE);
           image = new ImageIcon(newImage);
        }
        catch (Exception e)
        {  JOptionPane.showMessageDialog(Environment.getRootFrame(), e);
           System.exit(1);
        }
        return image;
     }
}       // End of Icoons class
