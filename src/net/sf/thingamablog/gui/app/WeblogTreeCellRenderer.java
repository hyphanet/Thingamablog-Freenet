/*
 * Created on May 3, 2004
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
import java.awt.Component;
import java.awt.Font;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.blog.Template;
import net.sf.thingamablog.blog.Weblog;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class WeblogTreeCellRenderer extends DefaultTreeCellRenderer
{	
	private static final Icon cats = Utils.createIcon(TBGlobals.RESOURCES + "cats16.gif");
	private static final Icon arcs = Utils.createIcon(TBGlobals.RESOURCES + "arcs16.gif");		
	private static final Icon current = Utils.createIcon(TBGlobals.RESOURCES + "cur_entries16.gif");
	private static final Icon expired = Utils.createIcon(TBGlobals.RESOURCES + "exp_entries16.gif");
	private static final Icon drafts = Utils.createIcon(TBGlobals.RESOURCES + "drafts16.gif");
	private static final Icon entries = Utils.createIcon(TBGlobals.RESOURCES + "entries16.gif");
	private static final Icon template = Utils.createIcon(TBGlobals.RESOURCES + "template16.png");		
	private static final Icon textFile = Utils.createIcon(TBGlobals.RESOURCES + "text_file16.png");
	private static final Icon imageFile = Utils.createIcon(TBGlobals.RESOURCES + "image_file16.png");
	private static final Icon blogClosed = Utils.createIcon(TBGlobals.RESOURCES + "blog_closed16.png");
	private static final Icon blogOpen = Utils.createIcon(TBGlobals.RESOURCES + "blog_open16.png");
	
	private Font normalFont, boldFont;
	private FileSystemView fsv = null;
	
	public WeblogTreeCellRenderer()
	{
		super();
		
        normalFont = new Font("Dialog", Font.PLAIN, 11);
        boldFont = normalFont.deriveFont(Font.BOLD);        
        
        if(System.getProperty("os.name").startsWith("Windows"))
            fsv = FileSystemView.getFileSystemView();
	}
	
	private Weblog getParentWeblog(JTree tree, int row)
	{
		while(tree.getPathForRow(row) != null)
		{
			TreePath p = tree.getPathForRow(row);
			if(p.getLastPathComponent() instanceof Weblog)
				return (Weblog)p.getLastPathComponent();
			row--;
		}
		
		return null;
	}
	
	private Icon getFileIcon(File f)
	{				
		Icon ico;
		if(fsv != null)
		    ico = fsv.getSystemIcon(f);
		else
		{
		    if(TBGlobals.isImageFile(f))
				ico = imageFile;
			else 
			    ico = textFile;	
		}
		return ico;		
	}
	
	public Component getTreeCellRendererComponent(JTree tree,
		   Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) 
	{
	    
		String str = value.toString();
		
		if(value instanceof File)
		{				
			//setLeafIcon(getDefaultLeafIcon());
			File f = (File)value;
			value = f.getName();
			if(f.isFile())
			{
				setLeafIcon(getFileIcon(f));
				Weblog w = getParentWeblog(tree, row);
				if(w != null && w.getLastPublishDate().getTime() >= f.lastModified())
				    setFont(normalFont);
				else
				    setFont(boldFont);
			}
			else
			{
				setFont(normalFont);
			    setOpenIcon(getDefaultOpenIcon());
				setClosedIcon(getDefaultClosedIcon());			
			}
		}
		else if(leaf)
		{			
			setFont(normalFont);
		    if(value instanceof Template)
			{
				setLeafIcon(template);
			}
			else if(str.equals(WeblogTreeModel.CURRENT))
				setLeafIcon(current);
			else if(str.equals(WeblogTreeModel.EXPIRED))
				setLeafIcon(expired);
			else if(str.equals(WeblogTreeModel.DRAFTS))
				setLeafIcon(drafts);
			else
				setLeafIcon(entries);
		}
		else
		{			
			setFont(normalFont);
		    if(value == WeblogTreeModel.ROOT)
			{
				Icon ico = UIManager.getIcon("FileView.computerIcon");
				
				if(ico != null)
				{				
					setOpenIcon(UIManager.getIcon("FileView.computerIcon"));
					setClosedIcon(UIManager.getIcon("FileView.computerIcon"));
				}
				else
				{
					setOpenIcon(getDefaultOpenIcon());
					setClosedIcon(getDefaultClosedIcon());
				}
			}
			else if(value instanceof Weblog)
			{
				setOpenIcon(blogOpen);				
				setClosedIcon(blogClosed);
			}
			else if(str.equals(WeblogTreeModel.DATED_ARCS))
			{
				setOpenIcon(arcs);
				setClosedIcon(arcs);
			}
			else if(str.equals(WeblogTreeModel.CATS))
			{
				setOpenIcon(cats);
				setClosedIcon(cats);
			}
			else
			{
				setOpenIcon(getDefaultOpenIcon());
				setClosedIcon(getDefaultClosedIcon());
			}
		}
		
		Component c = super.getTreeCellRendererComponent(
			tree, value, sel, expanded, leaf, row, hasFocus);
		
		return c;				
	}	
}
