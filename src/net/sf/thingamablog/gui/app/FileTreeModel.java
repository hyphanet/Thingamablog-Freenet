package net.sf.thingamablog.gui.app;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


public class FileTreeModel implements TreeModel
{
    private File root;
    private FileFilter dirFilter = new DirOnlyFilter();
    private FileFilter fileFilter = new FileOnlyFilter();
    private Comparator fileComp = new FileComparator();
    
    public FileTreeModel(File root)
    {
        this.root = root;
    }

    public void addTreeModelListener(TreeModelListener l)
    {        

    }

    public Object getChild(Object parent, int index)
    {     
        List f = getChildren((File)parent);        
        return f.get(index);
    }

    public int getChildCount(Object parent)
    {
        File f = (File)parent;
        String ch[] = f.list();
        if(ch == null)
            return 0;
        
        return ch.length;
    }

    public int getIndexOfChild(Object parent, Object child)
    {        
        List files = getChildren((File)parent);        
        for(int i = 0; i < files.size(); i++)
            if(files.get(i) == child)
                return i;
        
        return 0;
    }
    
    private List getChildren(File dir)
    {
        List files = new ArrayList();
        List d = Arrays.asList(dir.listFiles(dirFilter));
        Collections.sort(d, fileComp);
        files.addAll(d);
        
        List f = Arrays.asList(dir.listFiles(fileFilter));
        Collections.sort(f, fileComp);
        files.addAll(f);
        
        return files;
    }

    public Object getRoot()
    {        
        return root;
    }

    public boolean isLeaf(Object node)
    {
        File f = (File)node;
        return f.isFile();
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {        

    }
    
    private class DirOnlyFilter implements FileFilter
    {
        public boolean accept(File f)
        {            
            return f.isDirectory();
        }        
    }
    
    private class FileOnlyFilter implements FileFilter
    {
        public boolean accept(File f)
        {            
            return !f.isDirectory();
        }
    }
    
    private class FileComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {            
            File f1 = (File)o1;
            File f2 = (File)o2;
            
            return f1.getName().compareTo(f2.getName());
        }        
    }
}
