/*
 * Created on Oct 18, 2007
 */
package net.sf.thingamablog.transport;


/**
 * @author Bob Tantlinger
 *
 */
public interface MailTransportProgress
{
    public boolean isAborted();
    
    public void emailCheckStarted(String serverName);
    
    public void numberOfMessagesToCheck(int num);
    
    public void messageChecked(String subject, boolean isImportable);
    
    public void emailCheckComplete();
    
    public void mailCheckFailed(String message);
}
