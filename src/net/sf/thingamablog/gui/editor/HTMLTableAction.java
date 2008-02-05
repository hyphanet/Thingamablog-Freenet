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
public class HTMLTableAction extends HTMLTextEditAction
{
    private Frame parent;
    private final String RES = TBGlobals.RESOURCES;
    

    public HTMLTableAction(Frame p)
    {
        super(Messages.getString("HTMLEditorActionSet.Table"));
        Messages.setMnemonic("HTMLEditorActionSet.Table", this);
        parent = p;
        this.putValue(SMALL_ICON, Utils.createIcon(RES + "table.png"));        
    }


    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {       
        TableDialog dlg = new TableDialog(parent);
        dlg.setLocationRelativeTo(parent);		
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;
        
        editor.replaceSelection(dlg.getHTML());
    }

    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        TableDialog dlg = new TableDialog(parent);
        dlg.setLocationRelativeTo(parent);
		//dlg.setModal(true);
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;
		
		HTMLDocument document = (HTMLDocument)editor.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();
		
		String tableTag = dlg.getHTML();
		String parentname =
			document
				.getParagraphElement(editor.getCaretPosition())
				.getParentElement()
				.getName();
		HTML.Tag parentTag = HTML.getTag(parentname);
		//System.out.println(parentTag + ":\n" + tableTag);
		
		int caret = editor.getCaretPosition();
		//bugfix: Table doesnt show up if document is empty
		//if(parentTag.equals(HTML.Tag.BODY) && document.getLength() < 2)
		if(caret == 1)
		{
		    CompoundUndoHandler.beginCompoundEdit(document);
		    try
	        {
	            //document.setInnerHTML(document.getParagraphElement(editor.getCaretPosition())
	            //    .getParentElement(),"<p></p>" + tableTag + "<p></p>");
	            
	            document.insertAfterStart(document.getParagraphElement(caret)
	                .getParentElement(),"<p></p>" + tableTag + "<p></p>");
	        }
	        catch(Exception ex)
	        {
	            ex.printStackTrace();
	        }
	        CompoundUndoHandler.endCompoundEdit(document);
		}
		else
		{
		    CompoundUndoHandler.beginCompoundEdit(document);
		    try {
				editorKit.insertHTML(
					document,
					editor.getCaretPosition(),
					tableTag,
					1,
					0,
					HTML.Tag.TABLE);
				//removeIfEmpty(document.getParagraphElement(editor.getCaretPosition()-1));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			CompoundUndoHandler.endCompoundEdit(document);
		}
				
    }

}
