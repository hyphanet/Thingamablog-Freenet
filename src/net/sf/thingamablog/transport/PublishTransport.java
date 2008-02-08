/*
 * Created on Apr 29, 2004
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
package net.sf.thingamablog.transport;
import java.io.File;


/**
 * Interface that describes a PublishTransport. A PublishTransport
 * publishes the files of a weblog.
 * 
 * @author Bob Tantlinger
 *
 */
public interface PublishTransport extends Transport
{
	
	
	/**
	 * Publishes a file
	 * 
	 * @param pubPath The path to publish the file to
	 * @param file The file to publish
	 * @param tp The transport progress
	 * @return true if the file was publish, false if the publish failed
	 */	
	public boolean publishFile(String pubPath, File file, TransportProgress tp);
	
	
}
