/*
 * Created on Aug 27, 2004
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.jstatcom.component.JHelpAction;

/**
 * @author Bob Tantlinger
 */
public class TBHelpAction extends AbstractAction
{
    private static boolean IS_HELP_WORKER_STARTED = false;
    private static final String HELP_SET = 
        "userguide/thingamablog/help/tb_help.hs";
    
    private String helpPath;
    
    public TBHelpAction(String name, String helpPath)
    {
        super(name);
        this.helpPath = helpPath;
    }
   
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if(!IS_HELP_WORKER_STARTED)
        {
            JHelpAction.startHelpWorker(HELP_SET);
            IS_HELP_WORKER_STARTED = true;
        }

        JHelpAction.showHelp(helpPath);
    }
}
