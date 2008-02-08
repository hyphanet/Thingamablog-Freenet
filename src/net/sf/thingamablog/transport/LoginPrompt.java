/*
 * Created on Jul 9, 2004
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

/**
 * Interface which specifies a Login prompt for logging into a transport
 * 
 * @author Bob Tantlinger
 *
 */
public interface LoginPrompt
{
	/**
	 * Prompts the user for a password
	 * 
	 * @param userName The username to display
	 */
	public void promptUser(String userName, String serverName);
	
	/**
	 * Gets the password from the user
	 * 
	 * @return The password
	 */
	public String getPassword();
	
	/**
	 * Indicates if the user canceled the login
	 * 
	 * @return true if cancelled, false otherwise
	 */
	public boolean isLoginCancelled();
}
