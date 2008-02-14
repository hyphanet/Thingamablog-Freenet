/*
 * Created on Oct 29, 2007
 */
package net.sf.thingamablog.gui.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import net.atlanticbb.tantlinger.io.IOUtils;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.DiskTemplatePack;
import net.sf.thingamablog.blog.NullPublishProgress;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.TemplatePack;
import net.sf.thingamablog.blog.WeblogBackend;

import org.jdesktop.jdic.desktop.Desktop;



/**
 * @author Bob Tantlinger
 *
 */
public class WeblogPreviewer
{
    private static WeblogPreviewer singleton;
    
    private TBWeblog previewBlog;
    File outputDir = new File(TBGlobals.getPreviewDirectory(), "output");
    //private WeblogBackend backend;
    
    
    private WeblogPreviewer()
    {
        if(singleton != null)
            throw new IllegalStateException("Can only have one instance");
        singleton = this;
    }
    
    public static WeblogPreviewer getInstance()
    {
        if(singleton == null)
            return new WeblogPreviewer();
        return singleton;
    }
    
    public void clearPreviewData() 
    {
        if(previewBlog != null)
        {
            try
            {                
                previewBlog.deleteAll();                                  
            }
            catch(Exception ex)
            {
                //table not found?
                //ex.printStackTrace();
            }
        }
        
        IOUtils.deleteRecursively(outputDir);
    }
            
        
    public void previewInBrowser(TBWeblog blog, BlogEntry[] ents) throws Exception
    {
        previewInBrowser(blog, ents, new DiskTemplatePack(blog.getHomeDirectory()));
    }
    
    public void previewInBrowser(TBWeblog blog, BlogEntry[] ents, TemplatePack pack) throws Exception
    {
        String cats[] = blog.getCategories();
        Author[] auths = blog.getAuthors();
        
        String bUrl = blog.getBaseUrl();
        String baUrl = blog.getArchiveUrl();
        String bmUrl = blog.getMediaUrl();
        
        String baseUrl = outputDir.toURI().toURL().toExternalForm();        
        String arcUrl = new File(outputDir, 
            baUrl.substring(bUrl.length() - 1, baUrl.length())).toURI().toURL().toExternalForm();
        String mediaUrl = new File(outputDir, 
            bmUrl.substring(bUrl.length() - 1, bmUrl.length())).toURI().toURL().toExternalForm();
              
        initPreviewBlog(blog.getBackend(), pack, blog.getTitle(), blog.getDescription(), cats, auths, ents, blog.getType());
        previewBlog.setBlogUrls(outputDir.getAbsolutePath(), baseUrl, arcUrl, mediaUrl);
        
        //mimic the necessary attributes of the blog we're previewing
        previewBlog.setLocale(blog.getLocale());
        previewBlog.setArchivePolicy(blog.getArchivePolicy());
        previewBlog.setArchiveByDayInterval(blog.getArchiveByDayInterval());
        previewBlog.getPageGenerator().setCharset(blog.getPageGenerator().getCharset());
        previewBlog.getPageGenerator().setDateFormat(blog.getPageGenerator().getDateFormat());
        previewBlog.getPageGenerator().setTimeFormat(blog.getPageGenerator().getTimeFormat());
        previewBlog.getPageGenerator().setArchiveRangeFormat(
            blog.getPageGenerator().getArchiveRangeFormat(), 
            blog.getPageGenerator().isSpanArcRange());
        previewBlog.getPageGenerator().setCustomTags(blog.getPageGenerator().getCustomTags());
        previewBlog.getPageGenerator().setFrontPageAscending(blog.getPageGenerator().isFrontPageAscending());
        previewBlog.getPageGenerator().setFrontPageLimit(blog.getPageGenerator().getFrontPageLimit());
       
        doPreview();
    }
    
    public void previewInBrowser(WeblogBackend backend, TemplatePack pack, String title, String desc, String[] cats, Author[] auths, String type) throws Exception
    {
        previewInBrowser(backend, pack, title, desc, cats, auths, null, type);
    }
    
    public void previewInBrowser(WeblogBackend backend, TemplatePack pack, String title, String desc, String[] cats, Author[] auths, BlogEntry[] ents, String type) 
    throws Exception
    {       
        initPreviewBlog(backend, pack, title, desc, cats, auths, ents, type);
        doPreview();        
    }
    
    private void doPreview() throws Exception
    {        
    	IOUtils.deleteRecursively(outputDir);
    	
    	try
        {            
            previewBlog.publishAll(new NullPublishProgress());
            if(previewBlog.getType().equals("internet")) {
                Desktop.browse(new URL(previewBlog.getFrontPageUrl()));            
            } else {
                String nodeHostname = TBGlobals.getProperty("NODE_HOSTNAME");
                Desktop.browse(new URL("http://" + nodeHostname + ":8888" + previewBlog.getFrontPageUrl()));
            }
        }
        catch(Exception ex)
        {           
            throw ex;
        }
        finally
        {
            try
            {
                previewBlog.deleteAll();
            }
            catch(BackendException bex)
            {
                bex.printStackTrace();
            }
        }
    }
    
    
    
    private synchronized void initPreviewBlog(WeblogBackend backend, TemplatePack pack, String title, String desc, String[] cats, Author[] auths, BlogEntry[] ents, String type) 
    throws Exception
    {        
        clearPreviewData();
        
        previewBlog = new TBWeblog(TBGlobals.getPreviewDirectory());       
        previewBlog.setBackend(backend);     
        previewBlog.setTitle(title);
        previewBlog.setDescription(desc);
        previewBlog.setType(type);
        
        //set last publish date really old so that web files modified date is
        //certain to be newer 
        previewBlog.setLastPublishDate(new Date(0)); 
        
        try
        {
        	pack.installPack(previewBlog.getHomeDirectory());
        	
        	String url = outputDir.toURL().toExternalForm();
            previewBlog.setBlogUrls(outputDir.getAbsolutePath(), url, url, url);
            
            for(int i = 0; cats != null && i < cats.length; i++)
            {
                previewBlog.addCategory(cats[i]);
            }
            
            for(int i = 0; auths != null && i < auths.length; i++)
            {
                previewBlog.addAuthor(auths[i]);
            }
            
            if(ents == null)
            {
                createExampleEntries(previewBlog);            
            }
            else
            {
                for(int i = 0; i < ents.length; i++)
                {
                    previewBlog.addEntry(ents[i]);
                }
            }
            
            //installTemplates(baseDir); 
        }
        catch(Exception ex)
        {
            clearPreviewData();
            throw ex;
        }
    }
    
    
    
    private void createExampleEntries(TBWeblog blog) throws BackendException
    {
        InputStream is = null;
        String text = "";
        try
        {
            URL u = Thread.currentThread().getContextClassLoader().getResource(
                "net/sf/thingamablog/gui/app/lorem.txt");
            is = u.openStream();
            text = IOUtils.read(is);
        }
        catch(IOException e)
        {           
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {                
                e.printStackTrace();
            }
        }
        
        String[] cats = blog.getCategories();
        Author[] a = blog.getAuthors();
        Author auth = null;
        if(a != null && a.length >= 1)
            auth = a[0];
        
        Calendar cal = Calendar.getInstance();
        
        BlogEntry be = new BlogEntry();
        be.setCategories(cats);
        be.setAuthor(auth);
        be.setDate(cal.getTime());
        be.setTitle("Lorem ipsum dolor sit");
        be.setText(text);        
        blog.addEntry(be);
        
        cal.add(Calendar.DAY_OF_YEAR, -1);
        be.setID(0);
        be.setDate(cal.getTime());
        blog.addEntry(be);
        
        cal.add(Calendar.HOUR, -1);
        be.setID(0);
        be.setDate(cal.getTime());
        blog.addEntry(be);
    }
}
