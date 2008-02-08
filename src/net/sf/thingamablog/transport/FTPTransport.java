/*
 * Created on Aug 14, 2004
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
package net.sf.thingamablog.transport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageListener;
import com.enterprisedt.net.ftp.FTPProgressMonitor;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * Transport for publishing weblogs via FTP
 * 
 * @author Bob Tantlinger
 */
public class FTPTransport extends RemotePublishTransport
{
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.transport");
    
    private FTPClient ftp = null;
	private boolean isPassiveMode;
	private String failMsg = "";
    private MessageListener msgListener = new MessageListener();
    
    private List asciiTypes = new ArrayList();
	
    public void setASCIIExtentions(List l)
    {
        asciiTypes = l;
    }
    
    public List getASCIIExtensions()
    {
        return asciiTypes;
    }
    
    
    
    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#connect()
     */
    public boolean connect()
    {
		//msgListener.progress = prg;
        failMsg = "";
        if(isConnected)
		{
			failMsg = "Already connected";
		    return false;
		}
        
       	try
       	{
       		ftp = new FTPClient();
       		ftp.setMessageListener(msgListener);            
       		ftp.setRemotePort(getPort());//ftp.setControlPort(getPort());
       		ftp.setRemoteHost(getAddress());      		
			if(isPassiveMode)
				ftp.setConnectMode(FTPConnectMode.PASV);
			else
				ftp.setConnectMode(FTPConnectMode.ACTIVE);		

			logger.info("Connecting to FTP");
			ftp.connect();
			ftp.login(getUserName(), getPassword());
			logger.info("Logged in to FTP");
			
			isConnected = true;
			return true;
       	}
       	catch(Exception ex)
       	{
       		failMsg = "Error logging in to " + getAddress();
       		failMsg += "\n" + ex.getMessage();
       		logger.log(Level.WARNING, failMsg, ex);
       	    ex.printStackTrace();
       	}
       	
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#publishFile(java.lang.String, java.io.File, net.sf.thingamablog.transport.TransportProgress)
     */
    public boolean publishFile(String pubPath, File file, TransportProgress tp)
    {
        msgListener.progress = tp;
        
        if(ftp == null)
        {
            failMsg = "FTP Client not initialized!";
            return false;
        }
        
        if(!isConnected())
        {
            failMsg = "Not Connected!!!";
            return false;
        }
        
        if(tp.isAborted())
        {
            failMsg = "Aborted";
            return false;
        }
        
       
		if(!pubPath.endsWith("/"))
			pubPath += "/"; //append a trailing slash if needed 
        
		try
		{
		    String cwd = ftp.pwd();
		    if(!cwd.endsWith("/"))
		        cwd += "/";
		    if(!pubPath.equals(cwd))
		    {
		        boolean changedDir = false;
		        try
		        {
		            ftp.chdir(pubPath); //try to change to the pub path
		            changedDir = true; //changed dir OK
		            System.out.println("Changed to " + pubPath);
		            logger.info("Changed to " + pubPath);
		        }
		        catch(Exception cdEx)
		        {
		            logger.log(Level.WARNING, "Problem changing FTP dir", cdEx);
		        }
		        
		        if(!changedDir)
		        {
		            //was unable to change dir. The dir likely does not exist
		            //so we'll try making the dir structure of pubPath
		            mkdirs(pubPath);
		            //ftp.chdir(pubPath);
		        }
		    }
			
			//set up the transfer properties
			if(isASCII(file))
				ftp.setType(FTPTransferType.ASCII);
			else
				ftp.setType(FTPTransferType.BINARY);			
			
			ftp.setProgressMonitor(new MyProgressMonitor(tp));
						
			//ftp.put(new FileInputStream(file), dest);
			ftp.put(new FileInputStream(file), file.getName());	
			
			//finished = true;
			return true;
		}
		catch(Exception ex)
		{
			failMsg = "Error publishing file to " + pubPath;
			failMsg += "\n" + ex.getMessage();
		    ex.printStackTrace();
		    logger.log(Level.WARNING, failMsg, ex);
		}
		
		//finished = true;
		return false;
    }   
    
    
	private void mkdirs(String path) throws IOException, FTPException
	{		
	    String cwd = ftp.pwd();
	    	    
	    while(!path.startsWith(cwd))
	    {
	        System.out.println(cwd + " " + path);
	        ftp.cdup();//should throw exception if can't cdup
	        System.out.println("CDUP!");
	        cwd = ftp.pwd();
	    }
	    
        String mkPath = path.substring(cwd.length(), path.length());
        System.out.println("DIRS TO MAKE: " + mkPath);

        String dirs[] = splitPath(mkPath);
		for(int i = 0; i < dirs.length; i++)
		{
			System.out.println("mkdir " + dirs[i]);
			logger.info("mkdir " + dirs[i]);

			//swallow exception that results from trying to
			//make a dir that already exists
			try
			{
				ftp.mkdir(dirs[i]);
			}
			catch(Exception ex)
			{}

			//change to the new dir
			//throws an exception if something went wrong                
			ftp.chdir(dirs[i]);
		}
	}


    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#disconnect()
     */
    public boolean disconnect()
    {		
        msgListener.progress = null;
        try
		{			
			if(isConnected)
				ftp.quit();
		}
		catch(Exception ex)
		{        	
			ex.printStackTrace();
			logger.log(Level.WARNING, "Problem disconnecting from FTP", ex);
			return false;
		}
		finally
		{
			System.out.println("Disconnected");
			logger.info("Disconnected FTP");
			isConnected = false;
			if(!isSavePassword())
				setPassword("");
		}
		return true;        
    }
    
    public String getFailureReason()
    {
        return failMsg;
    }
    
	/**
	 * Indicates whether the transport uses passive mode
	 * 
	 * @return true if passive mode is enabled, false otherwise
	 */
	public boolean isPassiveMode()
	{
		return isPassiveMode;
	}

	/**
	 * Sets the transport to use passive mode or active mode
	 * 
	 * @param b true for passive mode, false for active mode
	 */
	public void setPassiveMode(boolean b)
	{
		isPassiveMode = b;
	}
    
    private boolean isASCII(File f)
    {
        String name = f.getName().toLowerCase();
        for(Iterator it = asciiTypes.iterator(); it.hasNext();)
        {
            String ext = it.next().toString().toLowerCase();
            if(name.endsWith(ext))
                return true;
        }
        
        return false;
    }
	
	private class MyProgressMonitor implements FTPProgressMonitor
	{
		private TransportProgress progress;		
		private long total;
		
		public MyProgressMonitor(TransportProgress tp)
		{
			progress = tp;			
		}
		
		public void bytesTransferred(long count)
		{			
			progress.bytesTransferred(count - total);
			total = count;
			
			//check if the user cancelled the upload
			if(progress.isAborted())
				ftp.cancelTransfer();
		}
	}
	
	private class MessageListener implements FTPMessageListener
	{
	    TransportProgress progress;
        
        
        
        public void logCommand(String cmd)
	    {
	        logger.finer("CMD  : " + cmd);
            if(progress != null)
                progress.logMessage("CMD  : " + cmd);
	    }
	    
	    public void logReply(String rpl)
	    {
	        logger.finer("REPLY: " + rpl);
            if(progress != null)
                progress.logMessage("REPLY: " + rpl);
	    }
	}
}
