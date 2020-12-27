/*
 * Ontology.java
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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wolf.lib.DeepCopy;

/** Dictionary ontology items */
public class Ontology extends Item implements Serializable, Cloneable
{   private static final long serialVersionUID = 1;

    private String parent, value, abbrev, data, phonetics;
    private int type;

    /** Constructor to initialize object with fixed data
     * 
     * @param parent The Ontology paradigm
     * @param value The Pntology label
     * @param abbrev The abbreviation of the label for output
     * @param data The indigenous form
     * @param type The type (constants in Constants interface)
     */
    public Ontology(String parent, String value, String abbrev, String data, int type)
    {
    	this();
    	this.parent = parent;
    	this.value  = value;
    	this.abbrev = abbrev;
    	this.data   = data;
    	this.type   = type;
    }

    /** Default Constructor */
    public Ontology()
    {  super();
       parent = value = data = abbrev = phonetics = "";
    }

    /** Create based on another ontology item */
    public Ontology(Ontology ontology)
    {  super(ontology);
       type = ontology.type;
       parent = value = data = abbrev = phonetics = "";
    }

    /** Set Ontology parent (ex: tense, mood, part of speech) */
    public void setParent(String p) { parent = p; }
    /** Get Ontology parent (ex: tense, mood, part of speech) */
    public String getParent() { return parent; }

    /** Set Ontology value (ex: past, present, future) */
    public void setValue(String v) { value = v; }
    /** Get Ontology parent (ex: past, present, future) */
    public String getValue() { return value; }

    /** Set Ontology abbreviation (ex: pos for part of speech) */
    public void setAbbrev(String a) 
    { abbrev = a; }
    /** Get Ontology parent (ex:  (ex: pos for part of speech) */
    public String getAbbrev() { return abbrev; }

    /** Set Ontology data (ex: threw, ran, fell) */
    public void setData(String d)  { data = d; }
    /** Get Ontology data (ex: threw, ran, fell) */
    public String getData() { return data;  }

    /** Set phonetic representation of the ontology data */
    public void setPhonetics(String p) { phonetics = p; }
    /** Get phonetic representation of the ontology data */
    public String getPhonetics() { return phonetics; }

    /** Set ontology category */
    public void setType(int t) { type = t; }
    /** Get ontology category */
    public int getType() { return type; }

   /** Create the XML that represents this dictionary element */
   public Element exportXML(Document doc, File file)
   {  ArrayList<String[]> attributes = new  ArrayList<String[]>();
 
      attributes.add(new String[]{"parent", parent});
      attributes.add(new String[]{"child", value} );
      attributes.add(new String[]{"abbreviation", abbrev} );
      attributes.add(new String[]{"phonetics", phonetics} );
      attributes.add(new String[]{"type", ""+type} );
      Element node = makeNode(doc, "ontology", attributes);
      exportStyle(node);
      node.setTextContent(data);

      return node;
   }

   /** Configure this dictionary element based on XML specifications */
   public void importXML(Element node, File file)
   {   importStyle(node);
       parent = node.getAttribute("parent").trim();
       value = node.getAttribute("child").trim();
       abbrev = node.getAttribute("abbreviation").trim();
       phonetics = node.getAttribute("phonetics").trim();

       String stringType = node.getAttribute("type").trim();
       if (stringType.length()>0)
          try { type = Integer.parseInt(stringType); } catch(Exception e) {}
       data = node.getTextContent().trim();
   }

    /**  Make a deep identical copy; necessary for undo and redo operations */
    public @Override Object clone() { return DeepCopy.copy(this);  }

   public @Override boolean equals(Object object)
   {   if (object instanceof Ontology)
       {   Ontology ontology = (Ontology)object;
           if (!super.isEqual(ontology))              return false;
           if (!parent.equals(ontology.parent))       return false;
           if (!value.equals(ontology.value))         return false;
           if (!data.equals(ontology.data))           return false;
           if (!abbrev.equals(ontology.abbrev))       return false;
           if (!phonetics.equals(ontology.phonetics)) return false;
           if (type != ontology.type)                 return false;
           return true;
       }
       return false;
   }

   public @Override int hashCode()
   {  return (parent+value+data+abbrev+phonetics).hashCode()
             + 31*type + super.hashCode();
   }
}   // End of Ontology class
