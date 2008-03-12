/*
 * A KDE style about box
 * Copyright (C) 2003  Bob Tantlinger
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package net.sf.thingamablog.gui;

import freenet.utils.BrowserLaunch;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;

import net.atlanticbb.tantlinger.i18n.I18n;

import org.jdesktop.jdic.desktop.Desktop;

//import com.Ostermiller.util.Browser;

/**
 * A KDE style about dialog.
 *
 * @author Bob Tantlinger
 * @author <a href="http://thingamablog.sf.net">http://thingamablog.sf.net</a>
 * @author published under the terms and conditions of the
 *      GNU Lesser General Public License.
 *
 * @version 1.0, August 21, 2003
 */

public class JAboutBox extends JDialog
{	
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui");
    private final String MAILTO = "mailto:"; //$NON-NLS-1$
	
	private JLabel appTitleLabel = new JLabel();
	private JLabel appMiscLabel = new JLabel();
	private JLabel appDescLabel = new JLabel();
	private JLabel appCopyRightLabel = new JLabel();
	private JLabel appUrlLabel = new UrlLabel(""); //$NON-NLS-1$

	private PersonPanel authorPanel = new PersonPanel();
	private PersonPanel thanksPanel = new PersonPanel();

	private JTextArea licenseTextArea = new JTextArea();

	private JTabbedPane tabs = new JTabbedPane();
	private JButton closeButton = new JButton(i18n.str("close"));  //$NON-NLS-1$

  /**
   * create an <code>AboutBox</code>
   *
   * @param parent  the parent Frame of this dialog
   * @param title  the dialog title
   * @param license  the license file
   */
	public JAboutBox(Frame parent, String title, File license)
	{	   	
	   	super(parent, title, true);
	   	init(license, true);
	}
	
  /**
   * create an <code>AboutBox</code>
   *
   * @param parent  the parent Dialog of this dialog
   * @param title  the dialog title
   * @param license  the license file
   */
	public JAboutBox(Dialog parent, String title, File license)
	{	   	
	   	super(parent, title, true);
	   	init(license, true);
	}
	
	/**
	 * create an <code>AboutBox</code>
	 *
	 * @param parent  the parent Frame of this dialog
	 * @param title  the dialog title
	 * @param license  the license file
	 * @param thanksTab  show the "Thanks To" tab
	 */
	public JAboutBox(Frame parent, String title, File license, boolean thanksTab)
	{	   	
		super(parent, title, true);
	  	init(license, thanksTab);
	}
	
	/**
	 * create an <code>AboutBox</code>
	 *
	 * @param parent  the parent Dialog of this dialog
	 * @param title  the dialog title
	 * @param license  the license file
	 * @param thanksTab  show the "Thanks To" tab
	 */
	public JAboutBox(Dialog parent, String title, File license, boolean thanksTab)
	{	   	
		super(parent, title, true);
		init(license, thanksTab);
	}
	
	private void init(File license, boolean showThanks)
	{	    
		getContentPane().setLayout(new BorderLayout());
		
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		appTitleLabel.setFont(new Font("Dialog", Font.BOLD, 14)); //$NON-NLS-1$
      	titlePanel.add(appTitleLabel);
		getContentPane().add(titlePanel, BorderLayout.NORTH);
		
		JPanel aboutPanel = new JPanel(new GridBagLayout());
		JPanel itemPanel = new JPanel(new GridLayout(4, 1));
		JPanel pan = new JPanel();
		pan.add(appDescLabel);
		itemPanel.add(pan);
		pan = new JPanel();
		pan.add(appCopyRightLabel);
		itemPanel.add(pan);
		pan = new JPanel();
		pan.add(appUrlLabel);
		itemPanel.add(pan);
		pan = new JPanel();
		pan.add(appMiscLabel);
		itemPanel.add(pan);
		aboutPanel.add(itemPanel);
		tabs.addTab(i18n.str("about"), aboutPanel);  //$NON-NLS-1$
 
		tabs.addTab(i18n.str("authors"), new JScrollPane(authorPanel));  //$NON-NLS-1$
		
		if(showThanks) 
			tabs.addTab(i18n.str("thanks_to"), new JScrollPane(thanksPanel)); //$NON-NLS-1$

		licenseTextArea.setEditable(false);
		readLicense(license);
		tabs.addTab(i18n.str("license_agreement"), new JScrollPane(licenseTextArea)); //$NON-NLS-1$
      
		tabs.setBorder(new EmptyBorder(8, 8, 8, 8));
		getContentPane().add(tabs, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(closeButton);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		closeButton.addActionListener(new ActionListener()
		{
		   public void actionPerformed(ActionEvent e)
			{
			   dispose();
			}
		});
		
		setSize(420, 350);
	}
	
	/**
   	 * Set the application Icon
   	 *
   	 * @param ico  The icon
   	 */
	public void setAppIcon(ImageIcon ico)
	{
		appTitleLabel.setIcon(ico);	
	}
	
	/**
   	 * Set the application Title
   	 *
   	 * @param  title The title
   	 */
	public void setAppTitle(String title)
	{
		appTitleLabel.setText(title);	
	}
	
	public void setMisc(String m)
	{
	    appMiscLabel.setText(m);
	}
	
	/**
   	 * Set the application description
   	 *
   	 * @param d  The description
   	 */
	public void setAppDescription(String d)
	{
		appDescLabel.setText(d);	
	}
	
	/**
   	 * Set the application copyright string
   	 *
   	 * @param c  The copyright string
   	 */
	public void setAppCopyright(String c)
	{
		appCopyRightLabel.setText(c);	
	}
	
	/**
   	 * Set the application url
   	 *
   	 * @param url  the url
   	 */
	public void setAppUrl(String url)
	{
		appUrlLabel.setText(url);	
	}
	
	/**
	 * Add an author to the "Authors" tab
	 * 
	 * The String passed to this method should be of the form:
	 *  "Person's Name\nhttp://website.com\nmailto:email@address.net\nContribution"
	 * 
	 * Each element of the string should be seperated by a newline '\n' 
	 *
	 * @param auth  A string describing an author
	 */	
	public void addAuthor(String auth)
	{
		authorPanel.addPerson(auth);
	}
	
	/**
	 * Add a contributor to the "Thanks To" tab
	 * 
	 * The String passed to this method should be of the form:
	 *  "Person's Name\nhttp://website.com\nmailto:email@address.net\nContribution"
	 * 
	 * Each element of the string should be seperated by a newline '\n' 
	 *
	 * @param auth  A string describing a contributor
	 */	
	public void addContributor(String contrib)
	{
		thanksPanel.addPerson(contrib);
	}
	
	private void readLicense(File f)
	{
	    FileReader reader =  null;
		try
		{
		   	reader = new FileReader(f);
			licenseTextArea.read(reader, null);
		}
		catch(IOException ioe)
		{
		   licenseTextArea.setText("Unable to read " + f.getAbsolutePath()); //$NON-NLS-1$
		}
		finally
		{
		   	try
		   	{
			   if(reader != null)
				   reader.close();
			}
			catch(Exception ex){}
		}
	}
	
	private class PersonPanel extends JPanel implements Scrollable
	{
      	/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private Box pp;

		public PersonPanel()
		{
		    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			pp = Box.createVerticalBox();			
			add(pp);						
		}
		
		public void addPerson(String p)
		{
			StringTokenizer st = new StringTokenizer(p, "\n"); //$NON-NLS-1$
			int count = 0;
			JPanel panel = new JPanel(new GridLayout(st.countTokens(), 1));
			while(st.hasMoreTokens())
			{
				String s = st.nextToken();
				JLabel label;				
				if(s.startsWith(MAILTO) || s.startsWith("http://")) //$NON-NLS-1$
					label = new UrlLabel(s);
				else
					label = new JLabel(s);				
				
				if(count++ == 0)
					label.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
				else
					label.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
				
				JPanel linkPanel = new JPanel(new BorderLayout());			
				linkPanel.add(label, BorderLayout.WEST);
				linkPanel.add(new JPanel(), BorderLayout.CENTER);
				
				panel.add(linkPanel);				
			}
			
			JPanel perPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			perPanel.add(panel);
			
			pp.add(perPanel);
			pp.add(Box.createVerticalStrut(5));
			 
		} 
		
	   	public Dimension getPreferredScrollableViewportSize()
	   	{
	   	 	return getPreferredSize();
	   	}
	   	 
	   	public int getScrollableUnitIncrement(Rectangle vr, int o, int d)
	   	{
	   		return 8; //scroll by 5 pixels at a time	
	   	}
	   	 
	   	public int getScrollableBlockIncrement(Rectangle vr, int o, int d)
	   	{
	   	 	return 8;	
	   	}
	   	 
		public boolean getScrollableTracksViewportWidth()
		{
		 	return getPreferredSize().width <= getVisibleRect().width;	 		
		}
		 
		public boolean getScrollableTracksViewportHeight()
		{
			return getPreferredSize().height <= getVisibleRect().height;	 		
		}
	}
	
	private class UrlLabel extends JLabel implements MouseListener
	{
	   	/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String url = "";		 //$NON-NLS-1$
		private final Color LINK_COLOR = Color.blue;
		private final Color HOVER_COLOR = Color.red;

		public UrlLabel(String s)
		{
		    setForeground(LINK_COLOR);
			setText(s);
			addMouseListener(this);
		}
		
		public void setText(String s)
		{
		   	url = s;
			if(s.startsWith(MAILTO) && s.length() > MAILTO.length())
			   s = s.substring(MAILTO.length(), s.length());
			
			super.setText(s);
		}
		
		public void mouseEntered(MouseEvent e)
		{
		   	setCursor(new Cursor(Cursor.HAND_CURSOR));
			setForeground(HOVER_COLOR);
		}
		public void mouseExited(MouseEvent e)
		{
		   	setCursor(Cursor.getDefaultCursor());
			setForeground(LINK_COLOR);
		}
		public void mousePressed(MouseEvent e)
		{
			BrowserLaunch.launch(url);
		}
		public void mouseReleased(MouseEvent e){}
		public void mouseClicked(MouseEvent e){}
	}
}
