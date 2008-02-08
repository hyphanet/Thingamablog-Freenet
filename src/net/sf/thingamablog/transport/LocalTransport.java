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
package net.sf.thingamablog.transport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transport for publishing weblogs to a local or network drive
 * 
 * @author Bob Tantlinger
 *
 */
public class LocalTransport implements PublishTransport
{
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.transport");
    private boolean isConnected;
    private String failure = "";

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#connect()
     */
    public boolean connect()
    {
        failure = "";
        isConnected = true;
        return true;
    }

    public boolean publishFile(String pubPath, File file, TransportProgress tp)
    {
		File dir = new File(pubPath);        
		if(file.isDirectory() || !isConnected || tp.isAborted())
		{
			failure = "Publish aborted";
		    return false;
		}
        
		if(!dir.exists() || !dir.isDirectory())
		{        
			boolean result = dir.mkdirs();
			if(!result)//couldn't access pubPath
			{
				failure = "Unable to create the path: " + dir.getAbsolutePath();
			    return false;
			}
		}
		
		try 
		{
			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(file).getChannel();
    
			
			File dest = new File(dir, file.getName());
			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(dest).getChannel();
    
			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    
			// Close the channels
			srcChannel.close();
			dstChannel.close();
			
			tp.bytesTransferred(file.length());
			
			return true;
		} 
		catch(IOException e) 
		{
		    failure = e.getMessage();
		    logger.log(Level.WARNING, failure, e);
		}
		
		return false;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#disconnect()
     */
    public boolean disconnect()
    {
        isConnected = false;
        return true;
    }

    public boolean isConnected()
    {
        return isConnected;
    }
    
    public String getFailureReason()
    {
        return failure;
    }
}
