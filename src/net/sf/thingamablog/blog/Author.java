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
 */

package net.sf.thingamablog.blog;
import java.text.Collator;

/**
 * 
 * Class which defines a weblog author
 * 
 * @author Bob Tantlinger
 *
 * 
 */
public class Author implements Comparable
{
	private static Collator col = Collator.getInstance();
	
	private String name = "";
	private String email = "";
	private String url = "";
	

	/**
	 * Sets all the fields of an author at once
	 * @param str A String describing an author in the form of
	 * name/temail/turl with each field seperated by a tab
	 */
	public void setString(String str)
	{
		int n1 = str.indexOf("\t");
		if(n1 < 0)
		{		
			name = str;
			return;
		}
		int n2 = str.indexOf("\t", n1 + 1);
		if(n2 < 0)
			return;	
		
		name = str.substring(0, n1).trim();
		email = str.substring(n1, n2).trim();
		url = str.substring(n2, str.length()).trim();			
	}
	
	/**
	 * Sets the name of the author
	 * @param n
	 */
	public void setName(String n)
	{
		name = n;
	}
	
	/**
	 * Gets the name of the author
	 * @return
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the email address of the author
	 * @param n
	 */
	public void setEmailAddress(String n)
	{
		email = n;
	}
	
	/**
	 * Gets the email address of the author
	 * @return
	 */
	public String getEmailAddress()
	{
		return email;
	}

	/**
	 * Sets the author's url
	 * @param n
	 */
	public void setUrl(String n)
	{
		url = n;
	}
	
	/**
	 * Gets the authors url
	 * @return
	 */
	public String getUrl()
	{
		return url;
	}
	
	/**
	 * Gets the string representation of all the fields
	 * of this author, seperated by tabs
	 * @return
	 */
	public String getString()
	{
		return name + '\t' + email + '\t' + url;	
	}
	
	/**
	 * Synonomous with getName()
	 */
	public String toString()
	{
		return name;
	}
	
	public int compareTo(Object o)
	{
		String s1 = toString().toLowerCase();
		String s2 = o.toString().toLowerCase();
		return col.compare(s1, s2);
	}
}