/*
 * Created on Jun 16, 2005
 *
 */
package net.sf.thingamablog.gui.editor;

import java.awt.Color;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JEditorPane;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;


/**
 *
 */
public class HTMLUtils
{

    /**
     * Tests if an element is an implied paragraph (p-implied)
     * @param el The element
     * @return true if the elements name equals "p-implied", false otherwise
     */
    public static boolean isImplied(Element el)
    {
        return el.getName().equals("p-implied");
    }
    
    /**
     * Incloses a chunk of HTML text in the specified tag
     * @param enclTag the tag to enclose the HTML in
     * @param innerHTML the HTML to be inclosed
     * @return
     */
    public static String createTag(HTML.Tag enclTag, String innerHTML)
    {
        return createTag(enclTag, new SimpleAttributeSet(), innerHTML);
    }
    
    /**
     * Incloses a chunk of HTML text in the specified tag
     * with the specified attribs
     * 
     * @param enclTag
     * @param set
     * @param innerHTML
     * @return
     */
    public static String createTag(HTML.Tag enclTag, AttributeSet set, String innerHTML)
    {
        String t = tagOpen(enclTag, set) + innerHTML + tagClose(enclTag);        
        return t;
    }
    
    private static String tagOpen(HTML.Tag enclTag, AttributeSet set)
    {
        String t = "<" + enclTag;
        for(Enumeration e = set.getAttributeNames(); e.hasMoreElements();)
        {
            Object name = e.nextElement();
            if(!name.toString().equals("name"))
            {               
                Object val = set.getAttribute(name);
                t += " " + name + "=\"" + val + "\"";
            }
        }
        
        return t + ">";
    }
    
    private static String tagClose(HTML.Tag t)
    {
        return "</" + t + ">";
    }
    
    /**
     * Searches upward for the specified parent for the element.
     * @param curElem
     * @param parentTag
     * @return The parent element, or null if the parent wasnt found
     */
    public static Element getParent(Element curElem, HTML.Tag parentTag)
    {
        Element parent = curElem;
        while(parent != null)
        {            
            if(parent.getName().equals(parentTag.toString()))
                return parent;
            parent = parent.getParentElement();
        }
        
        return null;
    }
    
    /**
     * Searches for a list Elelemt that is the parent of the specified element.
     * 
     * @param elem
     * @return A list element (UL, OL, DIR, MENU, or DL) if found, null otherwise
     */
    public static Element getListParent(Element elem)
    {
        Element parent = elem;
        while(parent != null)
        {
            if(parent.getName().toUpperCase().equals("UL") || 
                parent.getName().toUpperCase().equals("OL") ||
                parent.getName().equals("dl") || parent.getName().equals("menu") ||
                parent.getName().equals("dir"))
                return parent;
            parent = parent.getParentElement();
        }
        return null;
    }
    
    public static String removeEnclosingTags(Element elem, String txt)
    {
        HTML.Tag t = HTML.getTag(elem.getName());
        return removeEnclosingTags(t, txt);
    }

    public static String removeEnclosingTags(HTML.Tag t, String txt)
    {
        if(t != null)
        {
            try
            {
                txt = txt.split("<" + t + "(.*?)>")[1];
            }
            catch (Exception ex)
            {
            }

            try
            {
                txt = txt.split("</" + t + ">")[0];
            }
            catch (Exception ex)
            {
            }
        }

        return txt;
    }

    public static String getElementHTML(Element el, boolean includeEnclosingTags)
    {
        String txt = "";
        int start = el.getStartOffset();
        int len = el.getEndOffset() - start;
        try
        {
            StringWriter out = new StringWriter();
            ElementWriter w = new ElementWriter(out, el);
            w.write();
            txt = out.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        if(includeEnclosingTags)
            return txt;
        return removeEnclosingTags(el, txt);
    }

    public static void removeElement(Element el) throws BadLocationException
    {
        int start = el.getStartOffset();
        int len = el.getEndOffset() - start;
        Document document = el.getDocument();
        if(el.getEndOffset() > document.getLength())
            document.insertString(document.getLength(), "\n", null);
        document.remove(start, len);
    }
    
    /**
     * Converts a Color to a hex string
     * in the format "#RRGGBB"
     */
    public static String colorToHex(Color color) 
    {
        String colorstr = new String("#");

        // Red
        String str = Integer.toHexString(color.getRed());
        if (str.length() > 2)
            str = str.substring(0, 2);
        else if (str.length() < 2)
            colorstr += "0" + str;
        else
            colorstr += str;

        // Green
        str = Integer.toHexString(color.getGreen());
        if (str.length() > 2)
            str = str.substring(0, 2);
        else if (str.length() < 2)
            colorstr += "0" + str;
        else
            colorstr += str;

        // Blue
        str = Integer.toHexString(color.getBlue());
        if (str.length() > 2)
            str = str.substring(0, 2);
        else if (str.length() < 2)
            colorstr += "0" + str;
        else
            colorstr += str;

        return colorstr;
    }
    
    /**
     * Convert a "#FFFFFF" hex string to a Color.
     * If the color specification is bad, an attempt
     * will be made to fix it up.
     */
    public static Color hexToColor(String value)
    {
        String digits;
        //int n = value.length();
        if(value.startsWith("#"))        
            digits = value.substring(1, Math.min(value.length(), 7));        
        else         
            digits = value;
        
        String hstr = "0x" + digits;
        Color c;
        
        try 
        {
            c = Color.decode(hstr);
        } 
        catch(NumberFormatException nfe) 
        {
            c = Color.BLACK; // just return black
        }
        return c; 
    }
    
    /**
     * Convert a color string such as "RED" or "#NNNNNN" or "rgb(r, g, b)"
     * to a Color.
     */
    public static Color stringToColor(String str) 
    {
        Color color = null;
        
        if (str.length() == 0)
            color = Color.black;      
        else if (str.charAt(0) == '#')
            color = hexToColor(str);
        else if (str.equalsIgnoreCase("Black"))
            color = hexToColor("#000000");
        else if(str.equalsIgnoreCase("Silver"))
            color = hexToColor("#C0C0C0");
        else if(str.equalsIgnoreCase("Gray"))
            color = hexToColor("#808080");
        else if(str.equalsIgnoreCase("White"))
            color = hexToColor("#FFFFFF");
        else if(str.equalsIgnoreCase("Maroon"))
            color = hexToColor("#800000");
        else if(str.equalsIgnoreCase("Red"))
            color = hexToColor("#FF0000");
        else if(str.equalsIgnoreCase("Purple"))
            color = hexToColor("#800080");
        else if(str.equalsIgnoreCase("Fuchsia"))
            color = hexToColor("#FF00FF");
        else if(str.equalsIgnoreCase("Green"))
            color = hexToColor("#008000");
        else if(str.equalsIgnoreCase("Lime"))
            color = hexToColor("#00FF00");
        else if(str.equalsIgnoreCase("Olive"))
            color = hexToColor("#808000");
        else if(str.equalsIgnoreCase("Yellow"))
            color = hexToColor("#FFFF00");
        else if(str.equalsIgnoreCase("Navy"))
            color = hexToColor("#000080");
        else if(str.equalsIgnoreCase("Blue"))
            color = hexToColor("#0000FF");
        else if(str.equalsIgnoreCase("Teal"))
            color = hexToColor("#008080");
        else if(str.equalsIgnoreCase("Aqua"))
            color = hexToColor("#00FFFF");
        else
            color = hexToColor(str); // sometimes get specified without leading #
        return color;
    }

    public static void insertInlineHTML(String tagText, HTML.Tag tag, JEditorPane editor)
    {
        HTMLEditorKit editorKit;
        HTMLDocument document;
        try
        {
            editorKit = (HTMLEditorKit)editor.getEditorKit();
            document = (HTMLDocument)editor.getDocument();
        }
        catch (ClassCastException ex)
        {
            return;
        }

        int caret = editor.getCaretPosition();
        Element pElem = document.getParagraphElement(caret);

        boolean breakParagraph = tag.breaksFlow() || tag.isBlock();
        boolean beginParagraph = caret == pElem.getStartOffset();

        try
        {
            if(breakParagraph && beginParagraph)
            {
                /*
                 * Trick: insert a non-breaking space before start, so that we're inserting into the middle of a line.
                 * Then, remove the space. This works around a bug when using insertHTML near the beginning of a
                 * paragraph.
                 */
                document.insertBeforeStart(pElem, "&nbsp;");
                editorKit.insertHTML(document, caret + 1, tagText, 1, 0, tag);
                document.remove(caret, 1);
            }
            else if(breakParagraph && !beginParagraph)
            {
                editorKit.insertHTML(document, caret, tagText, 1, 0, tag);
            }
            else if(!breakParagraph && beginParagraph)
            {
                /*
                 * Trick: insert a non-breaking space after start, so that we're inserting into the middle of a line.
                 * Then, remove the space. This works around a bug when using insertHTML near the beginning of a
                 * paragraph.
                 */
                document.insertAfterStart(pElem, "&nbsp;");
                editorKit.insertHTML(document, caret + 1, tagText, 0, 0, tag);
                document.remove(caret, 1);
            }
            else if(!breakParagraph && !beginParagraph)
            {
                editorKit.insertHTML(document, caret, tagText, 0, 0, tag);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static Hashtable HTMLColors;
    static
    {
        HTMLColors = new Hashtable();
        HTMLColors.put("red", Color.red);
        HTMLColors.put("green", Color.green);
        HTMLColors.put("blue", Color.blue);
        HTMLColors.put("cyan", Color.cyan);
        HTMLColors.put("magenta", Color.magenta);
        HTMLColors.put("yellow", Color.yellow);
        HTMLColors.put("black", Color.black);
        HTMLColors.put("white", Color.white);
        HTMLColors.put("gray", Color.gray);
        HTMLColors.put("darkgray", Color.darkGray);
        HTMLColors.put("lightgray", Color.lightGray);
        HTMLColors.put("orange", Color.orange);
        HTMLColors.put("pink", Color.pink);
    }

    public static Color getColorForName(String name, Color defaultColor)
    {
        if(HTMLColors.contains(name.toLowerCase()))
            return (Color)HTMLColors.get(name.toLowerCase());
        return defaultColor;
    }

    public static Color decodeColor(String color, Color defaultColor)
    {
        String colorVal = "";
        if(color.length() > 0)
        {
            colorVal = color.trim();
            if(colorVal.startsWith("#"))
                colorVal = colorVal.substring(1);
            try
            {
                colorVal = new Integer(Integer.parseInt(colorVal, 16)).toString();
                return Color.decode(colorVal.toLowerCase());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        else
            return defaultColor;
        return getColorForName(color, defaultColor);
    }

    public static String encodeColor(Color color)
    {
        return "#" + Integer.toHexString(color.getRGB() - 0xFF000000).toUpperCase();
    }

    public static Color decodeColor(String color)
    {
        return decodeColor(color, Color.white);
    }

    public static void setBgcolorField(JTextField field)
    {
        Color c = decodeColor(field.getText());
        field.setBackground(c);
        field.setForeground(new Color(~c.getRGB()));
    }

    public static void setColorField(JTextField field)
    {
        Color c = decodeColor(field.getText(), Color.black);
        field.setForeground(c);
        //field.setForeground(new Color(~c.getRGB()));
    }


}
