/*
 * Created on Oct 23, 2007
 */
package net.sf.thingamablog.gui.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.PingProgress;
import net.sf.thingamablog.blog.PublishProgress;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.transport.MailTransportProgress;
import thingamablog.l10n.i18n;


/**
 * @author Bob Tantlinger
 *
 */
public class TaskDialog extends JDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JButton closeButton, clearButton;
    private JComboBox blogCombo;
    private DefaultComboBoxModel comboModel;
    
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Map blogMap = new HashMap();
    private Map logPanelMap = new HashMap();
    
    public TaskDialog()
    {
        init();
    }
    
    public TaskDialog(Frame parent)
    {
        super(parent, TBGlobals.APP_NAME);        
        init();
    }    
    
    
    private void init()
    {
        getContentPane().setLayout(new BorderLayout());
        
        comboModel = new DefaultComboBoxModel();
        blogCombo = new JComboBox(comboModel); 
        blogCombo.addItemListener(new ComboChangeHandler());
        JLabel siteLabel = new JLabel(i18n.str("site"));
        clearButton = new JButton(i18n.str("clear_log"));
        clearButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                getLogPanel((Weblog)blogCombo.getSelectedItem()).clearLog();
            }            
        });        
        JPanel comboPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridy = 0;
        comboPanel.add(siteLabel, gbc);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.gridx = 1;
        comboPanel.add(blogCombo, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        comboPanel.add(clearButton, gbc);
        comboPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        getContentPane().add(comboPanel, BorderLayout.NORTH);
        
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        getContentPane().add(cardPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton(i18n.str("close")); //$NON-NLS-1$
        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {                
                setVisible(false);
            }           
        });
        buttonPanel.add(closeButton);        
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                TBGlobals.putProperty("TASK_DIALOG_WIDTH", getWidth()+"");
                TBGlobals.putProperty("TASK_DIALOG_HEIGHT", getHeight()+"");
            }
        });
        
        int w = 600, h = 400;        
        try
        {
            w = Integer.parseInt(TBGlobals.getProperty("TASK_DIALOG_WIDTH"));
            h = Integer.parseInt(TBGlobals.getProperty("TASK_DIALOG_HEIGHT"));
        }
        catch(Exception ex){}
        setSize(w, h);        
    }
    
    public List getWeblogs()
    {
        return new ArrayList(blogMap.entrySet());
    }
    
    public void addWeblog(Weblog b)
    {
        if(b.getKey() == null)
            return;
        
        comboModel.addElement(b);
        LogPanel p = new LogPanel();
        cardPanel.add(p, b.getKey());
        blogMap.put(b.getKey(), b);
        logPanelMap.put(b.getKey(), p);
    }
    
    public void removeWeblog(Weblog b)
    {
        if(b.getKey() == null)
            return;
        
        comboModel.removeElement(b); 
        cardPanel.remove((Component)logPanelMap.get(b.getKey()));
        blogMap.remove(b.getKey());
        logPanelMap.remove(b.getKey());
    }
    
    public void removeAllWeblogs()
    {
        comboModel.removeAllElements();
        cardPanel.removeAll();
        blogMap.clear();
        logPanelMap.clear();
    }
    
    public void showDetails(Weblog b)
    {           
        blogCombo.setSelectedItem(b);        
    }
    
    public MailTransportProgress getMailTransportProgress(Weblog b)
    {        
        if(b.getKey() != null)
            return (MailTransportProgress)logPanelMap.get(b.getKey());
        return null;
    }
    
    public PingProgress getPingProgress(Weblog b)
    {        
        if(b.getKey() != null)
            return (PingProgress)logPanelMap.get(b.getKey());
        return null;
    }
    
    public PublishProgress getPublishProgress(Weblog b)
    {
        if(b.getKey() != null)
            return (PublishProgress)logPanelMap.get(b.getKey());
        return null;
    }
    
    public LogPanel getLogPanel(Weblog b)
    {
        if(b.getKey() != null)
            return (LogPanel)logPanelMap.get(b.getKey());
        return null;
    }
    
    private class ComboChangeHandler implements ItemListener
    {

        /* (non-Javadoc)
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent e)
        {            
            if(blogCombo.getItemCount() > 1)
            {
            	Weblog b = (Weblog)blogCombo.getSelectedItem();
            	cardLayout.show(cardPanel, b.getKey());
            }
        }        
    }
}
