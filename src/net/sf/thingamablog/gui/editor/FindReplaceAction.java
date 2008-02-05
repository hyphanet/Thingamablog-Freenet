package net.sf.thingamablog.gui.editor;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sf.thingamablog.gui.Messages;

public class FindReplaceAction extends AbstractAction 
{
	private boolean isReplaceTab;
	private JTextComponent textComponent;
	private TextFinderDialog d;
	
	public FindReplaceAction(JTextComponent tc, boolean isReplace)
	{
		super(null);
		if(isReplace)
		{			
			putValue(NAME, Messages.getString("TextEditActionSet.Replace")); //$NON-NLS-1$
			Messages.setMnemonic("TextEditActionSet.Replace", this);
		}
		else
		{			
			putValue(NAME, Messages.getString("TextEditActionSet.Find")); //$NON-NLS-1$
			Messages.setMnemonic("TextEditActionSet.Find", this);
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK));
		}
				
		isReplaceTab = isReplace;
		setEditor(tc);
	}
	
	public void setEditor(JTextComponent tc)
	{
	    textComponent = tc;
	    if(d != null)
	        d.dispose();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(textComponent == null)
		    return;
		
	    Component c = textComponent.getParent();
		while(c.getParent() != null)			
			c = (Component)c.getParent();			
		
		if(c instanceof Frame)
		{			
			if(isReplaceTab)
			d = new TextFinderDialog((Frame)c, textComponent, TextFinderDialog.REPLACE);
			else
			d = new TextFinderDialog((Frame)c, textComponent, TextFinderDialog.FIND);
		}
		else if(c instanceof Dialog)
		{			
			if(isReplaceTab)
			d = new TextFinderDialog((Dialog)c, textComponent, TextFinderDialog.REPLACE);
			else
			d = new TextFinderDialog((Dialog)c, textComponent, TextFinderDialog.FIND);
		}
		else 
			return;
		
		//if(textComponent.getSelectionStart() != textComponent.getSelectionEnd())
		//	d.setSearchText(textComponent.getSelectedText());
		
		d.setLocationRelativeTo(c);
		d.setVisible(true);
	}
}