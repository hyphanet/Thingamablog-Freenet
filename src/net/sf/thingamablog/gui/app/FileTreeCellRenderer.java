
package net.sf.thingamablog.gui.app;
import java.awt.Component;
import java.io.File;

import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer
{
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private FileSystemView fsv = null;
	
	public FileTreeCellRenderer()
	{
		super();
        fsv = FileSystemView.getFileSystemView();
	}
	
	public Component getTreeCellRendererComponent(JTree tree,
		   Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus) 
	{
	    
		
		
		if(value instanceof File)
		{				
			//setLeafIcon(getDefaultLeafIcon());
			File f = (File)value;
			value = f.getName();
			if(f.isFile())
			{
				setLeafIcon(fsv.getSystemIcon(f));			
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
