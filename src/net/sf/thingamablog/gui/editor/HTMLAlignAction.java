/*
 * Created on Feb 25, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;


/**
 * 
 */
public class HTMLAlignAction extends HTMLTextEditAction
{
    private final String RES = TBGlobals.RESOURCES;
    
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int CENTER = 2;
    public static final int JUSTIFY = 3;
    
    public static final String ALIGNMENTS[] =
    {
        "left", "right", "center", "justify"
    };
    
    private static final String IMGS[] =
    {
        "alignleft.png", "alignright.png", "aligncenter.png", "alignjust.png"
    };
    
    private static final String ALIGN_NAMES[] =
    {
        "HTMLEditorActionSet.Left", "HTMLEditorActionSet.Right",
        "HTMLEditorActionSet.Center", "HTMLEditorActionSet.Justify"
    };
    
    private int align;
    
    public HTMLAlignAction(int al) throws IllegalArgumentException
    {
        super("");
        if(al < 0 || al >= ALIGNMENTS.length)
            throw new IllegalArgumentException("Illegal Argument");
        this.putValue(NAME, Messages.getString(ALIGN_NAMES[al]));
        Messages.setMnemonic(ALIGN_NAMES[al], this);
        this.putValue(SMALL_ICON, Utils.createIcon(RES + IMGS[al]));
        align = al;
    }
    
    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        int caretPosn = editor.getCaretPosition();
 	    Element elem =
 	           ((HTMLDocument)editor.getDocument()).getParagraphElement(caretPosn);
 	    //Set the HTML attribute on the paragraph...
 	    javax.swing.text.MutableAttributeSet set = new
 	    SimpleAttributeSet(elem.getAttributes());
 	    set.removeAttribute(HTML.Attribute.ALIGN);
 	    set.addAttribute(HTML.Attribute.ALIGN, ALIGNMENTS[align]);
 	     //Set the paragraph attributes...
 	    int start = elem.getStartOffset();
 	    int length = elem.getEndOffset() - elem.getStartOffset();
 	    ((HTMLDocument)editor.getDocument()).setParagraphAttributes(start,length-1,set,true);
    }
    
    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        String prefix = "<p align=\"" + ALIGNMENTS[align] + "\">";
        String postfix = "</p>";
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
}
