/*
 * Created on May 20, 2004
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
package net.sf.thingamablog.gui.properties;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;

import net.sf.thingamablog.blog.Weblog;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class AuthorsPanel extends PropertyPanel
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Weblog weblog;
	private WeblogEditableListModel wModel;	

    
    public AuthorsPanel(Weblog wb)
    {
    	weblog = wb;
    	wModel = new WeblogEditableListModel(WeblogEditableListModel.AUTHORS);
    	EditableList eList = new EditableList(wModel);
    	eList.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    	
    	try
    	{
    		eList.setListData(weblog.getAuthors());
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
		setLayout(new BorderLayout(5, 5));
		add(eList, BorderLayout.CENTER);
    	
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {       
		try
		{		
			wModel.syncListWithWeblog(weblog);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
    }
    
	public boolean isValidData()
	{
		return true;
	}
}
