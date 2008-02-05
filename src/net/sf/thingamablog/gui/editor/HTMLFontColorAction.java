/*
 * Created on Feb 28, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;


/**
 * 
 */
public class HTMLFontColorAction extends HTMLTextEditAction
{
    private final String RES = TBGlobals.RESOURCES;
    private Component parent;
    
    public HTMLFontColorAction(Component c)
    {
        super(Messages.getString("HTMLEditorActionSet.Color"));
        Messages.setMnemonic("HTMLEditorActionSet.Color", this);
        this.putValue(SMALL_ICON, Utils.createIcon(RES + "color.png"));
        parent = c;
    }

    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        Color c = getColorFromUser();
        if(c == null)
            return;
        
        String prefix = "<font color=" + Utils.colorToHex(c) + ">";
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

    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        Color color = getColorFromUser();
		if(color != null)
		{
		    Action a = new StyledEditorKit.ForegroundAction("Color", color);
		    a.actionPerformed(e);
		}
    }
    
    private Color getColorFromUser()
    {	
        Color color = 
			JColorChooser.showDialog(parent, "Color", Color.black);	 //$NON-NLS-1$
		return color;
    }

}
