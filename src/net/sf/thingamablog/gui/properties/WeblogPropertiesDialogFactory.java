/*
 * Created on May 17, 2004
 *
 */
package net.sf.thingamablog.gui.properties;
import java.awt.Frame;

import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.gui.Messages;
import net.sf.thingamablog.gui.StandardDialog;

/**
 * @author Bob Tantlinger
 *
 * Creates and shows the appropriate weblog properties dialog
 */
public class WeblogPropertiesDialogFactory
{
	/**
	 * Shows a weblog property dialog box
	 * @param wb - The weblog
	 * @param f - The dialog's parent frame
	 * @return - true if the user has not cancelled, false otherwise
	 */
	public static boolean showPropertiesDialog(Weblog wb, Frame f)
	{		
		StandardDialog dialog;
		String pTitle = Messages.getString("WeblogPropertiesDialogFactory.Configure"); //$NON-NLS-1$
		if(wb instanceof TBWeblog)
		{
			TBWeblog tbw = (TBWeblog)wb;
			String title = pTitle + " [" + tbw.getTitle() + "]";						 //$NON-NLS-1$ //$NON-NLS-2$
			dialog = new TBWeblogPropertiesDialog(f, title, tbw);
		}
		else
			dialog = new StandardDialog(f, pTitle);
			
		//other types of weblogs may be added in the future
		//for now we're only dealing with TBWeblogs	
		dialog.setLocationRelativeTo(f);
		dialog.setResizable(false);	
		dialog.setVisible(true);
		return dialog.hasUserCancelled();
	}	
}
