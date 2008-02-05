package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.CustomFileFilter;
import net.sf.thingamablog.gui.ImagePanel;
import net.sf.thingamablog.gui.ImageViewerDialog;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;


/**
 * @author Bob Tantlinger
  
 */
public class TBImageDialog extends HTMLOptionDialog
{
	private final static String ALIGNMENTS[] =
	{
		"", "left", "right", "middle", "absmiddle", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		"top", "texttop", "bottom", "center", "baseline", "absbottom" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	};
	
	private final String NO_IMG = " ";
    
    private JTextField imgLocField = new JTextField(30);
    private JTextField altTextField = new JTextField(30);
    private JTextField urlField = new JTextField(30);
    
    private int tfSize = 4;
    private JTextField width = new JTextField(tfSize);
	private JTextField height = new JTextField(tfSize);
	private JTextField border = new JTextField(tfSize);
	private JTextField vSpace = new JTextField(tfSize);
	private JTextField hSpace = new JTextField(tfSize);
	
	private JComboBox alignCombo = new JComboBox(ALIGNMENTS);
	private JComboBox imagesCombo = new JComboBox();
	
	private JButton browseButton, viewButton;
	private File imageDir;
	
    /**
     * @param parent
     * @param title
     */
    public TBImageDialog(Frame parent, File dir)
    {
        super(parent, 
            Messages.getString("ImagePublisherDialog.Image"), 
            Utils.createIcon(TBGlobals.RESOURCES + "imgbig.png")); //$NON-NLS-1$
        imageDir = dir;
        if((!imageDir.exists()) || imageDir.isFile())
        {
            imageDir.mkdirs();
        }
       	init();
    }

    public String getSrc()
    {
        return imgLocField.getText();
    }
    
    public void setSrc(String s)
    {        
        imgLocField.setText(s);
    }
    
    public void setAltText(String s)
    {
        altTextField.setText(s);
    }
    
    public void setLink(String s)
    {
        urlField.setText(s);
    }
    
    public void setBorder(String s)
    {
        border.setText(s);
    }
    
    public void setVSpace(String s)
    {
        vSpace.setText(s);
    }
    
    public void setHSpace(String s)
    {
        hSpace.setText(s);
    }
    
    public void setWidth(String s)
    {
        width.setText(s);
    }
    
    public void setHeight(String s)
    {
        height.setText(s);
    }
    
    public void setAlignment(String s)
    {
        alignCombo.setSelectedItem(s);
    }
    
    private void init()
    {   	
        TextEditPopupManager pm = new TextEditPopupManager();
        pm.addJTextComponent(imgLocField);
        pm.addJTextComponent(altTextField);
        pm.addJTextComponent(urlField);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        Insets insets = new Insets(2, 2, 2, 2);
		browseButton = new JButton(Utils.createIcon(TBGlobals.RESOURCES + "open16.gif")); //$NON-NLS-1$
		browseButton.setToolTipText(Messages.getString("ThingamablogFrame.Import"));
		browseButton.setMargin(insets); 
		browseButton.addActionListener(new BrowseButtonHandler());
		
		viewButton = new JButton(Utils.createIcon(TBGlobals.RESOURCES + "image.png"));
		viewButton.setToolTipText(Messages.getString("ThingamablogFrame.View"));
		viewButton.setMargin(insets); 
		viewButton.addActionListener(new ViewButtonHandler());
		
		refreshImageCombo();		
		imagesCombo.setRenderer(new ImageComboRenderer());
		imagesCombo.addActionListener(new ComboListener());
		imagesCombo.setSelectedItem(NO_IMG);
		
		JPanel locPanel = new JPanel(new BorderLayout(5, 5));
		locPanel.add(imagesCombo, BorderLayout.CENTER);
		JPanel bPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		bPanel.add(viewButton);
		bPanel.add(browseButton);
		locPanel.add(bPanel, BorderLayout.EAST);
		
		LabelledItemPanel lip = new LabelledItemPanel();
		lip.addItem(Messages.getString("ImagePublisherDialog.Image"), locPanel);
		lip.addItem(Messages.getString("ImagePublisherDialog.Image") + " URL", imgLocField); //$NON-NLS-1$
		
		lip.addItem(Messages.getString("ImagePublisherDialog.Alt._Text"), altTextField); //$NON-NLS-1$
		lip.addItem("Hyperlink", urlField);
		lip.setBorder(new TitledBorder(Messages.getString("ImagePublisherDialog.Image"))); //$NON-NLS-1$    	
    	
    	JPanel attribs = new JPanel(new GridLayout(3, 2));
       	attribs.add(control(Messages.getString("ImagePublisherDialog.Width"), width)); //$NON-NLS-1$
    	attribs.add(control(Messages.getString("ImagePublisherDialog.Height"), height)); //$NON-NLS-1$
    	
		attribs.add(control(Messages.getString("ImagePublisherDialog.VSpace"), vSpace)); //$NON-NLS-1$
		attribs.add(control(Messages.getString("ImagePublisherDialog.HSpace"), hSpace)); //$NON-NLS-1$

		attribs.add(control(Messages.getString("ImagePublisherDialog.Border"), border)); //$NON-NLS-1$
		attribs.add(control(Messages.getString("ImagePublisherDialog.Align"), alignCombo)); //$NON-NLS-1$
		
		JPanel bottomPanel = spacer(attribs);
		bottomPanel.setBorder(new TitledBorder(Messages.getString("ImagePublisherDialog.Attributes"))); //$NON-NLS-1$
		
		JPanel controlPanel = new JPanel(new BorderLayout());
		controlPanel.add(lip, BorderLayout.NORTH);
		controlPanel.add(bottomPanel, BorderLayout.CENTER);		
        
		mainPanel.add(controlPanel, BorderLayout.CENTER);    	
    	setContentPanel(mainPanel);
    	pack();
    	setResizable(false);   
    }
    
    private void refreshImageCombo()
    {
		imagesCombo.removeAllItems();
        File f[] = loadImageFiles();
		imagesCombo.addItem(NO_IMG);
		for(int i = 0; i < f.length; i++)
		    imagesCombo.addItem(f[i]);
    }
    
    private File getImageDirectory()
    {
        return imageDir;        
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
				    return n.endsWith(".gif") || n.endsWith(".png") || 
				    	n.endsWith(".jpg") || n.endsWith(".jpeg");
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
    
    private JPanel spacer(Component c)
    {
    	JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	p.add(c);
    	return p;
    }
    
    private JPanel control(String text, Component c)
    {
    	JPanel ctrl = new JPanel(new BorderLayout(5, 5));
    	ctrl.add(rightLabel(text), BorderLayout.WEST);
    	ctrl.add(spacer(c), BorderLayout.CENTER);
    	return ctrl;
    }
    
    private JLabel rightLabel(String text)
    {
    	JLabel l = new JLabel(text);
    	l.setHorizontalAlignment(JLabel.RIGHT);
    	l.setPreferredSize(new Dimension(50, l.getHeight()));
    	return l;
    }
    
	public String getHTML()
	{
		String image = "<img"; //$NON-NLS-1$
		String src = imgLocField.getText();
		String align = alignCombo.getSelectedItem().toString();
		String alt = altTextField.getText();
		String w = width.getText();
		String h = height.getText();
		String brdr = border.getText();
		String vs = vSpace.getText();
		String hs = hSpace.getText();		
		
		if(src != null && !src.equals("")) //$NON-NLS-1$
			image += " src=\"" + src + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(align != null && !align.equals("")) //$NON-NLS-1$
			image += " align=\"" + align + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(alt != null && !alt.equals("")) //$NON-NLS-1$
			image += " alt=\"" + alt + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(w != null && !w.equals("")) //$NON-NLS-1$
			image += " width=\"" + w + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(h != null && !h.equals("")) //$NON-NLS-1$
			image += " height=\"" + h + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(brdr != null && !brdr.equals("")) //$NON-NLS-1$
			image += " border=\"" + brdr + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(vs != null && !vs.equals("")) //$NON-NLS-1$
			image += " vspace=\"" + vs + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		if(hs != null && !hs.equals("")) //$NON-NLS-1$
			image += " hspace=\"" + hs + "\"";   //$NON-NLS-1$ //$NON-NLS-2$
		
		image += ">"; //$NON-NLS-1$
		
		if(urlField.getText().length() > 3)
		   image = "<a href=\"" + urlField.getText() + "\">" + image + "</a>"; 
		
		return image;
	}
	
	public boolean isValidData()
	{
		if(imgLocField.getText().equals("")) //$NON-NLS-1$
		{
			JOptionPane.showMessageDialog(this, Messages.getString("ImagePublisherDialog.enter_image_prompt"), //$NON-NLS-1$
				Messages.getString("ImagePublisherDialog.Warning"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
			return false;	
		}
		
		return true;	
	}
    
	private class ViewButtonHandler implements ActionListener
	{
	    public void actionPerformed(ActionEvent e)
	    {
	        if(imagesCombo.getSelectedItem() != NO_IMG)
	        {
	            File f = (File)imagesCombo.getSelectedItem();
	            ImageViewerDialog dlg = new ImageViewerDialog(TBImageDialog.this, f);
	            dlg.setSize(300, 300);
	            dlg.setLocationRelativeTo(TBImageDialog.this);
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
            if(TBGlobals.getProperty("LAST_IMG_DIR") != null)            
                initialDir = new File(TBGlobals.getProperty("LAST_IMG_DIR"));
            
            if(initialDir != null && initialDir.isDirectory());
                ifc.setCurrentDirectory(initialDir);
            
    		ifc.showOpenDialog(TBImageDialog.this);
            
    		
    		File imageFile = ifc.getSelectedFile();
    		if(imageFile == null || imageFile.isDirectory())
    		{
    			imageFile = null;
    			return;
    		}
    		
    		File dir = getImageDirectory();
    		File dest = new File(dir, imageFile.getName());
    		try 
    		{
    			// Create channel on the source
    			FileChannel srcChannel = new FileInputStream(imageFile).getChannel();   			
    			
    			// Create channel on the destination
    			FileChannel dstChannel = new FileOutputStream(dest).getChannel();
        
    			// Copy file contents from source to destination
    			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        
    			// Close the channels
    			srcChannel.close();
    			dstChannel.close();
                TBGlobals.putProperty("LAST_IMG_DIR", imageFile.getParent());
    			
    		} 
    		catch(IOException ioe) 
    		{
    		    JOptionPane.showMessageDialog(TBImageDialog.this, 
    		        "Error importing image", "Error", JOptionPane.ERROR_MESSAGE);
    		    ioe.printStackTrace();
    		}
    		
    		
    		ImageIcon img = new ImageIcon(dest.getAbsolutePath());
    		width.setText(img.getIconWidth() + ""); //$NON-NLS-1$
    		height.setText(img.getIconHeight() + "");	 //$NON-NLS-1$
    		
    		refreshImageCombo();
    		imagesCombo.setSelectedItem(dest);
    	}
    }
    
    private class ComboListener implements ActionListener
	{
	    public void actionPerformed(ActionEvent e)
	    {
	        String w = "";
	        String h = "";
	        if(imagesCombo.getSelectedItem() == NO_IMG)
	        {
	            imgLocField.setEditable(true);
	            imgLocField.setText("http://");
	            viewButton.setEnabled(false);
	        }
	        else
	        {	            
	            imgLocField.setEditable(false);
	            viewButton.setEnabled(true);
	            
	            try
	            {
	                File f = (File)imagesCombo.getSelectedItem();
	                imgLocField.setText(f.toURL().toExternalForm());
	        		ImageIcon img = new ImageIcon(f.getAbsolutePath());
	        		w = img.getIconWidth() + ""; 
	        		h = img.getIconHeight() + "";                
	            }
	            catch(Exception ex){}
	        }
	        
    		width.setText(w); 
    		height.setText(h);	
	    }
	}
        
    private class ImageComboRenderer extends DefaultListCellRenderer
    {
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
		private JCheckBox previewCb;
		private ImagePanel ipanel = new ImagePanel();
    	
		public ImageFileChooser()
		{
			super();
			JPanel previewPanel = new JPanel(new BorderLayout());		
			previewPanel.add(ipanel, BorderLayout.CENTER);			
			previewCb = new JCheckBox(Messages.getString("ImagePublisherDialog.Show_preview")); //$NON-NLS-1$
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

