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
import net.sf.thingamablog.blog.Weblog;

/**
 * Factory for logging in to a weblog's transport
 * 
 * @author Bob Tantlinger
 *
 */
public class LoginFactory
{
	
	/**
	 * Logs in to a weblog's transport 
	 * 
	 * @param w The weblog
	 * @param prompt The prompt
	 * @return true if logged in successfully, false otherwise
	 */
	public static boolean login(Weblog w, LoginPrompt prompt)
	{		
		if(w.getPublishTransport() instanceof LocalTransport)
			return true; //no login for local transports
			
		if(w.getPublishTransport() instanceof RemoteTransport)
		{
			RemoteTransport rt = (RemoteTransport)w.getPublishTransport();
			if(!rt.isSavePassword())//got to get the user's pw
			{
				prompt.promptUser(rt.getUserName());										
				rt.setPassword(prompt.getPassword());					
				return !prompt.isLoginCancelled();					
			}
		}		
		
		return true;//all other Transport types return successful login
	}
}
