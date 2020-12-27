/*
 * UndoRedoLanguage.java
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

package org.wolf.undoredo;

import java.util.Vector;

import org.acorns.language.LanguageFont;
import org.wolf.data.DictionaryData;
import org.wolf.data.Language;
import org.wolf.lib.DeepCopy;

/** Class to undo and redo language changes */
public class UndoRedoLanguage extends UndoRedoData
{   LanguageFont[] oldFonts, newFonts;
    String[] oldCodes, newCodes;

    public UndoRedoLanguage
            (Language[] oldLangs, Language[] newLangs)
    { oldFonts = copyFonts(oldLangs);
      oldCodes = copyCodes(oldLangs);
      newFonts = copyFonts(newLangs);
      newCodes = copyCodes(newLangs);
    }

    public String redo(DictionaryData data)
    { Language[] languages = data.getLanguages();
      Vector<Language> redoneLanguages = new Vector<Language>();
      Language     language;
      int index, index2;

      for (int i=0; i<newCodes.length; i++)
      {   index = find(newCodes[i], oldCodes);
          if (index>=0)
          {  index2 = find(newCodes[i], languages);
             if (index2<0) continue; // Shouldn't happen

             language = languages[index2];
             language.setLanguageFont(oldFonts[index]);
          }
          else
          {  language = new Language();
             language.setLanguageFont(newFonts[i]);
             language.setLanguageFields(newCodes[i]);

          }
          redoneLanguages.add(language);
   }
      languages = new Language[redoneLanguages.size()];
      languages = redoneLanguages.toArray(languages);
      data.setLanguages(languages);
      return "";
    }

    public String undo(DictionaryData data) 
    { Language[] languages = data.getLanguages();
      Vector<Language> undoneLanguages = new Vector<Language>();
      Language   language;
      int index, index2;

      for (int i=0; i<oldCodes.length; i++)
      {   index = find(oldCodes[i], newCodes);
          if (index>=0)
          {  index2 = find(oldCodes[i], languages);
             if (index2<0) continue; // Shouldn't happen
             
             language = languages[index2];
             language.setLanguageFont(newFonts[index]);
          }
          else
          {  language = new Language();
             language.setLanguageFont(oldFonts[i]);
             language.setLanguageFields(oldCodes[i]);
          }
          undoneLanguages.add(language);
      }

      languages = new Language[undoneLanguages.size()];
      languages = undoneLanguages.toArray(languages);
      data.setLanguages(languages);
      return "";
    }

    public int find(String langCode, Language[] languages)
    {  for (int i=0; i<languages.length; i++)
       {  if (langCode.equals(languages[i].getLanguageCode())) return i;  }
       return -1;

    }
    public int find(String langCode, String[] codes)
    {  for (int i=0; i<codes.length; i++)
       {  if (codes[i].equals(langCode)) return i;  }
       return -1;
    }

    public LanguageFont[] copyFonts(Language[] languages)
    {   LanguageFont[] fonts = new LanguageFont[languages.length];
        LanguageFont languageFont;
        for (int i=0; i<languages.length; i++)
        {   languageFont = languages[i].getLanguageFont();
            languageFont = (LanguageFont)DeepCopy.copy(languageFont);
            fonts[i] = languageFont;
        }
        return fonts;
    }

    public String[] copyCodes(Language[] languages)
    {  String[] codes = new String[languages.length];
       for (int i=0; i<languages.length; i++)
       {  codes[i] = languages[i].getLanguageCode(); }
       return codes;
    }
}  // End of UndoRedoLanguage class
