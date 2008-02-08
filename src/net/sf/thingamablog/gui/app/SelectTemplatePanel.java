package net.sf.thingamablog.gui.app;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.atlanticbb.tantlinger.i18n.I18n;


public class SelectTemplatePanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app");
    public static final String TEMPLATE_ZIP_PATH = "templateZip";  //  @jve:decl-index=0: //$NON-NLS-1$
    public static final String TEMPLATE_NAME = "templateName"; //$NON-NLS-1$
    
    
    private JLabel msgLabel = null;
    private JButton openButton = null;
    private JScrollPane scrollPane = null;
    private JList tmplList = null;
    
    private String zipPath;

    /**
     * This method initializes 
     * 
     */
    public SelectTemplatePanel() {
    	super();
    	initialize();
    }
    
    public boolean canProceed()
    {
        if(zipPath == null)
        {
            JOptionPane.showMessageDialog(this, i18n.str("open_pack_prompt"), i18n.str("missing_data"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        
        //values.put(TEMPLATE_ZIP_PATH, zipPath);
        
        String tName = (String)tmplList.getSelectedValue();
        if(tName == null)
        {
            JOptionPane.showMessageDialog(this, i18n.str("open_pack_prompt"), i18n.str("missing_data"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        
        //values.put(TEMPLATE_NAME, tName);
        
        return true;
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        gridBagConstraints2.insets = new Insets(5, 0, 0, 0);
        gridBagConstraints2.gridx = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints1.insets = new Insets(5, 0, 0, 5);
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridy = 0;
        msgLabel = new JLabel();
        msgLabel.setText("<html>" + i18n.str("select_template_prompt") + "</html>"); //$NON-NLS-1$
        this.setLayout(new GridBagLayout());
        this.setSize(new Dimension(434, 260));
        this.add(msgLabel, gridBagConstraints);
        this.add(getOpenButton(), gridBagConstraints1);
        this.add(getScrollPane(), gridBagConstraints2);
    		
    }

    /**
     * This method initializes openButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getOpenButton()
    {
        if(openButton == null)
        {
            openButton = new JButton();
            openButton.setText(i18n.str("open_zip_file_")); //$NON-NLS-1$
            openButton.addActionListener(new OpenHandler());
        }
        return openButton;
    }

    /**
     * This method initializes scrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getScrollPane()
    {
        if(scrollPane == null)
        {
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(getTmplList());
        }
        return scrollPane;
    }

    /**
     * This method initializes tmplList	
     * 	
     * @return javax.swing.JList	
     */
    private JList getTmplList()
    {
        if(tmplList == null)
        {
            tmplList = new JList();
            tmplList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        return tmplList;
    }
    
    /*
    private String getTemplateDirRoot(String entPath)
    {        
        int pos = entPath.lastIndexOf("/templates/"); //$NON-NLS-1$
        if(pos == -1)
            return null;
        String root = entPath.substring(0, pos);
        
        int slashCount = 0;
        for(int i = 0; i < root.length(); i++)
        {
            if(root.charAt(i) == '/')
                slashCount++;
            if(slashCount > 2)
                return null;
        }
        return root;
    }
    */
    
    private class OpenHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {           
            /*JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogTitle("Open Template Pack"); 
            fc.setFileFilter(new javax.swing.filechooser.FileFilter()
            {
                public boolean accept(File f)
                {
                    return f.isDirectory() || 
                        f.getName().toLowerCase().endsWith(".zip");
                }
                
                public String getDescription()
                {
                    return "Template Pack Zip file"; 
                }           
            });
            String lastTmplPack = (String)TemplateImportWizard.getProperties().get("last_tmpl_pack");
            if(lastTmplPack != null)
            {
                File f = new File(lastTmplPack);
                if(f.exists())
                    fc.setSelectedFile(f);
            }
            
            int r = fc.showOpenDialog(SelectTemplatePanel.this);
            if(r == JFileChooser.CANCEL_OPTION || fc.getSelectedFile() == null)
                return;
            
            File zipFile = fc.getSelectedFile();            
            ZipFile z = null;
            
            try
            {
                z = new ZipFile(zipFile);
                zipPath = zipFile.getAbsolutePath();
                Enumeration eenum = z.entries();                                
                Vector roots = new Vector();
                
                while(eenum.hasMoreElements()) 
                {
                    ZipEntry ze = (ZipEntry) eenum.nextElement(); 
                    String name = ze.getName();
                    if(name.toLowerCase().endsWith(".template"))
                    {
                        String root = getTemplateDirRoot(name);
                        if(root != null && !roots.contains(root))
                        {
                            //System.err.println(root);
                            roots.add(root);
                        }
                    }                   
                }
                
                getTmplList().setListData(roots);
                //TemplateImportWizard.getProperties().put("last_tmpl_pack", zipFile.getAbsolutePath());
                
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            finally
            {
                if(z != null)
                {
                    try
                    {
                        z.close();
                    }
                    catch(Exception ex){}
                }
            }            */
        }
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
