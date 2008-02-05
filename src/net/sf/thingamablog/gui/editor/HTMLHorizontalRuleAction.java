/*
 * Created on Mar 3, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.Element;
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
public class HTMLHorizontalRuleAction extends HTMLTextEditAction
{
    private final String RES = TBGlobals.RESOURCES;

    /**
     * @param name
     */
    public HTMLHorizontalRuleAction()
    {
        super(Messages.getString("HTMLEditorActionSet.Horizontal_Rule"));
        Messages.setMnemonic("HTMLEditorActionSet.Horizontal_Rule", this);
        this.putValue(SMALL_ICON, Utils.createIcon(RES + "hr.png"));        
    }

    /* (non-Javadoc)
     * @see com.bob.blah.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JTextPane)
     */
    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        editor.replaceSelection("<hr>");
    }

    /* (non-Javadoc)
     * @see com.bob.blah.HTMLTextEditAction#wysiwygEditPerformed(java.awt.event.ActionEvent, javax.swing.JTextPane)
     */
    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
		HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();
		HTMLDocument document = (HTMLDocument)editor.getDocument();
        
		int caret = editor.getCaretPosition();
        Element pElem = document.getParagraphElement(caret);
		String parentname = pElem.getParentElement().getName();
		HTML.Tag parentTag = HTML.getTag(parentname);		
		
		//bugfix: HR doesnt show up if document is empty
		//if(parentTag.equals(HTML.Tag.BODY) && document.getLength() < 2)
		if(caret == 1)
		{
	        try
	        {
	            
	            document.insertAfterStart(document.getParagraphElement(caret)
	                .getParentElement(),"<p></p>" + "<hr>" + "<p></p>");
	        }
	        catch(Exception ex)
	        {
	            ex.printStackTrace();
	        }
		}
		else
		{				
		    try 
		    {
		        editorKit.insertHTML(document, caret,
		            "<hr>", 0, 0, HTML.Tag.HR);
		    } 
		    catch (Exception ex) 
		    {
		        ex.printStackTrace();
		    }
		}
    }

}
