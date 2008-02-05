/*
 * Created on Jan 29, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;


/**
  * @author Bob Tantlinger
 */
public abstract class TemplateTag implements TemplateElement
{
    private String name;
    
    public TemplateTag(String name)
    {
        this.name = name;
    }
    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateElement#getName()
     */
    public String getName()
    {
       return name;
    }
    
    public abstract String process(Object val, Hashtable attribs);

}
