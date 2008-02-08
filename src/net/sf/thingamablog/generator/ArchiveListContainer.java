/*
 * Created on Jan 31, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.TBWeblog;


/**
 * @author Bob Tantlinger
 */
public class ArchiveListContainer extends ListContainer
{
    private TBWeblog blog;
    private HyperTextTag labelTag;
    private TextTag linkTag;
    private Vector tags = new Vector();
    private ArchiveRange arcs[];
    private String arcUrl;
    private String format = "";
    private boolean span;
    
	/** Archive range format attrib */
	public static final String ARC_FORMAT = "format";
	/** Span archive range attrib */
	public static final String SPAN_RANGE = "span";
    
    /**
     * @param name
     */
    public ArchiveListContainer(TBWeblog blog, String defaultFormat, boolean span)
    {
        this("ArchiveList", blog, null, defaultFormat, span);
    }
    
    public ArchiveListContainer(String name, TBWeblog blog, ArchiveRange arcs[], String defaultFormat, boolean span)
    {
        super(name);
        this.arcs = arcs;
        this.blog = blog;
		labelTag = new HyperTextTag("ArchiveName");
		linkTag = new TextTag("ArchiveLink");
		tags.add(labelTag);
		tags.add(linkTag);
		
		Hashtable ht = getDefaultAttributes();    	
		ht.put(ARC_FORMAT, defaultFormat);
		if(span)
		    ht.put(SPAN_RANGE, "1");
		else
		    ht.put(SPAN_RANGE, "0");
    }
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.ListContainer#initListData(boolean, java.util.Hashtable)
     */
    public void initListData(boolean asc, Hashtable attribs)
    {
        try
        {
            if(arcs == null)
                arcs = blog.getArchives();            
        }
        catch(Exception ex)
        {
            arcs = new ArchiveRange[0];
        }
        
        Arrays.sort(arcs, new ArcComparator(!asc));
        
        format = attribs.get(ARC_FORMAT).toString();
        span = attribs.get(SPAN_RANGE).toString().equals("1");
        
		arcUrl = blog.getArchiveUrl();
		if(!arcUrl.endsWith("/"))
			arcUrl += "/";
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.ListContainer#getListDataSize()
     */
    public int getListDataSize()
    {        
        return arcs.length;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.ListContainer#getValueForTag(net.sf.thingamablog.generator1.TemplateTag, int)
     */
    public Object getValueForTag(TemplateTag t, int index)
    {
        try
        {        
            if(t == labelTag)
            {
                ArchiveRange ar = arcs[index];
                SimpleDateFormat df = new SimpleDateFormat(format, blog.getLocale());
                ar.setFormatter(df, span);
                return ar.getFormattedRange();
            }
        
            if(t == linkTag)
            {
                return arcUrl + blog.getArchiveFileName(arcs[index]);
            }
        }
        catch(ArrayIndexOutOfBoundsException ex)
        {}
        
        return "";
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
        return null;
    }
    
    public boolean isVisible()
    {
        return true;
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
