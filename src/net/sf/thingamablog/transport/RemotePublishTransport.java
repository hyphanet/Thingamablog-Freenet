/*
 * Created on Oct 17, 2007
 */
package net.sf.thingamablog.transport;

import java.util.StringTokenizer;


/**
 * @author Bob Tantlinger
 *
 */
public abstract class RemotePublishTransport extends RemoteTransport implements PublishTransport
{
    /**
     * Convienence method for splitting a path into an array
     * 
     * @param the path to split
     * @return The individual path elements, or a zero length
     * array if the path can't be split
     */
    protected String[] splitPath(String path)
    {
        return split(path, "/", -1);
    }
    
    private String[] split(String str, String separator, int max)
    {
        StringTokenizer tok = null;
        if(separator == null)
        {
            // Null separator means we're using StringTokenizer's default
            // delimiter, which comprises all whitespace characters.
            tok = new StringTokenizer(str);
        }
        else
        {
            tok = new StringTokenizer(str, separator);
        }

        int listSize = tok.countTokens();
        if(max > 0 && listSize > max)
        {
            listSize = max;
        }

        String[] list = new String[listSize];
        int i = 0;
        int lastTokenBegin = 0;
        int lastTokenEnd = 0;
        while(tok.hasMoreTokens())
        {
            if(max > 0 && i == listSize - 1)
            {
                // In the situation where we hit the max yet have
                // tokens left over in our input, the last list
                // element gets all remaining text.
                String endToken = tok.nextToken();
                lastTokenBegin = str.indexOf(endToken, lastTokenEnd);
                list[i] = str.substring(lastTokenBegin);
                break;
            }
            else
            {
                list[i] = tok.nextToken();
                lastTokenBegin = str.indexOf(list[i], lastTokenEnd);
                lastTokenEnd = lastTokenBegin + list[i].length();
            }
            i++;
        }
        return list;
    }   
}
