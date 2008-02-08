/*
 * Created on May 31, 2004
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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.SortedMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.transport.FTPTransport;
import net.sf.thingamablog.transport.LocalTransport;
import net.sf.thingamablog.transport.PublishTransport;
import net.sf.thingamablog.transport.SFTPTransport;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBPublishTransportPanel extends PropertyPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    
    private final String FTP = "FTP"; //$NON-NLS-1$
	private final String SFTP = "SFTP"; //$NON-NLS-1$
	private final String LOCAL = "Local"; //$NON-NLS-1$
	
	private TBWeblog weblog;
	private JComboBox encodingsCombo;
	private JComboBox transportTypeCombo;
	private JPanel transportsPanel;
	private RemoteTransportPanel ftpPanel;
	private RemoteTransportPanel sftpPanel;
	private JPanel localPanel;
	private CardLayout tLayout;
    private JTabbedPane ftpTabs = new JTabbedPane();
    private ASCIIPanel asciiPanel = new ASCIIPanel();
	
	public TBPublishTransportPanel(TBWeblog wb)
	{
		weblog = wb;
		String types[] = {FTP, SFTP, LOCAL};
		
		tLayout = new CardLayout();
		ftpPanel = new RemoteTransportPanel(true);
		ftpPanel.setBorder(new TitledBorder(i18n.str("ftp_transport"))); //$NON-NLS-1$
		sftpPanel = new RemoteTransportPanel(false);
		sftpPanel.setBorder(new TitledBorder(i18n.str("sftp_transport"))); //$NON-NLS-1$
		localPanel = new JPanel();
        
        ftpTabs.add(ftpPanel, "FTP");
        asciiPanel.setPreferredSize(new Dimension(210, 150));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(asciiPanel);
        ftpTabs.add(p, "ASCII");
        
		
		transportsPanel = new JPanel();
		transportsPanel.setLayout(tLayout);		
		transportsPanel.add(ftpTabs, FTP);
		transportsPanel.add(sftpPanel, SFTP);
		transportsPanel.add(localPanel, LOCAL);	
		
		/* Fill combo with all supported encodings */
		encodingsCombo = new JComboBox();
		SortedMap encodings = Charset.availableCharsets();
		Iterator encodingsIt = encodings.keySet().iterator();
		while (encodingsIt.hasNext()) 
		{
			String encoding = (String) encodingsIt.next();
			encodingsCombo.addItem(encoding);
		}
		encodingsCombo.setSelectedItem(weblog.getPageGenerator().getCharset());
		
		transportTypeCombo = new JComboBox(types);
		if(wb.getPublishTransport() instanceof FTPTransport)
		{		
			FTPTransport t = (FTPTransport)wb.getPublishTransport();
			ftpPanel.setServer(t.getAddress());
			ftpPanel.setUserName(t.getUserName());
			ftpPanel.setSavePassword(t.isSavePassword());
			ftpPanel.setPort(t.getPort());
			ftpPanel.setPassive(t.isPassiveMode());
            asciiPanel.setListData(t.getASCIIExtensions());
			if(ftpPanel.isSavePassword())
				ftpPanel.setPassword(t.getPassword());
			transportTypeCombo.setSelectedItem(FTP);
			tLayout.show(transportsPanel, FTP);
		}
		else if(wb.getPublishTransport() instanceof SFTPTransport)
		{		
			SFTPTransport t = (SFTPTransport)wb.getPublishTransport();
			sftpPanel.setServer(t.getAddress());
			sftpPanel.setUserName(t.getUserName());
			sftpPanel.setPort(t.getPort());
			sftpPanel.setSavePassword(t.isSavePassword());
			if(sftpPanel.isSavePassword())
				sftpPanel.setPassword(t.getPassword());
			transportTypeCombo.setSelectedItem(SFTP);
			tLayout.show(transportsPanel, SFTP);
		}
		else
		{		
			transportTypeCombo.setSelectedItem(LOCAL);
			tLayout.show(transportsPanel, LOCAL);
		}
		transportTypeCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				tLayout.show(transportsPanel, 
					transportTypeCombo.getSelectedItem().toString());
			}
		});			
		
		setLayout(new BorderLayout());
		LabelledItemPanel lip = new LabelledItemPanel();
		JPanel spacer = new JPanel(new BorderLayout());
		spacer.add(encodingsCombo, BorderLayout.WEST);
		spacer.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(i18n.str("character_encoding"), spacer);		 //$NON-NLS-1$
		
		spacer = new JPanel(new BorderLayout());
		spacer.add(transportTypeCombo, BorderLayout.WEST);
		spacer.add(new JPanel(), BorderLayout.CENTER);
		lip.addItem(i18n.str("transport_type"), spacer); //$NON-NLS-1$
				
		add(lip, BorderLayout.NORTH);				
		add(transportsPanel, BorderLayout.CENTER);
		
	}
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {        
		Object o = transportTypeCombo.getSelectedItem();
		PublishTransport transport = null;
		if(o == FTP)
		{
			FTPTransport pt = new FTPTransport();
			pt.setAddress(ftpPanel.getServer());
			pt.setPassword(ftpPanel.getPassword());
			pt.setPort(ftpPanel.getPort());
			pt.setUserName(ftpPanel.getUserName());
			pt.setSavePassword(ftpPanel.isSavePassword());
            pt.setASCIIExtentions(asciiPanel.getListData());
			if(pt.isSavePassword())
				pt.setPassword(ftpPanel.getPassword());
			pt.setPassiveMode(ftpPanel.isPassive());
			transport = pt;						
		}
		else if(o == SFTP)
		{
			SFTPTransport pt = new SFTPTransport();
			pt.setAddress(sftpPanel.getServer());
			pt.setPassword(sftpPanel.getPassword());
			pt.setPort(sftpPanel.getPort());
			pt.setUserName(sftpPanel.getUserName());
			pt.setSavePassword(sftpPanel.isSavePassword());
			if(pt.isSavePassword())
				pt.setPassword(sftpPanel.getPassword());
			transport = pt;					
		}
		else
		{
			transport = new LocalTransport();
		}
		
		weblog.setPublishTransport(transport);
		weblog.getPageGenerator().setCharset(encodingsCombo.getSelectedItem().toString());
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#isValidData()
     */
    public boolean isValidData()
    {
        Object o = transportTypeCombo.getSelectedItem();
        if(o == FTP)
        	return validateOptions(ftpPanel);
        if(o == SFTP)
        	return validateOptions(sftpPanel);
        	
        return true;
    }
    
    private boolean validateOptions(RemoteTransportPanel rtp)
    {
    	if(rtp.getServer() == null || rtp.getServer().equals("")) //$NON-NLS-1$
    	{
    		JOptionPane.showMessageDialog(this, i18n.str("enter_a_server"), i18n.str("warning"),  //$NON-NLS-1$ //$NON-NLS-2$
    			JOptionPane.WARNING_MESSAGE);
    		return false;
    	}
    	
		return true;
    }
    
    private class RemoteTransportPanel extends JPanel
    {
    	/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private JTextField serverField;
		private JTextField portField;
		private JTextField userNameField;
		private JPasswordField passwordField;
		private JCheckBox savePasswordCheckBox;
		private JCheckBox passiveModeCheckBox;
		
		public RemoteTransportPanel(boolean isFtp)
		{
			serverField = new JTextField();
			portField = new JTextField(4);
			userNameField = new JTextField();
			passwordField = new JPasswordField();
			savePasswordCheckBox = new JCheckBox();
			passiveModeCheckBox = new JCheckBox();
			
			TextEditPopupManager pm = TextEditPopupManager.getInstance();
			pm.registerJTextComponent(serverField);
			pm.registerJTextComponent(userNameField);
			pm.registerJTextComponent(passwordField);
			
			if(isFtp)
				portField.setText("21"); //$NON-NLS-1$
			else
				portField.setText("22");//sftp //$NON-NLS-1$
			
			LabelledItemPanel lip = new LabelledItemPanel();
			lip.addItem(i18n.str("server"), serverField); //$NON-NLS-1$
			JPanel p = new JPanel(new BorderLayout());
			p.add(portField, BorderLayout.WEST);
			p.add(new JPanel(), BorderLayout.CENTER);
			lip.addItem(i18n.str("port"), p); //$NON-NLS-1$
			lip.addItem(i18n.str("user_name"), userNameField); //$NON-NLS-1$
			lip.addItem(i18n.str("password"), passwordField); //$NON-NLS-1$
			lip.addItem(i18n.str("save_password"), savePasswordCheckBox); //$NON-NLS-1$
			if(isFtp)
				lip.addItem(i18n.str("passive_mode"), passiveModeCheckBox); //$NON-NLS-1$
			setLayout(new BorderLayout());
			add(lip, BorderLayout.CENTER);			
		}
        /**
         * @return
         */
        public String getPassword()
        {
            return new String(passwordField.getPassword());
        }

        /**
         * @return
         */
        public int getPort()
        {
            try
            {
            	return Integer.parseInt(portField.getText());
            }
            catch(Exception ex){}
            return 21;
        }

        /**
         * @return
         */
        public boolean isSavePassword()
        {
            return savePasswordCheckBox.isSelected();
        }

        /**
         * @return
         */
        public String getServer()
        {
            return serverField.getText();
        }

        /**
         * @return
         */
        public String getUserName()
        {
            return userNameField.getText();
        }

        /**
         * @param field
         */
        public void setPassword(String s)
        {
            passwordField.setText(s);
        }

        /**
         * @param field
         */
        public void setPort(int p)
        {
            portField.setText(p + ""); //$NON-NLS-1$
        }

        /**
         * @param box
         */
        public void setSavePassword(boolean b)
        {
            savePasswordCheckBox.setSelected(b);
        }

        /**
         * @param field
         */
        public void setServer(String s)
        {
            serverField.setText(s);
        }

        /**
         * @param field
         */
        public void setUserName(String s)
        {
            userNameField.setText(s);
        }
        /**
         * @return
         */
        public boolean isPassive()
        {
            return passiveModeCheckBox.isSelected();
        }

        /**
         * @param box
         */
        public void setPassive(boolean b)
        {
            passiveModeCheckBox.setSelected(b);
        }
    }
}
