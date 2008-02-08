/*
 * Created on Apr 23, 2004
 *
 * This file is part of Thingamablog. ( http://thingamablog.sf.net )
 *
 * Copyright (c) 2004, Bob Tantlinger All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */
package net.sf.thingamablog.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

/**
 * A tag for date-related values
 * 
 * @author Bob Tantlinger
 *
 */
public class DateTag extends TemplateTag
{
	/** Attribute name for the format value */
	public static final String FORMAT = "format";
	public static final String LANG = "lang";
	public static final String COUNTRY = "country";
	
	public static final String RFC822 = "RFC822";
	public static final String RFC822_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";
	
	private Hashtable ht;
	private Locale locale = Locale.getDefault();
	private SimpleDateFormat df;
	
	/**
	 * Constructs a DateTag
	 * 
	 * @param name The name of the tag
	 */
	public DateTag(String name)
	{
		super(name);
		ht = new Hashtable();
		ht.put(FORMAT, "dd/MM/yy h:mm");
		ht.put(LANG, "");
		ht.put(COUNTRY, "");
		df = new SimpleDateFormat(ht.get(FORMAT).toString());
	} 

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpl.TemplateElement#getDefaultAttributes()
     */
    public Hashtable getDefaultAttributes()
    {        
        return ht;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.tmpl.TemplateElement#process(java.lang.Object, java.lang.String, java.util.Hashtable)
     */
    public String process(Object obj, Hashtable attribs)
    {		
        SimpleDateFormat sdf = initFormatter(attribs);
        try
        {
            return sdf.format((Date)obj);
        }
        catch(ClassCastException cce){}
		return "";
    }
    
    private SimpleDateFormat initFormatter(Hashtable ht)
    {        
        if(ht.get(FORMAT).toString().equalsIgnoreCase(RFC822))
        {
            ht.put(FORMAT, RFC822_FORMAT);
            ht.put(LANG, "en");
            ht.put(COUNTRY, "US");            
        }
        
        if(ht.get(LANG).equals("") && ht.get(COUNTRY).equals(""))
        {
            df.applyPattern(ht.get(FORMAT).toString());
            return df; //no locale attribs specified so return default
        }
        
        Locale loc = new Locale(locale.getLanguage(), locale.getCountry());
        if(!ht.get(LANG).equals(""))
            loc = new Locale(ht.get(LANG).toString(), loc.getCountry());
        if(!ht.get(COUNTRY).equals(""))
            loc = new Locale(loc.getLanguage(), ht.get(COUNTRY).toString()); 
        
        return new SimpleDateFormat(ht.get(FORMAT).toString(), loc);
    }

    /**
     * Gets the locale of the date formatter
     * 
     * @return The locale
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Sets the locale of the date formatter
     * 
     * @param locale The locale
     */
    public void setLocale(Locale locale)
    {
        this.locale = locale;
        df = new SimpleDateFormat(ht.get(FORMAT).toString(), locale);
    }
}
