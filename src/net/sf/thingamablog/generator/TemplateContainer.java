/*
 * Created on Jan 29, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;
import java.util.List;




/**
 * @author Bob Tantlinger
 */
public interface TemplateContainer extends TemplateElement
{
    public void initialize(Hashtable attribs);
    
    public Object getValueForTag(TemplateTag t);
    
    public List getTags();
    
    public List getContainers();    
    
    public boolean processAgain();
    
    public boolean isVisible();
    
    public String prefix();
    
    public String postfix();
}
