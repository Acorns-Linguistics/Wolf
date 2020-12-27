/*
 * OntologyData.java
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

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wolf.lib.DeepCopy;
import org.wolf.system.Environment;

/** Create GUI components to represent the GOLD ontology  */
public class OntologyData implements Serializable, Cloneable
{   
	private static final long serialVersionUID = 1;

    private OntologyNode ontologyRoot;
    
    /** Constructor to parse the gold Ontology and prepare for creating GUIs
     * 
     * @param gold The path to the gold Ontology
     * @param root Root to the gold Ontology tree
     */
    public OntologyData(String gold, OntologyNode root)
    {   
    	ontologyRoot = root;
        try
        { 
        	if (root==null)
        	{
				String fileName = "/resources/" + gold;
	            URL url = getClass().getResource(fileName);
	            URLConnection urlConn = url.openConnection();
	            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder builder = factory.newDocumentBuilder();
	
	            InputStream stream = urlConn.getInputStream();
	            Document document = builder.parse(stream);
	            stream.close();
	            importDocument(document);
        	}
        }
        catch (Exception e) 
        {  
        	javax.swing.JOptionPane.showMessageDialog(Environment.getRootFrame(), e.toString());
        }
    }

    /** Create an internal representation of the Gold ontology
     *
     * @param document The XML parsed document
     */
    private void importDocument(Document document)
    {  Element root = document.getDocumentElement();
       root.normalize();
       NodeList concepts = root.getElementsByTagName("concept"), labelNodes;

       // Table of parent nodes and all their children
       Hashtable<String, ArrayList<String>> ontology;
       ontology = new Hashtable<String, ArrayList<String>>();

       Element node;
       String parent, label;
       ArrayList<String> data;
       int length = concepts.getLength();
       
       for (int i=0; i<length; i++)
       {  try
          {   
    	   	  node = (Element)concepts.item(i);
              labelNodes = node.getElementsByTagName("label");
              if (labelNodes.getLength()==0) continue;

              parent = node.getAttribute("parent");
              if (parent.length()==0) continue;

              label = labelNodes.item(0).getTextContent();
              parent = parent.substring(parent.lastIndexOf('/') + 1).trim();
              if (parent.length()==0) parent = "General";
              
              label  = label.trim();
              data = (ArrayList<String>)ontology.get(parent);
              if (data == null) data = new ArrayList<String>();
              if (!data.contains(label))
              {  data.add(label);
                 ontology.put(parent, data);
              }
          } catch (Exception e) {}
       }

       // Create the Ontology tree
       ontologyRoot = makeTree(ontology);
    }

    /** Get hash table of user ontology nodes
     *
     * @return Hash table object (Entries: category name, list of children
     */
    public ArrayList<OntologyNode>getOntologyList()
    {   ArrayList<OntologyNode> list = new ArrayList<OntologyNode>();
        if (ontologyRoot!=null)
        {  ontologyRoot.getList(list);
           Collections.sort(list);
        }
        return list;
    }

    /** get the Gold oontology tree */
    public OntologyNode getOntologyTree() { return ontologyRoot; }

    /** Create a tree structure of all the nodes
     *
     * @param table The hash table contining the parent and child keys
     */
    private OntologyNode makeTree(Hashtable<String, ArrayList<String>> table)
    {  // Create stack for depth first creation of the ontology nodes.
       OntologyNode[] stack = new OntologyNode[table.size()];
       int top = -1;

       OntologyNode parent = null;
       String child;
       ArrayList<String> children;
       ArrayList<OntologyNode> nodes;

       OntologyNode root = ontologyRoot;
       // The following causes a problem if Gold is modified
       //  if (root==null) 
    	   root = new OntologyNode(null,"owl#Thing");
       stack[++top] = root;

       while (top>=0)
       {   parent = stack[top--];
           children = table.get(parent.getName());
           if (children==null) continue;

           nodes = new ArrayList<OntologyNode>();

           for (int i=0; i<children.size(); i++)
           {   child = children.get(i);
               stack[++top] = new OntologyNode(parent, child);
               if (!nodes.contains(stack[top]))  
            	   nodes.add(stack[top]);
           }
           parent.setChildren(nodes);
       } 
       return root;
    }  // End of makeTree.

   public @Override Object clone()  {  return DeepCopy.copy(this);   }

}      // End of OntologyData class
