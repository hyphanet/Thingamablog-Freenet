/*
 * Created on Jun 19, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;


/**

 */
public class PasteAction extends TextAction
{
    public PasteAction()
    {
        super(Messages.getString("TextEditActionSet.Paste"));
        Messages.setMnemonic("TextEditActionSet.Paste", this);
        putValue(SMALL_ICON, Utils.createIcon(TBGlobals.RESOURCES + "paste.png"));
		putValue(ACCELERATOR_KEY,
			KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
    }
    
    public void actionPerformed(ActionEvent e)
    {
		JTextComponent tc = getTextComponent(e);
		if(tc == null)return;
		try
		{    		
		    JEditorPane editor = (JEditorPane)tc;
		    HTMLEditorKit ekit = (HTMLEditorKit)editor.getEditorKit();
		    HTMLDocument document = (HTMLDocument)editor.getDocument();
		    
		    Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		    try 
		    {
		        Transferable content = clip.getContents(this);
		        if(content == null)
		            return;
		        String txt = content.getTransferData(
		            new DataFlavor(String.class, "String")).toString();
		        
		        document.replace(editor.getSelectionStart(),
		            editor.getSelectionEnd() - editor.getSelectionStart(),
		        	txt, ekit.getInputAttributes());
		    } 
		    catch (Exception ex) 
		    {
		        ex.printStackTrace();
		    }
		}
		catch(ClassCastException cce)//Non html editor paste
		{
		    tc.paste();
		}
    }
}
