/*
 * Created on Jan 31, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;


/**
 * @author Bob Tantlinger
 */
public class CustomTag extends TextTag
{
    private String value;
    
    /**
     * @param name
     */
    public CustomTag(String name, String value)
    {
        super(name);
        this.value = value;        
    }
    
	/**
	 *  overridden from super class to just return the value
	 */
	public String process(Object obj, Hashtable attribs)
	{        
		return super.process(obj, attribs);
	}
	
    /**
     * Gets the value
     * 
     * @return The value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets the value
     * 
     * @param val The value
     */
    public void setValue(String val)
    {
        value = val;
    }
    
    /**
     * Gets the string representation of the tag.
     * 
     * @return The name of the Tag inclosed in template brackets eg &lt;$TagName$&gt;
     */
    public String toString()
    {
    	return "<$" + getName() + "$>";
    }

}
