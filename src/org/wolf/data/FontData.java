package org.wolf.data;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipException;

import org.acorns.language.FontHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.data.makeApp.ZipWebPage;

import com.google.typography.font.sfntly.FontFactory;
import com.google.typography.font.sfntly.data.WritableFontData;
import com.google.typography.font.tools.conversion.woff.WoffWriter;

public class FontData 
{
	private ArrayList<Font> fonts;
	private ArrayList<String> fontNames;
	
	private static final String[] tags =
		{"amdx", "language", "classification"};
	
    private final static char[] HEX = "0123456789ABCDEF".toCharArray();
	
	public FontData()
	{
		fonts = new ArrayList<Font>();
		fontNames = new ArrayList<String>();
	}

	/** Constructor to get a list of fonts specified in the document
	 * 
	 * @param document The Wolf dictionary document
	 */
	public FontData(Document document)
	{
		this();
		NodeList nodes;
		Element element;
		String face, size;
		Font font;
		int len;
		
		for (int t=0; t<tags.length; t++)
		{
			nodes = document.getElementsByTagName(tags[t]);
			len = nodes.getLength();
			for (int n=0; n<len; n++)
			{
				element = (Element)nodes.item(n);
				face = element.getAttribute("face");
				if (face==null || face.length()==0) 
					continue;
				
				size = element.getAttribute("size");
				if (size==null || size.length()==0)
					size = "12";
				
				font = new Font(face, Font.PLAIN, Integer.parseInt(size));
				if (!fonts.contains(font)) 
				{
					fonts.add(font);
					fontNames.add(font.getName());
				}
			}
		}
	}

	/** Add and register a dictionary font
	 * 
	 * @param file The file object containing the font
	 */
	public void addFont(File file)
	{
		Font font = registerFont(file);
		fonts.add(font);
	}
	
	/** Get dictionary font list */
	public ArrayList<Font> getFonts() 
	{
		return fonts;
	}
	
	/** Register all of the fonts in the directory
	 * 
	 * @param directory Source directory containing the fonts
	 */
	public void registerFonts(File directory)
	{
		String[] files = directory.list();
		if (files==null) return;
		
		String file;
		
		for (int f=0; f<files.length; f++)
		{
			file = files[f];
			if (file.toLowerCase().endsWith(".ttf"))
			{
				registerFont(new File(directory, file));
			}
		}
		
		
	}
	
	/** Register a font if already not installed 
	 * 
	 * @param file File containing the font
	 * @return The derived font
	 */
	public Font registerFont(File file)
	{
		Font derivedFont = null;
		
		// Get the font family name
        try
        {
			derivedFont = Font.createFont(Font.TRUETYPE_FONT, file);
			String fontFamily = derivedFont.getFamily();
			String name = derivedFont.getName();
			
			for (int f=0; f<fonts.size(); f++)
			{
				if (fonts.get(f).getName().equals(name))
				{
					// Determine if the font already exists
					GraphicsEnvironment graphicsEnvironment 
									= GraphicsEnvironment.getLocalGraphicsEnvironment();
					String[] fonts=graphicsEnvironment.getAvailableFontFamilyNames();
			        for (int i = 0; i < fonts.length; i++) 
			        {
			            if(fonts[i].equals(fontFamily)) return derivedFont;
			        }
			        
			        // Register the custom font
		        	graphicsEnvironment.registerFont(derivedFont);
		        	break;
				}
			}
        }
        catch (Exception e) {  }
        return derivedFont;
	}
	
	  /** Read font and convert into an array of hex characters
	   *
	   *  @param File object defining the source location
	   *  @return the hex string containing the file's contents
	   */
	  public static String readFontHex(File file) 
	  					throws FileNotFoundException, IOException {
	     FileInputStream fileInputStream = null;
	     byte[] bFile = new byte[(int) file.length()];

	     //convert file into array of bytes
	     fileInputStream = new FileInputStream(file);
	     try
	     {
	    	 fileInputStream.read(bFile);
	     }
	     finally { fileInputStream.close(); }
	     return bytesToHex(bFile);
	  }
	
	  /** Convert an array of bytes to a hexadecimal string
	   * @param The array of bytes
	   * @return String containing the corresponding hexadecimal codes
	   */  
	  private static String bytesToHex(byte[] bytes) {
	     char[] hexChars = new char[bytes.length * 2];
	     for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX[v >>> 4];
	        hexChars[j * 2 + 1] = HEX[v & 0x0F];
	     }
	     
	     String s = new String(hexChars);
	     StringBuilder build = new StringBuilder();
	     int last = 0;
	     for (int i=0; i<s.length(); i+= 128) {
	        last = (i+128 < s.length()) ? i+128 : s.length();
	        build.append(s.substring(i, last) + "\n");
	     }
	     return build.toString();
	  }
	
	public void writeFont(File file)
					throws FileNotFoundException, IOException
	{
       
        String path = file.getCanonicalPath();
	    String directory = path.substring(0,path.lastIndexOf("."));

		File directoryFile = new File(directory, "Assets/Fonts");
		
		if (!path.endsWith(".acorns"))
			directoryFile.mkdirs();
		
		byte[] bytes;
		Font font;
		File fontPath;
		String fontName;
		ZipWebPage zip;
		StringBuilder fontProperties = new StringBuilder();
		for (int f=0; f<fonts.size(); f++)
		{
			font = fonts.get(f);
			fontPath = FontHandler.getHandler().getFontPath(font);
		    if (fontPath == null) continue;
		    
		    fontName = new File(directory).getName() 
    		        + "/Assets/Fonts/" + fontPath.getName();
		    
	
			// Write to mobile archive
		    if (path.endsWith(".acorns")) 
			{
			    zip = ZipWebPage.getZipWebPage(file);
				bytes = writeFont(fontPath, null);
				try
				{
					zip.addFileBytes(fontName, bytes);
				
					bytes = writeWoff(fontPath);
					fontName = fontName.substring(0, fontName.lastIndexOf('.')) + ".woff";
					zip.addFileBytes(fontName, bytes);
				}
				catch (ZipException e) 
				{
					System.out.println(e);
				} // File already there
			}
			else
			{
				// Write to html directory
				writeFont(fontPath, directoryFile);
				bytes = writeWoff(fontPath);
				
				String name = fontPath.getName();
				name = name.substring(0,  name.lastIndexOf(".")) + ".woff";
				File destination = new File(directoryFile, name);
				writeBytes(destination, bytes);
				
				fontName = fontNames.get(f);
				fontName = fontName.replaceAll(" ",  "\\\\ ");
				fontProperties.append(fontName + "=" + fontPath.getName() + "\n");
			}
		}
		
		if (!path.endsWith(".acorns"))
		{
			File destination = new File(directoryFile, "pd4fonts.properties");
			FileOutputStream stream = new FileOutputStream(destination);
			try
			{
				stream.write(fontProperties.toString().getBytes());
			}
			finally { stream.close(); }
		}
	}

	/** Write the font to disk
	 * 
	 * @param source Path to the font to write
	 * @param file The path for writing the font
	 * @param byte array of font file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
    private byte[] writeFont(File source, File file) 
			throws IOException, FileNotFoundException
	{
		FileInputStream input = new FileInputStream(source);
		byte[] buf = new byte[1024];
		int len;

		// No file means create byte array
		if (file == null)
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			while ((len=input.read(buf)) > 0)  
			{ 
				stream.write(buf, 0, len);
			}
			
			// Note: closing ByteArrayOutputStream has no effect
			input.close();  
			return stream.toByteArray();
		}
			
		// Write to output file
		file.mkdirs();
		File destination = new File(file, source.getName());
		
		FileOutputStream output = new FileOutputStream(destination);
		while ((len=input.read(buf)) > 0)  
		{ 
			output.write(buf, 0, len); 
		}
		input.close();
		output.close();
		return null;	// No byte array needed
	}
    
    /** Copy byte stream to a file 
     * 
     * @param destination Output file
     * @param bytes Data to write
     * @throws IOException
     */
    public void writeBytes(File destination, byte[] bytes)
    				throws IOException
    {
    	FileOutputStream output = new FileOutputStream(destination);
    	output.write(bytes,  0,  bytes.length);
    	output.close();
    }
 
    /** Create woff version of the font 
     * 
     * @return byte array of the font
     */
    private byte[] writeWoff(File file)
    {
        WoffWriter ww = new WoffWriter();
        FontFactory fontFactory = FontFactory.getInstance();
        byte[] bytes;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            Path path = Paths.get(file.getCanonicalPath());
            bytes = Files.readAllBytes(path);
            com.google.typography.font.sfntly.Font font = fontFactory.loadFonts(bytes)[0];
            WritableFontData wfd = ww.convert(font);

            wfd.copyTo(stream);
            // stream.close();  // Has no effect to close ByteArrayOutputStream
            
        } catch (IOException e1) { e1.printStackTrace(); }
        
        return stream.toByteArray();
    }

}	// End of FontData class
