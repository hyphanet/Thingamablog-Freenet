package net.sf.thingamablog.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;

import net.sf.thingamablog.Utils;


public class JSplash extends JWindow implements KeyListener, MouseListener, 
    ActionListener {

    public JSplash(JFrame parent, String filename, int timeout) { 
        super(parent);

        Dimension size;
        Point location;

        ImageIcon image = Utils.createIcon(filename); 
        int w = image.getIconWidth(); 
        int h = image.getIconHeight(); 
        
        size = Toolkit.getDefaultToolkit().getScreenSize();
        //size = parent.getSize();
        location = parent.getLocation();
                
        int x = (size.width - w) / 2; 
        int y = (size.height - h) / 2; 
        setBounds(location.x + x, location.y + y, w, h);

        getContentPane().setLayout(new BorderLayout()); 
        JLabel picture = new JLabel(image); 
        getContentPane().add("Center", picture); 
        //picture.setBorder(new BevelBorder(BevelBorder.RAISED)); 

        // Listen for key strokes 
        addKeyListener(this); 

        // Listen for mouse events from here and parent 
        addMouseListener(this); 
        parent.addMouseListener(this); 

        // Timeout after a while 
        Timer timer = new Timer(0, this); 
        timer.setRepeats(false); 
        timer.setInitialDelay(timeout); 
        timer.start(); 
    } 

    public void block() { 
        while(isVisible()) {} 
    } 

    // Dismiss the window on a key press 
    public void keyTyped(KeyEvent event) {} 
    public void keyReleased(KeyEvent event) {} 
    public void keyPressed(KeyEvent event) { 
        setVisible(false); 
        dispose(); 
    } 

    // Dismiss the window on a mouse click 
    public void mousePressed(MouseEvent event) {} 
    public void mouseReleased(MouseEvent event) {} 
    public void mouseEntered(MouseEvent event) {} 
    public void mouseExited(MouseEvent event) {} 
    public void mouseClicked(MouseEvent event) { 
        setVisible(false); 
        dispose(); 
    } 

    // Dismiss the window on a timeout 
    public void actionPerformed(ActionEvent event) { 
        setVisible(false); 
        dispose(); 
    } 
}
