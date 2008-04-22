/*
 * Created on Apr 30, 2004
 *
 * This file is part of Thingamablog. ( http://thingamablog.sf.net )
 *
 * Copyright (c) 2004, Bob Tantlinger All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 * 
 */
package net.sf.thingamablog.gui.app;


import net.sf.thingamablog.feed.FeedItem;
import net.sf.thingamablog.gui.table.DefaultSortTableModel;
import thingamablog.l10n.i18n;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class FeedTableModel extends DefaultSortTableModel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public static final Object READ =  " "; //$NON-NLS-1$
	public static final Object ITEM = i18n.str("item"); //$NON-NLS-1$
	
	public static final Object DATE = i18n.str("date_posted");;
	//public static final Object RETRIEVED = i18n.str("retrieved"); //$NON-NLS-1$
	//public static final Object POSTED = "Posted";
	public final static Object COLS[] =	
	{READ, ITEM, DATE};    
	public final static int  READ_COL = 0, ITEM_COL = 1, DATE_COL = 2;
	
	
	
	/**
	 * Create a new FeedTableModel
	 *
	 */
	public FeedTableModel()
	{
		super(COLS, 0);
	}	
	/**
	 * Always returns false since we dont want editable Rss tables
	 */
	public boolean isCellEditable(int row, int col)
	{
		return false; //we don't want editable tables
	}
	
	/**
	 * Set the Items for this model. 
	 * This method should be used to populate the model
	 * @param items - the items
	 */
	public void setItems(FeedItem items[])
	{
		setRowCount(items.length);//clear the table
		for(int i = 0; i < items.length; i++)
		{
			ItemWrapper iw = new ItemWrapper(
				items[i].getTitle(), items[i].getID(), items[i].isRead());
			setValueAt(new Boolean(iw.isRead()), i, READ_COL);
			setValueAt(iw, i, ITEM_COL);			
			setValueAt(items[i].getPubDate(), i, DATE_COL);
			//setValueAt(items[i].getPubDate(), i, POSTED_COL);
		}
		
		if(items.length == 0)
			fireTableDataChanged();
	}
	/**
	 * Get the item id at row
	 * @param row - the Item's row
	 * @return - The item id at row n
	 */
	public long getItemIDAtRow(int row)
	{
		
		ItemWrapper iw = (ItemWrapper)getValueAt(row, ITEM_COL);
		return iw.getItemID();
	
	}
	
	public boolean isItemAtRowRead(int row)
	{
		ItemWrapper iw = (ItemWrapper)getValueAt(row, ITEM_COL);
		return iw.isRead();
	}
	
	public void setItemAtRowRead(int row, boolean read)
	{
		ItemWrapper iw = (ItemWrapper)getValueAt(row, ITEM_COL);
		iw.setRead(read);
		setValueAt(new Boolean(iw.isRead()), row, READ_COL);
	}
	
	private class ItemWrapper implements Comparable
	{
		private String item;
		private long itemID;
		private boolean isRead;
		
		public ItemWrapper(String i, long id, boolean read)
		{
			item = i;
			itemID = id;
			isRead = read;
		}
		
		public void setRead(boolean read)
		{
			isRead = read;
		}
		
		public boolean isRead()
		{
			return isRead;
		}
		
		public long getItemID()
		{
			return itemID;
		}
		
		public String toString()
		{
			return item;
		}
		
		public int compareTo(Object o)
		{
			return toString().compareToIgnoreCase(o.toString());
		}
	}	
}
