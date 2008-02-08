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
 * Abstract definition of a PingService. Subclasses will typically define
 * an Xml-RPC based ping service
 * 
 * @author Bob Tantlinger
 */
public abstract class PingService
{
	private boolean isEnabled = true;
	private String name;
	private String url;
	
	/**
	 * Gets the name of the procedure to invoke
	 * @return the proc name
	 */
	public abstract String getProcedureName();
	
	/**
	 * Get the parameters that the procedure requires
	 * @param blog
	 * @return
	 */
	public abstract String[] getParameters(Weblog blog);
	
	/**
	 * Sets the service name
	 * @param s
	 */
	public void setServiceName(String s)
	{
		name = s;
	}
	
	/**
	 * Gets the service name
	 * @return
	 */
	public String getServiceName()
	{
		return name;
	}
	
	/**
	 * Sets the URL of the service
	 * @param s
	 */
	public void setServiceUrl(String s)
	{
		url = s;
	}
	
	/**
	 * Gets the URL of the service
	 * @return
	 */
	public String getServiceUrl()
	{
		return url;
	}
	
	/**
	 * Enable or disable the service from being pinged
	 * @param b
	 */
	public void setEnabled(boolean b)
	{
		isEnabled = b;
	}
	
	/**
	 * Indicates whether this service should be pinged
	 * @return
	 */
	public boolean isEnabled()
	{
		return isEnabled;	
	}
}
