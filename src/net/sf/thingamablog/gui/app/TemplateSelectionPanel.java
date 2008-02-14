/*
 * Created on Oct 31, 2007
 */
package net.sf.thingamablog.gui.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.Author;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.TemplatePack;
import net.sf.thingamablog.blog.WeblogBackend;


/**
 * @author Bob Tantlinger
 *
 */
public class TemplateSelectionPanel extends JPanel
{    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.app");
    
    private JComboBox tmplCombo; 
    private TemplatePropertiesPanel propertyPanel;
    
    //private TemplatePack selectedPack;
    
    public TemplateSelectionPanel(final TBWeblog blog)
    {
        ActionListener prevListener = new ActionListener()
        {               
            public void actionPerformed(ActionEvent e)
            {                   
                
                WeblogPreviewer pw = WeblogPreviewer.getInstance();
                TemplatePack pack = getSelectedPack();
                if(pack == null)
                    return;
                try
                {
                    pw.previewInBrowser(blog, null, pack);
                }
                catch (Exception ex)
                {                        
                    ex.printStackTrace();
                }                  
            }
        };
        init(prevListener);
    }
    
    public TemplateSelectionPanel(final WeblogBackend backend, final String title, final String descr, final String[] cats, final Author[] auths, final String type)
    {
        ActionListener prevListener = new ActionListener()
        {               
            public void actionPerformed(ActionEvent e)
            {                   
                
                WeblogPreviewer pw = WeblogPreviewer.getInstance();
                TemplatePack pack = getSelectedPack();
                if(pack == null)
                    return;
                try
                {
                    pw.previewInBrowser(backend, pack, title, descr, cats, auths, type);
                }
                catch (Exception ex)
                {                        
                    ex.printStackTrace();
                }                  
            }
        };
        init(prevListener);
    }
        
    public void setSelectedPack(TemplatePack pack)
    {
        if(pack == null)
            return;
        
        int num = tmplCombo.getItemCount();
        for(int i = 0; i < num; i++)
        {
            TemplatePack p = (TemplatePack)tmplCombo.getItemAt(i);
            if(p.getTitle().equals(pack.getTitle()))
            {
                tmplCombo.setSelectedIndex(i);
                return;
            }
        }
        
        //pack isn't installed, so add it...
        tmplCombo.addItem(pack);
        tmplCombo.setSelectedItem(pack);
    }    
    
    public TemplatePack getSelectedPack()
    {
        return (TemplatePack)tmplCombo.getSelectedItem();
    }
    
    private void init(ActionListener al)
    {
        DefaultListCellRenderer renderer = new DefaultListCellRenderer()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(
            JList l,Object v, int i, boolean isSel, boolean hasFocus)
            {
                TemplatePack f = (TemplatePack)v;
                String name = f.getTitle();
                return super.getListCellRendererComponent(l, name, i, isSel, hasFocus);
            }   
        };
        
        Comparator packComparator = new Comparator()
        {
            public int compare(Object o1, Object o2)
            {                    
                TemplatePack p1 = (TemplatePack)o1;
                TemplatePack p2 = (TemplatePack)o2;
                return p1.getTitle().compareToIgnoreCase(p2.getTitle());
            }                
        };
        
        Vector packs = new Vector(TBGlobals.getAllAvailableTemplates());
        Collections.sort(packs, packComparator);
        tmplCombo = new JComboBox(packs);
        tmplCombo.setRenderer(renderer);
        tmplCombo.addItemListener(new ItemListener()
        {

            public void itemStateChanged(ItemEvent e)
            {
                TemplatePack pack = (TemplatePack)e.getItem();
                try
                {
                    propertyPanel.setProperties(pack.getPackProperties());
                }
                catch(IOException ex)
                {                        
                    ex.printStackTrace();
                }                    
            }
        });
        
        propertyPanel = new TemplatePropertiesPanel();
        propertyPanel.setEditable(false);
        if(packs.size() > 0)
            propertyPanel.setTemplatePack((TemplatePack)packs.get(0));
        
        JButton previewButton = new JButton(i18n.str("preview_")); //$NON-NLS-1$
        previewButton.addActionListener(al);
        
        JPanel selPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.fill = GridBagConstraints.BOTH;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        gridBagConstraints2.gridwidth = 2;
        gridBagConstraints2.gridy = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints1.gridy = 0;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        gridBagConstraints.gridx = 0;
        
        selPanel.add(tmplCombo, gridBagConstraints);
        selPanel.add(previewButton, gridBagConstraints1);
        selPanel.add(propertyPanel, gridBagConstraints2);
        
        setLayout(new BorderLayout());
        add(selPanel, BorderLayout.CENTER);
    }
}
