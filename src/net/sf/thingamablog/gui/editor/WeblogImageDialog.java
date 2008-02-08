/*
 * Created on Nov 5, 2007
 */
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.io.IOUtils;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.ImageDialog;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.gui.CustomFileFilter;
import net.sf.thingamablog.gui.ImagePanel;
import net.sf.thingamablog.gui.ImageViewerDialog;
import net.sf.thingamablog.gui.LabelledItemPanel;





/**
 * @author Bob Tantlinger
 *
 */
public class WeblogImageDialog extends ImageDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.editor"); //$NON-NLS-1$
    
    private final String NO_IMG = " "; //$NON-NLS-1$
    
    private Weblog blog;
    private JComboBox imagesCombo = new JComboBox();
    
    private JButton browseButton, viewButton;
    /**
     * @param parent
     */
    public WeblogImageDialog(Frame parent, Weblog blog)
    {
        super(parent);
        initUI(blog);
    }

    /**
     * @param parent
     */
    public WeblogImageDialog(Dialog parent, Weblog blog)
    {
        super(parent); 
        initUI(blog);
    }
    
    private void initUI(Weblog b)
    {
        blog = b;
        
        Insets insets = new Insets(2, 2, 2, 2);
        browseButton = new JButton(UIUtils.getIcon(UIUtils.X16, "export.png")); //$NON-NLS-1$
        browseButton.setToolTipText(i18n.str("import")); //$NON-NLS-1$
        browseButton.setMargin(insets); 
        browseButton.addActionListener(new BrowseButtonHandler());
        
        viewButton = new JButton(UIUtils.getIcon(UIUtils.X16, "image.png")); //$NON-NLS-1$
        viewButton.setToolTipText(i18n.str("view")); //$NON-NLS-1$
        viewButton.setMargin(insets); 
        viewButton.addActionListener(new ViewButtonHandler());
        viewButton.setEnabled(false);
        
        refreshImageCombo();        
        imagesCombo.setRenderer(new ImageComboRenderer());
        imagesCombo.addItemListener(new ComboListener());
        imagesCombo.setSelectedItem(NO_IMG);
        
        JPanel locPanel = new JPanel(new BorderLayout(5, 5));
        locPanel.add(imagesCombo, BorderLayout.CENTER);
        JPanel bPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        bPanel.add(viewButton);
        bPanel.add(browseButton);
        locPanel.add(bPanel, BorderLayout.EAST);
        
        LabelledItemPanel lip = new LabelledItemPanel();
        lip.addItem(i18n.str("image"), locPanel); //$NON-NLS-1$
        
        JPanel contentPane = new JPanel(new BorderLayout(5, 5));        
        Container old = getContentPane();
        
        contentPane.add(lip, BorderLayout.NORTH);
        contentPane.add(old, BorderLayout.CENTER);
        setContentPane(contentPane);
        
        setSize(getWidth(), 390);
    }
    
    private File getImageDirectory()
    {
        return EntryImageUtils.getImageDirectory(blog);
    }
    
    private File[] loadImageFiles()
    {        
        FileFilter filter = new FileFilter()
        {
            public boolean accept(File f)
            {
                if(f.isFile())
                {
                    String n = f.getName().toLowerCase();
                    return n.endsWith(".gif") || n.endsWith(".png") ||  //$NON-NLS-1$ //$NON-NLS-2$
                        n.endsWith(".jpg") || n.endsWith(".jpeg"); //$NON-NLS-1$ //$NON-NLS-2$
                }               
                return false;                   
            }
        };
        
        File dir = getImageDirectory();
        File f[] = dir.listFiles(filter);
        if(f == null)
            f = new File[0];
        return f; 
    }
    
    private void refreshImageCombo()
    {
        imagesCombo.removeAllItems();
        File f[] = loadImageFiles();
        imagesCombo.addItem(NO_IMG);
        for(int i = 0; i < f.length; i++)
            imagesCombo.addItem(f[i]);
    }
    
    private class ViewButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if(imagesCombo.getSelectedItem() != NO_IMG)
            {
                File f = (File)imagesCombo.getSelectedItem();
                ImageViewerDialog dlg = new ImageViewerDialog(WeblogImageDialog.this, f);
                dlg.setSize(300, 300);
                dlg.setLocationRelativeTo(WeblogImageDialog.this);
                dlg.setModal(true);
                dlg.setVisible(true);
            }
        }
    }
    
    private class BrowseButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            ImageFileChooser ifc = new ImageFileChooser();
            File initialDir = null;
            if(TBGlobals.getProperty("LAST_IMG_DIR") != null)             //$NON-NLS-1$
                initialDir = new File(TBGlobals.getProperty("LAST_IMG_DIR")); //$NON-NLS-1$
            
            if(initialDir != null && initialDir.isDirectory());
                ifc.setCurrentDirectory(initialDir);
            
            ifc.showOpenDialog(WeblogImageDialog.this);
            
            
            File imageFile = ifc.getSelectedFile();
            if(imageFile == null || imageFile.isDirectory())
            {
                imageFile = null;
                return;
            }
            
            File dir = getImageDirectory();
            //TODO ask user what to do if dest file exists...
            File dest = IOUtils.createUniqueFile(new File(dir, imageFile.getName()));
            if(!dest.exists())
                dest.getParentFile().mkdirs();
            try 
            {
                IOUtils.copy(imageFile, dest, false);                        
                
            } 
            catch(IOException ioe) 
            {
                JOptionPane.showMessageDialog(WeblogImageDialog.this, 
                    i18n.str("error_importing_image"), i18n.str("error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
                ioe.printStackTrace();
            }
            
            TBGlobals.putProperty("LAST_IMG_DIR", imageFile.getParent()); //$NON-NLS-1$
                        
            refreshImageCombo();
            imagesCombo.setSelectedItem(dest);
        }
    }
    
    private class ComboListener implements ItemListener
    {
        public void itemStateChanged(ItemEvent e)
        {
            //String w = "";
            //String h = "";
            Map attrs = getImageAttributes();
            
            if(imagesCombo.getSelectedItem() == NO_IMG)
            {
                //imgLocField.setEditable(true);
                //imgLocField.setText("http://");
                attrs.put("src", "http://"); //$NON-NLS-1$ //$NON-NLS-2$
                viewButton.setEnabled(false);
            }
            else
            {               
                //imgLocField.setEditable(false);
                try
                {
                    File f = (File)imagesCombo.getSelectedItem();
                    attrs.put("src", f.toURL()); //$NON-NLS-1$
                    //imgLocField.setText(f.toURL().toExternalForm());
                    ImageIcon img = new ImageIcon(f.getAbsolutePath());
                    attrs.put("width", img.getIconWidth() + ""); //$NON-NLS-1$ //$NON-NLS-2$
                    attrs.put("height", img.getIconHeight() + "");                     //$NON-NLS-1$ //$NON-NLS-2$
                    viewButton.setEnabled(true);
                }
                catch(Exception ex){}
            }
            
            setImageAttributes(attrs);
            //width.setText(w); 
            //height.setText(h);  
        }       
    }
        
    private class ImageComboRenderer extends DefaultListCellRenderer
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(
            JList l, Object v, int i, boolean isSel, boolean hasFocus)
        {
            String name = v.toString();
            try
            {
                File f = (File)v;
                if(f != null && f.isFile())
                    name = f.getName();
            }
            catch(ClassCastException ex){}
            
            return super.getListCellRendererComponent(l, name, i, isSel, hasFocus);
        }   
    }
    
    private class ImageFileChooser extends JFileChooser implements PropertyChangeListener
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private JCheckBox previewCb;
        private ImagePanel ipanel = new ImagePanel();
        
        public ImageFileChooser()
        {
            super();
            JPanel previewPanel = new JPanel(new BorderLayout());       
            previewPanel.add(ipanel, BorderLayout.CENTER);          
            previewCb = new JCheckBox(i18n.str("show_preview")); //$NON-NLS-1$
            previewCb.setSelected(true);            
            previewPanel.add(previewCb, BorderLayout.SOUTH);
            previewPanel.setPreferredSize(new Dimension(150, 100));
            previewPanel.setBorder(new EtchedBorder());
            
            
            CustomFileFilter cff = new CustomFileFilter();
            cff.addExtension("gif"); //$NON-NLS-1$
            cff.addExtension("jpg"); //$NON-NLS-1$
            cff.addExtension("jpeg"); //$NON-NLS-1$
            cff.addExtension("png"); //$NON-NLS-1$
            
            setFileSelectionMode(FILES_ONLY);
            setFileFilter(cff);
            setAccessory(previewPanel);
            addPropertyChangeListener(this);
        }
        
        public void propertyChange(PropertyChangeEvent evt) 
        {
            String prop = evt.getPropertyName();
            if(prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) 
            {
                File selected = getSelectedFile();
                if(selected != null && !selected.isDirectory() && previewCb.isSelected()) 
                {               
                    ImageIcon ii = new ImageIcon(selected.getAbsolutePath());
                    ipanel.setImage(ii.getImage());     
                }
                else
                    ipanel.setImage(null);              
             }
        }
    }
}
