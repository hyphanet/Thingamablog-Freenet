/*
 * Created on May 30, 2004
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

/**
 * 
 * Concrete implementation of a Weblogs.com style XML-RPC ping
 * 
 * @author Bob Tantlinger
 *
 * 
 */
public class WeblogsDotComPing extends PingService
{
	private final String THINGA_PING1 = "http://thingamablog.sourceforge.net/rpc.php";
	private final String THINGA_PING2 = "http://thingamablog.sf.net/rpc.php";
	
    private String proc = "weblogUpdates.ping";
	
	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.PingService#getProcedureName()
	 */
	public String getProcedureName()
	{
		return proc;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.PingService#getParameters(net.sf.thingamablog.blog.Weblog)
	 */
	public String[] getParameters(Weblog blog)
	{
		//kludgy spam protection for the thinga ping server
	    String title = blog.getTitle();
		String url = getServiceUrl();
		if(url.equals(THINGA_PING1) || url.equals(THINGA_PING2))
		    title = "THINGA" + title;//the THINGA prefix is removed on the server end
	    
	    String s[] = new String[2];
		s[0] = title;		
		s[1] = blog.getFrontPageUrl();				
		return s;	
	}
	
	public String toString()
	{
		return getServiceName();
	}
}
