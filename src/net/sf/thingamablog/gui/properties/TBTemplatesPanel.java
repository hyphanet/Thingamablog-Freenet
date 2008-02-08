/*
 * Created on Nov 1, 2007
 */
package net.sf.thingamablog.gui.properties;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.TemplatePack;
import net.sf.thingamablog.gui.app.TemplateSelectionPanel;


/**
 * @author Bob Tantlinger
 *
 */
public class TBTemplatesPanel extends PropertyPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    
    private TemplateSelectionPanel tmplPanel;
    private TBWeblog blog;
    private TemplatePack currentPack;
    
    /**
     * 
     */
    public TBTemplatesPanel(TBWeblog b)
    {
        blog = b;
        setLayout(new BorderLayout(5, 5));
        
        JLabel msgLabel = new JLabel();
        String msg = 
            i18n.str("select_template_set_prompt"); //$NON-NLS-1$
        msgLabel.setText("<html>" + msg + "</html>"); //$NON-NLS-1$ //$NON-NLS-2$
        
        tmplPanel = new TemplateSelectionPanel(blog);
        
        add(msgLabel, BorderLayout.NORTH);
        add(tmplPanel, BorderLayout.CENTER);
        
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        try
        {
            tmplPanel.setSelectedPack(blog.getTemplatePack());
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#isValidData()
     */
    public boolean isValidData()
    {        
        try
        {        
            currentPack = blog.getTemplatePack();
        }
        catch(IOException ioe)
        {            
            UIUtils.showError(this, ioe);
            return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.thingamablog.gui.properties.PropertyPanel#saveProperties()
     */
    public void saveProperties()
    {
        if(currentPack == null || !currentPack.getTitle().equals(tmplPanel.getSelectedPack().getTitle()))
        {
            TemplatePack p = tmplPanel.getSelectedPack();
            try
            {
                p.installPack(blog.getHomeDirectory());
            }
            catch(IOException e)
            {                
                UIUtils.showError(this, e);
            }
        }
    }
    
}
