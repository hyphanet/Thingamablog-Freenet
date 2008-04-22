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
import java.awt.Insets;
import java.text.SimpleDateFormat;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.feed.Feed;
import thingamablog.l10n.i18n;



/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class StatusBar extends JPanel 
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    final static Icon blogIcon = UIUtils.getIcon(UIUtils.X16, "blog.png"); //$NON-NLS-1$
	final static Icon feedIcon = UIUtils.getIcon(UIUtils.X16, "blogpages.png"); //$NON-NLS-1$
	final static Icon errFeedIcon = UIUtils.getIcon(UIUtils.X16, "err_feed.png"); //$NON-NLS-1$
	final static Icon taskIcon = UIUtils.getIcon(UIUtils.X16, "cogs.png");
    
	//final static String LAST_PUBLISH_PREFIX = "Last published on ";
	final static SimpleDateFormat LAST_PUBLISH_FORMAT = 
		new SimpleDateFormat("yyyy/MM/dd h:mm a z");  //$NON-NLS-1$
	
	private JProgressBar progressBar;
	private JLabel lastPublishedLabel;
	private JLabel viewingLabel;
	private JLabel refreshingLabel;
    
    private JButton taskDialogButton;
    
    private ThingamablogFrame tambFrame;
	
	
	public StatusBar(ThingamablogFrame f)
	{
		tambFrame = f;
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
        
        taskDialogButton = new JButton(tambFrame.showTasksAction);
        taskDialogButton.setText(null);
        taskDialogButton.setPreferredSize(new Dimension(21, 21));
        taskDialogButton.setMargin(new Insets(0, 0, 0, 0));
        taskDialogButton.setToolTipText(tambFrame.showTasksAction.getValue(Action.NAME).toString());
        /*taskDialogButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                tambFrame.showTaskDialog();                
            }
        });*/
        add(taskDialogButton);

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
			str += " - " + i18n.str("last_published") + ": "+  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
				str += " - " + i18n.str("last_updated") + ": "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
