/*
 * Created on Mar 5, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Frame;

import javax.swing.JTextField;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;


/**
 * 
 */
public class ElementDialog extends HTMLOptionDialog
{
    private static final String RES = TBGlobals.RESOURCES;
    
    private JTextField classField = new JTextField(25);
    private JTextField idField = new JTextField(25);
    private JTextField styleField = new JTextField(25);    
    
    
    public ElementDialog(Frame parent)
    {
        //super(parent, Messages.getString("ElementDialog.Object_Properties"),
        super(parent, Messages.getString("ElementDialog.Style"),
            Utils.createIcon(RES + "textbig.png"));
        LabelledItemPanel lip = new LabelledItemPanel();
        
        TextEditPopupManager pm = new TextEditPopupManager();
        pm.addJTextComponent(idField);
        pm.addJTextComponent(classField);
        pm.addJTextComponent(styleField);
        
        lip.addItem(Messages.getString("ElementDialog.ID"), idField);
        lip.addItem(Messages.getString("ElementDialog.Class"), classField);
        lip.addItem(Messages.getString("ElementDialog.Style"), styleField);
        setContentPanel(lip);
        pack();
        setResizable(false);
    }
    
    public String getID()
    {
        return idField.getText();
    }
    
    public String getStyleClass()
    {
        return classField.getText();
    }
    
    public String getStyle()
    {
        return styleField.getText();
    }
    
    public void setID(String id)
    {
        idField.setText(id);
    }
    
    public void setStyleClass(String s)
    {
        classField.setText(s);
    }
    
    public void setStyle(String s)
    {
        styleField.setText(s);
    }
    
    /* (non-Javadoc)
     * @see org.openmechanics.htmleditor.HTMLOptionDialog#getHTML()
     */
    public String getHTML()
    {
        String p = "<p";
        
        if(!getID().equals(""))
            p += " id=\"" + getID() + "\"";
        
        if(!getStyleClass().equals(""))
            p += " class=\"" + getStyleClass() + "\"";
        
        if(!getStyle().equals(""))
            p += " style=\"" + getStyle() + "\"";
        
        return p + ">";
    }

}
