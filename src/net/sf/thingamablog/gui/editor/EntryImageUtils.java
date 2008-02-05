/*
 * Created on May 23, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.thingamablog.blog.Weblog;


/**
 * @author Bob Tantlinger
 */
public class EntryImageUtils
{

    private static String extractSrc(String tag)
    {
        String srcAtt = "src=\"";
        int s = tag.indexOf(srcAtt) + srcAtt.length();
        if(s == -1)
            return null;

        int e = tag.indexOf("\"", s);
        if(e == -1)
            return null;
        String src = tag.substring(s, e);

        //System.out.println("SRC ====>>> " + src);

        String st = src.toLowerCase();
        if(st.endsWith(".gif") || st.endsWith(".png") || st.endsWith(".jpg") || st.endsWith(".jpeg"))
            return src;

        return null;
    }
	
	private static String replace(String text, String replace, String with)
	{
	    StringBuffer sb = new StringBuffer(text);
	    int start = sb.indexOf(replace);
	    if(start != -1)
	    {
	        try
	        {
	            sb.delete(start, start + replace.length());
	            sb.insert(start, with);	                        
	        }
	        catch(Exception ex)
	        {
	            ex.printStackTrace();
	        }
	    }	    
	    return sb.toString();
	}
	
    public static List parseImageTags(String text)
	{
	    ArrayList list = new ArrayList();
	    String img[] = {"<img", "<IMG"};
	    
	    for(int i = 0; i < img.length; i++)
	    {	        
	        int p = 0;
	        while((p = text.indexOf(img[i], p)) != -1)
	        {	        
	            int end = text.indexOf(">", p);
	            if(end == -1)
	                break;
	            list.add(text.substring(p, end + 1));
	            p = end;
	        }
	    }
	    
	    return list;
	}

    public static String changeLocalImageURLs(String html, Weblog blog)
    {
        String mediaURL = blog.getMediaUrl();
        File dir = getImageDirectory(blog);
        
        List tags = parseImageTags(html);
        for(int i = 0; i < tags.size(); i++)
        {
            String oldTag = tags.get(i).toString();
            String src = extractSrc(oldTag);
            if(src == null)
                continue;

            try
            {
                String imgDir = dir.toURL().toExternalForm();
                File imgFile = new File(src);
                imgFile = new File(dir, imgFile.getName());

                if(imgFile.exists() && src.startsWith(imgDir) && oldTag.indexOf(src) != -1)
                {
                    String nURL = mediaURL + imgFile.getName();
                    String newTag = replace(oldTag, src, nURL);
                    //System.err.println(oldTag + " -> " + newTag);
                    html = replace(html, oldTag, newTag);
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
        return html;
    }

    public static File getImageDirectory(Weblog blog)
    {
        File dir = blog.getWebFilesDirectory();
        String base = blog.getBaseUrl();
        String media = blog.getMediaUrl();

        if(media.startsWith(base) && !media.equals(base))
        {
            StringBuffer s = new StringBuffer(media.substring(base.length()));
            if(s.toString().startsWith("/"))
                s.deleteCharAt(0);

            dir = new File(dir, s.toString());            
        }

        return dir;
    }
}
