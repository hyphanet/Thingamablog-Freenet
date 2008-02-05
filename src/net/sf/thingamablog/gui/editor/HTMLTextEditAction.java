/*
 * Created on Feb 26, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;


/**
 * @author Bob Tantlinger
 *
 */
public abstract class HTMLTextEditAction extends TextAction
{

    public HTMLTextEditAction(String name)
    {
        super(name);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        JTextComponent tc = getTextComponent(e);
        if(tc == null)return;
        
        if(tc instanceof JEditorPane && tc.getDocument() instanceof HTMLDocument)
        {
            JEditorPane ed = (JEditorPane)tc;            
            if(ed.getEditorKit() instanceof HTMLEditorKit)
                wysiwygEditPerformed(e, ed);      
                
        }
        else if(tc instanceof JTextArea)
        {
            sourceEditPerformed(e, tc);
        }
    }
    
    public abstract void sourceEditPerformed(ActionEvent e, JTextComponent editor);
    
    public abstract void wysiwygEditPerformed(ActionEvent e, JEditorPane editor);
}
