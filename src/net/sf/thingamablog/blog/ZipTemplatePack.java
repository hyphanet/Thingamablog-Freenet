/*
 * Created on Oct 30, 2007
 */
package net.sf.thingamablog.blog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.atlanticbb.tantlinger.io.IOUtils;



/**
 * @author Bob Tantlinger
 *
 */
public class ZipTemplatePack implements TemplatePack
{
    private Map zipEntries;
    private ZipFile tmplPack;
    private Properties packProps;

    public ZipTemplatePack(File zipFile) throws ZipException, IllegalArgumentException, IOException
    {
        initializePack(zipFile);
    }    
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.TemplatePack#getPackProperties()
     */
    public Properties getPackProperties()
    {        
        return packProps;
    }
    
    public String getTitle()
    {
        return packProps.getProperty("title");
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.TemplatePack#installPack(java.io.File)
     */
    public void installPack(File dir) throws IOException
    {        
        for(Iterator it = zipEntries.keySet().iterator(); it.hasNext();)
        {
            String path = (String)it.next();
            File outFile = new File(dir, path);
            outFile.getParentFile().mkdirs();
            if(!outFile.getParentFile().exists())
                throw new IOException("Couldn't create directory:" + outFile.getParentFile());
            ZipEntry entry = (ZipEntry)zipEntries.get(path);
            
            InputStream in = null;
            OutputStream out = null;
            try
            {
                in =  tmplPack.getInputStream(entry);
                out = new FileOutputStream(outFile);
                IOUtils.copy(in, out);                
            }
            catch(IOException ioex)
            {
                throw ioex;
            }
            finally
            {            
                IOUtils.close(out);
                IOUtils.close(in);  
            }            
        }
        
        //ensure the properties file exists
        OutputStream pout = null;
        try
        {
            pout = new FileOutputStream(new File(dir, "pack.properties"));
            Properties props = this.getPackProperties();
            if(!props.containsKey("created"))
                props.put("created", new Date().getTime() + "");
            props.store(pout, "");
        }
        catch(IOException ioe)
        {
            throw ioe;
        }
        finally
        {
            IOUtils.close(pout);
        }
    }
    
    /**
     * Close the zip file. Once a template pack is closed it cannot be used.
     * @throws IOException
     */
    public void close() throws IOException
    {
        if(tmplPack != null)
            tmplPack.close();
    }
    
    private void initializePack(File zipFile) throws ZipException, IOException, IllegalArgumentException
    {        
        tmplPack = new ZipFile(zipFile);
        String rootDir = getRootDirectory(tmplPack);
        if(rootDir == null)
        {
            throw new IllegalArgumentException("Invalid template pack");
        }
        
        zipEntries = new HashMap();
        String tmplRoot  = rootDir + "templates/";
        String webRoot = rootDir + "web/";
        String propFile = rootDir + "pack.properties";
        
        packProps = new Properties();
        packProps.put("title", IOUtils.getName(zipFile));              
        
        Enumeration eenum = tmplPack.entries();
        while(eenum.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry)eenum.nextElement();
            String name = entry.getName();
            
            //System.err.println(name + " - " + getFileName(name));
            //File outFile = null;
            if(name.equals(propFile))
            {
                zipEntries.put("pack.properties", entry);                                
            }
            else if(name.equals(tmplRoot + getFileName(name)) && name.endsWith(".template"))
            {                        
                //System.err.println("->" + name);
                zipEntries.put("templates/" + getFileName(name), entry);
            }
            else if(name.startsWith(webRoot) && (!name.equals(webRoot)) && (!entry.isDirectory()))
            {
                String path = name.substring(rootDir.length(), name.length());
                zipEntries.put(path, entry);
                //System.err.println("----->" + name + " : " + path);
            }
        }
        
        //did we have a "pack.properties" file?
        ZipEntry propEntry = (ZipEntry)zipEntries.get("pack.properties");
        if(propEntry != null)
        {
            InputStream in = null;
            try
            {
                in =  tmplPack.getInputStream(propEntry);
                packProps.load(in);
            }
            catch(IOException ioex)
            {
                throw ioex;
            }
            finally
            {            
                IOUtils.close(in);  
            }
        }
    }
    
    private String getTemplateDirRoot(String entPath)
    {        
        if(entPath.startsWith("templates/"))
            return "";
        
        int pos = entPath.lastIndexOf("/templates/");
        if(pos == -1)
            return null;
        String root = entPath.substring(0, pos);
        
        int slashCount = 0;
        for(int i = 0; i < root.length(); i++)
        {
            if(root.charAt(i) == '/')
                slashCount++;
            if(slashCount > 2)
                return null;
        }
        return root + '/';
    }
    
        
    private String getRootDirectory(ZipFile z)
    {
        try
        {            
            Enumeration eenum = z.entries();
            while(eenum.hasMoreElements()) 
            {
                ZipEntry ze = (ZipEntry) eenum.nextElement(); 
                String name = ze.getName();
                
                if(name.toLowerCase().endsWith(".template"))
                {
                    String root = getTemplateDirRoot(name);
                    //System.err.println(name);
                    if(root != null)
                    {
                        return root;
                    }
                }                   
            }            
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            
        }
        
        return null;    
    }
    
    private String getFileName(String path)
    {
        int p = path.lastIndexOf("/");
        if(p != -1 && p != path.length() - 1)
        {
            return path.substring(p + 1, path.length());
        }

        return path;
    }
}
