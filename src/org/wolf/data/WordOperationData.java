/**
 * WordOperationData.java
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

public class WordOperationData 
{
    public final static int INSERT = 0;
    public final static int REMOVE = 1;
    public final static int MODIFY = 2;
    
    Word newWord;
    Language language;
    int operation;
    Word origWord;
    
    public WordOperationData(Word newWord, Word origWord, Language language, int operation)
    {
        this.newWord = newWord;
        this.origWord = origWord;
        this.language = language;
        this.operation = operation;
    }
    
    public Word getOrigWord() { return origWord; }
    public Word getNewWord()  { return newWord; }
    public Language getLanguage() { return language; }
    public int getOperation() { return operation; }
    
}   // End of WordOperationData class
