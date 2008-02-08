/*
 * Created on May 1, 2004
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
package net.sf.thingamablog.gui.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.feed.FeedItem;
import net.sf.thingamablog.gui.ViewerPaneModel;
import net.sf.thingamablog.gui.editor.EntryImageUtils;

/**
 * @author Bob Tantlinger
 */
public class TBViewerPaneModel implements ViewerPaneModel
{
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app"); //$NON-NLS-1$
    
    private static final String exts[] = {".gif", ".jpg", ".png"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private Object data;		
	private SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm a z");	 //$NON-NLS-1$
	private String titles[] = new String[0];
	private String descr[] = new String[0];
	private Vector lst = new Vector();
    
    private Weblog weblog;
	
	public void setModelData(Object o)
	{
	    data = o;
	    if(data instanceof BlogEntry)
	    {
	        BlogEntry e = (BlogEntry)data;
	        titles = new String[4];
	        descr = new String[4];
	        
	        titles[0] = i18n.str("title") +  ":";	         //$NON-NLS-1$ //$NON-NLS-2$
	        descr[0] = e.getTitle();	        
	        
	        titles[1] = i18n.str("author") +  ":"; //$NON-NLS-1$ //$NON-NLS-2$
	        descr[1] = e.getAuthor().getName();
	        
	        titles[2] = i18n.str("categories") +  ":";  //$NON-NLS-1$ //$NON-NLS-2$
	        descr[2] = catString(e.getCategories());
	           
	        titles[3] = i18n.str("post_date") +  ":"; //$NON-NLS-1$ //$NON-NLS-2$
	        descr[3] = sdf.format(e.getDate());
	    }
	    else if(data instanceof FeedItem)
	    {	        
	        FeedItem i = (FeedItem)data;
	        Date d = i.getPubDate();
	        if(d == null)
	            d = i.getRetrieved();
	        
	        titles = new String[3];
	        descr = new String[3];
	        
	        titles[0] = i18n.str("title") +  ":"; //$NON-NLS-1$ //$NON-NLS-2$
	        descr[0] = i.getTitle();	        
	        
	        titles[1] = i18n.str("post_date") +  ":"; //$NON-NLS-1$ //$NON-NLS-2$
	        descr[1] = sdf.format(d);
	        
	        titles[2] = i18n.str("feed") +  ":"; //$NON-NLS-1$ //$NON-NLS-2$
	        descr[2] = i.getChannelTitle();	        
	    }
	    else
	    {
	        titles = new String[0];
	        descr = new String[0];
	    }
	    
	    fireModelDataChanged();
	}
	
    public int getHeaderCount()
    {
        return titles.length;
    }
    
    public String getHeaderTitle(int row)
    {
        return titles[row];
    }
    
    public String getHeaderDescription(int row)
    {
        return descr[row];
    }
    
    public void addChangeListener(ChangeListener l)
    {
        if(!lst.contains(l))
            lst.add(l);
    }
    
    public void removeChangeListener(ChangeListener l)
    {
        lst.remove(l);
    }
    
    protected void fireModelDataChanged()
    {
        for(Enumeration en = lst.elements(); en.hasMoreElements();)
        {
            ChangeEvent evt = new ChangeEvent(this);
            ChangeListener lst = (ChangeListener)en.nextElement();
            lst.stateChanged(evt);
        }
    }
	
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.ViewerPaneModel#getText()
     */
    public String getText()
    {
        String text = ""; //$NON-NLS-1$
        if(data instanceof BlogEntry)
        {
            text = ((BlogEntry)data).getText();            
            if(weblog != null)
            {
                text = EntryImageUtils.changeRelativeImageURLsToAbsolute(text, weblog);
            }
            
        }
        else if(data instanceof FeedItem)
        {
            FeedItem item = (FeedItem)data;
            String title = item.getTitle();
            if(title == null || title.equals("")) //$NON-NLS-1$
                title = i18n.str("link"); //$NON-NLS-1$
            
             text = 
            	"<div class=\"header\"><a href=\"" + item.getLink() + "\">" +  //$NON-NLS-1$ //$NON-NLS-2$
            	title + "</a></div>\n"; //$NON-NLS-1$
            	
            text += item.getDescription(); //+ TABLE_END;            
            
        }
        
        text = "<div class=\"item\">" + text + "</div>"; //$NON-NLS-1$ //$NON-NLS-2$
        return text;        
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.ViewerPaneModel#getIcon()
     */
    public ImageIcon getIcon()
    {
        if(data instanceof FeedItem)
        {            
            FeedItem item = (FeedItem)data;
            //image files are saved by the feed. The img filename
            //is the Absolute value of the hashcode of the feed's image url
            //with an extension of either gif, jpg, or png        
            String link = item.getChannelImageURL();
            String name = Math.abs(link.hashCode()) + ""; //$NON-NLS-1$
            String path = TBGlobals.IMG_CACHE_DIR + TBGlobals.SEP + name;
            
            for(int i = 0; i < exts.length; i++)
            {
                java.io.File f = new java.io.File(path + exts[i]);
                if(f.exists())
                {
                    ImageIcon img = new ImageIcon(f.getAbsolutePath());
                    return img;        	        
                }
            }
        }        
		
        return null;//no image	     
    }

    private String catString(String c[])
    {
    	String s = ""; //$NON-NLS-1$
    	for(int i = 0; i < c.length; i++)
    	{
    		s += c[i];
    		if(i < c.length - 1)
    			s += ", "; //$NON-NLS-1$
       	}
       	
       	return s;    	
    }

    
    /**
     * @return the weblog
     */
    public Weblog getWeblog()
    {
        return weblog;
    }

    
    /**
     * @param weblog the weblog to set
     */
    public void setWeblog(Weblog weblog)
    {
        this.weblog = weblog;
    }
}
