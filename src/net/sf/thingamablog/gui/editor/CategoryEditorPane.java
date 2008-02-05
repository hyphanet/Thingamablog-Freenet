/*
 * Created on Jul 7, 2004
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
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import net.sf.thingamablog.blog.BlogEntry;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class CategoryEditorPane extends JPanel implements Scrollable
{
	private JList blogCatList;
	private JList entryCatList;
	private JButton addButton;
	private JButton removeButton;
	
	private Vector blogCats = new Vector();
	//private String ecats[];
	private JPanel listPanel;
	
	private Font listFont = new Font("Dialog", Font.PLAIN, 12);
	
	
	public CategoryEditorPane()
	{	 	
    	
		listPanel = new JPanel();
    	listPanel.setBackground(Color.WHITE);
    	//setCategories(bcats, be);
    	
		JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mainPanel.add(listPanel);
		mainPanel.setBackground(Color.WHITE);
		
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		setBackground(Color.WHITE);
		
		//setPreferredSize(new Dimension(125, 10));		  		
	}
    
	private boolean contains(BlogEntry be, String c)
	{
		String ecats[] = be.getCategories();
		if(ecats == null)
			ecats = new String[0];
		
		for(int i = 0; i < ecats.length; i++)
			if(ecats[i].equals(c))
				return true;
    			
		return false;	
	}
	
	public void setCategories(String bcats[], BlogEntry be)
	{
		listPanel.removeAll();
		blogCats.removeAllElements();
		listPanel.setLayout(new GridLayout(bcats.length, 1));
		for(int i = 0; i < bcats.length; i++)
		{
			JCheckBox cb = new JCheckBox(bcats[i]);
			cb.setFont(listFont);
			cb.setForeground(Color.BLACK);
			cb.setOpaque(false);
			cb.setSelected(contains(be, bcats[i]));
			listPanel.add(cb);
			blogCats.add(cb);	
		}
		
		repaint();
	}
    
	public String[] getSelectedCategories()
	{
		Vector sel = new Vector();
		for(int i = 0; i < blogCats.size(); i++)
		{
			JCheckBox cb = (JCheckBox)blogCats.elementAt(i);
			if(cb.isSelected())
				sel.add(cb.getText());	
		}
    	
		String cats[] = new String[sel.size()];
		for(int i = 0; i < cats.length; i++)
			cats[i] = sel.elementAt(i).toString();
    	
		return cats;
	}
	
	public Dimension getPreferredScrollableViewportSize()
	{
		return getPreferredSize();
	}
	
	public int getScrollableUnitIncrement(Rectangle visibleRect,int o, int d)
	{
		return 5;
	}
	
	public int getScrollableBlockIncrement(Rectangle visibleRect, int o, int d)
	{
		return 5;
	}
	
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}
	
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}
}
