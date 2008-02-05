/*
 * Created on May 26, 2004
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
package net.sf.thingamablog.transport;

import java.util.StringTokenizer;

/**
 * An abstract remote transport.
 * 
 * @author Bob Tantlinger
 *
 */
public abstract class RemoteTransport implements PublishTransport
{
	private String userName = "";
	private String password = "";
	private String address = "";
	private boolean isSavePassword;	
	private int port = 21;
	
	protected boolean isConnected;
	
	public boolean isConnected()
	{
		return isConnected;	
	}
	
    /**
     * Gets the address of the remote server
     * 
     * @return The address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * Gets the password used to login
     * 
     * @return The password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the address of the remote server
     * 
     * @param string The address
     */
    public void setAddress(String string)
    {
        address = string;
    }

    /**
     * Sets the password to log in to the remote server
     * 
     * @param string The password
     */
    public void setPassword(String string)
    {
        password = string;
    }

    /**
     * Gets the user name to log in to the remote server
     * 
     * @return The user name
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Sets the user name to log in to the remote server 
     * 
     * @param string The user name
     */
    public void setUserName(String string)
    {
        userName = string;
    }

    /**
     * Indicates if the transport should save the password
     * 
     * @return true if the password should be saved, false otherwise
     */
    public boolean isSavePassword()
    {
        return isSavePassword;
    }

    /**
     * indicate whether or not the password should be saved
     * 
     * @param b
     */
    public void setSavePassword(boolean b)
    {
        isSavePassword = b;
    }
    
	/**
	 * Gets the port of the remote server
	 * 
	 * @return
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * Sets the port of the remote server
	 * 
	 * @param i
	 */
	public void setPort(int i)
	{
		port = i;
	}
	
	/**
	 * Convienence method for splitting a path into an array
	 * 
	 * @param the path to split
	 * @return The individual path elements, or a zero length
	 * array if the path can't be split
	 */
	protected String[] splitPath(String path)
	{
		return split(path, "/", -1);
	}
	
	private String[] split(String str, String separator, int max)
	{
		StringTokenizer tok = null;
		if(separator == null)
		{
			// Null separator means we're using StringTokenizer's default
			// delimiter, which comprises all whitespace characters.
			tok = new StringTokenizer(str);
		}
		else
		{
			tok = new StringTokenizer(str, separator);
		}

		int listSize = tok.countTokens();
		if(max > 0 && listSize > max)
		{
			listSize = max;
		}

		String[] list = new String[listSize];
		int i = 0;
		int lastTokenBegin = 0;
		int lastTokenEnd = 0;
		while(tok.hasMoreTokens())
		{
			if(max > 0 && i == listSize - 1)
			{
				// In the situation where we hit the max yet have
				// tokens left over in our input, the last list
				// element gets all remaining text.
				String endToken = tok.nextToken();
				lastTokenBegin = str.indexOf(endToken, lastTokenEnd);
				list[i] = str.substring(lastTokenBegin);
				break;
			}
			else
			{
				list[i] = tok.nextToken();
				lastTokenBegin = str.indexOf(list[i], lastTokenEnd);
				lastTokenEnd = lastTokenBegin + list[i].length();
			}
			i++;
		}
		return list;
	}	

}
