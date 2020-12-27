/*
 * CellSizeDialog.java
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

package org.wolf.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/** Dialog to set the size of a dictionary cell */
public class CellSizeDialog extends JDialog
{   
	private static final long serialVersionUID = 1L;

	JTextField field;
    boolean confirm;

    /** Dialog to adjust cell width
     *
     * @param root The root frame to which to attach this dialog
     * @param width The current cell width
     */
    public CellSizeDialog(JFrame root, int width)
    {  super(root, true);

       setModal(true);
       confirm = false;
       setTitle("Type cell width");
       setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
       setLocationRelativeTo(root);

       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
       panel.add(Box.createHorizontalGlue());
       panel.add(new JLabel("Size: "));
       field = new JTextField("" + width);
       field.setMaximumSize(new Dimension(60, 25));
       field.setPreferredSize(new Dimension(60, 25));
       field.setFont(new Font(null, Font.PLAIN, 14));
       panel.setSize(new Dimension(125, 30));
       panel.add(field);
       panel.add(Box.createHorizontalGlue());

       JButton accept = new JButton("Accept");
       accept.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {   confirm = true;
                  setVisible(false);
                  dispose();
              }
           });

       JButton cancel = new JButton("Cancel");
       cancel.addActionListener(
           new ActionListener()
           {  public void actionPerformed(ActionEvent event)
              {   confirm = false;
                  setVisible(false);
                  dispose();
              }
           });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(accept);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalGlue());

       Container entry = getContentPane();
       entry.setLayout(new BoxLayout(entry, BoxLayout.Y_AXIS));
       entry.add(Box.createVerticalStrut(15));
       entry.add(panel);
       entry.add(Box.createVerticalStrut(15));
       entry.add(buttonPanel);
       entry.add(Box.createVerticalStrut(15));
       pack();
       setVisible(true);
    }

    /** Get the desired width of the field */
    public int getSelectedWidth()
    {  if (confirm)
       {   try
           {  return Integer.parseInt(field.getText());  }
           catch (NumberFormatException e)
           {   Toolkit.getDefaultToolkit().beep();
               return -1;  /// Illegal field width
           }
       }
       return -1;
    }
}   // End of CellSize dialog
