/*
 * Created on Mar 13, 2004
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
import java.io.IOException;
/**
 * Abstract definition of a weblog template
 *  @author Bob Tantlinger
 *
 */
public abstract class Template
{
	private String name = "";	
	
	/**
	 * Loads the template
	 * @return The template text
	 * @throws IOException If an error occurs while loading the template
	 */
	public abstract String load() throws IOException;
	
	/**
	 * Saves the template
	 * @param text the text to save
	 * @throws IOException
	 */
	public abstract void save(String text) throws IOException;
	
    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param string
     */
    public void setName(String string)
    {
        name = string;
    }
    
    /**
     * Overriden to return the name of the template
     */
    public String toString()
    {
    	return getName();
    }
}
