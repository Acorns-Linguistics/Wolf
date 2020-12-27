/*
 * OntologyNode.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.wolf.lib.DeepCopy;

/** An Ontology Node consisting of an ontology category and possible values */
public class OntologyNode implements Serializable, Comparable<OntologyNode>
{   
	private static final long serialVersionUID = 1;
   
	private static final String[] filterTerms =
    { "thing", "abstract", "object", "system", "taxon", "datastructure"
    , "language", "linguistic", "orthographic", "glyph"
    , "specification", "syntactic"};

    private String name;
    private ArrayList<OntologyNode> children;
    private String abbreviation;
    private OntologyNode parent;
    private boolean disabled;

    /** Create a new Ontology node
     *
     * @param parent The link to the parent node
     * @param name The name of this node
     */
    public OntologyNode(OntologyNode parent, String  name)
    {   this.parent        = parent;
        this.name         = name;
        this.abbreviation = name;
        this.children     = null;
        this.disabled     = false;
    }

    /** Check how search key compares to this node
     *
     * @param key Key to compare to
     * @return <0 if less, = if same, >0 if greater
     */
    public int compareTo(OntologyNode key)
    { 
      OntologyNode node = (OntologyNode)key;
          return name.compareTo( node.getName() );
    }
    
    public int compareTo(String key)
    {
    	return name.compareTo(key);
    }

    /** Method to get the name of this node (The key) */
    public String getName() { return name; }

    /** Return the array of child keys */
    public ArrayList<OntologyNode> getChildren() { return children; }

    /** Set the array of children nodes
     *
     * @param c ArrayList of child ontology nodes
     */
    public void setChildren(ArrayList<OntologyNode> c)
    {   if ((c==null) || (children==null)) children = c;
        else
        {  for (int i=0; i<c.size(); i++)
           {  if (children.contains(c.get(i))) continue;
              children.add(c.get(i));
           }
        }
        Collections.sort(children);
    }

    /** Get the parent node */
    public OntologyNode getParent() { return parent; }

    /** Set the link to the parent node */
    public void setParent(OntologyNode node) { parent = node; }

    /** Get the abbreviation associated with this node */
    public String getAbbreviation() 
    { 
    	return abbreviation; 
    }

    /** Set the abbreviation associated with this node */
    public void setAbbreviation(String abbreviation)
    { 
    	if (abbreviation.length() != 0) 
    	  this.abbreviation = abbreviation; 
    }

    /** Get boolean flag to determine if this node is active */
    public boolean getDisabled() { return disabled; }

    /** Set whether this node is active */
    public void setDisabled(boolean disabled) { this.disabled = disabled; }

    /** Get list of Ontology categories
     *
     * @param list The list of categories to which we should append
     * @return The updated list
     */
    public ArrayList<OntologyNode> getList(ArrayList<OntologyNode> list)
    {   if (isLeaf()) return list;

        OntologyNode child;
        boolean anyLeafs = false;
        ArrayList<OntologyNode>nodes = getChildren();

        for (int i=0; i<nodes.size(); i++)
        {  child = nodes.get(i);
           if (child.isLeaf()) { anyLeafs = true; }
           list = child.getList(list); // Recursive call to traverse child nodes
        }

        // See if this node should be filtered from the list.
        OntologyNode parentNode = getParent();
        if (parentNode==null) return list;
        for (int i=0; i<filterTerms.length; i++)
        {   if (getName().toLowerCase().indexOf(filterTerms[i])>=0)
                  return list; 
        }
        if (anyLeafs)  {  list.add(this); }
        return list;
    }

    /** Format a parent name for output
     *  @return formatted parent name
     */
    public String getFormattedParent()
    {  return format(getName().replaceAll("Property", ""));  }

    /** Format a child node for output
     * @return formatted child name
     */
    public String getFormattedChild()
    {   String childName = getName().replaceAll("Property", "");
        if (getParent()==null) return childName;

        String parentName = getParent().getName().replaceAll("Property", "");
        childName = formatChildNode(parentName, childName);
        return childName;
    }

    /** Determine if this is a leaf node */
    public boolean isLeaf()
    { return getChildren() == null;   }

    /** Create string representation of this object for debugging purposes */
    public @Override String toString()
    {   StringBuffer buf = new StringBuffer();
        return print(buf, 0).toString();
    }
    /** Traverse and create a string representation of the tree
     *
     * @param buf Object to contain the string data
     * @param indent Characters to indent this node
     * @return StringBuffer representation
     */
    private StringBuffer print(StringBuffer buf, int indent)
    {   String spaces = "                              ";
        buf.append(spaces.substring(0,indent) + name + "\n");
        if (children!=null)
        {  for (int i=0; i<children.size(); i++)
           {   children.get(i).print(buf, indent + 3); }
        }
        return buf;
    }

    /** Format the child node by removing redundancies
     *
     * @param parent The name of the parent
     * @param child The name of the child
     * @return The formated child name
     */
    private String formatChildNode(String parent, String child)
    {  String entry = child;
       if (child.equals(parent)) return child;

       String[] parentWords = format(parent).split(" ");
       String[] childWords  = format(child).split(" ");
       entry = "";

       boolean match; 
       for (int c=0; c<childWords.length; c++)
       {  match = false;
          for (int p=0; p<parentWords.length; p++)
          {  if (parentWords[p].equals(childWords[c]))
             {   match = true;   break;  }
          }
          if (!match) entry += ((c==0)?"":" ") + childWords[c];
       }
       return entry;
    }

    /** Format by adding a space before each upper case letter
     *
     * @param name The original name
     * @return The name formated with spaces
     */
    private String format(String name)
    {   if (name.length()==0) return name;
        
        StringBuffer buf = new StringBuffer();
        buf.append(name.charAt(0));
        for (int i=1; i<name.length(); i++)
        {  if (Character.isUpperCase(name.charAt(i))) buf.append(' ');
           buf.append(name.charAt(i));
        }
        return buf.toString();
    }

    public @Override Object clone()  {  return DeepCopy.copy(this);   }

    public @Override boolean equals(Object o)
    {  if (o instanceof OntologyNode)
       {   OntologyNode node = (OntologyNode)o;
           return node.name.equals(name);
       }
       return false;
    }

    public @Override int hashCode() { return name.hashCode(); }

}           // End of OntologyNode class
