/*
 * Created on Jan 31, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.TBWeblog;

/**
 * @author Bob Tantlinger
 *
 */
public class PageGenerator
{
	/** Constant for the RSS feed */
	public static final int RSS_PAGE   = -1;
	/** Constant for the front page */
	public static final int FRONT_PAGE = -2;
	/** Constant for the archive index page */
	public static final int INDEX_PAGE = -3;
	
	private Vector customTags = new Vector();	
	private String charset = "UTF-8";//default charset
	
	//default container attributes
	private String archiveRangeFormat = "MMMM, dd yyyy";
	private boolean spanArcRange = true;
	
	private String dateFormat = archiveRangeFormat;
	private String timeFormat = "h:mm a";
	
	private int frontPageLimit = 10;
	private boolean isLimitFrontPage = true;
	
	private int categoryPageLimit = 20;
	private boolean isLimitCategoryPage = true;
	
	private boolean isFrontPageAscending;
	private boolean isCategoryPageAscending;
	private boolean isArchivePageAscending;
	
	private boolean isLimitRssEntry = true;
	
	private TemplateProcessor pageBuilder = new TemplateProcessor();
	
	
	private void writePage(BlogPageContainer bpc, String template, OutputStream out)
	throws IOException
	{
		writePage(bpc, null, template, out);
	}
	
	private void writePage(BlogPageContainer bpc, BlogEntryContainer bec, String template, OutputStream out)
	throws IOException
	{
		for(int i = 0; i < customTags.size(); i++)		
			bpc.addCustomTag((CustomTag)customTags.elementAt(i));		
	    
	    OutputStreamWriter writer = new OutputStreamWriter(out, charset);
		PrintWriter pw = new PrintWriter(writer);
		
		String rootName = BlogPageContainer.NAME;
		template = "<" + rootName + ">" + template + "</" + rootName + ">";		
	    String text = pageBuilder.processTemplate(template, bpc);
	    
	    /*
	     * Writing all the entries for a page to a string
	     * would be memeory-intensive if there are a lot of entries.
	     * Thus, it seems safer and more effecient to write the 
	     * entries directly to disk.
	     */
	    if(bec != null)
	    {
	        List entryTmpls = pageBuilder.parseContainers(text, bec);
	        if(entryTmpls.size() == 0)//no entry containers found
	        {	    
	            pw.write(text);
	        }
	        else
	        {
	            Iterator it = entryTmpls.iterator();
	            int pos = 0;	        
	            while(it.hasNext())
	            {	            
	                String ec = it.next().toString();
	            
	                int ecPos = text.indexOf(ec, pos);
	                String part = text.substring(pos, ecPos);
	                writer.write(part);
	                pageBuilder.writeContainer(ec, bec, writer);
	                pos = ecPos + ec.length();	            
	            }
	        
	            writer.write(text.substring(pos, text.length()));
	        }
	    }
	    else
	    {
	        pw.write(text);
	    }
	    
        writer.close();
        pw.close();	
	}
	
	/**
	 * Generate a category page
	 * 
	 * @param blog The weblog from which the page will be generated
	 * @param cat The category 
	 * @param out The OutputStream to write the page to 
	 * @param template The template
	 */
	public void generatePage(TBWeblog blog, String cat, OutputStream out, String template)
	throws IOException
	{		
	    BlogPageContainer bpc = new BlogPageContainer(blog, cat, charset);
	    BlogEntryContainer bec = new BlogEntryContainer(blog, cat);
	    bec.setDefaultSortOrder(isCategoryPageAscending);
		bec.setDefaultDateFormat(dateFormat);
		bec.setDefaultTimeFormat(timeFormat);
		bec.setDefaultIsLimit(isLimitCategoryPage);
		bec.setDefaultLimitBy(categoryPageLimit);
		bpc.addContainer(new CalendarContainer(blog, cat));
		bpc.addContainer(new CategoryListContainer(blog));
		bpc.addContainer(new ArchiveYearsContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new ArchiveListContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new NextPreviousContainer(blog, cat, NextPreviousContainer.NEXT));
		bpc.addContainer(new NextPreviousContainer(blog, cat, NextPreviousContainer.PREV));
		bpc.addContainer(new IncludeContainer());
		//bpc.addContainer(bec);
		//writePage(bpc, template, out);	
		writePage(bpc, bec, template, out);
	}
	
	/**
	 * Generate an archive page
	 * 
	 * @param blog The weblog
	 * @param arc The archive of the page
	 * @param out The OutputStream to write the page to
	 * @param template The template
	 */
	public void generatePage(TBWeblog blog, ArchiveRange arc, OutputStream out, String template)
	throws IOException
	{		
	    BlogPageContainer bpc = new BlogPageContainer(blog, 
	        formatArcRange(arc, blog.getLocale()), charset);
	    BlogEntryContainer bec = new BlogEntryContainer(blog, arc);
	    bec.setDefaultSortOrder(isArchivePageAscending);
		bec.setDefaultDateFormat(dateFormat);
		bec.setDefaultTimeFormat(timeFormat);		
		bpc.addContainer(new CalendarContainer(blog, arc));
		bpc.addContainer(new CategoryListContainer(blog));
		bpc.addContainer(new ArchiveYearsContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new ArchiveListContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new NextPreviousContainer(blog, arc, NextPreviousContainer.NEXT));
		bpc.addContainer(new NextPreviousContainer(blog, arc, NextPreviousContainer.PREV));
		bpc.addContainer(new IncludeContainer());
		
		//bpc.addContainer(bec);
		//writePage(bpc, template, out);
		writePage(bpc, bec, template, out);
	}
	
	private String formatArcRange(ArchiveRange ar, Locale locale)
	{
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(archiveRangeFormat, locale);
		String s = sdf.format(ar.getStartDate());
		if(spanArcRange)
			s += " - " + sdf.format(ar.getExpirationDate());
		
		return s;
	}
	
	public void generatePage(TBWeblog blog, long id, OutputStream out, String template)
	throws IOException
	{
	    String title;
        try
        {
            title = blog.getEntry(id).getTitle();
        }
        catch(Exception ex)
        {
            title = blog.getTitle();
        }
        BlogPageContainer bpc = new BlogPageContainer(blog, title, charset);
	    BlogEntryContainer container = 
			new BlogEntryContainer(blog, id);			
		container.setDefaultDateFormat(dateFormat);
		container.setDefaultTimeFormat(timeFormat);
		//container.setDefaultIsLimit(isLimitFrontPage);
		//container.setDefaultLimitBy(frontPageLimit);
		//container.setDefaultSortOrder(isFrontPageAscending);
		//bpc.addContainer(container);
		bpc.addContainer(new CategoryListContainer(blog));
		bpc.addContainer(new ArchiveYearsContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new ArchiveListContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new NextPreviousContainer(blog, id, NextPreviousContainer.NEXT));
		bpc.addContainer(new NextPreviousContainer(blog, id, NextPreviousContainer.PREV));
		bpc.addContainer(new IncludeContainer());
		
		writePage(bpc, container, template, out);
		//writePage(bpc, template, out);
	}
	
	/**
	 * Generate a front page, rss feed, or archive index page
	 * 
	 * @param blog The weblog
	 * @param type The type of the page: FONT_PAGE, INDEX_PAGE, RSS_PAGE
	 * @param out The OutputStream to write the page to
	 * @param template The template
	 */
	public void generatePage(TBWeblog blog, int type, OutputStream out, String template)
	throws IOException
	{
		BlogPageContainer bpc = new BlogPageContainer(blog, blog.getTitle(), charset);
		
		if(type == RSS_PAGE)
		{
			BlogEntryContainer container = new BlogEntryContainer(blog, BlogEntryContainer.RSS_PAGE);
			container.setLimitEntryBody(isLimitRssEntry);
			container.setDefaultIsLimit(isLimitFrontPage);
			container.setDefaultLimitBy(frontPageLimit);
			container.setDefaultSortOrder(false);
			writePage(bpc, container, template, out);
			return;
		}
		
		bpc.addContainer(new CalendarContainer(blog));
		bpc.addContainer(new CategoryListContainer(blog));
		bpc.addContainer(new ArchiveYearsContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new ArchiveListContainer(blog, archiveRangeFormat, spanArcRange));
		bpc.addContainer(new NextPreviousContainer(NextPreviousContainer.NEXT));
		bpc.addContainer(new NextPreviousContainer(NextPreviousContainer.PREV));
		bpc.addContainer(new IncludeContainer());
		
		if(type == FRONT_PAGE)
		{
			BlogEntryContainer container = 
				new BlogEntryContainer(blog, BlogEntryContainer.FRONT_PAGE);			
			container.setDefaultDateFormat(dateFormat);
			container.setDefaultTimeFormat(timeFormat);
			container.setDefaultIsLimit(isLimitFrontPage);
			container.setDefaultLimitBy(frontPageLimit);
			container.setDefaultSortOrder(isFrontPageAscending);			
			writePage(bpc, container, template, out);
			return;
		}
		
		writePage(bpc, template, out);
	}
	
	/**
	 * Adds a custom tag to the generator
	 * 
	 * @param tag The CustomTag
	 */
	public void addCustomTag(CustomTag tag)
	{
		if(!customTags.contains(tag))
			customTags.add(tag);
	}
	
	/**
	 * Removes a custom tag from the generator
	 * 
	 * @param tag The CustomTag
	 */
	public void removeCustomTag(CustomTag tag)
	{
		customTags.remove(tag);
	}
	
	/**
	 * Sets all the custom tags for the generator
	 * 
	 * @param tags The CustomTags
	 */
	public void setCustomTags(CustomTag tags[])
	{
		customTags.removeAllElements();
		for(int i = 0; i < tags.length; i++)
		{
			customTags.add(tags[i]);
		}
	}
	
	
	/**
	 * Gets all the custom tags from the generator
	 * 
	 * @return an array of CustomTags
	 */
	public CustomTag[] getCustomTags()
	{
		CustomTag tags[] = new CustomTag[customTags.size()];
		for(int i = 0; i < tags.length; i++)
		{
			tags[i] = (CustomTag)customTags.elementAt(i);
		}
		
		return tags;
	}
	
	
	
    /**
     * Gets the default ArchiveRange format
     * 
     * @return The format
     */
    public String getArchiveRangeFormat()
    {
        return archiveRangeFormat;
    }

    /**
     * Gets the default category page limit
     * 
     * @return The limit
     */
    public int getCategoryPageLimit()
    {
        return categoryPageLimit;
    }

    /**
     * Gets the default date format
     * 
     * @return The format
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    /**
     * Gets the default front page limit
     * 
     * @return The limit
     */
    public int getFrontPageLimit()
    {
        return frontPageLimit;
    }

    /**
     * Indicates if entries are written in 
     * ascending order by default for archive pages
     * 
     * @return true if ascending, false otherwise
     */
    public boolean isArchivePageAscending()
    {
        return isArchivePageAscending;
    }

    /**
     * Indicates if category page entries are written 
     * in ascending order by default
     * 
     * @return true if ascending, false otherwise
     */
    public boolean isCategoryPageAscending()
    {
        return isCategoryPageAscending;
    }

    /**
     * Indicates if front page entries are written 
     * in ascending order by default
     * 
     * @return true if ascending, false otherwise
     */
    public boolean isFrontPageAscending()
    {
        return isFrontPageAscending;
    }

    /**
     * Indicates if entries on category pages should be limited by default
     * 
     * @return true if limit, false otherwise
     */
    public boolean isLimitCategoryPage()
    {
        return isLimitCategoryPage;
    }

    /**
     * Indicates if entries on the front page should be limited by default
     * 
     * @return true if limit, false otherwise
     */
    public boolean isLimitFrontPage()
    {
        return isLimitFrontPage;
    }

    /**
     * Indicates if archive list formated dates should include both dates
     * 
     * @return true if span, false otherwise
     */
    public boolean isSpanArcRange()
    {
        return spanArcRange;
    }

    /**
     * Gets the default time format
     * 
     * @return The format
     */
    public String getTimeFormat()
    {
        return timeFormat;
    }

    /**
     * Sets the default ArchiveRange format
     * 
     * @param string The format
     * @param span should span
     */
    public void setArchiveRangeFormat(String string, boolean span)
    {
        archiveRangeFormat = string;
        spanArcRange = span;
    }

    /**
     * Sets the default entry limit for category pages
     * 
     * @param i The limit
     */
    public void setCategoryPageLimit(int i)
    {
        categoryPageLimit = i;
    }

    /**
     * Sets the default date format
     * 
     * @param string The format
     */
    public void setDateFormat(String string)
    {
        dateFormat = string;
    }

    /**
     * Sets the default front page entry limit
     * 
     * @param i The limit
     */
    public void setFrontPageLimit(int i)
    {
        frontPageLimit = i;
    }

    /**
     * Sets the default chronological sort order of entries on archive pages
     * 
     * @param b The sort order
     */
    public void setArchivePageAscending(boolean b)
    {
        isArchivePageAscending = b;
    }

    /**
     * Sets the default chronological sort order of entries on category pages
     * 
     * @param The sort order
     */
    public void setCategoryPageAscending(boolean b)
    {
        isCategoryPageAscending = b;
    }

    /**
     * Sets the default chronological sort order of entries on the front page
     * 
     * @param b The sort order
     */
    public void setFrontPageAscending(boolean b)
    {
        isFrontPageAscending = b;
    }

    /**
     * Sets the default limit policy for category pages
     * 
     * @param b should limit
     */
    public void setLimitCategoryPage(boolean b)
    {
        isLimitCategoryPage = b;
    }

    /**
     * Sets the default limit policy for the front page
     * 
     * @param b should limit
     */
    public void setLimitFrontPage(boolean b)
    {
        isLimitFrontPage = b;
    }

    /**
     * Sets the default time format
     * 
     * @param string The format
     */
    public void setTimeFormat(String string)
    {
        timeFormat = string;
    }
    
    /**
     * Indicates if the body of RSS entries should be truncated by default
     * 
     * @return should limit
     */
    public boolean isLimitRssEntry()
    {
        return isLimitRssEntry;
    }

    /**
     * Indicates if the body of RSS entries should be truncated by default
     * 
     * @param b should limit
     */
    public void setLimitRssEntry(boolean b)
    {
        isLimitRssEntry = b;
    }
    
    /**
     * Gets the charset of generated pages
     * 
     * @return The charset
     */
    public String getCharset()
    {
        return charset;
    }

    /**
     * Sets the charset of generated pages
     * 
     * @param string The charset
     */
    public void setCharset(String string)
    {
        if(java.nio.charset.Charset.isSupported(string))
        	charset = string;
    }
}
