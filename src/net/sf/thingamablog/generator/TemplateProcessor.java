/*
 * Created on Jan 28, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


public class TemplateProcessor
{	
	private static final String LT = "<";
	private static final String GT = ">";
	private static final String END_LT = "</";
	private static final String TAG_LT = LT + '$';
	private static final String TAG_GT = '$' + GT;
	
    public String processTemplate(String tmpl, TemplateContainer root)
	{
        List temps = parseContainers(tmpl, root);
        for(Iterator it = temps.iterator(); it.hasNext();)
		{			
            String curTmp = it.next().toString();
			StringWriter sw = new StringWriter(curTmp.length() * 2);
			
			try
			{
			    writeContainer(curTmp, root, sw);
			}
			catch(IOException ioe){}
			
			StringBuffer buf = sw.getBuffer();
			tmpl = replace(curTmp, buf.substring(0, buf.length()), tmpl);			
		}		
		
        return tmpl;
	}
    
    public void writeContainer(String tmpl, TemplateContainer cont, Writer writer)
    throws IOException
    {		
		cont.initialize(parseAttributes(tmpl, cont));		
		if(cont.isVisible())
		{
		    do
		    {	        
		        String result = processVariables(stripOffTags(tmpl, cont), cont);		       
		        if(cont.prefix() != null)
		            writer.write(cont.prefix());		        
		        writer.write(processContainers(result, cont));		        
		        if(cont.postfix() != null)
		            writer.write(cont.postfix());
		    }
		    while(cont.processAgain());
		}
    }
    
    public List parseContainers(String tmpl, TemplateContainer tc)
    {
		int _pos = 0;
        Vector temps = new Vector();
		String containerStart = LT + tc.getName();
		String closeTag = END_LT + tc.getName() + GT;
		
		while(tmpl.indexOf(containerStart, _pos) > -1)
		{
			int s = tmpl.indexOf(containerStart, _pos);//open tag begin
			if(s < 0)//not found
				continue;
			
			_pos = s + tc.getName().length() + 1; //update pos;
			
			char ch = tmpl.charAt(_pos);
			if(ch != '>' && !Character.isWhitespace(ch))
				continue;   //this isn't the right tag, 
							//it only starts like the one we're looking for
			//System.out.println("After tag = " + t.charAt(pos));
			
			//now we have to find the end ">" of the open tag
			int otEnd = tmpl.indexOf(GT, _pos);
			if(otEnd < 0)//incomplete tag
				continue;		
			
			_pos = otEnd;
			
			int e = tmpl.indexOf(closeTag, _pos);
			if(e < 0)//incomple tag
				continue;
				
			e += closeTag.length();
			_pos = e;
			
			temps.add(tmpl.substring(s, e));
		}
		
		return temps;
    }
	
	private String stripOffTags(String t, TemplateContainer tc)
	{
		int s = t.indexOf(GT); //end of open tag
		int e = t.indexOf(END_LT + tc.getName() + GT);//end of close tag
		if(s < 0 || e < 0 || (s + 1) >= t.length())
			return t;
		return t.substring(s + 1, e);
	}    

	private String processVariables(String tmpl, TemplateContainer tc)
	{
	    List list = tc.getTags();
	    if(list != null)
	    {
	        Iterator it = list.iterator();
	        while(it.hasNext())
	        {
	            TemplateTag tag = (TemplateTag)it.next();
	            tmpl = parseVariable(tmpl, tag, tc.getValueForTag(tag));
	        }
	    }	    
	    return tmpl;
	}
	
	private String processContainers(String tmpl, TemplateContainer tc)
	{
        List list = tc.getContainers();        
        if(list != null)
        {
            Iterator it = list.iterator();
            while(it.hasNext())
            {
                tmpl = processTemplate(tmpl, (TemplateContainer)it.next());	
            }
        }        
        return tmpl;
	}
    
	private String parseVariable(String tmpl, TemplateTag tag, Object obj)
	{		
		int _pos = 0;
		String attribsStart = TAG_LT;
		String attribsEnd = TAG_GT;
		String tagStart = attribsStart + tag.getName();
		
		while((_pos = tmpl.indexOf(tagStart, _pos)) > -1)
		{
			int end = tmpl.indexOf(attribsEnd, _pos) + attribsEnd.length();
			if(end < _pos)//no more
				return tmpl;
			
			String var = tmpl.substring(_pos, end);
			int nameLen = tag.getName().length();
			int normalLen = 
				attribsStart.length() + nameLen + attribsEnd.length();
						
			if(var.length() > normalLen)
			{
				//this tag should have whitespace between the tag name and
				//attribute list because it's longer than
				//the normal, no-attribute, length.
				char c = var.charAt(attribsStart.length() + nameLen);
				if(!Character.isWhitespace(c))
				{				
					//System.out.println(c + " - " + tag.getName() + " - " + var);
					//No whitespace found so
					//this isn't the right tag, it only starts
					//like the one we're looking for, so pass it over
					_pos = end;
					continue;					
				}			
			}
						
			Hashtable ht = parseAttributes(var, tag);
			String val = tag.process(obj, ht);
			tmpl = replace(var, val, tmpl);			
		}
		
		return tmpl;		
	}
    
    private Hashtable parseAttributes(String t, TemplateElement te)
    {
        Hashtable defaults = te.getDefaultAttributes();
        if(defaults == null)//don't allow null attributes
            defaults = new Hashtable();//empty attributes
        Hashtable ht = new Hashtable(defaults);//copy defaults
        
        String attribsStart = getAttribsStart(te) + te.getName();
        String attribsEnd = getAttribsEnd(te);
        int s = t.indexOf(attribsStart, 0);//open tag begin
        if(s == -1)//not found
            return ht;
        
        int e = t.indexOf(attribsEnd, s + attribsStart.length());
        if(e == -1 || e == (s + attribsStart.length()))//no attributes given, use defaults
            return ht;
        
        String attribs = t.substring(s + attribsStart.length(), e).trim();
        //System.out.println(attribs);
        attribs = " " + attribs;//ensure the first attrib has a prefixed space
        char ws[] = {' ', '\n', '\t'};
        
        for(Enumeration eEnum = ht.keys(); eEnum.hasMoreElements();) 
        {
            String key = eEnum.nextElement().toString();
            int p = 0;
            for(int i = 0; i < ws.length; i++)
            {
                p = attribs.indexOf(ws[i] + key);
                if(p != -1)
                    break;
            }           
            if(p == -1)//attribute wasn't found, default will be used
                continue;            
            //skip over the space and attribute name        
            p += key.length() + 1;
            
            try
            {
                //iterate thru whitespace till we hit equals
                while(Character.isWhitespace(attribs.charAt(p++)));             
                if(attribs.charAt(--p) != '=')
                    break;
                //iterate thru whitespace till we hit opening quote
                while(Character.isWhitespace(attribs.charAt(++p)));             
                if(attribs.charAt(p) != '\"')
                    break;
                
                int end = attribs.indexOf("\"", p + 1);//end quote
                if(end == -1)
                    break;//no closing quote
                
                
                //FIXED with trim() this screws up attribs like glue=", "
                //String val = attribs.substring(p, end).trim();
                String val = attribs.substring(p, end);
                //remove quotes 
                if(val.startsWith("\"") /*&& val.length() > 1*/)
                    val = val.substring(1, val.length());
                
                if(val.endsWith("\"") /*&& val.length() > 1*/)
                    val = val.substring(0, val.length() - 1);               
                
                ht.put(key, val);
                //System.out.println(key + " = " +val); 
                
            }
            catch(IndexOutOfBoundsException ex)
            {
                break;
            }
        }
        
        return ht;
    }
	
	private String replace(String var, String val, String tmpl)
	{		
		if(var.equals("") || var == null)
			 return tmpl;
		StringBuffer sb = new StringBuffer(val.length() + tmpl.length());
		sb.append(tmpl);
		while(sb.indexOf(var) != -1 && !var.equals(val))
		{			
			int s = sb.indexOf(var);            
			int e = s + var.length();
			sb.delete(s, e);
			sb.insert(s, val);
		}                
		return sb.substring(0, sb.length());
	}
	
	private String getAttribsStart(TemplateElement te)
	{
	    if(te instanceof TemplateTag)
	        return TAG_LT;
	    return LT;
	}
	
	private String getAttribsEnd(TemplateElement te)
	{
	    if(te instanceof TemplateTag)
	        return TAG_GT;
	    return GT;
	}
}
