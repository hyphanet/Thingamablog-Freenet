/*
 * Created on Jun 18, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import net.sf.thingamablog.TBGlobals;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.RTextArea;


/**
 * @author Bob Tantlinger
 */
public class SourceTextArea extends RSyntaxTextArea
{
    
    /**
     * 
     */
    public SourceTextArea()
    {
        super();
        init();
    }

    /**
     * @param wordWrapEnabled
     * @param textMode
     */
    public SourceTextArea(boolean wordWrapEnabled, int textMode)
    {
        super(wordWrapEnabled, textMode);
        init();
    }
    
    private void init()
    {
        RTextArea.setIconGroup(
            new IconGroup("MyGroup", TBGlobals.RESOURCES, null, "png"));
        restoreDefaultSyntaxHighlightingColorScheme();
        
		//set some default syntax colors/fonts
        String name = TBGlobals.getEditorFont().getFamily();
		int size = TBGlobals.getEditorFont().getSize();
        SyntaxScheme ss[] = getSyntaxHighlightingColorScheme().syntaxSchemes;
        ss[Token.LITERAL_STRING_DOUBLE_QUOTE].foreground = new Color(153, 0, 153);
        ss[Token.RESERVED_WORD].foreground = new Color(0, 0, 153);        
		for(int i = 0; i < ss.length; i++)
		{
		    if(ss[i] != null)
		    {
		        ss[i].font = new Font(name, ss[i].font.getStyle(), size);
		    }
		}
		setFont(TBGlobals.getEditorFont());
        
		//For some reason the delete key isn't mapped to the editor by default
        KeyStroke delKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        
        //shift + insert doesnt paste either
        KeyStroke siPasteKeyStroke = KeyStroke.getKeyStroke("shift INSERT");
        
		InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = getActionMap();		

		inputMap.put(delKeyStroke, "MyDel");
		actionMap.put("MyDel", 
            RSyntaxTextArea.getAction(RSyntaxTextArea.DELETE_ACTION));
        
        inputMap.put(siPasteKeyStroke, "MyPaste");
        actionMap.put("MyPaste", 
            RSyntaxTextArea.getAction(RSyntaxTextArea.PASTE_ACTION));
		
        //some defaults for our editors
		setAutoIndentEnabled(true);
        setBracketMatchingEnabled(true);
        setMatchedBracketBorderColor(Color.BLACK);
        setTabsEmulated(true);
        setCurrentLineHighlightColor(new Color(184, 220, 255));
        setFadeCurrentLineHighlight(true);        
        setSelectionColor(Color.LIGHT_GRAY);        
    }  
 
}
