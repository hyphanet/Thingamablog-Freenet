/*
 * Created on Apr 8, 2004
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.io.IOUtils;
import net.sf.thingamablog.generator.PageGenerator;


/**
 * Concrete implementation of a Weblog. 
 * This is the standard Thingamablog Weblog class
 * 
 * @author Bob Tantlinger
 */
public class TBWeblog extends Weblog
{    
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.blog");
    private static I18n i18n = I18n.getInstance("net.sf.thingamablog.blog");
	
	//vaid chars for category page files names
	private static final String VALID_CHARS = 
	    "abcdefghijklmnopqrstuvwxyz0123456789_-";
	
	/** Constant indicating a monthly archive policy */
	public static final int ARCHIVE_MONTHLY = 0;
	
	/** Constant indicating a weekly archive policy */
	public static final int ARCHIVE_WEEKLY = 1;
	
	/** Constant indicating a "by n days" archive policy */
	public static final int ARCHIVE_BY_DAY_INTERVAL = 2;
		
	private int archivePolicy = ARCHIVE_MONTHLY;
	private Date archiveBaseDate = new Date(0);
	private int archiveByDayInterval = 5;
	private ArchiveRange archives[] = new ArchiveRange[0];
	
	private Locale locale = Locale.getDefault();
	private PageGenerator generator = new PageGenerator();
	
	private String frontPageFileName = "blog.html";
	private String archiveIndexFileName = "archives.html";
	private String rssFileName = "rss.xml";
	private String archivesExtension = ".html";
	private String categoriesExtension = ".html";
    private String categoriesFeedExtension = ".rss";
	private String entryPageExtension = ".html";
	
	private String baseUrl = "";
	private String archiveUrl = baseUrl;
	private String mediaUrl = baseUrl;
	
	private String basePath = "/";
	private String archivePath = basePath;
	private String mediaPath = basePath;
	
	//private Vector outdatedTopLevelPages = new Vector();
	private Vector outdatedCategoryPages = new Vector();
	private Vector outdatedArchivePages = new Vector();
	private Vector outdatedEntryPages = new Vector();
	
	private boolean generateRssFeed = true;
    private boolean generateCategoryFeeds = false;
	private boolean generateArchiveIndex = true;
	private boolean generateEntryPages = true;
	
	private boolean shouldPublishAll;
	
	private TBTemplate mainTemplate;
	private TBTemplate archiveTemplate;
	private TBTemplate categoryTemplate;
	private TBTemplate arcIndexTemplate;
	private TBTemplate entryTemplate;
	private TBTemplate feedTemplate;
	private Vector templates = new Vector(5, 2);
	
	private String key;
	
	private File homeDir;
	private File tmplDir;
	private File outputDir;
	
	
	/**
	 * Constructs a TBWeblog and creates the required directory structure
	 * @param dir The home directory of the weblog. The directory 
	 * structure will be created in this directory.
	 * @param key The unique key of this weblog
	 */
	public TBWeblog(File dir, String key)
	{
		this.key = key;	
		init(dir);	
	}
	
	/**
	 * Constructs a TBWeblog and creates its directory structure
	 * @param dir The home directory of the weblog. The directory structure
	 * will be created in this directory
	 */
	public TBWeblog(File dir)
	{
		//unique key for this blog, it should never change
		key = System.currentTimeMillis() + "";
		init(dir);
	}
	
	private void init(File dir)
	{		
		//make our dirs if needed
		homeDir = new File(dir, key);
		if(!homeDir.exists() || homeDir.isFile())
			homeDir.mkdirs();
		
		tmplDir = new File(homeDir, "templates");
		if(!tmplDir.exists() || tmplDir.isFile())
			tmplDir.mkdir();
		
		outputDir = new File(homeDir, "temp");
		if(!outputDir.exists() || outputDir.isFile())
			outputDir.mkdir();
		
		webFilesDirectory = new File(homeDir, "web");
		if(!webFilesDirectory.exists() || webFilesDirectory.isFile())
			webFilesDirectory.mkdir();
		
		//set the default arc list format
		generator.setArchiveRangeFormat("MMMM yyyy", false);
		
		//init the templates
		mainTemplate = new TBTemplate(new File(tmplDir, "main.template"), i18n.str("front_page"));
		templates.add(mainTemplate);
		archiveTemplate = new TBTemplate(new File(tmplDir, "archive.template"), i18n.str("archive"));
		templates.add(archiveTemplate);
		categoryTemplate = new TBTemplate(new File(tmplDir, "category.template"), i18n.str("category"));
		templates.add(categoryTemplate);
		entryTemplate = new TBTemplate(new File(tmplDir, "entry.template"), i18n.str("entry_pages"));
		templates.add(entryTemplate);
		arcIndexTemplate = new TBTemplate(new File(tmplDir, "index.template"), i18n.str("archive_index"));
		templates.add(arcIndexTemplate);
		feedTemplate = new TBTemplate(new File(tmplDir, "feed.template"), i18n.str("feed"));
		templates.add(feedTemplate);

		
		addWeblogListener(new TBWeblogListener());
		addCategoryListener(new TBCategoryListener());
		addAuthorListener(new TBAuthorListener());		
	}
	
	/**
	 * Overridden from superclass to remove the TBWeblog dir structure
	 */
	public void deleteAll() throws BackendException
	{
		super.deleteAll();
		
		//be careful about deleting dirs
        IOUtils.deleteRecursively(outputDir);
		IOUtils.deleteRecursively(tmplDir);		
		if(webFilesDirectory.getParent().equals(homeDir.getAbsolutePath()))
			IOUtils.deleteRecursively(webFilesDirectory);
		new File(homeDir, "pack.properties").delete();
		//won't delete if not empty
		homeDir.delete();
	}
    
    /**
     * Gets the home directory of this TBWeblog. The home dir
     * contains the directory structure of the weblog
     * @return The home directory
     */
    public File getHomeDirectory()
    {
    	return homeDir;	
    }
    
    /**
     * Gets the directory which contains the weblog's templates
     * @return The template directory
     */
    public File getTemplateDirectory()
    {
    	return tmplDir;	
    }
    
    /**
     * Overridden from Weblog so that archives are rebuilt for the new backend 
     */
    public void setBackend(WeblogBackend backend)
    {
    	super.setBackend(backend);
    	try
    	{
    		updateArchives();
    	}
    	catch(BackendException be){}	
    }
    
    public String getKey()
    {
    	return key;
    }
    

    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.Weblog#getTemplates()
     */
    public Template[] getTemplates()
    {
		Template t[] = new Template[templates.size()];
		for(int i = 0; i < t.length; i++)
			t[i] = (Template)templates.elementAt(i);			
		return t;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.Weblog#getArchives()
     */
    public ArchiveRange[] getArchives()
    {        
        if(archives == null)
        {
        	try
        	{
        		updateArchives();	
        	}
        	catch(Exception ex)
        	{
        	    logger.log(Level.WARNING, ex.getMessage(), ex);
        	    ex.printStackTrace();
        	}
        }
        
        return archives;
    }
    
    /**
     * Gets the PageGenerator for this TBWeblog
     * @return The page generator
     */
    public PageGenerator getPageGenerator()
    {
    	return generator;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.Weblog#getCurrentEntries()
     */
    public BlogEntry[] getCurrentEntries() throws BackendException
    {
		int limit = generator.getFrontPageLimit();
		EntryEnumeration eEnum = backend.getEntries(getKey(), false);
		Vector v = new Vector(10, 2);
		int count = 0;
		while(eEnum.hasMoreEntries() && count < limit)
		{
			v.add(eEnum.nextEntry());
			count++;	
		}
		eEnum.close();
		BlogEntry be[] = new BlogEntry[v.size()];
		for(int i = 0; i < be.length; i++)
			be[i] = (BlogEntry)v.elementAt(i);
		return be;
    }
    
	public BlogEntry[] getExpiredEntries() throws BackendException
	{
		EntryEnumeration eEnum = backend.getEntriesBefore(getKey(), archiveBaseDate, false);
		Vector v = new Vector(10, 2);		
		while(eEnum.hasMoreEntries())
		{
			v.add(eEnum.nextEntry());				
		}
		eEnum.close();
		BlogEntry be[] = new BlogEntry[v.size()];
		for(int i = 0; i < be.length; i++)
			be[i] = (BlogEntry)v.elementAt(i);
		return be;
	}
    
    
    private class TBWeblogListener implements WeblogListener
    {
		public void entryAdded(WeblogEvent e)
		{
			System.out.println("Entry added");					
			BlogEntry be = e.getEntry();
			Date d = be.getDate();
			//is it a valid date?
			Date now = new Date();
			if(!be.isDraft() && d.after(archiveBaseDate) && 
			(d.before(now) || d.compareTo(now) == 0))
			{				
				//updateArchives();
				ArchiveRange ar = getArchiveForDate(d);
				if(ar != null)//has archive for this range
				{
					//System.out.println("State changed");
					addOutdatedArchive(ar);					
					String cats[] = be.getCategories();
					for(int i = 0; i < cats.length; i++)
						addOutdatedCategory(cats[i]);
					
					addOutdatedEntryID(new Long(be.getID()));
					outdatePrevNextEntries(be);
				}
				else
				{
					try
					{
						updateArchives();
					}
					catch(Exception ex)
					{
					    logger.log(Level.WARNING, ex.getMessage(), ex);
					    ex.printStackTrace();	
					}
					shouldPublishAll = true;
				}									
			}
		}
		
		public void entryUpdated(WeblogEvent e)
		{
			System.out.println("Entry updated");
			try
			{				
			    BlogEntry oldEntry = e.getEntry();
				BlogEntry newEntry = getEntry(oldEntry.getID());
				if(oldEntry.isDraft() && newEntry.isDraft())
				    return;
				
				addOutdatedEntryID(new Long(newEntry.getID()));
				
				String cats[] = oldEntry.getCategories();
				for(int i = 0; i < cats.length; i++)
				    addOutdatedCategory(cats[i]);
				cats = newEntry.getCategories();
				for(int i = 0; i < cats.length; i++)
				    addOutdatedCategory(cats[i]);				
								    
			    updateArchives();
			    ArchiveRange oar = getArchiveForDate(oldEntry.getDate());
			    ArchiveRange nar = getArchiveForDate(newEntry.getDate());
			    if(oar == null || nar == null)
			        shouldPublishAll = true;
			    else
			    {
			        addOutdatedArchive(oar);
			        addOutdatedArchive(nar);
			        outdatePrevNextEntries(newEntry);
			    }
			}
			catch(Exception ex)
			{
			    logger.log(Level.WARNING, ex.getMessage(), ex);
			    ex.printStackTrace();	
			}								
		}
		
		public void entryRemoved(WeblogEvent e)
		{
			System.out.println("Entry removed");		    
			BlogEntry entry = e.getEntry();
			outdatedEntryPages.remove(new Long(entry.getID()));
			if(entry.isDraft() || entry.getDate().before(getArchiveBaseDate()))
			    return;			
			
			String cats[] = entry.getCategories();
			for(int i = 0; i < cats.length; i++)
			    addOutdatedCategory(cats[i]);
			
			try
			{				    
			    updateArchives();
			    ArchiveRange ar = getArchiveForDate(entry.getDate());
			    if(ar == null)
			        shouldPublishAll = true;
			    else
			    {
			        addOutdatedArchive(ar);
			        outdatePrevNextEntries(entry);
			    }
			    
			}
			catch(Exception ex)
			{
			    logger.log(Level.WARNING, ex.getMessage(), ex);
			    ex.printStackTrace();	
			}						
		}
		
		private void outdatePrevNextEntries(BlogEntry be)
		{		    
		    try
		    {
		        BlogEntry e = getEntryAfter(be.getDate());		        
		        if(e != null)
		        {
		            addOutdatedEntryID(new Long(e.getID()));		            
		        }
		        		        
		        e = getEntryBefore(be.getDate());
		        if(e != null)
		        {
		            addOutdatedEntryID(new Long(e.getID()));		            
		        }		        
		    }
		    catch(Exception ex)
		    {
		        logger.log(Level.WARNING, ex.getMessage(), ex);
		        ex.printStackTrace();
		    }
		}
    }
    
	private class TBCategoryListener implements CategoryListener
	{
		public void categoryAdded(CategoryEvent e)
		{
			catsChanged();
		}
		
		public void categoryRenamed(CategoryEvent e)
		{
			catsChanged();
		}
		
		public void categoryRemoved(CategoryEvent e)
		{
			catsChanged();
		}
		
		private void catsChanged()
		{
			try
			{
				updateArchives();
			}
			catch(Exception ex)
			{
			    logger.log(Level.WARNING, ex.getMessage(), ex);
			    ex.printStackTrace();	
			}
			shouldPublishAll = true;	
		}
	}
	
	private class TBAuthorListener implements AuthorListener
	{
		public void authorAdded(AuthorEvent e)
		{
			//System.out.println("Author added " + e.getAuthor().getName());
			//adding an author doesn't effect the state of the pages
		}
		
		public void authorUpdated(AuthorEvent e)
		{
			//System.out.println("Author updated " + e.getAuthor().getName());
			shouldPublishAll = true;
		}
		
		public void authorRemoved(AuthorEvent e)
		{
			//System.out.println("Author removed " + e.getAuthor().getName());
			shouldPublishAll = true;
		}		
	}


    public Date getArchiveBaseDate()
    {
        return archiveBaseDate;
    }

    /**
     * Gets the n days that separate "by day" archives
     * @return n days
     */
    public int getArchiveByDayInterval()
    {
        return archiveByDayInterval;
    }

    /**
     * Gets the archive policy of this weblog
     * @return one of ARCHIVE_MONTHLY, ARCHIVE_WEEKLY, or ARCHIVE_BY_DAY_INTERVAL
     */
    public int getArchivePolicy()
    {
        return archivePolicy;
    }

    /**
     * Sets the base date for the archives. The weblog will not 
     * generate entries that have a post date before this date
     * @param date The base date
     */
    public void setArchiveBaseDate(Date d)
    {       
		//set to 12:00 AM 
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);       
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 
			cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);       
		 archiveBaseDate = cal.getTime();        
    }

    /**
     * Sets the number of days to serparate "by n days" archives
     * @param i
     */
    public void setArchiveByDayInterval(int i)
    {
        archiveByDayInterval = i;
    }

    /**
     * Sets the archive policy of the weblog
     * @param one of the constants:
     * ARCHIVE_MONTHLY, ARCHIVE_WEEKLY, or ARCHIVE_BY_DAY_INTERVAL
     */
    public void setArchivePolicy(int i)
    {
        archivePolicy = i;
    }
    
    public String getUrlForEntry(BlogEntry b)
    {
       return getArchiveUrl() + getEntryPathPart(b) + getEntryFileName(b);
    }
    
    public String getUrlForCategory(String cat)
    {
        return getArchiveUrl() + getCategoryFileName(cat);
    }
    
    public String getUrlForArchive(ArchiveRange arc)
    {
        return getArchiveUrl() + getArchiveFileName(arc);
    }
    
    public String getUrlForWebFile(File f) throws IllegalArgumentException
    {
        if(!f.getAbsolutePath().startsWith(getWebFilesDirectory().getAbsolutePath()))
            throw new IllegalArgumentException("File " + f + " does not belong to blog [" + getTitle() + "]");
        
        try
        {            
            String url = f.toURL().toExternalForm();
            String wurl = getWebFilesDirectory().toURL().toExternalForm();
            int s = url.indexOf(wurl) + wurl.length();
            return getBaseUrl() +  url.substring(s, url.length());
        }
        catch(Exception ex)
        {            
        }
        
        return null;
    }
    
    private String getPathForEntry(BlogEntry b)
    {
        String arcPath = getArchivePath();
        if(!arcPath.endsWith("/"))
            arcPath += "/";
        return arcPath + getEntryPathPart(b); 
    }
    
    private String getEntryPathPart(BlogEntry b)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(b.getDate());
        String yr = cal.get(Calendar.YEAR) + "";
        String mo = "0";
        
        int month = cal.get(Calendar.MONTH) + 1;
        if(month < 10)            
            mo += month + "";
        else
            mo = month + "";
        
        return yr + '/' + mo + '/';            
    }
    
    private String getEntryFileName(BlogEntry e)
    {
        return "entry_" + e.getID() + getEntryPageExtension();
    }
    
    /**
     * Gets an archive for a date
     * @param d the date
     * @return an archive which the date falls between
     */
    public ArchiveRange getArchiveForDate(Date d)
    {
    	if(archives != null)
    	{   	
    		for(int i = 0; i < archives.length; i++)
    		{
    			Date s = archives[i].getStartDate();
    			Date e = archives[i].getExpirationDate();
    		    if(s.before(d) && e.after(d))
    				return archives[i];
    		    if(s.equals(d) || e.equals(d))
    		        return archives[i];
    		}
    	}  		
    	System.out.println(d);
    	return null;
    }
    
    
	/**
	 * Gets the file name of the archive index page
	 * @return
	 */
	public String getArchiveIndexFileName()
	{
		return archiveIndexFileName;
	}

	/**
	 * Gets the server archive path
	 * @return
	 */
	public String getArchivePath()
	{
		return archivePath;
	}

	/**
	 * Gets the base URL of the archive pages
	 * @return
	 */
	public String getArchiveUrl()
	{
		return archiveUrl;
	}

	/**
	 * Gets the base server path
	 * @return
	 */
	public String getBasePath()
	{
		return basePath;
	}

	/**
	 * Gets the base URL of the weblog
	 * @return
	 */
	public String getBaseUrl()
	{
		return baseUrl;
	}

	/**
	 * Gets the file name of the front page
	 * @return
	 */
	public String getFrontPageFileName()
	{
		return frontPageFileName;
	}

	/**
	 * Gets the server path where media files are published to
	 * @return
	 */
	public String getMediaPath()
	{
		return mediaPath;
	}

	/**
	 * Gets the media URL
	 * @return
	 */
	public String getMediaUrl()
	{
		return mediaUrl;
	}

	/**
	 * Gets the file name of the weblog's front page feed
	 * @return
	 */
	public String getRssFileName()
	{
		return rssFileName;
	}

	/**
	 * @param string
	 */
	public void setArchiveIndexFileName(String string)
	{
		if(string != null && !string.equals(""))
			archiveIndexFileName = string;
	}

	/**
	 * @param string
	 */
	public void setFrontPageFileName(String string)
	{
		if(string != null && !string.equals(""))
			frontPageFileName = string;
	}
    
    /**
     * Sets the URLs for the weblog
     * @param path The base server path
     * @param base The base URL
     * @param arcs The archive's base URL
     * @param media the media files URL
     */
	public void setBlogUrls(String path, String base, String arcs, String media)
	{
		basePath = appendSlash(path);//append slash??
		baseUrl = appendSlash(base);
		if(arcs.startsWith(baseUrl) && arcs.length() > baseUrl.length())
		{
			archiveUrl = appendSlash(arcs);
			String pfix = archiveUrl.substring(baseUrl.length(), archiveUrl.length());    		
			archivePath = basePath + pfix;
		}
		else
		{
			archiveUrl = baseUrl;
			archivePath = basePath;	
		}
		
		if(media.startsWith(baseUrl) && media.length() > baseUrl.length())
		{
			mediaUrl = appendSlash(media);
			String pfix = mediaUrl.substring(baseUrl.length(), mediaUrl.length());    		
			mediaPath = basePath + pfix;
		}
		else
		{
			mediaUrl = baseUrl;
			mediaPath = basePath;	
		}	
	}

	/**
	 * @param string
	 */
	public void setRssFileName(String string)
	{
		if(string != null && !string.equals(""))
			rssFileName = string;
	}

    public String getFrontPageUrl()
    {
    	String url = getBaseUrl();
    	if(!url.endsWith("/"))
    		url += "/";
    	return url + getFrontPageFileName();
    }
    
	protected Hashtable weblogFiles(boolean pubAll) throws BackendException, IOException
	{
		Hashtable ht = new Hashtable();		
		if(pubAll)
			shouldPublishAll = true;
		
		//determine which archive pages to publish
		if(shouldPublishAll ||
		archiveTemplate.getLastModifiedDate().after(lastPublishDate))
		{
			outdatedArchivePages.removeAllElements();			
			updateArchives();
			for(int i = 0; i < archives.length; i++)
				addOutdatedArchive(archives[i]);  		
		}
		
		//determine which category pages to publish
		if(shouldPublishAll || 
		categoryTemplate.getLastModifiedDate().after(lastPublishDate) ||
        (isGenerateCategoryFeeds() && 
            feedTemplate.getLastModifiedDate().after(lastPublishDate)))
		{
			outdatedCategoryPages.removeAllElements();
			String cats[] = getCategories();
			for(int i = 0; i < cats.length; i++)
			{				
				addOutdatedCategory(cats[i]);  
			}			
		}		
		
		if(generateEntryPages && (shouldPublishAll || 
		entryTemplate.getLastModifiedDate().after(lastPublishDate)))
		{
		    outdatedEntryPages.removeAllElements();
		    try
		    {
		        EntryEnumeration eEnum = 
		            backend.getEntriesAfter(getKey(), getArchiveBaseDate(), true);
		        while(eEnum.hasMoreEntries())
		        {
		            BlogEntry ent = eEnum.nextEntry();
		            if(!ent.isDraft())
		            {
		                addOutdatedEntryID(new Long(ent.getID()));		                
		            }
		        }
		        eEnum.close();
		    }
		    catch(Exception ex)
		    {
		        logger.log(Level.WARNING, ex.getMessage(), ex);
		        ex.printStackTrace();
		    }
		}
		
		//generate top level pages
		//top level pages get published every time
		genTopLevelPages(ht);
		
		//generate archives
		String arcTmpl = archiveTemplate.load();
		for(int i = 0; i < outdatedArchivePages.size(); i++)
		{				
			ArchiveRange arc = (ArchiveRange)outdatedArchivePages.elementAt(i);				
			File f = new File(outputDir, getArchiveFileName(arc));
			OutputStream out = new FileOutputStream(f);
			generator.generatePage(this, arc, out, arcTmpl);
			out.close();				
			ht.put(f, getArchivePath());
		}
    		
		//generate categories
		String catTmpl = categoryTemplate.load();
        String catFeedTmpl = null;
        if(isGenerateCategoryFeeds())
            catFeedTmpl = feedTemplate.load();
		for(int i = 0; i < outdatedCategoryPages.size(); i++)
		{			
			String cat = outdatedCategoryPages.elementAt(i).toString();
            //generate the cat page
			File f = new File(outputDir, getCategoryFileName(cat));
			OutputStream out = new FileOutputStream(f);			
			generator.generatePage(this, cat, out, catTmpl);
			out.close();				
			ht.put(f, getArchivePath());
            
            //generate cat feed
            if(catFeedTmpl != null)
            {
                f = new File(outputDir, getCategoryFeedFileName(cat));
                out = new FileOutputStream(f);
                generator.generatePage(this, cat, out, catFeedTmpl);
                out.close();                
                ht.put(f, getArchivePath());
            }
		}
		
		//generate entry pages
		if(generateEntryPages)
		{
		    String entryTmpl = entryTemplate.load();
		    for(int i = 0; i < outdatedEntryPages.size(); i++)
		    {			
		        Long id = (Long)outdatedEntryPages.elementAt(i);				

		        try
		        {
		            BlogEntry be = getEntry(id.longValue());
		            File f = new File(outputDir, getEntryFileName(be));
		            OutputStream out = new FileOutputStream(f);
		            generator.generatePage(this, be.getID(), out, entryTmpl);
		            out.close();
		            ht.put(f, getPathForEntry(be));
		        }		    
		        catch(Exception ex)
		        {
		            logger.log(Level.WARNING, ex.getMessage(), ex);
		            ex.printStackTrace();
		        }						
		    }
		}
		return ht;		
	}
	
	private void genTopLevelPages(Hashtable ht) throws IOException
	{
		//generate top level pages
		File f = new File(outputDir, frontPageFileName);
		OutputStream out = new FileOutputStream(f);
		generator.generatePage(this, PageGenerator.FRONT_PAGE, out, mainTemplate.load());
		out.close();
		ht.put(f, getBasePath());		
			
		if(generateArchiveIndex)
		{			
			f = new File(outputDir, archiveIndexFileName);
			out = new FileOutputStream(f);
			generator.generatePage(this, 
				PageGenerator.INDEX_PAGE, out, arcIndexTemplate.load());
			out.close();
			ht.put(f, getBasePath());			
		}
			
		if(generateRssFeed)
		{			
			f = new File(outputDir, rssFileName);
			out = new FileOutputStream(f);
			generator.generatePage(this, 
				PageGenerator.RSS_PAGE, out, feedTemplate.load());
			out.close();
			ht.put(f, getBasePath());			
		}		
	}
	
	protected void publishComplete(Hashtable ht, boolean failed)
	{				
		//delete the weblog files		
		for(Enumeration e = ht.keys() ; e.hasMoreElements() ;) 
		{
			try
			{
				File f = (File)e.nextElement();
				f.delete();				
			}
			catch(ClassCastException cce){}
		}
				
		if(!failed)
		{		
			System.out.println("clearing");
			shouldPublishAll = false;//no pages need updated		
			outdatedArchivePages.removeAllElements();
			outdatedCategoryPages.removeAllElements();
			outdatedEntryPages.removeAllElements();
		}				
	}
    


    
	private String appendSlash(String u)
	{
		if(!u.endsWith("/") && !u.equals( "" )) //Bug fix by John Montgomery
			u += "/";
    	
		return u;     	
	}
    
    /**
     * Adds an ArchiveRange to the list of archives to publish
     * @param ar - an outdated ArchiveRange
     */
    public void addOutdatedArchive(ArchiveRange ar)
    {
    	if(!outdatedArchivePages.contains(ar))
    		outdatedArchivePages.add(ar);
    }
    
    /**
     * 
     * @param ar - an outdated ArchiveRange
     */
    public void addOutdatedEntryID(Long id)
    {
    	if(!outdatedEntryPages.contains(id))
    		outdatedEntryPages.add(id);
    }
    
    public long[] getOutdatedEntryIDs()
    {
        long ids[] = new long[outdatedEntryPages.size()];
        for(int i = 0; i < ids.length; i++)
            ids[i] = ((Long)outdatedEntryPages.elementAt(i)).longValue();
        return ids;
    }
    
    /**
     * Get the list of outdated ArchivesRanges
     * @return an array of ArchiveRanges
     */
    public ArchiveRange[] getOutdatedArchives()
    {
    	ArchiveRange ar[] = new ArchiveRange[outdatedArchivePages.size()];
    	for(int i = 0; i < ar.length; i++)
    		ar[i] = (ArchiveRange)outdatedArchivePages.elementAt(i);
    	return ar;
    }
    
    /**
     * Adds a category to the list of categories to publish
     * @param cat - a category
     */
    public void addOutdatedCategory(String cat)
    {
    	if(!outdatedCategoryPages.contains(cat))
    		outdatedCategoryPages.add(cat);
    }
    
    /**
     * Get the list of categories to publish
     * @return a string array
     */
	public String[] getOutdatedCategories()
	{
		String s[] = new String[outdatedCategoryPages.size()];
		for(int i = 0; i < s.length; i++)
			s[i] = outdatedCategoryPages.elementAt(i).toString();
		return s;
	}
	
	/**
	 * When set to true, the entries blog is published on when one of the publish
	 * methods are called. When set to false, only outdated archives/categories
	 * are published
	 * @param b
	 */
	public void setPublishAll(boolean b)
	{
		shouldPublishAll = b;
	}
	
	/**
	 * Indicates whether the weblog should publish everything 
	 * on the next publish session
	 * @return - true if the blog is set to publish all, false otherwise
	 */
	public boolean isPublishAll()
	{
		return shouldPublishAll;
	}
	
    /**
     * Recalculates the weblog's archives
     * @throws BackendException If an error occurs while recalculating the archives
     */
    public void updateArchives() throws BackendException
    {    	 
    	EntryEnumeration eEnum = backend.getEntriesAfter(getKey(), archiveBaseDate, true);
		ArchiveRange cur = null;
		Vector arcs = new Vector(20, 20);
		while(eEnum.hasMoreEntries())
		{			
			BlogEntry be = eEnum.nextEntry();
			Date ts = be.getDate();
			//System.out.println(ts);
			//current record greater than current page date
			if(cur == null || cur.getExpirationDate().compareTo(ts) < 0)//before
			{
				Date d1 = new Date(ts.getTime());				
				Calendar cal = Calendar.getInstance();
				cal.setTime(ts);
				cal.add(Calendar.DATE, archiveByDayInterval - 1);
				Date d2 = cal.getTime();
           		
				if(archivePolicy == ARCHIVE_MONTHLY)
				{
					cal = Calendar.getInstance();
					cal.setTime(d1);
					cal.set(Calendar.DAY_OF_MONTH, 1);
					d1 = cal.getTime();
           		
					cal.add(Calendar.MONTH, 1);
					cal.add(Calendar.DATE, -1);
					d2 = cal.getTime();           	
				}
				else if(archivePolicy == ARCHIVE_WEEKLY)
				{
					cal = Calendar.getInstance();
					cal.setTime(d1);
					//cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
					//bug fix - in some countries Monday is the 1st day of the week
					cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
					d1 = cal.getTime();					
					//cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
					cal.add(Calendar.DAY_OF_MONTH, 6);
					d2 =  cal.getTime();      			
				}
           		
				cur = new ArchiveRange(d1, d2);               
				arcs.add(cur);
			}			
		}
		eEnum.close();
		archives = new ArchiveRange[arcs.size()];
		for(int i = 0; i < archives.length; i++)
		{		
			archives[i] = (ArchiveRange)arcs.elementAt(i);
			//set the formatting
			archives[i].setFormatter(
				new java.text.SimpleDateFormat(generator.getArchiveRangeFormat()),
				generator.isSpanArcRange());
		}   	   
    }   
    
    /**
     * Get the file name for a category
     * @param cat
     * @return
     */
	public String getCategoryFileName(String cat)
	{	    
	    return getCategoryFileName(cat, categoriesExtension);	    
	}
    
    public String getCategoryFeedFileName(String cat)
    {
        return getCategoryFileName(cat, categoriesFeedExtension);
    }
    
    public String getCategoryFileName(String cat, String ext)
    {
        char in[]=cat.toLowerCase().toCharArray();
        char checkagainst[]=VALID_CHARS.toCharArray();
        StringBuffer fileName = new StringBuffer();
        
        for(int x = 0; x < in.length; x++)    // check every inchar
        {
            for(int y = 0; y < checkagainst.length; y++)  // aginst every validchar
            {
                if(in[x] == checkagainst[y]) 
                    fileName.append(in[x]);
            }
        } 
        
        //no valid chars were found or the category name is too long
        if(fileName.length() == 0 || fileName.length() > 20)
            fileName = new StringBuffer(Integer.toString(Math.abs(cat.hashCode())));        
        
        fileName.append(ext);
        return "cat_" + fileName.toString();
    }
	
	/**
	 * Get the file name for a category
	 * @param arc
	 * @return
	 */
	public String getArchiveFileName(ArchiveRange arc)
	{
		java.text.SimpleDateFormat fileNameFormat = 
				new java.text.SimpleDateFormat("MM-dd-yyyy");
		
		String d1 = fileNameFormat.format(arc.getStartDate());
		String d2 = fileNameFormat.format(arc.getExpirationDate());
		return d1 + "_" + d2 + archivesExtension;
	}	
	

    /**
     * Indicates whether the weblog should generate the archive index page
     * @return
     */
    public boolean isGenerateArchiveIndex()
    {
        return generateArchiveIndex;
    }

    /**
     * Indicates whether the weblog should generate the syndication feed
     * @return
     */
    public boolean isGenerateRssFeed()
    {
        return generateRssFeed;
    }
    
    /**
     * Indicates whether the weblog should generate entry pages
     * @return
     */
    public boolean isGenerateEntryPages()
    {
        return generateEntryPages;
    }

    /**
     * @param b
     */
    public void setGenerateArchiveIndex(boolean b)
    {
        generateArchiveIndex = b;
        if(b)
        {
        	if(!templates.contains(arcIndexTemplate))
        		templates.add(arcIndexTemplate);
        }
        else
        	templates.remove(arcIndexTemplate);
    }

    /**
     * @param b
     */
    public void setGenerateRssFeed(boolean b)
    {
        generateRssFeed = b;
		if(b)
		{
			if(!templates.contains(feedTemplate))
				templates.add(feedTemplate);
		}
		else
        {
			if(!isGenerateCategoryFeeds())
			    templates.remove(feedTemplate);
        }
    }
    
    /**
     * @param generateCategoryFeeds The generateCategoryFeeds to set.
     */
    public void setGenerateCategoryFeeds(boolean b)
    {
        generateCategoryFeeds = b;
        if(b)
        {
            if(!templates.contains(feedTemplate))
                templates.add(feedTemplate);
        }
        else
        {
            if(!isGenerateRssFeed())
                templates.remove(feedTemplate);
        }
    }
    
    /**
     * @return Returns the generateCategoryFeeds.
     */
    public boolean isGenerateCategoryFeeds()
    {
        return generateCategoryFeeds;
    }   

    
    public void setGenerateEntryPages(boolean b)
    {
        generateEntryPages = b;
		if(b)
		{
			if(!templates.contains(entryTemplate))
				templates.add(entryTemplate);
		}
		else
			templates.remove(entryTemplate);
    }

    /**
     * Gets the locale of the weblog. 
     * The locale specifies how dates should be formatted
     * @return
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Sets the locale of the weblog
     * @param locale
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Gets the extension of archive pages
     * @return
     */
    public String getArchivesExtension()
    {
        return archivesExtension;
    }

    /**
     * Gets the extension of category pages
     * @return
     */
    public String getCategoriesExtension()
    {
        return categoriesExtension;
    }

    /**
     * Sets the extension of archive pages
     * @param string
     */
    public void setArchivesExtension(String string)
    {
        if(!string.startsWith("."))
        	string = "." + string;
        archivesExtension = string;
    }

    /**
     * Sets the extension of category pages
     * @param string
     */
    public void setCategoriesExtension(String string)
    {
		if(!string.startsWith("."))
			string = "." + string;
        categoriesExtension = string;
    }
    
    public void setEntryPageExtension(String string)
    {
		if(!string.startsWith("."))
			string = "." + string;
        entryPageExtension = string;
    }
    
    public String getEntryPageExtension()
    {
        return entryPageExtension;
    }

    
    /**
     * @return Returns the categoriesFeedExtension.
     */
    public String getCategoriesFeedExtension()
    {
        return categoriesFeedExtension;
    }

    
    /**
     * @param categoriesFeedExtension The categoriesFeedExtension to set.
     */
    public void setCategoriesFeedExtension(String ext)
    {
        if(!ext.startsWith("."))
            ext = "." + ext;
        categoriesFeedExtension = ext;
    }
    
    public TemplatePack getTemplatePack() throws IOException
    {
        try
        {
            DiskTemplatePack pack = new DiskTemplatePack(getHomeDirectory());
            return pack;
        }
        catch(IllegalArgumentException ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
}
