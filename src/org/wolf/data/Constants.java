/*
 * Constants.java
 *    This clas hold constants used by various WOLF modules
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
package org.wolf.data;

import java.awt.Color;

/** Define various constants used by various dictionary modules */
public interface Constants
{   // The current WOLF version
    public final static String version = "3.0.2";

    /** Ontology one line: leaf name */
    public final static int ONTOLOGY_VALUE = 0;

    /** Ontology one line: parent & leaf names */
    public final static int ONTOLOGY_PARENTVALUE = 1;

    /** Ontology two lines: (1) parent name; (2) leaf name */
    public final static int ONTOLOGY_PARENT_VALUE = 2;

    /** Ontology two lines: (1) leaf name; (2) input data */
    public final static int ONTOLOGY_VALUE_DATA = 3;

    /** Ontology two lines: (1) parent and leaf names; (2) input data */
    public final static int ONTOLOGY_PARENTVALUE_DATA = 4;
    
    /** Ontology one line: input data */
    public final static int ONTOLOGY_DATA = 5;

    /** Names of Ontology field icons */
    public final static String[] ONTOLOGY_NAMES
            = {"Ontology_Value.jpg", "Ontology_ParentValue.jpg",
               "Ontology_Parent_Value.jpg", "Ontology_Value_Data.jpg",
               "Ontology_ParentValue_Data.jpg", "Ontology_Data.jpg"};
    
    /** Index order of SIL language codes (vernacular, index, national, regional) */
    public final static String SIL_CODES = "venr";

    /** The default language names when not supplied when importing SFM files */
	public static final String[] DEFAULT_LANGUAGES = 
		{"ver Vernacular", "eng English", "nat National", "reg Regional"};


    
    /** Number of characters of all standard SIL language codes */
    public final static int LANGUAGE_CODE_SIZE = 3;

    /** Gap between font size height and text components */
    public final static int GAP = 7;

    /** Default widget field height */
    public final static int FIELD_HEIGHT = 20;

    public final static Color WIDGET_BACKGROUND = new Color(192,192,192);
    public final static Color WIDGET_FOREGROUND = Color.BLACK;
    
	/** Default field height */
    public final static int TEXT_HEIGHT = 20;

    /** Default width for column widgets */
    public final static int COLUMN_WIDGET_WIDTH = 200;

    /** Default width for row widgets */
    public final static int ROW_WIDGET_WIDTH = 900;

    /** Default width for definition widgets */
    public final static int DEFINITION_WIDGET_WIDTH = 500;

    /** Default height for text area widgets (when expanded) */
    public final static int AREA_WIDGET_HEIGHT = 200;

    /** Default widget height */
    public static final int WIDGET_HEIGHT = 50;

    /** Default with if example row widgets */
    public static final int EXAMPLE_WIDGET_WIDTH = 800;


}  // End of Constants interface
