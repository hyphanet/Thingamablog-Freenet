/*
 * This file is part of Thingamablog. ( http://thingamablog.sf.net )
 *
 * Copyright (c) 2004, Bob Tantlinger All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 *
 */



package net.sf.thingamablog.blog;
import java.util.Date;
import java.util.Vector;

/**
 * 
 * Class which defines a weblog entry
 * 
 * @author Bob Tantlinger 
 */
public class BlogEntry
{
	private Date ts, lastModified;
	private String title = "";
	private Author author = null;
	private boolean expired, draft;
	private long id;    
	private Vector cats = new Vector();
	private String text = "";
        
	/**
	 * Gets the main body text of the entry
	 * @return The body text
	 */
	public String getText()
	{
		return text;
	}
	
	/**
	 * Sets the body text of the entry
	 * @param t
	 */
	public void setText(String t)
	{
		text = t;
	}
	
	/**
	 * Sets the post date of an entry
	 * @param t The date
	 */
	public void setDate(Date t)
	{
		ts = t;
	}
    
    /**
     * Gets the post date of an entry
     * @return
     */
	public Date getDate()
	{
		return ts;
	}
    
    /**
     * Sets the date that the entry was last modified
     * @param t
     */
	public void setLastModified(Date t)
	{
		lastModified = t;
	}
    
    /**
     * Gets the date the entry was last modified
     * @return
     */
	public Date getLastModified()
	{
		return lastModified;
	} 
    
    /**
     * Sets the title of an entry
     * @param t
     */
	public void setTitle(String t)
	{
		title = t;
	}
    
    /**
     * Gets the title of an entry
     * @return
     */
	public String getTitle()
	{
		return title;
	}
    
    /**
     * Adds a category to the entry
     * @param c
     */
	public void addCategory(String c)
	{
		if(!cats.contains(c));
			cats.add(c);
	}
    
    /**
     * Sets the categories that this entry belongs to
     * @param c
     */
	public void setCategories(String c[])
	{
		cats.removeAllElements();
		if(c == null) return;    	    	
		for(int i = 0; i < c.length; i++)	
			cats.add(c[i]);
	}
    
    /**
     * Gets the categories that this entry belongs to
     * @return
     */
	public String[] getCategories()
	{
		String c[] = new String[cats.size()];
		for(int i = 0; i < c.length; i++)
			c[i] = cats.elementAt(i).toString();
		return c;
	}
    
    /**
     * Gets the author of this entry
     * @return
     */
	public Author getAuthor()
	{
		return author;
	}
    
    /**
     * Sets the author of this entry
     * @param c
     */
	public void setAuthor(Author c)
	{
		author = c;
	}
    
    /**
     * Sets the ID of this entry
     * @param i
     */
	public void setID(long i)
	{
		id = i;
	}
    
    /**
     * Gets the ID of this entry
     * @return
     */
	public long getID()
	{
		return id;
	}
    
	
    /**
     * Sets whether this entry is a draft
     * @param e
     */
	public void setDraft(boolean e)
	{
		draft = e;
	}
    
    /**
     * Indicates if this entry is a draft
     * @return
     */
	public boolean isDraft()
	{
		return draft;
	}
}
