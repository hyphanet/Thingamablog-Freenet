
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

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.transport.FCPTransport;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBGeneralPanel extends PropertyPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    
    private TBWeblog weblog;
	
	private JTextField titleField;
	private JTextArea descrArea;
        private JTextField typeField;
	private JTextField basePathField;
	private JTextField urlField;
	private JTextField arcUrlField;
	private JTextField mediaUrlField;
	
	private JComboBox localeCombo;
	private JComboBox dateFormatCombo;
	private JComboBox timeFormatCombo;
	
	private static Locale LOCS[] = DateFormat.getAvailableLocales();
        
	private String[] getDateFormats(Locale locale) {
		String s = i18n.str("DateFormatOptions",locale);
		return tokenize(s);
	}
	private String[] getTimeFormats(Locale locale) {
		String s = i18n.str("TimeFormatOptions",locale);
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
        boolean setEditable = true;
        if (weblog.getPublishTransport() instanceof FCPTransport)
            setEditable = false;
    	
    	TextEditPopupManager popupMan = TextEditPopupManager.getInstance();
		titleField = new JTextField();
		titleField.setText(weblog.getTitle());
		popupMan.registerJTextComponent(titleField);
		
		descrArea = new JTextArea(3, 2);
		//descrArea.setMinimumSize(descrArea.getPreferredSize());		
		descrArea.setLineWrap(true);
		descrArea.setWrapStyleWord(true);
		descrArea.setText(weblog.getDescription());
		popupMan.registerJTextComponent(descrArea);

		
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
		                
                typeField = new JTextField();
                typeField.setText(weblog.getType());
                typeField.setEditable(false);                
                
		basePathField = new JTextField();
		basePathField.setText(weblog.getBasePath());
		popupMan.registerJTextComponent(basePathField);
		
		urlField = new JTextField();
		urlField.setText(weblog.getBaseUrl());
		popupMan.registerJTextComponent(urlField);
                urlField.setEditable(setEditable);
		
		arcUrlField = new JTextField();
		arcUrlField.setText(weblog.getArchiveUrl());
		popupMan.registerJTextComponent(arcUrlField);
                arcUrlField.setEditable(setEditable);
		
		mediaUrlField = new JTextField();
		mediaUrlField.setText(weblog.getMediaUrl());
		popupMan.registerJTextComponent(mediaUrlField);
                mediaUrlField.setEditable(setEditable);
		
		setLayout(new BorderLayout(5, 5));
		setBorder(new EmptyBorder(5, 5, 5, 5));
		LabelledItemPanel lip1 = new LabelledItemPanel();
		lip1.setBorder(new TitledBorder(i18n.str("weblog"))); //$NON-NLS-1$
		lip1.addItem(i18n.str("site_title"), titleField); //$NON-NLS-1$
		//JPanel descPanel = new JPanel(new BorderLayout());
		//descPanel.add(new JScrollPane(descrArea), BorderLayout.CENTER);
		//lip1.addItem("Description", descPanel);
		lip1.addItem(i18n.str("description"), new JScrollPane(descrArea)); //$NON-NLS-1$
		lip1.addItem(i18n.str("language"), localeCombo); //$NON-NLS-1$
		lip1.addItem(i18n.str("date_format"), dateFormatCombo); //$NON-NLS-1$
		lip1.addItem(i18n.str("time_format"), timeFormatCombo); //$NON-NLS-1$
		lip1.addItem(i18n.str("type"), typeField);
                
		LabelledItemPanel lip2 = new LabelledItemPanel();
		lip2.setBorder(new TitledBorder(i18n.str("location"))); //$NON-NLS-1$
		lip2.addItem(i18n.str("base_path"), basePathField); //$NON-NLS-1$
		lip2.addItem(i18n.str("base_url"), urlField); //$NON-NLS-1$
		lip2.addItem(i18n.str("archive_url"), arcUrlField); //$NON-NLS-1$
		lip2.addItem(i18n.str("media_url"), mediaUrlField); //$NON-NLS-1$
		
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
				i18n.str("no_base_path_prompt"), //$NON-NLS-1$
				i18n.str("warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				return false;
		}
		
		if(typeField.getText().equals("internet") && (!isValidUrl(base) || !isValidUrl(arc) || !isValidUrl(media)))
			return false;	
		
                if(typeField.getText().equals("freenet") && (!isValidSSK(base)) || !isValidSSK(arc) || !isValidSSK(media))
                        return false;
                
		if(!arc.startsWith(base))
		{
			JOptionPane.showMessageDialog(this,
				i18n.str("bad_arc_url_prompt"), //$NON-NLS-1$
				i18n.str("warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			
			return false;	
		}
		
		if(!media.startsWith(base))
		{
			JOptionPane.showMessageDialog(this,
				i18n.str("bad_media_url_prompt"), //$NON-NLS-1$
				i18n.str("warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			
			return false;	
		}
		
		return true;
    }
    
    private boolean isValidUrl(String u)
    {
    	try
    	{
    		new URL(u);
    	}
    	catch(Exception ex)//malformed url
    	{
			JOptionPane.showMessageDialog(this,
				u + i18n.str("invalid_url_prompt"), //$NON-NLS-1$
				i18n.str("warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
    		return false;
    	}
    	return true;    	
    }
    
    private boolean isValidSSK(String u)
    {
        // TODO : Check if u match a SSK key
        return true;
    }

    
	private class ComboRenderer extends DefaultListCellRenderer
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
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
