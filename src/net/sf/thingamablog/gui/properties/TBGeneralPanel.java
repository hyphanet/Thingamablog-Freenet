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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.editor.TextEditPopupManager;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBGeneralPanel extends PropertyPanel
{
	private TBWeblog weblog;
	
	private JTextField titleField;
	private JTextArea descrArea;
	private JTextField basePathField;
	private JTextField urlField;
	private JTextField arcUrlField;
	private JTextField mediaUrlField;
	
	private JComboBox localeCombo;
	private JComboBox dateFormatCombo;
	private JComboBox timeFormatCombo;
	
	private static Locale LOCS[] = DateFormat.getAvailableLocales();

	private String[] getDateFormats(Locale locale) {
		String s = Messages.getString("TBGeneralPanel.DateFormatOptions",locale);
		return tokenize(s);
	}
	private String[] getTimeFormats(Locale locale) {
		String s = Messages.getString("TBGeneralPanel.TimeFormatOptions",locale);
		return tokenize(s);
	}
	private String[] tokenize(String s) {
		StringTokenizer st = new StringTokenizer(s,"|");
		String[] formats = new String[st.countTokens()];
		for (int i = 0; i < formats.length; i++) {
			formats[i] = st.nextToken();
		}
		return formats;
	}
	/*
	private static final String DATE_FORMATS[] =
	{
		"EEEE, MMMM dd, yyyy", //$NON-NLS-1$
		"EEE, MMM dd, yyyy", //$NON-NLS-1$
		"EEEE, dd MMMM, yyyy", //$NON-NLS-1$
		"EEEE dd MMMM yyyy", //$NON-NLS-1$
		"EEEE",
		"MMMM dd, yyyy", //$NON-NLS-1$
		"MMMM dd yyyy", //$NON-NLS-1$		
    	
		"MM/dd/yyyy",    		 //$NON-NLS-1$
		"MM/dd/yy", //$NON-NLS-1$
		"dd/MM/yyyy", //$NON-NLS-1$
		"dd/MM/yy", //$NON-NLS-1$
    	
		"dd MMMM yyyy", //$NON-NLS-1$
		"dd MMMM", //$NON-NLS-1$
    	    	
		"yyyy-MM-dd", //$NON-NLS-1$
		"yy-MM-dd", //$NON-NLS-1$
		"yy.MM.dd", //$NON-NLS-1$
		"yyyy.MM.dd", //$NON-NLS-1$
		"EEEE, MM/dd/yyyy", //$NON-NLS-1$
		"EEE, MM/dd/yyyy" //$NON-NLS-1$
	};
    
	private static final SimpleDateFormat GMT_TIME_FORMAT = 
		new SimpleDateFormat("k:mm.ss z"); //$NON-NLS-1$
	private static final String TIME_FORMATS[] =
	{
		"h:mm a", //$NON-NLS-1$
		"h:mm.ss a", //$NON-NLS-1$
		"h:mm a z", //$NON-NLS-1$
		"h:mm.ss a z", //$NON-NLS-1$
		"h:mm a zzzz", //$NON-NLS-1$
		"h:mm.ss a zzzz", //$NON-NLS-1$
		"k:mm", //$NON-NLS-1$
		"k:mm.ss", //$NON-NLS-1$
		//GMT_TIME_FORMAT       
	};	
    */
	
    public TBGeneralPanel(TBWeblog blog)
    {
    	weblog = blog;
    	
    	TextEditPopupManager popupMan = new TextEditPopupManager();
		titleField = new JTextField();
		titleField.setText(weblog.getTitle());
		popupMan.addJTextComponent(titleField);
		
		descrArea = new JTextArea(3, 2);
		//descrArea.setMinimumSize(descrArea.getPreferredSize());		
		descrArea.setLineWrap(true);
		descrArea.setWrapStyleWord(true);
		descrArea.setText(weblog.getDescription());

		
		//DateFormat.getAvailableLocales()
		localeCombo = new JComboBox(LOCS);
		//localeCombo = new JComboBox();
		localeCombo.setSelectedItem(weblog.getLocale());
		localeCombo.setRenderer(new ComboRenderer());
		localeCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String[] dateFormats = getDateFormats((Locale)localeCombo.getSelectedItem());
				replaceComboItem(dateFormatCombo,dateFormats);
				String[] timeFormats = getTimeFormats((Locale)localeCombo.getSelectedItem());
				replaceComboItem(timeFormatCombo,timeFormats);								
			}
		});
		
		dateFormatCombo = new JComboBox(getDateFormats(weblog.getLocale()));
		dateFormatCombo.setSelectedItem(weblog.getPageGenerator().getDateFormat());
		dateFormatCombo.setRenderer(new ComboRenderer());
		
		timeFormatCombo = new JComboBox(getTimeFormats(weblog.getLocale()));
		timeFormatCombo.setSelectedItem(weblog.getPageGenerator().getTimeFormat());
		timeFormatCombo.setRenderer(new ComboRenderer());
		
		basePathField = new JTextField();
		basePathField.setText(weblog.getBasePath());
		popupMan.addJTextComponent(basePathField);
		
		urlField = new JTextField();
		urlField.setText(weblog.getBaseUrl());
		popupMan.addJTextComponent(urlField);
		
		arcUrlField = new JTextField();
		arcUrlField.setText(weblog.getArchiveUrl());
		popupMan.addJTextComponent(arcUrlField);
		
		mediaUrlField = new JTextField();
		mediaUrlField.setText(weblog.getMediaUrl());
		popupMan.addJTextComponent(mediaUrlField);
		
		setLayout(new BorderLayout(5, 5));
		setBorder(new EmptyBorder(5, 5, 5, 5));
		LabelledItemPanel lip1 = new LabelledItemPanel();
		lip1.setBorder(new TitledBorder(Messages.getString("TBGeneralPanel.Weblog"))); //$NON-NLS-1$
		lip1.addItem(Messages.getString("TBGeneralPanel.Site_Title"), titleField); //$NON-NLS-1$
		//JPanel descPanel = new JPanel(new BorderLayout());
		//descPanel.add(new JScrollPane(descrArea), BorderLayout.CENTER);
		//lip1.addItem("Description", descPanel);
		lip1.addItem(Messages.getString("TBGeneralPanel.Description"), new JScrollPane(descrArea)); //$NON-NLS-1$
		lip1.addItem(Messages.getString("TBGeneralPanel.Language"), localeCombo); //$NON-NLS-1$
		lip1.addItem(Messages.getString("TBGeneralPanel.Date_Format"), dateFormatCombo); //$NON-NLS-1$
		lip1.addItem(Messages.getString("TBGeneralPanel.Time_Format"), timeFormatCombo); //$NON-NLS-1$
		
		LabelledItemPanel lip2 = new LabelledItemPanel();
		lip2.setBorder(new TitledBorder(Messages.getString("TBGeneralPanel.Location"))); //$NON-NLS-1$
		lip2.addItem(Messages.getString("TBGeneralPanel.Base_Path"), basePathField); //$NON-NLS-1$
		lip2.addItem(Messages.getString("TBGeneralPanel.Base_URL"), urlField); //$NON-NLS-1$
		lip2.addItem(Messages.getString("TBGeneralPanel.Archive_URL"), arcUrlField); //$NON-NLS-1$
		lip2.addItem(Messages.getString("TBGeneralPanel.Media_URL"), mediaUrlField); //$NON-NLS-1$
		
		add(lip1, BorderLayout.CENTER);
		add(lip2, BorderLayout.SOUTH);
					 	
    }
    private void replaceComboItem(JComboBox combo, String[] items) {
    	combo.removeAllItems();
    	for (int i = 0; i < items.length; i++) {
    		combo.addItem(items[i]);
    	}
	}
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {
        weblog.setPublishAll(true);        
        weblog.setBlogUrls(
        	basePathField.getText(), urlField.getText(), 
        	arcUrlField.getText(), mediaUrlField.getText());
        weblog.setLocale((Locale)localeCombo.getSelectedItem());
        weblog.setTitle(titleField.getText());
        weblog.setDescription(descrArea.getText());
        weblog.getPageGenerator().setDateFormat(dateFormatCombo.getSelectedItem().toString());
		weblog.getPageGenerator().setTimeFormat(timeFormatCombo.getSelectedItem().toString());
					
    }
    

    
    public boolean isValidData()
    {
		String base = urlField.getText();
		String arc = arcUrlField.getText();
		String media = mediaUrlField.getText();
		
		if(basePathField.getText() == null || basePathField.getText().equals("")) //$NON-NLS-1$
		{		
			JOptionPane.showMessageDialog(this,
				Messages.getString("TBGeneralPanel.no_base_path_prompt"), //$NON-NLS-1$
				Messages.getString("TBGeneralPanel.Warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				return false;
		}
		
		if(!isValidUrl(base))
			return false;
		if(!isValidUrl(arc))
			return false;
		if(!isValidUrl(media))
			return false;	
		
		if(!arc.startsWith(base))
		{
			JOptionPane.showMessageDialog(this,
				Messages.getString("TBGeneralPanel.bad_arc_url_prompt"), //$NON-NLS-1$
				Messages.getString("TBGeneralPanel.Warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			
			return false;	
		}
		
		if(!media.startsWith(base))
		{
			JOptionPane.showMessageDialog(this,
				Messages.getString("TBGeneralPanel.bad_media_url_prompt"), //$NON-NLS-1$
				Messages.getString("TBGeneralPanel.Warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			
			return false;	
		}
		
		return true;
    }
    
    private boolean isValidUrl(String u)
    {
    	try
    	{
    		URL url = new URL(u);
    	}
    	catch(Exception ex)//malformed url
    	{
			JOptionPane.showMessageDialog(this,
				u + Messages.getString("TBGeneralPanel.invalid_url_prompt"), //$NON-NLS-1$
				Messages.getString("TBGeneralPanel.Warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
    		return false;
    	}
    	return true;    	
    }
    

    
	private class ComboRenderer extends DefaultListCellRenderer
	{
		private Date date = new Date();
		
		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			if(value instanceof Locale)
			{
				Locale loc = (Locale)value;
				value = loc.getDisplayName();
			}
			else 
			{
				Locale loc = (Locale)localeCombo.getSelectedItem();
				SimpleDateFormat df = new SimpleDateFormat(value.toString(),loc);
				value = df.format(date);				
			}
			
			return super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);
		}
	}

}
