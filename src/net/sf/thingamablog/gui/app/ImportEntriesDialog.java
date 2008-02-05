/*
 * Created on Aug 6, 2004
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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.gui.CustomFileFilter;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.editor.TextEditPopupManager;
import net.sf.thingamablog.xml.RSSImportExport;

/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class ImportEntriesDialog extends JDialog
{
	private JButton cancelButton;
	private JButton importButton;
	private JButton browseButton;
	
	private JTextField urlField;
	private JLabel message;
	private Weblog weblog;
	
	private JProgressBar progress;
	
	public ImportEntriesDialog(Frame owner, Weblog w)
	{
		super(owner, Messages.getString("ImportEntriesDialog.Import_Entries")); //$NON-NLS-1$
		setModal(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if(cancelButton.isEnabled())
					dispose();		
			}
		});
		
		weblog = w;
		
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		String msg = Messages.getString("ImportEntriesDialog.enter_url_prompt"); //$NON-NLS-1$
		message = new JLabel(msg);
		mainPanel.add(message, BorderLayout.NORTH);
		
		JPanel urlPanel = new JPanel(new BorderLayout(5, 5));
		browseButton = new JButton("..."); //$NON-NLS-1$
		browseButton.setMargin(new Insets(1, 1, 1, 1));
		browseButton.addActionListener(new ButtonHandler());
		TextEditPopupManager popupMan = new TextEditPopupManager();
		urlField = new JTextField(35);
		popupMan.addJTextComponent(urlField);
		urlField.addCaretListener(new UrlValidator());
		
		urlPanel.add(urlField, BorderLayout.CENTER);
		urlPanel.add(browseButton, BorderLayout.EAST);
		
		JPanel spacer = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacer.add(urlPanel);
		
		mainPanel.add(spacer, BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		
		progress = new JProgressBar();
		progress.setPreferredSize(urlField.getPreferredSize());
		mainPanel.add(progress, BorderLayout.SOUTH);
		
		importButton = new JButton(Messages.getString("ImportEntriesDialog.Import")); //$NON-NLS-1$
		importButton.addActionListener(new ButtonHandler());
		importButton.setEnabled(false);
		
		cancelButton = new JButton(Messages.getString("ImportEntriesDialog.Close")); //$NON-NLS-1$
		cancelButton.addActionListener(new ButtonHandler());	
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		buttonPanel.add(importButton);
		buttonPanel.add(cancelButton);
		
		spacer = new JPanel();
		spacer.add(buttonPanel);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(mainPanel, BorderLayout.CENTER);
		contentPanel.add(spacer, BorderLayout.SOUTH);
		setContentPane(contentPanel);
		
		pack();
		setResizable(false);							
	}
	
	private void doImport()
	{
		Thread th = new Thread()
		{
			public void run()
			{
				message.setText(Messages.getString("ImportEntriesDialog.importing_entries_prompt")); //$NON-NLS-1$
				boolean err = false;
				try
				{				
					RSSImportExport.importEntriesFromFeed(urlField.getText(), weblog);					
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				    err = true;
				}
				
				final boolean didErrOccur = err;
		        SwingUtilities.invokeLater(new Runnable() 
		        {
		        	public void run()
		        	{
		        	    if(!didErrOccur)
		        	    {
		        	        message.setText(Messages.getString("ImportEntriesDialog.imported_ok_prompt")); //$NON-NLS-1$
		        	    }
		        	    else
		        	    {
						    message.setText(Messages.getString("ImportEntriesDialog.import_failed_prompt")); //$NON-NLS-1$
							message.setForeground(Color.red);
		        	    }
		        	    
						progress.setIndeterminate(false);				
						cancelButton.setEnabled(true);
		        	}
		        });	
			}
		};
		
		//System.out.println("Import"); //$NON-NLS-1$
		
		urlField.setEditable(false);
		browseButton.setEnabled(false);
		importButton.setEnabled(false);
		cancelButton.setEnabled(false);
		progress.setIndeterminate(true);
		th.start();
	}
	
	private class UrlValidator implements CaretListener
	{
		public void caretUpdate(CaretEvent e)
		{
			try
			{
				URL url = new URL(urlField.getText());
				importButton.setEnabled(true);
			}
			catch(MalformedURLException ex)
			{
				importButton.setEnabled(false);
			}
		}
	}
	
	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{			
			if(e.getSource() == cancelButton)
			{				
				dispose();
			}
			else if(e.getSource() == importButton)
			{
				doImport();
			}
			else if(e.getSource() == browseButton)
			{
				JFileChooser fc = new JFileChooser();
				CustomFileFilter cff = new CustomFileFilter();
				cff.addExtension("rss"); //$NON-NLS-1$
				cff.addExtension("rdf"); //$NON-NLS-1$
				cff.addExtension("xml");				 //$NON-NLS-1$
				fc.setFileFilter(cff);				
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);				
				fc.setDialogTitle(Messages.getString("ImportEntriesDialog.Import_Entries_from_Feed")); //$NON-NLS-1$
				int r = fc.showOpenDialog(ImportEntriesDialog.this);
				fc.setApproveButtonText(Messages.getString("ImportEntriesDialog.Import")); //$NON-NLS-1$
				if(r == JFileChooser.CANCEL_OPTION)
					return;
        		
				if(fc.getSelectedFile() == null)
					return;
        		
        		try
        		{        		
					urlField.setText(fc.getSelectedFile().toURL().toString());
        		}
        		catch(Exception ex){}				
			} 
		}
	}	
}
