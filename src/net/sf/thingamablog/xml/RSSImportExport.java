/*
 * Created on Aug 5, 2004
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
package net.sf.thingamablog.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.feed.FeedUtils;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;


/**
 * Static methods for importing entries from RSS feeds and exporting
 * weblogs to syndication feed files
 * 
 * @author Bob Tantlinger
 *
 */
public class RSSImportExport
{
	/**
	 * Imports the entries of a feed into a weblog
	 * 
	 * @param feedURL The URL of the feed
	 * @param weblog The weblog to import to
	 * @throws IOException If an error occurs reading the feed
	 * @throws BackendException If an error occurs importing entries
	 * @throws MalformedURLException If the URL is malformed
	 */
	public static void importEntriesFromFeed(String feedURL, Weblog weblog)
	throws IOException, BackendException, MalformedURLException
	{
		//SyndFeedI feed = null;
	    SyndFeed feed = null;
		String weblogCats[] = weblog.getCategories();
		Author weblogAuths[] = weblog.getAuthors();
		
		try 
		{
			URL feedUrl = new URL(feedURL);
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(FeedUtils.getFeedReader(feedUrl));
		}
		catch(FeedException ex)
		{
			System.out.println("ERROR: "+ex.getMessage());
			throw new IOException(ex.getMessage());			
		}
		
		List fItems = feed.getEntries();
		Iterator it = fItems.iterator();
		while(it.hasNext())
		{
			SyndEntry si = (SyndEntry)it.next();
			BlogEntry entry = new BlogEntry();
			
			entry.setCategories(getValidCategories(weblogCats, si));
			Author a = getValidAuthor(weblogAuths, si);
			if(a != null)
				entry.setAuthor(a);			
			if(si.getTitle() != null)
				entry.setTitle(si.getTitle());
			if(si.getPublishedDate() != null)
				entry.setDate(si.getPublishedDate());
			else
				entry.setDate(new Date());
			
			String bodyText = "";
			List cont = si.getContents();
			Iterator cIt = cont.iterator();
			while(cIt.hasNext())
			{
				SyndContent iContent = (SyndContent)cIt.next();
				bodyText += iContent.getValue();				
			}
			
			entry.setText(bodyText);
			weblog.addEntry(entry);
		}		
	}
	
	private static Author getValidAuthor(Author wauths[], SyndEntry e)
	{
		if(e.getAuthor() != null)
		{		
			for(int i = 0; i < wauths.length; i++)
			{
				if(wauths[i].equals(e.getAuthor()))
					return wauths[i];
			}
		}
		
		return null;
	}
	
	private static String[] getValidCategories(String wcats[], SyndEntry e)
	{
		Vector c = new Vector();		
		
		Iterator catIt = e.getCategories().iterator();
		while(catIt.hasNext())
		{
			SyndCategory syndCat = (SyndCategory)catIt.next();
			String cat = syndCat.getName();
			for(int i = 0; i < wcats.length; i++)
			{
				if(cat.equals(wcats[i]))
				{
					c.add(wcats[i]);
					continue;
				}
			}
		}		
		String cats[] = new String[c.size()];
		for(int i = 0; i < cats.length; i++)
			cats[i] = c.elementAt(i).toString();
		return cats;
	}
	
	public static void exportWeblogToFeed(Weblog weblog, File feedFile)
	throws BackendException, IOException
	{
		String enc = "UTF-8";
	    //SyndFeedI feed = new SyndFeed();
		SyndFeed feed = new SyndFeedImpl();
		feed.setFeedType("rss_2.0");
		feed.setEncoding(enc);

		feed.setTitle(weblog.getTitle());
		feed.setLink(weblog.getFrontPageUrl());
		feed.setDescription(weblog.getDescription());
		
		String cats[] = weblog.getCategories();
		List catList = new ArrayList();
		for(int i = 0; i < cats.length; i++)
		{
			//SyndCategory sCat = new SyndCategory();
		    SyndCategory sCat = new SyndCategoryImpl();
			sCat.setName(cats[i]);
			sCat.setTaxonomyUri(null);
			catList.add(sCat);		
		}		
		feed.setCategories(catList);
		
		BlogEntry wEntries[] = weblog.getEntries();
		List entryList = new ArrayList();
		for(int i = 0; i < wEntries.length; i++)
		{
			//SyndEntryI entry = new SyndEntry();
		    SyndEntry entry = new SyndEntryImpl();
			//SyndContentI description = new SyndContent();
		    SyndContent description = new SyndContentImpl();
			
			entry.setTitle(wEntries[i].getTitle());
			entry.setPublishedDate(wEntries[i].getDate());
			
			Author auth = wEntries[i].getAuthor();
			if(auth != null)
				entry.setAuthor(auth.getName());
			
			String ecats[] = wEntries[i].getCategories();
			if(ecats != null)
			{
				List sCatList = new ArrayList();
				for(int j = 0; j < ecats.length; j++)
				{				
					//SyndCategoryI syndCat = new SyndCategory();
				    SyndCategory syndCat = new SyndCategoryImpl();
					syndCat.setName(ecats[j]);
					syndCat.setTaxonomyUri(null);
					sCatList.add(syndCat);
				}
				
				entry.setCategories(sCatList);
			}
			
			description.setType("text/html");
			description.setValue(wEntries[i].getText());
			entry.setDescription(description);
			
			entryList.add(entry);						
		}
		
		feed.setEntries(entryList);
		
		Writer writer = new OutputStreamWriter(new FileOutputStream(feedFile), enc);
		SyndFeedOutput output = new SyndFeedOutput();
		try
		{		
			output.output(feed, writer);
		}
		catch(Exception ex)
		{
			throw new IOException(ex.getMessage());
		}
		writer.close();
	}	
}
