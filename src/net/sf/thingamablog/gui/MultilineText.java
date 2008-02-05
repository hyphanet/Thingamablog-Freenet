/*
 * Created on Jul 1, 2004
 * 
 */
package net.sf.thingamablog.gui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author Bob Tantlinger
 *
 * Multiline text suitable for descriptive text on dialogs etc. 
 */
public class MultilineText extends JPanel
{
	private JTextArea textArea;		
	
	/**
	 * 
	 * @param text the multiline text
	 */	
	public MultilineText(String text)
	{
		textArea = new JTextArea();
		textArea.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		textArea.setBackground(getBackground());
		textArea.setEditable(false);
		textArea.setSelectionColor(getBackground());
		textArea.setSelectedTextColor(textArea.getForeground());			
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);	
		textArea.setText(text);			
		setLayout(new BorderLayout());			
		add(textArea, BorderLayout.CENTER);
	}
	
	/**
	 * Sets the multiline text
	 * @param t The multiline text
	 */
	public void setText(String t)
	{
		textArea.setText(t);
	}
}
