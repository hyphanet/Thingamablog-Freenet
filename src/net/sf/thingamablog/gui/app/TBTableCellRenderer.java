/*
 * Created on May 1, 2004
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.feed.FeedItem;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBTableCellRenderer extends JLabel implements TableCellRenderer
{
	private DateFormat df = 
		DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		
	private Date expireDate = null;
		
	private Font plainFont = new Font("Dialog", Font.PLAIN, 12);
	private Font boldFont = new Font("Dialog", Font.BOLD, 12);
	private ImageIcon unreadItemIcon = Utils.createIcon(TBGlobals.RESOURCES + "unread_item.gif");
	private ImageIcon readItemIcon = Utils.createIcon(TBGlobals.RESOURCES + "read_item.gif");
	private ImageIcon postIcon = Utils.createIcon(TBGlobals.RESOURCES + "entry.png");
	private ImageIcon uPostIcon = Utils.createIcon(TBGlobals.RESOURCES + "updated_entry.png");
	
	public void setExpireDate(Date d)
	{
		expireDate = d;
	}
	
	public Date getExpireDate()
	{
		return expireDate;
	}
	
	public Component getTableCellRendererComponent(
	JTable table, Object value, boolean isSelected,
	boolean hasFocus, int row, int column)
	{
		setOpaque(true);
		setFont(plainFont);
		//setBorder(new EmptyBorder(10, 10, 10, 6));
		
		if(isSelected)
		{
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		}
		else
		{
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
			
		if(table.getModel() instanceof FeedTableModel)
		{					
			FeedTableModel rsstm = (FeedTableModel)table.getModel();
			TableColumn col = table.getColumnModel().getColumn(column);					
			if(!rsstm.isItemAtRowRead(row))
				setFont(boldFont);
			if(col.getHeaderValue() == FeedTableModel.READ)
			{			
				if(rsstm.isItemAtRowRead(row))
					setIcon(readItemIcon);
				else
					setIcon(unreadItemIcon);
			}
			else
				setIcon(null);
				
		}
		else if(table.getModel() instanceof WeblogTableModel)
		{
			WeblogTableModel btm = (WeblogTableModel)table.getModel();
			if(column == 0)
			{
				//is the modified column on the TableModel null?
				if(btm.getValueAt(row, WeblogTableModel.MODIFIED_COL) == null)
					setIcon(postIcon);
				else
					setIcon(uPostIcon);
			}
			else
				setIcon(null);
		}
			
		if(value instanceof FeedItem)
		{
			setText(((FeedItem)value).getTitle());
			return this;
		}
		else if(value instanceof Boolean)
		{
			setText("");
			setHorizontalAlignment(SwingConstants.CENTER);
			return this;
		}
		else if(value instanceof Integer)
		{
			Integer val = (Integer)value;
			setHorizontalAlignment(SwingConstants.CENTER);
			setText(val.intValue() + "");
			return this;
		}
		else if(value instanceof Date)
		{                
			Date d = (Date)value;
			setText(df.format(d));
			if((table.getModel() instanceof WeblogTableModel) && 
			(column == WeblogTableModel.DATE_COL))
			{
				if(expireDate != null && d.before(expireDate))
					setForeground(Color.red);	
			} 
                                	
			return this;
		}
            
		setHorizontalAlignment(SwingConstants.LEFT);    		
		//setToolTipText(value.toString());
		if(value == null)
			setText("");
		else
			setText(value.toString());
		return this;
	}
}
