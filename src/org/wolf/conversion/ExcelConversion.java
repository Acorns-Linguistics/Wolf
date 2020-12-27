package org.wolf.conversion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import org.acorns.language.LanguageFont;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wolf.data.Author;
import org.wolf.data.Comment;

/** Class to convert an XML file into Wolf format 
 * 		to a WOLF dictionary
 * 
 *  Copyright: Dan Harvey (2020) All rights reserved
 *  
*/

import org.wolf.data.Constants;
import org.wolf.data.DictionaryData;
import org.wolf.data.Group;
import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.data.Ontology;
import org.wolf.data.Word;
import org.wolf.data.Reference;
import org.wolf.data.Unit;


public class ExcelConversion implements Constants
{
	private static String[][] fields =
	{
		{"Word", "" + DEFINITION_WIDGET_WIDTH, "t"}, 
		
		{"Compare", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Encyclopedia Info", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Gloss", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Lexical Function", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"References", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Reversals", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Restrictions", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Usage", "" + COLUMN_WIDGET_WIDTH, "t"},
		{"Variants", "" + COLUMN_WIDGET_WIDTH, "t"},
		
		{"Definition", "" + ROW_WIDGET_WIDTH, "t"},
		{"Example", "" + EXAMPLE_WIDGET_WIDTH, "t"},

		{"Annotations", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Antonyms", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Categories", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Etymology", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Language Links", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Main Entry", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Morphemes", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Refer To", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Spelling", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Subentry", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Synonyms", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Table", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Thesaurus", "" + COLUMN_WIDGET_WIDTH, "c"},
		
		{"Row Comment", "" + ROW_WIDGET_WIDTH, "c"},
		{"Comment", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Ontology", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Ontology-C", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Ontology-PC", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Ontology-P/C", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Ontology-C/D", "" + COLUMN_WIDGET_WIDTH, "c"},
		{"Ontology-PC/D", "" + COLUMN_WIDGET_WIDTH, "c"},
	};
	
	private static enum WOLF
	{
		Field,
		Width,
		Type,
	}
			
	private static enum FONT
	
	{
		FontName, 
		Family, 
		Size, 
		Color,
		Width,
	}
	
	private static String[] defaultFont = new String[] {"Calabri", "Swiss", "12", "#000000", "50"};

	private static enum FIELD
	{
		FieldName,
		LanguageCode,   // ISP code/variant/sort order
		LanguageName,
		LanguageKey,
		Width,
		Type,			// Translation ("t") or Comment ("c") item
		Apply
	}
	
	private static int fieldLen = FIELD.values().length;
	
	private Element rootNode;
	private DictionaryData dictionary;
	
	
	private Hashtable<String, String[]> styles;
	
	public ExcelConversion(Element rootNode, DictionaryData dictionary)
			throws IOException
	{
		this.rootNode = rootNode;
		this.dictionary = dictionary;
		
		String[] newDefault = processFont(rootNode);
		if (newDefault != null)
			defaultFont = newDefault;
		
	    NodeList stylesNode = rootNode.getElementsByTagName("Styles");
	    Element rootStyles;
	    if (stylesNode.getLength() >0)
	    {
	    	rootStyles = (Element)stylesNode.item(0);
	    	NodeList styleList = rootStyles.getElementsByTagName("Style");
		    styles = processStyles(styleList);
	    }
	}
	
	public String convert() throws IOException
	{
		NodeList propertyList= rootNode.getElementsByTagName("DocumentProperties");
		if (propertyList.getLength() > 0)
		{
			Node properties = propertyList.item(0);
			addAuthors(properties);
		}

		NodeList workSheets = rootNode.getElementsByTagName("Worksheet");
		int sheets = workSheets.getLength();
		if (sheets == 0)
		{
			return "There are no worksheets in this dictionary";
		}
		
		for (int w=0; w<sheets; w++)
		{
			String result =  processWorksheet(w, workSheets.item(w));
			if (result!= null)
				return result;
		}
		
		return null;
	}

	/** Process the list of styles in the workbook */
	private Hashtable<String, String[]> processStyles(NodeList styleNodes)
	{
		styles = new Hashtable<String, String[]>();

		for (int i=0; i<styleNodes.getLength(); i++)
		{
			Node node = styleNodes.item(i);
			Element style = (Element)node;
			
			String id = style.getAttribute("ss:ID");
			if (id.length() == 0)
			{
				continue;
			}

			String[] fontData;
			fontData = processFont(style);
			if (fontData != null)
				styles.put(id, fontData);
			else
			{
				String parent = style.getAttribute("ss:Parent");
				fontData = styles.get(parent);
				if (fontData!=null)
					styles.put(id, fontData);
			}
		}
		return styles;
	}

	/** Process an XML Dictionary worksheet */
	private String processWorksheet(int sheetNo, Node sheetNode)
	{
		Element sheetElement = (Element)sheetNode;
		NodeList table = sheetElement.getElementsByTagName("Table");
		int tableLen = table.getLength();
		if (tableLen==0)
		{
			return error(sheetNo, "No dictionary data is present ");
		}
		
		Node tableNode = table.item(0);
		Element tableElement = (Element)tableNode;
		
		String defaultFontId = tableElement.getAttribute("ss:StyleID");
		String[] tableFont = processFont(defaultFontId);
		if (tableFont == null) 
		{
			tableFont = defaultFont;
		}
		
		String tRows = tableElement.getAttribute("ss:ExpandedRowCount");
		if (tRows.length()==0 || tRows.equals("0"))
		{
			return error(sheetNo, "No row data");
		}
		
		String tColumns = tableElement.getAttribute("ss:ExpandedColumnCount");
		if (tColumns.length()==0 || tColumns.equals("0"))
		{
			return error(sheetNo, "No column data");
		}
		
		// Process Column tags
		String defaultCellWidth = tableElement.getAttribute("ss:DefaultColumnWidth");
		NodeList columns = tableElement.getElementsByTagName("Column");
		Hashtable<Integer, String[]> columnSpecs 
					= processColumnCells(tableFont, defaultCellWidth, columns);

		// Process first row for field specifications
		NodeList rows = tableElement.getElementsByTagName("Row");
		Element row = (Element)(rows.item(0));
		NodeList cells = row.getElementsByTagName("Cell");

		int tableColumns = cells.getLength();
		String[][] columnFields = new String[tableColumns][];
		String[][] columnFonts  = new String[tableColumns][];
		
		String defaultLanguage = "";
		int cellLen = cells.getLength();
		if (cellLen == 0)
		{
			return error(sheetNo, "There are no fields specified");
		}

		String word = fields[0][WOLF.Field.ordinal()];
		for (int c=0; c<cellLen; c++)
		{
			if (c>=columnFields.length)
				break;
			
			Element columnElement = (Element)(cells.item(c));
			String columnIndex = columnElement.getAttribute("ss:Index");
			int column = c;
			if (columnIndex.length()>0)
			{
				column = Integer.parseInt(columnIndex) - 1;
				if (column > c) 
				{
					return error(sheetNo, "There is now field header for column " + (c+1));
				}
			}
			
			String field = columnElement.getTextContent().trim();
			columnFields[c] = verifyField(field);
			if (columnFields[c] == null)
			{
				return error(sheetNo, "Column header for field " + field + " is illegal");
			}
			
			String fieldName = columnFields[c][FIELD.FieldName.ordinal()];
			String fieldLanguage = columnFields[c][FIELD.LanguageCode.ordinal()];
			
			if (c==0)
			{
				defaultLanguage = fieldLanguage;

				if (!fieldName.equals(word))
				{
					return error(sheetNo, "The first column field must be 'Word'");
				}
				
				if (fieldLanguage.length() == 0)
				{
					return error(sheetNo, "There is no language code in the initial field");
				}
			}
			else
			{
				String type  = columnFields[c][FIELD.Type.ordinal()];
				if (type.equals("t") && fieldLanguage.length() == 0)
				{
					fieldLanguage = columnFields[c][FIELD.LanguageCode.ordinal()] = defaultLanguage;
				}

				if (fieldName.equals(word) && fieldLanguage.equals(defaultLanguage))
				{
					return error(sheetNo, "Only one word can be specified on a row");
				}
			}
			
			columnFonts[c] = processFont(columnElement);
			if (columnFonts[c] == null)
			{
				columnFonts[c] = columnSpecs.get(c);
				if (columnFonts[c] == null)
				{
					columnFonts[c] = tableFont;
				}
			}
			
			// Configure field width
			String width = columnElement.getAttribute("ss:Width");
			
			columnFields[c][FIELD.Width.ordinal()] =  
					maxValue(width, 
					columnFields[c][FIELD.Width.ordinal()], 
					columnFonts[c][FONT.Width.ordinal()],
					tableFont[FONT.Width.ordinal()]);
		}
		
		// Add Languages
		if (!addLanguages(columnFields, columnFonts))
		{
			return error(sheetNo, "The dictionary has no languages specified");
		}
		
		// Add words and fields
		String result = addWords(rows, columnFields, columnFonts);
		if (result != null)
		{
			return error(sheetNo, result);
		}
		
		return null;
	}
	
	/** Process column nodes
	 * 
	 * @param tableFont The default font to use
	 * @param defaultCellWidth The default table width
	 * @param columns A node list of Column tags
	 * @return Hash table with column specifications
	 */
	private Hashtable<Integer, String[]> processColumnCells
		(String[] tableFont, String defaultCellWidth, NodeList columns)
	{
		Hashtable<Integer, String[]> columnSpecs = new Hashtable<Integer, String[]>();
		
		for (int fc=0; fc< columns.getLength(); fc++)
		{
			Node columnNode = columns.item(fc);
			
			if (!(columnNode instanceof Element))
				continue;
			
			Element column = (Element)columnNode;
			
			int index = fc;
			String ssIndex = column.getAttribute("ss:Index");
			if (ssIndex.length()>0) 
				index = Integer.parseInt(ssIndex) -1; // To index from 0, not 1
			if (index > fc) fc = index;

			String ssStyleId = column.getAttribute("ss:StyleID");
			String[] data = styles.get(ssStyleId);
			if (data == null)
				data = tableFont.clone();

			String ssWidth = column.getAttribute("ss:Width");
			if (isGreater(defaultCellWidth, ssWidth))
			{
				ssWidth = defaultCellWidth;
			}
			
			if (isGreater(ssWidth, data[FONT.Width.ordinal()]))
			{
				data[FONT.Width.ordinal()] = ssWidth;
			}
			
			columnSpecs.put(index, data);
		}
		return columnSpecs;
	}


	/** Determine if column header is valid
	 * 
	 * @param field The cell data field
	 * @return The Wolf data corresponding to the field
	 */
	private String[] verifyField(String field)
	{
		try
		{
			String[] fieldElements = field.split(":");
			String languageCode = "", languageName = "", languageKey = "", apply = "w";
			
			if (fieldElements.length == 0)
				return null;
			
			if (fieldElements.length >= 2)
			{
				String[] codeElements = fieldElements[1].split("/");
				languageKey = codeElements[0];
				
				if (codeElements[0].length() != 3 && codeElements[0].length()>0)
					return null;
				
				if (codeElements.length >1)
				{
				    if (codeElements[1].length()>2)
					{
						codeElements[1] = codeElements[1].substring(0,2);
					}
					languageKey += "/" + codeElements[1];
				}
				fieldElements[1] = String.join("/", codeElements);
				languageCode = fieldElements[1];
			}
			
			if (fieldElements.length > 2)
				languageName = fieldElements[2].trim();
			if (fieldElements.length>3)
				apply = fieldElements[3].trim();
			
			
			// Determine if column header contains a valid Wolf field name
			// Format: [Dictionary field name, language code, language name]
			for (int f=0; f<fields.length; f++)
			{
				String[] dictionaryField = fields[f];

				if (dictionaryField[WOLF.Field.ordinal()].equals(fieldElements[0]))
				{
					String[] fieldItem = new String[fieldLen];
					fieldItem[FIELD.FieldName.ordinal()] = dictionaryField[WOLF.Field.ordinal()];
					fieldItem[FIELD.LanguageCode.ordinal()] = languageCode;
					fieldItem[FIELD.LanguageKey.ordinal()] = languageKey;
					fieldItem[FIELD.LanguageName.ordinal()] = languageName;
					fieldItem[FIELD.Width.ordinal()] = dictionaryField[WOLF.Width.ordinal()];
					fieldItem[FIELD.Type.ordinal()] = dictionaryField[WOLF.Type.ordinal()];
					fieldItem[FIELD.Apply.ordinal()] = apply;
					return fieldItem;
				}
			}
		}
		catch (Exception e) {}
		return null;
	}

	/** Process the author for this dictionary
	 * 
	 * @param properties The Excel DocumentProperties node
	 */
	public void addAuthors(Node properties)
	{
		NodeList list = properties.getChildNodes();
		int count = list.getLength();
		
		Element element;
		String text, nodeName;
		
		Author[] authors = dictionary.getAuthors(), newAuthors;
		int numAuthors = authors.length;
		for (int c=0; c<count; c++)
		{
			if (!(list.item(c) instanceof Element))
				continue;
			
			element = (Element)list.item(c);
			nodeName = element.getNodeName().toLowerCase();
			if (nodeName.equals("author")
					|| nodeName.equals("lastauthor"))
			{
				text = element.getTextContent().trim();
				if (isAuthor(authors, text))
				{
					continue;
				}
				Author author = new Author();
				author.setField(text, Author.NAME);
				
				if (author.isClear()) // There is no author data
					continue;

				newAuthors = new Author[++numAuthors];
				System.arraycopy(authors,0,newAuthors,0,authors.length);
				newAuthors[numAuthors-1] = author;
				authors = newAuthors;
			}
		}
		dictionary.setAuthors(authors);
	}

	/** Check if an author is already in the list 
	 * 
	 * @param authors Array of author objects
	 * @param name The name of the author in question
	 * @return true if the author already exists
	 */
	private boolean isAuthor(Author[] authors, String name)
	{
		for (Author author: authors)
		{
			if (author.getFields()[Author.NAME].equals(name))
				return true;
		}
		
		return false;
	}
	
	/** Find language object
	 * 
	 * @param languages Array of languages
	 * @param name The name of the language to find
	 * @return The language object or null if not found
	 */
	private Language findLanguage(Language[] languages, String name)
	{
		for (Language language: languages)
		{
			if (language.getLanguageCode().equals(name))
				return language;
		}
		
		return null;
		
	}
	
	/** Add the appropriate languages to the dictionary
	 * 
	 * @param columnFields The definition of Wolf data fields
	 * @param columnFonts The column font definitions
	 * @return true if successful
	 */
	private boolean addLanguages(String[][] columnFields, String[][] columnFonts)
	{
        Language[] languageArray = dictionary.getLanguages();
        ArrayList<Language> languages = new ArrayList<Language>();
        Collections.addAll(languages, languageArray);
        
        for (int c=0; c<columnFields.length; c++)
        {  
        	 String fontName = columnFonts[c][FONT.FontName.ordinal()];
        	 int fontSize = Integer.parseInt(columnFonts[c][FONT.Size.ordinal()]);
        	 
        	 String languageName = columnFields[c][FIELD.LanguageName.ordinal()];
        	 LanguageFont font = new LanguageFont(fontName, fontSize, languageName);
        	 
        	 String languageCode = columnFields[c][FIELD.LanguageCode.ordinal()];
        	 if (languageCode.length()==0)
        		 continue;
        	 
        	 Language language = new Language(languageCode, font);

        	 boolean found = false;
        	 
        	 // Don't add if already in the list
        	 for (int lang=0; lang<languages.size(); lang++)
        	 {
        		 if (languages.get(lang).compareTo(language) == 0)
        		 {
        			 found = true;
        			 break;
        		 }
        	 }
        	 
        	 if (found==false)
        	 {
            	 languages.add(language);
        	 }
        }
        
        int size = languages.size();
        if (size == 0) return false;
        
        languageArray = languages.toArray(new Language[languages.size()]);
		dictionary.setLanguages(languageArray);
        dictionary.setActiveLanguages();
		return true;
	}

	/** Process work sheet rows to add dictionary words and fields to the dictionary
	 * 
	 * @param rowNodes NodeList of Excel rows
	 * @param columnFields The specifications for each column field
	 * @param columnFonts The specification for each column font
	 * @return null if successful or an error string if not
	 */
	private String addWords(NodeList rowNodes, String[][] columnFields, String[][] columnFonts)
	{
		
		Language[] languages = dictionary.getLanguages();
		boolean definitionGloss = false; // Did previous definition have gloss entry
		
		String defaultLanguage = columnFields[0][FIELD.LanguageKey.ordinal()];
		Language language = findLanguage(languages, defaultLanguage);

		for (int r=1; r<rowNodes.getLength(); r++)
		{
			Word word = null;
			Group group = null;
			Unit example = null;
			Reference reference = null;

			if (!(rowNodes.item(r) instanceof Element))
				continue;
			
			Element row = (Element)(rowNodes.item(r));
			NodeList cellNodes = row.getElementsByTagName("Cell");
			for (int c=0; c<cellNodes.getLength(); c++)
			{
				if (!(cellNodes.item(c) instanceof Element))
					continue;

				Element cell = (Element)(cellNodes.item(c));
				
				String cellIndex = cell.getAttribute("ss:Index");
				if (cellIndex.length()>0)
				{
					int columnIndex = Integer.parseInt(cellIndex) - 1;
					if (columnIndex<c)
					{
						return "Row " + r + " has duplicate columns";
					}

					if (columnIndex > c)
					{
						if (c == 0)
						{
							return "Row " + r + " has No word specified";
						}
						
						c = columnIndex;
						if (columnIndex > columnFields[c].length)
						{
							return null; // Ignore extra columns
						}
					}
				}
				
				String lang = columnFields[c][FIELD.LanguageKey.ordinal()];
				if (lang==null || lang.length() == 0) lang = defaultLanguage;
				
				String fieldType = columnFields[c][FIELD.FieldName.ordinal()];
				Item item;

				String data = cell.getTextContent();
				if (!fieldType.equals("Definition"))
				{
					definitionGloss = false;
				}
				switch (fieldType)
				{
					case "Word":
						if (word == null)
						{
							group = word = new Word(data);
							configureCell(lang, cell, word, columnFields[c], columnFonts[c]);
						}
						else
						{
							word.setIndigenousData(lang, data);
						}
						break;
	
					case "Compare":
					case "Encyclopedia Info":
					case "Gloss":
					case "References":
					case "Restrictions":
					case "Usage":
					case "Variants":
					case "Lexical Function":
					case "Reversals":
						if (lang.equals(defaultLanguage) || reference == null
						           || !(reference.getTitle().equals(fieldType)))
						{
							reference = new Reference();
						}
						configureCell(lang, cell, reference, columnFields[c], columnFonts[c]);
						addToColumn(word, group, reference, columnFields[c]);
						
						if (!(fieldType.equals("Reversals")))
								break;
					
						// Create the word in the other language for reversal
						if (!lang.equals(defaultLanguage))
						{
							for (Language reverseLanguage: languages)
							{
								String code = reverseLanguage.getLanguageCode();
								if (code.equals(lang))
								{
									String wordKey = word.getKey();
									if (wordKey.length()>0)
									{
										Word reverseWord = new Word(data);
										reverseWord.setIndigenousData(defaultLanguage, wordKey);
										reverseLanguage.mergeWord(reverseWord);
									}
								}
							}
						}
						break;
	
					case "Definition":
						if ( (lang.equals(defaultLanguage) == definitionGloss)
								|| group == null || group instanceof Word)
						{
							group = new Group("Definition");
							configureCell(lang, cell, group, columnFields[c], columnFonts[c]);
					        group.setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));
					        group.getMedia().setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));
					        if (lang.equals(defaultLanguage))
					        		group.getMedia().getTranslationData().setGloss(data);
							word.addRow(group);
						}
						else
						{
							group.setIndigenousData(lang, data);
						}
						
						// Indicate if gloss has been specified
						if (lang.equals(defaultLanguage))
						{
							definitionGloss = true;
							group.getMedia().getTranslationData().setGloss(data);					
						}
						break;
						
					case "Example":
						if (lang.equals(defaultLanguage) || example == null)
						{
							example = new Unit("Unit");
							configureCell(lang, cell, example, columnFields[c], columnFonts[c]);
					        example.setSize(new Dimension(EXAMPLE_WIDGET_WIDTH, TEXT_HEIGHT));
					        if (lang.equals(defaultLanguage))
					        	example.setGloss(data);
							addToRow(word, group, example, columnFields[c]);
						}
						else
						{
							example.setIndigenousData(lang, data);
						}
						break;
	
					case "Antonyms":
					case "Categories":
					case "Etymology":
					case "Language Links":
					case "Main Entry":
					case "Refer To":
					case "Spelling":
					case "Subentry":
					case "Synonyms":
					case "Table":
					case "Thesaurus":
						item = new Comment();
						configureCell(lang, cell, item, columnFields[c], columnFonts[c]);
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					case "Comment":
						item = new Comment();
						configureCell(lang, cell, item, columnFields[c], columnFonts[c]);
						item.setTitle("");
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					
					case "Row Comment":
						item = new Comment();
						configureCell(lang, cell, item, columnFields[c], columnFonts[c]);
						item.setTitle("");
						addToRow(word, group, item, columnFields[c]);
						break;
						
					case "Ontology":
						item = makeOntologyField(ONTOLOGY_DATA, data);
						if (item == null)
						{
							return "Illegal Ontology data on row " + r + " column " + c;
						}
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					case "Ontology-C":
						item = makeOntologyField(ONTOLOGY_VALUE, data);
						if (item == null)
						{
							return "Illegal Ontology data on row " + r + " column " + c;
						}
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					case "Ontology-PC":
						item = makeOntologyField(ONTOLOGY_PARENTVALUE, data);
						if (item == null)
						{
							return "Illegal Ontology data on row " + r + " column " + c;
						}
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					case "Ontology-P/C":
						item = makeOntologyField(ONTOLOGY_PARENT_VALUE, data);
						if (item == null)
						{
							return "Illegal Ontology data on row " + r + " column " + c;
						}
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					case "Ontology-C/D":
						item = makeOntologyField(ONTOLOGY_VALUE_DATA, data);
						if (item == null)
						{
							return "Illegal Ontology data on row " + r + " column " + c;
						}
						addToColumn(word, group, item, columnFields[c]);
						break;
	
					case "Ontology-PC/D":
						item = makeOntologyField(ONTOLOGY_PARENTVALUE_DATA, data);
						if (item == null)
						{
							return "Illegal Ontology data on row " + r + " column " + c;
						}
						addToColumn(word, group, item, columnFields[c]);
						break;
						
					default:
						return "Illegal field specifier row " + r + " column " + c;
				}
			} 		 // End of processing rows
			
			language.mergeWord(word);
		}
		
		dictionary.setLanguages(languages);
		return null; // End of addWords class, successful return
	}

	// Add the item to columns or words depending per column specification
	private void addToColumn(Word word, Group group, Item item, String[] fieldData)
	{
		if (fieldData[FIELD.Apply.ordinal()].equalsIgnoreCase("w"))
			word.addColumn(item);
		else
			group.addColumn(item);
	}
	
	// Add the item to columns or words depending per column specification
	private void addToRow(Word word, Group group, Item item, String[] fieldData)
	{
		if (fieldData[FIELD.Apply.ordinal()].equalsIgnoreCase("w"))
			word.addRow(item);
		else
			group.addRow(item);
	}

	/** Make an ontology cell
	 * 
	 * @param type The type of Ontology cell
	 * @param data The data to go in the cell
	 * @param item
	 */
	private Item makeOntologyField(int type, String data)
	{
		String[] ontologyData = data.split(",");
		String parent, child, abbrev,  value = data;
		parent = child = abbrev = "";
		
		switch (type)
		{
		case ONTOLOGY_DATA:
			value = data;
			break;
		case ONTOLOGY_VALUE:
			child = data;
			break;
		case ONTOLOGY_PARENTVALUE:
		case ONTOLOGY_PARENT_VALUE:
			if (ontologyData.length != 2)
				return null;
			
			parent = ontologyData[0].trim();
			child = ontologyData[1].trim();
			break;
			
		case ONTOLOGY_VALUE_DATA:
			child = ontologyData[0].trim();
			value = ontologyData[1].trim();
			break;
			
		case ONTOLOGY_PARENTVALUE_DATA:
			if (ontologyData.length != 3)
				return null;
			parent = ontologyData[0].trim();
			child = ontologyData[1].trim();
			value = ontologyData[2].trim();
			break;
		
		}
		
		Item item = new Ontology(parent, child, abbrev, value, type);
		return item;
	}
	
	/** Configure a cell format along with text and possible phonetic data
	 * 
	 * @param lang  The cells language
	 * @param cellNode The Excel node containing XML data
	 * @param item The dictionary cell type
	 * @param fieldData The definition of the field
	 * @param fontData The definition of the font
	 */
	private void configureCell(String lang, Element cell, Item item, String[] fieldData, String[] fontData)
	{
		Group group = null;
		if (item instanceof Group)
			group = (Group)item;
		
		String fieldName = fieldData[FIELD.FieldName.ordinal()];
		item.setTitle(fieldName);
		
		String style = cell.getAttribute("ss:StyleID");
		String[] styleData = styles.get(style);
		if (styleData == null)
			styleData = fontData;
		
		Font font = makeFont(styleData);
		item.setFont(font);
		
		Color color = Color.WHITE;
		
		String colorStyle = styleData[FONT.Color.ordinal()];		
		if (colorStyle.length()==7)  // #rrggbb format
		{
			int red = Integer.parseInt(colorStyle.substring(1,3));
			int green = Integer.parseInt(colorStyle.substring(3,5));
			int blue = Integer.parseInt(colorStyle.substring(5,7));
			color = new Color(red, green, blue);
		}
		
		item.setForeground(color);

		Dimension size = item.getSize();
		int width = Integer.parseInt(fieldData[FIELD.Width.ordinal()]);
		size.height = AREA_WIDGET_HEIGHT;
		if (width > size.width)
		{
			size.width = width;
		}
		item.setSize(size);
		
		NodeList children = cell.getChildNodes();
		for (int c=0; c<children.getLength(); c++)
		{
			if (!(children.item(c) instanceof Element))
				continue;

			Element child = (Element)(children.item(c));
			String data = child.getTextContent().trim();
			
			switch (child.getNodeName())
			{
				case "Data":
					if (group != null && !(group instanceof Word))
						group.setIndigenousData(lang, data);
					else
					{
						if (item instanceof Comment)
						{
							Comment comment = (Comment)item;
							comment.setComment(data);
						}
						
						// The language code follows the underscore.
						else if (item instanceof Reference)
						{
							Reference reference = (Reference)item;
							reference.setIndigenousData(lang, data);
						}
						
						else if (item instanceof Unit)
						{
							Unit unit = (Unit)item;
							unit.setIndigenousData(lang, data);
						}
						else if (item instanceof Reference)
						{
							Unit reference = (Unit)item;
							String title = item.getTitle();
							if (title.equals("Lexical Function"))
							{
								reference.setGloss(data);
							}
						}
					}
					break;
					
				case "Phonetics":
					if (group != null)
						group.getMedia().getTranslationData().setPhonetics(data);
					else
					{
						if (item instanceof Comment)
						{
							Comment comment = (Comment)item;
							comment.setPhonetics(data);
						}
					}
					break;
			}
		}
	}
	
	/** Create a font from the font data array */
	private Font makeFont(String[] fontData)
	{
		String name = fontData[FONT.FontName.ordinal()];
		int size = Integer.parseInt(fontData[FONT.Size.ordinal()]);
		Font font = new Font(name, Font.PLAIN, size);
		return font;
	}

	/** Process a <Font> node and return the values contained */
	private String[] processFont(Node node)
	{
		String[] specsData = defaultFont.clone();
		Element element = (Element)node;
		NodeList list = element.getElementsByTagName("Font");

		if (list.getLength()>0)
		{
			element = (Element)(list.item(0));
			specsData[FONT.FontName.ordinal()] = element.getAttribute("ss:FontName");
			specsData[FONT.Family.ordinal()] = element.getAttribute("x:Family");
			specsData[FONT.Size.ordinal()] = element.getAttribute("ss:Size");
			specsData[FONT.Color.ordinal()] = element.getAttribute("ss:Color");
			return specsData;
		}

		return null;
	}

	/** Get font from Excel style table
	 * 
	 * @param id Key for finding the font style
	 * @param defaultFont default font specs to use
	 */
	private String[] processFont(String id)
	{
		if (id.length()==0)
			return null;
		
		String[] fontData = styles.get(id);
		return fontData;
	}

	/** Return the max of three string values
	 * 
	 * @param first
	 * @param second
	 * @param third
	 * @param fourth
	 * @return Maximum string
	 */
	private String maxValue(String first, String second, String third, String fourth)
	{
		if (first.length()==0)
			first = "0";
		
		String max = first;
		if (isGreater(second, max))
			max = second;
		
		if (isGreater(third, max))
				max = third;
		
		if (isGreater(fourth, max))
			max = fourth;

		return max;
	}
	
	/** Compare the number value of two strings
	 * 
	 * @param first String A
	 * @param second String B
	 * @returns true if A>B
	 */
	private boolean  isGreater(String first, String second)
	{
		if (first.length() == 0) first = "0";
		if (second.length() == 0) second = "0";
		
		return Double.parseDouble(first) > Double.parseDouble(second);
	}
	
	/** Construct error message for an illegal worksheet */
	private String error(int sheet, String msg)
	{
		return "Worksheet " + sheet + ": " + msg;
	}
	
}	// End of ExcelConversion class
