/*
 * Created on Jan 29, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;


/**
 * @author Bob Tantlinger
 */
public interface TemplateElement
{
	/**
	 * Gets the name of the element
	 * 
	 * @return The element name
	 */
	public String getName();
	
	/**
	 * Gets the default attributes of the element. Default
	 * attributues are used if no matching attributes are 
	 * supplied in the template
	 * 
	 * @return The default attributes.
	 */
	public Hashtable getDefaultAttributes();
}
