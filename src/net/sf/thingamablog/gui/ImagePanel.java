/* This file is part of Thingamablog. ( http://thingamablog.sf.net )
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
*/

package net.sf.thingamablog.gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
*   
*	Panel for viewing and manipulating Images
*	@author Bob Tantlinger 7/20/2002
*
*/
public class ImagePanel extends JPanel implements Scrollable
{
  	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    public final static int ZOOM_IN = 50;
  	public final static int ZOOM_OUT = 100;
  	public final static int ZOOM_ACTUAL = 150;
	public static final int MAX_ZOOM = 32;
  	  	
  	public final static int ROTATE_CW = 25;
  	public final static int ROTATE_CCW = 75;  	
  	
  	
  	private Image image;
   	private int imageWidth, imageHeight;
   	private double scaleFactor = 1.0;
   	private double angle;   	
   	private boolean bestFit = true;
   	private boolean displayable;
   	private int scrollInc = 10;
   	private int zInc = 1; 
   	private boolean isShowNoPreview; 	
   	
   	public ImagePanel(){}
    	
   	/*public void setImage(Image img)
   	{
   		if(img == null) 
   			return;
   			
   		image = img;
   		imageWidth = image.getWidth(this);   // Get image width
   		imageHeight = image.getHeight(this); // and its height
   		angle = 0;
		scaleBestFit();
   		repaint();
   	} */
   	
   	public void setImage(Image img)
   	{
   		image = img;
   		if(image != null)
   		{  		
   			imageWidth = image.getWidth(this);   // Get image width
   			imageHeight = image.getHeight(this); // and its height   			
   		}
   		else
   			imageWidth = imageHeight = 0;

   		displayable = 
   			((imageWidth < 1 || imageHeight < 1) ? false : true);    			   		  		
   		angle = 0;
		scaleBestFit();
   		//repaint();
   	}
   	
   	public Image getImage()
   	{
   		return image;
   	}
   	
   	public Dimension getImageSize()
   	{
 		if(image == null)
 			return null;
 		
 		return new Dimension(image.getWidth(this), 
   						image.getHeight(this));   			
   	}
   	
	public boolean isImageDisplayed()
	{
		return displayable;
	}
   	
   	public boolean isBestFit()
   	{ 
   		return bestFit; 
   	}
   	
   	public void scaleBestFit()
   	{ 
   		bestFit = true;

   		setPreferredSize(null);
   		revalidate();   		   			
   		repaint(); 
   	}
   	
	public void zoom(int z)
   	{   		  		
   		if(isBestFit())   			
   			zInc = Math.round(1/(float)scaleFactor) * -1;
   		
   		if(z == ZOOM_IN)
   		{   		
   			if(zInc < MAX_ZOOM)
   				zInc++;
   			if(zInc == 0)
   				zInc = 2;
   		}   	
   		else if(z == ZOOM_OUT)
   		{   	
   			if(zInc > -MAX_ZOOM)
   				zInc--;
   			if(zInc == 0)
   				zInc = -2;
   		} 
   		else if(z == ZOOM_ACTUAL)
   			zInc = 1;   			
   		else
   			return;
   		  		
   		double temp; 
   		if(zInc >= 1)
   			temp = (double)Math.abs(zInc)/1;
   		else
   			temp = (double)1/Math.abs(zInc);
   	
   		if(Math.min(imageWidth * temp, imageHeight * temp) <= 0)
   			return;
   		else
   			scaleFactor = temp;   			
   		
   		bestFit = false;   		
   		resizePanelToImage();
   		repaint();	
   	}
   	
   	public void rotate90deg(int dir)
   	{
      	if(image == null)
   			return;
      	
      	if(dir == ROTATE_CW)
      		angle += 90;
      	else if(dir == ROTATE_CCW)
      		angle -= 90;
      		
      	if(angle > 360)
      		angle = 90;
      	else if(angle < 0)
      		angle = 270;
      	
      	if((int)angle == 90 || (int)angle == 270)
      	{
      		imageWidth = image.getHeight(this);
      		imageHeight = image.getWidth(this);	
      	}
      	else
      	{
      		imageWidth = image.getWidth(this);   
   			imageHeight = image.getHeight(this); 
      	} 	
      		
      	scaleBestFit();
   	}
   	
   	public void paintComponent(Graphics g)
   	{
  		super.paintComponent(g);
  		drawImage(g);  		
   	}
   	
	protected void drawImage(Graphics g)
	{
   		if(!displayable)
   		{
   			drawNoPreviewMessage(g);
   			return;
  		}
  		  		
   		Graphics2D g2D = (Graphics2D)g;
   		
   		if(isBestFit())
   			scaleFactor = bestFitScaleFactor();   		
   		
   		//absolute image width/height
   		int iwidth = image.getWidth(this);     			
   		int iheight = image.getHeight(this);
   		
   		// Create a transform to translate and scale the image   		
   		AffineTransform at = new AffineTransform();   		
   		at.setToTranslation((getSize().width - iwidth*scaleFactor)/2,
                            (getSize().height - iheight*scaleFactor)/2);

   		at.scale(scaleFactor, scaleFactor);      		
   		g2D.transform(at); //scale and translate
   		
   		g2D.rotate(angle * Math.PI/180, iwidth/2.0, iheight/2.0); 		      		

   		g2D.drawImage(image, 0, 0, this);	
	}
   	
   	private void drawNoPreviewMessage(Graphics g)
   	{
   		String message = " ";
   		if(isShowNoPreview)
   			message = "No preview available";
   		FontMetrics fm = g.getFontMetrics();
   		int swidth = fm.stringWidth(message);
   		int sheight = fm.getHeight();
   		g.drawString(message, (getWidth()/2 - swidth/2),
   					(getHeight()/2 - sheight/2));   		
   	}
   	
   	private double bestFitScaleFactor()
   	{
   		int padding = 5;        //padding around image
   		double h = 1.0, v = 1.0; //horizontal and vertical
   							     //default scale factor of 100%   		
 		
   		if(getSize().width - padding < imageWidth)      		
   			h = (double)(getSize().width - padding)/imageWidth;
     		
   		if(getSize().height - padding < imageHeight)
   			v = (double)(getSize().height - padding)/imageHeight;  		
     			      		      				
   		return Math.min(h, v);   			
   	}
  	
   	private void resizePanelToImage()
   	{
   		int w = (int)(imageWidth * scaleFactor);
   		int h = (int)(imageHeight * scaleFactor);
   		   		
   		setPreferredSize(new Dimension(w, h));   		
   		revalidate();     			
   	}
   	
   	/* Scrollable interface implementation so the panel can be
   	 *  properly used in a JScrollPane, etc...
   	 */   	 
   	 public Dimension getPreferredScrollableViewportSize()
   	 {
   	 	return getPreferredSize();
   	 }
   	 
   	 public int getScrollableUnitIncrement(Rectangle vr, int o, int d)
   	 {
   	 	return scrollInc; //scroll by scrollInc pixels at a time	
   	 }
   	 
   	 public int getScrollableBlockIncrement(Rectangle vr, int o, int d)
   	 {
   	 	return scrollInc * 5;	
   	 }
   	 
	 public boolean getScrollableTracksViewportWidth()
	 {
	 	return getPreferredSize() == null || 
	 		getPreferredSize().width <= getVisibleRect().width;	 		
	 }
	 
	 public boolean getScrollableTracksViewportHeight()
	 {
	 	return getPreferredSize() == null || 
	 		getPreferredSize().height <= getVisibleRect().height;	 		
	 }   	    	      	                                    
    /**
     * @return
     */
    public boolean isShowNoPreview()
    {
        return isShowNoPreview;
    }

    /**
     * @param b
     */
    public void setShowNoPreview(boolean b)
    {
        isShowNoPreview = b;
    }

}