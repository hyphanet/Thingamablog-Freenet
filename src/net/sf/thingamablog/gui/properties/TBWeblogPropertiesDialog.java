/*
 * Created on Jun 30, 2004
 */
package net.sf.thingamablog.gui.properties;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.StandardDialog;

/**
 * @author Bob Tantlinger
 *
 * The properties dialog for a TBWeblog
 */
public class TBWeblogPropertiesDialog extends StandardDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties"); //$NON-NLS-1$
    
    private TBWeblog tbw;
	private Vector opts = new Vector();
	private JPanel optionPanel;
	private CardLayout optionLayout = new CardLayout();	
	
    /**
     * @param parent
     * @param title
     */
    public TBWeblogPropertiesDialog(Frame parent, String title, TBWeblog wb)
    {
        super(parent, title, BUTTONS_RIGHT, 5);
        init(wb);
        
    }

    /**
     * @param parent
     * @param title
     */
    public TBWeblogPropertiesDialog(Dialog parent, String title, TBWeblog wb)
    {
        super(parent, title, BUTTONS_RIGHT, 5);
        init(wb);
        
    }
    
    private void init(TBWeblog wb)
    {
		tbw = wb;	
		
		opts.add(new DialogPanel(new TBGeneralPanel(tbw), i18n.str("general")));		 //$NON-NLS-1$
		opts.add(new DialogPanel(new TBFrontPagePanel(tbw), i18n.str("front_page")));		 //$NON-NLS-1$
		opts.add(new DialogPanel(new TBArchivingPanel(tbw), i18n.str("archiving")));		 //$NON-NLS-1$
		opts.add(new DialogPanel(new TBCategoriesPanel(tbw), i18n.str("categories")));		 //$NON-NLS-1$
		opts.add(new DialogPanel(new AuthorsPanel(tbw), i18n.str("authors")));		 //$NON-NLS-1$
		opts.add(new DialogPanel(new TBCustomVariablesPanel(tbw), i18n.str("custom_tags")));		 //$NON-NLS-1$
        opts.add(new DialogPanel(new TBTemplatesPanel(tbw), i18n.str("templates"))); //$NON-NLS-1$
        opts.add(new DialogPanel(new TBEmailPanel(tbw), i18n.str("email"))); //$NON-NLS-1$
        opts.add(new DialogPanel(new XmlRpcPingPanel(tbw), i18n.str("pinging")));		 //$NON-NLS-1$
		opts.add(new DialogPanel(new TBPublishTransportPanel(tbw), i18n.str("publishing"))); //$NON-NLS-1$
		
		optionPanel = new JPanel(optionLayout);
		for(int i = 0; i < opts.size(); i++)
		{
			DialogPanel dp = (DialogPanel)opts.elementAt(i);
			optionPanel.add(dp, dp.getName());
		}
		
		final JList optList = new JList(opts);		
		optList.setCellRenderer(new OptionListCellRenderer());		
		JScrollPane scroller = new JScrollPane(optList);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setPreferredSize(new Dimension(110, optList.getPreferredSize().height));
		optList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					DialogPanel dp =  (DialogPanel)optList.getSelectedValue();
					optionLayout.show(optionPanel, dp.getName());
				}				
			}
		});
		
		JPanel mainPanel = new JPanel(new BorderLayout(5, 0));
		mainPanel.add(scroller, BorderLayout.WEST);
		mainPanel.add(optionPanel, BorderLayout.CENTER);
		setContentPane(mainPanel);		
		setSize(500, 565);
    }
    
    public boolean isValidData()
    {
    	//check each panel to make sure the entered data is correct
    	for(int i = 0; i < opts.size(); i++)
    	{
    		DialogPanel dp = (DialogPanel)opts.elementAt(i);
    		if(!dp.getContentPanel().isValidData())
    		{
				//switch to the panel with the problem
				optionLayout.show(optionPanel, dp.getName());
				return false;    
    		}
    	}
    		
    	//everything was ok so save the data
    	for(int i = 0; i < opts.size(); i++)
    	{
			DialogPanel dp = (DialogPanel)opts.elementAt(i);
			dp.getContentPanel().saveProperties();    		
    	}
    	
    	return true; //success    	
    }
    
    private class OptionListCellRenderer extends DefaultListCellRenderer
    {
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private ImageIcon genImg = UIUtils.getIcon(UIUtils.X32, "cogs.png"); //$NON-NLS-1$
		private ImageIcon fpImg = UIUtils.getIcon(UIUtils.MISC, "frontpage.png"); //$NON-NLS-1$
		private ImageIcon arcsImg = UIUtils.getIcon(UIUtils.X32, "arcs.png"); //$NON-NLS-1$
		private ImageIcon catsImg = UIUtils.getIcon(UIUtils.X32, "cats.png"); //$NON-NLS-1$
		private ImageIcon authImg = UIUtils.getIcon(UIUtils.X32, "users.png"); //$NON-NLS-1$
		private ImageIcon varsImg = UIUtils.getIcon(UIUtils.MISC, "variables.png"); //$NON-NLS-1$
		private ImageIcon pingImg = UIUtils.getIcon(UIUtils.MISC, "pinging.png"); //$NON-NLS-1$
		private ImageIcon pubImg = UIUtils.getIcon(UIUtils.X32, "upload.png"); //$NON-NLS-1$
        private ImageIcon emailImg = UIUtils.getIcon(UIUtils.X32, "email.png"); //$NON-NLS-1$
        private ImageIcon tmplImg = UIUtils.getIcon(UIUtils.MISC, "template24.gif"); //$NON-NLS-1$
        
		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			
			super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);
			
			if(value instanceof DialogPanel)
			{
				DialogPanel dp = (DialogPanel)value;
				if(dp.getContentPanel() instanceof TBGeneralPanel)
				{
					configLabel(genImg);
				}
				else if(dp.getContentPanel() instanceof TBFrontPagePanel)
				{
					configLabel(fpImg);
				}
				else if(dp.getContentPanel() instanceof TBArchivingPanel)
				{
					configLabel(arcsImg);
				}
				else if(dp.getContentPanel() instanceof TBCategoriesPanel)
				{
					configLabel(catsImg);
				}
				else if(dp.getContentPanel() instanceof AuthorsPanel)
				{
					configLabel(authImg);
				}
				else if(dp.getContentPanel() instanceof TBCustomVariablesPanel)
				{
					configLabel(varsImg);
				}
				else if(dp.getContentPanel() instanceof XmlRpcPingPanel)
				{
					configLabel(pingImg);
				}
				else if(dp.getContentPanel() instanceof TBPublishTransportPanel)
				{
					configLabel(pubImg);
				}
                else if(dp.getContentPanel() instanceof TBEmailPanel)
                {
                    configLabel(emailImg);
                }
                else if(dp.getContentPanel() instanceof TBTemplatesPanel)
                {
                    configLabel(tmplImg);
                }
				
			}
			
			setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
			return this;
			
		}
		
		private void configLabel(ImageIcon img)
		{
			setIcon(img);			
			setHorizontalTextPosition(CENTER);
			setVerticalTextPosition(BOTTOM);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			setIconTextGap(2);
			setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));			
		}
    }
    
	private class DialogPanel extends JPanel
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
        private PropertyPanel content;
		private String name;
		
		public DialogPanel(PropertyPanel cont, String label)
		{
			content = cont;
			name = label;
			
			setLayout(new BorderLayout());
			JLabel topLabel = new JLabel(label);
			topLabel.setOpaque(true);
			topLabel.setBackground(Color.GRAY);
			topLabel.setForeground(Color.WHITE);
			topLabel.setFont(new Font("Dialog", Font.BOLD + Font.ITALIC, 12)); //$NON-NLS-1$
			topLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
			JPanel spacer = new JPanel(new GridLayout(1, 1));
			spacer.add(topLabel, BorderLayout.CENTER);	
			
			Box box = Box.createVerticalBox();
			box.add(spacer);
			box.add(content);

			add(box, BorderLayout.CENTER);
		}
		
		public PropertyPanel getContentPanel()
		{
			return content;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String toString()
		{
			return name;
		}
	}
}
