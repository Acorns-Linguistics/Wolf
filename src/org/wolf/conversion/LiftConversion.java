package org.wolf.conversion;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.acorns.language.LanguageFont;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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

public class LiftConversion implements Constants
{
	ArrayList<Language> languages; 
	DictionaryData dictionary;
	Element root;
	Language defaultLanguage;
	String directory;
	
	public LiftConversion(Element rootNode, DictionaryData dictionary, File file)
	{
		this.dictionary = dictionary;
		this.root = rootNode;
		
		this.directory  = file.getParent();
		if (directory == null) directory = "";
		else directory += File.separator;
		
        Language[] languageArray = dictionary.getLanguages();
        languages = new ArrayList<Language>();
        Collections.addAll(languages, languageArray);
        
        // Add all of the languages
        addLanguages("range-element");
        addLanguages("gloss");
    	addLanguages("form");
        
        int size = languages.size();
        if (size == 0) 
        	return; 
        
        languageArray = languages.toArray(new Language[languages.size()]);
		dictionary.setLanguages(languageArray);
        dictionary.setActiveLanguages();
	}
	
	public String convert() throws IOException
	{
		if (languages.size()==0)
			return "No languages found in imported dictionary";
		
		NodeList entryNodes = root.getElementsByTagName("entry");
		NodeList children;
		String homograph;
		
		for (int e=0; e<entryNodes.getLength(); e++)
		{
			Element entry = (Element)(entryNodes.item(e));
			homograph = entry.getAttribute("order"); // same spelling, different pronunciation
			children = entry.getChildNodes();
			
			Word word = null;
			Group definition = null;
			Item item;
			Reference reference;
			String type, value, code;
			Ontology ontology = null;
			Node node;
			NodeList annotationNodes;
			Unit media;
			
			for (int c=0; c<children.getLength(); c++)
			{
				node = children.item(c);
				if (!(node instanceof Element))
					continue;
				
				Element child = (Element)(node);
				String nodeName = child.getNodeName();
				switch (nodeName)
				{
					case "lexical-unit":
						if (word != null)
						{
							if (ontology != null)
							{
								if (definition == null)
									 word.addColumn(ontology);
								else definition.addColumn(ontology);
								
								ontology = null;
							}

							defaultLanguage.mergeWord(word);
						}
						
						word = (Word)processTranslationCells(child, "Word");
						if (homograph.length()>0)
						{
							String key = word.getKey();
							key += "_" + homograph;
							word.setKey(key);
						}
						
						definition = null;
						
						// Process embedded annotations
						annotationNodes = child.getElementsByTagName("annotation");
						for (int a=0; a<annotationNodes.getLength(); a++) 
						{
							Node annotationNode = annotationNodes.item(a);
							Element annotationElement = (Element)annotationNode;
							if (definition != null)
							{
								processAnnotation(annotationElement, definition);
							}
							else processAnnotation(annotationElement, word);
						}
						break;
						
					case "citation":
						type = "Encyclopedic Info";
						item = (Unit)processTranslationCells(child, type);
						if (item!=null)
						{
							word.addColumn(item);
						}
						break;
					case "pronunciation":
					case "illustration":
						media = word.getMedia();
						item = processMedia(child, media);
						if (item!=null)
							word.addColumn(item);
						break;
					case "variant":
						String trait = "";
						type = "Variants";
						
						if (word == null)
						{
							word = (Word)processTranslationCells(child, "Word");
							item = processField(child);
							if (item != null)
								word.addColumn(item);
							break;
						}

						reference = (Reference)processTranslationCells(child, type);
						NodeList variantNodes;
						value = "";
						if (reference == null)
						{
							variantNodes = child.getElementsByTagName("trait");
							if (variantNodes.getLength()>0)
							{
								node = variantNodes.item(0);
								Element element = (Element)node;
								value = element.getAttribute("value") + ": ";
							}
							
							value += child.getAttribute("ref");
							item = new Comment("Subentry", value);
							word.addColumn(item);
						}
						word.addColumn(reference);
						variantNodes = child.getChildNodes();
						for (int v=0; v<variantNodes.getLength(); v++) 
						{
							node = variantNodes.item(v);
							if (!(node instanceof Element))
								continue;
							
							Element variantElement = (Element)(node);
							String variantTag = variantElement.getTagName();
							switch (variantTag)
							{
								case "relation":
									item = processRelation(variantElement);
									word.addColumn(item);
									break;
								case "trait":
									String name = variantElement.getAttribute("name");
									value = variantElement.getAttribute("value");
									if (trait.length()>0)
										trait += ", ";
									trait += name + "=" + value;
									break;
								case "form":
									if (trait.length()>0)
									{
										code = getLanguageCode(variantElement.getAttribute("lang"));
										if (code.length()>0)
										{
											value = variantElement.getTextContent().trim();
											reference.setIndigenousData(code, value + ": " + trait);
											trait = "";
										}
									}
									break;
								case "field":
									item = processField(variantElement);
									if (item != null)
										word.addColumn(item);
									break;
							}
						}
						break;
					case "note":
						item = processNote(child);
						word.addColumn(item);
						break;
					case "etymology":
						item = processEtymology(child);
						word.addColumn(item);
						break;
					case "relation":
						item = processRelation(child);
						word.addColumn(item);
						break;
					case "annotation":
						processAnnotation(child, word);
						break;
					case "sense":
						ArrayList<Element> queue = new ArrayList<Element>();
						queue.add(child);
						
						while (!queue.isEmpty())
						{
							child = queue.remove(0);
							definition = null;
							NodeList senseNodes = child.getChildNodes();
							for (int s=0; s<senseNodes.getLength(); s++) 
							{
								node = senseNodes.item(s);
								if (!(node instanceof Element))
									continue;
								
								Element senseElement = (Element)node;
								String senseTag = senseElement.getTagName();
								switch (senseTag)
								{
									case "grammatical-info":
										value = senseElement.getAttribute("value");
										ontology = new Ontology("", "", "", value, ONTOLOGY_DATA);
										break;
									case "gloss":
										String lang = senseElement.getAttribute("lang");
										String text = senseElement.getTextContent().trim().replaceAll("\\s+"," ");
										if (definition == null && word.getKey().length()==0)
										{
											word.setKey(text);
											break;
										}
										
										if (definition == null && getLanguageCode(lang) != defaultLanguage.getLanguageCode())
										{
											media = word.getMedia();
											String indigenous = media.getIndigenousData().get(lang);
											if (indigenous != null)
											{
												text = indigenous + ", " + text;
											}
											word.getMedia().setIndigenousData(lang, text);
											break;
										}
										
										reference = new Reference("Gloss");
										reference.setIndigenousData(lang, text);
										if (definition == null)
											 word.addColumn(reference);
										else definition.addColumn(reference);
										break;
									case "definition":
										definition = (Group)processTranslationCells(senseElement, "Definition");
										word.addRow(definition);
	
										if (ontology != null)
										{
											definition.addColumn(ontology);
											ontology = null;
										}
										break;
									case "relation":
										item = processRelation(senseElement);
										if (definition == null)
											 word.addColumn(item);
										else definition.addColumn(item);
										break;
									case "note":
										item = processNote(senseElement);
										if (definition == null)
											 word.addColumn(item);
										else definition.addColumn(item);
										break;
									case "example":
										Unit example = (Unit)processTranslationCells(senseElement, "Example");
										if (definition == null)
											 word.addRow(example);
										else definition.addRow(example);
										processMedia(child, example);
										break;
									case "reversal":
										reference = (Reference)processTranslationCells(senseElement, "Reversals");
										if (reference!=null)
										{
											if (definition == null)
												 word.addColumn(reference);
											else definition.addColumn(reference);
											
											value = "";
											NodeList mainList = senseElement.getElementsByTagName("main");
											if (mainList.getLength()>0)
											{
												Node mainNode = mainList.item(0);
												value = mainNode.getTextContent().trim();
											}
									
											for (Language reverseLanguage: languages)
											{
												
												code = reverseLanguage.getLanguageCode();
												String data = reference.getIndigenousData().get(code);
												if (data==null || data.length()==0)
													continue;
												
												String[] split = data.split(", ");
												for (int d=0; d<split.length; d++)
												{
													data = split[d];
													if (data.length()==0)
														continue;
													
													if (!code.equals(defaultLanguage.getLanguageCode()))
													{
														String wordKey = word.getKey();
														if (wordKey.length()>0)
														{
															Word reverseWord = new Word(data);
															if (value.length()>0)
															{
																item = processComment("Main Entry", value);
																reverseWord.addColumn(item);
																Word mainWord = new Word(value);
																reverseLanguage.mergeWord(mainWord);
															}
															reverseWord.setIndigenousData(defaultLanguage, wordKey);
															reverseLanguage.mergeWord(reverseWord);
														}
													}
												}
											}
										}
										break;
									case "pronunciation":
									case "illustration":
										if (definition == null)
											break;
										
										media = definition.getMedia();
										item = processMedia(senseElement, media);
										if (item != null) 
										{
											definition.addColumn(item);
										}
										break;
									case "annotation":
										if (definition == null)
											 processAnnotation(senseElement, definition);
										else processAnnotation(senseElement, word);
										break;
									case "trait":
										String name = senseElement.getAttribute("name");
										value = senseElement.getAttribute("value");
										item = processComment(name, value);
										if (definition == null)
											 word.addColumn(item);
										else definition.addColumn(item);
										break;
									case "subsense":
										queue.add(senseElement);
										break;
									default:
										return "Illegal sense element " + senseTag;
								}	// end of all sense cases 
							}		// end process all sense nodes
							
							// Process embedded annotations
							annotationNodes = child.getElementsByTagName("annotation");
							for (int a=0; a<annotationNodes.getLength(); a++) 
							{
								Node annotationNode = annotationNodes.item(a);
								Element annotationElement = (Element)annotationNode;
								if (definition == null)
								{
									processAnnotation(annotationElement, definition);
								}
								else processAnnotation(annotationElement, word);
							}
						}		// End of processing sense and subsense elements

						break;
					default:
						return "Illegal Entry element " + nodeName;
				}
			}	// End processing entry elements
			
			if (word != null)
			{
				if (ontology != null)
				{
					if (definition == null)
						 word.addColumn(ontology);
					else definition.addColumn(ontology);
					
					ontology = null;
				}
				
				defaultLanguage.mergeWord(word);
			}
		} 
		return "";
	}
	
	/** Process Note element in XML
	 * 
	 * @param element The relation element to process
	 * @return Appropriate dictionary object
	 */
	private  Item processNote(Element element)
	{
		String type = element.getAttribute("type");
		
		ArrayList<String[]> formData = processTranslation(element, "form", false);
		ArrayList<String[]> glossData = processTranslation(element, "gloss", false);

		type = convertType(type);
		
		String[] data = mergeTranslations(glossData, formData, false);
		String text = "";
		int count = 0;
		for (String entry: data)
		{
			if (entry.length()>0)
			{
				text = entry;
				count++;
			}
		}
		
		Item item;
		if (count>1)
		{
			item = (Reference)processTranslationCells(element, type);
		}
		else
		{
			item = new Comment(type, text);
		}
		return item;
	}
	
	/** Process Field element in XML
	 * 
	 * @param element The relation element to process
	 * @return Appropriate dictionary object
	 */
	private Item processField(Element element)
	{
		Item item;
		String value;

		NodeList fieldNodes = element.getElementsByTagName("annotation");
		if (fieldNodes.getLength()==0)
			return null;
		
		Element fieldElement = (Element)fieldNodes.item(0);
		value = fieldElement.getAttribute("value");
		item = new Comment("Comment", value);
		return item;
	}
	
	/** Process Etymology element in XML
	 * 
	 * @param element The relation element to process
	 * @return Appropriate dictionary object
	 */
	private Item processEtymology(Element element)
	{
		ArrayList<String[]> gloss = processTranslation(element, "gloss", false);
		ArrayList<String[]> form = processTranslation(element, "form", false);
		
		String text = element.getAttribute("source") + ": ";
		
		for (String[] data: form)
		{
			text += data[1] + " ";
		}
		
		if (gloss.size()>0)
			text += " -> ";
		
		for (String[] data: gloss)
		{
			text += data[0] + " : " + data[1] + " ";
		}
		
		Item item = new Comment("Etymology", text);
		return item;
	}

	/** Process Relation element in XML
	 * 
	 * @param element The relation element to process
	 * @return Appropriate dictionary object
	 */
	private Item processRelation(Element element)
	{
		String type = element.getAttribute("type");
		if (type.length()==0)
			type = "Comment";
		String value = element.getAttribute("ref");
		
		ArrayList<String[]> formData = processTranslation(element, "form", false);
		ArrayList<String[]> glossData = processTranslation(element, "gloss", false);
		
		Item item;
		if (value.length()>0)
		{
			String[] text = mergeTranslations(glossData, formData, true);
			if (text[0].length()==0)
			{
				if (type.equals("subentry"))
					 item = processComment("Subentry", value);
				else item = processComment("Subentry", type + ": " + value);
			}
			else item = processComment(type, value + text[0]);
			return item;
		}
		
		
		if (value.length()==0)
			value = element.getTextContent().trim();
		
		item = (Reference)processTranslationCells(element, "References");
		if (item==null)
		{
			item = processComment(type, value);
		}
		return item;
	}
	
	/** Process various types of comment elements 
	 * 
	 * @param title The type of dictionry object
	 * @param value to store in the dictionary object
	 * @return The relation Dictionary object
	 */
	private Item processComment(String title, String value)
	{
		title = convertType(title);
		Comment comment = new Comment(title, value);
		return comment;
	}

	/** Add annotation to a word or definition
	 * 
	 * @param element XML Annotation element
	 * @param group The dictionary word or definition object
	 */
	private void processAnnotation(Element element, Group group)
	{
		ArrayList<Item> items = group.getColumns();
		Comment table = null;
		boolean found = false;
		for (Item item: items)
		{
			if (item.getTitle().equals("Annotation"))
			{
				if (item instanceof Comment)
				{
					table = (Comment)item;
					found = true;
					break;
				}
			}
		}
		
		if (table == null)
		{
			table = new Comment("Annotation", "");
		}
		
		String name = element.getAttribute("name");
		String value = element.getAttribute("value");
		String when = element.getAttribute("when");
		
		String who = element.getAttribute("who");
		
		String content = element.getTextContent().trim();
		
		String text = "";
		if (name.length()>0)
			text += "Name: " + name + " ";
		
		if (value.length()>0)
			text += "Value: " + value + " ";
		
		if (when.length()>0)
			text += "When: " + when + " ";
		
		if (who.length()>0)
			text += "Who: " + who;
		
		if (content.length()>0)
			text += "\n" + content;

		String comment = table.getComment();
		if (comment.length()>0)
			comment += "\n\n";
		
		text = comment + text;
		table.setComment(text);

		if (!found) group.addColumn(table);
	}
	
	/** Configure audio or picture into dictionary object
	 * 
	 * @param element The parent which may have a descendant media element.
	 * @param media A dictionary object containing multi-media assets.
	 * @param Item reference or translation pertaining to the media
	 */
	private Item processMedia(Element element, Unit media)
	{
		NodeList nodes = element.getChildNodes();
		int len = nodes.getLength();
		Node node;
		String name;
		
		for (int n=0; n<len; n++)
		{
			node = nodes.item(n);
			if (!(node instanceof Element))
				continue;
			
			Element child = (Element)node;
			name = child.getTagName();
			String url = child.getAttribute("href");
			if (url.length()==0)
				continue;
			
			url = directory + url;
			if (name.equals("media"))
			{
				media.insertPicture(url);
			}
			else if (name.equals("phonetic"))
			{
				media.insertAudio(url);
			}
		}
		
		Item item = processRelation(element);
		return item;
	}
		
	/** Merge translation data from XML into a single string
	 * 
	 * @param glossData Gloss objects
	 * @param formData form objects
	 * @return merged array of text for exch language separated by commas
	 */
	private String[] mergeTranslations(ArrayList<String[]> glossData, ArrayList<String[]> formData, boolean single)
	{
		String[] text = new String[languages.size()];
		for (int i=0; i<text.length; i++)
			text[i] = "";
		
		int index = 0;
		for (String[] data: glossData)
		{
			if (!single)
			{
				index = findLanguageIndex(data[0]);
				if (index == -1) continue;
			}
			
			if (text[index].length()>0)
				text[index] += ", ";
			
			text[index] += data[1];
		}
		
		for (String[] data: formData)
		{
			if (!single)
			{
				index = findLanguageIndex(data[0]);
				if (index == -1) continue;
			}
			
			if (text[index].length()>0)
				text[index] += ", ";
			
			text[index] += data[1];
		}
		
		return text;
	}

	/** Configure translation cells containing appropriate data
	 * 
	 * @param cell The cell in the xml document
	 * @param fieldType The type of cell object
	 * @return Dictionary cell with languages configured (or null if no language data)
	 */
	private Item processTranslationCells(Element cell, String fieldType)
	{
		ArrayList<String[]> formData = processTranslation(cell, "form", false);
		ArrayList<String[]> glossData = processTranslation(cell, "gloss", false);
		ArrayList<String[]> phoneticData = processTranslation(cell, "form", true);
		
		String phonetics = "";
		if (!phoneticData.isEmpty())
		{
			phonetics = phoneticData.get(0)[1];
		}
		
		// Configure the gloss language
		String[] gloss = null;
		if (glossData.isEmpty())
		{
			if (formData.isEmpty())
			{
				return null;
			}
			else
			{
				gloss = formData.get(0);
				if (fieldType.equals("Word"))
				{
					String cellName = cell.getNodeName();
					if (cellName.equals("lexical-unit") || cellName.equals("variant") )
					{
					   defaultLanguage = findLanguage(gloss[0]);
					}					

					String lang = getLanguageCode(gloss[0]);
					String defaultCode = defaultLanguage.getLanguageCode();
					if (lang.equals(defaultCode))
						formData.remove(0);
					else gloss = null;
				}
				else if (fieldType.equals("Definition"))
				{
					String lang = getLanguageCode(gloss[0]);
					String defaultCode = defaultLanguage.getLanguageCode();
					if (lang.equals(defaultCode))
						formData.remove(0);
					else gloss = null;
				}
			}
		}
		else gloss = glossData.get(0);		

		// Create appropriate dictionary object with translation data.
		Item result;
		switch (fieldType)
		{
			case "Word":
				Word word = new Word();
				result = word;
				configureLanguageData(gloss, formData, result);
				word.getMedia().getTranslationData().setPhonetics(phonetics);
				break;

			case "Definition":
				Group definition = new Group("Definition");
				result = definition;
				configureLanguageData(gloss, formData, result);
				definition.getMedia().getTranslationData().setPhonetics(phonetics);
				configureCellSize(definition, DEFINITION_WIDGET_WIDTH);
				configureCellSize(definition.getMedia(), DEFINITION_WIDGET_WIDTH);
				break;
				
			case "Example":
				Unit example = new Unit("Example");
				result = example;
				configureLanguageData(gloss, formData, result);
				example.getTranslationData().setPhonetics(phonetics);
				configureCellSize(example, ROW_WIDGET_WIDTH);
				break;
				
			default:
				Reference reference = new Reference();
				result = reference;
				reference.setTitle(fieldType);
				configureLanguageData(gloss, formData, reference);
				break;
		}
		return result;
	}
	
	/** Configure width of wider dictionary objects 
	 * 
	 * @param item Object to configure
	 * @param width desired width
	 */
	private void configureCellSize(Item item, int width)
	{
		Dimension size = item.getSize();
		size.height = WIDGET_HEIGHT;
		if (width > size.width)
		{
			size.width = width;
		}
		item.setSize(size);
	}
	
	/** Configure dictionary component with language data.
	 * 
	 * @param gloss The gloss language if specified
	 * @param indigenous The indigenous data
	 * @param item Item to configure with language data
	 */
	private void configureLanguageData(String[] gloss, ArrayList<String[]> indigenous, Item item)
	{
		if (gloss !=null)
		{
			String key = gloss[1];
			if (item instanceof Word)
			{
				Word word = (Word)item;
				word.setKey(key);
			}
			else if (item instanceof Group)
			{
				Group group = (Group)item;
				group.getMedia().getTranslationData().setGloss(key);
			}
		}
		
		String 	langCode = defaultLanguage.getLanguageCode();
		for (int i=0; i<indigenous.size(); i++)
		{
			String[] value = indigenous.get(i);
			String code = getLanguageCode(value[0]);
			
			Hashtable<String, String> hash = item.getIndigenousData();
			String data = hash.get(code);
			if (data!=null && data.length()>0)
				data += ", " + value[1];
			else data = value[1];
				
			item.setIndigenousData(code, data);
			if (code.equals(langCode) && item instanceof Unit)
			{
				Unit unit = (Unit) item;
				unit.setGloss(data);
			}
		}
	}
	
	/** Process element for language and text
	 * 
	 * @param element The element with 0 or more form or .
	 * @param elementType The type of sub-element ('form' or 'gloss')
	 * @param phonetics true to process phonetic translations.
	 * @return ArrayList of String arrays: [0] = language code, [1] = text content.
	 * 
	 */
	private ArrayList<String[]> processTranslation(Element element, String elementType, boolean phonetics)
	{
		ArrayList<String[]> list = new ArrayList<String[]>();
		NodeList nodes = element.getElementsByTagName(elementType);
		if (nodes.getLength()==0)
		{
			return list;
		}
		
		Element form;
		String lang, text, parent;
		boolean parentType;
		for (int e=0; e<nodes.getLength(); e++)
		{
			form = (Element)(nodes.item(e));
			lang = form.getAttribute("lang");
			text = form.getTextContent();
			text = text.trim();
			text = text.replaceAll("\\s+"," ");
			
			parent = form.getParentNode().getNodeName();
			parentType  = parent.equalsIgnoreCase("phonetic");
			if (parentType != phonetics)
				continue;
			
			list.add(new String[] {lang, text});
		}
		return list;
	}
	
	/** Add a languages to the list that don't exist
	 * 
	 * @param elementType The element type that could have a 'lang' attribute.
	 * @return default language
	 */
	private void addLanguages(String elementType)
	{
    	String fontName = "calibri"; // Font and size to use
    	int fontSize = 12;
    	String name, lang;

    	// Add all of the languages
        NodeList nodes = root.getElementsByTagName(elementType);
        Element node;
        for (int f=0; f<nodes.getLength(); f++)
		{
        	node = (Element)(nodes.item(f));
        	
        	if (elementType.equals("range-element")) 
        	{
        		NodeList children = node.getChildNodes();
        		Element child;
  			    name= lang = "";
        		for (int c=0; c<children.getLength(); c++)
        		{
        		   Node childNode = children.item(c);
        		   if (childNode instanceof Element)
        		   {
        			   child = (Element)childNode;
        			   switch (child.getTagName())
        			   {
	        			   case "label":
	        				   name = child.getTextContent().trim();
	        				   break;
	        			   case "abbrev":
	        				   lang = child.getTextContent().trim();
	        				   break;
        			   }
        		   }
        		}
        		
        		if (name.length()>0 && lang.length()>0)
    		    {
    	        	LanguageFont font = new LanguageFont(fontName, fontSize, name);
    				Language language = new Language(lang, font);
    				if (!languages.contains(language))
    				{
    	    			languages.add(language);
    	    		}
    				if (defaultLanguage==null)
    					defaultLanguage = language;
    		    }
        		
        		continue;
        	}
        	
    		name = node.getAttribute("lang");
    		lang = getLanguageCode(name);
    		if (lang.length()==0)
    			continue;
    		
        	LanguageFont font = new LanguageFont(fontName, fontSize, name);
			Language language = new Language(lang, font);
			if (!languages.contains(language))
			{
    			languages.add(language);
    		}
			if (defaultLanguage==null)
				defaultLanguage = language;
		}	// end for
	}		// end of addLanguage method

	/** Get language code from full language name 
	 * 
	 * @param name Full language name
	 * @return Language code
	 */
	private String getLanguageCode(String name)
	{
		String[] langDecode = name.split("-");
		if (langDecode.length==0)
			return "";;
		
		String lang = langDecode[0];
		
		if (langDecode.length >= 4)
		{
			String variant = langDecode[3];
			if (variant.length() >1)
			{
				if (variant.length()>2)
					variant = variant.substring(0,1);
			
				lang = lang + "/" + variant;
			}
		}
		return lang;
	}

	/** Find the language with the designate language code
	 * 
	 * @param name Name of the language
	 * @return Language object or null if not found
	 */
	private Language findLanguage(String name)
	{
		String code = getLanguageCode(name);
		for (Language language: languages)
		{
			if (language.getLanguageCode().equals(code))
				return language;
		}
		return null;
	}

	/** Get index to a particular language 
	 * 
	 * @param name Name of language to surch for
	 * @return Index of language or -1 if not found
	 */
	private int findLanguageIndex(String name)
	{
		String code = getLanguageCode(name);
		Language language;
		for (int i=0; i<languages.size(); i++)
		{
			language = languages.get(i);
			if (language.getLanguageCode().equals(code))
				return i;
		}
		return -2;
	}
	
	/** Convert type from lift to wolf
	 * 
	 * @param type Lift type
	 * @return Wolf type
	 */
	private String convertType(String type)
	{
		if (type.length()>0)
			type = type.substring(0, 1).toUpperCase() + type.substring(1);

		type = type.replace("BaseForm", "Main Entry");
		type = type.replace("Encyclopedic", "Encyclopedic Info");
		type = type.replace("Shared semantic core", "Subentry");
		type = type.replace("Source", "Refer To");
		type = type.replace("Semantic-domain", "Categories");
		return type;
	}

}	// end of LiftConversion class
