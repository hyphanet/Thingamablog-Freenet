/*
 * Created on Jun 7, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.util.Vector;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;


public abstract class CompoundUndoHandler implements UndoableEditListener
{    
    private UndoManager undoer;
    private CompoundEdit compoundEdit = null;
    
    private static Vector docs = new Vector();
    private static Vector lsts = new Vector();
    
    protected static void registerDocument(Document doc, CompoundUndoHandler lst)
    {
        docs.add(doc);
        lsts.add(lst);
    }    
    
    public static void beginCompoundEdit(Document doc)
    {
        for(int i = 0; i < docs.size(); i++)
        {
            if(docs.elementAt(i) == doc)
            {
                CompoundUndoHandler l = (CompoundUndoHandler)lsts.elementAt(i);
                l.beginCompoundEdit();
                return;
            }
        }
    }
    
    public static void endCompoundEdit(Document doc)
    {
        for(int i = 0; i < docs.size(); i++)
        {
            if(docs.elementAt(i) == doc)
            {
                CompoundUndoHandler l = (CompoundUndoHandler)lsts.elementAt(i);
                l.endCompoundEdit();
                return;
            }
        }
    }    

    public CompoundUndoHandler(Document doc, UndoManager um)
    {
       undoer = um;
       registerDocument(doc, this);
    }   
    
    public void undoableEditHappened(UndoableEditEvent evt)
    {       
        UndoableEdit edit = evt.getEdit();
        if(compoundEdit != null)
        {             
            //System.out.println("adding to compound");
            compoundEdit.addEdit(edit);               
        }
        else
        {            
            undoer.addEdit(edit);
            editAdded();
        }        
    }
    
    public void beginCompoundEdit()
    {
        //System.out.println("starting compound");
        compoundEdit = new CompoundEdit();
    }
    
    public void endCompoundEdit()
    {
        //System.out.println("ending compound");
        if(compoundEdit != null)
        {
            compoundEdit.end();
            undoer.addEdit(compoundEdit);
            editAdded();
        }
        compoundEdit = null;
    }
    
    public abstract void editAdded();
}