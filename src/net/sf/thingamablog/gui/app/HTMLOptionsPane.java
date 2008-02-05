/*
 * Created on Feb 10, 2005
 *
 */
package net.sf.thingamablog.gui.app;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.sf.thingamablog.TBGlobals;



/**
 * @author Owner
 *
 */
public class HTMLOptionsPane extends JPanel
{
    private final String CSS = TBGlobals.RESOURCES + "options.css";
    
    private JEditorPane pane;    
    private HTMLEditorKit kit;    
    private Vector layout = new Vector();
    private String title = "";
    private URL titleImgURL;
    
    public HTMLOptionsPane()
    {
        setLayout(new BorderLayout());
        pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        
        StyleSheet styleSheet = new StyleSheet();
        try
        {            
            Reader reader = new InputStreamReader(ClassLoader.getSystemResourceAsStream(CSS));
            styleSheet.loadRules(reader, ClassLoader.getSystemResource(CSS));
            reader.close();
        }
        catch(IOException ie)
        {
            ie.printStackTrace();
        }
        
        kit = new MyHTMLEditorKit();        
        kit.setStyleSheet(styleSheet); 
        pane.setEditorKit(kit);
        pane.addHyperlinkListener(new ClickListener());
        
        add(new JScrollPane(pane));
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public void setImageURL(URL u)
    {
        titleImgURL = u;
    }
    
    public void addHeading(String heading)
    {
        layout.add(heading);
    }    
        
    public void addOption(HTMLOptionLink opt)
    {
        layout.add(opt);
    }
    
    public boolean remove(Object obj)
    {
        return layout.remove(obj);
    }
    
    public void clearOptions()
    {
        layout.removeAllElements();
    }
    
    public void refresh()
    {
        //System.out.println("REFRESHING");
        String html = "<html>\n<h1>";
        if(titleImgURL != null)
            html += "<img src=\"" + titleImgURL.toExternalForm() + "\" align=middle>";
        html += title + "</h1>\n";        
        
        for(int i = 0; i < layout.size(); i++)
        {            
            Object o = layout.elementAt(i);
            if(o instanceof HTMLOptionLink)
            {
                html += "<table BORDER=0 CELLPADDING=5 CELLSPACING=5>";
                html += "<tr>";
                html += "<td>";
                HTMLOptionLink link = (HTMLOptionLink)o;
                if(link.getImageURL() != null);
                	html += "<img src=\"" + link.getImageURL().toExternalForm() + "\">";
                html += "</td><td><a href=\"http://" + i + "\">" + link.getLinkText() + "</a></td>";
                html += "</tr></table>";
            }
            else
            {
                html += "<h2>" + o + "</h2>";
            }
            
        }
        html += "</html>";
        
        pane.setText("");
        StringReader reader = new StringReader(html);
        
        try
        {       
            kit.read(reader, pane.getDocument(), 0);
        }
        catch(Exception ex){}
        pane.setCaretPosition(0);
    } 
    
    /*
     * This class extends HTMLEditor kit to override 
     * get/setStyleSheet because HTMLEditorkit uses a static StyleSheet
     * instance, thereby forcing all editorkits to use that static set of styles
     * 
     */
    private class MyHTMLEditorKit extends HTMLEditorKit
    {
        private StyleSheet styleSheet;
        
        public void setStyleSheet(StyleSheet ss)
        {
            styleSheet = ss;
        }
        
        public StyleSheet getStyleSheet() 
        {
        	if(styleSheet == null)
        	    return super.getStyleSheet();
        	return styleSheet;
        }        
    }
    
    private class ClickListener implements HyperlinkListener
    {
        public void hyperlinkUpdate(HyperlinkEvent ev)
        {
            try
            {
                URL u = ev.getURL();
                int num = Integer.parseInt(u.getHost());
                HTMLOptionLink link = (HTMLOptionLink)layout.elementAt(num);
                link.hyperlinkUpdate(ev);               
            }
            catch(Exception ex){}
        }
    } 
}
