/*
 * Created on Feb 1, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;



/**
 * @author Owner
 */
public abstract class BasicContainer implements TemplateContainer
{
    private Vector tags = new Vector();
    private Vector containers = new Vector();
    private String name = "";
    
    public BasicContainer(String name)
    {
        this.name = name;
    }
    
    public void registerTag(TemplateTag t)
    {
        tags.add(t);
    }
    
    public void registerContainer(TemplateContainer tc)
    {
        containers.add(tc);
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#init(java.util.Hashtable)
     */
    public void initialize(Hashtable attribs)
    {        

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#getTags()
     */
    public List getTags()
    {        
        return tags;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#getContainers()
     */
    public List getContainers()
    {        
        return containers;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#processAgain()
     */
    public boolean processAgain()
    {        
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#isVisible()
     */
    public boolean isVisible()
    {        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#prefix()
     */
    public String prefix()
    {        
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#postfix()
     */
    public String postfix()
    {        
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateElement#getName()
     */
    public String getName()
    {        
        return name;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateElement#getDefaultAttributes()
     */
    public Hashtable getDefaultAttributes()
    {        
        return null;
    }
}
