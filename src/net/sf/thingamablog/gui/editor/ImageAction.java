package net.sf.thingamablog.gui.editor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;


public class ImageAction extends HTMLTextEditAction
{
    private Frame parent;
    private File imgDir;

    public ImageAction(Frame parent, File imgDir)
    {
        super(Messages.getString("EntryEditor.Image"));
        Messages.setMnemonic("EntryEditor.Image", this);
        putValue(SMALL_ICON, Utils.createIcon(TBGlobals.RESOURCES + "image.png"));
        this.parent = parent;
        setImageDirectory(imgDir);
    }
    
    public void setImageDirectory(File dir)
    {
        imgDir = dir;
    }

    public void sourceEditPerformed(ActionEvent e, JTextComponent editor)
    {
        TBImageDialog dlg = new TBImageDialog(parent, imgDir);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        if(dlg.hasUserCancelled())
            return;

        editor.requestFocusInWindow();
        editor.replaceSelection(dlg.getHTML());       
    }

    public void wysiwygEditPerformed(ActionEvent e, JEditorPane editor)
    {
        TBImageDialog dlg = new TBImageDialog(parent, imgDir);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        if(dlg.hasUserCancelled())
            return;

        HTMLDocument document = (HTMLDocument)editor.getDocument();
        HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();

        String tagText = dlg.getHTML();
        if(editor.getCaretPosition() == document.getLength())
            tagText += "&nbsp;";

        editor.replaceSelection("");
        HTML.Tag tag = HTML.Tag.IMG;
        if(tagText.startsWith("<a"))
            tag = HTML.Tag.A;

        HTMLUtils.insertInlineHTML(tagText, tag, editor);        
    }
}