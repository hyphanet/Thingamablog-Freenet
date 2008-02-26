
package net.sf.thingamablog.gui.app;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.io.IOUtils;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.SwingWorker;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.backend.HSQLDatabaseBackend;
import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.PingService;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.Template;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogList;
import net.sf.thingamablog.feed.Feed;
import net.sf.thingamablog.feed.FeedBackendException;
import net.sf.thingamablog.feed.FeedFolder;
import net.sf.thingamablog.feed.FeedItem;
import net.sf.thingamablog.feed.UpdateProgress;
import net.sf.thingamablog.gui.CustomFileFilter;
import net.sf.thingamablog.gui.GUILoginPrompt;
import net.sf.thingamablog.gui.ImageViewerDialog;
import net.sf.thingamablog.gui.JSplash;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.TBAbout;
import net.sf.thingamablog.gui.TBHelpAction;
import net.sf.thingamablog.gui.UpdatableAction;
import net.sf.thingamablog.gui.ViewerPane;
import net.sf.thingamablog.gui.editor.EntryEditor;
import net.sf.thingamablog.gui.editor.HTMLEditor;
import net.sf.thingamablog.gui.properties.TBFlogNodeWizardDialog;
import net.sf.thingamablog.gui.properties.TBFlogWizardDialog;
import net.sf.thingamablog.gui.properties.TBWizardDialog;
import net.sf.thingamablog.gui.properties.WeblogPropertiesDialogFactory;
import net.sf.thingamablog.gui.table.JSortTable;
import net.sf.thingamablog.transport.LoginFactory;
import net.sf.thingamablog.xml.OPMLImportExport;
import net.sf.thingamablog.xml.RSSImportExport;
import net.sf.thingamablog.xml.TBPersistFactory;

import org.jdesktop.jdic.desktop.Desktop;

import com.l2fprod.common.swing.JDirectoryChooser;



/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class ThingamablogFrame extends JFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app"); //$NON-NLS-1$
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.gui.app"); //$NON-NLS-1$
    
    private final JFrame FRAME = this;
	private static final Image ICON = 
		UIUtils.getIcon(UIUtils.X16, "tamb.png").getImage(); //$NON-NLS-1$
    
	private Dimension editorSize = new Dimension(640, 480);
	//private boolean isEditorWordWrap = true;
		
	//publish type constants 
	//private static int PUB_CURRENT = 0;
	private static int PUB_CHANGED = 1;
	private static int PUB_ALL = 2;
	private static int PING_ONLY = 3;
	
	private static final int ITEM_VIEW = -1;
	private static final int ENTRY_VIEW = -2;
	private int tableView = ENTRY_VIEW;	

	//private int curLayoutStyle = TBGlobals.getLayoutStyle();	
	private JPanel contentPanel;
	private JPanel blogTreePanel;
	private JPanel feedTreePanel;
	private JSplitPane tableViewerDivider;
	private JSplitPane hSplitPane;	
	private JSplitPane feedSplitPane;
	
	private JPanel viewerPanel;
	private CardLayout viewerPanelLayout = new CardLayout();
	private HTMLOptionsPane htmlOptionsPane;
	private final String HTML_VIEW = "HTML_VIEW"; //$NON-NLS-1$
	private final String TABLE_VIEW = "TABLE_VIEW"; //$NON-NLS-1$
	
	private JTree blogTree;
	private JTree feedTree;
	private WeblogTreeModel weblogTreeModel;
	private FeedTreeModel feedTreeModel;
	
	private ViewerPane viewerPane;
	private TBViewerPaneModel viewerPaneModel = new TBViewerPaneModel();
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
	//private JPopupMenu viewerPopup = null;
	
	private StatusBar statusBar;
	
	private WeblogList weblogList = new WeblogList();
		
	private UpdateMonitor updateMonitor = new UpdateMonitor();
	//toolbar button for starting/cancelling feed updates
	private AbstractButton updateButton = new JButton(); 
	
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
	
    private Action refreshBlogTreeAction = new RefreshBlogTreeAction();
    private Action openFolderAction;
	private Action newEntryAction;
	private Action newEntryFromItemAction;
	private Action editEntryAction;
	private Action deleteEntriesAction;
	private Action weblogPropertiesAction;
	private Action editTemplateAction;
	private Action exportWeblogToRSSAction;
	private Action importEntriesFromFeedAction;
    private Action exportTemplatesAction;
	//private Action importLegacyWeblogAction;
	
	private Action newWeblogAction;
        private Action newFlogAction;
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
    private Action importFromEmailAction;
	
	private Action viewWeblogAction;
    
    protected Action showTasksAction;
	
	private Vector actions = new Vector(20, 5);	
	
	private FeedTreeMoveHandler moveHandler;
	private FeedFolder feedRoot = new FeedFolder(i18n.str("my_subscriptions")); //$NON-NLS-1$
	private Feed lastSelFeed;
	private Weblog curSelWeblog;
	private Weblog curViewWeblog;
	
	private TBSearchDialog searchDialog;
	
	private File curDB;
	private boolean isDBOpen;
	private boolean	isAppOpen = false;
    	
	private javax.swing.Timer feedUpdateTimer;
    private javax.swing.Timer mailCheckTimer;
    
    private TaskDialog taskDialog;
	
	public ThingamablogFrame()
	{				
	    // give the user something to look at while we're loading the app
        if(TBGlobals.isStartWithSplash())
        {
            JSplash ss = new JSplash(this, UIUtils.MISC + "splash.gif", 6000);           //$NON-NLS-1$
            ss.setVisible(true);
        }
        
        Runtime.getRuntime().addShutdownHook(new MyShutdownHook());
	    
		tableColumnModel.loadColumnData();
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
		statusBar = new StatusBar(this);		
		
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
		blogTree = new JTree(weblogTreeModel)
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public String getToolTipText(MouseEvent event) 
            {
                Point point = event.getPoint();
                TreePath path = getPathForLocation(point.x, point.y);
                if(path == null) 
                {
                    return null;
                }
                
                if(path.getLastPathComponent() instanceof Weblog)
                {
                    Weblog b = (Weblog)path.getLastPathComponent();
                    String tttext = "<html>" + b.getTitle(); //$NON-NLS-1$
                    
                    if(b.isPublishFailed())                    
                        tttext += "<p><b>" + i18n.str("publish_failed_tooltip") + "</p></b>"; //$NON-NLS-1$
                    
                    if(b.isMailCheckFailed())
                        tttext += "<p><b>" + i18n.str("mail_check_failed_tooltip") + "</p></b>"; //$NON-NLS-1$
                    tttext += "</html>"; //$NON-NLS-1$
                    return tttext;
                }
                
                return super.getToolTipText(event);
            }
        };
        ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
        toolTipManager.setInitialDelay(1000);
        toolTipManager.registerComponent(blogTree);
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
		
		JToolBar feedToolBar = new JToolBar(SwingConstants.HORIZONTAL);
		feedToolBar.setFloatable(false);
		feedToolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //$NON-NLS-1$
		UIUtils.addToolBarButton(feedToolBar, newFeedFolderAction);
		UIUtils.addToolBarButton(feedToolBar, newFeedAction);
		UIUtils.addToolBarButton(feedToolBar, deleteFromFeedTreeAction);		
		JPanel feedUpperPanel = new JPanel(new BorderLayout());
		JLabel feedUpperPanelLabel = new JLabel(i18n.str("news_feeds")); //$NON-NLS-1$
		feedUpperPanelLabel.setIcon(UIUtils.getIcon(UIUtils.X16, "feed_icon.png")); //$NON-NLS-1$
		feedUpperPanelLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));		
		feedUpperPanel.add(feedUpperPanelLabel, BorderLayout.CENTER);
		feedUpperPanel.add(feedToolBar, BorderLayout.EAST);
		feedTreePanel.add(feedUpperPanel, BorderLayout.NORTH);
		feedTreePanel.add(new JScrollPane(feedTree), BorderLayout.CENTER);
		feedTree.addFocusListener(focusListener);		
		
		/*viewerPane.getJEditorPane().addMouseListener(new PopupMenuListener());
		viewerPopup = new JPopupMenu();
		viewerPopup.add(viewerPane.getCopyAction());
		viewerPopup.add(viewerPane.getSelectAllAction());*/

		//Backackground has to be set to the table color		
		JScrollPane scroller = new JScrollPane(table);
		scroller.getViewport().setBackground(table.getBackground()); 
		scroller.setHorizontalScrollBarPolicy(
			  ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
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
		
		//searchDialog = new TBSearchDialog(this, weblogList, feedRoot); TODO
		//searchDialog.addActionListener(new FindHandler());		
		
		loadProperties();
		updateActions();
		
		feedUpdateTimer = new javax.swing.Timer(
		        TBGlobals.getFeedUpdateInterval(), new TimerHandler());
        mailCheckTimer = new javax.swing.Timer(10000, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //System.err.println("firing timer");
                if(!isCheckingEmail)
                {
                    Weblog b = getBlogMostOverdueForEmailCheck();
                    //System.err.println(b);
                    if(b != null)
                    {
                        importEntriesFromEmail(b);
                    }
                }                    
            }
        });
		
		if(TBGlobals.isStartWithLastDatabase() && 
		TBGlobals.getLastOpenedDatabase() != null)
		{
			openDB(new File(TBGlobals.getLastOpenedDatabase()));	
		}
		else		
		    blogTree.setSelectionRow(0);
		
		isAppOpen = true;
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
        
        taskDialog = new TaskDialog(FRAME);
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
		i18n.str("first_run_prompt"); //$NON-NLS-1$
		
		int r = JOptionPane.showConfirmDialog(
			FRAME, msg, i18n.str("confirm"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
		
		if(r == JOptionPane.YES_OPTION)
			createNewDatabase();				
	}
	
	private void createNewDatabase()
	{
		JDirectoryChooser fc = new JDirectoryChooser();
        fc.setAccessory(new JLabel("<html>" + i18n.str("new_database_prompt") + "</html>"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle(i18n.str("new_database_title")); //$NON-NLS-1$
		int r = fc.showDialog(FRAME, i18n.str("ok")); //$NON-NLS-1$
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
				i18n.str("db_exists_prompt"), i18n.str("warning"), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.WARNING_MESSAGE);
			return;				
		}
		
		File tmplXML = new File(TBGlobals.DEFAULT_TMPL_DIR, TBGlobals.USER_XML_FILENAME);	
			
		try
        {
            IOUtils.copy(tmplXML, xml);
        }        
        catch(IOException e)
        {
            UIUtils.showError(FRAME, e);
        }
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
				            i18n.str("my_subscriptions")); //$NON-NLS-1$
				    weblogList = new WeblogList();
				    
                    
				    System.out.println("LOADING DATA"); //$NON-NLS-1$
				    TBPersistFactory.loadData(xmlPath, weblogList, feedRoot, backend, backend);
				    System.out.println("DONE LOADING DATA"); //$NON-NLS-1$
				    
                    
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
                    UIUtils.showError(FRAME, i18n.str("error"), exc);			         //$NON-NLS-1$
			    }
				else if(obj == null || obj.toString().equals(false + "")) //$NON-NLS-1$
				{
				    UIUtils.showError(FRAME, i18n.str("unable_to_open_database")); //$NON-NLS-1$
				    //return;
				}
			    feedTreeModel = new FeedTreeModel(feedRoot);
				weblogTreeModel = new WeblogTreeModel(weblogList);
				for(int i = 0; i < weblogList.getWeblogCount(); i++)
                {
			        taskDialog.addWeblog(weblogList.getWeblogAt(i));
                }
						
				blogTree.setModel(weblogTreeModel);			
				feedTree.setModel(feedTreeModel);
				blogTree.expandRow(0);
				
				//Select previously selected weblog
				String key = TBGlobals.getProperty("LAST_SEL_BLOG"); //$NON-NLS-1$
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
                mailCheckTimer.start();
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
            mailCheckTimer.stop();
            taskDialog.removeAllWeblogs();
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
		
        openFolderAction = new OpenFolderAction();
        actions.add(openFolderAction);
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
        exportTemplatesAction = new ExportTemplatesAction();
        actions.add(exportTemplatesAction);
		//importLegacyWeblogAction = new ImportLegacyWeblogAction();
		//actions.add(importLegacyWeblogAction);
        
        showTasksAction = new ShowTaskDialogAction();
        actions.add(showTasksAction);
		
		publishAction = new PublishAction();
		actions.add(publishAction);
		publishAllAction = new PublishAllAction();
		actions.add(publishAllAction);
		weblogPingAction = new WeblogPingAction();
		actions.add(weblogPingAction);
		viewWeblogAction = new ViewWeblogAction();
		actions.add(viewWeblogAction);
        importFromEmailAction = new ImportFromEmailAction();
        actions.add(importFromEmailAction);
		
		weblogPropertiesAction = new WeblogPropertiesAction();
		actions.add(weblogPropertiesAction);
		newWeblogAction = new NewWeblogAction();
		actions.add(newWeblogAction);
                newFlogAction = new NewFlogAction();
                actions.add(newFlogAction);
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
				
		UIUtils.addToolBarButton(toolBar, newEntryAction);		
		UIUtils.addToolBarButton(toolBar, deleteEntriesAction);	
		toolBar.addSeparator();		
		UIUtils.addToolBarButton(toolBar, searchAction);		
		toolBar.addSeparator();
		
		//Got to use an instance var for the button, so we can access it later
		//update button changes its state when updating
		updateButton = UIUtils.addToolBarButton(toolBar, updateAllFeedsAction);				
		toolBar.addSeparator();
		
		UIUtils.addToolBarButton(toolBar, publishAction);		
		UIUtils.addToolBarButton(toolBar, publishAllAction);		
		
        UIUtils.addToolBarButton(toolBar, importFromEmailAction);        
        UIUtils.addToolBarButton(toolBar, weblogPingAction);
        toolBar.addSeparator();
        
		UIUtils.addToolBarButton(toolBar, viewWeblogAction);	
		toolBar.addSeparator();
		
		UIUtils.addToolBarButton(toolBar, weblogPropertiesAction);	
		toolBar.addSeparator();
		
		UIUtils.addToolBarButton(toolBar, nextAction);
		UIUtils.addToolBarButton(toolBar, prevAction);	
		
		return toolBar;
	}
	
	private JMenuBar createMenuBar()
	{
		JMenuBar mb = new JMenuBar();
		
		//create the file menu
		JMenu fileMenu = new JMenu(i18n.str("file")); //$NON-NLS-1$
		fileMenu.setMnemonic(i18n.mnem("file")); //$NON-NLS-1$
		fileMenu.add(newWeblogAction);
                fileMenu.add(newFlogAction);
		fileMenu.add(newEntryAction);
		//fileMenu.add(newFeedAction);
		fileMenu.addSeparator();
		fileMenu.add(openDBAction);
		fileMenu.add(newDBAction);
		fileMenu.addSeparator();
        fileMenu.add(new InstallTemplatePackAction());
        fileMenu.addSeparator();
        
        JMenu importMenu = new JMenu(i18n.str("import")); //$NON-NLS-1$
		importMenu.setMnemonic(i18n.mnem("import")); //$NON-NLS-1$
		//importMenu.add(importLegacyWeblogAction);
		importMenu.add(importEntriesFromFeedAction);
		importMenu.add(importFeedFolderAction);
		fileMenu.add(importMenu);
		
		JMenu exportMenu = new JMenu(i18n.str("export")); //$NON-NLS-1$
		exportMenu.setMnemonic(i18n.mnem("export")); //$NON-NLS-1$
		exportMenu.add(exportWeblogToRSSAction);
		exportMenu.add(exportFeedFolderAction);		        
		fileMenu.add(exportMenu);
		
		JMenuItem exitItem = new JMenuItem(i18n.str("exit")); //$NON-NLS-1$
		exitItem.setMnemonic(i18n.mnem("exit")); //$NON-NLS-1$
		exitItem.addActionListener(new ActionListener()
		{public void actionPerformed(ActionEvent e){closeApp(true);}});
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		mb.add(fileMenu);
        
        //edit menu
		JMenu editMenu = new JMenu(i18n.str("edit")); //$NON-NLS-1$
		editMenu.setMnemonic(i18n.mnem("edit")); //$NON-NLS-1$
		editMenu.add(TextEditPopupManager.getInstance().getAction(TextEditPopupManager.COPY));
		editMenu.add(TextEditPopupManager.getInstance().getAction(TextEditPopupManager.SELECT_ALL));		
		editMenu.addSeparator();
		editMenu.add(deleteEntriesAction);
		editMenu.add(editEntryAction);
		editMenu.addSeparator();
		editMenu.add(findEntriesAction);
		editMenu.add(findItemsAction); 
		mb.add(editMenu);
		
		//view menu
		JMenu viewMenu = new JMenu(i18n.str("view")); //$NON-NLS-1$
		viewMenu.setMnemonic(i18n.mnem("view")); //$NON-NLS-1$
		sortMenu = new JMenu(i18n.str("sort_by"));//updated whenever the view type changes //$NON-NLS-1$
		sortMenu.setMnemonic(i18n.mnem("sort")); //$NON-NLS-1$
		viewMenu.add(sortMenu);
		viewMenu.addSeparator();
		viewMenu.add(nextAction);
		viewMenu.add(prevAction);
		viewMenu.addSeparator();
		viewMenu.add(nextUnreadAction);	
        viewMenu.add(showTasksAction);
		viewMenu.addSeparator();
		viewMenu.add(viewWeblogAction);
		mb.add(viewMenu);
		
		//Weblogs menu
		JMenu weblogsMenu = new JMenu(i18n.str("weblog")); //$NON-NLS-1$
		weblogsMenu.setMnemonic(i18n.mnem("weblog")); //$NON-NLS-1$
		weblogsMenu.add(newEntryAction);		
		weblogsMenu.addSeparator();
		weblogsMenu.add(publishAction);
		weblogsMenu.add(publishAllAction);
        weblogsMenu.add(importFromEmailAction);
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
		JMenu feedMenu = new JMenu(i18n.str("news")); //$NON-NLS-1$
		feedMenu.setMnemonic(i18n.mnem("news"));	 //$NON-NLS-1$
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
		JMenu configMenu = new JMenu(i18n.str("configure")); //$NON-NLS-1$
		configMenu.setMnemonic(i18n.mnem("configure")); //$NON-NLS-1$
		JMenuItem optsItem = new JMenuItem(i18n.str("options_")); //$NON-NLS-1$
		optsItem.setMnemonic(i18n.mnem("options_")); //$NON-NLS-1$
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
		optsItem.setIcon(UIUtils.getIcon(UIUtils.X16, "config.png")); //$NON-NLS-1$
		configMenu.add(optsItem);
		mb.add(configMenu);	
		
		JMenu helpMenu = new JMenu(i18n.str("help")); //$NON-NLS-1$
		helpMenu.setMnemonic(i18n.mnem("help")); //$NON-NLS-1$
		
		Action helpContents = 
		    new TBHelpAction(
		            i18n.str("help_contents_"), "index"); //$NON-NLS-1$ //$NON-NLS-2$
		helpContents.putValue(Action.SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "help.png")); //$NON-NLS-1$
		helpContents.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		helpContents.putValue(Action.MNEMONIC_KEY, new Integer(i18n.mnem("help_contents_")));		 //$NON-NLS-1$
		Action tutorial = new TBHelpAction(
		        i18n.str("tutorial_"), "ch02.index"); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*JMenuItem donateItem = new JMenuItem(i18n.str("donate") + "...");
		donateItem.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        try{
		        Browser.displayURL(
		            "http://sourceforge.net/donate/index.php?group_id=86787");
		        }catch(Exception ex){}
		    }
		});*/
		
		helpMenu.add(helpContents);	
		helpMenu.add(tutorial);
		helpMenu.addSeparator();
		helpMenu.add(new HomePageAction());
		//helpMenu.add(donateItem);
		helpMenu.addSeparator();	
		
		JMenuItem aboutItem = new JMenuItem(i18n.str("about_")); //$NON-NLS-1$
		aboutItem.setMnemonic(i18n.mnem("about_")); //$NON-NLS-1$
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
		
		String key = ""; //$NON-NLS-1$
		if(curSelWeblog != null)
		    key = curSelWeblog.getKey();
		TBGlobals.putProperty("LAST_SEL_BLOG", key); //$NON-NLS-1$
		
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
		    JOptionPane.showMessageDialog(FRAME, i18n.str("no_database_is_open")); //$NON-NLS-1$
		    return;
		}
	    
	    TBWizardDialog wiz = new TBWizardDialog(FRAME, curDB, backend);
		wiz.setLocationRelativeTo(FRAME);
		wiz.setVisible(true);
		
		if(!wiz.hasUserCancelled())
		{
			Weblog w = wiz.getWeblog();
            weblogList.addWeblog(w);
            taskDialog.addWeblog(w);
            
			weblogTreeModel.setData(weblogList);				
			if(searchDialog != null)
				searchDialog.setWeblogList(weblogList);
				
			//updateActions();
			saveCurrentData();
			selectWeblog(wiz.getWeblog());
		}
	}
        
        	
        private void showNewFlogWizard()
	{
		if(!isDBOpen)
		{
		    JOptionPane.showMessageDialog(FRAME, i18n.str("no_database_is_open")); //$NON-NLS-1$
		    return;
		}
	    
                TBFlogNodeWizardDialog wiz = new TBFlogNodeWizardDialog(FRAME, curDB, backend);
		wiz.setLocationRelativeTo(FRAME);
		wiz.setVisible(true);
		
		if(!wiz.hasUserCancelled())
		{
			Weblog w = wiz.getWeblog();
            weblogList.addWeblog(w);
            taskDialog.addWeblog(w);
            
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
			sortMenu.add(new SortTableAction(i18n.str("read"), FeedTableModel.READ_COL)); //$NON-NLS-1$
			sortMenu.add(new SortTableAction(FeedTableModel.ITEM.toString(),
				FeedTableModel.ITEM_COL));
			sortMenu.add(new SortTableAction(FeedTableModel.DATE.toString(), 
				FeedTableModel.DATE_COL));
		}
		
		final JRadioButtonMenuItem ascItem = new JRadioButtonMenuItem(i18n.str("ascending")); //$NON-NLS-1$
		final JRadioButtonMenuItem descItem = new JRadioButtonMenuItem(i18n.str("descending")); //$NON-NLS-1$
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
				    UIUtils.showError(FRAME, 
						i18n.str("feed_update_error_prompt"), ioe);	 //$NON-NLS-1$
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
    
    private boolean isCheckingEmail, isPublishing;
    
    private Weblog getBlogMostOverdueForEmailCheck()
    {        
        Weblog oldest = null;
        for(int i = 0; i < weblogList.getWeblogCount(); i++)
        {            
            Weblog temp = weblogList.getWeblogAt(i);
            if(temp.isOutdated() && !temp.isCheckingEmail())
            {
                if(oldest == null || temp.getLastEmailCheck() == null || 
                    oldest.getLastEmailCheck().after(temp.getLastEmailCheck()))
                    oldest = temp;   
            }
        }
        
        return oldest;
    }
    
    protected void showTaskDialog()
    {
        if(!taskDialog.isVisible())
        {
            taskDialog.setLocationRelativeTo(FRAME);
            taskDialog.setVisible(true);            
        }
    }
    
    private void importEntriesFromEmail(final Weblog blog)
    {
        final LogPanel d = taskDialog.getLogPanel(blog);//logPanel;
        if(d == null || blog.isPublishing() || blog.isCheckingEmail())
            return;
               
        Thread runner = new Thread()
        {
            public void run()
            {
                
                boolean hadEntries = false;
                
                try
                {                   
                    d.reset();
                    hadEntries = blog.importEntriesFromEmail(d);                    
                }
                catch(Exception ex)
                {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                    ex.printStackTrace();
                }
                finally
                {
                    final boolean shouldPub = hadEntries;
                    Runnable r = new Runnable()
                    {
                        public void run()
                        {                            
                            if(shouldPub)
                                refreshTable();
                            updateActions();
                            if(!isPublishing)
                                statusBar.getJProgressBar().setIndeterminate(false);
                            blogTree.repaint();
                            if(shouldPub)
                                publishWeblog(blog, PUB_CHANGED);
                        }
                    };
                    isCheckingEmail = false;
                    SwingUtilities.invokeLater(r);                    
                }
            }
        };
                
        
        if(blog.isImportFromEmailEnabled())
        {
            isCheckingEmail = true;
            if(LoginFactory.emailLogin(blog, new GUILoginPrompt(FRAME)))
            {
                runner.start();
                statusBar.getJProgressBar().setIndeterminate(true);
                taskDialog.showDetails(blog);            
                this.updateActions();
            }
            else
            {
                //user probably canceled email login
                isCheckingEmail = false;
                blog.setLastEmailCheck(new Date());
            }
        }
    }
	
	private void publishWeblog(final Weblog blog, final int pubType)
	{		
		/*final PublishDialog d = new PublishDialog(
					FRAME, i18n.str("publishing") + ": " + blog.getTitle(), false); //$NON-NLS-1$
		*/
        
        final LogPanel d = taskDialog.getLogPanel(blog);//logPanel;
        if(d == null || blog.isPublishing() || blog.isCheckingEmail())
            return;
                
		Thread runner = new Thread()
		{
			public void run()
			{
				try
				{					
                    d.reset();
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
                finally
                {
                    Runnable r = new Runnable()
                    {
                        public void run()
                        {
                            
                            updateActions();
                            if(!isCheckingEmail)
                                statusBar.getJProgressBar().setIndeterminate(false);
                            blogTree.repaint();
                        }
                    };
                    isPublishing = false;
                    SwingUtilities.invokeLater(r);                    
                }
			}
		};
		
		/*if(blog.isPublishing())
			return;*/
		
		boolean okToPublish = false;		
		if(pubType == PING_ONLY)
			okToPublish = hasServicesToPing(blog);
		else
		    okToPublish = LoginFactory.publishLogin(blog, new GUILoginPrompt(FRAME));
			//okToPublish = LoginFactory.login(curSelWeblog, new GUILoginPrompt(FRAME));
			
		if(okToPublish)
		{
			/*d.setLocationRelativeTo(FRAME);
			d.setVisible(true);*/
			isPublishing = true;
            runner.start();            
            taskDialog.showDetails(blog);            
            statusBar.getJProgressBar().setIndeterminate(true);
            this.updateActions();
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
			//ed.setSize(640, 480);						
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
                int row = blogTree.getClosestRowForLocation(e.getX(), e.getY());
                
                if(row != -1 && blogTree.getPathForRow(row).getLastPathComponent().equals(node))
                {    				
                    if(node instanceof Template)
    				{
    					Template t = (Template)blogTree.getLastSelectedPathComponent();
    							
    					HTMLEditor ed = new HTMLEditor(t);
    					//ed.setSize(640, 480);
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
                    //System.err.println(e.getPath().getLastPathComponent());
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
			    ClassLoader.getSystemResource(UIUtils.MISC + "tamb96.png"));	         //$NON-NLS-1$
			htmlOptionsPane.addHeading(i18n.str("weblog_management"));		 //$NON-NLS-1$
			htmlOptionsPane.addOption(new NewEntryLink());
			htmlOptionsPane.addOption(new ReadEntriesLink());
			htmlOptionsPane.addOption(new ConfigureWeblogLink());
			htmlOptionsPane.addHeading(i18n.str("publishing")); //$NON-NLS-1$
			htmlOptionsPane.addOption(new PublishLink());
			htmlOptionsPane.addOption(new PublishAllLink());
			htmlOptionsPane.addOption(new PingLink());			
	    }
	    else if(obj == WeblogTreeModel.ROOT)
	    {
	        htmlOptionsPane.setTitle(WeblogTreeModel.ROOT.toString());
	        htmlOptionsPane.setImageURL(
			    ClassLoader.getSystemResource(UIUtils.MISC + "webpages.png")); //$NON-NLS-1$
	        
	        htmlOptionsPane.addOption(new NewWeblogLink());
	        if(weblogList.getWeblogCount() > 0)
	        {
	            htmlOptionsPane.addHeading(i18n.str("weblogs")); //$NON-NLS-1$
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
            blogTreePopup.add(openFolderAction);
            blogTreePopup.addSeparator();
			blogTreePopup.add(newWebFolderAction);
			blogTreePopup.add(importFileAction);
            blogTreePopup.addSeparator();            
            blogTreePopup.add(refreshBlogTreeAction);
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
        p.add(importFromEmailAction);
		p.add(weblogPingAction);
		p.addSeparator();
        p.add(viewWeblogAction);
        p.addSeparator();
		p.add(weblogPropertiesAction);
		p.addSeparator();
		p.add(deleteWeblogAction);
		p.addSeparator();
		p.add(importEntriesFromFeedAction);
		p.add(exportWeblogToRSSAction);
        p.add(exportTemplatesAction);
        return p;	
	}
		
	private JPopupMenu createDirectorySelectedPopup()
	{
		JPopupMenu p = new JPopupMenu();	
        p.add(openFolderAction);
        p.addSeparator();
		p.add(newWebFolderAction);
		p.add(importFileAction);
		p.addSeparator();			
		p.add(deleteFileAction);
		p.add(renameFileAction);
        p.addSeparator();
        p.add(refreshBlogTreeAction);
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
                viewerPaneModel.setWeblog(curViewWeblog);
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
				//else if(c == viewerPane.getJEditorPane() && viewerPopup != null)
				//	viewerPopup.show(c, e.getX(), e.getY());
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
		
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewEntryAction()
		{			
			super(i18n.str("new_entry_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_entry_"))); //$NON-NLS-1$
			
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "edit1.png")); //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "edit1.png"));
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewEntryFromItemAction()
		{
			super(i18n.str("post_to_weblog_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("post_to_weblog_"))); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private boolean readOrUnread;
		public MarkSelectedItemsAction(boolean read)
		{
			super(null);			
			if(read)
			{
				putValue(NAME, i18n.str("mark_items_read")); //$NON-NLS-1$							
			}
			else
			{
				putValue(NAME, i18n.str("mark_items_unread"));											 //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private boolean read;
		public MarkCurrentFeedAction(boolean read)
		{			
			this.read = read;
			if(read)
			{			
				putValue(NAME, i18n.str("mark_feed_read")); //$NON-NLS-1$
                putValue(MNEMONIC_KEY, new Integer(i18n.mnem("mark_feed_read"))); //$NON-NLS-1$
			}
			else
			{			
				putValue(NAME, i18n.str("mark_feed_unread"));//$NON-NLS-1$
                putValue(MNEMONIC_KEY, new Integer(i18n.mnem("mark_feed_unread"))); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ImportFeedFolderAction()
		{
			super(i18n.str("import_feeds_from_opml_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("import_feeds_from_opml_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "import.png"));	 //$NON-NLS-1$
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
				fc.setDialogTitle(i18n.str("import_from_opml")); //$NON-NLS-1$
				int r = fc.showOpenDialog(ThingamablogFrame.this);
				fc.setApproveButtonText(i18n.str("import")); //$NON-NLS-1$
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
					UIUtils.showError(FRAME, i18n.str("error_importing_opml_prompt")); //$NON-NLS-1$
					ex.printStackTrace();
                    logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}	
		}	
	}
	
	
	private class ExportFeedFolderAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ExportFeedFolderAction()
		{
			super(i18n.str("export_feeds_to_opml_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("export_feeds_to_opml_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "export.png"));	 //$NON-NLS-1$
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
				fc.setDialogTitle(i18n.str("export_to_opml_")); //$NON-NLS-1$
				fc.setSelectedFile(new File(TBGlobals.USER_HOME, parent.getName() + ".opml")); //$NON-NLS-1$
				int r = fc.showSaveDialog(FRAME);
				fc.setApproveButtonText(i18n.str("export")); //$NON-NLS-1$
				if(r == JFileChooser.CANCEL_OPTION)
					return;
        		
				if(fc.getSelectedFile() == null)
					return;
        			
				File sel = fc.getSelectedFile();
				if(sel == null)
					return;
					
				if(sel.exists())
				{
					int yn = JOptionPane.showConfirmDialog(FRAME, i18n.str("overwrite_existing_file_prompt"), //$NON-NLS-1$
						i18n.str("confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
					
					if(yn == JOptionPane.NO_OPTION)
						return;					
				}				
				
				try
				{
					OPMLImportExport.exportFolderToOPML(parent, sel.getAbsolutePath());					
				}
				catch(Exception ex)
				{
					UIUtils.showError(FRAME, i18n.str("error_exporting_folder_prompt")); //$NON-NLS-1$
					ex.printStackTrace();
                    logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}	
		}	
	}
	
	//**********************************************
	//Actions for importing/exporting weblog entries
	//**********************************************
	/*private class ImportLegacyWeblogAction extends UpdatableAction
	{
	    public ImportLegacyWeblogAction()
	    {
			super(i18n.str("import_09x_weblog_")); //$NON-NLS-1$
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
	}*/
	
	private class ImportEntriesFromFeedAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ImportEntriesFromFeedAction()
		{
			super(i18n.str("import_entries_from_feed_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("import_entries_from_feed_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "import.png")); //$NON-NLS-1$
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
    
    private class ExportTemplatesAction extends UpdatableAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ExportTemplatesAction()
        {
            
            super(i18n.str("export_templates_")); //$NON-NLS-1$
            //Messages.setMnemonic("ThingamablogFrame.Export_Weblog_to_Feed", this);
            putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "export.png")); //$NON-NLS-1$
            
        }
        
        public void update()
        {
            setEnabled(blogTree.getLastSelectedPathComponent() instanceof TBWeblog);
        }
        
        public void actionPerformed(ActionEvent e)
        {
            if(blogTree.getLastSelectedPathComponent() instanceof TBWeblog)
            {
                TBWeblog w = (TBWeblog)blogTree.getLastSelectedPathComponent();
                ExportTemplatePackDialog d = new ExportTemplatePackDialog(FRAME, w);
                d.setLocationRelativeTo(FRAME);
                d.setVisible(true);
            }
        }
    }
	
	private class ExportWeblogToRSSAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ExportWeblogToRSSAction()
		{
			super(i18n.str("export_weblog_to_feed_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("export_weblog_to_feed_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "export.png"));//$NON-NLS-1$
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
				fc.setDialogTitle(i18n.str("export_to_feed")); //$NON-NLS-1$
				fc.setSelectedFile(new File(TBGlobals.USER_HOME, w.getTitle() + ".xml")); //$NON-NLS-1$
				int r = fc.showSaveDialog(FRAME);
				fc.setApproveButtonText(i18n.str("export")); //$NON-NLS-1$
				if(r == JFileChooser.CANCEL_OPTION)
					return;
        		
				if(fc.getSelectedFile() == null)
					return;
        			
				File sel = fc.getSelectedFile();
				if(sel == null)
					return;
					
				if(sel.exists())
				{
					int yn = JOptionPane.showConfirmDialog(FRAME, i18n.str("overwrite_existing_file_prompt"), //$NON-NLS-1$
						i18n.str("confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //$NON-NLS-1$
					
					if(yn == JOptionPane.NO_OPTION)
						return;					
				}				
				
				try
				{
					RSSImportExport.exportWeblogToFeed(w, sel);					
				}
				catch(Exception ex)
				{
					UIUtils.showError(FRAME, i18n.str("error_exporting_folder_prompt")); //$NON-NLS-1$
					ex.printStackTrace();
                    logger.log(Level.WARNING, ex.getMessage(), ex);
				}
			}
		}	
	}	
	
	private class NewFeedFolderAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewFeedFolderAction()
		{
			super(i18n.str("new_feed_folder_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_feed_folder_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "new_folder.png")); //$NON-NLS-1$
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
				Object s = JOptionPane.showInputDialog(FRAME, i18n.str("folder_name"), i18n.str("new_folder"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.QUESTION_MESSAGE, null, null, i18n.str("new_folder")); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewFeedAction()
		{
			super(i18n.str("new_feed_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_feed_"))); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK));			
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "add.png"));//$NON-NLS-1$
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
				TextEditPopupManager pm = TextEditPopupManager.getInstance();
				pm.registerJTextComponent(tf);
				LabelledItemPanel lip = new LabelledItemPanel();
				lip.addItem("Feed URL:", tf); //$NON-NLS-1$
				tf.requestFocus();
				int s = JOptionPane.showConfirmDialog(FRAME, lip, i18n.str("feed_url"),  //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);				
				
				if(s == JOptionPane.OK_OPTION && !tf.getText().equals("")) //$NON-NLS-1$
				{
					Feed child = new Feed(tf.getText());
					child.setTitle(i18n.str("new_feed_title")); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public RenameFeedFolderAction()
		{
			super(i18n.str("rename_"));//$NON-NLS-1$
			
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
					FRAME, i18n.str("rename_folder"), i18n.str("rename_title"), //$NON-NLS-1$ //$NON-NLS-2$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DeleteFromFeedTreeAction()
		{
			super(i18n.str("delete_")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "remove.png")); //$NON-NLS-1$
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
				i18n.str("delete_title") + " '" + s + "'?",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				i18n.str("confirm"), JOptionPane.YES_NO_OPTION,//$NON-NLS-1$ 
				JOptionPane.QUESTION_MESSAGE);
			
			return r == JOptionPane.YES_OPTION;	
		}	
	}
	
	private class FeedPropertiesAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public FeedPropertiesAction()
		{
			super(i18n.str("feed_properties_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("feed_properties_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "config.png")); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private boolean updating;
		
		public UpdateAllFeedsAction()
		{
			setAbortable(false);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK));
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "refresh.png"));
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
				putValue(NAME, i18n.str("cancel_update")); //$NON-NLS-1$
                putValue(MNEMONIC_KEY, new Integer(i18n.mnem("cancel_update"))); //$NON-NLS-1$
				putValue(SHORT_DESCRIPTION, getValue(NAME));
				putValue(Action.SMALL_ICON, 
                    UIUtils.getIcon(UIUtils.X16, "cancel.png")); //$NON-NLS-1$
				
				updateButton.setText(null);
				updateButton.setIcon(
                    UIUtils.getIcon(UIUtils.X24, "cancel.png")); //$NON-NLS-1$
			}
			else
			{
				putValue(NAME, i18n.str("update_all_news")); //$NON-NLS-1$
                putValue(MNEMONIC_KEY, new Integer(i18n.mnem("update_all_news"))); //$NON-NLS-1$
				putValue(SHORT_DESCRIPTION, getValue(NAME));
				putValue(Action.SMALL_ICON, 
                    UIUtils.getIcon(UIUtils.X16, "refresh.png")); //$NON-NLS-1$
				
				updateButton.setText(null);
				updateButton.setIcon(
                    UIUtils.getIcon(UIUtils.X24, "refresh.png")); //$NON-NLS-1$
				setEnabled(true);
			}	
		}
	}
	
	private class UpdateFolderAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public UpdateFolderAction()
		{
			super(i18n.str("update_folder")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("update_folder"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "update_folder.png")); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public UpdateCurrentFeedAction()
		{
			super(i18n.str("update_feed")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("update_feed"))); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public EditEntryAction()
		{
			super(i18n.str("edit_selected_entry_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("edit_selected_entry_"))); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DeleteEntriesAction()
		{
			super(i18n.str("delete_selected_entries_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("delete_selected_entries_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "delete.png")); //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "delete.png"));
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
						i18n.str("delete_entries_prompt"), i18n.str("confirm"), //$NON-NLS-1$ //$NON-NLS-2$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public PublishAction()
		{
			super(i18n.str("publish")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("publish"))); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "upload.png"));	 //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "upload.png"));
		}
		
		public void update()
		{
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
            setEnabled(curSelWeblog != null && (!curSelWeblog.isCheckingEmail()) && (!curSelWeblog.isPublishing()) && !rootSel);
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public PublishAllAction()
		{
			super(i18n.str("publish_entire_weblog")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("publish_entire_weblog"))); //$NON-NLS-1$
            putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "pub_all.png"));
            putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "pub_all.png"));
		}
		
		public void update()
		{
			TreePath tp = blogTree.getSelectionPath();
			boolean rootSel = tp != null && 
				tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
            setEnabled(curSelWeblog != null && (!curSelWeblog.isCheckingEmail()) && (!curSelWeblog.isPublishing()) && !rootSel);
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
    
    private class ImportFromEmailAction extends UpdatableAction
    {       
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ImportFromEmailAction()
        {
            super(i18n.str("import_entries_from_email")); 
            //Messages.setMnemonic("ThingamablogFrame.Publish", this);
            //putValue(ACCELERATOR_KEY,
            //    KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
            putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "email.png"));    //$NON-NLS-1$
            putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "email.png"));
        }
        
        public void update()
        {
            TreePath tp = blogTree.getSelectionPath();
            boolean rootSel = tp != null && 
                tp.getLastPathComponent().equals(WeblogTreeModel.ROOT);
            setEnabled(curSelWeblog != null && curSelWeblog.isImportFromEmailEnabled() && (!curSelWeblog.isCheckingEmail()) && (!curSelWeblog.isPublishing()) && !rootSel);
            if(isEnabled())
                putValue(Action.SHORT_DESCRIPTION, 
                getValue(NAME) + " [" + curSelWeblog.getTitle() + "]");  //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            //if(blogTree.getLastSelectedPathComponent() instanceof Weblog)
            if(curSelWeblog != null)
            {
                ///publishWeblog(curSelWeblog, PUB_CHANGED);
                importEntriesFromEmail(curSelWeblog);
            }
        }
    }
	
	//***************************************
	//Action to ping the services of a weblog
	//****************************************
	private class WeblogPingAction extends UpdatableAction
	{		
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public WeblogPingAction()
		{
			super(i18n.str("ping_services")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("ping_services"))); //$NON-NLS-1$
            putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "ping.png"));
            putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "ping.png"));
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ViewWeblogAction()
		{
			super(i18n.str("view_weblog_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("view_weblog_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "browser.png"));	 //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "browser.png"));
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
                            if(curSelWeblog instanceof TBWeblog){
                                TBWeblog tb = (TBWeblog) curSelWeblog;
                                if(tb.getType().equals("internet")){
				try
				{				
					//Browser.displayURL(curSelWeblog.getFrontPageUrl());
                    Desktop.browse(new URL(curSelWeblog.getFrontPageUrl()));
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
					logger.log(Level.WARNING, ex.getMessage(), ex);
				}
                            } else {
                                try
                                {   
                                    String nodeHostname = TBGlobals.getProperty("NODE_HOSTNAME");
                                    Desktop.browse(new URL("http://" + nodeHostname + ":8888" + curSelWeblog.getFrontPageUrl()));
                                }
                                catch(Exception ex)
                                {
                                    System.err.println("erreur freenet");
                                    ex.printStackTrace();
                                    logger.log(Level.WARNING, ex.getMessage(), ex);
                                }
                            }
                            }else{
                                try
				{				
					//Browser.displayURL(curSelWeblog.getFrontPageUrl());
                    Desktop.browse(new URL(curSelWeblog.getFrontPageUrl()));
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
	
	//*************************************************
	//Action to open the properties dialog for a Weblog
	//*************************************************
	private class WeblogPropertiesAction extends UpdatableAction
	{		
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public WeblogPropertiesAction()
		{			
			super(i18n.str("configure_weblog_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("configure_weblog_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "config.png")); //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "config.png"));
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private boolean isSwitchTab;
		private int searchTab;
		
		public SearchAction()
		{
			super(i18n.str("find")); //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "find.png"));
			putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "find.png"));
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
				putValue(NAME, i18n.str("find_entries_"));//$NON-NLS-1$
                putValue(MNEMONIC_KEY, new Integer(i18n.mnem("find_entries_"))); //$NON-NLS-1$
				putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));				
			}
			else
			{
				searchTab = TBSearchDialog.FEED_TAB;
				putValue(NAME, i18n.str("find_items_")); //$NON-NLS-1$
                putValue(MNEMONIC_KEY, new Integer(i18n.mnem("find_items_"))); //$NON-NLS-1$
			}
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "find.png"));
			putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "find.png"));
			
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewWeblogAction()
		{
			super(i18n.str("new_weblog_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_weblog_"))); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "blog_glow.png")); //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "blog_glow.png"));
		}
		
		public void actionPerformed(ActionEvent e)
		{
		    showNewWeblogWizard();
		}	
	}
        
        //************************************************
	//Action for creating a new Flog from the wizard
	//************************************************
	private class NewFlogAction extends AbstractAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewFlogAction()
		{
			super(i18n.str("new_flog_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_flog_"))); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "flog_glow.png")); //$NON-NLS-1$
			putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "flog_glow.png"));
		}
		
		public void actionPerformed(ActionEvent e)
		{
		    showNewFlogWizard();
		}	
	}
	
	//*************************************************
	//Action for deleting the currently selected weblog
	//*************************************************
	private class DeleteWeblogAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DeleteWeblogAction()
		{
			super(i18n.str("delete_weblog_")); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "remove.png"));	 //$NON-NLS-1$
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
							i18n.str("delete_weblog_prompt") + " [" + blog.getTitle() + "]",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							i18n.str("confirm"), //$NON-NLS-1$ 
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);            
				if(yn == JOptionPane.NO_OPTION)
					return;
					
				try
				{
					weblogList.deleteWeblog(blog);
                    taskDialog.removeWeblog(blog);
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewDatabaseAction()
		{
			super(i18n.str("new_database_"));//$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_database_"))); //$NON-NLS-1$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			createNewDatabase();			
		}	
	}
	
	private class OpenDatabaseAction extends AbstractAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public OpenDatabaseAction()
		{
			super(i18n.str("open_database_"));//$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("open_database_"))); //$NON-NLS-1$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogTitle(i18n.str("open_database_title")); //$NON-NLS-1$
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
				UIUtils.showError(FRAME, i18n.str("invalid_file")); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private File lastDir = null;
	    
	    public ImportFileAction()
		{
			super(i18n.str("import_file_")); //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("import_file_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "import.png")); //$NON-NLS-1$
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
				fc.setDialogTitle(i18n.str("import_file_title")); //$NON-NLS-1$
				int r = fc.showOpenDialog(ThingamablogFrame.this);
				if(r == JFileChooser.CANCEL_OPTION)
					return;
				
				lastDir = fc.getCurrentDirectory();
				File sel[] = fc.getSelectedFiles();
				if(sel == null)
				{
					UIUtils.showError(ThingamablogFrame.this, i18n.str("invalid_file")); //$NON-NLS-1$
					return;
				}
				
				for(int i = 0; i < sel.length; i++)
				{
				    File outFile = new File(f, sel[i].getName());
				
				    //check if the file already exists and prompt for overwrite
				    if(outFile.exists())
				    {
				        int yn = JOptionPane.showConfirmDialog(FRAME,
				                i18n.str("overwrite") + "[" + outFile.getName() + "]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				                i18n.str("confirm"), //$NON-NLS-1$
				                JOptionPane.YES_NO_OPTION);
					
				        if(yn == JOptionPane.NO_OPTION)
				            continue;					
				    }
				
				    try
                    {
                        IOUtils.copy(sel[i], outFile);
                    }
                    catch(IOException ex)
                    {                        
                        UIUtils.showError(FRAME, ex);
                    }
                    //Utils.copyFile(sel[i].getAbsolutePath(), outFile.getAbsolutePath());
				}
				refreshTree(blogTree);				
			}
		}	
	}
	
	private class NewWebFolderAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NewWebFolderAction()
		{
			super(i18n.str("new_web_folder_"));	 //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("new_web_folder_"))); //$NON-NLS-1$
			putValue(Action.SMALL_ICON, 
                UIUtils.getIcon(UIUtils.X16, "new_folder.png")); //$NON-NLS-1$
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
					FRAME, i18n.str("enter_folder_name_prompt"), i18n.str("new_folder"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.QUESTION_MESSAGE, null, null, i18n.str("new_folder")); //$NON-NLS-1$
				if(o != null)
				{
					File newFolder = new File(f, o.toString());
					if(newFolder.mkdir())
					{				
						refreshTree(blogTree);
					}
					else
					{
						UIUtils.showError(FRAME, i18n.str("unable_to_create_folder_prompt")); //$NON-NLS-1$
					}						
				}
			}
		}	
	}
	
	//This Action deletes a File or Directory
	private class DeleteFileAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public DeleteFileAction()
		{
			super(i18n.str("delete_")); //$NON-NLS-1$
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
							i18n.str("delete_title") + " '" + f.getName() + "' ?", i18n.str("confirm"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public RenameFileAction()
		{
			super(i18n.str("rename_"));	 //$NON-NLS-1$
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
					FRAME, i18n.str("rename_title"), i18n.str("rename_title"), //$NON-NLS-1$ //$NON-NLS-2$
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
						UIUtils.showError(FRAME, i18n.str("unable_to_rename_file_prompt"));	 //$NON-NLS-1$
					}	
				}				
			}
		}	
	}
	
	private class OpenFileAction extends UpdatableAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public OpenFileAction()
		{
			super(i18n.str("open_"));	 //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public EditTemplateAction()
		{
			super(i18n.str("edit_template_"));	 //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("edit_template_"))); //$NON-NLS-1$
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
				//ed.setSize(640, 480);
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NextAction()
		{
			super(i18n.str("next"));	 //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("next"))); //$NON-NLS-1$
            putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "down.png"));
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public PreviousAction()
		{
			super(i18n.str("previous"));	 //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("previous"))); //$NON-NLS-1$
            putValue("LARGE_ICON", UIUtils.getIcon(UIUtils.X24, "up.png"));
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public NextUnreadAction()
		{
			super(i18n.str("next_unread_item"));	 //$NON-NLS-1$
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("next_unread_item"))); //$NON-NLS-1$
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
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public HomePageAction()
		{
			super(i18n.str("website_"));	//$NON-NLS-1$ 
		}
		
		public void actionPerformed(ActionEvent e)
		{
			try
			{			
				//Browser.displayURL(TBGlobals.APP_URL);
                Desktop.browse(new URL(TBGlobals.APP_URL));
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
			/*if(!(w instanceof EntryEditor))
				return;            
			EntryEditor editor = (EntryEditor)w;*/
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
				UIUtils.showError(FRAME, i18n.str("error_saving_entry_prompt")); //$NON-NLS-1$
				ex.printStackTrace();
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
	        /*if(isDBOpen && !updateMonitor.isUpdateStarted())
	            updateFolder(feedRoot);*/
	    }
	}
	
	
	//********************************************************************
	//Link actions - execute when a link is clickedo nthe HTMLOptionsPane
	//********************************************************************
	private class NewEntryLink implements HTMLOptionLink
	{
	    public String getLinkText()
	    {
	        return i18n.str("compose_entry"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.X32 + "edit1.png"); //$NON-NLS-1$
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
	        return i18n.str("read_previous"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.MISC + "read_previous32.png"); //$NON-NLS-1$
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
	        return i18n.str("configure_weblog_settings"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.X32 + "config.png"); //$NON-NLS-1$
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
	        return i18n.str("publish_weblog"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.X32 + "upload.png"); //$NON-NLS-1$
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
	        return i18n.str("republish"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.X32 + "pub_all.png"); //$NON-NLS-1$
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
	        return i18n.str("ping_services"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.X32 + "ping.png"); //$NON-NLS-1$
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
	        return ClassLoader.getSystemResource(UIUtils.X32 + "blog.png"); //$NON-NLS-1$
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
	        return i18n.str("create_weblog"); //$NON-NLS-1$
	    }
	    
	    public URL getImageURL()
	    {
	        return ClassLoader.getSystemResource(UIUtils.X48 + "pencil.png"); //$NON-NLS-1$
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
                System.err.println("Shutdown Hook"); //$NON-NLS-1$
    		    try{ 
    				backend.shutdown();    		    
    		    }catch(Exception ex){}                
    		}
        }
    }
    
    private class OpenFolderAction extends UpdatableAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public OpenFolderAction()
        {
            super(i18n.str("open_"));     //$NON-NLS-1$
            //Messages.setMnemonic("ThingamablogFrame.Next", this);
        }
        
        public void update()
        {            
            Object o = blogTree.getLastSelectedPathComponent();
            if(o instanceof File)
            {
                File f = (File)o;
                if(f.isDirectory())
                {
                    setEnabled(true);
                    return;
                }
            }
            setEnabled(isWebFolderSelected());
        }
        
        public void actionPerformed(ActionEvent e)
        {
            Object o = blogTree.getLastSelectedPathComponent();
            File f;
            if(o instanceof File)
            {
                f = (File)o;                
            }
            else if(isWebFolderSelected())
            {
                f = curSelWeblog.getWebFilesDirectory();
            }
            else
                return;
            
            if(f.isDirectory())
            {
                try
                {
                    Desktop.open(f);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private class RefreshBlogTreeAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public RefreshBlogTreeAction()
        {
            super(i18n.str("refresh"));      //$NON-NLS-1$
        }        
        
        public void actionPerformed(ActionEvent e)
        {
            refreshTree(blogTree);
        }
    }
    
    private class ShowTaskDialogAction extends UpdatableAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ShowTaskDialogAction()
        {
            super(i18n.str("tasks_"));     //$NON-NLS-1$
            this.putValue(Action.SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "cogs.png")); //$NON-NLS-1$
        }        
        
        public void actionPerformed(ActionEvent e)
        {
            showTaskDialog();
        }

        /* (non-Javadoc)
         * @see net.sf.thingamablog.gui.UpdatableAction#update()
         */
        public void update()
        {
            setEnabled(isDBOpen);            
        }
    }
    
    private class InstallTemplatePackAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public InstallTemplatePackAction()
        {
            super(i18n.str("install_template_pack_")); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            JDialog d = new InstallTemplateDialog(FRAME);
            d.setLocationRelativeTo(FRAME);
            d.setVisible(true);
        }
    }
    
    
}
