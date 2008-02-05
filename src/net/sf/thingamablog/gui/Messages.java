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
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.Action;

/*
 * Created on Oct 11, 2003
 */

/**
 * @author Bob Tantlinger
 *
 * Static methods and a ResourceBundle for i18n of text and mnemonics
 * 
 */
public class Messages
{

    private static final String MNEM_POSTFIX = ".mnemonic";
    private static final String I18N_BUNDLE = "net/sf/thingamablog/i18n/messages";
    private static final String DEFAULT_BUNDLE = "net/sf/thingamablog/gui/resources/messages"; //$NON-NLS-1$    
    
    private static ResourceBundle bun = null;
    static
    {
        try
        {
            bun = ResourceBundle.getBundle(I18N_BUNDLE);
        }
        catch(MissingResourceException ex)
        {
            //If, for some reason, the i18n lang zip doesn't exist
            //use the default english message.properties in the 
            //resource package
            System.err.println("USING DEFAULT RESOURCE BUNDLE");
            bun = ResourceBundle.getBundle(DEFAULT_BUNDLE);
        }
    }
    private static final ResourceBundle RESOURCE_BUNDLE = bun;

    /**
     * @param key The key value of the resource
     * @return The i18n resource
     */
    public static String getString(String key)
    {
        try
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        catch(MissingResourceException e)
        {
            return '!' + key + '!'; 
            //just return the key if we cant find the resource
        }
    }
    public static String getString(String key, Locale locale)
    {
        try
        {
            return ResourceBundle.getBundle(I18N_BUNDLE,locale).getString(key);
        }
        catch(MissingResourceException e)
        {
            return '!' + key + '!'; 
            //just return the key if we cant find the resource
        }
    }
 
    /** Set the i18n Mnemonic, if possible, for an action */
    public static void setMnemonic(String resName, Action a)
    {
        String s = getString(resName + MNEM_POSTFIX);
        if(s != null && !s.equals(""))
        {           
            a.putValue(Action.MNEMONIC_KEY, new Integer(s.charAt(0)));              
        }               
    }
    
    /** Set the i18n Mnemonic, if possible, for a button */
    public static void setMnemonic(String resName, AbstractButton b)
    {
        String s = getString(resName + MNEM_POSTFIX);
        if(s != null && !s.equals(""))
        {           
            b.setMnemonic(s.charAt(0));
        }       
    }
}
