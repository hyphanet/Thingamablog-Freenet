/*
 * Copyright (C) 2004  Bob Tantlinger
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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.sf.thingamablog.gui.app.ExperiencedBlue;
import net.sf.thingamablog.gui.app.ThingamablogFrame;

import com.jgoodies.plaf.LookUtils;
import com.jgoodies.plaf.Options;
import com.jgoodies.plaf.plastic.PlasticLookAndFeel;
import thingamablog.l10n.i18n;

/** Application starter */


public class App
{
    /*
     * TODO
     *  -- more detail on HTML view when blog/entry is selected
     *  -- update user manual  
     *  -- feed url for PingService
     *  
     */
    final static Logger logger = Logger.getLogger("net.sf.thingamablog");
    final static String langPack = "lang.messages";
    
    public static void main(String args[])
    {        
        File libDir = new File(System.getProperty("user.dir"), "lib");
        String jarName = null;        
        if(EnvUtils.WINDOWS == EnvUtils.getPlatform())
            jarName = "jdic_stub_win.jar";
        else if(EnvUtils.LINUX == EnvUtils.getPlatform())
            jarName = "jdic_stub_lin.jar";
        else if(EnvUtils.MAC_OS == EnvUtils.getPlatform())
            jarName = "jdic_stub_mac.jar";
        else
            System.err.println("jdic is not supported here");
        
        if(jarName != null)
        {
            try
            {
                ClassPathHacker.addFile(new File(libDir, jarName));
            }
            catch(IOException e)
            {            
                System.err.println("Couldn't initialize jdic support");
                e.printStackTrace();
            }
        }
        
        TBGlobals.loadProperties();
        
        //set up the I18n resource bundles
        if(TBGlobals.getProperty("LANG_LOCALE") != null) {
        	i18n.setLocale(TBGlobals.getProperty("LANG_LOCALE"));
        } else {
            String defaultLoc = Locale.getDefault().getLanguage();
            String selected = "en";
            Locale[] available = i18n.getAvailableLanguagePackLocales();
            for (int i = 0; i < available.length; i++) {
                if (defaultLoc.equals(available[i].getLanguage()))
                    selected = defaultLoc;
            }
            i18n.setLocale(selected);
        }
        
        //OSX properties
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", TBGlobals.APP_NAME);
        
        //init logging
        try
        {
            File dir = new File(TBGlobals.PROP_DIR);
            if(!dir.exists())
                dir.mkdirs();
                        
            Handler fh = new FileHandler(TBGlobals.PROP_DIR + "/thinga%g.log", 50000, 5, true);
            fh.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(fh);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
                
        
        String osType = new String(System.getProperty("os.name"));
        String osVersion = new String(System.getProperty("os.version"));
        String osArch = new String(System.getProperty("os.arch"));
        String javaVersion = new String(System.getProperty("java.runtime.version"));
        String app = TBGlobals.APP_NAME + " " + TBGlobals.VERSION;

        System.out.println("Starting " + app + "...\n");
        System.out.println("Target OS       = " + osType);
        System.out.println("OS Version      = " + osVersion);
        System.out.println("OS Architecture = " + osArch);
        System.out.println("Java Version    = " + javaVersion);
        
        logger.info(app + "  OS: " + osType + "  JRE: " + javaVersion);
        
        Runnable r = new Runnable()
        {
            public void run()
            {
                long time = System.currentTimeMillis();
                createAndShowGUI();
                long initTime = System.currentTimeMillis() - time;        
                logger.info("Init time: " + initTime + "ms");
            }
        };
        SwingUtilities.invokeLater(r);
        
    }

    private static void createAndShowGUI()
    {
        setLookAndFeel(TBGlobals.getLookAndFeelClassName());       
        
        JFrame f = new ThingamablogFrame();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = ((d.width - f.getSize().width) / 2);
        int y = ((d.height - f.getSize().height) / 2);
        f.setLocation(x, y);
        f.setTitle(TBGlobals.APP_NAME + ' ' + TBGlobals.VERSION);
        f.setVisible(true);
    }
    
    private static void setLookAndFeel(String className)
    {
        try
        {   
            //Bug fix: Non-western chars can't display with the default
            //plastic theme, so a custom theme needs to be set here
            if(className.equals(Options.getCrossPlatformLookAndFeelClassName()))
            {                           
                if(LookUtils.IS_OS_WINDOWS_XP)
                {
                    PlasticLookAndFeel.setMyCurrentTheme(new ExperiencedBlue());                    
                }
                else if(LookUtils.IS_OS_WINDOWS)
                {
                    PlasticLookAndFeel.setMyCurrentTheme(
                            new com.jgoodies.plaf.plastic.theme.SkyBluer());
                }               
            }
            
            UIManager.setLookAndFeel(className);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            logger.log(Level.WARNING, ex.getMessage(), ex);
            //System.err.println(i18n.str("invalid_laf_prompt") + className); //$NON-NLS-1$
        }   
    }
            
}