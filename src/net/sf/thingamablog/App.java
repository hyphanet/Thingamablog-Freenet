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
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFrame;

import net.sf.thingamablog.gui.app.ThingamablogFrame;

/** Application starter */

public class App
{
    public static void main(String args[])
    {        
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
        
        Logger logger = Logger.getLogger("net.sf.thingamablog");
        
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
        
        long time = System.currentTimeMillis();
        createAndShowGUI();
        long initTime = System.currentTimeMillis() - time;        
        logger.info("Init time: " + initTime + "ms");
        
    }

    private static void createAndShowGUI()
    {
       //RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
        
        JFrame f = new ThingamablogFrame();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int)((d.width - f.getSize().width) / 2);
        int y = (int)((d.height - f.getSize().height) / 2);
        f.setLocation(x, y);
        f.setTitle(TBGlobals.APP_NAME + ' ' + TBGlobals.VERSION);
        f.setVisible(true);
    }
}