/*
 * Created on May 28, 2004
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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.blog.PingProgress;
import net.sf.thingamablog.blog.PingService;
import net.sf.thingamablog.blog.PublishProgress;
import net.sf.thingamablog.gui.Messages;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class PublishDialog extends JDialog implements PublishProgress, PingProgress
{
	private JProgressBar progressBar;
	private JButton abortButton;
	private JLabel label;
	private boolean aborted;
	private JEditorPane output;
	private DefaultStyledDocument doc;
	
	private boolean hasPublishFailed;
	
	
	private ImageIcon errIcon = Utils.createIcon(TBGlobals.RESOURCES + "error.png");
	private ImageIcon connIcon = Utils.createIcon(TBGlobals.RESOURCES + "connecting.png");
	private ImageIcon pingIcon = Utils.createIcon(TBGlobals.RESOURCES + "pinging.png");
	private ImageIcon fileIcon = Utils.createIcon(TBGlobals.RESOURCES + "htmlfile.png");
	private ImageIcon completeIcon = Utils.createIcon(TBGlobals.RESOURCES + "complete.png");


    /**
     * @param owner
     * @param title
     * @param modal
     * 
     */
    public PublishDialog(Frame owner, String title, boolean modal)
    {
        super(owner, title, modal);
        init();
    }    

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 * 
	 */
	public PublishDialog(Dialog owner, String title, boolean modal)
	{
		super(owner, title, modal);
		init();
	}
    
    
    private void init()
    {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressBar = new JProgressBar();
		progressBar.setValue(0);        
		progressBar.setStringPainted(true);        
		//progressBar.setPreferredSize(new Dimension(360, 20));
        
		label = new JLabel();
		label.setPreferredSize(new Dimension(400, 36));
		//label.setText(Messages.getString("PublishDialog.Connecting_to_server"));  //$NON-NLS-1$
		//label.setText("Creating Weblog...");
		updateLabelText(connIcon, Messages.getString("PublishDialog.Connecting_to_server"), false);
		
		doc = new DefaultStyledDocument();
		output = new JEditorPane("text/rtf", "") //$NON-NLS-1$ //$NON-NLS-2$
		{
			public boolean getScrollableTracksViewportWidth() 
			{
				return false;//don't want wordwrap
			}
		};
		output.setEditable(false);
		output.setDocument(doc);
		JScrollPane outputScroller = new JScrollPane(output);
		outputScroller.getViewport().setBackground(output.getBackground());
        
		abortButton = new JButton(Messages.getString("PublishDialog.Close")); //$NON-NLS-1$
		abortButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				aborted = true;
				dispose();
			}
		});
		abortButton.setEnabled(false);
		
		addWindowListener(new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		        if(abortButton.isEnabled())
		            dispose();
		    }
		});
        
		getContentPane().setLayout(new BorderLayout());
		JPanel pPanel = new JPanel(new BorderLayout(5, 5));
		pPanel.add(label, BorderLayout.NORTH);
		pPanel.add(progressBar, BorderLayout.CENTER);
		pPanel.setBorder(new EmptyBorder(5, 5, 5, 5));         
        
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(abortButton);
		getContentPane().add(pPanel, BorderLayout.NORTH);
		getContentPane().add(outputScroller, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		setSize(480, 280);
    }

    public JButton getAbortButton()
    {
        return abortButton;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#publishStarted(long)
     */
    public void publishStarted(final long totalBytesToPublish)
    {
        updateLabelText(connIcon, Messages.getString("PublishDialog.Connecting_to_server"), false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {                
            	progressBar.setMaximum((int)totalBytesToPublish);
            }
        });    	
        hasPublishFailed = false;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#filePublishStarted(java.io.File)
     */
    public void filePublishStarted(File f, String pubPath)
    {
        //label.setText(f.getName());
    	updateLabelText(fileIcon, f.getName(), false);
        append(Messages.getString("PublishDialog.Publishing") + ": " + f.getName() + " -> " + pubPath, Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        //abortButton.setEnabled(true);
        //abortButton.setText(Messages.getString("PublishDialog.Cancel")); //$NON-NLS-1$
        updateAbortButton(Messages.getString("PublishDialog.Cancel"), true);
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
        append(Messages.getString("PublishDialog.Publish_complete") + "\n", Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$
        //label.setText("Publish complete"); //$NON-NLS-1$
        updateLabelText(completeIcon, "Publish complete", false);
        //progressBar.setValue(progressBar.getMaximum());
        updateProgressValue(progressBar.getMaximum());
        //abortButton.setText(Messages.getString("PublishDialog.Close")); //$NON-NLS-1$
        updateAbortButton(Messages.getString("PublishDialog.Close"), true);
    }
    
    public void publishFailed(String reason)
    {
    	//label.setForeground(Color.red);
    	//label.setText(reason);
    	updateLabelText(errIcon, "Publish Failed", true);
    	//abortButton.setText(Messages.getString("PublishDialog.Close")); //$NON-NLS-1$
    	//abortButton.setEnabled(true);
        append(reason, Color.red);
    	updateAbortButton(Messages.getString("PublishDialog.Close"), true);
    	//progressBar.setValue(0);
    	updateProgressValue(0);
    	hasPublishFailed = true;
    }
    
    public boolean isDisplayingFailedMessage()
    {
    	return hasPublishFailed;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.TransportProgress#bytesTransferred(long)
     */
    public void bytesTransferred(final long bytes)
    {
    	SwingUtilities.invokeLater(new Runnable() 
    	{
    		public void run() 
    		{
    		   	int cur = progressBar.getValue();
    	        progressBar.setValue((int)bytes + cur);
    		}
    	});
    	
 
        //updateProgressValue((int)bytes + cur);
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
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
		    {
				progressBar.setMaximum(totalServices);
				progressBar.setValue(0);    	
		    }
		});
	}
	
	public void pingStarted(PingService ps)
	{
		//label.setText(ps.getServiceName());
		updateLabelText(pingIcon, ps.getServiceName(), false);
		//abortButton.setEnabled(true);
		//abortButton.setText(Messages.getString("PublishDialog.Cancel")); //$NON-NLS-1$
		updateAbortButton(Messages.getString("PublishDialog.Cancel"), true);
		append("\n" + Messages.getString("PublishDialog.Pinging") + ": " + ps.getServiceName() + "...", Color.blue);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	public void pingFinished(PingService ps, boolean success, String message)
	{
		//progressBar.setValue(progressBar.getValue() + 1);
		updateProgressValue(progressBar.getValue() + 1);
		if(!success)
		{			
			append(Messages.getString("PublishDialog.Ping_Failed") + ": " + ps.getServiceName(), Color.red);  //$NON-NLS-1$ //$NON-NLS-2$
			append(message, Color.red);
		}
		else
			append(message, new Color(15, 195, 15));		
	}
	
	public void pingSessionCompleted()
	{
		//label.setText(Messages.getString("PublishDialog.Publish_complete"));  //$NON-NLS-1$
		//append(Messages.getString("PublishDialog.Pinging_complete"), Color.blue);  //$NON-NLS-1$
		//abortButton.setEnabled(true);
		//abortButton.setText(Messages.getString("PublishDialog.Close")); //$NON-NLS-1$
		
		updateLabelText(completeIcon, Messages.getString("PublishDialog.Publish_complete"), false);
		updateAbortButton(Messages.getString("PublishDialog.Close"), true);
		append(Messages.getString("PublishDialog.Pinging_complete"), Color.blue);
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
					abortButton.setText(text);
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
            	label.setText(text);            	
            }
        });	
	}
	
	private void updateProgressValue(final int val)
	{
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	progressBar.setValue(val);
            }
        });			
	}	
}
