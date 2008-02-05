/*
 * Created on Mar 9, 2005
 *
 */
package net.sf.thingamablog.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
/**
 * @author Bob Tantlinger
  */
public class ErrorDialog extends JDialog
{

    private JButton close = new JButton(Messages.getString("JAboutBox.Close"));

    /**
     * @param parent
     * @param title
     */
    public ErrorDialog(Frame parent, String title, String message, Exception ex)
    {
        super(parent, title);        
        init(message, ex);
        
    }

    /**
     * @param parent
     * @param title
     */
    public ErrorDialog(Dialog parent, String title, String message, Exception ex)
    {
        super(parent, title);        
        init(message, ex);        
    }
    
    private void init(String msg, Exception ex)
    {        
        JLabel message = new JLabel(msg);
        message.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
        message.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel detailsPanel = new JPanel(new BorderLayout());
        JPanel main = new JPanel(new BorderLayout(5, 5));         
        main.add(message, BorderLayout.NORTH);
        
        Dimension size = new Dimension(300, 400);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        if(ex != null)
        {            
            String errData = "Error Details...\n";
            errData += ex.getClass().getName() + "\n";
            errData += ex.getLocalizedMessage() + "\n";
            StackTraceElement el[] = ex.getStackTrace();
            for(int i = 0; i < el.length; i++)
            {            
                errData += el[i].toString() + "\n";
            }
        
            JTextArea ta = new JTextArea();
            ta.setEditable(false);
            ta.setText(errData);
            ta.setCaretPosition(0);
            detailsPanel.add(new JScrollPane(ta));
            main.add(detailsPanel, BorderLayout.CENTER);
                        
        }
        
        ActionListener lst = new ButtonHandler();
        close.addActionListener(lst);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(close);
        main.add(buttonPanel, BorderLayout.SOUTH);        
        
        getContentPane().add(main);
        
        setModal(true);
        setSize(400, 250);
        
    }
    
    private class ButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == close)
                dispose(); 
        }
    }
    
}
