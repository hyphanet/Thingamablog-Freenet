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
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * A list of weblogs
 * 
 *  @author Bob Tantlinger
 *
 * 
 */
public class WeblogList
{
	private Vector list = new Vector();
	private Comparator comparator = new WeblogComparator();
	
	/**
	 * Sorts the list alphabetically. This should be called
	 * after manipulating the list
	 *
	 */
	public void sortList()
	{
		Collections.sort(list, comparator);
	}
	
	/**
	 * Adds a weblog to the list
	 * @param weblog
	 */
	public void addWeblog(Weblog weblog)
	{	
		list.add(weblog);
		//Collections.sort(list, comparator);		
	}
	
	/**
	 * Gets the weblogs in the list
	 * @return an array of Weblogs
	 */
	public Weblog[] getWeblogs()
	{
		Weblog blogs[] = new Weblog[list.size()];
		for(int i = 0; i < blogs.length; i++)
			blogs[i] = (Weblog)list.elementAt(i);
		return blogs;
	}
	
	/**
	 * Removes a weblog from the list
	 * @param weblog
	 */
	public void removeWeblog(Weblog weblog)
	{
		list.remove(weblog);
		//Collections.sort(list, comparator);
	}
	
	/**
	 * Deletes a weblog and removes it from the list.
	 * Be careful with this, for it deletes everything in the weblog
	 * 
	 * @param weblog
	 * @throws BackendException
	 */
	public void deleteWeblog(Weblog weblog) throws BackendException
	{
		if(list.contains(weblog))
		{		
			removeWeblog(weblog);
			weblog.deleteAll();
		}
	}
	
	/**
	 * Gets the number of weblogs in the list
	 * @return The number of weblogs in the list
	 */
	public int getWeblogCount()
	{
		return list.size();
	}
	
	/**
	 * Gets a weblog at a position in the list
	 * @param i The index of the weblog
	 * @return The weblog at index
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public Weblog getWeblogAt(int i) throws ArrayIndexOutOfBoundsException
	{
		return (Weblog)list.elementAt(i);
	}
	
	private class WeblogComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			Weblog w1 = (Weblog)o1;
			Weblog w2 = (Weblog)o2;
			
			String s1 = w1.getTitle().toLowerCase();
			String s2 = w2.getTitle().toLowerCase();
			
			Collator coll = Collator.getInstance();
			return coll.compare(s1, s2);
			//return w1.getTitle().compareToIgnoreCase(w2.getTitle());
		}
		
		public boolean equals(Object obj)
		{
			return obj.equals(this);
		}
	}    
}
