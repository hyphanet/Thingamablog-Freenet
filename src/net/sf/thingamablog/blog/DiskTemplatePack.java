/*
 * Created on Oct 30, 2007
 */
package net.sf.thingamablog.blog;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;
import net.atlanticbb.tantlinger.io.IOUtils;



/**
 * @author Bob Tantlinger
 *
 */
public class DiskTemplatePack implements TemplatePack
{
    //private String zipFileName = null;
    
    protected File srcTmplDir;
    protected File srcWebDir;
    protected Properties packProps;
        
    
    /*
     * Creates a {@link TemplatePack} that reads from a directory and installs to a directory
     * 
     * @param tmplDir
     * @throws IOException
     * @throws IllegalArgumentException
     *
    public DiskTemplatePack(File tmplDir) throws IOException, IllegalArgumentException
    {
        this(tmplDir, null);
    } */
    
            
    /**
     * Creates a {@link TemplatePack} that reads from a directory and installs to a directory
     * 
     * @param tmplDir
     * @param zipFileName
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public DiskTemplatePack(File tmplDir/*, String zipFileName*/) throws IOException, IllegalArgumentException
    {        
        if(!tmplDir.isDirectory())
            throw new IllegalArgumentException("Template directory does not exist:" + tmplDir);
        
        srcTmplDir = new File(tmplDir, "templates");        
        File test[] = srcTmplDir.listFiles(new TemplateFileFilter());
        if(test == null || test.length == 0)
            throw new IllegalArgumentException("Invalid template directory");
        srcWebDir = new File(tmplDir, "web");
        
        packProps = new Properties();
        packProps.put("title", tmplDir.getName());
        File propFile = new File(tmplDir/*.getName()*/, "pack.properties");
        if(propFile.exists())
        {
            InputStream in = null;
            try
            {
                in = new FileInputStream(propFile);
                packProps.load(in);
            }
            catch(IOException ioe)
            {
                throw ioe;
            }
            finally
            {
                IOUtils.close(in);
            }
        }
        
        /*if(zipFileName != null)
        {
            if(!zipFileName.toLowerCase().endsWith(".zip"))
                zipFileName = zipFileName + ".zip";
        }
        
        this.zipFileName = zipFileName;*/
    }
    
    /*public boolean isOutputToZipFile()
    {
        return zipFileName != null;
    }*/
    
    public String getTitle()
    {
        return packProps.getProperty("title");
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.TemplatePack#getPackProperties()
     */
    public Properties getPackProperties() throws IOException
    {        
        return packProps;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.TemplatePack#installPack(java.io.File)
     */
    public void installPack(File dir) throws IOException
    {
        //if(zipFileName == null)
        {
            File destTmplDir = new File(dir, "templates");
            File destWebDir = new File(dir, "web");
            
            destTmplDir.mkdirs();
            destWebDir.mkdirs();
            
            IOUtils.copyFiles(srcTmplDir, destTmplDir);
            if(srcWebDir.isDirectory())
                IOUtils.copyFiles(srcWebDir, destWebDir);
            OutputStream pout = null;
            try
            {
                pout = new FileOutputStream(new File(dir, "pack.properties"));
                storeProperties(pout);
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
        /*else
        {            
            ZipOutputStream zos = null;
            try
            {            
                zos = new ZipOutputStream(new FileOutputStream(new File(dir, zipFileName)));            
                zipDir(srcTmplDir, zos);
                zipDir(srcWebDir, zos);
                //create an entry for the props file
                zos.putNextEntry(new ZipEntry("pack.properties"));
                storeProperties(zos);
                zos.closeEntry();            
            }
            catch(IOException ioe)
            {
                throw ioe;
            }
            finally
            {
                IOUtils.close(zos);
            }
        }*/
    }
    
    protected void storeProperties(OutputStream pout) throws IOException
    {
        Properties props = this.getPackProperties();
        if(!props.containsKey("created"))
            props.put("created", new Date().getTime() + "");
        props.store(pout, "");
    }
    
    /*private void zipDir(File dir, ZipOutputStream zos) throws IOException
    {
        if(dir.isDirectory())
            zipDir(dir.getAbsolutePath(), dir.getParentFile().getAbsolutePath(), zos);
        else
        {
            zos.putNextEntry(new ZipEntry(dir.getName() + '/'));
            zos.closeEntry();
        }
    }

    private void zipDir(String dir2zip, String rootDir, ZipOutputStream zos) throws IOException
    {
       
        // create a new File object based on the directory we have to zip
        File zipDir = new File(dir2zip);
        // get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;
        // loop through dirList, and zip the files
        for(int i = 0; i < dirList.length; i++)
        {
            File f = new File(zipDir, dirList[i]);
            if(f.isDirectory())
            {
                // if the File object is a directory, call this
                // function again to add its content recursively
                String filePath = f.getPath();
                zipDir(filePath, rootDir, zos);
                // loop again
                continue;
            }
            
            // if we reached here, the File object f was not a directory
            // create a FileInputStream on top of f
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream(f);
                // create a new zip entry
                String path = f.getPath().substring(rootDir.length());
                if(path.charAt(0) == File.separatorChar)
                    path = path.substring(1);
                // System.out.println(path);
                ZipEntry anEntry = new ZipEntry(path.replace('\\', '/')); //FIXME what if unix file has \ in name?
                // place the zip entry in the ZipOutputStream object
                zos.putNextEntry(anEntry);
                // now write the content of the file to the ZipOutputStream
                while((bytesIn = fis.read(readBuffer)) != -1)
                {
                    zos.write(readBuffer, 0, bytesIn);
                }
                zos.closeEntry();
            }
            catch(IOException ioe)
            {
                throw ioe;
            }
            finally
            {
                IOUtils.close(fis);
            }
        }       
    }*/
    
    private class TemplateFileFilter implements FileFilter
    {       
        public boolean accept(File f)
        {            
            return f.canRead() && f.getName().toLowerCase().endsWith(".template");
        }        
    }
}
