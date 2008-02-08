/*
 * Created on Nov 4, 2007
 */
package net.sf.thingamablog.gui.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.atlanticbb.tantlinger.ui.text.dialogs.HyperlinkDialog;
import net.sf.thingamablog.blog.ArchiveRange;
import net.sf.thingamablog.blog.BackendException;
import net.sf.thingamablog.blog.BlogEntry;
import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.gui.app.FileTreeModel;
import net.sf.thingamablog.gui.app.WeblogTreeCellRenderer;

import com.xduke.xswing.DataTipManager;


/**
 * @author Bob Tantlinger
 *
 */
public class InternalLinkDialog extends HyperlinkDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.editor");
    
    private static Icon icon = UIUtils.getIcon(UIUtils.X48, "webloglink.png"); //$NON-NLS-1$
    private static String title = i18n.str("internal_link"); //$NON-NLS-1$
    private static String desc = i18n.str("internal_link_desc"); //$NON-NLS-1$
       
    private JTabbedPane tabs;
    private TBWeblog blog;  
    
    private TreeSelectionListener treeSelectionHandler = new TreeSelectionHandler();
    

    /**
     * @param parent
     * @param title
     * @param desc
     * @param ico
     */
    public InternalLinkDialog(Frame parent, TBWeblog b)
    {
        super(parent, title, desc, icon, false);
        initUI(b);
    }

    /**
     * @param parent
     * @param title
     * @param desc
     * @param ico
     */
    public InternalLinkDialog(Dialog parent, TBWeblog b)
    {
        super(parent, title, desc, icon, false);
        initUI(b);
    }
    
    private void initUI(TBWeblog b)
    {
        blog = b; 
        JPanel contentPane = new JPanel(new BorderLayout(5, 5));        
        Container old = getContentPane();
        
        tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.setPreferredSize(new Dimension(185, 5));        
        try
        {
            ArchiveRange[] ar = blog.getArchives();
            Arrays.sort(ar, new ArchiveRangeComparator());
            if(ar != null && ar.length > 0)
                tabs.addTab(i18n.str("archives"), createArchivesPanel(ar)); //$NON-NLS-1$
            String[] cats = blog.getCategories();
            if(cats != null && cats.length > 0)
                tabs.addTab(i18n.str("categories"), createCategoriesPanel(cats)); //$NON-NLS-1$
        }
        catch(BackendException ex)
        {
            ex.printStackTrace();
        }
        tabs.addTab(i18n.str("web_files"), createWebFilesPanel()); //$NON-NLS-1$
        
        contentPane.add(old, BorderLayout.CENTER);
        contentPane.add(tabs, BorderLayout.WEST);
        setContentPane(contentPane);
        
        setSize(515, 435);        
               
    }
    
    private JComponent createWebFilesPanel()
    {        
        FileTreeModel model = new FileTreeModel(blog.getWebFilesDirectory());
        JTree t = new JTree(model);
        DataTipManager.get().register(t);
        t.setCellRenderer(new WeblogTreeCellRenderer());
        t.addTreeSelectionListener(treeSelectionHandler);
        
        return new JScrollPane(t);
    }
    
    private JComponent createArchivesPanel(ArchiveRange[] arcs) throws BackendException
    {
        JPanel p = new JPanel(new BorderLayout());
        final JComboBox arcCombo = new JComboBox(arcs);
        final JTree tree = new JTree(new EntryTreeModel(arcs[0]));
        DataTipManager.get().register(tree);
        tree.addTreeSelectionListener(treeSelectionHandler);
        tree.setCellRenderer(new WeblogTreeCellRenderer());
        arcCombo.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {                
                try
                {
                    tree.setModel(new EntryTreeModel((ArchiveRange)arcCombo.getSelectedItem()));
                }
                catch (BackendException ex)
                {                    
                    ex.printStackTrace();
                }
            }
        });
        
        p.add(arcCombo, BorderLayout.NORTH);
        p.add(new JScrollPane(tree), BorderLayout.CENTER);
        return p;
    }
    
    private JComponent createCategoriesPanel(String[] cats) throws BackendException
    {
        JPanel p = new JPanel(new BorderLayout());
        final JComboBox catCombo = new JComboBox(cats);
        final JTree tree = new JTree(new EntryTreeModel(cats[0]));
        DataTipManager.get().register(tree);
        tree.addTreeSelectionListener(treeSelectionHandler);
        tree.setCellRenderer(new WeblogTreeCellRenderer());
        catCombo.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {                
                try
                {
                    tree.setModel(new EntryTreeModel((String)catCombo.getSelectedItem()));
                }
                catch(BackendException ex)
                {                    
                    ex.printStackTrace();
                }
            }
        });
        
        p.add(catCombo, BorderLayout.NORTH);
        p.add(new JScrollPane(tree), BorderLayout.CENTER);
        return p;
    }
    
    private class TreeSelectionHandler implements TreeSelectionListener
    {

        /* (non-Javadoc)
         * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
         */
        public void valueChanged(TreeSelectionEvent e)
        {            
            if(e.isAddedPath())
            {
                Object o = e.getPath().getLastPathComponent();
                String url = "", text = ""; //$NON-NLS-1$ //$NON-NLS-2$
                
                if(o instanceof String)//category
                {
                    url = blog.getUrlForCategory((String)o);
                    text = (String)o;
                }
                else if(o instanceof ArchiveRange)
                {
                    ArchiveRange ar = (ArchiveRange)o;
                    url = blog.getUrlForArchive(ar);    
                    text = ar.getFormattedRange();
                }
                else if(o instanceof BlogEntry)
                {
                    BlogEntry be = (BlogEntry)o;
                    url = blog.getUrlForEntry(be);
                    text = be.getTitle();
                }
                else if(o instanceof File) //web file
                {
                    File f = (File)o;
                    url = blog.getUrlForWebFile(f);
                    text = f.getName();
                    //System.out.println(url);
                }
                
                Map m = getAttributes();
                m.put("href", toRelativeUrl(url)); //$NON-NLS-1$
                setAttributes(m);
                setLinkText(text);
            }           
        }
    }
    
    private String toRelativeUrl(String url)
    {
        try
        {
            URL u = new URL(url);
            String host = u.getHost();
            if(!host.equals("")) //$NON-NLS-1$
            {
                int s = url.indexOf(host) + host.length();
                return url.substring(s, url.length());
            }
        }
        catch(MalformedURLException ex){}
        return url;
    }
    
    private class EntryTreeModel implements TreeModel
    {
        Object root;
        BlogEntry[] entries;
        
        public EntryTreeModel(ArchiveRange rootArc) throws BackendException
        {
            root = rootArc;
            entries = blog.getEntriesFromArchive(rootArc);
        }
        
        public EntryTreeModel(String rootCat) throws BackendException
        {
            root = rootCat;
            entries = blog.getEntriesFromCategory(rootCat);
        }
        
        public Object getChild(Object parent, int index)
        {            
            if(parent == root)
                return entries[index];
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
         */
        public int getChildCount(Object parent)
        {
            if(parent == root)
                return entries.length;
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
         */
        public int getIndexOfChild(Object parent, Object child)
        {            
            if(parent == root)
            {
                for(int i = 0; i < entries.length; i++)
                    if(child == entries[i])
                        return i;
            }
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#getRoot()
         */
        public Object getRoot()
        {            
            return root;
        }

        /* (non-Javadoc)
         * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
         */
        public boolean isLeaf(Object node)
        {            
            return node instanceof BlogEntry;
        }
        
        public void addTreeModelListener(TreeModelListener l){}        
        public void removeTreeModelListener(TreeModelListener l){}        
        public void valueForPathChanged(TreePath path, Object newValue){}
        
    }
    
    private class ArchiveRangeComparator implements Comparator
    {
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            ArchiveRange a1 = (ArchiveRange)o1;
            ArchiveRange a2 = (ArchiveRange)o2;
            return a2.getStartDate().compareTo(a1.getStartDate());
        }        
    }

}
