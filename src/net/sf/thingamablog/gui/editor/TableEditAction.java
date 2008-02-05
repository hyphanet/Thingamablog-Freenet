/*
 * Created on Jun 16, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.gui.Messages;


/**
 * 
 */
public class TableEditAction extends AbstractAction
{
    public static final int INSERT_CELL = 0;
    public static final int DELETE_CELL = 1;    
    public static final int INSERT_ROW = 2;
    public static final int DELETE_ROW = 3;
    public static final int INSERT_COL = 4;
    public static final int DELETE_COL = 5;
    
    
    private static final String NAMES[] =
    {
        "HTMLEditorActionSet.Insert_Cell", 
        "HTMLEditorActionSet.Delete_Cell", 
        "HTMLEditorActionSet.Insert_Row", 
        "HTMLEditorActionSet.Delete_Row", 
        "HTMLEditorActionSet.Insert_Column", 
        "HTMLEditorActionSet.Delete_Column"
    };
    
    private JEditorPane editor;
    private int type;
    
    public TableEditAction(int type, JEditorPane editor) throws IllegalArgumentException
    {
        super("");
        if(type < 0 || type >= NAMES.length)
            throw new IllegalArgumentException("Invalid type");
        this.type = type;
        putValue(NAME, Messages.getString(NAMES[type]));
        setEditor(editor);
    }
    
    public void setEditor(JEditorPane editor)
    {
        this.editor = editor;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {        
        Element td = getTDElement();
        Element tr = getTRElement();
        HTMLDocument document = getDocument();
        if(td == null || tr == null || document == null)
            return;
        
        String tdTag = "<td><p></p></td>";
        CompoundUndoHandler.beginCompoundEdit(document);
        try
        {            
            if(type == INSERT_CELL)
                document.insertAfterEnd(td, tdTag);
            else if(type == DELETE_CELL)
                HTMLUtils.removeElement(td);
            else if(type == INSERT_ROW)
                document.insertAfterEnd(tr, getRowHTML(tr));
            else if(type == DELETE_ROW)
                HTMLUtils.removeElement(tr);
            else if(type == INSERT_COL)
                insertTableColumn();
            else if(type == DELETE_COL)
                deleteTableColumn();                
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        CompoundUndoHandler.endCompoundEdit(document);
    }
    
	/** Method for inserting a column into an HTML Table
	  */
	private void insertTableColumn()
	{
		HTMLDocument htmlDoc = getDocument();
		if(htmlDoc == null)
		    return;
		
	    int caretPos = editor.getCaretPosition();
		Element	element = htmlDoc.getCharacterElement(editor.getCaretPosition());
		Element elementParent = element.getParentElement();
		int startPoint = -1;
		int rowCount   = -1;
		int cellOffset =  0;
		while(elementParent != null && !elementParent.getName().equals("body"))
		{
			if(elementParent.getName().equals("table"))
			{
				startPoint = elementParent.getStartOffset();
				rowCount   = elementParent.getElementCount();
				break;
			}
			else if(elementParent.getName().equals("tr"))
			{
				int rowStart = elementParent.getStartOffset();
				int rowCells = elementParent.getElementCount();
				for(int i = 0; i < rowCells; i++)
				{
					Element currentCell = elementParent.getElement(i);
					if(editor.getCaretPosition() >= currentCell.getStartOffset() && 
					    editor.getCaretPosition() <= currentCell.getEndOffset())
					{
						cellOffset = i;
					}
				}
				elementParent = elementParent.getParentElement();
			}
			else
			{
				elementParent = elementParent.getParentElement();
			}
		}
		
		if(startPoint > -1 && rowCount > -1)
		{
			editor.setCaretPosition(startPoint);
			String sCell = "<td><p></p></td>";
			ActionEvent actionEvent = new ActionEvent(editor, 0, "insertTableCell");
			for(int i = 0; i < rowCount; i++)
			{
				Element row = elementParent.getElement(i);
				Element whichCell = row.getElement(cellOffset);
				editor.setCaretPosition(whichCell.getStartOffset());
				new HTMLEditorKit.InsertHTMLTextAction(
				    "insertTableCell", sCell, HTML.Tag.TR, HTML.Tag.TD, 
				    HTML.Tag.TH, HTML.Tag.TD).actionPerformed(actionEvent);
			}
			//refreshOnUpdate();
			editor.setCaretPosition(caretPos);
		}
	}

	/** Method for deleting a column from an HTML Table
	  */
	private void deleteTableColumn()
	throws BadLocationException
	{
		HTMLDocument htmlDoc = getDocument();
		if(htmlDoc == null)
		    return;
	    
	    int caretPos = editor.getCaretPosition();
		Element	element       = htmlDoc.getCharacterElement(editor.getCaretPosition());
		Element elementParent = element.getParentElement();
		Element	elementCell   = (Element)null;
		Element	elementRow    = (Element)null;
		Element	elementTable  = (Element)null;
		// Locate the table, row, and cell location of the cursor
		while(elementParent != null && !elementParent.getName().equals("body"))
		{
			if(elementParent.getName().equals("td"))
			{
				elementCell = elementParent;
			}
			else if(elementParent.getName().equals("tr"))
			{
				elementRow = elementParent;
			}
			else if(elementParent.getName().equals("table"))
			{
				elementTable = elementParent;
			}
			elementParent = elementParent.getParentElement();
		}
		int whichColumn = -1;
		if(elementCell != null && elementRow != null && elementTable != null)
		{
			// Find the column the cursor is in
			for(int i = 0; i < elementRow.getElementCount(); i++)
			{
				if(elementCell == elementRow.getElement(i))
				{
					whichColumn = i;
				}
			}
			if(whichColumn > -1)
			{				
			    // Iterate through the table rows, deleting cells from the indicated column
				for(int i = 0; i < elementTable.getElementCount(); i++)
				{
					elementRow  = elementTable.getElement(i);
					elementCell = (elementRow.getElementCount() > whichColumn ? elementRow.getElement(whichColumn) : elementRow.getElement(elementRow.getElementCount() - 1));
					int columnCellStart = elementCell.getStartOffset();
					int columnCellEnd   = elementCell.getEndOffset();
					htmlDoc.remove(columnCellStart, columnCellEnd - columnCellStart);
				}
				//editor.setDocument(htmlDoc);
				//registerDocument(htmlDoc);
	 			//refreshOnUpdate();
	 			if(caretPos >= htmlDoc.getLength())
	 			{
	 				caretPos = htmlDoc.getLength() - 1;
	 			}
	 			editor.setCaretPosition(caretPos);
			}
		}
	}
    
    private HTMLDocument getDocument()
    {       
        try
        {            
            return (HTMLDocument)editor.getDocument();
        }
        catch(ClassCastException ex){}
        return null;
    }
    
    private Element getTDElement()
    {
        HTMLDocument doc = getDocument();
        if(doc == null)
            return null;
        Element td = doc.getParagraphElement(editor.getCaretPosition());
        while(td != null)
        {
            if(td.getName().toUpperCase().equals("TD"))
                return td;
            td = td.getParentElement();
        }
        
        return null;
    }
    
    private Element getTRElement()
    {
        HTMLDocument doc = getDocument();
        if(doc == null)
            return null;
        Element tr = doc.getParagraphElement(editor.getCaretPosition());
        while(tr != null)
        {
            if(tr.getName().toUpperCase().equals("TR"))
                return tr;
            tr = tr.getParentElement();
        }
        
        return null;
    }
    
    private String getRowHTML(Element tr)
    {
        String trTag = "<tr>";
        if(tr.getName().toUpperCase().equals("TR"))
        {       
            for(int i = 0; i < tr.getElementCount(); i++)
                if (tr.getElement(i).getName().toUpperCase().equals("TD"))
                    trTag += "<td><p></p></td>";
        }
		trTag += "</tr>";
		return trTag;
    }
    
    private boolean isInTD()
    {
        return getTDElement() != null;
    }
    
	public void update() 
	{	    
	    setEnabled(isInTD());
	}
}
