/*
 * Created on Oct 31, 2007
 */
package net.sf.thingamablog.blog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.atlanticbb.tantlinger.io.IOUtils;



/**
 * @author Bob Tantlinger
 *
 */
public class ZipExportableTemplatePack extends DiskTemplatePack
{
    private String zipFileName;
    private HashSet filesToInclude;
    
    public ZipExportableTemplatePack(File tmplDir, String zipFileName) throws IllegalArgumentException, IOException 
    {
        this(tmplDir, zipFileName, null);
    }
    
    /**
     * @param tmplDir
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws IOException 
     */
    public ZipExportableTemplatePack(File tmplDir, String zipFileName, File[] webFilesToInclude) throws IllegalArgumentException, IOException
    {
        super(tmplDir);        
        
        if(zipFileName != null)
        {
            if(!zipFileName.toLowerCase().endsWith(".zip"))
                zipFileName = zipFileName + ".zip";
        }
        else
            throw new IllegalArgumentException("zip file name is null");
        
        this.zipFileName = zipFileName;
        
        filesToInclude = new HashSet();
        if(webFilesToInclude == null)
        {            
            webFilesToInclude = IOUtils.getDirectoryContents(srcWebDir);            
        }
        
        for(int i = 0; i < webFilesToInclude.length; i++)
        {
            File f = webFilesToInclude[i];
            filesToInclude.add(f.getAbsolutePath());
        }
    }
    
    public void installPack(File dir) throws IOException
    {
        ZipOutputStream zos = null;
        try
        {            
            zos = new ZipOutputStream(new FileOutputStream(new File(dir, zipFileName)));
            
            //ensure web entry exists
            zos.putNextEntry(new ZipEntry(srcWebDir.getName() + '/'));
            zos.closeEntry();
            
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
    }
    
    private void zipDir(File dir, ZipOutputStream zos) throws IOException
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
            
            if(f.getParentFile().equals(srcTmplDir) || filesToInclude.contains(f.getAbsolutePath())) //do we want to include this file?
            {            
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
        }       
    }    
}
