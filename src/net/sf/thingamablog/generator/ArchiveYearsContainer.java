/*
 * Created on Feb 21, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.TBWeblog;



/**
 * @author Bob Tantlinger
 *
 */
public class ArchiveYearsContainer extends ListContainer
{
    private String format;
    private boolean span;
    private TBWeblog blog;
    private Vector years = new Vector();
    private ArchiveRange arcs[];
    private Calendar cal;
    private TextTag yearTag = new TextTag("Year");
    private ArrayList tags = new ArrayList(1);
    
    public ArchiveYearsContainer(TBWeblog blog, String format, boolean span)
    {
        super("ArchiveYears");
        this.blog = blog;
        this.span = span;
        this.format = format;
        cal = Calendar.getInstance(blog.getLocale());
        tags.add(yearTag);
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.ListContainer#initListData(boolean, java.util.Hashtable)
     */
    public void initListData(boolean asc, Hashtable attribs)
    {        
        years.removeAllElements();
        
        try
        {            
            arcs = blog.getArchives();
            Arrays.sort(arcs, new ArcComparator(!asc));
        }
        catch(Exception ex)
        {
            arcs = new ArchiveRange[0];
        }
        
        int curYear = -1;
        for(int i = 0; i < arcs.length; i++)
        {
            cal.setTime(arcs[i].getStartDate());
            if(cal.get(Calendar.YEAR) != curYear)
            {
                curYear = cal.get(Calendar.YEAR);
                years.add(new Integer(curYear));
            }
        }
    }

    private ArchiveRange[] getArcsForYear(int y)
    {
        Vector v = new Vector();
        for(int i = 0; i < arcs.length; i++)
        {
            cal.setTime(arcs[i].getStartDate());
            if(cal.get(Calendar.YEAR) == y)
                v.add(arcs[i]);
        }
        
        ArchiveRange ar[] = new ArchiveRange[v.size()];
        for(int i = 0; i < ar.length; i++)
            ar[i] = (ArchiveRange)v.elementAt(i);
        
        return ar;
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.ListContainer#getListDataSize()
     */
    public int getListDataSize()
    {        
        return years.size();
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.ListContainer#getValueForTag(net.sf.thingamablog.generator.TemplateTag, int)
     */
    public Object getValueForTag(TemplateTag t, int index)
    {
        if(t == yearTag)
        {   
            return years.elementAt(index).toString();
        }
        return "";
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#getTags()
     */
    public List getTags()
    {        
        return tags;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#getContainers()
     */
    public List getContainers()
    {        
        ArrayList c = new ArrayList(1);
        Integer year = (Integer)years.elementAt(currentIndex());
        ArchiveRange ar[] = getArcsForYear(year.intValue());
        ArchiveListContainer a = new  ArchiveListContainer("ArchiveYear", blog, ar, format, span);
        c.add(a);
        return c;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#isVisible()
     */
    public boolean isVisible()
    {        
        return years.size() > 0;
    }
    
	private class ArcComparator implements Comparator
	{
		private boolean asc;
		public ArcComparator(boolean asc)
		{
			this.asc = asc;
		}
		
		public int compare(Object one, Object two) 
		{
			ArchiveRange a1 = (ArchiveRange)one;
			ArchiveRange a2 = (ArchiveRange)two;
			if(asc)			
				return a1.getExpirationDate().compareTo(a2.getExpirationDate());	
			
			return a2.getExpirationDate().compareTo(a1.getExpirationDate());
		}
	}
}
