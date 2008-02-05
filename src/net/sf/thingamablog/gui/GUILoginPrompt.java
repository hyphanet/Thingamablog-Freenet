/*
 * Created on Jul 9, 2004
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
 */
package net.sf.thingamablog.gui;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.thingamablog.transport.LoginPrompt;


/**
 * GUI implementation of a LoginPrompt
 * 
 * @author Bob Tantlinger
 *
 */
public class GUILoginPrompt extends StandardDialog implements LoginPrompt
{
	private JPasswordField pwField;
	private JTextField userField;
	private Component parent;
	
	public GUILoginPrompt(Frame p)
	{
		super(p, ""); //$NON-NLS-1$
		parent = p;
		init();
	}
	
	public GUILoginPrompt(Dialog p)
	{
		super(p, ""); //$NON-NLS-1$
		parent = p;
		init();
	}
	
	private void init()
	{
		pwField = new JPasswordField(25);
		userField = new JTextField(25);
		userField.setEditable(false);
		
		LabelledItemPanel lip = new LabelledItemPanel();
		lip.addItem(Messages.getString("GUILoginPrompt.User_Name"), userField); //$NON-NLS-1$
		lip.addItem(Messages.getString("GUILoginPrompt.Password"), pwField); //$NON-NLS-1$
		setContentPane(lip);
	}
	
	public void promptUser(String userName)
	{
		System.out.println(Messages.getString("GUILoginPrompt.Prompting_user")); //$NON-NLS-1$
		setTitle(Messages.getString("GUILoginPrompt.Login")); //$NON-NLS-1$
		userField.setText(userName);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
        pwField.requestFocusInWindow();
		setVisible(true);
	}
	
	public String getPassword()
	{
		String s = new String(pwField.getPassword());
		return s;
	}
	
	public boolean isLoginCancelled()
	{
		return hasUserCancelled();
	}
}
