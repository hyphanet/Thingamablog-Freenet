/*
 * Created on Feb 19, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.TBWeblog;


/**
 * @author BT
 *
 */
public class NextPreviousContainer implements TemplateContainer
{
    public static final int NEXT = -1;
    public static final int PREV = -2;    
    
    private int dir = NEXT;
    private long entryID = -1;
    private ArchiveRange arc = null;
    private String cat = null;
    
    private TBWeblog blog = null;
    
    private boolean exists = true;
    
    private HyperTextTag pageTitleTag = new HyperTextTag("PageName");
    private TextTag pageLinkTag = new TextTag("PageLink");
        
    private String pageTitle;
    private String pageLink;
    
    
    public NextPreviousContainer(int dir)
    {         
        this.dir = dir;
    }
    
    public NextPreviousContainer(TBWeblog blog, String cat, int dir)
    {        
        this(dir);
        this.cat = cat;
        this.blog = blog;
    }
    
    public NextPreviousContainer(TBWeblog blog, ArchiveRange arc, int dir)
    {        
        this(dir);
        this.arc = arc;
        this.blog = blog;
    }
    
    public NextPreviousContainer(TBWeblog blog, long id, int dir)
    {        
        this(dir);
        this.entryID = id;
        this.blog = blog;
    }
    
    public List getContainers()
    {
        ArrayList list = new ArrayList(2);
        list.add(new IfPageExists());
        list.add(new IfNoPageExists());
        return list;
    }
    
    public List getTags()
    {
        //return tags;
        return null;
    }
    
    public Hashtable getDefaultAttributes()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#processAgain()
     */
    public boolean processAgain()
    {        
        return false;
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
    
    public void initialize(Hashtable attribs)
    {        
        if(blog == null)
        {
            exists = false;
            return;
        }
        
        if(cat != null)
        {
            try
            {
                String cats[] = blog.getCategories();
                Object o = getNextOrPrev(cat, cats);
                if(o != null)
                {
                    String c = o.toString();
                    pageTitle = c;
                    pageLink = blog.getArchiveUrl() + blog.getCategoryFileName(c);
                    exists = true;
                }
                else
                    exists = false;
            }
            catch(Exception ex)
            {
                exists = false;
            }           
        }
        else if(arc != null)
        {
            try
            {
                ArchiveRange arcs[] = blog.getArchives();
                Object o = getNextOrPrev(arc, arcs);
                if(o != null)
                {
                    ArchiveRange a = (ArchiveRange)o;
                    pageTitle = a.getFormattedRange();
                    pageLink = blog.getArchiveUrl() + blog.getArchiveFileName(a);
                    exists = true;
                }
                else
                    exists = false;
            }
            catch(Exception ex)
            {
                exists = false;
            }           
        }
        else if(entryID > -1)
        {
            try
            {
               BlogEntry be = blog.getEntry(entryID);
               if(dir == NEXT)
                   be = blog.getEntryAfter(be.getDate());
               else
                   be = blog.getEntryBefore(be.getDate());
               if(be != null)
               {
                   pageTitle = be.getTitle();
                   pageLink = blog.getUrlForEntry(be);
               }
               else
                   exists = false;
            }
            catch(Exception ex)
            {
                exists = false;
            }
        }
            
    }    

    private Object getNextOrPrev(Object o, Object ar[])
    {
        if(ar.length == 0)
            return null;
        
        int i;
        for(i = 0; i < ar.length; i++)
        {
            if(ar[i].equals(o))
                break;
        }
        
        if(dir == NEXT)
        {
            if(i < ar.length - 1)
                return ar[i + 1];            
        }
        else
        {
            if(i > 0)
                return ar[i - 1];            
        }
        
        return null;
    }
    
    public String getName()
    {
        if(dir == NEXT)
            return "NextPage";
        return "PreviousPage";
    }
    
    public boolean isVisible()
    {
        return true;
    }
    
    public Object getValueForTag(TemplateTag tag)
    {
        return "";
    }
    
    private class IfPageExists extends BasicContainer
    {
        public IfPageExists()
        {
            super("IfPageExists");
            registerTag(pageTitleTag);
            registerTag(pageLinkTag);
        }
        
        public boolean isVisible()
        {
            return exists;
        }
        
        public Object getValueForTag(TemplateTag tag)
        {
            if(tag == pageLinkTag)
                return pageLink;
            if(tag == pageTitleTag)
                return pageTitle;
            return "";
        }
    }
    
    private class IfNoPageExists extends BasicContainer
    {
        public IfNoPageExists()
        {
            super("IfNoPageExists");
        }
        
        public boolean isVisible()
        {
            return !exists;
        }
        
        public Object getValueForTag(TemplateTag tag)
        {
            return "";
        }
    }
}
