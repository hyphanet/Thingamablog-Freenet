package net.sf.thingamablog.gui.app;

import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.table.DefaultSortTableModel;


public class WeblogTableModel extends DefaultSortTableModel
{
	public static final Object TITLE = Messages.getString("WeblogTableModel.Title"); //$NON-NLS-1$
	public static final Object POST_DATE = Messages.getString("WeblogTableModel.Date_Posted"); //$NON-NLS-1$
	public static final Object AUTHOR = Messages.getString("WeblogTableModel.Author"); //$NON-NLS-1$
	public static final Object ID = Messages.getString("WeblogTableModel.ID"); //$NON-NLS-1$
	public static final Object MODIFIED = Messages.getString("WeblogTableModel.Modified"); //$NON-NLS-1$
	
	public final static Object COLS[] = {
	TITLE, POST_DATE, AUTHOR, ID, MODIFIED};
    
	public final static int TITLE_COL = 0, DATE_COL = 1, AUTHOR_COL = 2;
	public final static int ID_COL = 3, MODIFIED_COL = 4;
    
	public WeblogTableModel()
	{
		super(COLS, 0);
	}
	
	/**
	 * Overridden from superclas to always return false.
	 * We dont want editable tables
	 */
	public boolean isCellEditable(int row, int col)
	{
		return false; //we don't want editable tables
	}
	
	/**
	 * Sets the BlogEntryHeaderData for this model.
	 * This Method should be used to populate the model
	 * @param hd - an array of BlogEntryHeaderData
	 */
	public void setBlogEntries(BlogEntry entries[])
	{	
		setRowCount(entries.length); //clear the table
		for(int r = 0; r < entries.length; r++)
		{
			setValueAt(entries[r].getTitle(), r, TITLE_COL);
			setValueAt(entries[r].getDate(), r, DATE_COL);
			setValueAt(new Long(entries[r].getID()), r, ID_COL);
			setValueAt(entries[r].getAuthor(), r, AUTHOR_COL);
			setValueAt(entries[r].getLastModified(), r, MODIFIED_COL);
		}
		
		if(entries.length == 0)
			fireTableDataChanged();
	}
	
	public boolean isSortable(int col)
	{
		//the modified column shouldn't be sorted because 
		//it will likely contain a lot of null dates
		return col != MODIFIED_COL;
	}
	
	/**
	 * returns the entry ID at the row specified
	 * @param r the table row
	 * @return the Entry ID
	 */
	public long getEntryIDAtRow(int r)
	{
		Long id = (Long)getValueAt(r, ID_COL);
		return id.longValue();
	}
}