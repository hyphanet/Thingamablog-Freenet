/*
 * Created on Feb 11, 2005
 *
 */
package net.sf.thingamablog.gui.app;

import java.net.URL;

import javax.swing.event.HyperlinkListener;

/**
 * @author Bob Tantlinger
 */
public interface HTMLOptionLink extends HyperlinkListener
{
    public String getLinkText();
    public URL getImageURL();    
}
