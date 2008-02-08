/*
 * Created on Oct 19, 2007
 */
package net.sf.thingamablog.gui.properties;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.blog.Weblog;


/**
 * @author Bob Tantlinger
 *
 */
public class TBEmailPanel extends PropertyPanel
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties"); //$NON-NLS-1$
    
    private static final String[] PROTOCOLS = {"POP3", "IMAP"}; //$NON-NLS-1$ //$NON-NLS-2$
    private JLabel protocolLabel = null;
    private JComboBox protocolCombo = null;
    private JCheckBox postFromMailCheckBox = null;
    private JLabel serverLabel = null;
    private JTextField serverField = null;
    private JLabel userNameLabel = null;
    private JTextField userNameField = null;
    private JLabel passwordLabel = null;
    private JPasswordField passwordField = null;
    private JPanel autoCheckPanel = null;
    private JCheckBox autoUpdateCheckBox = null;
    private JSpinner autoCheckSpinner = null;
    private JPanel spacerPanel = null;
    
    private SpinnerNumberModel minutesSpinnerModel;
    
    private Weblog blog;
    private JLabel portLabel = null;
    private JTextField portField = null;
    private JCheckBox savePasswordCheckBox = null;
    private JPanel postPrefixPanel = null;
    private JLabel importLabel = null;
    private JTextField prefixField = null;

    /**
     * This method initializes 
     * 
     */
    public TBEmailPanel() {
    	super();
    	initialize();
    }
    
    public TBEmailPanel(Weblog w)
    {
        super();
        initialize();
        blog = w;
        
        postFromMailCheckBox.setSelected(blog.isImportFromEmailEnabled());       
        if(blog.getMailTransport().getProtocol().toUpperCase().equals("IMAP")) //$NON-NLS-1$
            protocolCombo.setSelectedItem(PROTOCOLS[1]);        
        serverField.setText(blog.getMailTransport().getAddress());
        portField.setText(blog.getMailTransport().getPort() + ""); //$NON-NLS-1$
        prefixField.setText(blog.getMailTransport().getPostDirective());
        userNameField.setText(blog.getMailTransport().getUserName());
        savePasswordCheckBox.setSelected(blog.getMailTransport().isSavePassword());
        if(savePasswordCheckBox.isSelected())
            passwordField.setText(blog.getMailTransport().getPassword());
        autoUpdateCheckBox.setSelected(blog.getOutdatedAfterMinutes() > 0);
        if(autoUpdateCheckBox.isSelected())
            minutesSpinnerModel.setValue(new Integer(blog.getOutdatedAfterMinutes()));       
       
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        
        GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
        gridBagConstraints14.gridx = 0;
        gridBagConstraints14.gridwidth = 2;
        gridBagConstraints14.anchor = GridBagConstraints.WEST;
        gridBagConstraints14.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints14.gridy = 1;
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.gridx = 0;
        gridBagConstraints31.anchor = GridBagConstraints.WEST;
        gridBagConstraints31.gridwidth = 2;
        gridBagConstraints31.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints31.gridy = 7;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints21.gridy = 3;
        gridBagConstraints21.weightx = 1.0;
        gridBagConstraints21.anchor = GridBagConstraints.WEST;
        gridBagConstraints21.insets = new Insets(0, 4, 5, 0);
        gridBagConstraints21.gridx = 1;
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.anchor = GridBagConstraints.WEST;
        gridBagConstraints11.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints11.gridy = 3;
        portLabel = new JLabel();
        portLabel.setText(i18n.str("port")); //$NON-NLS-1$
        minutesSpinnerModel = new SpinnerNumberModel(30, 1, 2880, 1);
        
        GridBagConstraints gridBagConstraints101 = new GridBagConstraints();
        gridBagConstraints101.gridx = 0;
        gridBagConstraints101.fill = GridBagConstraints.NONE;
        gridBagConstraints101.gridwidth = 2;
        gridBagConstraints101.weightx = 1.0;
        gridBagConstraints101.weighty = 1.0;
        gridBagConstraints101.gridy = 9;
        GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
        gridBagConstraints91.gridx = 0;
        gridBagConstraints91.gridwidth = 2;
        gridBagConstraints91.anchor = GridBagConstraints.WEST;
        gridBagConstraints91.gridy = 8;
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints8.gridy = 6;
        gridBagConstraints8.weightx = 0.0;
        gridBagConstraints8.anchor = GridBagConstraints.WEST;
        gridBagConstraints8.insets = new Insets(0, 4, 5, 0);
        gridBagConstraints8.gridx = 1;
        GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
        gridBagConstraints7.gridx = 0;
        gridBagConstraints7.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints7.anchor = GridBagConstraints.WEST;
        gridBagConstraints7.gridy = 6;
        passwordLabel = new JLabel();
        passwordLabel.setText(i18n.str("password")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.gridy = 5;
        gridBagConstraints6.weightx = 0.0;
        gridBagConstraints6.anchor = GridBagConstraints.WEST;
        gridBagConstraints6.insets = new Insets(0, 4, 5, 0);
        gridBagConstraints6.gridx = 1;
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints5.anchor = GridBagConstraints.WEST;
        gridBagConstraints5.gridy = 5;
        userNameLabel = new JLabel();
        userNameLabel.setText(i18n.str("user_name")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.gridy = 4;
        gridBagConstraints4.weightx = 0.0;
        gridBagConstraints4.anchor = GridBagConstraints.WEST;
        gridBagConstraints4.insets = new Insets(0, 4, 5, 0);
        gridBagConstraints4.gridx = 1;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.anchor = GridBagConstraints.WEST;
        gridBagConstraints3.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints3.gridy = 4;
        serverLabel = new JLabel();
        serverLabel.setText(i18n.str("server")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.anchor = GridBagConstraints.WEST;
        gridBagConstraints2.insets = new Insets(0, 0, 8, 0);
        gridBagConstraints2.gridy = 0;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.insets = new Insets(0, 4, 5, 0);
        gridBagConstraints1.gridx = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints.gridy = 2;
        protocolLabel = new JLabel();
        protocolLabel.setText(i18n.str("protocol")); //$NON-NLS-1$
        this.setLayout(new GridBagLayout());
        //this.setSize(new Dimension(422, 294));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(protocolLabel, gridBagConstraints);
        this.add(getProtocolCombo(), gridBagConstraints1);
        this.add(getPostFromMailCheckBox(), gridBagConstraints2);
        this.add(serverLabel, gridBagConstraints3);
        this.add(getServerField(), gridBagConstraints4);
        this.add(userNameLabel, gridBagConstraints5);
        this.add(getUserNameField(), gridBagConstraints6);
        this.add(passwordLabel, gridBagConstraints7);
        this.add(getPasswordField(), gridBagConstraints8);
        this.add(getAutoCheckPanel(), gridBagConstraints91);
        this.add(getSpacerPanel(), gridBagConstraints101);
        this.add(portLabel, gridBagConstraints11);
        this.add(getPortField(), gridBagConstraints21);
        this.add(getSavePasswordCheckBox(), gridBagConstraints31);
        this.add(getPostPrefixPanel(), gridBagConstraints14);
        
        setEnabledState(false);    		
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#isValidData()
     */
    public boolean isValidData()
    {        
        if(postFromMailCheckBox.isSelected())
        {
            if(serverField.getText().equals("")) //$NON-NLS-1$
            {
                JOptionPane.showMessageDialog(this, i18n.str("enter_a_server"), i18n.str("warning"),  //$NON-NLS-1$ //$NON-NLS-2$
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }  
            
            if(prefixField.getText().equals("")) //$NON-NLS-1$
            {
                JOptionPane.showMessageDialog(this, "Enter a post prefix", i18n.str("warning"),  //$NON-NLS-1$ //$NON-NLS-2$
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#saveProperties()
     */
    public void saveProperties()
    {        
        blog.setImportFromEmailEnabled(postFromMailCheckBox.isSelected());
        blog.getMailTransport().setProtocol(protocolCombo.getSelectedItem().toString());
        blog.getMailTransport().setAddress(serverField.getText());
        blog.getMailTransport().setUserName(userNameField.getText());        
        blog.getMailTransport().setSavePassword(savePasswordCheckBox.isSelected());
        blog.getMailTransport().setPostDirective(prefixField.getText());
        
        if(savePasswordCheckBox.isSelected())
            blog.getMailTransport().setPassword(new String(passwordField.getPassword()));
        else
            blog.getMailTransport().setPassword(null);
        
        if(autoUpdateCheckBox.isSelected())
            blog.setOutdatedAfterMinutes(((Integer)minutesSpinnerModel.getValue()).intValue());
        else
            blog.setOutdatedAfterMinutes(-1);
        
        int port = (protocolCombo.getSelectedItem().equals("POP3")) ? 110 : 143; //$NON-NLS-1$
        try
        {
            port = Integer.parseInt(portField.getText());
        }
        catch(Exception ex){}
        blog.getMailTransport().setPort(port);
    }

    /**
     * This method initializes protocolCombo	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getProtocolCombo()
    {
        if(protocolCombo == null)
        {
            protocolCombo = new JComboBox(PROTOCOLS);
            protocolCombo.addItemListener(new java.awt.event.ItemListener()
            {
                public void itemStateChanged(java.awt.event.ItemEvent e)
                {
                    if(protocolCombo.getSelectedItem().equals("POP3")) //$NON-NLS-1$
                        portField.setText("110"); //$NON-NLS-1$
                    else
                        portField.setText("143"); //$NON-NLS-1$
                }
            });
        }
        return protocolCombo;
    }

    /**
     * This method initializes postFromMailCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getPostFromMailCheckBox()
    {
        if(postFromMailCheckBox == null)
        {
            postFromMailCheckBox = new JCheckBox();
            postFromMailCheckBox.setText(i18n.str("import_posts_from_email")); //$NON-NLS-1$
            postFromMailCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
            {
                public void stateChanged(javax.swing.event.ChangeEvent e)
                {
                    setEnabledState(postFromMailCheckBox.isSelected());
                    
                }
            });
        }
        return postFromMailCheckBox;
    }
    
    private void setEnabledState(boolean b)
    {
        protocolCombo.setEnabled(b);
        portField.setEditable(b);
        serverField.setEditable(b);
        userNameField.setEditable(b);        
        autoUpdateCheckBox.setEnabled(b);
        autoCheckSpinner.setEnabled(b && autoUpdateCheckBox.isSelected());
        savePasswordCheckBox.setEnabled(b);
        passwordField.setEditable(b && savePasswordCheckBox.isSelected());
        prefixField.setEditable(b);
    }

    /**
     * This method initializes serverField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getServerField()
    {
        if(serverField == null)
        {
            serverField = new JTextField();
            TextEditPopupManager.getInstance().registerJTextComponent(serverField);
        }
        return serverField;
    }

    /**
     * This method initializes userNameField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getUserNameField()
    {
        if(userNameField == null)
        {
            userNameField = new JTextField();
            TextEditPopupManager.getInstance().registerJTextComponent(userNameField);
        }
        return userNameField;
    }

    /**
     * This method initializes passwordField	
     * 	
     * @return javax.swing.JPasswordField	
     */
    private JPasswordField getPasswordField()
    {
        if(passwordField == null)
        {
            passwordField = new JPasswordField();
            TextEditPopupManager.getInstance().registerJTextComponent(passwordField);
        }
        return passwordField;
    }

    /**
     * This method initializes autoCheckPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getAutoCheckPanel()
    {
        if(autoCheckPanel == null)
        {
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.fill = GridBagConstraints.NONE;
            gridBagConstraints10.gridy = 0;
            gridBagConstraints10.weightx = 1.0;
            gridBagConstraints10.anchor = GridBagConstraints.WEST;
            gridBagConstraints10.gridx = 1;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 0;
            gridBagConstraints9.anchor = GridBagConstraints.WEST;
            gridBagConstraints9.gridy = 0;
            autoCheckPanel = new JPanel();
            autoCheckPanel.setLayout(new GridBagLayout());
            autoCheckPanel.add(getAutoUpdateCheckBox(), gridBagConstraints9);
            autoCheckPanel.add(getAutoCheckSpinner(), gridBagConstraints10);
        }
        return autoCheckPanel;
    }

    /**
     * This method initializes autoUpdateCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getAutoUpdateCheckBox()
    {
        if(autoUpdateCheckBox == null)
        {
            autoUpdateCheckBox = new JCheckBox();
            autoUpdateCheckBox.setText(i18n.str("check_email_every_minutes")); //$NON-NLS-1$
            autoUpdateCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
            {
                public void stateChanged(javax.swing.event.ChangeEvent e)
                {
                    autoCheckSpinner.setEnabled(autoUpdateCheckBox.isSelected());
                }
            });
        }
        return autoUpdateCheckBox;
    }

    /**
     * This method initializes autoCheckSpinner	
     * 	
     * @return javax.swing.JSpinner
     */
    private JSpinner getAutoCheckSpinner()
    {
        if(autoCheckSpinner == null)
        {
            autoCheckSpinner = new JSpinner(minutesSpinnerModel);
        }
        return autoCheckSpinner;
    }

    /**
     * This method initializes spacerPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getSpacerPanel()
    {
        if(spacerPanel == null)
        {
            spacerPanel = new JPanel();
            spacerPanel.setLayout(new GridBagLayout());
        }
        return spacerPanel;
    }

    /**
     * This method initializes portField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getPortField()
    {
        if(portField == null)
        {
            portField = new JTextField();
            portField.setColumns(4);
            portField.setText("110"); //$NON-NLS-1$
            TextEditPopupManager.getInstance().registerJTextComponent(portField);
        }
        return portField;
    }

    /**
     * This method initializes savePasswordCheckBox	
     * 	
     * @return javax.swing.JCheckBox	
     */
    private JCheckBox getSavePasswordCheckBox()
    {
        if(savePasswordCheckBox == null)
        {
            savePasswordCheckBox = new JCheckBox();
            savePasswordCheckBox.setText(i18n.str("save_password")); //$NON-NLS-1$
            savePasswordCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
            {
                public void stateChanged(javax.swing.event.ChangeEvent e)
                {
                    passwordField.setEditable(savePasswordCheckBox.isSelected());
                }
            });
        }
        return savePasswordCheckBox;
    }

    /**
     * This method initializes postPrefixPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPostPrefixPanel()
    {
        if(postPrefixPanel == null)
        {
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.fill = GridBagConstraints.BOTH;
            gridBagConstraints13.gridy = 0;
            gridBagConstraints13.weightx = 0.0;
            gridBagConstraints13.anchor = GridBagConstraints.WEST;
            gridBagConstraints13.gridx = 1;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 0;
            gridBagConstraints12.insets = new Insets(0, 0, 0, 4);
            gridBagConstraints12.anchor = GridBagConstraints.WEST;
            gridBagConstraints12.gridy = 0;
            importLabel = new JLabel();
            importLabel.setText(i18n.str("import_emails_having_subjects_prefixed_with")); //$NON-NLS-1$
            postPrefixPanel = new JPanel();
            postPrefixPanel.setLayout(new GridBagLayout());
            postPrefixPanel.add(importLabel, gridBagConstraints12);
            postPrefixPanel.add(getPrefixField(), gridBagConstraints13);
        }
        return postPrefixPanel;
    }

    /**
     * This method initializes prefixField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getPrefixField()
    {
        if(prefixField == null)
        {
            prefixField = new JTextField();
            prefixField.setColumns(5);
            TextEditPopupManager.getInstance().registerJTextComponent(prefixField);
        }
        return prefixField;
    }

}  
