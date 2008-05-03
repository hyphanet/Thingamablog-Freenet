/*
 * Created on Oct 11, 2007
 */
package net.sf.thingamablog.gui.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.blog.PingProgress;
import net.sf.thingamablog.blog.PingService;
import net.sf.thingamablog.blog.PublishProgress;
import net.sf.thingamablog.transport.MailTransportProgress;
import thingamablog.l10n.i18n;


/**
 * @author Bob Tantlinger
 *
 */
public class LogPanel extends JPanel implements PublishProgress, PingProgress, MailTransportProgress
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private JProgressBar progressBar;
    private JButton abortButton;
    private JLabel label;
    private boolean aborted;
    private JEditorPane output;
    private DefaultStyledDocument doc;
    
    private boolean hasPublishFailed, pingFailed, mailCheckFailed;
    
    private DateFormat df = DateFormat.getDateTimeInstance();
    private JPopupMenu popupMenu = new JPopupMenu();
    
    private ImageIcon errIcon = UIUtils.getIcon(UIUtils.X32, "failed.png"); //$NON-NLS-1$
    private ImageIcon connIcon = UIUtils.getIcon(UIUtils.X32, "transfer.png"); //$NON-NLS-1$
    private ImageIcon pingIcon = UIUtils.getIcon(UIUtils.X32, "ping.png"); //$NON-NLS-1$
    private ImageIcon fileIcon = UIUtils.getIcon(UIUtils.X32, "html.png"); //$NON-NLS-1$
    private ImageIcon completeIcon = UIUtils.getIcon(UIUtils.X32, "complete.png"); //$NON-NLS-1$
    
    private Color labelForeground;
    
    public LogPanel()
    {
        progressBar = new JProgressBar();
        progressBar.setValue(0);        
        progressBar.setStringPainted(true); 
        progressBar.setPreferredSize(new Dimension(10, 10));
                
        label = new JLabel();
        labelForeground = label.getForeground();
        label.setPreferredSize(new Dimension(400, 36));
                
        doc = new DefaultStyledDocument();
        output = new JEditorPane("text/rtf", "") //$NON-NLS-1$ //$NON-NLS-2$
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public boolean getScrollableTracksViewportWidth() 
            {
                return false;//don't want wordwrap
            }
        };
        output.setEditable(false);
        output.setDocument(doc);
        JScrollPane outputScroller = new JScrollPane(output);
        outputScroller.getViewport().setBackground(output.getBackground());
        outputScroller.setPreferredSize(new Dimension(20, 20));
        
        abortButton = new JButton(UIUtils.getIcon(UIUtils.X16, "cancel.png"));  //$NON-NLS-1$
        abortButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(!aborted)
                {
                	aborted = true;
                	updateLabelText(UIUtils.getIcon(UIUtils.X32, "err_feed.png"), i18n.str("task_canceled"), false); //$NON-NLS-1$
                	updateProgressBar(0, 0);
                	append(i18n.str("task_canceled"), Color.ORANGE);
                }
                //dispose();
            }
        });
        abortButton.setEnabled(false);
        abortButton.setPreferredSize(new Dimension(20, 20));
                      
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.fill = GridBagConstraints.BOTH;
        gridBagConstraints11.gridy = 2;
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.weighty = 1.0;
        gridBagConstraints11.gridwidth = 2;
        gridBagConstraints11.insets = new Insets(4, 0, 0, 0);
        gridBagConstraints11.gridx = 0;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.anchor = GridBagConstraints.WEST;
        gridBagConstraints2.insets = new Insets(0, 5, 0, 0);
        gridBagConstraints2.gridy = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.fill = GridBagConstraints.BOTH;
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.insets = new Insets(0, 0, 0, 0);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        gridBagConstraints.gridy = 0;        
       
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(label, gridBagConstraints);
        this.add(progressBar, gridBagConstraints1);
        this.add(abortButton, gridBagConstraints2);
        this.add(outputScroller, gridBagConstraints11);
        
        TextEditPopupManager.getInstance().registerJTextComponent(output);
        popupMenu.add(TextEditPopupManager.getInstance().getAction(TextEditPopupManager.COPY));
        popupMenu.add(TextEditPopupManager.getInstance().getAction(TextEditPopupManager.SELECT_ALL));
        output.addMouseListener(new PopupHandler());
        reset();                       
    }
    
    public void reset()
    {
        hasPublishFailed = false;
        pingFailed = false;
        mailCheckFailed = false;        
        aborted = false;
                
        updateLabelText(UIUtils.getIcon(UIUtils.X32, "cogs.png"), i18n.str("tasks_"), false); //$NON-NLS-1$
        updateProgressBar(0, 0);
        updateAbortButton(i18n.str("cancel"), false);         //$NON-NLS-1$
    }
    
    public void clearLog()
    {
        output.setText("");
    }
    
    public void logMessage(String msg)
    {
        append(msg, Color.black);
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#publishStarted(long)
     */
    public void publishStarted(final long totalBytesToPublish)
    {        
        updateLabelText(connIcon, i18n.str("connecting_to_server"), false); //$NON-NLS-1$
        append("\n" + i18n.str("publishing") + " @ " + df.format(new Date()), Color.blue);
        append("=====================================", Color.blue); //$NON-NLS-1$
        updateProgressBar(0, (int)totalBytesToPublish);
        //append(i18n.str("publish_complete") + "\n", Color.blue);       
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#filePublishStarted(java.io.File)
     */
    public void filePublishStarted(File f, String pubPath)
    {
        //label.setText(f.getName());
        updateLabelText(fileIcon, f.getName(), false);
        append(i18n.str("publishing") + ": " + f.getName() + " -> " + pubPath, Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //abortButton.setEnabled(true);
        //abortButton.setText(i18n.str("cancel")); //$NON-NLS-1$
        updateAbortButton(i18n.str("cancel"), true); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#filePublishCompleted(java.io.File)
     */
    public void filePublishCompleted(File f, String pubPath)
    {
        
    }    

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#publishCompleted()
     */
    public void publishCompleted()
    {
        append(i18n.str("publish_complete") + "\n", Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$
        //label.setText("Publish complete"); //$NON-NLS-1$
        updateLabelText(completeIcon, i18n.str("publish_complete"), false); //$NON-NLS-1$
        //progressBar.setValue(progressBar.getMaximum());
        updateProgressValue(progressBar.getMaximum());
        //abortButton.setText(i18n.str("close")); //$NON-NLS-1$
        updateAbortButton(i18n.str("cancel"), false); //$NON-NLS-1$
    }
    
    public void publishFailed(String reason)
    {
        //label.setForeground(Color.red);
        //label.setText(reason);
        updateLabelText(errIcon, i18n.str("publish_failed"), true); //$NON-NLS-1$
        //abortButton.setText(i18n.str("close")); //$NON-NLS-1$
        //abortButton.setEnabled(true);
        append(reason, Color.red);
        updateAbortButton(i18n.str("cancel"), false); //$NON-NLS-1$
        //progressBar.setValue(0);
        updateProgressValue(0);
        hasPublishFailed = true;
    }
    
    public boolean isDisplayingFailedMessage()
    {
        return hasPublishFailed || this.pingFailed || this.mailCheckFailed;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.TransportProgress#bytesTransferred(long)
     */
    public void bytesTransferred(final long bytes)
    { 
        updateProgressValue((int)bytes, true);
    }
    
    public void updateBlocksTransferred(final int blocks, final int total, String name)
    {
        if (!label.getIcon().equals(fileIcon))
            updateLabelText(fileIcon, name, false);
        updateProgressBar(blocks, total);
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.TransportProgress#isAborted()
     */
    public boolean isAborted()
    {        
        return aborted;
    }
    
    public void pingSessionStarted(final int totalServices)
    {
        append("\n" + i18n.str("pinging") + " @ " + df.format(new Date()), Color.blue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        append("=====================================", Color.blue); //$NON-NLS-1$
        updateAbortButton(i18n.str("cancel"), true); //$NON-NLS-1$
        updateProgressBar(0, totalServices);
    }
    
    public void pingStarted(PingService ps)
    {
        //label.setText(ps.getServiceName());
        updateLabelText(pingIcon, ps.getServiceName(), false);
        //abortButton.setEnabled(true);
        //abortButton.setText(i18n.str("cancel")); //$NON-NLS-1$
        updateAbortButton(i18n.str("cancel"), true); //$NON-NLS-1$
        append(i18n.str("pinging") + ": " + ps.getServiceName() + "...", Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    }
    
    public void pingFinished(PingService ps, boolean success, String message)
    {
        //progressBar.setValue(progressBar.getValue() + 1);
        //updateAbortButton(i18n.str("cancel"), false);
        updateProgressValue(progressBar.getValue() + 1);
        if(!success)
        {           
            append(i18n.str("ping_failed") + ": " + ps.getServiceName(), Color.red);  //$NON-NLS-1$ //$NON-NLS-2$
            append(message, Color.red);
        }
        else
            append(message, new Color(15, 195, 15));        
    }
    
    public void pingSessionCompleted()
    {        
        updateLabelText(completeIcon, i18n.str("publish_complete"), false); //$NON-NLS-1$
        updateAbortButton(i18n.str("cancel"), false); //$NON-NLS-1$
        append(i18n.str("pinging_complete") + "\n", Color.blue); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public boolean isPingSessionAborted()
    {
        return aborted;
    }
    
    private void append(final String str, final Color c)
    {       
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                SimpleAttributeSet sas = new SimpleAttributeSet();
                sas.addAttribute(StyleConstants.Foreground, c);             
                try
                {           
                    doc.insertString(doc.getLength(), str + '\n', sas);
                    output.setCaretPosition(doc.getLength());
                }
                catch(BadLocationException ex)
                {
                    ex.printStackTrace();
                }                
            }
        }); 
    }
    
    private void updateAbortButton(final String text, final boolean enabled)
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                abortButton.setEnabled(enabled);
                if(text != null)
                    abortButton.setToolTipText(text);
            }
        }); 
    }
    
    private void updateLabelText(final Icon ico, final String text, final boolean err)
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                label.setIcon(ico);
                if(err)
                    label.setForeground(Color.RED);
                else
                    label.setForeground(labelForeground);
                label.setText(text);                
            }
        }); 
    }
    
    private void updateProgressValue(int val)
    {
        this.updateProgressValue(val, false);
    }
    
    private void updateProgressValue(final int val, final boolean increment)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(increment)
                    progressBar.setValue(val + progressBar.getValue());
                else
                    progressBar.setValue(val);
            }
        });         
    }
    
    private void updateProgressBar(final int val, final int max)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setMaximum(max);
                progressBar.setValue(val);
            }
        });         
    }   
    
    private class PopupHandler extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {             
            checkForPopupTrigger(e); 
        }
            
        public void mouseReleased(MouseEvent e)
        { checkForPopupTrigger(e); }
            
        private void checkForPopupTrigger(MouseEvent e)
        {
            if(e.isPopupTrigger())
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.MailTransportProgress#emailCheckComplete()
     */
    public void emailCheckComplete()
    {
        updateAbortButton(i18n.str("cancel"), false); //$NON-NLS-1$
        updateLabelText(completeIcon, i18n.str("email_check_complete"), false); //$NON-NLS-1$
        append(i18n.str("email_check_complete"), Color.blue); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.MailTransportProgress#emailCheckStarted()
     */
    public void emailCheckStarted(String serverName)
    {
        updateAbortButton(i18n.str("cancel"), true); //$NON-NLS-1$
        updateLabelText(connIcon, i18n.str("checking_email"), false); //$NON-NLS-1$
        append("\n" + i18n.str("checking_email") + ": " + serverName + " @ " + df.format(new Date()), Color.blue); //$NON-NLS-2$
        append("=====================================", Color.blue); //$NON-NLS-1$
        
        //append(i18n.str("publish_complete") + "\n", Color.blue);
        
        
        //hasPublishFailed = false;
       // aborted = false;//TODO hmm
        
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.MailTransportProgress#importingMailMessage(java.lang.String)
     */
    public void messageChecked(String subject, boolean isImportable)
    {
        if(isImportable)
            append("* " + subject, new Color(15, 195, 15)); //$NON-NLS-1$
        else
            append(subject, Color.gray);
        updateProgressValue(1, true);
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.MailTransportProgress#mailCheckFailed(java.lang.String)
     */
    public void mailCheckFailed(String message)
    {
        append(message, Color.red); 
        updateProgressValue(0);        
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.MailTransportProgress#numberOfMessagesToCheck(int)
     */
    public void numberOfMessagesToCheck(final int num)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {   
                label.setForeground(Color.black);
                progressBar.setValue(0); 
                progressBar.setMaximum(num);
            }
        });        
    }
    
       
}
