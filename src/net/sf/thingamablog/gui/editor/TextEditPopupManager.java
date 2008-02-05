/*
 * Created on Feb 21, 2005
  */
package net.sf.thingamablog.gui.editor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;

/**
 * @author bob
 *
 */
public class TextEditPopupManager 
{
	private final String RES = TBGlobals.RESOURCES;
	private static final Action cut = new DefaultEditorKit.CutAction();
	private static final Action copy = new DefaultEditorKit.CopyAction();
	private static final Action paste = new DefaultEditorKit.PasteAction();
	private static final Action selectAll = new SelectAllAction();	
    private Action undo = new UndoAction();
    private Action redo = new RedoAction();
    
    private Vector textComps = new Vector();
    private Vector undoers = new Vector();
    private UndoManager undoer;
    private FocusListener focusHandler = new PopupFocus();
    private MouseListener popupHandler = new PopupHandler();
    private UndoListener undoHandler = new UndoListener();
    private JPopupMenu popup = new JPopupMenu();
    
    public TextEditPopupManager()
    {
	    cut.putValue(Action.NAME, Messages.getString("TextEditActionSet.Cut"));
	    cut.putValue(Action.SMALL_ICON, Utils.createIcon(RES + "cut.png"));
        copy.putValue(Action.NAME, Messages.getString("TextEditActionSet.Copy"));
        copy.putValue(Action.SMALL_ICON, Utils.createIcon(RES + "copy.png"));
        paste.putValue(Action.NAME, Messages.getString("TextEditActionSet.Paste"));
        paste.putValue(Action.SMALL_ICON, Utils.createIcon(RES + "paste.png"));
        selectAll.putValue(Action.ACCELERATOR_KEY, null);
        
       	popup.add(undo);
		popup.add(redo);
		popup.addSeparator();
		popup.add(cut);
		popup.add(copy);
		popup.add(paste);
		popup.addSeparator();
		popup.add(selectAll);
    }
    
    public void addJTextComponent(JTextComponent tc)
    {    	
    	tc.addFocusListener(focusHandler);    	
    	tc.addMouseListener(popupHandler);
    	tc.getDocument().addUndoableEditListener(undoHandler);
    	UndoManager um = new UndoManager();
    	textComps.add(tc);
    	undoers.add(um);	
    }
    
	private void updateUndo()
    {
        undo.setEnabled(undoer.canUndo());
        redo.setEnabled(undoer.canRedo());
    }
	
	private class UndoListener implements UndoableEditListener
    {	
        public void undoableEditHappened(UndoableEditEvent e) 
        {            
            UndoableEdit edit = e.getEdit(); 
            if(undoer != null)
            {
                undoer.addEdit(edit);           
                updateUndo(); 
            }
        }
    }
    
	private class RedoAction extends AbstractAction 
	{
        public RedoAction()
        {
            super(Messages.getString("TextEditActionSet.Redo"),
                Utils.createIcon(RES + "redo.png"));
         }

        public void actionPerformed(ActionEvent e)
        {
			try
			{				
				if(undoer != null)
				{
				    undoer.redo(); 
				    updateUndo(); 
				}
			}
			catch(Exception ex)
			{
				System.out.println("Cannot Undo"); //$NON-NLS-1$
			}
            
        }
	}
	
	private class UndoAction extends AbstractAction 
	{
        public UndoAction()
        {
            super(Messages.getString("TextEditActionSet.Undo"), 
                Utils.createIcon(RES + "undo.png"));
        }

        public void actionPerformed(ActionEvent e)
        {
        	try
        	{				
				if(undoer != null)
				{
				    undoer.undo(); 
					updateUndo();
				}
        	}
        	catch(Exception ex)
        	{
          		System.out.println("Cannot Undo"); //$NON-NLS-1$
        	}     
            
        }
	}
    
    private class PopupFocus implements FocusListener
    {
    	public void focusGained(FocusEvent e)
    	{    		
    		if(e.isTemporary())
    			return;
    		//System.out.println("Focus gained");
    		JTextComponent tc = (JTextComponent)e.getComponent();
    		int index = textComps.indexOf(tc);
    		if(index != -1)
    		{    			
    			undoer = (UndoManager)undoers.get(index);
    			if(undoer != null)
    			    updateUndo();
    		}  		    		
    	}
    	
    	public void focusLost(FocusEvent e)
    	{
    		
    	}
    }
    
    private class PopupHandler extends MouseAdapter
    {
		public void mousePressed(MouseEvent e)
		{ checkForPopupTrigger(e); }
		
		public void mouseReleased(MouseEvent e)
		{ checkForPopupTrigger(e); }
		
		private void checkForPopupTrigger(MouseEvent e)
		{
			if(e.isPopupTrigger())
			{
				if(e.getComponent().isFocusOwner())
					popup.show(e.getComponent(), e.getX(), e.getY());				
			}
		}
    }
}
