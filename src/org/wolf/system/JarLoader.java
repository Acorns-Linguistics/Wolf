/*
 * JarLoader.java
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.NoSuchElementException;
import java.util.Vector;

/** Load jars into the classloader from the plugins directory */
public class JarLoader
{
   /** Method to add jar files to the system loader
    * 
    * @param codebase The root folder containing the jar files
    */
   public JarLoader(URL codebase)  
   {
      // Find the jars in the base directory
      Vector<String> types = new Vector<String>();
      Vector<URL> jars = findJars(codebase, types);
      if (jars==null)
      { throw new NoSuchElementException("Couldn't find any jars"); }
  
   }	   // End of JarLoader.
	
	/** Do a directory search for all the jar files 
  * 
  *  @param urlBase the URL to the folder containing the codebase
  *  @param types Vector of jars already loaded (if null, return all jars)
  *  @return A vector of the jars found in the codebase folder (or null if none)
  */
   private Vector<URL>findJars(URL urlBase, Vector<String> types)
   {  Vector<URL> urls = new Vector<URL>();
      
      try
      {  
    	 URL url = new URL(urlBase + "jars");
         URLConnection connection = url.openConnection();
         InputStream stream = connection.getInputStream();
         BufferedReader in = new BufferedReader(new InputStreamReader(stream));
         String[] fileName;
         String line;
         URL jarURL;
         Boolean addJar = false;
      
         while ( (line = in.readLine()) != null)
         {  addJar = true;
            fileName = line.split(";");
            if (fileName.length>0 && fileName[0].endsWith(".jar"))
            {   // If class already loaded, don't attach the jar.
                if (fileName.length>1)                   
                {  if (fileName[1].equals(".")) 
                   {  if (types!=null) addJar = false;   }
                   else
                   {   try
                       { types.add(fileName[1]);  }
                       catch (Throwable t)  {}
                   }
                }
                else if (types==null) addJar = false;
                if (addJar)
                {  jarURL = new URL(urlBase + fileName[0]);
                   urls.add(jarURL);
                }
            }
         }
         in.close();
         return urls;
      }
      catch (Exception e)  {  return null; }
   }		
}  // End of JarLoader