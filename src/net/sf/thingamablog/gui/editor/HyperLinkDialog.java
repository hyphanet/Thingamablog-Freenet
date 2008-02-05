/*
 * Created on Mar 4, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.Messages;



public class HyperLinkDialog extends HTMLOptionDialog
{
    private static final String RES = TBGlobals.RESOURCES;   
    
    private static final String NEW_WIN = "New Window"; //$NON-NLS-1$
	private static final String SAME_WIN = "Same Window"; //$NON-NLS-1$
	private static final String SAME_FRAME = "Same Frame"; //$NON-NLS-1$
	private static final String TARGETS[] =
	{NEW_WIN, SAME_WIN, SAME_FRAME};
	private final static String[] ALIGNS = {"", "left", "center", "right"};
	
	private JComboBox targetCombo;
	private JCheckBox targetCb;
    private JTextField txtURL = new JTextField("http://", 25);    
    private JTextField txtName = new JTextField(25);    
    private JTextField txtTitle = new JTextField(25);    
    private JTextField txtDesc = new JTextField(25);    
    
    public HyperLinkDialog(Frame parent)
    {
        super(parent, Messages.getString("LinkDialog.Hyperlink"), 
            Utils.createIcon(RES + "linkbig.png"));
        
        LabelledItemPanel lip = new LabelledItemPanel();        
        JPanel main = new JPanel(new BorderLayout());
        
        TextEditPopupManager pm = new TextEditPopupManager();
        pm.addJTextComponent(txtURL);
        pm.addJTextComponent(txtName);
        pm.addJTextComponent(txtTitle);
        pm.addJTextComponent(txtDesc);
        
        lip.addItem(Messages.getString("LinkDialog.URL"), txtURL);
        lip.addItem(Messages.getString("LinkDialog.Name"), txtName);
        lip.addItem(Messages.getString("LinkDialog.Title"), txtTitle);
        lip.addItem(Messages.getString("LinkDialog.Text"), txtDesc);
        
		targetCb = new JCheckBox(Messages.getString("LinkDialog.Open_link_in")); //$NON-NLS-1$
		targetCombo = new JComboBox(TARGETS);
		targetCombo.setEnabled(targetCb.isSelected());
		JPanel targetPanel = new JPanel(new BorderLayout(5, 5));
		targetPanel.add(targetCb, BorderLayout.WEST);
		targetPanel.add(targetCombo, BorderLayout.CENTER);
		targetPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		targetCb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateTargetState();	
			}
		});
		
		main.add(lip, BorderLayout.CENTER);
		main.add(targetPanel, BorderLayout.SOUTH);
		setContentPanel(main);
		pack();
		setResizable(false);
    }
    
    private void updateTargetState()
    {
        targetCombo.setEnabled(targetCb.isSelected());
    }
    
    public String getLinkURL()
    {
        return txtURL.getText();
    }
    
    public String getLinkName()
    {
        return txtName.getText();
    }
    
    public String getLinkTitle()
    {
        return txtTitle.getText();
    }
    
    public String getLinkDescription()
    {
        return txtDesc.getText();
    }
    
    public void setLinkURL(String s)
    {
        txtURL.setText(s);
    }
    
    public void setLinkName(String s)
    {
        txtName.setText(s);
    }
    
    public void setLinkTitle(String s)
    {
        txtTitle.setText(s);
    }
    
    public void setLinkDescription(String s)
    {
        txtDesc.setText(s);
    }
    
    public void setTarget(String s)
    {
        if(s == null)
            return;
        
        if(s.equalsIgnoreCase("_top"))
        {
            targetCb.setSelected(true);
            targetCombo.setSelectedItem(SAME_WIN);
        }
        else if(s.equalsIgnoreCase("_blank"))
        {
            targetCb.setSelected(true);
            targetCombo.setSelectedItem(NEW_WIN);
        }
        else if(s.equalsIgnoreCase("_self"))
        {
            targetCb.setSelected(true);
            targetCombo.setSelectedItem(SAME_FRAME);
        }
        else
        {
            targetCb.setSelected(false);
        }
        
        updateTargetState();
    }
    
    public String getHTML()
    {
  		String aTag = "<a";
  		if (txtURL.getText().length() > 0)
  			aTag += " href=\"" + txtURL.getText() + "\"";
  		if (txtName.getText().length() > 0)
  			aTag += " name=\"" + txtName.getText() + "\"";
  		if (txtTitle.getText().length() > 0)
  			aTag += " title=\"" + txtTitle.getText() + "\"";

  		if(targetCb.isSelected())
  		{
  			aTag += " target=\""; //$NON-NLS-1$
  			if(targetCombo.getSelectedItem() == NEW_WIN)
  				aTag += "_blank"; //$NON-NLS-1$
  			else if(targetCombo.getSelectedItem() == SAME_WIN)
  				aTag += "_top"; //$NON-NLS-1$
  			else if(targetCombo.getSelectedItem() == SAME_FRAME)
  				aTag += "_self"; //$NON-NLS-1$
  			
  			aTag += "\""; //$NON-NLS-1$
  		}
  		aTag += ">" + txtDesc.getText() + "</a>";
  		
  		return aTag;
    }
}
