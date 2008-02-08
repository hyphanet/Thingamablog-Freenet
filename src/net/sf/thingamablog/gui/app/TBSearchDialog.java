/*
 * Created on Jun 12, 2004
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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.CategoryEvent;
import net.sf.thingamablog.blog.CategoryListener;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogList;
import net.sf.thingamablog.blog.WeblogSearch;
import net.sf.thingamablog.feed.FeedBackendException;
import net.sf.thingamablog.feed.FeedFolder;
import net.sf.thingamablog.feed.FeedItem;
import net.sf.thingamablog.feed.FeedSearch;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.StandardDialog;

import com.tantlinger.jdatepicker.JCalendarComboBox;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBSearchDialog extends JDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app");
    
    private final String DRAFTS = i18n.str("drafts"); //$NON-NLS-1$
	private final String POSTS = i18n.str("posts"); //$NON-NLS-1$
	private final String CREATED = i18n.str("created"); //$NON-NLS-1$
	private final String MODIFIED = i18n.str("modified"); //$NON-NLS-1$
	private final String ALL_CATS = i18n.str("all_categories"); //$NON-NLS-1$
	
	public static final String ENTRIES_TAB = i18n.str("entries"); //$NON-NLS-1$
	public static final String ITEMS_TAB = i18n.str("feed_items"); //$NON-NLS-1$
	public static final int WEBLOG_TAB = 0;
	public static final int FEED_TAB = 1;
	
	private JTabbedPane tabs;
	
	private JTextField entryField;
	private JTextField titleField;
	private JCalendarComboBox fromField, toField;
	private JComboBox catBox, postTypeBox, dateTypeBox, weblogBox;
	private JButton findButton;
	private JButton closeButton;
	private JLabel status = new JLabel(i18n.str("ready")); //$NON-NLS-1$
	private WeblogList weblogList;
	private Weblog selWeblog;
	
	//private JButton folderBrowseButton;
	private JCheckBox includeSubsCb;
	private JTextField folderField;
	private JTextField feedDescField;
	private JTextField feedTitleField;
	private JCalendarComboBox feedFromField, feedToField;
	
	private FeedFolder rootFolder;
	private FeedFolder selFolder;
	
	private CategoryListener catListener = new CategoryChangeHandler();
	
	public TBSearchDialog(Frame owner, WeblogList list, FeedFolder root)
	{
		super(owner, false);
		init(list, root);  
	}
	
	public TBSearchDialog(Dialog owner, WeblogList list, FeedFolder root)
	{
		super(owner, false);
		init(list, root);
	}
	
	private void init(WeblogList list, FeedFolder root)
	{
		rootFolder = root;
		selFolder = root;		
		
		weblogList = list;
		
		setTitle(i18n.str("search")); //$NON-NLS-1$
		status.setBorder(new BevelBorder(BevelBorder.LOWERED));
		status.setHorizontalAlignment(SwingConstants.LEFT);
		
		String initialCats[] = null;
		if(weblogList.getWeblogCount() > 0)
		{		
			selWeblog = weblogList.getWeblogAt(0);
			try
			{
				initialCats = selWeblog.getCategories();
				
			}catch(Exception ex){}
		}
		
		weblogBox = new JComboBox(weblogList.getWeblogs());
		weblogBox.setSelectedItem(selWeblog);
		weblogBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selWeblog = (Weblog)weblogBox.getSelectedItem();
				try
				{
					catBox.removeAllItems();
					String c[] = selWeblog.getCategories();
					for(int i = 0; i < c.length; i++)
						catBox.addItem(c[i]);
					catBox.addItem(ALL_CATS);						
				}
				catch(Exception ex){}
			}
		});
		
		
		if(initialCats != null)
			catBox = new JComboBox(initialCats);
		else
			catBox = new JComboBox();
		catBox.setMaximumRowCount(5);
		catBox.addItem(ALL_CATS);
        
		postTypeBox = new JComboBox();
		postTypeBox.addItem(POSTS);
		postTypeBox.addItem(DRAFTS);
        
		entryField = new JTextField(25);
		titleField = new JTextField(25);
		feedDescField = new JTextField(25);
		feedTitleField = new JTextField(25);
		folderField = new JTextField();
		folderField.setText(selFolder.getName());
		folderField.setEditable(false);		
		includeSubsCb = new JCheckBox(i18n.str("include_subfolders")); //$NON-NLS-1$
		JButton folderBrowseButton = new JButton("..."); //$NON-NLS-1$
		folderBrowseButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				FolderChooserDialog d = new FolderChooserDialog(TBSearchDialog.this, rootFolder);
				d.setSize(250, 250);
				d.setLocationRelativeTo(TBSearchDialog.this);
				d.setVisible(true);
				if(!d.hasUserCancelled())
				{				
					selFolder = d.getSelectedFolder();
					folderField.setText(selFolder.getName());
				}
			}
		});        

        
		dateTypeBox = new JComboBox();
		dateTypeBox.addItem(CREATED);
		dateTypeBox.addItem(MODIFIED);
        
        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
        
		toField = new JCalendarComboBox();
        toField.setDateFormat(format);        
		feedToField = new JCalendarComboBox();
        feedToField.setDateFormat(format);
		Calendar cal = Calendar.getInstance();
		toField.setDate(cal.getTime());
		feedToField.setDate(new Date(cal.getTime().getTime()));
        
		//roll back the "from" date 1 year 
		fromField = new JCalendarComboBox();
        fromField.setDateFormat(format);
		feedFromField = new JCalendarComboBox();
        feedFromField.setDateFormat(format);
		cal.add(Calendar.YEAR, -1);
		fromField.setDate(cal.getTime()); 
		feedFromField.setDate(new Date(cal.getTime().getTime()));       
        
        
		findButton = new JButton(i18n.str("find")); //$NON-NLS-1$
		findButton.setMnemonic('F');
		closeButton = new JButton(i18n.str("close"));		 //$NON-NLS-1$
		closeButton.setMnemonic('C');
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
        
		//create the entry finder panel
		LabelledItemPanel lip = new LabelledItemPanel();
		JPanel p = new JPanel(new BorderLayout());		
		p.add(weblogBox, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(i18n.str("in_site"), weblogBox);		 //$NON-NLS-1$
		p = new JPanel(new BorderLayout());
		p.add(postTypeBox, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);		
		lip.addItem(i18n.str("find"), p);		 //$NON-NLS-1$
		lip.addItem(i18n.str("title_contains"), titleField); //$NON-NLS-1$
		lip.addItem(i18n.str("body_contains"), entryField); //$NON-NLS-1$
		lip.addItem(i18n.str("in_category"), catBox); //$NON-NLS-1$
        
		Box datePanel = Box.createHorizontalBox();
		datePanel.add(Box.createHorizontalStrut(10));
		datePanel.add(dateTypeBox);
		datePanel.add(Box.createHorizontalStrut(5));
		datePanel.add(new JLabel(" > ")); //$NON-NLS-1$
		datePanel.add(Box.createHorizontalStrut(5));
		datePanel.add(fromField);
		datePanel.add(new JLabel(" - ")); //$NON-NLS-1$
		datePanel.add(toField);
		datePanel.add(Box.createHorizontalStrut(10));
        
		JPanel weblogFindPanel = new JPanel(new BorderLayout());
		weblogFindPanel.add(lip, BorderLayout.CENTER);
		weblogFindPanel.add(datePanel, BorderLayout.SOUTH);	
		
		
		//create the item finder panel
		lip = new LabelledItemPanel();
		p = new JPanel(new BorderLayout());		
		p.add(folderField, BorderLayout.CENTER);
		p.add(folderBrowseButton, BorderLayout.EAST);
		lip.addItem(i18n.str("look_in_folder"), p); //$NON-NLS-1$
		lip.addItem("", includeSubsCb); //$NON-NLS-1$
		lip.addItem(i18n.str("title_contains"), feedTitleField); //$NON-NLS-1$
		lip.addItem(i18n.str("body_contains"), feedDescField); //$NON-NLS-1$
		datePanel = Box.createHorizontalBox();
		datePanel.add(Box.createHorizontalStrut(5));
		datePanel.add(feedFromField);
		datePanel.add(new JLabel(" - ")); //$NON-NLS-1$
		datePanel.add(feedToField);
		datePanel.add(Box.createHorizontalStrut(10));
		lip.addItem(i18n.str("updated_between"), datePanel); //$NON-NLS-1$
		JPanel feedFindPanel = lip;
        
		//JPanel bottomPanel = new JPanel(new BorderLayout());
		//bottomPanel.add(tablePane, BorderLayout.CENTER);
		//bottomPanel.add(status, BorderLayout.SOUTH);
        
		JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		buttonPanel.add(findButton);
		buttonPanel.add(closeButton);
		JPanel paddedButtonPanel = new JPanel();
		paddedButtonPanel.add(buttonPanel);
        
        
        
		tabs = new JTabbedPane();
		tabs.addTab(ENTRIES_TAB, weblogFindPanel);
		tabs.addTab(ITEMS_TAB, feedFindPanel);
		tabs.setBorder(new EmptyBorder(0, 8, 12, 0));
		tabs.addChangeListener(new javax.swing.event.ChangeListener()
		{
			public void stateChanged(javax.swing.event.ChangeEvent e)
			{
				if(tabs.getSelectedIndex() == WEBLOG_TAB)
					findButton.setEnabled(weblogList.getWeblogCount() > 0);
				else
					findButton.setEnabled(true);			
			}
		});
        
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(tabs, BorderLayout.CENTER);
		topPanel.add(paddedButtonPanel, BorderLayout.EAST);
		
        
		getContentPane().setLayout(new BorderLayout());		
		getContentPane().add(topPanel, BorderLayout.CENTER);
		getContentPane().add(status, BorderLayout.SOUTH);
		pack();
		setSize(480, getSize().height);
		setResizable(false);
		
		//add listeners
		setWeblogList(weblogList);
	}
	
	public void setWeblogList(WeblogList list)
	{
		if(weblogList != null)
		{
		    //remove listeners
		    for(int i = 0; i < weblogList.getWeblogCount(); i++)
		        weblogList.getWeblogAt(i).removeCategoryListener(catListener);
		}
			
		weblogList = list;
		weblogBox.removeAllItems();
		boolean enabled = weblogList.getWeblogCount() > 0;
			
		for(int i = 0; i < weblogList.getWeblogCount(); i++)
		{
			Weblog w = weblogList.getWeblogAt(i);
			//add listeners to the new list
			w.addCategoryListener(catListener);
			weblogBox.addItem(w);			
		}		
	
		entryField.setEditable(enabled);
		titleField.setEditable(enabled);
		postTypeBox.setEnabled(enabled);
		weblogBox.setEnabled(enabled);
		dateTypeBox.setEnabled(enabled);
		catBox.setEnabled(enabled);
		fromField.setEnabled(enabled);
		toField.setEnabled(enabled);
		if(tabs.getSelectedIndex() == WEBLOG_TAB)
			findButton.setEnabled(enabled);				
	}
	
	public void setRootFeedFolder(FeedFolder root)
	{
		rootFolder = selFolder = root;
		folderField.setText(selFolder.getName());
	}
	
	public void addActionListener(ActionListener al)
	{
		findButton.addActionListener(al);
	}
	
	public void removeActionListener(ActionListener al)
	{
		findButton.removeActionListener(al);
	}
	
	public int getCurrentSearchTab()
	{
		if(tabs.getSelectedIndex() == 0)
			return WEBLOG_TAB;
		else
			return FEED_TAB;			
	}
	
	public void setCurrentSearchTab(int tab)
	{
		if(tab == FEED_TAB || tab == WEBLOG_TAB)
			tabs.setSelectedIndex(tab);
	}
	
	public WeblogSearch getWeblogSearch()
	{
		WeblogSearch search = new WeblogSearch();
		if(titleField.getText() != null && !titleField.getText().equals("")) //$NON-NLS-1$
			search.setTitleContains(titleField.getText());
		if(entryField.getText() != null && !entryField.getText().equals("")) //$NON-NLS-1$
			search.setBodyContains(entryField.getText());
		
		if(!catBox.getSelectedItem().toString().equals(ALL_CATS))
			search.setCategory(catBox.getSelectedItem().toString());
		
		search.setFindDrafts(postTypeBox.getSelectedItem().toString().equals(DRAFTS));
		search.setFindModifiedEntries(
			dateTypeBox.getSelectedItem().toString().equals(MODIFIED));
		
		search.setStartDate(fromField.getDate());
		search.setEndDate(toField.getDate());		
		return search;
	}
	
	public FeedSearch getFeedSearch()
	{
		FeedSearch search = new FeedSearch();
		if(feedTitleField.getText() != null && !feedTitleField.getText().equals("")) //$NON-NLS-1$
			search.setTitleContains(feedTitleField.getText());
		
		if(feedDescField.getText() != null && !feedDescField.getText().equals("")) //$NON-NLS-1$
			search.setDescriptionContains(feedDescField.getText());
		
		search.setStartRetrievedDate(feedFromField.getDate());
		search.setEndRetrievedDate(feedToField.getDate());		
		return search;
	}
	
	public boolean isIncludeSubfolders()
	{
		return includeSubsCb.isSelected();
	}
	
	public FeedFolder getSelectedFeedFolder()
	{
		return selFolder;
	}
	
	public Weblog getSelectedWeblog()
	{
		return selWeblog;
	}
	
	public BlogEntry[] performWeblogSearch() throws BackendException
	{
		BlogEntry be[] = selWeblog.findEntries(getWeblogSearch());
		String str = i18n.str("entries_found") + ": " + be.length; //$NON-NLS-1$ //$NON-NLS-2$
		status.setText(str);
		return be;
	}
	
	public FeedItem[] performFeedSearch() throws FeedBackendException
	{
		FeedItem items[] = selFolder.findItems(getFeedSearch(), includeSubsCb.isSelected());
		String str = i18n.str("items_found") + ": " + items.length; //$NON-NLS-1$ //$NON-NLS-2$
		status.setText(str);
		return items;
	}
	
	private class CategoryChangeHandler implements CategoryListener
	{
		public void categoryAdded(CategoryEvent e)
		{
			updateCatCombo(e);
		}
		
		public void categoryRenamed(CategoryEvent e)
		{
			updateCatCombo(e);
		}
		
		public void categoryRemoved(CategoryEvent e)
		{
			updateCatCombo(e);
		}
		
		private void updateCatCombo(CategoryEvent e)
		{
			Weblog cur = (Weblog)weblogBox.getSelectedItem();
			if(e.getWeblog() == cur)
			{
				catBox.removeAllItems();
				try
				{				
					String cats[] = cur.getCategories();
					for(int i = 0; i < cats.length; i++)
					{
						catBox.addItem(cats[i]);
					}
				}
				catch(Exception ex){}
			}
		}
	}
	
	private class FolderChooserDialog extends StandardDialog
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private JTree tree;
		private FeedTreeModel model;
		
		public FolderChooserDialog(Dialog owner, FeedFolder root)
		{
			super(owner, i18n.str("select_folder")); //$NON-NLS-1$
			model = new FeedTreeModel(root, true);
			tree = new JTree(model);
			tree.setCellRenderer(new FeedTreeCellRenderer());
			JPanel p = new JPanel(new BorderLayout());
			p.add(new JScrollPane(tree));
			setContentPane(p);		
		}
		
		public boolean isValidData()
		{
			if(tree.getSelectionPath() == null)
				return false;
			return true;
		}
		
		public FeedFolder getSelectedFolder()
		{
			return (FeedFolder)tree.getLastSelectedPathComponent();
		}
	}
}
