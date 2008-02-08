/*
 * Created on Oct 31, 2007
 */
package net.sf.thingamablog.gui.app;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.io.IOUtils;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.TextEditPopupManager;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.TemplatePack;
import net.sf.thingamablog.blog.ZipExportableTemplatePack;
import net.sf.thingamablog.gui.StandardDialog;



/**
 * @author Bob Tantlinger
 *
 */
public class ExportTemplatePackDialog extends StandardDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app"); //$NON-NLS-1$
    private static final String TITLE = i18n.str("export_templates"); //$NON-NLS-1$
    
    private JLabel msgLabel = null;
    private JLabel backupNameLabel = null;
    private JTextField backupNameField = null;
    private JScrollPane scrollPane = null;
    private JTree fileTree = null;
    private JLabel locLabel = null;
    private JTextField locField = null;
    private JButton browseButton = null;
    private JLabel filesLabel = null;
    private CheckTreeManager checkTreeManager = null;
    
    private TemplatePropertiesPanel propertiesPanel;
    
    private File backupDir;    
    private TBWeblog weblog;
    
    /**
     * @param parent
     * @param title
     */
    public ExportTemplatePackDialog(Frame parent, TBWeblog blog)
    {
        super(parent, TITLE, BUTTONS_RIGHT, 5);
        weblog = blog;
        initialize();
    }

    /**
     * @param parent
     * @param title
     */
    public ExportTemplatePackDialog(Dialog parent, TBWeblog blog)
    {
        super(parent, TITLE, BUTTONS_RIGHT, 5);
        weblog = blog;
        initialize();
    } 
    
    public boolean isValidData()
    {
        if(backupDir == null || backupNameField.equals("")) //$NON-NLS-1$
        {
            UIUtils.showWarning(this, i18n.str("specifiy_template_export_location")); //$NON-NLS-1$
            return false;
        }     
        
        //get the web files...
        List paths = getAllCheckedPaths(checkTreeManager, fileTree);
        List fileList = new ArrayList();  
        if(paths != null)
        {     
            for(int i = 0; i < paths.size(); i++)
            {            
               TreePath p = (TreePath)paths.get(i);
               File f = (File)p.getLastPathComponent();               
               if(!f.isDirectory())
                   fileList.add(f);
            }          
        }
        
        File dir = weblog.getHomeDirectory();
        String fileName = backupNameField.getText();
        if(!fileName.toLowerCase().endsWith(".zip")) //$NON-NLS-1$
            fileName = fileName + ".zip"; //$NON-NLS-1$

        //check if the file already exists and prompt for overwrite
        File outFile = new File(backupDir, fileName);
        if(outFile.exists())
        {
            int yn = JOptionPane.showConfirmDialog(this,
                    i18n.str("overwrite") + "[" + outFile.getName() + "]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    i18n.str("confirm"), //$NON-NLS-1$
                    JOptionPane.YES_NO_OPTION);
        
            if(yn == JOptionPane.NO_OPTION)
                return false;                   
        }        
        
        File[] webFiles = (File[])fileList.toArray(new File[fileList.size()]);
        Properties props = propertiesPanel.getProperties();
        
        //TODO this shoud be done in its own thread...
        try
        {
            TemplatePack zipPack = new ZipExportableTemplatePack(dir, fileName, webFiles);
            zipPack.getPackProperties().putAll(props);
            zipPack.installPack(backupDir);
            UIUtils.showInfo(this, i18n.str("exported_templates_prompt")); //$NON-NLS-1$
        }
        /*catch(IllegalArgumentException e)
        {
            UIUtils.showError(this, e);
        }*/
        catch(Exception e)
        {
            UIUtils.showError(this, e);
        }
        
        return true;
    }
    
    /**
     * This method initializes this
     * 
     */
    private void initialize() 
    {
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        gridBagConstraints4.gridx = 1;
        gridBagConstraints4.anchor = GridBagConstraints.NORTHEAST;
        gridBagConstraints4.insets = new Insets(0, 0, 0, 5);
        gridBagConstraints4.gridy = 3;
        filesLabel = new JLabel();
        filesLabel.setText(i18n.str("web_files")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
        gridBagConstraints31.gridx = 3;
        gridBagConstraints31.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints31.gridy = 1;
        GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
        gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints21.gridy = 1;
        gridBagConstraints21.weightx = 1.0;
        gridBagConstraints21.anchor = GridBagConstraints.WEST;
        gridBagConstraints21.insets = new Insets(0, 0, 5, 3);
        gridBagConstraints21.gridx = 2;
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.gridx = 1;
        gridBagConstraints11.anchor = GridBagConstraints.EAST;
        gridBagConstraints11.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints11.gridy = 1;
        locLabel = new JLabel();
        locLabel.setText(i18n.str("create_in_folder")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.fill = GridBagConstraints.BOTH;
        gridBagConstraints3.gridy = 3;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        gridBagConstraints3.gridwidth = 2;
        gridBagConstraints3.gridx = 2;
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.gridy = 2;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.anchor = GridBagConstraints.WEST;
        gridBagConstraints2.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.gridx = 2;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.anchor = GridBagConstraints.EAST;
        gridBagConstraints1.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints1.gridy = 2;
        backupNameLabel = new JLabel();
        backupNameLabel.setText(i18n.str("file_name")); //$NON-NLS-1$
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new Insets(0, 0, 10, 0);
        gridBagConstraints.gridy = 0;
        msgLabel = new JLabel();
        msgLabel.setText("<html>" + i18n.str("export_templates_prompt") + "</html>"); //$NON-NLS-1$
        
        JPanel exportPanel = new JPanel();
        exportPanel.setLayout(new GridBagLayout());
        exportPanel.setSize(new Dimension(497, 251));
        exportPanel.add(msgLabel, gridBagConstraints);
        exportPanel.add(backupNameLabel, gridBagConstraints1);
        exportPanel.add(getBackupNameField(), gridBagConstraints2);
        exportPanel.add(getScrollPane(), gridBagConstraints3);
        exportPanel.add(locLabel, gridBagConstraints11);
        exportPanel.add(getLocField(), gridBagConstraints21);
        exportPanel.add(getBrowseButton(), gridBagConstraints31);
        exportPanel.add(filesLabel, gridBagConstraints4);
        exportPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        checkTreeManager = new CheckTreeManager(getFileTree());
        
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.addTab(i18n.str("export"), exportPanel); //$NON-NLS-1$
        
        propertiesPanel = new TemplatePropertiesPanel();
        tabs.addTab(i18n.str("properties"), propertiesPanel); //$NON-NLS-1$
        
        this.setContentPane(tabs); 
        setSize(425, 340);
        setResizable(false);
        
        Properties p = new Properties();
        InputStream in = null;
        try
        {
            in = new FileInputStream(new File(weblog.getHomeDirectory(), "pack.properties")); //$NON-NLS-1$
            p.load(in);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            IOUtils.close(in);
        }
        
        p.setProperty("created", new Date().getTime()+""); //$NON-NLS-1$ //$NON-NLS-2$
        propertiesPanel.setProperties(p); 
        updateTree();
    }
    
        
    /**
     * This method initializes backupNameField  
     *  
     * @return javax.swing.JTextField   
     */
    private JTextField getBackupNameField()
    {
        if(backupNameField == null)
        {
            backupNameField = new JTextField();
            String name = IOUtils.sanitize(weblog.getTitle() + "-"); //$NON-NLS-1$
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
            name += "templates-" + sdf.format(new Date()) + ".zip"; //$NON-NLS-1$ //$NON-NLS-2$
            backupNameField.setText(name);
            TextEditPopupManager.getInstance().registerJTextComponent(backupNameField);
        }
        return backupNameField;
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
            scrollPane.setViewportView(getFileTree());
        }
        return scrollPane;
    }

    /**
     * This method initializes fileTree 
     *  
     * @return javax.swing.JTree    
     */
    private JTree getFileTree()
    {
        if(fileTree == null)
        {
            //String dir = "C:\\Documents and Settings\\Owner\\My Documents\\weblogs\\1092845426875\\web";
            TreeModel model = new FileTreeModel(new File("web")); //$NON-NLS-1$
            fileTree = new JTree(model);
            fileTree.setCellRenderer(new FileTreeCellRenderer());            
        }
        return fileTree;
    }

    /**
     * This method initializes locField 
     *  
     * @return javax.swing.JTextField   
     */
    private JTextField getLocField()
    {
        if(locField == null)
        {
            locField = new JTextField();
            locField.setEditable(false);
            TextEditPopupManager.getInstance().registerJTextComponent(locField);
        }
        return locField;
    }

    /**
     * This method initializes browseButton 
     *  
     * @return javax.swing.JButton  
     */
    private JButton getBrowseButton()
    {
        if(browseButton == null)
        {
            browseButton = new JButton();
            browseButton.setMargin(new Insets(0, 0, 0, 0));
            browseButton.setText(""); //$NON-NLS-1$
            browseButton.setToolTipText(i18n.str("browse_")); //$NON-NLS-1$
            browseButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            browseButton.setIcon(UIUtils.getIcon(UIUtils.X16, "import.png")); //$NON-NLS-1$
            browseButton.setPreferredSize(new Dimension(21, 21));
            browseButton.addActionListener(new BrowseHandler());
        }
        return browseButton;
    }
    
    private void updateTree()
    {
        //String userxml = (String)values.get(SelectBlogPanel.USERXML_PATH_PROPERTY);
        //String bkey = (String)values.get(SelectBlogPanel.BLOGKEY_PROPERTY);
        //if(userxml != null && bkey != null)
        {
            //File f = new File(userxml);
            File webRoot = weblog.getHomeDirectory();//new File(f.getParentFile(), bkey);
            webRoot = new File(webRoot, "web"); //$NON-NLS-1$
            
            //System.err.println(webRoot + " " + webRoot.exists());
            
            FileTreeModel m = (FileTreeModel)fileTree.getModel();
            if(!m.getRoot().equals(webRoot))
            {
                //got to unselect the paths in the selecton model, otherwise
                //they stay selected and are not part of the new TreeModel
                List paths = getAllCheckedPaths(checkTreeManager, fileTree);
                for(int i = 0; i < paths.size(); i++)
                {
                    TreePath p = (TreePath)paths.get(i);
                    checkTreeManager.getSelectionModel().removeSelectionPath(p);
                }
                fileTree.setModel(new FileTreeModel(webRoot));                
            }
        }
    }
    
    private void addChildPaths(TreePath path, TreeModel model, List result)
    {
        Object item = path.getLastPathComponent();
        int childCount = model.getChildCount(item);
        for(int i = 0; i<childCount; i++)
            result.add(path.pathByAddingChild(model.getChild(item, i)));
    }

    private ArrayList getDescendants(TreePath paths[] , TreeModel model)
    {
        ArrayList result = new ArrayList();
        Stack pending = new Stack();
        pending.addAll(Arrays.asList(paths));
        while(!pending.isEmpty())
        {
            TreePath path = (TreePath)pending.pop();
            addChildPaths(path, model, pending);
            result.add(path);
        }
        return result;
    }


    private ArrayList getAllCheckedPaths(CheckTreeManager manager, JTree tree)
    {
        TreePath p[] = manager.getSelectionModel().getSelectionPaths();
        if(p == null)
            return new ArrayList();
        return getDescendants(p, tree.getModel());
    }
    
    private class BrowseHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {           
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle(i18n.str("select_backup_folder"));  //$NON-NLS-1$
            String lastDir = TBGlobals.getProperty("last_backup_dir"); //$NON-NLS-1$
            if(lastDir != null)
            {
                File f = new File(lastDir);
                if(f.exists())
                {
                    fc.setSelectedFile(f);                   
                }
            }
            
            int r = fc.showDialog(ExportTemplatePackDialog.this, "OK"); //$NON-NLS-1$
            
            //int r = fc.showOpenDialog(BackUpPanel.this);
            if(r == JFileChooser.CANCEL_OPTION || fc.getSelectedFile() == null)
                return;
            
            backupDir = fc.getSelectedFile();
            TBGlobals.putProperty("last_backup_dir", backupDir.getAbsolutePath()); //$NON-NLS-1$
            locField.setText(backupDir.getAbsolutePath());
        }
    }
}
