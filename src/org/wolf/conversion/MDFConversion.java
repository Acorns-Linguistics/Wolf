package org.wolf.conversion;

/** Class to convert an SIL Standard Format Marker (SFM) file 
 * 		to a WOLF dictionary
 * 
 * Note: Reference objects are initialized with languages "v", "e", "n", "r" codes
 *  		which are replaced by actual codes after the initial reading
 *  		and converting of the file.
 *  
 *  Homonyms: add _# at the end of a word. Wolf needs to subscript the number 
 *  				on print preview and web application output.
 */

import java.awt.Dimension;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javax.swing.JLabel;

import org.acorns.language.LanguageFont;
import org.wolf.application.RootDictionaryPanel;
import org.wolf.data.Comment;
import org.wolf.data.Constants;
import org.wolf.data.DictionaryData;
import org.wolf.data.Group;
import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.data.Ontology;
import org.wolf.data.Reference;
import org.wolf.data.Unit;
import org.wolf.data.Word;
import org.wolf.lib.LanguageCodes;

public class MDFConversion implements Constants
{
	public static String[] Vernacular = new String[]{"Citation", "Morph"};
	
	private BufferedReader in;
	private FileChannel channel;
	private long fileLength, position;
	
	private JLabel label;
	
	
	private DictionaryData dictionary;
	private String         directory;
	
	private ArrayList<String> languages;
	private int[] counts;
	private String[] names;
	
	private ArrayList<Word> words;
	private ArrayList<Reversal> reversals;

	private ArrayList<Item> columns;
	private ArrayList<Item> rows;
	private ArrayList<Item> references;
    
    // Current word object being built from SFM
    private Word      word;
    
    // The current Reference object with variants in each language
	private Item reference;
	
	// The current ontologyItem being built from SFM
    private Ontology  ontologyItem;
    
    // The current label to be prepended to a lexical function
    private String    lexicalLabel;
    
    // The reference value to be included wtih mn entries.
    private String	  ctValue;
    
    // SE comment
    private Comment   seComment;
    
    // Last Word, Definition or example Unit object encountered.
    private Unit      lastUnit; 
    
    // Last type (Definition, Gloss, or Example)
    private String 	  lastType;
    
    // Last and current sense
    private int 	  lastSense, sense;
    
    // Last Word or Definition Group object
    private Group     lastGroup;
    
    // The language indicated by the previous SFM code
    private String    lastLanguage;
    
 	public MDFConversion(File file, DictionaryData dictionary)
				throws IOException
	{
 		FileInputStream fis = new FileInputStream(file);
 		channel = fis.getChannel();
 		Reader reader = new InputStreamReader(fis, "UTF-8");
        in = new BufferedReader( reader);
        
        fileLength = file.length();
        position = 0;
        
		this.dictionary = dictionary;
		
		this.directory  = file.getParent();
		if (directory == null) directory = "";
		else directory += File.separator;
		
        // Initialize for gloss, vernacular, regional, national
        languages = new ArrayList<String>();
        for (int i=0; i<DEFAULT_LANGUAGES.length; i++)
        	languages.add("");
                
        counts = new int[DEFAULT_LANGUAGES.length];
        names = new String[DEFAULT_LANGUAGES.length];

        // Initialize lists accumulated during the parsing
        references   = new ArrayList<Item>();
		words = new ArrayList<Word>();
		reversals = new ArrayList<Reversal>(); 
		
        // The following are list that are reset during the parsing
        columns = new ArrayList<Item>(); // Word, definition or sub-entry columns
        rows = new ArrayList<Item>(); // Word, definition or sub-entry rows
        
        reference    = null;
        ontologyItem = null;
        lexicalLabel = null;
        ctValue      = "";
        
        lastUnit     = null; 
        lastGroup    = null;
        lastType     = null;
        lastSense    = 0;
        sense = -1;
        lastLanguage = "";
        seComment    = null;
        
        PropertyChangeListener[] pcl 
        = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                         ("DictionaryListeners");
        RootDictionaryPanel root  = (RootDictionaryPanel)pcl[0];
        label = root.getErrorLabel();

   	}
 	
	/** Convert input text file in mdf format to a Wolf dictionary
	 * 
	 * @return "" if OK, otherwise and error message
	 * @throws IOException
	 */
	public String convert() throws IOException
	{
		String line, language, nextLine;
		String marker = "", lastMarker = ""; 
		
		String command = null, lastCommand = null, separator;
		boolean wordActive = false;
		
        int start, end;
        
        line = in.readLine();
        if (line==null) throw new IOException("File has no Data");;
        
        while (line!= null)
        {
        	nextLine = in.readLine();
            position = channel.position();
            
        	if (nextLine!=null)
        	{
        		nextLine = nextLine.trim();
        		if (nextLine.length()==0) continue;
        	
        		// Handle multi-line comments
        		start = nextLine.indexOf('\\');
	        	if (start<0)
	        	{
	        		separator = (line!=null && line.contains("\\tb")) ? "\n" : " ";
	        		line += separator + nextLine;
	        		continue;
	        	}
        	}
        	
        	// We have a command at this point.
            updateProgress();
        	
        	start = line.indexOf('\\');
        	if (line.length() == 0 || line.startsWith("\\_") 
        			||start<0 || start == line.length()) 
        	{
        		line = nextLine;
        		continue;
        	}

        	end = line.indexOf(' ');
        	if (end<0) end = line.length();

        	lastCommand = command;
        	command = line.substring(start + 1, end).toLowerCase();
          	String data = (end==line.length()) ? "" : line.substring(end+1);
                  	
        	start = command.indexOf('_');
        	language = ""; 
        	marker = command;
        	
        	if (start>=0)
        	{
        	   language = command.substring(start + 1).toLowerCase();
        	   marker = command.substring(0,start).toLowerCase();
        	}
        	
        	if (!marker.equals("lx") && !wordActive)
        	{
        		line = nextLine;
        		continue;
        	}
        		
        	switch (marker)
        	{
        		// head word section
        		case "lx":	// Lexeme (Vernacular)
        			if (lastCommand != null && 
        				(lastCommand.equals("hm") || lastCommand.equals("lx")))
        			{
        				addLanguage(language, marker);
        				break;
        			}
    				clearData(word);

    				wordActive = data.length()>0;
    				if (!wordActive) break;
    				
    				word = new Word(data);
        			words.add(word);
    				addLanguage(language, marker);
        			
        			lastUnit = word.getMedia();
        			references.add(lastUnit);
        			
        			lastGroup = word;
        			sense = -1;
        			lastSense = 0;
        			break;
        		
        		case "hm":	// Homonym number (append _# to word key)
        			if (word == null) throw new IOException("Missing Headword");
        			word.setKey(word.getKey() + "_" + data);
        			break;
    	
        		case "lc":	// Citation form (Vernacular)
    				addColumn(new Comment("", "Citation " + data), language, marker + "v");
    				break;

        		case "se":	// Sub-entry (Vernacular)
        			addColumn((seComment = new Comment("Subentry", data)), language, marker);
    				break;

    			case "ph":	// phonetic form 
    				word.setPhonetics(data);
    				break;
        			
        		case "pn":	// Part of speech (National)
        		case "ps":	// Part of speech (English)
         		   	ontologyItem = new Ontology("Part of Speech", "Part of Speech", "ps", data, ONTOLOGY_DATA);
        			addColumn(ontologyItem, language, marker);
        			break;
        			
        		case "sn":	// sense number
        			wordSense();
					break;
					
        		case "cet":  // ???complex etymological term??? for subentry reference. Not in documentation
        			if (seComment != null)
           				seComment.setComment(data + " " + seComment.getComment());
        			break;
        			
           		case "mn":	// Main Entry Cross Reference (Vernacular)
           			if (seComment==null)
           			{
           				addColumn(new Comment("Main Entry", data), language, marker + "v");
           			}
           			else seComment.setComment(seComment.getComment() + ctValue + data);
        			ctValue = ",";
           			break;
           			
        		case "ct":   // ??? relationship to main entry ??? Not in documentation
       				addColumn((seComment = new Comment("Main Entry", data)), language, marker + "v");
       				ctValue = " ";
       				break;
        		
    			// Definition section
    			case "d":	// Definition("dv" if no language present
    			case "dv":	// Definition (Vernacular)
    			case "de":	// Definition (English)
    			case "dn":	// Definition (National)
    			case "dr":	// Definition (Regional)
   					addRow("Definition", data, language, marker);
    				break;

    			case "g":   // Gloss - Short definition ("gv" if no language present)
    			case "gv":	// Gloss - Short definition (Vernacular)
    			case "ge":	// Gloss - Short definition (English)
    			case "gn":	// Gloss - Short definition (National)
    			case "gr":	// Gloss - Short definition (Regional)
     				addRow("Gloss", data, language, marker);
 			        break;

    			case "r":
    			case "re":	// Reversal (English)
    			case "rn":	// Reversal (National)
    			case "rr":	// Reversal (Regional)
    				if (sense<0)
    				{
        				setIndigenousData(word, data, language, marker);
    				}
    				else
    				{
        				addReference("Reversals", data, language, marker);
        				addReference("Reversals", word.getKey(), "", marker+"v");
    				}
   	   				reversals.add(new Reversal(word, getLanguageCode(language, marker), data));
     				break;

    			case "w":   // Word level gloss ("wv" if no language present)
    			case "wv":	// Word level gloss (Vernacular)
    			case "we":	// Word level gloss (English)
    			case "wn":	// Word level gloss (National)
    			case "wr":	// Word level gloss (Regional)
    				ArrayList<Item> wordItems = word.getColumns();
    				int wordSize = wordItems.size();
        			if (wordSize==0 || references.size()==0 || wordItems.get(wordSize-1) != reference)
        			{
        				reference = new Reference("Gloss");
        				references.add(reference);
        				addItem(wordItems, reference, language, marker);
        			}
    				setIndigenousData(reference, data, language, marker );
        			break;

    			// Identity of the head word
    			case "lt":	// Literally (English)
    				addColumn(new Comment("", "Lit: " + data), language, marker + "e");
    				break;

    			case "sc":	// Scientific name (English)
    				addColumn(new Comment("", "Sc: " + data), language, marker + "e");
    				break;
		        
    			// Example sentences
    			case "rf":	// Reference (English)
    				addColumn(new Comment("Refer To", "Ref: " + data), language, marker + "e");
    				break;
        
    			case "x":	// Example ("xe" if no language present
    			case "xv":	// Example (Vernacular)
            	case "xe":	// Example free translation (English)
	        	case "xn":	// Example free translation (National)
	        	case "xr":	// Example free translation (Regional)
	        		addRow("Example", data, language, marker);
	        		break;
    			
	        	// Range of meaning and usage
	        	case "u":   // Assume "uv" if language not present
    			case "uv":	// Usage (Vernacular)
    			case "ue":	// Usage (Vernacular)
    			case "un":	// Usage (Vernacular)
    			case "ur":	// Usage (Vernacular)
    				addReference("Usage", data, language, marker);
    				break;

    			case "e":	// Encyclopedic information ("ev" if no language present)
    			case "ev":	// Encyclopedic information (Vernacular)
    			case "ee":	// Encyclopedic information (English)
    			case "en":	// Encyclopedic information (National)
    			case "er":	// Encyclopedic information (Regional)
    				addReference("Encyclopedic Info", data, language, marker);
    				break;
    			
    			case "o":	// Only/restrictions ("ov" if no language present)
    			case "ov":	// Only/restrictions (Vernacular)
    			case "oe":	// Only/restrictions (English)
    			case "on":	// Only/restrictions (National)
    			case "or":	// Only/restrictions (Regional)
    				addReference("Restrictions", data, language, marker);
    				break;
			        
        		// Lexical functions
    			case "lf":	// Lexical function label (English)
    				reference = null;
					lexicalLabel = data;
    				addReference("Lexical Function", data, language, marker);
 					break;
    
    			case "lv":	// Lexical function value (vernacular)
    			case "le":	// Lexical function gloss (English)
    			case "ln":	// Lexical function gloss (National)
    			case "lr":	// Lexical function gloss (Regional)
					if (lexicalLabel != null && lexicalLabel.length()>0)
					{
						data = lexicalLabel + ":" + data;
					}
    				addReference("Lexical Function", data, language, marker);
					break;
		        
	        	// Additional lexical cultural network of the head word
    			case "sy":	// Synonyms (Vernacular)
    				addColumn(new Comment("Synonyms", "Syn: " + data), language, marker + "v");
    				break;
    				
    			case "a":  // Spelling (Vernacular)
    				addColumn(new Comment("Spelling", data), language, marker + "v");
    				break;
        
    			case "an":	// Antonyms (Vernacular)
    				addColumn(new Comment("Antonyms", "Ant: " + data), language, marker + "v");
	        		break;
		        
    			case "mr":	// Morphology (Vernacular)
       				addColumn(new Comment("", "Morphology " + data), language, marker + "v");
    				break;
		        
    			case "cf":   // Cross Reference Gloss (Vernacular)
        		case "ce":	// Cross Reference Gloss (English)
        		case "cn":	// Cross Reference Gloss (National)
        		case "cr":	// Cross Reference Gloss (Regional)
    				addReference("Compare", data, language, marker);
    				break;

        		case "vet": // Variant etymolical term ??? Not a mdf marker.
        			if (references.size()>0)
        			{
	        			if (reference.getTitle().equalsIgnoreCase("Variants"))
	        			{
	    					String oldData = reference.getIndigenousData().get(lastLanguage);
	        				reference.setIndigenousData(lastLanguage, data + " " + oldData );
	        			}
        			}
        			break;
        			
    			case "va":	// Variant Form(s) (Vernacular)
    			case "ve":	// Variant Comment (English)
    			case "vn":	// Variant Comment (National)
    			case "vr":	// Variant Comment (Regional)
    				addReference("Variants", data, language, marker);
					break;
			         
			    // Origins of the head word
				case "bw":	// Borrowed Word (loan) (English)
					addColumn(new Comment("Etymology", "From: " + data), language, marker + "e");
					break;
        		
				case "et":	// Etymology (proto form) (Vernacular)
					addColumn(new Comment("Etymology", "Etym: "  + data), language, marker + "v");
					break;
	        		
				case "eg":	// Etymology Gloss (English)
					addColumn(new Comment("Etymology", "Gloss: " + data ), language, marker + "e");
					break;
	        		
				case "es":	// Etymology Source (English)
					addColumn(new Comment("Etymology", "Source: " + data), language, marker + "e");
					break;
	        		
				case "ec":	// Etymology Comment (English)
					addColumn(new Comment("Etymology", "Comment: " + data + ')'), language, marker + "e");
					break;
        		
        		// Grammatical paradigms
				case "pd":	// Paradigm (English)
					ontologyItem = new Ontology("data", data, data, "", ONTOLOGY_VALUE);
        			addColumn(ontologyItem, language, marker);
        			break;
							
				case "pdl":	// Paradigm label (English)
					if (ontologyItem==null) 
					{
						ontologyItem = new Ontology("Paradigm", data, data, "", ONTOLOGY_PARENT_VALUE);
	        			addColumn(ontologyItem, language, marker);
					}
					else
					{
						ontologyItem.setType(ONTOLOGY_PARENTVALUE_DATA);
						ontologyItem.setAbbrev(data);
						ontologyItem.setValue(data);
						ontologyItem.setData("");
					}
					break;
				
				case "pdv":	// Paradigm form Gloss (Vernacular)
				case "pde":	// Paradigm form Gloss (English)
				case "pdn":	// Paradigm form Gloss (National)
				case "pdr": // Paradigm form Gloss (Regional)
					if (ontologyItem == null)
					{
						ontologyItem = new Ontology("", marker, marker, data, ONTOLOGY_DATA);
	        			addColumn(ontologyItem, language, marker);
						break;
					}
					
					if (ontologyItem.getType()==ONTOLOGY_VALUE_DATA)
					{
						ontologyItem = new Ontology
								(ontologyItem.getParent(), ontologyItem.getValue(), ontologyItem.getAbbrev(), 
										data, ONTOLOGY_VALUE_DATA);
						addColumn(ontologyItem, language, marker);
						break;
					}
						
					ontologyItem.setData(data);
					ontologyItem.setType(ONTOLOGY_VALUE_DATA);
					break;
							
				case "sg":	// Singular form (Vernacular)
					addColumn(new Ontology("Number", "Singular", "Sg", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "pl":	// Plural form (Vernacular)
					addColumn(new Ontology("Number", "Plural", "Pl", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "rd":	// Re-duplication form(s) (Vernacular)
					addColumn(new Ontology("Derivational Unit", "Reduplication", "Redup", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "1s":	// First singular (Vernacular)
					addColumn(new Ontology("Number", "First Singular", "1s", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "2s":	// Second singular (Vernacular)
					addColumn(new Ontology("Number", "Second Singular", "2s", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "3s":	// Third singular (Vernacular)
					addColumn(new Ontology("Number", "Third Singular", "3s", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "4s":	// Non-animate singular (Vernacular)
					addColumn(new Ontology("Number", "Fourth Singular", "3sn", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "1d":	// First dual (Vernacular)
					addColumn(new Ontology("Person Plural", "First Dual", "1d", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "2d":	// Second dual (Vernacular)
					addColumn(new Ontology("Person Plural", "Second Dual", "2d", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "3d":	// Third dual (Vernacular)
					addColumn(new Ontology("Person Plural", "Third Dual", "3d", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "4d":	// Non-animate dual (Vernacular)
					addColumn(new Ontology("Person Plural", "Fourth Dual", "3dn", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "1p":	// First plural (Vernacular)
					addColumn(new Ontology("Person Plural", "First Plural", "1P", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "1i":	// First plural inclusive (Vernacular)
					addColumn(new Ontology("Person", "First Inclusive", "1pi", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "1e":	// First plural exclusive (Vernacular)
					addColumn(new Ontology("Person", "First Exclusive", "1px", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "2p":	// Second plural (Vernacular)
					addColumn(new Ontology("Person Plural", "Second Plural", "2P", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "3p":	// Third plural (Vernacular)
					addColumn(new Ontology("Person Plural", "Third Plural", "3P", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;
	        		
				case "4p":	// Non-animate plural (Vernacular)
					addColumn(new Ontology("Person Plural", "Fourth Plural", "4Pn", data, ONTOLOGY_VALUE_DATA), language, marker);
					break;

        		// Tables (tabs and/or new lines contained)
				case "tb":	// Table (with tabs and new lines)
					addColumn(new Comment("Table", data), language, marker + "v");
					break;
			
				// Categories (semantics and meanings)
				case "sd":	// Semantic domain (English)
					// Combine if multiple markers appear in a row
					if (marker.equals(lastMarker))
					{
						Item item = columns.get(columns.size()-1);
						Comment column = (Comment)(item);
						column.setComment(column.getComment() + "; " + data);
					}
					else
						addColumn(new Comment("Categories", "SD: " + data), language, marker + "e");
					break;
			
				case "is":	// Index of semantics (English)
					addColumn(new Comment("", "Semantics: + " + data), language, marker + "e");
					break;
			
				case "th":	// Thesaurus (Vernacular)
					addColumn(new Comment("Thesaurus", "Thes: " + data), language, marker + "v");
					break;
		
				// External references
				case "bb":	// Bibliography (English)
					addColumn(new Comment("Refer To", "Read: " + data), language, marker + "e");
					break;
			
				case "pc":	// Picture
					if (data.startsWith(".G.")) 
					{
						data = data.substring(3);
						String[] split = data.split(";");
						if (split.length>0) data = split[0].trim();
					}
					if (!new File(data).exists()) data = directory + data;
					lastUnit.insertPicture(data);
					break;
					
				case "sf":  // Audio tags
				case "xsf":
				case "xse":
					if (!new File(data).exists()) data = directory + data;
					lastUnit.insertAudio(data);
					break;
			
				// Notes
				case "nt":	// General notes (English)
					addColumn(new Comment("", "Note: " + data), language, marker + "e");
					break;
			
				case "np":	// Phonology notes (English)
					addColumn(new Comment("", "Phon: " + data), language, marker + "e");
					break;
			
				case "ng":	// Grammar notes (English)
					addColumn(new Comment("", "Gram: " + data), language, marker + "e");
					break;
			
				case "nd":	// Discourse notes (English)
					addColumn(new Comment("", "Disc: " + data), language, marker + "e");
					break;
			
				case "na":	// Anthropology notes (English)
					addColumn(new Comment("", "Anth: " + data), language, marker + "e");
					break;
			
				case "ns":	// Sociology notes (English)
					addColumn(new Comment("", "Socio: " + data), language, marker + "e");
					break;
			
				case "nq":	// Questions (English)
					addColumn(new Comment("", "Ques: " + data), language, marker + "e");
					break;
		
				// Miscellaneous
				case "so":	// Source (English)
		        	addColumn(new Comment("Refer To", "Source: " + data), language, marker+"e");
		        	break;
					
				case "st":	// Status (English)
    				addColumn(new Comment("", "Status: " + data), language, marker+"e");
    				break;
		
				case "dt":	// Date (English dd/MMM/yyyy)
					clearData(lastGroup); 
    				addColumn(new Comment("", "date: " + data), language, marker+"e");
    				break;
		
				default:	// non-MDF
					if (!(marker.equals("a") || marker.equals("sf") || marker.equalsIgnoreCase("xsf")))
    				addColumn(new Comment("", "??: " + data), language, marker+"e");
    				break;
        	}	// End switch

        	lastMarker = marker;
        	line = nextLine;

        }	// End while
        in.close();
        
        clearData(lastGroup);
        updateLanguageCodes();  // Replace the temporary language codes with those of actual languages.
        ArrayList<Language> dictionaryLanguages = createLanguages();
        if (dictionaryLanguages.size()>0)
        {
            addWords(dictionaryLanguages);
        }
        dictionary.setActiveLanguages();
 		return "";
	}

	/** Get the unique list of languages codes and their names associated with this dictionary
	 * 
	 * @return Array list of unique language codes
	 */
	public ArrayList<String> getLanguages()
	{
		ArrayList<String> uniqueLanguages = new ArrayList<String>();
		String code, name;

		
		for (int index=0; index<languages.size(); index++)
		{
			code =  languages.get(index);
			
			if (code.length()==0)
			{
				continue;
			}

			name = (index<names.length && names[index]!=null) ? names[index] : LanguageCodes.getName(code);
			if (name.length()==0) name = "???";
			code = code + " " + name;
			
			if (!uniqueLanguages.contains(code)) uniqueLanguages.add(code);
		}
		return uniqueLanguages;
	}
	

	/** Set the initial word sense if not already set */
	private void wordSense()
	{
		clearData(lastGroup);
		lastType = null;
		lastSense = sense;
		sense++;
	}


	/** Clear all the data remaining from the last Definition, Word, or Subentry
	 * 
	 */
	private void clearData(Group group)
	{
		Item item;
		
		if (group!=null)
		{
			for (int i=0; i<columns.size(); i++)
			{
				item = columns.get(i); 
				group.addColumn(item);
			}
		}
		columns.clear();
		
		if (group !=null)
		{
			for (int i=0; i<rows.size(); i++)
			{
				item = rows.get(i);
				group.addRow(item);
			}
		}
		rows.clear();
		
        reference    = null;
        ontologyItem = null;
        lexicalLabel = null;
        seComment    = null;
        ctValue      = "";
 	}

	/** Add the imported words to the dictionary for the indicated language */
	private void addWords(ArrayList<Language> langs)
	{
		String key="", code, keys[];
		int    hm = 0, len = words.size();
		Word word, nextWord, reversal;
		Reversal reversalObject;
		Language language;
		
		Language vernacular = langs.get(0);
		
		for (int w=0; w<len; w++)
		{ 
			word = words.get(w);
			if (w<len-1)  
			{
				nextWord = words.get(w+1);
				key = word.getKey();
				
				if (key.equals(nextWord.getKey()))
				{
					word.setKey(key + "_" + ++hm);
				}
				else
				{
					if (hm!=0)
						word.setKey(word.getKey() + "_" + ++hm);
						
					hm = 0;
				}
			}
			vernacular.addWord(word); 
		}
		
		for (int i=0; i<reversals.size(); i++)
		{
			reversalObject = reversals.get(i);
			code = reversalObject.getLanguage();
			word = reversalObject.getWord();
			key = reversalObject.getKey();
			
			if (key==null || key.length()==0) continue;
			keys = key.split(";");
			
			key  = word.getKey();
			
			
			for (int j=0; j<keys.length; j++)
			{
				language = null;
				for (int k=0; k<langs.size(); k++)
				{
					if (langs.get(k).getLanguageCode().equals(code))
					{
						language = langs.get(k);
						break;
					}
				}

				if (language!=null)
				{
					reversal = (Word)word.clone();
					reversal.setIndigenousData(languages.get(0), key);
					reversal.setKey(keys[j].trim());
					language.addWord(reversal);
				}
			}
				
		}   // end for reversals
		
		for (int i=1; i<langs.size(); i++ )
		{
			langs.get(i).sortWords();
		}
		dictionary.setLanguages(langs);
		
	}

	/** Create the language objects for the dictionary */
	private ArrayList<Language> createLanguages()
	{
		LanguageFont font;
		Language language;
		ArrayList<Language> dictionaryLanguages = new ArrayList<Language>();
		ArrayList<String> uniqueLanguages = getLanguages();
		String name;
		int index;
		
		for (String lang: uniqueLanguages)
		{
			index = lang.indexOf(' ');
			name = lang.substring(index + 1);
			lang = lang.substring(0,index);
			
			font = new LanguageFont("Times New Roman", 12, name);
			language = new Language(lang, font);
			dictionaryLanguages.add(language);
		}
		return dictionaryLanguages;
	}
	
	/** Replace the temporary language codes with those of actual languages */
	private void updateLanguageCodes()
	{
		String code;
		
		for (int i=0; i<languages.size(); i++)
		{
			if (languages.get(i).length()==0 && i<DEFAULT_LANGUAGES.length && counts[i]>0)
			{
				languages.set(i, DEFAULT_LANGUAGES[i].split(" ")[0]);
				names[i] = DEFAULT_LANGUAGES[i].split(" ")[1];
			}
		}
		
		for (Item reference: references)
		{
			reference.updateLanguageCodes(languages);
		}
		
		Reversal reversal;
		for (int i=0; i<reversals.size(); i++)
		{
			reversal = reversals.get(i);
			code = reversal.getLanguage();
			code = updateLanguageCodes(languages, code);
			reversal.setLanguage(code);
		}
	}
	
	/** Add a language to the list
	 * 
	 * @param language The standardized language code
	 * @param marker The SFM marker
	 */
	private void addLanguage(String language, String marker)
	{
		char type = marker.charAt(marker.length()-1);
		int index = SIL_CODES.indexOf(type);
		if (index>=0) 
		{
			counts[index]++;
		}

		if (language.length()==0) return;
		
		if (index>=0 && languages.get(index).length()==0)
			languages.set(index,  language);
		else if (!languages.contains(language))
			 languages.add(language);
	}
	
	/** Add row item and set the language
	 * 
	 * @param item The dictionary item object
	 * @param language The language code
	 * @param data To store into the object
	 * @param marker The SFM marker
	 */
	private void addRow(String type, String data, String language, String marker)
	{
		boolean same = lastType!=null && lastType.equalsIgnoreCase(type);
		lastType = type;
		
		if (type.equalsIgnoreCase("Example"))
		{
			if (marker.endsWith("v")) 
			{
				lastUnit = new Unit(type);
		        lastUnit.setSize(new Dimension(EXAMPLE_WIDGET_WIDTH, TEXT_HEIGHT));
				references.add(lastUnit);

				addLanguage(language, marker);
				lastUnit.setGloss(data);
				lastGroup.addRow(lastUnit);
			}
			else setIndigenousData(lastUnit, data, language, marker);
		}
		else if (type.equalsIgnoreCase("Gloss"))
		{
			if (!same)
			{
				if (sense==lastSense)
				{
    				addReference("Gloss", data, language, marker);
				}
				else
				{
					if (sense<0) wordSense();
					lastGroup= new Group("Definition");
			        lastGroup.setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));
			        lastGroup.getMedia().setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));

					clearData(lastGroup);
					lastUnit  = lastGroup.getMedia();
					references.add(lastUnit);
					addItem(rows, lastGroup, language, marker);
					clearData(word);
				}
			}
			if (reference!=null && reference.getTitle().equalsIgnoreCase("Gloss"))
				setIndigenousData(reference, data, language, marker);
			else 
				setIndigenousData(lastGroup, data, language, marker);
		}
		else if (type.equalsIgnoreCase("Definition"))
		{
			if (!same)
			{
				if (lastGroup!=null && lastGroup.getMedia().getCategory().equalsIgnoreCase("Definition"))
				{
					lastUnit = lastGroup.getMedia();
					reference = new Reference("Gloss");
					reference.setIndigenousData(lastUnit.getIndigenousData());
					reference.setTitle("Gloss");
					references.add(reference);
					lastGroup.addColumn(reference);
				}
				else if (lastGroup!=null && !lastGroup.getMedia().getCategory().equalsIgnoreCase("Definition"))
				{
					lastGroup = new Group("Definition");
			        lastGroup.setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));
			        lastGroup.getMedia().setSize(new Dimension(DEFINITION_WIDGET_WIDTH, WIDGET_HEIGHT));

					clearData(lastGroup);
					lastUnit  = lastGroup.getMedia();
					references.add(lastUnit);
					
					addItem(rows, lastGroup, language, marker);
					lastUnit.setGloss(data);
					clearData(word);
				}
			}
			setIndigenousData(lastGroup, data, language, marker);
		}
		
		if (sense<0) wordSense();
		lastSense = sense;
	}	// End of addRow()
	
	/** Add column item and set the language
	 * 
	 * @param item The dictionary item object
	 * @param language The language code
	 * @param marker The SFM marker
	 */
	private void addColumn(Item item, String language, String marker )
	{
		addItem(columns, item, language, marker);
	}

	/** Configure a reference object and add it as a column
	 * 
	 * @param title The type of reference field
	 * @param data The indigenous translation
	 * @param language The language code
	 * @param marker The SFM marker
	 * @param row true if a row, false if a column
	 */
	private void addReference(String title, String data, String language, String marker)
	{
		if (reference==null || !reference.getTitle().equals(title))
		{
			if (title.equals("Lexical Function"))
			{
				 lastUnit = new Unit(title);
				 lastUnit.setGloss(data);
				 reference = lastUnit;
			}
			else 
			{
				reference = new Reference(title);
				setIndigenousData(reference, data, language, marker);
			}
			
			references.add(reference);
			addColumn(reference, language, marker);
		}
		else  
		{
			addLanguage(language, marker);
			setIndigenousData(reference, data, language, marker);
		}
		
	}
	
	/** Add column or row item and set the language
	 * 
	 * @param Column or Row items for a word or definition
	 * @param item The dictionary item object
	 * @param language The language code
	 * @param marker The SFM marker
	 * @param english True if English font
	 */
	private void addItem(ArrayList<Item> items, Item item, String language, String marker)
	{
		addLanguage(language, marker);
		items.add(item);
	}

	/** Store indigenous data into a Reference or Unit object
	 * 
	 * @param item The Reference or Unit object
	 * @param data Data to store
	 * @param language The language code extracted from SFM
	 * @param marker The last character of the SFM marker (before the underscore)
	 */
	private void setIndigenousData(Item item, String data, String language, String marker)
	{
		String code = getLanguageCode(language, marker);
		
		lastLanguage = code;
		
		if (item instanceof Reference)
			((Reference)item).setIndigenousData(code, data);
		else if (item instanceof Group)
			((Group)item).setIndigenousData(code,  data);
		else if (item instanceof Unit)
			((Unit)item).setIndigenousData(code, data);
	}

	/** Get the language code to temporarily */
	private String getLanguageCode(String language, String marker)
	{
		String code = "~" + marker.substring(marker.length()-1);
		if (SIL_CODES.indexOf(code.charAt(code.length()-1)) < 0 || marker.length()==1) 
		{
			code += 'v';
			if (language.length()>0)
			    code = language;
		}
		return code;
	}
	
	/** Replace temporary SIL markers with actual language codes
	  * 
	  * @param languages Array list of actual language codes
	  * @param language code to be updated
	  * @return updated language code
	  */
	private String updateLanguageCodes(ArrayList<String> languages, String code)
	{
		if (!(code.charAt(0)=='~'))
			return code;
		
	    int index = SIL_CODES.indexOf(code.charAt(code.length()-1));
	    if (index>=0 && languages.get(index).length() == LANGUAGE_CODE_SIZE)
		   return languages.get(index);
	   
	    return "";
   }
	
   private void updateProgress()
   {
	   double percent = position * 100.0 / fileLength;
       String progress = String.format("Progress = %7.3f", percent);
       label.setText(progress);
   }

}		// End MDFConversion class
