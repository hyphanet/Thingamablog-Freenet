/*
 * Created on Feb 20, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;


import net.atlanticbb.tantlinger.io.IOUtils;
import net.sf.thingamablog.TimeoutInputStream;

/**
  * @author Bob Tantlinger
  */
public class IncludeContainer extends BasicContainer
{
    public static final String FILE = "file";
    
    private HyperTextTag includeText = new HyperTextTag("IncludeText");
    private Hashtable def = new Hashtable();
    private String text = null;
    
    
    public IncludeContainer()
    {
        super("Include");
        registerTag(includeText);
        def.put(FILE, "");
    }
    
    public void initialize(Hashtable at)
    {
        text = null; 
        InputStream is = null;
        try
        {            
            String src = at.get(FILE).toString();
            if(isValidURL(src))
            {
                URL url = new URL(src);                
                is = new TimeoutInputStream(url.openStream(), 1024, 10000, 10000);
            }
            else
            {
                File f = new File(src);
                if(f.isFile() && f.canRead())                
                    is = new FileInputStream(f);                
            }
            
            if(is != null)
            {
                text = IOUtils.read(is);
                is.close();
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        } 
        finally
        {
            IOUtils.close(is);
        }
    }
    
    private boolean isValidURL(String src)
    {
        try
        {
            new URL(src);
            return true;
        }
        catch(MalformedURLException ex){}
        
        return false;
    }
    
/*    public void initialize(Hashtable at)
    {
        if(at.get(FILE) != null && !at.get(FILE).equals(""))        
            file = new File(at.get(FILE).toString());
        else
            file = null;        
    }*/
    
    public Hashtable getDefaultAttributes()
    {
        return def;
    }
    
    public boolean isVisible()
    {
        
        return text != null;
/*        try 
        {
            return file != null && file.canRead();
        }
        catch(Exception ex)
        {
            return false;
        }*/
    }
    
    public Object getValueForTag(TemplateTag t)
    {        
        return text;        
    }
}
