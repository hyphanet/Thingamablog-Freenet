/*
 * Created on Jan 31, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.EntryEnumeration;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.WeblogBackend;



/**
 * @author Owner
 *
 */
public class BlogEntryContainer extends ListContainer
{
	/** Constant for front page entries */
	public static final int FRONT_PAGE = 0;
	
	/** Constant for the feed */
	public static final int RSS_PAGE = 3;
	
	/** Constant indicating an archive page */
	public static final int ARC_PAGE = 1;
	
	/** Constant indicating a category page */
	public static final int CAT_PAGE = 2;
	
	public static final int ENTRY_PAGE = 4;
	
	/** The name of the container */
	public static final String NAME = "BlogEntry";	
	private static final String LIMIT = "limit";
	private static final String LIMIT_BY = "limit_by";
	
	private int pageType;
	private String cat = "";
	private ArchiveRange arc;
	private long entryID;
    
    private TemplateTag entryBodyTag = new HyperTextTag("EntryBody");
    private TemplateTag entryTitleTag = new HyperTextTag("EntryTitle");
	private TemplateTag entryIDTag = new TextTag("EntryID");
	private TemplateTag entryAuthorTag = new TextTag("EntryAuthor");
	private TemplateTag entryAuthorEmailTag = new EmailTag("EntryAuthorEmail");
	private TemplateTag entryAuthorUrlTag = new TextTag("EntryAuthorURL");
	private TemplateTag entryArchivePageTag = new TextTag("EntryArchivePage");
	private TemplateTag entryPermalinkTag = new TextTag("EntryPermalink");
	private DateTag entryDateTag = new DateTag("EntryDate");
	private DateTag entryTimeTag = new DateTag("EntryTime");
	private DateTag entryDateTimeTag = new DateTag("EntryDateTime");	
	
	private TBWeblog blog;
	private Vector entryIDs = new Vector();
	
	private Calendar cal = Calendar.getInstance();
	private int currentDay;
	private BlogEntry curEntry = null;
	
	private Vector tags = new Vector();
	private Hashtable tagValues = new Hashtable();
	
	private DayFooterContainer dayFooter;	
	private EntryTitleContainer entryTitle;	
	
	private int entryIndex = 0;
    
	public BlogEntryContainer(TBWeblog blog, int type)
	{
	    super(NAME);
	    initContainer(blog, type);
	}
	
	public BlogEntryContainer(TBWeblog blog, String cat)
	{
	    super(NAME);
	    this.cat = cat;
	    initContainer(blog, CAT_PAGE);
	}
	
	public BlogEntryContainer(TBWeblog blog, ArchiveRange arc)
	{
	    super(NAME);
	    this.arc = arc;
	    initContainer(blog, ARC_PAGE);
	}
	
	public BlogEntryContainer(TBWeblog blog, long id)
	{
	    super(NAME);
	    entryID = id;
	    initContainer(blog, ENTRY_PAGE);
	}
	
	private void initContainer(TBWeblog b, int type)
	{
	    this.blog = b;
	    tags.add(entryBodyTag);
	    tags.add(entryTitleTag);
	    tags.add(entryIDTag);
	    tags.add(entryAuthorTag);
	    tags.add(entryAuthorEmailTag);
	    tags.add(entryAuthorUrlTag);
	    tags.add(entryArchivePageTag);
	    tags.add(entryDateTag);
	    tags.add(entryTimeTag);
	    tags.add(entryDateTimeTag);
	    tags.add(entryPermalinkTag);
	    
	    entryDateTag.setLocale(blog.getLocale());
	    entryTimeTag.setLocale(blog.getLocale());
	    entryDateTimeTag.setLocale(blog.getLocale());	    
	    
	    dayFooter = new DayFooterContainer();
	    entryTitle = new EntryTitleContainer();	   
	    
	    pageType = type;		
		
		defaults.put(LIMIT, "1");
		defaults.put(LIMIT_BY, "10");
	}
	
	/**
	 * Sets the default date format for entry dates. Can be overriden
	 * in the templates
	 * 
	 * @param format The date format string that should work with SimpleDateFormat
	 */
	public void setDefaultDateFormat(String format)
	{
		Hashtable def = entryDateTag.getDefaultAttributes();
		def.put(DateTag.FORMAT, format);
		
		updateDateTimeFormat();
	}
	
	/**
	 * Sets the default time format
	 * 
	 * @param format The default time format
	 */
	public void setDefaultTimeFormat(String format)
	{
		Hashtable def = entryTimeTag.getDefaultAttributes();
		def.put(DateTag.FORMAT, format);
		
		updateDateTimeFormat();
	}
	
	private void updateDateTimeFormat()
	{
	    Hashtable ht = entryDateTimeTag.getDefaultAttributes();
	    String dformat = entryDateTag.getDefaultAttributes().get(DateTag.FORMAT).toString();
	    String tformat = entryTimeTag.getDefaultAttributes().get(DateTag.FORMAT).toString();
	    ht.put(DateTag.FORMAT, dformat + " " + tformat);
	}
	
	/**
	 * Sets the default chronological order of the entries. 
	 * This can be overriden in the template with the attribute "sort_order"
	 * 
	 * @param ascending The chronological order
	 */
	public void setDefaultSortOrder(boolean ascending)
	{
		if(ascending)
			defaults.put(SORT_ORDER, "ascend");
		else
			defaults.put(SORT_ORDER, "descend");
	}
	
	/**
	 * Sets the default limit attribute. The limit attribute
	 * specifies if the number of entries per page should be
	 * limited. This can be overriden in the template. 
	 * A value of "1" limits the entries.
	 * 
	 * @param limit true to limit, false otherwise
	 */
	public void setDefaultIsLimit(boolean limit)
	{
		if(limit)
			defaults.put(LIMIT, "1");
		else
			defaults.put(LIMIT, "0");
	}
	
	/**
	 * Sets the default number to limit entries by
	 * 
	 * @param limit The number to limit entries to
	 */
	public void setDefaultLimitBy(int limit)
	{
		if(limit >= 0)
			defaults.put(LIMIT_BY, limit+"");
	}
	
	
	/**
	 * Indicates whether or not entries on RSS pages should be truncated
	 * Can be overriden in the RSS template
	 *  
	 * @param b true to limit RSS entry bodies, false otherwise
	 */
	public void setLimitEntryBody(boolean b)
	{		
		if(pageType == RSS_PAGE)
		{		
			String lim = "0";
			if(b)lim = "20";
			entryBodyTag.getDefaultAttributes().put(HyperTextTag.WORDS, lim);
		}
	}
	
	/**
	 * Indicates if RSS entry bodies will be limited
	 * 
	 * @return
	 */
	public boolean isLimtEntryBody()
	{
		boolean limit = 
			!entryBodyTag.getDefaultAttributes().get(HyperTextTag.WORDS).equals("0");
		return 	limit && pageType == RSS_PAGE;
	}
    
    private EntryEnumeration getEntriesForPage(boolean sortAsc) throws BackendException
    {
		WeblogBackend backend = blog.getBackend();		
		EntryEnumeration eEnum = null;    		
		if(pageType == ARC_PAGE)
			eEnum = backend.getEntriesBetween(
				blog.getKey(), arc.getStartDate(), arc.getExpirationDate(), sortAsc);
		else if(pageType == CAT_PAGE)
			eEnum = backend.getEntriesFromCategory(blog.getKey(), cat, sortAsc);
		else
			eEnum = backend.getEntries(blog.getKey(), sortAsc);
		
		return eEnum;
    }    
	
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#init(java.util.Hashtable)
     */
    public void initListData(boolean asc, Hashtable attribs)
    {        
        entryIDs.removeAllElements();
        if(pageType == ENTRY_PAGE)
        {
            ListElement ele = new ListElement();
            ele.id = entryID;
            ele.isFooterVisible = true;
            ele.isHeaderVisible = true;
            entryIDs.add(ele);
        }
        else
            initEntryIDs(asc, attribs);
        
        if(!entryIDs.isEmpty())
        {
            ((ListElement)entryIDs.lastElement()).isFooterVisible = true;
        }
    }
    
    private void initEntryIDs(boolean asc, Hashtable attribs)
    {
    	String attr = attribs.get(LIMIT).toString();
    	boolean shouldLimit = attr.equals("1") && pageType != ARC_PAGE;
    	int entryLimit = 10;
    	try{
			entryLimit = Integer.parseInt(attribs.get(LIMIT_BY).toString());  
    	}catch(Exception ex){}
    	
    	
    	currentDay = 0;
    	
    	try
    	{
    		EntryEnumeration  eEnum = null;
    		if(!shouldLimit)
    		{				
				eEnum = getEntriesForPage(asc);
				while(eEnum.hasMoreEntries())
				{						
					BlogEntry be = eEnum.nextEntry();
					if(be.getDate().before(blog.getArchiveBaseDate()))
						continue;
					addListElement(be);
				}
    		}
    		else
    		{				
				eEnum = getEntriesForPage(false);//newest first
				int count = 0;
				if(!asc) //newest first
				{
					while(eEnum.hasMoreEntries())
					{
						BlogEntry be = eEnum.nextEntry();
						if(count >= entryLimit || be.getDate().before(blog.getArchiveBaseDate()))
							break;						
						addListElement(be);
						count++;
					}
				}
				else //oldest first
				{				
					//Vectorize
					Long h[] = new Long[entryLimit];					
					while(eEnum.hasMoreEntries())
					{
						if(count >= entryLimit)
							break;			
						BlogEntry be = eEnum.nextEntry();
						h[count++] = new Long(be.getID());
						//System.out.println(h[count]);		
					}
					for(int i = (h.length - 1); i > -1; --i)					
					{						
						if(h[i] != null)
						{
							BlogEntry be = blog.getEntry(h[i].longValue());							
							if(be.getDate().before(blog.getArchiveBaseDate()))
								continue;
							addListElement(be);
						}
					}
				}
    		}    		
			eEnum.close();	
    	}
    	catch(Exception ex)
    	{
    	    ex.printStackTrace();
    	}
    }
    
    private void addListElement(BlogEntry be)
    {
        cal.setTime(be.getDate());
        ListElement ele = new ListElement();
        ele.id = be.getID();
        if(entryIDs.size() == 0)
        {
            currentDay = cal.get(Calendar.DAY_OF_YEAR);
            ele.isHeaderVisible = true;
        }
        else if(currentDay != cal.get(Calendar.DAY_OF_YEAR))
        {
            ((ListElement)entryIDs.lastElement()).isFooterVisible = true;
            ele.isHeaderVisible = true; 
            currentDay = cal.get(Calendar.DAY_OF_YEAR);
        }
        
        entryIDs.add(ele);        
    }

    public int getListDataSize()
    {
        return entryIDs.size();
    }
    
    public Object getValueForTag(TemplateTag tag, int index)
    {
        //this method is called first, so we can init curEntry here
        ListElement e = (ListElement)entryIDs.elementAt(index);
        try
        {
            curEntry = blog.getEntry(e.id);
            Author auth = curEntry.getAuthor();
            if(auth == null)
                auth = new Author();
            
            if(tag == entryIDTag)
                return Long.toString(curEntry.getID());
            if(tag == entryAuthorTag)
                return auth.getName();
            if(tag == entryAuthorEmailTag)
                return auth.getEmailAddress();
            if(tag == entryAuthorUrlTag)
                return auth.getUrl();
            if(tag == entryDateTag || tag == entryTimeTag || tag == entryDateTimeTag)
                return curEntry.getDate();
            if(tag == entryBodyTag)
                return curEntry.getText();
            if(tag == entryTitleTag && curEntry.getTitle() != null)
                return curEntry.getTitle();
            else if(tag == entryPermalinkTag)
            {
                return blog.getUrlForEntry(curEntry);
            }
            if(tag == entryArchivePageTag)
            {
        		ArchiveRange ar = blog.getArchiveForDate(curEntry.getDate());
        		return blog.getArchiveUrl() + blog.getArchiveFileName(ar);
            }
            
        }
        catch(Exception ex)
        {
            ex.printStackTrace();            
        }
        
        return "";
    }


    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#getTags()
     */
    public List getTags()
    {        
        return tags;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#getContainers()
     */
    public List getContainers()
    {        
        ListElement le = (ListElement)entryIDs.elementAt(currentIndex());        
        
        DayHeaderContainer dayHeader = new DayHeaderContainer();
        dayHeader.setDate(curEntry.getDate());
        dayHeader.setVisible(le.isHeaderVisible);
        
        dayFooter.setVisible(le.isFooterVisible);
        entryTitle.setTitle(curEntry.getTitle());
        
        EntryModifiedDateContainer entryModified = new EntryModifiedDateContainer();
        entryModified.setDate(curEntry.getLastModified());
        
        ArrayList list = new ArrayList(5);
        list.add(dayHeader);
        list.add(dayFooter);
        list.add(entryTitle);
        list.add(entryModified);
        list.add(new CategoryListContainer(blog, curEntry));
        
        return list;
    }
 
    
    public boolean isVisible()
    {
        return !entryIDs.isEmpty();
    }
    
    private class ListElement
    {
        public long id;
        public boolean isHeaderVisible;
        public boolean isFooterVisible;
    }
    
    private class DayFooterContainer extends BasicContainer
    {
        private boolean visible;
        
        public DayFooterContainer()
        {
            super("DayFooter");            
        }
        
        public Object getValueForTag(TemplateTag t)
        {
            return "";
        }
        
        //override from super class
        public boolean isVisible()
        {
            return visible;
        }
        
        public void setVisible(boolean b)
        {
            visible = b;
        }
    }
    
    private class DayHeaderContainer extends BasicContainer
    {
        private boolean visible;
        private Date date;
        private DateTag dayHeaderDate = new DateTag("DayHeaderDate");
        
        public DayHeaderContainer()
        {
            super("DayHeader");
            dayHeaderDate.setLocale(blog.getLocale());
            String format = entryDateTag.getDefaultAttributes().get(DateTag.FORMAT).toString();
            Hashtable def = dayHeaderDate.getDefaultAttributes();    		
    		def.put(DateTag.FORMAT, format);
            registerTag(dayHeaderDate);
        }
        
        public void setVisible(boolean b)
        {
            visible = b;
        }
        
        public void setDate(Date d)
        {
            date = d;
        }
        
        public Object getValueForTag(TemplateTag t)
        {
            if(t == dayHeaderDate)
                return date;
            return "";
        }
        
        //overridden from super class
        public boolean isVisible()
        {
            return visible;
        }
    }
    
    private class EntryTitleContainer extends BasicContainer
    {
        private HyperTextTag titleTag = new HyperTextTag("EntryTitle");
        private String title = "";
        public EntryTitleContainer()
        {
            super("EntryTitle");
            registerTag(titleTag);            
        }
        
        public void setTitle(String t)
        {
            title = t;
        }
        
        public Object getValueForTag(TemplateTag t)
        {
            if(t == titleTag)
                return title;
            
            return "";
        }
        
        public boolean isVisible()
        {
            return title != null && !title.equals("");
        }
    }
    
    private class EntryModifiedDateContainer extends BasicContainer
    {
        private Date modDate = null;
        private DateTag entryModifiedDateTag = new DateTag("EntryModifiedDate");
        
        public EntryModifiedDateContainer()
        {
            super("EntryModifiedDate");
            entryModifiedDateTag.setLocale(blog.getLocale());
            String format = entryDateTimeTag.getDefaultAttributes().get(DateTag.FORMAT).toString();
    		Hashtable def = entryModifiedDateTag.getDefaultAttributes();    		
    		def.put(DateTag.FORMAT, format);
            registerTag(entryModifiedDateTag);
        }
        
        public boolean isVisible()
        {
            return modDate != null;
        }
        
        public void setDate(Date d)
        {
            modDate = d;
        }
        
        public Object getValueForTag(TemplateTag t)
        {
            if(t == entryModifiedDateTag)
                return modDate;
            return "";
        }
    }
}
