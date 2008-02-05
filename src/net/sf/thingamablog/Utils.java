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

package net.sf.thingamablog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

public class Utils
{  	
  	public static void errMsg(Component owner, String msg, Exception e) 
  	{
    	if(msg != null) 
    	{
      		JOptionPane.showMessageDialog(
      			owner, msg, "Error", JOptionPane.ERROR_MESSAGE);
    	}
    	if(e != null) 
    	{
      		e.printStackTrace();
    	}
  	}
    
    public static ImageIcon createIcon(String path)
	{
		URL u = ClassLoader.getSystemResource(path);
		return new ImageIcon(u);
	}
	
	public static JMenuItem addMenuItem(JComponent menu, Action action)
	{
		JMenuItem item;
		if(menu instanceof JMenu)
		{
			JMenu aMenu = (JMenu)menu;
			item = aMenu.add(action);
		}
		else if(menu instanceof JPopupMenu)
		{
			JPopupMenu aMenu = (JPopupMenu)menu;
			item = aMenu.add(action);
		}
		else
			item = new JMenuItem(action);

		KeyStroke keystroke = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
		if (keystroke != null)
		   item.setAccelerator(keystroke);

		item.setIcon(null);
		item.setToolTipText(null);
		return item;
	}
	
	public static JButton addToolbarButton(JToolBar tb, Action a)
	{
		JButton button = addToolbarButton(tb, new JButton(a));
		button.setToolTipText(a.getValue(Action.NAME).toString());		
		return button;
	}
	
	public static JButton addToolbarButton(JToolBar tb, JButton button)
	{
		button.setText(null);
		button.setMnemonic(0);
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setFocusable(false);
		button.setFocusPainted(false);
		button.putClientProperty("hideActionText", Boolean.TRUE);
		tb.add(button);		
		return button;
	}
	
	public static String colorToHex(Color color) 
	{
		String colorstr = new String("#");

		// Red
		String str = Integer.toHexString(color.getRed());
		if (str.length() > 2)
			str = str.substring(0, 2);
		else if (str.length() < 2)
			colorstr += "0" + str;
		else
			colorstr += str;

		// Green
		str = Integer.toHexString(color.getGreen());
		if (str.length() > 2)
			str = str.substring(0, 2);
		else if (str.length() < 2)
			colorstr += "0" + str;
		else
			colorstr += str;

		// Blue
		str = Integer.toHexString(color.getBlue());
		if (str.length() > 2)
			str = str.substring(0, 2);
		else if (str.length() < 2)
			colorstr += "0" + str;
		else
			colorstr += str;

		return colorstr;
	}
	
	public static String assemblePath(String p1, String p2)
	{
		String sep = System.getProperty("file.separator");
		if(!p1.endsWith(sep))
			p1 += sep;
		
		return p1 + p2;
	}
	
	public static boolean copyFile(String in_name, String out_name)
	{
		BufferedInputStream in;
		BufferedOutputStream out;
  		if(in_name==null || out_name==null)
    		return false;
    		
  		//Open input file
  		try
    	{
    		in = new BufferedInputStream(new FileInputStream(in_name));
    	}
  		catch (Exception e)
    	{
    		System.out.println("Opening input file:"+e);
    		return false;
    	}
  		//Open output file
  		try
    	{
    		out = new BufferedOutputStream(new FileOutputStream(out_name));
    	}
  		catch (Exception e)
    	{
    		System.out.println("Opening output file:"+e);
    		return false;
    	}

  		//Copy the data
  		byte buf[] = new byte[2048];
  		int len;
  		while (true)
    	{
    		try
      		{
      			len = in.read(buf);
      			if (len<1)//done?
        			break;
      		}
    		catch (Exception e)
      		{
      			System.out.println("Read error:"+e);
      			break;
      		}
      		
    		try
      		{
      			out.write(buf,0,len);
      		}
    		catch (Exception e)
      		{
      			System.out.println("Write error:"+e);
      		}
    	}//while


  		//Close input file
  		try
    	{
    		in.close();
    	}
  		catch (Exception e)
    	{
    		System.out.println("Closing input file:"+e);
    	}
  		//#Close output file
  		try
    	{
    		out.close();
    	}
  		catch (Exception e)
    	{
    		System.out.println("Closing output file:"+e);
    	} 
    	
    	return true;
	}
	
	
	//be careful with this
	public static void deleteDir(File file)
	{
		if(file.isDirectory())
		{
			File contents[] = file.listFiles();
			for (int i = 0; i < contents.length; i++)
				 deleteDir(contents[i]);
		} 
		
		file.delete();
	}
	
	/**
	 * Gets the currently active frame
	 * @return The active frame, or null if no frame is active
	 */
	public static Frame getActiveFrame()
	{
	    Frame[] f = Frame.getFrames();
	    for(int i = 0; i < f.length; i++) 
	    {
	        if(f[i].isActive())
	            return f[i];	      
	    }
	    
	    return null;
	}
}