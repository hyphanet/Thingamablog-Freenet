/*
 * Created on Oct 29, 2007
 */
package net.sf.thingamablog.gui.app;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.io.IOUtils;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.TemplatePack;
import net.sf.thingamablog.blog.ZipTemplatePack;



/**
 * @author Bob Tantlinger
 *
 */
public class InstallTemplateDialog extends JDialog
{
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app");
    
    private static final long serialVersionUID = 1L;
    private JLabel instrLabel = null;
    private JButton openButton = null;
    private JButton installButton = null;
    private TemplatePropertiesPanel propertyPanel = null;
    
    private ZipTemplatePack tmplPack;
    private JButton closeButton = null;
    
    private JPanel contentPane;
    
    /**
     * This is the default constructor
     */
    public InstallTemplateDialog(Frame owner)
    {
        super(owner, i18n.str("install_template_pack")); //$NON-NLS-1$
        initialize();
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(contentPane, BorderLayout.CENTER);
        this.setSize(new Dimension(455, 295));
        this.setResizable(false);
        this.setModal(true);
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize()
    {
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints1.insets = new Insets(25, 5, 5, 0);
        gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.gridy = 3;
        GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
        gridBagConstraints6.gridx = 1;
        gridBagConstraints6.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints6.insets = new Insets(5, 5, 0, 0);
        gridBagConstraints6.gridy = 2;
        GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
        gridBagConstraints5.gridx = 0;
        gridBagConstraints5.gridheight = 3;
        gridBagConstraints5.fill = GridBagConstraints.BOTH;
        gridBagConstraints5.weightx = 1.0;
        gridBagConstraints5.weighty = 1.0;
        gridBagConstraints5.gridy = 1;
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints4.insets = new Insets(5, 5, 0, 0);
        gridBagConstraints4.gridx = 1;
        gridBagConstraints4.gridy = 1;
        gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 0, 15, 0);
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridy = 0;
        instrLabel = new JLabel();
        instrLabel.setText(i18n.str("install_pack_prompt")); //$NON-NLS-1$
                
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.add(instrLabel, gridBagConstraints);
        contentPane.add(getOpenButton(), gridBagConstraints4);        
        contentPane.add(getPropertyPanel(), gridBagConstraints5);
        contentPane.add(getInstallButton(), gridBagConstraints6);
        contentPane.add(getCloseButton(), gridBagConstraints1);
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
            openButton.setText(i18n.str("open_")); //$NON-NLS-1$
            openButton.addActionListener(new OpenHandler());
        }
        return openButton;
    }

    /**
     * This method initializes installButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getInstallButton()
    {
        if(installButton == null)
        {
            installButton = new JButton();
            installButton.setText(i18n.str("install")); //$NON-NLS-1$
            installButton.setEnabled(false);
            installButton.addActionListener(new InstallHandler());
        }
        return installButton;
    }   

    /**
     * This method initializes propertyPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getPropertyPanel()
    {
        if(propertyPanel == null)
        {
            propertyPanel = new TemplatePropertiesPanel(); 
            propertyPanel.setEditable(false);
        }
        return propertyPanel;
    }  
    
    private void closePack()
    {
        if(tmplPack != null)
        {
            try
            {
                if(tmplPack instanceof ZipTemplatePack)
                (tmplPack).close();
            }
            catch (IOException e)
            {                
                e.printStackTrace();
            }
            
            installButton.setEnabled(false);
        }
    }
    
    private boolean isTemplateWithSameNameInstalled(TemplatePack pack)
    {
        List tmpls = TBGlobals.getAllAvailableTemplates();
        for(Iterator it = tmpls.iterator(); it.hasNext();)
        {
            TemplatePack p = (TemplatePack)it.next();
            if(p.getTitle().equals(pack.getTitle()))
                return true;
        }
        
        return false;
    }
    
    private class OpenHandler implements ActionListener
    {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {            
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogTitle(i18n.str("open_template_pack"));  //$NON-NLS-1$
            fc.setFileFilter(new javax.swing.filechooser.FileFilter()
            {
                public boolean accept(File f)
                {
                    return f.isDirectory() || 
                        f.getName().toLowerCase().endsWith(".zip"); //$NON-NLS-1$
                }
                
                public String getDescription()
                {
                    return i18n.str("template_pack_zip_file");  //$NON-NLS-1$
                }           
            });
            
            int r = fc.showOpenDialog(InstallTemplateDialog.this);
            if(r == JFileChooser.CANCEL_OPTION || fc.getSelectedFile() == null)
                return;
            
            File zipFile = fc.getSelectedFile();            
            closePack();
            try
            {
                tmplPack = new ZipTemplatePack(zipFile);
                propertyPanel.setProperties(tmplPack.getPackProperties());
                installButton.setEnabled(true);
            }
            catch(IllegalArgumentException iae)
            {
                UIUtils.showWarning(InstallTemplateDialog.this, i18n.str("invalid_pack"),  //$NON-NLS-1$
                    i18n.str("invalid_pack_prompt")); //$NON-NLS-1$
                propertyPanel.setProperties(new Properties());
            }
            catch(Exception ex)
            {
                ex.printStackTrace(); 
                closePack();
            }            
        }        
    }
    
    private class InstallHandler implements ActionListener
    {

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            if(tmplPack == null)
                return;
            
            while(isTemplateWithSameNameInstalled(tmplPack))
            {                
                String title = tmplPack.getTitle();
                String newTitle = JOptionPane.showInputDialog(InstallTemplateDialog.this, 
                    i18n.str("rename_pack_prompt"),                     //$NON-NLS-1$
                    title);
                if(newTitle == null)
                    return;
                if(!newTitle.equals("")) //$NON-NLS-1$
                {
                    tmplPack.getPackProperties().put("title", newTitle); //$NON-NLS-1$
                }
            }
            
            String dirName = IOUtils.sanitize(tmplPack.getTitle());
            File destDir = IOUtils.createUniqueFile(new File(TBGlobals.getUserInstalledTemplatesDirectory(), dirName));
                        
            try
            {
                tmplPack.installPack(destDir); 
                UIUtils.showInfo(InstallTemplateDialog.this, i18n.str("pack_installed_prompt")); //$NON-NLS-1$
                dispose();
            }
            catch(Exception ex)
            {
                UIUtils.showError(InstallTemplateDialog.this, ex);
            }
            finally
            {
                closePack();
            }                     
        }        
    }

    /**
     * This method initializes closeButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getCloseButton()
    {
        if(closeButton == null)
        {
            closeButton = new JButton();
            closeButton.setText(i18n.str("close")); //$NON-NLS-1$
            closeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {                   
                    dispose();
                }
            });
        }
        return closeButton;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
