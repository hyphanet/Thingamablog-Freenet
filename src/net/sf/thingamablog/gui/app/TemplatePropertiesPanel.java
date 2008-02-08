/*
 * Created on Oct 29, 2007
 */
package net.sf.thingamablog.gui.app;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Insets;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.BorderFactory;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.blog.TemplatePack;


/**
 * @author Bob Tantlinger
 *
 */
public class TemplatePropertiesPanel extends JPanel
{    
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app");
    private static final long serialVersionUID = 1L;
    
    private JLabel titleLabel = null;
    private JTextField titleField = null;
    private JLabel authorLabel = null;
    private JTextField authorField = null;
    private JLabel dateLabel = null;
    private JTextField dateField = null;
    private JLabel descrLabel = null;
    private JScrollPane descrScrollPane = null;
    private JTextArea descrArea = null;
    
    private DateFormat dateFormat = DateFormat.getDateTimeInstance();
    private Date date;

    /**
     * This is the default constructor
     */
    public TemplatePropertiesPanel()
    {
        super();
        initialize();
    }
    
    public void setTemplatePack(TemplatePack p)
    {
        try
        {
            setProperties(p.getPackProperties());
        }
        catch(IOException e)
        {            
            e.printStackTrace();
        }
    }
    
    public void setProperties(Properties m)
    {
        titleField.setText(""); //$NON-NLS-1$
        authorField.setText(""); //$NON-NLS-1$
        dateField.setText(""); //$NON-NLS-1$
        descrArea.setText(""); //$NON-NLS-1$
        
        if(m.containsKey("title")) //$NON-NLS-1$
            titleField.setText(m.getProperty("title")); //$NON-NLS-1$
        if(m.containsKey("author")) //$NON-NLS-1$
            authorField.setText(m.getProperty("author")); //$NON-NLS-1$
        if(m.containsKey("created")) //$NON-NLS-1$
        {
            try
            {
                date = new Date(Long.parseLong((m.getProperty("created")))); //$NON-NLS-1$
                dateField.setText(dateFormat.format(date));
            }
            catch(Exception ex){}
        }
        if(m.containsKey("description")) //$NON-NLS-1$
            descrArea.setText(m.getProperty("description")); //$NON-NLS-1$
    }
    
    public Properties getProperties()
    {
        Properties p = new Properties();
        p.put("title", titleField.getText()); //$NON-NLS-1$
        p.put("author", authorField.getText()); //$NON-NLS-1$
        if(date != null)
            p.put("created", date.getTime()+""); //$NON-NLS-1$ //$NON-NLS-2$
        p.put("description", descrArea.getText()); //$NON-NLS-1$
        return p;
    }
    
    public void setEditable(boolean b)
    {
        titleField.setEditable(b);
        authorField.setEditable(b);
        //dateField.setEditable(b);
        descrArea.setEditable(b);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.fill = GridBagConstraints.BOTH;
        gridBagConstraints31.gridy = 3;
        gridBagConstraints31.weightx = 1.0;
        gridBagConstraints31.weighty = 1.0;
        gridBagConstraints31.gridx = 1;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.gridx = 0;
        gridBagConstraints21.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints21.insets = new Insets(0, 0, 0, 5);
        gridBagConstraints21.gridy = 3;
        descrLabel = new JLabel();
        descrLabel.setText(i18n.str("description")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints11.gridy = 2;
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.anchor = GridBagConstraints.WEST;
        gridBagConstraints11.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints11.gridx = 1;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.anchor = GridBagConstraints.WEST;
        gridBagConstraints4.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints4.gridy = 2;
        dateLabel = new JLabel();
        dateLabel.setText(i18n.str("created_on")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.anchor = GridBagConstraints.WEST;
        gridBagConstraints3.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints3.gridx = 1;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.anchor = GridBagConstraints.WEST;
        gridBagConstraints2.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints2.gridy = 1;
        authorLabel = new JLabel();
        authorLabel.setText(i18n.str("author")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.anchor = GridBagConstraints.WEST;
        gridBagConstraints1.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints1.gridx = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridy = 0;
        titleLabel = new JLabel();
        titleLabel.setText(i18n.str("title")); //$NON-NLS-1$
        this.setSize(278, 243);
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(titleLabel, gridBagConstraints);
        this.add(getTitleField(), gridBagConstraints1);
        this.add(authorLabel, gridBagConstraints2);
        this.add(getAuthorField(), gridBagConstraints3);
        this.add(dateLabel, gridBagConstraints4);
        this.add(getDateField(), gridBagConstraints11);
        this.add(descrLabel, gridBagConstraints21);
        this.add(getDescrScrollPane(), gridBagConstraints31);
    }

    /**
     * This method initializes titleField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTitleField()
    {
        if(titleField == null)
        {
            titleField = new JTextField();
            TextEditPopupManager.getInstance().registerJTextComponent(titleField);
        }
        return titleField;
    }

    /**
     * This method initializes authorField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getAuthorField()
    {
        if(authorField == null)
        {
            authorField = new JTextField();
            TextEditPopupManager.getInstance().registerJTextComponent(authorField);
        }
        return authorField;
    }

    /**
     * This method initializes dateField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getDateField()
    {
        if(dateField == null)
        {
            dateField = new JTextField();
            dateField.setEditable(false);
            TextEditPopupManager.getInstance().registerJTextComponent(dateField);
        }
        return dateField;
    }

    /**
     * This method initializes descrScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getDescrScrollPane()
    {
        if(descrScrollPane == null)
        {
            descrScrollPane = new JScrollPane();
            descrScrollPane.setViewportView(getDescrArea());
        }
        return descrScrollPane;
    }

    /**
     * This method initializes descrArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getDescrArea()
    {
        if(descrArea == null)
        {
            descrArea = new JTextArea();
            descrArea.setWrapStyleWord(true);
            descrArea.setLineWrap(true);
            TextEditPopupManager.getInstance().registerJTextComponent(descrArea);
        }
        return descrArea;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
