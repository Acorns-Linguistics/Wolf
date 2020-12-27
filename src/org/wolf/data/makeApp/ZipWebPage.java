package org.wolf.data.makeApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWebPage
{
	public static ZipWebPage zipPage;
	
	/** Output stream to which to append new zip entries */
	public ZipOutputStream zipOut;

	/** Default constructor */
	public ZipWebPage()
	{
		zipPage = null;
	}

	/** Get singleton object */
	public static ZipWebPage getZipWebPage(File archive)
				throws FileNotFoundException, IOException
	{
		if (zipPage==null)
		{
			zipPage = new ZipWebPage();
		}
		
		if (zipPage.zipOut==null)
		{
			String path = archive.getCanonicalPath();
			if (path.toLowerCase().endsWith(".acorns"))
			    zipPage.open(archive);
		}	
		return zipPage;
	}
	
	/** Open a new archive
	 * 
	 * @param directory Name of the archive (without extension)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void open(File archive) 
					throws FileNotFoundException, IOException
	{
		close();  // If open archive, close it.
		
		if (archive.exists()) 
		   if (!archive.delete())
			   throw new IOException("Couldn't delete " + archive); 
		  
		FileOutputStream fos = new FileOutputStream(archive);
		zipOut = new ZipOutputStream(fos);
	}

	/** Close zip archive */
	public void close()
	{
		try { zipOut.close(); }
		catch (Exception e) {}
		
		zipOut = null;
	}

	/** Add file to the archive
	 * 
	 * @param path Source path to file to add
	 * @param zipName The name fo the file to add
	 * @throws IOException
	 */
	public void addFile(String path, String zipName) 
			throws IOException
	{
		 if (zipOut==null) return;
		 
		 byte[] buffer = new byte[1024];
		 int len;
		    
		 ZipEntry zipEntry = new ZipEntry(zipName);
         zipOut.putNextEntry(zipEntry);
            
         FileInputStream in = new FileInputStream(path);
         while ((len = in.read(buffer)) > 0) 
         {
             zipOut.write(buffer, 0, len);
         }
         in.close();
         zipOut.closeEntry();
    	
	}

	/** Add byte array as file to zip archive
	 * 
	 * @param zipName The name of the file to add
	 * @param bytes The array of the file's byte data
	 * @throws IOException
	 */
	public void addFileBytes(String zipName, byte[] bytes)
						throws IOException
	{
		if (zipOut==null) return;
		
		ZipEntry zipEntry = new ZipEntry(zipName);
        zipOut.putNextEntry(zipEntry);
        zipOut.write(bytes);
        zipOut.closeEntry();
	}
	
}	// End of ZipWebPage class
