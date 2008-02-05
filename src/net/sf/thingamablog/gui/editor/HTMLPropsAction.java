/*
 * Created on Feb 26, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.StringWriter;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.Utils;
import net.sf.thingamablog.gui.Messages;

//TODO This class needs cleaned up...
public class HTMLPropsAction extends AbstractAction
{
    public static final String TABLE_PROPS = 
        Messages.getString("HTMLEditorActionSet.Table_Properties");
    public static final String IMG_PROPS = 
        Messages.getString("HTMLEditorActionSet.Image_Properties");
    public static final String LINK_PROPS = 
        Messages.getString("HTMLEditorActionSet.Hyperlink_Properties");
    public static final String PARA_PROPS = 
        Messages.getString("EntryEditor.Paragraph_Style");
    public static final String OBJECT_PROPS = 
        Messages.getString("HTMLEditorActionSet.Object_Properties");
    
    private final String RES = TBGlobals.RESOURCES;
    private Frame parent;
    private File imgDir;
    private JEditorPane editor;
    
    public HTMLPropsAction(Frame parent, JEditorPane editor, File imgDir)
    {
        super(OBJECT_PROPS +  "...");
        //Messages.setMnemonic("HTMLEditorActionSet.Object_Properties", this);
        this.parent = parent;
        this.putValue(SMALL_ICON, Utils.createIcon(RES + "properties.png"));
        this.editor = editor;
        setImageDirectory(imgDir);
    }
    
    public void setImageDirectory(File dir)
    {
        imgDir = dir;
    }
    
    public void update()
    {
        HTMLDocument document;
        try{            
            document = (HTMLDocument)editor.getDocument();
        }catch(ClassCastException ex){
            putValue(NAME, OBJECT_PROPS + "...");
            setEnabled(false);
            return;
        }        
        document = (HTMLDocument)editor.getDocument();
        AbstractDocument.BranchElement pEl = (AbstractDocument.BranchElement)document.getParagraphElement(editor
            .getCaretPosition());
        Element el = pEl.positionToElement(editor.getCaretPosition());        
        AttributeSet attrs = el.getAttributes();        
        
        String elName = attrs.getAttribute(StyleConstants.NameAttribute).toString().toUpperCase();
        if(elName.equals("IMG"))
        {
            putValue(NAME, IMG_PROPS + "...");
            setEnabled(true);
            return;
        }
        
        Object k = null;
        for(Enumeration en = attrs.getAttributeNames(); en.hasMoreElements();)
        {
            k = en.nextElement();
            if(k.toString().equals("a"))
            {
                putValue(NAME, LINK_PROPS + "...");
                setEnabled(true);
                return;
            }
        }
        
        if(pEl.getParentElement().getName().toUpperCase().equals("TD"))
        {
            putValue(NAME, TABLE_PROPS + "...");
            setEnabled(true);
            return;
        }

        if(pEl.getName().toUpperCase().equals("P"))
        {
            putValue(NAME, PARA_PROPS + "...");
            setEnabled(true);
            return;
        }
        
        putValue(NAME, OBJECT_PROPS + "...");
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {        
        HTMLDocument document;
        try{            
            document = (HTMLDocument)editor.getDocument();
        }catch(ClassCastException ex){
            return;
        }        
        document = (HTMLDocument)editor.getDocument();
        AbstractDocument.BranchElement pEl = (AbstractDocument.BranchElement)document.getParagraphElement(editor
            .getCaretPosition());
        Element el = pEl.positionToElement(editor.getCaretPosition());        
        AttributeSet attrs = el.getAttributes();        
        
        String elName = attrs.getAttribute(StyleConstants.NameAttribute).toString().toUpperCase();
        if(elName.equals("IMG"))
        {
            String src = "", link = "", alt = "", width = "", height = "";
            String hspace = "", vspace = "", border = "", align = "";
            if(attrs.isDefined(HTML.Attribute.SRC))
                src = attrs.getAttribute(HTML.Attribute.SRC).toString();
            if(attrs.isDefined(HTML.Attribute.ALT))
                alt = attrs.getAttribute(HTML.Attribute.ALT).toString();
            if(attrs.isDefined(HTML.Attribute.WIDTH))
                width = attrs.getAttribute(HTML.Attribute.WIDTH).toString();
            if(attrs.isDefined(HTML.Attribute.HEIGHT))
                height = attrs.getAttribute(HTML.Attribute.HEIGHT).toString();
            if(attrs.isDefined(HTML.Attribute.HSPACE))
                hspace = attrs.getAttribute(HTML.Attribute.HSPACE).toString();
            if(attrs.isDefined(HTML.Attribute.VSPACE))
                vspace = attrs.getAttribute(HTML.Attribute.VSPACE).toString();
            if(attrs.isDefined(HTML.Attribute.BORDER))
                border = attrs.getAttribute(HTML.Attribute.BORDER).toString();
            if(attrs.isDefined(HTML.Attribute.ALIGN))
                align = attrs.getAttribute(HTML.Attribute.ALIGN).toString();
            
            //get a link URL if the image has one
            for(Enumeration en = attrs.getAttributeNames(); en.hasMoreElements();)
            {
                Object o = en.nextElement();
                if(o.toString().equalsIgnoreCase("a"))
                {
                    Object n = attrs.getAttribute(o);
                    if(n != null && n.toString().startsWith("href="))                    
                        link = n.toString().split("=")[1];
                    break;
                }                
            }
            
            setImageProperties(editor, el, src, alt, link, width, height, hspace, vspace, border, align);
            return;
        }        
        
        Object k = null;
        for(Enumeration en = attrs.getAttributeNames(); en.hasMoreElements();)
        {
            k = en.nextElement();
            if(k.toString().equals("a"))
            {
                String[] param = attrs.getAttribute(k).toString().split(" ");
                String href = "", target = "", title = "", name = "";
                for(int i = 0; i < param.length; i++)
                    if(param[i].startsWith("href="))
                        href = param[i].split("=")[1];
                    else if(param[i].startsWith("title="))
                        title = param[i].split("=")[1];
                    else if(param[i].startsWith("target="))
                        target = param[i].split("=")[1];
                    else if(param[i].startsWith("name="))
                        name = param[i].split("=")[1];
                setLinkProperties(editor, el, href, target, title, name);
                return;
            }
            System.out.println(k + " = '" + attrs.getAttribute(k) + "'");
        }

        if(pEl.getParentElement().getName().toUpperCase().equals("TD"))
        {
            setTableProperties(editor, pEl.getParentElement());
            return;
        }

        if(pEl.getName().toUpperCase().equals("P"))
        {
            String id = "", cls = "", sty = "";
            AttributeSet pa = pEl.getAttributes();
            if(pa.getAttribute(HTML.Attribute.ID) != null)
                id = pa.getAttribute(HTML.Attribute.ID).toString();
            if(pa.getAttribute(HTML.Attribute.CLASS) != null)
                cls = pa.getAttribute(HTML.Attribute.CLASS).toString();
            if(pa.getAttribute(HTML.Attribute.STYLE) != null)
                sty = pa.getAttribute(HTML.Attribute.STYLE).toString();
            setElementProperties(editor, pEl, id, cls, sty);
        }
    }
    
    protected void setElementProperties(JEditorPane editor, Element el, String id, String cls, String sty) 
    {
		HTMLDocument document = (HTMLDocument)editor.getDocument();
		ElementDialog dlg = new ElementDialog(parent);
		dlg.setLocationRelativeTo(parent);
		
		dlg.setModal(true);		
		dlg.setID(id);
		dlg.setStyleClass(cls);
		dlg.setStyle(sty);
		// Uncommented, returns a simple p into the header... fix needed ?
		//dlg.header.setText(el.getName());
		dlg.setVisible(true);
		if(dlg.hasUserCancelled())
			return;		
		
		SimpleAttributeSet attrs = new SimpleAttributeSet(el.getAttributes());
		if (dlg.getID().length() > 0)
			attrs.addAttribute(HTML.Attribute.ID, dlg.getID());
		if (dlg.getStyleClass().length() > 0)
			attrs.addAttribute(HTML.Attribute.CLASS, dlg.getStyleClass());
		if (dlg.getStyle().length() > 0)
			attrs.addAttribute(HTML.Attribute.STYLE, dlg.getStyle());
		document.setParagraphAttributes(el.getStartOffset(), 0, attrs, true);
	}
    
	protected void setTableProperties(JEditorPane editor, Element td) 
	{
		HTMLDocument document = (HTMLDocument)editor.getDocument();
		HTMLEditorKit editorKit = (HTMLEditorKit)editor.getEditorKit();
	    
	    Element tr = td.getParentElement();
		Element table = tr.getParentElement();

		TablePropertiesDialog dlg = new TablePropertiesDialog(parent);		    
		dlg.setLocationRelativeTo(parent);

		dlg.setModal(true);
		//dlg.setTitle(Local.getString("Table properties"));

		/** **********PARSE ELEMENTS*********** */
		// TD***
		AttributeSet tda = td.getAttributes();
		if (tda.isDefined(HTML.Attribute.BGCOLOR)) {
			dlg.tdBgcolorField.setText(
				tda.getAttribute(HTML.Attribute.BGCOLOR).toString());
			HTMLUtils.setBgcolorField(dlg.tdBgcolorField);
		}
		if (tda.isDefined(HTML.Attribute.WIDTH))
			dlg.tdWidthField.setText(
				tda.getAttribute(HTML.Attribute.WIDTH).toString());
		if (tda.isDefined(HTML.Attribute.HEIGHT))
			dlg.tdHeightField.setText(
				tda.getAttribute(HTML.Attribute.HEIGHT).toString());
		if (tda.isDefined(HTML.Attribute.COLSPAN))
			try {
				Integer i =
					new Integer(
						tda.getAttribute(HTML.Attribute.COLSPAN).toString());
				dlg.tdColspan.setValue(i);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		if (tda.isDefined(HTML.Attribute.ROWSPAN))
			try {
				Integer i =
					new Integer(
						tda.getAttribute(HTML.Attribute.ROWSPAN).toString());
				dlg.tdRowspan.setValue(i);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		if (tda.isDefined(HTML.Attribute.ALIGN))
			dlg.tdAlignCB.setSelectedItem(
				tda
					.getAttribute(HTML.Attribute.ALIGN)
					.toString()
					.toLowerCase());
		if (tda.isDefined(HTML.Attribute.VALIGN))
			dlg.tdValignCB.setSelectedItem(
				tda
					.getAttribute(HTML.Attribute.VALIGN)
					.toString()
					.toLowerCase());
		dlg.tdNowrapChB.setSelected((tda.isDefined(HTML.Attribute.NOWRAP)));

		//TR ****
		AttributeSet tra = tr.getAttributes();
		if (tra.isDefined(HTML.Attribute.BGCOLOR)) {
			dlg.trBgcolorField.setText(
				tra.getAttribute(HTML.Attribute.BGCOLOR).toString());
			HTMLUtils.setBgcolorField(dlg.trBgcolorField);
		}
		if (tra.isDefined(HTML.Attribute.ALIGN))
			dlg.trAlignCB.setSelectedItem(
				tra
					.getAttribute(HTML.Attribute.ALIGN)
					.toString()
					.toLowerCase());
		if (tra.isDefined(HTML.Attribute.VALIGN))
			dlg.trValignCB.setSelectedItem(
				tra
					.getAttribute(HTML.Attribute.VALIGN)
					.toString()
					.toLowerCase());

		//TABLE ****
		AttributeSet ta = table.getAttributes();
		if (ta.isDefined(HTML.Attribute.BGCOLOR)) {
			dlg.bgcolorField.setText(
				ta.getAttribute(HTML.Attribute.BGCOLOR).toString());
			HTMLUtils.setBgcolorField(dlg.bgcolorField);
		}
		if (ta.isDefined(HTML.Attribute.WIDTH))
			dlg.widthField.setText(
				ta.getAttribute(HTML.Attribute.WIDTH).toString());
		if (ta.isDefined(HTML.Attribute.HEIGHT))
			dlg.heightField.setText(
				ta.getAttribute(HTML.Attribute.HEIGHT).toString());
		if (ta.isDefined(HTML.Attribute.ALIGN))
			dlg.alignCB.setSelectedItem(
				ta.getAttribute(HTML.Attribute.ALIGN).toString().toLowerCase());
		if (ta.isDefined(HTML.Attribute.VALIGN))
			dlg.vAlignCB.setSelectedItem(
				ta
					.getAttribute(HTML.Attribute.VALIGN)
					.toString()
					.toLowerCase());
		if (ta.isDefined(HTML.Attribute.CELLPADDING))
			try {
				Integer i =
					new Integer(
						ta.getAttribute(HTML.Attribute.CELLPADDING).toString());
				dlg.cellpadding.setValue(i);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		if (ta.isDefined(HTML.Attribute.CELLSPACING))
			try {
				Integer i =
					new Integer(
						ta.getAttribute(HTML.Attribute.CELLSPACING).toString());
				dlg.cellspacing.setValue(i);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		if (ta.isDefined(HTML.Attribute.BORDER))
			try {
				Integer i =
					new Integer(
						ta.getAttribute(HTML.Attribute.BORDER).toString());
				dlg.border.setValue(i);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		/** ****************************** */

		dlg.setVisible(true);
		if (dlg.CANCELLED)
			return;

		/** ******** SET ATTRIBUTES ********* */
		// TD***
		String tdTag = "<td";
		if (dlg.tdBgcolorField.getText().length() > 0)
			tdTag += " bgcolor=\"" + dlg.tdBgcolorField.getText() + "\"";

		if (dlg.tdWidthField.getText().length() > 0)
			tdTag += " width=\"" + dlg.tdWidthField.getText() + "\"";

		if (dlg.tdHeightField.getText().length() > 0)
			tdTag += " height=\"" + dlg.tdHeightField.getText() + "\"";

		if (!dlg.tdColspan.getValue().toString().equals("0"))
			tdTag += " colspan=\"" + dlg.tdColspan.getValue().toString() + "\"";

		if (!dlg.tdRowspan.getValue().toString().equals("0"))
			tdTag += " rowspan=\"" + dlg.tdRowspan.getValue().toString() + "\"";

		if (dlg.tdAlignCB.getSelectedItem().toString().length() > 0)
			tdTag += " align=\""
				+ dlg.tdAlignCB.getSelectedItem().toString()
				+ "\"";

		if (dlg.tdValignCB.getSelectedItem().toString().length() > 0)
			tdTag += " valign=\""
				+ dlg.tdValignCB.getSelectedItem().toString()
				+ "\"";

		if (dlg.tdNowrapChB.isSelected())
			tdTag += " nowrap";

		tdTag += ">";

		//TR***
		String trTag = "<tr";
		if (dlg.trBgcolorField.getText().length() > 0)
			trTag += " bgcolor=\"" + dlg.trBgcolorField.getText() + "\"";

		if (dlg.trAlignCB.getSelectedItem().toString().length() > 0)
			trTag += " align=\""
				+ dlg.trAlignCB.getSelectedItem().toString()
				+ "\"";

		if (dlg.trValignCB.getSelectedItem().toString().length() > 0)
			trTag += " valign=\""
				+ dlg.trValignCB.getSelectedItem().toString()
				+ "\"";

		trTag += ">";

		//TABLE ***
		String tTag = "<table";
		if (dlg.bgcolorField.getText().length() > 0)
			tTag += " bgcolor=\"" + dlg.bgcolorField.getText() + "\"";

		if (dlg.widthField.getText().length() > 0)
			tTag += " width=\"" + dlg.widthField.getText() + "\"";

		if (dlg.heightField.getText().length() > 0)
			tTag += " height=\"" + dlg.heightField.getText() + "\"";

		tTag += " cellpadding=\""
			+ dlg.cellpadding.getValue().toString()
			+ "\"";

		tTag += " cellspacing=\""
			+ dlg.cellspacing.getValue().toString()
			+ "\"";

		tTag += " border=\"" + dlg.border.getValue().toString() + "\"";

		if (dlg.alignCB.getSelectedItem().toString().length() > 0)
			tTag += " align=\""
				+ dlg.alignCB.getSelectedItem().toString()
				+ "\"";

		if (dlg.vAlignCB.getSelectedItem().toString().length() > 0)
			tTag += " valign=\""
				+ dlg.vAlignCB.getSelectedItem().toString()
				+ "\"";

		tTag += ">";

		/** ****************************** */

		/** ** UPDATE TABLE ***** */
		CompoundUndoHandler.beginCompoundEdit(document);
		try {
			StringWriter sw = new StringWriter();
			String copy;

			editorKit.write(
				sw,
				document,
				td.getStartOffset(),
				td.getEndOffset() - td.getStartOffset());
			copy = sw.toString();
			copy = copy.split("<td(.*?)>")[1];
			copy = copy.split("</td>")[0];
			//System.out.println(tdTag+copy+"</td>");
			document.setOuterHTML(td, tdTag + copy + "</td>");

			//System.out.println("*******");

			sw = new StringWriter();
			editorKit.write(
				sw,
				document,
				tr.getStartOffset(),
				tr.getEndOffset() - tr.getStartOffset());
			copy = sw.toString();
			copy = copy.split("<tr(.*?)>")[1];
			copy = copy.split("</tr>")[0];
			//System.out.println(trTag+copy+"</tr>");
			document.setOuterHTML(tr, trTag + copy + "</tr>");

			//System.out.println("*******");

			sw = new StringWriter();
			editorKit.write(
				sw,
				document,
				table.getStartOffset(),
				table.getEndOffset() - table.getStartOffset());
			copy = sw.toString();
			copy = copy.split("<table(.*?)>")[1];
			copy = copy.split("</table>")[0];
			//System.out.println(tTag+copy+"</table>");
			
			
			document.setOuterHTML(table, tTag + copy + "</table>");
			

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		CompoundUndoHandler.endCompoundEdit(document);
	}

    protected void setLinkProperties(JEditorPane editor, Element el, String href,
        String target, String title, String name)
    {
        HTMLDocument document = (HTMLDocument)editor.getDocument();

        HyperLinkDialog dlg = new HyperLinkDialog(parent);
        dlg.setLocationRelativeTo(parent);
        //dlg.setModal(true);

        dlg.setLinkURL(href);
        dlg.setLinkName(name);
        dlg.setLinkTitle(title);
        dlg.setTarget(target);
                

        try
        {
            dlg.setLinkDescription(
                document.getText(el.getStartOffset(), 
                    el.getEndOffset() - el.getStartOffset()));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        dlg.setVisible(true);
        if(dlg.hasUserCancelled())
            return;

        String aTag = dlg.getHTML();
        try
        {
            document.setOuterHTML(el, aTag);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    protected void setImageProperties(JEditorPane editor, Element el, String src, String alt, String link, String width,
        String height, String hspace, String vspace, String border, String align)
    {
		HTMLDocument document;
		try
		{
		    document = (HTMLDocument)editor.getDocument();
		}
		catch(Exception ex)
		{
		    return;
		}
        
		TBImageDialog id = new TBImageDialog(parent, imgDir);
        id.setSrc(src);
		id.setAltText(alt);
		id.setWidth(width);
		id.setHeight(height);
		id.setHSpace(hspace);
		id.setVSpace(vspace);
		id.setBorder(border);
		id.setAlignment(align);
		id.setLink(link);
		
		id.setLocationRelativeTo(parent);				
		id.setVisible(true);
		if(id.hasUserCancelled())
		    return;
		String imgTag = id.getHTML();
		if (editor.getCaretPosition() == document.getLength())
			imgTag += "&nbsp;";
		
		CompoundUndoHandler.beginCompoundEdit(document);
		try
		{
		    document.setOuterHTML(el, imgTag);
		}
		catch(Exception ex)
		{
		    ex.printStackTrace();
		}
		CompoundUndoHandler.endCompoundEdit(document);
    }
}