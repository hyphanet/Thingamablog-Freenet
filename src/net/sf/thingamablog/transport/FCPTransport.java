/*
 * FCPTransport.java
 *
 * Created on 13 février 2008, 02:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.thingamablog.transport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.plaf.ProgressBarUI;
import net.sf.thingamablog.blog.PublishProgress;

import net.sf.thingamablog.util.freenet.fcp.Client;
import net.sf.thingamablog.util.freenet.fcp.ClientPutComplexDir;
import net.sf.thingamablog.util.freenet.fcp.Connection;
import net.sf.thingamablog.util.freenet.fcp.DirectFileEntry;
import net.sf.thingamablog.util.freenet.fcp.DiskFileEntry;
import net.sf.thingamablog.util.freenet.fcp.FileEntry;
import net.sf.thingamablog.util.freenet.fcp.Message;
import net.sf.thingamablog.util.freenet.fcp.Verbosity;
import net.sf.thingamablog.util.freenet.fcp.fcpManager;
import net.sf.thingamablog.util.string.ASCIIconv;

/**
 * There is *a lot* of code below that comes from jSite
 * @author dieppe
 */
public class FCPTransport implements PublishTransport {
    private static Logger logger = Logger.getLogger("net.sf.thingamablog.transport");
    private fcpManager Manager = new fcpManager();
    private String insertURI;
    private Client client;
    private String failMsg;
    private int edition;
    private String hostname;
    private int port;
    private boolean activeLink;
    private String activeLinkPath;
    
    /**
     * Connects the transport
     *
     * @return true on success, false otherwise
     */
    public boolean connect(){
        failMsg="";
        if(Manager.isConnected()){
            failMsg="Already connected";
            return false;
        }
        try {
            logger.info("Connecting to the node...");
            Manager.getConnection().connect();
            client = new Client(Manager.getConnection());
            logger.info("Connected!");
            return true;
        } catch (IOException ioe) {
            failMsg="Unable to connect to the node : " + ioe.getMessage();
            logger.log(Level.WARNING,failMsg);
            ioe.printStackTrace();
        }
        return false;
    }
    
    /**
     * Disconnects the transport
     *
     * @return true on success, false otherwise
     */
    public boolean disconnect(){
        if (client.isDisconnected())
            return true;
        logger.info("Disconnecting from the node...");
        Manager.getConnection().disconnect();
        logger.info("Disconnected!");
        return true;
    }
    
    /**
     * Indicates if the transport is connected
     *
     * @return true if connected, false if not
     */
    public boolean isConnected(){
        return Manager.isConnected();
    }
    
    /**
     * Returns the reason the connect or publishFile returned false.. i.e failed
     *
     * @return The reason for failing
     */
    public String getFailureReason(){
        return failMsg;
    }
    
    public boolean publishFile(String pubPath, File file, TransportProgress tp) {
        logger.log(Level.WARNING,"You shouldn't be here! Only complete dir are publish with fcp.");
        return false;
    }
    
    public boolean publishFile(Hashtable ht, PublishProgress tp, String frontPage, String arcPath, String title){
        //We do the publish job for an entire directory
        if(!Manager.isConnected()){
            logger.log(Level.WARNING,"The connection to the node is not open !");
            failMsg="Not connected";
            return false;
        }
        System.out.println("Beginning of the publish process...");
        String dirURI = "freenet:USK@" + insertURI + "/" + ASCIIconv.convertNonAscii(title) + "/" + edition + "/";
        System.out.println("Insert URI : " + dirURI);
        ClientPutComplexDir putDir = new ClientPutComplexDir("Thingamablog insert", dirURI);
        System.out.println("Default name : " + frontPage);
        putDir.setDefaultName(frontPage);
        putDir.setMaxRetries(-1);
        putDir.setVerbosity(Verbosity.ALL);
        int totalBytes = 0;
        for(Enumeration e = ht.keys() ; e.hasMoreElements() ;) {
            Object element = e.nextElement();
            File file = (File)element;
            String path = ((String) ht.get(element)).substring(arcPath.length());
            FileEntry fileEntry = createFileEntry(file, edition, path);            
            if (fileEntry != null) {
                System.out.println("File to insert : " + fileEntry.getFilename());
                totalBytes += file.length();
                putDir.addFileEntry(fileEntry);
            }
        }
        // If there is an active link set, we publish it
        if (activeLink) {
            File file = new File(activeLinkPath);
            String content = DefaultMIMETypes.guessMIMEType(file.getName());
            FileEntry fileEntry = new DiskFileEntry("activelink.png", content, file.getPath());
            if (fileEntry != null) {
                System.out.println("File to insert : activelink.png");
                totalBytes += file.length();                
                putDir.addFileEntry(fileEntry);
            }
        }
        try {            
            tp.publishStarted(totalBytes);
            client.execute(putDir);
            System.out.println("Publish in progress...");
        } catch (IOException ioe) {
            logger.log(Level.WARNING,"Publish process failed : " + ioe.getMessage());
            return false;
        }
        String finalURI = null;
        boolean success = false;
        boolean finished = false;
        boolean disconnected = false;
        while (!finished) {
            Message message = client.readMessage();
            finished = (message == null) || (disconnected = client.isDisconnected());
            logger.log(Level.INFO, "Message from the node :" + message);
            if (!finished) {
                String messageName = message.getName();
                if ("URIGenerated".equals(messageName)) {
                    finalURI = message.get("URI");
                }
                if ("SimpleProgress".equals(messageName)) {
                    int total = Integer.parseInt(message.get("Total"));
                    int succeeded = Integer.parseInt(message.get("Succeeded"));
                    int fatal = Integer.parseInt(message.get("FatallyFailed"));
                    int failed = Integer.parseInt(message.get("Failed"));
                    boolean finalized = Boolean.valueOf(message.get("FinalizedTotal")).booleanValue();
                    tp.bytesTransferred(succeeded);
                }
                success = "PutSuccessful".equals(messageName);
                finished = success || "PutFailed".equals(messageName) || messageName.endsWith("Error");
            }            
        }
        // If the publish has been made, we update the edition number to the current edition +1
        if(finalURI != null){
            edition = Integer.parseInt(finalURI.substring(finalURI.length()-1)) + 1;
        }
        return success;
    }
    
    private FileEntry createFileEntry(File file, int edition, String path){
        String content = DefaultMIMETypes.guessMIMEType(file.getName());
        FileEntry fileEntry = new DiskFileEntry(path + file.getName(), content, file.getPath());
        return fileEntry;
    }
    
    public void setNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
        Manager.setNode(hostname,port);
    }
    
    public int getPort(){
        return this.port;
    }
    
    public String getHostname(){
        return this.hostname;
    }
    
    public String getInsertURI(){
        return this.insertURI;
    }
    
    public void setInsertURI(String insertURI){
        this.insertURI=shortenURI(insertURI);
    }
    
    public void setEdition(String edition){
        this.edition=Integer.parseInt(edition);
    }
    
    public int getEdition(){
        return this.edition;
    }
    
    public void setActiveLink(boolean b){
        this.activeLink = b;
    }
    
    public boolean getActiveLink(){
        return this.activeLink;
    }
    
    public void setActiveLinkPath(String activeLinkPath){
        this.activeLinkPath = activeLinkPath;
    }
    
    public String getActiveLinkPath(){
        return this.activeLinkPath;
    }
    
    private String shortenURI(String uri) {
        if (uri.startsWith("freenet:")) {
            uri = uri.substring("freenet:".length());
        }
        if (uri.startsWith("SSK@")) {
            uri = uri.substring("SSK@".length());
        }
        if (uri.startsWith("USK@")) {
            uri = uri.substring("USK@".length());
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length()-1);
        }
        return uri;
    }
}
