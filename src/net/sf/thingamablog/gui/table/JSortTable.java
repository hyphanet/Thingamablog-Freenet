/*
 * Copyright (C) 2003  Bob Tantlinger
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */


package net.sf.thingamablog.gui.table;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public class JSortTable extends JTable implements MouseListener
{
    protected int sortedColumnIndex = -1;
    protected boolean sortedColumnAscending = true;
  
    public JSortTable()
    {
        this(new DefaultSortTableModel());
    }
  
    public JSortTable(int rows, int cols)
    {
        this(new DefaultSortTableModel(rows, cols));
    }
  
    public JSortTable(Object[][] data, Object[] names)
    {
        this(new DefaultSortTableModel(data, names));
    }
  
    public JSortTable(Vector data, Vector names)
    {
        this(new DefaultSortTableModel(data, names));
    }
  
    public JSortTable(SortTableModel model)
    {
        super(model);
        initSortHeader();
        sort(0, true);
    }

    public JSortTable(SortTableModel model, TableColumnModel colModel)
    {
        super(model, colModel);
        initSortHeader();
    }

    public JSortTable(SortTableModel model, 
    TableColumnModel colModel, ListSelectionModel selModel)
    {
        super(model, colModel, selModel);
        initSortHeader();
    }

    protected void initSortHeader()
    {
        JTableHeader header = getTableHeader();
        header.setDefaultRenderer(new SortHeaderRenderer());
        header.addMouseListener(this);
    }

    public int getSortedColumnIndex()
    {
        return sortedColumnIndex;
    }
  
    public boolean isSortedColumnAscending()
    {
        return sortedColumnAscending;
    }
    
    /*public void mouseReleased(MouseEvent event)
    {
        TableColumnModel colModel = getColumnModel();
        int index = colModel.getColumnIndexAtX(event.getX());
        int modelIndex = colModel.getColumn(index).getModelIndex();
    
        SortTableModel model = (SortTableModel)getModel();
        if(model.isSortable(modelIndex))
        {
            // toggle ascension, if already sorted
            if(sortedColumnIndex == index)
            {
                sortedColumnAscending = !sortedColumnAscending;
            }
            sortedColumnIndex = index;            
            
            int cols = getColumnCount();
            
    		Vector sel = null;
    		int r = getSelectedRow();
    		if(r != -1)
    		{
    			sel = new Vector();
    			for(int i = 0; i < cols; i++)
    				sel.add(getValueAt(r, i));	
    		}    	
    	
    		clearSelection();
    		
    		model.sortColumn(modelIndex, sortedColumnAscending);
    	
    		if(sel == null)
    			return;
    	
    		for(int i = 0; i < getRowCount(); i++)
    		{
    			Vector h = new Vector();
    			for(int c = 0; c < cols; c++)
    				h.add(getValueAt(i, c));
    			
    			if(h.equals(sel))
    			{
    				setRowSelectionInterval(i, i);
    				Rectangle rect = getCellRect(i, 0, true);
        			scrollRectToVisible(rect);
    				break;	
    			}
    		}    		
        }
    } */
    
    
    public void sort(int index, boolean ascend)
    {
        TableColumnModel colModel = getColumnModel();
        TableColumn tCol = null;        
        try
        {        
        	tCol = colModel.getColumn(index);
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {
        	return;      
        }
        int modelIndex = tCol.getModelIndex();
    
        SortTableModel model = (SortTableModel)getModel();
        if(model.isSortable(modelIndex))
        {

            sortedColumnAscending = ascend;
            sortedColumnIndex = index;            
            
            int cols = getColumnCount();
            
    		Vector sel = null;
    		int r = getSelectedRow();
    		if(r != -1)
    		{
    			sel = new Vector();
    			for(int i = 0; i < cols; i++)
    				sel.add(getValueAt(r, i));	
    		}    	
    	
    		clearSelection();
    		
    		model.sortColumn(modelIndex, sortedColumnAscending);
    	
    		if(sel == null)
    			return;
    	
    		for(int i = 0; i < getRowCount(); i++)
    		{
    			Vector h = new Vector();
    			for(int c = 0; c < cols; c++)
    				h.add(getValueAt(i, c));
    			
    			if(h.equals(sel))
    			{
    				setRowSelectionInterval(i, i);
    				Rectangle rect = getCellRect(i, 0, true);
        			scrollRectToVisible(rect);
    				break;	
    			}
    		}    		
        }	
    }
  
    public void mouseReleased(MouseEvent event)
    {
        TableColumnModel colModel = getColumnModel();
        int index = colModel.getColumnIndexAtX(event.getX());
        if(index < 0)
        	return;
        boolean asc = sortedColumnAscending;
        // toggle ascension
        if(sortedColumnIndex == index)
        {
        	asc = !sortedColumnAscending;
        }
        sort(index, asc);
    }
  
    public void mousePressed(MouseEvent event){}
    public void mouseClicked(MouseEvent event){}
    public void mouseEntered(MouseEvent event){}
    public void mouseExited(MouseEvent event){}
}

