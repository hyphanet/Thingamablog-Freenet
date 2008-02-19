/*
 * Created on Mar 13, 2004
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
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.thingamablog.transport.EMailTransport;
import net.sf.thingamablog.transport.FCPTransport;
import net.sf.thingamablog.transport.LocalTransport;
import net.sf.thingamablog.transport.MailTransportProgress;
import net.sf.thingamablog.transport.PublishTransport;

import org.apache.xmlrpc.XmlRpcClient;

/**
 *  
 * An abstract weblog.
 * @author Bob Tantlinger
 */
public abstract class Weblog
{
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.blog");
    
    private Vector weblogListeners = new Vector(2, 2);
	private Vector categoryListeners = new Vector(2, 2);
	private Vector authorListeners = new Vector(2, 2);	
	private Vector pingServices = new Vector(2, 2);
	private PublishTransport transport = new LocalTransport();
    
    private EMailTransport mailTransport = new EMailTransport();
    private boolean isImportFromEmail, isCheckingEmail;
	
	private boolean isPublishing;
	
	/** The title of the weblog */
	protected String title = null;
	
	/** The description of the weblog */
	protected String description;
	
	/** The backend which stores the entries */
	protected  WeblogBackend backend;
	
	/** The backend which stores the authors */
	protected  AuthorStore authorStore;
	
	/** The backend which stores the categories */
	protected  CategoryStore categoryStore;
	
	/** The date that the weblog was last published */
	protected Date lastPublishDate = new Date();
	
	/** The home directory of the web files */
	protected  File webFilesDirectory = new File(System.getProperty("user.home"));
		
	private Date lastEmailCheck = new Date();
    private int outdatedAfterMinutes = 30;
    
    private boolean publishFailed, mailCheckFailed;
    
		
	
	/**
	 * Gets the URL of the front page of the weblog
	 * @return The front page url
	 */
	public abstract String getFrontPageUrl();
	
	/**
	 * Gets the server base path
	 * @return
	 */
	public abstract String getBasePath();
	
	/**
	 * Gets the server archive path
	 * @return
	 */
	public abstract String getArchivePath();
	
	/**
	 * Gets the server media path
	 * @return
	 */
	public abstract String getMediaPath();
	
	/**
	 * Gets the base URL of the weblog
	 * @return
	 */
	public abstract String getBaseUrl();
	
	/**
	 * Gets the base URL of the archives
	 * @return
	 */
	public abstract String getArchiveUrl();
	
	/**
	 * Gets the base URL of the media files
	 * @return
	 */
	public abstract String getMediaUrl();	
	
	/**
	 * Gets the unique key of the weblog. Each weblog must have 
	 * a unique key by which to interact with the backend
	 * @return a unique key
	 */
	public abstract String getKey();
	
	/**
	 * Gets the weblog's templates
	 * @return an array of templates
	 */
	public abstract Template[] getTemplates();
	
	/**
	 * Gets the weblog's archives
	 * @return an array of ArchiveRanges
	 */
	public abstract ArchiveRange[] getArchives();
	
	/**
	 * Gets the current - front page- entries of the weblog 
	 * @return
	 * @throws BackendException If an error occurs while retriving the entries
	 */
	public abstract BlogEntry[] getCurrentEntries() throws BackendException;
	
	/**
	 * Gets the expired entries for the weblog. Expired entries are dated
	 * before the archive base date
	 * @return
	 * @throws BackendException
	 */
	public abstract BlogEntry[] getExpiredEntries() throws BackendException;
	
	/**
	 * Gets the base date of the archives
	 * @return
	 */
	public abstract Date getArchiveBaseDate();
	//TODO hashtable probably shouldn't be returned	
	protected abstract Hashtable weblogFiles(boolean pubAll) throws BackendException, IOException;
	protected abstract void publishComplete(Hashtable ht, boolean failed);
	
	/**
	 * Publishes weblog files, including any web files whose modified date
	 * is after the last publish date
	 * @param progress
	 * @throws BackendException
	 */
	public void publish(PublishProgress progress) throws BackendException
	{
		doWeblogPublish(progress, false);
	}
	
	/**
	 * Publishes everything
	 * @param progress
	 * @throws BackendException
	 */
	public void publishAll(PublishProgress progress) throws BackendException
	{
		doWeblogPublish(progress, true);
	}
    
    public boolean isPublishFailed()
    {
        return publishFailed;
    }
    
    public void setPublishFailed(boolean b)
    {
        publishFailed = b;
    }
    
    public boolean isMailCheckFailed()
    {
        return mailCheckFailed;
    }
    
    public void setMailCheckFailed(boolean b)
    {
        mailCheckFailed = b;
    }
    
   /* private boolean connectTransport(PublishProgress p)
	{
		if(!isPublishing())
		{    		
			isPublishing = true;
		    if(transport.connect())
				return true;
    		
			//p.publishFailed(transport.getFailureReason());
			//return false;   		
		}    	
		
		isPublishing = false;
		p.publishFailed(transport.getFailureReason());
		return false;//publishing
	}*/
	
	private synchronized void doWeblogPublish(PublishProgress progress, boolean pubAll) throws BackendException
	{	
        if(isPublishing())
        	return;
		
        isPublishing = true;
        publishFailed = false;
        
        Hashtable ht = null;
		try
		{		
			ht = weblogFiles(pubAll);
		}
		catch(IOException ioe)
		{
			isPublishing = false;
			publishFailed = true;
			progress.publishFailed("Error building pages: " + ioe.getLocalizedMessage());
			return;
		}
		
		//if we're publishing all the files, set the web files'
		//modified date to the current date so they get published too
		if(pubAll)
			markWebFilesAsUpdated();		
		File webFiles[] = getUpdatedWebFiles();
		long totalBytes = 0;				
		//count the total bytes for this publish
		for(int i = 0; i < webFiles.length; i++)
			totalBytes += webFiles[i].length();
		for(Enumeration e = ht.keys() ; e.hasMoreElements() ;) 
		{
			try
			{
				File f = (File)e.nextElement();
				totalBytes += f.length();
			}
			catch(ClassCastException cce){}
		}
		
		progress.publishStarted(totalBytes);
        
		if(!transport.connect())
		{
			isPublishing = false;
			publishFailed = true;
			publishComplete(ht, true);
			progress.publishFailed(transport.getFailureReason());
			return;
		}
        
        
        
        /*progress.publishStarted(1);//FIXME...
        if(!connectTransport(progress))
        {
            publishFailed = true;
            return;
        }
        
        String failedMsg = "Publish failed";
		Hashtable ht = null;
		try
		{		
			ht = weblogFiles(pubAll);
		}
		catch(IOException ioe)
		{
			progress.publishFailed(failedMsg);			
		}*/
		              
		
		//publish weblog files, if any
		boolean failed = false;
		for(Enumeration e = ht.keys() ; e.hasMoreElements() ;) 
		{
			if(progress.isAborted())
				break;
				
			try
			{
				File f = (File)e.nextElement();
				String pubPath = ht.get(f).toString();
				progress.filePublishStarted(f, pubPath);
				boolean result = transport.publishFile(pubPath, f, progress);
				if(!result)
				{
					//progress.publishFailed(transport.getFailureReason());
					failed = true;
					break;	
				}			
				progress.filePublishCompleted(f, pubPath);				
			}
			catch(ClassCastException cce){}
		}
		
		if(!failed)
		{		
			failed = !publishWebFiles(webFiles, progress);
		}
		
        if(transport.isConnected())
            transport.disconnect();
        
		if(!failed)
		{			
			progress.publishCompleted();//publish completed okay
			lastPublishDate = new Date();			
		}
		else
		{
			if(!progress.isAborted())
			    progress.publishFailed(transport.getFailureReason());
		}		
        
        publishFailed = failed && !progress.isAborted();
		publishComplete(ht, failed);
		isPublishing = false;
		System.out.println("PUBLISH COMPLETE");				
	}
	
        public synchronized void doFlogPublish(PublishProgress progress) throws BackendException
        {            
            if(isPublishing())
                return;
            isPublishing = true;
            publishFailed = false;
        
            Hashtable ht = null;
                try
		{		
			ht = weblogFiles(true);
		}
                catch (IOException ioe)
		{
			isPublishing = false;
			publishFailed = true;
			progress.publishFailed("Error building pages: " + ioe.getLocalizedMessage());
			return;
		}
            markWebFilesAsUpdated();
            File webFiles[] = getUpdatedWebFiles();
            long totalBytes = 0;				
            //count the total bytes for this publish
            for(int i = 0; i < webFiles.length; i++)
            totalBytes += webFiles[i].length();
            for(Enumeration e = ht.keys() ; e.hasMoreElements() ;) 
            {
                    try
                    {
                        File f = (File)e.nextElement();
                        totalBytes += f.length();
                    }   
                    catch(ClassCastException cce){}
            }
		
            progress.publishStarted(totalBytes);
        
            if(!transport.connect())
            {
                isPublishing = false;
                publishFailed = true;
                publishComplete(ht, true);
                progress.publishFailed(transport.getFailureReason());
                return;
            }            
            //publish weblog files, if any
            boolean failed = false;
            //we are publishing a flog with fcp
            FCPTransport fcp = (FCPTransport) transport;
            boolean result = fcp.publishFile(ht,progress);
            if(!result)
				{
					//progress.publishFailed(transport.getFailureReason());
					failed = true;					
				}            
            if(transport.isConnected())
            transport.disconnect();
            
            if(!failed)
            {			
		progress.publishCompleted();//publish completed okay
		lastPublishDate = new Date();			
            }
            else
            {
		if(!progress.isAborted())
		    progress.publishFailed(transport.getFailureReason());
            }		        
            publishFailed = failed && !progress.isAborted();
            publishComplete(ht, failed);
            isPublishing = false;
            System.out.println("PUBLISH COMPLETE");	
        }
        
	private boolean publishWebFiles(File webFiles[], PublishProgress progress)
	{
		String webPaths[] = getWebFilesServerPaths(webFiles);
		if(webPaths.length != webFiles.length)
			return false;
		
		for(int i = 0; i < webFiles.length; i++)
		{
			if(progress.isAborted())
				return false;
			progress.filePublishStarted(webFiles[i], webPaths[i]);
			boolean result = transport.publishFile(webPaths[i], webFiles[i], progress);
			if(!result)
			{				
				return false;	
			}			
			progress.filePublishCompleted(webFiles[i], webPaths[i]);
		}
		
		return true;		
	}
	
	private String[] getWebFilesServerPaths(File webFiles[])
	{
		String paths[] = new String[webFiles.length];
		for(int i = 0; i < webFiles.length; i++)
		{
			String fileDir = webFiles[i].getParent();
			String webDir = webFilesDirectory.getAbsolutePath();
    		
			if(fileDir.equals(webDir))
			{
				paths[i] = getBasePath();
			}
			else if(fileDir.startsWith(webDir))
			{    			
				int index = webDir.length();
				String str = fileDir.substring(index, fileDir.length());
				StringBuffer sb = new StringBuffer(str);
				for(int c = 0; c < sb.length(); c++)    			
					if(sb.charAt(c) == '\\')
						sb.setCharAt(c, '/');
				String bPath = getBasePath();
				if(!bPath.endsWith("/"))
					bPath += "/";
				if(sb.toString().startsWith("/"))
					sb.deleteCharAt(0);
				paths[i] = bPath + sb.toString();    			    			
			}
		}
    	
		return paths;
	}
	
	
	/**
	 * Registers a WeblogListener with this weblog
	 * @param listener
	 */
	public void addWeblogListener(WeblogListener listener)
	{
		weblogListeners.add(listener);		
	}
	
	/**
	 * Removes a WeblogListener from this weblog
	 * @param listener
	 */
	public void removeWeblogListener(WeblogListener listener)
	{
		weblogListeners.remove(listener);
	}
	
	/**
	 * Registers a CategoryListener with this weblog
	 * @param listener
	 */
	public void addCategoryListener(CategoryListener listener)
	{
		categoryListeners.add(listener);
	}
	
	/**
	 * Removes a CategoryListener from this weblog
	 * @param listener
	 */
	public void removeCategoryListener(CategoryListener listener)
	{
		categoryListeners.remove(listener);
	}
	
	/**
	 * Registers an AuthorListener with this weblog
	 * @param listener
	 */
	public void addAuthorListener(AuthorListener listener)
	{
		authorListeners.add(listener);
	}
	
	/**
	 * Removes an AuthorListener from this weblog
	 * @param listener
	 */
	public void removeAuthorListener(AuthorListener listener)
	{
		authorListeners.remove(listener);
	}
	
	protected void fireEntryAdded(BlogEntry be)
	{
		for(int i = 0; i < weblogListeners.size(); i++)
		{
			WeblogListener wl = (WeblogListener)weblogListeners.elementAt(i);
			wl.entryAdded(new WeblogEvent(this, be));		
		}
	}
	
	protected void fireEntryUpdated(BlogEntry be)
	{
		for(int i = 0; i < weblogListeners.size(); i++)
		{
			WeblogListener wl = (WeblogListener)weblogListeners.elementAt(i);
			wl.entryUpdated(new WeblogEvent(this, be));		
		}		
	}
	
	protected void fireEntryRemoved(BlogEntry be)
	{
		for(int i = 0; i < weblogListeners.size(); i++)
		{
			WeblogListener wl = (WeblogListener)weblogListeners.elementAt(i);
			wl.entryRemoved(new WeblogEvent(this, be));		
		}
	}
	
	
	protected void fireAuthorAdded(Author auth)
	{
		for(int i = 0; i < authorListeners.size(); i++)
		{
			AuthorListener wl = (AuthorListener)authorListeners.elementAt(i);
			wl.authorAdded(new AuthorEvent(this, auth));		
		}
	}
	
	protected void fireAuthorUpdated(Author auth)
	{
		for(int i = 0; i < authorListeners.size(); i++)
		{
			AuthorListener wl = (AuthorListener)authorListeners.elementAt(i);
			wl.authorUpdated(new AuthorEvent(this, auth));		
		}		
	}
	
	protected void fireAuthorRemoved(Author auth)
	{
		for(int i = 0; i < authorListeners.size(); i++)
		{
			AuthorListener wl = (AuthorListener)authorListeners.elementAt(i);
			wl.authorRemoved(new AuthorEvent(this, auth));		
		}
	}
	
	
	protected void fireCategoryAdded(String cat)
	{
		for(int i = 0; i < categoryListeners.size(); i++)
		{
			CategoryListener wl = (CategoryListener)categoryListeners.elementAt(i);
			wl.categoryAdded(new CategoryEvent(this, cat));		
		}
	}
	
	protected void fireCategoryRenamed(String cat)
	{
		for(int i = 0; i < categoryListeners.size(); i++)
		{
			CategoryListener wl = (CategoryListener)categoryListeners.elementAt(i);
			wl.categoryRenamed(new CategoryEvent(this, cat));		
		}		
	}
	
	protected void fireCategoryRemoved(String cat)
	{
		for(int i = 0; i < categoryListeners.size(); i++)
		{
			CategoryListener wl = (CategoryListener)categoryListeners.elementAt(i);
			wl.categoryRemoved(new CategoryEvent(this, cat));		
		}
	}
	
	
	/**
	 * Deletes EVERYTHING from the weblog, including entries, categories and authors
	 * @throws BackendException
	 */
	public void deleteAll() throws BackendException
	{
		backend.removeAllWeblogData(getKey());
	}
	
	/**
	 * Adds an entry to the weblog and notifies WeblogListeners
	 * @param be
	 * @throws BackendException
	 */
	public void addEntry(BlogEntry be) throws BackendException
	{
		long id = backend.addEntry(getKey(), be);
		be.setID(id);
		fireEntryAdded(be);		
	}
	
	/**
	 * Updates an entry that already exists in the weblog
	 * and notifies WeblogListeners
	 * @param be
	 * @throws BackendException
	 */
	public void updateEntry(BlogEntry be) throws BackendException
	{
		BlogEntry old = getEntry(be.getID());
	    backend.updateEntry(getKey(), be);		
		fireEntryUpdated(old);
	}
	
	/**
	 * Removes an entry from the weblog and notifies WeblogListeners
	 * @param be
	 * @throws BackendException
	 */
	public void removeEntry(BlogEntry be) throws BackendException
	{
		backend.removeEntry(getKey(), be.getID());
		fireEntryRemoved(be);
	}
	
	/**
	 * Adds a category to the weblog and notifies CategoryListeners
	 * @param cat
	 * @throws BackendException
	 */
	public void addCategory(String cat) throws BackendException
	{
		String cats[] = getCategories();
		for(int i = 0; i < cats.length; i++)
			if(cats[i].equals(cat))
				return;		
		categoryStore.addCategory(getKey(), cat);
		fireCategoryAdded(cat);
	}
	
	/**
	 * Removes a category from the weblog and notifies CategoryListeners
	 * @param cat
	 * @throws BackendException
	 */
	public void removeCategory(String cat) throws BackendException
	{
		categoryStore.removeCategory(getKey(), cat);
		fireCategoryRemoved(cat);
	}
	
	/**
	 * Renames a category and notifies CategoryListeners
	 * @param oldCat
	 * @param newCat
	 * @throws BackendException
	 */
	public void renameCategory(String oldCat, String newCat) throws BackendException
	{
		categoryStore.renameCategory(getKey(), oldCat, newCat);
		fireCategoryRenamed(newCat);
	}
	
	
	/**
	 * Adds an Author to the weblog and notifies AuthorListeners
	 * @param auth
	 * @throws BackendException
	 */
	public void addAuthor(Author auth) throws BackendException
	{
		Author auths[] = getAuthors();
		for(int i = 0; i < auths.length; i++)
			if(auths[i].getString().equals(auth.getString()))
				return;
		authorStore.addAuthor(getKey(), auth);
		fireAuthorAdded(auth);
	}
	
	/**
	 * Removes an Author from the weblog and notifies AuthorListeners
	 * @param auth
	 * @throws BackendException
	 */
	public void removeAuthor(Author auth) throws BackendException
	{
		authorStore.removeAuthor(getKey(), auth);
		fireAuthorRemoved(auth);
	}
	
	/**
	 * Updates an existing Author and notifies AuthorListeners
	 * @param oldAuth
	 * @param newAuth
	 * @throws BackendException
	 */
	public void updateAuthor(Author oldAuth, Author newAuth) throws BackendException
	{
		authorStore.updateAuthor(getKey(), oldAuth, newAuth);
		fireAuthorUpdated(newAuth);
	}
	
	/**
	 * Gets the Authors which belongs to this weblog
	 * @return an array of Authors
	 * @throws BackendException
	 */
	public Author[] getAuthors() throws BackendException
	{
		return authorStore.getAuthors(getKey(), true);
	}
	
	/**
	 * Gets the categories that belong to this weblog
	 * @return an array of categories
	 * @throws BackendException
	 */
	public String[] getCategories() throws BackendException
	{
		return categoryStore.getCategories(getKey(), true);
	}
	
	/**
	 * Gets an entry
	 * @param id The ID of the entry
	 * @return a weblog entry
	 * @throws BackendException
	 */
	public BlogEntry getEntry(long id) throws BackendException
	{
		return backend.getEntry(getKey(), id);
	}
	
	/**
	 * Gets all entries from the weblog, excluding drafts
	 * @return
	 * @throws BackendException
	 */
	public BlogEntry[] getEntries() throws BackendException
	{		
		EntryEnumeration eEnum = backend.getEntries(getKey(), true);
		return toArray(eEnum);				
	}
	
	/**
	 * Finds entries in the weblog
	 * @param search The serach criteria
	 * @return entries that match the search criteria
	 * @throws BackendException
	 */
	public BlogEntry[] findEntries(WeblogSearch search) throws BackendException
	{
		EntryEnumeration eEnum = backend.findEntries(getKey(), search);
		return toArray(eEnum);
	}
	
	/**
	 * Gets the entries that belong to a category
	 * @param cat The category
	 * @return entries that are categorized under the category
	 * @throws BackendException
	 */
	public BlogEntry[] getEntriesFromCategory(String cat) throws BackendException
	{
		EntryEnumeration eEnum = backend.getEntriesFromCategory(getKey(), cat, true);
		return toArray(eEnum);
	}
	
	/**
	 * Gets the entries that fall between an ArchiveRange
	 * @param range The range
	 * @return entries with post dates >= the start date and <= the expiration date 
	 * @throws BackendException
	 */
	public BlogEntry[] getEntriesFromArchive(ArchiveRange range) throws BackendException
	{
		EntryEnumeration eEnum = backend.getEntriesBetween(
			getKey(), range.getStartDate(), range.getExpirationDate(), true);
		return toArray(eEnum);
	}
	
	/**
	 * Gets entries which are drafts
	 * @return
	 * @throws BackendException
	 */
	public BlogEntry[] getDraftEntries() throws BackendException
	{
		EntryEnumeration eEnum = backend.getDraftEntries(getKey(), true);
		return toArray(eEnum);	
	}
	
	private BlogEntry[] toArray(EntryEnumeration eEnum)
	{
		Vector v = new Vector(10, 5);
		while(eEnum.hasMoreEntries())
		{
			BlogEntry be = eEnum.nextEntry();
			v.add(be);
		}
		eEnum.close();
		
		BlogEntry entries[] = new BlogEntry[v.size()];
		for(int i = 0; i < entries.length; i++)
			entries[i] = (BlogEntry)v.elementAt(i);
		
		return entries;				
	}

	 
    /**
     * Gets the weblog description
     * @return
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets the weblog title
     * @return
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the weblog description
     * @param string
     */
    public void setDescription(String string)
    {
        description = string;
    }

    /**
     * Sets the weblog title
     * @param string
     */
    public void setTitle(String string)
    {
        title = string;
    }

    /**
     * Gets the backend of the weblog
     * @return
     */
    public WeblogBackend getBackend()
    {
        return backend;
    }

    /**
     * Sets the backend of the weblog
     * @param backend
     */
    public void setBackend(WeblogBackend backend)
    {
        this.backend = backend;
        
        try
        {        
        	this.backend.initEntryStoreForWeblog(getKey());
        	categoryStore = backend.getCategoryStore();
        	authorStore = backend.getAuthorStore();
        }
        catch(BackendException ex)
        {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Gets the author store
     * @return
     */
    public AuthorStore getAuthorStore()
    {
        return authorStore;
    }

    /**
     * Gets the category store
     * @return
     */
    public CategoryStore getCategoryStore()
    {
        return categoryStore;
    }

    /**
     * Sets the publish transport which publishes any files
     * associated with the weblog
     * @param pt
     */
	public void setPublishTransport(PublishTransport pt)
	{
		transport = pt;	
	}
    
    /**
     * Gets the publish transport
     * @return
     */
	public PublishTransport getPublishTransport()
	{
		return transport;	
	}
	
	
	/**
	 * Indicates if the weblog is publishing
	 * @return true if publishing, false otherwise
	 */
	public boolean isPublishing()
	{
		//return transport.isConnected();
	    return isPublishing;
	}
	
	/*
	 * Publishes a file to the media path
	 * 
	 * @param file The file to publish
	 * @param p progress of the publish
	 * @throws BackendException
	 */
	/*public void publishMedia(File file, PublishProgress p) throws BackendException
	{
		if(!connectTransport(p))
			return;
		
		System.out.println("PUBLISHING...");
		p.publishStarted(file.length());
		transport.connect();
		p.filePublishStarted(file, getMediaPath());		
		if(transport.publishFile(getMediaPath(), file, p))
		{
			p.filePublishCompleted(file, getMediaPath());
			p.publishCompleted();							
		}
		else
		{		
			p.publishFailed(transport.getFailureReason());
		}
		transport.disconnect();
		isPublishing = false;
	}*/
	
	
    /**
     * Overriden to return the title of the weblog
     */
    public String toString()
    {
    	return getTitle();
    }
    
    /**
     * Gets the date that the weblog was last published
     * @return
     */
    public Date getLastPublishDate()
    {
        return lastPublishDate;
    }

    /**
     * Sets the date of the last publish
     * @param date
     */
    public void setLastPublishDate(Date date)
    {
        lastPublishDate = date;
    }

    /**
     * Gets the root directory of the web files
     * @return
     */
    public File getWebFilesDirectory()
    {
        return webFilesDirectory;
    }

    /**
     * Sets the root directory of the web files
     * @param file
     */
    public void setWebFilesDirectory(File file)
    {
        if(file.isDirectory())
        	webFilesDirectory = file;
    }
    
    /**
     * Gets web files that have a modified date after the last publish date
     * @return
     */
	public File[] getUpdatedWebFiles() 
	{
		if(webFilesDirectory != null && webFilesDirectory.exists())
		{		
			Vector v = scanDir(webFilesDirectory, new Vector());
			File f[] = new File[v.size()];
			for(int i = 0; i < f.length; i++)
				f[i] = (File)v.elementAt(i);
			return f;
		}
		
		return null;				
	}
	
	/**
	 * Marks all web files updated
	 */
	public void markWebFilesAsUpdated()
	{
		if(webFilesDirectory != null && webFilesDirectory.exists())
			markWebDirectoryUpdated(webFilesDirectory);
	}
	
	public void markWebDirectoryUpdated(File dir)
	{
		File dirs[] = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isDirectory();
			}	
		});
		
		for(int i = 0; i < dirs.length; i++)
			markWebDirectoryUpdated(dirs[i]);
		
		File files[] = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isFile();
			}	
		});
		
		for(int i = 0; i < files.length; i++)
		{
			files[i].setLastModified(System.currentTimeMillis());
		}
	}
	
	private Vector scanDir(File dir, Vector updatedFiles)
	{		
		File files[] = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				if(!f.isFile())
					return false;
				if(lastPublishDate == null)
					return true;				
				Date d = new Date(f.lastModified());
				if(d.after(lastPublishDate))
					return true;
				
				return false;				
			}
		});		
		for(int i = 0; i < files.length; i++)
			updatedFiles.add(files[i]);
		
		File dirs[] = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isDirectory();
			}	
		});
		for(int i = 0; i < dirs.length; i++)
			scanDir(dirs[i], updatedFiles);
		
		return updatedFiles;
	}
	
	public BlogEntry getEntryAfter(Date d) throws BackendException
	{
	    BlogEntry ent = null;
	    WeblogBackend be = getBackend();
	    if(be == null)
	        return null;
	    EntryEnumeration eEnum = be.getEntriesAfter(getKey(), d, true);
        
	    long chkTime = d.getTime();
	    long arcTime = getArchiveBaseDate().getTime();
	    
        while(eEnum.hasMoreEntries())
        {
            BlogEntry ee  = eEnum.nextEntry();
            long curTime = ee.getDate().getTime();
            if((!ee.isDraft()) && (curTime > chkTime) && (curTime > arcTime))
            {                    
                ent = ee;
                break;
            }
        }        
        eEnum.close();
        return ent;
	}
	
	public BlogEntry getEntryBefore(Date d) throws BackendException
	{
	    long arcTime = getArchiveBaseDate().getTime();
	    long chkTime = d.getTime();	    
	    
	    if(chkTime < arcTime)
	        return null;
	    
	    WeblogBackend be = getBackend();
	    if(be == null)
	        return null;
	    
	    BlogEntry ent = null;
	    EntryEnumeration eEnum = be.getEntriesBefore(getKey(), d, false);        
	    
        while(eEnum.hasMoreEntries())
        {
            BlogEntry ee  = eEnum.nextEntry();
            long curTime = ee.getDate().getTime();
            if((!ee.isDraft()) && (curTime < chkTime) && (curTime > arcTime))
            {                    
                ent = ee;
                break;
            }
        }        
        eEnum.close();
        return ent;
	}
	
	/**
	 * Adds a ping service to the weblog
	 * @param ps
	 */
	public void addPingService(PingService ps)
	{
		pingServices.add(ps);
	}
	
	/**
	 * Removes a ping service from the weblog
	 * @param ps
	 */
	public void removePingService(PingService ps)
	{
		pingServices.remove(ps);
	}
	
	/**
	 * Gets the ping services of the weblog
	 * @return
	 */
	public PingService[] getPingServices()
	{
		PingService ps[] = new PingService[pingServices.size()];
		for(int i = 0; i < ps.length; i++)
			ps[i] = (PingService)pingServices.elementAt(i);
		return ps;	
	}
	
	/**
	 * Sends pings to the services registered with this weblog
	 * @param progress
	 */
	public void sendPings(PingProgress progress)
	{		
		if(progress.isPingSessionAborted())
			return;
		
		//get the enabled services
		Vector services = new Vector(2, 2);
		for(int i = 0; i < pingServices.size(); i++)
		{
			PingService ps = (PingService)pingServices.elementAt(i);
			if(ps.isEnabled())
				services.add(ps);
		}
		
		progress.pingSessionStarted(services.size());
		for(int i = 0; i < services.size(); i++)
		{
			if(progress.isPingSessionAborted())
				return;
				
			PingService ps = (PingService)services.elementAt(i);
			boolean success = false;
			String message = "";
			
			String s[] = ps.getParameters(this);
			Vector params = new Vector();
			for(int j = 0; j < s.length; j++)
				params.add(s[j]);
			
			//System.out.println("\nPinging " + ps.getServiceName() + "...");
			progress.pingStarted(ps);
			try 
			{      	
				XmlRpcClient xmlrpc = new XmlRpcClient(ps.getServiceUrl());                
				message = xmlrpc.execute(ps.getProcedureName(), params).toString();		
				System.out.println(message);
				success = message.indexOf("flerror=true") == -1;				
				//if(message.indexOf("flerror=true") != -1)
				//	System.out.println("Failed to ping " + ps.getServiceName());
				//else
				//	System.out.println("Pinged " + ps.getServiceName() + " successfully");										
					
			}
			catch(Exception e) 
			{
				e.printStackTrace();
				logger.log(Level.WARNING, e.getMessage(), e);
				System.out.println(e.getMessage());
				System.out.println("Failed to ping " + ps.getServiceName());				
			}
			finally
			{
				progress.pingFinished(ps, success, message);
			}			
		}
		
		progress.pingSessionCompleted();		
	}
    
    public boolean isImportFromEmailEnabled()
    {
        return isImportFromEmail && !mailTransport.getAddress().equals("");
    }
    
    public void setImportFromEmailEnabled(boolean b)
    {
        isImportFromEmail = b;
        mailCheckFailed = false;
    }
    
    public boolean isCheckingEmail()
    {
        return isCheckingEmail;
    }
    
    public EMailTransport getMailTransport()
    {
        return mailTransport;
    }
    
    public synchronized boolean importEntriesFromEmail(MailTransportProgress prg) throws BackendException
    {
        
        boolean result = getEntriesFromEmail(prg);
        isCheckingEmail = false;
        lastEmailCheck = new Date();
        return result;
    }
    
    private boolean getEntriesFromEmail(MailTransportProgress prg) throws BackendException
    {
        if(isCheckingEmail() || !isImportFromEmailEnabled())
        {            
            return false;        
        }
        
        isCheckingEmail = true;
               
        Author[] auths = this.getAuthors();
        String[] cats = this.getCategories();
        mailCheckFailed = false;
        
        if(mailTransport.connect())
        {                      
            try
            {
                List entries = mailTransport.getEntries(auths, cats, prg); 
                if(entries.size() > 0)
                {
                    if(mailTransport.disconnect())
                    {
                        if(!prg.isAborted())
                        {
                            for(Iterator it = entries.iterator(); it.hasNext();)
                            {
                                this.addEntry((BlogEntry)it.next());
                            }
                                                    
                            return true;
                        }
                    }
                    else
                    {
                        prg.mailCheckFailed(mailTransport.getFailureReason());
                        mailCheckFailed = true;
                    }
                }
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                mailCheckFailed = true;
                //prg.mailCheckFailed(mailTransport.getFailureReason());
            }   
        }
        else
        {
            prg.mailCheckFailed(mailTransport.getFailureReason());
            mailCheckFailed = true;
        }
        
        return false;
    }
    
    public int getOutdatedAfterMinutes()
    {
        return outdatedAfterMinutes;
    }

    public void setOutdatedAfterMinutes(int outdatedAfterMinutes)
    {
        this.outdatedAfterMinutes = outdatedAfterMinutes;
    }

    /**
     * Indicates if this site is outdated
     * 
     * @return
     */
    public boolean isOutdated()
    {
        if(isImportFromEmailEnabled())
        {
            if(lastEmailCheck == null)
                return true;
            if(outdatedAfterMinutes > 0)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(lastEmailCheck);
                cal.add(Calendar.MINUTE, outdatedAfterMinutes);
                
                Date now = new Date();           
                return cal.getTime().before(now);
            }
        }
        
        return false;
    }
    
    public Date getLastEmailCheck()
    {
        return lastEmailCheck;
    }
    
    public void setLastEmailCheck(Date d)
    {
        lastEmailCheck = d;
    }
}
