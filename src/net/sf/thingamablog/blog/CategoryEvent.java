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
 * 
 * An event which indicates an category-related action occurred within a weblog
 * 
 * @author Bob Tantlinger
 */
public class CategoryEvent extends EventObject
{
	private String cat;
	private Weblog blog;
	
	/**
	 * Constructs a CategoryEvent
	 * @param blog The blog from which the event originated
	 * @param cat The category
	 */
	public CategoryEvent(Weblog blog, String cat)
	{
		super(blog);
		this.blog = blog;
		this.cat = cat;
	}
	/**
	 * @return
	 */
	public Weblog getWeblog()
	{
		return blog;
	}

	/**
	 * @return
	 */
	public String getCategory()
	{
		return cat;
	}
}
