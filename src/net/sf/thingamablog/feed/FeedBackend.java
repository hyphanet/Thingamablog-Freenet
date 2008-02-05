/*
 * Created on Apr 4, 2004
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
 * An interface for a FeedBackend.
 * A FeedBackend manages the FeedItems for one or more Feeds
 * 
 * @author Bob Tantlinger
 */
public interface FeedBackend
{
	/**
	 * Adds an item to the backend
	 * 
	 * @param item The item to add
	 * @param updateIfExists true to overwrite existing items, false otherwise
	 * @throws FeedBackendException If an error occurs adding the item
	 */
	public void addItem(FeedItem item, boolean updateIfExists) throws FeedBackendException;
	
	/**
	 * Updates an existing item in the backend
	 * 
	 * @param item The item to update
	 * @throws FeedBackendException If an error occurs updating the item
	 */
	public void updateItem(FeedItem item) throws FeedBackendException;
	
	/**
	 * Removes an item from the backend
	 * 
	 * @param id The ID of the item to remove
	 * @throws FeedBackendException If an error occurs removing the item
	 */
	public void removeItem(long id) throws FeedBackendException;
	
	/**
	 * Retrieves an item from the backend
	 * 
	 * @param id The ID of the item to retrieve
	 * @return The item
	 * @throws FeedBackendException If an error occurs getting the item
	 */
	public FeedItem getItem(long id) throws FeedBackendException;
	
	/**
	 * Finds items in the backend
	 * 
	 * @param channelLink The feed link of the item
	 * @param search The search criteria
	 * @return The found items
	 * @throws FeedBackendException If an error occurs finding items
	 */
	public FeedItem[] findItems(String channelLink, FeedSearch search) throws FeedBackendException;
	
	/**
	 * Gets the items for a feed
	 * 
	 * @param channelLink The feed's link
	 * @param orderByRetDateAsc Chronological sort order (Sorts by retrieved date)
	 * @return The items
	 * @throws FeedBackendException If an error occurs getting the items
	 */
	public FeedItem[] getItems(String channelLink, boolean orderByRetDateAsc) throws FeedBackendException;
	
	/**
	 * Gets the unread items for a feed
	 * 
	 * @param channelLink The feed's link
	 * @param orderByRetDateAsc Chronological sort order
	 * @return The unread items
	 * @throws FeedBackendException If an error occurs getting the items
	 */
	public FeedItem[] getUnreadItems(String channelLink, boolean orderByRetDateAsc) throws FeedBackendException;
}
