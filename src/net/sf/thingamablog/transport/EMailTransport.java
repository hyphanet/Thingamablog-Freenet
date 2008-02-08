/*
 * Created on Oct 17, 2007
 */
package net.sf.thingamablog.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.BlogEntry;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;


/**
 * @author Bob Tantlinger
 *
 */
public class EMailTransport extends RemoteTransport 
{    
    //pop3 or imap
    private String protocol = "pop3";    
    private Store store = null;
    private String failureReason = null;
    private String postDirective = "POST";
    
    private Tidy tidy = new Tidy();
    
    public EMailTransport()
    {
        this.setPort(110);
        tidy.setXHTML(true);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.MailTransport#getEntries()
     */
    public List getEntries(Author[] authors, String[] catNames, MailTransportProgress prg)
    throws Exception
    {        
        List entries = new ArrayList();
        prg.emailCheckStarted(getAddress());
        try
        {
            Folder folder = store.getFolder("INBOX");                       
            if(folder != null)
            {
                folder.open(Folder.READ_WRITE); 
                Message[] message = folder.getMessages();            
                if(message != null)
                {
                    prg.numberOfMessagesToCheck(message.length);
                    for(int i = 0; i < message.length; i++)
                    {                        
                        if(prg.isAborted())
                            break;
                        String subj = message[i].getSubject();
                        
                        boolean isAdded = false;
                        BlogEntry be = this.createEntryFromMessage(message[i], authors, catNames);
                        if(be != null)
                        {
                            entries.add(be);
                            message[i].setFlag(Flags.Flag.DELETED, true);
                            isAdded = true;
                        }
                        
                        prg.messageChecked((subj == null) ? "(No subject)" : subj, isAdded);
                    }
                }
                
                folder.close(true);
            }        
        }
        catch(Exception ex)
        {
            prg.mailCheckFailed(ex.getLocalizedMessage());
            throw ex;
        }
        finally
        {
            prg.emailCheckComplete();
        }
        
        return entries;
    }
    
    private BlogEntry createEntryFromMessage(Message m, Author[] auths, String[] cats) throws MessagingException, IOException
    {
        String subj = m.getSubject();            
        if(subj == null || !subj.toLowerCase().startsWith(postDirective.toLowerCase())) //this isn't a post email
            return null;
        
        int colonIndex = subj.indexOf(':', postDirective.length());//subj.indexOf(':');
        if(colonIndex == -1)
            return null; //didn't end with a colon        
        
        Author auth = getAuthor(m, auths);
        if(auth == null)
            return null; //we don't know who this message is from
        
        BlogEntry be = new BlogEntry();
        be.setTitle(subj.substring(colonIndex + 1, subj.length()).trim());
        be.setAuthor(auth);
        
        String postPrefix = subj.substring(postDirective.length(), colonIndex).trim().toLowerCase();        
        
        //now figure out what cats there are
        if(postPrefix.equals("")) //no cats
        {
            //System.err.println("NO CATS");
        }
        else if(postPrefix.startsWith("in ")) //we have some cats
        {
            postPrefix = postPrefix.substring(3, postPrefix.length());
            //System.err.println(postPrefix);
            
            String[] ecats = postPrefix.split(",");
            HashSet entryCats = new HashSet();
            for(int i = 0; i < ecats.length; i++)
            {
                String temp = ecats[i].trim();                
                for(int j = 0; j < cats.length; j++)
                {
                    //the blog cat we're looking for
                    String theCat = cats[j].toLowerCase().trim();
                    
                    //if the blog cat has a comma or colon in it
                    //we'll replace it with the char entity so we
                    //can compare it with the cat in the email subject
                    //comma=&#44;, colon entity= &#58;
                    theCat = theCat.replaceAll(",", "&#44;");
                    theCat = theCat.replaceAll(":", "&#58;");                 
                    if(temp.equals(theCat))
                        entryCats.add(cats[j]);
                }
            }
            
            Iterator it = entryCats.iterator();
            while(it.hasNext())
            {
                be.addCategory(it.next().toString());
            }
        }
        else
        {
            //malformed - has non cats between 'POST' and ':'
            //System.err.println("MALFORMED: " + postPrefix);
            return null;
        }
        
        be.setDate(m.getSentDate());
        be.setText(getMessageBody(m));        
        return be;
    }
    
    private Author getAuthor(Message m, Author[] auths) throws MessagingException
    {
        Address[] adr = m.getFrom();
        if(adr != null && adr.length > 0 && adr[0] instanceof InternetAddress)
        {                            
            String iAdr = ((InternetAddress)adr[0]).getAddress();
            if(iAdr != null && !iAdr.equals(""))
            {
                for(int i = 0; i < auths.length; i++)
                {
                    if(auths[i].getEmailAddress().equals(iAdr))
                        return auths[i];
                }
            }
        }
        
        return null;
    }
    
        
    private String getMessageBody(Message m) throws MessagingException, IOException
    {
        String bodyText = null;
        
        Object content = m.getContent();
        if(content instanceof String) //text/plain
            return getTextBetweenDelimiters((String)content);
        if(content instanceof Multipart)
        {            
            Multipart multiPart = (Multipart)content;
            int partCount = multiPart.getCount();
                        
            for(int i = 0; i < partCount; i++)
            {
                BodyPart bp = multiPart.getBodyPart(i);
                                
                //if it has HTML text, use it instead
                if(bp.getContentType().toLowerCase().startsWith("text/html")) 
                {
                    String rawHtml = getTextBetweenDelimiters(bp.getContent().toString());
                    ByteArrayInputStream bin = new ByteArrayInputStream(rawHtml.getBytes());
                    Document doc = tidy.parseDOM(bin, null);
                    return getBodyText(doc);
                }
                
                if(bodyText == null && bp.getContentType().toLowerCase().startsWith("text/plain"))
                    bodyText = bp.getContent().toString();
            }
        }
        
        if(bodyText == null)
            return "";
        return getTextBetweenDelimiters(bodyText);            
    }
    
    public String getTextBetweenDelimiters(String text)
    {
    	String openDelim =  "$body_start$";
    	String closeDelim = "$body_end$";
    	
    	int openStart = text.indexOf(openDelim);
    	int closeStart = text.lastIndexOf(closeDelim);
    	if(openStart != -1 && closeStart != -1 && openStart < closeStart)
    	{
    		return text.substring(openStart + openDelim.length(), closeStart);
    	}
    	
    	//no delims so just return the original text
    	return text;
    }
    
    private String getBodyText(Document doc)
    {
        NodeList nodelist = doc.getElementsByTagName("body");
        Node body = null;
        if(nodelist != null)
        {
            body = nodelist.item(0);
            NodeList bodyChildren = body.getChildNodes();
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < bodyChildren.getLength(); i++)
            {                
                sb.append(xmlToString(bodyChildren.item(i)));
            }
            
            //System.err.println(sb.toString());
            
            return sb.toString();                
        }
        
        return "";           
    }
    
    private String xmlToString(Node node)
    {
        try
        {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(source, result);
            
            return stringWriter.getBuffer().toString();
        }
        catch(TransformerConfigurationException e)
        {
            e.printStackTrace();
        }
        catch(TransformerException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
    *Sets the protocol name 
    *@param protocol the name of the protocol (supposes "pop3" or "imap")
    */
    public void setProtocol(String prot) throws IllegalArgumentException
    {
        if(prot.compareToIgnoreCase("pop3") == 0)
        {
            protocol = "pop3";            
            //port="110";
        }
        else if(prot.compareToIgnoreCase("imap") == 0)
        {
            protocol = "imap";
            //port="143";
        }
        else 
        {   
           throw new IllegalArgumentException("Invalid protocol: " + prot);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.Transport#connect(net.sf.thingamablog.transport.TransportProgress)
     */
    public boolean connect()
    {        
        failureReason = null;
        Properties props = new Properties();
        props.put("mail." + protocol + ".port", getPort() + "");        
        Session session = Session.getDefaultInstance(props, null);
        
        try
        {
            store = session.getStore(protocol);
            store.connect(getAddress(), getUserName(), getPassword());
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            failureReason = ex.getLocalizedMessage();
            //prg.mailCheckFailed(failureReason);
            return false;
        }        
        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.Transport#disconnect()
     */
    public boolean disconnect()
    {    
        if(store != null)
        {
            try
            {
                store.close();
            }
            catch(MessagingException e)
            {                
                e.printStackTrace();
                return false;
            }
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.transport.Transport#getFailureReason()
     */
    public String getFailureReason()
    {
        return failureReason;
    }

    
    /**
     * @return the protocol
     */
    public String getProtocol()
    {
        return protocol;
    }

    
    /**
     * @return the postDirective
     */
    public String getPostDirective()
    {
        return postDirective;
    }
    
    /**
     * @param postDirective the postDirective to set
     */
    public void setPostDirective(String postDirective)
    {
        this.postDirective = postDirective;
    }
}
