package org.wolf.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.wolf.data.DictionaryData;

public class PrintDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private static final int SIZE = 150, GAP = 20;
	
	JEditorPane printPanel;
	JLabel label;
    DictionaryData dictionary;
    
    public PrintDialog()
    {
    	
    }

    /** Print and Print Preview dialog
     *
     * @param root The root frame to which to attach this dialog
     * @param The html text to be displayed
     */
    public PrintDialog(JFrame root, String html)
    {   
    	super(root, true);
    	
        setModal(true);
        
        PrinterJob printJob
        	= PrinterJob.getPrinterJob();
        PageFormat format = printJob.defaultPage();
        Dimension panelSize = new Dimension((int)format.getWidth(), (int)format.getHeight());
        setSize(panelSize);
        setPreferredSize(panelSize);
        setMaximumSize(panelSize);
        setLocationRelativeTo(root);
       
        setTitle("Print Preview");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        printPanel = new JEditorPane("text/html", "");
        HTMLEditorKit kit = new HTMLEditorKit();
        printPanel.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("img{height:96px;width:auto;}");
        printPanel.setText(html);
        printPanel.setBackground(new Color(208,208,208));
        JScrollPane scroll = new JScrollPane(printPanel);
                 
        JButton accept = new JButton("Cancel");
        accept.addActionListener(
           new ActionListener()
           {             
              public void actionPerformed(ActionEvent event)
              {   
                  setVisible(false);
                  dispose();
              }
           });
           
        JButton cancel = new JButton("Print");
        cancel.addActionListener(
           new ActionListener()
           {             
              public void actionPerformed(ActionEvent event) 
              {   
            	  String result = printPane(printPanel);
            	  if (result != null) 
            	  {
            		  label.setText(result);
            		  return;
            	  }
            	  else label.setText("Printing Complete");
            	  
                  setVisible(false);
                  dispose();
              }
           });
        
        label = new JLabel();
        label.setPreferredSize(new Dimension(SIZE, 30));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(80,80,80));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(label);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(accept);
        buttonPanel.add(Box.createHorizontalStrut(GAP));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalStrut(GAP));

        Container entry = getContentPane();
        entry.setLayout(new BorderLayout());
        entry.add(scroll, BorderLayout.CENTER);
        entry.add(buttonPanel, BorderLayout.SOUTH);
        Point point = root.getLocation();
        setLocation(point);
        setVisible(true);
    }
    
    /** Output the HTML to a printer
     * 
     * @param html The text to output
     * @return null if ok, otherwise an error message
     */
    public String printHTML(String html)
    {
    	JEditorPane pane = new JEditorPane("text/html", html); 
    	String result = printPane(pane);
    	return result;

    }

    /** Method to print the HTML text
     * 
     * @param pane The JEditorPane containing the text
     * @return "" if successful, error message otherwise
     */
    private String printPane(JEditorPane pane)
    {
    	Date today = new Date();
    	String strDate = MessageFormat.format("{0,date,full} {0,time,short}", today);
    	String title = getTitle(pane.getText());
  
    	MessageFormat footerFormat = new MessageFormat("Page {0}   " + strDate);
        MessageFormat headerFormat = new MessageFormat(title);
        try
        {
        	Color color = pane.getBackground();
        	pane.setBackground(Color.WHITE);
        	pane.print(headerFormat, footerFormat);
        	pane.setBackground(color);
        }
        catch (PrinterException e)
        {
        	return e.toString();
        }
        return "";
    }
    
    /** Method to get the document title
     * 
     * @return the title for the printout
     */
    private String getTitle(String html)
    {   
    	String title = "<title>";
    	int start = html.indexOf(title) + title.length();
    	int end = html.indexOf("</title>");
    	if (start>0 && end > start)
    		title = html.substring(start,  end);
    	else title = "Dictionary Output";
    	return title;
    }
}
