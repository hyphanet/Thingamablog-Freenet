package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

import net.sf.thingamablog.gui.Messages;


/**
 * @author Bob 
 * Select all action
 */
public class SelectAllAction extends TextAction
{
    public SelectAllAction()
    {
        super(Messages.getString("TextEditActionSet.Select_All"));
        Messages.setMnemonic("TextEditActionSet.Select_All", this);
        putValue(ACCELERATOR_KEY, 
            KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e)
    {
        JTextComponent tc = getTextComponent(e);
        tc.selectAll();
    }
}