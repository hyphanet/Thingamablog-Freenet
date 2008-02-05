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
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;


/**
 * Transport for publishing weblogs via SFTP
 * 
 * @author Bob Tantlinger
 */
public class SFTPTransport extends RemoteTransport
{
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.transport");
    private ChannelSftp sftp;
    private String failMsg = "";

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#connect()
     */
    public boolean connect()
    {
        failMsg = "";
        if(isConnected)
		{
			failMsg = "Already connected";
		    return false;
		}

        try
        {
			JSch jsch = new JSch();
            Session session = jsch.getSession(getUserName(), getAddress(), getPort());

            // password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo(getPassword());
            session.setUserInfo(ui);

            logger.info("Connecting to SFTP");
            session.connect();
            logger.info("Logged in to SFTP");

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp)channel;
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
        if(sftp == null)
        {
            failMsg = "SFTP Client not initialized!";
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
            String cwd = sftp.pwd();
            if(!cwd.endsWith("/"))
                cwd += "/";
            if(!pubPath.equals(cwd))
            {            
                boolean changedDir = false;
                try
                {
                    sftp.cd(pubPath); //try to change to the pub path
                    changedDir = true; //changed dir OK
                    System.out.println("Changed to " + pubPath);
                }
                catch(Exception cdEx)
                {
                    logger.log(Level.WARNING, "Problem changing SFTP dir", cdEx);    
                }
                
                if(!changedDir)
                {
                    //was unable to change dir. the dir likely does not exist
                    //so we'll try making the dir structure of pubPath
                    mkdirs(pubPath);
                    //sftp.cd(pubPath);
                }
            }

            int mode = ChannelSftp.OVERWRITE;
            //String dest = pubPath + file.getName();
            InputStream is = new FileInputStream(file);
            sftp.put(is, file.getName(), new MyProgressMonitor(tp), mode);
            is.close();

            return true;
        }
        catch(Exception ex)
        {
			failMsg = "Error publishing file to " + pubPath;
			failMsg += "\n" + ex.getMessage();
			logger.log(Level.WARNING, failMsg, ex);
            ex.printStackTrace();
        }

        return false;
    }

    private void mkdirs(String path) throws Exception
    {
        //sftp.cd("/"); //change to the root dir      

        //String dirs[] = splitPath(path);
        
        String cwd = sftp.pwd();
        //System.out.println("CWD: " + cwd);      
        
        while(!path.startsWith(cwd))
        {
            System.out.println(cwd + " " + path);
            sftp.cd("..");//should throw exception if can't cdup
            System.out.println("CDUP!");
            cwd = sftp.pwd();
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
                sftp.mkdir(dirs[i]);
            }
            catch(Exception ex)
            {}

            //change to the new dir
            //throws an exception if something went wrong                
            sftp.cd(dirs[i]);
        }
    }

    public String getFailureReason()
    {
        return failMsg;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.PublishTransport#disconnect()
     */
    public boolean disconnect()
    {
        try
        {
            System.out.println("Disconnecting SFTP...");
            if (sftp != null)
                sftp.exit();
        }
        catch(Exception ioe)
        {
            logger.log(Level.WARNING, "Problem disconnecting from SFTP", ioe);
        }

        if (!isSavePassword())
            setPassword("");

        System.out.println("Disconnected SFTP");
        logger.info("Disconnected SFTP");
        isConnected = false;
        return true;
    }

    private class MyProgressMonitor implements SftpProgressMonitor
    {
        private TransportProgress progress;

        public MyProgressMonitor(TransportProgress tp)
        {
            progress = tp;
        }

        public void init(int op, String src, String dest, long max)
        {}

        public boolean count(long count)
        {
            progress.bytesTransferred(count);
            return !progress.isAborted();
        }

        public void end()
        {}
    }

    private class MyUserInfo implements UserInfo
    {
        String pw;

        public MyUserInfo(String p)
        {
            pw = p;
        }

        public String getPassword()
        {
            return pw;
        }

        public boolean promptYesNo(String str)
        {
            return true;
        }

        public String getPassphrase()
        {
            return null;
        }

        public boolean promptPassphrase(String message)
        {
            return true;
        }

        public boolean promptPassword(String message)
        {
            return true;
        }
        public void showMessage(String message)
        {}
    }

}
