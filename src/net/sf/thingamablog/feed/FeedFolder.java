/*
 * Created on May 4, 2004
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;


/**
 * FeedFolders maintain a hierarchical list of Feeds, 
 * in addition to other FeedFolders
 * 
 * @author Bob Tantlinger
 *
 */
public class FeedFolder
{
	private String name;
	private FeedFolder parent = null;
	private Vector feedChildren = new Vector(5, 2);
	private Vector folderChildren = new Vector(5, 2);
	
	/**
	 * Constructs a FeedFolder
	 * 
	 * @param name The folder's name
	 */
	public FeedFolder(String name)
	{
		this.name = name;
		//this.parent = parent;
	}
	
	/**
	 * Gets the feed children of this folder
	 * 
	 * @return The folder's Feed children
	 */
	public Feed[] getFeeds()
	{
		Feed feeds[] = new Feed[feedChildren.size()];
		for(int i = 0; i < feeds.length; i++)
			feeds[i] = (Feed)feedChildren.elementAt(i);
		
			
		return feeds;
	} 
	
	public List getFeeds1()
	{
		return feedChildren;
	}
	
	/**
	 * Gets the folder children of this folder
	 * 
	 * @return The folder children
	 */
	public FeedFolder[] getFolders()
	{
		FeedFolder f[] = new FeedFolder[folderChildren.size()];
		for(int i = 0; i < f.length; i++)
			f[i] = (FeedFolder)folderChildren.elementAt(i);
		
		
		return f;		
	}
	
	public List getFolders1()
	{
		return folderChildren;
	}
	
	/**
	 * Adds a Feed to this folder
	 * 
	 * @param f The feed to add
	 */
	public void addFeed(Feed f)
	{
		if(!feedChildren.contains(f))
		{		
			feedChildren.addElement(f);
			Collections.sort(feedChildren, new FeedComparator());
		}
	}
	
	/**
	 * Adds a subfolder to this folder and sets this folder
	 * as the added folder's parent
	 * 
	 * @param f The subfolder
	 */
	public void addFolder(FeedFolder f)
	{
		if(!folderChildren.contains(f))
		{
			f.setParent(this);
			folderChildren.addElement(f);
			Collections.sort(folderChildren, new FeedFolderComparator());
		}
	}
	
	/**
	 * Removes a feed from this folder
	 * 
	 * @param f The Feed to remove
	 */
	public void removeFeed(Feed f)
	{
		feedChildren.removeElement(f);
	}
	
	/**
	 * Removes a subfolder from this folder
	 * 
	 * @param f The subfolder to remove
	 */
	public void removeFolder(FeedFolder f)
	{
		folderChildren.removeElement(f);
	}
	
	/**
	 * Deletes ALL contents of this folder and removes all
	 * the items from the Feeds it contains
	 * 
	 * @throws FeedBackendException If an error occurs removing the contents
	 */
	public void deleteContents() throws FeedBackendException
	{
		for(int i = 0; i < folderChildren.size(); i++)
		{
			FeedFolder subFolder = (FeedFolder)folderChildren.elementAt(i);
			subFolder.deleteContents();
		}		
		
		Feed feeds[] = getFeeds();
		for(int i = 0; i < feeds.length; i++)
		{
			feeds[i].removeAllItems();					
		}
	}
	
	/**
	 * Gets the number of Feeds this FeedFolder contains
	 * 
	 * @param subFolders Include Feeds in subfolders
	 * @return The number of Feeds
	 */
	public int getFeedCount(boolean subFolders)
	{
		int count = 0;
		if(subFolders)
		{
			for(int i = 0; i < folderChildren.size(); i++)
			{
				FeedFolder subFolder = (FeedFolder)folderChildren.elementAt(i);
				count += subFolder.getFeedCount(subFolders);
			}
		}
		
		//System.out.println(count + feedChildren.size());		
		return count + feedChildren.size();
	}
	
	/**
	 * Updates Feeds in this folder
	 * 
	 * @param subFolders Include feeds in subfolders
	 * @param progress The progress of the update
	 */
	public void updateFeeds(boolean subFolders, UpdateProgress progress)
	{
		if(progress.isAborted())
			return;
		
		if(!progress.isUpdateStarted())	
			progress.updateStart(getFeedCount(subFolders));
		
		if(subFolders)
		{
			for(int i = 0; i < folderChildren.size(); i++)
			{
				FeedFolder subFolder = (FeedFolder)folderChildren.elementAt(i);
				subFolder.updateFeeds(subFolders, progress);
			}	
		}
		
		for(int i = 0; i < feedChildren.size(); i++)
		{
			if(progress.isAborted())
			{
				progress.updateFinish();
				return;
			}		
			
			Feed f = (Feed)feedChildren.elementAt(i);
			progress.feedUpdating(f);
			
			try
			{
				f.update();
			}
			catch(Exception ex)
			{ 
				ex.printStackTrace(); 
			}
			
			int n = progress.feedUpdated();
			if(n >= progress.getUpdateSize())
				progress.updateFinish();		
		}
	}
	
	/**
	 * Finds items in this folder
	 * 
	 * @param search The search criteria
	 * @param subFolders Include feeds in subfolders
	 * @return The found items
	 * @throws FeedBackendException If an error occurs finding items
	 */
	public FeedItem[] findItems(FeedSearch search, boolean subFolders) throws FeedBackendException
	{
		Vector v = recursiveSearch(search, new Vector(), subFolders);
		FeedItem items[] = new FeedItem[v.size()];
		for(int i = 0; i < items.length; i++)
			items[i] = (FeedItem)v.elementAt(i);
		return items;		
	}
	
	private Vector recursiveSearch(FeedSearch search, Vector list, boolean subFolders) throws FeedBackendException
	{
		if(subFolders)
		{
			for(int i = 0; i < folderChildren.size(); i++)
			{
				FeedFolder subFolder = (FeedFolder)folderChildren.elementAt(i);				
				subFolder.recursiveSearch(search, list, subFolders);				
			}
		}
			
		for(int i = 0; i < feedChildren.size(); i++)
		{
			Feed f = (Feed)feedChildren.elementAt(i);
			FeedItem items[] = f.findItems(search);
			for(int j = 0; j < items.length; j++)
			{
				list.add(items[j]);								
			}		
		}
			
		return list;		
	}
	
	/**
	 * Indicates whether this folder contains a specific subfolder
	 * 
	 * @param f The subfolder
	 * @return true if contains the subfolder, false otherwise
	 */
	public boolean containsFolder(FeedFolder f)
	{
		for(int i = 0; i < folderChildren.size(); i++)
		{
			FeedFolder subFolder = (FeedFolder)folderChildren.elementAt(i);
			if(subFolder.toString().equals(f.toString()) || subFolder.containsFolder(f))
				return true;
		}
		
		return false;
	}
	
    /**
     * Gets the name of this folder
     * 
     * @return The name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the parent of this folder
     * 
     * @return The parent FeedFolder, or null if this is the root FeedFolder
     */
    public FeedFolder getParent()
    {
        return parent;
    }

    /**
     * Sets the name of this FeedFolder
     * 
     * @param string The name
     */
    public void setName(String string)
    {
        name = string;
    }

    /**
     * Sets the parent of this FeedFolder
     * 
     * @param folder The parent
     */
    protected void setParent(FeedFolder folder)
    {
        parent = folder;
    }
    
    /**
     * Gets the contents of this folder
     * 
     * @return The contents
     */
    public Object[] getContents()
    {
    	int size = folderChildren.size() + feedChildren.size();
    	Object o[] = new Object[size];
    	for(int i = 0; i < folderChildren.size(); i++)
    		o[i] = folderChildren.elementAt(i);
    	
    	for(int i = folderChildren.size(), j = 0; i < size; i++, j++)
    		o[i] = feedChildren.elementAt(j);
    	return o;
    }
    
    /**
     * 
     *  Overriden to return the name of the folder
     */
    public String toString()
    {
    	return getName();
    }
    
    private class FeedComparator implements Comparator
    {
		public int compare(Object o1, Object o2)
		{
			Feed f1 = (Feed)o1;
			Feed f2 = (Feed)o2;
			
			return f1.getTitle().compareToIgnoreCase(f2.getTitle());
		}
		
		public boolean equals(Object obj)
		{
			return obj.equals(this);
		}
    }
    
	private class FeedFolderComparator implements Comparator
	{
		public int compare(Object o1, Object o2)
		{
			FeedFolder f1 = (FeedFolder)o1;
			FeedFolder f2 = (FeedFolder)o2;
			
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
		
		public boolean equals(Object obj)
		{
			return obj.equals(this);
		}
	}    
}
