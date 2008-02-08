/*
 * Created on Oct 17, 2007
 */
package net.sf.thingamablog.transport;


/**
 * @author Bob Tantlinger
 *
 */
public interface Transport
{
    /**
     * Connects the transport
     * 
     * @return true on success, false otherwise
     */
    public boolean connect();
    
    /**
     * Disconnects the transport
     * 
     * @return true on success, false otherwise
     */ 
    public boolean disconnect();
    
    /**
     * Indicates if the transport is connected
     * 
     * @return true if connected, false if not
     */
    public boolean isConnected();
    
    /**
     * Returns the reason the connect or publishFile returned false.. i.e failed
     * 
     * @return The reason for failing
     */
    public String getFailureReason();
}
