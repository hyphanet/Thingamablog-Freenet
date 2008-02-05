/*
 * Created on Aug 29, 2004
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
package net.sf.thingamablog.gui.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.feed.FeedItem;


/**
 * @author Bob Tantlinger
 */
public class FeedItemFormatter
{
    private static final String FILE_PATH = 
        TBGlobals.PROP_DIR + TBGlobals.SEP + "item_post.template";
    
    private static final String DESC = "${DESCRIPTION}";
    private static final String TITLE = "${TITLE}";
    private static final String LINK = "${LINK}";
    private static final String FEED_TITLE = "${FEED_TITLE}";
    
    private static final String DEFAULT_TEMPLATE =   
    "<blockquote>\n<cite>\n" + DESC + "\n<br>\n<br>\n<a href=\"" + LINK + "\">" + 
    TITLE + "</a>\n</cite>\n</blockquote>\n<p></p>";
    
    private static String TEMPLATE = DEFAULT_TEMPLATE;
    
    public static void loadTemplate()
    {
		File tmplFile = new File(FILE_PATH);
		if(!tmplFile.exists())
		{
		    TEMPLATE = DEFAULT_TEMPLATE;
		    return;
		}
        
		try
		{
		    BufferedReader reader = new BufferedReader(new FileReader(tmplFile));		
		    StringBuffer sb = new StringBuffer();
		    String line;        
		    while((line = reader.readLine()) != null)
		    {		
		        sb.append(line); 
		        sb.append('\n');
		    }
        
			reader.close();
			TEMPLATE = sb.toString();
		}
		catch(IOException ex)
		{
		    TEMPLATE = DEFAULT_TEMPLATE;
		}
    }
    
    public static void setTemplate(String tmpl)
    {
        TEMPLATE = tmpl;
    }
    
    public static String getTemplate()
    {
        return TEMPLATE;
    }
    
    public static void saveTemplate() throws IOException
    {
		File tmplFile = new File(FILE_PATH);
        PrintWriter pw = new PrintWriter(new FileWriter(tmplFile));
		pw.print(TEMPLATE);
		pw.close();
    }
    
    public static String format(FeedItem item)
    {
        String tmp = TEMPLATE;        
        tmp = replaceVariable(TITLE, item.getTitle(), tmp);
        tmp = replaceVariable(LINK, item.getLink(), tmp);
        tmp = replaceVariable(FEED_TITLE, item.getChannelTitle(), tmp);
        tmp = replaceVariable(DESC, item.getDescription(), tmp);
        return tmp;
    }
    
	private static String replaceVariable(String var, String val, String tmpl)
	{              
		if(var.equals(""))//don't try to process empty templates
			 return tmpl;
		
	    while(tmpl.indexOf(var) != -1 && !var.equals(val))
		{
			StringBuffer sb = new StringBuffer(tmpl);  
			int s = sb.toString().indexOf(var);            
			int e = s + var.length();
			sb.delete(s, e);
			sb.insert(s, val);
			tmpl = sb.toString();			
		}
                
		return tmpl;
	}
}
