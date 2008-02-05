/*
 * Created on Feb 25, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;
import java.io.StringWriter;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.TextAction;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class ParaBreakAction extends TextAction
{    
    private Action delegate = null;
    
    public ParaBreakAction(Action defaultParaAction)
    {
        super("ParaBreakAction");
        delegate = defaultParaAction;
    }

    public void actionPerformed(ActionEvent e)
    {
        JEditorPane editor;
        HTMLDocument document;
        HTMLEditorKit editorKit;        
        try
        {
            editor = (JEditorPane)getTextComponent(e);
            document = (HTMLDocument)editor.getDocument();
            editorKit = (HTMLEditorKit)editor.getEditorKit();
        }
        catch(ClassCastException ex)
        {
            delegate.actionPerformed(e);
            return;
        }

        Element elem = document.getParagraphElement(editor.getCaretPosition());
        String elName = elem.getName().toUpperCase();
        String parentname = elem.getParentElement().getName();
        
        HTML.Tag parentTag = HTML.getTag(parentname);
        if(parentname.toUpperCase().equals("P-IMPLIED"))
            parentTag = HTML.Tag.IMPLIED;
        
        CompoundUndoHandler.beginCompoundEdit(document);
        if(parentname.toLowerCase().equals("li"))
        {
            if(elem.getEndOffset() - elem.getStartOffset() > 1)
            {
                try
                {
                    document.insertAfterEnd(elem.getParentElement(), "<li></li>");
                    editor.setCaretPosition(elem.getParentElement().getEndOffset());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {
                try
                {
                    document.remove(editor.getCaretPosition(), 1);
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                Element listParentElement = elem.getParentElement().getParentElement().getParentElement();
                HTML.Tag listParentTag = HTML.getTag(listParentElement.getName());
                String listParentTagName = listParentTag.toString();
                if(listParentTagName.toLowerCase().equals("li"))
                {
                    Element listAncEl = listParentElement.getParentElement();
                    try
                    {
                        editorKit.insertHTML(document, listAncEl.getEndOffset(), "<li><p></p></li>", 3, 0, HTML.Tag.LI);
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                else
                {
                    HTMLEditorKit.InsertHTMLTextAction pAction = 
                        new HTMLEditorKit.InsertHTMLTextAction("insertP",
                        "<p></p>", listParentTag, HTML.Tag.P);
                    pAction.actionPerformed(e);
                }
            }
        }        
        else if((elName.equals("PRE")) || (elName.equals("ADDRESS")) || (elName.equals("BLOCKQUOTE")))
        {
            try
            {
                insertParagraphAfter(elem, editor);                
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }            
        }        
        else if(elName.equals("P-IMPLIED"))
        {
            try
            {
                System.out.println("IMPLIED");
                System.out.println("PARENTNAME " + parentname);
                if(parentname.equals("body"))
                {
                    String text = "<html><body><p>" + editor.getText() + "</p></body></html>";
                    document.setInnerHTML(elem.getParentElement(), text);                    
                }                
                else if(parentname.toUpperCase().equals("PRE") || 
                    parentname.toUpperCase().equals("ADDRESS") || 
                    parentname.toUpperCase().equals("BLOCKQUOTE"))
                {                
                    insertParagraphAfter(elem.getParentElement(), editor);
                }
                else
                {
                    delegate.actionPerformed(e);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
        else
        {                  
            if((elem.getEndOffset() - editor.getCaretPosition()) == 1)                
            {
                //caret at end of para
                editor.replaceSelection("\n ");
                editor.setCaretPosition(editor.getCaretPosition() - 1);
            }
            else
            {                
                delegate.actionPerformed(e);
            }
            editorKit.getInputAttributes().removeAttribute(HTML.Attribute.ID);
            editorKit.getInputAttributes().removeAttribute(HTML.Attribute.CLASS);
        }

        CompoundUndoHandler.endCompoundEdit(document);
    }

    private void insertParagraphAfter(Element nEle, JEditorPane editor) 
    throws BadLocationException, java.io.IOException
    {
        int cr = editor.getCaretPosition();
        HTMLDocument document = (HTMLDocument)nEle.getDocument();
        HTML.Tag t = HTML.getTag(nEle.getName());        
        if(t == null)                   
            t = HTML.Tag.P;       
        
        String html = "";
        int end = nEle.getEndOffset();
        StringWriter out = new StringWriter();
        ElementWriter w = new ElementWriter(out, nEle, nEle.getStartOffset(), cr);                    
        w.write();
        html = "<" + t + ">" + 
        	HTMLUtils.removeEnclosingTags(nEle, out.toString()) + "</" + t + ">";
        
        out = new StringWriter();
        w = new ElementWriter(out, nEle, cr, nEle.getEndOffset());                    
        w.write();
        html += "<" + t + ">" + 
        	HTMLUtils.removeEnclosingTags(nEle, out.toString()) + "</" + t + ">";       
        
        document.setOuterHTML(nEle, html);
        editor.setCaretPosition(cr + 1);
    }
}