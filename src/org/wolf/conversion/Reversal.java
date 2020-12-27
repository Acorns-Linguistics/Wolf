package org.wolf.conversion;

import org.wolf.data.Word;

public class Reversal implements Comparable<Reversal>
{
	private Word   word;
	private String lang;
	private String key;
	
	public Reversal(Word word, String lang, String key)
	{
		this.word = word;
		this.lang = lang;
		this.key = key;
	}
	
	public Word getWord() { return word; }
	public String getLanguage() { return lang; }
	public String getKey() { return key; }
	
	public void setLanguage(String lang) { this.lang = lang; }
	
	@Override
	public int compareTo(Reversal reversal) 
	{
		return key.compareTo(reversal.getKey());
	}
}
