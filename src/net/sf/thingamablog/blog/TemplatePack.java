/*
 * Created on Oct 30, 2007
 */
package net.sf.thingamablog.blog;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


/**
 * @author Bob Tantlinger
 *
 */
public interface TemplatePack
{
    public String getTitle();
    
    public Properties getPackProperties() throws IOException;
    
    public void installPack(File dir) throws IOException;    
    
}
