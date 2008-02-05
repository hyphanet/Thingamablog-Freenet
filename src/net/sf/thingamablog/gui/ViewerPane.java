/*
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
 */

package net.sf.thingamablog.gui;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;

import com.Ostermiller.util.Browser;


/**
 * A view pane
 * 
 * @author Bob Tantlinger
 */
public class ViewerPane extends JComponent
{    
    private final String CSS = TBGlobals.RESOURCES + "viewer.css";
    private final int LABEL_GAP = 2;
    
    private Action copy, selectAll;
	private JEditorPane textArea;
	private JLabel icon;
	private JPanel labelPanel;
	private JPanel headerPanel;	
	
	private ViewerPaneModel model;
	private Vector headerData = new Vector(4, 2);
	private Vector headerTitles = new Vector(4, 2);
	private ChangeListener updateHandler = new UpdateHandler();		
	private Font descrFont = new Font("Dialog", Font.PLAIN, 12); //$NON-NLS-1$
	private Font titleFont = new Font("Dialog", Font.BOLD, 12);
    
	public ViewerPane()
	{
		textArea = new JEditorPane();
		textArea.setContentType("text/html");         //$NON-NLS-1$
		textArea.setEditable(false);
		textArea.setMargin(new Insets(5, 10, 5, 10));		
       
		StyleSheet styleSheet = new StyleSheet();
        try
        {            
            Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(CSS));
            styleSheet.loadRules(reader, ClassLoader.getSystemResource(CSS));
            reader.close();
        }
        catch(IOException ie)
        {
            ie.printStackTrace();
        }
        
        MyHTMLEditorKit kit = new MyHTMLEditorKit();
        styleSheet.addStyleSheet(kit.getStyleSheet());
        kit.setStyleSheet(styleSheet);
        //kit.getStyleSheet().addStyleSheet(styleSheet);         
        textArea.setEditorKit(kit);       
        
		copy = new CopyAction();
		copy.setEnabled(false);        
		selectAll = new SelectAllAction();
        
		textArea.addCaretListener(new CaretListener()
		{
			public void caretUpdate(CaretEvent e)
			{
				int len = textArea.getText().length();
				boolean enabled = 
					textArea.getSelectionStart() != textArea.getSelectionEnd()
					 && len != 0;                 
				copy.setEnabled(enabled);				
			} 	
		});
		
		textArea.addHyperlinkListener(new HyperlinkListener()
		{
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				try
				{				
					if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
						Browser.displayURL(e.getURL().toString());
				}
				catch(Exception ex){}
			}
		});    
		
		icon = new JLabel();
		icon.setHorizontalAlignment(JLabel.RIGHT);
                
		JScrollPane scroller = new JScrollPane(textArea,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        labelPanel = new JPanel();
        headerPanel = new JPanel(new BorderLayout());        
		headerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		headerPanel.add(labelPanel, BorderLayout.CENTER);		
		headerPanel.add(icon, BorderLayout.EAST);
		
		setLayout(new BorderLayout());
		add(headerPanel, BorderLayout.NORTH);
		add(scroller, BorderLayout.CENTER);

	}
    
	public void setModel(ViewerPaneModel m)
	{
	    if(model != null)
	        model.removeChangeListener(updateHandler);
	    model = m;
	    model.addChangeListener(updateHandler);
	    refresh();
	}
	
	public void setText(String text)
	{
		textArea.setText(text);
		textArea.setCaretPosition(0);
	}
	
	public JEditorPane getJEditorPane()
	{
		return textArea;
	}
    
	public Action getCopyAction()
	{
		return copy;
	}
    
	public Action getSelectAllAction()
	{
		return selectAll;	
	}
	
	private void refresh()
	{
	    int rows = model.getHeaderCount();	    
	    if(rows != headerData.size())
	    {
	        headerData.removeAllElements();
	        headerTitles.removeAllElements();
			labelPanel.removeAll();
			labelPanel.setLayout(new GridLayout(rows, 1, LABEL_GAP, LABEL_GAP));
			for(int i = 0; i < rows; i++)
			{			    
			    JLabel title = new JLabel(model.getHeaderTitle(i));
			    title.setFont(titleFont);
			    headerTitles.add(title);
			    
			    JLabel descr = new JLabel(model.getHeaderDescription(i));
			    descr.setFont(descrFont);
			    headerData.add(descr);
			    
			    labelPanel.add(createField(title, descr));
			}
			labelPanel.revalidate();
	    }
	    else
	    {
	        for(int i = 0; i < rows; i++)
	        {
	            JLabel l = (JLabel)headerTitles.elementAt(i);
	            l.setText(model.getHeaderTitle(i));
	            l = (JLabel)headerData.elementAt(i);
	            l.setText(model.getHeaderDescription(i));
	        }
	    }
	    		
		Icon ic = model.getIcon();
	    if(ic != null)
			icon.setPreferredSize(
				new Dimension(ic.getIconWidth(), labelPanel.getHeight()));
		else
			icon.setPreferredSize(null);
	    
	    icon.setIcon(ic);
	    setText(model.getText());
	}
	
	private Box createField(JLabel h, JLabel d)
	{
		Box b = Box.createHorizontalBox();
		b.add(h);
		b.add(Box.createHorizontalStrut(5));
		b.add(d);
        
		return b;
	}
	
	private void sizeLabelPanel()
	{
	    int w = 0, h = 0;

	    for(int i = 0; i < headerData.size(); i++)
	    {
	        JLabel lb = (JLabel)headerData.get(i);
	        lb.setMaximumSize(lb.getPreferredSize());
	        lb.setMinimumSize(lb.getPreferredSize());
	        if(lb.getWidth() >= w)
	            w = lb.getPreferredSize().width;
	        h += lb.getPreferredSize().height + LABEL_GAP;
	    }
	    Dimension size = new Dimension(w, h);
	    labelPanel.setPreferredSize(size);
	    labelPanel.setMaximumSize(size);
	    labelPanel.setMinimumSize(size);
	}
	
	private class UpdateHandler implements ChangeListener
	{
	    public void stateChanged(ChangeEvent e)
	    {
	        refresh();
	    }
	}
    
	private class CopyAction extends AbstractAction 
	{
		public CopyAction()
		{
			super(Messages.getString("ViewerPane.Copy")); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK));
			putValue(SMALL_ICON, Utils.createIcon(TBGlobals.RESOURCES + "copy16.gif"));
			Messages.setMnemonic("ViewerPane.Copy", this);
			putValue(SHORT_DESCRIPTION, getValue(NAME));
		}

		public void actionPerformed(ActionEvent e)
		{
			textArea.requestFocus();
			textArea.copy();
		}
	}
    
	private class SelectAllAction extends AbstractAction 
	{
		public SelectAllAction()
		{
			super(Messages.getString("ViewerPane.Select_All")); //$NON-NLS-1$
			putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK));
			Messages.setMnemonic("ViewerPane.Select_All", this);
			putValue(SHORT_DESCRIPTION, getValue(NAME));
		}

		public void actionPerformed(ActionEvent e)
		{            
			textArea.requestFocus();
			textArea.selectAll();
		}
	}
	
	/*
     * This class extends HTMLEditor kit to override 
     * get/setStyleSheet because HTMLEditorkit uses a static StyleSheet
     * instance, thereby forcing all editorkits to use that static set of styles
     * 
     */
    private class MyHTMLEditorKit extends HTMLEditorKit
    {
        private StyleSheet styleSheet;
        
        public void setStyleSheet(StyleSheet ss)
        {
            styleSheet = ss;
        }
        
        public StyleSheet getStyleSheet() 
        {
        	if(styleSheet == null)
        	    return super.getStyleSheet();
        	return styleSheet;
        }        
    }
}
