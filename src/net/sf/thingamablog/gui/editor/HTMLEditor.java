/*
 * Created on Jun 10, 2004
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
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.blog.Template;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.TBAbout;
import net.sf.thingamablog.gui.TBHelpAction;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;


/**
 * @author Bob Tantlinger
 */
public class HTMLEditor extends JFrame
{
	public static final String RES = TBGlobals.RESOURCES;
	public static final int TEMPLATE_MODE = 1;
	public static final int FILE_MODE = 2;
	private int mode = FILE_MODE;
	
	private Template curTemplate;
	private File curFile = new File(System.getProperty("user.home"), "editor.txt"); //$NON-NLS-1$ //$NON-NLS-2$
	
	private RSyntaxTextArea editor;

	private Action fontColor = new HTMLFontColorAction(this);
	private Action alLeft = new HTMLAlignAction(HTMLAlignAction.LEFT);
	private Action alRight = new HTMLAlignAction(HTMLAlignAction.RIGHT);
	private Action alCenter = new HTMLAlignAction(HTMLAlignAction.CENTER);
	private Action alJust = new HTMLAlignAction(HTMLAlignAction.JUSTIFY);
	private Action ul = new HTMLBlockAction(HTMLBlockAction.T_UL);
	private Action ol = new HTMLBlockAction(HTMLBlockAction.T_OL);
	private Action bold = new HTMLInlineAction(HTMLInlineAction.BOLD);
	private Action italic = new HTMLInlineAction(HTMLInlineAction.ITALIC);
	private Action under = new HTMLInlineAction(HTMLInlineAction.UNDERLINE);
	private Action link = new HTMLLinkAction(this);
	private Action table = new HTMLTableAction(this);
	private Action insertHR = new HTMLHorizontalRuleAction();
	private Action lineBreak = new HTMLLineBreakAction();
		
	private File lastDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
	
	private Action openAction, saveAction;	
	private boolean isTextChanged;
	
    public HTMLEditor(Template tmp)
    {
		super(Messages.getString(
		    "HTMLEditor.Template_Editor") + " [" + tmp.getName() + "]");
			
		curTemplate = tmp;
		mode = TEMPLATE_MODE;
		init();
    }
    
    public HTMLEditor(File f)
    {		
		if(f != null)
		    setTitle(f.getName());
		String text = ""; //$NON-NLS-1$
				
		curFile = f;
		mode = FILE_MODE;
		init();		 //$NON-NLS-1$
    }
    
    public HTMLEditor(String title)
    {
    	super(title);
    	mode = FILE_MODE;
    	init(); 
    }
    
    private void load()
    {				
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
    		ioe.printStackTrace();
    	}   	
    	
		editor.setCaretPosition(0);		
		editor.discardAllEdits();
		saveAction.setEnabled(isTextChanged);		
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
		setIconImage(Utils.createIcon(TBGlobals.RESOURCES + "ticon.gif").getImage());		
		
		editor = new SourceTextArea();
		editor.setSyntaxEditingStyle(RSyntaxTextArea.XML_SYNTAX_STYLE);
		editor.setLineWrap(false);
		editor.getDocument().addDocumentListener(new TextChangeListener());	

		JScrollPane scroller = new RTextScrollPane(80, 40, editor, true);
		getContentPane().add(scroller, BorderLayout.CENTER);		
		
		initActions();
		
		editor.discardAllEdits();
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		setJMenuBar(createMenuBar());
		load();
		isTextChanged = false;
		saveAction.setEnabled(isTextChanged);
    }
    
	private void initActions()
	{
		openAction = new OpenAction();
		saveAction = new SaveAction();
		saveAction.setEnabled(false);
	}
	
	private JMenuBar createMenuBar()
	{
		JMenuBar mb = new JMenuBar();
		JMenu fileMenu = new JMenu(Messages.getString("EntryEditor.File")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.File", fileMenu); //$NON-NLS-1$
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
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
		if(mode == TEMPLATE_MODE)
		{		
			fileMenu.add(new ImportBloggerTemplateAction());
			fileMenu.addSeparator();
		}
		fileMenu.add(close);
		mb.add(fileMenu);		
		
		JMenu basicEditMenu = new JMenu(Messages.getString("EntryEditor.Edit")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.Edit", basicEditMenu); //$NON-NLS-1$
		basicEditMenu.add(RTextArea.getAction(RTextArea.UNDO_ACTION));
		basicEditMenu.add(RTextArea.getAction(RTextArea.REDO_ACTION));
		basicEditMenu.addSeparator();
		basicEditMenu.add(RTextArea.getAction(RTextArea.CUT_ACTION));
		basicEditMenu.add(RTextArea.getAction(RTextArea.COPY_ACTION));
		basicEditMenu.add(RTextArea.getAction(RTextArea.PASTE_ACTION));
		basicEditMenu.addSeparator();
		basicEditMenu.add(RTextArea.getAction(RTextArea.SELECT_ALL_ACTION));        
		basicEditMenu.addSeparator();
		basicEditMenu.add(new FindReplaceAction(editor, false));
		basicEditMenu.add(new FindReplaceAction(editor, true));
        
		mb.add(basicEditMenu);        
		mb.add(createFormatMenu());        
		mb.add(createInsertMenu());        

		JMenu helpMenu = new JMenu(Messages.getString("ThingamablogFrame.Help"));
		Messages.setMnemonic("ThingamablogFrame.Help", helpMenu);
		Action help = new TBHelpAction(
		        Messages.getString("ThingamablogFrame.Help_Contents"), "ch03.item5");
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
				TBAbout.showAboutBox(HTMLEditor.this);
			}
		});
		helpMenu.add(aboutItem);
		mb.add(helpMenu);
		
		return mb;	
	}
	
	private JMenu createInsertMenu()
	{
		JMenu m = new JMenu(Messages.getString("EntryEditor.Insert")); //$NON-NLS-1$
		Messages.setMnemonic("EntryEditor.Insert", m); //$NON-NLS-1$
		
		m.add(table);
		m.add(link);		
		m.add(fontColor);
		
		m.addSeparator();
		m.add(ul);
		m.add(ol);
		
		m.addSeparator();
		m.add(new HTMLBlockAction(HTMLBlockAction.T_P));
		m.add(lineBreak);
		m.add(insertHR);
		
		return m;
	}
	
	private JMenu createFormatMenu()
    {
        JMenu size = new JMenu(Messages.getString("EntryEditor.Size"));
        Messages.setMnemonic("EntryEditor.Size", size);

        JMenu textStyle = new JMenu(Messages.getString("EntryEditor.Style"));
        Messages.setMnemonic("EntryEditor.Style", textStyle);

        JMenu paragraph = new JMenu(Messages.getString("HTMLEditorActionSet.Paragraph"));
        Messages.setMnemonic("HTMLEditorActionSet.Paragraph", paragraph);

        JMenu align = new JMenu(Messages.getString("EntryEditor.Align"));
        Messages.setMnemonic("EntryEditor.Align", align);

        align.add(alLeft);
        align.add(alCenter);
        align.add(alRight);
        align.add(alJust);

        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XXSMALL));
        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XSMALL));
        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.SMALL));
        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.MEDIUM));
        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.LARGE));
        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XLARGE));
        size.add(new HTMLFontSizeAction(HTMLFontSizeAction.XXLARGE));

        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_P));
        paragraph.addSeparator();
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_PRE));
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_BLOCKQ));
        paragraph.addSeparator();
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_H1));
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_H2));
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_H3));
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_H4));
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_H5));
        paragraph.add(new HTMLBlockAction(HTMLBlockAction.T_H6));

        textStyle.add(bold);
        textStyle.add(italic);
        textStyle.add(under);
        textStyle.addSeparator();
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.I_CITE));
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.I_CODE));
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.I_EM));
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.I_STRONG));
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.I_SUBSCRIPT));
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.I_SUPERSCRIPT));
        textStyle.add(new HTMLInlineAction(HTMLInlineAction.STRIKE));

        JMenu m = new JMenu(Messages.getString("EntryEditor.Format")); //$NON-NLS-1$
        Messages.setMnemonic("EntryEditor.Format", m); //$NON-NLS-1$

        m.add(new EditFontAction());
        m.add(size);
        m.add(textStyle);
        m.add(fontColor);        
        m.addSeparator();
        m.add(paragraph);
        m.add(align);
        return m;
	}
	
	
	private JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //$NON-NLS-1$
		
		Utils.addToolbarButton(toolBar, openAction);
		Utils.addToolbarButton(toolBar, saveAction);
		toolBar.addSeparator();
		
		Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.UNDO_ACTION));
		Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.REDO_ACTION));
		toolBar.addSeparator();
		Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.CUT_ACTION));
		Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.COPY_ACTION));
		Utils.addToolbarButton(toolBar, RTextArea.getAction(RTextArea.PASTE_ACTION));
		toolBar.addSeparator();    	
		Utils.addToolbarButton(toolBar, bold);
		Utils.addToolbarButton(toolBar, italic);
		Utils.addToolbarButton(toolBar, under);
		
		toolBar.addSeparator();
		Utils.addToolbarButton(toolBar, alLeft);
		Utils.addToolbarButton(toolBar, alCenter);
		Utils.addToolbarButton(toolBar, alRight);
		Utils.addToolbarButton(toolBar, alJust);
		toolBar.addSeparator();
		Utils.addToolbarButton(toolBar, ul);
		Utils.addToolbarButton(toolBar, ol);
		toolBar.addSeparator();		
		Utils.addToolbarButton(toolBar, fontColor);
		Utils.addToolbarButton(toolBar, table);
		Utils.addToolbarButton(toolBar, link);
		Utils.addToolbarButton(toolBar, lineBreak);
		    	
		return toolBar;
	}
	
	public void setVisible(boolean b)
	{
		if(b)
		{
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) ((d.width - getSize().width) / 2);
			int y = (int) ((d.height - getSize().height) / 2);
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
				Messages.getString("HTMLEditor.save_changes_prompt"), Messages.getString("HTMLEditor.Text_Changed"), //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if(r == JOptionPane.CANCEL_OPTION)
				return;
			
			if(r == JOptionPane.YES_OPTION)
			{				
				save();
			}
		}	
		
		dispose();		
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
	
	private void openFile(boolean convertBloggerTemplate)
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
			if(convertBloggerTemplate)
				text = BloggerTemplateConverter.convert(text, curTemplate);	
			editor.setText(text);
			editor.setCaretPosition(0);
			editor.discardAllEdits();
			lastDir = new File(f.getParent());
			isTextChanged = false;
			saveAction.setEnabled(true);						
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	private class EditFontAction extends AbstractAction
	{
		public EditFontAction()
		{
			super(Messages.getString("HTMLEditorActionSet.Font"), null);
			Messages.setMnemonic("HTMLEditorActionSet.Font", this);
			putValue(SMALL_ICON, 
			    Utils.createIcon(TBGlobals.RESOURCES + "font16.gif"));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			HTMLFontDialog d = new HTMLFontDialog(HTMLEditor.this);
			d.setText(editor.getSelectedText());	
			d.setVisible(true);			
			if(!d.hasUserCancelled())
			{
				editor.requestFocusInWindow();
				editor.replaceSelection(d.getFontHTML());
			}
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
		public OpenAction()
		{
			super(Messages.getString("HTMLEditor.Open"), Utils.createIcon(RES + "open16.gif")); //$NON-NLS-1$ //$NON-NLS-2$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			openFile(false);			
		}
	}
	
	private class ImportBloggerTemplateAction extends AbstractAction
	{
		public ImportBloggerTemplateAction()
		{
			super(Messages.getString("HTMLEditor.Import_Blogger_Template")); //$NON-NLS-1$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			openFile(true);
		}
	}
	
	private class SaveAction extends AbstractAction
	{
		public SaveAction()
		{
			super(Messages.getString("HTMLEditor.Save"), Utils.createIcon(RES + "save16.gif")); //$NON-NLS-1$ //$NON-NLS-2$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
		}
		
		public void actionPerformed(ActionEvent e)
		{
			save();
		}
	}
}
