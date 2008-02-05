/*
 * Created on Apr 30, 2004
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
package net.sf.thingamablog.backend;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.AuthorStore;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.CategoryStore;
import net.sf.thingamablog.blog.EntryEnumeration;
import net.sf.thingamablog.blog.WeblogBackend;
import net.sf.thingamablog.blog.WeblogSearch;
import net.sf.thingamablog.feed.FeedBackend;
import net.sf.thingamablog.feed.FeedBackendException;
import net.sf.thingamablog.feed.FeedItem;
import net.sf.thingamablog.feed.FeedSearch;

/**
 * 
 * A Backend implementation wrapping an HSQL database which stores Feeds and Weblogs
 * 
 * @author Bob Tantlinger  
 */
public class HSQLDatabaseBackend implements WeblogBackend, FeedBackend
{
	//blog table stuff
	private static final String AUTH_TABLE = "AUTH_TABLE";
	private static final String BLOG_AUTHOR = "BLOG_AUTHOR";
	
	private static final String CAT_TABLE = "CAT_TABLE";
	private static final String BLOG_CATEGORY = "BLOG_CATEGORY";
	
	private static final String ENTRY_TABLE = "ENTRY_TABLE_";	         
	private static final String ID = "ID";
	private static final String BLOG = "BLOG";
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String CATEGORIES = "CATEGORIES";
	private static final String TITLE = "TITLE";
	private static final String ENTRY = "ENTRY";    
	private static final String AUTHOR = "AUTHOR";
	private static final String MODIFIED = "MODIFIED";
	private static final String DRAFT = "DRAFT";
	
	//Feed table stuff
	private static final String FEED_TABLE = "FEED_ITEMS";            
	private static final String ITEM_ID = "ID";
	private static final String RETRIEVED = "RETRIEVED";
	private static final String PUB_DATE = "PUB_DATE";
	private static final String LINK = "LINK";
	private static final String CHANNEL_LINK = "CHANNEL_LINK";
	
	private static final String CHANNEL_TITLE = "CHANNEL_TITLE";
	private static final String CHANNEL_IMG_LINK = "CHANNEL_IMG_LINK";
	
	private static final String ITEM_TITLE = "TITLE";
	private static final String DESC = "DESC";    
	private static final String ITEM_AUTHOR = "AUTHOR";
	private static final String READ = "READ";
	private static final String HASH_CODE = "HASH_CODE";
	
	/** The connection to the database */
	protected Connection conn;
	
	private AuthorStore authorStore = new DBAuthorStore();
	private CategoryStore categoryStore = new DBCategoryStore();	
	
	
	public AuthorStore getAuthorStore()
	{
		return authorStore;
	}


	public CategoryStore getCategoryStore()
	{
		return categoryStore;
	}
    
	/**
	 * Opens a connection to the database 
	 * @param dir The directory where the database is located
	 * @throws Exception If an error occurs while connecting to the database
	 */
	public synchronized void connectToDB(File dir) throws Exception
	{        
		File dbDir = new File(dir, "database");
		if(!dbDir.exists() ||dbDir.isFile())
			dbDir.mkdirs();
		
		try
		{
			//connect to the local blog
			connect(dbDir.getAbsolutePath() + File.separator + "database");
		}
		catch(Exception ex)
		{
			throw new Exception("Unable to connect to database");            
		}
		
		//create the author table if it doesn't exist
		try 
		{   //make a cached TABLE if one doesn't already exist
			query
			(
				"CREATE CACHED TABLE " + AUTH_TABLE + " (" +				
				BLOG + " LONGVARCHAR, " + 				
				BLOG_AUTHOR + " LONGVARCHAR)"                
			);
		} 
		catch(SQLException sqle) 
		{
			//thrown when TABLE already exists...			
			//System.out.println(AUTH_TABLE + " exists");
		}
		
		//create the category table if it doesn't exist
		try 
		{   
			query
			(
				"CREATE CACHED TABLE " + CAT_TABLE + " (" +				
				BLOG + " LONGVARCHAR, " + 				
				BLOG_CATEGORY + " LONGVARCHAR)"                
			);
		} 
		catch(SQLException sqle) 
		{
			//System.out.println(CAT_TABLE + " exists");
		}
		
		//create the feed item table if it doesn't exist
		try 
		{   
			query
			(
				"CREATE CACHED TABLE " + FEED_TABLE + " (" + 
				ITEM_ID + " INTEGER IDENTITY, " +
				RETRIEVED + " TIMESTAMP, " + 				
				PUB_DATE + " LONGVARCHAR, " +
				ITEM_TITLE + " LONGVARCHAR, " + 
				DESC + " LONGVARCHAR, " + 
				ITEM_AUTHOR + " LONGVARCHAR, " + 
				READ + " BIT, " + 
				HASH_CODE + " INTEGER, " +
				LINK + " LONGVARCHAR, " + 
				CHANNEL_TITLE + " LONGVARCHAR, " +
				CHANNEL_LINK + " LONGVARCHAR, " + 
				CHANNEL_IMG_LINK + " LONGVARCHAR)"                
			);
		} 
		catch(SQLException sqle) 
		{
			//System.out.println(FEED_TABLE + " exists");
		}		
	}
	
	/**
	 * Imports an entry into the backend. The method first checks 
	 * if an entry with the specified ID exists. If no entry with
	 * that ID exists, it adds it to the backend. If an entry with
	 * the ID does exist, the method does nothing.
	 * 
	 * @param blogKey The weblog to add the entry to
	 * @param e The entry
	 * @param id The ID the entry should have
	 * @throws BackendException If an error occurs
	 */
	public synchronized void importEntry(String blogKey, BlogEntry e, long id) throws BackendException
	{		
		//check if an entry with the id exists
		try
		{
			getEntry(blogKey, id);
			return; //an exception wasn't thrown so the entry must exist
		}
		catch(Exception ex){}
		
		String table = ENTRY_TABLE + blogKey;
		try
		{
			PreparedStatement ps = conn.prepareStatement
			(
					"INSERT INTO " + table + "(" +
					ID + ", " +
					TIMESTAMP + ", " +					
					TITLE + ", " +
					CATEGORIES + ", " +
					ENTRY + ", " +
					DRAFT + ", " +
					MODIFIED + ", " +
					AUTHOR + ") " +
					"VALUES(?, ?, ?, ?, ?, ?, ?, ?)"
			);
        
			ps.setLong(1, id);
			ps.setTimestamp(2, new Timestamp(e.getDate().getTime()));			
			ps.setString(3, e.getTitle());
			ps.setString(4, catsString(e.getCategories()));
			ps.setString(5, e.getText());
			ps.setBoolean(6, e.isDraft());
		
			Timestamp ts = null;
			if(e.getLastModified() != null)
				ts = new Timestamp(e.getLastModified().getTime());		
			ps.setTimestamp(7, ts);			
			if(e.getAuthor() == null)
				ps.setString(8, null);
			else
				ps.setString(8, e.getAuthor().getString());
			ps.executeUpdate();
			ps.close();       	
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);	
		}
	}
	
	public synchronized void removeAllWeblogData(String blogKey) throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		try
		{
			String cats[] = categoryStore.getCategories(blogKey, false);
			Author auths[] = authorStore.getAuthors(blogKey, false);
						
			for(int i = 0; i < cats.length; i++)
			{
				categoryStore.removeCategory(blogKey, cats[i]);	
			}		
			
			for(int i = 0; i < auths.length; i++)
			{
				authorStore.removeAuthor(blogKey, auths[i]);	
			}			
			
			query("DROP TABLE " + table);

		}
		catch(SQLException ex)
		{
			throw new BackendException(ex);
		}
	}
	
	public synchronized void initEntryStoreForWeblog(String blogKey) throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		try 
		{   
			query
			(
				"CREATE CACHED TABLE " + table + " (" + 
				ID + " INTEGER IDENTITY, " +
				TIMESTAMP + " TIMESTAMP, " + 
				TITLE + " LONGVARCHAR, " + 
				CATEGORIES + " LONGVARCHAR, "  + 
				ENTRY + " LONGVARCHAR, " + 
				DRAFT + " BIT, " +
				MODIFIED + " TIMESTAMP, " +
				AUTHOR + " LONGVARCHAR)"                
			);
		} 
		catch(SQLException sqle) 
		{
			//thrown when TABLE already exists...
			//System.out.println(table + " exists");
		}
	}
	
	public synchronized EntryEnumeration findEntries(String blogKey, WeblogSearch search) throws BackendException
	{
		EntryEnumeration entries = null;
		String table = ENTRY_TABLE + blogKey;
		try
		{
			String stmnt = "SELECT * FROM " + table + " WHERE ";
			Date d1 = search.getStartDate();
			Date d2 = search.getEndDate();
			
			int t, e, c, ts1, ts2;			
			t = e = c = ts1 = ts2 = 0;
			int i = 1;
        
			String dateCondition = null;
			Timestamp from = null, to = null;
			if(d1 != null && d2  != null)
			{
				if(d1.compareTo(d2) < 0)
				{
					//System.out.println(" first d1 < d2 " + d1.compareTo(d2));
					from = new Timestamp(d1.getTime());
					to = new Timestamp(d2.getTime());
				}
				else if(d1.compareTo(d2) > 0)
				{
					//System.out.println(" second d1 > d2 " + d1.compareTo(d2));
					from = new Timestamp(d2.getTime());
					to = new Timestamp(d1.getTime());
				}
				else
				{
					//System.out.println(" equal d1 = d2 " + d1.compareTo(d2));
					from = new Timestamp(d1.getTime());
					to = new Timestamp(d2.getTime());
				}
            
				if(!search.isFindModifiedEntries())
					dateCondition = " " + TIMESTAMP + " >= ? AND " + TIMESTAMP + " <= ? AND";
				else
					dateCondition = " " + MODIFIED + " >= ? AND " + MODIFIED + " <= ? AND";
			}
			
			        
			if(search.getTitleContains() != null)
			{
				stmnt += " " + TITLE + " LIKE CONCAT('%', CONCAT(?, '%')) AND";
				t = i++;
			}
        
			if(search.getBodyContains() != null)
			{
				stmnt += " " + ENTRY + " LIKE CONCAT('%', CONCAT(?, '%')) AND";
				e = i++;
			}
        
			if(dateCondition != null)
			{
				stmnt += dateCondition;
				ts1 = i++;
				ts2 = i++;
			}
        
			if(search.getCategory() != null)
			{    
				stmnt += " " + CATEGORIES + " LIKE CONCAT('%', CONCAT(?, '%')) AND";
				c = i++;
			}
        
			stmnt += " " + DRAFT + " = ? AND";
        
			stmnt = stmnt.substring(0, stmnt.length() - 4);        
			PreparedStatement ps = conn.prepareStatement(stmnt);
						
			if(search.getTitleContains() != null)
				ps.setString(t, search.getTitleContains());
        
			if(search.getBodyContains() != null)
				ps.setString(e, search.getBodyContains());
             
			if(dateCondition != null)
			{
				
				ps.setTimestamp(ts1, from);
				ps.setTimestamp(ts2, to);
			}
        
			if(search.getCategory() != null)
				ps.setString(c , toDBEntryCat(search.getCategory()));
        
			ps.setBoolean(i, search.isFindDrafts());
			
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);
		}
		catch(Exception sqle)
		{
			throw new BackendException(sqle);
		}
		
		return entries;
	}
	
	/**
	 * Finds feed items
	 */
	public synchronized FeedItem[] findItems(String url, FeedSearch search) throws FeedBackendException
	{
		FeedItem[] items;
		try
		{
			//ps 1
			String stmnt = "SELECT * FROM " + FEED_TABLE + " WHERE " + CHANNEL_LINK + " = ?";
			Timestamp from, to;
			Date d1 = search.getStartRetrievedDate();
			Date d2 = search.getEndRetrievedDate();
			if(d1.compareTo(d2) < 0)
			{				
				from = new Timestamp(d1.getTime());
				to = new Timestamp(d2.getTime());
			}
			else if(d1.compareTo(d2) > 0)
			{				
				from = new Timestamp(d2.getTime());
				to = new Timestamp(d1.getTime());
			}
			else
			{				
				from = new Timestamp(d1.getTime());
				to = new Timestamp(d2.getTime());
			}
			
			//ps 2 and 3
			stmnt += " AND " + RETRIEVED + " >= ? AND " + RETRIEVED + " <= ?";
			
			int title = 3;
			if(search.getTitleContains() != null)
			{				 
				title++;
				stmnt += " AND " + ITEM_TITLE + " LIKE CONCAT('%', CONCAT(?, '%'))";
			}
			
			int desc = title;
			if(search.getDescriptionContains() != null)
			{
				desc++;
				stmnt += " AND " + DESC + " LIKE CONCAT('%', CONCAT(?, '%'))";
			}			
			
			PreparedStatement ps = conn.prepareStatement(stmnt);
			ps.setString(1, url);
			ps.setTimestamp(2, from);
			ps.setTimestamp(3, to);
			if(search.getTitleContains() != null)
				ps.setString(title, search.getTitleContains());
			if(search.getDescriptionContains() != null)
				ps.setString(desc, search.getDescriptionContains());
			
			ResultSet rs = ps.executeQuery();
			items = createItemsFromResultSet(rs);
			
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);
		}		
	
		return items;
	}
    

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#addEntry(net.sf.thingamablog.blog.BlogEntry)
	 */
	public synchronized long addEntry(String blogKey, BlogEntry e) throws BackendException
	{
		long id;
	    String table = ENTRY_TABLE + blogKey;
		try
		{
			PreparedStatement ps = conn.prepareStatement
			(
					"INSERT INTO " + table + "(" +
					TIMESTAMP + ", " +					
					TITLE + ", " +
					CATEGORIES + ", " +
					ENTRY + ", " +
					DRAFT + ", " +
					MODIFIED + ", " +
					AUTHOR + ") " +
					"VALUES(?, ?, ?, ?, ?, ?, ?)"
			);
        
			ps.setTimestamp(1, new Timestamp(e.getDate().getTime()));
			ps.setString(2, e.getTitle());
			ps.setString(3, catsString(e.getCategories()));
			ps.setString(4, e.getText());
			ps.setBoolean(5, e.isDraft());
		
			Timestamp ts = null;
			if(e.getLastModified() != null)
				ts = new Timestamp(e.getLastModified().getTime());		
			ps.setTimestamp(6, ts);	
					
			if(e.getAuthor() == null)
				ps.setString(7, null);
			else
				ps.setString(7, e.getAuthor().getString());
				
			ps.executeUpdate();
			ps.close();
			
			//now get the id for the added entry
			//this probably isn't the best way to do this
			//Hypothetically if an entry has the exact same date
			//it might return the wrong ID... seems extremely unlikely tho
			String stmt = "SELECT * FROM " + table + 
			" WHERE " + TIMESTAMP + " = ? AND " + TITLE + " = ? AND " +
			CATEGORIES + " = ? AND " + DRAFT + " = ? AND " + AUTHOR + " = ?";
			ps = conn.prepareStatement(stmt);			
			ps.setTimestamp(1, new Timestamp(e.getDate().getTime()));
			ps.setString(2, e.getTitle());
			ps.setString(3, catsString(e.getCategories()));
			ps.setBoolean(4, e.isDraft());
			if(e.getAuthor() != null)
			    ps.setString(5, e.getAuthor().getString());
			else
			    ps.setString(5, null);
			ResultSet rs = ps.executeQuery();			
			
			rs.next();			
			id = rs.getInt(ID);				
			ps.close();
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);	
		}
		
		return id;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#updateEntry(net.sf.thingamablog.blog.BlogEntry)
	 */
	public synchronized void updateEntry(String blogKey, BlogEntry be) throws BackendException
	{
		//throw an exception if the entry doesn't exist
		getEntry(blogKey, be.getID());
		
		String table = ENTRY_TABLE + blogKey;
		try
		{
			PreparedStatement ps = conn.prepareStatement
			(
					"UPDATE " + table + " SET " +
					TIMESTAMP + " = ?, " +
					TITLE + " = ?, " +
					CATEGORIES + " = ?, " +
					ENTRY + " = ?, " +
					DRAFT + " = ?, " +
					MODIFIED + " = ?, " +
					AUTHOR + " = ? WHERE " + ID + " = ?"
			);
        
			ps.setTimestamp(1, new Timestamp(be.getDate().getTime()));
			ps.setString(2, be.getTitle());
			ps.setString(3, catsString(be.getCategories()));
			ps.setString(4, be.getText());
			ps.setBoolean(5, be.isDraft());	
					
			Timestamp ts = null;
			if(be.getLastModified() != null)
				ts = new Timestamp(be.getLastModified().getTime());
			ps.setTimestamp(6, ts);
			
			if(be.getAuthor() == null)
				ps.setString(7, null);
			else
				ps.setString(7, be.getAuthor().getString());
				
			ps.setLong(8, be.getID());
        
			ps.executeUpdate();
			ps.close();       
		
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#removeEntry(net.sf.thingamablog.blog.BlogEntry)
	 */
	public synchronized void removeEntry(String blogKey, long id) throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		try
		{			
			update("DELETE FROM " + table + " WHERE " + ID + " = " + id);
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getEntry(long)
	 */
	public synchronized BlogEntry getEntry(String blogKey, long id) throws BackendException
	{
		BlogEntry be = new BlogEntry();
		String table = ENTRY_TABLE + blogKey;
		try
		{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
				"SELECT * FROM " + table + 
				" WHERE " + ID + " = " + id);

			rs.next();
			        
			be.setDate(rs.getTimestamp(TIMESTAMP));
			be.setCategories(tokenizeCatString(rs.getString(CATEGORIES)));
			be.setID(rs.getInt(ID));
			be.setTitle(rs.getString(TITLE));
			be.setText(rs.getString(ENTRY));
			
			Author a = new Author();
			String auth = rs.getString(AUTHOR);
			if(auth != null)
				a.setString(auth);
			be.setAuthor(a);
			
			be.setDraft(rs.getBoolean(DRAFT));
			be.setLastModified(rs.getTimestamp(MODIFIED));
			
			st.close();			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}		
		return be;
	}


	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getEntries(java.lang.String, boolean)
	 */
	public synchronized EntryEnumeration getEntries(String blogKey, boolean orderByDateAsc)
		throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		EntryEnumeration entries = null;
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + table +
			" WHERE " + DRAFT + " = " + false + 
			" ORDER BY " + TIMESTAMP + " " + orderBy(orderByDateAsc));			
			
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}		
		return entries;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getEntriesFromCategory(java.lang.String, java.lang.String, boolean)
	 */
	public synchronized EntryEnumeration getEntriesFromCategory(String blogKey, String category, boolean orderByDateAsc)
		throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		EntryEnumeration entries = null;
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + table +
			" WHERE " + CATEGORIES + " LIKE CONCAT('%', CONCAT(?, '%'))" +
			" AND " + DRAFT + " = " + false + 
			" ORDER BY " + TIMESTAMP + " " + orderBy(orderByDateAsc));         	
			
			ps.setString(1, toDBEntryCat(category));
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}
		
		return entries;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getEntriesBefore(java.lang.String, java.util.Date, boolean)
	 */
	public synchronized EntryEnumeration getEntriesBefore(String blogKey, Date d, boolean orderByDateAsc)
		throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		EntryEnumeration entries = null;
		Timestamp ts = new Timestamp(d.getTime());
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + table +
			" WHERE " + TIMESTAMP + " <= ?" + 
			" AND " + DRAFT + " = " + false + 
			" ORDER BY " + TIMESTAMP + " " + orderBy(orderByDateAsc));
         	
			ps.setTimestamp(1, ts);
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}
		
		return entries;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getEntriesAfter(java.lang.String, java.util.Date, boolean)
	 */
	public synchronized EntryEnumeration getEntriesAfter(String blogKey, Date d, boolean orderByDateAsc)
		throws BackendException
	{		
		String table = ENTRY_TABLE + blogKey;
		EntryEnumeration entries = null;
		Timestamp ts = new Timestamp(d.getTime());
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + table +
			" WHERE " + TIMESTAMP + " >= ?" + 
			" AND " + DRAFT + " = " + false + 
			" ORDER BY " + TIMESTAMP + " " + orderBy(orderByDateAsc));
         	
			ps.setTimestamp(1, ts);
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}
		
		return entries;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getEntriesBetween(java.lang.String, java.util.Date, java.util.Date, boolean)
	 */
	public synchronized EntryEnumeration getEntriesBetween(String blogKey, Date from, Date to, boolean orderByDateAsc)
		throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		EntryEnumeration entries = null;
		Timestamp fts = new Timestamp(from.getTime());
		Timestamp tts = new Timestamp(to.getTime());
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + table +
			" WHERE " + TIMESTAMP + " >= ? AND " + TIMESTAMP + " <= ?" +
			" AND " + DRAFT + " = " + false +  
			" ORDER BY " + TIMESTAMP + " " + orderBy(orderByDateAsc));
         	
			ps.setTimestamp(1, fts);
			ps.setTimestamp(2, tts);
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}
		
		return entries;
	}

	/* (non-Javadoc)
	 * @see net.sf.thingamablog.blog.WeblogBackend#getDraftEntries(java.lang.String, boolean)
	 */
	public synchronized EntryEnumeration getDraftEntries(String blogKey, boolean orderByDateAsc)
		throws BackendException
	{
		String table = ENTRY_TABLE + blogKey;
		EntryEnumeration entries = null;
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + table +
			" WHERE " + DRAFT + " = " + true + 
			" ORDER BY " + TIMESTAMP + " " + orderBy(orderByDateAsc));
			
			ResultSet rs = ps.executeQuery();
			entries = new RSEntryEnumeration(rs, ps);			
		}
		catch(Exception ex)
		{
			throw new BackendException(ex);
		}		
		return entries;
	}
	
	
	
	//*********** FeedBackend implementation *****************
	public synchronized void addItem(FeedItem item, boolean addIfExists) throws FeedBackendException
	{
		if(!addIfExists)
		{
			try
			{			
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(
					"SELECT * FROM " + FEED_TABLE + 
					" WHERE " + HASH_CODE + " = " + item.hashCode());

				if(rs.next()) //this item exists in the DB so we won't addit
				{
					st.close();
					return;
				}
				st.close();
				
			}
			catch(SQLException sqle)
			{
				throw new FeedBackendException(sqle);
			}
		}
		
		try
		{
			PreparedStatement ps = conn.prepareStatement
			(
					"INSERT INTO " + FEED_TABLE + "(" +
					RETRIEVED + ", " +
					PUB_DATE + ", " +
					LINK + ", " +
					CHANNEL_LINK + ", " +
					ITEM_TITLE + ", " +
					DESC + ", " +
					ITEM_AUTHOR + ", " +
					READ + ", " +
					HASH_CODE + ", " +
					CHANNEL_TITLE + ", " +
					CHANNEL_IMG_LINK + ") " +
					"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
			);
        
			ps.setTimestamp(1, new Timestamp(item.getRetrieved().getTime()));
			//FIXME should be a date, not a string
			//The feed table is a LONGVARCHAR for the pubdate
			//A timestamp would be better, but since users already
			//have DBs initialized with this column, we have to settle
			//for converting dates to and from strings for now
			if(item.getPubDate() != null)
			    ps.setString(2, item.getPubDate().getTime() + "");
			ps.setString(3, item.getLink());
			ps.setString(4, item.getChannelLink());
			ps.setString(5, item.getTitle());
			ps.setString(6, item.getDescription());		
			ps.setString(7, item.getAuthor());
			ps.setBoolean(8, item.isRead());
			ps.setInt(9, item.hashCode());
			ps.setString(10, item.getChannelTitle());
			ps.setString(11, item.getChannelImageURL());
			
			ps.executeUpdate();
			ps.close();       	
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);	
		}		
	}
	
	public synchronized void updateItem(FeedItem item) throws FeedBackendException
	{
		try
		{
			PreparedStatement ps = conn.prepareStatement
			(
					"UPDATE " + FEED_TABLE + " SET " +
					RETRIEVED + " = ?, " +
					PUB_DATE + " = ?, " +
					LINK + " = ?, " +
					CHANNEL_LINK + " = ?, " +
					ITEM_TITLE + " = ?, " +
					DESC + " = ?, " +
					ITEM_AUTHOR + " = ?, " +
					READ + " = ?, " +
					HASH_CODE + " = ?, " +
					CHANNEL_TITLE + " = ?, " +
					CHANNEL_IMG_LINK + " = ? " +
					"WHERE " + ITEM_ID + " = ?"
			);
        
			ps.setTimestamp(1, new Timestamp(item.getRetrieved().getTime()));
			//FIXME should be a date
			if(item.getPubDate() != null)
			    ps.setString(2, item.getPubDate().getTime() + "");
			ps.setString(3, item.getLink());
			ps.setString(4, item.getChannelLink());
			ps.setString(5, item.getTitle());
			ps.setString(6, item.getDescription());		
			ps.setString(7, item.getAuthor());
			ps.setBoolean(8, item.isRead());
			ps.setInt(9, item.hashCode());
			ps.setString(10, item.getChannelTitle());
			ps.setString(11, item.getChannelImageURL());
			ps.setLong(12, item.getID());
        
			ps.executeUpdate();
			ps.close();		
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);
		}		
	}
	
	public synchronized void removeItem(long id) throws FeedBackendException
	{
		try
		{			
			update("DELETE FROM " + FEED_TABLE + " WHERE " + ITEM_ID + " = " + id);
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);
		}		
	}
	
	public synchronized FeedItem getItem(long id) throws FeedBackendException
	{
		FeedItem item = new FeedItem();
		try
		{
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
				"SELECT * FROM " + FEED_TABLE + 
				" WHERE " + ITEM_ID + " = " + id);

			rs.next();
			
			item.setRetrieved(rs.getTimestamp(RETRIEVED));
			//FIXME should be a date
			try{
			    item.setPubDate(new Date(Long.parseLong(rs.getString(PUB_DATE))));
			}catch(Exception ex){}			
			item.setLink(rs.getString(LINK));			
			item.setTitle(rs.getString(ITEM_TITLE));
			item.setDescription(rs.getString(DESC));
			item.setAuthor(rs.getString(ITEM_AUTHOR));
			item.setRead(rs.getBoolean(READ));
			item.setID(rs.getInt(ITEM_ID));
			item.setChannelLink(rs.getString(CHANNEL_LINK));
			
			item.setChannelTitle(rs.getString(CHANNEL_TITLE));
			item.setChannelImageURL(rs.getString(CHANNEL_IMG_LINK));			
			st.close();			
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);
		}		
		return item;
	}
	
	public synchronized FeedItem[] getItems(String channelLink, boolean orderByRetDateAsc) throws FeedBackendException
	{
		FeedItem items[];
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + FEED_TABLE +
			" WHERE " + CHANNEL_LINK + " = ?" + 
			" ORDER BY " + RETRIEVED + " " + orderBy(orderByRetDateAsc));			
			
			ps.setString(1, channelLink);
			ResultSet rs = ps.executeQuery();
			items = createItemsFromResultSet(rs);
			
			ps.close();						
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);
		}	

		return items;
	}
	
	
	public synchronized FeedItem[] getUnreadItems(String channelLink, boolean orderByRetDateAsc) throws FeedBackendException
	{
		FeedItem items[];
		try
		{		
			PreparedStatement ps = conn.prepareStatement
			("SELECT * FROM " + FEED_TABLE +
			" WHERE " + CHANNEL_LINK + " = ? AND " + READ + " = ?" + 
			" ORDER BY " + RETRIEVED + " " + orderBy(orderByRetDateAsc));			
			
			ps.setString(1, channelLink);
			ps.setBoolean(2, false);
			ResultSet rs = ps.executeQuery();
			items = createItemsFromResultSet(rs);
			
			ps.close();						
		}
		catch(Exception ex)
		{
			throw new FeedBackendException(ex);
		}		

		return items;
	}
	    
        
	/**
	 * Shuts down and compacts the database. This should be called
	 * whenever the application exits
	 * @throws SQLException If an error occurs while shutting down
	 */
	public synchronized void shutdown() throws SQLException 
	{
		if(conn == null || conn.isClosed())
			return;
			
		Statement st = conn.createStatement();        
		st.executeQuery("SHUTDOWN COMPACT");
		st.close();      
		conn.close();   // if there are no other open connection
						// db writes out to files and shuts down
						// this happens anyway at garbage collection
						// when program ends
		System.out.println("Database shutdown");
	}
	
	protected void connect(String db_file_name_prefix) throws Exception// note more general exception
	{

		// Load the HSQL Database Engine JDBC driver
		// hsqldb.jar should be in the class path or made part of the current jar		
		Class.forName("org.hsqldb.jdbcDriver");		

		// connect to the database.   This will load the db files and start the
		// database if it is not alread running.
		// db_file_name_prefix is used to open or create files that hold the state
		// of the db.
		// It can contain directory names relative to the
		// current working directory
		System.out.println("\nConnecting to database...");
		conn = DriverManager.getConnection("jdbc:hsqldb:"
										   + db_file_name_prefix,   // filenames
										   "sa",                    // username
										   "");                     // password
		System.out.println("Connected.");
	}
	
	protected synchronized void update(String expression) throws SQLException 
	{
		Statement st = null;
		st = conn.createStatement();                // statements

		int i = st.executeUpdate(expression);       // run the query

		if (i == -1) {
			System.out.println("db error : " + expression);
		}

		st.close();
	}
	
	protected synchronized void query(String expression) throws SQLException 
	{
		Statement st = null;
		ResultSet rs = null;

		st = conn.createStatement();            // statement objects can be reused with
												// repeated calls to execute but we
												// choose to make a new one each time
		rs = st.executeQuery(expression);       // run the query
		st.close();     
	}
	
	private String toDBEntryCat(String cat)
	{
		return "<" + cat + ">";
	}
	
	private String[] tokenizeCatString(String cats)
	{
		if(cats == null || cats.length() == 0)
			return null;
    		
		StringTokenizer st = new StringTokenizer(cats, "\n");
		String c[] = new String[st.countTokens()];
		int i = 0;
    	
		while(st.hasMoreTokens()) 
		{
			StringBuffer s = new StringBuffer(st.nextToken());
			if(s.toString().endsWith(">"))
				s.deleteCharAt(s.length() - 1);
			if(s.toString().startsWith("<"))
				s.deleteCharAt(0);
        	
			c[i++] = s.toString();
		}     	
		return c;
	}
    
	private String catsString(String c[])
	{
		if(c == null)
			return null;
    	
		String s = "";
		for(int i = 0; i < c.length; i++)
		{
			s += toDBEntryCat(c[i]);
			if(i != c.length - 1)
				s += "\n";				
		}    	
		return s;
	}
	
	private String orderBy(boolean asc)
	{
		String order = "DESC";
		if(asc)order = "ASC";
		return order;
	}
	
	private FeedItem[] createItemsFromResultSet(ResultSet rs) throws SQLException
	{
		Vector v = new Vector(50, 10);
		while(rs.next())
		{				
			FeedItem item = new FeedItem();
			item.setRetrieved(rs.getTimestamp(RETRIEVED));
			//FIXME should be a date
			try{			    
			    item.setPubDate(new Date(Long.parseLong(rs.getString(PUB_DATE))));
			}catch(Exception ex){}	
			item.setLink(rs.getString(LINK));
			item.setTitle(rs.getString(ITEM_TITLE));
			item.setDescription(rs.getString(DESC));
			item.setAuthor(rs.getString(ITEM_AUTHOR));
			item.setRead(rs.getBoolean(READ));
			item.setID(rs.getInt(ITEM_ID));
			
			item.setChannelLink(rs.getString(CHANNEL_LINK));
			
			item.setChannelTitle(rs.getString(CHANNEL_TITLE));
			item.setChannelImageURL(rs.getString(CHANNEL_IMG_LINK));				
			v.add(item);	
		}
		
		FeedItem items[] = new FeedItem[v.size()];
		for(int i = 0; i < items.length; i++)
			items[i] = (FeedItem)v.elementAt(i);
		return items;
	}
	
	/**
	 * 
	 * Database implementation of an AuthorStore
	 * 
	 * @author Bob Tantlinger
	 *
	 */
	private class DBAuthorStore implements AuthorStore
	{
		public synchronized void addAuthor(String blogKey, Author auth) throws BackendException
		{
			try
			{
				PreparedStatement ps = conn.prepareStatement
				(
					"INSERT INTO " + AUTH_TABLE + "(" +
					BLOG + ", " +
					BLOG_AUTHOR + ") " +
					"VALUES(?, ?)"
				);
				ps.setString(1, blogKey);
				ps.setString(2, auth.getString());
				ps.executeUpdate();
				ps.close();
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
		}
		
		public synchronized void removeAuthor(String blogKey, Author auth) throws BackendException
		{			
			try
			{
				PreparedStatement ps = conn.prepareStatement
				(
					"DELETE FROM " + AUTH_TABLE + " WHERE " +
					BLOG + " = ? AND " + BLOG_AUTHOR + " = ?"
				);
				ps.setString(1, blogKey);
				ps.setString(2, auth.getString());
				ps.executeUpdate();
				ps.close();
				
				String table = ENTRY_TABLE + blogKey;
				PreparedStatement ps2 = conn.prepareStatement
				("SELECT * FROM " + table +
				 " WHERE " + AUTHOR + " = ?");
         
				ps2.setString(1, auth.getString());
				ResultSet rs = ps2.executeQuery();
				while(rs.next())
				{
					int id = rs.getInt(ID);
					PreparedStatement update = conn.prepareStatement
					(
						"UPDATE " + table + " SET " +
						 AUTHOR + " = ? " +               
						"WHERE " + ID + " = ?"
					);        
					update.setString(1, "");
					update.setInt(2, id);
					update.executeUpdate();
					update.close();			      		
				}
      	
				ps2.close();				
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
		}
		
		public synchronized void updateAuthor(String blogKey, Author oldAuth, Author newAuth) throws BackendException
		{
			try
			{
				PreparedStatement ps = conn.prepareStatement
				(
					"UPDATE " + AUTH_TABLE + " SET " +
					BLOG_AUTHOR + " = ? WHERE " + BLOG + " = ? AND " +
					BLOG_AUTHOR + " = ?"
						
				);				
				ps.setString(1, newAuth.getString());
				ps.setString(2, blogKey);
				ps.setString(3, oldAuth.getString());			
        
				ps.executeUpdate();
				ps.close();
				
				String table = ENTRY_TABLE + blogKey;
				PreparedStatement ps2 = conn.prepareStatement
				("SELECT * FROM " + table +
				 " WHERE " + AUTHOR + " = ?");
         
				ps2.setString(1, oldAuth.getString());
				ResultSet rs = ps2.executeQuery();
				while(rs.next())
				{
					int id = rs.getInt(ID);
					PreparedStatement update = conn.prepareStatement
					(
						"UPDATE " + table + " SET " +
						 AUTHOR + " = ? " +               
						"WHERE " + ID + " = ?"
					);        
					update.setString(1, newAuth.getString());
					update.setInt(2, id);
					update.executeUpdate();
					update.close();			      		
				}
      	
				ps2.close();     	   		
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
		}
		
		public synchronized Author[] getAuthors(String blogKey, boolean sortAsc) throws BackendException
		{
			Vector v = new Vector(4, 2);
			try
			{
				PreparedStatement ps = conn.prepareStatement
				("SELECT * FROM " + AUTH_TABLE +
				" WHERE " + BLOG + " = ?" + 
				" ORDER BY " + BLOG_AUTHOR + " " + orderBy(sortAsc));
			
				ps.setString(1, blogKey);
				ResultSet rs = ps.executeQuery();				
				while(rs.next())
				{
					Author auth = new Author();
					auth.setString(rs.getString(BLOG_AUTHOR));
					v.add(auth);
				}
				ps.close();				
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
			
			Author authors[] = new Author[v.size()];
			for(int i = 0; i < authors.length; i++)			
				authors[i] = (Author)v.elementAt(i);
			
			return authors;			
		}
	}
	
	
	/**
	 * Database CategoryStore
	 * 
	 * @author Bob
	 */
	private class DBCategoryStore implements CategoryStore
	{
		public synchronized void addCategory(String blogKey, String cat) throws BackendException
		{
			try //what happens if a cat is added that already exists?
			{
				PreparedStatement ps = conn.prepareStatement
				(
					"INSERT INTO " + CAT_TABLE + "(" +
					BLOG + ", " +
					BLOG_CATEGORY + ") " +
					"VALUES(?, ?)"
				);
				ps.setString(1, blogKey);
				ps.setString(2, cat);
				ps.executeUpdate();
				ps.close();
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
		}
		
		public synchronized void removeCategory(String blogKey, String cat) throws BackendException
		{
			try
			{
				PreparedStatement ps = conn.prepareStatement
				(
					"DELETE FROM " + CAT_TABLE + " WHERE " +
					BLOG + " = ? AND " + BLOG_CATEGORY + " = ?"
				);
				ps.setString(1, blogKey);
				ps.setString(2, cat);
				ps.executeUpdate();
				ps.close();
				
				String table = ENTRY_TABLE + blogKey;
				PreparedStatement ps2 = conn.prepareStatement
				 ("SELECT * FROM " + table +
				  " WHERE " + CATEGORIES + " LIKE CONCAT('%', CONCAT(?, '%'))");
         
				 String theCat = toDBEntryCat(cat);
				 ps2.setString(1, theCat);
				 ResultSet rs = ps2.executeQuery();
				 while(rs.next())
				 {
					 String c = rs.getString(CATEGORIES);
					 int id = rs.getInt(ID);
					 int s = c.indexOf(theCat);
		
					 // + 1 gets rid of trailing space
					 // seems kind of dangerous, but it works...
					 int e = s + theCat.length() + 1; 
		
					 StringBuffer sb = new StringBuffer(c);
					 sb.delete(s, e);
					 //System.out.println(sb.toString());
			
					 PreparedStatement update = conn.prepareStatement
					 (
						 "UPDATE " + table + " SET " +
						  CATEGORIES + " = ? " +               
						 "WHERE " + ID + " = ?"
					 );        
					 update.setString(1, sb.toString());
					 update.setInt(2, id);
					 update.executeUpdate();
					 update.close();			      		
				 }      	
				 ps2.close();     	
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
		}
		
		public synchronized void renameCategory(String blogKey, String oldCat, String newCat) throws BackendException
		{
			try
			{
				PreparedStatement ps = conn.prepareStatement
				(
						"UPDATE " + CAT_TABLE + " SET " +
						BLOG_CATEGORY + " = ? WHERE " + BLOG + " = ? AND " +
						BLOG_CATEGORY + " = ?"						
				);				
				ps.setString(1, newCat);
				ps.setString(2, blogKey);
				ps.setString(3, oldCat);			
        
				ps.executeUpdate();
				ps.close();	
				
				String table = ENTRY_TABLE + blogKey;
				PreparedStatement ps2 = conn.prepareStatement
				 ("SELECT * FROM " + table +
				  " WHERE " + CATEGORIES + " LIKE CONCAT('%', CONCAT(?, '%'))");
         
				
				 ps2.setString(1, toDBEntryCat(oldCat));
				 ResultSet rs = ps2.executeQuery();
				 while(rs.next())
				 {
					 String c = rs.getString(CATEGORIES);
					 int id = rs.getInt(ID);
					 int s = c.indexOf(oldCat);
					 int e = s + oldCat.length(); 
		
					 StringBuffer sb = new StringBuffer(c);
					 sb.replace(s, e, newCat);
					 //System.out.println(sb.toString());
			
					 PreparedStatement update = conn.prepareStatement
					 (
						 "UPDATE " + table + " SET " +
						  CATEGORIES + " = ? " +               
						 "WHERE " + ID + " = ?"
					 );        
					 update.setString(1, sb.toString());
					 update.setInt(2, id);
					 update.executeUpdate();
					 update.close();			      		
				 }
      	
				 ps2.close();	
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
		}
		
		public synchronized String[] getCategories(String blogKey, boolean sortAsc) throws BackendException
		{
			Vector v = new Vector(4, 2);
			try
			{
				PreparedStatement ps = conn.prepareStatement
				("SELECT * FROM " + CAT_TABLE +
				" WHERE " + BLOG + " = ?" + 
				" ORDER BY " + BLOG_CATEGORY + " " + orderBy(sortAsc));
			
				ps.setString(1, blogKey);
				ResultSet rs = ps.executeQuery();				
				while(rs.next())
				{
					String str = rs.getString(BLOG_CATEGORY);
					v.add(str);
				}
				ps.close();				
			}
			catch(Exception ex)
			{
				throw new BackendException(ex);
			}
			
			//sort the cats properly
			final java.text.Collator catCollator = java.text.Collator.getInstance();
			final boolean asc = sortAsc;    	
			Comparator catComparator = new Comparator()
			{
				public boolean equals(Object o)
				{ 
					return o.equals(this);
				}
        
				public int compare(Object o1, Object o2) 
				{
					String c1 = o1.toString();
					String c2 = o2.toString();
					if(asc)
						return catCollator.compare(c1.toLowerCase(), c2.toLowerCase());
					return catCollator.compare(c2.toLowerCase(), c1.toLowerCase());
				}
			};    	
			Collections.sort(v, catComparator);
			
			String cats[] = new String[v.size()];
			for(int i = 0; i < cats.length; i++)			
				cats[i] = v.elementAt(i).toString();			
			return cats;			
		}
	}	
	
	
	/**
	 * EntryEnumerator implementation which walks through a ResultSet
	 * 
	 * @author Bob
	 */
	private class RSEntryEnumeration implements EntryEnumeration
	{
		private ResultSet rs = null;
		private Statement st = null;
		
		public RSEntryEnumeration(ResultSet r, Statement s)
		{
			st = s;
			rs = r;	
		}
		
		public boolean hasMoreEntries()
		{
			boolean hasNext = false;
			try{
				hasNext = rs.next();
			}catch(SQLException ex){}
			
			return rs != null && hasNext;
		}
		
		public BlogEntry nextEntry()
		{
			BlogEntry h = null;
			try
			{
				h = new BlogEntry();
				h.setID(rs.getInt(ID));
				h.setTitle(rs.getString(TITLE));
				h.setCategories(tokenizeCatString(rs.getString(CATEGORIES)));
				h.setDate(rs.getTimestamp(TIMESTAMP));
				h.setDraft(rs.getBoolean(DRAFT));
				h.setLastModified(rs.getTimestamp(MODIFIED));
				String auth = rs.getString(AUTHOR);
				if(auth != null)
				{				
					Author a = new Author();
					a.setString(auth);
					h.setAuthor(a);
				}	
				h.setText(rs.getString(ENTRY));							
			}
			catch(SQLException sqle)
			{
				sqle.printStackTrace();
			}
			return h;
		}
		
		public void close()
		{
			try
			{
				st.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();		
			}			
		}
	}

}
