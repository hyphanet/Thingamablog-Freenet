/*
 * Created on Feb 3, 2005
 *
 */
package net.sf.thingamablog.generator;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.TBWeblog;


/**
 * @author Owner
 */
public class CalendarContainer implements TemplateContainer
{

    static final int TOP_PAGE = 0;
    /** Constant for an archive page */
    static final int ARC_PAGE = 1;
    /** Constant for a category page */
    static final int CAT_PAGE = 2;
    
    private String category;
    private ArchiveRange archive;
    
    public static final String NAME = "Calendar";
    
    /**
     * The month attribute 1 - 12
     */
    public static final String MONTH = "month";
    /**
     * The year attribute
     */
    public static final String YEAR = "year";
    
    private Calendar calendar;
    private TBWeblog blog;
    private int pageType;
    private TemplateTag monthLabelTag = new DateTag("MonthLabel");
    
    private int month, year;
    
    
    /**
     * Constructs a CalendarContainer for a top level page
     * 
     * @param wb
     */
    public CalendarContainer(TBWeblog wb)
    {
        _init(wb, TOP_PAGE);
    }
    
    public CalendarContainer(TBWeblog wb, String cat)
    {
        category = cat;
        _init(wb, CAT_PAGE);
    }
    
    public CalendarContainer(TBWeblog wb, ArchiveRange arc)
    {
        archive = arc;
        _init(wb, ARC_PAGE);
    }
    
    
    private void _init(TBWeblog wb, int type)
    {
        blog = wb;
        pageType = type;
        calendar = Calendar.getInstance(blog.getLocale());
        Hashtable ht = monthLabelTag.getDefaultAttributes();
        ht.put(DateTag.FORMAT, "MMMM yyyy");
    }
    
    private int getCurrentMonth()
    {
        Date d = new Date();
        if(pageType == ARC_PAGE)
            d = archive.getStartDate();
        calendar.setTime(d);
        return calendar.get(Calendar.MONTH) + 1;//we want 1 - 12 based months 
    }
    
    private int getCurrentYear()
    {
        Date d = new Date();
        if(pageType == ARC_PAGE)
            d = archive.getStartDate();
        calendar.setTime(d);
        return calendar.get(Calendar.YEAR);  
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#init(java.util.Hashtable)
     */
    public void initialize(Hashtable attribs)
    {        
        month = getCurrentMonth();
        year = getCurrentYear();        
        
        //get the month attrib
        try
        {
            int n = Integer.parseInt(attribs.get(MONTH).toString());
            if(n > 0 && n <= 12)
                month = n;
        }
        catch(Exception ex){}
        
        //get the year attrib
        try
        {
            int n = Integer.parseInt(attribs.get(YEAR).toString());
            if(n >= 1900 && n <= 2100)
                year = n;
        }
        catch(Exception ex){}
        
        //java Calendar months are 0 based (0 - 11), 
        //since attribs go from 1 - 12 decrement month by one
        month--;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#getValueForTag(net.sf.thingamablog.generator.TemplateTag)
     */
    public Object getValueForTag(TemplateTag t)
    {       
        if(t == monthLabelTag)
        {
            Calendar c = Calendar.getInstance(blog.getLocale());
            c.set(year, month, 1, 1, 1, 1);
            return c.getTime();
        }
        return "";
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#getTags()
     */
    public List getTags()
    {        
        Vector v = new Vector();
        v.add(monthLabelTag);
        return v;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#getContainers()
     */
    public List getContainers()
    {        
        ArrayList v = new ArrayList(2);
        v.add(new WeekDayContainer());
        v.add(new CalendarWeekContainer(month, year));
        return v;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#processAgain()
     */
    public boolean processAgain()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#isVisible()
     */
    public boolean isVisible()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#prefix()
     */
    public String prefix()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateContainer#postfix()
     */
    public String postfix()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateElement#getName()
     */
    public String getName()
    {        
        return NAME;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.generator.TemplateElement#getDefaultAttributes()
     */
    public Hashtable getDefaultAttributes()
    {        
        //the defaults are the current month/year        
        Hashtable ht = new Hashtable();
        ht.put(MONTH, getCurrentMonth() + "");
        ht.put(YEAR, getCurrentYear() + "");
        return ht;
    }
    
    private class WeekDayContainer extends ListContainer
    {
        private Vector weekDayLabels = new Vector();
        private HyperTextTag weekDay = new HyperTextTag("WeekDay");
        private ArrayList containers = new ArrayList(0);
        private ArrayList tags = new ArrayList(1);
        
        public WeekDayContainer()
        {
            super("WeekDays");
            getDefaultAttributes().put("long", "0");
            tags.add(weekDay);
        }
        
        public void initListData(boolean asc, Hashtable attribs)
        {
            boolean longNames = false;
            if(attribs.get("long") != null && attribs.get("long").equals("1"))
                longNames = true;
            
            int weekDays[] = new int[7];
            weekDays[0] = Calendar.SUNDAY;
            weekDays[1] = Calendar.MONDAY;
            weekDays[2] = Calendar.TUESDAY;
            weekDays[3] = Calendar.WEDNESDAY;
            weekDays[4] = Calendar.THURSDAY;
            weekDays[5] = Calendar.FRIDAY;
            weekDays[6] = Calendar.SATURDAY;
            if(calendar.getFirstDayOfWeek() == Calendar.MONDAY)
            {
                weekDays[0] = Calendar.MONDAY;
                weekDays[1] = Calendar.TUESDAY;
                weekDays[2] = Calendar.WEDNESDAY;
                weekDays[3] = Calendar.THURSDAY;
                weekDays[4] = Calendar.FRIDAY;
                weekDays[5] = Calendar.SATURDAY;
                weekDays[6] = Calendar.SUNDAY;
            }
            
            DateFormatSymbols dfs = new DateFormatSymbols(blog.getLocale());
            String days[];
            if(longNames)
                days = dfs.getWeekdays();
            else
                days = dfs.getShortWeekdays();
            for(int i = 0; i < weekDays.length; i++)
                weekDayLabels.add(days[weekDays[i]]);
        }
        
        public List getTags()
        {
            return tags;
        }
        
        public List getContainers()
        {
            return containers;
        }
        
        public int getListDataSize()
        {
            return weekDayLabels.size();
        }
        
        public Object getValueForTag(TemplateTag t, int index)
        {
            if(t == weekDay)
            {
                return weekDayLabels.elementAt(index);
            }
            return "";
        }
        
        public boolean isVisible()
        {
            return true;
        }
    }
    
    private class CalendarWeekContainer extends ListContainer
    {
        private int w_month, w_year;
        private Calendar cal;
        private Vector weekStarts = new Vector();
        private DateTag dateOfWeek = new DateTag("DateOfWeek");
        private ArrayList tags = new ArrayList(1);
        
        public CalendarWeekContainer(int m, int y)
        {
            super("CalendarWeek");
            w_month = m;
            w_year = y;
            cal = Calendar.getInstance(blog.getLocale());
            dateOfWeek.setLocale(blog.getLocale());
            tags.add(dateOfWeek);
        }
        
        public void initListData(boolean asc, Hashtable attribs)
        {
            cal.set(w_year, w_month, 1, 0, 0, 0);
            int curMonth = cal.get(Calendar.MONTH);
        
            //roll back the calendar to the first day of the week       
            while(cal.get(Calendar.DAY_OF_WEEK) != cal.getFirstDayOfWeek())
                cal.add(Calendar.HOUR, -24);        
            
            weekStarts.removeAllElements();
            //loop 5 or 6 times so the cal grid is big enuff
            for(int week = 0; week < 6; week++)
            {
                boolean monthChanged = false;
                weekStarts.add(cal.getTime());
                for(int days = 1; days <= 7; days++)
                {
                    cal.add(Calendar.DAY_OF_YEAR, 1);//next day
                    monthChanged = week >= 4 && cal.get(Calendar.MONTH) != curMonth;
                }
                
                //we don't need to generate another week
                if(monthChanged)
                    break;              
            }
        }
        
        public int getListDataSize()
        {
            return weekStarts.size();
        }
        
        public List getTags()
        {
            return tags;
        }
        
        public List getContainers()
        {
            ArrayList v = new ArrayList(1);
            Date d = (Date)weekStarts.elementAt(currentIndex());
            TemplateContainer tc;               
            if(pageType == ARC_PAGE)
                tc = new CalendarDayContainer(blog, d, archive, w_month);
            else if(pageType == CAT_PAGE)
                tc = new CalendarDayContainer(blog, d, category, w_month);
            else
                tc = new CalendarDayContainer(blog, d, w_month);
            
            v.add(tc);
            return v;
        }
        
        public Object getValueForTag(TemplateTag t, int index)
        {
            if(t == dateOfWeek)
                return weekStarts.elementAt(index);
            return "";
        }
        
        public boolean isVisible()
        {
            return true;
        }
    }
}
