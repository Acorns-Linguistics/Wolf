package org.wolf.data;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.makeApp.MakeWebApp;
import org.wolf.system.MultimediaManager;
import org.zefer.pd4ml.PD4Constants;
import org.zefer.pd4ml.PD4ML;
import org.zefer.pd4ml.PD4PageMark;

public class FormatData implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public  static final String WORD = "word";
	public  static final String DEFINITION = "definition";
	private static final String EXAMPLE = "example";
	private static final String COMMENT = "comment";
	private static final String TEMPLATE = "template";
	
	private static final int FIELD_SIZE =    150;
	
	public static final String[] WORD_HEADERS = 
	{
		"phonetic", "format", "picture", "languages", "separator"
	};
	
	public static final int W_PHONETICS      = 0;
	public static final int W_FORMAT         = 1;
	public static final int W_SHOW_PICTURE   = 2;
	public static final int W_SHOW_LANGUAGES = 3;
	public static final int W_SEPARATOR		 = 4;
	
	public static final String[] CATEGORY_HEADERS = 
	{
		"count", "rows", "title", "format", "separator", "position", "picture",
	};
	
	public final static int C_COUNT        = 0;
	public final static int C_ROWS         = 1;
	public final static int C_TITLE        = 2;
	public final static int C_FORMAT       = 3;
	public final static int C_SEPARATOR    = 4;
	public final static int C_UNUSED    = 5;
	public final static int C_SHOW_PICTURE = 6;
	
	public static final String[] FIELD_HEADERS = 
	{
		"field", "title", "format", "separator", "position"
	};
	
	public final static int F_FIELD     = 0;
	public final static int F_TITLE     = 1;
	public final static int F_FORMAT    = 2;
	public final static int F_SEPARATOR = 3;
	public final static int F_POSITION  = 4;
	
	public static final String FIELD = "fields";
	
	public static enum t 
	{
		W_HDR, W_FLD, D_HDR, D_FLD, E_HDR, E_FLD, C_HDR, C_FLD
	};
	
	public final static String DEFAULT = "default";
	
	public final static String[][][] DEFAULT_TEMPLATE =
	{
		{ {"false", "none", "false", "false", ","} }, // W_HDR
		{ {"ontology", "", "normal", "()", ""}, },  // W_FLD
		{ {"None", "false", "", "normal", "none", "first", "false"} },  // D_HDR
		{  },  // D_FLD
		{  },  // E_HDR
		{  },  // E_FLD
		{  },  // C_HDR
		{  },  // C_FLD
	};
	
	public final static int TRANSLATIONS = 0;
	public final static int COLUMNS      = 1;
	public final static int ROWS         = 2;
	public final static int MEDIA        = 3;
	public final static int LIST_SIZE    = 4;

	public final static String HTML_HEADER =
		"<!DOCTYPE html>\n"
		+ "<!-- saved from url=(0016)http://localhost -->\n"
		+ "<html>\n" 
	    + "<head>\n"
	    + "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n"
   	    + "<meta charset=\"utf-8\" />\n"
		+ "<meta name=\"viewport\" id=\"viewport\" content=\"width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1, user-scalable=no\" />\n"
		;
	
	/** Instance and static variables for this class */
	
	HashMap<String, String[][][]> templates;
	String directory, directoryPath;
	
	static JComboBox<String> selectionCombo = null;
	static DefaultComboBoxModel<String> model = null;
	static String[][][] template = null;

	/** Start of methods for this class */
	
	public FormatData()
	{
		templates = new HashMap<String, String[][][]>();
		directory = null;
	}
	
	public void setDirectory(File file) throws IOException
	{
	    directory = directoryPath =  null;
		if (file != null)
		{
			directoryPath = file.getCanonicalPath();
			int index = directoryPath.lastIndexOf(".");
			if (index>0)
				directoryPath = directoryPath.substring(0, index);
			
			directory = new File(directoryPath).getName();
		}
	}
	
	/** Get the data associated with a particular template */
	public String[][][] getTemplate(String templateKey)
	{
		template = templates.get(templateKey);
		return template;
	}

	/** Get selected dictionary formatting template
	 * 
	 * @return The template (or null if not found or selected)
	 */
	public String[][][] getSelectedTemplate()
	{
		String selection = (String)(getTemplateCombo().getSelectedItem());
		if (selection==null) return null;
		return getTemplate(selection);
	}
	
	public void setTemplate(String template, String[][][] values)
	{
		values = values.clone();
		if (templates.get(template) == null)
		{
			model.addElement(template);
		}
		templates.put(template, values);
	}
	
	/** Remove a template from the list */
	public boolean removeTemplate(String template)
	{
		if (template.equals(DEFAULT)) return false;
		
		model.removeElement(template);
		templates.remove(template);
		return true;
	}
	
	/** Get the list of available templates */
	public ArrayList<String> getTemplateList()
	{
		ArrayList<String> keys = new ArrayList<String>();
		
		if (templates.get(DEFAULT) == null)
			templates.put(DEFAULT, DEFAULT_TEMPLATE);

		for(Map.Entry<String,String[][][]> map : templates.entrySet())
		{
			keys.add(map.getKey());
		}
		return keys;
	}
	
	public JComboBox<String> getTemplateCombo()
	{
		if (selectionCombo != null) return selectionCombo;
		
		Vector<String> keys = new Vector<String>(getTemplateList());
		model = new DefaultComboBoxModel<String>(keys);
		selectionCombo = new JComboBox<String>(model);
		
		Color optionColor = new Color(220, 220, 220);
	    selectionCombo.setBackground(optionColor);
	    selectionCombo.setEditable(false);
	      
	    Dimension size = new Dimension(FIELD_SIZE, 20);
	    selectionCombo.setPreferredSize(size);
	    selectionCombo.setSize(size);
	    selectionCombo.setMinimumSize(size);
	    selectionCombo.setMaximumSize(size);
	    
	    selectionCombo.setToolTipText("Select output format for dictionary");
		return selectionCombo;
	}

	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		ArrayList<String> keys = getTemplateList();
		String template[][][], key;
		
		build.append("[");
		for (int k=0; k<keys.size(); k++)
		{
			if (k>0) build.append(", ");
			build.append("{");
			
			key = keys.get(k);
			addField(build, TEMPLATE, key);
			build.append(", ");

			template = getTemplate(key);
						
			// Create JSON for words
			addCategory(build, WORD
					, WORD_HEADERS, FIELD_HEADERS
					, template[t.W_HDR.ordinal()][0]
					, template[t.W_FLD.ordinal()]);
			
			if (template[t.D_HDR.ordinal()].length != 0)
			{
				build.append(", ");
				
				// Create JSON for definitions
				addCategory(build, DEFINITION
						, CATEGORY_HEADERS, FIELD_HEADERS
						, template[t.D_HDR.ordinal()][0]
						, template[t.D_FLD.ordinal()]);
			}
			
			if (template[t.E_HDR.ordinal()].length != 0)
			{
				build.append(", ");
	
				// Create JSON for examples
				addCategory(build, EXAMPLE
						, CATEGORY_HEADERS, FIELD_HEADERS
						, template[t.E_HDR.ordinal()][0]
						, template[t.E_FLD.ordinal()]);
			}
			
			if (template[t.C_HDR.ordinal()].length != 0)
			{
				build.append(", ");
	
				// Create JSON for comments
				addCategory(build, COMMENT
						, CATEGORY_HEADERS, FIELD_HEADERS
						, template[t.C_HDR.ordinal()][0]
						, template[t.C_FLD.ordinal()]);
			}
			
			build.append("}");
			
		}	// End for.
		build.append("]");
		return build.toString();
	}
	
	/** Create a JSON field from key/value pair */
	private void addField(StringBuilder build, String key, String value)
	{
		addString(build, key);
		build.append(": ");
		value = value.replaceAll("\\\\n", "<br>");
		value = value.replaceAll("\\\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		value = value.replace("\\s", "&nbsp;");
		value = value.replaceAll("\\s+", " ");
		value = value.replace("\"", "\\\\\"");
		addString(build, value);
	}

	/** Create JSON for words, definitions, examples, and comments */
	private void addCategory(StringBuilder build, String category, String[] header, String[] fields, String[] hdrValue, String[][] fldValue )
	{
		
		addString(build, category);
		build.append(": { ");
		boolean comma = false;
		
		for (int h=0; h<header.length; h++)
		{
			if (hdrValue != null && hdrValue[h].length()>0)
			{
				if (comma) build.append(", ");
				addField(build, header[h], hdrValue[h]);
				comma = true;
			}
		}
		
		// Process field section if any exist
		boolean notFirst = false;
		if (fldValue.length>0)
		{
			for (int f=0; f<fldValue.length; f++)
			{
				if (fldValue[f][0] == null || fldValue[f][0].length() == 0)
					continue; // field type required
				
				if (notFirst) build.append(", ");
				else
				{
					build.append(", ");
					addString(build, FIELD);
					build.append(": [");
				}
				notFirst = true;
				
				build.append(" { ");
				comma = false;
				String field;
				for (int i=0; i<fldValue[f].length; i++)
				{
					if (fldValue[f][i]!=null && fldValue[f][i].length()>0)
					{
						if (comma) build.append(", ");
						
						field = fldValue[f][i];
						if (i==3)
						{
							field = field.replace("'", "\\'");
							field = field.replace("\"", "\\\\\"");
						}
						addField(build, fields[i], field);
						comma = true;
					}
				}
				build.append("}");
			}
		}
		if (notFirst) build.append(" ]");
		build.append(" }");
	}
	
	/** Add a JSON key or value element */
	private void addString(StringBuilder build, String str)
	{
		build.append("\"");
		build.append(str);
		build.append("\"");
	}

	private final static int LANGUAGE = 0;
	private final static int FACE     = 1;
	private final static int SIZE     = 2;
	private final static int FONT_LEN = 3;
	
	private final static int PHONETIC = 0;
	private final static int GLOSS    = 1;
	private final static int ACTIVE   = 2;
	
	/** Format the dictionary in accordance to the selected template
	 * 
	 * @param document The DOM object containing dictionary information
	 * @param gloss The gloss language code
	 * @param active The active languages for cross reference
	 * @return The HTML document to display the dictionary or null if no template selected
	 */
	public String toHTML(Document document, String gloss, String[] active)
	{
		String[][][] template = getSelectedTemplate();
		return toHTML(document, gloss, active, template);
	}
	
	/** Format the dictionary in RTF accordance to the selected template
	 * 
	 * @param document The DOM object containing dictionary information
	 * @param gloss The gloss language code
	 * @param active The active languages for cross reference
	 * @return The HTML document to display the dictionary or null if no template selected
	 */
	public String toRTF(Document document, String gloss, String[] active)
						throws IOException
	{
		String htmlStr = toHTML(document, gloss, active);
       	byte[] rtfBytes = makeRTForPDF(htmlStr, PD4Constants.RTF_WMF );
       	return new String(rtfBytes);
	}
	
	/** Format the dictionary in PDF accordance to the selected template
	 * 
	 * @param document The DOM object containing dictionary information
	 * @param gloss The gloss language code
	 * @param active The active languages for cross reference
	 * @return The HTML document to display the dictionary or null if no template selected
	 */
	public byte[] toPDF(Document document, String gloss, String[] active)
						throws IOException
	{
		String htmlStr = toHTML(document, gloss, active);
       	byte[] rtfBytes = makeRTForPDF(htmlStr, PD4Constants.PDF );
       	return rtfBytes;
	}
	
	
	/** Format the dictionary in HTML accordance to the selected template
	 * 
	 * @param document The DOM object containing dictionary information
	 * @param gloss The gloss language code
	 * @param active The active languages for cross reference
	 * @param template The selected template to use
	 * @return The HTML document to display the dictionary or null if no template selected
	 */
	public String toHTML(Document document, String gloss, String[] active, String[][][] template)
	{
		if (gloss==null || gloss.length()==0)
			throw new IllegalArgumentException("No language is selected");
		
		if (template == null) 
			throw new IllegalArgumentException("No formatting templates found");
		
		if (active==null) active = new String[0];
		
		int len = active.length + ACTIVE;
		String[][] languages = new String[FONT_LEN][len];
		
		// Set the language codes to format
		languages[LANGUAGE][PHONETIC] = gloss;
		languages[LANGUAGE][GLOSS] = gloss;
		System.arraycopy(active, 0, languages[LANGUAGE], ACTIVE, len-ACTIVE);
		
		// Set the font faces and sizes
		Element root = document.getDocumentElement();
		languages[FACE][PHONETIC] = root.getAttribute("face");
		languages[SIZE][PHONETIC] = root.getAttribute("size");
		
		NodeList nodes = document.getElementsByTagName("language");
		int langs = nodes.getLength();
		Element element, glossLanguage = null;
		String lang, langName = "";
		for (int n=0; n<langs; n++)
		{
			element = (Element)nodes.item(n);
			lang = element.getAttribute("lang");
			
			if (lang.equalsIgnoreCase(gloss))
			{
				languages[FACE][GLOSS] = element.getAttribute("face");
				languages[SIZE][GLOSS] = element.getAttribute("size");
				langName = element.getAttribute("name");
				glossLanguage = element;
			}
			else
			{
				for (int a=0; a<active.length; a++)
				{
					if (lang.equalsIgnoreCase(active[a]))
					{
						languages[FACE][ACTIVE+a] = element.getAttribute("face");
						languages[SIZE][ACTIVE+a] = element.getAttribute("size");
					}
				}
			}
		}
		
		if (glossLanguage == null) return null;
		
		nodes = glossLanguage.getElementsByTagName("word");
		int words = nodes.getLength();
		Element word;
		StringBuilder build = new StringBuilder();
		build.append(HTML_HEADER);
		build.append("<title>" + langName + " (" + gloss + ") "  
				+ "dictionary" + "</title>\n");

		// Embed the fonts if creating a web page
		if (directory!= null)
		{
			build.append("<style type = \"text/css\">\n");
			ArrayList<Font> fonts = new FontData(document).getFonts();
			build.append(MakeWebApp.createFontFace(directory, fonts));
			build.append("</style>\n</head>\n<body>\n");
		}
		
		for (int w=0; w<words; w++)
		{
			word = (Element)nodes.item(w);
			build.append("<div style=\"clear:both;\">");
			styleWord(build, w, word, template, languages);
			build.append("</div>");
		}
		
		build.append("</body>\n</html>\n");
		return build.toString();
	}
	
	   /** Create rtf or pdf file using the PD4ML library
	    *  @param htmlString String containing the HTML data
	    *  @param type PD4Constants.RTF_WMF or PD4Constants.PDF
	    *  @return byte array containing the RTF or PDF data
	    */   
	   private byte[] makeRTForPDF(String htmlString, String type)
	   				throws FileNotFoundException, IOException, MalformedURLException
	   {

 	      PD4ML pd4ml = new PD4ML();
	      pd4ml.outputFormat(type);
		  pd4ml.setPageInsets(new Insets(20, 10, 10, 10));
		  pd4ml.setHtmlWidth(950);
	      pd4ml.setDefaultTTFs("Times New Roman", "Arial", "Courier New");  
		  //pd4ml.enableDebugInfo();
		  //pd4ml.setPageSize(pd4ml.changePageOrientation(PD4Constants.A4)); // landscape page orientation
		  pd4ml.setPageSize(PD4Constants.A4);
		  
		  PD4PageMark footer = new PD4PageMark();  
		  footer.setPageNumberTemplate("-- $[page] --");  
		  footer.setTitleAlignment(PD4PageMark.LEFT_ALIGN);  
		  footer.setPageNumberAlignment(PD4PageMark.CENTER_ALIGN);  
		  footer.setColor(new Color(0x008000));  
		  footer.setInitialPageNumber(1);  
		  footer.setPagesToSkip(1);  
		  footer.setFontSize(14);  
		  footer.setAreaHeight(18);     
		  pd4ml.setPageFooter(footer);  
		  
	      if (type.equals(PD4Constants.RTF_WMF)) {
	          htmlString = htmlString.replaceAll("<sub>","#SUB#");
		      htmlString = htmlString.replaceAll("</sub>","#XSUB#");
	      }
	      
	      StringReader reader = new StringReader(htmlString);
	      ByteArrayOutputStream stream = new ByteArrayOutputStream();
          String sep = File.separator;
          String fonts = directoryPath + sep + "Assets" + sep + "Fonts";
	      File file = new File(fonts);
	      fonts = file.getCanonicalPath();
		      
		  if ( fonts != null && fonts.length() > 0 ) {
				pd4ml.useTTF( fonts, true );
		  }
	      
	      pd4ml.render(reader, stream, new URL("file:" + directoryPath), "UTF-8");

	      if (type.equals(PD4Constants.RTF_WMF)) {
	         String rtf = new String(stream.toByteArray(), "UTF-8");

	         rtf = rtf.replaceAll("\\\\par", "\\\\pard\\\\par");
	         rtf = rtf.replaceAll("#SUB#", "{\\\\sub ");
		     rtf = rtf.replaceAll("#XSUB#", "}");
	         rtf = embedFonts(fonts, rtf);
	         return rtf.getBytes();
	      }
	      
	      return stream.toByteArray();
	  }

	  /** Insert embedded fonts into the rtf data
	   *
	   *  @param fonts path to the directory containing ttf fonts
	   *  @param rtfData string containing PD4ML rtf data
	   *  @return string with embedded fonts inserted into rtf data
	   */
	  private String embedFonts(String fonts, String rtfData)
	                throws FileNotFoundException, IOException {
	      
		  
		  String path = fonts + File.separator + "pd4fonts.properties";

	      String data = readFile(path, StandardCharsets.UTF_8);
	      BufferedReader rdr = new BufferedReader(new StringReader(data));
	      String[] fontData;
	 
	      // Only manipulate front of string for speed
	      //   StringBuilder is mutable, strings are not     
	      StringBuilder build = new StringBuilder();
	      int index = rtfData.indexOf("\\pard");
	      if (index<0) return rtfData;
	      
	      build.append(rtfData.substring(0,index));
	      String back = rtfData.substring(index);
	      
	      String font, fontPath;
	      int fontIndex;
	      for (String line = rdr.readLine(); line != null; line = rdr.readLine()) 
	      {
	         if (line.charAt(0) == '#') continue;
	         
	         fontData = line.split("=");
	         if (fontData.length!=2) continue;
	         
	         fontData[0] = fontData[0].replaceAll("\\\\","");
	         fontIndex = build.indexOf(fontData[0]);
	         if (fontIndex>0) {
	            fontPath = fonts + File.separator + fontData[1];
	            font = makeFont(new File(fontPath));
	            if (font.length()>0) {
	               build.insert(fontIndex, font);
	            }
	         }
	      }
	      rdr.close();
	      return build.toString() + back;
	  }
	  
	  /** Create font data for an embedded font
	   *  @param file File object addressing a ttf file
	   *  @return string containing ttf embedded data
	   */  
	  private String makeFont(File file) 
	  		throws FileNotFoundException, IOException {
	     if (!file.exists()) return "";
	     
	     StringBuilder build = new StringBuilder();
	     build.append("{\\*\\fontemb\\ftruetype\n");

	     build.append(FontData.readFontHex(file));
	     build.append("} ");
	     return build.toString();
	  }
	  
	  /** Read text file with specified encoding
	   *
	   * @param path The absolute path to the file
	   * @param encoding Encoding (i.e. UTF-8) 
	   * @return string containing the file's contents
	   */  
	  String readFile(String path, Charset encoding) 
	        throws IOException  {
	      byte[] data = Files.readAllBytes(Paths.get(path));
	      return new String(data, encoding);
	  }
	  
	/** Format a word and its fields
	 * 
	 * @param build The stringBuilder object
	 * @param index The word index into the dictionary
	 * @param word The word to format
	 * @param languages The list of active words
	 */
	private void styleWord(StringBuilder build, int index, Element word, String[][][] template, String[][] languages)
	{
		// Get the columns, rows, and translations elements
		Element[] elementLists = getElementLists(word);
		
		// style the word header
		Element translations = elementLists[TRANSLATIONS];
		Node node = translations.getFirstChild();
		String wordText = (node==null) ? "" : node.getNodeValue();
		if (wordText.matches(".*_[0-9]+"))
		{
			int lastIndex = wordText.lastIndexOf('_');
			wordText = wordText.substring(0, lastIndex) + "<sub>" + wordText.substring(lastIndex+1) + "</sub>";
		}
		String phonetic = "";
		if (template[t.W_HDR.ordinal()][0][W_PHONETICS].equalsIgnoreCase("true"))
		{
			phonetic = translations.getAttribute("phonetics");
		}
		
		String format = template[t.W_HDR.ordinal()][0][W_FORMAT];
		String showPicture = template[t.W_HDR.ordinal()][0][W_SHOW_PICTURE];
		boolean show = showPicture != null && showPicture.equals("true"); 
		
		if (index>0) build.append("<br>\n");
		styleString(build, wordText, -1, "none", 
				" ", languages[FACE][GLOSS],
				languages[SIZE][GLOSS], format );
		
		if (phonetic.length()>0)
		{
			styleString(build, phonetic, -1, "none", 
					" ", languages[FACE][PHONETIC],
					languages[SIZE][PHONETIC], "italic" );
			build.append(" ");
		}
		
		styleTranslations(build, elementLists[TRANSLATIONS], languages, "()", true);
		styleFields(build, elementLists[COLUMNS], template[t.W_FLD.ordinal()], languages, show, true);
		if (show) stylePicture(build, elementLists[MEDIA]);
		styleRows(build, elementLists[ROWS], template, languages);
	}

	/** Method to style the data in word or definition rows
	 * 
	 * @param build The StringBuilder object
	 * @param columns The <columns> element
	 * @param template the formatting information for each column's field type
	 * @param languages the formatting information for each column's field type
	 */
	private void styleRows(StringBuilder build, Element rows, String[][][] template, String[][] languages)
	{
		if (rows==null) return; 
		
		NodeList nodes = rows.getChildNodes();
		ArrayList<Element> definitions = new ArrayList<Element>();
		ArrayList<Element> examples = new ArrayList<Element>();
		ArrayList<Element> comments = new ArrayList<Element>();
		
		Element element;
		for (int n=0; n<nodes.getLength(); n++)
		{
			element = (Element)nodes.item(n);
			switch (element.getTagName())
			{
				case "definition":
					definitions.add(element);
					break;
				case "example":
					examples.add(element);
					break;
				case "classification":
					comments.add(element);
					break;
			}
		}
		
		styleDefinitions(build, definitions, template, languages);
		
		if (template[t.E_HDR.ordinal()].length != 0)
			styleCategory(build, examples, template[t.E_HDR.ordinal()][0], languages);
		if (template[t.C_HDR.ordinal()].length != 0)
			styleCategory(build, comments, template[t.C_HDR.ordinal()][0], languages);
	}
	
	/** Format a definition, its fields, examples, and comments
	 * 
	 * @param build The stringBuilder object
	 * @param definitions The list of definitions to format
	 * @param template The formatting template for the comment
	 * @param languages The list of active words
	 */
	private void styleDefinitions(StringBuilder build, ArrayList<Element> definitions, String[][][] template, String[][] languages)
	{
		Element definition;
		String face, size, text; 
		Node node;
		
		// Index where definition should appear among the column output
		if (template[t.D_HDR.ordinal()].length == 0) return;
		
		String order = template[t.D_HDR.ordinal()][0][C_COUNT];
		String rows = template[t.D_HDR.ordinal()][0][C_ROWS];
		String title = template[t.D_HDR.ordinal()][0][C_TITLE];
		title = title.replaceAll("\\\\n", "<br>\n");
		title = title.replaceAll("\\\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		title = title.replace("\\s", "&nbsp;");
		String format = template[t.D_HDR.ordinal()][0][C_FORMAT];
		String separator = template[t.D_HDR.ordinal()][0][C_SEPARATOR];
		String showPicture = template[t.D_HDR.ordinal()][0][C_SHOW_PICTURE];
		boolean show = showPicture != null && showPicture.equals("true"); 
		
		for (int i=0; i<definitions.size(); i++)
		{
			definition = definitions.get(i);
			if (title.length()>0 && i==0) build.append(title);
			if (rows.equalsIgnoreCase("true")) build.append("<br>\n");
			else build.append(" ");
			
			Element[] elementLists = getElementLists(definition);
			
			Element translations = elementLists[TRANSLATIONS];
			
			// Style the definition text
			face = languages[FACE][GLOSS];
			size = languages[SIZE][GLOSS];
			styleFields(build, elementLists[COLUMNS], template[t.D_FLD.ordinal()], languages, show, false);

			node = translations.getFirstChild();
			styleString(build, "", i+1, order, separator, null,
					null, "normal" );

			styleTranslations(build, elementLists[TRANSLATIONS], languages, separator, true);

			if (node != null)
			{
				text = node.getNodeValue();
				styleString(build, text, i+1, "none", separator, face,
						size, format );
			}
			styleFields(build, elementLists[COLUMNS], template[t.D_FLD.ordinal()], languages, show, true);
			if (show) stylePicture(build, elementLists[MEDIA]);
			styleRows(build, elementLists[ROWS], template, languages);
		}
	}
	
	/** Format a list of comments or examples associated with a word or definition
	 * 
	 * @param build The stringBuilder object
	 * @param categories The list of comments to format
	 * @param template The formatting template for the comment
	 * @param languages The list of active words
	 */
	private void styleCategory(StringBuilder build, ArrayList<Element> categories, String[] template, String[][] languages)
	{
		Element element;
		Element[] list;
		Node node;
		String face, size, text; 
		
		String count = template[C_COUNT];
		String rows = template[C_ROWS];
		String title = template[C_TITLE];
		title = title.replaceAll("\\\\n", "<br>\n");
		String format = template[C_FORMAT];
		String separator = template[C_SEPARATOR];
		StringBuilder temp;
		String show = template[C_SHOW_PICTURE];
		
		for (int i=0; i<categories.size(); i++)
		{
			element = categories.get(i);
			face = element.getAttribute("face");
			size = element.getAttribute("size");
			node = element.getFirstChild();
			text = (node==null) ? "" : node.getNodeValue();
			StringBuilder image = new StringBuilder();
			if (element.getTagName().equalsIgnoreCase("classification"))
			{
				face = languages[GLOSS][FACE];
				size = languages[GLOSS][SIZE];
			}
			else if (element.getTagName().equalsIgnoreCase("example"))
			{
				temp = new StringBuilder();
				face = languages[FACE][GLOSS];
				size = languages[SIZE][GLOSS];
				list = getElementLists(element);
				if (list[TRANSLATIONS] == null) continue;
				
				element = list[TRANSLATIONS];
				text = "";
				node = element.getFirstChild();
				if (node!=null) text = node.getNodeValue();
				styleTranslations(temp, element, languages, "none", false);

				if (text != null && text.length()>0) 
				{
					styleString(temp, text, -1, "", "none", face, size, format );
				}

				if (show != null && show.equals("true"))
				{
					stylePicture(image, list[MEDIA]);
				}
				text =  temp.toString();
			}
			
			if (title.length()>0 && i==0) build.append(title);
			if (rows.equalsIgnoreCase("true")) build.append("<br>\n");
			
			styleString(build, text, i+1, count, separator, face,
					size, format );
			
			String imageTag = image.toString();
			if (imageTag.length()>0)
				build.append(imageTag);
		}
	}
	
	/** Method to style the data in word or definition columns
	 * 
	 * @param build The StringBuilder object
	 * @param columns The <columns> element
	 * @param template the formatting information for each column's field type
	 * @param languages the formatting information for each column's field type
	 * @param show display picture if it exists
	 * @param processed true if before Word or Definition processed
	 */
	private void styleFields(StringBuilder build, Element columns, String[][] template, String[][] languages, boolean show, boolean processed)
	{
		if (columns==null) return;
		
		String[] referenceList = 
		{
			"compare",          "encyclopedic info", "gloss", 
			"references", 		 "restrictions",     "usage",            
			"variants"
		};
		
		ArrayList<Element> elements = new ArrayList<Element>();
		ArrayList<String> names = new ArrayList<String>();
		Element element;
		String name;

		// Put all of the column elements into array lists
		NodeList list = columns.getElementsByTagName("classification");
		int i, len = list.getLength();
		for (i=0; i<len; i++)
		{
			element = (Element)list.item(i);
			name = element.getAttribute("title").toLowerCase();
			if (name.length()==0) name = "comments";
			
			elements.add(element);
			names.add(name);
		}
		
		list = columns.getElementsByTagName("translations");
		len = list.getLength();
		for (i=0; i<len; i++)
		{
			element = (Element)list.item(i);
			name = element.getAttribute("title").toLowerCase();
			if (name.length()==0) name = "references";
			elements.add(element);
			names.add(name);
		}
		
		list = columns.getElementsByTagName("example");
		len = list.getLength();
		for (i=0; i<len; i++)
		{
			element = (Element)list.item(i);
			elements.add(element);
			names.add("lexical function");
		}
		
		list = columns.getElementsByTagName("ontology");
		len = list.getLength();
		name = "ontology";
		for (i=0; i<len; i++)
		{
			element = (Element)list.item(i);
			elements.add(element);
			names.add(name);
		}
		
		
		// Begin the formatting
		String field, title, format, separator, fieldText = "", output, before;
		String parent, abbrev, child, face, size;
		int location;
		for (i=0; i<template.length; i++)
		{
			field = template[i][F_FIELD];
			title = template[i][F_TITLE];
			format = template[i][F_FORMAT];
			separator = template[i][F_SEPARATOR];
			
			before = "false";
			try
			{
				before = template[i][F_POSITION];
			}
			catch (Exception e) {}
			
			if (!processed && before.equals("false"))
				continue;
			else if (processed && before.equals("true"))
				continue;
			
			location = names.indexOf(field);
			if (location >= 0)
			{
				element = elements.get(location);
				elements.remove(location);
				names.remove(location);
				
				if (title.length() > 0) 
				{
					title = title.replaceAll("\\\\n", "<br>\n");
					title = title.replaceAll("\\\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
					title = title.replace("\\s", "&nbsp;");

					build.append(title);
				}
				
				build.append(" ");
				
				Node firstChild = element.getFirstChild();
				if (firstChild != null) fieldText = firstChild.getNodeValue();
				if (fieldText == null) fieldText = "";
				
				boolean isReference = false;
				for (String temp : referenceList)
				{
					if (temp.equalsIgnoreCase(field))
					{
						isReference = true;
						break;
					}
				}
				
				if (isReference)
				{
					if (fieldText.length()>0)
					{
						styleString(build, 
							fieldText, 
							-1, "none", " ", languages[FACE][GLOSS],
							languages[SIZE][GLOSS], "normal" );
					}
					styleTranslations(build, element, languages, separator, false);
				}
				else if (field.equalsIgnoreCase("lexical function"))
				{
					Element[] elementLists = getElementLists(element);
						
					element = elementLists[TRANSLATIONS];
					fieldText = element.getFirstChild().getNodeValue();
					if (show) stylePicture(build, elementLists[MEDIA]);
					
					if (fieldText!= null && fieldText.length()>0)
					{
						styleString(build, 
							fieldText, 
							-1, "none", " ", languages[FACE][GLOSS],
							languages[SIZE][GLOSS], "normal" );
					}
					styleTranslations(build, element, languages, separator, false);
				}
				else if (field.equalsIgnoreCase("ontology"))
				{
					abbrev = element.getAttribute("abbreviation");
					parent = element.getAttribute("parent");
					child  = element.getAttribute("child");
					if (abbrev.length()==0 || abbrev.equals(child+parent))
						 output = child;
					else output = abbrev;
					
					if (child.length() + fieldText.length() == 0) 
						continue;
					
					int ontLen = (abbrev + parent + child).length();
					if (ontLen>0 && fieldText.length()>0) output += ": ";
					output += fieldText;

					styleString(build, 
							output, -1, "none", separator, 
							languages[FACE][GLOSS],
							languages[SIZE][GLOSS], format );
				}
				else
				{
					face = element.getAttribute("face");
					size = element.getAttribute("size");
					fieldText = element.getTextContent();
					
					if (field.equalsIgnoreCase("table"))
					{
						build.append("<br><pre>" + fieldText + "</pre>");
					}
					else
					{
						styleString(build, fieldText,  -1, "none", 
								separator, face, size, format);
					}
				}
			}   // End of processing field
		}
		
	}   // End of styleFields method
	
	/** Format a group of translation elements
	 * 
	 * @param build THe string builder object
	 * @param element The <translations> element to begin searching
	 * @param languages The languages to format
	 * @param separator The separator to enclose the translation element
	 * @param isGroup true if the translations are for a word or definition element
	 */
	private void styleTranslations(StringBuilder build, Element element, String[][] languages, String separator, boolean isGroup)
	{
		if (element==null) return;
		
		StringBuilder temp = new StringBuilder();
		
		// Assume default if W_SEPARATOR is not defined
		String translationSeparator = ",";
		try
		{
			translationSeparator = template[t.W_HDR.ordinal()][0][W_SEPARATOR];
		}
		catch (Exception e) {}
		
		// Assume false if W_SHOW_LANGUAGE is not defined
		String excludeLanguage = "false";
		try
		{
			excludeLanguage = template[t.W_HDR.ordinal()][0][W_SHOW_LANGUAGES];
		}
		catch (Exception e) {}
		
		Element translation;
		String lang, text;
		boolean first = true;
		int len;
		
		NodeList translations = element.getElementsByTagName("translation");
		len = translations.getLength();
		boolean shouldWrap = excludeLanguage.equals("false");
		if (shouldWrap && separator.length()==0)
			separator = "()";
		
		for (int t= 0; t<len; t++)
		{
			translation = (Element)translations.item(t);
			lang = translation.getAttribute("lang");
			Node node;
			
			for (int i=GLOSS; i<languages[LANGUAGE].length; i++)
			{
				if (lang.equals(languages[0][0]) && isGroup)
					continue;
				
				if (lang.equalsIgnoreCase(languages[LANGUAGE][i]))
				{
					if (!first)
						temp.append(" " + translationSeparator + " ");
					
				    first = false;
					node = translation.getFirstChild();
					text = (node==null) ? "" : node.getNodeValue();
					if (shouldWrap)
						temp.append(lang + ": ");
					
					styleString(temp, text, 
							-1, "none", "", languages[FACE][i],
							languages[SIZE][i], "normal" );
				}
			}		// End of processing each language
		}			// End of processing translation list

		String result = temp.toString();
		
		if (result.length()>0)
		{
			if (separator.equals("none"))
			{
				build.append(result);
				build.append(" " + translationSeparator);
			}
			else if (separator.length()>0)
			{
				// Format the first part of the separator
				if (separator.length() == 2)
				{
					build.append(separator.charAt(0));
				}
				build.append(result);
				
				// Append the second part of the separator
				build.append(separator.charAt(separator.length()-1));
			}
		}
		build.append(" ");

	}	// End of formatTranslations
	
	/** Format a picture element
	 * 
	 * @param build THe string builder object
	 * @param element The <translations> element to begin searching
	 */
	private void stylePicture(StringBuilder build, Element media)
	{
		if (media==null) return;
		
		String picture = media.getAttribute("picture");
		if (picture==null || picture.length() == 0) return;
		
		MultimediaManager manager = getMediaManager();
		String path = manager.getPicturePath(picture);
		String pathName = new File(path).getName();
		if (path.length() > 0)
		{
			if (directory != null)
			{
				String sep = File.separator;
				path = "." + sep + directory + sep + picture;
			}

			try
			{
				int lastIndex = pathName.lastIndexOf(".");
			    String extension = pathName.substring(lastIndex);
				String name = pathName.substring(0, lastIndex);
				File temp = File.createTempFile(name, extension);
				temp.deleteOnExit();
				
				manager.resizePicture(new URL(path), temp.getAbsolutePath(), -1,96);
				path = temp.toURI().toURL().toString();
			}
			catch (Exception e)  {}
			
			build.append("<br />");

/* Alternat approach to displaying a picture

			String divString = " <div style=\"height:96px;"
				    + "max-height:96px;"
				    + "height:96px"
				    + "width:50%;"
				    + "max-width:50%"
				    + "margin-left:30px;"
				    + "background-image:url(" + path + ");" 
					+ "background-repeat:no-repeat;"
					+ "\">";
			
			build.append(divString);
*/
			build.append("&nbsp;&nbsp;&nbsp;");
			
			String imgString = " <img src=\"" + path + "\""
					+ " alt=\"" + pathName + "\""
					+ " align=\"middle\" "
					+ "/>";
			
			build.append(imgString);

			//build.append("/div>?);
			build.append("<span style=\"clear:both\"></span>");
		}
	}
	
	/**
	 * 
	 * @param build String builder object to append
	 * @param text The text to style
	 * @param count The index for this text component
	 * @param order # #. #) i i. i) I I. I) a a. a) A A. A) or none
	 * @param separator () [] <> {} "" '' : ; , - | or none
	 * @param fontFace font to style or null
	 * @param fontSize font size in pts or null
	 * @param format normal, bold, italic, gold, blue, green, red, magenta, black
	 */
	private void styleString
			(	StringBuilder build, String text, 
				int count, String order, 
				String separator, 
				String fontFace, String fontSize, 
				String format)
	{
		// Handle proper ordering of the string
		switch (order)
		{
			case "#":
				build.append("" + count + " ");
			case "#.":
				build.append("" + count + ". ");
				break;
			case "#)":
				build.append("" + count + ") ");
				break;
				
			case "i":
			case "I":
				build.append(romanNumerals(count, order.charAt(0)=='I') + " ");
				break;
			case "i.":
			case "I.":
				build.append(romanNumerals(count, order.charAt(0)=='I') + ". ");
				break;
			case "i)":
			case "I)":
				build.append(romanNumerals(count, order.charAt(0)=='I') + ") ");
				break;
	
			case "a":
			case "A":
				build.append(alphaNumerals(count, order.charAt(0)=='A') + " ");
				break;
			case "a.":
			case "A.":
				build.append(alphaNumerals(count, order.charAt(0)=='A') + ". ");
				break;
			case "a)":
			case "A)":
				build.append(alphaNumerals(count, order.charAt(0)=='A') + ") ");
				break;
		}
		
		// Nothing to style if no text
		if (text==null || text.length()==0) return;
		
		// Format the first part of the separator
		if (separator.length() == 2)
		{
			build.append(separator.charAt(0));
		}
			
		// Appropriately style the text
		build.append("<span style = \"");
		if (fontFace!=null && fontFace.length()>0)
		{
			build.append("font-family:" + fontFace + ", serif;");
		}
		
		if (fontSize!=null && fontSize.length() > 0)
			build.append("font-size:" + fontSize + "pt;");
		
		switch (format.toLowerCase())
		{
		case "normal":
		case "italic":
			build.append("font-style:" + format + ";");
			break;
			
		case "bold":
			build.append("font-weight:" + format + ";");
			break;
			
		case "red":
		case "green":
		case "blue":
		case "gold":
		case "magenta":
		case "black":
			build.append("color:" + format + ";");
			break;
		}
		
		build.append("\">");
		
		build.append(text);
		build.append("</span>");
		
		// Append the second part of the separator
		if (separator.length() == 2)
		{
			build.append(separator.charAt(1));
			build.append(' ');
		}
		else if (separator.length() == 1)
			build.append(separator.charAt(0));
		else if (separator.equalsIgnoreCase("none"))
			build.append(' ');
	}

	/** Find translation, column, and row lists from definition or word tag
	 * 
	 * @param element The word or definition element
	 * @return Array of translation, column, and row list tags
	 */
	private Element[] getElementLists(Element element)
	{
		NodeList list = element.getChildNodes();
		Element child, elementLists[] = new Element[LIST_SIZE];
		String tag;
		for (int i=0; i<list.getLength(); i++)
		{
			child = (Element)list.item(i);
			tag = child.getTagName();
			switch (tag)
			{
				case "translations":
					elementLists[TRANSLATIONS] = child;
					break;
				case "columns":
					elementLists[COLUMNS] = child;
					break;
				case "rows":
					elementLists[ROWS] = child;
					break;
				case "media":
					elementLists[MEDIA] = child;
					break;
			}
		}
		return elementLists;
	}

	/** Convert count to upper or lower case Roman numerals
	 * 
	 * @param count integer to convert
	 * @param upper true if convert to upper case
	 * @return converted integer as a string
	 */
	private String romanNumerals(int count, boolean upper)
	{
		// if out of range, don't convert
		if (count <= 0 || count >= 4000) return "" + count; 

        String symbol[]= {"m","cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"};
        if (upper)
          symbol = new String[]
        		         {"M","CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        int magnitude[]={1000, 900, 500,  400, 100,   90,  50,   40,  10,    9,   5,    4,  1 };

        String roman="";
        int digit=count;
        for(int x=0; count>0; x++)
        {
            digit=count/magnitude[x];
            for(int i=1; i<=digit; i++)
            {
                roman=roman + symbol[x];
            }
            count=count % magnitude[x];
        }
        return roman;
    }
	
	private final static int LETTERS = 26;
	/** Convert count to upper or lower case alpha numerals
	 * 
	 * @param count integer to convert
	 * @param upper true if convert to upper case
	 * @return converted integer as a string
	 */
	private String alphaNumerals(int count, boolean upper)
	{
		if (count<=0) return "" + count;
      
		char base = (char)((upper) ? 'A' : 'a');
		if (count==1) return "" + base;
      
		String result = "";
	    char letter;
	    count--;
		while (count!=0)
		{
			letter = (char)(((count)%LETTERS) + base);
			result = letter + result;
			count /= LETTERS;
		}
		return result;
	}
	
    public RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

	
    private MultimediaManager getMediaManager()
    {   RootDictionaryPanel rootPanel  = getRootDictionaryPanel();
        return rootPanel.getEnv().getMultimediaManager();
    }

}
