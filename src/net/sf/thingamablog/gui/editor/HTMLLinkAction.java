/*
 * Created on Feb 26, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;



/**
 * 
 */
public class HTMLLinkAction extends HTMLTextEditAction
{
    private final String RES = TBGlobals.RESOURCES;
    private Frame parent;

    public HTMLLinkAction(Frame parent)
    {
        super(Messages.getString("HTMLEditorActionSet.Hyperlink"));
        Messages.setMnemonic("HTMLEditorActionSet.Hyperlink", this);
        this.parent = parent;
        this.putValue(SMALL_ICON, Utils.createIcon(RES + "link.png"));
    }

    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        HyperLinkDialog dlg = new HyperLinkDialog(parent);
        dlg.setLocationRelativeTo(parent);
		//dlg.setModal(true);
		
		dlg.setName(editor.getSelectedText());
		dlg.setLinkDescription(editor.getSelectedText());
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;
		
		editor.requestFocusInWindow();
		editor.replaceSelection(dlg.getHTML());		
    }

    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        HyperLinkDialog dlg = new HyperLinkDialog(parent);
        if(editor.getSelectedText() != null)
            dlg.setLinkDescription(editor.getSelectedText());
        dlg.setLocationRelativeTo(parent);		
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;
		
		HTMLDocument document = (HTMLDocument)editor.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();
		
		String tagText = dlg.getHTML();
		//if(editor.getCaretPosition() == document.getLength())
		if(editor.getSelectedText() == null)
			tagText += "&nbsp;";		
		
		editor.replaceSelection("");
		HTMLUtils.insertInlineHTML(tagText, HTML.Tag.A, editor);
    }
}
