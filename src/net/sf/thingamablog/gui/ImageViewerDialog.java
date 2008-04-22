/*
 * Created on Jun 15, 2004
 *
 * This file is part of Thingamablog. ( http://thingamablog.sf.net )
 *
 * Copyright (c) 2004, Bob Tantlinger All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 * 
 */
package net.sf.thingamablog.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import net.atlanticbb.tantlinger.ui.UIUtils;
import thingamablog.l10n.i18n;


/**
 * @author Bob Tantlinger
 *
 * 
 * 
 */
public class ImageViewerDialog extends JDialog
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private ImagePanel imagePanel;
	private File currentImageFile;
	//private static final String RES = TBGlobals.RESOURCES;	
	
	public ImageViewerDialog(Frame f, File i)
	{
		super(f, false);
		init(i);
	}
	
	public ImageViewerDialog(Dialog d, File i)
	{
		super(d, false);
		init(i);
	}
	
	private void init(File f)
	{
		currentImageFile = f;
		setTitle(f.getName());
		
		imagePanel = new ImagePanel();
		getContentPane().add(new JScrollPane(imagePanel), BorderLayout.CENTER);
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		
		ImageIcon ii = new ImageIcon(f.getAbsolutePath());
		imagePanel.setImage(ii.getImage());
		
		JLabel statusBar = new JLabel(" "); //$NON-NLS-1$
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		String status = ""; //$NON-NLS-1$
		if(imagePanel.getImage() != null && currentImageFile != null)
		{
			Dimension d = imagePanel.getImageSize();
			double size = currentImageFile.length()/1024.0;
			java.text.DecimalFormat df = new java.text.DecimalFormat("0.0"); //$NON-NLS-1$
			status += " " + currentImageFile.getName() + ": " + //$NON-NLS-1$ //$NON-NLS-2$
					  '(' + d.width + 'x' + d.height + ") " + //$NON-NLS-1$
					  '(' +	df.format(size) + " KB" + ")";	 //$NON-NLS-1$ //$NON-NLS-2$
		}
		statusBar.setText(status);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
	}
	
	private JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE); //$NON-NLS-1$
		
		UIUtils.addToolBarButton(toolBar, new BestFitAction());
		UIUtils.addToolBarButton(toolBar, new ActualSizeAction());
		toolBar.addSeparator();
		UIUtils.addToolBarButton(toolBar, new ZoomInAction());
		UIUtils.addToolBarButton(toolBar, new ZoomOutAction());
		return toolBar;
	}
	
	private class BestFitAction extends AbstractAction
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public BestFitAction()
		{
			super(i18n.str("best_fit"), UIUtils.getIcon(UIUtils.MISC, "bestfit.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		public void actionPerformed(ActionEvent e)
		{
			imagePanel.scaleBestFit();
		}
	}
	
	private class ActualSizeAction extends AbstractAction 
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ActualSizeAction() 
		{
			super(i18n.str("actual_size"), UIUtils.getIcon(UIUtils.MISC, "actual.gif")); //$NON-NLS-1$ //$NON-NLS-2$
		}
        
		public void actionPerformed(ActionEvent e) 
		{
			imagePanel.zoom(ImagePanel.ZOOM_ACTUAL);
		}
	}
	
	private class ZoomInAction extends AbstractAction 
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ZoomInAction() 
		{
			super(i18n.str("zoom_in"), UIUtils.getIcon(UIUtils.X24, "magnify_up.png"));			      	 //$NON-NLS-1$ //$NON-NLS-2$
		}
        
		public void actionPerformed(ActionEvent e) 
		{
			imagePanel.zoom(ImagePanel.ZOOM_IN);
		}
	}
    
	private class ZoomOutAction extends AbstractAction 
	{
		/**
         * 
         */
        private static final long serialVersionUID = 1L;

        public ZoomOutAction() 
		{
			super(i18n.str("zoom_out"), UIUtils.getIcon(UIUtils.X24, "magnify_down.png")); //$NON-NLS-1$ //$NON-NLS-2$
		}
        
		public void actionPerformed(ActionEvent e)
		{
			imagePanel.zoom(ImagePanel.ZOOM_OUT);
		}
	}		
}
