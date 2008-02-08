/*
 * Created on Jul 1, 2004
 * 
 */
package net.sf.thingamablog.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * @author Bob Tantlinger
 *
 * Multiline text suitable for descriptive text on dialogs etc. 
 */
public class MultilineText extends JPanel
{
	//private JTextArea textArea;	
    
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel textLabel = null;

    public MultilineText()
    {
        initialize();
    }
    
    public MultilineText(String text)
    {
        initialize();
        setText(text);
    }
		
	
	/**
	 * 
	 * @param text the multiline text
	 */	
	/*public MultilineText(String text)
	{
		textArea = new JLabel();
        
        
        textArea = new JTextArea();
		textArea.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		textArea.setBackground(getBackground());
		textArea.setEditable(false);
		textArea.setSelectionColor(getBackground());
		textArea.setSelectedTextColor(textArea.getForeground());			
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);	
		textArea.setText(text);			
        textArea.setPreferredSize(null);
        setText(text);        
		setLayout(new BorderLayout());			
		add(textArea, BorderLayout.CENTER);        
	}
    */
    
	
	/**
     * This method initializes this
     * 
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.gridy = 0;
        textLabel = new JLabel();
        //textLabel.setText("<html>a really really really really friggin long bit of text. Yes, it is really really really long. It has to be because we need to check and see how it's going to behave... blah blah blah balh b alhd sdfsdf ok, i think this should be enough </html>");
        this.setLayout(new GridBagLayout());
        //this.setSize(new Dimension(261, 173));
        this.add(textLabel, gridBagConstraints);
    		
    }

    /**
	 * Sets the multiline text
	 * @param t The multiline text
	 */
	public void setText(String t)
	{
		textLabel.setText("<html>" + t + "</html>");
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
