/*
 * Created on Apr 8, 2004
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
package net.sf.thingamablog.blog;

import java.util.EventListener;

/**
 * The listener interface for recieving CategoryEvents
 * 
 * @author Bob Tantlinger 
 */
public interface CategoryListener extends EventListener
{
	/**
	 * Invoked when a category is added to a weblog
	 * @param e
	 */
	public void categoryAdded(CategoryEvent e);
	
	/**
	 * Invoked when a category is removed from a weblog
	 * @param e
	 */
	public void categoryRemoved(CategoryEvent e);
	
	/**
	 * Invoked when a category is renamed
	 * @param e
	 */
	public void categoryRenamed(CategoryEvent e);
}
