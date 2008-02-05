/*
 * Created on Aug 2, 2004
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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.sf.thingamablog.backend.HSQLDatabaseBackend;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.WeblogList;
import net.sf.thingamablog.blog.WeblogsDotComPing;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.transport.FTPTransport;
import net.tb.legacy.LegacyAuthor;
import net.tb.legacy.LegacyEntry;
import net.tb.legacy.LegacyWeblog;



/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class ImportLegacyWeblogDialog extends JDialog
{
	private JButton browseButton;
	private JButton importButton;
	private JButton closeButton;
	
	private JTextArea outputArea;
	private JTextField pathField;
	
	private boolean isImporting;
	private JFileChooser fc;
	private WeblogList weblogList;
	
	private File curBlog = null;
	private File dbDir = null;
	private HSQLDatabaseBackend backend;
	
	public ImportLegacyWeblogDialog(Frame f, File dir, 
	net.sf.thingamablog.blog.WeblogList list,
	HSQLDatabaseBackend bEnd)
	{
		super(f, Messages.getString("ImportLegacyWeblogDialog.Import_Legacy_Weblog"), true);		 //$NON-NLS-1$
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		weblogList = list;
		dbDir = dir;
		backend = bEnd;
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			public boolean accept(File f)
			{
				return f.isDirectory() || 
					f.getName().equalsIgnoreCase("blog.dat"); //$NON-NLS-1$
			}
				
			public String getDescription()
			{
				return "blog.dat";	 //$NON-NLS-1$
			}			
		});
		
		addWindowListener(new WindowAdapter()
		{		
			public void windowClosing(WindowEvent e)
			{
				if(!isImporting)
					dispose();
			}
		});
		
		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
		
		outputArea = new JTextArea();
		outputArea.setEditable(false);		
		
		pathField = new JTextField();
		pathField.setEditable(false);		
		
		JPanel importPanel = new JPanel(new BorderLayout(5, 5));
		importPanel.add(pathField, BorderLayout.NORTH);
		importPanel.add(new JScrollPane(outputArea));
		importPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		ActionListener lst = new ButtonHandler();
		browseButton = new JButton(Messages.getString("ImportLegacyWeblogDialog.Browse")); //$NON-NLS-1$
		browseButton.addActionListener(lst);
		
		importButton = new JButton(Messages.getString("ImportLegacyWeblogDialog.Import")); //$NON-NLS-1$
		importButton.addActionListener(lst);
		importButton.setEnabled(false);
		
		closeButton = new JButton(Messages.getString("ImportLegacyWeblogDialog.Close")); //$NON-NLS-1$
		closeButton.addActionListener(lst);
		
		JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));
		buttonPanel.add(browseButton);
		buttonPanel.add(importButton);
		buttonPanel.add(new JPanel());
		buttonPanel.add(closeButton);
		
		JPanel sidePanel = new JPanel();
		sidePanel.add(buttonPanel);
		
		mainPanel.add(sidePanel, BorderLayout.EAST);
		mainPanel.add(importPanel, BorderLayout.CENTER);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel);
		setSize(430, 285);
		setResizable(false);		
	}
	
	private void importWeblog()
	{
		if(curBlog == null)
			return;
		
		Thread th = new Thread()
		{
			public void run()
			{
				TBWeblog weblog = null;
				try
				{				
					isImporting = true;
					LegacyWeblog oldBlog = new LegacyWeblog();
					
					println("Opening 0.9.x Weblog for Importing"); //$NON-NLS-1$
					oldBlog.connectToWeblog(curBlog);
					weblog = new TBWeblog(dbDir);
					weblog.setBackend(backend);
					
					copySettings(oldBlog, weblog);										
					copyTemplates(oldBlog, weblog);
					copyAuthorsAndCats(oldBlog, weblog);
					copyEntries(oldBlog, weblog.getKey());
					println("Import Complete!!!");					 //$NON-NLS-1$
					oldBlog.shutdown();
					
					weblog.updateArchives();
					weblog.setPublishAll(true);
					weblogList.addWeblog(weblog);
				}
				catch(Exception ex)
				{
					//import failed
					try
					{					
						if(weblog != null)
							weblog.deleteAll();
					}
					catch(Exception e){}
					println("Import Failed!!!"); //$NON-NLS-1$
					ex.printStackTrace();				
				}
				finally
				{
			        SwingUtilities.invokeLater(new Runnable() {
			        	public void run(){
			        	    closeButton.setEnabled(true);
			        	}
			        });				    
					isImporting = false;
				}
			}
		};
		
		browseButton.setEnabled(false);
		importButton.setEnabled(false);
		closeButton.setEnabled(false);
		outputArea.setText(""); //$NON-NLS-1$
		th.start();
	}
	
	private void println(final String s)
	{		
        SwingUtilities.invokeLater(new Runnable() {
        	public void run(){
        	    outputArea.append(s + '\n');
        		outputArea.setCaretPosition(outputArea.getText().length());
        	}
        });	
	}
	

	
	private void copySettings(LegacyWeblog blog, TBWeblog tbw)
	throws Exception
	{
		tbw.setTitle(blog.getTitle());
		tbw.setDescription(blog.getDescription());
		tbw.setArchiveBaseDate(blog.getBaseDate());
		tbw.setFrontPageFileName(blog.getFrontPageFileName());
		tbw.setRssFileName(blog.getRssFileName());
		
		String basePath = blog.getBasePath();
		String baseURL = blog.getBaseUrl();
		String arcsURL = blog.getArchivesUrl();
		String mediaURL = blog.getMediaUrl();
		
		tbw.setBlogUrls(basePath, baseURL, arcsURL, mediaURL);
		
		int arcPolicy = blog.getArchivePolicy();
		if(arcPolicy == LegacyWeblog.ARCHIVE_BY_DAY_INTERVAL)
		{			
			tbw.setArchivePolicy(TBWeblog.ARCHIVE_BY_DAY_INTERVAL);
		}
		else if(arcPolicy == LegacyWeblog.ARCHIVE_MONTHLY)
		{			
			tbw.setArchivePolicy(TBWeblog.ARCHIVE_MONTHLY);
		}
		else if(arcPolicy == LegacyWeblog.ARCHIVE_WEEKLY)
		{			
			tbw.setArchivePolicy(TBWeblog.ARCHIVE_WEEKLY);
		}
		
		tbw.setArchiveByDayInterval(blog.getDaysArchiveInterval());
		
		String format = blog.getArchiveRangeFormat();
		boolean span = blog.isSpanArchiveRangeFormat();		
		tbw.getPageGenerator().setArchiveRangeFormat(format, span);
		
		copyFTPSettings(blog, tbw);
		copyPingSettings(blog, tbw);				
	}
	
	private void copyFTPSettings(LegacyWeblog blog, TBWeblog tbw)
	{
		try
		{
			File dir = blog.getBaseDirectory();
			File propFile = new File(dir, "ftp.properties"); //$NON-NLS-1$
			Properties props = new Properties();
			FileInputStream fis = new FileInputStream(propFile);
			props.load(fis);
			
			FTPTransport ftp = new FTPTransport();
			if(props.getProperty("SERVER") != null) //$NON-NLS-1$
				ftp.setAddress(props.getProperty("SERVER")); //$NON-NLS-1$
			if(props.getProperty("USER") != null) //$NON-NLS-1$
				ftp.setUserName(props.getProperty("USER")); //$NON-NLS-1$
			if(props.getProperty("SAVE_PASSWORD") != null && //$NON-NLS-1$
			props.getProperty("SAVE_PASSWORD").equals("true")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				ftp.setSavePassword(true);
				if(props.getProperty("PASSWORD") != null) //$NON-NLS-1$
					ftp.setPassword(props.getProperty("PASSWORD")); //$NON-NLS-1$
			}
			else
			{			
				ftp.setSavePassword(false);
			}
			
			if(props.getProperty("PASSIVE_MODE") != null &&  //$NON-NLS-1$
			props.getProperty("PASSIVE_MODE").equals("true")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				ftp.setPassiveMode(true);
			}
			else
			{
				ftp.setPassiveMode(false);
			}
			
			int port = 21;
			try
			{
				port = Integer.parseInt(props.getProperty("PORT")); //$NON-NLS-1$
			}
			catch(Exception ex){}
			ftp.setPort(port);
			
			tbw.setPublishTransport(ftp);
			fis.close();			
		}
		catch(Exception ex){}
	}
	
	private void copyPingSettings(LegacyWeblog blog, TBWeblog tbw)
	{
		try
		{
			File dir = blog.getBaseDirectory();
			File propFile = new File(dir, "ping.properties"); //$NON-NLS-1$
			Properties props = new Properties();
			FileInputStream fis = new FileInputStream(propFile);
			props.load(fis);
			
			int count = 0;
			try
			{
				count = Integer.parseInt(props.getProperty("NUM_SERVICES"));			 //$NON-NLS-1$
			}
			catch(Exception ex)
			{
				fis.close();
				return;
			}
			
			for(int i = 0; i < count; i++)
			{
				String url = ""; //$NON-NLS-1$
				String name = ""; //$NON-NLS-1$
				boolean enabled = true;
				
				if(props.getProperty(i + "_NAME") != null) //$NON-NLS-1$
					name = props.getProperty(i + "_NAME"); //$NON-NLS-1$
				else
					continue;
				
				if(props.getProperty(i + "_URL") != null) //$NON-NLS-1$
					url = props.getProperty(i + "_URL"); //$NON-NLS-1$
				else
					continue;
				
				enabled = props.getProperty(i + "_ENABLED") != null && //$NON-NLS-1$
					props.getProperty(i + "_ENABLED").equals("true"); //$NON-NLS-1$ //$NON-NLS-2$
				
				WeblogsDotComPing ping = new WeblogsDotComPing();
				ping.setServiceName(name);
				ping.setServiceUrl(url);
				ping.setEnabled(enabled);
				tbw.addPingService(ping);
			}
			
			fis.close();
		}
		catch(Exception ex){}
	}
	
	private void copyEntries(LegacyWeblog blog, String key)
	throws Exception
	{
		println("\nImporting Entries..."); //$NON-NLS-1$
		long ids[] = blog.getAllEntryIDs();
		for(int i = 0; i < ids.length; i++)
		{
			LegacyEntry be = blog.getEntry(ids[i]);
			net.sf.thingamablog.blog.BlogEntry newEntry = 
				new net.sf.thingamablog.blog.BlogEntry();
			
			println("Importing: " + be.getTitle()); //$NON-NLS-1$
			newEntry.setTitle(be.getTitle());
			newEntry.setDate(be.getDate());
			newEntry.setCategories(be.getCategories());
			newEntry.setText(be.getText());
			newEntry.setLastModified(be.getLastModified());
			
			net.sf.thingamablog.blog.Author auth = new net.sf.thingamablog.blog.Author();
			LegacyAuthor oldAuth = be.getAuthor();
			if(oldAuth != null)
			{			
				if(oldAuth.getName() != null)
					auth.setName(oldAuth.getName());
				if(oldAuth.getUrl() != null)
					auth.setUrl(oldAuth.getUrl());
				if(oldAuth.getEmailAddress() != null)
					auth.setEmailAddress(oldAuth.getEmailAddress());
			
				newEntry.setAuthor(auth);
			}
			
			backend.importEntry(key, newEntry, be.getID());
						
		}
	}
	
	private void copyAuthorsAndCats(LegacyWeblog blog, TBWeblog tbw)
	throws Exception
	{
		LegacyAuthor a[] = blog.getAuthors();
		for(int i = 0; i < a.length; i++)
		{
			net.sf.thingamablog.blog.Author auth = new net.sf.thingamablog.blog.Author();
			auth.setName(a[i].getName());
			auth.setEmailAddress(a[i].getEmailAddress());
			auth.setUrl(a[i].getUrl());
			
			println("Importing Author: " + auth.getName());			 //$NON-NLS-1$
			tbw.addAuthor(auth);
		}
		
		String cats[] = blog.getCategories();
		for(int i = 0; i < cats.length; i++)
		{
			println("Importing Category: " + cats[i]); //$NON-NLS-1$
			tbw.addCategory(cats[i]);
		}
	}
	
	
	
	private void copyTemplates(LegacyWeblog blog, TBWeblog tbw)
	throws IOException
	{
		File outDir = tbw.getTemplateDirectory();
		
		println("\nImporting main.template"); //$NON-NLS-1$
		String fp = readTemplate(blog.getMainPageTemplate());
		writeTemplate(new File(outDir, "main.template"), fp); //$NON-NLS-1$
		
		println("Importing archive.template"); //$NON-NLS-1$
		String arc = readTemplate(blog.getArchiveTemplate());
		writeTemplate(new File(outDir, "archive.template"), arc); //$NON-NLS-1$
		
		println("Importing category.template"); //$NON-NLS-1$
		writeTemplate(new File(outDir, "category.template"), arc); //$NON-NLS-1$
		
		println("Importing index.template"); //$NON-NLS-1$
		String index = readTemplate(blog.getIndexTemplate());
		writeTemplate(new File(outDir, "index.template"), index); //$NON-NLS-1$
	}
	
	private void writeTemplate(File f, String text) throws IOException
	{
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		pw.print(text);
		pw.close();
	}
	
	private String readTemplate(File t) throws IOException
	{
		String template = ""; //$NON-NLS-1$
		BufferedReader reader = new BufferedReader(new FileReader(t));
		String line;        
		while((line = reader.readLine()) != null)
			template += line + '\n';
        
		reader.close();
		return template;
	}
	
	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == closeButton)
			{
				dispose();
			}
			else if(e.getSource() == browseButton)
			{
				int r = fc.showOpenDialog(ImportLegacyWeblogDialog.this);
				if(r == JFileChooser.CANCEL_OPTION)
					return;
				
				File f = fc.getSelectedFile();
				if(f != null && f.exists() && f.isFile())
				{
					pathField.setText(f.getAbsolutePath());
					curBlog = f;
					importButton.setEnabled(true);					
				}
			}
			else if(e.getSource() == importButton)
			{
				importWeblog();
			}
		}
	}
}
