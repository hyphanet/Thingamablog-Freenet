/*
 * Created on May 23, 2004
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.LabelledItemPanel;
import thingamablog.l10n.i18n;




/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBCategoriesPanel extends PropertyPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private TBWeblog weblog;
	private EditableList eCatList;
	private WeblogEditableListModel edModel;
	
	
	private JCheckBox limitCatEntriesCb;
    private JCheckBox genCatFeedsCb;
    
	private SpinnerNumberModel numOnCatModel;
	private JComboBox extCombo;
    private JComboBox feedExtCombo;
	private JCheckBox oldestFirstCb;
	
	public TBCategoriesPanel(TBWeblog wb)
	{
		weblog = wb;
		edModel = new WeblogEditableListModel(WeblogEditableListModel.CATEGORIES);
		eCatList = new EditableList(edModel);
		eCatList.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		try
		{
			eCatList.setListData(weblog.getCategories());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		numOnCatModel = new SpinnerNumberModel(10, 1, 1000, 1);    	
		final JSpinner numOnCatSpinner = new JSpinner(numOnCatModel);
		numOnCatModel.setValue(new Integer(weblog.getPageGenerator().getCategoryPageLimit()));
		
		limitCatEntriesCb = new JCheckBox(i18n.str("limit_category_page")); //$NON-NLS-1$
		limitCatEntriesCb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{				
				numOnCatSpinner.setEnabled(limitCatEntriesCb.isSelected());
			}
		});
		limitCatEntriesCb.setSelected(weblog.getPageGenerator().isLimitCategoryPage());
		
		extCombo = new JComboBox(TBGlobals.TEXT_FILE_EXTS);
		extCombo.setEditable(true);
		extCombo.setSelectedItem(weblog.getCategoriesExtension());
        
        genCatFeedsCb = new JCheckBox(i18n.str("generate_category_feeds")); //$NON-NLS-1$
        genCatFeedsCb.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {               
                feedExtCombo.setEnabled(genCatFeedsCb.isSelected());
            }
        });
        genCatFeedsCb.setSelected(weblog.isGenerateCategoryFeeds());
        
        feedExtCombo = new JComboBox(TBGlobals.TEXT_FILE_EXTS);
        feedExtCombo.setEnabled(weblog.isGenerateCategoryFeeds());
        feedExtCombo.setEditable(true);
        feedExtCombo.setSelectedItem(weblog.getCategoriesFeedExtension());
		
		oldestFirstCb = new JCheckBox(i18n.str("generate_oldest_entries_first")); //$NON-NLS-1$
    	oldestFirstCb.setSelected(weblog.getPageGenerator().isCategoryPageAscending());    	

    	        
        JPanel limitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		limitPanel.add(limitCatEntriesCb);
		limitPanel.add(numOnCatSpinner);
		LabelledItemPanel lip = new LabelledItemPanel();
		JPanel spacer = new JPanel(new BorderLayout());
		spacer.add(extCombo, BorderLayout.WEST);
		spacer.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(i18n.str("category_pages_extension"), spacer); //$NON-NLS-1$
		
        JPanel catFeedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        catFeedPanel.add(genCatFeedsCb);
        LabelledItemPanel lip1 = new LabelledItemPanel();
        spacer = new JPanel(new BorderLayout());
        spacer.add(feedExtCombo, BorderLayout.WEST);
        spacer.add(new JPanel(), BorderLayout.CENTER);
        lip1.addItem(i18n.str("category_feeds_extension"), spacer); 
        
		JPanel bottomPanel = new JPanel(new GridLayout(5, 1));
		spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacer.add(oldestFirstCb);
		bottomPanel.add(spacer);        
		bottomPanel.add(limitPanel);
		bottomPanel.add(lip);
        bottomPanel.add(catFeedPanel);
        bottomPanel.add(lip1);
		
		setLayout(new BorderLayout(5, 5));
		add(eCatList,  BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {        
		weblog.setPublishAll(true);
		weblog.setCategoriesExtension(extCombo.getSelectedItem().toString());
		weblog.getPageGenerator().setCategoryPageAscending(oldestFirstCb.isSelected());
		weblog.getPageGenerator().setLimitCategoryPage(limitCatEntriesCb.isSelected());
		Integer lim = (Integer)numOnCatModel.getValue();
		weblog.getPageGenerator().setCategoryPageLimit(lim.intValue());
        weblog.setCategoriesFeedExtension(feedExtCombo.getSelectedItem().toString());
        weblog.setGenerateCategoryFeeds(genCatFeedsCb.isSelected());
		
		try
		{
			edModel.syncListWithWeblog(weblog);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }
    
	public boolean isValidData()
	{
		return true;
	}

}
