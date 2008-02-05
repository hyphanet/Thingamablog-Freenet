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

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.feed.FeedItem;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.ViewerPaneModel;

/**
 * @author Bob Tantlinger
 */
public class TBViewerPaneModel implements ViewerPaneModel
{
	private static final String exts[] = {".gif", ".jpg", ".png"}; 
	private Object data;		
	private SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy hh:mm a z");	
	private String titles[] = new String[0];
	private String descr[] = new String[0];
	private Vector lst = new Vector();
	
	public void setModelData(Object o)
	{
	    data = o;
	    if(data instanceof BlogEntry)
	    {
	        BlogEntry e = (BlogEntry)data;
	        titles = new String[4];
	        descr = new String[4];
	        
	        titles[0] = Messages.getString("TBViewerPaneModel.Title") +  ":";	        
	        descr[0] = e.getTitle();	        
	        
	        titles[1] = Messages.getString("TBViewerPaneModel.Author") +  ":";
	        descr[1] = e.getAuthor().getName();
	        
	        titles[2] = Messages.getString("TBViewerPaneModel.Categories") +  ":"; 
	        descr[2] = catString(e.getCategories());
	           
	        titles[3] = Messages.getString("TBViewerPaneModel.Post_Date") +  ":";
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
	        
	        titles[0] = Messages.getString("TBViewerPaneModel.Title") +  ":";
	        descr[0] = i.getTitle();	        
	        
	        titles[1] = Messages.getString("TBViewerPaneModel.Post_Date") +  ":";
	        descr[1] = sdf.format(d);
	        
	        titles[2] = Messages.getString("TBViewerPaneModel.Feed") +  ":";
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
        String text = "";
        if(data instanceof BlogEntry)
            text = ((BlogEntry)data).getText();
        else if(data instanceof FeedItem)
        {
            FeedItem item = (FeedItem)data;
            String title = item.getTitle();
            if(title == null || title.equals(""))
                title = "Link";
            
             text = 
            	"<div class=\"header\"><a href=\"" + item.getLink() + "\">" + 
            	title + "</a></div>\n";
            	
            text += item.getDescription(); //+ TABLE_END;            
            text = "<div class=\"item\">" + text + "</div>";
        }
        
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
}
