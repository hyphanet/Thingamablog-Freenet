/*
 * Created on Mar 4, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;

/**

 */
public class TableDialog extends HTMLOptionDialog
{
    private static final String RES = TBGlobals.RESOURCES;
	private final String[] aligns = {"", "left", "center", "right"};
	private final String[] valigns = {"", "top", "center", "bottom"};
    
	private JButton bgColorB = new JButton();
	private JComboBox vAlignCB = new JComboBox(valigns);
	private JComboBox alignCB = new JComboBox(aligns);
    private JTextField bgcolorField = new JTextField();
	private JTextField heightField = new JTextField();
    private JTextField widthField = new JTextField();
	private JSpinner columns = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
	private JSpinner rows = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
	private JSpinner cellpadding = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
	private JSpinner cellspacing = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
	private JSpinner border = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
   
    public TableDialog(Frame parent)
    {
        super(parent, Messages.getString("TableDialog.Table"), Utils.createIcon(RES + "tablebig.png"));
        
    	JLabel lblPadding = new JLabel();
    	JLabel lblSpacing = new JLabel();
    	JLabel lblColumns = new JLabel();
    	JLabel lblRows = new JLabel();    	
    	JLabel lblOutline = new JLabel();    	
    	JLabel lblVertOutline = new JLabel();    	
    	JLabel lblFillColor = new JLabel();    	
    	JLabel lblBorder = new JLabel();
    	JLabel lblWidth = new JLabel();
    	JLabel lblHeight = new JLabel();

    	JPanel areaPanel = new JPanel(new GridBagLayout());
        lblColumns.setText(Messages.getString("TableDialog.Columns"));
        GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 10, 5, 5);
		areaPanel.add(lblColumns, gbc);
		columns.setPreferredSize(new Dimension(50, 24));
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 5, 0);
		areaPanel.add(columns, gbc);
		lblRows.setText(Messages.getString("TableDialog.Rows"));
		gbc = new GridBagConstraints();
		gbc.gridx = 3; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 5, 5);
		areaPanel.add(lblRows, gbc);
		rows.setPreferredSize(new Dimension(50, 24));
		gbc = new GridBagConstraints();
		gbc.gridx = 4; gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 5, 5, 0);
		areaPanel.add(rows, gbc);
		lblWidth.setText(Messages.getString("TableDialog.Width"));
		gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		areaPanel.add(lblWidth, gbc);
		widthField.setPreferredSize(new Dimension(50, 25));
		widthField.setText("100%");
		gbc = new GridBagConstraints();
		gbc.gridx = 1; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 0);
		areaPanel.add(widthField, gbc);
		lblHeight.setText(Messages.getString("TableDialog.Height"));
		gbc.gridx = 3; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		areaPanel.add(lblHeight, gbc);
		heightField.setPreferredSize(new Dimension(50, 25));
		gbc.gridx = 4; gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 0);
		areaPanel.add(heightField, gbc);
		lblPadding.setText(Messages.getString("TableDialog.Cell_padding"));
		gbc.gridx = 0; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		areaPanel.add(lblPadding, gbc);
		cellpadding.setPreferredSize(new Dimension(50, 24));
		gbc.gridx = 1; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 0);
		areaPanel.add(cellpadding, gbc);
		lblSpacing.setText(Messages.getString("TableDialog.Cell_Spacing"));
		gbc.gridx = 3; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		areaPanel.add(lblSpacing, gbc);
		cellspacing.setPreferredSize(new Dimension(50, 24));
		gbc.gridx = 4; gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 0);
		areaPanel.add(cellspacing, gbc);
		lblBorder.setText(Messages.getString("TableDialog.Border"));
		gbc.gridx = 0; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 5, 5);
		areaPanel.add(lblBorder, gbc);
		border.setPreferredSize(new Dimension(50, 24));
		gbc.gridx = 1; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 0);
		areaPanel.add(border, gbc);
		lblFillColor.setText(Messages.getString("TableDialog.Fill_Color"));
		gbc.gridx = 3; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		areaPanel.add(lblFillColor, gbc);
		bgcolorField.setPreferredSize(new Dimension(50, 24));
		HTMLUtils.setBgcolorField(bgcolorField);
		gbc.gridx = 4; gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 5);
		areaPanel.add(bgcolorField, gbc);
		bgColorB.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bgColorB_actionPerformed(e);
			}
		});
		bgColorB.setIcon(Utils.createIcon(RES + "color.png"));
		bgColorB.setPreferredSize(new Dimension(25, 25));
		gbc.gridx = 5; gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 5, 10);
		areaPanel.add(bgColorB, gbc);
		lblOutline.setText(Messages.getString("TableDialog.Align"));
		gbc.gridx = 0; gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 10, 5);
		areaPanel.add(lblOutline, gbc);
		alignCB.setBackground(new Color(230, 230, 230));
		alignCB.setFont(new java.awt.Font("Dialog", 1, 10));
		alignCB.setPreferredSize(new Dimension(70, 25));
		gbc.gridx = 1; gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 10, 5);
		areaPanel.add(alignCB, gbc);																																																																				
		lblVertOutline.setText(Messages.getString("TableDialog.Vert_Align"));
		gbc.gridx = 3; gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 10, 5);
		areaPanel.add(lblVertOutline, gbc);		
		vAlignCB.setPreferredSize(new Dimension(70, 25));
		vAlignCB.setFont(new java.awt.Font("Dialog", 1, 10));
		vAlignCB.setBackground(new Color(230, 230, 230));
		gbc.gridx = 4; gbc.gridy = 4;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 5, 10, 0);
		areaPanel.add(vAlignCB, gbc);
		
		setContentPanel(areaPanel);
		pack();
		setResizable(false);
    }
    
	private void bgColorB_actionPerformed(ActionEvent e) 
	{
	    Color initColor = HTMLUtils.decodeColor(bgcolorField.getText());
		Color c =
			JColorChooser.showDialog(
				this,
				Messages.getString("TableDialog.Color"),
				initColor);
		if(c == null)
			return;

		bgcolorField.setText(
			"#" + Integer.toHexString(c.getRGB()).substring(2).toUpperCase());
		HTMLUtils.setBgcolorField(bgcolorField);
	}
    
    /* (non-Javadoc)
     * @see org.openmechanics.htmleditor.HTMLOptionDialog#getHTML()
     */
    public String getHTML()
    {        
		String tableTag = "<table ";
		String w = widthField.getText().trim();
		if (w.length() > 0)
			tableTag += " width=\"" + w + "\" ";
		String h = heightField.getText().trim();
		if (h.length() > 0)
			tableTag += " height=\"" + h + "\" ";
		String cp = cellpadding.getValue().toString();
		try {
			Integer.parseInt(cp, 10);
			tableTag += " cellpadding=\"" + cp + "\" ";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String cs = cellspacing.getValue().toString();
		try {
			Integer.parseInt(cs, 10);
			tableTag += " cellspacing=\"" + cs + "\" ";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		String b = border.getValue().toString();
		try {
			Integer.parseInt(b, 10);
			tableTag += " border=\"" + b + "\" ";
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (alignCB.getSelectedIndex() > 0)
			tableTag += " align=\"" + alignCB.getSelectedItem() + "\" ";
		if (vAlignCB.getSelectedIndex() > 0)
			tableTag += " valign=\"" + vAlignCB.getSelectedItem() + "\" ";
		if (bgcolorField.getText().length() > 0)
			tableTag += " bgcolor=\"" + bgcolorField.getText() + "\" ";
		tableTag += ">\n";
		
		int cols = 1;
		int trows = 1;
		try {
			cols = ((Integer) columns.getValue()).intValue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			trows = ((Integer)rows.getValue()).intValue();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		for (int r = 0; r < trows; r++) {
			tableTag += "<tr>\n";
			for (int c = 0; c < cols; c++)
				tableTag += "<td><p></p></td>\n";
			    //tableTag += "<td></td>\n";
			tableTag += "</tr>\n";
		}
		tableTag += "</table>";
		return tableTag;
    }
}
