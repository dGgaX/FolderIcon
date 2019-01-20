/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package foldermenue;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Karima
 */
public class AddFolderTrayIcon {
    
    private final JPopupMenu jPopupMenu;
    private TrayIcon trayIcon;
    private final SystemTray tray;
    private final JDialog invoker;
    
    /**
     * generates a new FolderTrayIcon
     * @param folder 
     */
    public AddFolderTrayIcon(File folder) {
    
        jPopupMenu = new JPopupMenu();
    
        invoker = new JDialog();
        invoker.setUndecorated(true);
        invoker.setOpacity(0.0f);
        invoker.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        if (!SystemTray.isSupported()) {
            Logger.getLogger(AddFolderTrayIcon.class.getName()).log(Level.SEVERE, null, "SystemTray is not supported");
            exitProgram();
        }
        
        tray = SystemTray.getSystemTray();

        try {
            
            int taskbarheight = Toolkit.getDefaultToolkit().getScreenSize().height - GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
            
            Icon icon;
            
            if (taskbarheight < 32) {
                icon = new ImageIcon(getClass().getResource("/foldermenue/icon/menue-icon-16.png"));
            } else {
                icon = new ImageIcon(getClass().getResource("/foldermenue/icon/menue-icon-32.png"));
            }
            
            Image image = convertIconToImage(icon);
                        
            trayIcon = new TrayIcon(image, "Programme");
          
            tray.add(trayIcon);
            
            addFolderStructure(jPopupMenu, folder);
            
            addExitButton(jPopupMenu);
            

            trayIcon.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger() ||
                       (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1)) {
                        
                        invoker.setVisible(true);
                        jPopupMenu.show(invoker, e.getX(), e.getY());
//                        jPopupMenu.setLocation(e.getX(), e.getY());
//                        jPopupMenu.setInvoker(invoker);
//                        jPopupMenu.setVisible(true);
                        invoker.setLocation(e.getX(), e.getY());
                        invoker.setSize(jPopupMenu.getPreferredSize());
                        
                    }
                }
            });
            
        } catch (AWTException ex) {
            Logger.getLogger(AddFolderTrayIcon.class.getName()).log(Level.SEVERE, null, "SystemTray is not supported");
            exitProgram();
        }
            
    }
    
    private void addFolderStructure(JComponent jMenu, File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                JMenu jMenuSub = addFolderButton(jMenu, fileEntry);
                addFolderStructure(jMenuSub, fileEntry);
            } else {
                addFileButton(jMenu, fileEntry);
            }
        }
    }
    
    public final Image convertIconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        } else {
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) image.getGraphics();
            icon.paintIcon(null, g2, 0, 0);
            return image;
        }
    }
    
    private JMenuItem addFileButton(JComponent parentMenu, File file) {
        JMenuItem jMenuItem = new JMenuItem();
        
        jMenuItem.setText(file.getName().substring(0, file.getName().lastIndexOf(".")));
        jMenuItem.setToolTipText(file.getAbsolutePath());
        
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon( file );
        jMenuItem.setIcon(icon);
        
        jMenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
            try {
                Process p = Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " +  file.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(AddFolderTrayIcon.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        });
        if (parentMenu instanceof JMenu) {
            ((JMenu) parentMenu).add(jMenuItem);
        } else if (parentMenu instanceof JPopupMenu) {
            ((JPopupMenu) parentMenu).add(jMenuItem);
        }
        
        return jMenuItem;
    }
    
    private JMenuItem addExitButton(JComponent parentMenu) {
        
        JMenuItem jMenuExit = new JMenuItem();
        jMenuExit.setText("Beenden");
        jMenuExit.setToolTipText("Beendet dieses Programm!");
        
        Icon icon = new ImageIcon(getClass().getResource("/foldermenue/images/cross.png"));
        jMenuExit.setIcon(icon);
        
        jMenuExit.addActionListener((java.awt.event.ActionEvent evt) -> {
            exitProgram();
        });
        
        if (parentMenu instanceof JMenu) {
            ((JMenu) parentMenu).add(new Separator());
            ((JMenu) parentMenu).add(jMenuExit);
        } else if (parentMenu instanceof JPopupMenu) {
            ((JPopupMenu) parentMenu).add(new Separator());
            ((JPopupMenu) parentMenu).add(jMenuExit);
        }
    
        return jMenuExit;
        
    }
    
    private JMenu addFolderButton(JComponent parentMenu, File folder) {
        JMenu jMenu = new JMenu();
        jMenu.setText(folder.getName());
        jMenu.setToolTipText(folder.getAbsolutePath());
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon( folder );
        jMenu.setIcon(icon);
        if (parentMenu instanceof JMenu) {
            ((JMenu) parentMenu).add(jMenu);
        } else if (parentMenu instanceof JPopupMenu) {
            ((JPopupMenu) parentMenu).add(jMenu);
        }
        return jMenu;
    }

    /**
     * exits the Programm and deletes the TryIcon...
     */
    public final void exitProgram() {
        tray.remove(trayIcon);
        System.exit(0);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AddFolderTrayIcon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AddFolderTrayIcon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AddFolderTrayIcon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AddFolderTrayIcon.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        if (args.length > 0) {
            String path = args[0];
            for (int i = 1; i < args.length; i++) {
                path += " " + args[i];
            }
            File folder = new File(path);
            AddFolderTrayIcon addFolderTrayIcon = new AddFolderTrayIcon(folder);
        }
        
    }
    
}
