/*
 * Created on May 6, 2004
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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.feed.Feed;
/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class FeedTreeCellRenderer extends DefaultTreeCellRenderer
{
	private ImageIcon feed, errFeed;
	//private Font normalFont, boldFont; 
	
	public FeedTreeCellRenderer()
	{
		super();
        //normalFont = new Font("Dialog", Font.PLAIN, 11);
        //boldFont = normalFont.deriveFont(Font.BOLD); 
	    feed = Utils.createIcon(TBGlobals.RESOURCES + "feed16.gif");
		errFeed = Utils.createIcon(TBGlobals.RESOURCES + "err_feed16.gif");
	}
	
	public Component getTreeCellRendererComponent(JTree tree,
		   Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) 
	{
		if(leaf && value instanceof Feed)
		{
			Feed ch = (Feed)value;
			if(ch.getTitle() == null || ch.getTitle().equals(""))
			{
				String uri = ch.getURL();
				value = new String(uri);
				
			}
			else
				value = new String(ch.getTitle());
			
				
			try
			{
				int n = ch.getUnreadItems().length;
				if(n > 0)
				{
					value = value.toString() + " (" + n + ")";
					//setFont(boldFont);
				}
				else
				{
				    //setFont(normalFont);
				}
				//System.out.println(value);
			}
			catch(Exception ex)
			{
				//ex.printStackTrace();
			}
			
			if(ch.isLastUpdateFailed())
			{
				setToolTipText(ch.getLastUpdateFailedReason());
				setLeafIcon(errFeed);
			}
			else
			{
				setToolTipText(ch.getTitle());
				setLeafIcon(feed);
			}
		}
		else
		{
		    //setFont(normalFont);
		}
		
		Component c = super.getTreeCellRendererComponent(
			tree, value, sel, expanded, leaf, row, hasFocus);
		
		
		return c;			
				
	}
}
