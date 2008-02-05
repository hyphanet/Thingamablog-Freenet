/*
 * Created on Feb 25, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;


/**
  
 */
public class HTMLInlineAction extends HTMLTextEditAction
{
    private final String RES = TBGlobals.RESOURCES;    

    public static final int I_EM = 0;

    public static final int I_STRONG = 1;

    public static final int I_CODE = 2;

    public static final int I_CITE = 3;

    public static final int I_SUPERSCRIPT = 4;

    public static final int I_SUBSCRIPT = 5;
    
    public static final int BOLD = 6;
    
    public static final int ITALIC = 7;
    
    public static final int UNDERLINE = 8;
    
    public static final int STRIKE = 9;

    private static final String[] inlineTypes = 
    {        
        "HTMLEditorActionSet.Emphasis",
        "HTMLEditorActionSet.Strong",
        "HTMLEditorActionSet.Code",
        "HTMLEditorActionSet.Cite",
        "HTMLEditorActionSet.Superscript",
        "HTMLEditorActionSet.Subscript",        
        "HTMLEditorActionSet.Bold",
        "HTMLEditorActionSet.Italic",
        "HTMLEditorActionSet.Underline",
        "HTMLEditorActionSet.Strikethrough"        
    };
    

    private int type;

    public HTMLInlineAction(int itype) throws IllegalArgumentException
    {
        super("");
        type = itype;
        if(type < 0 || type >= inlineTypes.length)
            throw new IllegalArgumentException("Illegal Argument");
        putValue(NAME, Messages.getString(inlineTypes[type]));
        Messages.setMnemonic(inlineTypes[type], this);
        
        Icon ico = null;
        KeyStroke ks = null;
        if(type == BOLD)
        {
            ico = Utils.createIcon(RES + "bold.png");
            ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        }
        else if(type == ITALIC)
        {
            ico = Utils.createIcon(RES + "italic.png");
            ks = KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK);
        }
        else if(type == UNDERLINE)
        {
            ico = Utils.createIcon(RES + "underline.png");
            ks = KeyStroke.getKeyStroke(KeyEvent.VK_U, Event.CTRL_MASK);
        }
        putValue(SMALL_ICON, ico);
        putValue(ACCELERATOR_KEY, ks);
    }

    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        HTML.Tag tag = getTag();
        String prefix = "<" + tag.toString() + ">";
        String postfix = "</" + tag.toString() + ">";
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

    private HTML.Tag getTag()
    {
        HTML.Tag tag = null;
                
        switch(type)
        {
            case I_EM:
                tag = HTML.Tag.EM;
                break;
            case I_STRONG:
                tag = HTML.Tag.STRONG;
                break;
            case I_CODE:
                tag = HTML.Tag.CODE;
                break;
            case I_SUPERSCRIPT:
                tag = HTML.Tag.SUP;
                break;
            case I_SUBSCRIPT:
                tag = HTML.Tag.SUB;
                break;
            case I_CITE:
                tag = HTML.Tag.CITE;
                break;
            case BOLD:
                tag = HTML.Tag.B;
                break;
            case ITALIC:
                tag = HTML.Tag.I;
                break;
            case UNDERLINE:
                tag = HTML.Tag.U;
                break;
            case STRIKE:
                tag = HTML.Tag.STRIKE;
                break;                
        }
        return tag;
    }

    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        if(type == BOLD)
        {
            new StyledEditorKit.BoldAction().actionPerformed(e);
            return;
        }
        
        if(type == ITALIC)
        {
            new StyledEditorKit.ItalicAction().actionPerformed(e);
            return;
        }
        
        if(type == UNDERLINE)
        {
            new StyledEditorKit.UnderlineAction().actionPerformed(e);
            return;
        }        

        HTMLDocument document = (HTMLDocument)editor.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();        
        Element el = document.getCharacterElement(editor.getCaretPosition());
        MutableAttributeSet attrs = new SimpleAttributeSet();               
       
        boolean shouldClearSel = false;
        if(editor.getSelectedText() == null && document.getLength() > 0)
        {
            editor.replaceSelection("  ");
            editor.setSelectionStart(editor.getCaretPosition() - 1);
            editor.setSelectionEnd(editor.getSelectionStart() + 1);
            
            shouldClearSel = true;
        }
        attrs.addAttribute(getTag(), new SimpleAttributeSet());
        if(editor.getSelectionEnd() > editor.getSelectionStart())
            document.setCharacterAttributes(editor.getSelectionStart(), 
                editor.getSelectionEnd() - editor.getSelectionStart(), attrs, false);
        
        if(shouldClearSel)
        {
            editor.setSelectionStart(editor.getCaretPosition());
            editor.setSelectionEnd(editor.getCaretPosition());
        }
    }
}