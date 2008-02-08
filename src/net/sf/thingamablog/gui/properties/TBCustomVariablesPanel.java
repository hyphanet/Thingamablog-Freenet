/*
 * Created on Jun 29, 2004
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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.generator.CustomTag;
import net.sf.thingamablog.gui.StandardDialog;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class TBCustomVariablesPanel extends PropertyPanel
{	
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    
    private TBWeblog weblog;
	private EditableList eList;
	    
    public TBCustomVariablesPanel(TBWeblog wb)
    {    	
    	weblog = wb;
		
		eList = new EditableList(new VarEditableListModel());
		eList.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		eList.setListData(weblog.getPageGenerator().getCustomTags());
		
		setLayout(new BorderLayout(5, 5));
		add(eList, BorderLayout.CENTER);
    }
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#savePropertiesToWeblog()
     */
    public void saveProperties()
    {       
		weblog.setPublishAll(true);
		java.util.Vector v = eList.getListData();
		CustomTag tags[] = new CustomTag[v.size()];
		for(int i = 0; i < tags.length; i++)
			tags[i] = (CustomTag)v.elementAt(i);
		
		weblog.getPageGenerator().setCustomTags(tags);		
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#isValidData()
     */
    public boolean isValidData()
    {       
        return true;
    }
    
    private class VarEditableListModel implements EditableListModel
    {
    	public Object add(EditableList c)
    	{
    		VariableEditor ed = createEditor();
			ed.setLocationRelativeTo(c);
    		ed.setVisible(true);
    		if(ed.hasUserCancelled())
    			return null;
    		
    		return ed.getVariable();
    	}
    	
		public boolean shouldRemove(Object o, EditableList c)
		{
			return true;
		}
		
		public Object edit(Object o, EditableList c)
		{
			VariableEditor ed = createEditor();
			CustomTag v = (CustomTag)o;
			ed.setVariable(v);
			ed.setLocationRelativeTo(c);
			ed.setVisible(true);
			if(ed.hasUserCancelled())
				return null;
			
			return ed.getVariable();
		}
		
		private VariableEditor createEditor()
		{
			Component c = getParent();
			while(c.getParent() != null)			
				c = c.getParent();			
			
			VariableEditor d;
			if(c instanceof Frame)
				d = new VariableEditor((Frame)c);
			else if(c instanceof Dialog)
				d = new VariableEditor((Dialog)c);
			else 
				d = new VariableEditor();
			
			return d;
		}
    }
    
    private class VariableEditor extends StandardDialog
    {
    	/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private JTextField varNameField = new JTextField();
    	private JTextArea textArea = new JTextArea();
    	private String title = i18n.str("variable_editor"); //$NON-NLS-1$
    	
    	public VariableEditor(Dialog d)
    	{    		
    		super(d, "");
    		init();
    	}
    	
		public VariableEditor(Frame f)
		{    		
			super(f, "");
			init();
		}
		
		public VariableEditor()
		{
			init();
		}
    	
    	private void init()
    	{    		    		
    		setTitle(title); 
    		
    		TextEditPopupManager popper = TextEditPopupManager.getInstance();
    		popper.registerJTextComponent(varNameField);
    		popper.registerJTextComponent(textArea);
    		    		
    		JPanel namePanel = new JPanel(new BorderLayout());
    		namePanel.add(new JLabel(i18n.str("name")), BorderLayout.NORTH); //$NON-NLS-1$
    		namePanel.add(varNameField, BorderLayout.CENTER);
    		
    		JPanel valuePanel = new JPanel(new BorderLayout());
    		valuePanel.add(new JLabel(i18n.str("value")), BorderLayout.NORTH); //$NON-NLS-1$
    		valuePanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
    		
    		JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
    		mainPanel.add(namePanel, BorderLayout.NORTH);
    		mainPanel.add(valuePanel, BorderLayout.CENTER);
    		
    		setContentPane(mainPanel);
    		setSize(360, 250);  		
    	}
    	
    	public void setVariable(CustomTag v)
    	{
    		varNameField.setText(v.getName());
    		textArea.setText(v.getValue());
    	}
    	
    	public void setVariable(String name, String value)
    	{
    		varNameField.setText(name);
    		textArea.setText(value);
    	}
    	
    	public CustomTag getVariable()
    	{    		
    		String name = varNameField.getText();
    		String value = textArea.getText();    		
    		return new CustomTag(name, value);
    	}
    	
    	public boolean isValidData()
    	{
    		String s = varNameField.getText();
    		if(s.length() == 0)
    		{
				JOptionPane.showMessageDialog(VariableEditor.this, 
					i18n.str("no_name_prompt"), //$NON-NLS-1$
					i18n.str("invalid_variable"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				return false;
    		}
    		
    		if(textArea.getText().length() == 0)
    		{
				JOptionPane.showMessageDialog(VariableEditor.this, 
					i18n.str("no_value_prompt"), //$NON-NLS-1$
					i18n.str("invalid_variable"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				return false;
    		}
    		
    		for(int i = 0; i < s.length(); i++)
    		{
    			char ch = s.charAt(i);
    			if(!Character.isJavaIdentifierPart(ch) || ch == '$')
    			{
    				JOptionPane.showMessageDialog(VariableEditor.this, 
    					i18n.str("invalid_variable_name_prompt"), //$NON-NLS-1$
    					i18n.str("invalid_variable"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
    				
    				return false;
    			}
    		}
    		
    		return true;
    	}
    }
}
