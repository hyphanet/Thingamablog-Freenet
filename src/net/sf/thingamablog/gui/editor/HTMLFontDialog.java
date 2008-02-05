/*
 * Copyright (C) 2003  Bob Tantlinger
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.StandardDialog;

public class HTMLFontDialog extends StandardDialog
{
	private JComboBox namesBox;
	private JComboBox sizeBox;
	private JButton colorButton;
	private JTextField colorField;
	private String text = ""; //$NON-NLS-1$
	
	private final String fontNames[] =
	{
		"Arial,Helvetica,sans-serif", //$NON-NLS-1$
		"Verdana,Arial,Helvetica,sans-serif", //$NON-NLS-1$
		"'MS Sans Serif',Geneva,sans-serif", //$NON-NLS-1$
		"System,Chicago,sans-serif", //$NON-NLS-1$
		"'Times New Roman',Times,serif", //$NON-NLS-1$
		"'MS Serif','New York',serif", //$NON-NLS-1$
		"'Courier New',Courier,monospace", //$NON-NLS-1$
		"Terminal,Monaco,monospace", //$NON-NLS-1$
		"Wingdings,'Zapf Dingbats'"			 //$NON-NLS-1$
	};
	
	
	public HTMLFontDialog()
	{
		super();		
		init();
	}
	
	public HTMLFontDialog(Frame parent)
	{
		super(parent, ""); //$NON-NLS-1$
		init();
		setLocationRelativeTo(parent);
	}
	
	public HTMLFontDialog(Dialog parent)
	{
		super(parent, ""); //$NON-NLS-1$
		init();
		setLocationRelativeTo(parent);
	}
	
	public void init()
	{
		setTitle(Messages.getString("HTMLFontDialog.Font")); //$NON-NLS-1$
		String fontSizes[] = new String[8];
		fontSizes[0] = ""; //$NON-NLS-1$
		for(int i = 1; i < fontSizes.length; i++)
			fontSizes[i] = i + ""; //$NON-NLS-1$
		
		namesBox = new JComboBox(fontNames);
		namesBox.setEditable(true);
		sizeBox = new JComboBox(fontSizes);
		sizeBox.setEditable(true);
		colorField = new JTextField(8);
		colorButton = new JButton("..."); //$NON-NLS-1$
		colorButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Color color = 
				JColorChooser.showDialog(HTMLFontDialog.this, Messages.getString("HTMLFontDialog.Color"), Color.black);	 //$NON-NLS-1$
			
				if(color != null)
					colorField.setText(Utils.colorToHex(color));	
			}	
		});
		
		LabelledItemPanel lip = new LabelledItemPanel();
		lip.addItem(Messages.getString("HTMLFontDialog.Font"), namesBox); //$NON-NLS-1$
		JPanel p = new JPanel(new BorderLayout());
		p.add(colorField, BorderLayout.CENTER);
		p.add(colorButton, BorderLayout.EAST);
		lip.addItem(Messages.getString("HTMLFontDialog.Color"), p); //$NON-NLS-1$
		p = new JPanel(new BorderLayout());
		p.add(sizeBox, BorderLayout.WEST);
		p.add(new JPanel(), BorderLayout.CENTER);//ehh
		lip.addItem(Messages.getString("HTMLFontDialog.Size"), p); //$NON-NLS-1$
		
		setContentPane(lip);
		pack();
		setResizable(false);				
	}
	
	public void setText(String t)
	{
		if(t == null)
			text = ""; //$NON-NLS-1$
		else
			text = t;	
	}
	
	public String getFontHTML()
	{
		String font = "<font"; //$NON-NLS-1$
		String fn = namesBox.getSelectedItem().toString();
		String sz = sizeBox.getSelectedItem().toString();
		String hex = colorField.getText();
		
		if(fn != null && !fn.equals("")) //$NON-NLS-1$
			font += " face=\"" + fn + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(sz != null && !sz.equals("")) //$NON-NLS-1$
			font += " size=\"" + sz + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(!hex.equals("")) //$NON-NLS-1$
			font += " color=\"" + hex + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		
		font += ">" + text + "</font>"; //$NON-NLS-1$ //$NON-NLS-2$
		
		return font;			
	}
}