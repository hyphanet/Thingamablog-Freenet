/*
 * Created on May 30, 2004
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.sf.thingamablog.blog.PingService;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogsDotComPing;
import net.sf.thingamablog.gui.LabelledItemPanel;
import thingamablog.l10n.i18n;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class XmlRpcPingPanel extends PropertyPanel
{	
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Weblog weblog;
   	private PingerTableModel model;
   
   	public XmlRpcPingPanel(Weblog wb)
   	{
   		weblog = wb;
		
		String col1String = i18n.str("ping"); //$NON-NLS-1$
		model = new PingerTableModel(col1String, i18n.str("services")); //$NON-NLS-1$
				
		final JTable table = new JTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model.setPingServices(weblog.getPingServices());
		TableColumn tcol = table.getColumn(col1String);
		tcol.setPreferredWidth(55);
		tcol.setMaxWidth(55);
		tcol.setResizable(false);        
        
		final JButton addButton = new JButton(i18n.str("add_")); //$NON-NLS-1$
		final JButton removeButton = new JButton(i18n.str("remove")); //$NON-NLS-1$
		final JButton editButton = new JButton(i18n.str("edit_")); //$NON-NLS-1$
		final JTextField aNameField = new JTextField(10);
		final JTextField aUrlField = new JTextField(10);
		final LabelledItemPanel lip = new LabelledItemPanel();
		lip.addItem(i18n.str("service_name"), aNameField); //$NON-NLS-1$
		lip.addItem(i18n.str("ping_url"), aUrlField); //$NON-NLS-1$
		ActionListener authorListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(e.getSource() == addButton)
				{
					aNameField.setText(""); //$NON-NLS-1$
					aUrlField.setText(""); //$NON-NLS-1$
					int r = JOptionPane.showConfirmDialog(
						XmlRpcPingPanel.this, lip, i18n.str("add_ping_service"), //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    	                    
					if(r == JOptionPane.OK_OPTION)
					{
						WeblogsDotComPing ps = new WeblogsDotComPing();
						ps.setServiceName(aNameField.getText());
						ps.setServiceUrl(aUrlField.getText());						
						model.addPingService(ps);
						table.revalidate();	
					}
				}
				else if(e.getSource() == removeButton)
				{
					PingService  ps = null;
					try
					{                    
						ps = model.getPingServiceAtRow(table.getSelectedRow());
						int r = JOptionPane.showConfirmDialog(XmlRpcPingPanel.this,
							i18n.str("remove_service") + " : "+  ps.getServiceName(), i18n.str("confirm"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(r == JOptionPane.NO_OPTION)
							return;                    		 
					}
					catch(Exception ex)
					{
						return;
					}                   
                    
					try
					{
						model.removePingServiceAtRow(table.getSelectedRow());						
						table.revalidate();                   	
					}
					catch(Exception ex)
					{
						System.out.println("Unable to remove service");	 //$NON-NLS-1$
					}                   
                                     	
				}
				else if(e.getSource() == editButton)
				{                    
					PingService xps = null;                    
					try
					{
						xps = model.getPingServiceAtRow(table.getSelectedRow());	                    	                    
						aNameField.setText(xps.getServiceName());
						aUrlField.setText(xps.getServiceUrl());
						int r = JOptionPane.showConfirmDialog(
						XmlRpcPingPanel.this, lip, i18n.str("edit_service"), //$NON-NLS-1$
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); 
	                    
						if(r == JOptionPane.CANCEL_OPTION)
							return;
					}
					catch(Exception ex)
					{
						return;	
					}                  
                    	
					xps.setServiceName(aNameField.getText());
					xps.setServiceUrl(aUrlField.getText());
					//model.update();
					table.repaint(); 	
   
				}
			} 
		};
		addButton.addActionListener(authorListener);
		removeButton.addActionListener(authorListener);
		editButton.addActionListener(authorListener);
        
		JPanel bPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		bPanel.add(addButton);
		bPanel.add(removeButton);
		bPanel.add(editButton);
		JPanel left = new JPanel();
		left.add(bPanel);
		
		setLayout(new BorderLayout(5, 5));
		add(new JScrollPane(table), BorderLayout.CENTER); 
		add(left, BorderLayout.WEST);
		add(new JPanel(), BorderLayout.SOUTH);
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));        
		
   	}
   
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {
		PingService ps[] = weblog.getPingServices();
		for(int i = 0; i < ps.length; i++)
			weblog.removePingService(ps[i]);
		
		for(int i = 0; i < model.getRowCount(); i++)
			weblog.addPingService(model.getPingServiceAtRow(i));
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#isValidData()
     */
    public boolean isValidData()
    {        
        return true;
    }
    
	/* TableModel for the Ping table */
	private class PingerTableModel implements TableModel
	{
		String COLS[] = {i18n.str("ping"), i18n.str("service")}; //$NON-NLS-1$ //$NON-NLS-2$
		private Vector rowData = new Vector();
		public PingerTableModel(String col1, String col2)
		{
			COLS[0] = col1;
			COLS[1] = col2;	
		}
		
		public int getRowCount()
		{
			return rowData.size();	
		}
		
		public int getColumnCount()
		{
			return COLS.length;
		}
		
		public String getColumnName(int columnIndex)
		{
			return COLS[columnIndex];	
		}
		
		public Class getColumnClass(int columnIndex)
		{
			if(columnIndex == 0)
				return Boolean.class;
			else
				return Object.class;	
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			if(columnIndex == 0)
				return true;
			
			return false;	
		}
		
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Vector v = (Vector)rowData.elementAt(rowIndex);
			return v.elementAt(columnIndex);
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			Vector v = (Vector)rowData.elementAt(rowIndex);
			v.set(columnIndex, aValue);
			if(columnIndex == 0 && aValue instanceof Boolean)
			{
				Boolean b = (Boolean)aValue;
				PingService p = (PingService)v.elementAt(1);
				p.setEnabled(b.booleanValue());				
			}							
		}
		
		public void addTableModelListener(TableModelListener l){}
		public void removeTableModelListener(TableModelListener l){}
		
		public PingService getPingServiceAtRow(int r) throws IndexOutOfBoundsException
		{
			Vector v = (Vector)rowData.elementAt(r);
			return (PingService)v.elementAt(1);			
		}
		
		public void setPingServices(PingService ps[])
		{			
			rowData.removeAllElements();
			for(int i = 0; i < ps.length; i++)
			{
				addPingService(ps[i]);
			}			
		}
		
		public void addPingService(PingService ps)
		{
			Vector v = new Vector();
			v.add(new Boolean(ps.isEnabled()));
			v.add(ps);
			rowData.add(v);
		}
		
		public void removePingServiceAtRow(int row)
		{
			rowData.removeElementAt(row);
		}
	}
}
