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

import java.util.Date;

/**
 * Class which defines a FeedItem. A FeedItem is an article contained in a
 * syndication Feed
 * 
 * @author Bob Tantlinger
 *
 */
public class FeedItem
{
	private String title = "";
	private String link = null;
	private String channelLink = null;
	
	private String channelTitle = "";
	private String channelImageURL = "";
	
	private String description = "";
	private Date retrieved = new Date();
	private Date pubDate;
	private String author = "";
	private boolean isRead;
	private long id;
	
    /**
     * Gets the author of the item
     * 
     * @return The author
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Gets the channel link/Feed link of the item
     * 
     * @return The link
     */
    public String getChannelLink()
    {
        return channelLink;
    }

    /**
     * Gets the description 
     * 
     * @return The description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Indicates whether this item has been read
     * 
     * @return true if read, false otherwise
     */
    public boolean isRead()
    {
        return isRead;
    }

    /**
     * Gets the link for this item
     * 
     * @return The link
     */
    public String getLink()
    {
        return link;
    }

    /**
     * Gets the publish date of this item
     * 
     * @return The pubdate
     */
    public Date getPubDate()
    {
        if(pubDate == null)
            return getRetrieved();
        return pubDate;
    }
    
 

    /**
     * Gets the date this item was retrieved
     * 
     * @return The retrieval date
     */
    public Date getRetrieved()
    {
        return retrieved;
    }

    /**
     * Gets the title
     * 
     * @return The title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the author
     * 
     * @param string The author
     */
    public void setAuthor(String string)
    {
        author = string;
    }

    /**
     * Sets the channel/feed link
     * 
     * @param string The link
     */
    public void setChannelLink(String string)
    {
        channelLink = string;
    }

    /**
     * Sets the description
     * 
     * @param string The description
     */
    public void setDescription(String string)
    {
        description = string;
    }

    /**
     * Sets if this item is read
     * 
     * @param b The read state, true = read, false = unread
     */
    public void setRead(boolean b)
    {
        isRead = b;
    }

    /**
     * Sets the item link
     * 
     * @param string The link
     */
    public void setLink(String string)
    {
        link = string;
    }
    

    /**
     * Sets the publish date
     * 
     * @param date The pubdate
     */
    public void setPubDate(Date pd)
    {
        pubDate = pd;
    }

    /**
     * Sets the date the item was retrieved
     * 
     * @param date The date
     */
    public void setRetrieved(Date date)
    {
        retrieved = date;
    }

    /**
     * Sets the title
     * 
     * @param string The title
     */
    public void setTitle(String string)
    {
        title = string;
    }

    /**
     * Gets the item's ID
     * 
     * @return The ID
     */
    public long getID()
    {
        return id;
    }

    /**
     * Sets the item's ID
     * 
     * @param l The ID
     */
    public void setID(long l)
    {
        id = l;
    }
    
    public String toString()
    {
    	String s = "\n";
    	s += title + "\n";
    	s += author + "\n";
    	s += description + "\n";
    	s += link + "\n\n";
    	return s;
    }
    
	/**
	 * Generates a reasonably unique hash code for this item. Currently it uses
	 * the hash codes for the title and the link added together.
	 * 
	 * @return a unique hash
	 */
	public int hashCode() 
	{
		// Return a semi-unique hash code for this item.
		if(description == null)
			description = "";
		if(channelLink == null)
			channelLink = "";
		if(title == null)
		    title = "";
		
		return title.hashCode() + link.hashCode() + description.hashCode() + channelLink.hashCode();
	}
	
    /**
     * @return
     */
    public String getChannelImageURL()
    {
        return channelImageURL;
    }

    /**
     * @return
     */
    public String getChannelTitle()
    {
        return channelTitle;
    }

    /**
     * @param string
     */
    public void setChannelImageURL(String string)
    {
        channelImageURL = string;
    }

    /**
     * @param string
     */
    public void setChannelTitle(String string)
    {
        channelTitle = string;
    }

}
