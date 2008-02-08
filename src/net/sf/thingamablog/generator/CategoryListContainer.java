/*
 * Created on Jan 31, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.TBWeblog;


/**
 * @author Bob Tantlinger
 */
public class CategoryListContainer extends ListContainer
{
    private static final int ENTRY_CATS = 0;
    private static final int BLOG_CATS = 1;
    
    private TBWeblog blog;
    private BlogEntry entry;
    private int mode;
    private TextTag linkTag, feedLinkTag;
    private HyperTextTag labelTag;
    private Vector tags = new Vector();
    private String cats[];
    private String arcUrl;

    /**
     * @param name
     */
    public CategoryListContainer(TBWeblog blog)
    {
        super("CategoryList");
        this.blog = blog;
        mode = BLOG_CATS;
        createTags();
    }
    
    public CategoryListContainer(TBWeblog blog, BlogEntry entry)
    {
        super("EntryCategories");
        this.blog = blog;
        this.entry = entry;
        mode = ENTRY_CATS;
        createTags();
    }
    
    private void createTags()
    {
		labelTag = new HyperTextTag("CategoryName");
		linkTag = new TextTag("CategoryLink");
        feedLinkTag = new TextTag("FeedLink");
		tags.add(labelTag);
		tags.add(linkTag);
        tags.add(feedLinkTag);
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.ListContainer#initListData(boolean, java.util.Hashtable)
     */
    public void initListData(boolean asc, Hashtable attribs)
    {
        try
        {
            if(mode == ENTRY_CATS)
                cats = entry.getCategories();
            else
                cats = blog.getCategories();            
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            cats = new String[0];
        }
        
        Arrays.sort(cats, new CatComparator(asc));
		arcUrl = blog.getArchiveUrl();
		if(!arcUrl.endsWith("/"))
			arcUrl += "/";
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.ListContainer#getListDataSize()
     */
    public int getListDataSize()
    {        
        return cats.length;
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
                return cats[index];
            }
        
            if(t == linkTag)
            {
                return arcUrl + blog.getCategoryFileName(cats[index]);
            }
            
            if(t == feedLinkTag)
            {
                return arcUrl + blog.getCategoryFeedFileName(cats[index]);
            }
            
        }
        catch(ArrayIndexOutOfBoundsException ex){}
        
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
    
	private class CatComparator implements Comparator
	{
		private boolean asc;		
		public CatComparator(boolean asc)
		{
			this.asc = asc;
		}
		
		public int compare(Object one, Object two) 
		{
			String s1 = one.toString();
			String s2 = two.toString();
			Collator coll = Collator.getInstance();
			if(asc)			
				return coll.compare(s1, s2);			
			return coll.compare(s2, s1);			
		}
	}
}
