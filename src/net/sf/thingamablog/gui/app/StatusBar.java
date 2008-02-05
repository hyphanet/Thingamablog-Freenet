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
import java.awt.Dimension;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.feed.Feed;
import net.sf.thingamablog.gui.Messages;



/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class StatusBar extends JPanel 
{
	final static Icon blogIcon = Utils.createIcon(TBGlobals.RESOURCES + "blog_open16.png"); //$NON-NLS-1$
	final static Icon feedIcon = Utils.createIcon(TBGlobals.RESOURCES + "feed16.gif"); //$NON-NLS-1$
	final static Icon errFeedIcon = Utils.createIcon(TBGlobals.RESOURCES + "err_feed16.gif"); //$NON-NLS-1$
	
	//final static String LAST_PUBLISH_PREFIX = "Last published on ";
	final static SimpleDateFormat LAST_PUBLISH_FORMAT = 
		new SimpleDateFormat("yyyy/MM/dd h:mm a z");  //$NON-NLS-1$
	
	private JProgressBar progressBar;
	private JLabel lastPublishedLabel;
	private JLabel viewingLabel;
	private JLabel refreshingLabel;
	
	
	public StatusBar()
	{
		int h = 21;		
		// Set up the status bar at the bottom of the window.
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
				
		lastPublishedLabel = new JLabel(""); //$NON-NLS-1$
		lastPublishedLabel.setPreferredSize(new Dimension(250, h));
		lastPublishedLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		lastPublishedLabel.setMaximumSize(new Dimension(32000, h));
		add(lastPublishedLabel);
		
		refreshingLabel = new JLabel(""); //$NON-NLS-1$
		refreshingLabel.setPreferredSize(new Dimension(200, h));
		refreshingLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		refreshingLabel.setMaximumSize(new Dimension(300, h));
		add(refreshingLabel);		

		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(150, h));
		progressBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		progressBar.setMinimumSize(new Dimension(10, h));
		progressBar.setMaximumSize(new Dimension(150, h));
		add(progressBar);

		viewingLabel = new JLabel(""); //$NON-NLS-1$
		viewingLabel.setPreferredSize(new Dimension(70,h));
		viewingLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		viewingLabel.setMinimumSize(new Dimension(70,h));
		viewingLabel.setMaximumSize(new Dimension(70, h));
		viewingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(viewingLabel);
	}
	
	public void setItem(Weblog w)
	{
		String str = w.getTitle();
		if(w.getLastPublishDate() != null)
			str += " - " + Messages.getString("StatusBar.Last_published") + ": "+  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			LAST_PUBLISH_FORMAT.format(w.getLastPublishDate());
		lastPublishedLabel.setText(str);
		lastPublishedLabel.setIcon(blogIcon);
	}
	
	public void setItem(Feed f)
	{
		String str = f.getTitle();
		Icon ico = feedIcon;
		
		if(f.getLastUpdated() != null)
		{		
			if(!f.isLastUpdateFailed())
			{		
				str += " - " + Messages.getString("StatusBar.Last_updated") + ": "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				LAST_PUBLISH_FORMAT.format(f.getLastUpdated());			
			}
			else
			{
				str += " - " + f.getLastUpdateFailedReason(); //$NON-NLS-1$
				ico = errFeedIcon;
			}
		}
		
		lastPublishedLabel.setText(str);
		lastPublishedLabel.setIcon(ico);		
	}
	
	
	public void setViewingCount(int n)
	{
		viewingLabel.setText("" + n); //$NON-NLS-1$
	}
	
	public void setRefreshingText(String t)
	{
		refreshingLabel.setText(t);
	}
	
	public JProgressBar getJProgressBar()
	{
		return progressBar;
	}
}
