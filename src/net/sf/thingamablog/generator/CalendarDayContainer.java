package net.sf.thingamablog.generator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.EntryEnumeration;
import net.sf.thingamablog.blog.TBWeblog;


public class CalendarDayContainer extends ListContainer
{
	static final int TOP_PAGE = 0;
	/** Constant for an archive page */
	static final int ARC_PAGE = 1;
	/** Constant for a category page */
	static final int CAT_PAGE = 2;
    
    private Calendar cal;
    private static Calendar monthCheck = Calendar.getInstance();
    
    private int numDays = 7;
    private Vector days = new Vector();
    private Date startingDay;
    private TBWeblog blog;
    
	private TextTag calendarDay = new TextTag("DayOfMonth");
	private DateTag calendarDate = new DateTag("DateOfDay");
	
	private List containers;
	private DayContainer ifCurrentDay, ifDayHasEntries, ifDayHasNoEntries;
	private DayContainer ifEmptySpace;
	
	private int pageType;
	private String category;
	private ArchiveRange archive;
	private int month = -1;
    
    public CalendarDayContainer(TBWeblog blog, Date start, int m)
    {
        super("CalendarDay"); 
        _init(TOP_PAGE, blog, start, m);        
    }
    
    public CalendarDayContainer(TBWeblog blog, Date start, ArchiveRange arc, int m)
    {
        super("CalendarDay"); 
        archive = arc;
        _init(ARC_PAGE, blog, start, m);        
    }
    
    public CalendarDayContainer(TBWeblog blog, Date start, String cat, int m)
    {
        super("CalendarDay"); 
        category = cat;
        _init(CAT_PAGE, blog, start, m);        
    }
    
    private void _init(int type, TBWeblog wb, Date start, int m)
    {
        blog = wb;
        month = m;
        cal = Calendar.getInstance(blog.getLocale());
        pageType = type;
        startingDay = start;
        
        ifCurrentDay = new IfCurrentDayContainer();
        ifDayHasEntries = new IfDayHasEntriesContainer();
        ifDayHasNoEntries = new IfDayHasNoEntriesContainer();
        ifEmptySpace = new IfEmptySpaceContainer();
        containers = new ArrayList(4);
        containers.add(ifCurrentDay);
        containers.add(ifDayHasEntries);
        containers.add(ifDayHasNoEntries);
        containers.add(ifEmptySpace);
    }
    

    
    public void initListData(boolean asc, Hashtable attribs)
    {
        days.removeAllElements();
        cal.setTime(startingDay);
	    for(int d = 0; d < numDays; d++)
		{
	        days.add(cal.getTime());
	        cal.add(Calendar.DAY_OF_YEAR, 1);//next day		        
		}
    }
    
    public boolean isVisible()
    {
        return true;
    }
    
    public List getTags()
    {
        return null;
    }
    
    public List getContainers()
    {        
        Date d = (Date)days.elementAt(currentIndex());        
        ifDayHasEntries.setDay(d);
        ifDayHasNoEntries.setDay(d);
        ifCurrentDay.setDay(d);
        ifEmptySpace.setDay(d);
        
        return containers;
    }
    
    public Object getValueForTag(TemplateTag t, int i)
    {
        return "";
    }
    
    public int getListDataSize()
    {
        return days.size();
    }
    
    private String dayString(Date d)
    {
        cal.setTime(d);
		return cal.get(Calendar.DAY_OF_MONTH) + "";
    }
    
	private BlogEntry getFirstEntryForDay(Date d)
	{
		Date d2 = new Date(d.getTime());
		ArchiveRange arc = new ArchiveRange(d, d2);
		BlogEntry be = null;
		try
		{
			EntryEnumeration eEnum = blog.getBackend().getEntriesBetween(
				blog.getKey(), arc.getStartDate(), arc.getExpirationDate(), true);			
			while(eEnum.hasMoreEntries())
			{
				if(pageType == CAT_PAGE)
				{
					BlogEntry e = eEnum.nextEntry();
					String cats[] = e.getCategories();
								
					boolean found = false;
					for(int i = 0; i < cats.length; i++)
					{
						if(cats[i].equals(category))
						{
							be = e;
							found = true;
							break;
						}
					}
					
					if(found)
						break;//break out of while					
				}
				else
				{				
					be = eEnum.nextEntry();
					break;//we only want this entry
				}
			}
			
			eEnum.close();			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();			
		}
		
		return be;		
	}
    
    private boolean hasEntriesForDay(Date d)
    {	
        //if this cal is on an arc page, we only want the days on the page
		//to be highlighted in the cal
		if(pageType == ARC_PAGE)
		{
			if(d.before(archive.getStartDate()))
				return false;
			if(d.after(archive.getExpirationDate()))
				return false;
		}
		
		Date d2 = new Date(d.getTime());
		ArchiveRange arc = new ArchiveRange(d, d2);
		
		try
		{			 
			//we have to check if the blog's archive base date is
			//after the day, otherwise we'll get a calendar with
			//links to non-existant entries
			if(blog.getArchiveBaseDate().after(arc.getStartDate()))							
				return false;			
			
			EntryEnumeration eEnum = blog.getBackend().getEntriesBetween(
					blog.getKey(), arc.getStartDate(), arc.getExpirationDate(), false);			
			
			boolean hasEntries = false;
			if(pageType == TOP_PAGE || pageType == ARC_PAGE)
			{			
				//doesn't matter which cat
				hasEntries = eEnum.hasMoreEntries();				
			}
			else if(pageType == CAT_PAGE)
			{
				while(eEnum.hasMoreEntries())//find first matching cat
				{
					BlogEntry be = eEnum.nextEntry();
					String cats[] = be.getCategories();
										
					for(int i = 0; i < cats.length; i++)
					{
						if(cats[i].equals(category))
						{
							hasEntries = true;
							break;
						}
					}
					
					if(hasEntries)
						break;
				}
			}
			
			eEnum.close();
			return hasEntries;			
		}
		catch(Exception ex){}
		
		return false;
    }
    

    
    private abstract class DayContainer extends BasicContainer
    {
        private Date day;
        
        public DayContainer(String name)
        {
            super(name);
        }
        
        public void setDay(Date d)
        {
            day = d;
        }
        
        public Date getDay()
        {
            return day;
        }
        
        public boolean isCurrentMonth()
        {
            monthCheck.setTime(day);
            return monthCheck.get(Calendar.MONTH) == month;
        }
    }
     
    private class IfDayHasEntriesContainer extends DayContainer //BasicContainer
    {        
        //private Date day;
		private TemplateTag entryIDTag = new TextTag("EntryID");
		private TemplateTag entryArcPageTag = new TextTag("EntryArchivePage");
		
        
        public IfDayHasEntriesContainer()
        {
            super("IfDayHasEntries");
            registerTag(calendarDay);
            registerTag(calendarDate);
            registerTag(entryIDTag);
            registerTag(entryArcPageTag);
            //day = d;
        }        
 
        
        public Object getValueForTag(TemplateTag tag)
        {
            if(tag == calendarDay)
                return dayString(getDay());
            if(tag == calendarDate)
                return getDay();
            if(tag == entryIDTag)
            {
                BlogEntry be = getFirstEntryForDay(getDay());
                if(be != null)
                    return be.getID() + "";
            }
            if(tag == entryArcPageTag)
            {
    			ArchiveRange ar = blog.getArchiveForDate(getDay());
    			if(ar != null)
    			    return blog.getArchiveUrl() + blog.getArchiveFileName(ar);
            }            
            
            // no entries/something went wrong
            return dayString(getDay());
        }
        
        public boolean isVisible()
        {
            return hasEntriesForDay(getDay()) && isCurrentMonth();
        }
    }
    
    private class IfDayHasNoEntriesContainer extends DayContainer//BasicContainer
    {
        //private Date day;
        
        public IfDayHasNoEntriesContainer()
        {
            super("IfDayHasNoEntries");
            registerTag(calendarDay);
            registerTag(calendarDate);
            //day = d;
        } 
        
        public Object getValueForTag(TemplateTag tag)
        {
            if(tag == calendarDay)
                return dayString(getDay());
            if(tag == calendarDate)
                return getDay();
            return "";
        }
        
        public boolean isVisible()
        {
            return !hasEntriesForDay(getDay()) && isCurrentMonth();
        }
    }
    
    private class IfCurrentDayContainer extends DayContainer//BasicContainer
    {
        //private Date day;
        
        public IfCurrentDayContainer()
        {
            super("IfCurrentDay");            
        }
         
        public Object getValueForTag(TemplateTag tag)
        {
            return "";
        }
        
        public boolean isVisible()
        {
			Calendar c = Calendar.getInstance(blog.getLocale());
			int dOfy = c.get(Calendar.DAY_OF_YEAR);
			c.setTime(getDay());
			return c.get(Calendar.DAY_OF_YEAR) == dOfy && isCurrentMonth();
        }
    }
    
    private class IfEmptySpaceContainer extends DayContainer
    {        
        public IfEmptySpaceContainer()
        {
            super("IfEmptySpace");            
        }
        
        public Object getValueForTag(TemplateTag tag)
        {
            return "";
        }
        
        public boolean isVisible()
        {
            return !isCurrentMonth();
        }
    }
}