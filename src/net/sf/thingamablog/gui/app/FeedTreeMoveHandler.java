/*
 * Created on May 5, 2004
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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.feed.Feed;
import net.sf.thingamablog.feed.FeedFolder;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class FeedTreeMoveHandler extends MouseAdapter implements MouseMotionListener
{
	private Cursor moveCursor;
	private Cursor invalidCursor;
	private boolean isMoving;
	private boolean canMove;
	private JFrame frame;
	private JTree tree;
	private FeedTreeModel model;
	private boolean isEnabled = true;
		
	public FeedTreeMoveHandler(JTree feedTree, JFrame frame)
	{
		this.frame = frame;
		feedTree.addMouseListener(this);
		feedTree.addMouseMotionListener(this);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		ImageIcon img = Utils.createIcon(
			TBGlobals.RESOURCES + "move_cur.gif");
		moveCursor = toolkit.createCustomCursor(
			img.getImage() , new Point(0,0), "move");
			
		img = Utils.createIcon(TBGlobals.RESOURCES + "invalid_cur.gif");
		invalidCursor = toolkit.createCustomCursor(
						img.getImage() , new Point(0,0), "invalid");
				
	}
	
	public void setEnabled(boolean enabled)
	{
		isEnabled = enabled;
	}
	
	public boolean isEnabled()
	{
		return isEnabled;
	}
		
	public void mousePressed(MouseEvent e)
	{
		if(!initTree(e))
			return;
		canMove = (tree.getRowForLocation(e.getX(), e.getY()) > 0);				
	}
		
	public void mouseReleased(MouseEvent e)
	{			
		if(!initTree(e))
			return;
			
		int row = tree.getRowForLocation(e.getX(), e.getY());
		if(row > -1)
		{
			Object root = tree.getModel().getRoot();
			Object source = tree.getLastSelectedPathComponent();
			Object dest = tree.getPathForRow(row).getLastPathComponent();
			TreePath selPath = tree.getSelectionPath();
			if(isMoving && source != null && source != root && 
			dest instanceof FeedFolder)
			{				
				FeedFolder destFolder = (FeedFolder)dest;									
				if(source instanceof FeedFolder && source != destFolder)
				{
					FeedFolder sourceFolder = (FeedFolder)source;
					if(!sourceFolder.containsFolder(destFolder))
					{
						FeedFolder parent = sourceFolder.getParent();
						parent.removeFolder(sourceFolder);
						destFolder.addFolder(sourceFolder);
						refreshTree();
					}
				}
				else if(source instanceof Feed)
				{
					TreePath pSelPath = selPath.getParentPath();
					if(pSelPath.getLastPathComponent() instanceof FeedFolder)
					{
						FeedFolder parent = (FeedFolder)pSelPath.getLastPathComponent();
						parent.removeFeed((Feed)source);
						destFolder.addFeed((Feed)source);
						refreshTree();
					}
				}				
				
				//System.out.println("Drag Stopped");
				//System.out.println("Row = " +tree.getRowForLocation(e.getX(), e.getY()));
			}
		}
		isMoving = false;
		canMove = false;
		frame.setCursor(Cursor.getDefaultCursor());
	}
		
	public void mouseDragged(MouseEvent e)
	{
		if(!initTree(e))
			return;
		//System.out.println("Drag");
		if(tree.getLastSelectedPathComponent() != null && canMove)
		{
			isMoving = true;
			int row = tree.getRowForLocation(e.getX(), e.getY());
			if(row > -1)
			{
				Object o = tree.getPathForRow(row).getLastPathComponent();
				if(o instanceof FeedFolder)
					frame.setCursor(moveCursor);
				else
					frame.setCursor(invalidCursor);
			}
			else
				frame.setCursor(invalidCursor);
		}	
	}	
		
	public void mouseMoved(MouseEvent e){}
	
	private boolean initTree(MouseEvent e)
	{
		if(e.getSource() instanceof JTree && isEnabled)
		{
			tree = (JTree)e.getSource();
			if(tree.getModel() instanceof FeedTreeModel)
			{
				model = (FeedTreeModel)tree.getModel();
				return true;
			}	
		}
		
		return false;
	}
	
	private void refreshTree()
	{
		Enumeration eEnum = tree.getExpandedDescendants(
			new TreePath(tree.getModel().getRoot()));
		model.refresh();
		while(eEnum.hasMoreElements())
		{
			tree.expandPath((TreePath)eEnum.nextElement());	
		}			
	}
}
