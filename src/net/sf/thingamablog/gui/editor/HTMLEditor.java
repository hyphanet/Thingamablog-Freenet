/*
 * Created on Nov 7, 2007
 */
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.CompoundUndoManager;
import net.atlanticbb.tantlinger.ui.text.IndentationFilter;
import net.atlanticbb.tantlinger.ui.text.SourceCodeEditor;
import net.atlanticbb.tantlinger.ui.text.actions.FindReplaceAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLEditorActionFactory;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLFontAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLFontColorAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLInlineAction;
import net.atlanticbb.tantlinger.ui.text.actions.HTMLTextEditAction;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.Template;
import net.sf.thingamablog.gui.TBAbout;
import net.sf.thingamablog.gui.TBHelpAction;
import novaworx.syntax.SyntaxFactory;
import novaworx.textpane.SyntaxDocument;
import novaworx.textpane.SyntaxGutter;
import novaworx.textpane.SyntaxGutterBase;

import org.bushe.swing.action.ActionList;
import org.bushe.swing.action.ActionUIFactory;


/**
 * @author Bob Tantlinger
 *
 */
public class HTMLEditor extends JFrame
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.editor");
       
    public static final int TEMPLATE_MODE = 1;
    public static final int FILE_MODE = 2;
    private int mode = FILE_MODE;
    
    private Template curTemplate;
    private File curFile = new File(System.getProperty("user.home"), "editor.txt"); //$NON-NLS-1$ //$NON-NLS-2$
    
    private SourceCodeEditor editor;
    private File lastDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
    
    
    private Action openAction, saveAction;  
    private boolean isTextChanged;
    
    private String syntax = "html";
    
    private ActionList actionList;
    
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPopupMenu popupMenu;
    
    public HTMLEditor(Template tmp)
    {
        super(i18n.str("template_editor") + " [" + tmp.getName() + "]");
            
        curTemplate = tmp;
        mode = TEMPLATE_MODE;
        syntax = "html";
        init();
    }
    
    public HTMLEditor(File f)
    {       
        if(f != null)
            setTitle(f.getName());        
                
        curFile = f;
        mode = FILE_MODE;
        syntax = "html";
        
        int dot = curFile.getName().lastIndexOf('.');
        if(dot != -1 && dot != curFile.getName().length() - 1);
        {
            syntax = curFile.getName().substring(dot + 1, curFile.getName().length());            
        }     
        
        init();     
    }
    
    public HTMLEditor(String title)
    {
        super(title);
        mode = FILE_MODE;
        syntax = "html";        
        init(); 
    }
       
    
    private void init()
    {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                closeEditor();
            }
        });
        setIconImage(UIUtils.getIcon(UIUtils.X16, "tamb.png").getImage());       
        
        editor = new SourceCodeEditor();
        editor.setFont(TBGlobals.getEditorFont());
        editor.addCaretListener(new CaretListener()
        {
            public void caretUpdate(CaretEvent e)
            {                
                actionList.putContextValueForAll(HTMLTextEditAction.EDITOR, editor);
                //actionList.updateEnabledForAll();
            }
        });
        editor.addMouseListener(new PopupHandler());        
        
        JScrollPane scrollPane = new JScrollPane(editor);        
        SyntaxGutter gutter = new SyntaxGutter(editor);
        SyntaxGutterBase gutterBase = new SyntaxGutterBase(gutter);
        scrollPane.setRowHeaderView(gutter);
        scrollPane.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, gutterBase);
        
        initActions();
        
        setJMenuBar(menuBar);
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        load();
        int w = 640, h = 480;        
        try
        {
            w = Integer.parseInt(TBGlobals.getProperty("SRC_EDITOR_WIDTH"));
            h = Integer.parseInt(TBGlobals.getProperty("SRC_EDITOR_HEIGHT"));
        }
        catch(Exception ex){}
        setSize(w, h);
        actionList.putContextValueForAll(HTMLTextEditAction.EDITOR, editor);
        actionList.updateEnabledForAll();
    }
    
    public void setVisible(boolean b)
    {
        if(b)
        {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int x = ((d.width - getSize().width) / 2);
            int y = ((d.height - getSize().height) / 2);
            setLocation(x, y);      
            super.setVisible(true);
        }
        else
            super.setVisible(false);        
    }
    
    private void closeEditor()
    {
        if(isTextChanged)
        {
            int r = JOptionPane.showConfirmDialog(this,
                i18n.str("save_changes_prompt"), i18n.str("text_changed"), //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            
            if(r == JOptionPane.CANCEL_OPTION)
                return;
            
            if(r == JOptionPane.YES_OPTION)
            {               
                save();
            }
        } 
        
        TBGlobals.putProperty("SRC_EDITOR_WIDTH", getWidth()+"");
        TBGlobals.putProperty("SRC_EDITOR_HEIGHT", getHeight()+"");
        
        dispose();      
    }
    
    private void initActions()
    {
        actionList = new ActionList("HTMLEditorActions");
        menuBar = new JMenuBar();
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        openAction = new OpenAction();
        saveAction = new SaveAction();
        saveAction.setEnabled(false);
        JMenuItem close = new JMenuItem(i18n.str("close")); //$NON-NLS-1$
        close.setMnemonic(i18n.mnem("close"));
        close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeEditor();              
            }
        });
        JMenu fileMenu = new JMenu(i18n.str("file")); //$NON-NLS-1$
        fileMenu.setMnemonic(i18n.mnem("file"));
        fileMenu.add(openAction);
        UIUtils.addToolBarButton(toolBar, openAction);//toolBar.add(openAction);
        fileMenu.add(saveAction);
        UIUtils.addToolBarButton(toolBar, saveAction);//toolBar.add(saveAction);
        toolBar.addSeparator();
        fileMenu.addSeparator();
        fileMenu.add(close);
        actionList.add(openAction);
        actionList.add(saveAction);
        menuBar.add(fileMenu);
        
        ActionList lst = HTMLEditorActionFactory.createEditActionList();
        popupMenu = ActionUIFactory.getInstance().createPopupMenu(lst);
        ActionList tbEditActions = new ActionList("tbEditActions");
        tbEditActions.addAll(lst);
        tbEditActions.remove(tbEditActions.size() - 1); //remove selectAll action
        tbEditActions.remove(tbEditActions.size() - 1); //remove separator
        tbEditActions.remove(tbEditActions.size() - 1); //remove paste formatted
        tbEditActions.add(null); //add separator
        addToToolBar(tbEditActions);
        lst.add(null);        
        Action act = new HTMLInlineAction(HTMLInlineAction.BOLD);
        actionList.add(act);
        UIUtils.addToolBarButton(toolBar, new JButton(act));//toolBar.add(act);
        act = new HTMLInlineAction(HTMLInlineAction.ITALIC);
        actionList.add(act);
        UIUtils.addToolBarButton(toolBar, new JButton(act));//toolBar.add(act);
        act = new HTMLInlineAction(HTMLInlineAction.UNDERLINE);
        actionList.add(act);
        UIUtils.addToolBarButton(toolBar, new JButton(act));//toolBar.add(act);
        toolBar.addSeparator();        
        lst.add(new FindReplaceAction(false));
        actionList.addAll(lst);
        JMenu editMenu = ActionUIFactory.getInstance().createMenu(lst);
        editMenu.setText(i18n.str("edit"));
        editMenu.setMnemonic(i18n.mnem("edit"));
        menuBar.add(editMenu);
        
        //  create format menu
        JMenu formatMenu = new JMenu(i18n.str("format"));        
        formatMenu.setMnemonic(i18n.mnem("format")); 
        lst = HTMLEditorActionFactory.createFontSizeActionList();//HTMLEditorActionFactory.createInlineActionList();
        actionList.addAll(lst);        
        formatMenu.add(createMenu(lst, i18n.str("size")));        
        
        lst = HTMLEditorActionFactory.createInlineActionList();
        actionList.addAll(lst);
        formatMenu.add(createMenu(lst, i18n.str("style")));
        
        act = new HTMLFontColorAction();
        actionList.add(act);
        formatMenu.add(act);
        
        act = new HTMLFontAction();
        actionList.add(act);
        formatMenu.add(act);
                
        lst = HTMLEditorActionFactory.createBlockElementActionList();
        actionList.addAll(lst);
        formatMenu.add(createMenu(lst, i18n.str("paragraph")));
                
        lst = HTMLEditorActionFactory.createListElementActionList();
        actionList.addAll(lst);
        addToToolBar(lst);
        toolBar.addSeparator();
        formatMenu.add(createMenu(lst, i18n.str("list")));
        formatMenu.addSeparator();
                
        lst = HTMLEditorActionFactory.createAlignActionList();
        actionList.addAll(lst);  
        addToToolBar(lst);
        toolBar.addSeparator();
        formatMenu.add(createMenu(lst, i18n.str("align")));
        menuBar.add(formatMenu);
        
        lst = HTMLEditorActionFactory.createInsertActionList();
        actionList.addAll(lst);
        JMenu insertMenu = ActionUIFactory.getInstance().createMenu(lst);
        insertMenu.setText(i18n.str("insert")); //$NON-NLS-1$
        menuBar.add(insertMenu);        
        insertMenu.setMnemonic(i18n.mnem("insert"));
        addToToolBar(lst); 
        
        JMenu helpMenu = new JMenu(i18n.str("help"));
        helpMenu.setMnemonic(i18n.mnem("help"));
        Action help = new TBHelpAction(
                i18n.str("help_contents_"), "ch03.item5");
        help.putValue(Action.SMALL_ICON, UIUtils.getIcon(UIUtils.X16, "help.png"));
        help.putValue(Action.MNEMONIC_KEY, new Integer(i18n.mnem("help_contents_")));
        help.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        helpMenu.add(help);
        JMenuItem aboutItem = new JMenuItem(i18n.str("about_"));
        aboutItem.setMnemonic(i18n.mnem("about_"));
        aboutItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                TBAbout.showAboutBox(HTMLEditor.this);
            }
        });
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);
    }
    
    private JMenu createMenu(ActionList lst, String menuName)
    {
        JMenu m = ActionUIFactory.getInstance().createMenu(lst);
        m.setText(menuName);
        return m;
    }
    
    private void addToToolBar(List al)
    {
        for(Iterator it = al.iterator(); it.hasNext();)
        {
            Action a = (Action)it.next();
            if(a == null)
                toolBar.addSeparator();
            else
            {
                JButton button = new JButton(a);
                button.setToolTipText((String)button.getAction().getValue(Action.NAME));
                Icon ico = button.getIcon();        
                if(ico != null)
                {
                    button.setText(null);
                    button.setMnemonic(0);        
                    button.putClientProperty("hideActionText", Boolean.TRUE);
                    int square = Math.max(ico.getIconWidth(), ico.getIconHeight()) + 6;
                    Dimension size = new Dimension(square, square);
                    button.setPreferredSize(size);                    
                }
                toolBar.add(button);
            }
        }
    }
     
    
    private void load()
    {       
        CompoundUndoManager.discardAllEdits(editor.getDocument());
        
        SyntaxDocument doc = new SyntaxDocument();
        doc.setSyntax(SyntaxFactory.getSyntax(syntax));        
        CompoundUndoManager cuh = new CompoundUndoManager(doc, new UndoManager());        
        doc.addUndoableEditListener(cuh);
        doc.setDocumentFilter(new IndentationFilter());        
        editor.setDocument(doc);
        
        try
        {       
            if(mode == TEMPLATE_MODE)
            {           
                editor.setText(curTemplate.load());
                isTextChanged = true;
            }
            else
            {               
                editor.setText(readFile(curFile));              
            }           
        }
        catch(Exception ioe)
        {            
            UIUtils.showError(this, ioe);
        }       
        
        CompoundUndoManager.discardAllEdits(doc);
        editor.setCaretPosition(0);              
        isTextChanged = false;
        saveAction.setEnabled(isTextChanged);
        doc.addDocumentListener(new TextChangeListener());
    }
    
    private String readFile(File f) throws IOException
    {
        StringBuffer sb = new StringBuffer((int)f.length());
        BufferedReader reader = new BufferedReader(new FileReader(f));      
        String line = null;
        while((line = reader.readLine()) != null)
        {       
            sb.append(line);
            sb.append('\n');
        }
        
        reader.close();
        return sb.toString();
    }
    
    private void openFile()
    {
        JFileChooser fc = new JFileChooser(lastDir);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int r = fc.showOpenDialog(HTMLEditor.this);
        if(r == JFileChooser.CANCEL_OPTION)
            return;
        File f = fc.getSelectedFile();
        if(f == null)
            return;         
            
        try
        {               
            String text = readFile(f);            
            editor.setText(text);
            editor.setCaretPosition(0);
            CompoundUndoManager.discardAllEdits(editor.getDocument());
            lastDir = new File(f.getParent());
            isTextChanged = false;
            saveAction.setEnabled(true);                        
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    private void save()
    {
        try
        {
            if(mode == TEMPLATE_MODE)
                curTemplate.save(editor.getText());
            else
            {               
                Writer writer = new FileWriter(curFile);
                editor.write(writer);
                writer.close();
            }               
            saveAction.setEnabled(false);
            isTextChanged = false;
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }   
    }
    
    private class TextChangeListener implements DocumentListener
    {
        public void insertUpdate(DocumentEvent e)
        {           
            isTextChanged = true;
            saveAction.setEnabled(true);            
        }
        
        public void removeUpdate(DocumentEvent e)
        {            
            isTextChanged = true;
            saveAction.setEnabled(true);            
        }
        
        public void changedUpdate(DocumentEvent e)
        {
            
        }
    }
    
    private class OpenAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public OpenAction()
        {
            super(i18n.str("open_"), UIUtils.getIcon(UIUtils.X16, "export.png")); //$NON-NLS-1$ //$NON-NLS-2$
            putValue(ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            openFile();            
        }
    }
    
    private class SaveAction extends AbstractAction
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public SaveAction()
        {
            super(i18n.str("save"), UIUtils.getIcon(UIUtils.X16, "save.png")); //$NON-NLS-1$ //$NON-NLS-2$
            putValue(ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        }
        
        public void actionPerformed(ActionEvent e)
        {
            save();
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
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
