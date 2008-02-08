/*
 * Created on Jan 30, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.thingamablog.xml.Entities;

import org.w3c.tidy.Tidy;

public class HyperTextTag extends TextTag
{
	public static final String ENCODE_HTML = "encode_html";
	public static final String STRIP_HTML = "strip_html";
	public static final String WORDS = "words";
    public static final String TIDY_HTML = "tidy_html";    
    public static final String CLOSE_EMPTY_TAGS = "close_empty_tags";
    public static final String EMPTY_TAGS[] = {"br", "hr", "img", "input", "area"};
    public static final String FIND = "find";
    public static final String REPLACE = "replace";
    public static final Object EMPTY = new Object();  
    
    //undocumented and experimental attributes
    public static final String ESCAPE = "escape";
    public static final String UNESCAPE = "unescape";
    //escape types
    private static final Hashtable ENTITIES = new Hashtable();
    static
    {
        ENTITIES.put("xml", Entities.XML);
        ENTITIES.put("html_basic", Entities.HTML_BASIC);
        ENTITIES.put("html32", Entities.HTML32);
        ENTITIES.put("html40", Entities.HTML40); 
        ENTITIES.put("html40_full", Entities.HTML40_FULL);
    }
    
    public HyperTextTag(String name)
    {
        super(name);
        Hashtable ht = getDefaultAttributes();
		ht.put(ENCODE_HTML, "0");
		ht.put(STRIP_HTML, "0");
		ht.put(WORDS, "0");
        ht.put(TIDY_HTML, "0");
        ht.put(CLOSE_EMPTY_TAGS, "0");        
        ht.put(FIND, "");        
        ht.put(REPLACE, EMPTY); 
        
        ht.put(ESCAPE, "0");
        ht.put(UNESCAPE, "0");
    }
    
    private String findReplace(String text, Hashtable attrs)
    {
        Object rep = attrs.get(REPLACE);
        if(!attrs.get(FIND).toString().equals("") && rep != EMPTY)
        {
            String regex = attrs.get(FIND).toString().replaceAll("\\&quot;", "\\\"");            
            String replace = rep.toString().replaceAll("\\&quot;", "\\\"");
            
            List regexs = tokenizeFindReplaceValues(regex);
            List reps = tokenizeFindReplaceValues(replace);
            for(int i = 0; i < regexs.size(); i++)
            {
                String re = regexs.get(i).toString();
                String rp = "";
                try{
                    rp = reps.get(i).toString();
                }catch(IndexOutOfBoundsException ex){}
                //System.out.println(re + "-" + rp);
                try{
                    text = text.replaceAll(re, rp);
                }catch(Exception ex){}
            }
        }
        
        return text;
    }
    
    private List tokenizeFindReplaceValues(String val)
    {
        String delim = ",";
        int pos = 0;
        Vector tokens = new Vector();
        
        while(pos != -1)
        {
            int npos = val.indexOf(delim, pos); 
            while(npos > 0 && val.charAt(npos - 1) == '\\')            
                npos = val.indexOf(delim, npos + delim.length());
            
            String tok;
            if(npos == -1)               
                tok = val.substring(pos, val.length());
            else
            {
                tok = val.substring(pos, npos).trim();
                npos += delim.length();
            }
            
            tokens.add(tok.trim());
            pos = npos;           
        }
        
        return tokens;
    }
    
    
    
    public String process(Object obj, Hashtable attribs)
    {       
        String text = obj.toString();
        
        text = findReplace(text, attribs);
        
        int len = 0;
        try{
        	len = Integer.parseInt(attribs.get(WORDS).toString());
        }catch(Exception ex){}
        
        if(attribs.get(CLOSE_EMPTY_TAGS).toString().equals("1"))
            for(int i = 0; i < EMPTY_TAGS.length; i++)
                text = closeEmptyTags(text, EMPTY_TAGS[i]);
        	
        if(len > 0)
            text = tidyHTML(limitWords(text, len));
        
        if(attribs.get(TIDY_HTML).toString().equals("1"))
            text = tidyHTML(text);
        
        if(attribs.get(STRIP_HTML).toString().equals("1"))           
            text = tidyHTML(text).replaceAll("\\<.*?\\>","");
                    
        if(attribs.get(ENCODE_HTML).toString().equals("1"))
        	text = encodeHTML(text);
        
        if(!attribs.get(ESCAPE).toString().equals("0"))
        {
            Entities e = getEntityMap(attribs.get(ESCAPE).toString().trim());
            text = e.escape(text);  
            System.out.println("\nESCAPED...\n" + text);
        }
        
        if(!attribs.get(UNESCAPE).toString().equals("0"))
        {
            String atr = attribs.get(UNESCAPE).toString().trim(); 
            Entities e = getEntityMap(atr);
            if(atr.equals("1"))
                text = e.unescape(text); 
            else
            {
                text = e.unescapeUnknownEntities(text);              
            }
            
            System.out.println("\nUNESCAPED...\n" + text);
        }
        
        return text;
    }
    
    private Entities getEntityMap(String type)
    {        
        Entities ents = (Entities)ENTITIES.get(type);
        if(ents == null)
            ents = Entities.HTML40_FULL;
        
        return ents;
    }
        
    private String tidyHTML(String html)
    {
        
       Tidy tidy = new Tidy();
       tidy.setXHTML(true);
       tidy.setQuiet(true);
       tidy.setShowWarnings(false);
       System.err.println(tidy.getOutputEncoding());
       tidy.setOutputEncoding("UTF-8");
       
       StringReader reader = new StringReader(html);
       StringWriter writer = new StringWriter();
       tidy.parse(reader, writer);
       
       html = writer.toString();
       String bodyStart = "<body>";
       String bodyEnd = "</body>";
       
       int s = html.indexOf(bodyStart);
       int e = html.lastIndexOf(bodyEnd);
       if(s != -1 && e != -1)
       {
           html = html.substring(s + bodyStart.length(), e);
       }    
       
       //System.out.println("\n\nTIDY....\n" + html);
       
       //tidy.
        
        /*StringReader reader = new StringReader(html);
        StringWriter writer = new StringWriter();
        
        XMLWriter xmlw = new XMLWriter(writer);
        xmlw.setHTMLMode(true);       
        
        XMLReader r = new org.ccil.cowan.tagsoup.Parser();
        
        try
        {
            r.setFeature(Parser.namespacesFeature, false);
            r.setFeature(Parser.namespacePrefixesFeature, false);
            r.setContentHandler(xmlw);
            r.setProperty(Parser.lexicalHandlerProperty, xmlw);
        
            InputSource isrc = new InputSource(reader);
            r.parse(isrc);
            
            html = writer.toString();
            String bodyStart = "<body>";
            String bodyEnd = "</body>";
            
            int s = html.indexOf(bodyStart);
            int e = html.lastIndexOf(bodyEnd);
            if(s != -1 && e != -1)
            {
                html = html.substring(s + bodyStart.length(), e);
            }            
        }
        catch(Exception ex){}
        System.out.println("\n\nTIDY....\n" + html);*/
        return html;
    }
    
    private String closeEmptyTags(String html, String tagName)
    {
        if(!tagName.startsWith("<"))
            tagName = "<" + tagName;
        
        StringBuffer text = new StringBuffer(html);
        
        int p = 0;
        while((p = text.indexOf(tagName, p)) != -1)
        {           
            int end = text.indexOf(">", p);
            if(end == -1)
                break;
            String tag = text.substring(p, end + 1);            
            p = end;
         
            if(tag.indexOf("\n") == -1 && text.charAt(end - 1) != '/')
            {
                text.insert(end, " /");
                p += 2;
            }
        }
        
        return text.toString();
    }
    
	private String encodeHTML(String string)
	{
	    //return Entities.HTML40.escape(string, false);
        
        StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		//boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++)
		{
			c = string.charAt(i);
			if(c == ' ')
			{
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you lose 
				// word breaking
			    
				//if(lastWasBlankChar)
				//{
				//	lastWasBlankChar = false;
				//	sb.append("&amp;nbsp;");
				//}
				//else
				//{
				//	lastWasBlankChar = true;
					sb.append(' ');
				//}
			}
			else
			{
				//lastWasBlankChar = false;
				//
				// HTML Special Chars
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				//else if (c == '\n')
				// Handle Newline
				//	sb.append("&lt;br/&gt;");
				else
				{
					//int ci = 0xffff & c;
					//if (ci < 160)
						// nothing special only 7 Bit
						sb.append(c);
					//else
					//{
						// Not 7 Bit use the unicode system
						//sb.append("&#");
						//sb.append(new Integer(ci).toString());
						//sb.append(';');
					//}
				}
			}
		}
		return sb.toString();
	}
    
	/*
	// added by John Montgomery - strips HTML tags from entries
	private String stripHTML(String s)
	{
		if (s.indexOf('<') < 0 && s.indexOf('>') < 0 && s.indexOf('&') < 0)
			return s;

		StringBuffer buffer = new StringBuffer(s.length());

		int index = -1;
		while ((index = s.indexOf('<')) != -1)
		{
			String head = s.substring(0, index);
			String tail = s.substring(index);
			buffer.append(head);
			index = tail.indexOf('>');
			if (index != -1) // if it's -1 we're partway thru a tag
			{
				tail = tail.substring(index + 1);
			}
			else
			{
				//convert broken tags or trailing '<' to &lt 
				//so we don't get stuck in an infinte loop				
				tail = encodeHTML(tail);
			}
			s = tail;
		}
		buffer.append(s); // add what ever is left over		
		return encodeHTML(buffer.toString().trim());
	}
	*/
    
	private String limitWords(String text, int n)
	{
		StringTokenizer st = new StringTokenizer(text);
		int count = 0;
		String words = "";

		while(st.hasMoreTokens() && count <= n)
		{
			words += st.nextToken();
            if(count < n)
                words += ' ';
			count++;
		}
		
		if(st.hasMoreTokens())
		    words += "...";
		return words;
	}    
 
}
