/*
 * Created on Jan 30, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;

/**
 * @author Owner
 */
public abstract class ListContainer implements TemplateContainer
{
    private String name;
	/** Sort order attrib */
	public static final String SORT_ORDER = "sort_order";
	/** Glue attrib */
	public static final String GLUE = "glue";
	
	public static final String ASC = "ascend";
	public static final String DESC = "descend";
	
	protected Hashtable defaults = new Hashtable();
	
	private int curIndex = 0;
	private String glue = "";	
    
    public ListContainer(String name)
    {
        this.name = name;
        defaults.put(SORT_ORDER, ASC);
        defaults.put(GLUE, "");
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateContainer#init(java.util.Hashtable)
     */
    public void initialize(Hashtable attribs)
    {        
        curIndex = 0;
        glue = attribs.get(GLUE).toString();
        Object order = attribs.get(SORT_ORDER);       
        initListData(
            order != null && order.toString().equalsIgnoreCase(ASC), attribs);       
    }
    
    public abstract void initListData(boolean asc, Hashtable attribs);
    public abstract int getListDataSize();
    public abstract Object getValueForTag(TemplateTag t, int index);
    
    public Object getValueForTag(TemplateTag t)
    {
        return getValueForTag(t, curIndex);
    }
    
    public int currentIndex()
    {
        return curIndex;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateContainer#processAgain()
     */
    public boolean processAgain()
    {        
        curIndex++;
        return curIndex < getListDataSize();
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateContainer#prefix()
     */
    public String prefix()
    {        
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateContainer#postfix()
     */
    public String postfix()
    {        
        if(curIndex < getListDataSize() - 1)
            return glue;
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateElement#getName()
     */
    public String getName()
    {       
        return name;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpltest.TemplateElement#getDefaultAttributes()
     */
    public Hashtable getDefaultAttributes()
    {        
        return defaults;
    }
}
