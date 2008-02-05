/*
 * Created on May 17, 2004
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
package net.sf.thingamablog.gui.properties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBFrontPagePanel extends PropertyPanel
{
	private TBWeblog weblog;
	
	private JTextField fpFileNameField;
	private JTextField rssFileNameField; 
	private JCheckBox generateRssCb;
	private JCheckBox writeFullRssEntriesCb;   
	private JCheckBox writeOldFirstCb;    
	private JCheckBox limitMainEntriesCb;
	private JSpinner numOnMainSpinner;
	private SpinnerNumberModel numOnMainModel;
    
	public TBFrontPagePanel(TBWeblog wb)
	{
		weblog = wb;
		fpFileNameField = new JTextField(15);
		fpFileNameField.setText(weblog.getFrontPageFileName());
    	
		writeOldFirstCb = new JCheckBox(Messages.getString("TBFrontPagePanel.Generate_oldest_entries_first")); //$NON-NLS-1$
		writeOldFirstCb.setSelected(weblog.getPageGenerator().isFrontPageAscending());
    	    	
		numOnMainModel = new SpinnerNumberModel(10, 1, 1000, 1);
		numOnMainSpinner = new JSpinner(numOnMainModel);
		limitMainEntriesCb = new JCheckBox(Messages.getString("TBFrontPagePanel.Limit_front_page_entries")); //$NON-NLS-1$
		limitMainEntriesCb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{				
				numOnMainSpinner.setEnabled(limitMainEntriesCb.isSelected());
			}
		});
		limitMainEntriesCb.setSelected(weblog.getPageGenerator().isLimitFrontPage());
		numOnMainModel.setValue(new Integer(weblog.getPageGenerator().getFrontPageLimit()));
		numOnMainSpinner.setEnabled(limitMainEntriesCb.isSelected());
    	
		generateRssCb = new JCheckBox(Messages.getString("TBFrontPagePanel.Generate_RSS")); //$NON-NLS-1$
		generateRssCb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{				
				rssFileNameField.setEditable(generateRssCb.isSelected());
				writeFullRssEntriesCb.setEnabled(generateRssCb.isSelected());
			}
		});
		generateRssCb.setSelected(weblog.isGenerateRssFeed());
		
		rssFileNameField = new JTextField(15);
		rssFileNameField.setText(weblog.getRssFileName());
		rssFileNameField.setEditable(weblog.isGenerateRssFeed());
		writeFullRssEntriesCb = new JCheckBox(Messages.getString("TBFrontPagePanel.Write_full_RSS")); //$NON-NLS-1$
		writeFullRssEntriesCb.setSelected(!weblog.getPageGenerator().isLimitRssEntry());
		writeFullRssEntriesCb.setEnabled(weblog.isGenerateRssFeed());
    	
		setLayout(new BorderLayout(5, 5));
		setBorder(new EmptyBorder(5, 5, 5, 5));
    	
		Box box = Box.createVerticalBox();    	
		box.setBorder(new TitledBorder(Messages.getString("TBFrontPagePanel.Front_Page")));		 //$NON-NLS-1$

		JPanel spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));    	
		spacer.add(writeOldFirstCb);
		box.add(spacer);
		JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p3.add(limitMainEntriesCb);
		p3.add(numOnMainSpinner);
		box.add(p3);
		LabelledItemPanel fp = new LabelledItemPanel();
		spacer = new JPanel(new BorderLayout());    	
		spacer.add(fpFileNameField, BorderLayout.WEST);
		spacer.add(new JPanel(), BorderLayout.CENTER);
		fp.addItem(Messages.getString("TBFrontPagePanel.File_Name"), spacer); //$NON-NLS-1$
		box.add(fp);
		
		
		Box box2 = Box.createVerticalBox();
		box2.setBorder(new TitledBorder(Messages.getString("TBFrontPagePanel.RSS_Feed"))); //$NON-NLS-1$
		spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacer.add(generateRssCb);
		box2.add(spacer);

		spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacer.add(writeFullRssEntriesCb);
		box2.add(spacer);
		
		add(box, BorderLayout.NORTH);
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//bottom.add(box2);
		LabelledItemPanel lip = new LabelledItemPanel();
		spacer = new JPanel(new BorderLayout());
		spacer.add(rssFileNameField, BorderLayout.WEST);
		spacer.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(Messages.getString("TBFrontPagePanel.File_Name"), spacer); //$NON-NLS-1$
		box2.add(lip);
		add(box2, BorderLayout.CENTER);
		//add(bottom, BorderLayout.CENTER);    	
	}
    
	/* (non-Javadoc)
	 * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
	 */
	public void saveProperties()
	{
		weblog.setFrontPageFileName(fpFileNameField.getText());
		weblog.getPageGenerator().setLimitFrontPage(limitMainEntriesCb.isSelected());
		Integer lim = (Integer)numOnMainModel.getValue();
		weblog.getPageGenerator().setFrontPageLimit(lim.intValue());
		weblog.getPageGenerator().setFrontPageAscending(writeOldFirstCb.isSelected());
		
		weblog.setGenerateRssFeed(generateRssCb.isSelected());
		weblog.setRssFileName(rssFileNameField.getText());
		weblog.getPageGenerator().setLimitRssEntry(!writeFullRssEntriesCb.isSelected());
	}
	

	
	public boolean isValidData()
	{
		if(generateRssCb.isSelected())
		{
			if(rssFileNameField.getText() == null ||
			rssFileNameField.getText().equals("")) //$NON-NLS-1$
			{
				JOptionPane.showMessageDialog(this, 
					Messages.getString("TBFrontPagePanel.enter_name_prompt"), Messages.getString("TBFrontPagePanel.Warning"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		if(fpFileNameField.getText() == null ||
		fpFileNameField.getText().equals("")) //$NON-NLS-1$
		{
			JOptionPane.showMessageDialog(this, 
				Messages.getString("TBFrontPagePanel.enter_name_prompt"), Messages.getString("TBFrontPagePanel.Warning"), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}

}
