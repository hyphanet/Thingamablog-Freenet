/*
 * Created on May 28, 2004
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

import java.io.File;

import net.sf.thingamablog.transport.TransportProgress;
/**
 * Interface to update the progress of a weblog publish
 * 
 * @author Bob Tantlinger
 */
public interface PublishProgress extends TransportProgress
{
	/**
	 * Invoked when a publish session is started
	 * @param totalBytesToPublish The total bytes to be published
	 */
	public void publishStarted(long totalBytesToPublish);
	
	/**
	 * Invoked before an individual file is published
	 * @param f The file to publish
	 * @param pubPath The path to publish the file to
	 */
	public void filePublishStarted(File f, String pubPath);
	
	/**
	 * Invoked after a file was published
	 * @param f The file that was published
	 * @param pubPath The path that the file was published to
	 */
	public void filePublishCompleted(File f, String pubPath);
	
	/**
	 * Invoked when a publish session fails
	 * @param reason The reason the publish failed
	 */
	public void publishFailed(String reason);
	
	/**
	 * Invoked when a publish session completes
	 *
	 */
	public void publishCompleted();
}
