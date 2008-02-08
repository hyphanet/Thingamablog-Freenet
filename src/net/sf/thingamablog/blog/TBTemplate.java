/*
 * Created on May 16, 2004
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import net.atlanticbb.tantlinger.io.IOUtils;

/**
 * 
 * Concrete implementation of a Template for TBWeblogs
 * 
 * @author Bob Tantlinger 
 */
public class TBTemplate extends Template
{
	private File tmplFile;
	
   	/**
   	 * Constructs a TBTemplate
   	 * 
   	 * @param file The file that contains the template
   	 * @param name The name of the template
   	 */
   	public TBTemplate(File file, String name)
   	{
   		setName(name);
   		tmplFile = file;  
   	}
   	
   	/**
   	 * Gets the date the template was last modified
   	 * 
   	 * @return The date the template was last modified. 
   	 * If the template doesn't exist, returns the epoc
   	 */
   	public Date getLastModifiedDate()
   	{   		
   		return new Date(tmplFile.lastModified());   			
   	}
   
    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.Template#load()
     */
    public String load() throws IOException
    {	
        String text = null;
        
        //check if the template exists. 
		//if it doesn't, just supply some default text        
		if(!tmplFile.exists())
		{
			text  = "This is the '" + getName() + "' template."; 
		}
		else
        {
			text = IOUtils.read(tmplFile);
        }
		return text;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.Template#save(java.lang.String)
     */
    public void save(String text) throws IOException
    {        
		PrintWriter pw = new PrintWriter(new FileWriter(tmplFile));
		pw.print(text);
		pw.close();
    }    

}
