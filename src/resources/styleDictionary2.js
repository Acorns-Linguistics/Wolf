/** Class to format the dictionary using an appropriate template 

 * @param document The DOM object containing dictionary information
 *
 */
function Format(sys, doc)
{
	var system = sys;
	var xmlDoc = doc;

	// Rows of array for word and definition sections
	var TRANSLATIONS = 0, COLUMNS = 1, ROWS = 2, MEDIA = 3, LIST_SIZE = 4;
	
	// Second index of languages array for formatting appropriately
	var LANGUAGE = 0, FACE = 1, SIZE = 2, FONT_LEN = 3;
	
	// Columns of languages array
	var  PHONETIC = 0, GLOSS = 1, ACTIVE = 2;

	// Enumeration of template field offsets
	var t =
	{
		W_HDR: { name: "word" }, 
		W_FLD: { name: "fields" },
		D_HDR: { name: "definition" },
		D_FLD: { name: "fields" }, 
		E_HDR: { name: "example" }, 
		E_FLD: { name: "fields" }, 
		C_HDR: { name: "comment" }, 
		C_FLD: { name: "fields" }
	};
 
	/** Format the dictionary in accordance to the selected template
	 * 
	 * @param document The DOM object containing dictionary information
	 * @param wordList The list of words to format
	 * @param gloss: The gloss language code
	 * @param active: The active languages for cross reference
	 * @return The HTML document to display the dictionary or null if no template selected
	 */
	this.toHTML = function(wordList, template, gloss, active)
	{
		var len = active.length + ACTIVE, i, words = wordList.length;
		var languages = new Array(FONT_LEN);
		for (i=0; i<FONT_LEN; i++)
			languages[i] = [];
		
		// Set the font faces and sizes
		var root = xmlDoc;
		var nodes = xmlDoc.getElementsByTagName("language");
		var langs = nodes.length;
		var element, glossLanguage = null;
		var lang, langName = "", n, a, w, i, word, build = new StringBuffer();
		
		// Set the language codes to format
		languages[LANGUAGE][PHONETIC] = gloss;
		languages[LANGUAGE][GLOSS] = gloss;

		if (!template) return null;

		for (i=0; i<len-ACTIVE; i++)
		{
			languages[LANGUAGE][ACTIVE+i] = active[i];
		}
		
		languages[FACE][PHONETIC] = getAttribute(root, "face");
		languages[SIZE][PHONETIC] = getAttribute(root, "size");
		
		for (n=0; n<langs; n++)
		{
			element = nodes[n];
			lang = getAttribute(element, "lang").toLowerCase();
			
			if (lang == gloss)
			{
				languages[FACE][GLOSS] = getAttribute(element, "face");
				languages[SIZE][GLOSS] = getAttribute(element, "size");
				langName = getAttribute(element, "name");
				glossLanguage = element;
			}
			else
			{
				for (a=0; a<len; a++)
				{
					if (lang == active[a])
					{
						languages[FACE][ACTIVE+a] = getAttribute(element, "face");
						languages[SIZE][ACTIVE+a] = getAttribute(element, "size");
					}
				}
			}
		}
		
		if (glossLanguage == null) return "";
		
		for (w=0; w<words; w++)
		{
			word = wordList[w];
			build.append("<div style=\"clear:both;\">");
			styleWord(build, w, word, template, languages);
			build.append("</div>");
		}
		
		return build.toString();
	}

	/** Format a word and its fields
	 * 
	 * @param build The stringBuffer object
	 * @param index The word index into the dictionary
	 * @param word The word to format
	 * @param languages The list of active languages
	 */
	var styleWord = function(build, index, word, template, languages)
	{
		// Get the columns, rows, and translations elements
		var elementLists = getElementLists(word);
		
		// style the word header
		var translations = elementLists[TRANSLATIONS];
		var node = translations.firstChild;
		var wordText = (node) ? node.nodeValue : "";
	    var showPicture = template[t.W_HDR.name].picture;
		var show = showPicture && showPicture == "true"; 
		var isField;
		
		var stem = wordText.replace(/_[0-9]+$/g, "");

		if (stem.length < wordText.length)
		{
			wordText = stem + "<sub>" + wordText.substring(stem.length+1) + "</sub>";
		}

		var format = template[t.W_HDR.name].format;
		var phonetic = getAttribute(translations, "phonetics");
		
		if (index>0) build.append("<br>\n");
		styleString(build, wordText, -1, "none", 
				" ", languages[FACE][GLOSS],
				languages[SIZE][GLOSS], format, elementLists[MEDIA], phonetic);
		
		if (phonetic.length>0 && (template[t.W_HDR.name].phonetic.toLowerCase() == "true"))
		{
			styleString(build, phonetic, -1, "none", 
					" ", languages[FACE][PHONETIC],
					languages[SIZE][PHONETIC], "italic", null, phonetic );
			build.append(" ");
		}
		
		isField = wordText.length + phonetic.length > 0;
			
		styleTranslations(build, translations, languages,isField);
		if (show)
		    stylePicture(build, elementLists[MEDIA]);
		styleFields(build, elementLists[COLUMNS], template[t.W_HDR.name][t.W_FLD.name], languages, -1, "", show);
		styleRows(build, elementLists[ROWS], template, languages);
	}

	/** Method to style the data in word or definition rows
	 * 
	 * @param build The StringBuffer object
	 * @param columns The <columns> element
	 * @param template the formatting information for each column's field type
	 * @param languages the formatting information for each column's field type
	 */
	var styleRows = function(build, rows, template, languages)
	{
		if (rows==null) return; 
		if (!template)  return;
		
		var nodes = rows.childNodes;
		var nLen = nodes.length;
		var definitions = [];
		var examples = [];
		var comments = [];
		
		var element, n;
		for (n=0; n<nLen; n++)
		{
			element = nodes[n];
			switch (element.tagName)
			{
				case "definition":
					definitions.push(element);
					break;
				case "example":
					examples.push(element);
					break;
				case "classification":
					comments.push(element);
					break;
			}
		}
		
		styleDefinitions(build, definitions, template, languages);
		
		if (template[t.E_HDR.name] && template[t.E_HDR.name].length != 0)
			styleCategory(build, examples, template[t.E_HDR.name], languages);
		if (template[t.C_HDR.name] && template[t.C_HDR.name].length != 0)
			styleCategory(build, comments, template[t.C_HDR.name], languages);
	}

	/** Format a definition, its fields, examples, and comments
	 * 
	 * @param build The stringBuiffer object
	 * @param definitions The list of definitions to format
	 * @param template The formatting template for the comment
	 * @param languages The list of active words
	 */
	var styleDefinitions = function(build, definitions, template, languages)
	{
		var definition, face, size, text, len = definitions.length, index = -1, i, temp;
		var node;
		
		// Index where definition should appear among the column output
		if (!template[t.D_HDR.name] || template[t.D_HDR.name].length == 0) return;
		
		var position = template[t.D_HDR.name].position;
		switch (position)
		{
			case "first":
				index = 1;
				break;
			case "second":
				index = 2;
				break;
			case "third":
				index = 3;
				break;
			case "fourth":
				index = 4;
				break;
		}
		
		var order = template[t.D_HDR.name].count;
		var rows = template[t.D_HDR.name].rows;
		var title = template[t.D_HDR.name].title;
		if (title) title = title.replace(/\n/g, "<br>\n");
		var format = template[t.D_HDR.name].format;
		var separator = template[t.D_HDR.name].separator;
		var showPicture = template[t.D_HDR.name].picture;
		var show = showPicture && showPicture=="true";
		var	isField;
		
		for (i=0; i<len; i++)
		{
			definition = definitions[i];
			if (title && title.length>0 && i==0) build.append(title);
			if (rows.toLowerCase() == "true") build.append("<br>\n");
			else build.append(" ");
			
			elementLists = getElementLists(definition);
			
			var translations = elementLists[TRANSLATIONS];
			var phonetics = getAttribute(translations, "phonetics");
			
			// Style the definition text
			temp = new StringBuffer();
			face = languages[FACE][GLOSS];
			size = languages[SIZE][GLOSS];
			node = translations.firstChild;
			
			if (node)
			{
				text = node.nodeValue;
				styleString(temp, text, i+1, order, separator, face,
						size, format, elementLists[MEDIA], phonetics );

				isField = text && text.length>0;
				styleTranslations(temp, elementLists[TRANSLATIONS], languages, isField);
			}
			
			if (show)
				stylePicture(temp, elementLists[MEDIA]);
			text = temp.toString();

			styleFields(build, elementLists[COLUMNS], template[t.D_HDR.name][t.D_FLD.name], languages, index, text, show);
			styleRows(build, elementLists[ROWS], template, languages);
			
			temp = new StringBuffer();
		}
	}

	/** Format a list of comments or examples associated with a word or definition
	 * 
	 * @param build The stringBuffer object
	 * @param categories The list of comments to format
	 * @param template The formatting template for the comment
	 * @param languages The list of active words
	 */
	var styleCategory = function(build, categories, template, languages)
	{
		var element, list, node, face, size, text, temp, i, tagl, phonetics; 
		
		var count = template.count;
		var rows = template.rows;
		var title = template.title;
		if (title) title = title.replace(/\n/g, "<br>\n");
		var format = template.format;
		var separator = template.separator;
		var show = template.picture;
		var len = categories.length;
		var node;
		var isField;
		
		for (i=0; i<len; i++)
		{
			//build.append("<br />"); 
			element = categories[i];
			face = getAttribute(element, "face");
			size = getAttribute(element, "size");
			node = element.firstChild;
			text = (node) ? node.nodeValue : "";
			tag = element.tagName.toLowerCase();
			if (tag == "classification")
			{
				face = languages[GLOSS][FACE];
				size = languages[GLOSS][SIZE];
			}
			else if (tag == "example")
			{
				temp = new StringBuffer();
				face = languages[FACE][GLOSS];
				size = languages[SIZE][GLOSS];
				list = getElementLists(element);
				if (!list[TRANSLATIONS]) continue;
				
				element = list[TRANSLATIONS];
				phonetics = getAttribute(element, "phonetics");
				text = "";
				node = element.firstChild;
				if (node!=null) text = node.nodeValue;
				if (text != null && text.length>0) 
				{
					styleString(temp, text, -1, "", separator, face, size, format, list[MEDIA], phonetics );
				}

				isField = text && text.length>0;
				styleTranslations(temp, list[TRANSLATIONS], languages,isField);
				if (show && show == "true") 
					stylePicture(temp, list[MEDIA]);
				text =  temp.toString();
			}
			
			if (title && title.length>0 && i==0) build.append(title);
			if (rows.toLowerCase() == "true") build.append("<br>\n");
			
			styleString(build, text, i+1, count, separator, face,
					size, format );
		}
	}

	/** Method to style the data in word or definition columns
	 * 
	 * @param build The StringBuffer object
	 * @param columns The <columns> element
	 * @param template the formatting information for each column's field type
	 * @param languages the formatting information for each column's field type
	 * @param index Offset to store the text (-1 for no text)
	 * @param text styled text to merge with column data (or null if none)
	 * @param show true if picture should be displayed
	 */
	var styleFields = function(build, columns, template, languages, index, text, show)
	{
		if (columns==null) return;
		
		var referenceList = 
		[
			"compare",           "encyclopedic info", "gloss", 
			"references", 		 "restrictions",      "usage",            
			"variants"
		];
		
		var elements = [], names = [], name, i, j, phonetics;
		var list = columns.getElementsByTagName("classification");
		var len = list.length;
		
		for (i=0; i<len; i++)
		{
			element = list[i];
			name = getAttribute(element, "title").toLowerCase();
			if (name.length==0) name = "comments";
			
			elements.push(element);
			names.push(name);
		}
		
		list = columns.getElementsByTagName("translations");
		len = list.length;
		for (i=0; i<len; i++)
		{
			element = list[i];
			name = getAttribute(element, "title").toLowerCase();
			if (name.length==0) name = "references";

			elements.push(element);
			names.push(name);
		}
		
		list = columns.getElementsByTagName("example");
		len = list.length;
		for (i=0; i<len; i++)
		{
			element = list[i];
			elements.push(element);
			names.push("lexical function");
		}
		
		list = columns.getElementsByTagName("ontology");
		len = list.length;
		name = "ontology";
		for (i=0; i<len; i++)
		{
			element = list[i];
			elements.push(element);
			names.push(name);
		}
		
		
		// Begin the formatting
		var field, title, format, separator, fieldText = "", output;
		var parent, child, abbrev, face, size, elementLists;
		var location;
		len = (template) ? template.length : 0;
		for (i=0; i<len; i++)
		{
			if (text && text.length>0 && i==index) { build.append(text); }
			
			field = template[i].field.toLowerCase();
			title = template[i].title;
			if (title) title = title.replace(/\n/g, "<br>\n");
			format = template[i].format;
			separator = template[i].separator;
			
			location = names.indexOf(field);
			if (location >= 0)
			{
				element = elements[location];
				elements.splice(location, 1);
				names.splice(location, 1);
				
				if (title && title.length > 0) 
				{
					build.append(title);
				}
				
				build.append(" ");
				var firstChild = element.firstChild;
				var	phonetics = getAttribute(element, "phonetics");
				
				if (firstChild) fieldText = firstChild.nodeValue;
				if (!fieldText) fieldText = "";

				var isReference = false;
				var isField = false;
				var rLen = referenceList.length;
				for (j=0; j<rLen; j++)
				{
					if (referenceList[j] == field.toLowerCase())
					{
						isReference = true;
						break;
					}
				}
				
				if (isReference)
				{
					if (fieldText) fieldText = fieldText.trim();
					if (fieldText.length>0)
					{
						styleString(build, 
							fieldText, 
							-1, "none", " ", languages[FACE][GLOSS],
							languages[SIZE][GLOSS], "normal" );
						isField = true;
					}
					styleTranslations(build, element, languages, isField);
				}
				else if (field.toLowerCase() == "lexical function")
				{
					elementLists = getElementLists(element);
					element = elementLists[TRANSLATIONS];
					fieldText = element.firstChild.nodeValue;
					if (show) stylePicture(build, elementLists[MEDIA]);
					
					if (fieldText && fieldText.length>0)
					{
						styleString(build, 
							fieldText, 
							-1, "none", " ", languages[FACE][GLOSS],
							languages[SIZE][GLOSS], "normal", elementLists[MEDIA] );
						isField = true;
					}
					styleTranslations(build, element, languages, isField);
				}
				else if (field.toLowerCase() == "ontology")
				{
					abbrev = getAttribute(element, "abbreviation");
					parent = getAttribute(element, "parent");
					child  = getAttribute(element, "child");
					if (!abbrev || abbrev.length==0 || abbrev == child + parent)
						 output = child;
					else output = abbrev;
					
					if (child.length + fieldText.length == 0) 
						continue;
										
					if (fieldText.length>0) output += ": ";
					output += fieldText;

					styleString(build, 
							output, -1, "none", separator, 
							languages[FACE][GLOSS],
							languages[SIZE][GLOSS], format, null, phonetics );
				}
				else
				{
					face = size = "";
					firstWord = fieldText.split(" ")[0];
					if (firstWord.charAt(firstWord.length-1)!=':')
					{
						face = getAttribute(element, "face");
						size = getAttribute(element, "size");
					}
					
					if (field.toLowerCase() == "table")
					{
					   build.append("<br><pre>" + fieldText + "</pre>");
					}
					else
					{
					   styleString(build, fieldText,  -1, "none", 
						   separator, face, size, format, null,  phonetics);
					}
				}
			}   // End of processing field
		}
		
		if (text!=null && text.length>0 && i<=index) build.append(text);
		
	}   // End of styleFields method

	/** Format a group of translation elements
	 * 
	 * @param build The string builder object
	 * @param element The <translations> element to begin searching
	 * @param languages The languages to format
	 * @param true if gloss field exists
	 */
	var styleTranslations = function(build, element, languages,isField)
	{
		var lang, text, first = true, t, i;
		var translations = element.getElementsByTagName("translation");
		var phonetics = getAttribute(element, "phonetics");
		var len = translations.length, langs;
		var shouldWrap = len>1 || shouldWrap
		var node;

		if (!element) return;
		
		for (t=0; t<len; t++)
		{
			translation = translations[t];
			lang = getAttribute(translation, "lang");
			langs = languages[LANGUAGE].length;
			
			for (i=GLOSS; i<langs; i++)
			{
				if (lang.toLowerCase() == languages[LANGUAGE][i].toLowerCase())
				{
					if (first && shouldWrap) 
					{
					   build.append("(");
					   first = false;
					}
					else if (!first) 
						build.append(", ");
					
					node = translation.firstChild;
					text = (node) ? node.nodeValue : "";
					if (shouldWrap)
						build.append(lang + " ");
					styleString(build, text, 
							-1, "none", "", languages[FACE][i],
							languages[SIZE][i], "normal", null, phonetics );
				}
			}
		}
		if (!first && shouldWrap) build.append(") ");

	}	// End of formatTranslations
	
	/** Style a picture element
	 * @param build String builder object to append
	 * @param element containing picture element
	 */
	var stylePicture = function(build, media)
	{
		var picture;
		
		if (!media) return;
		
		picture = getAttribute(media, "picture");
		if (!picture || picture.length == 0) return;
		
		build.append("<img src=\"" + system.getFileDirectory() + picture 
				+ "\" class=\"picture\" height=\"96px\" width=\"auto\" alt = \"???\">");
	}
	
	/** Appropriately style a string
	 * 
	 * @param build String builder object to append
	 * @param text The text to style
	 * @param count The index for this text component
	 * @param order # #. #) i i. i) I I. I) a a. a) A A. A) or none
	 * @param separator () [] <> {} "" '' : ; , - | or none
	 * @param fontFace font to style or null
	 * @param fontSize font size in pts or null
	 * @param format normal, bold, italic, gold, blue, green, red, magenta, black
	 * @param media element containing media (if it exists)
	 * @param phonetics text (if it exists)
	 */
	var styleString = function( build, text, count, order
						, separator, fontFace, fontSize, format, media, phonetics)
	{
		var audio = "", video = "", mediaText;
		
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
		if (!text || text.length == 0) return;
		
		// Format the first part of the separator
		if (separator.length == 2)
		{
			build.append(separator.charAt(0));
		}
			
		// Appropriately style the text
		build.append("<span ");
		
		if (media)
		{
			audio = getAttribute(media, "audio");
			video = getAttribute(media, "video");
			if (!audio) audio = "";
			if (!video) video = "";
			mediaText = audio + "..." + video;
			if (audio.length>0 || video.length>0)
			{
				build.append("name=\"media\" title=\"" + mediaText + "\" ");
			}
		}
		
		if (audio.length==0 && video.length==0 && phonetics && phonetics.length>0)
		{
			build.append("name=\"phonetics\" title=\"" + phonetics + "\" ");
		}
		
		build.append("style = \"");
		
		
		if (fontFace && fontFace.length>0)
		{
			build.append("font-family:" + fontFace + ", serif;");
		}
		
		if (fontSize && fontSize.length > 0)
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
		if (separator.length == 2)
		{
			build.append(separator.charAt(1));
			build.append(' ');
		}
		else if (separator.length == 1)
			build.append(separator.charAt(0));
		else if (separator.toLowerCase() === "none")
			build.append(' ');
	}
	
	/** Get and return attribute; return "" if not found */
	var getAttribute = function(element, attribute)
	{
		att = element.getAttribute(attribute);
		if (!att) att = "";
		return att;
	}

	/** Find translation, column, and row lists from definition or word tag
	 * 
	 * @param element The word, definition, or example element
	 * @return Array of translation, column, and row list tags
	 */
	var getElementLists = function(element)
	{
		var child, elementLists = new Array(LIST_SIZE), list = element.childNodes;
		var i, tag, len = list.length;
		
		if (!element.hasChildNodes()) return elementLists;

		for (i=0; i<len; i++)
		{
			child = list[i];
			tag = child.tagName;
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
	var romanNumerals = function(count, upper)
	{
		var roman="", digit=count, x, i;
		var magnitude = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1];
		var symbol = ["m","cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"];
		if (upper)
		    symbol = ["M","CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"];
			
		// if out of range, don't convert
		if (count <= 0 || count >= 4000) return "" + count; 

		for(x=0; count>0; x++)
		{
			digit=count/magnitude[x];
			for(i=1; i<=digit; i++)
			{
				roman=roman + symbol[x];
			}
			count=count % magnitude[x];
		}
		return roman;
	}

	/** Convert count to upper or lower case alpha numerals
	 * 
	 * @param count integer to convert
	 * @param upper true if convert to upper case
	 * @return converted integer as a string
	 */
	var alphaNumerals = function(count, upper)
	{
		var LETTERS = 26, result = "", letter;
	
		if (count<=0) return "" + count;
	  
		var base = (upper) ? 'A' : 'a';
		if (count==1) return "" + base;
	  
		count--;
		while (count!=0)
		{  
		   letter = (count%LETTERS) + base.charCodeAt();
		   result = String.fromCharCode(letter);;
		   count = Math.floor(count/LETTERS);
		}
		return result;
	}
	
	/** Enables fast string concatenation using append() and toString() methods */
	function StringBuffer()
	{
	   var buffer = []; 
	   
	   this.append   = function(string) { buffer.push(string); };
	   this.toString = function() { return buffer.join(""); };
	}
	
}	// End of Format class


