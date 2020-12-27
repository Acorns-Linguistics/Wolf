/*
 * DictionaryListener.java
 *
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

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.wolf.components.ScrollableToolbar;
import org.wolf.data.Author;
import org.wolf.data.DictionaryData;
import org.wolf.data.Language;
import org.wolf.data.OntologyData;
import org.wolf.dialogs.AuthorDialog;
import org.wolf.dialogs.ChooseFileDialog;
import org.wolf.dialogs.CopyrightDialog;
import org.wolf.dialogs.LanguageDialog;
import org.wolf.dialogs.OntologyDialog;
import org.wolf.dialogs.OptionsDialog;
import org.wolf.dialogs.PrintDialog;
import org.wolf.dialogs.TemplateDialog;
import org.wolf.print.DictionaryPrintable;
import org.wolf.print.PrintPreview;
import org.wolf.system.Environment;
import org.wolf.undoredo.UndoRedo;
import org.wolf.undoredo.UndoRedoAuthors;
import org.wolf.undoredo.UndoRedoCopyright;
import org.wolf.undoredo.UndoRedoData;
import org.wolf.undoredo.UndoRedoLanguage;

/**
 *
 * Class to listen for SoundPanel button depressions.
 */
public class DictionaryListener implements ActionListener, PropertyChangeListener
{
    protected JLabel            label;   // Label for error messages
    private   RootDictionaryPanel  dictionaryPanelProperties;
    
    
    // Principle data maintained.
    private   ActionEvent  event;      // Is event in process?

    /** Creates a new instance of ButtonListener
     *  @param label JLabel to display errors and messages
     *  @param toolbar ScrollableToolbar to hold language buttons
     */
    public DictionaryListener(JLabel label, ScrollableToolbar toolbar)
    {   this.label      = label;
        
        // Get the ACORNS property change listener.
        PropertyChangeListener[] pcl 
              = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                               ("DictionaryListeners");
        dictionaryPanelProperties = (RootDictionaryPanel)pcl[0];

        toolbar.addPropertyChangeListener(this);
    }  // End SoundListener constructor.
    
    public void propertyChange(PropertyChangeEvent event)
    {   String command = event.getPropertyName();
        if (command.equals("select") || command.equals("active"))
        {   
        	getDisplayPanel().enableDictionary(false);
        	getDisplayPanel().getDict().setListOfLanguages();
            getDisplayPanel().reloadDictionary(); 
        }
    }


    /**  Listener to respond to sound commands in a separate thread
     *   @param e triggered by a sound command
     */
    public void actionPerformed(ActionEvent actionEvent)
    {    processThread(actionEvent);
         return;
    }
    
    /**  Listener to respond to sound commands in a separate thread
     *   @param e triggered by a sound command
     */
    public void processThread(ActionEvent actionEvent)
    {
        if (event!=null)
        {
            label.setText("System is busy - try again later");
            return;
        }
                 
        event = actionEvent;
        label.setText("Processing - please wait");
        SwingUtilities.invokeLater(new ProcessDictionaryListener());
    }

    class ProcessDictionaryListener extends Thread
    { 
        /** The thread run method to process the command. */
        public @Override void run()
        {
            String result = processCommand(event);
            if (result==null) label.setText("Command not implemented");
            else if (!result.equals("")) 
            			label.setText(result);
                 else label.setText("Processing complete");
            getDisplayPanel().enableDictionary(true);
            event = null;
        }

        /**  Listener to respond to sound commands
         *   @param e triggered by a sound command
         *   @return string to set in label, "" if none, null if command unknown
         */
         public synchronized String processCommand(ActionEvent e)
         {
            DictionaryData dictionary 
                    = dictionaryPanelProperties.getDictionaryData();
            Language language = dictionary.getActiveLanguage();

            Component component = (Component)e.getSource();

            JTextField whichField = null;
            if (component instanceof JTextField) 
                whichField = (JTextField)component;
             
            UndoRedo undoRedo = dictionaryPanelProperties.getUndoRedo();
            JFrame root = Environment.getRootFrame();

            String componentName = component.getName();
            char category = componentName.charAt(1);
            char option   = componentName.charAt(0);
            String msg;
            File file;
            
            try
            {
	            switch(category)
	            {   case 'f':              // File commands
	                    switch (option)
	                    {
	                       case 'l':        // Load
	                           msg =  loadImport(dictionary, ChooseFileDialog.LOAD);
	                           if (msg.equals("")) enableGhostedButtons(true);
	                           return msg;
	
	                        case 'c':       // Close
	                            if (!dictionary.checkDirty())
	                                return "Operation canceled";
	
	                            getDisplayPanel().enableDictionary(false);
	                            msg = dictionary.closeFile();
	                            if (msg.length()>0) return msg;
	                            
	                            dictionary = new DictionaryData();
	                            dictionaryPanelProperties.setDictionaryData
	                                                                   (dictionary);
	                            getDisplayPanel().loadDictionary();
	                            undoRedo.resetRedoUndo();
	                            enableGhostedButtons(false);
	                            ChooseFileDialog.resetFileName();
	                            return "Dictionary file closed";
	
	                       case 'i':        // Import
	                           msg =  loadImport
	                                          (dictionary, ChooseFileDialog.IMPORT);
	                           if (msg.equals("")) enableGhostedButtons(true);
	                           return msg;
	                       
	                       case 'w':        // Web page
	                            ChooseFileDialog wDialog;
	                            wDialog = new ChooseFileDialog
	                                            (root, ChooseFileDialog.WEB); 
	
	                            file = wDialog.getSelectedFile();
	                            msg = dictionary.exportXML(file); 
	                            return msg;
	                    	   
	
	                       case 'e':        // Export
	                            ChooseFileDialog xDialog;
                            	xDialog = new ChooseFileDialog
	                                        (root, ChooseFileDialog.EXPORT);
	
	                            file = xDialog.getSelectedFile();
	                            msg = dictionary.exportXML(file); 
	                            return msg;
	                            
	                       case 'm':
	                    	    ChooseFileDialog mDialog;
	                    	    mDialog = new ChooseFileDialog(root, ChooseFileDialog.MOBILE);
	                    	    
	                            file = mDialog.getSelectedFile();
	                            msg = dictionary.exportXML(file); 
	                    	    return msg;
	                            
	                        case 's':       // Save
	                            ChooseFileDialog sDialog;
	                            sDialog = new ChooseFileDialog
	                                                (root, ChooseFileDialog.SAVE); 
	
	                            file = sDialog.getSelectedFile();
	                            msg = dictionary.writeFile(file);
	                            return msg;
	                    }
	                 break;   // End of File Commands.
	
	                 case 's':             // Store data commands
	                    switch (option)
	                    {
	                        case 's':        // Sort the dictionary
	                            if (language!=null) language.sortLanguage();
	                            getDisplayPanel().reloadDictionary();
	                            undoRedo.resetRedoUndo();
	                            return "Sort operation complete";
	
	                        case 't':       // toggle phonetics
	                        	if (getDisplayPanel().togglePhonetics())
	                                 return "The phonetics display is active";
	                            else return "The indigenous display is active";
	
	                        case 'c':       // copyright
	                            CopyrightDialog cDialog 
	                               = new CopyrightDialog(root, dictionary);
	                            String result = cDialog.getDescription();
	                            if (result != null)  
	                            {   
	                            	String old = dictionary.getCopyright();
	                                UndoRedoCopyright copyPush
	                                        =  new UndoRedoCopyright(old, result);
	                                dictionary.push(copyPush);
	                                dictionary.setCopyright(result);
	                                return "The copyright update was successful";
	                            }
	                            break;
	                                                         
	                        case 'a':       // author
	                            AuthorDialog aDialog 
	                                    = new AuthorDialog(root, dictionary);
	                            Author[] newAuthors = aDialog.getAuthors();
	                            Author[] old = dictionary.getAuthors();
	                            if (newAuthors != null)
	                            {  UndoRedoAuthors authorPush =
	                                    new UndoRedoAuthors(old, newAuthors);
	                               dictionary.push(authorPush);
	                               dictionary.setAuthors(newAuthors);
	                               return "The author list update was successful";
	                            }
	                            break;
	                                                        
	                        case 'l':       // language
	                           Language[] oldLangs = dictionary.getLanguages();
	                           LanguageDialog lDialog
	                                         = new LanguageDialog(root, dictionary);
	                           Language[] languages = lDialog.getLanguages();
	                           if (languages != null)
	                           {   if (lDialog.isRemove())
	                               {  if (!areYouSure())
	                                  {  return "Operation canceled"; }
	                               }
	
	                               getDisplayPanel().enableDictionary(false);
	                               UndoRedoLanguage undoRedoLanguage
	                                    = new UndoRedoLanguage(oldLangs,languages);
	                               dictionary.setLanguages(languages);
	                               dictionary.push(undoRedoLanguage);
	                               dictionary.sortLanguages();
	                               Language active = dictionary.getActiveLanguage();
	                               updateToolbar(dictionary);
	                               if (active!=null)
	                               {  
	                            	   getToolbar().setActiveButton
	                                                     (active.getLanguageCode());
	                            	   setSearchFont();
	                            	  
	                               }
	                               getDisplayPanel().reloadDictionary();
	                               enableGhostedButtons(languages.length!=0);
	                               return "The language list update was successful";
	                           }
	                           break;
	                           
	                        case 'd':		// Dictionary template output formats
	                        	new TemplateDialog(root, dictionary);
	                        	break;
	                            
	                        case 'o':       // ontology terms
	                           OntologyData ontology = dictionary.getOntologyData();
	                           if (ontology==null || ontology.getOntologyTree()==null)
	                           {  return "The GOLD Ontology is not available";  }
	
	                           new OntologyDialog(root, dictionary);
	                           return "GOLD ontology updates complete";
	                    }
	                    return "Operation cancelled";
	
	                case 'v':     // Forward, back and refresh operations.
	                    if (language==null) return "No language is active";
	                      
	                    DictionaryView view  = language.getView();
	                    if (view==null) return "Use search mode first";
	                    try
	                    {   getDisplayPanel().enableDictionary(false);
	                        switch (option)
	                        {  case 'b':  // previous view
	                               view.previousView();
	                               break;
	                            case 'f':  // next view
	                                view.nextView();
	                                break;
	                            case 'r':  // reload dictionary
	                                view.resetView();
	                                break;
	                            default: return null;
	                        }
	                        getDisplayPanel().reloadDictionary();
	                        return "";
	                    }
	                    catch (NoSuchElementException nse) 
	                    {  msg = "No language view exists"; }
	                    getDisplayPanel().reloadDictionary();
	                    return msg;
	                    
	               case 'w':
	                   switch (option)
	                   {  case 's':
	                           String pattern = whichField.getText();
	                           if (pattern.length()==0)
	                               return "Please enter a regular "
	                                       + "expression search pattern";
	
	                           if (language==null)
	                           {  return "There is no active language"; }
	
	                           getDisplayPanel().enableDictionary(false);
	                           language.setView(pattern);
	                           getDisplayPanel().reloadDictionary();
	                           return "";
	
	                       default: return null;
	                   }
	                   
	                 case 'p':
	                     switch (option)
	                     {   case 'p':
	                             try
	                             {  
	                                 String output = dictionary.exportXML(null);
	                                 if (output.indexOf("<html>")>=0)
	                                 {
	                                 	PrintDialog print = new PrintDialog();
	                                 	return print.printHTML(output);
	                                 }
	
	                                 PrinterJob job = PrinterJob.getPrinterJob();
	                                 PageFormat format = job.defaultPage();
	
	                                 File printFile = getEnv().getSelectedFile();
	                                 job.setJobName(printFile.getAbsolutePath());
	
	                                 DictionaryPrintable printable
	                                      = new DictionaryPrintable
	                                                    (output, format, printFile);
	                       	         job.setPrintable(printable);
	                                 if (!job.printDialog())
	                                 {
	                                    return "Print Job Canceled";
	                                 }
	                                
	                                 job.print();
	                             }
	                             catch (NoSuchElementException exception)
	                             { return "This command requires " 
	                                       + "an active language"; }
	                             catch (PrinterException exception)
	                             { return "Error: Printer Failure"; }
	                             return "Print Job Started";
	
	                         case 'v':
	                             try
	                             {   
	                                String output = dictionary.exportXML(null);
	                                if (output.indexOf("<html>")>=0)
	                                {
	                                	new PrintDialog(root, output);
	                                	return "";
	                                }
	
	                            	 PrinterJob printJob
	                                                   = PrinterJob.getPrinterJob();
	                                 PageFormat format = printJob.defaultPage();
	
	                                 File printFile = getEnv().getSelectedFile();
	                                 DictionaryPrintable printable
	                                      = new DictionaryPrintable
	                                                    (output, format, printFile);
	                                 new PrintPreview(printable, format,
	                                         "Wolf dictionary output");
	                             }
	                             catch (NoSuchElementException exception)
	                             { return "There is no active language"; }
	                             catch (PrinterException exception)
	                             { return "Printer failure"; }
	                             return "";
	                             
	                         default: return null;
	                     }
	                 
	                case 'o':              // Redo and undo previous operations.
	                    switch (option)
	                    {  
	                       case 'd':       // Dictionary options
	                          OptionsDialog oDialog 
	                                  = new OptionsDialog(root, dictionary);
	                          if (oDialog.optionsConfirmed())
	                             return   "The dictionary options" 
	                                       + " were successfully updated";
	                          else return "Operation canceled";
	    
	                        case 'r':        // Redo
	                         UndoRedoData redoData = undoRedo.peekRedo();
	                         if (redoData==null) return "Nothing to redo";
	                         if (redoData instanceof UndoRedoLanguage)
	                                getDisplayPanel().enableDictionary(false);
	                         dictionary.redo(getDisplayPanel());
	                         
	                         if (redoData instanceof UndoRedoLanguage)
	                         {   
	                        	 getDisplayPanel().enableDictionary(false);
	                             updateToolbar(dictionary);
	                             getDisplayPanel().reloadDictionary();
	
	                             Language[] languages = dictionary.getLanguages();
	                        	 enableGhostedButtons(languages != null && languages.length!=0);
	                         }
	                         break;
	
	                       case 'u':        // Undo
	                         UndoRedoData undoData = undoRedo.peekUndo();
	                         if (undoData==null) return "Nothing to undo";
	                         if (undoData instanceof UndoRedoLanguage)
	                                getDisplayPanel().enableDictionary(false);
	                         dictionary.undo(getDisplayPanel());
	
	                         if (undoData instanceof UndoRedoLanguage)
	                         {   getDisplayPanel().enableDictionary(false);
	                             updateToolbar(dictionary);
	                             getDisplayPanel().reloadDictionary();
	
	                             Language[] languages = dictionary.getLanguages();
	                        	 enableGhostedButtons(languages != null && languages.length!=0);
	                         }
	                         break;
	
	                       default: return null;
	                    }
	
	                break;  // End of operation commands.
	
	                default: return null;
	             }   // End of command categories.
            }
            catch (FileNotFoundException fnf)
            {
            	return fnf.getMessage();
            }
            catch (Throwable cause)
            { 
            	StackTraceElement[] elements = cause.getStackTrace();
            	String className = elements[0].getClassName();
            	String methodName = elements[0].getMethodName();
            	int line = elements[0].getLineNumber();
            	String message = cause.getMessage();
            	return className + "." + methodName + ": line = " + line + " " + message; 
            }
             return "";

         }   // End actionPerformed()
        
    }     // End of ProcessThread class

    /** Load or import a dictionary */
    private String loadImport(DictionaryData dictionary, int option)
    {   if (!dictionary.checkDirty())
           return "Operation canceled";

        ChooseFileDialog dialog;
        JFrame root = Environment.getRootFrame();

        try { dialog = new ChooseFileDialog(root, option); }
        catch (Exception ex) 
        { return ex.getMessage(); }
        File file = dialog.getSelectedFile();
        String msg;

        getDisplayPanel().stopEditing();
        getDisplayPanel().enableDictionary(false);
        msg = dictionary.closeFile();

        try
        {   if (option == ChooseFileDialog.LOAD)
                 msg = dictionary.readFile(file);
            else msg = dictionary.importXML(file);
        }
        catch (Exception e) 
        { 
        	return "Illegal Dictionary Format"; 
        }

        if (msg==null || msg.equals(""))
        {   dictionary = dictionary.getDictionary();
            dictionaryPanelProperties.setDictionaryData(dictionary);
            UndoRedo  undoRedo = dictionaryPanelProperties.getUndoRedo();
            undoRedo.resetRedoUndo();
            updateToolbar(dictionary);
            getDisplayPanel().enableDictionary(true);
            return "";
        }
        else return msg;
    }

    /** Process drops of dictionary objects */
    public void dropDictionary(File file)
    {
        DictionaryData dictionary
                    = dictionaryPanelProperties.getDictionaryData();
        if (dictionary==null) dictionary = new DictionaryData();

        String msg;
        if (!dictionary.checkDirty())
        {   label.setText("Operation canceled");
            return;
        }

        getDisplayPanel().stopEditing();
        getDisplayPanel().enableDictionary(false);
        msg = dictionary.closeFile();

        getEnv().setPath(file, Environment.SAVE_DICT);
        String extension = file.getName();
        extension = extension.substring(extension.lastIndexOf(".")+1);
        try
        {   if (extension.equals("adct")) msg = dictionary.readFile(file);
            else msg = dictionary.importXML(file);

            if (msg.equals(""))
            {   dictionary = dictionary.getDictionary();
                dictionaryPanelProperties.setDictionaryData(dictionary);
                UndoRedo  undoRedo = dictionaryPanelProperties.getUndoRedo();
                undoRedo.resetRedoUndo();
                updateToolbar(dictionary);
                msg = "Drop operation successful";
            }
        }
        catch (Exception e) { msg = e.toString(); }
        label.setText(msg);
    }

        /** Method to update the language toolbar to only include the needed buttons
     *
     * @param toolbar The Scrollable toolbar object
     */
    private void updateToolbar(DictionaryData dictionary)
    {
        Language languageObject;
        String code;
        Vector<String>codes = new Vector<String>();

        // Add the missing buttons
        ScrollableToolbar toolbar = getToolbar();
        toolbar.reset();
        Vector<Language> languages = dictionary.getSavedLanguages();
        for (int b=0; b<languages.size(); b++)
        {
            languageObject = languages.get(b);
            code = languageObject.getLanguageCode();
            toolbar.addButton(code, "language.png");
            codes.add(code);
        }

        // Remove the buttons no longer needed
        toolbar.purgeButtons(codes);

        toolbar.setSelectedButtons(dictionary.getSelectedLanguages());
        String activeLanguage = dictionary.getSavedLanguage();
        if (activeLanguage != null) 
        	toolbar.setActiveButton(activeLanguage);
        setSearchFont();
        toolbar.validate();
    }

    /** Determine if non-recoverable operation should proceed
     *
     * @return true if yes, false to cancel the operation
     */
    public boolean areYouSure()
    {   int ok = JOptionPane.showConfirmDialog(Environment.getRootFrame()
                , "Data lost from this operation cannot be recovered - Proceed?"
                , "Wolf Dictionary - Are you sure dialog"
                , JOptionPane.YES_NO_CANCEL_OPTION
                , JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION)  { return true; }
        return false;
    }

    /** Get the system toolbar of languages */
    public ScrollableToolbar getToolbar()
    {  return dictionaryPanelProperties.getToolbar(); }

    /** Get the dictionary display panel */
    public DictionaryDisplayPanel getDisplayPanel()
    {  return dictionaryPanelProperties.getDisplayPanel(); }

    /** Get the environment object */
    private Environment getEnv()
    {  return dictionaryPanelProperties.getEnv(); }

    /** Enable or disable the ghostable buttons */
    private void enableGhostedButtons(boolean flag)
    {
        DictionaryPanel buttons = getDictionaryPanelProperties().getButtonPanel();
        buttons.enableButtons(flag);
    }

    /** Configure the font for the search button */
    private void setSearchFont()
    {
    	DictionaryPanel buttons = getDictionaryPanelProperties().getButtonPanel();
        DictionaryData dictionary = getDictionaryPanelProperties().getDictionaryData();
        
        Language language = dictionary.getActiveLanguage();
    	buttons.setSearchFont(language);
    }
    
    /** Get dictionary panel properties */
    private RootDictionaryPanel getDictionaryPanelProperties()
    {
        PropertyChangeListener[] pcl 
        = Toolkit.getDefaultToolkit().getPropertyChangeListeners
                                         ("DictionaryListeners");
        dictionaryPanelProperties = (RootDictionaryPanel)pcl[0];
        return dictionaryPanelProperties;

    }

}   // End DictionaryListener class
