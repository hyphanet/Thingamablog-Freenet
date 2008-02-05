/*
 * Created on May 1, 2004
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
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;

/**
 * @author Bob Tantlinger
 *
 */
public interface ViewerPaneModel
{
	public void setModelData(Object o);
    public String getText();
	public ImageIcon getIcon();
	public int getHeaderCount();
	public String getHeaderTitle(int row);
	public String getHeaderDescription(int row);
	
	public void addChangeListener(ChangeListener l);
	public void removeChangeListener(ChangeListener l);
	
}
