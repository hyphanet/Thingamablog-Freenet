
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.AuthorEvent;
import net.sf.thingamablog.blog.AuthorListener;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.CategoryEvent;
import net.sf.thingamablog.blog.CategoryListener;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogEvent;
import net.sf.thingamablog.blog.WeblogList;
import net.sf.thingamablog.blog.WeblogListener;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.TBAbout;
import net.sf.thingamablog.gui.TBHelpAction;
import net.sf.thingamablog.xml.Entities;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;

import com.tantlinger.jdatepicker.JCalendarComboBox;


/**
 * 
 * @author Bob Tantlinger
 *
 */
public class EntryEditor extends JFrame
{	
	//constants
	public static final int NEW_ENTRY_MODE = -1;
	public static final int UPDATE_ENTRY_MODE = -2;	
	private static final int WYSIWYG = 0; //wysiwyg editor tab
	private static final int SOURCE = 1;//source editor tab;
	private static final String INVALID_TAGS[] = {"html", "head", "body", "title"};
	private static final String RES = TBGlobals.RESOURCES;	
    private final Border plainBorder = BorderFactory.createEtchedBorder(
		Color.white, new Color(142, 142, 142));
	private final Border pressedBorder = BorderFactory.createBevelBorder(
		BevelBorder.LOWERED, Color.white, Color.white,
		new Color(142, 142, 142), new Color(99, 99, 99));
	
    //editors n stuff
	private JEditorPane editor;
    private HTMLDocument document;
    private RSyntaxTextArea srcEditor;
	private CharTablePanel charTablePanel;
    private JPanel editorPanel;
    private JTabbedPane tabs = new JTabbedPane(JTabbedPane.BOTTOM);
    private JButton fontSizeButton = new JButton(Utils.createIcon(RES + "fontsize.png"));
    private JButton boldButton;
    private JButton italicButton;
    private JButton underlineButton;
    private JButton specialCharButton;    
    private JButton olButton;
    private JButton ulButton;
    private JButton alLeftButton;
    private JButton alCenterButton;
    private JButton alRightButton;
    private JButton alJustButton;
    private JComboBox fontFamilyCombo;
    private JComboBox paraStyleCombo;
    private boolean isWysTextChanged;
    private boolean shouldAskToSave;
    private DocumentListener textChangedListener = new TextChangedListener();
    
    //editor actions
    private HTMLPropsAction props;
    private ImageAction image;
    private Action wordWrap = new WordWrapAction();
	private Action fontColor = new HTMLFontColorAction(this);
	   
	private Action alLeft = new HTMLAlignAction(HTMLAlignAction.LEFT);
	private Action alRight = new HTMLAlignAction(HTMLAlignAction.RIGHT);
	private Action alCenter = new HTMLAlignAction(HTMLAlignAction.CENTER);
    private Action alJust = new HTMLAlignAction(HTMLAlignAction.JUSTIFY);
	private Action lineBreak = new HTMLLineBreakAction();
	private Action insertChar = new InsertCharAction();
		
	private Action link = new HTMLLinkAction(this);
	private Action table = new HTMLTableAction(this);
	private Action insertHR = new HTMLHorizontalRuleAction();
	private Action clearStyles = new ClearStylesAction();
	private Action ilCite = new HTMLInlineAction(HTMLInlineAction.I_CITE);
	private Action ilCode = new HTMLInlineAction(HTMLInlineAction.I_CODE);
	private Action ilEm = new HTMLInlineAction(HTMLInlineAction.I_EM);
	private Action ilStrong = new HTMLInlineAction(HTMLInlineAction.I_STRONG);
	private Action ilSub = new HTMLInlineAction(HTMLInlineAction.I_SUBSCRIPT);
	private Action ilSup = new HTMLInlineAction(HTMLInlineAction.I_SUPERSCRIPT);
	private Action bold = new HTMLInlineAction(HTMLInlineAction.BOLD);
	private Action italic = new HTMLInlineAction(HTMLInlineAction.ITALIC);
	private Action under = new HTMLInlineAction(HTMLInlineAction.UNDERLINE);
	private Action ilStrike = new HTMLInlineAction(HTMLInlineAction.STRIKE);
	
	private Action ul = new HTMLBlockAction(HTMLBlockAction.T_UL);
	private Action ol = new HTMLBlockAction(HTMLBlockAction.T_OL);
	private Action para = new HTMLBlockAction(HTMLBlockAction.T_P);
	private Action pre = new HTMLBlockAction(HTMLBlockAction.T_PRE);
	private Action blockq = new HTMLBlockAction(HTMLBlockAction.T_BLOCKQ);
	private Action h1 = new HTMLBlockAction(HTMLBlockAction.T_H1);
	private Action h2 = new HTMLBlockAction(HTMLBlockAction.T_H2);
	private Action h3 = new HTMLBlockAction(HTMLBlockAction.T_H3);
	private Action h4 = new HTMLBlockAction(HTMLBlockAction.T_H4);
	private Action h5 = new HTMLBlockAction(HTMLBlockAction.T_H5);
	private Action h6 = new HTMLBlockAction(HTMLBlockAction.T_H6);
	private Action xxSmall = new HTMLFontSizeAction(HTMLFontSizeAction.XXSMALL);
	private Action xSmall = new HTMLFontSizeAction(HTMLFontSizeAction.XSMALL);
	private Action small = new HTMLFontSizeAction(HTMLFontSizeAction.SMALL);
	private Action medium = new HTMLFontSizeAction(HTMLFontSizeAction.MEDIUM);
	private Action large = new HTMLFontSizeAction(HTMLFontSizeAction.LARGE);
	private Action xLarge = new HTMLFontSizeAction(HTMLFontSizeAction.XLARGE);
	private Action xxLarge = new HTMLFontSizeAction(HTMLFontSizeAction.XXLARGE);
	private Action cut = new HTMLEditorKit.CutAction();
	private Action copy = new HTMLEditorKit.CopyAction();
	private Action paste = new PasteAction();
	private Action selectAll = new SelectAllAction();

	private TableEditAction insertTableCell;
	private TableEditAction deleteTableCell;
	private TableEditAction insertTableRow;	
	private TableEditAction deleteTableRow;
	private TableEditAction insertTableCol;
	private TableEditAction deleteTableCol;
	
    private FindReplaceAction findAction;
    private FindReplaceAction replaceAction;
    private ActionListener editHandler = new EditActionHandler();
    
	private UndoAction undoAction = new UndoAction();
	private RedoAction redoAction = new RedoAction();
	private UndoManager wysUndoer = new UndoManager();
        
    //Menu items that need to be kept track of
    private JMenu formatMenu;
    private JMenu editMenu; 
    private JRadioButtonMenuItem richTextMI;
    private JRadioButtonMenuItem srcEditMI;    
    private JRadioButtonMenuItem alLeftMI = new JRadioButtonMenuItem(alLeft);
    private JRadioButtonMenuItem alCenterMI = new JRadioButtonMenuItem(alCenter);
    private JRadioButtonMenuItem alJustMI = new JRadioButtonMenuItem(alJust);
    private JRadioButtonMenuItem alRightMI = new JRadioButtonMenuItem(alRight);
    private JRadioButtonMenuItem paraMI = new JRadioButtonMenuItem(para);
    private JRadioButtonMenuItem preMI = new JRadioButtonMenuItem(pre);
    private JRadioButtonMenuItem blockqMI = new JRadioButtonMenuItem(blockq);
    private JRadioButtonMenuItem h1MI = new JRadioButtonMenuItem(h1);
    private JRadioButtonMenuItem h2MI = new JRadioButtonMenuItem(h2);
    private JRadioButtonMenuItem h3MI = new JRadioButtonMenuItem(h3);
    private JRadioButtonMenuItem h4MI = new JRadioButtonMenuItem(h4);
    private JRadioButtonMenuItem h5MI = new JRadioButtonMenuItem(h5);
    private JRadioButtonMenuItem h6MI = new JRadioButtonMenuItem(h6);    
    private JRadioButtonMenuItem xxSmallMI = new JRadioButtonMenuItem(xxSmall);
    private JRadioButtonMenuItem xSmallMI = new JRadioButtonMenuItem(xSmall);
    private JRadioButtonMenuItem smallMI = new JRadioButtonMenuItem(small);
    private JRadioButtonMenuItem mediumMI = new JRadioButtonMenuItem(medium);
    private JRadioButtonMenuItem largeMI = new JRadioButtonMenuItem(large);
    private JRadioButtonMenuItem xLargeMI = new JRadioButtonMenuItem(xLarge);
    private JRadioButtonMenuItem xxLargeMI = new JRadioButtonMenuItem(xxLarge);
    private JRadioButtonMenuItem ulMI = new JRadioButtonMenuItem(ul);
    private JRadioButtonMenuItem olMI = new JRadioButtonMenuItem(ol);
    
    private ButtonGroup sizeGroup = new ButtonGroup();
    private ButtonGroup paraGroup = new ButtonGroup();
    private Hashtable paraActions = new Hashtable();    
    
    //style menu items that need to be kept track of
    private JCheckBoxMenuItem citeMI = new JCheckBoxMenuItem(ilCite);
    private JCheckBoxMenuItem codeMI = new JCheckBoxMenuItem(ilCode);
    private JCheckBoxMenuItem emMI = new JCheckBoxMenuItem(ilEm);
    private JCheckBoxMenuItem strongMI = new JCheckBoxMenuItem(ilStrong);
    private JCheckBoxMenuItem subMI = new JCheckBoxMenuItem(ilSub);
    private JCheckBoxMenuItem supMI = new JCheckBoxMenuItem(ilSup);
    private JCheckBoxMenuItem strikeMI = new JCheckBoxMenuItem(ilStrike);
    private JCheckBoxMenuItem boldMI = new JCheckBoxMenuItem(bold);
    private JCheckBoxMenuItem italicMI  = new JCheckBoxMenuItem(italic);
    private JCheckBoxMenuItem underMI  = new JCheckBoxMenuItem(under); 
    private JCheckBoxMenuItem wordWrapMenuItem;
    
    //entry editor components
	private JTextField titleField;
	private JComboBox authorCombo;
	private JComboBox weblogCombo;
	private JCalendarComboBox dateCombo;
	private CategoryEditorPane catPane;
	private JCheckBox markModifiedCb;
	private JSplitPane attribSplitPane;
	
	private Action savePublishEntryAction, saveEntryAction;
	private Action saveAsDraft = new SaveAsDraftAction();
	private Action spellCheck = new SpellCheckAction();
	private Action imageAction;
    
	private boolean cancelled = true;
	private boolean isPublish = false;
	private int mode;
	private BlogEntry entry;	
	private Weblog weblog;
	private WeblogList weblogList;
	private WeblogChangeListener changeListener = new WeblogChangeListener();
	
	private JToolBar toolBar;	

	public EntryEditor(Weblog blog, WeblogList list) throws BackendException
	{
		this(new BlogEntry(), blog, list, NEW_ENTRY_MODE);
	}
	
	public EntryEditor(BlogEntry e, Weblog blog, WeblogList list) 
	throws BackendException
	{
		this(e, blog, list, UPDATE_ENTRY_MODE);
	}
	
	public EntryEditor(BlogEntry e, Weblog blog, WeblogList list, int entryMode) 
	throws BackendException
	{		
	    entry = e;
		weblogList = list;
		weblog = blog;
		mode = entryMode;		
		if(entry.isDraft())
		    entry.setDate(new Date());
		
		setIconImage(Utils.createIcon(RES + "new_entry16.png").getImage());
		setTitle(entry.getTitle());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
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
		
		//initialize the editors
		editorPanel = createEditorPanel();
        JPanel attribs = createEntryAttributePanel();        
        initActions();
		
        //build gui
        toolBar = createToolBar();
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(createFormatToolbar(), BorderLayout.NORTH);
		contentPanel.add(editorPanel, BorderLayout.CENTER);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(attribs, BorderLayout.NORTH);
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(toolBar, BorderLayout.NORTH);
		setJMenuBar(createMenuBar());
		
		//wysiwyg editor is a little crazy if a paragraph tag is missing
		if(tabs.getSelectedIndex() == WYSIWYG && mode == NEW_ENTRY_MODE)
		    entry.setText("<p>" + entry.getText() + "</p>");
            
        
		
		configureCurrentEditor(entry.getText());
		updateEnabledStates();
		shouldAskToSave = false;
	}
	
    private JPanel createEditorPanel()
    {
        initWYSEditor();
        initSRCEditor();        
        
        JPanel editorPanel = new JPanel(new BorderLayout());
		JScrollPane scroller = new JScrollPane(editor);
		tabs.addTab(Messages.getString("EntryEditor.Edit"), scroller);
		scroller = new JScrollPane(srcEditor,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tabs.addTab(Messages.getString("EntryEditor.Edit_Tab"), scroller);
		editorPanel.add(tabs, BorderLayout.CENTER);
		try{
		    int tab = Integer.parseInt(TBGlobals.getProperty("EDITOR_TYPE"));		    
		    tabs.setSelectedIndex(tab);
		}catch(Exception ex){}
        tabs.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if(tabs.getSelectedIndex() == SOURCE)
                {                    
                    configureCurrentEditor(editor.getText());                    
                    charTablePanel.setEditor(srcEditor);
                }
                else
                {
                    configureCurrentEditor(srcEditor.getText());
                    charTablePanel.setEditor(editor);
                }               
            }
        });
        
        return editorPanel;
    }
    
    private void initWYSEditor()
    {
        editor = new JTextPane();    
        editor.setEditorKitForContentType("text/html", new WysiwygHTMLEditorKit());
        editor.setContentType("text/html");
        HTMLEditorKit ekit = (HTMLEditorKit)editor.getEditorKit();
        Action a[] = ekit.getActions();
        Hashtable ht = new Hashtable();
        for(int i = 0; i < a.length; i++)
            ht.put(a[i].getValue(Action.NAME), a[i]);
        
        Action defPara = (Action)ht.get(HTMLEditorKit.insertBreakAction);       
        Action defBS = (Action)ht.get(HTMLEditorKit.deletePrevCharAction);
        Action defDel = (Action)ht.get(HTMLEditorKit.deleteNextCharAction);
        
        KeyStroke enterKS = KeyStroke.getKeyStroke("ENTER");
        KeyStroke bsKS = KeyStroke.getKeyStroke("typed \010");//backspace
        KeyStroke delKS = KeyStroke.getKeyStroke("DELETE");
        KeyStroke pasteKS = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
        
        InputMap inputMap = editor.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = editor.getActionMap();
        
        //get rid of this mapping if need be
        inputMap.put(KeyStroke.getKeyStroke("BACK_SPACE"), "none");        
        
        inputMap.put(enterKS, "MyEnter");
        actionMap.put("MyEnter", new EnterKeyAction(defPara));
        
        inputMap.put(pasteKS, "MyPaste");
        actionMap.put("MyPaste", paste);
        
        inputMap.put(delKS, "MyDel");
        actionMap.put("MyDel", new RemoveAction(RemoveAction.DELETE, defDel));
        
        inputMap.put(bsKS, "MyBS");
        actionMap.put("MyBS", new RemoveAction(RemoveAction.BACKSPACE, defBS));
        
        editor.addCaretListener(new CaretListener()
        {
            public void caretUpdate(CaretEvent e)
            {
                boolean sel = e.getDot() != e.getMark();
                cut.setEnabled(sel);
                copy.setEnabled(sel);
                wysiwygUpdated();
            }
        });     
        document = (HTMLDocument)editor.getDocument();
        CompoundUndoHandler cuh = new CompoundUndoHandler(document, wysUndoer)
        {
            public void editAdded()
            {
                updateUndo();
            }
        };
        document.addUndoableEditListener(cuh);
        document.addDocumentListener(textChangedListener);
        document.setPreservesUnknownTags(true);
        editor.addMouseListener(new PopupListener());
        charTablePanel = new CharTablePanel(editor);        
    }
	

    
    private void initSRCEditor()
    {        
        srcEditor = new SourceTextArea();
        srcEditor.setSyntaxEditingStyle(RSyntaxTextArea.HTML_SYNTAX_STYLE);
        
		String tf = TBGlobals.getProperty("EDITOR_WORDWRAP"); 
		srcEditor.setLineWrap(tf != null && tf.equals("true"));
		srcEditor.getDocument().addDocumentListener(textChangedListener);
    }
	
	private void initActions()
	{
        String t[] = HTMLBlockAction.elementTypes;        
        paraActions.put(t[HTMLBlockAction.T_P], para);
        paraActions.put(t[HTMLBlockAction.T_PRE], pre);
        paraActions.put(t[HTMLBlockAction.T_BLOCKQ], blockq);
        paraActions.put(t[HTMLBlockAction.T_H1], h1);
        paraActions.put(t[HTMLBlockAction.T_H2], h2);
        paraActions.put(t[HTMLBlockAction.T_H3], h3);
        paraActions.put(t[HTMLBlockAction.T_H4], h4);
        paraActions.put(t[HTMLBlockAction.T_H5], h5);
        paraActions.put(t[HTMLBlockAction.T_H6], h6);
        paraActions.put(t[HTMLBlockAction.T_UL], ul);
        paraActions.put(t[HTMLBlockAction.T_OL], ol);
        
        paraGroup.add(paraMI);
        paraGroup.add(preMI);
        paraGroup.add(blockqMI);
        paraGroup.add(h1MI);
        paraGroup.add(h2MI);
        paraGroup.add(h3MI);
        paraGroup.add(h4MI);
        paraGroup.add(h5MI);
        paraGroup.add(h6MI);
        paraGroup.add(ulMI);
        paraGroup.add(olMI);
        
        sizeGroup.add(xxSmallMI);
        sizeGroup.add(xSmallMI);
        sizeGroup.add(smallMI);
        sizeGroup.add(mediumMI);
        sizeGroup.add(largeMI);
        sizeGroup.add(xLargeMI);
        sizeGroup.add(xxLargeMI);
          
        JTextComponent tc = srcEditor;
	    if(tabs.getSelectedIndex() == WYSIWYG)	    
	        tc = editor;	    
        findAction = new FindReplaceAction(tc, false);
        replaceAction = new FindReplaceAction(tc, true);
        
	    cut.putValue(Action.NAME, Messages.getString("TextEditActionSet.Cut"));
        copy.putValue(Action.NAME, Messages.getString("TextEditActionSet.Copy"));
        Messages.setMnemonic("TextEditActionSet.Cut", cut);
        Messages.setMnemonic("TextEditActionSet.Copy", copy);
        cut.putValue(Action.SMALL_ICON, Utils.createIcon(RES + "cut.png"));
        cut.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        copy.putValue(Action.SMALL_ICON, Utils.createIcon(RES + "copy.png"));
        copy.putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
	    
	    insertTableCell = new TableEditAction(TableEditAction.INSERT_CELL, editor);
	    deleteTableCell = new TableEditAction(TableEditAction.DELETE_CELL, editor);	    
		insertTableRow = new TableEditAction(TableEditAction.INSERT_ROW, editor);
		deleteTableRow = new TableEditAction(TableEditAction.DELETE_ROW, editor);		
		insertTableCol = new TableEditAction(TableEditAction.INSERT_COL, editor);
		deleteTableCol = new TableEditAction(TableEditAction.DELETE_COL, editor);
	    
		File dir = EntryImageUtils.getImageDirectory(weblog);
		image = new ImageAction(EntryEditor.this, dir);
		props = new HTMLPropsAction(EntryEditor.this, editor, dir);		
		
		//init post actions
		if(mode == UPDATE_ENTRY_MODE && !entry.isDraft())
		{
			saveEntryAction = new EntryAction(
				Messages.getString("EntryEditor.Update"), //$NON-NLS-1$
				Utils.createIcon(RES + "update.gif"), false); //$NON-NLS-1$
			Messages.setMnemonic("EntryEditor.Update", saveEntryAction); //$NON-NLS-1$
			
			savePublishEntryAction = new EntryAction(Messages.getString("EntryEditor.Update_and_Publish"), //$NON-NLS-1$
				Utils.createIcon(RES + "update_pub.gif"), true); //$NON-NLS-1$
		}
		else
		{
			saveEntryAction = new EntryAction(
				Messages.getString("EntryEditor.Post"), //$NON-NLS-1$
				Utils.createIcon(RES + "post.gif"), false); //$NON-NLS-1$
			Messages.setMnemonic("EntryEditor.Post", saveEntryAction); //$NON-NLS-1$
			
			savePublishEntryAction = new EntryAction(Messages.getString("EntryEditor.Post_and_Publish"), //$NON-NLS-1$
				Utils.createIcon(RES + "post_pub.gif"), true);			 //$NON-NLS-1$
		}
	}
	
	private JMenuBar createMenuBar()
	{		
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu(Messages.getString("EntryEditor.File")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.File", fileMenu); //$NON-NLS-1$
		JMenuItem mi = fileMenu.add(savePublishEntryAction);
		mi.setIcon(null);
		mi = fileMenu.add(saveEntryAction);
		mi.setIcon(null);
		mi = fileMenu.add(saveAsDraft);
		mi.setIcon(null);
		fileMenu.addSeparator();
		JMenuItem close = new JMenuItem(Messages.getString("EntryEditor.Close")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.Close", close); //$NON-NLS-1$
		close.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				closeEditor();
			}
		});
		fileMenu.add(close);
		mb.add(fileMenu); 
	    
	    editMenu = new JMenu(Messages.getString("EntryEditor.Edit")); //$NON-NLS-1$
	    Messages.setMnemonic("EntryEditor.Edit", editMenu); //$NON-NLS-1$
	    richTextMI = new JRadioButtonMenuItem(Messages.getString("EntryEditor.Rich_Text"));
	    srcEditMI = new JRadioButtonMenuItem(Messages.getString("EntryEditor.Source"));
	    wordWrapMenuItem = new JCheckBoxMenuItem(wordWrap);
	    wordWrapMenuItem.setToolTipText(null);
	    wordWrapMenuItem.setSelected(srcEditor.getLineWrap());
		ActionListener lst = new ActionListener()
	    {
	        public void actionPerformed(ActionEvent e)
	        {
	            if(e.getSource() == richTextMI)
	                tabs.setSelectedIndex(WYSIWYG);
	            else
	                tabs.setSelectedIndex(SOURCE);
	        }
	    };
	    richTextMI.addActionListener(lst);
	    srcEditMI.addActionListener(lst);	    
		mb.add(editMenu);	
		
		formatMenu = new JMenu(Messages.getString("EntryEditor.Format"));
		Messages.setMnemonic("EntryEditor.Format", formatMenu);		
		mb.add(formatMenu);
        
		JMenu insertMenu = new JMenu(Messages.getString("EntryEditor.Insert")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.Insert", insertMenu); //$NON-NLS-1$
        insertMenu.add(link);
        insertMenu.add(image);
        insertMenu.add(table);
        insertMenu.addSeparator();
        insertMenu.add(lineBreak);
        insertMenu.add(insertHR);
        insertMenu.add(insertChar);
		mb.add(insertMenu);
        
		JMenu toolsMenu = new JMenu(Messages.getString("EntryEditor.Tools")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.Tools", toolsMenu); //$NON-NLS-1$
		toolsMenu.add(new SpellCheckAction());
		mb.add(toolsMenu); 
		
		JMenu helpMenu = new JMenu(Messages.getString("ThingamablogFrame.Help"));
		Messages.setMnemonic("ThingamablogFrame.Help", helpMenu);
		
		Action help = new TBHelpAction(
		        Messages.getString("ThingamablogFrame.Help_Contents"), "ch03.item2");
		help.putValue(Action.SMALL_ICON, Utils.createIcon(TBGlobals.RESOURCES + "help.png"));
		help.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		Messages.setMnemonic("ThingamablogFrame.Help_Contents", help);	
		
		helpMenu.add(help);
		JMenuItem aboutItem = new JMenuItem(Messages.getString("ThingamablogFrame.About"));
		Messages.setMnemonic("ThingamablogFrame.About", aboutItem);
		aboutItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TBAbout.showAboutBox(EntryEditor.this);
			}
		});
		helpMenu.add(aboutItem);
		mb.add(helpMenu);
        
		return mb;	
	}
	
	private void updateEditMenu()
	{
	    editMenu.removeAll();
	    editMenu.add(richTextMI);
	    editMenu.add(srcEditMI);
	    editMenu.addSeparator();
	    
	    if(tabs.getSelectedIndex() == WYSIWYG)
	    {
		    editMenu.add(undoAction);
		    editMenu.add(redoAction);
		    editMenu.addSeparator();
		    editMenu.add(cut);
		    editMenu.add(copy);
		    editMenu.add(paste);
		    editMenu.addSeparator();
		    editMenu.add(selectAll);
	    }
	    else
	    {
	        editMenu.add(wordWrapMenuItem);
	    	editMenu.addSeparator();
		    editMenu.add(RTextArea.getAction(RTextArea.UNDO_ACTION));
		    editMenu.add(RTextArea.getAction(RTextArea.REDO_ACTION));
		    editMenu.addSeparator();
		    editMenu.add(RTextArea.getAction(RTextArea.CUT_ACTION));
		    editMenu.add(RTextArea.getAction(RTextArea.COPY_ACTION));
		    editMenu.add(RTextArea.getAction(RTextArea.PASTE_ACTION));
		    editMenu.addSeparator();
		    editMenu.add(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION));
	    }

	    editMenu.addSeparator();
	    editMenu.add(findAction);
	    editMenu.add(replaceAction);
	    
	    richTextMI.setSelected(tabs.getSelectedIndex() == WYSIWYG);	    
	    srcEditMI.setSelected(!richTextMI.isSelected());
	}
	
    private void updateFormatMenu()
    {
        formatMenu.removeAll();
        
        JMenu size = new JMenu(Messages.getString("EntryEditor.Size"));
        Messages.setMnemonic("EntryEditor.Size", size);
        
        JMenu textStyle = new JMenu(Messages.getString("EntryEditor.Style"));
        Messages.setMnemonic("EntryEditor.Style", textStyle);        
        
        JMenu paragraph = new JMenu(Messages.getString("HTMLEditorActionSet.Paragraph"));
        Messages.setMnemonic("HTMLEditorActionSet.Paragraph", paragraph);
        
        JMenu align = new JMenu(Messages.getString("EntryEditor.Align"));
        Messages.setMnemonic("EntryEditor.Align", align);
        
        JMenu list = new JMenu(Messages.getString("EntryEditor.List"));
        
        JMenu tableMenu = new JMenu(Messages.getString("EntryEditor.Table"));
        Messages.setMnemonic("EntryEditor.Table", tableMenu);
        tableMenu.add(table);
        tableMenu.addSeparator();
        JMenu tInsert = new JMenu(Messages.getString("EntryEditor.Insert"));
        tInsert.add(insertTableCell);
        tInsert.add(insertTableRow);
        tInsert.add(insertTableCol);
        tableMenu.add(tInsert);
        JMenu tDelete = new JMenu(Messages.getString("EntryEditor.Delete"));
        tDelete.add(deleteTableCell);
        tDelete.add(deleteTableRow);
        tDelete.add(deleteTableCol);
        tableMenu.add(tDelete);        
        
        if(tabs.getSelectedIndex() == 0)
        {
            size.add(xxSmallMI);
            size.add(xSmallMI);
            size.add(smallMI);
            size.add(mediumMI);
            size.add(largeMI);
            size.add(xLargeMI);
            size.add(xxLargeMI);
            
            paragraph.add(paraMI);
            paragraph.addSeparator();
            paragraph.add(preMI);
            paragraph.add(blockqMI);
            paragraph.addSeparator();
            paragraph.add(h1MI);
            paragraph.add(h2MI);
            paragraph.add(h3MI);
            paragraph.add(h4MI);
            paragraph.add(h5MI);
            paragraph.add(h6MI);            
                        
            textStyle.add(boldMI);
            textStyle.add(italicMI);
            textStyle.add(underMI);
            textStyle.addSeparator();
            textStyle.add(citeMI);
            textStyle.add(codeMI);
            textStyle.add(emMI);
            textStyle.add(strongMI);
            textStyle.add(subMI);
            textStyle.add(supMI);
            textStyle.add(strikeMI);
            
            list.add(ulMI);
            list.add(olMI);
            
            align.add(alLeftMI);
            align.add(alCenterMI);
            align.add(alRightMI);
            align.add(alJustMI);
        }
        else
        {
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XXSMALL));
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XSMALL));
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.SMALL));
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.MEDIUM));
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.LARGE));
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XLARGE));
            size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XXLARGE));
            
            paragraph.add(para);
            paragraph.addSeparator();
            paragraph.add(pre);
            paragraph.add(blockq);
            paragraph.addSeparator();
            paragraph.add(h1);
            paragraph.add(h2);
            paragraph.add(h3);
            paragraph.add(h4);
            paragraph.add(h5);
            paragraph.add(h6);
            
            textStyle.add(bold);
            textStyle.add(italic);
            textStyle.add(under);
            textStyle.addSeparator();
            textStyle.add(ilCite);
            textStyle.add(ilCode);
            textStyle.add(ilEm);
            textStyle.add(ilStrong);
            textStyle.add(ilSub);
            textStyle.add(ilSup);
            textStyle.add(ilStrike);
            
            list.add(ul);
            list.add(ol);
            
            align.add(alLeft);
            align.add(alCenter);
            align.add(alRight);
            align.add(alJust);
        }        
        
        formatMenu.add(size);
        formatMenu.add(textStyle);
        formatMenu.add(fontColor);
        formatMenu.add(clearStyles);
        formatMenu.addSeparator();
        formatMenu.add(paragraph);
        formatMenu.add(list);
        formatMenu.addSeparator();
        formatMenu.add(align);
        formatMenu.add(tableMenu);
        formatMenu.addSeparator();
        formatMenu.add(props);        
    }
	
	private JPanel createEntryAttributePanel() throws BackendException
	{	    
		//init title field
		TextEditPopupManager popupMan = new TextEditPopupManager();
		titleField = new JTextField();
		titleField.setText(entry.getTitle());
		titleField.addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				setTitle(titleField.getText());	
			}
		});
		popupMan.addJTextComponent(titleField);
				
		//init author author combo
		Author authors[] = weblog.getAuthors();
		authorCombo = new JComboBox(authors);		
		authorCombo.addItem(""); //$NON-NLS-1$		
		if(mode == UPDATE_ENTRY_MODE)
		{			
			for(int i = 0; i < authors.length; i++)
				if(authors[i].getString().equals(entry.getAuthor().getString()))
					authorCombo.setSelectedIndex(i);
		}
		
		//init category list
		catPane = new CategoryEditorPane();
		catPane.setCategories(weblog.getCategories(), entry);
		
		//mark modified check box
		markModifiedCb = new JCheckBox(Messages.getString("EntryEditor.Mark_as_modified")); //$NON-NLS-1$
		markModifiedCb.setSelected(true);
		markModifiedCb.setEnabled(!entry.isDraft());
		
		//init weblog changer combo
		weblogCombo = new JComboBox(weblogList.getWeblogs());
		weblogCombo.setSelectedItem(weblog);
		//we have to change cats and authors when the weblog selection changes
		weblogCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				weblog = (Weblog)weblogCombo.getSelectedItem();				
				File dir = EntryImageUtils.getImageDirectory(weblog);
				image.setImageDirectory(dir);
				props.setImageDirectory(dir);
				updateAuthors(weblog);
				updateCategories(weblog);
			}
		});		
		//can't select diff weblog if we're updating the entry
		weblogCombo.setEnabled(mode == NEW_ENTRY_MODE);		
	    
	    JPanel attribPanel = new JPanel(new GridLayout(3, 1, 5, 5));
	    JPanel labelPanel = new JPanel(new GridLayout(3, 1, 5, 5));
	    
		//init the blog and auth combo panel			
		JPanel blogAndAuthPanel = new JPanel(new GridLayout(1, 2, 10, 5));
		JPanel authPanel = new JPanel(new BorderLayout(5, 5));
		authPanel.add(new JLabel(Messages.getString("EntryEditor.Author")), BorderLayout.WEST); //$NON-NLS-1$
		authPanel.add(authorCombo, BorderLayout.CENTER);		
		blogAndAuthPanel.add(weblogCombo);
		blogAndAuthPanel.add(authPanel);
		attribPanel.add(blogAndAuthPanel);
		labelPanel.add(new JLabel(Messages.getString("EntryEditor.Site"))); //$NON-NLS-1$
	    
		//init the title and date/modified panels
		dateCombo = new JCalendarComboBox(new Date(), true, (mode == NEW_ENTRY_MODE) || entry.isDraft());		
		if(mode == UPDATE_ENTRY_MODE)
			dateCombo.setDate(entry.getDate());
		attribPanel.add(titleField);
		labelPanel.add(new JLabel(Messages.getString("EntryEditor.Title"))); //$NON-NLS-1$
		JPanel halfPanel = new JPanel(new GridLayout(1, 2, 10, 5));
		halfPanel.add(dateCombo);
		JPanel modifiedPanel = new JPanel(new BorderLayout());
		modifiedPanel.add(markModifiedCb, BorderLayout.WEST);
		modifiedPanel.add(new JPanel(), BorderLayout.CENTER);
		if(mode == UPDATE_ENTRY_MODE)//user can mark modified if in update mode
			halfPanel.add(modifiedPanel);
		else
			halfPanel.add(new JPanel());
		attribPanel.add(halfPanel);
		labelPanel.add(new JLabel(Messages.getString("EntryEditor.Date"))); //$NON-NLS-1$
		
		JPanel topPanel = new JPanel(new BorderLayout(5, 0));
		topPanel.add(labelPanel, BorderLayout.WEST);
		topPanel.add(attribPanel, BorderLayout.CENTER);
		topPanel.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));				

		JScrollPane catScroller = new JScrollPane(catPane);
		catScroller.getViewport().setBackground(catPane.getBackground());
		catScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		catScroller.setPreferredSize(new Dimension(160, topPanel.getHeight()));
		JPanel catPanel = new JPanel(new BorderLayout());
		catPanel.add(new JLabel(Messages.getString("EntryEditor.Categories") + ":"), BorderLayout.NORTH); //$NON-NLS-1$ //$NON-NLS-2$
		catPanel.add(catScroller, BorderLayout.CENTER);
		attribSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		attribSplitPane.setLeftComponent(topPanel);
		attribSplitPane.setRightComponent(catPanel);					
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(attribSplitPane, BorderLayout.CENTER);		
		return mainPanel;
	}
	
	private void updateToolBar()
	{
	    toolBar.removeAll();
	    Utils.addToolbarButton(toolBar, savePublishEntryAction);
		Utils.addToolbarButton(toolBar, saveEntryAction);
		Utils.addToolbarButton(toolBar, saveAsDraft);
		toolBar.addSeparator();
		JButton b;
		
		if(tabs.getSelectedIndex() == WYSIWYG)
		{
			b = Utils.addToolbarButton(toolBar, cut);			
			b.setIcon(Utils.createIcon(RES + "cut24.gif")); //$NON-NLS-1$
			
			b = Utils.addToolbarButton(toolBar, copy);
			b.setIcon(Utils.createIcon(RES + "copy24.gif")); //$NON-NLS-1$
			
			b = Utils.addToolbarButton(toolBar, paste);
			b.setIcon(Utils.createIcon(RES + "paste24.gif")); //$NON-NLS-1$
			
			toolBar.addSeparator();
						
			b = Utils.addToolbarButton(toolBar, undoAction);			
			b.setIcon(Utils.createIcon(RES + "undo24.gif")); //$NON-NLS-1$			
			
			b = Utils.addToolbarButton(toolBar, redoAction);			
			b.setIcon(Utils.createIcon(RES + "redo24.gif")); //$NON-NLS-1$						
		}
		else
		{
			b = Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.CUT_ACTION));			
			b.setIcon(Utils.createIcon(RES + "cut24.gif")); //$NON-NLS-1$
			
			b = Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.COPY_ACTION));
			b.setIcon(Utils.createIcon(RES + "copy24.gif")); //$NON-NLS-1$
			
			b = Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.PASTE_ACTION));
			b.setIcon(Utils.createIcon(RES + "paste24.gif")); //$NON-NLS-1$
			
			toolBar.addSeparator();
						
			b = Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.UNDO_ACTION));
			b.setIcon(Utils.createIcon(RES + "undo24.gif")); //$NON-NLS-1$
						
			b = Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.REDO_ACTION));
			b.setIcon(Utils.createIcon(RES + "redo24.gif")); //$NON-NLS-1$			
		}
		
		toolBar.addSeparator();
		b  = Utils.addToolbarButton(toolBar, spellCheck);
		b.setIcon(Utils.createIcon(RES + "spellcheck24.gif")); //$NON-NLS-1$
		toolBar.repaint();
	}
	
	private JToolBar createToolBar()
	{		
		JToolBar toolBar = new JToolBar();
		toolBar.setFocusable(false);
		toolBar.setFloatable(false);
		toolBar.setBorderPainted(true);
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //$NON-NLS-1$
    	
		return toolBar;
	}
	
    private JToolBar createFormatToolbar()
    {
        GraphicsEnvironment gEnv = 
        	GraphicsEnvironment.getLocalGraphicsEnvironment();
        String envfonts[] = gEnv.getAvailableFontFamilyNames();
        Vector fonts = new Vector();
        fonts.add("Default");
        fonts.add("serif");
        fonts.add("sans-serif");
        fonts.add("monospaced");
        for (int i = 0; i < envfonts.length; i++)
            fonts.add(envfonts[i]);
        
        Font comboFont = new Font("Dialog", Font.PLAIN, 12);
        fontFamilyCombo = new JComboBox(fonts);
		fontFamilyCombo.setPreferredSize(new Dimension(140, 22));
		fontFamilyCombo.setMinimumSize(new Dimension(140, 22));
		fontFamilyCombo.setMaximumSize(new Dimension(140, 22));
		fontFamilyCombo.setFont(comboFont);
		fontFamilyCombo.addActionListener(editHandler);		
		
		paraStyleCombo = new JComboBox(HTMLBlockAction.elementTypes);
		paraStyleCombo.setPreferredSize(new Dimension(120, 22));
		paraStyleCombo.setMinimumSize(new Dimension(120, 22));
		paraStyleCombo.setMaximumSize(new Dimension(120, 22));
		paraStyleCombo.setFont(comboFont);
		paraStyleCombo.addActionListener(editHandler);
        
        JToolBar toolbar = new JToolBar();
        toolbar.setFocusable(false);
        toolbar.setFloatable(false);
        
        fontSizeButton.addActionListener(editHandler);
        toolbar.add(paraStyleCombo);
        toolbar.addSeparator();
        toolbar.add(fontFamilyCombo);
        toolbar.add(fontSizeButton);
        configToolbarButton(fontSizeButton);
        JButton b = toolbar.add(fontColor);
        configToolbarButton(b);
        toolbar.addSeparator();
        
        ActionListener al = new ToggleButtonHandler();
        boldButton = new JButton();
        boldButton.addActionListener(al);
        boldButton.setIcon(Utils.createIcon(RES + "bold.png"));
        configToolbarButton(boldButton);
        toolbar.add(boldButton);        
        italicButton = new JButton();
        italicButton.addActionListener(al);
        italicButton.setIcon(Utils.createIcon(RES + "italic.png"));
        configToolbarButton(italicButton);
        toolbar.add(italicButton);        
        underlineButton = new JButton();
        underlineButton.addActionListener(al);
        underlineButton.setIcon(Utils.createIcon(RES + "underline.png"));
        configToolbarButton(underlineButton);
        toolbar.add(underlineButton);        
        toolbar.addSeparator();
        ulButton = new JButton();
        ulButton.addActionListener(al);
        ulButton.setIcon(Utils.createIcon(RES + "listunordered.png"));
        configToolbarButton(ulButton);
        toolbar.add(ulButton);
        olButton = new JButton();
        olButton.addActionListener(al);
        olButton.setIcon(Utils.createIcon(RES + "listordered.png"));
        configToolbarButton(olButton);
        toolbar.add(olButton);
        toolbar.addSeparator();
      
        alLeftButton = new JButton();
        alLeftButton.setIcon(Utils.createIcon(RES + "alignleft.png"));
        alLeftButton.addActionListener(al);
        configToolbarButton(alLeftButton);
        toolbar.add(alLeftButton);
        alCenterButton = new JButton();
        alCenterButton.setIcon(Utils.createIcon(RES + "aligncenter.png"));
        alCenterButton.addActionListener(al);
        configToolbarButton(alCenterButton);
        toolbar.add(alCenterButton);
        alRightButton = new JButton();
        alRightButton.setIcon(Utils.createIcon(RES + "alignright.png"));
        alRightButton.addActionListener(al);
        configToolbarButton(alRightButton);
        toolbar.add(alRightButton);
        alJustButton = new JButton();
        alJustButton.setIcon(Utils.createIcon(RES + "alignjust.png"));
        alJustButton.addActionListener(al);
        configToolbarButton(alJustButton);
        toolbar.add(alJustButton);
        toolbar.addSeparator();
        
        b = toolbar.add(link);
        configToolbarButton(b);
        b = toolbar.add(image);
        configToolbarButton(b);
        b = toolbar.add(table);
        configToolbarButton(b);        
        toolbar.addSeparator();
        
        b = toolbar.add(lineBreak);
        configToolbarButton(b);
        specialCharButton = new JButton();
        specialCharButton.addActionListener(al);
        specialCharButton.setIcon(Utils.createIcon(RES + "char.png"));
        configToolbarButton(specialCharButton);
        toolbar.add(specialCharButton);
        
        return toolbar;
    }
    
    private void configToolbarButton(JButton button)
    {
		button.setText(null);
		button.setMnemonic(0);
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setMaximumSize(new Dimension(22, 22));
		button.setMinimumSize(new Dimension(22, 22));
		button.setPreferredSize(new Dimension(22, 22));
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.setBorder(plainBorder);
		Action a = button.getAction();
		if(a != null)
		    button.setToolTipText(a.getValue(Action.NAME).toString());
    }   
	
    private void updateEnabledStates()
    {        
        updateUndo();
        if(tabs.getSelectedIndex() != WYSIWYG)
        {
            insertTableRow.setEnabled(false);
            deleteTableRow.setEnabled(false);
            insertTableCell.setEnabled(false);
            deleteTableCell.setEnabled(false);
            insertTableCol.setEnabled(false);
            deleteTableCol.setEnabled(false);            
            props.setEnabled(false);
            boldButton.setBorder(plainBorder);
            italicButton.setBorder(plainBorder);
            underlineButton.setBorder(plainBorder);
            olButton.setBorder(plainBorder);
            ulButton.setBorder(plainBorder);
        }
        else
        {
            updatePositionDependantActions();
        }
       
        boolean isWys = (tabs.getSelectedIndex() == WYSIWYG);        
        paraStyleCombo.setEnabled(isWys);
        fontFamilyCombo.setEnabled(isWys);        
        clearStyles.setEnabled(isWys);
        wordWrap.setEnabled(!isWys);
    }
    
    private void updatePositionDependantActions()
    {
        insertTableRow.update();
        deleteTableRow.update();
        insertTableCell.update();
        deleteTableCell.update();
        insertTableCol.update();
        deleteTableCol.update();
        props.update();
    }
    
	private void closeEditor()
	{
	    if(shouldAskToSave)
	    {
	        String msg = Messages.getString("EntryEditor.Save_Changes_Draft_Msg");
	        if(mode == UPDATE_ENTRY_MODE)
	            msg = Messages.getString("EntryEditor.Save_Changes_Msg");
	        
	        int r = JOptionPane.showConfirmDialog(this, 
	            msg, Messages.getString("EntryEditor.Save"), 
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
		TBGlobals.putProperty("EDITOR_DIV", attribSplitPane.getDividerLocation() + "");
		super.dispose();
	}
    
	private void saveEntryAndExit(boolean isSaveAsDraft)
	{
		if(isSaveAsDraft)
		{
			entry.setDraft(true);
			isPublish = false;
			entry.setLastModified(null);
			//entry.setDate(dateCombo.getDate());
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
		
		Object a = authorCombo.getSelectedItem();
		if(a instanceof Author)    		
			entry.setAuthor((Author)a);
    		
		entry.setDate(dateCombo.getDate());
		entry.setTitle(titleField.getText());		
		entry.setCategories(catPane.getSelectedCategories());
		
		if(tabs.getSelectedIndex() == SOURCE)
		    entry.setText(srcEditor.getText());
		else
        {
            String txt = removeInvalidTags(editor.getText());
            txt = Entities.HTML_BASIC.unescapeUnknownEntities(txt);
		    entry.setText(txt);            
        }
        
        //System.err.println(entry.getText());
    	
		if(!entry.isDraft())
		    entry.setText(EntryImageUtils.changeLocalImageURLs(entry.getText(), weblog));
		    
		
		cancelled = false;
		dispose();
	}

	private String deIndent(String html)
	{
	    String ws = "\n    ";
	    StringBuffer sb = new StringBuffer(html);
	    
	    while(sb.indexOf(ws) != -1)
		{			  
			int s = sb.indexOf(ws);            
			int e = s + ws.length();
			sb.delete(s, e);
			sb.insert(s, "\n");			 
		}
	    
	    return sb.toString();
	}
	
	private String removeInvalidTags(String html)
	{
	    for(int i = 0; i < INVALID_TAGS.length; i++)
	    {
	        html = deleteOccurance(html, '<' + INVALID_TAGS[i] + '>');
	        html = deleteOccurance(html, "</" + INVALID_TAGS[i] + '>');
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
		String entry = Messages.getString("EntryEditor.New_Entry"); //$NON-NLS-1$
		if(mode == UPDATE_ENTRY_MODE)
			entry = Messages.getString("EntryEditor.Update_Entry"); //$NON-NLS-1$
		
		if(title.equals("")) //$NON-NLS-1$
			title = Messages.getString("EntryEditor.Untitled"); //$NON-NLS-1$
			
		entry += " [" + title +"]"; //$NON-NLS-1$ //$NON-NLS-2$
		super.setTitle(entry);
	}
	
	public void setVisible(boolean b)
	{
		if(b)
		{
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) ((d.width - getSize().width) / 2);
			int y = (int) ((d.height - getSize().height) / 2);
			setLocation(x, y);
			
			try
			{
			    int loc = Integer.parseInt(TBGlobals.getProperty("EDITOR_DIV"));
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
    
    private void updateAlignmentActions()
    {
        Element pEl = document.getParagraphElement(editor.getCaretPosition());
        AttributeSet at = pEl.getAttributes();        
        boolean left = at.containsAttribute(HTML.Attribute.ALIGN, "left");
        boolean center = at.containsAttribute(HTML.Attribute.ALIGN, "center");
        boolean right = at.containsAttribute(HTML.Attribute.ALIGN, "right");
        boolean justify = at.containsAttribute(HTML.Attribute.ALIGN, "justify");
                            
        alLeftMI.setSelected(left);
        alCenterMI.setSelected(center);
        alRightMI.setSelected(right); 
        alJustMI.setSelected(justify);
        
        if(left)
            alLeftButton.setBorder(pressedBorder);
        else
            alLeftButton.setBorder(plainBorder);
        
        if(center)
            alCenterButton.setBorder(pressedBorder);
        else
            alCenterButton.setBorder(plainBorder);
        
        if(right)
            alRightButton.setBorder(pressedBorder);
        else
            alRightButton.setBorder(plainBorder); 
        
        if(justify)
            alJustButton.setBorder(pressedBorder);
        else
            alJustButton.setBorder(plainBorder);
    }
    
    private void updateStyleActions(AttributeSet at)
    {        
        boldMI.setSelected(at.containsAttribute(StyleConstants.Bold, new Boolean(true))); 
		italicMI.setSelected(at.containsAttribute(StyleConstants.Italic, new Boolean(true)));
		underMI.setSelected(at.containsAttribute(StyleConstants.Underline, new Boolean(true)));
		strikeMI.setSelected(at.isDefined(HTML.Tag.STRIKE));
		citeMI.setSelected(at.isDefined(HTML.Tag.CITE));
		codeMI.setSelected(at.isDefined(HTML.Tag.CODE));
		emMI.setSelected(at.isDefined(HTML.Tag.EM));
		supMI.setSelected(at.isDefined(HTML.Tag.SUP));
		subMI.setSelected(at.isDefined(HTML.Tag.SUB));
		strongMI.setSelected(at.isDefined(HTML.Tag.STRONG));
		
		if(boldMI.isSelected())
		    boldButton.setBorder(pressedBorder);
		else
		    boldButton.setBorder(plainBorder);		
		if(italicMI.isSelected())
		    italicButton.setBorder(pressedBorder);
		else
		    italicButton.setBorder(plainBorder);		
		if(underMI.isSelected())
		    underlineButton.setBorder(pressedBorder);
		else
		    underlineButton.setBorder(plainBorder);		
    }
    
    private void updateSizeActions(AttributeSet at)
    {		        
		if(at.containsAttribute(StyleConstants.FontSize, new Integer(8)))
		    xxSmallMI.setSelected(true);
		else if(at.containsAttribute(StyleConstants.FontSize, new Integer(10)))
			xSmallMI.setSelected(true);
		else if(at.containsAttribute(StyleConstants.FontSize, new Integer(12)))
			smallMI.setSelected(true);
		else if(at.containsAttribute(StyleConstants.FontSize, new Integer(14)))
			mediumMI.setSelected(true);
		else if(at.containsAttribute(StyleConstants.FontSize, new Integer(18)))
			largeMI.setSelected(true);
		else if(at.containsAttribute(StyleConstants.FontSize, new Integer(24)))
			xLargeMI.setSelected(true);
		else if(at.containsAttribute(StyleConstants.FontSize, new Integer(28)))
			xxLargeMI.setSelected(true);
		else
			mediumMI.setSelected(true);
    }
    
    private void updateParagraphActions()
    {
		HTMLBlockAction.LOCK = true;
        Element pEl = document.getParagraphElement(editor.getCaretPosition());
        Element listParent = pEl.getParentElement().getParentElement();
        HTML.Tag listTag = HTML.getTag(listParent.getName());
		String pName = pEl.getName().toUpperCase();		
		if(pName.equals("P-IMPLIED"))
			pName = pEl.getParentElement().getName().toUpperCase();
		
		Border ulBorder = plainBorder;
		Border olBorder = plainBorder;
		if(listTag.equals(HTML.Tag.UL))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_UL);
			ulMI.setSelected(true);
			ulBorder = pressedBorder;
		}
		else if(listTag.equals(HTML.Tag.OL))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_OL);
			olMI.setSelected(true);
			olBorder = pressedBorder;
		}
		else if(pName.equals("P"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_P);
			paraMI.setSelected(true);
		}
		else if(pName.equals("H1"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_H1);
			h1MI.setSelected(true);
		}
		else if(pName.equals("H2"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_H2);
			h2MI.setSelected(true);
		}
		else if(pName.equals("H3"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_H3);
			h3MI.setSelected(true);
		}
		else if(pName.equals("H4"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_H4);
			h4MI.setSelected(true);
		}
		else if(pName.equals("H5"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_H5);
			h5MI.setSelected(true);
		}
		else if(pName.equals("H6"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_H6);
			h6MI.setSelected(true);
		}
		else if(pName.equals("PRE"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_PRE);
			preMI.setSelected(true);
		}
		else if(pName.equals("BLOCKQUOTE"))
		{
			paraStyleCombo.setSelectedIndex(HTMLBlockAction.T_BLOCKQ);
			blockqMI.setSelected(true);
		}
		
		ulButton.setBorder(ulBorder);
		olButton.setBorder(olBorder);		
		HTMLBlockAction.LOCK = false;
    }  
    
    private void wysiwygUpdated()
    {
        updatePositionDependantActions();
        
        AttributeSet chAt = null;
        int caret;
 		if(editor.getCaretPosition() > 0)		    
		    caret = editor.getCaretPosition() - 1;		
        else
            caret = editor.getCaretPosition();        
 		
        chAt = document.getCharacterElement(caret).getAttributes();
        
		fontFamilyCombo.removeActionListener(editHandler);
		Object val =  chAt.getAttribute(StyleConstants.FontFamily);        
        if(chAt.isDefined(StyleConstants.FontFamily))
			fontFamilyCombo.setSelectedItem(val.toString());
        else
            fontFamilyCombo.setSelectedIndex(0);
        fontFamilyCombo.addActionListener(editHandler);
        
        updateAlignmentActions();
        updateStyleActions(chAt);
        updateSizeActions(chAt);
        updateParagraphActions();                
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
	
	private void configureCurrentEditor(String text)
	{
	    boolean isSrcEditor = tabs.getSelectedIndex() == SOURCE;
        
	    //lock the block actions... Without this on java 6.0, the format combo fires
        //an event when the tabs change, thereby adding a new paragraph. eek.
	    HTMLBlockAction.LOCK = true;
	    if(!isSrcEditor)
	    {	        
	        editor.setText("");	        
			insertHTML(removeInvalidTags(text), 0);			
			wysUndoer.discardAllEdits();
			editor.requestFocusInWindow();
			editor.setCaretPosition(0);
			TBGlobals.putProperty("EDITOR_TYPE", WYSIWYG + "");
			findAction.setEditor(editor);
			replaceAction.setEditor(editor);
	    }
	    else 
	    {	        
	        if(isWysTextChanged || srcEditor.getText().equals(""))
            {	            
                String t = deIndent(removeInvalidTags(text));
                //JEditorPane escapes non-latin characters, so we need
                //to unescape Unknown HTML40 entities.
                t = Entities.HTML_BASIC.unescapeUnknownEntities(t);                
                srcEditor.setText(t);
            }
	        srcEditor.discardAllEdits();
	        srcEditor.requestFocusInWindow();
			srcEditor.setCaretPosition(0);			
			TBGlobals.putProperty("EDITOR_TYPE", SOURCE + "");
			findAction.setEditor(srcEditor);
			replaceAction.setEditor(srcEditor);
	    }	    
		
	    isWysTextChanged = false;
	    wordWrap.setEnabled(isSrcEditor);
	    updateEditMenu();
	    updateToolBar();
	    updateFormatMenu();		
		updateEnabledStates();
        HTMLBlockAction.LOCK = false;//unlock the block actions we locked above.
        
	}
	
	private void updateUndo()
	{
	    undoAction.setEnabled(wysUndoer.canUndo());
	    redoAction.setEnabled(wysUndoer.canRedo());
	}
	
	public void insertHTML(String html, int location) 
	{		
		try 
		{
			HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
			Document doc = editor.getDocument();
			StringReader reader = new StringReader(html);
			kit.read(reader, doc, location);
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
    //listens for changes on the weblogs and adjusts the editor accordingly
	private class WeblogChangeListener 
	implements WeblogListener, CategoryListener, AuthorListener
	{
		private Weblog getCurrentWeblog()
		{
			Weblog w = (Weblog)weblogCombo.getSelectedItem();
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
    
	public class SaveAsDraftAction extends AbstractAction 
	{
		public SaveAsDraftAction()
		{
			super(Messages.getString("EntryEditor.Save_As_Draft"), 
			    Utils.createIcon(RES + "save24.gif")); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, getValue(NAME));
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
			Messages.setMnemonic("EntryEditor.Save_As_Draft", this);//$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e)
		{
			saveEntryAndExit(true);
		}
	}
    
	private class EntryAction extends AbstractAction
	{
		private boolean publishAction = false;
    	
		public EntryAction(String name, Icon i, boolean pubAction)
		{
			super(name, i);
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
						Messages.getString("EntryEditor.save_as_draft_prompt"), //$NON-NLS-1$
						Messages.getString("EntryEditor.Confirm"), JOptionPane.YES_NO_OPTION,  //$NON-NLS-1$
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
    
    //Listener for certain wysiwyg editing functions
    private class EditActionHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {       
            if(e.getSource() == fontSizeButton)
            {
                JPopupMenu p = new JPopupMenu();
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.XXSMALL));
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.XSMALL));
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.SMALL));
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.MEDIUM));
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.LARGE));
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.XLARGE));
                p.add(new HTMLFontSizeAction(HTMLFontSizeAction.XXLARGE));
                
                p.show(fontSizeButton, 0, 
                    fontSizeButton.getHeight());
                return;
            }
            else if(e.getSource() == fontFamilyCombo && editor.isShowing())
            {            
                HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();
                MutableAttributeSet tagAttrs = new SimpleAttributeSet();
                String val = fontFamilyCombo.getSelectedItem().toString();
                
                boolean shouldClearSel = false;
                if(editor.getSelectedText() == null && document.getLength() > 0)
                {
                    editor.replaceSelection("  ");
                    editor.setSelectionStart(editor.getCaretPosition() - 1);
                    editor.setSelectionEnd(editor.getSelectionStart() + 1);                
                    shouldClearSel = true;
                }            

                tagAttrs.addAttribute(StyleConstants.FontFamily, val);
                if(editor.getSelectionEnd() > editor.getSelectionStart())
                    document.setCharacterAttributes(editor.getSelectionStart(), 
                        editor.getSelectionEnd() - editor.getSelectionStart(), tagAttrs, false);
                
                if(shouldClearSel)
                {
                    editor.setSelectionStart(editor.getCaretPosition());
                    editor.setSelectionEnd(editor.getCaretPosition());
                    editor.setCaretPosition(editor.getCaretPosition() - 1);
                    editor.setCaretPosition(editor.getCaretPosition() + 1);
                }
                editor.requestFocusInWindow();
            }            
            else if(e.getSource() == paraStyleCombo && editor.isShowing())
            {
                Action a = (Action)paraActions.get(paraStyleCombo.getSelectedItem());
                a.actionPerformed(e);
            }
        }
    }

	//Popupmenu implementation
	private class PopupListener extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{ checkForPopupTrigger(e); }
		
		public void mouseReleased(MouseEvent e)
		{ checkForPopupTrigger(e); }
		
		private void checkForPopupTrigger(MouseEvent e)
		{
			if(!e.isPopupTrigger())
			    return;
			JPopupMenu popupMenu = new JPopupMenu();
			popupMenu.add(undoAction);
			popupMenu.add(redoAction);
			popupMenu.addSeparator();
			popupMenu.add(cut);
			popupMenu.add(copy);
			popupMenu.add(paste);
			popupMenu.addSeparator();
			popupMenu.add(selectAll);
			if(tabs.getSelectedIndex() == WYSIWYG)
			{			    
			    if(props.isEnabled())
			    {
			        popupMenu.addSeparator();
			        popupMenu.add(props);
			    }
			    
			    if(insertTableCell.isEnabled())
			    {			        
			        JMenu tInsert = new JMenu(Messages.getString("EntryEditor.Insert"));
			        tInsert.add(insertTableCell);
			        tInsert.add(insertTableRow);
			        tInsert.add(insertTableCol);
			        popupMenu.add(tInsert);
			        JMenu tDelete = new JMenu(Messages.getString("EntryEditor.Delete"));
			        tDelete.add(deleteTableCell);
			        tDelete.add(deleteTableRow);
			        tDelete.add(deleteTableCol);
			        popupMenu.add(tDelete);			        
			    }
			}
			popupMenu.show(e.getComponent(), e.getX(), e.getY());			
		}
	}
    
	//Manages the pressed state of toggle buttons
	private class ToggleButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == boldButton)
            {
                bold.actionPerformed(e); 
                updateBorder((JButton)e.getSource());
            }
            else if(e.getSource() == italicButton)
            {
                italic.actionPerformed(e);
                updateBorder((JButton)e.getSource());
            }
            else if(e.getSource() == underlineButton)
            {
                under.actionPerformed(e);
                updateBorder((JButton)e.getSource());
            }
            else if(e.getSource() == olButton)
            {
                ol.actionPerformed(e);
                updateParagraphActions();
            }
            else if(e.getSource() == ulButton)
            {
                ul.actionPerformed(e);
                updateParagraphActions();
            }
            else if(e.getSource() == alLeftButton)
            {
                alLeft.actionPerformed(e);
                updateAlignmentActions();                
            }
            else if(e.getSource() == alCenterButton)
            {
                alCenter.actionPerformed(e);
                updateAlignmentActions();                
            }
            else if(e.getSource() == alRightButton)
            {
               alRight.actionPerformed(e);
               updateAlignmentActions();               
            }
            else if(e.getSource() == alJustButton)
            {
                alJust.actionPerformed(e);
                updateAlignmentActions(); 
            }
            else if(e.getSource() == specialCharButton)
            {
                insertChar.actionPerformed(e);                
            }
            else
                return;
            
            editor.requestFocusInWindow();            
        }
        
        private void updateBorder(JButton b)
        {
            if(tabs.getSelectedIndex() != 0)            
                return;            
            
            if(b.getBorder() == plainBorder)
                b.setBorder(pressedBorder);
            else if(b.getBorder() == pressedBorder)
                b.setBorder(plainBorder);
        }
    }
        
    //shows the special char panel
    private class InsertCharAction extends AbstractAction
    {
        public InsertCharAction()
        {
            super(Messages.getString("EntryEditor.Special_Character"),
                Utils.createIcon(RES + "char.png"));
            Messages.setMnemonic("EntryEditor.Special_Character", this);
        }
        
        public void actionPerformed(ActionEvent e)
        {
                       
            if(charTablePanel.isShowing())
            {
                editorPanel.remove(charTablePanel);
                specialCharButton.setBorder(plainBorder);
            }
            else
            {
                editorPanel.add(charTablePanel, BorderLayout.NORTH);
                specialCharButton.setBorder(pressedBorder);
            }
            
            editorPanel.validate();            
        }
    }
    
    private class TextChangedListener implements DocumentListener
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
            if(tabs.getSelectedIndex() == WYSIWYG)
                isWysTextChanged = true;
            
            shouldAskToSave = true;
        }
    }
    
    private class UndoAction extends AbstractAction 
	{
		public UndoAction() 
		{
			super(Messages.getString("TextEditActionSet.Undo"), 
			    Utils.createIcon(RES + "undo.png"));
			Messages.setMnemonic("TextEditActionSet.Undo", this);
			setEnabled(false);
			putValue(
				Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) 
		{
			try 
			{				
			    wysUndoer.undo();
			} 
			catch (CannotUndoException ex) 
			{
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			
			updateUndo();
		}
	}

	private class RedoAction extends AbstractAction 
	{
		public RedoAction() 
		{
			super(Messages.getString("TextEditActionSet.Redo"), 
			    Utils.createIcon(RES + "redo.png"));
			Messages.setMnemonic("TextEditActionSet.Redo", this);
			setEnabled(false);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
		}

		public void actionPerformed(ActionEvent e) 
		{
			try 
			{				
			    wysUndoer.redo();				
			} 
			catch (CannotUndoException ex) 
			{
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateUndo();
		}


	}
	
	// ------- word wrap --------
	public class WordWrapAction extends AbstractAction 
	{
		public WordWrapAction()
		{
			super(Messages.getString("EntryEditor.Word_Wrap"), null); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
			Messages.setMnemonic("EntryEditor.Word_Wrap", this); //$NON-NLS-1$
			putValue(SHORT_DESCRIPTION, getValue(NAME));
		}

		public void actionPerformed(ActionEvent e)
		{
			srcEditor.setLineWrap(!srcEditor.getLineWrap());
			wordWrapMenuItem.setState(srcEditor.getLineWrap());
			TBGlobals.putProperty("EDITOR_WORDWRAP", srcEditor.getLineWrap() + "");			
		}
	}
	
	//Spell checker action
	private class SpellCheckAction extends AbstractAction
	{    
		public SpellCheckAction()
		{
			super(Messages.getString("EntryEditor.Check_Spelling"), 
			    Utils.createIcon(RES + "spellcheck16.png")); //$NON-NLS-1$
			putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
			Messages.setMnemonic("EntryEditor.Check_Spelling", this); //$NON-NLS-1$
			putValue(Action.SHORT_DESCRIPTION, getValue(NAME));
		}
		
		public void actionPerformed(ActionEvent e)
		{
            File d = new File(TBGlobals.DICT_DIR, TBGlobals.getDictionary() + ".dic");
            File a = new File(TBGlobals.DICT_DIR, TBGlobals.getDictionary() + ".aff");
            File userDic = new File(TBGlobals.PROP_DIR, "dict.user");
            
            try
            {
                SpellDictionary dict = new OpenOfficeSpellDictionary(d, a, userDic);
                SpellChecker checker = new SpellChecker(dict);
                JTextComponentSpellChecker textSpellChecker = 
                    new JTextComponentSpellChecker(checker); 
                
                JTextComponent textArea = null;
				if(tabs.getSelectedIndex() == WYSIWYG)
				    textArea = editor;
				else
				    textArea = srcEditor;
                
        	    if(textSpellChecker.spellCheck(textArea))
        	    {
        	        JOptionPane.showMessageDialog(EntryEditor.this,
        	            Messages.getString("EntryEditor.Spellcheck_Complete"), //$NON-NLS-1$
        	            Messages.getString("EntryEditor.Spellcheck_Complete"),  //$NON-NLS-1$
        	            JOptionPane.INFORMATION_MESSAGE);
        	    }
                textArea.requestFocusInWindow();                
            }
        	catch(Exception ex)
            {
				Utils.errMsg(EntryEditor.this, 
					Messages.getString("EntryEditor.Spellcheck_Error"), ex); //$NON-NLS-1$  
            }
		}
	}
}