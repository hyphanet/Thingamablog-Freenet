/*
 * Created on Jun 12, 2004
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
package net.sf.thingamablog.feed;

import java.util.Date;

/**
 * Criteria for a Feed search
 * 
 * @author Bob Tantlinger
 *
 */
public class FeedSearch
{
	private Date startRetrievedDate = new Date(0);
	private Date endRetrievedDate = new Date();
	
	private String titleContains = null;
	private String descriptionContains = null;
	
	
    /**
     * Gets text that the description should contain
     * 
     * @return The text
     */
    public String getDescriptionContains()
    {
        return descriptionContains;
    }

    /**
     * Gets the text that the title should contain
     * 
     * @return The title text
     */
    public String getTitleContains()
    {
        return titleContains;
    }

    /**
     * Sets the text the description should contain
     * 
     * @param string The description text
     */
    public void setDescriptionContains(String string)
    {
        descriptionContains = string;
    }

    /**
     * Sets the text the title should contain
     * 
     * @param string Title text
     */
    public void setTitleContains(String string)
    {
        titleContains = string;
    }

    /**
     * Gets the date that items should be before
     * 
     * @return The end date
     */
    public Date getEndRetrievedDate()
    {
        return endRetrievedDate;
    }

    /**
     * Gets the date that items should be after
     * 
     * @return The start date
     */
    public Date getStartRetrievedDate()
    {
        return startRetrievedDate;
    }

    /**
     * Sets the date that items should be before
     * 
     * @param date The end date
     */
    public void setEndRetrievedDate(Date date)
    {
        endRetrievedDate = date;
    }

    /**
     * Sets the date that items should be after
     * 
     * @param date The start date
     */
    public void setStartRetrievedDate(Date date)
    {
        startRetrievedDate = date;
    }

}
