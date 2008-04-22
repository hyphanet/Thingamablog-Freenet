/*
 * Created on May 7, 2004
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
package net.sf.thingamablog.gui.app;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.thingamablog.feed.Feed;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.StandardDialog;
import thingamablog.l10n.i18n;

/**
 * @author Bob Tantlinger
 *
 */
public class FeedPropertiesDialog extends StandardDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Feed feed;
	private JTextField urlField;
	private JTextField titleField;
	private JCheckBox arcCheckBox;
	private SpinnerNumberModel arcSpinnerModel;
	private DateFormat df = 
		DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	
	public FeedPropertiesDialog(Frame parent, Feed feed)
	{
		super(parent, i18n.str("feed_properties_")); //$NON-NLS-1$
		this.feed = feed;
		init();
		

		 
	}
	
	public FeedPropertiesDialog(Dialog parent, Feed feed)
	{
		super(parent, i18n.str("feed_properties_")); //$NON-NLS-1$
		this.feed = feed;
		init();
	}
	
	private void init()
	{
		titleField = new JTextField();
		titleField.setEditable(false);
		urlField = new JTextField();
		urlField.setEditable(false);
		
		arcSpinnerModel = 
			new SpinnerNumberModel(feed.getItemLimit(), 5, 1000, 5);    	
		final JSpinner arcSpinner = new JSpinner(arcSpinnerModel);
		arcSpinner.setEnabled(feed.isLimitItems());   
		
		titleField.setText(feed.getTitle());
		urlField.setText(feed.getURL());
		
		arcCheckBox = new JCheckBox();
		arcCheckBox.setSelected(feed.isLimitItems());
		arcCheckBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				arcSpinner.setEnabled(arcCheckBox.isSelected());						
			}
		});
		
		Date date = feed.getLastUpdated();
		String updateStr = i18n.str("never"); //$NON-NLS-1$
		if(date != null)
			updateStr = df.format(feed.getLastUpdated());
		JLabel updateLabel = new JLabel(updateStr);		
		updateLabel.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
		
		LabelledItemPanel lip = new LabelledItemPanel();
		lip.addItem(i18n.str("feed_title"), titleField);		 //$NON-NLS-1$
		lip.addItem(i18n.str("feed_url"), urlField);		 //$NON-NLS-1$
		lip.addItem(i18n.str("updated"), updateLabel); //$NON-NLS-1$
		lip.setBorder(new TitledBorder(i18n.str("feed"))); //$NON-NLS-1$
		
		LabelledItemPanel lip1 = new LabelledItemPanel();
		lip1.addItem(i18n.str("limit_headlines"), arcCheckBox); //$NON-NLS-1$
		JPanel spinPanel = new JPanel(new BorderLayout(5, 5));
		spinPanel.add(arcSpinner, BorderLayout.WEST);
		spinPanel.add(new JPanel(), BorderLayout.CENTER);
		lip1.addItem(i18n.str("max_headlines"), spinPanel); //$NON-NLS-1$
		lip1.setBorder(new TitledBorder(i18n.str("archiving"))); //$NON-NLS-1$
				
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPanel.add(lip, BorderLayout.CENTER);
		mainPanel.add(lip1, BorderLayout.SOUTH);
		setContentPane(mainPanel);
		
		pack();
		setSize(370, getHeight());
		setResizable(false);						
	}
	
	public boolean isValidData()
	{
		//feed.setTitle(titleField.getText());
	    feed.setLimitItems(arcCheckBox.isSelected());
		feed.setItemLimit(arcSpinnerModel.getNumber().intValue());
		return true;
	}
}
