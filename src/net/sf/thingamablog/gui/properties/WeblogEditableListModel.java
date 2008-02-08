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
package net.sf.thingamablog.gui.properties;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.gui.LabelledItemPanel;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class WeblogEditableListModel implements EditableListModel
{
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    
    public static final int CATEGORIES = -1;
	public static final int AUTHORS = -2;
	private int mode;
	private Vector listEdits = new Vector();
	
	public WeblogEditableListModel(int mode)
	{
		this.mode = mode;
	}
	
	public void syncListWithWeblog(Weblog weblog) throws BackendException
	{
		for(int i = 0; i < listEdits.size(); i++)
		{
			ListEdit ed = (ListEdit)listEdits.elementAt(i);
			if(mode == AUTHORS)
				saveAuthEditToWeblog(weblog, ed);
			else
				saveCatEditToWeblog(weblog, ed);
		}
	}
	
	private void saveAuthEditToWeblog(Weblog weblog, ListEdit ed) throws BackendException
	{		
		if(ed.getType() == ListEdit.ADD)
		{
			weblog.addAuthor((Author)ed.getObject());
		}
		else if(ed.getType() == ListEdit.REMOVE)
		{
			weblog.removeAuthor((Author)ed.getObject());
		}
		else if(ed.getType() == ListEdit.EDIT)
		{
			weblog.updateAuthor((Author)ed.getObject(), (Author)ed.getNewObject());
		}
	}
	
	private void saveCatEditToWeblog(Weblog weblog, ListEdit ed) throws BackendException
	{		
		if(ed.getType() == ListEdit.ADD)
		{
			weblog.addCategory(ed.getObject().toString());
		}
		else if(ed.getType() == ListEdit.REMOVE)
		{
			weblog.removeCategory(ed.getObject().toString());
		}
		else if(ed.getType() == ListEdit.EDIT)
		{
			weblog.renameCategory(ed.getObject().toString(), ed.getNewObject().toString());
		}
	}
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.EditableListModel#add(net.sf.thingamablog.gui.properties.EditableList)
     */
    public Object add(EditableList c)
    {
        if(mode == AUTHORS)
        	return addAuthor(c);
        
        return addCat(c);
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.EditableListModel#shouldRemove(java.lang.Object, net.sf.thingamablog.gui.properties.EditableList)
     */
	public boolean shouldRemove(Object o, EditableList c)
	{
		int r = JOptionPane.showConfirmDialog(c,
			i18n.str("remove") + " : " + o.toString(), i18n.str("confirm"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if(r == JOptionPane.NO_OPTION)
			return false;
			
		listEdits.add(new ListEdit(o, ListEdit.REMOVE));
			
		return true;                    		 			
	}

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.EditableListModel#edit(java.lang.Object, net.sf.thingamablog.gui.properties.EditableList)
     */
    public Object edit(Object o, EditableList c)
    {        
		if(mode == AUTHORS)
			return editAuthor(o, c);
        
		return editCat(o, c);   
    }    
    
    
    private Object addCat(EditableList c)
    {
		String s = JOptionPane.showInputDialog(c, i18n.str("enter_a_category"), i18n.str("add_category"), //$NON-NLS-1$ //$NON-NLS-2$
			JOptionPane.QUESTION_MESSAGE);			
		if(s != null && !s.equals(""))
		{
			listEdits.add(new ListEdit(s, ListEdit.ADD));
			return s;
		}
    	
    	return null;
    }
    
   
	private Object editCat(Object o, EditableList c)
	{
		String prompt = i18n.str("category_rename_prompt"); //$NON-NLS-1$
		Object newo = JOptionPane.showInputDialog(c, prompt, i18n.str("rename"), //$NON-NLS-1$
				JOptionPane.QUESTION_MESSAGE, null, null, o);
				
		if(newo != null && !newo.toString().equals(""))
		{	
			ListEdit e = new ListEdit(o, ListEdit.EDIT);
			e.setNewObject(newo);
			listEdits.add(e);	
			return newo;
		}
        
		return null;
	}
    
	private Object addAuthor(EditableList c)
	{
		AuthorEditPane aep = new AuthorEditPane(new Author());
		int r = JOptionPane.showConfirmDialog(
			c, aep, i18n.str("add_author"), //$NON-NLS-1$
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    	                    
		if(r == JOptionPane.OK_OPTION)
		{
			Author a = aep.getAuthor();
			if(!a.getName().equals(""))
			{			
				listEdits.add(new ListEdit(a, ListEdit.ADD));
				return aep.getAuthor();
			}
		}
			
		return null;				
	}
		

		
	private Object editAuthor(Object o, EditableList c)
	{
		AuthorEditPane aep = new AuthorEditPane((Author)o);
		int r = JOptionPane.showConfirmDialog(
			c, aep, i18n.str("edit_author"), //$NON-NLS-1$
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    	                    
		if(r == JOptionPane.OK_OPTION)
		{
			Author a = aep.getAuthor();			
			if(!a.getName().equals(""))
			{			
				ListEdit e = new ListEdit(o, ListEdit.EDIT);			
				e.setNewObject(a);
				listEdits.add(e);
				return aep.getAuthor();
			}
		}
			
		return null;			
	}    
    
	private class AuthorEditPane extends LabelledItemPanel
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        JTextField aNameField = new JTextField(10);
		JTextField aEmailField = new JTextField(10);
		JTextField aUrlField = new JTextField(10);
		
		public AuthorEditPane(Author a)
		{			
			addItem(i18n.str("name"), aNameField); //$NON-NLS-1$
			addItem(i18n.str("email"), aEmailField); //$NON-NLS-1$
			addItem(i18n.str("url"), aUrlField); //$NON-NLS-1$
			
			aNameField.setText(a.getName());
			aEmailField.setText(a.getEmailAddress());
			aUrlField.setText(a.getUrl()); 
		}
		
		public Author getAuthor()
		{
			Author a = new Author();
			a.setName(aNameField.getText());
			a.setEmailAddress(aEmailField.getText());
			a.setUrl(aUrlField.getText());
			
			return a;		
		}
	}
    
	private class ListEdit
	{
		public final static int ADD = 1;
		public final static int REMOVE = 2;
		public final static int EDIT = 3;
		
		private int type;
		private Object obj, newObj = null;
		
		public ListEdit(Object o, int t)
		{
			type = t;
			obj = o;
		}
		
		public int getType()
		{
			return type;
		}
		
		public Object getObject()
		{
			return obj;
		}
		
		public Object getNewObject()
		{
			return newObj;
		}
		
		public void setNewObject(Object o)
		{
			newObj = o;
		}		
	}

}
