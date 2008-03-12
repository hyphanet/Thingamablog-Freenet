/*
 * Created on Jun 20, 2004
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
package net.sf.thingamablog;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.UIManager;

import net.sf.thingamablog.blog.DiskTemplatePack;
import net.sf.thingamablog.blog.ZipTemplatePack;

import com.jgoodies.plaf.Options;

/**
 * Static fields and methods for setting/getting Thingamablog's 
 * various application-wide settings and properties
 * 
 * @author Bob Tantlinger
 *
 */
public class TBGlobals
{
	/** The name of the application */
	public static final String APP_NAME = "@APP_NAME@";
	/** The version of the application */
	public static final String VERSION = "@VERSION@";
	/** The build of the application */
	public static final String BUILD = "@BUILD@";
	/** The home page of the application */
	public static final String APP_URL = "@APP_URL@";
    
	/** Platform specific path separator */
	public static final String SEP = System.getProperty("file.separator");
	/** User's home directory */
	public static final String USER_HOME = System.getProperty("user.home");
	/** Directory where app properties are stored */
	public static final String PROP_DIR = USER_HOME + SEP + ".thinga";
	/** The app's property file */
	public static final String PROP_FILE = PROP_DIR + SEP + "thingamablog.properties";
	/** The application's install dir */
	public static final String USER_DIR = System.getProperty("user.dir");
	/** location of default templates */
	public static final String DEFAULT_TMPL_DIR = USER_DIR + SEP + "template_sets";	
	/** location of spell checker dictionary files */
	public static final String DICT_DIR = USER_DIR + SEP + "dictionaries";
		
	/** File name for the xml file that describes a 'database' */
	public static final String USER_XML_FILENAME = "user.xml";	
	
	/** File extensions for common text files */
	public static final String TEXT_FILE_EXTS[] =
	{
		".html", ".htm", ".shtml", ".xml", ".opml", ".rss", ".rdf", ".css", 
		".php", ".pl", ".txt", ".py", ".cgi", ".js", ".java", ".c", ".cpp" 	
	};
	
	/** location of the feed image cache */
	public static final String IMG_CACHE_DIR = PROP_DIR + SEP + "img_cache";
	
	/** 3 column layout constant */
	public static final int THREE_COL = -5;
	/** 2 column layout constant */
	public static final int TWO_COL = -6;
	
	private static int layoutStyle = TWO_COL;
	
	private static String lastOpenedDatabase = null;
	private static boolean isStartWithLastDatabase = true;	
	private static boolean startWithSplash = true;
	
	private static Font editorFont = new Font("Monospaced", Font.PLAIN, 12); //$NON-NLS-1$
	//private static File dictFile = new File(DICT_DIR + SEP + "english.dico"); //$NON-NLS-1$ 
	
	private static String dictionary = "en_US";
	
	private static Properties props = new Properties();
	
	//proxy stuff
	private static boolean useSocksProxy;
	private static boolean isSocksAuth;
	private static String socksHost = "";
	private static String socksPort = "1080";
	private static String socksUser = "";
	private static String socksPass = "";
        
        //node properties
        private static String nodePort = "9481";
        private static String nodeHostname = "localhost";
        private static String fproxyPort = "8888";
	
	//auto feed updater stuff
	private static int feedUpdateInterval = 1800000;//30 minutes
	private static boolean isAutoFeedUpdate;
	
	private static boolean isPingAfterPub = true;
	
	//default is the plastic system look and feel
	private static String lafName = Options.getCrossPlatformLookAndFeelClassName();	
	static
	{	    
		UIManager.installLookAndFeel("Plastic", lafName);	
	    
	    //use system laf for default on Win and Mac
		if(System.getProperty("os.name").toLowerCase().startsWith("windows") ||
		   System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
	        lafName = UIManager.getSystemLookAndFeelClassName();
	}
	
	/**
	 * Indicate whether a file is a known text file
	 * @param f a file
	 * @return true if the file is a text file, false otherwise
	 */
	public static boolean isTextFile(File f)
	{
		String n = f.getName().toLowerCase();
		for(int i = 0; i < TEXT_FILE_EXTS.length; i++)
		{
			if(n.endsWith(TEXT_FILE_EXTS[i]))
				return true;	
		}
		
		return false;
	}
	
	/**
	 * Indicate whether a file is a known image file
	 * @param f a file
	 * @return true if the file is an image file, false otherwise
	 */
	public static boolean isImageFile(File f)
	{
		String n = f.getName().toLowerCase();
		return (n.endsWith(".gif") || n.endsWith(".jpg") ||
				n.endsWith(".jpeg") || n.endsWith(".png"));	
	}
	
	/**
	 * Get a Thingamablog property
	 * @param key property key
	 * @return the property
	 */
	public static String getProperty(String key)
	{
		return props.getProperty(key);
	}
	
	/**
	 * Set a Thingamablog property
	 * @param key property key
	 * @param val the property
	 */
	public static void putProperty(String key, Object val)
	{
		props.put(key, val);
	}
	
	/**
	 * Initialize the proxy. This should only be called once
	 * during the application's startup
	 */
	private static void initProxy()
	{
		if(useSocksProxy)
		{		
			System.getProperties().put("socksProxyHost", socksHost);
			System.getProperties().put("socksProxyPort", socksPort);
			
			if(isSocksAuth)
			{
				Authenticator auth = new Authenticator()
				{
					protected PasswordAuthentication getPasswordAuthentication() 
					{
						return new PasswordAuthentication(socksUser, socksPass.toCharArray()); 	
					}
				};		
				Authenticator.setDefault(auth);
			}
		}	
	}
	
	/**
	 * Load the properties from the PROP_FILE
	 */
	public static void loadProperties()
	{		
		try
		{			
		    FileInputStream fis = new FileInputStream(PROP_FILE);
			props.load(fis);
			//Browser.load(props);
			fis.close();
			
			if(props.get("SOCKS_PROXY_HOST") != null)
				socksHost = props.getProperty("SOCKS_PROXY_HOST");
			if(props.get("SOCKS_PROXY_PORT") != null)	
				socksPort = props.getProperty("SOCKS_PROXY_PORT");
			if(props.get("SOCKS_PROXY_USER") != null)
				socksUser = props.getProperty("SOCKS_PROXY_USER");
			if(props.get("SOCKS_PROXY_PASSWORD") != null)
			{
			    socksPass = props.getProperty("SOCKS_PROXY_PASSWORD");
			    socksPass = PasswordUtil.decrypt(socksPass, PasswordUtil.KEY);
			}
			//vb1 didn't encrypt the password, so we have to load it
			//in case the user has upgraded to the current version			
			if(props.get("SOCKS_PROXY_PASS") != null)
			{
				socksPass = props.getProperty("SOCKS_PROXY_PASS");
				props.remove("SOCKS_PROXY_PASS");
			}
			
			String use = props.getProperty("USE_SOCKS_PROXY");			
			useSocksProxy = use != null && use.equals("true");
			String auth = props.getProperty("SOCKS_PROXY_LOGIN");
			isSocksAuth = auth != null && auth.equals("true");
			String update = props.getProperty("AUTO_UPDATE");
			isAutoFeedUpdate = update != null && update.equals("true");
			String pap = props.getProperty("PING_AFTER_PUB");
			//for user that are upgrading
			if(pap == null)
			    isPingAfterPub = true;
			else
			    isPingAfterPub = pap.equals("true");
			
			initProxy();
			
			try
			{
				layoutStyle = Integer.parseInt(props.getProperty("LAYOUT"));
				String name = props.getProperty("EDITOR_FONT_NAME");
				int size = Integer.parseInt(props.getProperty("EDITOR_FONT_SIZE"));
				setEditorFont(new Font(name, Font.PLAIN, size));
				setFeedUpdateInterval(Integer.parseInt(props.getProperty("FEED_UPDATE_INTERVAL")));
			}
			catch(Exception ex){}
			
			lastOpenedDatabase = props.getProperty("LAST_DB");
			if(props.get("START_LAST_DB") != null)
				isStartWithLastDatabase = props.getProperty("START_LAST_DB").equals("true");	
			//if(props.get("DICT_FILE") != null)
			//	dictFile = new File(props.getProperty("DICT_FILE"));
			if(props.get("DICT") != null)
				dictionary = props.getProperty("DICT");
			if(props.get("SPLASH_SCREEN") != null)
				startWithSplash = props.getProperty("SPLASH_SCREEN").equals("true");
			if(props.get("LAF") != null)
				lafName = props.getProperty("LAF");
						
                        if(props.get("NODE_PORT") != null)
                            nodePort = props.getProperty("NODE_PORT");
                        if(props.get("NODE_HOSTNAME") != null)
                            nodeHostname = props.getProperty("NODE_HOSTNAME");
                        if(props.get("FRPOXY_PORT") != null)
                            fproxyPort = props.getProperty("FPROXY_PORT");
		}
		catch(FileNotFoundException fnfe)
		{
			//Browser.init();//use defaults for browser	
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}	
	}
	
	/**
	 * Save the application properties to the PROP_FILE
	 */
	public static void saveProperties()
	{
		try
		{
			File f = new File(PROP_DIR);
			f.mkdir();
			FileOutputStream fos = new FileOutputStream(PROP_FILE);
			props.put("USE_SOCKS_PROXY", useSocksProxy + "");
			props.put("SOCKS_PROXY_HOST", socksHost);
			props.put("SOCKS_PROXY_PORT", socksPort);
			props.put("SOCKS_PROXY_LOGIN", isSocksAuth + "");
			props.put("SOCKS_PROXY_USER", socksUser);
			props.put("SOCKS_PROXY_PASSWORD", 
			        PasswordUtil.encrypt(socksPass, PasswordUtil.KEY));
			
			props.put("LAYOUT", layoutStyle + "");
			props.put("LAF", lafName);
			
			props.put("EDITOR_FONT_NAME", editorFont.getFamily());
			props.put("EDITOR_FONT_SIZE", editorFont.getSize() + "");
			//props.put("DICT_FILE", dictFile.getAbsolutePath());
			props.put("DICT", dictionary);
			
			props.put("SPLASH_SCREEN", startWithSplash + "");
			
			if(lastOpenedDatabase != null)
			{			
				props.put("LAST_DB", lastOpenedDatabase);
				props.put("START_LAST_DB", isStartWithLastDatabase + "");
			}
			
			props.put("FEED_UPDATE_INTERVAL", feedUpdateInterval + "");
			props.put("AUTO_UPDATE", isAutoFeedUpdate + "");
			props.put("PING_AFTER_PUB", isPingAfterPub + "");
                        props.put("NODE_PORT", nodePort);
                        props.put("NODE_HOSTNAME", nodeHostname);
                        props.put("FPROXY_PORT",fproxyPort);
			
			//Browser.save(props);
			props.store(fos, "Thingamablog Properties");			
			fos.close();
						
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * Sets the layout style property
	 * @param s The layout style. Either TWO_COL or THREE_COL
	 */
	public static void setLayoutStyle(int s)
	{
		if(s == TWO_COL)
			layoutStyle = s;
		else
			layoutStyle = THREE_COL;	
	}
	
	/**
	 * Gets the layout style property
	 * @return The layout style constant - TWO_COL or THREE_COL
	 */
	public static int getLayoutStyle()
	{
		return layoutStyle;	
	}
	
	/**
	 * Sets the font used by editors
	 * @param f the editor font
	 */
	public static void setEditorFont(Font f)
	{
		editorFont = f;	
	}
	
	/**
	 * Gets the font used by editors
	 * @return a Font
	 */
	public static Font getEditorFont()
	{
		return editorFont;
	}
	
	/**
	 * Sets the dictionary used by editors
	 * @param f a dictionary file
	 */
	public static void setDictionary(String d)
	{
	    dictionary = d;
	}
	
	/**
	 * Gets the dictionary used by editors
	 * @return
	 */
	public static String getDictionary()
	{
	    return dictionary;
	}
	
	/*
	public static void setDictionaryFile(File f)
	{
		dictFile = f;
	}
	

	public static File getDictionaryFile()
	{
		return dictFile;
	}
	*/
	
	//proxy settings

	/**
	 * set whether or not to used the SOCKS proxy
	 */
	public static void setUseSocksProxy(boolean b)
	{
		useSocksProxy = b;	
	}
	
	/**
	 * Indicates whether or not the app uses the SOCKS proxy
	 * @return true if the proxy is enabled, false otherwise
	 */
	public static boolean isUseSocksProxy()
	{
		return useSocksProxy;
	}
	
	/**
	 * Sets the host of the proxy
	 * @param host
	 */
	public static void setSocksProxyHost(String host)
	{
		socksHost = host;	
	}
	
	/**
	 * Gets the host of the proxy
	 * @return
	 */
	public static String getSocksProxyHost()
	{
		return socksHost;
	}
	
	/**
	 * Sets the port of the proxy
	 * @param port
	 */
	public static void setSocksProxyPort(String port)
	{
		if(port == null || port.equals(""))
			socksPort = "1080";
		else
			socksPort = port;	
	}
	
	/**
	 * Gets the port of the proxy
	 * @return
	 */
	public static String getSocksProxyPort()
	{
		return socksPort;	
	}
	
	/**
	 * Indicates whether or not to log-in to the proxy
	 * @param b
	 */
	public static void setSocksProxyRequiresLogin(boolean b)
	{
		isSocksAuth = b;	
	}
	
	/**
	 * Indicates whether or not the proxy requires a login
	 * @return
	 */
	public static boolean isSocksProxyRequiresLogin()
	{
		return isSocksAuth;
	}
	
	/**
	 * Sets the user name for the proxy
	 * @param user
	 */
	public static void setSocksProxyUser(String user)
	{
		socksUser = user;
	}
	
	/**
	 * Gets the user name for the proxy
	 * @return
	 */
	public static String getSocksProxyUser()
	{
		return socksUser;
	}
	
	/**
	 * Sets the password for the proxy
	 * @param pw
	 */
	public static void setSocksProxyPassword(String pw)
	{
		socksPass = pw;
	}
	
	/**
	 * Gets the password for the proxy
	 * @return
	 */
	public static String getSocksProxyPassword()
	{
		return socksPass;
	}
				
        /**
         * Sets the port of the node
         * @param port
         */
        public static void setNodePort(String port)
        {
            if(port == null || port.equals(""))
			nodePort = "9481";
		else
			nodePort = port;
        }
        
        /**
         * Gets the port of the node
         * @return
         */
        public static String getNodePort() {
            return nodePort;
        }
        
        /**
         * Sets the port of fproxy
         * @param port
         */
        public static void setFProxyPort(String port)
        {
            if(port == null || port.equals(""))
                fproxyPort = "8888";
            else
                fproxyPort = port;
        }
        
        /**
         * Gets the port of fproxy
         * @return
         */
        public static String getFProxyPort()
        {
            return fproxyPort;
        }
        
        /**
         * Sets the hostname of the machine the node is running on
         * @param hostname
         */
        public static void setNodeHostname(String hostname){
            nodeHostname = hostname;
        }
        
        /**
         * Gets the hostname of the machine the node is running on
         * @return
         */
        public static String getNodeHostname(){
            return nodeHostname;
        }
        
    /**
     * Indicates whether a splash screen is displayed at startup
     * @return
     */
    public static boolean isStartWithSplash()
    {
        return startWithSplash;
    }

    /**
     * @param b
     */
    public static void setStartWithSplash(boolean b)
    {
        startWithSplash = b;
    }
    
    /**
     * Get the look and feel class name of the app
     * @return
     */
    public static String getLookAndFeelClassName()
    {
    	return lafName;
    }
    
    /**
     * Sets the class name of the look and feel that should be used
     * @param name
     */
    public static void setLookAndFeelClassName(String name)
    {
    	lafName = name;
    }

    /**
     * Gets the path to the previously opened USER_XML file
     * @return
     */
    public static String getLastOpenedDatabase()
    {
        return lastOpenedDatabase;
    }

    /**
     * @param string
     */
    public static void setLastOpenedDatabase(String string)
    {
        lastOpenedDatabase = string;
    }

    /**
     * @return
     */
    public static boolean isStartWithLastDatabase()
    {
        return isStartWithLastDatabase;
    }

    /**
     * @param b
     */
    public static void setStartWithLastDatabase(boolean b)
    {
        isStartWithLastDatabase = b;
    }
    /**
     * @return Returns the feedUpdateInterval.
     */
    public static int getFeedUpdateInterval()
    {
        return feedUpdateInterval;
    }
    /**
     * @param feedUpdateInterval The feedUpdateInterval to set.
     */
    public static void setFeedUpdateInterval(int ms)
    {
        feedUpdateInterval = ms;
    }
    /**
     * @return Returns the isAutoFeedUpdate.
     */
    public static boolean isAutoFeedUpdate()
    {
        return isAutoFeedUpdate;
    }
    /**
     * @param isAutoFeedUpdate The isAutoFeedUpdate to set.
     */
    public static void setAutoFeedUpdate(boolean b)
    {
        isAutoFeedUpdate = b;
    }
    
    public static boolean isPingAfterPublish()
    {
        return isPingAfterPub;
    }
    
    public static void setPingAfterPublish(boolean b)
    {
        isPingAfterPub = b;
    }
    
    
    
    public static File getPreviewDirectory()
    {
        return new File(PROP_DIR, "preview");
    }
    
    public static File getUserInstalledTemplatesDirectory()
    {
        return new File(PROP_DIR, "installed_templates");
    }    
        
    public static File getDefaultTemplateDirectory()
    {
        return new File(DEFAULT_TMPL_DIR);
    }
    
    public static List getAllAvailableTemplates()
    {
        List packs = getDefaultTemplates();
        packs.addAll(getUserInstalledTemplates());
        return packs;
    }
    
    public static List getDefaultTemplates()
    {
        File dir = getDefaultTemplateDirectory();       
        if(!dir.isDirectory())
        {
            dir.mkdirs();
            return new ArrayList();
        }
        
        return getTemplatePacks(dir.listFiles());
    }
    
    public static List getUserInstalledTemplates()
    {
        File dir = getUserInstalledTemplatesDirectory();       
        if(!dir.isDirectory())
        {
            dir.mkdirs();
            return new ArrayList();
        }
        
        return getTemplatePacks(dir.listFiles());        
    }
    
    private static List getTemplatePacks(File[] files)
    {
        List packs = new ArrayList();
        for(int i = 0; i < files.length; i++)
        {
            if(files[i].isDirectory())
            {
                try
                {
                    packs.add(new DiskTemplatePack(files[i]));
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }    
            }
            else if(files[i].isFile() && files[i].getName().toLowerCase().endsWith(".zip"))
            {
            	try
            	{
            		packs.add(new ZipTemplatePack(files[i]));
            	}
            	catch(Exception ex)
            	{
            		ex.printStackTrace();
            	}
            }
            
        }
        
        return packs;
    }
}
