/*
 * Created on Jun 11, 2004
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
import java.util.Date;

/**
 * 
 * Class which defines the criteria for a weblog search
 * 
 * @author Bob Tantlinger 
 */
public class WeblogSearch
{
	
	private boolean isFindDrafts;
	private boolean isFindModifiedEntries;
	
	private Date startDate = new Date(0);
	private Date endDate = new Date();
	
	private String category = null;
	private String titleContains = null;
	private String bodyContains = null;
	
	
    /**
     * Gets the text that the body should contain
     * @return body contains text
     */
    public String getBodyContains()
    {
        return bodyContains;
    }

    /**
     * Gets the category to search in
     * @return
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * Gets the date that entries should be before
     * @return
     */
    public Date getEndDate()
    {
        return endDate;
    }

    /**
     * Gets the date enries should be after
     * @return
     */
    public Date getStartDate()
    {
        return startDate;
    }

    /**
     * Gets the text the entry title should contain
     * @return
     */
    public String getTitleContains()
    {
        return titleContains;
    }

    /**
     * Sets the text the entry body should contain
     * @param string
     */
    public void setBodyContains(String string)
    {
        bodyContains = string;
    }

    /**
     * Sets the category entries should have
     * @param string
     */
    public void setCategory(String string)
    {
        category = string;
    }

    /**
     * Sets the date that entries should be before
     * @param date
     */
    public void setEndDate(Date date)
    {
        endDate = date;
    }
    
    /**
     * Sets the date that entries should be after
     * @param date
     */
    public void setStartDate(Date date)
    {
        startDate = date;
    }

    /**
     * Sets the text that entry titles should contain
     * @param string
     */
    public void setTitleContains(String string)
    {
        titleContains = string;
    }

    /**
     * Indicates whether to find drafts or posts
     * @return
     */
    public boolean isFindDrafts()
    {
        return isFindDrafts;
    }

    /**
     * Indicates whether to find modified entries
     * @return
     */
    public boolean isFindModifiedEntries()
    {
        return isFindModifiedEntries;
    }

    /**
     * 
     * @param b
     */
    public void setFindDrafts(boolean b)
    {
        isFindDrafts = b;
    }

    /**
     * @param b
     */
    public void setFindModifiedEntries(boolean b)
    {
        isFindModifiedEntries = b;
    }

}
