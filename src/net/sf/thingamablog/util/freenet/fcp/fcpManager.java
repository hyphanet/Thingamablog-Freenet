/*
 * fcpManager.java
 *
 * Created on 17 février 2008, 12:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.thingamablog.util.freenet.fcp;

import java.io.IOException;

/**
 * Copy of jSite's Freenet7Interface
 * @author dieppe
 */
public class fcpManager {
    
    private Node node;
    private Connection connection;
    int counter;
    
    public void setNode(String hostname, int port){
        this.node=new Node(hostname,port);
        this.connection=new Connection(node,"Thingamablog :" + counter++);
    }
    
    public void setNode(String hostname){
        this.node=new Node(hostname);
        this.connection=new Connection(node,"Thingamablog :" + counter++);
    }
    
    public Connection getConnection(){
        return this.connection;
    }
    
    public boolean isNodePresent() throws IOException {
        if (!connection.isConnected()) {
            return connection.connect();
        }
        return true;
    }
    
    public String[] generateKeyPair() throws IOException {
        if (!isNodePresent()) {
            return null;
        }
        GenerateSSK generateSSK = new GenerateSSK();
        Client client = new Client(connection, generateSSK);
        Message keypairMessage = client.readMessage();
        return new String[] { keypairMessage.get("InsertURI"), keypairMessage.get("RequestURI") };
    }
    
    public boolean isConnected(){
        return connection.isConnected();
    }
    
    public Node getNode() {
        return node;
    }
    
    public boolean hasNode() {
        return (node != null) && (connection != null);
    }
    
}
