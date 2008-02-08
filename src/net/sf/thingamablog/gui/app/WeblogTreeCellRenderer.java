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

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.BlogEntry;
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
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Icon cats = UIUtils.getIcon(UIUtils.X16, "cats.png");
	private static final Icon arcs =  UIUtils.getIcon(UIUtils.X16, "arcs.png");		
	private static final Icon current =  UIUtils.getIcon(UIUtils.X16, "current.png");
	private static final Icon expired =  UIUtils.getIcon(UIUtils.MISC, "exp_entries16.gif");
	private static final Icon drafts = UIUtils.getIcon(UIUtils.X16, "drafts.png");
	private static final Icon entries = UIUtils.getIcon(UIUtils.X16, "copy.png");;
	private static final Icon template = UIUtils.getIcon(UIUtils.MISC, "template16.png");		
	private static final Icon textFile = UIUtils.getIcon(UIUtils.X16, "edit1.png");
	private static final Icon imageFile = UIUtils.getIcon(UIUtils.X16, "img_file.png");
	private static final Icon blogClosed = UIUtils.getIcon(UIUtils.X16, "blog.png");
	private static final Icon blogOpen = UIUtils.getIcon(UIUtils.X16, "blog_glow.png");
    private static final Icon failedBlog = UIUtils.getIcon(UIUtils.X16, "err_feed1.png");
    private static final Icon entry = UIUtils.getIcon(UIUtils.X16, "post.png");
    
	//private Font normalFont, boldFont;
	private FileSystemView fsv = null;
	
	public WeblogTreeCellRenderer()
	{
		super();
		
        //normalFont = getFont();
        //boldFont = normalFont.deriveFont(Font.BOLD);        
        
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
				if(w != null && w.getLastPublishDate().getTime() <= f.lastModified())
				    setFont(tree.getFont().deriveFont(Font.BOLD));
				else
				    setFont(tree.getFont());
			}
			else
			{
				setFont(tree.getFont());
			    setOpenIcon(getDefaultOpenIcon());
				setClosedIcon(getDefaultClosedIcon());			
			}
		}
		else if(leaf)
		{			
			setFont(tree.getFont());
		    if(value instanceof Template)
			{
				setLeafIcon(template);
			}
            else if(value instanceof BlogEntry)
            {
                value = ((BlogEntry)value).getTitle();
                setLeafIcon(entry);
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
			setFont(tree.getFont());
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
				Weblog b = (Weblog)value;                
                if(b.isMailCheckFailed() || b.isPublishFailed())
                {
                    setOpenIcon(failedBlog);
                    setClosedIcon(failedBlog);
                }
                else
                {
                    setOpenIcon(blogOpen);				
                    setClosedIcon(blogClosed);
                }
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
