/*
 * Created on Jan 31, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.transport.FCPTransport;



/**
 * @author Bob Tantlinger
 *
 */
public class BlogPageContainer implements TemplateContainer
{
    public static final String NAME = "BlogPage";
    private Hashtable tagValues = new Hashtable();
    private Hashtable customTags = new Hashtable();
    private Vector containers = new Vector();    
    private TBWeblog blog;
    
    private String pageTitle = "";
    private String charSet = "UTF-8";
 
    
    public BlogPageContainer(TBWeblog b, String pageTitle, String charSet)
    {
        blog = b;
        this.pageTitle = pageTitle;
        this.charSet = charSet;
    } 
    
    public void addContainer(TemplateContainer tc)
    {
        if(!containers.contains(tc))
            containers.add(tc);
    }
    
    public void addCustomTag(CustomTag t)
    {
        customTags.put(t, t.getValue());
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#init(java.util.Hashtable)
     */
    public void initialize(Hashtable attribs)
    {        
        DateTag curDateTag = new DateTag("CurrentDate");
        curDateTag.setLocale(blog.getLocale());
        
        tagValues.put(new TextTag("Charset"), charSet);
        tagValues.put(new TextTag("Lang"), blog.getLocale().getLanguage());
        tagValues.put(new TextTag("Country"), blog.getLocale().getCountry());
        tagValues.put(new TextTag("BlogTitle"), blog.getTitle());
        tagValues.put(new HyperTextTag("BlogDescription"), blog.getDescription());
        tagValues.put(new TextTag("FrontPageLink"), blog.getBaseUrl() + blog.getFrontPageFileName());
        if (blog.getType().equals("internet")) {
            tagValues.put(new TextTag("MetaDescription"),"");
            tagValues.put(new TextTag("RssLink"), blog.getBaseUrl() + blog.getRssFileName());        
            tagValues.put(new TextTag("SyndicateMessage"), "Syndicate this site !");
        } else {
            tagValues.put(new TextTag("MetaDescription"), "<meta name=\"description\" content=" + blog.getDescription());
            tagValues.put(new TextTag("RssLink"), "/?newbookmark=freenet:" + blog.getBaseUrl() + "&desc=" + blog.getDescription());        
            tagValues.put(new TextTag("SyndicateMessage"), "Bookmark this site !");
            if (blog.getPublishTransport() instanceof FCPTransport)
                            tagValues.put(new TextTag("EditionNumber"), (((FCPTransport)blog.getPublishTransport()).getEdition()+ 1) +"");        
        }
        tagValues.put(new TextTag("IndexPageLink"), blog.getBaseUrl() + blog.getArchiveIndexFileName());
        tagValues.put(curDateTag, new Date());
        tagValues.put(new TextTag("PageTitle"), pageTitle);
        tagValues.put(new TextTag("AppName"), TBGlobals.APP_NAME);
        tagValues.put(new TextTag("AppVersion"), TBGlobals.VERSION);
        tagValues.put(new TextTag("AppLink"), TBGlobals.APP_URL);
        tagValues.put(new TextTag("BaseURL"), blog.getBaseUrl());
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#getValueForTag(net.sf.thingamablog.generator1.TemplateTag)
     */
    public Object getValueForTag(TemplateTag t)
    {        
        Object o = customTags.get(t);
        if(o != null)
            return o;
        
        return tagValues.get(t);
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateContainer#getTags()
     */
    public List getTags()
    {        
        Vector tags = new Vector();
        //add customtags so they are processed first
        for(Enumeration e = customTags.keys(); e.hasMoreElements();)
            tags.add(e.nextElement());
        
        for(Enumeration e = tagValues.keys(); e.hasMoreElements();)
            tags.add(e.nextElement());
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
        return NAME;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator1.TemplateElement#getDefaultAttributes()
     */
    public Hashtable getDefaultAttributes()
    {        
        return null;
    }
    
    public boolean isVisible()
    {
        return true;
    }
}
