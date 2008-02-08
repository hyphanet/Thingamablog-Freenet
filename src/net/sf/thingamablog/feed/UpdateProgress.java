/*
 * Created on May 6, 2004
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
package net.sf.thingamablog.feed;

/**
 * Interface for monitoring the progress of feed updates
 * 
 * @author Bob Tantlinger
 *
 */
public interface UpdateProgress
{
	/**
	 * Invoked when an update starts
	 * 
	 * @param numOfFeeds The number of feeds to be updated
	 */
	public void updateStart(int numOfFeeds);
	
	/**
	 * Indicates whether or not the update is started
	 * 
	 * @return true if updating, false otherwise
	 */
	public boolean isUpdateStarted();
	
	/**
	 * Invoked when a feed is updating
	 * 
	 * @param feed
	 */
	public void feedUpdating(Feed feed);
	
	/**
	 * Invoked after a feed is updated
	 * 
	 * @return The index of the feed updated as specified in updateStart
	 */
	public int feedUpdated();
	
	/**
	 * Gets the number of feeds to be updated
	 * 
	 * @return The number of feeds to be updated
	 */
	public int getUpdateSize();
	
	/**
	 * 
	 * Invoked when the update completes
	 *
	 */
	public void updateFinish();
	
	/**
	 * Indicates if the user cancelled the update
	 * 
	 * @return true if aborted, false otherwise
	 */
	public boolean isAborted();
}
