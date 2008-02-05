/*
 * Created on May 4, 2004
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

import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.thingamablog.feed.Feed;
import net.sf.thingamablog.feed.FeedFolder;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class FeedTreeModel implements TreeModel
{
	private FeedFolder rootFolder;
	private Vector treeModelListeners = new Vector(2, 2);
	private boolean isFoldersOnly;
    
	public FeedTreeModel(FeedFolder root, boolean foldersOnly)
	{
		rootFolder = root;
		isFoldersOnly = foldersOnly;
	}   
   
    public FeedTreeModel(FeedFolder root)
    {
    	rootFolder = root;
    	isFoldersOnly = false;
    }
    
    public boolean isFoldersOnly()
    {
    	return isFoldersOnly;
    }
    
    public void refresh()
    {
    	fireTreeStructureChanged(rootFolder);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot()
    {        
        return rootFolder;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index)
    {
        FeedFolder f = (FeedFolder)parent;
        Object o[] = null;        
        if(isFoldersOnly)
        	o = f.getFolders();
        else
        	o = f.getContents();
        return o[index];       
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent)
    {
        if(parent instanceof FeedFolder)
        {
			FeedFolder f = (FeedFolder)parent;
			Object o[] = null;
			if(isFoldersOnly)
				o = f.getFolders();
			else
				o = f.getContents();
			return o.length;
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node)
    {
        if(node instanceof Feed)
        	return true;
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
     */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
       
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
     */
    public int getIndexOfChild(Object parent, Object child)
    {
        if(parent instanceof FeedFolder)
        {
			FeedFolder f = (FeedFolder)parent;
			Object o[] = null;
			if(isFoldersOnly)
				o = f.getFolders();
			else
				o = f.getContents();
			
			for(int i = 0; i < o.length; i++)
				if(child == o[i])
					return i;
        }
        return 0;
    }

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener l)
	{
		treeModelListeners.addElement(l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener l)
	{
		treeModelListeners.removeElement(l);
	}
    
	protected void fireTreeStructureChanged(Object oldRoot) 
	{
		int len = treeModelListeners.size();
		TreeModelEvent e = new TreeModelEvent(this, new Object[] {oldRoot});
		for (int i = 0; i < len; i++) 
		{
			((TreeModelListener)treeModelListeners.elementAt(i)).
					treeStructureChanged(e);
		}
	}

}
