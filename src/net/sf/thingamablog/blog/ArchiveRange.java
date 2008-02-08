/*
 * Created on Apr 30, 2004
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

package net.sf.thingamablog.blog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * A range describing an archive. The range includes the start date
 * and expiration date 
 * 
 * @author Bob Tantlinger
 */
public class ArchiveRange
{
    private Date start;
    private Date expire;
    private DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
    private boolean span = true;  
	private static Calendar cal = Calendar.getInstance();   
    
    /**
     * 
     * @param s The start date
     * @param e The expiration date
     * @throws IllegalArgumentException If the expiration date is greater than the
     * start date
     */
    public ArchiveRange(java.util.Date s, java.util.Date e) throws IllegalArgumentException
    {
        if (e.before(s))
            throw new IllegalArgumentException("Expire date must be >= start date");

        //set to 12:00:00.0 AM        
        cal.setTime(s);
        cal.set(cal.get(
        	Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
            0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        start = cal.getTime();

        //set to 11:59:59.59 PM
        cal.setTime(e);
        cal.set(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
            23, 59, 59);
        cal.set(Calendar.MILLISECOND, 59);
        expire = cal.getTime();
    }
    
    /**
     * Gets the start date of this range
     * @return the start date
     */
    public Date getStartDate()
    {
    	return start;
    }
    
    /**
     * Sets the format of this range. The formatter formats the range
     * when the toString() or getFormattedRange() method is sent the object
     * @param df the formatter
     * @param shouldSpan  true indicates whether both dates should
     * be included in the formatted range
     */
    public void setFormatter(DateFormat df, boolean shouldSpan)
    {
    	span = shouldSpan;
    	format = df;
    }
    
    /**
     * Gets the range formatted
     * @return the formatted range
     */
    public String getFormattedRange()
    {
    	String range = format.format(start);
    	if(span)
    		range += " - " + format.format(expire);
    	return range;
    }
    
    /**
     * Gets the expiration date
     * @return The expiration date
     */
    public Date getExpirationDate()
    {
    	return expire;
    }
    
    public String toString()
    {
    	return getFormattedRange();
    }
}