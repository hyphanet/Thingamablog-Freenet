/*
 * Created on Jun 23, 2004
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import net.sf.thingamablog.TBGlobals;

/**
 * @author Bob Tantlinger
 *
 * TableColumnModel which stores and restores column widths for
 *  known Thingamablog TableModels
 */
public class TBTableColumnModel extends DefaultTableColumnModel
{
    private Properties colWidths = new Properties();
    private PropertyChangeListener resizeHandler = new ColumnResizeHandler();
    
    public static final String COL_FILE = TBGlobals.PROP_DIR + TBGlobals.SEP + "col.properties";

    /**
     * 
     * Initialize the model with default column widths
     */
    public TBTableColumnModel()
    {
        super();

        //set the default sizes of the columns we're looking for
        colWidths.put(WeblogTableModel.TITLE, 200 + "");
        colWidths.put(WeblogTableModel.POST_DATE, 125 + "");
        colWidths.put(WeblogTableModel.AUTHOR, 75 + "");
        colWidths.put(WeblogTableModel.ID, 40 + "");
        colWidths.put(WeblogTableModel.MODIFIED, 125 + "");

        colWidths.put(FeedTableModel.READ, 30 + "");
        colWidths.put(FeedTableModel.ITEM, 325 + "");
        colWidths.put(FeedTableModel.DATE, 125 + "");

        addColumnModelListener(new ColumnSizeHandler());
    }
    
    public void saveColumnData()
    {
    	try
    	{			
			FileOutputStream fos = new FileOutputStream(COL_FILE);
			colWidths.store(fos, "Column data");
			fos.close();			
    	}
    	catch(IOException ioe){}
    }
    
    public void loadColumnData()
    {
    	try
    	{
			FileInputStream fis = new FileInputStream(COL_FILE);
			colWidths.load(fis);
			fis.close();    		
    	}
    	catch(IOException ioe)
    	{
    	}
    }
	
	/**
	Restores saved column widths when new columns are added
	*/
    private class ColumnSizeHandler implements TableColumnModelListener
    {

        public void columnAdded(TableColumnModelEvent e)
        {
            //System.out.println("Col added " + e.getSource());
            TableColumn col = getColumn(e.getToIndex());
            try
            {            
           		int width = Integer.parseInt(
           			colWidths.getProperty(col.getHeaderValue().toString()));
            
                col.setPreferredWidth(width);
                col.addPropertyChangeListener(resizeHandler);
                //col.sizeWidthToFit();
            }
            catch(Exception ex){}
            

            //System.out.println("new Col width " + width);
        }

        public void columnRemoved(TableColumnModelEvent e)
        {
            //System.out.println("Col Removed");
        }

        public void columnMoved(TableColumnModelEvent e)
        {
            //System.out.println("Col Moved");
        }

        public void columnMarginChanged(ChangeEvent e)
        {

        }

        public void columnSelectionChanged(ListSelectionEvent e)
        {}
    }

	/**
	Listens for TableColumn resizes from the user and updates the new width
	*/
    private class ColumnResizeHandler implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if(evt.getPropertyName().equals("preferredWidth") && 
            evt.getSource() instanceof TableColumn)
            {
                //System.out.println("Col Resized " + evt.getSource());
                TableColumn col = (TableColumn)evt.getSource();
                if(colWidths.containsKey(col.getHeaderValue().toString()))
                {
                	colWidths.put(col.getHeaderValue(), 
                		col.getPreferredWidth() + "");
                }                
            }
        }
    }
}
