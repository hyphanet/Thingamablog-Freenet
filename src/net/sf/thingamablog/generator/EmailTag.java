/*
 * Created on Mar 9, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.util.Hashtable;


/**
  */
public class EmailTag extends TextTag
{

    public static final String MUNG = "mung";

    private final static String ASCII_CODE = new String(
        " !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~");

    /**
     * @param name
     */
    public EmailTag(String name)
    {
        super(name);
        Hashtable ht = getDefaultAttributes();
        ht.put(MUNG, "0");
    }

    public String process(Object val, Hashtable attribs)
    {        
        String s = super.process(val, attribs);
        if(!attribs.get(MUNG).equals(FALSE))
            return mung(s);       
            
        return s;
    }    
    
    private String mung(String source)
    {
        int n, length = source.length();
        int everyOther = 0;
        StringBuffer dest = new StringBuffer();

        for(int i = 0; i < length; i++)
        {
            n = ASCII_CODE.indexOf(source.charAt(i));
            if(n == -1)
                dest.append(source.charAt(i));
            else if(everyOther != 0)
                dest.append(source.charAt(i));
            else
            {
                Integer entity = new Integer(n + 32);
                dest.append("&#" + entity.toString() + ";");
            }
            
            everyOther++;
            if(everyOther > 1)
                everyOther = 0;
        }

        return dest.toString();
    }

}