/*
 * Created on Apr 8, 2004
 * 
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

import java.util.EventObject;

/**
 * An event which indicates an Author-related action occurred within a weblog
 * 
 * @author Bob Tantlinger
 *  
 */
public class AuthorEvent extends EventObject
{
	private Author auth;
	private Weblog blog;
	
	/**
	 * Constructs an AuthorEvent object
	 * @param blog The weblog which this even originated from
	 * @param auth The Author on which this even occured
	 */
	public AuthorEvent(Weblog blog, Author auth)
	{
		super(blog);
		this.blog = blog;
		this.auth = auth;
	}
	/**
	 * Gets the weblog
	 * @return the weblog
	 */
	public Weblog getWeblog()
	{
		return blog;
	}

	/**
	 * Gets the author
	 * @return the author
	 */
	public Author getAuthor()
	{
		return auth;
	}
}
