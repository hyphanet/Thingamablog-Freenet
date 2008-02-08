/*
 * Created on Nov 10, 2007
 */
package net.sf.thingamablog;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;


/**
 * @author Bob Tantlinger
 *
 */
public class EnvUtils
{
    public static final int LINUX = 0;
    public static final int WINDOWS = 1;
    public static final int MAC_OS = 2;
    public static final int SOLARIS = 3;    
    public static final int OTHER = -1;
    
    public static final int getPlatform()
    {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.indexOf("linux") > -1)
            return LINUX;
        if(os.indexOf("windows") > -1)
            return WINDOWS;
        if(os.indexOf("mac os") > -1 || os.indexOf("os x") > -1 || os.indexOf("macintosh") > -1)
            return MAC_OS;
        if(os.indexOf("solaris") > -1)
            return SOLARIS;
 
        
        return OTHER;
    }
    
    public static String getPathForObject(Object obj)
    {
        URL url = getURLForObject(obj);
 
        if(url.getProtocol().equals("jar"))
        {
            try
            {
                JarURLConnection jarCon = (JarURLConnection)url.openConnection();
                url = jarCon.getJarFileURL();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
 
        try
        {
            File file = new File(URLDecoder.decode(url.getPath(), "UTF-8"));
            if(file.isFile())
                return file.getParent();
            return file.getPath();
        }
        catch(UnsupportedEncodingException e)
        {
            System.err.println("Urldecoding error: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
 
    private static URL getURLForObject(Object obj)
    {
        String name = obj.getClass().getName();
        int index = name.lastIndexOf('.');
 
        name = new String(name.substring(index + 1) + ".class");
         
        return obj.getClass().getResource(name);
    }
}
