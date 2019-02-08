// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.*;

public class MainPanel extends JPanel {
  protected static final Dimension ICONSZ = new Dimension(150, 40);
  protected final JDesktopPane desktop = new JDesktopPane();
  protected final String str = String.format("JDesktopIcon: %dx%d", ICONSZ.width, ICONSZ.height);
  protected final JCheckBox check = new JCheckBox(str);

  public MainPanel() {
    super(new BorderLayout());
    desktop.setDesktopManager(new DefaultDesktopManager() {
      @Override protected Rectangle getBoundsForIconOf(JInternalFrame f) {
        Rectangle r = super.getBoundsForIconOf(f);
        // TEST: r.width = 200;
        System.out.println(r.getSize());
        return r;
      }
    });

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    mb.add(new JButton(new AbstractAction("add") {
      private int num;
      @Override public void actionPerformed(ActionEvent e) {
        JInternalFrame f = createFrame("#" + num, num * 10, num * 10);
        desktop.add(f);
        desktop.getDesktopManager().activateFrame(f);
        num++;
      }
    }));
    mb.add(Box.createHorizontalGlue());
    mb.add(check);

    addIconifiedFrame(createFrame("Frame", 30, 10));
    addIconifiedFrame(createFrame("Frame", 50, 30));
    add(desktop);
    add(mb, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  protected final JInternalFrame createFrame(String t, int x, int y) {
    JInternalFrame f = new JInternalFrame(t, true, true, true, true);
    f.setDesktopIcon(new JInternalFrame.JDesktopIcon(f) {
      @Override public Dimension getPreferredSize() {
        if (!check.isSelected()) {
          return super.getPreferredSize();
        }
        // Java 9 error: package com.sun.java.swing.plaf.motif is not visible
        // if (getUI() instanceof MotifDesktopIconUI) {
        if (getUI().getClass().getName().contains("MotifDesktopIconUI")) {
          return new Dimension(64, 64 + 32);
        } else {
          return ICONSZ;
        }
      }
    });
    f.setSize(200, 100);
    f.setLocation(x, y);
    f.setVisible(true);
    return f;
  }

  private void addIconifiedFrame(JInternalFrame f) {
    desktop.add(f);
    try {
      f.setIcon(true);
    } catch (PropertyVetoException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.put("DesktopIcon.width", ICONSZ.width);
      // TEST:
      // Font font = UIManager.getFont("InternalFrame.titleFont");
      // UIManager.put("InternalFrame.titleFont", font.deriveFont(30f));
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() { /* Singleton */ }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
      }
    });
    lafGroup.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window: Frame.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
