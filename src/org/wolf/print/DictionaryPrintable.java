/*
 * DictionaryPrintable.java
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

package org.wolf.print;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/** The Printable class for printing dictionary data */
public class DictionaryPrintable implements Printable
{   private final static int TOP_MARGIN = 18;
    private final static int LEFT_MARGIN = 0;

    private ArrayList<String>  lines;
    private ArrayList<Integer> pages;

    private Font font, headerFont;
    private Dimension size;
    private String title;

    public DictionaryPrintable(String data, PageFormat format, File file)
                                                             throws IOException
    { 
      data = data.replaceFirst(">", ">\n");
      font = new Font("Serif", Font.PLAIN, 11);
      headerFont = new Font("Serif", Font.PLAIN, 8);
      title = file.getAbsolutePath();

      char[] chars = data.toCharArray();
      CharArrayReader charReader = new CharArrayReader(chars);
      BufferedReader bufReader  = new BufferedReader(charReader);
      //bufReader.readLine();  Eliminated <?xml line, but also <amdx> without newline

      size = new Dimension(
              (int)format.getImageableWidth(),(int)format.getImageableHeight());

      int charsPerLine = size.width / (font.getSize()  * 1 / 2);

      String line;
      lines = new ArrayList<String>();
      while ((line = bufReader.readLine()) != null)
      {   for (int i=0; i<line.length(); i+=charsPerLine)
          {  try { lines.add(line.substring(i, i+charsPerLine)); }
             catch (Exception e) { lines.add(line.substring(i));  }
          }
      }
      bufReader.close();
      initialize(size.width);
    }

    /** Initialize the text into pages */
    private void initialize(double imageableHeight)
    {  pages = new ArrayList<Integer>();

       int height = font.getSize();
       int y = height;

       int count = 0;
       pages.add(count);
       for (; count < lines.size(); count++)
       {  y += height;
          if (y + height * 2 > imageableHeight)
          {  y = 0;
             pages.add(count);
          }
       }
       if (pages.size() > 0) pages.add(count);
    }

    /** Print method to format and esch page */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
    {  if (pageIndex + 1 >= pages.size())  return NO_SUCH_PAGE;

      Graphics2D g2d = (Graphics2D) graphics;
      g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

      printHeader(g2d, pageIndex, pageFormat.getImageableWidth());
      paintPage(g2d, pageIndex);
      return PAGE_EXISTS;
    }

    /** Paint the actual data for a page */
    private void paintPage(Graphics2D g2d, int currentPage)
    {  int height = g2d.getFontMetrics().getHeight();
       float x = 0;
       float y = TOP_MARGIN + headerFont.getSize() + font.getSize();

       int startX = pages.get(currentPage);
       int endX  = pages.get(currentPage + 1);

       String line;
       for (int i=startX; i<endX; i++)
       {  line = lines.get(i);
          if (lines.size() > 0)  g2d.drawString(line, (int) x, (int) y);
          y += height;
       }
    }

    /** Method to initialize the graphicsw and print the page header
     *
     * @param title The title for this report
     * @param pageNumber The page number (counting from zero)
     */
    private void printHeader(Graphics2D g2d, int pageNumber, double pageWidth)
    {  Rectangle2D rect = new Rectangle2D.Float(0, 0, size.width, size.height);
       g2d.setPaint(Color.white);
       g2d.fill(rect);
       g2d.setPaint(Color.black);
       g2d.setFont(headerFont);

       Date today         = new Date();
       DateFormat df      = DateFormat.getInstance();
       String dateAndPage = df.format(today)+" Page  "+(pageNumber+1)+" ";

       int top  = TOP_MARGIN;
       int left = LEFT_MARGIN;

       FontMetrics metrics = g2d.getFontMetrics();
       int stringWidth = metrics.stringWidth(dateAndPage);
       g2d.drawString(dateAndPage, (int)(pageWidth-stringWidth), top);
       g2d.drawString(title, left, top);
       g2d.setFont(font);
    }  // End of printHeader()
}      // End of DictionaryPrintable class
