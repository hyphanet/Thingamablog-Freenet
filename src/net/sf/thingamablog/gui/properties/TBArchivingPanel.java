/*
 * Created on May 17, 2004
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
 * 
 */
package net.sf.thingamablog.gui.properties;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.LabelledItemPanel;

import com.tantlinger.jdatepicker.JCalendarComboBox;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBArchivingPanel extends PropertyPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    
    private static final String MONTHLY = i18n.str("monthly"); //$NON-NLS-1$
	private static final String WEEKLY = i18n.str("weekly"); //$NON-NLS-1$
	private static final  String DAY_INTERVAL = i18n.str("day_interval"); //$NON-NLS-1$
	
	private static final String ARC_TYPES[] = {MONTHLY, WEEKLY, DAY_INTERVAL};

	
	private final RangeFormat WEEKLY_DAY_RANGE_FORMATS[] =
	{
		new RangeFormat("MM/dd/yyyy", true), //$NON-NLS-1$
		new RangeFormat("MM/dd", true), //$NON-NLS-1$
		new RangeFormat("yyyy/MM/dd", true), //$NON-NLS-1$
		new RangeFormat("yyyy.MM.dd", true), //$NON-NLS-1$
		new RangeFormat("dd/MM/yyyy", true), //$NON-NLS-1$
		new RangeFormat("dd,MM.yyyy", true), //$NON-NLS-1$
        new RangeFormat("yyyy\u5e74MM\u6708dd\u65e5", true), //$NON-NLS-1$
		new RangeFormat("MMMM dd, yyyy", false), //$NON-NLS-1$
		new RangeFormat("MMMM dd", false), //$NON-NLS-1$
		new RangeFormat("MM/dd", false), //$NON-NLS-1$
		new RangeFormat("MM/dd/yyyy", false), //$NON-NLS-1$
		new RangeFormat("MM.dd.yyyy", false), //$NON-NLS-1$
		new RangeFormat("dd.MM.yyyy", false), //$NON-NLS-1$
		new RangeFormat("yyyy-MM-dd", false), //$NON-NLS-1$
		new RangeFormat("MM dd", false), //$NON-NLS-1$
		new RangeFormat("dd MM", false) //$NON-NLS-1$
        
	};
    
	private final RangeFormat MONTHLY_RANGE_FORMATS[] =
	{
		new RangeFormat("MMMM yyyy", false), //$NON-NLS-1$
		new RangeFormat("yyyy MMMM", false), //$NON-NLS-1$
		new RangeFormat("MMMM, dd yyyy", true), //$NON-NLS-1$
		new RangeFormat("yyyy-dd-MMMM", true), //$NON-NLS-1$
		new RangeFormat("yyyy/dd/MMMM", true), //$NON-NLS-1$
		new RangeFormat("dd.MM.yyyy", true), //$NON-NLS-1$
		new RangeFormat("MM/yy", false), //$NON-NLS-1$
		new RangeFormat("MM.yy", false), //$NON-NLS-1$               
        new RangeFormat("yyyy\u5e74MM\u6708", false)
	};
	
	
	private TBWeblog weblog;
	
	private JCalendarComboBox baseDateCombo;
	private JComboBox arcTypeCombo;
	private SpinnerNumberModel dayIntervalModel;
	//private JTextField arcIndexFileNameField;
	private JComboBox arcListFormatCombo;
	private JComboBox arcPagesExtCombo;
	private JComboBox entryPagesExtCombo;
	private JSpinner daySpinner;
	private JCheckBox oldestFirstCb;
	
	private JCheckBox masterIndexCb;
	private JCheckBox genEntryPagesCb;
	private JTextField indexFileNameField;
	
    
    public TBArchivingPanel(TBWeblog wb)
    {
    	weblog = wb;
    	baseDateCombo = new JCalendarComboBox();
    	baseDateCombo.setDate(weblog.getArchiveBaseDate());
    	
    	arcTypeCombo = new JComboBox(ARC_TYPES);
    	if(weblog.getArchivePolicy() == TBWeblog.ARCHIVE_MONTHLY)
    		arcTypeCombo.setSelectedItem(MONTHLY);
    	else if(weblog.getArchivePolicy() == TBWeblog.ARCHIVE_WEEKLY)
    		arcTypeCombo.setSelectedItem(WEEKLY);
    	else if(weblog.getArchivePolicy() == TBWeblog.ARCHIVE_BY_DAY_INTERVAL)
    		arcTypeCombo.setSelectedItem(DAY_INTERVAL);
    	arcTypeCombo.addItemListener(new ArchiveTypeListener());    	
    	
    	arcPagesExtCombo = new JComboBox(TBGlobals.TEXT_FILE_EXTS);
    	arcPagesExtCombo.setEditable(true);
    	arcPagesExtCombo.setSelectedItem(weblog.getArchivesExtension());
    	
    	entryPagesExtCombo = new JComboBox(TBGlobals.TEXT_FILE_EXTS);
    	entryPagesExtCombo.setEditable(true);
    	entryPagesExtCombo.setSelectedItem(weblog.getEntryPageExtension());
    	
    	arcListFormatCombo = new JComboBox();
        RangeFormat rfa[] = null;
    	if(weblog.getArchivePolicy() == TBWeblog.ARCHIVE_MONTHLY)
            rfa = MONTHLY_RANGE_FORMATS;    		
    	else    		
            rfa = WEEKLY_DAY_RANGE_FORMATS;
        
        arcListFormatCombo.setModel(new DefaultComboBoxModel(rfa));
    	RangeFormat rformat = new RangeFormat(
    		weblog.getPageGenerator().getArchiveRangeFormat(), 
    		weblog.getPageGenerator().isSpanArcRange());
        for(int i = 0; i < rfa.length; i++)
        {
            if(rfa[i].toString().equals(rformat.toString()))
            {
                arcListFormatCombo.setSelectedIndex(i);
                break;
            }
        }
    	
    	
		dayIntervalModel = new SpinnerNumberModel(10, 1, 1000, 1);
		daySpinner = new JSpinner(dayIntervalModel);
		daySpinner.setEnabled(weblog.getArchivePolicy() == TBWeblog.ARCHIVE_BY_DAY_INTERVAL);
		dayIntervalModel.setValue(new Integer(weblog.getArchiveByDayInterval())); 
		
		
		oldestFirstCb = new JCheckBox(i18n.str("generate_oldest_entries_first")); //$NON-NLS-1$
		oldestFirstCb.setSelected(weblog.getPageGenerator().isArchivePageAscending());
		
		masterIndexCb = new JCheckBox(i18n.str("generate_master_index")); //$NON-NLS-1$
		masterIndexCb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{				
				indexFileNameField.setEditable(masterIndexCb.isSelected());
			}
		});
		masterIndexCb.setSelected(weblog.isGenerateArchiveIndex());
		
		genEntryPagesCb = new JCheckBox(i18n.str("generate_entry_pages"));
		genEntryPagesCb.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        entryPagesExtCombo.setEnabled(genEntryPagesCb.isSelected());
		    }
		});
		genEntryPagesCb.setSelected(weblog.isGenerateEntryPages());
		entryPagesExtCombo.setEnabled(weblog.isGenerateEntryPages());
		
		indexFileNameField = new JTextField(15);
		indexFileNameField.setText(weblog.getArchiveIndexFileName());
		indexFileNameField.setEditable(weblog.isGenerateArchiveIndex());
		TextEditPopupManager.getInstance().registerJTextComponent(indexFileNameField);
		
		
		LabelledItemPanel lip = new LabelledItemPanel();
		lip.addItem(i18n.str("archives_begin_on"), baseDateCombo); //$NON-NLS-1$
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(arcTypeCombo, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(i18n.str("archive_type"), p); //$NON-NLS-1$
		
		p = new JPanel(new BorderLayout());
		p.add(daySpinner, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(i18n.str("day_interval"), p); //$NON-NLS-1$
		
		p = new JPanel(new BorderLayout());
		p.add(arcPagesExtCombo, BorderLayout.WEST);
		lip.addItem(i18n.str("archive_list_format"), arcListFormatCombo); //$NON-NLS-1$
		lip.addItem(i18n.str("archives_extension"), p); //$NON-NLS-1$
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(lip, BorderLayout.CENTER);
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(oldestFirstCb);
		topPanel.add(p, BorderLayout.SOUTH);
		topPanel.setBorder(new TitledBorder(i18n.str("archive_pages"))); //$NON-NLS-1$
		
		Box box = Box.createVerticalBox();
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(masterIndexCb);
		box.add(p);
		LabelledItemPanel lp = new LabelledItemPanel();
		p = new JPanel(new BorderLayout());
		p.add(indexFileNameField, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);
		lp.addItem(i18n.str("file_name"), p); //$NON-NLS-1$
		box.add(lp);
		box.setBorder(new TitledBorder(i18n.str("archive_index_page"))); //$NON-NLS-1$
		
		Box box1 = Box.createVerticalBox();
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(genEntryPagesCb);
		box1.add(p);
		lp = new LabelledItemPanel();
		p = new JPanel(new BorderLayout());
		p.add(entryPagesExtCombo, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);
		lp.addItem(i18n.str("entry_pages_extension"), p);
		box1.add(lp);
		box1.setBorder(new TitledBorder(i18n.str("entry_pages")));
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(box, BorderLayout.NORTH);
		//bottomPanel.add(new JPanel(), BorderLayout.CENTER);
		bottomPanel.add(box1, BorderLayout.CENTER);
		
		//setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH); 
		add(bottomPanel, BorderLayout.CENTER);	 
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {
		weblog.setPublishAll(true);
        weblog.setArchiveBaseDate(baseDateCombo.getDate());
        String archType = arcTypeCombo.getSelectedItem().toString();
        if(archType == MONTHLY)
        	weblog.setArchivePolicy(TBWeblog.ARCHIVE_MONTHLY);
        else if(archType == WEEKLY)
        	weblog.setArchivePolicy(TBWeblog.ARCHIVE_WEEKLY);
        else if(archType == DAY_INTERVAL)
        	weblog.setArchivePolicy(TBWeblog.ARCHIVE_BY_DAY_INTERVAL);
        Integer days = (Integer)dayIntervalModel.getValue();
        weblog.setArchiveByDayInterval(days.intValue());
        weblog.setArchivesExtension(arcPagesExtCombo.getSelectedItem().toString());
        weblog.getPageGenerator().setArchivePageAscending(oldestFirstCb.isSelected());
        RangeFormat rf = (RangeFormat)arcListFormatCombo.getSelectedItem();
        weblog.getPageGenerator().setArchiveRangeFormat(rf.getFormat(), rf.isSpan());
        
        weblog.setGenerateArchiveIndex(masterIndexCb.isSelected());
        weblog.setArchiveIndexFileName(indexFileNameField.getText());
        
        weblog.setGenerateEntryPages(genEntryPagesCb.isSelected());
        weblog.setEntryPageExtension(entryPagesExtCombo.getSelectedItem().toString());
        
        try
        {        
        	weblog.updateArchives();
        }
        catch(Exception ex){}        
                
    }
    
	public boolean isValidData()
	{
		if(masterIndexCb.isSelected())
		{
			if(indexFileNameField.getText() == null ||
			indexFileNameField.getText().equals("")) //$NON-NLS-1$
			{
				JOptionPane.showMessageDialog(this, 
					i18n.str("file_name_prompt"), i18n.str("warning"), //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		return true;
	}
    
	private class ArchiveTypeListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			if(e.getSource() == arcTypeCombo)
			{
				DefaultComboBoxModel cbm = new DefaultComboBoxModel();
				if(arcTypeCombo.getSelectedItem() == MONTHLY)
				{
					daySpinner.setEnabled(false);
					cbm = new DefaultComboBoxModel(MONTHLY_RANGE_FORMATS);
					
				}
				else if(arcTypeCombo.getSelectedItem() == WEEKLY)
				{
					daySpinner.setEnabled(false);
					cbm = new DefaultComboBoxModel(WEEKLY_DAY_RANGE_FORMATS);
				}
				else if(arcTypeCombo.getSelectedItem() == DAY_INTERVAL)
				{
					daySpinner.setEnabled(true);
					cbm = new DefaultComboBoxModel(WEEKLY_DAY_RANGE_FORMATS);
				}
				
				arcListFormatCombo.removeAllItems();
				arcListFormatCombo.setModel(cbm);
			}	
		}	
	}
    
    private class RangeFormat
    {
    	private String format = ""; //$NON-NLS-1$
    	private boolean span;
    	public RangeFormat(String f, boolean s)
    	{
    		format = f;
    		span = s;	
    	}
    	
    	public boolean isSpan()
    	{
    		return span;    
    	}
    	
    	public String getFormat()
    	{
    		return format;
    	}
    	
    	public String toString()
    	{
    		if(span)
    			return format + " - " + format; //$NON-NLS-1$
    		return format;
    	}
    }
}
