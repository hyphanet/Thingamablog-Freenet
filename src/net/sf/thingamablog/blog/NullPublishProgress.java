/*
 * Created on Oct 29, 2007
 */
package net.sf.thingamablog.blog;

import java.io.File;


/**
 * @author Bob Tantlinger
 *
 */
public class NullPublishProgress implements PublishProgress
{

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#filePublishCompleted(java.io.File, java.lang.String)
     */
    public void filePublishCompleted(File f, String pubPath)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#filePublishStarted(java.io.File, java.lang.String)
     */
    public void filePublishStarted(File f, String pubPath)
    {
        System.out.println(f + " -> " + pubPath);
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#publishCompleted()
     */
    public void publishCompleted()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#publishFailed(java.lang.String)
     */
    public void publishFailed(String reason)
    {
        System.out.println("FAILED:" + reason);

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.blog.PublishProgress#publishStarted(long)
     */
    public void publishStarted(long totalBytesToPublish)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.TransportProgress#bytesTransferred(long)
     */
    public void bytesTransferred(long bytes)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.TransportProgress#isAborted()
     */
    public boolean isAborted()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.TransportProgress#logMessage(java.lang.String)
     */
    public void logMessage(String msg)
    {
        // TODO Auto-generated method stub

    }

}
