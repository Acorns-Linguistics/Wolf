/*
 * DictionaryView.java
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

package org.wolf.application;

import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wolf.data.Comment;
import org.wolf.data.DictionaryData;
import org.wolf.data.Group;
import org.wolf.data.Item;
import org.wolf.data.Language;
import org.wolf.data.Ontology;
import org.wolf.data.Word;

/** Manage views to dictionary data */
public class DictionaryView
{   private int current;             // Index to current view
    private Language language;       // The active language
    private ArrayList<Integer> view; // The current view
    private ArrayList<String> views; // All of the views

    public DictionaryView()   {  resetView();  }

    /** Reset the dictionary views */
    public void resetView()
    {  views = new ArrayList<String>();
       current = -1;
       setEnabledButtons();
       setSearchMode('W');
       createView("*");
    }

    /** Get the number of words in the view */
    public int size()  { return view.size(); }
 
    /** Determine if a particular word is in the view */
    public boolean isInView(int index)
    {
    	return (view.contains(index));
    }

    /** Get the index from the view to the word index */
    public int mapViewIndex(int index)
    {  if (index>=view.size()) return index;
       Integer intVal = view.get(index);
       return intVal.intValue();
    }

    /** Insert view entry
     *
     * @param viewIndex View index
     */
    public  void insertViewIndex(int viewIndex)
    {  int wordIndex = mapViewIndex(viewIndex);
       int entryIndex;
       for (int i=0;  i<view.size(); i++)  // Adjust view pointers
       {  entryIndex =  view.get(i).intValue();
          if (entryIndex>=wordIndex) view.set(i, entryIndex+1);
       }
       view.add(viewIndex, wordIndex);
    }

    /** Remove view entry
     *
     * @param viewIndex View index
     */
    public void removeViewIndex(int viewIndex)
    {  int wordIndex = mapViewIndex(viewIndex);
       int entryIndex;
       view.remove(viewIndex);
       for (int i=0;  i<view.size(); i++)  // Adjust view pointers
       {  entryIndex =  view.get(i).intValue();
          if (entryIndex>=wordIndex) view.set(i, entryIndex-1);
       }
    }

    /** Switch to the previous view */
    public void previousView() throws NoSuchElementException
    {  if (!isPreviousView()) throw new NoSuchElementException();

       String pattern = views.get(--current);
       view = createView(pattern.charAt(0), pattern.substring(1));
       setEnabledButtons();
    }

    /** Switch to the next view */
    public void nextView() throws NoSuchElementException
    {  if (!isNextView()) throw new NoSuchElementException();
       String pattern = views.get(++current);
       view = createView(pattern.charAt(0), pattern.substring(1));
       setEnabledButtons();
    }

    /** Reload the current view */
    public void reloadView() throws NoSuchElementException
    {  if (current<0||current>=views.size()) throw new NoSuchElementException();
       String pattern = views.get(current);
       view = createView(pattern.charAt(0), pattern.substring(1));
    }

    /** Method to create a new view of the dictionary based on a seach pattern
     *
     * @param pattern The search pattern for the view
     * @throws NoSuchElementException
     */
    public void createView(String pattern) throws NoSuchElementException
    {   // clear out the views after this point
        for (int i=++current; i<views.size(); i++)
        {   views.remove(current); }

        language = getActiveLanguage();
        if (language == null)
        {   view = new ArrayList<Integer>();
            return;
        }
        char type = getSearchMode();
        view = createView(type, pattern);
        views.add(current, type + pattern);
        setEnabledButtons();
    }

    /** Construct the current view
     *
     * @param type The search mode
     * @param pat The search pattern
     */
    private ArrayList<Integer> createView(char type, String pattern)
    {   ArrayList<Integer> newView;

        switch(type)
        {  case 'C':  newView = createCategoryView(pattern);
                      break;
           case 'O':  newView = createOntologyView(pattern);
                      break;
           case 'W':  newView = createWordView(pattern);
                      break;
           default: throw new NoSuchElementException("Illegal view type");
        }
        return newView;
    }

    /** Determine if there is a previous view */
    private boolean isPreviousView()
    { return current>0 && current<views.size(); }

    /** Determine if there is a next view */
    private boolean isNextView()
    { return current>=0 && current<views.size()-1; }

    /** Set the ghostable buttons */
    private void setEnabledButtons()
    {   DictionaryPanel buttonPanel = getRootDictionaryPanel().getButtonPanel();
        buttonPanel.enableButton(DictionaryPanel.BACK, isPreviousView());
        buttonPanel.enableButton(DictionaryPanel.FORWARD, isNextView());
    }

    private char getSearchMode()
    {  DictionaryPanel buttonPanel = getRootDictionaryPanel().getButtonPanel();
       return buttonPanel.getSearchMode();
    }

    private void setSearchMode(char mode)
    {  DictionaryPanel buttonPanel = getRootDictionaryPanel().getButtonPanel();
       buttonPanel.setSearchMode(mode);
    }

    /** Create a category view */
    private ArrayList<Integer> createCategoryView(String pattern)
    {   ArrayList<Word> words = language.getWords();

        ArrayList<Integer> newView = new ArrayList<Integer>();
        ArrayList<Item> items = new ArrayList<Item>();

        Word word;
        Item item;
        Comment  comment;
        String   data;
        String[] categories;

        for (int  i=0; i<words.size(); i++)
        {  word = words.get(i);
           items.clear();
           searchColumns(word, items, false);

           for (int j=0; j<word.getRows().size(); j++)
           {   item = word.getRows().get(j);
               if (item instanceof Group) searchColumns((Group)item,items,false);
           }

           // See if this word should add to the view
           String newData;
           for (int j=0; j<items.size(); j++)
           {   item = items.get(j);
               comment = (Comment)item;
               data = comment.getComment();
               String[] split = data.split(" ");
               newData = (split.length>0) ? split[0] : "";
               if (newData.endsWith(":"))
            	   data = data.substring(newData.length()+1);
               
               categories = data.split(",|;");
               for (int k=0; k<categories.length; k++)
               {  categories[k] = categories[k].trim();
                  if (categories[k].length()==0) continue;
                  if (matchPattern(pattern, categories[k]))
                  {   newView.add(i);
                      break;
                  }
               }
           }
        }
        return newView;
    }

    /** Create an ontology view */
    private ArrayList<Integer> createOntologyView(String pattern)
    {   ArrayList<Word> words = language.getWords();
        ArrayList<Integer> newView = new ArrayList<Integer>();
        ArrayList<Item> items = new ArrayList<Item>();

        Word word;
        Item item;
        Ontology ontology;
        String parent, value;

        for (int  i=0; i<words.size(); i++)
        {  word = words.get(i);
           items.clear();
           searchColumns(word, items, true);

           for (int j=0; j<word.getRows().size(); j++)
           {   item = word.getRows().get(j);
               if (item instanceof Group) searchColumns((Group)item,items,true);
           }

           // See if this word should add to the view
           for (int j=0; j<items.size(); j++)
           {   item = items.get(j);
               ontology = (Ontology)item;
               parent = ontology.getParent();
               value  = ontology.getValue();
               if (matchPattern(pattern, parent)||matchPattern(pattern, value))
               {  newView.add(i);
                  break;
               }
           }
        }
        return newView;
    }

    /** Create a word view */
    private ArrayList<Integer> createWordView(String pattern)
    {   ArrayList<Word> words = language.getWords();

        ArrayList<Integer> newView = new ArrayList<Integer>();
        String key;
        Word word;
        for (int i=0; i<words.size(); i++)
        {  word = words.get(i);
           key = word.getKey();
           if (matchPattern(pattern, key)) { newView.add(i); }
        }
        return newView;
    }

    /** Find matching components that can contribute to the view
     *
     * @param group The Group object containing columns of components
     * @param found ArrayList of matching components
     * @param ontology true if ontology view, false if category view
     */
    private void searchColumns
                          (Group group, ArrayList<Item> found, boolean ontology)
    {  ArrayList<Item> items = group.getColumns();
       Item item;
       Comment comment;

       for (int i=0;  i<items.size(); i++)
       {  item = items.get(i);
          if (item instanceof Group)
          {  searchColumns((Group)item, found, ontology);
          }
          else if (ontology && item instanceof Ontology) found.add(item);
          else if (!ontology && item instanceof Comment)
          {  comment = (Comment)item;
             if (comment.getTitle().equals("Categories")) found.add(item);
          }
       }
    }

    /** Get the current activew language */
    private Language getActiveLanguage()
    {  DictionaryData data = getRootDictionaryPanel().getDictionaryData();
       return data.getActiveLanguage();        
    }

    /** Retrieve the root dictionary panel */
    private RootDictionaryPanel getRootDictionaryPanel()
    {   PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                .getPropertyChangeListeners("DictionaryListeners");

        return (RootDictionaryPanel)pcl[0];
    }

    /** Method to determine if a key matches a pattern
     *     The patterns roughly are regular expressons. Only lower case patterns
     *     are recognized. Also, '*' or '?' are replaced by '.*' and'.?' if the
     *     '.' character is missing.
     *
     *     These concessions is to make  the program easier for users to use
     *     if they don't have a background in regular expressions
     *
     * @param pattern A regular expression pattern to match
     */
    private static  boolean matchPattern(String pattern, String key)
    {   StringBuffer buf = new StringBuffer();
        char character;

        key = key.toLowerCase();
        for (int i=0; i<pattern.length(); i++)
        {  character = pattern.charAt(i);
           if (character=='?' || character=='*')
           {   if (i==0 || pattern.charAt(i-1)!='.') buf.append('.');  }
           buf.append(character);
        }
        pattern = buf.toString();
        Pattern compile = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher match = compile.matcher(key);
        return match.matches();
    }

}       // End of DictionaryView class
