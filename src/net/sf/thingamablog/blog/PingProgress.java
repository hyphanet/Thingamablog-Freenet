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
 * Interface for updating the progress of a pinging session
 * 
 * @author Bob Tantlinger
 */
public interface PingProgress
{
	/**
	 * Invoked when a ping session is started
	 * @param totalServices The number of services to ping
	 */
	public void pingSessionStarted(int totalServices);
	
	/**
	 * Invoked when a ping session completes
	 */
	public void pingSessionCompleted();
	
	/**
	 * Invoked when a service is initially pinged
	 * @param ps The service to ping
	 */
	public void pingStarted(PingService ps);
	
	/**
	 * Invoked after a service has been pinged
	 * @param ps The service
	 * @param success true if the service was pinged successfully, false otherwise
	 * @param message The message the service returned
	 */
	public void pingFinished(PingService ps, boolean success, String message);
	
	/**
	 * Indicates whether the user aborted the session
	 * @return
	 */
	public boolean isPingSessionAborted();
}
