/* JavaScript to process Wolf lesson types in a browser window, using parsed XML data */
		
var system = null;
var timer = null;
var resizable = true;

var WOLF_VIEW = "Wolf View ";

// Reload the window when it is resized
var reloadWindow = function()
{
	// Don't allow reloading while components are being drawn
	if (!resizable) return;

	// Stop multiple reloading calls and any lesson using the global timer.
	clearTimeout(timer); 
	system.disablePopups();

	// Restart the timer.
	timer = setTimeout(parseXML,500);
	if (window.audioTools)
		window.audioTools.outLog("resizing");
}

// Process error messages that could occur when the lesson fails due to an exception
var errorMessage = function(name, line1, line2)
{
	var h1 = document.createElement("h1");
	h1.style.textAlign = "center";
	h1.style.fontSize = "24px";
	var text = "Application " + name + " is not compatible with this browser<br /></h1>";
	h1.innerHTML = text;
	
	var displayArea = document.getElementById("dictionaryDisplay");
	displayArea.innerHTML = "";
	displayArea.appendChild(h1);
	
	if (line1)
	{
		var h3 = document.createElement("h3");
		h3.style.fontSize = "20px";
		text = line1;
		if (line2) text += "<br /><br />" + line2;
		h3.innerHTML = text;
		displayArea.appendChild(h3);
	}
}

var parse = function()
{
	setTimeout(parseXML, 500);
	if (window.audioTools)
		window.audioTools.outLog("parsing");
}

// Parse the lesson XML and start it playing
var parseXML = function()
{
   var name = "";
   try
   {
		clearTimeout(timer); 
		resizable = false;
		
		name = location.href.substring(location.href.lastIndexOf("/") + 1, location.href.lastIndexOf(".htm"));
		
		// Should return root element of the dicstionary
		system = parseXMLString(name, xml);
		if (!system) errorMessage(name);
		
		// This is where to put the processing to load the dictionary.	
		var dictionary = new Dictionary(system.getRoot());
		dictionary.loadDictionary(true);
		
		parser = null;  // Prevent memory leak of XML DOM data.
		if (window.onorientationchange)
			 window.onorientationchange = reloadWindow;
		else window.onresize = reloadWindow;
	}
	catch (e) 
	{
		errorMessage(name, e.message, e.stack);
	}
}

/** Parse the XML from a string */
var parseXMLString = function(fileName, xmlString)
{
	if (window.DOMParser)
	{
		parser=new DOMParser();
		xmlDoc=parser.parseFromString(xmlString,"text/xml");
		if (xmlDoc.documentElement.nodeName=="parsererror") return false; 
	}
	else // Internet Explorer
	{
		xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
		xmlDoc.async="false";
		xmlDoc.loadXML(xmlString); 
	} 
	var root = xmlDoc.documentElement;  // Doc now has the root element of the XML DOM
	return new System(fileName, root);
}


/** Class containing system wide functions
 *
 *  @parasm n  Path to this file
 *  @param  r  Root to the parsed XML file
 */
function System(n, r)
{
	var PHON = 0, COPY = 1, AUTH = 2, AUDIO = 3, VIDEO = 4;
	
	var previousOrientation = undefined;
	var audio = undefined;
	
 	var name = n; 
	var root = r;
	var i;
    this.assets = name + "/Assets/";
	
	this.getAssets = function() { return this.assets; }
	this.getFileDirectory = function() { return name + "/" };
	
	var timeout; // Timer object to initiate an audio playback

	this.iconLink = function()  {return this.assets + "Icons/" };
	this.fontLink = function()  { return this.assets + "Fonts/" };
	this.audioLink = function() { this.assets + "Audio/" };
	this.getRoot = function() { return root; }
	
	this.setFonts = function(f) { fonts = f; }
	this.getFonts = function() { return fonts; }
	
	var popups = [
		document.getElementById("phonPopup"),
		document.getElementById("copyrightArea"),
		document.getElementById("authorArea"),
		document.getElementById("audio"),
		document.getElementById("video")
	];

	/** Method to stop audio or video that is playing */
	var stopAudioVideo = function()
	{
		var player = popups[AUDIO];
		try
		{
			if (player.pause) player.pause();
			player.currentTime = 0;
			player.src = "";
		}
		catch (e) {}
		
		try
		{
			player = popups[VIDEO];
			if (player.pause) player.pause();
			player.currentTime = 0;
			player.src = "";
		}
		catch (e) {}
	}

	/** Method to disable all of the popup elements */
	this.disablePopups = function(element)
	{
		var i, len = popups.length;
		var copAutPopup = document.getElementById("copAutPopup");
		
		for (i=0; i<len; i++)
		{
			popups[i].style.display = "none";
		}
		
		copAutPopup.style.display = "none";
		
		
		stopAudioVideo();
	}
	
	for (i=0; i<AUDIO; i++)
	{
		popups[i].onclick = function() { system.disablePopups(); }
	}
	
	var copAutPopup = document.getElementById("copAutPopup");
	copAutPopup.onclick = function(e) 
	{ 
		if (!e) e = window.event;

		var tag = document.getElementById("audio");
		if (tag.style.display == "none")
			tag = document.getElementById("video");
		if (tag.style.display != "none")
		{
			var rect = tag.getBoundingClientRect();
			var x = e.clientX;    
			var y = e.clientY; 
			if (x<rect.left || x>rect.right || y<rect.top || y>rect.bottom)
			{
				system.disablePopups(); 
			}
			
		}
	}

	var backButton = document.getElementById("prevButton");
	backButton.onclick = function() 
	{  
		var index = location.hash;
		index = index.replace(/%20/g, " ");
		index = index.split(" ");
		if (index.length<3) return;
		window.history.back(); 
	}
	
	var forwardButton = document.getElementById("nextButton");
	forwardButton.onclick = function() 
	{ 
		window.history.forward(); 
	}
	
	var reloadButton = document.getElementById("reloadButton");
	reloadButton.onclick = function() 
	{ 
		var history = location.hash;
		history = decodeURI(history);
		var viewNum = history.split(" ");
		if (viewNum.length!=3)
		{
			location.hash = "";
			location.reload();
		}
		else
		{
			window.history.go(-parseInt(viewNum[2],10));
		}
	}

	this.disablePopups();

	// Determine which audio codecs are supported
	var audioSupport = function()
	{
		if (audio!=undefined) return audio;
		
		audio = [];
		audio["mp3"] = false; 
		try
		{
			var myAudio = popups[AUDIO];

			// Currently canPlayType(type) returns: "", "maybe" or "probably" 
			audio["mp3"] = myAudio.canPlayType && "" != myAudio.canPlayType('audio/mpeg');
		}
		catch(e) {   }
		audio["audio"] = audio["mp3"];
		return audio;
	}	// End audioSupport()
	
	this.loadVideo = function(video)
	{
		var player = popups[VIDEO];
		if (player.pause) player.pause();
		
		var child = player.getElementsByTagName("source");
		var src = name + "/" + video;
		var extension = (video.length>3) ? video.substring(video.length-3) : "";
		var type = "video/" + extension;

		player.setAttribute("src", src);
		player.setAttribute("type", type);
		player.play();
	}

	this.loadAudio = function(sound)
	{
		var player = popups[AUDIO];
		var audio = audioSupport();
		if (!audio["audio"])  return undefined;
		
		player.src = name + "/" + sound;

		if (player.load) 
		{
			player.load();
		}
		if (player.pause) player.pause();
		player.play();
		return player;
	}
	
	/** method to display a popup display element */
	this.popupDisplay = function(element)
	{

		var copAutPopup = document.getElementById("copAutPopup");
		
		if (element == popups[PHON])
		{
			element.style.display = "table";
		}
		else
		{
			copAutPopup.style.display = "table";
			element.style.display  = "block";
		}
	}
	
	/** Private method: Append text with attribute content to a popup element */
	var appendText = function(element, attribute, text)
	{
		if (attribute != null && attribute.length>0)
		{
			var txt = document.createTextNode(text + attribute);
			element.appendChild(txt);
			var br = document.createElement("br");
			element.appendChild(br);
		}
	}
	
	/** Load Copyright information 
	 *
	 * @param parsed xml for dictionary
	 **/
	this.loadCopyright = function(xmlDoc)
	{
		var copyrightArea = document.getElementById("copyrightArea");
		copyrightArea.onclick = function() { system.disablePopups(); }
		
		var copyright = xmlDoc.getElementsByTagName("copyright");
		if (copyright.length > 0)
		{
			appendText(copyrightArea, copyright[0].textContent, "Copyright: ");
			appendText(copyrightArea, copyright[0].getAttribute("date"), "Copyright Date: ");
		}
		else
		{
			appendText(copyrightArea, "No copyright information available.", "");
		}

		var copyButton = document.getElementById("copyButton");
		copyButton.onclick = function(e)
		{
			var disabled = copyrightArea.style.display == "none";
			system.disablePopups();
			if (disabled) system.popupDisplay(copyrightArea);
		}
	}
	
	/** scroll if too many authors to fit on display.  Max height + scrollable */
	this.loadAuthor = function(xmlDoc)
	{	
		var authorArea = document.getElementById("authorArea");
		authorArea.onclick = function()	{ system.disablePopups(); }
		
		var authors = xmlDoc.getElementsByTagName("author");
		var i; //counter for loops
		
		if (authors.length > 0)
		{
			for (i=0; i < authors.length; i++)
			{
				appendText(authorArea, authors[i].getAttribute("name"), "Name: ");
				appendText(authorArea, authors[i].getAttribute("org"), "Organization: ");
				appendText(authorArea, authors[i].getAttribute("url"), "Website: ");
				appendText(authorArea, authors[i].getAttribute("email"), "Email: ");
				appendText(authorArea, authors[i].getAttribute("initials"), "Initials: ");
				appendText(authorArea, authors[i].getAttribute("langs"), "Languages: ");
				authorArea.appendChild(document.createElement("br"));
			} //end for loop going through authors
		}
		else 
		{
			appendText(authorArea, "No author information available.", "");
		}
		
		var authButton = document.getElementById("authButton");
		authButton.onclick = function(e)
		{
			var disabled = authorArea.style.display == "none";
			system.disablePopups();
			if (disabled) system.popupDisplay(authorArea);
		}
	}
	
	/** Load Search Button 
	 *
	 *  @param dictionary dictionary object for reloading dictionary
	 **/
	this.loadSearchButton = function(dictionary)
	{
		var btn = document.getElementById("searchButton");
		var searchType, dictionaryArea
		btn.onclick = function()
		{
			searchType = document.getElementById("searchText").value;
			dictionary.loadDictionary(false, searchType);
		}
	}
	
	/** Enable popup state button 
	 *
	 *  @param dictionary dictionary object for enabling popup state button
	 */
	this.loadPopupState = function(dictionary)
	{
	
		window.onpopstate = function(event) 
		{
			var state = event.state;
			if (state) 
				location.hash = state;
			dictionary.loadDictionary(null, null);
		};
	}

	
	/** Enables fast string concatenation using append() and toString() methods */
	this.StringBuffer = function()
	{
	   var buffer = []; 
	   
	   this.append   = function(string) { buffer.push(string); };
	   this.toString = function() { return buffer.join(""); };
	}
	


}	// End of System class

/** Toolbar Class 
 *
 *  @param dictionaryObject The dictionary being displayed
 */
function Toolbar(dictionaryObject)
{
	var dictionary = dictionaryObject;
   
	var primaryLang = null;
	var secLangs = [];
	var sortOrder = [];
	
	this.configureToolbar = function(xmlDoc)
	{
		var i, button, txt, code, name, variant, maxCount, count;
		var buttonArea = document.getElementById("buttonArea");
		var langArr = xmlDoc.getElementsByTagName("language");
		var len = langArr.length;
		var langCounts = new Array(len); 

		var field = document.getElementById("searchText");
		var langFonts = {}, fontObject = {};
		var handler = null;
		
		if (len==0) return;
		
		primaryLang = langArr[0].getAttribute("lang");
		langCounts[0] = maxCount = langArr[0].getElementsByTagName("word").length;
		for (i=1; i< len; i++)
		{
			langCounts[i] = count = langArr[i].getElementsByTagName("word").length;
			if (count>=maxCount)
			{
				maxCount = count;
				primaryLang = langArr[i].getAttribute("lang");
			}
		}
		
		for (i=0; i < len; i++) //makes a button for each language found and adds it to the DOM with an event listener
		{
			if (langCounts[i]==0)
				continue;
			button = document.createElement("button");
			button.setAttribute("class", "lang");

			face = langArr[i].getAttribute("face");
			if (!face || face.length==0) face = "Times New Roman"; 
			size = langArr[i].getAttribute("size");
			if (!size || size.length==0) size="12";
			name = langArr[i].getAttribute("name");
			fontObject = {"face":face, "size":size, "name": name};
			code = langArr[i].getAttribute("lang");
			langFonts[code] = fontObject;
			
			variant = langArr[i].getAttribute("variant");
			if (variant && variant.length>0) name = name + "/" + variant;
			txt = document.createTextNode(name);
			
			sortOrder[code] = langArr[i].getAttribute("sort");
			button.appendChild(txt);
			
			button.setAttribute("id", code);
			button.onclick = function () 
			{ 
				if (this.id == primaryLang) //code for turning off primary lang
				{
					primaryLang = null;
					this.style.borderStyle = "hidden";
				}		
				else if (this.style.borderColor == "red" && this.style.borderStyle != "hidden" && primaryLang == null) //code for turning on primary language
				{
					primaryLang = this.id;
					var x = secLangs.indexOf(this.id);
					secLangs.splice(x, 1);
					this.style.borderStyle = "solid";
					this.style.borderColor = "green";
					field.style.fontSize = langFonts[code].size + "pt";
					field.style.fontFamily = langFonts[code].face;
					handler = new KeyboardHandler(field, langFonts[code].name);
				}
				else if (this.style.borderColor == "red" && this.style.borderStyle != "hidden") //code for turning off active language
				{
					this.style.borderStyle = "hidden";
					var x = secLangs.indexOf(this.id);
					secLangs.splice(x, 1);
				}
				else 
				{
					secLangs.push(this.id);
					this.style.borderStyle = "solid";
					this.style.borderColor = "red";
				}
				
				var dictionaryArea = document.getElementById("dictionaryDisplay");
				dictionaryArea.innerHTML = "";
				dictionary.clearView();
				dictionary.loadDictionary(true);
			}
			
			//code below for setting primary and secondary languages and needed styles initially
			if (primaryLang == code)
			{
				button.style.borderWidth = "5px";
				button.style.borderStyle = "solid";
				button.style.borderColor = "green";
				field.style.fontSize = langFonts[code].size + "pt";
				field.style.fontFamily = langFonts[code].face;
				handler = new KeyboardHandler(field, langFonts[code].name);
			}
			else
			{
				button.style.borderWidth = "5px";
				button.style.borderStyle = "solid";
				button.style.borderColor = "red";
				secLangs.push(code);
			}
			buttonArea.appendChild(button);
		}
	}
		
	this.getSecondaryLanguages = function() { return secLangs; }
	this.getPrimaryLanguage = function()    { return primaryLang; }
	this.getSortOrder = function()      	{ return sortOrder[primaryLang]; }
	
}	// End of Toolbar class

function KeyboardHandler(component, language)
{
   var SHIFT_MASK 		 =	  1; 
   var CTRL_MASK  		 =	  2;	// ctrl on mac
   var META_MASK  		 =	  4;	// command on mac
   var ALT_MASK   		 =	  8;	// option on mac
   var CAP_MASK   		 =	 16;  
   var LEFT_SHIFT_MASK  =	 32;
   var RIGHT_SHIFT_MASK = 	 64;
   var LEFT_CTRL_MASK   =  128;
   var RIGHT_CTRL_MASK  =  256;
   var LEFT_ALT_MASK    =  512;  
   var RIGHT_ALT_MASK 	 = 1024;
   
   /* Indices int modifier table */
   var MUST_HAVE = 0;
   var CANT_HAVE = 1;
   
   /* Indices for dead key map data */
   var ACTION  = 0;
   var OUTPUT = 1;
   var MAP_LEN = 2;
   
   /** Map from characters to MAC keycodes */
   var lowerKeyCodeMapping =
      "asdfhgzxcv" + "\0bqweryt12" + "3465=97-80" + "]ou[ip\0lj'"
         + "k;\\,/nm.\0\0" + "`";

   var upperKeyCodeMapping =
      "ASDFHGZXCV" + "\0BQWERYT!@" + "#$^%+(&_*)" + "}OU{IP\0LJ\""
         + "K:|<?NM>\0\0" +"~";

    


   language = language.replace(/-/g,'');
   try
   {
		var json = eval(language);
   }
   catch { return; }
   if (!json) return;
 
   var state = "none";

   var charCodes = json["charCodes"];
   var terminators = json["terminators"];
   var sequences = json["deadSequences"];
   var modifiers = json["modifiers"];
   var modifierData = json["modifierData"];
   var charKeyMaps = new Array();
   var deadKeyMaps = new Array();
   
   
   var index = null;
   for (index=0; index<modifierData.length; index++)
   {
	   charKeyMaps.push(modifierData[index].charMap);
	   deadKeyMaps.push(modifierData[index].deadKeyMap);
   }
   
   var getState = function() { return state; }
   var setState = function(s) { state = s; }

   // Search json array
   var searchJSON = function(tags, key)
   {
		var i = null;
		for (i = 0; tags.length > i; i += 1) 
		{
			if (tags[i].key === key) return tags[i];
        }
		return null;
    }
	
	var findDeadSequence = function(data, key)
	{
		var sequence = searchJSON(data, key);
		if (sequence)
		{
			return [sequence.action, sequence.output];
		}
	}
	
	var findModifierIndex = function(modifier)
	{
	  var index = null;
	  for (index=0; index < modifiers.length; index++)
	  {
		  values = [modifiers[index].must, modifiers[index].cant];
		  if ((values[MUST_HAVE] & modifier) != values[MUST_HAVE]) 
			 continue;
		  if ((values[CANT_HAVE] & modifier) != 0)
			  continue;
		  
		  return index;
	  }
	  return -1;
	}
	
	var getModifierKeyMap = function(modifier)
	{
		var index = findModifierIndex(modifier);
		if (index<0) return null;
		
		var data = new Array();
		var charMap = charKeyMaps[index];
		for (index=0; index<charMap.length; index++)
		{
			data[charMap[index].key] = charMap[index].output;
		}
		return data;
	}
	
	var getDeadKeyMap = function(modifier)
	{
		var index = findModifierIndex(modifier);
		if (index<0) return null;
		
		return deadKeyMaps[index];
	}
	
	var getCharacterCode = function(e)
	{
		var code = event.which || event.keyCode;

		var lowerSpecialsA = [';', '=', ',', '-', '.', '/', '`']; // 186-192
		var lowerSpecialsB = ['[', '\\', ']', '\'', '\'']; //219-222
		
		// Always return unshifted character 
		if (code>=48 && code <=57)
			return code;
		
		if (code>=65 && code<=90)
			return code;
		
		if (code>=186 && code <= 192)
		{
			code = lowerSpecialsA[code-186];
			return code.charCodeAt(0);
		}
		
		if (code>=219 && code<=222)
		{
			code = lowerSpecialsB[code-219];
			return code.charCodeAt(0);
		}
		return -1;
	}
	
   component.onkeydown = function(e) 
   {
	   	if (!e) e = window.event;
		
		character = getCharacterCode(e);
		if (character<0) return;
		
	   	if (processEvent(e, character)) 
		{
			if (event.preventDefault) event.preventDefault();
			e.stopPropagation();
		}
   }
 
   var processEvent = function(e, character)
   { 
		var modifier = getModifiers(e);

	    var sequence = computeOutput(modifier, character);
	    if (sequence==null) return true;
	    if (sequence.length==0) { return true; }

		sequence = sequence.replace(/undefined/g,"");
	    var field = e.currentTarget;
	    var start = field.selectionStart;
	    var end = field.selectionEnd;
		var text = field.value;
	    text = text.substring(0,start) + sequence + text.substring(end);
	    field.value = text;
	    var newPosition = start + sequence.length;
	    field.setSelectionRange(newPosition, newPosition);
	    return true;
   }
   
   var getModifiers = function(e)
   {
	   var modifiers = 0;
	   
	   if (e.shiftKey) modifiers += SHIFT_MASK;
	   if (e.altKey)   modifiers += ALT_MASK;
	   if (e.metaKey)  modifiers += META_MASK;
	   
	   if (event.getModifierState("CapsLock"))
		   modifiers += CAPS_LOCK;

	   if ((modifiers & SHIFT_MASK) != 0)
	   {
		   if (event.location != undefined)
		   { 
				if (event.location === KeyboardEvent.DOM_KEY_LOCATION_LEFT)
					modifiers |= LEFT_SHIFT_MASK;
				else if (event.location === KeyboardEvent.DOM_KEY_LOCATION_RIGHT)
					modifiers |= RIGHT_SHIFT_MASK;
		   }
	   }
		
	   if ((modifiers & ALT_MASK) != 0)
	   {
		   if (event.location != undefined)
		   { 
				if (event.location === KeyboardEvent.DOM_KEY_LOCATION_LEFT)
					modifiers |= LEFT_ALT_MASK;
				else if (event.location === KeyboardEvent.DOM_KEY_LOCATION_RIGHT)
					modifiers |= RIGHT_ALT_MASK;
		   }
	   }
   
	   return modifiers;
   }
  
   var computeOutput = function(modifier, character)
   {  
      var key, terminator, sequenceData;

	  deadKeyMap = getDeadKeyMap(modifier);
	  if (deadKeyMap==null)
	  {
		  terminator = searchJSON(terminators, getState()).output;
		  setState("none");
		  return terminator;
	  }
	
	  var mapData = findDeadSequence(deadKeyMap, character);
	  if (!mapData)
	  {
		  terminator = "";
		  if (!(getState() == "none"))
		  {
			  terminator = searchJSON(terminators, getState()).output;
		  }
    	  var keyMap = getModifierKeyMap(modifier);
    	  var xlate = translateChar
                  (keyMap, character);
    	  setState("none");
    	  return terminator + xlate;
	  }

	  if (mapData[ACTION].length==0)
	  {
		  setState("none");
		  return mapData[OUTPUT]; // No next state
	  }

	  if (getState() == "none")
      {
		  key = getState() + "~~" + mapData[ACTION];
		  sequenceData = findDeadSequence(sequences, key);
		  if (sequenceData == null)
		  {
			  terminator = searchJSON(terminators, mapData[ACTION]).output;
   		      setState("none");
			  return terminator;
		  }

		  if (sequenceData[ACTION].length>0)
		  {
			  setState(sequenceData[ACTION]);
			  return "";
		  }
		  else
		  {
			  setState("none");
			  return sequenceData[OUTPUT];
		  }
      }
	  
      key = getState() + "~~" + mapData[ACTION];
      sequenceData = findDeadSequence(sequences, key);
      if (sequenceData == null)
      {
		  terminator = searchJSON(terminators, getState()).output;
    	  var keyMap = getModifierKeyMap(modifier);
    	  var xlate = translateChar
                  (keyMap, character);
		  setState("none");
    	  return terminator + xlate;
      }
      
      if (sequenceData[ACTION].length==0)
      {
		  setState("none");
    	  return sequenceData[OUTPUT];
      }
    			  
      setState(sequenceData[ACTION]);
      return  "";
   }

   var translateChar = function(mapping, character) // expects integer, not character
   {  
   		var character = String.fromCharCode(character);
		if (character==' ')  return character;

		var lower = lowerKeyCodeMapping.indexOf(character);
		var upper = upperKeyCodeMapping.indexOf(character);

		var mapValue = lower;
		if (lower<0) mapValue = upper;
		if (mapValue<0) return '\0';
		return mapping[mapValue];
   }
   
}	// End of KeyboardHandler class	

/** Class to load and display dictionary output templates
 *
 *  @param dictionaryObject The dictionary being displayed
 */
function Template(dictionaryObject)
{
	var dictionary    = dictionaryObject;
	var jsonObj       = JSON.parse(json);
	var jsonLen       = jsonObj.length;
	var selectElement = document.getElementById("templateSelect");
	var currentIndex  = 0;

	// Add Select Options
	var text
	for (var i = 0; i < jsonLen; i++)
	{
		var optionElement = document.createElement("option");
		text = jsonObj[i].template;
		optionElement.text = text;
		if (text=="default") currentIndex = i;
		selectElement.add(optionElement);
	}
	selectElement.selectedIndex = "" + currentIndex;

		
	selectElement.onchange = function()
	{
		selectIndex  = selectElement.selectedIndex;
		selectOption = selectElement.options;
		currentIndex = selectOption[selectIndex].index;
		
		dictionary.clearView();
		dictionary.loadDictionary(true);
	}
	
	this.getCurrentTemplate = function() { return jsonObj[currentIndex]; }

}	// End of Template class


/** Dictionary Class **/
function Dictionary(parsedObject)
{
	// Instance Variables
	var xmlDoc = parsedObject;
	var dictionaryArea = document.getElementById("dictionaryDisplay"); 
	var view = [];
	var displayAudio = true;

	var template = new Template(this);
	var format = new Format(system, xmlDoc);
		
	var toolbar = new Toolbar(this, xmlDoc);
	toolbar.configureToolbar(xmlDoc);
	
	system.loadCopyright(xmlDoc);    // Enables copyright button
	system.loadAuthor(xmlDoc);     	 // Enables author button
	system.loadSearchButton(this);
	system.loadPopupState(this);
	
	this.clearView = function()
	{
		view = [];
		var history = location.hash;
		history = decodeURI(history);
		var viewNum = history.split(" ");
		if (viewNum.length==3)
		{
			window.history.go(-parseInt(viewNum[2],10));
		}
	}
	
	/** Public Method: Load Dictionary 
	 *
	 * @param doSort true if sort required
	 * @param Regular expression search pattern
	 */
	this.loadDictionary = function(doSort, pattern)
	{
		var index = location.hash;
		index = index.replace(/%20/g, " ");
		index = index.split(" ");
		if (index.length>=3) index = index[2];
		index = parseInt(index);
 
		var searchType, newWordList, html, list, i, listLen;
		var primaryLang = toolbar.getPrimaryLanguage();
		var secLangs = toolbar.getSecondaryLanguages();
		var sort = (doSort) ? toolbar.getSortOrder() : null;
		
		// array of all <word> tags in xml file
		var wordList = xmlDoc.getElementsByTagName("word"); 
		if (view.length > 0)
		{
			if (!isNaN(index) && index > 0)
			{
				wordList = view[index - 1];
			}
				
			sort = toolbar.getSortOrder();
		}
		else
		{
			var languages = xmlDoc.getElementsByTagName("language");
			var langs = languages.length, i, lang;
			for (i=0; i<langs; i++)
			{
				element = languages[i];
				lang = element.getAttribute("lang").toLowerCase();
			
				if (lang == primaryLang)
				{
					wordList = element.getElementsByTagName("word");
					break;
				}
			}
		}

		system.disablePopups();

		// Perform custom sort if primary language changed (temporarily commented out)
		if (doSort)
		{
			//wordList = sortDictionary(wordList, primaryLang, sort);
		}
		
		// Search by word, ontology, or category
		if (pattern != null && pattern.length > 0)
		{
			searchType = document.getElementById("searchSelect").value;
			newWordList = searchWords(searchType, wordList, pattern, primaryLang);
			
			// Adjust the view arrays
			if (view.length > 0)
			{
				view = view.splice(0, index); 
			}
			
			// Append the new view to view array
			index = view.length + 1;
			view.push(newWordList);
			location.hash = WOLF_VIEW + index;
			window.history.replaceState(WOLF_VIEW+index, location.hash, location.href);
			wordList = newWordList;
		}
		
		html = format.toHTML(wordList, template.getCurrentTemplate(), primaryLang, secLangs);
		dictionaryArea.innerHTML = "";
		dictionaryArea.innerHTML = html;

		list = document.getElementsByName("phonetics");
		listLen = list.length;
		for (i=0; i<listLen; i++)
		{
			list[i].onclick = function(e)
			{
				if (!e) e = window.event;
				if (e.stopPropagation)    e.stopPropagation();
				if (e.cancelBubble!=null) e.cancelBubble = true;

				var element = e.currentTarget;
				if (element)
				{
					var text = element.getAttribute("title");
					if (text && text.length>0)
					{
						var phonetic = document.getElementById("phoneticsArea");
						phonetic.innerHTML = text;

						var phonPopup = document.getElementById("phonPopup");
						system.disablePopups();
						system.popupDisplay(phonPopup);
					}
				}
			}
		}
		
		list = document.getElementsByName("media");
		listLen = list.length;
		for (i=0; i<listLen; i++)
		{
			list[i].onclick = function(e)
			{
				if (!e) e = window.event;
				if (e.stopPropagation)    e.stopPropagation();
				if (e.cancelBubble!=null) e.cancelBubble = true;

				var element = e.currentTarget;
				if (element)
				{
					var text = element.getAttribute("title");
					var videoElement = document.getElementById("video");
					var audioElement = document.getElementById("audio");
					if (text && text.length>0)
					{
						var media = text.split("...");
						var lenAudio = media[0].trim().length;
						var lenVideo = media[1].trim().length;
						if (lenAudio == 0 && lenVideo == 0)
						{
							return;
						}
						if (lenVideo==0 || (lenAudio>0 && displayAudio))
						{
							system.disablePopups();
							system.popupDisplay(audioElement);
							system.loadAudio(media[0]);
							if (lenVideo>0) displayAudio = !displayAudio;
						}
						else if (lenVideo>0)
						{
							system.disablePopups();
							system.popupDisplay(videoElement);
							system.loadVideo(media[1]);
							if (lenAudio>0) displayAudio = !displayAudio;
						}
					}       // End if text
				}			// End if element
			}				// End onClick function
		}					// End for
		
	} // End of loadDictionary() function

	/** Private Method: Search for matching words, categories, or ontology 
	 *
	 * @param type word, category, or ontology
	 * @param wordList The list of word elements in the dictionary
	 * @param pattern The user input of the reqular expression pattern
	 * @param active The active language
	 */
	var searchWords = function(type, wordList, pattern, active)
	{
		var i, j,k, text, title, elements, values, added, translations;
		var len = wordList.length, eLen, vLen;
		var newWordList = [];
		var regExp = makeRegExp(pattern);

		for (i = 0; i < len; i++)
		{
			switch (type.toLowerCase())
			{
				case "word":
					translations = wordList[i].getElementsByTagName("translations");
					if (!translations || translations.length==0) return;
					text = translations[0].firstChild.nodeValue;
					if (text.search(regExp)>=0)
					{
						newWordList.push(wordList[i]);
						continue;
					}
					break;
					
				case "ontology":
					elements = wordList[i].getElementsByTagName("ontology");
					if (!elements || elements.length==0) continue;
					
					eLen = elements.length;
					for (j=0; j<eLen; j++)
					{
						text = elements[j].getAttribute("abbreviation");
						if (!text || text.length==0)
							text = element.getAttribute("parent");
						if (!text || text.length==0) continue;
						if (text.search(regExp)>=0) 
						{
							newWordList.push(wordList[i]);
							break;
						}
					}
					break;
					
				case "category":
					elements = wordList[i].getElementsByTagName("classification");
					added = false;
					eLen = elements.length;
					for (j=0; j<eLen; j++)
					{
						title = elements[j].getAttribute("title");
						if (title && title.toLowerCase() == "categories")
						{
							text = elements[j].textContent;
							if (!text) continue;
							newText = text.split(" ")[0];
							if (newText.length>0 
									&& newText.charAt(newText.length-1)== ':')
								text = text.substring(newText.length+1);

							values = (text && text.length>0) ? text.split(",|;") : [];
							vLen = values.length;
							for (k=0; k<vLen; k++)
							{
								if (values[k]) values[k] = values[k].trim();
								if (values[k].search(regExp)>=0)
								{
									newWordList.push(wordList[i]);
									added = true;
									break;
								}
							}
						}
						if (added) break;
					}
					break;
			}
		}
		return newWordList;
	}

    /** Adjust regular expression to allow * or ? without preceding dot
      * @param original regular expression pattern
      */	  
	var makeRegExp = function(pattern)
	{
		var buf = new system.StringBuffer(), i, character, len = pattern.length;
        for (i=0; i<len; i++)
        {  
		   character = pattern.charAt(i);
           if (character==='?' || character==='*')
           {   
		      if (i==0 || pattern.charAt(i-1)!='.') buf.append('.');  
		   }
           buf.append(character);
        }
		
		var regExp;
		try
		{
			return new RegExp(buf.toString(), "iu");
		}
		catch (e)
		{
			return new RegExp(buf.toString(), "i");
		}
	}

	/** Return the translation corresponding to the active language
	 *  @param element Node containing translation child elements
	 *  @param active The active language
	 */
	var getActiveTranslation = function(word, active)
	{
		var translations = word.childNodes, i, len = translations.length;
		for (i=0; i<len; i++)
		{
			tag = translations[i].tagName;
			if (tag == "translations")
				return translations[i].firstChild.nodeValue;
		}
		return "????";
	}
	
	/** Private Method: Perform dictionary sort
	 *  @param wordList Dictionary word list
	 *  @param primaryLanguage gloss language code
	 *  @param sort custom sort order
	 */
	var sortDictionary = function(wordList, primaryLanguage, sort)
	{
	    var wordListArray = Array.prototype.slice.call( wordList, 0 );
		wordListArray.sort(function(a, b)
			{
				var first = getActiveTranslation(a, primaryLanguage);
				var second = getActiveTranslation(b, primaryLanguage);
				
				var size = Math.min(first.length, second.length);
				var i, charF, charS, indexF, indexS, result;
				for (i=0; i<size; i++)
				{
					charF = first.charAt(i);
					charS = second.charAt(i);
					indexF = (sort) ? sort[charF] : undefined;
					indexS = (sort) ? sort[charS] : undefined;
					if (!indexF && !indexS)
					{
						result = charF.toUpperCase().charCodeAt()
							  - charS.toUpperCase().charCodeAt();
						if (result != 0) return result;
					 
						result = charF.charCodeAt() - charS.charCodeAt();
						if (result == 0) continue;
						return result;
					}
					
					if (!indexF) return +1;
					if (!indexS) return -1;
					if (indexF != indexS)
						return (indexF - indexS);
				}   // end for
			}       // end function
			
		);			// end sort() call
		
		return wordListArray;
	}				// end sortDictionary

} // end of Dictionary class
