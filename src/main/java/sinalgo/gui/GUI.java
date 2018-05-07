/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package sinalgo.gui;

import lombok.Getter;
import sinalgo.configuration.AppConfig;
import sinalgo.configuration.Configuration;
import sinalgo.exception.ExportException;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.gui.controlPanel.ControlPanel;
import sinalgo.gui.controlPanel.MaximizedControlPanel;
import sinalgo.gui.controlPanel.MinimizedControlPanel;
import sinalgo.gui.dialogs.AboutDialog;
import sinalgo.gui.dialogs.GenerateNodesDialog;
import sinalgo.gui.dialogs.GlobalSettingsDialog;
import sinalgo.gui.dialogs.GraphInfoDialog;
import sinalgo.gui.dialogs.GraphPreferencesDialog;
import sinalgo.gui.dialogs.HelpDialog;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.Exporter;
import sinalgo.nodes.Position;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.AbstractCustomGlobal.GlobalMethod;
import sinalgo.runtime.Global;
import sinalgo.runtime.Main;
import sinalgo.runtime.SinalgoRuntime;
import sinalgo.runtime.events.Event;
import sinalgo.tools.storage.SortableVector;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Vector;

/**
 * The parent frame for the whole gui. It contains two children: the graph panel
 * and the control panel.
 */
public class GUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = -2301103668898732398L;

    private Font menuFont;
    private JMenu graphMenu;
    // private JMenuItem load;
    // private JMenuItem save;
    private JMenuItem exportMenuItem;
    private JMenuItem clearMenuItem;
    private JMenuItem reevaluateMenuItem;
    private JMenuItem generateMenuItem;
    private JMenuItem infoMenuItem;
    private JMenuItem preferencesMenuItem;
    private JMenuItem exitMenuItem;
    private JMenu globalMenu;
    private JMenu helpMenu;
    private JMenu viewMenu;
    private JMenuItem aboutMenuItem = new JMenuItem("About Sinalgo");
    private JMenuItem settingsMenuItem = new JMenuItem("Settings");
    private JMenuItem helpMenuItem = new JMenuItem("Help");
    private JMenuItem viewFullScreenMenuItem = new JMenuItem("Full Screen");
    private JMenuItem viewZoomInMenuItem = new JMenuItem("Zoom In");
    private JMenuItem viewZoomOutMenuItem = new JMenuItem("Zoom Out");
    private JMenuItem viewZoomFitMenuItem = new JMenuItem("Zoom To Fit");

    private GlobalInvoker globalInvoker = new GlobalInvoker();

    private GraphPanel graphPanel;
    private ControlPanel controlPanel;

    private HashMap<MenuElement, Method> methodsAndNames = new HashMap<>();

    private AppConfig appConfig = AppConfig.getAppConfig();

    /**
     * The constructor for the GUI class.
     *
     * @param r The runtime instance for which the gui was created.
     */
    public GUI(SinalgoRuntime r) {
        super(Global.useProject ? (Configuration.getAppName() + " - " + Global.projectName) : (Configuration.getAppName()));
        GuiHelper.setWindowIcon(this);

        // load the buttons for the menu - these settings should be done only once
        this.settingsMenuItem.addActionListener(this);
        this.settingsMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
        this.aboutMenuItem.addActionListener(this);
        this.aboutMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
        this.aboutMenuItem.setIcon(GuiHelper.getIcon("sinalgo_21.png"));
        this.helpMenuItem.addActionListener(this);
        this.helpMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_H);
        this.helpMenuItem.setIcon(GuiHelper.getIcon("helpSmall.gif"));
        this.helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

        this.viewFullScreenMenuItem.addActionListener(this);
        this.viewFullScreenMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
        this.viewFullScreenMenuItem.setIcon(GuiHelper.getIcon("zoomFullView.gif"));
        this.viewFullScreenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));

        this.viewZoomInMenuItem.addActionListener(this);
        this.viewZoomInMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_I);
        this.viewZoomInMenuItem.setIcon(GuiHelper.getIcon("zoominimage.png"));

        this.viewZoomOutMenuItem.addActionListener(this);
        this.viewZoomOutMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_O);
        this.viewZoomOutMenuItem.setIcon(GuiHelper.getIcon("zoomoutimage.png"));

        this.viewZoomFitMenuItem.addActionListener(this);
        this.viewZoomFitMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_T);
        this.viewZoomFitMenuItem.setIcon(GuiHelper.getIcon("zoomtofit.gif"));
        this.viewZoomFitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));

        this.runtime = r;
    }

    /**
     * @return The graph panel where the graph is drawn onto
     */
    public GraphPanel getGraphPanel() {
        return this.graphPanel;
    }

    /**
     * Returns the control panel of this GUI.
     *
     * @return The controlpanel of this GUI.
     */
    public ControlPanel getControlPanel() {
        return this.controlPanel;
    }

    private GenerateNodesDialog genNodesDialog = new GenerateNodesDialog(this);

    /**
     * The instance of the runtime to make changes comming from the gui.
     */
    @Getter
    private SinalgoRuntime runtime;

    // The zoom level used to draw the graph.
    private double zoomFactor = 1;

    /**
     * @return The zoom factor currently used to draw the graph.
     */
    public double getZoomFactor() {
        return this.zoomFactor;
    }

    /**
     * Sets the zoom factor at which the graph will be drawn and repaints the graph.
     *
     * @param zoom The new zoom factor.
     */
    public void setZoomFactor(double zoom) {
        this.setZoomFactorNoRepaint(zoom);
        this.redrawGUI(); // should be sufficient...
        // redrawGUINow();
    }

    /**
     * Sets the zoom factor at which the graph will be drawn, but does not repaint
     * the graph.
     *
     * @param zoom The new zoom factor.
     */
    public void setZoomFactorNoRepaint(double zoom) {
        this.zoomFactor = Math.max(zoom, Configuration.getMinZoomFactor()); // we have open-end zooming ;-)

        this.runtime.getTransformator().changeZoomFactor(this.zoomFactor);
    }

    /**
     * Increase the zoom factor by the factor specified in the config file and
     * redraw the graph.
     */
    public void zoomIn() {
        double newFactor = this.zoomFactor * Configuration.getZoomStep();
        this.setZoomFactor(newFactor);
    }

    /**
     * Multiply the zoom factor by a given number and redraw the graph.
     *
     * @param multiplicativeFactor The factor to multiply the zoom factor with.
     */
    public void zoom(double multiplicativeFactor) {
        this.setZoomFactor(this.zoomFactor * multiplicativeFactor);
    }

    /**
     * Decrease the zoom factor by the factor specified in the config file and
     * redraw the graph.
     */
    public void zoomOut() {
        double newFactor = Math.max(this.zoomFactor / Configuration.getZoomStep(), 0.01);
        this.setZoomFactor(newFactor);
    }

    /**
     * Toggle between full screen and normal view.
     */
    synchronized public void toggleFullScreen() {
        if (Global.isRunning) {
            return;
        }
        boolean full = !this.isUndecorated();
        this.dispose();
        this.setUndecorated(full); // window must be disposed prior to calling this method
        // setResizable(!full);
        this.setVisible(true);
        if (full) {
            // the following line seems to cause problems with transparent images under
            // windows
            // at least sometimes on my machine, but not on a notebook...
            // under windows, we could use the commented commands, but this seems not to
            // work under linux
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
            // setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            // restore the normal window size
            // setExtendedState(JFrame.NORMAL);
            // GUI.this.setSize(appConfig.guiWindowWidth, appConfig.guiWindowHeight);
            // GUI.this.setLocation(appConfig.guiWindowPosX, appConfig.guiWindowPosY);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
        }
        // update the menu title
        this.viewFullScreenMenuItem.setText(full ? "Exit Full Screen" : "Full Screen");

    }

    /**
     * This method places GUI elements in the Frame and initializes all its
     * children.
     */
    public void init() {
        WindowAdapter listener = new WindowAdapter() {

            // Catch the close events
            @Override
            public void windowClosing(WindowEvent event) {
                Main.exitApplication();
            }
        };
        this.addWindowListener(listener);
        this.addWindowStateListener(listener);

        // react upon resize-events
        this.addComponentListener(new ComponentListener() {

            int oldX = GUI.this.appConfig.getGuiWindowPosX(), oldY = GUI.this.appConfig.getGuiWindowPosY();

            @Override
            public void componentResized(ComponentEvent e) {
                if (GUI.this.getExtendedState() == Frame.MAXIMIZED_BOTH) {
                    GUI.this.appConfig.setGuiIsMaximized(true);
                    GUI.this.appConfig.setGuiWindowPosX(this.oldX);
                    GUI.this.appConfig.setGuiWindowPosY(this.oldY);
                } else {
                    GUI.this.appConfig.setGuiIsMaximized(false);
                    GUI.this.appConfig.setGuiWindowWidth(GUI.this.getWidth());
                    GUI.this.appConfig.setGuiWindowHeight(GUI.this.getHeight());
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // upon maximizing, first the component is moved, then resized. We only catch
                // the resize event
                this.oldX = GUI.this.appConfig.getGuiWindowPosX();
                this.oldY = GUI.this.appConfig.getGuiWindowPosY();
                GUI.this.appConfig.setGuiWindowPosX(GUI.this.getX());
                GUI.this.appConfig.setGuiWindowPosY(GUI.this.getY());
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        // -----------------------------------------------------
        // Global Key Input Listener
        // -----------------------------------------------------
        KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addKeyEventPostProcessor(e -> {
            // -----------------------------------------------------
            // ENTER starts / stops the simulation
            // -----------------------------------------------------
            if (!e.isConsumed() && e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                // Note: the event should not be marked consumed. A consumed Enter may
                // have been used elsewhere, e.g. to select a menu.
                if (Global.isRunning) {
                    this.controlPanel.stopSimulation();
                } else {
                    this.controlPanel.startSimulation();
                }
                return true; // no further event handling for this key event
            }
            return false;
        });

        this.setResizable(true);
        if (this.appConfig.isGuiIsMaximized()) {
            this.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        this.setSize(new Dimension(this.appConfig.getGuiWindowWidth(), this.appConfig.getGuiWindowHeight()));
        this.setLocation(this.appConfig.getGuiWindowPosX(), this.appConfig.getGuiWindowPosY());

        JMenuBar menuBar = new JMenuBar();
        this.menuFont = menuBar.getFont().deriveFont(Font.PLAIN);
        this.graphMenu = new JMenu("Simulation");
        this.graphMenu.setMnemonic(java.awt.event.KeyEvent.VK_S);

        this.exportMenuItem = new JMenuItem("Export...");
        this.exportMenuItem.addActionListener(this);
        this.exportMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_E);
        this.exportMenuItem.setIcon(GuiHelper.getIcon("export.gif"));

        this.clearMenuItem = new JMenuItem("Clear Graph");
        this.clearMenuItem.addActionListener(this);
        this.clearMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_C);
        this.clearMenuItem.setIcon(GuiHelper.getIcon("cleargraph.gif"));
        this.clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));

        this.reevaluateMenuItem = new JMenuItem("Reevaluate Connections");
        this.reevaluateMenuItem.addActionListener(this);
        this.reevaluateMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_R);
        this.reevaluateMenuItem.setIcon(GuiHelper.getIcon("connectnodes.gif"));
        this.reevaluateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

        this.generateMenuItem = new JMenuItem("Generate Nodes");
        this.generateMenuItem.addActionListener(this);
        this.generateMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_G);
        this.generateMenuItem.setIcon(GuiHelper.getIcon("addnodes.gif"));
        this.generateMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

        this.infoMenuItem = new JMenuItem("Network Info");
        this.infoMenuItem.addActionListener(this);
        this.infoMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_I);

        this.exitMenuItem = new JMenuItem("Exit");
        this.exitMenuItem.addActionListener(this);
        this.exitMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_X);

        this.preferencesMenuItem = new JMenuItem("Preferences");
        this.preferencesMenuItem.addActionListener(this);
        this.preferencesMenuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);

        this.graphMenu.add(this.generateMenuItem);
        this.graphMenu.add(this.reevaluateMenuItem);
        this.graphMenu.add(this.clearMenuItem);

        this.graphMenu.addSeparator();
        this.graphMenu.add(this.infoMenuItem);
        this.graphMenu.add(this.exportMenuItem);
        this.graphMenu.add(this.preferencesMenuItem);
        this.graphMenu.addSeparator();
        this.graphMenu.add(this.exitMenuItem);

        menuBar.add(this.graphMenu);

        this.globalMenu = new JMenu("Global");
        this.globalMenu.setMnemonic(java.awt.event.KeyEvent.VK_G);

        // Compose this menu every time when it is shown. This allows us to
        // give some more control to the user about the CustomMethods.
        this.globalMenu.addMenuListener(new MenuListener() {

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            /**
             * Parse a set of methods and include in the 'Global' menu the methods annotated
             * by {@link GlobalMethod}. For the methods from the project specific
             * CustomGlobal file, the project may revoke or modify the menu name.
             *
             * @param isProjectSpecific
             *            True if the list of methods belongs to the project specific
             *            CustomGlobal file.
             * @return True if at least one entry was added.
             */
            private boolean testMethods(Method[] methods, boolean isProjectSpecific) {
                boolean hasEntry = false;
                Vector<JMenu> subMenus = new Vector<>();

                // sort the methods according to the order-flag given in the annotation
                SortableVector<Method> mlist = new SortableVector<>();
                for (Method m : methods) {
                    AbstractCustomGlobal.GlobalMethod info = m.getAnnotation(AbstractCustomGlobal.GlobalMethod.class);
                    if (info != null) {
                        mlist.add(m);
                    }
                }
                mlist.sort((o1, o2) -> {
                    GlobalMethod info1 = o1
                            .getAnnotation(GlobalMethod.class);
                    GlobalMethod info2 = o2
                            .getAnnotation(GlobalMethod.class);
                    if (info1 != null && info2 != null) {
                        int i1 = info1.order();
                        int i2 = info2.order();
                        return Integer.compare(i1, i2);
                    }
                    return 0; // should not happen, if it does, ordering may be wrong
                });

                for (Method method : mlist) {
                    AbstractCustomGlobal.GlobalMethod info = method
                            .getAnnotation(AbstractCustomGlobal.GlobalMethod.class);
                    if (info != null) {
                        if (!isProjectSpecific) {
                            if (!Modifier.isStatic(method.getModifiers())) {
                                Main.warning("The method '" + method.getName()
                                        + "' in sinalgo.runtime.Global cannot be called from the dropdown menu, as it needs to be static.\nThe method is not added to the menu.");
                                continue;
                            }
                        }
                        if (method.getParameterTypes().length != 0) {
                            if (isProjectSpecific) {
                                Main.warning("The method '" + method.getName()
                                        + "' from the projects CustomGlobal class cannot be called from the dropdown menu, as it needs parameters to be called. \nThe method is not added to the menu.");
                            } else {
                                Main.warning("The method '" + method.getName()
                                        + "' in sinalgo.runtime.Global cannot be called from the dropdown menu, as it needs parameters to be called.\nThe method is not added to the menu.");
                            }
                            continue;
                        }
                        String text = isProjectSpecific
                                ? Global.customGlobal.includeGlobalMethodInMenu(method, info.menuText())
                                : info.menuText();
                        if (text == null) {
                            continue; // the method was dropped by the project
                        }
                        JMenuItem item = new JMenuItem(text);
                        item.addActionListener(GUI.this.globalInvoker);
                        GUI.this.methodsAndNames.put(item, method);

                        String subMenuText = info.subMenu();
                        if (subMenuText.equals("")) {
                            GUI.this.globalMenu.add(item);
                        } else {
                            JMenu menu = null;
                            for (JMenu m : subMenus) {
                                if (m.getText().equals(subMenuText)) {
                                    menu = m;
                                    break;
                                }
                            }
                            if (menu == null) {
                                menu = new JMenu(subMenuText);
                                subMenus.add(menu);
                                GUI.this.globalMenu.add(menu);
                            }
                            menu.add(item);
                        }
                        hasEntry = true;
                    }
                }
                return hasEntry;
            }

            @Override
            public void menuSelected(MenuEvent event) {
                GUI.this.globalMenu.removeAll();

                // add the project specific methods
                Method[] methods = Global.customGlobal.getClass().getMethods();
                if (this.testMethods(methods, true)) {
                    GUI.this.globalMenu.addSeparator();
                }

                // add the framework-side defined methods in sinalgo.runtime.Global
                try {
                    methods = Thread.currentThread().getContextClassLoader().loadClass("sinalgo.runtime.Global").getMethods();
                    if (this.testMethods(methods, false)) {
                        GUI.this.globalMenu.addSeparator();
                    }
                } catch (ClassNotFoundException e) {
                    throw new SinalgoFatalException("Could not find class sinalgo.runtime.Global to get the global gui methods from.");
                }

                // And finally the Settings and About dialog
                GUI.this.globalMenu.add(GUI.this.settingsMenuItem);

                // and set the font of the menu entries
                GUI.this.setMenuFont(GUI.this.globalMenu);
            }
        });

        menuBar.add(this.globalMenu);

        // ---------------------------------------------
        // View Menu
        // ---------------------------------------------
        this.viewMenu = new JMenu("View");
        this.viewMenu.setMnemonic(java.awt.event.KeyEvent.VK_V);
        this.viewMenu.add(this.viewZoomOutMenuItem);
        this.viewMenu.add(this.viewZoomInMenuItem);
        this.viewMenu.add(this.viewZoomFitMenuItem);
        this.viewMenu.add(this.viewFullScreenMenuItem);

        menuBar.add(this.viewMenu);

        // ---------------------------------------------
        // Help Menu
        // ---------------------------------------------
        this.helpMenu = new JMenu("Help");
        this.helpMenu.setMnemonic(java.awt.event.KeyEvent.VK_H);
        this.helpMenu.add(this.helpMenuItem);
        this.helpMenu.addSeparator();
        this.helpMenu.add(this.aboutMenuItem);
        menuBar.add(this.helpMenu);

        this.setMenuFont(menuBar);

        this.setJMenuBar(menuBar);

        // The content pane
        this.contentPane = new JPanel();

        this.graphPanel = new GraphPanel(this);
        // activate the Tooltip for the graphPanel. The "Default Tooltip" is actually
        // never shown
        // because the text to show is overwritten in the GraphPanel-Classes
        // getToolTipText(MouseEvent e)
        this.graphPanel.createToolTip();
        this.graphPanel.setToolTipText("Default Tooltip"); // to initialize, must set an arbitrary text
        this.graphPanel.requestDefaultViewOnNextDraw();

        if (Configuration.isExtendedControl()) {
            this.contentPane.setLayout(new BoxLayout(this.contentPane, BoxLayout.X_AXIS));
            this.controlPanel = new MaximizedControlPanel(this);
            this.contentPane.add(this.graphPanel);
            this.contentPane.add(this.controlPanel);
        } else {
            this.contentPane.setLayout(new BoxLayout(this.contentPane, BoxLayout.Y_AXIS));
            this.controlPanel = new MinimizedControlPanel(this);
            this.contentPane.add(this.controlPanel);
            this.contentPane.add(this.graphPanel);
        }

        this.add(this.contentPane);

        this.setVisible(true);
        // trigger a first paint (needed!)
        this.repaint();
    }

    private void setMenuFont(MenuElement m) {
        m.getComponent().setFont(this.menuFont);
        if (m.getSubElements().length > 0) {
            for (MenuElement e : m.getSubElements()) {
                this.setMenuFont(e);
            }
        }
    }

    private JPanel contentPane = null;

    /**
     * Switches between the two modes for the control panel depending on the boolean
     * parameter.
     *
     * @param toExtended if set true the control panel is set on Extended (
     */
    public void changePanel(boolean toExtended) {
        // STRANGE! we need to add the new contol panel before removing the old one
        // otherwise, the mouse scrolling wont be detected anymore.
        if (toExtended) { // from minimized to maximized
            this.contentPane.setLayout(new BoxLayout(this.contentPane, BoxLayout.X_AXIS));
            ControlPanel oldCP = this.controlPanel;
            this.controlPanel = new MaximizedControlPanel(this);
            this.contentPane.add(this.controlPanel, 2); // content pane must be after graph panel
            this.contentPane.remove(oldCP);
        } else { // from maximized to minimized
            this.contentPane.setLayout(new BoxLayout(this.contentPane, BoxLayout.Y_AXIS));
            ControlPanel oldCP = this.controlPanel;
            this.controlPanel = new MinimizedControlPanel(this);
            this.contentPane.add(this.controlPanel, 0); // content pane is first in list
            this.contentPane.remove(oldCP);
        }
        this.contentPane.revalidate();
        this.graphPanel.requireFullDrawOnNextPaint();
        this.repaint();
    }

    /**
     * This method resets the gui for the current configuration settings. This
     * method is called when the user removes all nodes.
     */
    public void allNodesAreRemoved() {
        this.graphPanel.allNodesAreRemoved();
    }

    /**
     * A boolean indicating whether the paint() method of this gui has been called
     * at least once. THe graph panel starts drawing itself only after this flag has
     * been set to true.
     */
    public boolean firstTimePainted = false;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        this.firstTimePainted = true;
        this.controlPanel.repaint();
        this.graphPanel.repaint();
    }

    /**
     * Returns the transformation instance that knows how to translate between the
     * logic coordinate system used by the simulation and the corresponding
     * coordinates on the GUI.
     *
     * @return The transformation instance.
     */
    public PositionTransformation getTransformator() {
        return this.runtime.getTransformator();
    }

    /**
     * This method redraws the Graph panel and the control panel.
     * <p>
     * Call this method whenever you require the graph to be redrawn immediately
     * (synchronously) within the simulation code. Note that this method draws the
     * graph immediately. I.e. to avoid concurrent access to the datastructures,
     * this method should only be called when no simulation round is executing.
     * <p>
     * The paint process of the graph panel is as following: First, the graph
     * (nodes, edges, background, ...) is painted onto an image. Then, the content
     * of the image is copied to the screen. Quite often, it is not necessary to
     * redraw the image, and we can paint again the 'old' image from the previous
     * call to paint the control panel. This is for example the case when the mouse
     * moved over a node or edge and triggered a tool tip window to be shown. When
     * the graph or transformation matrix changed, the image needs to be repainted.
     * <p>
     * This method forces a redraw of the image, which may be quite expensive for
     * huge graphs. Thus, call it only, when you really need a synchronous repaint
     * of the image.
     * <p>
     * In almost all cases, calling redrawGUI() is preferred.
     */
    public void redrawGUINow() {
        this.controlPanel.repaint();
        this.graphPanel.paintNow();
    }

    /**
     * Repaints the Control Panel and the Graph Panel. In contrast to redrawGUINow,
     * this method does not enforce the graph panel to be painted immediately, but
     * leaves it up to the JVM to schedule the repaint. Call this method whenever it
     * is not crucial to redraw the graph immediately.
     * <p>
     * <p>
     * The paint process of the graph panel is as following: First, the graph
     * (nodes, edges, background, ...) is painted onto an image. Then, the content
     * of the image is copied to the screen. Quite often, it is not necessary to
     * redraw the image, and we can paint again the 'old' image from the previous
     * call to paint the control panel. This is for example the case when the mouse
     * moved over a node or edge and triggered a tool tip window to be shown. When
     * the graph or transformation matrix changed, the image needs to be repainted.
     * <p>
     * This method forces a redraw of the image, which may be quite expensive for
     * huge graphs. Thus, call it only, when you really need the gui to be updated.
     *
     * @see GUI#redrawGUINow() for how the graph panel is drawn.
     */
    public void redrawGUI() {
        this.graphPanel.requireFullDrawOnNextPaint();
        this.controlPanel.repaint();
        this.graphPanel.repaint();
    }

    /**
     * Only redraw the control panel. This is used to redraw the controlpanel
     * (inclusive the small preview picture) without painting the whole graph.
     */
    public void redrawControl() {
        this.controlPanel.repaint();
    }

    /**
     * This method pops a Error message on the frame with the given message and the
     * given title.
     *
     * @param message The message to display.
     * @param title   The Title of the messageDialog.
     */
    public void popupErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method sets the start button of the control panel enabled according to
     * the boolean passed.
     *
     * @param b If b is true the start button is set enabled and the abort button
     *          is set false (and vice versa)
     */
    public void setStartButtonEnabled(boolean b) {
        this.controlPanel.setStartButtonEnabled(b);
        this.graphMenu.setEnabled(b);
        this.globalMenu.setEnabled(b);
        this.helpMenu.setEnabled(b);
        this.viewMenu.setEnabled(b);
        // We could disallow resizing the window here, but this flickers
        // setResizable(b);
    }

    /**
     * Resets the time performed and the number of events already executed.
     *
     * @param time        The time that passed by until the actual moment.
     * @param eventNumber The number of events that have been executed until now.
     */
    public void setRoundsPerformed(double time, int eventNumber) {
        this.controlPanel.setRoundsPerformed(time, eventNumber);
    }

    /**
     * This method changes the number of rounds to be displayed in the control
     * panel.
     *
     * @param i The number to be displayed in the control panel.
     */
    public void setRoundsPerformed(int i) {
        this.controlPanel.setRoundsPerformed(i);
    }

    /**
     * Sets the event that was processed last.
     *
     * @param e The event that was last processed, null if there was no event.
     */
    public void setCurrentlyProcessedEvent(Event e) {
        this.controlPanel.setCurrentEvent(e);
    }

    /**
     * Sets the mouse-position of the cursor.
     *
     * @param s A string representation of the position
     */
    public void setMousePosition(String s) {
        this.controlPanel.setMousePosition(s);
    }

    /**
     * Called when the user presses the button to remove all nodes from the
     * simulation.
     */
    public void clearAllNodes() {
        SinalgoRuntime.clearAllNodes();
    }

    /**
     * Opens a dialog that allows to add new nodes
     */
    public void addNodes() {
        this.genNodesDialog.compose(null);
    }

    /**
     * Opoens a dialog to specify the models to craete a node that is placed at the
     * specific position.
     *
     * @param pos The positino where the node will be placed.
     */
    public void addSingleNode(Position pos) {
        this.genNodesDialog.compose(pos);
    }

    /**
     * Creates a node with the default settings at the given position.
     *
     * @param pos The position
     */
    public void addSingleDefaultNode(Position pos) {
        this.genNodesDialog.generateDefaultNode(pos);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(this.generateMenuItem.getActionCommand())) {
            this.addNodes();
        } else if (e.getActionCommand().equals(this.clearMenuItem.getActionCommand())) {
            if (0 == JOptionPane.showConfirmDialog(this, "Do you really want to remove all nodes?", "Remove all nodes?",
                    JOptionPane.YES_NO_OPTION)) {
                this.clearAllNodes();
            }
        } else if (e.getActionCommand().equals(this.preferencesMenuItem.getActionCommand())) {
            new GraphPreferencesDialog(this);
        } else if (e.getActionCommand().equals(this.reevaluateMenuItem.getActionCommand())) {
            if (0 == JOptionPane.showConfirmDialog(this,
                    "Do you really want to reevaluate the connections of all nodes?", "Reevaluate Connections?",
                    JOptionPane.YES_NO_OPTION)) {
                SinalgoRuntime.reevaluateConnections();
                this.redrawGUI();
            }
        } else if (e.getActionCommand().equals(this.exportMenuItem.getActionCommand())) {
            try {
                new Exporter(this).export(new Rectangle(0, 0, this.graphPanel.getWidth(), this.graphPanel.getHeight()),
                        this.getTransformator());
            } catch (ExportException e1) {
                Main.minorError(e1.getMessage());
            }
        } else if (e.getActionCommand().equals(this.infoMenuItem.getActionCommand())) {
            new GraphInfoDialog(this);
        } else if (e.getActionCommand().equals(this.settingsMenuItem.getActionCommand())) {
            new GlobalSettingsDialog(this);
        } else if (e.getActionCommand().equals(this.aboutMenuItem.getActionCommand())) {
            new AboutDialog(this);
        } else if (e.getActionCommand().equals(this.helpMenuItem.getActionCommand())) {
            HelpDialog.showHelp(this); // start in a new thread
        } else if (e.getActionCommand().equals(this.exitMenuItem.getActionCommand())) {
            Main.exitApplication();
        } else if (e.getActionCommand().equals(this.viewFullScreenMenuItem.getActionCommand())) {
            this.toggleFullScreen();
        } else if (e.getActionCommand().equals(this.viewZoomInMenuItem.getActionCommand())) {
            this.zoomIn();
        } else if (e.getActionCommand().equals(this.viewZoomOutMenuItem.getActionCommand())) {
            this.zoomOut();
        } else if (e.getActionCommand().equals(this.viewZoomFitMenuItem.getActionCommand())) {
            this.getTransformator().zoomToFit(this.getGraphPanel().getWidth(), this.getGraphPanel().getHeight());
            this.setZoomFactor(this.getTransformator().getZoomFactor());
        }
    }

    // class used to invoke the global user-defined methods
    class GlobalInvoker implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {
            Method method = GUI.this.methodsAndNames.get(event.getSource());
            if (method == null) {
                throw new SinalgoFatalException("Cannot find method associated with menu item " + event.getActionCommand());
            }
            try {
                synchronized (GUI.this.getTransformator()) {
                    // synchronize it on the transformator to grant not to be concurrent with
                    // any drawing or modifying action
                    try {
                        method.invoke(Global.customGlobal, (Object[]) null);
                    } catch (IllegalArgumentException e) {
                        method.invoke(null, (Object[]) null);
                    }
                }
            } catch (IllegalArgumentException | SecurityException | InvocationTargetException | IllegalAccessException e) {
                Main.minorError(e);
            }
        }
    }
}
