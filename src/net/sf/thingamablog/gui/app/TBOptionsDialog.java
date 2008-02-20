/*
 * Created on Jul 13, 2004
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
package net.sf.thingamablog.gui.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.MultilineText;
import net.sf.thingamablog.gui.StandardDialog;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBOptionsDialog extends StandardDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app"); //$NON-NLS-1$
    
    private final String fontSizes[] = new String[] {"8", "9", "10", "11",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								"12", "14", "16", "18", "20", "22", "24",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
								"26", "28", "36", "48", "72"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	
	private File dicts[];
	//private Font font = new Font("Default", Font.PLAIN, 12); //$NON-NLS-1$
	private JComboBox lafCombo;
    private JComboBox langCombo;
	private JComboBox dictionaryCombo;
	private JComboBox fontNameCombo;
	private JComboBox fontSizeCombo;
	private JCheckBox openLastDBCb;
	private JCheckBox splashScreenCb;
	private JCheckBox pingAfterPubCb;
	private JRadioButton layout3ColRb;
	private JRadioButton layout2ColRb;
	
	private JCheckBox useSocksProxyCb;
	private JCheckBox socksProxyAuthCb;
	private JTextField socksHostField;
	private JTextField socksPortField;
	private JTextField socksUserField;
	private JPasswordField socksPasswordField;
        
        private JTextField nodePortTf;
        private JTextField hostNameTf;
	
	private JTextArea feedItemArea;
	private JCheckBox updateNewsCb;
	private SpinnerNumberModel minuteModel;
	private JSpinner minSpinner;
		
	
	//private String curLafName;
	private UIManager.LookAndFeelInfo[] lfinfo;
    
    
    /**
     * @param parent
     * @param title
     */
    public TBOptionsDialog(Frame parent)
    {
        super(parent, ""); //$NON-NLS-1$
        init();
       
    }

    /**
     * @param parent
     * @param title
     */
    public TBOptionsDialog(Dialog parent)
    {
        super(parent, ""); //$NON-NLS-1$
        init();
        
    }
    
    private void init()
    {
    	//init components    	
    	setTitle(i18n.str("options_")); //$NON-NLS-1$
    	JTabbedPane tabs = new JTabbedPane();
    	
		//general components
		lfinfo = UIManager.getInstalledLookAndFeels();
		String[] lfNames = new String[lfinfo.length];
		for(int i = 0; i < lfNames.length; i++) 
		{
			lfNames[i] = lfinfo[i].getName();
		}
		lafCombo = new JComboBox();
		lafCombo.setModel(new DefaultComboBoxModel(lfNames));
		lafCombo.setSelectedItem(UIManager.getLookAndFeel().getName());
        
        Vector locs = new Vector(Arrays.asList(I18n.getAvailableLanguagePackLocales()));
        Collections.sort(locs, new LocaleComparator());
        if(!locs.contains(I18n.getLocale()))
            locs.add(I18n.getLocale());
        langCombo = new JComboBox(locs);        
        langCombo.setSelectedItem(I18n.getLocale());
        langCombo.setRenderer(new LocaleListCellRenderer());
		
		File dictDir = new File(TBGlobals.DICT_DIR);
		dicts = dictDir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isFile() && f.getName().endsWith(".dic"); //$NON-NLS-1$
			}
		});
		
		if(dicts != null)
		    dictionaryCombo = new JComboBox(dicts);
		else
		    dictionaryCombo = new JComboBox();		
		dictionaryCombo.setRenderer(new DictListCellRenderer());
		
		fontSizeCombo = new JComboBox(fontSizes);
		Font editFont = TBGlobals.getEditorFont();
		GraphicsEnvironment ge = 
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		String fontNames[] = ge.getAvailableFontFamilyNames();
		fontNameCombo = new JComboBox(fontNames);
		fontNameCombo.setSelectedItem(editFont.getFamily());
    	
    	openLastDBCb = new JCheckBox(i18n.str("open_previous_database")); //$NON-NLS-1$
    	splashScreenCb = new JCheckBox(i18n.str("show_splash_screen")); //$NON-NLS-1$
    	
    	layout2ColRb = new JRadioButton(i18n.str("two_column_layout"));    	 //$NON-NLS-1$
		layout3ColRb = new JRadioButton(i18n.str("three_column_layout")); //$NON-NLS-1$
		
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(layout2ColRb);
		bGroup.add(layout3ColRb);
		
		pingAfterPubCb = new JCheckBox(i18n.str("ping_after_pub")); //$NON-NLS-1$
		
		feedItemArea = new JTextArea();
		minuteModel = new SpinnerNumberModel(10, 1, 1000, 1);    	
		minSpinner = new JSpinner(minuteModel);		
		updateNewsCb = new JCheckBox(i18n.str("update_feeds")); 	 //$NON-NLS-1$
		updateNewsCb.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{				
				minSpinner.setEnabled(updateNewsCb.isSelected());
			}
		});
				
		
		//proxy components		
		socksHostField = new JTextField(20);
		socksPortField = new JTextField(10);		
		socksUserField = new JTextField(20);
		socksPasswordField = new JPasswordField();
		useSocksProxyCb = new JCheckBox(i18n.str("use_socks_proxy")); //$NON-NLS-1$
		socksProxyAuthCb = new JCheckBox(i18n.str("proxy_requires_authentication")); //$NON-NLS-1$
		ActionListener lst = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateProxyComponentsEnabledState();
			}
		};				
		useSocksProxyCb.addActionListener(lst);		
		socksProxyAuthCb.addActionListener(lst);		
		
                //node components
                nodePortTf = new JTextField(10);
                hostNameTf = new JTextField(20);
			
    	
    	
    	//general tab
    	Box generalPanel = Box.createVerticalBox();
      	
    	JPanel startupPanel = new JPanel(new GridLayout(2, 1));
    	JPanel spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	spacer.add(openLastDBCb);
    	startupPanel.add(spacer);
    	spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	spacer.add(splashScreenCb);
    	startupPanel.add(spacer);
    	startupPanel.setBorder(new TitledBorder(i18n.str("startup"))); //$NON-NLS-1$
    	generalPanel.add(startupPanel);
    	
    	JPanel appearancePanel = new JPanel(new BorderLayout(5, 5));
    	JPanel colsPanel = new JPanel(new GridLayout(2, 1));
    	JPanel colOptPanel = new JPanel(new BorderLayout());
    	spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	spacer.add(layout2ColRb);
    	colOptPanel.add(spacer, BorderLayout.CENTER);
    	colOptPanel.add(new JLabel(
    		UIUtils.getIcon(UIUtils.MISC, "2col.png")), BorderLayout.WEST); //$NON-NLS-1$
    	colsPanel.add(colOptPanel);
    	colOptPanel = new JPanel(new BorderLayout());
		spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacer.add(layout3ColRb);
    	colOptPanel.add(spacer, BorderLayout.CENTER);
    	colOptPanel.add(new JLabel(
            UIUtils.getIcon(UIUtils.MISC, "3col.png")), BorderLayout.WEST); //$NON-NLS-1$
    	colsPanel.add(colOptPanel);
    	appearancePanel.add(colsPanel, BorderLayout.CENTER);    	
    	LabelledItemPanel lip = new LabelledItemPanel();
    	lip.addItem(i18n.str("look_and_feel"), lafCombo); //$NON-NLS-1$
    	lip.addItem(i18n.str("language"), langCombo); //$NON-NLS-1$
        
        appearancePanel.add(lip, BorderLayout.SOUTH);
    	appearancePanel.setBorder(new TitledBorder(i18n.str("appearance"))); //$NON-NLS-1$
    	generalPanel.add(appearancePanel);
    	
    	JPanel pingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	pingPanel.add(pingAfterPubCb);
    	pingPanel.setBorder(new TitledBorder(i18n.str("pinging"))); //$NON-NLS-1$
    	generalPanel.add(pingPanel);
    	
		lip = new LabelledItemPanel();		
		lip.addItem(i18n.str("dictionary"), dictionaryCombo);		 //$NON-NLS-1$
		JPanel p = new JPanel(new BorderLayout(5, 5));
		p.add(fontNameCombo, BorderLayout.CENTER);
		p.add(fontSizeCombo, BorderLayout.EAST);
		lip.addItem(i18n.str("editor_font"), p); //$NON-NLS-1$
		JPanel editorPanel = new JPanel(new BorderLayout());
		editorPanel.add(lip, BorderLayout.CENTER);
		editorPanel.setBorder(new TitledBorder(i18n.str("editors"))); //$NON-NLS-1$
		generalPanel.add(editorPanel);
		
		//News Reader tab
		JPanel newsPanel = new JPanel(new BorderLayout());
		JPanel textPanel = new JPanel(new BorderLayout(5, 5));
		textPanel.add(new JScrollPane(feedItemArea), BorderLayout.CENTER);
		textPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
		String inst = i18n.str("template_prompt");		 //$NON-NLS-1$
		MultilineText descrText = new MultilineText(inst);
		textPanel.add(descrText, BorderLayout.NORTH);
		JPanel formatPanel = new JPanel(new BorderLayout());
		formatPanel.setBorder(new TitledBorder(i18n.str("post_format"))); //$NON-NLS-1$
		formatPanel.add(textPanel, BorderLayout.CENTER);
		JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		updatePanel.add(updateNewsCb);
		updatePanel.add(minSpinner);
		updatePanel.setBorder(new EmptyBorder(4, 4, 4, 4));		
		newsPanel.add(updatePanel, BorderLayout.NORTH);		
		newsPanel.add(formatPanel, BorderLayout.CENTER);
		
		
		//Browser tab
		/*JPanel browserPanel = new JPanel(new BorderLayout());		
		browserPanel.add(Browser.getDialogPanel(this));
		browserPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));*/		
		
		//proxy tab
		JPanel proxyPanel = new JPanel(new BorderLayout());
		spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacer.add(useSocksProxyCb);
		proxyPanel.add(spacer, BorderLayout.NORTH);
		
		lip = new LabelledItemPanel();		
		lip.addItem(i18n.str("proxy_host"), socksHostField); //$NON-NLS-1$
		lip.addItem(i18n.str("proxy_port"), socksPortField); //$NON-NLS-1$
		lip.addItem("", socksProxyAuthCb); //$NON-NLS-1$
		lip.addItem(i18n.str("user_name"), socksUserField); //$NON-NLS-1$
		lip.addItem(i18n.str("password"), socksPasswordField); //$NON-NLS-1$
		lip.setBorder(new TitledBorder(i18n.str("proxy_details"))); //$NON-NLS-1$
		proxyPanel.add(lip, BorderLayout.CENTER);		
		
                //node tab
                JPanel nodePanel = new JPanel(new BorderLayout());
                lip = new LabelledItemPanel();
                lip.addItem(i18n.str("node_port"), nodePortTf);
                lip.addItem(i18n.str("node_hostname"), hostNameTf);
                lip.setBorder(new TitledBorder(i18n.str("node_details")));
                nodePanel.add(lip, BorderLayout.NORTH);
                
		tabs.add(generalPanel, i18n.str("general")); //$NON-NLS-1$
		tabs.add(newsPanel, i18n.str("feed_reader")); //$NON-NLS-1$
		//tabs.add(browserPanel, i18n.str("browser")); //$NON-NLS-1$
		tabs.add(proxyPanel, i18n.str("proxy")); //$NON-NLS-1$
		tabs.add(nodePanel, i18n.str("node"));
                
		setContentPane(tabs);
		setComponentValues();
		pack();
		
		setSize(360, getHeight());
		setResizable(false);		   	    	    	    	
    }
    
    private void setComponentValues()
    {
    	try
    	{    	
    		useSocksProxyCb.setSelected(TBGlobals.isUseSocksProxy());
    		socksHostField.setText(TBGlobals.getSocksProxyHost());
    		socksPortField.setText(TBGlobals.getSocksProxyPort());
    		socksProxyAuthCb.setSelected(TBGlobals.isSocksProxyRequiresLogin());
    		socksUserField.setText(TBGlobals.getSocksProxyUser());
    		socksPasswordField.setText(TBGlobals.getSocksProxyPassword());
                nodePortTf.setText(TBGlobals.getNodePort());
                hostNameTf.setText(TBGlobals.getNodeHostname());
  		
			updateProxyComponentsEnabledState();
    	}
    	catch(NullPointerException npe)
    	{    		   	
    	}
    	
		fontNameCombo.setSelectedItem(TBGlobals.getEditorFont().getFamily());
		fontSizeCombo.setSelectedItem(TBGlobals.getEditorFont().getSize() + ""); //$NON-NLS-1$
		//dictionaryCombo.setSelectedItem(TBGlobals.getDictionaryFile());
		File dict = new File(TBGlobals.DICT_DIR, TBGlobals.getDictionary() + ".dic"); //$NON-NLS-1$
		dictionaryCombo.setSelectedItem(dict);
    	
    	String lfcc = TBGlobals.getLookAndFeelClassName();
    	for(int i = 0; i < lfinfo.length; i++)
    	{
    		if(lfinfo[i].getClassName().equals(lfcc))
    		{
    			lafCombo.setSelectedIndex(i);
    			break;
    		}
    	}
    	
		splashScreenCb.setSelected(TBGlobals.isStartWithSplash());  
    	if(TBGlobals.getLayoutStyle() == TBGlobals.TWO_COL)
    		layout2ColRb.setSelected(true);
    	else
    		layout3ColRb.setSelected(true);
    	
    	pingAfterPubCb.setSelected(TBGlobals.isPingAfterPublish());
    	
    	openLastDBCb.setSelected(TBGlobals.isStartWithLastDatabase());
		//Browser.initPanel();
		
		feedItemArea.setText(FeedItemFormatter.getTemplate());
		minuteModel.setValue(new Integer((TBGlobals.getFeedUpdateInterval()/1000/60)));
		updateNewsCb.setSelected(TBGlobals.isAutoFeedUpdate());
		minSpinner.setEnabled(updateNewsCb.isSelected());
    }
    
    private void updateProxyComponentsEnabledState()
    {
		boolean sel = useSocksProxyCb.isSelected();
		socksHostField.setEditable(sel);
		socksPortField.setEditable(sel);
		socksPasswordField.setEditable(sel && socksProxyAuthCb.isSelected());
		socksUserField.setEditable(sel && socksProxyAuthCb.isSelected());
		socksProxyAuthCb.setEnabled(sel);
    }
    
    public void saveOptions()
    {
    	TBGlobals.setUseSocksProxy(useSocksProxyCb.isSelected());
    	TBGlobals.setSocksProxyHost(socksHostField.getText());
    	TBGlobals.setSocksProxyPort(socksPortField.getText());
    	TBGlobals.setSocksProxyRequiresLogin(socksProxyAuthCb.isSelected());
    	TBGlobals.setSocksProxyUser(socksUserField.getText());
    	TBGlobals.setSocksProxyPassword(new String(socksPasswordField.getPassword()));
        TBGlobals.setNodePort(nodePortTf.getText());
        TBGlobals.setNodeHostname(hostNameTf.getText());
    	
    	if(layout2ColRb.isSelected())
    		TBGlobals.setLayoutStyle(TBGlobals.TWO_COL);
    	else
    		TBGlobals.setLayoutStyle(TBGlobals.THREE_COL); 
    	
    	int size = Integer.parseInt(fontSizeCombo.getSelectedItem().toString());
    	String name = fontNameCombo.getSelectedItem().toString();
    	TBGlobals.setEditorFont(new Font(name, Font.PLAIN, size));    	
    	//TBGlobals.setDictionaryFile((File)dictionaryCombo.getSelectedItem());
    	File f = (File)dictionaryCombo.getSelectedItem();
    	TBGlobals.setDictionary(getDictName(f));    	
    	TBGlobals.setStartWithSplash(splashScreenCb.isSelected());
    	TBGlobals.setLookAndFeelClassName(
    		lfinfo[lafCombo.getSelectedIndex()].getClassName());
    	
    	TBGlobals.setStartWithLastDatabase(openLastDBCb.isSelected());
    	//Browser.userOKedPanelChanges();
    	
    	TBGlobals.setPingAfterPublish(pingAfterPubCb.isSelected());        
       
        TBGlobals.putProperty("LANG_LOCALE", ((Locale)langCombo.getSelectedItem()).toString());
    	
    	TBGlobals.setAutoFeedUpdate(updateNewsCb.isSelected());
    	Integer val = (Integer)minuteModel.getValue();
    	TBGlobals.setFeedUpdateInterval((val.intValue() * 1000) * 60);
        TBGlobals.saveProperties();
    	FeedItemFormatter.setTemplate(feedItemArea.getText());
    	try{
    	FeedItemFormatter.saveTemplate();
    	}catch(Exception ex){}
    }
    
    public boolean isValidData()
    {
    	String msg = ""; //$NON-NLS-1$
    	String curLaf = TBGlobals.getLookAndFeelClassName();
    	String selLaf = lfinfo[lafCombo.getSelectedIndex()].getClassName();
    	if(!curLaf.equals(selLaf) || (!langCombo.getSelectedItem().equals(I18n.getLocale())))
    	{    	
    		msg += i18n.str("look_and_feel_prompt"); //$NON-NLS-1$
    	}
    	
    	if(didProxySettingsChange())
    	{
			msg += i18n.str("proxy_settings_prompt"); //$NON-NLS-1$
    	}
    	
    	if(!msg.equals("")) //$NON-NLS-1$
			JOptionPane.showMessageDialog(this, msg, i18n.str("options_"), //$NON-NLS-1$
			 	JOptionPane.INFORMATION_MESSAGE);
    	
    	return true;   	
    }
    
    private boolean didProxySettingsChange()
    {
    	if(TBGlobals.isUseSocksProxy() != useSocksProxyCb.isSelected())
    		return true;     	
    	
    	if(TBGlobals.isSocksProxyRequiresLogin() != socksProxyAuthCb.isSelected())
    		return true;    	
    	
    	if(!TBGlobals.getSocksProxyHost().equals(socksHostField.getText()))
    		return true;
    	
    	if(!TBGlobals.getSocksProxyPort().equals(socksPortField.getText()))
    		return true;
    	
    	if(!TBGlobals.getSocksProxyUser().equals(socksUserField.getText()))
    		return true;
    		
    	String pw = new String(socksPasswordField.getPassword());
    	if(!TBGlobals.getSocksProxyPassword().equals(pw))
    		return true;
    	
    	return false;    		   
    }
    
    private String getDictName(File f)
    {
		String name = f.getName();
	    int dot = name.indexOf('.');
	    if(dot != -1 && name.length() > 2)
	    {
	        name = name.substring(0, dot);
	    }
	    
	    return name;
    }
    
    private class DictListCellRenderer extends DefaultListCellRenderer
    {
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected, boolean cellHasFocus){
			
			if(value instanceof File)
			{
				File f = (File)value;
				value = getDictName(f);													  
			}			
			
			return super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);
		}
    }
    
    private class LocaleListCellRenderer extends DefaultListCellRenderer
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected, boolean cellHasFocus){
            
            if(value instanceof Locale)
            {                
                value = ((Locale)value).getDisplayName();                                                   
            }           
            
            return super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        }
    }
    
    private class LocaleComparator implements Comparator
    {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {            
            Locale l1 = (Locale)o1;
            Locale l2 = (Locale)o2;
            return Collator.getInstance().compare(l1.getDisplayName(), l2.getDisplayName());
        }
        
    }
}
