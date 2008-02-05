/*
 * Created on Feb 26, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;

/**
 * Action which formats HTML block level elements
 * 
 * @author Bob Tantlinger
 *
 */
public class HTMLBlockAction extends HTMLTextEditAction
{    
    
    
    public static boolean LOCK = false;
    
    public static final int DIV = -1;
    public static final int T_P = 0;
    public static final int T_H1 = 1;
    public static final int T_H2 = 2;
    public static final int T_H3 = 3;
    public static final int T_H4 = 4;
    public static final int T_H5 = 5;
    public static final int T_H6 = 6;
    public static final int T_PRE = 7;  
    public static final int T_BLOCKQ = 8;
    public static final int T_OL = 9;
    public static final int T_UL = 10;
    
    
    private static final int KEYS[] =
    {
        KeyEvent.VK_D, KeyEvent.VK_ENTER, KeyEvent.VK_1, KeyEvent.VK_2, 
        KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, 
        KeyEvent.VK_R, KeyEvent.VK_Q, KeyEvent.VK_N, KeyEvent.VK_U
    };

    public static final String[] elementTypes =
    {
        //"Body Text",
        Messages.getString("HTMLEditorActionSet.Paragraph"),
        Messages.getString("HTMLEditorActionSet.Heading") + " 1",
        Messages.getString("HTMLEditorActionSet.Heading") + " 2",
        Messages.getString("HTMLEditorActionSet.Heading") + " 3",
        Messages.getString("HTMLEditorActionSet.Heading") + " 4",
        Messages.getString("HTMLEditorActionSet.Heading") + " 5",
        Messages.getString("HTMLEditorActionSet.Heading") + " 6",
        Messages.getString("HTMLEditorActionSet.Preformatted"),         
        Messages.getString("HTMLEditorActionSet.Blockquote"),
        Messages.getString("HTMLEditorActionSet.Ordered_List"),
        Messages.getString("HTMLEditorActionSet.Unordered_List")        
    };
    
    private int type;
    
    /**
     * Creates a new HTMLBlockAction
     * 
     * @param type A block type - P, PRE, BLOCKQUOTE, H1, H2, etc
     * 
     * @throws IllegalArgumentException
     */
    public HTMLBlockAction(int type) throws IllegalArgumentException
    {        
        super("");
        if(type < 0 || type >= elementTypes.length)
            throw new IllegalArgumentException("Illegal argument");
        
        this.type = type; 
        putValue(NAME, elementTypes[type]);
        putValue(Action.ACCELERATOR_KEY, 
            KeyStroke.getKeyStroke(KEYS[type], Event.ALT_MASK));
        if(type == T_P)
            Messages.setMnemonic("HTMLEditorActionSet.Paragraph", this);
        else if(type == T_PRE)
            Messages.setMnemonic("HTMLEditorActionSet.Preformatted", this);
        else if(type == T_BLOCKQ)
            Messages.setMnemonic("HTMLEditorActionSet.Blockquote", this);
        else if(type == T_OL)
        {
            putValue(SMALL_ICON,
                Utils.createIcon(TBGlobals.RESOURCES + "listordered.png"));
            Messages.setMnemonic("HTMLEditorActionSet.Ordered_List", this);
        }
        else if(type == T_UL)
        {
            putValue(SMALL_ICON,
                Utils.createIcon(TBGlobals.RESOURCES + "listunordered.png"));
            Messages.setMnemonic("HTMLEditorActionSet.Unordered_List", this);
        }
        else
        {
            String s = type + "";
            putValue(Action.MNEMONIC_KEY, new Integer(s.charAt(0)));
        }
    }

    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        if(LOCK)
            return;
        
        String tag = getTag().toString();
        String prefix = "\n<" + tag + ">\n\t";
        String postfix = "\n</" + tag + ">\n";
        if(type == T_OL || type == T_UL)
        {
            prefix += "<li>";
            postfix = "</li>" + postfix;
        }
        
        String sel = editor.getSelectedText();
        if(sel == null)
        {
            editor.replaceSelection(prefix + postfix);
            
            int pos = editor.getCaretPosition() - postfix.length();
            if(pos >= 0)
                editor.setCaretPosition(pos);                             
        }
        else
        {
            sel = prefix + sel + postfix;
            editor.replaceSelection(sel);                
        }
    }

    
    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        if(LOCK)
            return;
        
        HTMLDocument document = (HTMLDocument)editor.getDocument();
        int caret = editor.getCaretPosition();
        CompoundUndoHandler.beginCompoundEdit(document);        
        try
        {            
            if(type == T_OL || type == T_UL)
            {
                insertList(editor, e);
            }
            else 
            { 
                changeBlockType(editor, e);
            }
            editor.setCaretPosition(caret);
        }
        catch(Exception awwCrap)
        {
            awwCrap.printStackTrace();
        }        

        CompoundUndoHandler.endCompoundEdit(document);      
    }
    
    
    private HTML.Tag getRootTag(Element elem)
    {
        HTML.Tag root = HTML.Tag.BODY;
        if(HTMLUtils.getParent(elem, HTML.Tag.TD) != null)
            root = HTML.Tag.TD;
        return root;
    }
     
    private String cutOutElement(Element el) throws BadLocationException
    {
        String txt = HTMLUtils.getElementHTML(el, false);       
        HTMLUtils.removeElement(el);        
        return txt;
    }
    
    private void insertHTML(String html, HTML.Tag tag, HTML.Tag root, ActionEvent e)
    {        
        HTMLEditorKit.InsertHTMLTextAction a = 
            new HTMLEditorKit.InsertHTMLTextAction("insertHTML", html, root, tag);            
        a.actionPerformed(e);        
    }
    
    private void changeListType(Element listParent, HTML.Tag replaceTag, HTMLDocument document)
    {        
        StringWriter out = new StringWriter();
        ElementWriter w = new ElementWriter(out, listParent);        
        try
        {
            w.write();
            String html = out.toString();        
            html = html.substring(html.indexOf('>') + 1, html.length());
            html = html.substring(0, html.lastIndexOf('<'));        
            html = '<' + replaceTag.toString() + '>' + html + "</" + replaceTag.toString() + '>';
            document.setOuterHTML(listParent, html);    
        }
        catch(Exception idiotic){}
    }
    
    private void insertList(JEditorPane editor, ActionEvent e) 
    throws BadLocationException
    {
        HTMLDocument document = (HTMLDocument)editor.getDocument();
        int caretPos = editor.getCaretPosition();
        Element elem = document.getParagraphElement(caretPos);
        HTML.Tag parentTag = HTML.getTag(elem.getParentElement().getName());
        
        //check if we need to change the list from one type to another
        Element listParent = elem.getParentElement().getParentElement();
        HTML.Tag listTag = HTML.getTag(listParent.getName());        
        if(listTag.equals(HTML.Tag.UL) || listTag.equals(HTML.Tag.OL))
        {
            HTML.Tag t = HTML.getTag(listParent.getName());
            if(type == T_OL && t.equals(HTML.Tag.UL))
            {                
                changeListType(listParent, HTML.Tag.OL, document);                
                return;
            }
            else if(type == T_UL && listTag.equals(HTML.Tag.OL))
            {                
                changeListType(listParent, HTML.Tag.UL, document);                
                return;                
            } 
        }        

        if(!parentTag.equals(HTML.Tag.LI))//don't allow nested lists
        {            
            //System.err.println("INSERT LIST");
            changeBlockType(editor, e);
        }       
        else//is already a list, so turn off list
        {   
            HTML.Tag root = getRootTag(elem);
            String txt = HTMLUtils.getElementHTML(elem, false);
            editor.setCaretPosition(elem.getEndOffset());            
            insertHTML("<p>" + txt + "</p>", HTML.Tag.P, root, e);
            HTMLUtils.removeElement(elem);
        }
        
    }
    
    private void changeBlockType(JEditorPane editor, ActionEvent e) 
    throws BadLocationException
    {
        HTMLDocument doc = (HTMLDocument)editor.getDocument();
        Element curE = doc.getParagraphElement(editor.getSelectionStart());
        Element endE = doc.getParagraphElement(editor.getSelectionEnd());
        
        Element curTD = HTMLUtils.getParent(curE, HTML.Tag.TD);
        HTML.Tag tag = getTag();
        HTML.Tag rootTag = getRootTag(curE);
        String html = "";
        
        if(isListType())
        {
            html = "<" + getTag() + ">";
            tag = HTML.Tag.LI;
        }        
                        
        //a list to hold the elements we want to change
        List elToRemove = new ArrayList();
        elToRemove.add(curE);
        
        while(true)
        {            
            html += HTMLUtils.createTag(tag, 
                curE.getAttributes(), HTMLUtils.getElementHTML(curE, false));
            if(curE.getEndOffset() >= endE.getEndOffset()
                || curE.getEndOffset() >= doc.getLength())
                break;
            curE = doc.getParagraphElement(curE.getEndOffset() + 1);
            elToRemove.add(curE);
            
            //did we enter a (different) table cell?
            Element ckTD = HTMLUtils.getParent(curE, HTML.Tag.TD);
            if(ckTD != null && !ckTD.equals(curTD))
                break;//stop here so we don't mess up the table
        }
                
        if(isListType())
            html += "</" + getTag() + ">";
        
        //set the caret to the start of the last selected block element
        editor.setCaretPosition(curE.getStartOffset());
        
        //insert our changed block
        //we insert first and then remove, because of a bug in jdk 6.0
        insertHTML(html, getTag(), rootTag, e);
        
        //now, remove the elements that were changed.
        for(Iterator it = elToRemove.iterator(); it.hasNext();)
        {
            Element c = (Element)it.next();
            HTMLUtils.removeElement(c);
        }
    } 
    
    private boolean isListType()
    {
        return type == T_OL || type == T_UL;
    }

    private HTML.Tag getTag()
    {
        HTML.Tag tag = HTML.Tag.P;

        switch(type) 
        {
            case T_P :
                tag = HTML.Tag.P;
                break;
            case T_H1 :
                tag = HTML.Tag.H1;
                break;
            case T_H2 :
                tag = HTML.Tag.H2;
                break;
            case T_H3 :
                tag = HTML.Tag.H3;
                break;
            case T_H4 :
                tag = HTML.Tag.H4;
                break;
            case T_H5 :
                tag = HTML.Tag.H5;
                break;
            case T_H6 :
                tag = HTML.Tag.H6;
                break;
            case T_PRE :
                tag = HTML.Tag.PRE;
                break;
            case T_UL :
                tag = HTML.Tag.UL;
                break;
            case T_OL :
                tag = HTML.Tag.OL;
                break;
            case T_BLOCKQ :
                tag = HTML.Tag.BLOCKQUOTE;
                break;
            case DIV :
                tag = HTML.Tag.DIV;
                break;
        }
        
        return tag;
    }
}
