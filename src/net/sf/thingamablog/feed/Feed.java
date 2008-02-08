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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.thingamablog.TBGlobals;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;



/**
 * Class which defines a news feed
 * 
 * @author Bob Tantlinger
 *
 */
public class Feed
{
    private FeedBackend backend;
    private String url = "";
    private String link = null;
    private Date lastUpdated = null;
    private boolean lastUpdateFailed;
    private String lastUpdateFailedReason = "";

    private boolean isLimitItems = true;
    private int itemLimit = 50;

    private String title = "";
    private String description = "";
    private String language = "";
    private String imageURL = null;
    private String managingEditor = "";
    private String copyright = "";

    /**
     * Constructs a Feed
     * 
     * @param url The url of the Feed
     */
    public Feed(String url)
    {
        this.url = url;
    }    
    
	/**
	 * Updates the feed from the file located at the Feed's url
	 * 
	 * @throws FeedBackendException If an error occurs while updating the feed
	 */
    public void update() throws FeedBackendException
    {
		lastUpdateFailed = false;
		//SyndFeedI feed = null;
		SyndFeed feed = null;
		
		try 
		{
			URL feedUrl = new URL(url);
			SyndFeedInput input = new SyndFeedInput();
			feed = input.build(new XmlReader(feedUrl));
			//feed = input.build(FeedUtils.getFeedReader(feedUrl));
		}
		catch(MalformedURLException ex) 
		{
			lastUpdateFailedReason = "Invalid URL";
			lastUpdateFailed = true;			
			System.out.println("ERROR: "+ex.getMessage());
		}
		catch(Exception ex)
		{
			lastUpdateFailedReason = "Update failed";
			lastUpdateFailed = true;			
			System.out.println("ERROR: "+ex.getMessage());
		}
		
		if(lastUpdateFailed)
			return;
		
		List fItems = feed.getEntries();
		Iterator it = fItems.iterator();
		while(it.hasNext())
		{
			SyndEntry si = (SyndEntry)it.next();			
			FeedItem fi = new FeedItem();
			fi.setChannelLink(url);
			fi.setLink(si.getLink());
			fi.setAuthor(si.getAuthor());
			fi.setTitle(si.getTitle());
			fi.setChannelTitle(feed.getTitle());			
			
			fi.setRetrieved(new Date());
			Date pubDate = si.getPublishedDate();
			if(pubDate != null)
			    fi.setPubDate(pubDate);
			else
			    fi.setPubDate(fi.getRetrieved());
			
			if(feed.getImage() != null)
				fi.setChannelImageURL(feed.getImage().getUrl());						

			String itemDescr = "";				
			List cont = si.getContents();
			Iterator cIt = cont.iterator();
			while(cIt.hasNext())
			{
				SyndContent iContent = (SyndContent)cIt.next();
				itemDescr += iContent.getValue();
			}				
			fi.setDescription(itemDescr);		
			
			backend.addItem(fi, false);  
    	}
		
		//adjust items for limit
		if(isLimitItems)
		{
			FeedItem items[] = backend.getItems(url, false);
			for (int i = 0; i < items.length; i++)
			{
				if (i >= itemLimit)
					removeItem(items[i]);
			}
		}
		
		setTitle(feed.getTitle());
		setDescription(feed.getDescription());
		setLanguage(feed.getLanguage());
		//setManagingEditor(channel.getManagingEditor());
		setLink(feed.getLink());
		setCopyright(feed.getCopyright());
		setLastUpdated(new Date());
		
		
		if(feed.getImage() != null)
		{
			setImageURL(feed.getImage().getUrl());
			saveImage(feed.getImage().getUrl());
		}
		
    }
	


    private void saveImage(String iUrl)
    {
		if(iUrl == null)
			return;
		
		String imgType = null;
		if(iUrl.endsWith(".gif"))
			imgType = ".gif";
		else if(iUrl.endsWith(".jpg"))
			imgType = ".jpg";
		else if(iUrl.endsWith(".png"))
			imgType = ".png";
		else
			return;
		
		File dir = new File(TBGlobals.IMG_CACHE_DIR);
		if(!dir.exists() || dir.isFile())
			dir.mkdir();
			
		String fileName = Math.abs(iUrl.hashCode()) + imgType;		
		File file = new File(dir, fileName);
		if(file.exists())
			return; //don't download it if it exists
        
        try
        {
            URL url = new URL(iUrl);            
            // Copy resource to local file, use remote file           
            InputStream is = url.openStream();           
                       
            FileOutputStream fos = null;    
            fos = new FileOutputStream(file);
            int oneChar, count = 0;
            while ((oneChar = is.read()) != -1)
            {
                fos.write(oneChar);
                count++;
            }
            is.close();
            fos.close();            

        }
        catch (MalformedURLException e)
        {
            System.err.println(e.toString());
        }
        catch (IOException e)
        {
            System.err.println(e.toString());
        }
    }

	/**
	 * Updates a news item that exists in the feed
	 * 
	 * @param item The Item to update
	 * @throws FeedBackendException If an error occurs wile updating the item
	 */
    public void updateItem(FeedItem item) throws FeedBackendException
    {
        if (item.getChannelLink().equals(url))
            backend.updateItem(item);
    }

	/**
	 * Finds items in the feed
	 * 
	 * @param search The search criteria
	 * @return The items found
	 * @throws FeedBackendException If an error occurs while finding items
	 */
    public FeedItem[] findItems(FeedSearch search) throws FeedBackendException
    {
        return backend.findItems(url, search);
    }

	/**
	 * Gets all the items in this feed
	 * 
	 * @return The items
	 * @throws FeedBackendException If an error occurs getting the items
	 */
    public FeedItem[] getItems() throws FeedBackendException
    {
        return backend.getItems(url, true);
    }
    
	/**
	 * Gets the unread items in the feed
	 * 
	 * @return unread items
	 * @throws FeedBackendException If an error occurs getting the items
	 */
    public FeedItem[] getUnreadItems() throws FeedBackendException
    {
        return backend.getUnreadItems(url, true);
    }
	
	
	/**
	 * Marks all the items in the feed as read or unread
	 * 
	 * @param isRead true if read, false unread
	 * @throws FeedBackendException If an error occurs marking the items
	 */
    public void markAllItemsRead(boolean isRead) throws FeedBackendException
    {
        FeedItem items[] = getItems();
        for (int i = 0; i < items.length; i++)
        {
            items[i].setRead(isRead);
            backend.updateItem(items[i]);
        }
    }
	
	/**
	 * Removes an item from the feed
	 * 
	 * @param item The item to remove
	 * @throws FeedBackendException If an error occurs removing the item
	 */
    public void removeItem(FeedItem item) throws FeedBackendException
    {
        if (item.getChannelLink().equals(url))
            backend.removeItem(item.getID());
    }
    
    
	/**
	 * Removes all items from the feed
	 * 
	 * @throws FeedBackendException If an error occurs removing the items
	 */
    public void removeAllItems() throws FeedBackendException
    {
        FeedItem items[] = getItems();
        for (int i = 0; i < items.length; i++)
        {
            backend.removeItem(items[i].getID());
        }
    }

    /**
     * Gets the backend for this feed
     * 
     * @return The backend
     */
    public FeedBackend getBackend()
    {
        return backend;
    }

    /**
     * Gets the copyright for this feed
     * 
     * @return The copyright
     */
    public String getCopyright()
    {
        return copyright;
    }

    /**
     * Gets the description for this feed
     * 
     * @return The description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets the image URL of this feed
     * 
     * @return The image URL
     */
    public String getImageURL()
    {
        return imageURL;
    }

    /**
     * Gets the language of this feed
     * 
     * @return The language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Gets the date the feed was last updated
     * 
     * @return The last updated date
     */
    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    /**
     * Gets the link of this feed
     * 
     * @return The link
     */
    public String getLink()
    {
        return link;
    }

    /**
     * Gets the managing editor of this feed
     * 
     * @return The managing editor
     */
    public String getManagingEditor()
    {
        return managingEditor;
    }

    /**
     * Gets the title of this feed
     * 
     * @return The title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Gets the URL of this feed
     * 
     * @return The URL
     */
    public String getURL()
    {
        return url;
    }

    /**
     * Sets the backend for this feed
     * 
     * @param backend The backend
     */
    public void setBackend(FeedBackend backend)
    {
        this.backend = backend;
    }

    /**
     * Sets the copyright for this feed
     * 
     * @param string The copyright
     */
    public void setCopyright(String string)
    {
        copyright = string;
    }

    /**
     * Sets the description for this feed
     * 
     * @param string The description
     */
    public void setDescription(String string)
    {
        description = string;
    }

    /**
     * Sets the image URL for this feed
     * 
     * @param string The image URL
     */
    public void setImageURL(String string)
    {
        imageURL = string;
    }

    /**
     * Sets the language of this feed
     * 
     * @param string The language
     */
    public void setLanguage(String string)
    {
        language = string;
    }

    /**
     * Sets the last updated date of this feed
     * 
     * @param date The date the feed was last updated
     */
    public void setLastUpdated(Date date)
    {
        lastUpdated = date;
    }

    /**
     * Sets the link of the feed
     * 
     * @param string The link
     */
    public void setLink(String string)
    {
        link = string;
    }

    /**
     * Sets the managing editor of the feed
     * 
     * @param string The managing editor
     */
    public void setManagingEditor(String string)
    {
        managingEditor = string;
    }

    /**
     * Sets the title of the feed
     * 
     * @param string The title
     */
    public void setTitle(String string)
    {
        title = string;
    }

    /**
     * Sets the URL of the feed
     * 
     * @param string The URL
     */
    public void setURL(String string)
    {
        url = string;
    }

	/**
	 * Overriden to return the feed title
	 */
    public String toString()
    {
        return getTitle();
    }

    /**
     * Indicates if the last update failed
     * 
     * @return true if it failed, false otherwise
     */
    public boolean isLastUpdateFailed()
    {
        return lastUpdateFailed;
    }

    /**
     * Gets the reason the last update failed
     * 
     * @return The failue reason
     */
    public String getLastUpdateFailedReason()
    {
        if (isLastUpdateFailed())
            return lastUpdateFailedReason;
        return "";
    }

    /**
     * Indicate that the last update failed
     * 
     * @param b true if failed, false otherwise
     */
    public void setLastUpdateFailed(boolean b)
    {
        lastUpdateFailed = b;
    }

    /**
     * Sets the reason for the update failure
     * 
     * @param string The reason
     */
    public void setLastUpdateFailedReason(String string)
    {
        lastUpdateFailedReason = string;
    }

    /**
     * Indicates whether or not this feed limits the number of items
     * 
     * @return true if it limits, false otherwise
     */
    public boolean isLimitItems()
    {
        return isLimitItems;
    }

    /**
     * Gets the maximum number of items this feed can contain
     * 
     * @return The item limit
     */
    public int getItemLimit()
    {
        return itemLimit;
    }

    /**
     * Sets whether or not to limit items
     * 
     * @param b true to limit, false otherwise
     */
    public void setLimitItems(boolean b)
    {
        isLimitItems = b;
    }

    /**
     * Sets the number of items this feed can contain
     * 
     * @param i The item limit
     */
    public void setItemLimit(int i)
    {
        itemLimit = i;
    }
}
