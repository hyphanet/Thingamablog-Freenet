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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogList;
import net.sf.thingamablog.gui.Messages;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class WeblogTreeModel implements TreeModel
{

	public static final String ROOT = Messages.getString("WeblogTreeModel.My_Sites"); //$NON-NLS-1$
	
	public static final String CURRENT = Messages.getString("WeblogTreeModel.Current"); //$NON-NLS-1$
	public static final String DRAFTS = Messages.getString("WeblogTreeModel.Drafts"); //$NON-NLS-1$
	public static final String EXPIRED = Messages.getString("WeblogTreeModel.Expired");	 //$NON-NLS-1$
	public static final String DATED_ARCS = Messages.getString("WeblogTreeModel.Archives"); //$NON-NLS-1$
	public static final String CATS = Messages.getString("WeblogTreeModel.Categories"); //$NON-NLS-1$
	public static final String TEMPLATES = Messages.getString("WeblogTreeModel.Templates"); //$NON-NLS-1$
	public static final String WEB_SITE = Messages.getString("WeblogTreeModel.Web_Files"); //$NON-NLS-1$
	
	public static final String BLOG_CHILDREN[] = 
	{CURRENT, DRAFTS, EXPIRED, DATED_ARCS, CATS, TEMPLATES, WEB_SITE};
	
	private WeblogList blogs;
	private Vector treeModelListeners = new Vector(5, 5);
	private Vector catFolders = new Vector();
	private Vector arcFolders = new Vector();
	private Vector templateFolders = new Vector();
	private Vector webFolders = new Vector();
	
	public WeblogTreeModel(WeblogList list)
	{
		setData(list);	
	}
	
	public void refresh()
	{
		setData(blogs);
	}
	
	public void setData(WeblogList weblogList)
	{
		blogs = weblogList;
		blogs.sortList();	
		
		catFolders.removeAllElements();
		arcFolders.removeAllElements();
		webFolders.removeAllElements();
		templateFolders.removeAllElements();
		for(int i = 0; i < blogs.getWeblogCount(); i++)
		{
			arcFolders.add(new BlogFolder(blogs.getWeblogAt(i), DATED_ARCS));
			catFolders.add(new BlogFolder(blogs.getWeblogAt(i), CATS));
			webFolders.add(new BlogFolder(blogs.getWeblogAt(i), WEB_SITE));
			templateFolders.add(new BlogFolder(blogs.getWeblogAt(i), TEMPLATES));
		}		
		arcFolders.trimToSize();
		catFolders.trimToSize();
		webFolders.trimToSize();
		templateFolders.trimToSize();
		fireTreeStructureChanged(ROOT);    	
	}
	
    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    public Object getRoot()
    {        
        return ROOT;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    public Object getChild(Object parent, int index)
    {
		Object obj = new String("null"); //$NON-NLS-1$
		if(parent == ROOT)
			obj = blogs.getWeblogAt(index);
		else if(parent instanceof Weblog)
		{
			Weblog w = (Weblog)parent;
			Object o[] = new Object[BLOG_CHILDREN.length];
			for(int i = 0; i < blogs.getWeblogCount(); i++)
			{
				if(w == blogs.getWeblogAt(i))
				{
					if(BLOG_CHILDREN[index].equals(CATS))					
						obj = catFolders.elementAt(i);
					else if(BLOG_CHILDREN[index].equals(DATED_ARCS))
						obj = arcFolders.elementAt(i);
					else if(BLOG_CHILDREN[index].equals(TEMPLATES))
						obj = templateFolders.elementAt(i);
					else if(BLOG_CHILDREN[index].equals(WEB_SITE))
						obj = webFolders.elementAt(i);
					else					
						obj = new String(BLOG_CHILDREN[index]);						
				}
			}
		}
		else if(parent instanceof BlogFolder)//must be cats or arcs
		{			
			Object o[] = getBlogFolderContents((BlogFolder)parent);
			obj = o[index];
		}
		else if(parent instanceof File)
		{
			File f = (File)parent;
			Vector v = getDirContents(f);					
			return v.elementAt(index);
		}       
        
        return obj;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    public int getChildCount(Object parent)
    {
		if(parent == ROOT)
			return blogs.getWeblogCount();
		else if(parent instanceof Weblog)
			return BLOG_CHILDREN.length;
		else if(parent instanceof BlogFolder)
		{
			Object o[] = getBlogFolderContents((BlogFolder)parent);
			return o.length;
		}
		else if(parent instanceof File)
		{
			File f = (File)parent;			
			return getDirContents(f).size(); 
		}
		
        return 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    public boolean isLeaf(Object node)
    {
		if(node == ROOT || node instanceof Weblog || node instanceof BlogFolder)
			return false;
		if(node instanceof File)
		{
			File f = (File)node;
			return f.isFile();
		}
		return true;
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
		if(parent == ROOT)
		{
			for(int i = 0; i < blogs.getWeblogCount(); i++)
				if(child == blogs.getWeblogAt(i))
					return i;
		}
		else if(parent instanceof Weblog)
		{
			String s = child.toString();
			for(int i = 0; i < BLOG_CHILDREN.length; i++)
				if(s.equals(BLOG_CHILDREN[i]))
					return i;	
		}
		else if(parent instanceof BlogFolder)
		{
			Object o[] = getBlogFolderContents((BlogFolder)parent);
			for(int i = 0; i < o.length; i++)
				if(child == o[i])
					return i;
		}
		else if(parent instanceof File)
		{
			File f = (File)parent;
			Vector v = getDirContents(f);
			return v.indexOf(child);
		}
        
		return 0;//root       
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
	
	private Vector getDirContents(File f)
	{
		File dirs[] = f.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				if(f.isDirectory())
					return true;			
				return false;				
			}
		});			
		File files[] = f.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				if(f.isFile())
					return true;			
				return false;				
			}
		});
		
		Comparator comp = new FileComparator();
		Arrays.sort(dirs, comp);
		Arrays.sort(files, comp);
		
		Vector v = new Vector();
		for(int i = 0; i < dirs.length; i++)
			v.add(dirs[i]);
		for(int i = 0; i < files.length; i++)
			v.add(files[i]);
		
		return v;		
	}
    
	private Object[] getBlogFolderContents(BlogFolder folder)
	{
		Weblog w = folder.getBlog();
		try
		{		
			if(folder.toString().equals(CATS))
				return w.getCategories();
			else if(folder.toString().equals(DATED_ARCS))
				return w.getArchives();
			else if(folder.toString().equals(TEMPLATES))
				return w.getTemplates();
			else if(folder.toString().equals(WEB_SITE))
			{
				File f = w.getWebFilesDirectory();
				if(!f.exists() || f.isFile())
					return new Object[0];
				
				Vector v = getDirContents(f);
				Object files[] = new Object[v.size()];
				for(int i = 0; i < files.length; i++)				
					files[i] = v.elementAt(i);							
				return files;
			}
		}
		catch(Exception ex){}
		
		return new Object[0];
	}
	
	private class FileComparator implements Comparator
	{
		private java.text.Collator coll = java.text.Collator.getInstance();
		
		public int compare(Object o1, Object o2)
		{
			File f1 = (File)o1;
			File f2 = (File)o2;
			return coll.compare(f1.getName(), f2.getName());
		}
	}
	
	private class BlogFolder
	{
		private Weblog blog;
		private String name;
		public BlogFolder(Weblog wb, String name)
		{
			blog = wb;
			this.name = name;
		}
		
		public Weblog getBlog()
		{
			return blog;
		}
    	
		public String toString()
		{
			return new String(name);
		}
	}

}
