/*
 * Created on Jun 29, 2004
 * 
 */

package net.sf.thingamablog.gui.properties;


/**
 * @author Bob Tantlinger
 *
 * Model for an <code>EditableList</code>
 * 
 */
public interface EditableListModel
{
	/**
	 * 
	 * @param c The editable list
	 * @return The object to add to the list. Return null if the object shouldn't be added
	 */
	public Object add(EditableList c);
	
	/**
	 * 
	 * @param o The object to remove
	 * @param c The EditableList
	 * @return true if removes, false otherwise
	 */
	public boolean shouldRemove(Object o, EditableList c);
	
	/**
	 * 
	 * @param o The object to edit
	 * @param c The EditableList
	 * @return The edited object. Return null if the object shouldn't be edited
	 */
	public Object edit(Object o, EditableList c);
}
