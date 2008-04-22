package net.sf.thingamablog.gui.properties;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import thingamablog.l10n.i18n;



/*
 * Created on Jun 29, 2004
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

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class EditableList extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JList list;
	private JButton addButton;
	private JButton removeButton;
	private JButton editButton;	
	private Vector data = new Vector();
	private EditableListModel model;
	
	public EditableList(EditableListModel m)
	{
		list = new JList();
		if(m == null)
			model = new EmptyModel();
		else
			model = m;
			
		ActionListener listener = new EditHandler();
		
		addButton = new JButton(i18n.str("add_")); //$NON-NLS-1$
		addButton.addActionListener(listener);
		removeButton = new JButton(i18n.str("remove")); //$NON-NLS-1$
		removeButton.addActionListener(listener);
		editButton = new JButton(i18n.str("edit_")); //$NON-NLS-1$
		editButton.addActionListener(listener);
				
		setLayout(new BorderLayout(5, 5));
		JPanel bPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		bPanel.add(addButton);
		bPanel.add(removeButton);
		bPanel.add(editButton);
		JPanel left = new JPanel();
		left.add(bPanel);
		add(new JScrollPane(list), BorderLayout.CENTER); 
		add(left, BorderLayout.WEST);
	}
	
	public void setListData(Vector v)
	{
		data = v;
		list.setListData(data);
	}
	
	public void setListData(Object o[])
	{
		data.removeAllElements();
		for(int i = 0; i < o.length; i++)
		{
			data.add(o[i]);
		}
		list.setListData(data);
	}
	
	public Vector getListData()
	{
		return data;
	}
	
	private class EditHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == addButton)
			{			
				Object o = model.add(EditableList.this);
				if(o == null)
					return;					
				                  
				if(!data.contains(o))
					data.add(o);
				list.setListData(data);
				list.setSelectedValue(o, true);
				
			}
			else if(e.getSource() == removeButton)
			{
				if(list.isSelectionEmpty())
					return;
				               	
				Object o = list.getSelectedValue();					
				if(!model.shouldRemove(o, EditableList.this))
					return;
			
				data.remove(o);
				list.setListData(data);
				list.setSelectedValue(o, true);	
			
			}
			else if(e.getSource() == editButton)
			{
				if(list.isSelectionEmpty())
					return;
				
				Object old = list.getSelectedValue();                
				Object _new = model.edit(old, EditableList.this);
				if(_new == null)
					return;
				
				int index = data.indexOf(old);
				if(index < 0)
					return;				                    
				
				data.setElementAt(_new, index);
				list.setListData(data);
				list.setSelectedValue(_new, true);			
			}
		}
	}
	
	private class EmptyModel implements EditableListModel
	{
		public Object add(EditableList c)
		{
			return null;
		}
		
		public boolean shouldRemove(Object o, EditableList c)
		{
			return false;
		}
		
		public Object edit(Object o, EditableList c)
		{
			return null;
		}
	}
}
