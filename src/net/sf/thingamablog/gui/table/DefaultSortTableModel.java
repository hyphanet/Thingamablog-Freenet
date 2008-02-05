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

import java.util.Collections;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;


public class DefaultSortTableModel extends DefaultTableModel implements SortTableModel
{
    private int sortedColumn = 0;
    private boolean isSortedColumnAscending;
    
    public DefaultSortTableModel(){}
  
    public DefaultSortTableModel(int rows, int cols)
    {
        super(rows, cols);
    }
  
    public DefaultSortTableModel(Object[][] data, Object[] names)
    {
        super(data, names);
    }
  
    public DefaultSortTableModel(Object[] names, int rows)
    {
        super(names, rows);
    }
  
    public DefaultSortTableModel(Vector names, int rows)
    {
        super(names, rows);
    }
  
    public DefaultSortTableModel(Vector data, Vector names)
    {
        super(data, names);
    }
  
    public boolean isSortable(int col)
    {
        return true;
    }
  
    public void sortColumn(int col, boolean asc)
    {
        try
        {
        	Collections.sort(getDataVector(), new ColumnComparator(col, asc));
        	sortedColumn = col;
        	isSortedColumnAscending = asc;
           	
        }//dataVector contains elements not mutally comparable
        catch(ClassCastException ex) 
        {
        	System.out.println("Elements not mutually comparable " + ex);
        }
    }
    /**
     * @return
     */
    public boolean isSortedColumnAscending()
    {
        return isSortedColumnAscending;
    }

    /**
     * @return
     */
    public int getSortedColumn()
    {
        return sortedColumn;
    }
}

