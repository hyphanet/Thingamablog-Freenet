/*
 * Created on Mar 12, 2004
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
import java.util.Date;


/**
 * An interface for a WeblogBackend.
 * A WeblogBackend maintains entry data for one or more weblogs
 *
 *  @author Bob Tantlinger
 */
public interface WeblogBackend
{	
	
	public CategoryStore getCategoryStore();
	public AuthorStore getAuthorStore();
	
	public void removeAllWeblogData(String blogKey) throws BackendException;
	
	
	public void initEntryStoreForWeblog(String blogKey) throws BackendException;
	
	/**
	 * Adds an entry for the backend
	 * @param blogKey The key of the weblog the entry belongs to
	 * @param be a weblog entry
	 * @throws BackendException If an error occurs while adding the entry
	 * @return the id assigned to the entry that was added 
	 */
	public long addEntry(String blogKey, BlogEntry be) throws BackendException;
	
	/**
	 * Updates an entry in the backend
	 * @param be The entry to update
	 * @throws BackendException If an error occurs updating the entry
	 */	
	public void updateEntry(String blogKey, BlogEntry be) throws BackendException;
	
	/**
	 * Removes an entry from the backend
	 * @param id The ID of the entry
	 * @throws BackendException If an error occurs removing the entry
	 */
	public void removeEntry(String blogKey, long id) throws BackendException;
	
	/**
	 * Gets an entry from the backend
	 * @param id The ID of the entry
	 * @return an entry
	 * @throws BackendException If an error ocurrs getting the entry
	 */
	public BlogEntry getEntry(String blogKey, long id) throws BackendException;
	
	/**
	 * Finds entries in the backend that match the search criteria
	 * @param blogKey The key of the weblog to find entries in
	 * @param search The search criteria
	 * @return An enumeration of entries
	 * @throws BackendException If an error occurs while finding the entries
	 */
	public EntryEnumeration findEntries(String blogKey, WeblogSearch search) throws BackendException;
	
	/**
	 * Gets entries from the backend
	 * @param blogKey The key of the weblog that the entries belong to
	 * @param orderByDateAsc The chronological sort order
	 * @return An enumeration of sorted entries
	 * @throws BackendException
	 */
	public EntryEnumeration getEntries(String blogKey, boolean orderByDateAsc) throws BackendException;
	
	/**
	 * Gets categorized entries from the backend
	 * @param blogKey The key of the weblog that the entries belong to
	 * @param category The category of the entries
	 * @param orderByDateAsc The chronological sort order
	 * @return An enumeration of sorted entries
	 * @throws BackendException
	 */
	public EntryEnumeration getEntriesFromCategory(String blogKey, String category, boolean orderByDateAsc) throws BackendException;
	
	/**
	 * Gets entries before a certain date
	 * @param blogKey The key of the weblog that the entries belong to
	 * @param d The date the entries should be before
	 * @param orderByDateAsc The chronological sort order
	 * @return An enumeration of sorted entries
	 * @throws BackendException
	 */
	public EntryEnumeration getEntriesBefore(String blogKey, Date d, boolean orderByDateAsc) throws BackendException;
	
	/**
	 * Gets entries after a certain date
	 * @param blogKey The key of the weblog that the entries belong to
	 * @param d The date the entries should be after
	 * @param orderByDateAsc The chronological sort order
	 * @return An enumeration of sorted entries
	 * @throws BackendException
	 */
	public EntryEnumeration getEntriesAfter(String blogKey, Date d, boolean orderByDateAsc) throws BackendException;
	
	/**
	 * Gets entries that fall between two dates
	 * @param blogKey The key of the weblog that the entries belong to
	 * @param from The start date
	 * @param to The end date
	 * @param orderByDateAsc The chronological sort order
	 * @return An enumeration of sorted entries
	 * @throws BackendException
	 */
	public EntryEnumeration getEntriesBetween(String blogKey, Date from, Date to, boolean orderByDateAsc) throws BackendException;
	
	/**
	 * Gets entries form the backend which are drafts
	 * @param blogKey The key of the weblog that the entries belong to
	 * @param orderByDateAsc The chronological sort order
	 * @return An enumeration of sorted entries
	 * @throws BackendException
	 */
	public EntryEnumeration getDraftEntries(String blogKey, boolean orderByDateAsc) throws BackendException;
}
