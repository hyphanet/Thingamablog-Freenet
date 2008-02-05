/*
 * Created on Feb 27, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;

import net.sf.thingamablog.gui.Messages;



/**
 * @author Bob Tantlinger
 */
public class HTMLFontSizeAction extends HTMLTextEditAction
{
    public static final int XXSMALL = 0;
    public static final int XSMALL = 1;
    public static final int SMALL = 2;
    public static final int MEDIUM = 3;
    public static final int LARGE = 4;
    public static final int XLARGE = 5;
    public static final int XXLARGE = 6;
    
    private static final String SML = Messages.getString("HTMLEditorActionSet.Small");
    private static final String MED = Messages.getString("HTMLEditorActionSet.Medium");
    private static final String LRG = Messages.getString("HTMLEditorActionSet.Large");
    
    private final int sizes[] = {8, 10, 12, 14, 18, 24, 28};
    //public static boolean LOCK = false;
    
    public static final String SIZES[] =
    {
        "xx-" + SML, "x-" + SML, SML, MED,
        LRG, "x-" + LRG, "xx-" + LRG
    };
    
    private int size;
    
    /**
     * @param name
     */
    public HTMLFontSizeAction(int size) throws IllegalArgumentException
    {
        super("");
        if(size < 0 || size > 6)
            throw new IllegalArgumentException("Invalid size");
        this.size = size;
        this.putValue(NAME, SIZES[size]);
    }

    /* (non-Javadoc)
     * @see com.bob.blah.HTMLTextEditAction#sourceEditPerformed(java.awt.event.ActionEvent, javax.swing.JTextPane)
     */
    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        String prefix = "<font size=" + (size + 1) + ">";
        String postfix = "</font>";
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

    /* (non-Javadoc)
     * @see com.bob.blah.HTMLTextEditAction#wysiwygEditPerformed(java.awt.event.ActionEvent, javax.swing.JTextPane)
     */
    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {        
        Action a = new StyledEditorKit.FontSizeAction(SIZES[size], sizes[size]);
        a.actionPerformed(e);        
    }
}
