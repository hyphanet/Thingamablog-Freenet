/*
 * Created on Apr 29, 2004
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


import net.sf.thingamablog.*;
import net.sf.thingamablog.blog.*;
import net.sf.thingamablog.feed.*;
import net.sf.thingamablog.backend.*;
import net.sf.thingamablog.gui.*;
import net.sf.thingamablog.gui.GUILoginPrompt;
import net.sf.thingamablog.gui.UpdatableAction;
import net.sf.thingamablog.gui.ViewerPane;
import net.sf.thingamablog.gui.editor.*;
import net.sf.thingamablog.xml.*;
import net.sf.thingamablog.gui.properties.*;
import net.sf.thingamablog.gui.table.*;
import net.sf.thingamablog.transport.LoginFactory;
import com.Ostermiller.util.Browser;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.Options;
import com.jgoodies.plaf.plastic.PlasticLookAndFeel;



/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class ThingamablogFrame extends JFrame
{
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.gui.app");
    
    private final JFrame FRAME = this;
	private static final Image ICON = 
		Utils.createIcon(TBGlobals.RESOURCES + "ticon.gif").getImage();
    
	private Dimension editorSize = new Dimension(640, 480);
	//private boolean isEditorWordWrap = true;
		
	//publish type constants 
	private static int PUB_CURRENT = 0;
	private static int PUB_CHANGED = 1;
	private static int PUB_ALL = 2;
	private static int PING_ONLY = 3;
	
	private static final int ITEM_VIEW = -1;
	private static final int ENTRY_VIEW = -2;
	private int tableView = ENTRY_VIEW;	

	private int curLayoutStyle = TBGlobals.getLayoutStyle();	
	private JPanel contentPanel;
	private JPanel blogTreePanel;
	private JPanel feedTreePanel;
	private JSplitPane tableViewerDivider;
	private JSplitPane hSplitPane;	
	private JSplitPane feedSplitPane;
	
	private JPanel viewerPanel;
	private CardLayout viewerPanelLayout = new CardLayout();
	private HTMLOptionsPane htmlOptionsPane;
	private final String HTML_VIEW = "HTML_VIEW";
	private final String TABLE_VIEW = "TABLE_VIEW";
	
	private JTree blogTree;
	private JTree feedTree;
	private WeblogTreeModel weblogTreeModel;
	private FeedTreeModel feedTreeModel;
	
	private ViewerPane viewerPane;
	private ViewerPaneModel viewerPaneModel = new TBViewerPaneModel();
	private JSortTable table;
	private TBTableColumnModel tableColumnModel = new TBTableColumnModel();
	private WeblogTableModel blogTableModel = new WeblogTableModel();	
	private FeedTableModel feedTableModel = new FeedTableModel();	
	
	private boolean sortAscending;             //keeps track of the menu sort order
	//private JMenu sortMenuItem;              //menu item that contains the sort menu
	private JMenu sortMenu;                    //menu to sort the table cols
	
	private HSQLDatabaseBackend backend;
	
	private JPopupMenu blogTreePopup = null;
	private JPopupMenu feedTreePopup = null;
	private JPopupMenu tablePopup = null;
	private JPopupMenu viewerPopup = null;
	
	private StatusBar statusBar;
	
	private WeblogList weblogList = new WeblogList();
		
	private UpdateMonitor updateMonitor = new UpdateMonitor();
	//toolbar button for starting/cancelling feed updates
	private JButton updateButton = new JButton(); 
	
	private Action openDBAction;
	private Action newDBAction;
	
	private UpdateAllFeedsAction updateAllFeedsAction;
	private Action updateCurFeedAction;
	private Action updateFolderAction;
	private Action renameFeedFolderAction;
	private Action deleteFromFeedTreeAction;
	private Action markCurFeedReadAction;
	private Action markCurFeedUnreadAction;
	private Action markSelItemsReadAction;
	private Action markSelItemsUnreadAction;
	private Action newFeedFolderAction;
	private Action newFeedAction;
	private Action feedPropertiesAction;
	private Action importFeedFolderAction;
	private Action exportFeedFolderAction;
	
	private Action newEntryAction;
	private Action newEntryFromItemAction;
	private Action editEntryAction;
	private Action deleteEntriesAction;
	private Action weblogPropertiesAction;
	private Action editTemplateAction;
	private Action exportWeblogToRSSAction;
	private Action importEntriesFromFeedAction;
	private Action importLegacyWeblogAction;
	
	private Action newWeblogAction;
	private Action deleteWeblogAction;
	
	private Action importFileAction;
	private Action newWebFolderAction;
	private Action renameFileAction;
	private Action deleteFileAction;
	private Action openFileAction;
	
	private Action nextAction;
	private Action prevAction;
	private Action nextUnreadAction;
	
	private Action searchAction;
	private Action findEntriesAction;
	private Action findItemsAction;
	
	private Action publishAction;
	private Action publishAllAction;
	private Action weblogPingAction;
	
	private Action viewWeblogAction;
	
	private Vector actions = new Vector(20, 5);	
	
	private FeedTreeMoveHandler moveHandler;
	private FeedFolder feedRoot = new FeedFolder(Messages.getString("ThingamablogFrame.My_Subscriptions")); //$NON-NLS-1$
	private Feed lastSelFeed;
	private Weblog curSelWeblog;
	private Weblog curViewWeblog;
	
	private TBSearchDialog searchDialog;
	
	private File curDB;
	private boolean isDBOpen;
	private boolean	isAppOpen = false;
	
	private javax.swing.Timer feedUpdateTimer;
	
	public ThingamablogFrame()
	{				
	    Runtime.getRuntime().addShutdownHook(new MyShutdownHook());
	    TBGlobals.loadProperties();
		tableColumnModel.loadColumnData();
		
		setLookAndFeel(TBGlobals.getLookAndFeelClassName());		 
		
		//give the user something to look at while we're loading the app
		if(TBGlobals.isStartWithSplash())
		{
			JSplash ss = new JSplash(this, TBGlobals.RESOURCES + "splash.gif", 6000);			 //$NON-NLS-1$
			ss.setVisible(true);
		}
		
		setIconImage(ICON);
		initActions();	
		
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				closeApp(true);
			}
		});
		
		//init components
		contentPanel = new JPanel(new BorderLayout());
		blogTreePanel = new JPanel(new BorderLayout());
		feedTreePanel = new JPanel(new BorderLayout());
		tableViewerDivider = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		hSplitPane = new JSplitPane();	
		feedSplitPane = new JSplitPane();		
		viewerPane = new ViewerPane();
		viewerPane.setModel(viewerPaneModel);
		statusBar = new StatusBar();		
		
		table = new JSortTable();
		table.setColumnModel(tableColumnModel);
		table.setModel(blogTableModel);
		tableView = ENTRY_VIEW;		
		table.getSelectionModel().addListSelectionListener(new TableSelectionHandler());
		table.addMouseListener(new PopupMenuListener());
		table.setColumnSelectionAllowed(false);
		table.setShowGrid(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setIntercellSpacing(new Dimension(0, 0));        
        table.addMouseListener(new TableClickHandler());
		
		FocusListener focusListener = new TreeFocusListener();
		weblogTreeModel = new WeblogTreeModel(weblogList);
		blogTree = new JTree(weblogTreeModel);
		blogTree.setCellRenderer(new WeblogTreeCellRenderer());
		blogTree.addTreeSelectionListener(new TreeSelectionHandler());
		blogTree.addMouseListener(new PopupMenuListener());
		blogTree.addMouseListener(new BlogTreeClickHandler());
		blogTreePanel.add(new JScrollPane(blogTree));
		blogTree.addFocusListener(focusListener);		

		feedTreeModel = new FeedTreeModel(feedRoot);
		feedTree = new JTree(feedTreeModel);
		feedTree.setCellRenderer(new FeedTreeCellRenderer());
		feedTree.addTreeSelectionListener(new TreeSelectionHandler());
		feedTree.addMouseListener(new PopupMenuListener());
		moveHandler = new FeedTreeMoveHandler(feedTree, this);
		
		JToolBar feedToolBar = new JToolBar(JToolBar.HORIZONTAL);
		feedToolBar.setFloatable(false);
		feedToolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //$NON-NLS-1$
		Utils.addToolbarButton(feedToolBar, newFeedFolderAction);
		Utils.addToolbarButton(feedToolBar, newFeedAction);
		Utils.addToolbarButton(feedToolBar, deleteFromFeedTreeAction);		
		JPanel feedUpperPanel = new JPanel(new BorderLayout());
		JLabel feedUpperPanelLabel = new JLabel(Messages.getString("ThingamablogFrame.News_Feeds")); //$NON-NLS-1$
		feedUpperPanelLabel.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "news_feeds.png"));
		feedUpperPanelLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));		
		feedUpperPanel.add(feedUpperPanelLabel, BorderLayout.CENTER);
		feedUpperPanel.add(feedToolBar, BorderLayout.EAST);
		feedTreePanel.add(feedUpperPanel, BorderLayout.NORTH);
		feedTreePanel.add(new JScrollPane(feedTree), BorderLayout.CENTER);
		feedTree.addFocusListener(focusListener);		
		
		viewerPane.getJEditorPane().addMouseListener(new PopupMenuListener());
		viewerPopup = new JPopupMenu();
		viewerPopup.add(viewerPane.getCopyAction());
		viewerPopup.add(viewerPane.getSelectAllAction());

		//Backackground has to be set to the table color		
		JScrollPane scroller = new JScrollPane(table);
		scroller.getViewport().setBackground(table.getBackground()); 
		scroller.setHorizontalScrollBarPolicy(
			  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		//setAutoResizeMode() has to be called after the table is added
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
		tableViewerDivider.setTopComponent(scroller);		
		tableViewerDivider.setBottomComponent(viewerPane);		

		htmlOptionsPane = new HTMLOptionsPane();
		viewerPanel = new JPanel(viewerPanelLayout);
		viewerPanel.add(tableViewerDivider, TABLE_VIEW);
		viewerPanel.add(htmlOptionsPane, HTML_VIEW);
		viewerPanelLayout.show(viewerPanel, HTML_VIEW);
		

		layoutContentPanel(TBGlobals.getLayoutStyle());		
		
		getContentPane().add(createToolBar(), BorderLayout.NORTH);		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		setJMenuBar(createMenuBar());
		
		//searchDialog = new TBSearchDialog(this, weblogList, feedRoot);
		//searchDialog.addActionListener(new FindHandler());		
		
		loadProperties();
		updateActions();
		
		feedUpdateTimer = new javax.swing.Timer(
		        TBGlobals.getFeedUpdateInterval(), new TimerHandler());	
		
		if(TBGlobals.isStartWithLastDatabase() && 
		TBGlobals.getLastOpenedDatabase() != null)
		{
			openDB(new File(TBGlobals.getLastOpenedDatabase()));	
		}
		else		
		    blogTree.setSelectionRow(0);
		
		isAppOpen = true;
	}
	
	private void setLookAndFeel(String className)
	{
		try
		{   
		    //Bug fix: Non-western chars can't display with the default
		    //plastic theme, so a custom theme needs to be set here
		    if(className.equals(Options.getCrossPlatformLookAndFeelClassName()))
		    {		        	        
		        if(LookUtils.IS_OS_WINDOWS_XP)
		        {
		            PlasticLookAndFeel.setMyCurrentTheme(new ExperiencedBlue());		            
		        }
		        else if(LookUtils.IS_OS_WINDOWS)
		        {
		            PlasticLookAndFeel.setMyCurrentTheme(
		                    new com.jgoodies.plaf.plastic.theme.SkyBluer());
		        }		        
		    }
		    
		    UIManager.setLookAndFeel(className);
		}
		catch(Exception ex)
		{
		    logger.log(Level.WARNING, ex.getMessage(), ex);
		    System.err.println(Messages.getString("ThingamablogFrame.invalid_laf_prompt") + className); //$NON-NLS-1$
		}	
	}
	
	private void layoutContentPanel(int style)
	{		
		contentPanel.removeAll();		
		if(style == TBGlobals.THREE_COL)
		{
			
			feedSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			//feedSplitPane.setLeftComponent(tableViewerDivider);
			feedSplitPane.setLeftComponent(viewerPanel);
			feedSplitPane.setRightComponent(feedTreePanel);		
			
			hSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			hSplitPane.setLeftComponent(blogTreePanel);
			hSplitPane.setRightComponent(feedSplitPane);			
			hSplitPane.setDividerLocation(200);
			feedSplitPane.setDividerLocation(getWidth() - 300);		
			
		}
		else
		{			
			feedSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			feedSplitPane.setTopComponent(blogTreePanel);
			feedSplitPane.setBottomComponent(feedTreePanel);
			feedSplitPane.setOneTouchExpandable(true);
			feedSplitPane.setDividerLocation(contentPanel.getHeight() / 2);
			JPanel leftPanel = new JPanel(new BorderLayout());
			leftPanel.add(feedSplitPane);	
			
			hSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			hSplitPane.setLeftComponent(leftPanel);
			//hSplitPane.setRightComponent(tableViewerDivider);
			hSplitPane.setRightComponent(viewerPanel);
			hSplitPane.setDividerLocation(200);			
		}
		
		feedSplitPane.setOneTouchExpandable(true);
		hSplitPane.setOneTouchExpandable(true);
		contentPanel.add(hSplitPane);
		hSplitPane.resetToPreferredSizes();
		repaint();		
	}
	
	/**
	 * Overriden from super class to check if this is the first run
	 */
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		
		if(b)
			checkIfFirstRun();
	}
	
	private void checkIfFirstRun()
	{
		File f = new File(TBGlobals.PROP_FILE);
		//check if the properties file exists.
		//If it doesn't then this is likely the first run
		if(f.exists()) //not the first run so just return
			return;
		
		//This is the first run
		String msg = 
		Messages.getString("ThingamablogFrame.first_run_prompt"); //$NON-NLS-1$
		
		int r = JOptionPane.showConfirmDialog(
			FRAME, msg, Messages.getString("ThingamablogFrame.Confirm"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
		
		if(r == JOptionPane.YES_OPTION)
			createNewDatabase();				
	}
	
	private void createNewDatabase()
	{
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(Messages.getString("ThingamablogFrame.New_Database_Title")); //$NON-NLS-1$
		int r = fc.showSaveDialog(FRAME);
		if(r == JFileChooser.CANCEL_OPTION)
			return;
        
		File dir = fc.getSelectedFile();
        System.out.println(dir);
		if(dir == null || dir.isFile() || !dir.exists())
		{				
			dir = fc.getCurrentDirectory();
		}
			
		File xml = new File(dir, TBGlobals.USER_XML_FILENAME);
		if(xml.exists())
		{
			JOptionPane.showMessageDialog(FRAME, 
				Messages.getString("ThingamablogFrame.db_exists_prompt"), Messages.getString("ThingamablogFrame.Warning"), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.WARNING_MESSAGE);
			return;				
		}
		
		File tmplXML = new File(TBGlobals.DEFAULT_TMPL_DIR, TBGlobals.USER_XML_FILENAME);	
			
		Utils.copyFile(tmplXML.getAbsolutePath(), xml.getAbsolutePath());
		openDB(dir);
	}
	
	private void openDB(final File dir)
	{		
		final File userXML = new File(dir, TBGlobals.USER_XML_FILENAME);
        
        SwingWorker worker = new SwingWorker() 
		{
			public Object construct() 
			{				
				try
				{				
				    isDBOpen = false;
				    if(backend != null)
				        backend.shutdown();
				    else
				        backend = new HSQLDatabaseBackend();			
				    	
				    curDB = dir;
				    backend.connectToDB(curDB);			
				    //String xmlPath = curDB.getAbsolutePath() + 
					//	TBGlobals.SEP + TBGlobals.USER_XML_FILENAME;
                    String xmlPath = userXML.getAbsolutePath();
			
				    feedRoot = new FeedFolder(
				            Messages.getString("ThingamablogFrame.My_Subscriptions")); //$NON-NLS-1$
				    weblogList = new WeblogList();
			
				    System.out.println("LOADING DATA");
				    TBPersistFactory.loadData(xmlPath, weblogList, feedRoot, backend, backend);
				    System.out.println("DONE LOADING DATA");
				    isDBOpen = true;				    
				
				}
				catch(Exception ex)
				{
				    logger.log(Level.WARNING, ex.getMessage(), ex);
				    ex.printStackTrace();				    
				    return ex;
				}
				
				return new Boolean(isDBOpen);
			}
			
			public void finished()
			{

			    Object obj = get();
			    if(obj instanceof Exception)
			    {
			        Exception exc = (Exception)obj;
			        JDialog d = new ErrorDialog(
			            FRAME, "Error", "Unable to open database", exc);
			        d.setLocationRelativeTo(FRAME);
			        d.setVisible(true);
			    }
				else if(obj == null || obj.toString().equals(false + ""))
				{
				    Utils.errMsg(FRAME, "Unable to open database", null);
				    //return;
				}
			    feedTreeModel = new FeedTreeModel(feedRoot);
				weblogTreeModel = new WeblogTreeModel(weblogList);			
						
				blogTree.setModel(weblogTreeModel);			
				feedTree.setModel(feedTreeModel);
				blogTree.expandRow(0);
				
				//Select previously selected weblog
				String key = TBGlobals.getProperty("LAST_SEL_BLOG");
				Weblog foundBlog = null;
				for(int i = 0; i < weblogList.getWeblogCount(); i++)
				{
				    if(weblogList.getWeblogAt(i).getKey().equals(key))
				    {
				        foundBlog = weblogList.getWeblogAt(i);
				        break;
				    }
				}
				
				if(foundBlog != null)
				{
				    selectWeblog(foundBlog);
				    int r[] = blogTree.getSelectionRows();
				    blogTree.expandRow(r[0]);
				    blogTree.setSelectionRow(r[0] + 1);				    
				}
				else
				    blogTree.setSelectionRow(0);//select root node (My Sites)
				//blogTree.expandRow(1);
				//blogTree.setSelectionRow(2);
			
				if(searchDialog != null)
				{			
					searchDialog.setWeblogList(weblogList);			
					searchDialog.setRootFeedFolder(feedRoot);
				}
			
				if(TBGlobals.isStartWithLastDatabase())
				{
					TBGlobals.setLastOpenedDatabase(curDB.getAbsolutePath());	
				}				
				
				//isDBOpen = true;
				updateActions();
			    FRAME.getGlassPane().setVisible(false);
			    FRAME.getGlassPane().setCursor(Cursor.getDefaultCursor());
			    if(TBGlobals.isAutoFeedUpdate())
			    {
			        feedUpdateTimer.start();
			    }
			}
		};
		
        isDBOpen = false;
		if(userXML.exists())
        {
		    getGlassPane().setVisible(true);
		    getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    blogTree.clearSelection();
		    feedTree.clearSelection();
		    feedUpdateTimer.stop();
		    worker.start();	
        }
	}
	
	private void initActions()
	{
		openDBAction = new OpenDatabaseAction();
		actions.add(openDBAction);		
		newDBAction = new NewDatabaseAction();
		actions.add(newDBAction);
		
		updateAllFeedsAction = new UpdateAllFeedsAction();
		actions.add(updateAllFeedsAction);
		updateCurFeedAction = new UpdateCurrentFeedAction();
		actions.add(updateCurFeedAction);
		updateFolderAction = new UpdateFolderAction();
		actions.add(updateFolderAction);
		renameFeedFolderAction = new RenameFeedFolderAction();
		actions.add(renameFeedFolderAction);
		deleteFromFeedTreeAction = new DeleteFromFeedTreeAction();
		actions.add(deleteFromFeedTreeAction);
		markCurFeedReadAction = new MarkCurrentFeedAction(true);
		actions.add(markCurFeedReadAction);
		markCurFeedUnreadAction = new MarkCurrentFeedAction(false);
		actions.add(markCurFeedUnreadAction);
		markSelItemsReadAction = new MarkSelectedItemsAction(true);
		actions.add(markSelItemsReadAction);
		markSelItemsUnreadAction = new MarkSelectedItemsAction(false);
		actions.add(markSelItemsUnreadAction);
		newFeedFolderAction = new NewFeedFolderAction();
		actions.add(newFeedFolderAction);
		newFeedAction = new NewFeedAction();
		actions.add(newFeedAction);
		feedPropertiesAction = new FeedPropertiesAction();
		actions.add(feedPropertiesAction);
		importFeedFolderAction = new ImportFeedFolderAction();
		actions.add(importFeedFolderAction);
		exportFeedFolderAction = new ExportFeedFolderAction();
		actions.add(exportFeedFolderAction);
		
		newEntryAction = new NewEntryAction();
		actions.add(newEntryAction);
		newEntryFromItemAction = new NewEntryFromItemAction();
		actions.add(newEntryFromItemAction);
		editEntryAction = new EditEntryAction();
		actions.add(editEntryAction);
		deleteEntriesAction = new DeleteEntriesAction();
		actions.add(deleteEntriesAction);
		exportWeblogToRSSAction = new ExportWeblogToRSSAction();
		actions.add(exportWeblogToRSSAction);
		importEntriesFromFeedAction = new ImportEntriesFromFeedAction();
		actions.add(importEntriesFromFeedAction);
		importLegacyWeblogAction = new ImportLegacyWeblogAction();
		actions.add(importLegacyWeblogAction);
		
		publishAction = new PublishAction();
		actions.add(publishAction);
		publishAllAction = new PublishAllAction();
		actions.add(publishAllAction);
		weblogPingAction = new WeblogPingAction();
		actions.add(weblogPingAction);
		viewWeblogAction = new ViewWeblogAction();
		actions.add(viewWeblogAction);
		
		weblogPropertiesAction = new WeblogPropertiesAction();
		actions.add(weblogPropertiesAction);
		newWeblogAction = new NewWeblogAction();
		actions.add(newWeblogAction);
		deleteWeblogAction = new DeleteWeblogAction();
		actions.add(deleteWeblogAction);
		editTemplateAction = new EditTemplateAction();
		actions.add(editTemplateAction);
		
		nextAction = new NextAction();
		actions.add(nextAction);
		prevAction = new PreviousAction();
		actions.add(prevAction);
		nextUnreadAction = new NextUnreadAction();
		actions.add(nextUnreadAction);
		
		searchAction = new SearchAction();
		actions.add(searchAction);
		findEntriesAction = new SearchAction(TBSearchDialog.WEBLOG_TAB);
		actions.add(findEntriesAction);
		findItemsAction = new SearchAction(TBSearchDialog.FEED_TAB);
		actions.add(findItemsAction);
		
		importFileAction = new ImportFileAction();
		actions.add(importFileAction);
		newWebFolderAction = new NewWebFolderAction();
		actions.add(newWebFolderAction);
		deleteFileAction = new DeleteFileAction();
		actions.add(deleteFileAction);
		renameFileAction = new RenameFileAction();
		actions.add(renameFileAction);
		openFileAction = new OpenFileAction();
		actions.add(openFileAction);
		actions.trimToSize();
	}
	
	private void updateActions()
	{
		Enumeration eEnum = actions.elements();
		
		//disable all actions if a DB isn't open
		if(curDB == null || !isDBOpen)
		{
			while(eEnum.hasMoreElements())
			{
				Action a = (Action)eEnum.nextElement();
				a.setEnabled(false);
			}
		}
		else
		{			
			while(eEnum.hasMoreElements())
			{
				Action a = (Action)eEnum.nextElement();
				if(a instanceof UpdatableAction)
					((UpdatableAction)a).update();
				else
					a.setEnabled(true);
			}
		}
		
		//Always enabled
		newDBAction.setEnabled(true);
		openDBAction.setEnabled(true);
	}
	
	private JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //$NON-NLS-1$
				
		JButton b = Utils.addToolbarButton(toolBar, newWeblogAction);			
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "new_weblog.png")); //$NON-NLS-1$
		toolBar.addSeparator();		
		
		b = Utils.addToolbarButton(toolBar, newEntryAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "new_entry.png")); //$NON-NLS-1$
		
		b = Utils.addToolbarButton(toolBar, deleteEntriesAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "delete_entries.png")); //$NON-NLS-1$
		
		toolBar.addSeparator();
		
		b  = Utils.addToolbarButton(toolBar, searchAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "find.png")); //$NON-NLS-1$
		
		toolBar.addSeparator();
		
		//Got to use an instance var for the button, so we can access it later
		//update button changes its state when updating
		updateButton = Utils.addToolbarButton(toolBar, updateAllFeedsAction);
		updateButton.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "update_feeds.png")); //$NON-NLS-1$
		
		toolBar.addSeparator();
		
		b = Utils.addToolbarButton(toolBar, publishAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "publish24.gif"));		 //$NON-NLS-1$
		
		b = Utils.addToolbarButton(toolBar, publishAllAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "publish_all24.gif")); //$NON-NLS-1$
		
		b = Utils.addToolbarButton(toolBar, weblogPingAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "ping.png")); //$NON-NLS-1$
		
		b = Utils.addToolbarButton(toolBar, viewWeblogAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "view.png")); //$NON-NLS-1$
		
		toolBar.addSeparator();
		
		b = Utils.addToolbarButton(toolBar, weblogPropertiesAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "properties24.gif")); //$NON-NLS-1$
		
		toolBar.addSeparator();
		
		b = Utils.addToolbarButton(toolBar, nextAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "next.png")); //$NON-NLS-1$
		
		b = Utils.addToolbarButton(toolBar, prevAction);
		b.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "prev.png"));	 //$NON-NLS-1$
		
		return toolBar;
	}
	
	private JMenuBar createMenuBar()
	{
		JMenuBar mb = new JMenuBar();
		
		//create the file menu
		JMenu fileMenu = new JMenu(Messages.getString("ThingamablogFrame.File")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.File", fileMenu);
		fileMenu.add(newWeblogAction);
		fileMenu.add(newEntryAction);
		fileMenu.add(newFeedAction);
		fileMenu.addSeparator();
		fileMenu.add(openDBAction);
		fileMenu.add(newDBAction);
		fileMenu.addSeparator();
        
        JMenu importMenu = new JMenu(Messages.getString("ThingamablogFrame.Import")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Import", importMenu);
		importMenu.add(importLegacyWeblogAction);
		importMenu.add(importEntriesFromFeedAction);
		importMenu.add(importFeedFolderAction);
		fileMenu.add(importMenu);
		
		JMenu exportMenu = new JMenu(Messages.getString("ThingamablogFrame.Export")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Export", exportMenu);
		exportMenu.add(exportWeblogToRSSAction);
		exportMenu.add(exportFeedFolderAction);		        
		fileMenu.add(exportMenu);
		
		JMenuItem exitItem = new JMenuItem(Messages.getString("ThingamablogFrame.Exit")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Exit", exitItem);
		exitItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){closeApp(true);}});
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		mb.add(fileMenu);
        
        //edit menu
		JMenu editMenu = new JMenu(Messages.getString("ThingamablogFrame.Edit")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Edit", editMenu);
		editMenu.add(viewerPane.getCopyAction());
		editMenu.add(viewerPane.getSelectAllAction());		
		editMenu.addSeparator();
		editMenu.add(deleteEntriesAction);
		editMenu.add(editEntryAction);
		editMenu.addSeparator();
		editMenu.add(findEntriesAction);
		editMenu.add(findItemsAction); 
		mb.add(editMenu);
		
		//view menu
		JMenu viewMenu = new JMenu(Messages.getString("ThingamablogFrame.View")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.View", viewMenu);
		sortMenu = new JMenu(Messages.getString("ThingamablogFrame.Sort_by"));//updated whenever the view type changes //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Sort_by", sortMenu);
		viewMenu.add(sortMenu);
		viewMenu.addSeparator();
		viewMenu.add(nextAction);
		viewMenu.add(prevAction);
		viewMenu.addSeparator();
		viewMenu.add(nextUnreadAction);		
		viewMenu.addSeparator();
		viewMenu.add(viewWeblogAction);
		mb.add(viewMenu);
		
		//Weblogs menu
		JMenu weblogsMenu = new JMenu(Messages.getString("ThingamablogFrame.Weblog")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Weblog", weblogsMenu);
		weblogsMenu.add(newEntryAction);		
		weblogsMenu.addSeparator();
		weblogsMenu.add(publishAction);
		weblogsMenu.add(publishAllAction);
		weblogsMenu.add(weblogPingAction);		
		weblogsMenu.addSeparator();
		weblogsMenu.add(editTemplateAction);
		weblogsMenu.addSeparator();
		weblogsMenu.add(newWebFolderAction);
		weblogsMenu.add(importFileAction);		
		weblogsMenu.addSeparator();
		weblogsMenu.add(weblogPropertiesAction);		
		mb.add(weblogsMenu);
		
		//Feed menu
		JMenu feedMenu = new JMenu(Messages.getString("ThingamablogFrame.News")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.News", feedMenu);	
		feedMenu.add(newFeedAction);
		feedMenu.addSeparator();
		feedMenu.add(updateAllFeedsAction);
		feedMenu.add(updateFolderAction);
		feedMenu.add(updateCurFeedAction);
		feedMenu.addSeparator();
		feedMenu.add(markCurFeedReadAction);
		feedMenu.add(markCurFeedUnreadAction);
		feedMenu.addSeparator();
		feedMenu.add(newEntryFromItemAction);		
		feedMenu.addSeparator();
		feedMenu.add(feedPropertiesAction);	
		mb.add(feedMenu);
		
		//Configure menu
		JMenu configMenu = new JMenu(Messages.getString("ThingamablogFrame.Configure")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Configure", configMenu);
		JMenuItem optsItem = new JMenuItem(Messages.getString("ThingamablogFrame.Options")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Options", optsItem);
		optsItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int layout = TBGlobals.getLayoutStyle();
				TBOptionsDialog d = new TBOptionsDialog(FRAME);
				d.setLocationRelativeTo(FRAME);
				d.setVisible(true);
				if(!d.hasUserCancelled())
				{				
					d.saveOptions();
					//do layout if the style changed
					if(layout != TBGlobals.getLayoutStyle())
						layoutContentPanel(TBGlobals.getLayoutStyle());
					feedUpdateTimer.stop();
					if(TBGlobals.isAutoFeedUpdate())
					{					    
					    feedUpdateTimer.setInitialDelay(TBGlobals.getFeedUpdateInterval());
					    feedUpdateTimer.setDelay(TBGlobals.getFeedUpdateInterval());
					    feedUpdateTimer.start();
					}
				}
			}
		});
		optsItem.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "options16.png")); //$NON-NLS-1$
		configMenu.add(optsItem);
		mb.add(configMenu);	
		
		JMenu helpMenu = new JMenu(Messages.getString("ThingamablogFrame.Help")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.Help", helpMenu);
		
		Action helpContents = 
		    new TBHelpAction(
		            Messages.getString("ThingamablogFrame.Help_Contents"), "index");
		helpContents.putValue(Action.SMALL_ICON, Utils.createIcon(TBGlobals.RESOURCES + "help.png"));
		helpContents.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		Messages.setMnemonic("ThingamablogFrame.Help_Contents", helpContents);		
		Action tutorial = new TBHelpAction(
		        Messages.getString("ThingamablogFrame.Tutorial"), "ch02.index");
		
		JMenuItem donateItem = new JMenuItem(Messages.getString("ThingamablogFrame.Donate") + "...");
		donateItem.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        try{
		        Browser.displayURL(
		            "http://sourceforge.net/donate/index.php?group_id=86787");
		        }catch(Exception ex){}
		    }
		});
		
		helpMenu.add(helpContents);	
		helpMenu.add(tutorial);
		helpMenu.addSeparator();
		helpMenu.add(new HomePageAction());
		helpMenu.add(donateItem);
		helpMenu.addSeparator();	
		
		JMenuItem aboutItem = new JMenuItem(Messages.getString("ThingamablogFrame.About")); //$NON-NLS-1$
		Messages.setMnemonic("ThingamablogFrame.About", aboutItem);
		aboutItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TBAbout.showAboutBox(FRAME);
			}
		});
		helpMenu.add(aboutItem);
		mb.add(helpMenu);
		
		return mb;		
	}
	
	private void loadProperties()
	{
		int w = 800, h = 600;
		int h_div = 150, feed_div = 150, tv_div = 250;
		boolean maximized = false;
		
		int itemSortCol = 0;
		int entrySortCol = 0;
		
		try
		{		
			maximized = TBGlobals.getProperty("MAXIMIZED").toString().equals(true + "");		 //$NON-NLS-1$ //$NON-NLS-2$
			w = Integer.parseInt(TBGlobals.getProperty("WIDTH")); //$NON-NLS-1$
			h = Integer.parseInt(TBGlobals.getProperty("HEIGHT")); //$NON-NLS-1$
			h_div = Integer.parseInt(TBGlobals.getProperty("H_DIV")); //$NON-NLS-1$
			feed_div = Integer.parseInt(TBGlobals.getProperty("FEED_DIV")); //$NON-NLS-1$
			tv_div = Integer.parseInt(TBGlobals.getProperty("TABLE_VIEWER_DIV")); //$NON-NLS-1$
			itemSortCol = Integer.parseInt(TBGlobals.getProperty("ITEM_SORT_COL")); //$NON-NLS-1$
			entrySortCol = Integer.parseInt(TBGlobals.getProperty("ENTRY_SORT_COL")); //$NON-NLS-1$
			int ed_w = Integer.parseInt(TBGlobals.getProperty("EDITOR_WIDTH")); //$NON-NLS-1$
			int ed_h = Integer.parseInt(TBGlobals.getProperty("EDITOR_HEIGHT"));			 //$NON-NLS-1$
			editorSize = new Dimension(ed_w, ed_h);			
		}
		catch(Exception ex){}	
		
		if(maximized)
		{
			setSize(800, 600);
			setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
		}
		else
			setSize(w, h);
		hSplitPane.setDividerLocation(h_div);
		tableViewerDivider.setDividerLocation(tv_div);
		feedSplitPane.setDividerLocation(feed_div);		
				
		String tf = TBGlobals.getProperty("ITEM_SORT_ASC"); //$NON-NLS-1$
		boolean itemSortAsc = tf != null && tf.equals("true"); //$NON-NLS-1$
		feedTableModel.sortColumn(itemSortCol, itemSortAsc);
		
		tf = TBGlobals.getProperty("ENTRY_SORT_ASC"); //$NON-NLS-1$
		boolean entrySortAsc = tf != null && tf.equals("true"); //$NON-NLS-1$
		blogTableModel.sortColumn(entrySortCol, entrySortAsc);
		
/*		tf = TBGlobals.getProperty("EDITOR_WORDWRAP"); //$NON-NLS-1$
		if(tf == null)//default is true
			isEditorWordWrap = true;
		else			
			isEditorWordWrap = tf.equals("true"); //$NON-NLS-1$
*/		
		//load the template for the "post from item" format
		FeedItemFormatter.loadTemplate();		
	}
	
	private void saveProperties()
	{		
		TBGlobals.putProperty("EDITOR_WIDTH", editorSize.width + ""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty("EDITOR_HEIGHT", editorSize.height + ""); //$NON-NLS-1$ //$NON-NLS-2$
		//TBGlobals.putProperty("EDITOR_WORDWRAP", isEditorWordWrap + ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		TBGlobals.putProperty("MAXIMIZED", (getExtendedState() == MAXIMIZED_BOTH) + ""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty("WIDTH", getWidth() + ""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty("HEIGHT", getHeight() + ""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty("H_DIV", hSplitPane.getDividerLocation()+""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty("FEED_DIV", feedSplitPane.getDividerLocation()+""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty("TABLE_VIEWER_DIV",  //$NON-NLS-1$
			tableViewerDivider.getDividerLocation()+""); //$NON-NLS-1$
		
		TBGlobals.putProperty(
			"ENTRY_SORT_COL", blogTableModel.getSortedColumn() + ""); //$NON-NLS-1$ //$NON-NLS-2$
		TBGlobals.putProperty(
			"ENTRY_SORT_ASC", blogTableModel.isSortedColumnAscending() + ""); //$NON-NLS-1$ //$NON-NLS-2$
		
		TBGlobals.putProperty("ITEM_SORT_COL",  //$NON-NLS-1$
			feedTableModel.getSortedColumn() + ""); //$NON-NLS-1$
		TBGlobals.putProperty("ITEM_SORT_ASC",  //$NON-NLS-1$
			feedTableModel.isSortedColumnAscending() + ""); //$NON-NLS-1$
		
		String key = "";
		if(curSelWeblog != null)
		    key = curSelWeblog.getKey();
		TBGlobals.putProperty("LAST_SEL_BLOG", key);
		
		TBGlobals.saveProperties();
		tableColumnModel.saveColumnData();	
	}	


	
	private void scrollToTableRow(int r)
	{
		Rectangle rect = table.getCellRect(r, 0, true);
		table.scrollRectToVisible(rect);
	}
	
	
	private void updateTableCellRenderer()
	{		
		TableColumnModel colModel = table.getColumnModel();
		int cols = colModel.getColumnCount();
		for(int i = 0; i < cols; i++)
		{
			TBTableCellRenderer ren = new TBTableCellRenderer();
			TableColumn tCol = colModel.getColumn(i);
			if(tableView == ENTRY_VIEW && curSelWeblog != null)
			{				
				ren.setExpireDate(curSelWeblog.getArchiveBaseDate());				
			}			
						
			tCol.setCellRenderer(ren);
		}
	}
	
	
	private void refreshTable()
	{
		int row = table.getSelectedRow();
		table.clearSelection();
				
		if(table.getModel() == feedTableModel)
		{
			TreePath selPath = feedTree.getSelectionPath();
			if(selPath != null)
			{			
				feedTree.clearSelection();
				feedTree.setSelectionPath(selPath);
			}
		}
		else if(table.getModel() == blogTableModel)
		{
			long ids[] = new long[blogTableModel.getRowCount()];
			for(int r = 0; r < ids.length; r++)
			{
				ids[r] = blogTableModel.getEntryIDAtRow(r);
			}
			
			blogTableModel.setRowCount(0);
			Vector v = new Vector(ids.length, 1);
			for(int i = 0; i < ids.length; i++)
			{
				try
				{
					BlogEntry e = curViewWeblog.getEntry(ids[i]);
					if(e == null)continue;
					v.add(e);
				}
				catch(Exception ex){}
			}
			
			BlogEntry entries[] = new BlogEntry[v.size()];
			for(int i = 0; i < entries.length; i++)
				entries[i] = (BlogEntry)v.elementAt(i);
			blogTableModel.setBlogEntries(entries);			
		}
					
		if(row > -1 && row < table.getRowCount())
			table.setRowSelectionInterval(row, row);				
	}
	
	private void refreshTree(JTree tree)
	{
		TreePath selPath = tree.getSelectionPath();
		TreeModel model = tree.getModel();
		Enumeration eEnum = tree.getExpandedDescendants(new TreePath(model.getRoot()));
		if(model instanceof WeblogTreeModel)
			((WeblogTreeModel)model).setData(weblogList);
		else if(model instanceof FeedTreeModel)
			((FeedTreeModel)model).refresh();
		tree.repaint();
		while(eEnum.hasMoreElements())
		{
			tree.expandPath((TreePath)eEnum.nextElement());	
		}
		tree.setSelectionPath(selPath);			
	}
	
	/**
	 * Changes the current model of the table based on the view type
	 * @param type The view type
	 */
	private void setTableView(int type)
	{
		table.clearSelection();
		viewerPaneModel.setModelData(null);
		//viewerPane.setModel(null);
		if(type == ENTRY_VIEW)
		{
			curViewWeblog = curSelWeblog;
			tableView = ENTRY_VIEW;			
			table.setModel(blogTableModel);
			table.sort(blogTableModel.getSortedColumn(), 
				blogTableModel.isSortedColumnAscending());				
		}
		else if(type == ITEM_VIEW)
		{
			tableView = ITEM_VIEW;
			table.setModel(feedTableModel);
			table.sort(feedTableModel.getSortedColumn(), 
				feedTableModel.isSortedColumnAscending());										
		}
		updateTableCellRenderer();
		updateSortMenu();
		
		if(!table.isSortedColumnAscending() && table.getRowCount() > 0)
		{
			scrollToTableRow(0);			
		}
		else if(table.isSortedColumnAscending() && table.getRowCount() > 0)
		{
			scrollToTableRow(table.getRowCount() - 1);
		}
	}
	
	private void showWeblogConfigDialog()
	{
		//if(blogTree.getLastSelectedPathComponent() instanceof Weblog)
		if(curSelWeblog != null && !curSelWeblog.isPublishing())
		{
			boolean cancelled = 
			WeblogPropertiesDialogFactory.showPropertiesDialog(curSelWeblog, FRAME);
			if(!cancelled)
			{
				//update the table tree for any changes that have occured
				refreshTree(blogTree);
				refreshTable();
				
				saveCurrentData();	
			}				
		}
	}
	
	private void showNewWeblogWizard()
	{
		if(!isDBOpen)
		{
		    JOptionPane.showMessageDialog(FRAME, "No database is open.");
		    return;
		}
	    
	    TBWizardDialog wiz = new TBWizardDialog(FRAME, curDB, backend);
		wiz.setLocationRelativeTo(FRAME);
		wiz.setVisible(true);
		
		if(!wiz.hasUserCancelled())
		{
			weblogList.addWeblog(wiz.getWeblog());
			weblogTreeModel.setData(weblogList);				
			if(searchDialog != null)
				searchDialog.setWeblogList(weblogList);
				
			//updateActions();				
			saveCurrentData();
			selectWeblog(wiz.getWeblog());
		}
	}
	
	private void selectWeblog(Weblog blog)
	{
        blogTree.expandRow(0);
        int rc = blogTree.getRowCount();
        for(int i = 0; i < rc; i++)
        {
            TreePath p = blogTree.getPathForRow(i);
            if(p.getLastPathComponent() == blog)
                blogTree.setSelectionRow(i);
        }
	}
	
	/**
	 * Creates a JMenu that can sort the table columns
	 * 
	 */
	private void updateSortMenu()
	{
		sortMenu.removeAll();		
		if(tableView == ENTRY_VIEW)
		{
			sortMenu.add(new SortTableAction(WeblogTableModel.TITLE.toString(), 
				WeblogTableModel.TITLE_COL));
			sortMenu.add(new SortTableAction(WeblogTableModel.POST_DATE.toString(),
				WeblogTableModel.DATE_COL));
			sortMenu.add(new SortTableAction(WeblogTableModel.AUTHOR.toString(),
				WeblogTableModel.AUTHOR_COL));
			sortMenu.add(new SortTableAction(WeblogTableModel.ID.toString(), 
				WeblogTableModel.ID_COL));
		}
		else if(tableView == ITEM_VIEW)
		{
			sortMenu.add(new SortTableAction(Messages.getString("ThingamablogFrame.Read"), FeedTableModel.READ_COL)); //$NON-NLS-1$
			sortMenu.add(new SortTableAction(FeedTableModel.ITEM.toString(),
				FeedTableModel.ITEM_COL));
			sortMenu.add(new SortTableAction(FeedTableModel.DATE.toString(), 
				FeedTableModel.DATE_COL));
		}
		
		final JRadioButtonMenuItem ascItem = new JRadioButtonMenuItem(Messages.getString("ThingamablogFrame.Ascending")); //$NON-NLS-1$
		final JRadioButtonMenuItem descItem = new JRadioButtonMenuItem(Messages.getString("ThingamablogFrame.Descending")); //$NON-NLS-1$
		ActionListener sortOrderListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(e.getSource() == ascItem)
				{
					sortAscending = ascItem.isSelected();
					descItem.setSelected(!ascItem.isSelected());	
				}
				else if(e.getSource() == descItem)
				{
					sortAscending = descItem.isSelected();
					ascItem.setSelected(!descItem.isSelected());	
				}	
			}	
		};
		
		ascItem.setSelected(sortAscending);
		ascItem.addActionListener(sortOrderListener);
		descItem.setSelected(!sortAscending);
		descItem.addActionListener(sortOrderListener);
		
		sortMenu.addSeparator();
		sortMenu.add(ascItem);
		sortMenu.add(descItem);				
	}
	
	/**
	 * Updates a folder and all its subfolders
	 * @param f  the folder to update
	 * @param progress  the update progress
	 */
	private void updateFolder(final FeedFolder f)
	{
		if(updateMonitor.isUpdateStarted())
		{
			System.out.println("Update in progress"); //$NON-NLS-1$
			return;
		}
			
		Thread updater = new Thread()
		{
			public void run()
			{				
				f.updateFeeds(true, updateMonitor);
				
				SwingUtilities.invokeLater(new Runnable() {
	                public void run() {
	    				updateAllFeedsAction.setAbortable(false);				
	    				moveHandler.setEnabled(true);				
	    				updateMonitor.reset();
	    				if(TBGlobals.isAutoFeedUpdate())
	    				    feedUpdateTimer.start();
	                }
	            });				
				
				System.out.println("update thread exiting");//$NON-NLS-1$
			}			
		};
		
		moveHandler.setEnabled(false);
		updateAllFeedsAction.setAbortable(true);
		feedUpdateTimer.stop();
		updater.start();
	}
	
	private void updateFeed(final Feed f)
	{
		Thread updater = new Thread()
		{
			public void run()
			{
				try
				{
					f.update();	
				}
				catch(Exception ioe)
				{
				    logger.log(Level.WARNING, ioe.getMessage(),ioe);
				    Utils.errMsg(FRAME, 
						Messages.getString("ThingamablogFrame.feed_update_error_prompt"), ioe);	 //$NON-NLS-1$
				}
				finally
				{
					//call update finish to re-enable feed actions 
					updateMonitor.updateFinish();
			        SwingUtilities.invokeLater(new Runnable() {
			        	public void run(){
							refreshTable();
							statusBar.setRefreshingText(""); //$NON-NLS-1$
							statusBar.getJProgressBar().setIndeterminate(false);
			        	}
			        });											
				}
			}
		};
		
		//calling updateStart here to disable feed actions
		updateMonitor.updateStart(1);
		statusBar.setRefreshingText(f.getTitle());
		statusBar.getJProgressBar().setIndeterminate(true);
		updater.start();
	}
	
	private void publishWeblog(final Weblog blog, final int pubType)
	{		
		final PublishDialog d = new PublishDialog(
					FRAME, Messages.getString(
					"ThingamablogFrame.Publishing") + ": " + blog.getTitle(), false); //$NON-NLS-1$
		
		Thread runner = new Thread()
		{
			public void run()
			{
				try
				{					
					//publish changed pages and send pings
					if(pubType == PUB_CHANGED)
					{					
						blog.publish(d);							
						//was the publish aborted and are there services to ping?
						if(!d.isAborted() && hasServicesToPing(blog) &&
						 !d.isDisplayingFailedMessage() && 
						 TBGlobals.isPingAfterPublish())
							blog.sendPings(d);
					}					
					else if(pubType == PUB_ALL)//publish everything
					{
						blog.publishAll(d);	
					}
					else if(pubType == PING_ONLY)
					{
						blog.sendPings(d);
					}
					System.out.println("Publish thread exiting");						 //$NON-NLS-1$
				}
				catch(Exception ex)
				{
				    logger.log(Level.WARNING, ex.getMessage(), ex);
				    ex.printStackTrace();
				}
			}
		};
		
		if(blog.isPublishing())
			return;
		
		boolean okToPublish = false;		
		if(pubType == PING_ONLY)
			okToPublish = hasServicesToPing(blog);
		else
		    okToPublish = LoginFactory.login(blog, new GUILoginPrompt(FRAME));
			//okToPublish = LoginFactory.login(curSelWeblog, new GUILoginPrompt(FRAME));
			
		if(okToPublish)
		{
			d.setLocationRelativeTo(FRAME);
			d.setVisible(true);
			runner.start();
		}
	}
	
	private boolean hasServicesToPing(Weblog b)
	{
		PingService ps[] = b.getPingServices();
		for(int i = 0; i < ps.length; i++)
		{
			if(ps[i].isEnabled())
				return true;
		}
		
		return false;	
	}
	
	private boolean isWebFolderSelected()
	{		
		if(blogTree.isSelectionEmpty())
			return false;
		
		Object o = blogTree.getLastSelectedPathComponent();		
		if(o instanceof File)
		{
			File f = (File)o;
			return f.isDirectory();	
		}
		
		TreePath pPath = blogTree.getSelectionPath().getParentPath();
		if(pPath == null)
			return false;		
		
		Object parent = pPath.getLastPathComponent();
		
		if(o.toString().equals(WeblogTreeModel.WEB_SITE) && 
		parent instanceof Weblog)
		{
			return true;
		}
		
		return false;	
	}
	
	private void openWebFile(File file)
	{
		if(file.isDirectory())
			return;
		if(TBGlobals.isTextFile(file))
		{
			HTMLEditor ed = new HTMLEditor(file);
			//ed.load();											
			ed.setSize(640, 480);						
			ed.setVisible(true);																
		}
		else if(TBGlobals.isImageFile(file))
		{						
			ImageViewerDialog d = new ImageViewerDialog(FRAME, file);
			d.setSize(440, 280);
			d.setLocationRelativeTo(FRAME);
			d.setVisible(true);	
		}	
	}
	
	private BlogEntry createEntryFromItem(FeedItem item)
	{
		BlogEntry entry = new BlogEntry();
		entry.setTitle(item.getTitle());
		//String desc = item.getDescription();
		//desc += "\n<br>\n<a href=\"" +  //$NON-NLS-1$
		//	item.getLink() + "\">" + item.getLink() + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
		String desc = FeedItemFormatter.format(item);
		entry.setText(desc);
		return entry;	
	}
	
	private void saveCurrentData()
	{
		try
		{
			if(curDB != null && isDBOpen)
				TBPersistFactory.save(weblogList, feedRoot, 
					curDB.getAbsolutePath() + TBGlobals.SEP + TBGlobals.USER_XML_FILENAME);

		}
		catch(Exception ex)
		{		    
		    logger.log(Level.WARNING, ex.getMessage(), ex);
		    ex.printStackTrace();	
		}
		saveProperties();
	}
	
	private void closeApp(boolean needExit)
	{		
		saveCurrentData();
		setVisible(false);
		try
		{
			backend.shutdown();
			isDBOpen = false;
		}
		catch(Exception ex)
		{
			if(backend != null)
			{
			    ex.printStackTrace();
			    logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
		
		isAppOpen = false;
		if (needExit) {
			System.exit(0);
		}
	}
	
	private void showEditor(BlogEntry be, int editMode)
	{
		Weblog blog = null;
		if(editMode == EntryEditor.NEW_ENTRY_MODE || tableView == ITEM_VIEW)
			blog = curSelWeblog;
		else if(editMode == EntryEditor.UPDATE_ENTRY_MODE)
		{			
			blog = curViewWeblog;			
		}
		
		if(blog == null)
		{
			try
			{
				blog = weblogList.getWeblogAt(0);					
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
				return;
			}
		}
		
		try
		{				
			EntryEditor ed = new EntryEditor(be, blog, weblogList, editMode);
			ed.addWindowListener(new PostListener());
			ed.setSize(editorSize);
			//ed.setWordWrap(isEditorWordWrap);
			//ed.setIconImage(ICON);
			ed.setVisible(true); 
		}
		catch(BackendException ex)
		{
			ex.printStackTrace();
			logger.log(Level.WARNING, ex.getMessage(), ex);
		}  	
	}	
		
	
	
	/**
	 * 
	 * Handles table double clicks and starts an EntryEditor for BlogEntries
	 * and/or feedItems
	 */
	private class TableClickHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2 /*&& lastSelWeblog != null */) //double click
			{ 
				int r = table.getSelectedRow();
				if(tableView == ENTRY_VIEW)
				{
					long id = blogTableModel.getEntryIDAtRow(r);					
					try
					{
						BlogEntry be = curViewWeblog.getEntry(id);
						showEditor(be, EntryEditor.UPDATE_ENTRY_MODE);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						logger.log(Level.WARNING, ex.getMessage(), ex);
					}
					
				}
				else if(tableView == ITEM_VIEW)
				{					
					long id = feedTableModel.getItemIDAtRow(r);					
					try
					{
						FeedItem fi = backend.getItem(id);
						BlogEntry be = createEntryFromItem(fi);
						showEditor(be, EntryEditor.NEW_ENTRY_MODE);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
						logger.log(Level.WARNING, ex.getMessage(), ex);
					}					
				}
			}
		}
	}
	
	private class BlogTreeClickHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2) //double click
			{ 
				Object node = blogTree.getLastSelectedPathComponent();
				if(node instanceof Template)
				{
					Template t = (Template)blogTree.getLastSelectedPathComponent();
							
					HTMLEditor ed = new HTMLEditor(t);
					ed.setSize(640, 480);
					ed.setVisible(true);
					//ed.load();
									
				}
				else if(node instanceof File)
				{
					File file = (File)node;
					openWebFile(file);
				}
			}
		}
	}
	
	//*************************************************
	//Listens for focus changes between the two trees
	//If a tree gains focus on a node that is already selected,
	//this listener takes the place of the tree selection listener
	//which would not fire a selection event under that circumstance
	//*************************************************
	private class TreeFocusListener implements FocusListener
	{
		private TreePath blogTreeSelPath = null;
		private TreePath feedTreeSelPath = null;
		
		public void focusGained(FocusEvent e)
		{			
			if(e.isTemporary())
			    return;
						
		    try
			{
				if(e.getSource() == blogTree && blogTreeSelPath != null)
				{
					TreePath curSelPath = blogTree.getSelectionPath();
					boolean needsChanged = !htmlOptionsPane.isShowing()
						&& tableView != ENTRY_VIEW;
					needsChanged = needsChanged && curSelPath.equals(blogTreeSelPath);					
					if(curSelPath != null && needsChanged)
					{
						blogTreeSelected(blogTreeSelPath);						
					}
				}
				else if(e.getSource() == feedTree && feedTreeSelPath != null)
				{
					TreePath curSelPath = feedTree.getSelectionPath();
					if(curSelPath != null && curSelPath.equals(feedTreeSelPath))
					{
						feedTreeSelected(feedTreeSelPath);						
					}
				}					
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
		
		public void focusLost(FocusEvent e)
		{			
		    if(e.getSource() == blogTree)
			{
				blogTreeSelPath = blogTree.getSelectionPath();
			}
			else if(e.getSource() == feedTree)
			{			
				feedTreeSelPath = feedTree.getSelectionPath();	
			}
		}
	}
	
	
	private class TreeSelectionHandler implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent e)
		{
			if(!e.isAddedPath())//is it a deselection?
			{
			    updateActions();
			    return;
			}
									
			TreePath path = e.getPath();
			try
			{
				//which tree was selected?
				if(e.getSource() == blogTree)
				{
					//feedTree.clearSelection();
					blogTreeSelected(path);					
				}
				else if(e.getSource() == feedTree)
				{
				    //blogTree.clearSelection();
					feedTreeSelected(path);					
				}				
				statusBar.setViewingCount(table.getRowCount());
				updateActions();
			}	
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}			
		}
	}
	
	private void initHTMLView(Object obj)
	{
	    htmlOptionsPane.clearOptions();
	    if(obj instanceof Weblog)
	    {			
	        htmlOptionsPane.setTitle(((Weblog)obj).getTitle());
	        htmlOptionsPane.setImageURL(
			    ClassLoader.getSystemResource(TBGlobals.RESOURCES + "blog96.png"));	        
			htmlOptionsPane.addHeading(Messages.getString("ThingamablogFrame.Weblog_Management"));		
			htmlOptionsPane.addOption(new NewEntryLink());
			htmlOptionsPane.addOption(new ReadEntriesLink());
			htmlOptionsPane.addOption(new ConfigureWeblogLink());
			htmlOptionsPane.addHeading(Messages.getString("ThingamablogFrame.Publishing"));
			htmlOptionsPane.addOption(new PublishLink());
			htmlOptionsPane.addOption(new PublishAllLink());
			htmlOptionsPane.addOption(new PingLink());			
	    }
	    else if(obj == WeblogTreeModel.ROOT)
	    {
	        htmlOptionsPane.setTitle(WeblogTreeModel.ROOT.toString());
	        htmlOptionsPane.setImageURL(
			    ClassLoader.getSystemResource(TBGlobals.RESOURCES + "my_sites.png"));
	        
	        htmlOptionsPane.addOption(new NewWeblogLink());
	        if(weblogList.getWeblogCount() > 0)
	        {
	            htmlOptionsPane.addHeading(Messages.getString("ThingamablogFrame.Weblogs"));
	            for(int i = 0; i < weblogList.getWeblogCount(); i++)
	            {
	                htmlOptionsPane.addOption(new WeblogLink(weblogList.getWeblogAt(i)));
	            }
	        }			
	    }
	    
	    htmlOptionsPane.refresh();
	}
	
	private void blogTreeSelected(TreePath path) throws BackendException
	{
		if(path.getLastPathComponent() == WeblogTreeModel.ROOT)
		{
			blogTreePopup = new JPopupMenu();
			blogTreePopup.add(newWeblogAction);
			initHTMLView(WeblogTreeModel.ROOT);
			viewerPanelLayout.show(viewerPanel, HTML_VIEW);
			return;//nothing to do	
		}
			
		//get the blog for the path. For a WeblogTreeModel a blog should always
		//be at element 1
		try
		{
			curSelWeblog = (Weblog)path.getPathComponent(1);
			statusBar.setItem(curSelWeblog);
		}
		catch(ClassCastException cce)
		{
			cce.printStackTrace();
			logger.log(Level.WARNING, cce.getMessage(), cce);
			return;//something's wrong with this WeblogTreeModel	
		}			
			
		//do something for the selection type
		if(path.getLastPathComponent() instanceof Weblog)
		{
			blogTreePopup = createBlogSelectedPopup();
			Weblog w = (Weblog)path.getLastPathComponent();
			initHTMLView(w);
			viewerPanelLayout.show(viewerPanel, HTML_VIEW);
			return;			
		}
		
		if(path.getLastPathComponent() instanceof Template)
		{
			blogTreePopup = new JPopupMenu();
			blogTreePopup.add(editTemplateAction);	
		}
		//Either Curent, Drafts, Expired, or a category is selected
		else if(path.getLastPathComponent() instanceof String)
		{
			BlogEntry be[] = new BlogEntry[0];
			String str = path.getLastPathComponent().toString();
			blogTreePopup = null;//no popup for this type
				
			//Current or Drafts or Expired is selected
			if(path.getParentPath().getLastPathComponent() instanceof Weblog)
			{				
				if(str.equals(WeblogTreeModel.CURRENT))
				{						
					be = curSelWeblog.getCurrentEntries();					
				}
				else if(str.equals(WeblogTreeModel.DRAFTS))
				{
					be = curSelWeblog.getDraftEntries();
				}
				else if(str.equals(WeblogTreeModel.EXPIRED))
				{					
					be = curSelWeblog.getExpiredEntries();
				}				
				
			}//category is selected
			else if(path.getParentPath().getLastPathComponent().toString().equals(WeblogTreeModel.CATS))
			{						
				String cat = path.getLastPathComponent().toString();
				be = curSelWeblog.getEntriesFromCategory(cat);
				
			}					
			blogTableModel.setBlogEntries(be);
			tablePopup = createBlogTablePopup();
			//table.setModel(blogTableModel);
			setTableView(ENTRY_VIEW);
			viewerPanelLayout.show(viewerPanel, TABLE_VIEW);
							
		}//archive is selected
		else if(path.getLastPathComponent() instanceof ArchiveRange)
		{					
			ArchiveRange ar = (ArchiveRange)path.getLastPathComponent();					
			blogTableModel.setBlogEntries(curSelWeblog.getEntriesFromArchive(ar));
			//table.setModel(blogTableModel);
			setTableView(ENTRY_VIEW);
			tablePopup = createBlogTablePopup();
			blogTreePopup = null;
			viewerPanelLayout.show(viewerPanel, TABLE_VIEW);
		}
		//root of Web Files folder is selected
		else if(path.getParentPath().getLastPathComponent() instanceof Weblog &&
		path.getLastPathComponent().toString().equals(WeblogTreeModel.WEB_SITE))
		{
			blogTreePopup = new JPopupMenu();
			blogTreePopup.add(newWebFolderAction);
			blogTreePopup.add(importFileAction);			
		}//subfolder of Web Files folder is selected
		else if(path.getLastPathComponent() instanceof File)
		{
			File f = (File)path.getLastPathComponent();
			if(f.isDirectory())
			{				
				blogTreePopup = createDirectorySelectedPopup();					
			}
			else
			{
				blogTreePopup = createFileSelectedPopup();	
			}	
		}
		else
		{
			blogTreePopup = null;
		}
		
		//viewerPanelLayout.show(viewerPanel, TABLE_VIEW);
	}
		
	private void feedTreeSelected(TreePath path) throws FeedBackendException
	{
		if(path.getLastPathComponent() instanceof Feed)
		{
			lastSelFeed = (Feed)path.getLastPathComponent();
			feedTableModel.setItems(lastSelFeed.getItems());
			//table.setModel(feedTableModel);
			setTableView(ITEM_VIEW);
				
			feedTreePopup = createFeedSelectedPopup();				
			tablePopup = createFeedTablePopup();
				
			//String s = lastSelFeed.getTitle();
			//if(lastSelFeed.isLastUpdateFailed())
			//	s += " (" + lastSelFeed.getLastUpdateFailedReason() + ")";
			//statusBar.setText(s);
			statusBar.setItem(lastSelFeed);
			viewerPanelLayout.show(viewerPanel, TABLE_VIEW);
		}
		else if(path.getLastPathComponent() instanceof FeedFolder)
		{
			feedTreePopup = createFeedFolderSelectedPopup();
		}
	}
		
	//these methods create popups for various selection types
	private JPopupMenu createFeedSelectedPopup()
	{
		JPopupMenu p = new JPopupMenu();
		p.add(updateCurFeedAction);
		p.addSeparator();
		p.add(markCurFeedReadAction);
		p.add(markCurFeedUnreadAction);
		p.addSeparator();
		p.add(deleteFromFeedTreeAction);
		p.addSeparator();
		p.add(feedPropertiesAction);
			
		return p;
	}		
		
	private JPopupMenu createFeedFolderSelectedPopup()
	{
		JPopupMenu p = new JPopupMenu();
		p.add(updateFolderAction);
		p.addSeparator();
		p.add(newFeedFolderAction);
		p.add(newFeedAction);
		p.addSeparator();
		p.add(deleteFromFeedTreeAction);
		p.add(renameFeedFolderAction);
		p.addSeparator();
		p.add(importFeedFolderAction);
		p.add(exportFeedFolderAction);
			
		return p;
	}
		
	private JPopupMenu createFeedTablePopup()
	{
		JPopupMenu p = new JPopupMenu();
		p.add(updateCurFeedAction);
		p.addSeparator();
		p.add(newEntryFromItemAction);
		p.addSeparator();
		p.add(new MarkSelectedItemsAction(true));
		p.add(new MarkSelectedItemsAction(false));
		return p;
	}
		
	private JPopupMenu createBlogTablePopup()
	{
		JPopupMenu p = new JPopupMenu();
		//add through Utils.createMenuItem to remove icon etc
		p.add(editEntryAction);			
		p.add(deleteEntriesAction);
		return p;
	}
		
	private JPopupMenu createBlogSelectedPopup()
	{
		JPopupMenu p = new JPopupMenu();
		p.add(newEntryAction);
		p.addSeparator();
		p.add(publishAction);
		p.add(publishAllAction);
		p.add(weblogPingAction);
		p.addSeparator();
		p.add(weblogPropertiesAction);
		p.addSeparator();
		p.add(deleteWeblogAction);
		p.addSeparator();
		p.add(importEntriesFromFeedAction);
		p.add(exportWeblogToRSSAction);
		return p;	
	}
		
	private JPopupMenu createDirectorySelectedPopup()
	{
		JPopupMenu p = new JPopupMenu();		
		p.add(newWebFolderAction);
		p.add(importFileAction);
		p.addSeparator();			
		p.add(deleteFileAction);
		p.add(renameFileAction);
		return p;	
	}
		
	private JPopupMenu createFileSelectedPopup()
	{
		JPopupMenu p = new JPopupMenu();
		p.add(openFileAction);
		p.addSeparator();
		p.add(deleteFileAction);
		p.add(renameFileAction);
						
		return p;	
	}	
	
	private class TableSelectionHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{
			if(!e.getValueIsAdjusting())
			{
				int row = table.getSelectedRow();
				try
				{
					loadViewer(row);
					updateActions();
				}
				catch(Exception ex){}
			}
		}
		
		private void loadViewer(int row) throws BackendException, FeedBackendException
		{
			if(row > -1)
			{				
				//after a search the entries/items won't get loaded into 
				//the viewer because they won't belong to lastSelWeblog/lastSelFeed
				if(tableView == ENTRY_VIEW)
				{
					Long n = (Long)blogTableModel.getValueAt(row, WeblogTableModel.ID_COL);
					BlogEntry entry = curViewWeblog.getEntry(n.longValue());
					//viewerPane.setModel(new TBViewerPaneModel(entry));
					viewerPaneModel.setModelData(entry);
				}
				else if(tableView == ITEM_VIEW)
				{
					long id = feedTableModel.getItemIDAtRow(row);
					//Since all feeds have the same backend, we can
					//get/update items from the HSQLDBBackend
					FeedItem item = backend.getItem(id);
					if(!item.isRead())
					{
					    item.setRead(true);
						feedTableModel.setItemAtRowRead(row, true);
						backend.updateItem(item);
						feedTree.repaint();	
					}
					//viewerPane.setModel(new TBViewerPaneModel(item));
					viewerPaneModel.setModelData(item);
										
				}
			}
		}
	}
	
	private class PopupMenuListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{ 
		    selectNode(e);
		    checkForPopupTrigger(e); 
		}
			
		public void mouseReleased(MouseEvent e)
		{ checkForPopupTrigger(e); }
			
		private void checkForPopupTrigger(MouseEvent e)
		{
			if(e.isPopupTrigger())
			{
				Component c = e.getComponent();
				if(c == blogTree && blogTreePopup != null)								
					showTreePopup(blogTree, blogTreePopup, e);
				else if(c == feedTree && feedTreePopup != null)
					showTreePopup(feedTree, feedTreePopup, e);				
				else if(c == table && tablePopup != null)
					tablePopup.show(c, e.getX(), e.getY());
				else if(c == viewerPane.getJEditorPane() && viewerPopup != null)
					viewerPopup.show(c, e.getX(), e.getY());
			}
		}
		
		//selects the node, thereby firing a selection event
		//which should init the popup menu if needed
		private void selectNode(MouseEvent e)
		{
		    if(e.getSource() instanceof JTree)
		    {
		        JTree t = (JTree)e.getSource();
		        int row = t.getRowForLocation(e.getX(), e.getY());
		        if(row > -1)
		            t.setSelectionRow(row);
		    }
		}
		
		private void showTreePopup(JTree tree, JPopupMenu popup, MouseEvent e)
		{
			if(!tree.isSelectionEmpty())
			{						
				TreePath p = tree.getLeadSelectionPath();
				Rectangle rec = tree.getRowBounds(tree.getRowForPath(p));
				if(rec.contains(e.getX(), e.getY()))
					popup.show(tree, e.getX(), e.getY());
			}			
		}		
	}
	
	
	
	//********************
	//		Actions
	//********************
	
	//***********************************
	//Action to create a new weblog entry
	//***********************************
	private class NewEntryAction extends UpdatableAction
	{
		
		public NewEntryAction()
		{			
			super(Messages.getString("ThingamablogFrame.New_Entry")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.New_Entry", this);
			
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "new_entry16.png")); //$NON-NLS-1$
		}
		
		public void update()
		{
			setEnabled(weblogList.getWeblogCount() > 0);
		}
        
		public void actionPerformed(ActionEvent e)
		{
			showEditor(new BlogEntry(), EntryEditor.NEW_ENTRY_MODE);
		}
	}
	
	//****************************************************
	//Action to create a new weblog entry from a feed item
	//****************************************************
	private class NewEntryFromItemAction extends UpdatableAction
	{
		public NewEntryFromItemAction()
		{
			super(Messages.getString("ThingamablogFrame.Post_to_Weblog")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Post_to_Weblog", this);
		}
		
		public void update()
		{
			setEnabled(tableView == ITEM_VIEW && table.getSelectedRowCount() > 0 &&
				weblogList.getWeblogCount() > 0);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int r = table.getSelectedRow();
			if(lastSelFeed == null || r == -1 || tableView != ITEM_VIEW)
				return;
			
			long id = feedTableModel.getItemIDAtRow(r);
			try
			{
				FeedItem item = lastSelFeed.getBackend().getItem(id);
				BlogEntry entry = createEntryFromItem(item);
				showEditor(entry, EntryEditor.NEW_ENTRY_MODE);								
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}
	
	private class MarkSelectedItemsAction extends UpdatableAction
	{
		private boolean readOrUnread;
		public MarkSelectedItemsAction(boolean read)
		{
			super(null);			
			if(read)
			{
				putValue(NAME, Messages.getString("ThingamablogFrame.Mark_Items_Read")); //$NON-NLS-1$							
			}
			else
			{
				putValue(NAME, Messages.getString("ThingamablogFrame.Mark_Items_Unread"));											 //$NON-NLS-1$
			}
			
			readOrUnread = read;
		}
		
		public void update()
		{
			setEnabled(tableView == ITEM_VIEW && table.getSelectedRowCount() > 0);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int r[] = table.getSelectedRows();
			if(lastSelFeed == null || r.length == 0 || tableView != ITEM_VIEW)
				return;		
			
			for(int i = 0; i < r.length; i++)
			{			
				long id = feedTableModel.getItemIDAtRow(r[i]);
				
				try
				{
					table.clearSelection();
					FeedItem item = lastSelFeed.getBackend().getItem(id);
					item.setRead(readOrUnread);
					lastSelFeed.updateItem(item);
					refreshTable();
				}
				catch(Exception ex)
				{ 
				    ex.printStackTrace();
				    logger.log(Level.WARNING, ex.getMessage(), ex);
				}								
			}
			
			//mainPane.refreshView();
		}	
	}
		
	private class MarkCurrentFeedAction extends UpdatableAction
	{
		private boolean read;
		public MarkCurrentFeedAction(boolean read)
		{			
			this.read = read;
			if(read)
			{			
				putValue(NAME, Messages.getString("ThingamablogFrame.Mark_Feed_Read")); //$NON-NLS-1$
				Messages.setMnemonic("ThingamablogFrame.Mark_Feed_Read", this);
			}
			else
			{			
				putValue(NAME, Messages.getString("ThingamablogFrame.Mark_Feed_Unread"));//$NON-NLS-1$
				Messages.setMnemonic("ThingamablogFrame.Mark_Feed_Unread", this);
			}		 
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof Feed;			
			setEnabled(sel && !updateMonitor.isUpdateStarted() && tableView == ITEM_VIEW);
		}
		
		public void actionPerformed(ActionEvent e)
		{			
			if(lastSelFeed != null && tableView == ITEM_VIEW)
			{			
				try
				{
					table.clearSelection();
					lastSelFeed.markAllItemsRead(read);
					refreshTable();
				}
				catch(Exception ex)
				{
				    ex.printStackTrace();
				    logger.log(Level.WARNING, ex.getMessage(), ex);
				}
				//mainPane.selectObjectInTree(f);				
				//mainPane.repaint();				
			}
		}
	}
	
	//**************************************************
	//Actions for editing the feed tree
	//**************************************************
	private class ImportFeedFolderAction extends UpdatableAction
	{
		public ImportFeedFolderAction()
		{
			super(Messages.getString("ThingamablogFrame.Import_Feeds_from_OPML")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Import_Feeds_from_OPML", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "import16.png"));	 //$NON-NLS-1$
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof FeedFolder;
			setEnabled(sel && !updateMonitor.isUpdateStarted());
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(feedTree.getLastSelectedPathComponent() instanceof FeedFolder)
			{
				FeedFolder parent = (FeedFolder)feedTree.getLastSelectedPathComponent();
				JFileChooser fc = new JFileChooser();
				CustomFileFilter cff = new CustomFileFilter();
				cff.addExtension("opml"); //$NON-NLS-1$
				cff.addExtension("xml"); //$NON-NLS-1$
				fc.setFileFilter(cff);				
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);				
				fc.setDialogTitle(Messages.getString("ThingamablogFrame.Import_from_OPML")); //$NON-NLS-1$
				int r = fc.showOpenDialog(ThingamablogFrame.this);
				fc.setApproveButtonText(Messages.getString("ThingamablogFrame.Import")); //$NON-NLS-1$
				if(r == JFileChooser.CANCEL_OPTION)
					return;
        		
        		if(fc.getSelectedFile() == null)
        			return;
        			
				File sel = fc.getSelectedFile();
				
				try
				{
					OPMLImportExport.importFromOPML(parent, sel.getAbsolutePath(), backend);
					refreshTree(feedTree);					
				}
				catch(Exception ex)
				{
					Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.error_importing_opml_prompt"), ex); //$NON-NLS-1$
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}	
		}	
	}
	
	
	private class ExportFeedFolderAction extends UpdatableAction
	{
		public ExportFeedFolderAction()
		{
			super(Messages.getString("ThingamablogFrame.Export_Feeds_to_OPML")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Export_Feeds_to_OPML", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "export16.png"));	 //$NON-NLS-1$
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof FeedFolder;
			setEnabled(sel && !updateMonitor.isUpdateStarted());
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(feedTree.getLastSelectedPathComponent() instanceof FeedFolder)
			{
				FeedFolder parent = (FeedFolder)feedTree.getLastSelectedPathComponent();
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				CustomFileFilter cff = new CustomFileFilter();
				cff.addExtension("opml"); //$NON-NLS-1$
				fc.setFileFilter(cff);				
				fc.setDialogTitle(Messages.getString("ThingamablogFrame.Export_to_OPML")); //$NON-NLS-1$
				fc.setSelectedFile(new File(TBGlobals.USER_HOME, parent.getName() + ".opml")); //$NON-NLS-1$
				int r = fc.showSaveDialog(FRAME);
				fc.setApproveButtonText(Messages.getString("ThingamablogFrame.Export")); //$NON-NLS-1$
				if(r == JFileChooser.CANCEL_OPTION)
					return;
        		
				if(fc.getSelectedFile() == null)
					return;
        			
				File sel = fc.getSelectedFile();
				if(sel == null)
					return;
					
				if(sel.exists())
				{
					int yn = JOptionPane.showConfirmDialog(FRAME, Messages.getString("ThingamablogFrame.overwrite_file_prompt"), //$NON-NLS-1$
						Messages.getString("ThingamablogFrame.Confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
					
					if(yn == JOptionPane.NO_OPTION)
						return;					
				}				
				
				try
				{
					OPMLImportExport.exportFolderToOPML(parent, sel.getAbsolutePath());					
				}
				catch(Exception ex)
				{
					Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.error_exporting_folder_prompt"), ex); //$NON-NLS-1$
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}	
		}	
	}
	
	//**********************************************
	//Actions for importing/exporting weblog entries
	//**********************************************
	private class ImportLegacyWeblogAction extends UpdatableAction
	{
	    public ImportLegacyWeblogAction()
	    {
			super(Messages.getString("ThingamablogFrame.Import_09x_Weblog")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Import_09x_Weblog", this);
	    }
	    
	    public void update()
	    {
	        setEnabled(isDBOpen && curDB != null);
	    }
	    
		public void actionPerformed(ActionEvent e)
		{				
			JDialog d = new ImportLegacyWeblogDialog(FRAME, curDB, weblogList, backend);
			d.setLocationRelativeTo(FRAME);
			d.setVisible(true);
			refreshTree(blogTree);				
		}
	}
	
	private class ImportEntriesFromFeedAction extends UpdatableAction
	{
		public ImportEntriesFromFeedAction()
		{
			super(Messages.getString("ThingamablogFrame.Import_Entries_from_Feed")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Import_Entries_from_Feed", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "import16.png")); //$NON-NLS-1$
		}
		
		public void update()
		{
			setEnabled(blogTree.getLastSelectedPathComponent() instanceof Weblog);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(blogTree.getLastSelectedPathComponent() instanceof Weblog)
			{
				Weblog w = (Weblog)blogTree.getLastSelectedPathComponent();
				JDialog d = new ImportEntriesDialog(FRAME, w);
				d.setLocationRelativeTo(FRAME);
				d.setVisible(true);	
			}			
		}
	}
	
	private class ExportWeblogToRSSAction extends UpdatableAction
	{
		public ExportWeblogToRSSAction()
		{
			super(Messages.getString("ThingamablogFrame.Export_Weblog_to_Feed")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Export_Weblog_to_Feed", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "export16.png"));				 //$NON-NLS-1$
		}
		
		public void update()
		{
			setEnabled(blogTree.getLastSelectedPathComponent() instanceof Weblog);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(blogTree.getLastSelectedPathComponent() instanceof Weblog)
			{
				Weblog w = (Weblog)blogTree.getLastSelectedPathComponent();
				
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				CustomFileFilter cff = new CustomFileFilter();
				cff.addExtension("xml"); //$NON-NLS-1$
				fc.setFileFilter(cff);				
				fc.setDialogTitle(Messages.getString("ThingamablogFrame.Export_to_Feed")); //$NON-NLS-1$
				fc.setSelectedFile(new File(TBGlobals.USER_HOME, w.getTitle() + ".xml")); //$NON-NLS-1$
				int r = fc.showSaveDialog(FRAME);
				fc.setApproveButtonText(Messages.getString("ThingamablogFrame.Export")); //$NON-NLS-1$
				if(r == JFileChooser.CANCEL_OPTION)
					return;
        		
				if(fc.getSelectedFile() == null)
					return;
        			
				File sel = fc.getSelectedFile();
				if(sel == null)
					return;
					
				if(sel.exists())
				{
					int yn = JOptionPane.showConfirmDialog(FRAME, Messages.getString("ThingamablogFrame.overwrite_existing_file_prompt"), //$NON-NLS-1$
						Messages.getString("ThingamablogFrame.Confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
					
					if(yn == JOptionPane.NO_OPTION)
						return;					
				}				
				
				try
				{
					RSSImportExport.exportWeblogToFeed(w, sel);					
				}
				catch(Exception ex)
				{
					Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.error_exporting_folder_prompt"), ex); //$NON-NLS-1$
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
		}	
	}	
	
	private class NewFeedFolderAction extends UpdatableAction
	{
		public NewFeedFolderAction()
		{
			super(Messages.getString("ThingamablogFrame.New_Feed_Folder")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.New_Feed_Folder", this);
			putValue(Action.SMALL_ICON, 
					Utils.createIcon(TBGlobals.RESOURCES + "new_folder.png")); //$NON-NLS-1$
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof FeedFolder;
			setEnabled(sel && !updateMonitor.isUpdateStarted());				
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(feedTree.getLastSelectedPathComponent() instanceof FeedFolder)
			{
				FeedFolder parent = (FeedFolder)feedTree.getLastSelectedPathComponent();
				Object s = JOptionPane.showInputDialog(FRAME, Messages.getString("ThingamablogFrame.Folder_Name"), Messages.getString("ThingamablogFrame.New_Folder"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.QUESTION_MESSAGE, null, null, Messages.getString("ThingamablogFrame.New_Folder")); //$NON-NLS-1$
				if(s != null && !s.toString().equals("")) //$NON-NLS-1$
				{
					FeedFolder child = new FeedFolder(s.toString());
					parent.addFolder(child);
					refreshTree(feedTree);
					//tmodel.refresh();						
				}	
			}
		}	
	}
	
	private class NewFeedAction extends UpdatableAction
	{		
		public NewFeedAction()
		{
			super(Messages.getString("ThingamablogFrame.New_Feed")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.New_Feed", this);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));			
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "add_feed.png"));//$NON-NLS-1$
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof FeedFolder;
			setEnabled(sel && !updateMonitor.isUpdateStarted());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(feedTree.getLastSelectedPathComponent() instanceof FeedFolder)
			{
				FeedFolder parent = (FeedFolder)feedTree.getLastSelectedPathComponent();
				JTextField tf = new JTextField(25);
				TextEditPopupManager pm = new TextEditPopupManager();
				pm.addJTextComponent(tf);
				LabelledItemPanel lip = new LabelledItemPanel();
				lip.addItem("Feed URL:", tf); //$NON-NLS-1$
				tf.requestFocus();
				int s = JOptionPane.showConfirmDialog(FRAME, lip, Messages.getString("ThingamablogFrame.Feed_URL"),  //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);				
				
				if(s == JOptionPane.OK_OPTION && !tf.getText().equals("")) //$NON-NLS-1$
				{
					Feed child = new Feed(tf.getText());
					child.setTitle(Messages.getString("ThingamablogFrame.New_Feed_Title")); //$NON-NLS-1$
					child.setBackend(backend);
					parent.addFeed(child);					
					refreshTree(feedTree);
					
					saveCurrentData();
					updateFeed(child);
				}	
			}
		}	
	}
	
	private class RenameFeedFolderAction extends UpdatableAction
	{
		public RenameFeedFolderAction()
		{
			super(Messages.getString("ThingamablogFrame.Rename"));//$NON-NLS-1$
			
		}
		
		public void update()
		{
			boolean sel = 
			feedTree.getLastSelectedPathComponent() instanceof FeedFolder &&
			feedTree.getLastSelectedPathComponent() != feedRoot;
			setEnabled(sel && !updateMonitor.isUpdateStarted());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object obj = feedTree.getLastSelectedPathComponent();
			if(obj instanceof FeedFolder)
			{				
				FeedFolder folder = (FeedFolder)obj;
				Object o = JOptionPane.showInputDialog(
					FRAME, Messages.getString("ThingamablogFrame.Rename_Folder"), Messages.getString("ThingamablogFrame.Rename_Title"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.QUESTION_MESSAGE, null, null, folder.getName());
				if(o != null && !o.toString().equals("")) //$NON-NLS-1$
				{
					folder.setName(o.toString());
					refreshTree(feedTree);	
				}
			}
		}
			
	}
	
	private class DeleteFromFeedTreeAction extends UpdatableAction
	{
		public DeleteFromFeedTreeAction()
		{
			super(Messages.getString("ThingamablogFrame.Delete")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "delete16.png"));				 //$NON-NLS-1$
		}
		
		public void update()
		{			
			boolean sel = !feedTree.isSelectionEmpty();
			setEnabled(sel && 
				feedTree.getLastSelectedPathComponent() != feedRoot && 
				!updateMonitor.isUpdateStarted());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			Object obj = feedTree.getLastSelectedPathComponent();
			if(obj instanceof FeedFolder && obj != feedRoot)
			{
				FeedFolder folder =	(FeedFolder)obj;
				FeedFolder parent = folder.getParent();
				if(parent == null || !isOkToDelete(folder.getName()))
					return;
				try
				{
					folder.deleteContents();
					parent.removeFolder(folder);
					refreshTree(feedTree);
					feedTree.clearSelection();
					saveCurrentData();
				}
				catch(FeedBackendException ex)
				{
					ex.printStackTrace();
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}				
			}
			else if(obj instanceof Feed)
			{
				Feed feed = (Feed)obj;
				if(!isOkToDelete(feed.getTitle()))
					return;
				
				try
				{
					TreePath pPath = feedTree.getSelectionPath().getParentPath();
					FeedFolder parent = (FeedFolder)pPath.getLastPathComponent();

					feed.removeAllItems();
					parent.removeFeed(feed);
					refreshTree(feedTree);
					feedTree.clearSelection();
					saveCurrentData();
				}
				catch(ClassCastException cce)
				{
					cce.printStackTrace();	
				}
				catch(FeedBackendException fbe)
				{
					fbe.printStackTrace();
					logger.log(Level.WARNING, fbe.getMessage(), fbe);
				}	
			}
			
			updateActions();
		}
		
		private boolean isOkToDelete(String s)
		{
			int r = JOptionPane.showConfirmDialog(FRAME,
				Messages.getString("ThingamablogFrame.Delete_Title") + " '" + s + "'?", 
				Messages.getString("ThingamablogFrame.Confirm"), JOptionPane.YES_NO_OPTION,//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				JOptionPane.QUESTION_MESSAGE);
			
			return r == JOptionPane.YES_OPTION;	
		}	
	}
	
	private class FeedPropertiesAction extends UpdatableAction
	{
		public FeedPropertiesAction()
		{
			super(Messages.getString("ThingamablogFrame.Feed_Properties")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Feed_Properties", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "options16.png")); //$NON-NLS-1$
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof Feed;
			setEnabled(sel && !updateMonitor.isUpdateStarted());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(lastSelFeed != null)
			{
				FeedPropertiesDialog d = new FeedPropertiesDialog(FRAME, lastSelFeed);
				d.setLocationRelativeTo(FRAME);
				d.setVisible(true);	
			}
		}	
	}
	
	
	
	
	//***************************************************
	//Actions for updating RSS Feeds
	//***************************************************	
	private class UpdateAllFeedsAction extends AbstractAction
	{		
		private boolean updating;
		
		public UpdateAllFeedsAction()
		{
			setAbortable(false);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(!updating)
			{
				updateFolder(feedRoot);
			}
			else
			{
				updateMonitor.abortUpdate();
				setEnabled(false);
			}
		}
		
		public void setAbortable(boolean b)
		{
			updating = b;
			
			if(updating)
			{
				putValue(NAME, Messages.getString("ThingamablogFrame.Cancel_Update")); //$NON-NLS-1$
				Messages.setMnemonic("ThingamablogFrame.Cancel_Update", this);
				putValue(SHORT_DESCRIPTION, getValue(NAME));
				putValue(Action.SMALL_ICON, 
					Utils.createIcon(TBGlobals.RESOURCES + "abort_update16.png")); //$NON-NLS-1$
				
				updateButton.setText(null);
				updateButton.setIcon(
					Utils.createIcon(TBGlobals.RESOURCES + "abort_update.png")); //$NON-NLS-1$
			}
			else
			{
				putValue(NAME, Messages.getString("ThingamablogFrame.Update_All_News")); //$NON-NLS-1$
				Messages.setMnemonic("ThingamablogFrame.Update_All_News", this);
				putValue(SHORT_DESCRIPTION, getValue(NAME));
				putValue(Action.SMALL_ICON, 
					Utils.createIcon(TBGlobals.RESOURCES + "update_feeds16.png"));					 //$NON-NLS-1$
				
				updateButton.setText(null);
				updateButton.setIcon(
					Utils.createIcon(TBGlobals.RESOURCES + "update_feeds.png")); //$NON-NLS-1$
				setEnabled(true);
			}	
		}
	}
	
	private class UpdateFolderAction extends UpdatableAction
	{
		public UpdateFolderAction()
		{
			super(Messages.getString("ThingamablogFrame.Update_Folder")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Update_Folder", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "update_folder16.png")); //$NON-NLS-1$
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof FeedFolder;
			setEnabled(sel && !updateMonitor.isUpdateStarted());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(feedTree.getLastSelectedPathComponent() instanceof FeedFolder)
			{
				FeedFolder folder = (FeedFolder)feedTree.getLastSelectedPathComponent();					
				updateFolder(folder);				
			}
		}	
	}
	
	private class UpdateCurrentFeedAction extends UpdatableAction
	{
		public UpdateCurrentFeedAction()
		{
			super(Messages.getString("ThingamablogFrame.Update_Feed")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Update_Feed", this);
		}
		
		public void update()
		{
			boolean sel = feedTree.getLastSelectedPathComponent() instanceof Feed;
			setEnabled(sel && !updateMonitor.isUpdateStarted());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			final Feed f = lastSelFeed;
			if(f == null || updateMonitor.isUpdateStarted())
				return;	
			
			updateFeed(f);
		}
	}
	
	//***************************************
	//Edit and delete Blog Entries
	//***************************************
	private class EditEntryAction extends UpdatableAction
	{
		public EditEntryAction()
		{
			super(Messages.getString("ThingamablogFrame.Edit_Selected_Entry")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Edit_Selected_Entry", this);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
			putValue(SHORT_DESCRIPTION, getValue(NAME));
		}
		
		public void update()
		{
			setEnabled(tableView == ENTRY_VIEW && table.getSelectedRowCount() > 0 && table.isShowing());
		}        
        
		public void actionPerformed(ActionEvent e)
		{    
			if(tableView != ENTRY_VIEW || curSelWeblog == null)
				return;
				
			int r = table.getSelectedRow();
			if(r < 0)return;
			long id = blogTableModel.getEntryIDAtRow(r);
			try
			{
				BlogEntry be = curViewWeblog.getEntry(id);
				showEditor(be, EntryEditor.UPDATE_ENTRY_MODE);
				refreshTable();                
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}
	
	private class DeleteEntriesAction extends UpdatableAction
	{
		public DeleteEntriesAction()
		{
			super(Messages.getString("ThingamablogFrame.Delete_Selected_Entries")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Delete_Selected_Entries", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "delete_entries16.png")); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
			putValue(SHORT_DESCRIPTION, getValue(NAME));
		}
		
		public void update()
		{
			setEnabled(tableView == ENTRY_VIEW && table.getSelectedRowCount() > 0 && table.isShowing());
		}
        
		public void actionPerformed(ActionEvent e)
		{
			if(tableView != ENTRY_VIEW || curSelWeblog == null)
				return;
			
			int yn = JOptionPane.showConfirmDialog(ThingamablogFrame.this,
						Messages.getString("ThingamablogFrame.delete_entries_prompt"), Messages.getString("ThingamablogFrame.Confirm"), //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            
			if(yn == JOptionPane.NO_OPTION)
				return;
            
			int r[] = table.getSelectedRows();
			long ids[] = new long[r.length];
			for(int i = 0; i < ids.length; i++)
				ids[i] = blogTableModel.getEntryIDAtRow(r[i]);
			
			try
			{
				for(int i = 0; i < ids.length; i++)
				{
					BlogEntry be = curViewWeblog.getEntry(ids[i]);
					curViewWeblog.removeEntry(be);
				}
				refreshTable();
				refreshTree(blogTree);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}			
		}
	}
	
	

	
	//************************************************
	//classes for publishing weblogs
	//************************************************
	private class PublishAction extends UpdatableAction
	{		
		public PublishAction()
		{
			super(Messages.getString("ThingamablogFrame.Publish")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Publish", this);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "publish16.png"));	 //$NON-NLS-1$
		}
		
		public void update()
		{
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
			setEnabled(curSelWeblog != null && !curSelWeblog.isPublishing() && !rootSel);
			if(isEnabled())
				putValue(Action.SHORT_DESCRIPTION, 
				getValue(NAME) + " [" + curSelWeblog.getTitle() + "]");	 //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			//if(blogTree.getLastSelectedPathComponent() instanceof Weblog)
			if(curSelWeblog != null)
			{
				publishWeblog(curSelWeblog, PUB_CHANGED);
			}
		}
	}
	
	private class PublishAllAction extends UpdatableAction
	{		
		public PublishAllAction()
		{
			super(Messages.getString("ThingamablogFrame.Publish_Entire_Weblog")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Publish_Entire_Weblog", this);
		}
		
		public void update()
		{
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
			setEnabled(curSelWeblog != null && !curSelWeblog.isPublishing() && !rootSel);
			if(isEnabled())
				putValue(Action.SHORT_DESCRIPTION, 
				getValue(NAME) + " [" + curSelWeblog.getTitle() + "]");	 //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void actionPerformed(ActionEvent e)
		{			
			if(curSelWeblog != null)
			{
				publishWeblog(curSelWeblog, PUB_ALL);
			}
		}
	}
	
	//***************************************
	//Action to ping the services of a weblog
	//****************************************
	private class WeblogPingAction extends UpdatableAction
	{		
		public WeblogPingAction()
		{
			super(Messages.getString("ThingamablogFrame.Ping_Services")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Ping_Services", this);
		}
		
		public void update()
		{			
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
			setEnabled(curSelWeblog != null && 
				!curSelWeblog.isPublishing() && !rootSel &&
				hasServicesToPing(curSelWeblog));
			if(isEnabled())
				putValue(Action.SHORT_DESCRIPTION, 
				getValue(NAME) + " [" + curSelWeblog.getTitle() + "]");	 //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(curSelWeblog != null)
			{
				publishWeblog(curSelWeblog, PING_ONLY);
			}
		}
	}	
	
	
	//******************************************
	//Action to view the front page of a weblog
	//******************************************
	private class ViewWeblogAction extends UpdatableAction
	{		
		public ViewWeblogAction()
		{
			super(Messages.getString("ThingamablogFrame.View_Weblog")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.View_Weblog", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "preview16.png"));	 //$NON-NLS-1$
		}
		
		public void update()
		{			
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
			setEnabled(curSelWeblog != null && !rootSel);
			if(isEnabled())
				putValue(SHORT_DESCRIPTION, 
				getValue(NAME) + " [" + curSelWeblog.getTitle() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(curSelWeblog != null)
			{
				try
				{				
					Browser.displayURL(curSelWeblog.getFrontPageUrl());					
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}			
			}
		}
	}
	
	//*************************************************
	//Action to open the properties dialog for a Weblog
	//*************************************************
	private class WeblogPropertiesAction extends UpdatableAction
	{		
		public WeblogPropertiesAction()
		{			
			super(Messages.getString("ThingamablogFrame.Configure_Weblog")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Configure_Weblog", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "options16.png")); //$NON-NLS-1$
		}
		
		public void update()
		{			
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
			setEnabled(curSelWeblog != null && !curSelWeblog.isPublishing() && !rootSel);
			if(isEnabled())
				putValue(SHORT_DESCRIPTION, 
				getValue(NAME) + " [" + curSelWeblog.getTitle() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void actionPerformed(ActionEvent e)
		{
		    showWeblogConfigDialog();
		}
	}
	
	//*******************************************************
	//Actions that show the search dialog and handle a search
	//*******************************************************
	private class SearchAction extends AbstractAction
	{
		private boolean isSwitchTab;
		private int searchTab;
		
		public SearchAction()
		{
			super(Messages.getString("ThingamablogFrame.Find")); //$NON-NLS-1$
		}
		
		/**
		 * A search Action that opens the search dialog with the specified tab
		 * @param tab Should be either 
		 * TBSearchDialog.FEED_TAB or TBSearchDialog.WEBLOG_TAB
		 */
		public SearchAction(int tab)
		{			
			super();
			if(tab == TBSearchDialog.WEBLOG_TAB)
			{
				searchTab = tab;
				putValue(NAME, Messages.getString("ThingamablogFrame.Find_Entries"));//$NON-NLS-1$
				Messages.setMnemonic("ThingamablogFrame.Find_Entries", this);
				putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
			}
			else
			{
				searchTab = TBSearchDialog.FEED_TAB;
				putValue(NAME, Messages.getString("ThingamablogFrame.Find_Items")); //$NON-NLS-1$
				Messages.setMnemonic("ThingamablogFrame.Find_Items", this);
			}
			
			isSwitchTab = true;
		}		
		
		public void actionPerformed(ActionEvent e)
		{			
			if(searchDialog != null && searchDialog.isVisible())
				return;
			
			searchDialog = new TBSearchDialog(FRAME, weblogList, feedRoot);	
			searchDialog.addActionListener(new FindHandler());		
			
			//if(searchDialog != null && !searchDialog.isVisible())
			{
				searchDialog.setLocationRelativeTo(FRAME);
				if(isSwitchTab)
					searchDialog.setCurrentSearchTab(searchTab);
				searchDialog.setVisible(true);				
			}	
		}
	}
	
	//handle a search when the Find button on the search dialog is pressed
	private class FindHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(searchDialog == null)
				return;
			
			//searching should probably get executed in a thread
			//but for the current implementation the HSQLBackend is pretty fast
			//so we'll go with this for now...
			try
			{
				if(searchDialog.getCurrentSearchTab() == TBSearchDialog.FEED_TAB)
				{
					FeedItem items[] = searchDialog.performFeedSearch();
					feedTree.clearSelection();
					feedTableModel.setItems(items);
					setTableView(ITEM_VIEW);										
				}
				else //must be the weblog tab
				{
					BlogEntry be[] = searchDialog.performWeblogSearch();
					blogTree.clearSelection();
					blogTableModel.setBlogEntries(be);
					setTableView(ENTRY_VIEW);
					curSelWeblog = searchDialog.getSelectedWeblog();
                    curViewWeblog = curSelWeblog;
				}
				
				statusBar.setViewingCount(table.getRowCount());
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}				
		}
	}
	
	//************************************************
	//Action for creating a new Weblog from the wizard
	//************************************************
	private class NewWeblogAction extends AbstractAction
	{
		public NewWeblogAction()
		{
			super(Messages.getString("ThingamablogFrame.New_Weblog")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.New_Weblog", this);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "new_weblog16.png")); //$NON-NLS-1$
		}
		
		public void actionPerformed(ActionEvent e)
		{
		    showNewWeblogWizard();
		}	
	}
	
	//*************************************************
	//Action for deleting the currently selected weblog
	//*************************************************
	private class DeleteWeblogAction extends UpdatableAction
	{
		public DeleteWeblogAction()
		{
			super(Messages.getString("ThingamablogFrame.Delete_Weblog")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "delete16.png"));	 //$NON-NLS-1$
		}
		
		public void update()
		{			
			setEnabled(blogTree.getLastSelectedPathComponent() instanceof Weblog);			
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(blogTree.getLastSelectedPathComponent() instanceof Weblog)
			{
				Weblog blog = (Weblog)blogTree.getLastSelectedPathComponent();
				int yn = JOptionPane.showConfirmDialog(ThingamablogFrame.this,
							Messages.getString("ThingamablogFrame.delete_weblog_prompt") + " [" + blog.getTitle() + "]", 
							Messages.getString("ThingamablogFrame.Confirm"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);            
				if(yn == JOptionPane.NO_OPTION)
					return;
					
				try
				{
					weblogList.deleteWeblog(blog);
					weblogTreeModel.setData(weblogList);
					//clear the table
					blogTableModel.setBlogEntries(new BlogEntry[0]);
					if(searchDialog != null)
						searchDialog.setWeblogList(weblogList);
					//updateActions();
					saveCurrentData();
					blogTree.setSelectionRow(0); //select root note (My Sites)
				}
				catch(BackendException ex)
				{
					ex.printStackTrace();
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}				
			}
		}	
	}
	
	//*******************************************
	//Actions for creating/opening databases
	//*******************************************
	private class NewDatabaseAction extends AbstractAction
	{
		public NewDatabaseAction()
		{
			super(Messages.getString("ThingamablogFrame.New_Database"));//$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.New_Database", this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			createNewDatabase();			
		}	
	}
	
	private class OpenDatabaseAction extends AbstractAction
	{
		public OpenDatabaseAction()
		{
			super(Messages.getString("ThingamablogFrame.Open_Database"));//$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Open_Database", this);
		}
		
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle(Messages.getString("ThingamablogFrame.Open_Database_Title")); //$NON-NLS-1$
			fc.setFileFilter(new javax.swing.filechooser.FileFilter()
			{
				public boolean accept(File f)
				{
					return f.isDirectory() || 
						f.getName().equalsIgnoreCase(TBGlobals.USER_XML_FILENAME);
				}
				
				public String getDescription()
				{
					return TBGlobals.USER_XML_FILENAME;	
				}			
			});
			int r = fc.showOpenDialog(FRAME);
			if(r == JFileChooser.CANCEL_OPTION)
				return;
        
			File f = fc.getSelectedFile();
			if(f == null)
			{
				Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.Invalid_File"), null); //$NON-NLS-1$
				return;
			}
			
			File dir = f.getParentFile();
			openDB(dir);			
		}	
	}
	
	//*********************************************************
	//Actions for manipulating the Web Files folder of a weblog
	//*********************************************************	
	//Action for importing a file into a Web Files folder
	private class ImportFileAction extends UpdatableAction
	{
		private File lastDir = null;
	    
	    public ImportFileAction()
		{
			super(Messages.getString("ThingamablogFrame.Import_File")); //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Import_File", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "import16.png")); //$NON-NLS-1$
		}
		
		public void update()
		{			
			setEnabled(isWebFolderSelected());				
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(blogTree.isSelectionEmpty())
				return;
			Object obj = blogTree.getLastSelectedPathComponent();
			Object parent = blogTree.getSelectionPath().getParentPath().getLastPathComponent();
			//is the Web Files root dir selected?
			if(obj.toString().equals(WeblogTreeModel.WEB_SITE) && parent instanceof Weblog)
				obj = ((Weblog)parent).getWebFilesDirectory();
			
			if(obj instanceof File)
			{
				File f = (File)obj;
				if(!f.isDirectory())
					return;
				
				JFileChooser fc = new JFileChooser();
				if(lastDir != null)
				    fc.setCurrentDirectory(lastDir);
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setMultiSelectionEnabled(true);
				fc.setDialogTitle(Messages.getString("ThingamablogFrame.Import_File_Title")); //$NON-NLS-1$
				int r = fc.showOpenDialog(ThingamablogFrame.this);
				if(r == JFileChooser.CANCEL_OPTION)
					return;
				
				lastDir = fc.getCurrentDirectory();
				File sel[] = fc.getSelectedFiles();
				if(sel == null)
				{
					Utils.errMsg(ThingamablogFrame.this, Messages.getString("ThingamablogFrame.Invalid_File"), null); //$NON-NLS-1$
					return;
				}
				
				for(int i = 0; i < sel.length; i++)
				{
				    File outFile = new File(f, sel[i].getName());
				
				    //check if the file already exists and prompt for overwrite
				    if(outFile.exists())
				    {
				        int yn = JOptionPane.showConfirmDialog(FRAME,
				                "Overwrite? " + "[" + outFile.getName() + "]",
				                Messages.getString("ThingamablogFrame.Confirm"),
				                JOptionPane.YES_NO_OPTION);
					
				        if(yn == JOptionPane.NO_OPTION)
				            continue;					
				    }
				
				    Utils.copyFile(sel[i].getAbsolutePath(), outFile.getAbsolutePath());
				}
				refreshTree(blogTree);				
			}
		}	
	}
	
	private class NewWebFolderAction extends UpdatableAction
	{
		public NewWebFolderAction()
		{
			super(Messages.getString("ThingamablogFrame.New_Web_Folder"));	 //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.New_Web_Folder", this);
			putValue(Action.SMALL_ICON, 
				Utils.createIcon(TBGlobals.RESOURCES + "new_folder.png")); //$NON-NLS-1$
		}
		
		public void update()
		{			
			setEnabled(isWebFolderSelected());		
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(blogTree.isSelectionEmpty())
				return;
			Object obj = blogTree.getLastSelectedPathComponent();
			Object parent = blogTree.getSelectionPath().getParentPath().getLastPathComponent();
			//is the Web Files root dir selected?
			if(obj.toString().equals(WeblogTreeModel.WEB_SITE) && parent instanceof Weblog)
				obj = ((Weblog)parent).getWebFilesDirectory();
			
			if(obj instanceof File)
			{
				File f = (File)obj;
				if(!f.isDirectory())
					return;
				
				Object o = JOptionPane.showInputDialog(
					FRAME, Messages.getString("ThingamablogFrame.enter_folder_name_prompt"), Messages.getString("ThingamablogFrame.New_Folder"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.QUESTION_MESSAGE, null, null, Messages.getString("ThingamablogFrame.New_Folder")); //$NON-NLS-1$
				if(o != null)
				{
					File newFolder = new File(f, o.toString());
					if(newFolder.mkdir())
					{				
						refreshTree(blogTree);
					}
					else
					{
						Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.unable_to_create_folder_prompt"), null); //$NON-NLS-1$
					}						
				}
			}
		}	
	}
	
	//This Action deletes a File or Directory
	private class DeleteFileAction extends UpdatableAction
	{
		public DeleteFileAction()
		{
			super(Messages.getString("ThingamablogFrame.Delete")); //$NON-NLS-1$
		}
		
		public void update()
		{			
			boolean fileSel = blogTree.getLastSelectedPathComponent() instanceof File;			
			setEnabled(isWebFolderSelected() || fileSel);				
		}
		
		public void actionPerformed(ActionEvent e)
		{
			TreePath selPath = blogTree.getSelectionPath().getParentPath();
			if(selPath.getLastPathComponent() instanceof Weblog)
				return;
			if(blogTree.getLastSelectedPathComponent() instanceof File)
			{
				File f = (File)blogTree.getLastSelectedPathComponent();
				//don't delete the root directory
				if(f.getAbsolutePath().equals(
					curSelWeblog.getWebFilesDirectory().getAbsolutePath()))
						return;
				
				int yn = JOptionPane.showConfirmDialog(ThingamablogFrame.this,
							Messages.getString("ThingamablogFrame.Delete_Title") + " '" + f.getName() + "' ?", Messages.getString("ThingamablogFrame.Confirm"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);            
				if(yn == JOptionPane.NO_OPTION)
					return;
				
				if(f.isDirectory())
					deleteDir(f);
				else
					f.delete();
				
				refreshTree(blogTree);
			}	
		}
		
		//recursivly delete a directory
		private void deleteDir(File file)
		{
			if(file.isDirectory())
			{
				File contents[] = file.listFiles();
				for (int i = 0; i < contents.length; i++)
					 deleteDir(contents[i]);
			}		
			file.delete();
		}	
	}
	
	//Action to rename a file or directory
	private class RenameFileAction extends UpdatableAction
	{
		public RenameFileAction()
		{
			super(Messages.getString("ThingamablogFrame.Rename"));	 //$NON-NLS-1$
		}
		
		public void update()
		{			
			boolean fileSel = blogTree.getLastSelectedPathComponent() instanceof File;			
			setEnabled(isWebFolderSelected() || fileSel);	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			TreePath selPath = blogTree.getSelectionPath().getParentPath();
			if(selPath.getLastPathComponent() instanceof Weblog)
				return;
			if(blogTree.getLastSelectedPathComponent() instanceof File)
			{
				File f = (File)blogTree.getLastSelectedPathComponent();
				//don't rename the root directory
				if(f.getAbsolutePath().equals(
					curSelWeblog.getWebFilesDirectory().getAbsolutePath()))
						return;
				
				Object o = JOptionPane.showInputDialog(
					FRAME, Messages.getString("ThingamablogFrame.Rename_Title"), Messages.getString("ThingamablogFrame.Rename_Title"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.QUESTION_MESSAGE, null, null, f.getName());
				if(o != null)
				{
					if(f.renameTo(new File(f.getParent(), o.toString())))
					{				
						refreshTree(blogTree);
						if(f.isDirectory())
							curSelWeblog.markWebDirectoryUpdated(f);
					}
					else
					{
						Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.unable_to_rename_file_prompt"), null);	 //$NON-NLS-1$
					}	
				}				
			}
		}	
	}
	
	private class OpenFileAction extends UpdatableAction
	{
		public OpenFileAction()
		{
			super(Messages.getString("ThingamablogFrame.Open"));	 //$NON-NLS-1$
		}
		
		public void update()
		{
			TreePath p = blogTree.getSelectionPath();
			if(p != null && p.getLastPathComponent() instanceof File)
			{
				File f = (File)p.getLastPathComponent();
				if(f.isFile())
				{				
					setEnabled(TBGlobals.isImageFile(f) || TBGlobals.isTextFile(f));
				}
			}
			else
				setEnabled(false);	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			TreePath p = blogTree.getSelectionPath();
			if(p != null && p.getLastPathComponent() instanceof File)
			{
				File f = (File)p.getLastPathComponent();
				if(f.isFile())
					openWebFile(f);
			}	
		}	
	}
	
	//********************************
	//Action for editing a template
	//********************************
	private class EditTemplateAction extends UpdatableAction
	{
		public EditTemplateAction()
		{
			super(Messages.getString("ThingamablogFrame.Edit_Template"));	 //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Edit_Template", this);
		}
		
		public void update()
		{			
			TreePath p = blogTree.getSelectionPath();
			setEnabled(p != null && p.getLastPathComponent() instanceof Template);			
		}
		
		public void actionPerformed(ActionEvent e)
		{
			TreePath p = blogTree.getSelectionPath();
			if(p != null && p.getLastPathComponent() instanceof Template)
			{
				Template t = (Template)blogTree.getLastSelectedPathComponent();							
				HTMLEditor ed = new HTMLEditor(t);
				ed.setSize(640, 480);
				ed.setVisible(true);
				//ed.load();
			}	
		}	
	}
	
	
	//***************************************************
	//Action for sorting the table on the column argument
	//***************************************************
	private class SortTableAction extends AbstractAction
	{
		private int column;
		
		public SortTableAction(String label, int tableCol)
		{
			super(label);
			column = tableCol;				
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(column >= 0 && column < table.getColumnCount())
			{			
				table.sort(column, sortAscending);
				table.repaint();
			}
		}	
	}
	
	//*******************************************
	//Actions to view the next/previous table row
	//*******************************************
	private class NextAction extends UpdatableAction
	{
		public NextAction()
		{
			super(Messages.getString("ThingamablogFrame.Next"));	 //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Next", this);
		}
		
		public void update()
		{
			
		    setEnabled(table.getRowCount() > 0 && table.isShowing());	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int selRow = table.getSelectedRow() + 1;
			if(table.getRowCount() > 0 && selRow >= table.getRowCount())
			{
				table.setRowSelectionInterval(0, 0);
				scrollToTableRow(selRow);
			}						
			else if(selRow < table.getRowCount())
			{				
				table.setRowSelectionInterval(selRow, selRow);
				scrollToTableRow(selRow);
			}
		}
	}
	
	private class PreviousAction extends UpdatableAction
	{
		public PreviousAction()
		{
			super(Messages.getString("ThingamablogFrame.Previous"));	 //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Previous", this);
		}
		
		public void update()
		{
			setEnabled(table.getRowCount() > 0 && table.isShowing());				
		}
		
		public void actionPerformed(ActionEvent e)
		{
			int selRow = table.getSelectedRow() - 1;
			if(selRow < 0 && table.getRowCount() > 0)
			{			
				int r = table.getRowCount() - 1;
				table.setRowSelectionInterval(r, r);
				scrollToTableRow(r);
			}						
			else if(selRow >= 0)
			{			
				table.setRowSelectionInterval(selRow, selRow);
				scrollToTableRow(selRow);
			}			
		}	
	}
	
	private class NextUnreadAction extends UpdatableAction
	{
		public NextUnreadAction()
		{
			super(Messages.getString("ThingamablogFrame.Next_Unread_Item"));	 //$NON-NLS-1$
			Messages.setMnemonic("ThingamablogFrame.Next_Unread_Item", this);
		}
		
		public void update()
		{
			setEnabled(tableView == ITEM_VIEW && table.getRowCount() > 0);	
		}
		
		public void actionPerformed(ActionEvent e)
		{
			if(!(table.getModel() instanceof FeedTableModel))
				return;
			
			int selRow = table.getSelectedRow() + 1;
			FeedTableModel tm = (FeedTableModel)table.getModel();
			while(selRow < table.getRowCount() && tm.isItemAtRowRead(selRow))
			{
				selRow++;
			}
						
			if(selRow < table.getRowCount())
			{			
				table.setRowSelectionInterval(selRow, selRow);
				Rectangle rect = table.getCellRect(selRow, 0, true);
				table.scrollRectToVisible(rect);
			}
		}
	}
	
	
	//*************************************
	//Help menu actions
	//*************************************
	private class HomePageAction extends AbstractAction
	{
		public HomePageAction()
		{
			super(Messages.getString("ThingamablogFrame.Website") + "...");								
		}
		
		public void actionPerformed(ActionEvent e)
		{
			try
			{			
				Browser.displayURL(TBGlobals.APP_URL);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}
		}
	}
	
	
	
	
	
	
	//*************************************************
	//class for updating progress/tree on Feed updates
	//*************************************************	
	private class UpdateMonitor implements UpdateProgress
	{
		private boolean isUpdateStarted;
		private boolean isAborted;
		
		public void reset()
		{
			isAborted = false;
			updateFinish();	
		}
		
		public void updateStart(int numOfFeeds)
		{
			isUpdateStarted = true;
			isAborted = false;
			final int n = numOfFeeds;
			SwingUtilities.invokeLater(new Runnable() {
                public void run() {
        			statusBar.getJProgressBar().setMaximum(n);
        			statusBar.getJProgressBar().setValue(0);
        			updateActions();                    
                }
            });

		}
		
		public boolean isUpdateStarted()
		{
			return isUpdateStarted;			
		}
		
		public void feedUpdating(Feed feed)
		{			
			final Feed f = feed;
			SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                	statusBar.setRefreshingText(f.getTitle());
                }
            });
						
		}
		
		public int feedUpdated()
		{
			final int n = statusBar.getJProgressBar().getValue() + 1;
			SwingUtilities.invokeLater(new Runnable() {
                public void run() {
        			statusBar.getJProgressBar().setValue(n);
        			feedTree.repaint();//repaint the tree
                }
            });

			return n;
		}
		
		public int getUpdateSize()
		{
			return statusBar.getJProgressBar().getMaximum();
		}
		
		public void updateFinish()
		{
			SwingUtilities.invokeLater(new Runnable() {
                public void run() {
        			statusBar.setRefreshingText(""); //$NON-NLS-1$
        			statusBar.getJProgressBar().setValue(0);			
        			updateActions();                    
                }
            });			

			isUpdateStarted = false;
		}
		
		public void abortUpdate()
		{
			isAborted = true;	
		}
		
		public boolean isAborted()
		{
			return isAborted;
		}
	}
	
	
	//**********************************
	//Handles posts from entry editors
	//**********************************
	private class PostListener extends WindowAdapter
	{
		public void windowClosed(WindowEvent e)
		{
			Window w = e.getWindow();
			if(!(w instanceof EntryEditor))
				return;            
			EntryEditor editor = (EntryEditor)w;
			editorSize = editor.getSize();
			//isEditorWordWrap = editor.isWordWrap();
            			
			if(editor.hasUserCancelled())
				return;
            
			BlogEntry be = editor.getEntry();
			int mode = editor.getMode();
			boolean shouldPublish = editor.hasUserClickedPublish();
			Weblog edBlog = editor.getSelectedWeblog();
			
			try
			{
				if(mode == EntryEditor.UPDATE_ENTRY_MODE)
				{
					edBlog.updateEntry(be);
				}
				else if(mode == EntryEditor.NEW_ENTRY_MODE)
				{
					edBlog.addEntry(be);
				}                        	                        
			}
			catch(Exception ex)
			{
				Utils.errMsg(FRAME, Messages.getString("ThingamablogFrame.error_saving_entry_prompt"), ex); //$NON-NLS-1$
				logger.log(Level.WARNING, ex.getMessage(), ex);
			}
			finally
			{
				refreshTree(blogTree);
				refreshTable();
			}                    
			
			if(shouldPublish)
				publishWeblog(edBlog, PUB_CHANGED);        
		}    		
	}
	
	private class TimerHandler implements ActionListener
	{
	    public void actionPerformed(ActionEvent e)
	    {
	        if(isDBOpen && !updateMonitor.isUpdateStarted())
	            updateFolder(feedRoot);
	    }
	}
	
	
	//********************************************************************
	//Link actions - execute when a link is clickedo nthe HTMLOptionsPane
	//********************************************************************
	private class NewEntryLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Compose_Entry");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "new_entry32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	            showEditor(new BlogEntry(), EntryEditor.NEW_ENTRY_MODE);
	    }
	}
	
	private class ReadEntriesLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Read_Previous");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "read_previous32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	        {
	            if(!(blogTree.getLastSelectedPathComponent() instanceof Weblog))               
	                selectWeblog(curViewWeblog);
	            
	            //just move down to the current entries in the tree
	            int rows[] = blogTree.getSelectionRows();
	            blogTree.expandRow(rows[0]);
	            blogTree.setSelectionRow(rows[0] + 1);	            
	        }
	    }
	}
	
	private class ConfigureWeblogLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Configure_Weblog_Settings");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "configure32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	            showWeblogConfigDialog();
	    }
	}
	
	private class PublishLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Publish_Weblog");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "publish32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	            publishWeblog(curSelWeblog, PUB_CHANGED);
	    }
	}
	
	private class PublishAllLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Republish");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "publish_all32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	            publishWeblog(curSelWeblog, PUB_ALL);
	    }
	}
	
	private class PingLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Ping_Services");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "ping32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	            publishWeblog(curSelWeblog, PING_ONLY);
	    }
	}
	
	private class WeblogLink implements HTMLOptionLink
	{
	    private Weblog blog;
	    
	    public WeblogLink(Weblog w)
	    {
	        blog = w;
	    }
	    
	    public String getLinkText()
	    {
	        return blog.getTitle();
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "weblog32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(!(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED))
	            return;
	        
	        selectWeblog(blog);
	    }
	}
	
	private class NewWeblogLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return Messages.getString("ThingamablogFrame.Create_Weblog");
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(TBGlobals.RESOURCES + "new_weblog32.png");
	    }
	    
	    public void hyperlinkUpdate(HyperlinkEvent e)
	    {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
	            showNewWeblogWizard();
	    }
	}
		

    private class MyShutdownHook extends Thread 
    {
        public void run() 
        {            
        	if (isAppOpen) {
        		closeApp(false);
        	}
            if(curDB != null && isDBOpen)
    		{  		    
                System.err.println("Shutdown Hook");
    		    try{ 
    				backend.shutdown();    		    
    		    }catch(Exception ex){}                
    		}
        }
    }
}
