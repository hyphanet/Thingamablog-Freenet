/*
 * Created on Mar 4, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.thingamablog.gui.StandardDialog;


/**
 * @author Bob Tantlinger
 */
public abstract class HTMLOptionDialog extends StandardDialog
{
    private JPanel mainPanel = new JPanel(new BorderLayout());
    
    public HTMLOptionDialog(Frame parent, String title, ImageIcon ico)
    {
        super(parent, title, BUTTONS_RIGHT, 0);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel header = new JLabel(title);
        header.setFont(new java.awt.Font("Dialog", 0, 20));
        header.setForeground(new Color(0, 0, 124));
        header.setIcon(ico);
        topPanel.setBackground(Color.WHITE);        
        topPanel.add(header);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        super.setContentPane(mainPanel);
    }
    
    public void setContentPanel(JPanel cp)
    {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEtchedBorder(Color.WHITE,
            new Color(142, 142, 142)));
        p.add(cp, BorderLayout.CENTER);
        mainPanel.add(p, BorderLayout.CENTER);
    }
    
    public abstract String getHTML();
}
