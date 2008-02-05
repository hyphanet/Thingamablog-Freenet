/*
 * Created on Feb 28, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;

import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TextAction;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.gui.Messages;


/**
 * 
 *
 */
public class ClearStylesAction extends TextAction
{

    /**
     * @param name
     */
    public ClearStylesAction()
    {
        super(Messages.getString("HTMLEditorActionSet.Clear_Styles"));
        Messages.setMnemonic("HTMLEditorActionSet.Clear_Styles", this);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        JTextPane editor;
        HTMLDocument document;
        HTMLEditorKit editorKit;
        
        try
        {
            editor = (JTextPane)getTextComponent(e);
            document = (HTMLDocument)editor.getDocument();
            editorKit = (HTMLEditorKit)editor.getEditorKit();
        }
        catch(ClassCastException ex)
        {
            return;
        }
        
        Element el = document.getCharacterElement(editor.getCaretPosition());
        MutableAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.CONTENT);
        
        //boolean shouldClearSel = false;
        if(editor.getSelectedText() == null)
        {
            editor.replaceSelection("  ");
            editor.setSelectionStart(editor.getCaretPosition() - 1);
            editor.setSelectionEnd(editor.getSelectionStart() + 1); 
            document.setCharacterAttributes(editor.getSelectionStart(), 
                editor.getSelectionEnd() - editor.getSelectionStart(), attrs, true);
            editor.setSelectionStart(editor.getCaretPosition());
            editor.setSelectionEnd(editor.getCaretPosition());
        }
        else
        {
            document.setCharacterAttributes(editor.getSelectionStart(), 
                editor.getSelectionEnd() - editor.getSelectionStart(), attrs, true);
        }

    }

}
