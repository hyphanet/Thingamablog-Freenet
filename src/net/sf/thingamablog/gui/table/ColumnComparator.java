/*
 * Copyright (C) 2003  Bob Tantlinger
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */


package net.sf.thingamablog.gui.table;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

public class ColumnComparator implements Comparator
{
    protected int index;
    protected boolean ascending;
  
    public ColumnComparator(int index, boolean ascending)
    {
        this.index = index;
        this.ascending = ascending;
    }
  
    public int compare(Object one, Object two) 
    {
        if(one instanceof Vector && two instanceof Vector)
        {
            Vector vOne = (Vector)one;
            Vector vTwo = (Vector)two;
            Object oOne = vOne.elementAt(index);
            Object oTwo = vTwo.elementAt(index);

            if(oOne instanceof String && oTwo instanceof String)
            {
            	String s1 = (String)oOne;
            	String s2 = (String)oTwo;
            	Collator coll = Collator.getInstance();
            	if(ascending)
            	{
            		return coll.compare(s1, s2);
            	}
            	else
            	{
            		return coll.compare(s2, s1);
            	}            		
            }
            else if(oOne instanceof Date && oTwo instanceof Date)
            {
            	Date d1 = (Date)oOne;
            	Date d2 = (Date)oTwo;
            	
            	if(ascending)
            	{
            		return d1.compareTo(d2);
            	}
            	else
            	{
            		return d2.compareTo(d1);
            	}            		
            }
            else if(oOne instanceof Boolean && oTwo instanceof Boolean)
            {
            	boolean b1 = ((Boolean)oOne).booleanValue();
				boolean b2 = ((Boolean)oTwo).booleanValue();
				if(b1 == b2)
					return 0;
				else if(ascending)
				{
					if(b2 == false)
						return -1;
					else
						return 1;
				}
				else
				{
					if(b2 == false)
						return 1;
					else
						return -1;
				}
            }
            else if(oOne instanceof Comparable && oTwo instanceof Comparable)
            {
                Comparable cOne = (Comparable)oOne;
                Comparable cTwo = (Comparable)oTwo;
                if(ascending)
                {
                    return cOne.compareTo(cTwo);
                }
                else
                {
                    return cTwo.compareTo(cOne);
                }
            }
        }
        
        return 1;
    }
}

