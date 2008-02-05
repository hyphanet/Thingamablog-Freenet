package net.sf.thingamablog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;

/**
 * 
 * Various IO utility methods
 * 
 * @author Bob Tantlinger
 *
 */
public class IOUtils
{

    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * Copies an InputStream to an OutputStream
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copy(InputStream src, OutputStream dst) throws IOException
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        int n = 0;
        while((n = src.read(buffer)) != -1)
        {
            dst.write(buffer, 0, n);
        }
    }

    /**
     * Copies a Reader to a Writer.
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copy(Reader src, Writer dst) throws IOException
    {
        char[] buffer = new char[BUFFER_SIZE];
        int n = 0;
        while((n = src.read(buffer)) != -1)
        {
            dst.write(buffer, 0, n);
        }
    }

    /**
     * Copies a File
     * @param src
     * @param dst
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copy(File src, File dst) throws FileNotFoundException, IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        copy(in, out);
        close(in);
        close(out);
    }

    /**
     * Copies the file at the specified source path to the destination path
     * @param srcPath
     * @param dstPath
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copy(String srcPath, String dstPath) throws FileNotFoundException, IOException
    {
        copy(new File(srcPath), new File(dstPath));
    }

    public static String read(InputStream input) throws IOException
    {
        return read(new InputStreamReader(input));
    }

    /**
     * Reads a File and returns the contents as a String
     * @param file The File to read
     * @return The contents of the file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String read(File file) throws FileNotFoundException, IOException
    {
        return read(new FileReader(file));
    }

    /**
     * Reads a String from a Reader
     * @param input
     * @return
     * @throws IOException
     */
    public static String read(Reader input) throws IOException
    {
        BufferedReader reader = new BufferedReader(input);
        StringBuffer sb = new StringBuffer();
        int ch;

        while((ch = reader.read()) != -1)
            sb.append((char)ch);

        close(reader);
        return sb.toString();
    }


    /**
     * Writes a String to a File using a PrintWriter
     * @param file
     * @param str
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void write(File file, String str) throws FileNotFoundException, IOException
    {      
        PrintWriter out = new PrintWriter(new FileOutputStream(file));
        out.print(str);  
        close(out);
    }
    
    /**
     * Writes the raw data from an InputStream to a File
     * @param file
     * @param input
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void write(File file, InputStream input) throws FileNotFoundException, IOException
    {
        InputStream in = new BufferedInputStream(input);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        int ch;
        while((ch = in.read()) != -1)
        {
            out.write(ch);
        }
        
        close(in);
        close(out);
    }

    /**
     * Recursively deletes a directory, thereby removing all its contents
     * @param file the file or dir to delete
     * @return true if the file(s) were successfully deleted
     */
    public static boolean deleteRecursively(File file)
    {
        if(file.isDirectory())
        {
            File[] children = file.listFiles();
            for(int i = 0; i < children.length; i++)
            {
                boolean success = deleteRecursively(children[i]);
                if(!success)
                    return false;
            }
        }

        return file.delete();
    }

    /**
     * Closes a Writer, swallowing any exceptions
     * @param out
     */
    public static void close(Writer out)
    {
        if(out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Closes a Reader, swallowing any exceptions
     * @param in
     */
    public static void close(Reader in)
    {
        if(in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Closes an InputStream, swallowing any exceptions
     * @param in
     */
    public static void close(InputStream in)
    {
        if(in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Closes an Outputstream, swallowing any exceptions
     * @param out
     */
    public static void close(OutputStream out)
    {
        if(out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Closes a RandomAccessFile, swallowing any excepions
     * @param raf
     */
    public static void close(RandomAccessFile raf)
    {
        if(raf != null)
        {
            try
            {
                raf.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }
}