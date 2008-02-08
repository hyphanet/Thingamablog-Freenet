/*
 * Created on Jan 30, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;


/**
 * @author Bob Tantlinger
 */
public class TextTag extends TemplateTag
{
    public static final String TRIM = "trim";
    public static final String APPEND = "append";
    static final String FALSE = "0";
    
    protected Hashtable defaults = new Hashtable();
    
    public TextTag(String name)
    {
        super(name);
        defaults.put(TRIM, "0");
        defaults.put(APPEND, "");
    }
    
    public String process(Object val, Hashtable attribs)
    {        
        String text = val.toString();
        if(!attribs.get(TRIM).equals(FALSE))
            text = text.trim();
        return text + attribs.get(APPEND).toString();        
    }

    public Hashtable getDefaultAttributes()
    {        
        return defaults;
    }
}
