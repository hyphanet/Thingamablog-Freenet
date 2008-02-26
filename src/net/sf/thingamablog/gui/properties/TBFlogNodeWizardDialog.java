/*
 * TBFlogNodeWizardDialog.java
 *
 * Created on 26 février 2008, 06:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.thingamablog.gui.properties;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import net.atlanticbb.tantlinger.i18n.I18n;
import net.atlanticbb.tantlinger.ui.UIUtils;
import net.sf.thingamablog.TBGlobals;
import net.sf.thingamablog.blog.BackendException;

import net.sf.thingamablog.blog.TBWeblog;
import net.sf.thingamablog.blog.Weblog;
import net.sf.thingamablog.blog.WeblogBackend;
import net.sf.thingamablog.gui.LabelledItemPanel;
import net.sf.thingamablog.gui.MultilineText;
import net.sf.thingamablog.transport.FCPTransport;
import net.sf.thingamablog.util.freenet.fcp.fcpManager;

/**
 *
 * @author dieppe
 */
public class TBFlogNodeWizardDialog extends JDialog {
    
    private static final I18n i18n = I18n.getInstance("net.sf.thingamablog.gui.properties");
    private Logger logger = Logger.getLogger("net.sf.thingamablog.gui.properties");
    
    private PropertyPanel nodePanel;
    private Vector panels = new Vector();
    private CardLayout wizLayout;
    private JPanel wizPanel;
    
    private TBWeblog flog;
    
    private boolean isCancelled;
    
    /** Creates a new instance of TBFlogNodeWizardDialog */
    public TBFlogNodeWizardDialog(final Frame f, final File dir, final WeblogBackend backend) {
        super(f, true);
        
        setTitle(i18n.str("node_config")); //$NON-NLS-1$
        
        WindowAdapter windowAdapter = new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                cancelDialog();
            }
        };
        addWindowListener(windowAdapter);
        wizLayout = new CardLayout();
        wizPanel = new JPanel(wizLayout);
        
        final JButton nextButton;
        final JButton doneButton;
        flog = new TBWeblog(dir);
        flog.setBackend(backend);
        
        nodePanel = new NodePanel();
        nodePanel.setBorder(new EmptyBorder(10,10,10,10));
        panels.add(nodePanel);
        
        wizPanel.add(nodePanel, "1");
        
        nextButton = new JButton(i18n.str("next-")); //$NON-NLS-1$
        doneButton = new JButton(i18n.str("cancel"));
        
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(e.getSource() == nextButton) {
                    nodePanel.saveProperties();
                    setVisible(false);
                    TBFlogWizardDialog wiz = new TBFlogWizardDialog(f, dir, backend, flog);
                    wiz.setLocationRelativeTo(f);
                    wiz.setVisible(true);
                    if(!wiz.hasUserCancelled()) {
                        flog = wiz.getWeblog();
                    } else {
                        cancelDialog();
                    }
                    dispose();
                }
                if(e.getSource() == doneButton) {
                    cancelDialog();
                }
            }
        };
        
        nextButton.addActionListener(listener);
        doneButton.addActionListener(listener);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBorder(new EtchedBorder());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        buttonPanel.add(nextButton);
        buttonPanel.add(doneButton);
        controlPanel.add(buttonPanel);
        
//        JLabel img = new JLabel();
//        img.setVerticalAlignment(SwingConstants.TOP);
//        img.setOpaque(true);
//        img.setBackground(Color.WHITE);
//        img.setIcon(UIUtils.getIcon(UIUtils.MISC, "wizard.jpg")); //$NON-NLS-1$
        
        getContentPane().add(wizPanel, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);
//        getContentPane().add(img, BorderLayout.WEST);
        
        pack();
        setSize(250, getHeight());
        setResizable(false);
    }
    
    
    private void cancelDialog() {
        isCancelled = true;
        try{
            flog.deleteAll();
        }catch(BackendException ex){}
        dispose();
    }
    
    private JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel("<html><h2>" + text + "</h2></html>"); //$NON-NLS-1$ //$NON-NLS-2$
        return label;
    }
    
    public boolean hasUserCancelled(){
        return isCancelled;
    }
    
    public Weblog getWeblog() {
        return flog;
    }
    
    private class NodePanel extends PropertyPanel {
        private fcpManager Manager = new fcpManager();
        private JTextField portField = new JTextField(6);
        private JTextField hostnameField = new JTextField(20);
        private JButton testButton = new JButton();
        
        public NodePanel(){
            
            LabelledItemPanel lip = new LabelledItemPanel();
            JLabel header = createHeaderLabel(i18n.str("node_config"));
            String text = i18n.str("node_config_panel");
            
            portField.setText(TBGlobals.getProperty("NODE_PORT"));
            hostnameField.setText(TBGlobals.getProperty("NODE_HOSTNAME"));
            testButton.setText(i18n.str("test_config"));
            testButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int port = Integer.parseInt(portField.getText());
                    String hostname = hostnameField.getText();
                    Manager.setNode(hostname,port);
                    try {
                        Manager.getConnection().connect();
                        JOptionPane.showMessageDialog(TBFlogNodeWizardDialog.this,
                                i18n.str("node_config_ok"), i18n.str("node_config_ok"),  //$NON-NLS-1$ //$NON-NLS-2$
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(TBFlogNodeWizardDialog.this,
                                i18n.str("invalid_node_config"), ex.getMessage(),  //$NON-NLS-1$ //$NON-NLS-2$
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            
            lip.addItem(i18n.str("port"), portField);
            lip.addItem(i18n.str("hostname"), hostnameField);
            lip.addItem("", testButton);
            
            setLayout(new BorderLayout());
            add(header, BorderLayout.NORTH);
            add(new MultilineText(text), BorderLayout.CENTER);
            add(lip, BorderLayout.SOUTH);
        }
        
        public void saveProperties() {
            try {
                int port = Integer.parseInt(portField.getText());
                String keys[]=new String[2];
                String hostname = hostnameField.getText();
                Manager.setNode(hostname,port);
                keys=Manager.generateKeyPair();
                Manager.getConnection().disconnect();
                
                // We put "USK" instead of "SSK"
                keys[1] = keys[1].substring("SSK".length());
                String url = "USK" + keys[1];
                flog.setBlogUrls("",url,url,url);
                
                flog.setPublishTransport(new net.sf.thingamablog.transport.FCPTransport());
                ((FCPTransport) flog.getPublishTransport()).setInsertURI(keys[0]);
                logger.log(Level.INFO,"Transport method set to FCP");
            } catch (IOException ex) {
                logger.log(Level.INFO,"Node unreachable : " + ex.getMessage());
                logger.log(Level.INFO,"Transport method set to Local");
                JOptionPane.showMessageDialog(TBFlogNodeWizardDialog.this,
                        i18n.str("invalid_node_config"), ex.getMessage(),  //$NON-NLS-1$ //$NON-NLS-2$
                        JOptionPane.WARNING_MESSAGE);
                flog.setPublishTransport(new net.sf.thingamablog.transport.LocalTransport());
            }
        }
        
        
        public boolean isValidData() {
            return true;
        }
    }
}
