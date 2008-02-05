/*
 * Created on Feb 25, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Event;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
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
public class HTMLLineBreakAction extends HTMLTextEditAction
{
    private final String RES = TBGlobals.RESOURCES;

    public HTMLLineBreakAction()
    {
        super(Messages.getString("HTMLEditorActionSet.Line_Break"));
        Messages.setMnemonic("HTMLEditorActionSet.Line_Break", this);
        putValue(SMALL_ICON, Utils.createIcon(RES + "break.png"));
        putValue(ACCELERATOR_KEY, 
        	KeyStroke.getKeyStroke(Event.ENTER, Event.SHIFT_MASK));
    }    

    
    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        editor.replaceSelection("<br>\n");
    }
    
    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
		HTMLDocument document = (HTMLDocument)editor.getDocument();
		int pos = editor.getCaretPosition();
        
        String elName =
			document
				.getParagraphElement(pos)
				.getName();
		/*
		 * if ((elName.toUpperCase().equals("PRE")) ||
		 * (elName.toUpperCase().equals("P-IMPLIED"))) {
		 * editor.replaceSelection("\r"); return;
		 */
		HTML.Tag tag = HTML.getTag(elName);
		if (elName.toUpperCase().equals("P-IMPLIED"))
			tag = HTML.Tag.IMPLIED;

		HTMLEditorKit.InsertHTMLTextAction hta =
			new HTMLEditorKit.InsertHTMLTextAction(
				"insertBR",
				"<br>",
				tag,
				HTML.Tag.BR);
		hta.actionPerformed(e);
    }
}
