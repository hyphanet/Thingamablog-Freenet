/*
 * Created on Nov 13, 2005
 *
 */
package net.sf.thingamablog.gui.properties;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.thingamablog.gui.Messages;

public class ASCIIPanel extends JPanel implements ActionListener
{

    private JScrollPane jScrollPane = null;
    
    private JTextField addField = null;
    private JButton addButton = null;
    private JButton removeButton = null;
    private JLabel msgLabel = null;
    private JList asciiList = null;
    private DefaultListModel model;
    
    
    /**
     * This is the default constructor
     */
    public ASCIIPanel()
    {
        super();        
        model = new DefaultListModel();
/*        List exts = ftp.getASCIIExtensions();
        for(Iterator it = exts.iterator(); it.hasNext();)
            model.addElement(it.next());*/
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
        gridBagConstraints8.gridx = 0;
        gridBagConstraints8.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints8.gridwidth = 3;
        gridBagConstraints8.weightx = 0.0;
        gridBagConstraints8.weighty = 0.0;
        gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints8.gridheight = 1;
        gridBagConstraints8.insets = new java.awt.Insets(0,0,4,0);
        gridBagConstraints8.gridy = 0;
        msgLabel = new JLabel();
        String msg = Messages.getString("ASCIIPanel.msg");
        msgLabel.setText("<html>" + msg + "</html>");
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 2;
        gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints4.ipadx = 7;
        gridBagConstraints4.gridy = 3;
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 2;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints3.gridy = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.insets = new java.awt.Insets(0,0,5,0);
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.weightx = 0.0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.insets = new java.awt.Insets(0,0,0,8);
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(278, 221);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(getJScrollPane(), gridBagConstraints);
        this.add(getAddField(), gridBagConstraints1);
        this.add(getAddButton(), gridBagConstraints3);
        this.add(getRemoveButton(), gridBagConstraints4);
        this.add(msgLabel, gridBagConstraints8);
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == addButton && !addField.getText().equals(""))
        {
            String ext = addField.getText().toLowerCase();
            if(ext.startsWith("."))
            {                
                ext = ext.substring(1, ext.length()); 
            }
            
            if(!ext.equals(""))
            {
                model.addElement(ext);
                addField.setText("");
            }
        }
        else if(e.getSource() == removeButton && asciiList.getSelectedIndex() != -1)
        {           
            model.remove(asciiList.getSelectedIndex());
        }
            
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane()
    {
        if(jScrollPane == null)
        {
            jScrollPane = new JScrollPane(getAsciiList());            
        }
        return jScrollPane;
    }


    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getAddField()
    {
        if(addField == null)
        {
            addField = new JTextField();
            CaretListener cl = new CaretListener()
            {
                public void caretUpdate(CaretEvent e)
                {
                    addButton.setEnabled(!addField.getText().equals(""));
                }
            };
            addField.addCaretListener(cl);
            addField.setColumns(4);
        }
        return addField;
    }
    
    private JList getAsciiList()
    {
        if(asciiList == null)
        {
            asciiList = new JList(model); 
            ListSelectionListener lsl = new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    removeButton.setEnabled(asciiList.getSelectedIndex() != -1);
                }
            };
            asciiList.addListSelectionListener(lsl);
        }
        return asciiList;
    }

    /**
     * This method initializes addButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getAddButton()
    {
        if(addButton == null)
        {
            addButton = new JButton();
            addButton.setText(Messages.getString("ASCIIPanel.Add"));
            addButton.addActionListener(this);
            addButton.setEnabled(false);
        }
        return addButton;
    }

    /**
     * This method initializes removeButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getRemoveButton()
    {
        if(removeButton == null)
        {
            removeButton = new JButton();
            removeButton.setText(Messages.getString("ASCIIPanel.Remove"));
            removeButton.addActionListener(this);
            removeButton.setEnabled(false);
        }
        return removeButton;
    }
    
    public void setListData(List data)
    {
        model.removeAllElements();
        for(Iterator it = data.iterator(); it.hasNext();)
            model.addElement(it.next());
    }
    
    public List getListData()
    {
        ArrayList data = new ArrayList();
        for(int i = 0; i < model.size(); i++)
            data.add(model.get(i));
        
        return data;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
