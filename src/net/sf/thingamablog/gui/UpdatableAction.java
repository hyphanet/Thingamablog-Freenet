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

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * An abstract action which can determine its enabled state
 * 
 * @author Bob Tantlinger
 *
 */
public abstract class UpdatableAction extends AbstractAction
{

  	public UpdatableAction()
  	{
  	}
  	
    /**
     * @param name
     */
    public UpdatableAction(String name)
    {
        super(name);
        
    }

    /**
     * @param name
     * @param icon
     */
    public UpdatableAction(String name, Icon icon)
    {
        super(name, icon);
        
    }
    
    abstract public void update();

}
