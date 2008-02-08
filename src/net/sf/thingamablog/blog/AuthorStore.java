/*
 * Created on Apr 7, 2004
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
 * An interface for an AuthorStore. 
 * An AuthorStore is a backend which maintains author data for one or more weblogs
 * 
 * @author Bob Tantlinger
 */
public interface AuthorStore
{
	/**
	 * Adds an author to the store
	 * @param blogKey The key of the weblog to which the author is being added
	 * @param auth The author
	 * @throws BackendException If an error occurs while adding the author
	 */
	public void addAuthor(String blogKey, Author auth) throws BackendException;
	
	/**
	 * Removed an author from the store
	 * @param blogKey The key of the weblog from which to remove the author
	 * @param auth The author
	 * @throws BackendException If an error occurs while removing the author
	 */
	public void removeAuthor(String blogKey, Author auth) throws BackendException;
	
	/**
	 * Updates an author in the store
	 * @param blogKey The key of the weblog that contains the author to update
	 * @param oldAuth The author to update
	 * @param newAuth The new author to overwrite the old author
	 * @throws BackendException If an error occurs while updating the author
	 */
	public void updateAuthor(String blogKey, Author oldAuth, Author newAuth) throws BackendException;
	
	/**
	 * Gets all of the authors in the store
	 * @param blogKey The key of the weblog from which to retrive the authors
	 * @param sortAsc Indicates whether to sort the retrieved authors ascending/descending
	 * @return An array of authors
	 * @throws BackendException If an error occurs while getting the authors
	 */
	public Author[] getAuthors(String blogKey, boolean sortAsc) throws BackendException;
}
