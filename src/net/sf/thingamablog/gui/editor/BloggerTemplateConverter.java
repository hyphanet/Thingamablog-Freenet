/*
 * Created on Jul 20, 2004
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
 * 
 */
package net.sf.thingamablog.gui.editor;

/**
 * @author Bob Tantlinger
 */
public class BloggerTemplateConverter
{
	
	//Tags that work anywhere
	public static final String BLOG_URL = "<$FrontPageLink$>";
	public static final String RSS_FILE = "<$RssLink$>";
	public static final String BLOG_TITLE = "<$BlogTitle$>";
	public static final String BLOG_DESC = "<$BlogDescription$>";
	public static final String ARC_INDEX = "<$IndexPageLink$>";
	public static final String PAGE_TITLE = "<$PageTitle$>"; //unique
	
	public static final String ARCS_BEGIN = "<ArchiveList>";
	public static final String ARC_LINK = "<$ArchiveLink$>";
	public static final String ARC_NAME = "<$ArchiveName$>";
	public static final String ARCS_END = "</ArchiveList>";
	
	//unique to Thingamablog
	public static final String CATS_BEGIN = "<CategoryList>";
	public static final String CAT_LINK = "<$CategoryLink$>";
	public static final String CAT_NAME = "<$CategoryName$>";
	public static final String CATS_END = "</CategoryList>";		
	
	//Tags that only have meaning between <Blogger> and </Blogger>
	//This would be an entry template
	public static final String BLOG_ENTRIES_BEGIN = "<BlogEntry>";
	
	public static final String DAY_HEADER_BEGIN = "<DayHeader>";
	public static final String DAY_HEADER_DATE = "<$DayHeaderDate$>";
	public static final String DAY_HEADER_END = "</DayHeader>";		
	public static final String DAY_FOOTER_BEGIN = "<DayFooter>";
	public static final String DAY_FOOTER_END = "</DayFooter>";
	
	public static final String ENTRY_BODY = "<$EntryBody$>";
	public static final String ENTRY_ID = "<$EntryID$>";
	public static final String ENTRY_AUTHOR = "<$EntryAuthor$>";
	public static final String ENTRY_AUTHOR_EMAIL = "<$EntryAuthorEmail$>";
	public static final String ENTRY_AUTHOR_URL = "<$EntryAuthorURL$>";
	public static final String ENTRY_DATE = "<$EntryDate$>";//unique
	public static final String ENTRY_TIME = "<$EntryTime$>";//unique
	public static final String ENTRY_DATE_TIME = "<$EntryDateTime$>";
	public static final String ENTRY_ARCPAGE = "<$EntryArchivePage$>";
	public static final String ENTRY_PERMA = "<$EntryPermalink$>";
	
	public static final String ENTRY_TITLE_BEGIN = "<EntryTitle>";
	public static final String ENTRY_TITLE = "<$EntryTitle$>";
	public static final String ENTRY_TITLE_END = "</EntryTitle>";
	
	public static final String ENTRY_CATS_BEGIN = "<EntryCategories>";//unique
	public static final String ENTRY_CATS_END = "</EntryCategories>";//unique
	
	//unique
	public static final String ENTRY_MODIFIED_BEGIN = "<EntryModifiedDate>";
	public static final String ENTRY_MODIFIED_DATE = "<$EntryModifiedDate$>";
	public static final String ENTRY_MODIFIED_END = "</EntryModifiedDate>";
	
		
	public static final String BLOG_ENTRIES_END = "</BlogEntry>";
	
	
	public static synchronized String convert(String tmpl, net.sf.thingamablog.blog.Template t)
	{
		String fileName = "";
		if(t != null)
		    fileName = t.getName();
	    
		//TODO update template names when they get externalized
	    if(fileName.equals("Front Page"))
		{
		    tmpl = removeBloggerContainer("ItemPage", tmpl);
		    tmpl = removeBloggerContainer("ArchivePage", tmpl);
		}
		else if(fileName.equals("Archive") || fileName.equals("Category"))
		{
		    tmpl = removeBloggerContainer("ItemPage", tmpl);
		    tmpl = removeBloggerContainer("MainPage", tmpl);
		}
		else if(fileName.equals("Entry Pages"))
		{
		    tmpl = removeBloggerContainer("MainOrArchivePage", tmpl);
		    tmpl = removeBloggerContainer("ArchivePage", tmpl);
		    tmpl = removeBloggerContainer("MainPage", tmpl);
		}    
				
	    tmpl = removeBloggerContainer("BlogItemCommentsEnabled", tmpl);	    
	    
	    tmpl = replaceVariable("<MainOrArchivePage>", "", tmpl);
	    tmpl = replaceVariable("</MainOrArchivePage>", "", tmpl);
	    tmpl = replaceVariable("<MainPage>", "", tmpl);
	    tmpl = replaceVariable("</MainPage>", "", tmpl);
	    tmpl = replaceVariable("<ArchivePage>", "", tmpl);
	    tmpl = replaceVariable("</ArchivePage>", "", tmpl);
	    tmpl = replaceVariable("<ItemPage>", "", tmpl);
	    tmpl = replaceVariable("</ItemPage>", "", tmpl);
	    
	    tmpl = replaceVariable("<$BlogURL$>", BLOG_URL, tmpl);
	    tmpl = replaceVariable("<$BlogUrl$>", BLOG_URL, tmpl);
		tmpl = replaceVariable("<$BlogTitle$>", BLOG_TITLE, tmpl);
		tmpl = replaceVariable("<$BlogDescription$>", BLOG_DESC, tmpl);
		tmpl = replaceVariable("<$BlogPageTitle$>", PAGE_TITLE, tmpl);
		
		tmpl = replaceVariable("<$BlogArchiveFileName$>", ARC_INDEX, tmpl);
		tmpl = replaceVariable("<BloggerArchives>", ARCS_BEGIN, tmpl);
		tmpl = replaceVariable("<$BlogArchiveLink$>", ARC_LINK, tmpl);
		tmpl = replaceVariable("<$BlogArchiveName$>", ARC_NAME, tmpl);
		tmpl = replaceVariable("</BloggerArchives>", ARCS_END, tmpl);
    	
		tmpl = replaceVariable("<Blogger>", BLOG_ENTRIES_BEGIN, tmpl);
		tmpl = replaceVariable("<BlogDateHeader>", DAY_HEADER_BEGIN, tmpl);
		tmpl = replaceVariable("<$BlogDateHeaderDate$>", DAY_HEADER_DATE, tmpl);
		tmpl = replaceVariable("</BlogDateHeader>", DAY_HEADER_END, tmpl);
		tmpl = replaceVariable("<BlogDateFooter>", DAY_FOOTER_BEGIN, tmpl);
		tmpl = replaceVariable("</BlogDateFooter>", DAY_FOOTER_END, tmpl);
		tmpl = replaceVariable("<$BlogItemBody$>", ENTRY_BODY, tmpl);
		tmpl = replaceVariable("<$BlogItemNumber$>", ENTRY_ID, tmpl);
		tmpl = replaceVariable("<$BlogItemAuthor$>", ENTRY_AUTHOR, tmpl);
		tmpl = replaceVariable("<$BlogItemAuthorNickname$>", ENTRY_AUTHOR, tmpl);
		tmpl = replaceVariable("<$BlogItemAuthorEmail$>", ENTRY_AUTHOR_EMAIL, tmpl);
		tmpl = replaceVariable("<$BlogItemAuthorURL$>", ENTRY_AUTHOR_URL, tmpl);
		tmpl = replaceVariable("<$BlogItemAuthorUrl$>", ENTRY_AUTHOR_URL, tmpl);
		tmpl = replaceVariable("<$BlogItemDateTime$>", ENTRY_DATE_TIME, tmpl);
		tmpl = replaceVariable("<$BlogItemArchiveFileName$>", ENTRY_ARCPAGE, tmpl);
		tmpl = replaceVariable("<$BlogItemPermalinkURL$>", ENTRY_PERMA, tmpl);
		tmpl = replaceVariable("<$BlogItemPermalinkUrl$>", ENTRY_PERMA, tmpl);
		tmpl = replaceVariable("<$BlogItemControl$>", "", tmpl);
		tmpl = replaceVariable("<BlogItemTitle>", ENTRY_TITLE_BEGIN, tmpl);
		tmpl = replaceVariable("<$BlogItemTitle$>", ENTRY_TITLE, tmpl);
		tmpl = replaceVariable("<BlogItemURL>", "", tmpl);	
		tmpl = replaceVariable("<$BlogItemURL$>", ENTRY_PERMA, tmpl);
		tmpl = replaceVariable("<$BlogItemUrl$>", ENTRY_PERMA, tmpl);
		tmpl = replaceVariable("</BlogItemURL>", "", tmpl);
		tmpl = replaceVariable("</BlogItemTitle>", ENTRY_TITLE_END, tmpl);
		tmpl = replaceVariable("</Blogger>", BLOG_ENTRIES_END, tmpl);
		
		tmpl = replaceVariable("<$BlogEncoding$>", "<$Charset$>", tmpl);
		tmpl = replaceVariable("<$BlogMetaData$>", "", tmpl);
		
		String s = "<link rel=\".\" type=\"application/rss+xml\" title=\"<$BlogTitle$>\" href=\"<$RssLink$>\" />";
		tmpl = replaceVariable("<$BlogSiteFeedLink$>", s, tmpl);
		
		tmpl = replaceVariable("<BlogSiteFeed>", "", tmpl);
		tmpl = replaceVariable("<$BlogSiteFeedUrl$>", RSS_FILE, tmpl);
		tmpl = replaceVariable("</BlogSiteFeed>", "", tmpl);
		
		tmpl = replaceVariable("<$BlogOwnerFirstName$>", "John", tmpl);
		tmpl = replaceVariable("<$BlogOwnerLastName$>", "Doe", tmpl);
		tmpl = replaceVariable("<$BlogOwnerEmail$>", "user@domain.net", tmpl);
		tmpl = replaceVariable("<$BlogOwnerFullName$>", "John Doe", tmpl);
		tmpl = replaceVariable("<$BlogOwnerPhotoUrl$>", "http://yoursite.com/your_pic.jpg", tmpl);
		tmpl = replaceVariable("<$BlogOwnerNickName$>", "John", tmpl);
		tmpl = replaceVariable("<$BlogOwnerLocation$>", "Your Location", tmpl);
		tmpl = replaceVariable("<$BlogOwnerAboutMe$>", "Things about you", tmpl);
		tmpl = replaceVariable("<$BlogOwnerProfileURL$>", "http://whatever.com", tmpl);	
		tmpl = replaceVariable("<$BlogOwnerProfileUrl$>", "http://whatever.com", tmpl);	
		
		tmpl = replaceVariable("<BloggerPreviousItems>", "<BlogEntry limit=\"1\" limit_by=\"8\">", tmpl);
		tmpl = replaceVariable("<$BlogPreviousItemTitle$>", "<EntryTitle><$EntryTitle$></EntryTitle>", tmpl);
		tmpl = replaceVariable("</BloggerPreviousItems>", "</BlogEntry>", tmpl);		
    	
		return tmpl;	
	}	
	
	protected static String removeBloggerContainer(String name, String tmpl)
	{
	    String open = "<" + name + ">";
	    String close = "</" + name + ">";
	    
	    StringBuffer sb = new StringBuffer(tmpl);
	    int p = 0;
	    while((p = sb.indexOf(open, p)) != -1)
	    {
	        int end = sb.indexOf(close, p);
	        if(end == -1)
	            break;
	        
	        sb.delete(p, end + close.length());
	    }
	    
	    return sb.toString();
	}
	
	protected static String replaceVariable(String var, String val, String tmpl)
	{              
		if(var.equals(""))//don't try to process empty templates
			 return tmpl;
        
		while(tmpl.indexOf(var) != -1 && !var.equals(val))
		{
			StringBuffer sb = new StringBuffer(tmpl);  
			int s = sb.toString().indexOf(var);            
			int e = s + var.length();
			sb.delete(s, e);
			sb.insert(s, val);
			tmpl = sb.toString();
            
			//System.out.println("Replaced " + var + " with " + val);
		}
                
		return tmpl;
	}
}
