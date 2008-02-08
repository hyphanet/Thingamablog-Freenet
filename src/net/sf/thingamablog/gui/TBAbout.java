/*
 * Created on Aug 13, 2004
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
package net.sf.thingamablog.gui;
import java.awt.Frame;
import java.io.File;

import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.TBGlobals;


/**
 * Shows the Thingamablog about window
 * 
 * @author Bob Tantlinger
 *
 */
public class TBAbout
{
	public static void showAboutBox(Frame f)
	{
		JAboutBox ab = new JAboutBox(
			f, "About " + TBGlobals.APP_NAME, new File("license.txt")); //$NON-NLS-1$ //$NON-NLS-2$
		ab.setAppIcon(UIUtils.getIcon(UIUtils.X32, "tamb.png")); //$NON-NLS-1$
		ab.setAppTitle(TBGlobals.APP_NAME + ' ' + TBGlobals.VERSION);
		ab.setAppDescription("A cross-platform, standalone blogging application"); //$NON-NLS-1$
		ab.setAppCopyright("(c) 2003 - 2007, Bob Tantlinger"); //$NON-NLS-1$
		ab.setAppUrl("http://thingamablog.sourceforge.net"); //$NON-NLS-1$
		ab.setMisc("Build: " + TBGlobals.BUILD);
 		
		ab.addAuthor("Bob Tantlinger\n" +  //$NON-NLS-1$
			"http://thingamablog.sourceforge.net\n" + //$NON-NLS-1$
			"mailto:null_mind@users.sourceforge.net\n" +  //$NON-NLS-1$
			"Core developer and maintainer"); 		 //$NON-NLS-1$
			
			
		ab.addContributor("Hypersonic SQL\n" + //$NON-NLS-1$
			"http://hsqldb.sourceforge.net\n" + //$NON-NLS-1$
			"HSQLDB Backend"); //$NON-NLS-1$
		ab.addContributor("Enterprise Distributed Technologies\n" + //$NON-NLS-1$
			"http://www.enterprisedt.com/products/edtftpj/overview.html\n" + //$NON-NLS-1$			
			"FTP Package"); 		 //$NON-NLS-1$
		
		ab.addContributor("JMySpell\n" + //$NON-NLS-1$
			"http://javahispano.net/projects/jmyspell/\n" + //$NON-NLS-1$
			"Java Spell Check API"); //$NON-NLS-1$
		ab.addContributor("JDOM\n" + //$NON-NLS-1$
			"http://www.jdom.org\n" + //$NON-NLS-1$
			"Java XML API"); //$NON-NLS-1$
		ab.addContributor("Jsch\n" + //$NON-NLS-1$
			"http://www.jcraft.com/jsch/\n" + //$NON-NLS-1$
			"SFTP"); //$NON-NLS-1$
		ab.addContributor("JGoodies\n" + //$NON-NLS-1$
			"http://www.jgoodies.com/freeware/looks/\n" + //$NON-NLS-1$
			"Plastic Look and Feel"); //$NON-NLS-1$
		ab.addContributor("Apache Web Services Project\n" + //$NON-NLS-1$
			"http://ws.apache.org/xmlrpc/\n" + //$NON-NLS-1$
			"Java XML-RPC API"); //$NON-NLS-1$
		ab.addContributor("The Rome Project\n" + //$NON-NLS-1$
			"http://rome.dev.java.net/\n" + //$NON-NLS-1$
			"Java RSS/Atom utilities"); //$NON-NLS-1$
		ab.addContributor("Movablestyle.com\n" + //$NON-NLS-1$
			"http://www.movablestyle.com/\n" + //$NON-NLS-1$
			"Default Templates"); //$NON-NLS-1$			
		ab.addContributor("Templaillo Ahi\nSpanish translation");
		ab.addContributor("Guill\u00F4me\nFrench translation");
		ab.addContributor("Stef, Martin Hense, http://thingamablog.de.vu/ \nGerman translation");
        ab.addContributor("Tatsuya Aoyagi\nJapanese translation");
 		
		ab.setLocationRelativeTo(f);
		ab.setVisible(true);	
	}
}
