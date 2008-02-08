/*
 * Created on Nov 2, 2007
 */
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.UndoManager;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.DefaultAction;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.Entities;
import net.atlanticbb.tantlinger.ui.text.HTMLUtils;
import net.atlanticbb.tantlinger.ui.text.IndentationFilter;
import net.atlanticbb.tantlinger.ui.text.SourceCodeEditor;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.atlanticbb.tantlinger.ui.text.WysiwygHTMLEditorKit;
import net.atlanticbb.tantlinger.ui.text.actions.ClearStylesAction;
import net.atlanticbb.tantlinger.ui.text.actions.FindReplaceAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLEditorActionFactory;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLElementPropertiesAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLFontAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLFontColorAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLHorizontalRuleAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLImageAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLInlineAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLLineBreakAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLLinkAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLTableAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction;
import net.atlanticbb.tantlinger.ui.text.actions.SpecialCharAction;
import net.atlanticbb.tantlinger.ui.text.dialogs.HyperlinkDialog;
import net.atlanticbb.tantlinger.ui.text.dialogs.ImageDialog;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.AuthorEvent;
import net.sf.thingamablog.blog.AuthorListener;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.CategoryEvent;
import net.sf.thingamablog.blog.CategoryListener;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogEvent;
import net.sf.thingamablog.blog.WeblogList;
import net.sf.thingamablog.blog.WeblogListener;
import net.sf.thingamablog.gui.TBAbout;
import net.sf.thingamablog.gui.TBHelpAction;
import net.sf.thingamablog.gui.app.WeblogPreviewer;
import novaworx.syntax.SyntaxFactory;
import novaworx.textpane.SyntaxDocument;
import novaworx.textpane.SyntaxGutter;
import novaworx.textpane.SyntaxGutterBase;

import org.bushe.swing.action.ActionList;
import org.bushe.swing.action.ActionManager;
import org.bushe.swing.action.ActionUIFactory;
import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.JTextComponentSpellChecker;

import com.tantlinger.jdatepicker.JCalendarComboBox;


//TODO table edit actions on wysiwyg popup menu
//TODO editor should close if in update mode and weblog is deleted

/**
 * @author Bob Tantlinger
 *
 */
public class EntryEditor extends JFrame
{    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.editor"); //$NON-NLS-1$
    
    public static final int NEW_ENTRY_MODE = -1;
    public static final int UPDATE_ENTRY_MODE = -2; 
    public static final int WYSIWYG = 0; //wysiwyg editor tab
    public static final int SOURCE = 1;//source editor tab;
    
    private static final String INVALID_TAGS[] = {"html", "head", "body", "title"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    private JEditorPane wysEditor;
    private SourceCodeEditor srcEditor;
    private JTextComponent focusedEditor;
    private JComboBox fontFamilyCombo;
    private JComboBox paragraphCombo;
    private JButton fontSizeButton;
    private JTabbedPane tabs;
    private JMenuBar menuBar;
    private JToolBar formatToolBar, mainToolBar;
    
    private JPopupMenu wysPopupMenu, srcPopupMenu;
    
    private JComboBox siteCombo, authorCombo;
    private JCalendarComboBox dateCombo;
    private JTextField titleField;
    private JCheckBox markModifiedCb;
    private CategoryEditorPane catPane;
    private JSplitPane attribSplitPane;
    
    private ActionList actionList;
    
    private FocusListener focusHandler = new FocusHandler(); 
    private DocumentListener textChangedHandler = new TextChangedHandler();
    private ActionListener fontChangeHandler = new FontChangeHandler();
    private ActionListener paragraphComboHandler = new ParagraphComboHandler();
    private CaretListener caretHandler = new CaretHandler();
    private MouseListener popupHandler = new PopupHandler();
    private WeblogChangeListener changeListener = new WeblogChangeListener();
        
    private boolean isWysTextChanged;
    private boolean shouldAskToSave;    
    private boolean cancelled = true;
    private boolean isPublish = false;
    
    private int mode = NEW_ENTRY_MODE;
    private BlogEntry entry;    
    private Weblog weblog;
    private WeblogList weblogList;      
    
    public EntryEditor(Weblog blog, WeblogList list) throws BackendException
    {
        this(new BlogEntry(), blog, list, NEW_ENTRY_MODE);
    }
    
    public EntryEditor(BlogEntry e, Weblog blog, WeblogList list) 
    throws BackendException
    {
        this(e, blog, list, UPDATE_ENTRY_MODE);
    }
    
    public EntryEditor(BlogEntry e, Weblog blog, WeblogList list, int entryMode) throws BackendException
    {
        entry = e;
        weblogList = list;
        weblog = blog;
        mode = entryMode;       
        if(entry.isDraft())
            entry.setDate(new Date());
                
        setIconImage((UIUtils.getIcon(UIUtils.X16, "edit1.png")).getImage()); //$NON-NLS-1$
        setTitle(entry.getTitle());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                closeEditor();
            }
        });
        
        //register a listener with the weblogs to update changes
        for(int i = 0; i < weblogList.getWeblogCount(); i++)
        {
            Weblog w = weblogList.getWeblogAt(i);
            w.addAuthorListener(changeListener);
            w.addCategoryListener(changeListener);
            w.addWeblogListener(changeListener);        
        }
        
        initUI();
        
        if(mode == NEW_ENTRY_MODE && getEditMode() == WYSIWYG && 
            (entry.getText() == null || entry.getText().equals(""))) //$NON-NLS-1$
            entry.setText("<p></p>"); //$NON-NLS-1$
        if(getEditMode() == WYSIWYG)
        {
            insertHTML(entry.getText(), 0);
            wysEditor.setCaretPosition(0);
            CompoundUndoManager.discardAllEdits(wysEditor.getDocument());
        }
        else
        {
            srcEditor.setText(entry.getText());
            srcEditor.setCaretPosition(0);
            CompoundUndoManager.discardAllEdits(srcEditor.getDocument());
        }
        
        updateState();
        shouldAskToSave = false;
    }
    
    public int getEditMode()
    {
        return tabs.getSelectedIndex();
    }
    
    /* **************** UI initialization methods ********************/
    private void initUI() throws BackendException
    {
        createEditorTabs();
        createEditorActions();
        createAttributesComponent();
        
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.add(formatToolBar, BorderLayout.NORTH);
        editorPanel.add(tabs, BorderLayout.CENTER);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(attribSplitPane, BorderLayout.NORTH);
        mainPanel.add(editorPanel, BorderLayout.CENTER);
        
        getContentPane().add(mainToolBar, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);    
        setJMenuBar(menuBar);        
    }
       
    
    private void createAttributesComponent() throws BackendException
    {
        siteCombo = new JComboBox(weblogList.getWeblogs());
        siteCombo.setSelectedItem(weblog);        
        //we have to change cats and authors when the weblog selection changes
        siteCombo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                weblog = (Weblog)siteCombo.getSelectedItem();             
                //File dir = EntryImageUtils.getImageDirectory(weblog);
                //.... TODO resolve relative Image URLs
                updateAuthors(weblog);
                updateCategories(weblog);
                
            }
        });     
        //can't select diff weblog if we're updating the entry
        siteCombo.setEnabled(mode == NEW_ENTRY_MODE); 
        
        catPane = new CategoryEditorPane();
        catPane.setCategories(weblog.getCategories(), entry);
        
        Author authors[] = weblog.getAuthors();
        authorCombo = new JComboBox(authors);       
        authorCombo.addItem(""); //$NON-NLS-1$      
        if(mode == UPDATE_ENTRY_MODE)
        {           
            for(int i = 0; i < authors.length; i++)
                if(authors[i].getString().equals(entry.getAuthor().getString()))
                    authorCombo.setSelectedIndex(i);
        }
        
        titleField = new JTextField();
        titleField.setText(entry.getTitle());
        titleField.addCaretListener(new CaretListener()
        {
            public void caretUpdate(CaretEvent e)
            {
                setTitle(titleField.getText()); 
            }
        });
        titleField.addFocusListener(focusHandler);
        TextEditPopupManager.getInstance().registerJTextComponent(titleField);
        
        Date d  = (mode == NEW_ENTRY_MODE || entry.getDate() == null) ? new Date() : entry.getDate();
        dateCombo = new JCalendarComboBox(d, true, (mode == NEW_ENTRY_MODE) || entry.isDraft());
        
        markModifiedCb = new JCheckBox(i18n.str("mark_as_modified")); //$NON-NLS-1$
        markModifiedCb.setSelected(true);
        markModifiedCb.setEnabled(!entry.isDraft());
        markModifiedCb.setVisible(mode == UPDATE_ENTRY_MODE);
        
        JPanel attPanel = new JPanel(new GridBagLayout());
        attPanel.setBorder(BorderFactory.createEmptyBorder(16, 5, 16, 5));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridy = 0;
        attPanel.add(new JLabel(i18n.str("site")), gbc); //$NON-NLS-1$
                
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 1;
        attPanel.add(siteCombo, gbc);
                
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 0, 5);
        gbc.gridy = 0;
        attPanel.add(new JLabel(i18n.str("author")), gbc); //$NON-NLS-1$
             
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 3;
        attPanel.add(authorCombo, gbc);
             
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 0, 0);
        gbc.gridy = 1;
        attPanel.add(new JLabel(i18n.str("title")), gbc); //$NON-NLS-1$
                
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(5, 0, 0, 0);
        gbc.gridx = 1;
        attPanel.add(titleField, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 0, 0, 5);
        gbc.gridy = 2;
        attPanel.add(new JLabel(i18n.str("date")), gbc); //$NON-NLS-1$
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 0, 0);
        gbc.gridx = 1; 
        attPanel.add(dateCombo, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy = 2;
        attPanel.add(markModifiedCb, gbc);
                
        JScrollPane catScroller = new JScrollPane(catPane);
        catScroller.getViewport().setBackground(catPane.getBackground());
        catScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        catScroller.setPreferredSize(new Dimension(160, 5/*topPanel.getHeight()*/));
        JPanel catPanel = new JPanel(new BorderLayout());
        catPanel.add(new JLabel(i18n.str("categories") + ":"), BorderLayout.NORTH); //$NON-NLS-1$ //$NON-NLS-2$
        catPanel.add(catScroller, BorderLayout.CENTER);
        attribSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        attribSplitPane.setLeftComponent(attPanel);
        attribSplitPane.setRightComponent(catPanel);
    }
    
    private void createEditorActions()
    {
        menuBar = new JMenuBar();       
        actionList = new ActionList("editor-actions"); //$NON-NLS-1$
        
        ActionList paraActions = new ActionList("paraActions"); //$NON-NLS-1$
        ActionList fontSizeActions = new ActionList("fontSizeActions"); //$NON-NLS-1$
        ActionList editActions = HTMLEditorActionFactory.createEditActionList();
        ActionList mainToolBarActions = new ActionList("mainToolBar"); //$NON-NLS-1$
        Action objectPropertiesAction = new PropertiesAction();
        
        //create editor popupmenus
        wysPopupMenu = ActionUIFactory.getInstance().createPopupMenu(editActions);
        wysPopupMenu.addSeparator();
        wysPopupMenu.add(objectPropertiesAction);
        srcPopupMenu = ActionUIFactory.getInstance().createPopupMenu(editActions);               
                
        // create file menu
        JMenu fileMenu = new JMenu(i18n.str("file")); //$NON-NLS-1$
        fileMenu.setMnemonic(i18n.mnem("file")); //$NON-NLS-1$
        Action act = new EntryAction(getMode(), true);
        actionList.add(act);
        fileMenu.add(act);
        mainToolBarActions.add(act);
        act = new EntryAction(getMode(), false);
        actionList.add(act);
        fileMenu.add(act);
        mainToolBarActions.add(act);
        act = new SaveAsDraftAction();
        actionList.add(act);
        fileMenu.add(act);
        mainToolBarActions.add(act);
        JMenuItem close = new JMenuItem(i18n.str("close")); //$NON-NLS-1$
        close.setMnemonic(i18n.mnem("close")); //$NON-NLS-1$
        close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeEditor();
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(close);
        menuBar.add(fileMenu);
        
        // create edit menu   
        ActionList lst = new ActionList("edits");              //$NON-NLS-1$
        act = new ChangeTabAction(0);        
        lst.add(act);
        act = new ChangeTabAction(1);        
        lst.add(act);
        lst.add(null);//separator        
        lst.addAll(editActions);
        lst.add(null);
        lst.add(new FindReplaceAction(false));
        actionList.addAll(lst);
        JMenu editMenu = ActionUIFactory.getInstance().createMenu(lst);        
        editMenu.setText(i18n.str("edit")); //$NON-NLS-1$
        editMenu.setMnemonic(i18n.mnem("edit")); //$NON-NLS-1$
        
        editActions.remove(editActions.size() - 1); //remove select all
        editActions.remove(editActions.size() - 1);//remove separator  
        editActions.remove(editActions.size() - 1); //remove paste formatted
        mainToolBarActions.add(null);
        mainToolBarActions.addAll(editActions);
        
        menuBar.add(editMenu);        
        
        //create format menu
        JMenu formatMenu = new JMenu(i18n.str("format"));  //$NON-NLS-1$
        formatMenu.setMnemonic(i18n.mnem("format"));         //$NON-NLS-1$
        lst = HTMLEditorActionFactory.createFontSizeActionList();//HTMLEditorActionFactory.createInlineActionList();
        actionList.addAll(lst);        
        formatMenu.add(createMenu(lst, i18n.str("size"))); //$NON-NLS-1$
        fontSizeActions.addAll(lst);
        
        lst = HTMLEditorActionFactory.createInlineActionList();
        actionList.addAll(lst);
        formatMenu.add(createMenu(lst, i18n.str("style"))); //$NON-NLS-1$
        
        act = new HTMLFontColorAction();
        actionList.add(act);
        formatMenu.add(act);
        
        act = new HTMLFontAction();
        actionList.add(act);
        formatMenu.add(act);
        
        act = new ClearStylesAction();
        actionList.add(act);
        formatMenu.add(act);
        formatMenu.addSeparator();
        
        lst = HTMLEditorActionFactory.createBlockElementActionList();
        actionList.addAll(lst);
        formatMenu.add(createMenu(lst, i18n.str("paragraph"))); //$NON-NLS-1$
        paraActions.addAll(lst);
        
        lst = HTMLEditorActionFactory.createListElementActionList();
        actionList.addAll(lst);
        formatMenu.add(createMenu(lst, i18n.str("list"))); //$NON-NLS-1$
        formatMenu.addSeparator();
        paraActions.addAll(lst);
        
        lst = HTMLEditorActionFactory.createAlignActionList();
        actionList.addAll(lst);        
        formatMenu.add(createMenu(lst, i18n.str("align"))); //$NON-NLS-1$
                
        JMenu tableMenu = new JMenu(i18n.str("table")); //$NON-NLS-1$
        lst = HTMLEditorActionFactory.createInsertTableElementActionList();
        actionList.addAll(lst);
        tableMenu.add(createMenu(lst, i18n.str("insert"))); //$NON-NLS-1$
        
        lst = HTMLEditorActionFactory.createDeleteTableElementActionList();
        actionList.addAll(lst);
        tableMenu.add(createMenu(lst, i18n.str("delete"))); //$NON-NLS-1$
        formatMenu.add(tableMenu);
        formatMenu.addSeparator();
                
        actionList.add(objectPropertiesAction);
        formatMenu.add(objectPropertiesAction);
                
        menuBar.add(formatMenu);
        
        ActionList insertActions = new ActionList("insertActions"); //$NON-NLS-1$
        act = new HTMLLinkAction();
        insertActions.add(act);        
        
        act = new InternalLinkAction();
        insertActions.add(act);
        insertActions.add(null);//separator
        
        act = new WeblogImageAction();
        insertActions.add(act);
        
        act = new HTMLTableAction();
        insertActions.add(act);
        insertActions.add(null);
        
        act = new HTMLLineBreakAction();        
        insertActions.add(act);        
        
        act = new HTMLHorizontalRuleAction();        
        insertActions.add(act);        
        
        act = new SpecialCharAction();        
        insertActions.add(act);        
        
        actionList.addAll(insertActions);
        JMenu insertMenu = ActionUIFactory.getInstance().createMenu(insertActions);
        insertMenu.setText(i18n.str("insert")); //$NON-NLS-1$
        insertMenu.setMnemonic(i18n.mnem("insert"));         //$NON-NLS-1$
        menuBar.add(insertMenu);
        
        JMenu toolsMenu = new JMenu(i18n.str("tools")); //$NON-NLS-1$
        toolsMenu.setMnemonic(i18n.mnem("tools"));         //$NON-NLS-1$
        act = new SpellCheckAction();
        actionList.add(act);
        mainToolBarActions.add(null);
        mainToolBarActions.add(act);
        toolsMenu.add(act);
        act = new PreviewAction();
        actionList.add(act);
        mainToolBarActions.add(null);
        mainToolBarActions.add(act);
        toolsMenu.add(act);
        menuBar.add(toolsMenu); 
        
        JMenu helpMenu = new JMenu(i18n.str("help")); //$NON-NLS-1$
        helpMenu.setMnemonic(i18n.mnem("help"));         //$NON-NLS-1$
        
        Action help = new TBHelpAction(
                i18n.str("help_contents_"), "ch03.item2"); //$NON-NLS-1$ //$NON-NLS-2$
        help.putValue(Action.SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "help.png")); //$NON-NLS-1$
        help.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        help.putValue(Action.MNEMONIC_KEY, new Integer(i18n.mnem("help_contents_"))); //$NON-NLS-1$
        
        helpMenu.add(help);
        JMenuItem aboutItem = new JMenuItem(i18n.str("about_")); //$NON-NLS-1$
        aboutItem.setMnemonic(i18n.mnem("about_")); //$NON-NLS-1$
        aboutItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                TBAbout.showAboutBox(EntryEditor.this);
            }
        });
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
        
        createMainToolBar(mainToolBarActions);
        createFormatToolBar(paraActions, fontSizeActions, insertActions);
    }
    
    private void createMainToolBar(List actions)
    {
        mainToolBar = new JToolBar();        
        mainToolBar.setFloatable(false);
        for(Iterator it = actions.iterator(); it.hasNext();)
        {
            Action a = (Action)it.next();
            if(a == null)
                mainToolBar.addSeparator();
            else
            {
                UIUtils.addToolBarButton(mainToolBar, a);
            	/*Icon li = (Icon)a.getValue(ActionManager.LARGE_ICON);
                UIUtils.add
                JButton b = mainToolBar.add(a);
                if(li != null)
                    b.setIcon(li);*/
            }            
        }
    }
    
    private void createFormatToolBar(ActionList blockActs, ActionList fontSizeActs, ActionList insertActions)
    {
        formatToolBar = new JToolBar();
        formatToolBar.setFloatable(false);
        formatToolBar.setFocusable(false);
        
        Font comboFont = new Font("Dialog", Font.PLAIN, 12); //$NON-NLS-1$
        PropertyChangeListener propLst = new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if(evt.getPropertyName().equals("selected")) //$NON-NLS-1$
                {
                    if(evt.getNewValue().equals(Boolean.TRUE))
                    {
                        paragraphCombo.removeActionListener(paragraphComboHandler);                    
                        paragraphCombo.setSelectedItem(evt.getSource());
                        paragraphCombo.addActionListener(paragraphComboHandler);
                    }
                }
            }            
        };
        for(Iterator it = blockActs.iterator(); it.hasNext();)
        {
            Object o = it.next();
            if(o instanceof DefaultAction)
                ((DefaultAction)o).addPropertyChangeListener(propLst);
        }        
        paragraphCombo = new JComboBox(toArray(blockActs));       
        paragraphCombo.setPreferredSize(new Dimension(120, 22));
        paragraphCombo.setMinimumSize(new Dimension(120, 22));
        paragraphCombo.setMaximumSize(new Dimension(120, 22));
        paragraphCombo.setFont(comboFont);
        paragraphCombo.addActionListener(paragraphComboHandler);
        paragraphCombo.setRenderer(new ParagraphComboRenderer());
        formatToolBar.add(paragraphCombo);
        formatToolBar.addSeparator();
                
        Vector fonts = new Vector();
        fonts.add("Default"); //$NON-NLS-1$
        fonts.add("serif"); //$NON-NLS-1$
        fonts.add("sans-serif"); //$NON-NLS-1$
        fonts.add("monospaced");  //$NON-NLS-1$
        GraphicsEnvironment gEnv = 
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        fonts.addAll(Arrays.asList(gEnv.getAvailableFontFamilyNames()));             
        
        fontFamilyCombo = new JComboBox(fonts);
        fontFamilyCombo.setPreferredSize(new Dimension(150, 22));
        fontFamilyCombo.setMinimumSize(new Dimension(150, 22));
        fontFamilyCombo.setMaximumSize(new Dimension(150, 22));
        fontFamilyCombo.setFont(comboFont);
        fontFamilyCombo.addActionListener(fontChangeHandler);
        formatToolBar.add(fontFamilyCombo);        
        
        fontSizeButton = new JButton(UIUtils.getIcon(UIUtils.X16, "fontsize.png")); //$NON-NLS-1$
        fontSizeButton.setToolTipText(i18n.str("size")); //$NON-NLS-1$
        final JPopupMenu sizePopup = ActionUIFactory.getInstance().createPopupMenu(fontSizeActs);
        ActionListener al = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                sizePopup.show(fontSizeButton, 0, fontSizeButton.getHeight());
            }            
        };
        fontSizeButton.addActionListener(al);
        configToolbarButton(fontSizeButton);
        formatToolBar.add(fontSizeButton);
                
        Action act = new HTMLFontColorAction();
        actionList.add(act);
        addToToolBar(formatToolBar, act);        
        formatToolBar.addSeparator();
        
        act = new HTMLInlineAction(HTMLInlineAction.BOLD);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        actionList.add(act);
        addToToolBar(formatToolBar, act);
        
        act = new HTMLInlineAction(HTMLInlineAction.ITALIC);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        actionList.add(act);
        addToToolBar(formatToolBar, act);
        
        act = new HTMLInlineAction(HTMLInlineAction.UNDERLINE);
        act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
        actionList.add(act);
        addToToolBar(formatToolBar, act);
        formatToolBar.addSeparator();
        
        List alst = HTMLEditorActionFactory.createListElementActionList();
        for(Iterator it = alst.iterator(); it.hasNext();)
        {
            act = (Action)it.next();
            act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
            actionList.add(act);
            addToToolBar(formatToolBar, act);
        }
        formatToolBar.addSeparator();
        
        alst = HTMLEditorActionFactory.createAlignActionList();
        for(Iterator it = alst.iterator(); it.hasNext();)
        {
            act = (Action)it.next();
            act.putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_TOGGLE);
            actionList.add(act);
            addToToolBar(formatToolBar, act);
        }
        formatToolBar.addSeparator();
        
        for(Iterator it = insertActions.iterator(); it.hasNext();)
        {
            act = (Action)it.next();
            if(act == null)
                formatToolBar.addSeparator();
            else
                addToToolBar(formatToolBar, act);
        }        
    }
    
    private void addToToolBar(JToolBar toolbar, Action act)
    {
        AbstractButton button = ActionUIFactory.getInstance().createButton(act);
        configToolbarButton(button);
        toolbar.add(button);
    }
    
    /**
     * Converts an action list to an array. 
     * Any of the null "separators" or sub ActionLists are ommited from the array.
     * @param lst
     * @return
     */
    private Action[] toArray(ActionList lst)
    {
        List acts = new ArrayList();
        for(Iterator it = lst.iterator(); it.hasNext();)
        {
            Object v = it.next();
            if(v != null && v instanceof Action)
                acts.add(v);
        }
        
        return (Action[])acts.toArray(new Action[acts.size()]);
    }
        
    private void configToolbarButton(AbstractButton button)
    {
        button.setText(null);
        button.setMnemonic(0);
        button.setMargin(new Insets(1, 1, 1, 1));
        Dimension size = new Dimension(22, 22);        
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setFocusable(false);
        button.setFocusPainted(false);
        //button.setBorder(plainBorder);
        Action a = button.getAction();
        if(a != null)
            button.setToolTipText(a.getValue(Action.NAME).toString());
    }   
    
    private JMenu createMenu(ActionList lst, String menuName)
    {
        JMenu m = ActionUIFactory.getInstance().createMenu(lst);
        m.setText(menuName);
        return m;
    }
    
    private void createEditorTabs()
    {
        tabs = new JTabbedPane(SwingConstants.BOTTOM);
        wysEditor = createWysiwygEditor();
        srcEditor = createSourceEditor();        
        
        tabs.addTab(i18n.str("edit"), new JScrollPane(wysEditor)); //$NON-NLS-1$
        
        JScrollPane scrollPane = new JScrollPane(srcEditor);        
        SyntaxGutter gutter = new SyntaxGutter(srcEditor);
        SyntaxGutterBase gutterBase = new SyntaxGutterBase(gutter);
        scrollPane.setRowHeaderView(gutter);
        scrollPane.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, gutterBase);        
        tabs.addTab(i18n.str("edit_tab"), scrollPane); //$NON-NLS-1$
        
        try
        {
            tabs.setSelectedIndex(Integer.parseInt(TBGlobals.getProperty("EDITOR_TYPE"))); //$NON-NLS-1$
        }
        catch(NumberFormatException ex){}
        
        tabs.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {                
                updateEditView();                
            }
        });       
    }
    
    private SourceCodeEditor createSourceEditor()
    {        
        SourceCodeEditor ed = new SourceCodeEditor();
        SyntaxDocument doc = new SyntaxDocument();
        doc.setSyntax(SyntaxFactory.getSyntax("html"));         //$NON-NLS-1$
        CompoundUndoManager cuh = new CompoundUndoManager(doc, new UndoManager());        
        
        doc.addUndoableEditListener(cuh);
        doc.setDocumentFilter(new IndentationFilter());
        doc.addDocumentListener(textChangedHandler);
        ed.setDocument(doc);
        ed.addFocusListener(focusHandler);        
        ed.addCaretListener(caretHandler);
        ed.addMouseListener(popupHandler);
        ed.setFont(TBGlobals.getEditorFont());
        
        return ed;
    }
    
    private JEditorPane createWysiwygEditor()
    {
        JEditorPane ed = new JTextPane();        
        HTMLEditorKit editorKit = new WysiwygHTMLEditorKit(); 
        //change default body font to Tahoma... The default font can't handle
        //some greek characters
        editorKit.getStyleSheet().addRule("body { font-family:Tahoma; }");        
        ed.setEditorKitForContentType("text/html", editorKit); //$NON-NLS-1$
        ed.setContentType("text/html");        //$NON-NLS-1$                            
                
        ed.addCaretListener(caretHandler);
        ed.addFocusListener(focusHandler);
        ed.addMouseListener(popupHandler);
        
        HTMLDocument document = (HTMLDocument)ed.getDocument();        
        CompoundUndoManager cuh = new CompoundUndoManager(document, new UndoManager());
        document.addUndoableEditListener(cuh);
        document.addDocumentListener(textChangedHandler);
                
        return ed;        
    }
    /* ****************** end UI creation methods ********************** */
        
        
    private void insertHTML(String html, int location) 
    {       
        try 
        {
            HTMLEditorKit kit = (HTMLEditorKit)wysEditor.getEditorKit();
            Document doc = wysEditor.getDocument();
            StringReader reader = new StringReader(HTMLUtils.jEditorPaneizeHTML(html));
            kit.read(reader, doc, location);
            //wysEditor.read(reader, doc);
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    // called when changing tabs
    private void updateEditView()
    {       
        Document doc = null;
        if(getEditMode() == WYSIWYG)
        {           
            String topText = removeInvalidTags(srcEditor.getText());            
            wysEditor.setText(""); //$NON-NLS-1$
            insertHTML(topText, 0);
            wysEditor.setCaretPosition(0);
            //CompoundUndoManager.discardAllEdits(wysEditor.getDocument());
            doc = wysEditor.getDocument();
        }
        else 
        {           
            String topText = removeInvalidTags(wysEditor.getText());            
            if(isWysTextChanged || srcEditor.getText().equals("")) //$NON-NLS-1$
            {
                String t = deIndent(removeInvalidTags(topText));
                t = Entities.HTML40.unescapeUnknownEntities(t);                
                srcEditor.setText(t);
            }
            srcEditor.setCaretPosition(0);
            //CompoundUndoManager.discardAllEdits(srcEditor.getDocument());
            doc = srcEditor.getDocument();
        }       
        
        TBGlobals.putProperty("EDITOR_TYPE", getEditMode() + ""); //$NON-NLS-1$ //$NON-NLS-2$
        CompoundUndoManager.discardAllEdits(doc);  
        CompoundUndoManager.updateUndo(doc);
        isWysTextChanged = false;        
        updateState();        
    }
    
    
    /* *******************************************************************
     *  Methods for dealing with HTML between wysiwyg and source editors 
     * ******************************************************************/
    private String deIndent(String html)
    {
        String ws = "\n    "; //$NON-NLS-1$
        StringBuffer sb = new StringBuffer(html);
        
        while(sb.indexOf(ws) != -1)
        {             
            int s = sb.indexOf(ws);            
            int e = s + ws.length();
            sb.delete(s, e);
            sb.insert(s, "\n");           //$NON-NLS-1$
        }
        
        return sb.toString();
    }
    
    private String removeInvalidTags(String html)
    {
        for(int i = 0; i < INVALID_TAGS.length; i++)
        {
            html = deleteOccurance(html, '<' + INVALID_TAGS[i] + '>');
            html = deleteOccurance(html, "</" + INVALID_TAGS[i] + '>'); //$NON-NLS-1$
        }
           
        return html.trim();
    }
    
    private String deleteOccurance(String text, String word)
    {
        StringBuffer sb = new StringBuffer(text);       
        int p;
        while((p = sb.toString().toLowerCase().indexOf(word.toLowerCase())) != -1)
        {           
            sb.delete(p, p + word.length());            
        }
        return sb.toString();
    }
    /* ************************************* */
    
    /**
     * Manages the 'selected' state of affairs of UI components
     */
    private void updateState()
    {       
        if(focusedEditor == wysEditor)
        {            
            fontFamilyCombo.removeActionListener(fontChangeHandler);
            String fontName = HTMLUtils.getFontFamily(wysEditor);
            if(fontName == null)
                fontFamilyCombo.setSelectedIndex(0);
            else
                fontFamilyCombo.setSelectedItem(fontName);
            fontFamilyCombo.addActionListener(fontChangeHandler);            
        }        
                
        actionList.putContextValueForAll(HTMLTextEditAction.EDITOR, focusedEditor);        
        //actionList.updateEnabledForAll();
    }
    
    public boolean hasUserCancelled()
    {
        return cancelled;   
    }
    
    public boolean hasUserClickedPublish()
    {
        return isPublish;       
    }
    
    public int getMode()
    {
        return mode;    
    }
    
    public BlogEntry getEntry()
    {
        return entry;   
    }
  
    public Weblog getSelectedWeblog()
    {
        return weblog;
    }
    
    public void setTitle(String title)
    {
        String entry = i18n.str("new_entry"); //$NON-NLS-1$
        if(mode == UPDATE_ENTRY_MODE)
            entry = i18n.str("update_entry"); //$NON-NLS-1$
        
        if(title.equals("")) //$NON-NLS-1$
            title = i18n.str("untitled"); //$NON-NLS-1$
            
        entry += " [" + title +"]"; //$NON-NLS-1$ //$NON-NLS-2$
        super.setTitle(entry);
    }
    
    public void setVisible(boolean b)
    {
        if(b)
        {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int x = ((d.width - getSize().width) / 2);
            int y = ((d.height - getSize().height) / 2);
            setLocation(x, y);
            
            try
            {
                int loc = Integer.parseInt(TBGlobals.getProperty("EDITOR_DIV")); //$NON-NLS-1$
                attribSplitPane.setDividerLocation(loc);
                attribSplitPane.setResizeWeight((double)loc/getWidth());
            }
            catch(Exception ex)
            {
                System.out.println(ex.getMessage());
                attribSplitPane.setDividerLocation(.80);
                attribSplitPane.setResizeWeight(.80);
            }           
        }

        super.setVisible(b);
    }
    
    public void dispose()
    {
        //remove the listeners registered with the weblog
        for(int i = 0; i < weblogList.getWeblogCount(); i++)
        {
            Weblog w = weblogList.getWeblogAt(i);
            if(w != null)
            {
                w.removeAuthorListener(changeListener);
                w.removeCategoryListener(changeListener);
                w.removeWeblogListener(changeListener);
            }
        }
        TBGlobals.putProperty("EDITOR_DIV", attribSplitPane.getDividerLocation() + ""); //$NON-NLS-1$ //$NON-NLS-2$
       
        WeblogPreviewer.getInstance().clearPreviewData();        
        super.dispose();
    }
    
    private void closeEditor()
    {
        if(shouldAskToSave)
        {
            String msg = i18n.str("save_changes_draft_msg"); //$NON-NLS-1$
            if(mode == UPDATE_ENTRY_MODE)
                msg = i18n.str("save_changes_msg"); //$NON-NLS-1$
            
            int r = JOptionPane.showConfirmDialog(this, 
                msg, i18n.str("save"),  //$NON-NLS-1$
                JOptionPane.YES_NO_CANCEL_OPTION);
            if(r == JOptionPane.YES_OPTION)             
                saveEntryAndExit(entry.isDraft() || mode != UPDATE_ENTRY_MODE);
            else if(r == JOptionPane.NO_OPTION)
                dispose();
            else
                return;
        }
        else
            dispose();
    }
    
    private void saveEntryAndExit(boolean isSaveAsDraft)
    {
        if(isSaveAsDraft)
        {
            entry.setDraft(true);
            isPublish = false;
            entry.setLastModified(null);            
        }
        else
        {
            if(mode == UPDATE_ENTRY_MODE && !entry.isDraft())
            {           
                if(markModifiedCb.isSelected())
                    entry.setLastModified(new Date());
                else
                    entry.setLastModified(null);
            }
            //shouldn't ever get executed
            else if(mode == UPDATE_ENTRY_MODE && entry.isDraft())
            {
                entry.setLastModified(null);                
            }           
            
            entry.setDraft(false);          
        }       
        
        updateEntry(entry);
        
        if(!entry.isDraft())
            entry.setText(EntryImageUtils.changeLocalImageURLs(entry.getText(), weblog));
                
        cancelled = false;
        dispose();
    }
    
    private void updateEntry(BlogEntry entry)
    {
        Object a = authorCombo.getSelectedItem();
        if(a instanceof Author)         
            entry.setAuthor((Author)a);
            
        entry.setDate(dateCombo.getDate());
        entry.setTitle(titleField.getText());       
        entry.setCategories(catPane.getSelectedCategories());
        
        if(getEditMode() == SOURCE)
            entry.setText(srcEditor.getText());
        else
        {
            String txt = removeInvalidTags(wysEditor.getText());
            txt = Entities.HTML_BASIC.unescapeUnknownEntities(txt);
            entry.setText(txt);            
        }
    }
    
    private void updateAuthors(Weblog weblog)
    {       
        authorCombo.removeAllItems();
        try
        {                       
            Author auths[] = weblog.getAuthors();
            for(int i = 0; i < auths.length; i++)
                authorCombo.addItem(auths[i]);                  
        }
        catch(Exception ex){}
        authorCombo.addItem("");//$NON-NLS-1$
    }
    
    private void updateCategories(Weblog weblog)
    {
        entry.setCategories(new String[0]);
        try
        {       
            //TODO if editor isn't visible, catPane doesn't seem to get repainted
            catPane.setCategories(weblog.getCategories(), entry);
            catPane.revalidate();
            attribSplitPane.repaint();          
        }
        catch(BackendException ex)
        {
        }
    }
    
    
    private class CaretHandler implements CaretListener
    {
        /* (non-Javadoc)
         * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
         */
        public void caretUpdate(CaretEvent e)
        {            
            updateState();
        }        
    }
    
    private class PopupHandler extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        { checkForPopupTrigger(e); }
        
        public void mouseReleased(MouseEvent e)
        { checkForPopupTrigger(e); }
        
        private void checkForPopupTrigger(MouseEvent e)
        {
            if(e.isPopupTrigger())
            {                    
                JPopupMenu p = null;
                if(e.getSource() == wysEditor)
                    p =  wysPopupMenu;
                else if(e.getSource() == srcEditor)
                    p = srcPopupMenu;
                else
                    return;
                p.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    private class FocusHandler implements FocusListener
    {
        public void focusGained(FocusEvent e)
        {
            if(e.getComponent() instanceof JTextComponent)
            {
                JTextComponent ed = (JTextComponent)e.getComponent();
                //CompoundUndoManager.updateUndo(ed.getDocument());
                focusedEditor = ed;                
                updateState(); 
                actionList.updateEnabledForAll();
                boolean isWys = focusedEditor == wysEditor;
                fontSizeButton.setEnabled(focusedEditor == wysEditor || focusedEditor == srcEditor);
                fontFamilyCombo.setEnabled(isWys && tabs.getSelectedIndex() == 0);
                paragraphCombo.setEnabled(isWys && tabs.getSelectedIndex() == 0);
            }
        }
        
        public void focusLost(FocusEvent e)
        {           
        }
    }
    
    private class TextChangedHandler implements DocumentListener
    {
        public void insertUpdate(DocumentEvent e)
        {
            textChanged();
        }
        
        public void removeUpdate(DocumentEvent e)
        {
            textChanged();
        }
        
        public void changedUpdate(DocumentEvent e)
        {
            textChanged();
        }
        
        private void textChanged()
        {
            if(tabs.getSelectedIndex() == 0)
                isWysTextChanged = true;
            
            shouldAskToSave = true;
        }
    }
    
    private class ChangeTabAction extends DefaultAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        int tab;        
        public ChangeTabAction(int tab)
        {
            super((tab == 0) ? i18n.str("rich_text") : //$NON-NLS-1$
                i18n.str("source")); //$NON-NLS-1$
            this.tab = tab;
            putValue(ActionManager.BUTTON_TYPE, ActionManager.BUTTON_TYPE_VALUE_RADIO);
        }
        
        protected void execute(ActionEvent e)
        {
            tabs.setSelectedIndex(tab);
            setSelected(true);
        }
        
        protected void contextChanged()
        {
            setSelected(tabs.getSelectedIndex() == tab);
        }
    }
    
    private class ParagraphComboHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == paragraphCombo)
            {
                Action a = (Action)(paragraphCombo.getSelectedItem());
                a.actionPerformed(e);
            }
        }
    }
    
    private class ParagraphComboRenderer extends DefaultListCellRenderer
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
        {
            if(value instanceof Action)
            {
                value = ((Action)value).getValue(Action.NAME);
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
    
    private class FontChangeHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == fontFamilyCombo && focusedEditor == wysEditor )
            {                
                //MutableAttributeSet tagAttrs = new SimpleAttributeSet();
                HTMLDocument document = (HTMLDocument)focusedEditor.getDocument();
                CompoundUndoManager.beginCompoundEdit(document);                
                if(fontFamilyCombo.getSelectedIndex() != 0)
                {
                    HTMLUtils.setFontFamily(wysEditor, (String)fontFamilyCombo.getSelectedItem());                    
                }
                else
                {
                    HTMLUtils.setFontFamily(wysEditor, null);                    
                }
                CompoundUndoManager.endCompoundEdit(document);
            }
        }
    }
    
    
    
    //Spell checker action
    private class SpellCheckAction extends AbstractAction
    {    
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SpellCheckAction()
        {
            super(i18n.str("check_spelling_")); //$NON-NLS-1$
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("check_spelling_"))); //$NON-NLS-1$
            putValue(Action.SHORT_DESCRIPTION, getValue(NAME));
            putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "spellcheck.png")); //$NON-NLS-1$
            putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "spellcheck.png")); //$NON-NLS-1$
        }
        
        public void actionPerformed(ActionEvent e)
        {
            File d = new File(TBGlobals.DICT_DIR, TBGlobals.getDictionary() + ".dic"); //$NON-NLS-1$
            File a = new File(TBGlobals.DICT_DIR, TBGlobals.getDictionary() + ".aff"); //$NON-NLS-1$
            File userDic = new File(TBGlobals.PROP_DIR, "dict.user"); //$NON-NLS-1$
            
            try
            {
                SpellDictionary dict = new OpenOfficeSpellDictionary(d, a, userDic);
                SpellChecker checker = new SpellChecker(dict);
                JTextComponentSpellChecker textSpellChecker = 
                    new JTextComponentSpellChecker(checker); 
                
                JTextComponent textArea = null;
                if(getEditMode() == WYSIWYG)
                    textArea = wysEditor;
                else
                    textArea = srcEditor;
                
                if(textSpellChecker.spellCheck(textArea))
                {
                    JOptionPane.showMessageDialog(EntryEditor.this,
                        i18n.str("spellcheck_complete"), //$NON-NLS-1$
                        i18n.str("spellcheck_complete"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE);
                }
                textArea.requestFocusInWindow();                
            }
            catch(Exception ex)
            {               
                UIUtils.showError(EntryEditor.this, i18n.str("spellcheck_error"), ex); //$NON-NLS-1$  
            }
        }
    }
    
    private class PreviewAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public PreviewAction()
        {
            super(i18n.str("preview_")); //$NON-NLS-1$
            putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
            //Messages.setMnemonic("EntryEditor.Check_Spelling", this); //$NON-NLS-1$
            putValue(Action.SHORT_DESCRIPTION, getValue(NAME));
            putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "browser.png")); //$NON-NLS-1$
            putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "browser.png")); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {            
            BlogEntry temp = new BlogEntry();
            updateEntry(temp);
            
            try
            {
                TBWeblog tbw = (TBWeblog)getSelectedWeblog();
                WeblogPreviewer.getInstance().previewInBrowser(tbw, new BlogEntry[]{temp});
            }
            catch(Exception ex)
            {
                UIUtils.showError(EntryEditor.this, 
                    "Error occured while trying to launch preview.", ex); //$NON-NLS-1$ 
            }
        }
    }
    
    public class SaveAsDraftAction extends AbstractAction 
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SaveAsDraftAction()
        {
            super(i18n.str("save_as_draft")); //$NON-NLS-1$
            putValue(SHORT_DESCRIPTION, getValue(NAME));
            putValue(ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
            putValue(MNEMONIC_KEY, new Integer(i18n.mnem("save_as_draft"))); //$NON-NLS-1$
            putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "save.png")); //$NON-NLS-1$
            putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "save.png")); //$NON-NLS-1$
        }

        public void actionPerformed(ActionEvent e)
        {
            saveEntryAndExit(true);
        }
    }
        
    
    private class EntryAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private boolean publishAction = false;
        
        public EntryAction(int mode, boolean pubAction)
        {
            super(""); //$NON-NLS-1$
            if(mode == UPDATE_ENTRY_MODE)
            {
                if(pubAction)
                {
                    putValue(NAME, i18n.str("update_and_publish")); //$NON-NLS-1$
                    putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "update_post_pub.png")); //$NON-NLS-1$
                    putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "update_post_pub.png")); //$NON-NLS-1$
                }
                else
                {
                    putValue(NAME, i18n.str("update")); //$NON-NLS-1$
                    putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "update_post.png")); //$NON-NLS-1$
                    putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "update_post.png")); //$NON-NLS-1$
                }
            }
            else
            {
                if(pubAction)
                {
                    putValue(NAME, i18n.str("post_and_publish")); //$NON-NLS-1$
                    putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "post_pub.png")); //$NON-NLS-1$
                    putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "post_pub.png")); //$NON-NLS-1$
                }
                else
                {
                    putValue(NAME, i18n.str("post")); //$NON-NLS-1$
                    putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "post.png")); //$NON-NLS-1$
                    putValue(ActionManager.LARGE_ICON, UIUtils.getIcon(UIUtils.X24, "post.png")); //$NON-NLS-1$
                }
            }
            
            putValue(SHORT_DESCRIPTION, getValue(NAME));
            publishAction = pubAction;
        }
        
        public void actionPerformed(ActionEvent e)
        {           
            boolean saveAsDraft = false;
            
            //is the date combo set after the current date?
            if(dateCombo.getDate().after(new Date()))
            {
                int r = JOptionPane.showConfirmDialog(EntryEditor.this, 
                        i18n.str("save_as_draft_prompt"), //$NON-NLS-1$
                        i18n.str("confirm"), JOptionPane.YES_NO_OPTION,  //$NON-NLS-1$
                        JOptionPane.QUESTION_MESSAGE);
                
                if(r == JOptionPane.YES_OPTION)
                    saveAsDraft = true;
                else
                    return;                 
            }
            
            //entry.setDraft(false);            
            isPublish = publishAction;
            saveEntryAndExit(saveAsDraft);
        }
    }
    
    private class InternalLinkAction extends HTMLLinkAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public InternalLinkAction()
        {
            super();
            putValue(NAME, i18n.str("internal_link_")); //$NON-NLS-1$
            putValue(SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "webloglink.png")); //$NON-NLS-1$
        }
        
        protected HyperlinkDialog createDialog(JTextComponent ed)
        {
            return new InternalLinkDialog(EntryEditor.this, (TBWeblog)getSelectedWeblog());
        }
    }
    
    private class WeblogImageAction extends HTMLImageAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected ImageDialog createDialog(JTextComponent ed)
        {
            return new WeblogImageDialog(EntryEditor.this, getSelectedWeblog());
        }
    }
    
    private class PropertiesAction extends HTMLElementPropertiesAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected ImageDialog createImageDialog()
        {
            return new WeblogImageDialog(EntryEditor.this, getSelectedWeblog());
        }
    }
    
    //listens for changes on the weblogs and adjusts the editor accordingly
    private class WeblogChangeListener 
    implements WeblogListener, CategoryListener, AuthorListener
    {
        private Weblog getCurrentWeblog()
        {
            Weblog w = (Weblog)siteCombo.getSelectedItem();
            return w;
        }
        
        public void authorAdded(AuthorEvent e)
        {
            updateAuthors(getCurrentWeblog());
        }
        
        public void authorUpdated(AuthorEvent e)
        {
            updateAuthors(getCurrentWeblog());
        }
        
        public void authorRemoved(AuthorEvent e)
        {
            updateAuthors(getCurrentWeblog());
        }
        
        public void categoryAdded(CategoryEvent e)
        {
            updateCategories(getCurrentWeblog());
        }
        
        public void categoryRenamed(CategoryEvent e)
        {
            updateCategories(getCurrentWeblog());
        }
        
        public void categoryRemoved(CategoryEvent e)
        {
            updateCategories(getCurrentWeblog());
        }
        
        public void entryRemoved(WeblogEvent e)
        {
            //the entry we're editing has been deleted
            //so close the editor
            if(mode == UPDATE_ENTRY_MODE)
            {
                if(entry.getID() == e.getEntry().getID())
                    closeEditor();
            }
        }
        
        public void entryAdded(WeblogEvent e){}     
        public void entryUpdated(WeblogEvent e){}
    }
}
