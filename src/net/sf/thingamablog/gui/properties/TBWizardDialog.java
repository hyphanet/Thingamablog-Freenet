/*
 * Created on Jun 16, 2004
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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.WeblogBackend;
import net.sf.thingamablog.blog.WeblogsDotComPing;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.MultilineText;
import net.sf.thingamablog.gui.editor.TextEditPopupManager;

import com.Ostermiller.util.Browser;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBWizardDialog extends JDialog
{
	private static final String CANCEL = Messages.getString("TBWizardDialog.Cancel"); //$NON-NLS-1$
	private static final String FINISH = Messages.getString("TBWizardDialog.Finish");	 //$NON-NLS-1$
	
	private CardLayout wizLayout;
	private JPanel wizPanel;
	
	private PropertyPanel starterPanel;
	private PropertyPanel titlePanel;
	private PropertyPanel catPanel;
	private PropertyPanel authPanel;
	private PropertyPanel templPanel;
	private PropertyPanel transportPanel;
	private PropertyPanel donePanel;
	private Vector panels = new Vector();	
		
	private JButton nextButton, backButton, doneButton;
	
	private boolean isCancelled;
	
	private TBWeblog weblog;
	private WeblogBackend backend;

	private TextEditPopupManager popupManager = new TextEditPopupManager();
	
	public TBWizardDialog(Frame f, File dir, WeblogBackend backend)
	{
		super(f, true);
		setTitle(Messages.getString("TBWizardDialog.New_Weblog")); //$NON-NLS-1$
		
		this.backend = backend;
		//catStore = cs;
		//authStore = as;
		
		WindowAdapter windowAdapter = new WindowAdapter()
		{
			public void windowClosing(WindowEvent windowEvent)
			{
				cancelDialog();
			}
		};
		addWindowListener(windowAdapter);
		
		weblog = new TBWeblog(dir);
		weblog.setBackend(backend);
		weblog.setPublishTransport(new net.sf.thingamablog.transport.FTPTransport());
		//weblog.setAuthorStore(authStore);
		//weblog.setCategoryStore(catStore);
		
		wizLayout = new CardLayout();
		wizPanel = new JPanel(wizLayout);
		
		starterPanel = new StarterPanel();
		starterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(starterPanel);
		
		titlePanel = new TitleDescrPanel();
		titlePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(titlePanel);
		
		catPanel = new CategoriesPanel();
		catPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(catPanel);
		
		authPanel = new AuthorsPanel();
		authPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(authPanel);
		
		templPanel = new TemplatePanel();
		templPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(templPanel);
		
		transportPanel = new TransportPanel();
		transportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(transportPanel);
		
		donePanel = new DonePanel();
		donePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panels.add(donePanel);
		
		wizPanel.add(starterPanel, "1"); //$NON-NLS-1$
		wizPanel.add(titlePanel, "2"); //$NON-NLS-1$
		wizPanel.add(catPanel, "3"); //$NON-NLS-1$
		wizPanel.add(authPanel, "4"); //$NON-NLS-1$
		wizPanel.add(templPanel, "5"); //$NON-NLS-1$
		wizPanel.add(transportPanel, "6"); //$NON-NLS-1$
		wizPanel.add(donePanel, "7");	 //$NON-NLS-1$
		
		ActionListener listener = new ButtonHandler();
		nextButton = new JButton(Messages.getString("TBWizardDialog.Next")); //$NON-NLS-1$
		nextButton.addActionListener(listener);
		backButton = new JButton(Messages.getString("TBWizardDialog.Back")); //$NON-NLS-1$
		backButton.setEnabled(false);
		backButton.addActionListener(listener);
		doneButton = new JButton(CANCEL);
		doneButton.addActionListener(listener);
		
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.setBorder(new EtchedBorder());
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));		
		buttonPanel.add(backButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(doneButton);
		controlPanel.add(buttonPanel);
		
		JLabel img = new JLabel();
		img.setVerticalAlignment(SwingConstants.TOP);
		img.setOpaque(true);
		img.setBackground(Color.WHITE);
		img.setIcon(Utils.createIcon(TBGlobals.RESOURCES + "wizard.jpg")); //$NON-NLS-1$
		
		getContentPane().add(wizPanel, BorderLayout.CENTER);
		getContentPane().add(controlPanel, BorderLayout.SOUTH);
		getContentPane().add(img, BorderLayout.WEST);
		
		pack();
		setResizable(false);
	}
	
	public TBWeblog getWeblog()
	{
		return weblog;
	}
	
	public boolean hasUserCancelled()
	{
		return isCancelled;
	}
	
	private void doFinish()
	{		
		for(int i = 0; i < panels.size(); i++)
		{
			PropertyPanel p = (PropertyPanel)panels.elementAt(i);
			p.saveProperties();
		}
		
		//add a couple ping services
		WeblogsDotComPing ping = new WeblogsDotComPing();
        
        //removed because the TAMB ping server has been shutdown...
		/*ping.setServiceName("Updated Thingamablogs");
		ping.setServiceUrl("http://thingamablog.sourceforge.net/rpc.php");
		ping.setEnabled(true);		
		weblog.addPingService(ping);*/
		
		ping = new WeblogsDotComPing();
		ping.setServiceName("weblogs.com");
		ping.setServiceUrl("http://rpc.weblogs.com/RPC2");
		ping.setEnabled(false);
		weblog.addPingService(ping);	
		
		dispose();
	}
	
	private void cancelDialog()
	{
		isCancelled = true;
		try{
			weblog.deleteAll();
		}catch(BackendException ex){}
		dispose();		
	}
	
	private JLabel createHeaderLabel(String text)
	{
		JLabel label = new JLabel("<html><h2>" + text + "</h2></html>"); //$NON-NLS-1$ //$NON-NLS-2$
		return label;
	}
	


	private boolean isCurrentPanelValid()
	{
		for(int i = 0; i < panels.size(); i++)
		{
			PropertyPanel p = (PropertyPanel)panels.elementAt(i);
			if(p.isVisible())
			{
				return p.isValidData();
			}
		}
		
		return false;
	}
	
	private class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if(e.getSource() == nextButton)
			{
				if(!donePanel.isVisible())
				{				
					if(isCurrentPanelValid())
						wizLayout.next(wizPanel);					
				}
														
				if(donePanel.isVisible())
				{
					doneButton.setText(FINISH);
					nextButton.setEnabled(false);
				}
				backButton.setEnabled(true);
			}
			else if(e.getSource() == backButton)
			{
				if(!starterPanel.isVisible())
					wizLayout.previous(wizPanel);				
				if(starterPanel.isVisible())
					backButton.setEnabled(false);				
				if(doneButton.getText().equals(FINISH))
					doneButton.setText(CANCEL);
				nextButton.setEnabled(true);
			}
			else if(e.getSource() == doneButton)
			{
				//the new Weblog was canceled, so delete the 
				//directory structure that was created when
				//the Weblog was instantiated
				if(doneButton.getText().equals(FINISH))
				{
					doFinish();				
				}
				else
				{
					cancelDialog();	
				}								
			}
		}
	}
	


	
	private class StarterPanel extends PropertyPanel
	{
		private JTextField pathField = new JTextField(20);
		private JTextField urlField = new JTextField(20);
		
		public StarterPanel()
		{			
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Weblog_Wizard"));			 //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.welcome_panel_text"); //$NON-NLS-1$
			
			
			LabelledItemPanel lip = new LabelledItemPanel();
			lip.addItem(Messages.getString("TBWizardDialog.Base_Path"), pathField); //$NON-NLS-1$
			lip.addItem(Messages.getString("TBWizardDialog.Base_URL"), urlField); //$NON-NLS-1$
			
			popupManager.addJTextComponent(pathField);
			popupManager.addJTextComponent(urlField);
			
			setLayout(new BorderLayout());	
			add(header, BorderLayout.NORTH);
			add(new MultilineText(text), BorderLayout.CENTER);
			add(lip, BorderLayout.SOUTH);			
		}
		
		public boolean isValidData()
		{
			try
			{
				URL url = new URL(urlField.getText());				
			}
			catch(Exception ex)
			{
				JOptionPane.showMessageDialog(TBWizardDialog.this,
					Messages.getString("TBWizardDialog.invalid_url_prompt"), Messages.getString("TBWizardDialog.Invalid_URL"),  //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.WARNING_MESSAGE);
				return false;
			}
			
			if(pathField.getText() == null || pathField.getText().equals("")) //$NON-NLS-1$
			{
				JOptionPane.showMessageDialog(TBWizardDialog.this,
					Messages.getString("TBWizardDialog.invalid_path_prompt"), Messages.getString("TBWizardDialog.Invalid_path"),  //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.WARNING_MESSAGE);
				return false;
			}
			
			return true;
		}
		
		public void saveProperties()
		{
			String path = pathField.getText();
			String url = urlField.getText();
			if(!url.endsWith("/")) //$NON-NLS-1$
				url += "/";; //$NON-NLS-1$
			String arcUrl = url + "archives"; //$NON-NLS-1$
			String mediaUrl = url + "media"; //$NON-NLS-1$
			
			weblog.setBlogUrls(path, url, arcUrl, mediaUrl);						
		}
	}
	
	private class TitleDescrPanel extends PropertyPanel
	{
		private JTextField titleField = new JTextField();
		private JTextArea textArea = new JTextArea(4, 4);
		
		public TitleDescrPanel()
		{
			setLayout(new BorderLayout());
			
			JPanel instrPanel = new JPanel(new BorderLayout());
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Title_and_Description")); //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.title_panel_text"); //$NON-NLS-1$
			instrPanel.add(header, BorderLayout.NORTH);
			instrPanel.add(new MultilineText(text), BorderLayout.CENTER);
			
			popupManager.addJTextComponent(titleField);
			popupManager.addJTextComponent(textArea);
			
			LabelledItemPanel lip = new LabelledItemPanel();
			lip.addItem(Messages.getString("TBWizardDialog.Site_Title"), titleField); //$NON-NLS-1$
			lip.addItem(Messages.getString("TBWizardDialog.Description"), new JScrollPane(textArea)); //$NON-NLS-1$
			
			add(instrPanel, BorderLayout.NORTH);
			add(lip, BorderLayout.CENTER);						
		}
		
		public boolean isValidData()
		{			
			if(titleField.getText().equals("")) //$NON-NLS-1$
			{
				JOptionPane.showMessageDialog(TBWizardDialog.this, 
					Messages.getString("TBWizardDialog.invalid_title_prompt"), Messages.getString("TBWizardDialog.Title"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}
			return true;	
		}
		
		public void saveProperties()
		{
			weblog.setTitle(titleField.getText());
			weblog.setDescription(textArea.getText());
		}		
	}
	
	private class CategoriesPanel extends PropertyPanel
	{
		private EditableList list;
		private WeblogEditableListModel model;
		
		public CategoriesPanel()
		{
			setLayout(new BorderLayout(5, 5));
			
			JPanel instrPanel = new JPanel(new BorderLayout());
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Categories")); //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.categories_panel_text"); //$NON-NLS-1$
			instrPanel.add(header, BorderLayout.NORTH);
			instrPanel.add(new MultilineText(text), BorderLayout.CENTER);
			
			model = new WeblogEditableListModel(WeblogEditableListModel.CATEGORIES);
			list = new EditableList(model);
			setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			
			add(instrPanel, BorderLayout.NORTH);
			add(list, BorderLayout.CENTER);										
		}
		
		public boolean isValidData()
		{			
			return true;
		}
		
		public void saveProperties()
		{
			//ListModel lm = catList.getModel();			
			try
			{
				model.syncListWithWeblog(weblog);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}		
	}
	
	private class AuthorsPanel extends PropertyPanel
	{
		private EditableList list;
		private WeblogEditableListModel model;
		
		public AuthorsPanel()
		{
			setLayout(new BorderLayout(5, 5));
			
			JPanel instrPanel = new JPanel(new BorderLayout());
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Authors")); //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.authors_panel_text"); //$NON-NLS-1$
			instrPanel.add(header, BorderLayout.NORTH);
			instrPanel.add(new MultilineText(text), BorderLayout.CENTER);
			
			model = new WeblogEditableListModel(WeblogEditableListModel.AUTHORS);
			list = new EditableList(model);
			setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			
			add(instrPanel, BorderLayout.NORTH);
			add(list, BorderLayout.CENTER);										
		}
		
		public boolean isValidData()
		{			
			return true;
		}
		
		public void saveProperties()
		{
			try
			{
				model.syncListWithWeblog(weblog);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}		
	}
	
	private class TransportPanel extends PropertyPanel
	{
		TBPublishTransportPanel pubPanel;
		
		public TransportPanel()
		{
			setLayout(new BorderLayout());
			
			JPanel instrPanel = new JPanel(new BorderLayout());
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Publishing")); //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.publishing_panel_text"); //$NON-NLS-1$
			instrPanel.add(header, BorderLayout.NORTH);
			instrPanel.add(new MultilineText(text), BorderLayout.CENTER);

			pubPanel = new TBPublishTransportPanel(weblog);
			add(instrPanel, BorderLayout.NORTH);
			add(pubPanel, BorderLayout.CENTER);							
		}
		
		public boolean isValidData()
		{			
			return pubPanel.isValidData();
		}
		
		public void saveProperties()
		{
			pubPanel.saveProperties();
		}		
	}
	
	private class TemplatePanel extends PropertyPanel
	{
		private JComboBox cssCombo;
		
		public TemplatePanel()
		{
			setLayout(new BorderLayout());
			
			JPanel instrPanel = new JPanel(new BorderLayout());
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Templates")); //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.templates_panel_text"); //$NON-NLS-1$
			instrPanel.add(header, BorderLayout.NORTH);
			instrPanel.add(new MultilineText(text), BorderLayout.CENTER);
			
			final File tmplDir = new File(TBGlobals.DEFAULT_TMPL_DIR);
			File css[] = tmplDir.listFiles(new FileFilter()
			{
				public boolean accept(File f)
				{
					if(f.isDirectory())
						return true;
					return false;					
				}
			});
			
			DefaultListCellRenderer renderer = new DefaultListCellRenderer()
			{
				public Component getListCellRendererComponent(
				JList l,Object v, int i, boolean isSel, boolean hasFocus)
				{
					File f = (File)v;
					String name = f.getName();
					return super.getListCellRendererComponent(l, name, i, isSel, hasFocus);
				}	
			};
			cssCombo = new JComboBox(css);
			cssCombo.setRenderer(renderer);
			
			
			JButton prevButton = new JButton(Messages.getString("TBWizardDialog.Preview")); //$NON-NLS-1$
			prevButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//get the selected style
					File f = (File)cssCombo.getSelectedItem();
					if(f.isFile())return;
					
					//get the preview file for the selected style
					File preview = new File(f, "preview.html");					 //$NON-NLS-1$
					try
					{
						URL url = preview.toURL();
						Browser.displayURL(url.toString());
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}					
				}
			});
			
			JPanel selPanel = new JPanel();
			selPanel.add(cssCombo);
			selPanel.add(prevButton);
			
			add(instrPanel, BorderLayout.NORTH);
			add(selPanel, BorderLayout.CENTER);								
		}
		
		public boolean isValidData()
		{			
			return true;
		}
		
		public void saveProperties()
		{
			File dir = (File)cssCombo.getSelectedItem();
			File files[] = dir.listFiles(new FileFilter()
			{
				public boolean accept(File f)
				{
					if(f.isDirectory() || f.getName().endsWith(".html")) //$NON-NLS-1$
						return false;
					
					return true;	
				}
			});
			
			//copy the contents of the selected style dir to the weblog
			for(int i = 0; i < files.length; i++)
			{
				File f = new File(weblog.getWebFilesDirectory(), files[i].getName());
				Utils.copyFile(files[i].getAbsolutePath(), f.getAbsolutePath());	
			}
			
			//copy over the templates
			dir = new File(TBGlobals.DEFAULT_TMPL_DIR);
			files = dir.listFiles(new FileFilter()
			{
				public boolean accept(File f)
				{
					if(f.isDirectory() || !f.getName().endsWith(".template")) //$NON-NLS-1$
						return false;
					
					return true;	
				}
			});
			
			for(int i = 0; i < files.length; i++)
			{
				File f = new File(weblog.getTemplateDirectory(), files[i].getName());
				Utils.copyFile(files[i].getAbsolutePath(), f.getAbsolutePath());	
			}						
		}		
	}
	
	
	private class DonePanel extends PropertyPanel
	{
		public DonePanel()
		{
			JLabel header = createHeaderLabel(Messages.getString("TBWizardDialog.Done")); //$NON-NLS-1$
			String text =
			Messages.getString("TBWizardDialog.finished_panel_text"); //$NON-NLS-1$
			
			setLayout(new BorderLayout());
			add(header, BorderLayout.NORTH);
			add(new MultilineText(text), BorderLayout.CENTER);	
		}
		
		public boolean isValidData()
		{			
			return true;
		}
		
		public void saveProperties()
		{			
		}		
	}
}
