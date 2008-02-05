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
* */

package net.sf.thingamablog.blog;

/**
 * An enumeration of weblog entries
 * 
 * @author Bob Tantlinger
 */
public interface EntryEnumeration
{
	/**
	 * Indicates if there are more entries in the enumeration
	 * @return true if there are more entries, false otherwise
	 */
	public boolean hasMoreEntries();
	
	/**
	 * Gets the next entry in the enumeration
	 * @return a BlogEntry
	 */
	public BlogEntry nextEntry();
	
	/**
	 * Closes the enumeration. You should ALWAYS call this method 
	 * after you're finished with the enumeration because different
	 * backends may need to close whatever it is that this interface is wrapping. 
	 *
	 */
	public void close();
}