/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.undo.UndoManager;

import org.apache.logging.log4j.LogManager;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CControlRegister;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CloseActionFactory;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CDockableLocationListener;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.action.CDecorateableAction;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.perspective.CDockablePerspective;
import bibliothek.gui.dock.common.perspective.CPerspective;
import bibliothek.gui.dock.common.perspective.SingleCDockablePerspective;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.themes.color.TitleColor;
import bibliothek.gui.dock.util.FocusedWindowProvider;
import bibliothek.gui.dock.util.Priority;
import bibliothek.gui.dock.util.color.ColorBridge;
import bibliothek.gui.dock.util.color.ColorManager;
import bibliothek.gui.dock.util.color.DockColor;
import bibliothek.util.Filter;
import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import ca.phon.app.log.LogUtil;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuManager;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DefaultEditorViewModel implements EditorViewModel {

	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(DefaultEditorViewModel.class.getName());

	/* Since there is not one but many main-Frames, it is hard to specify which one is the root-window. The
     * FocusedWindowProvider always assumes that the window that is or was focused is the root-window. */
	private FocusedWindowProvider windows = new FocusedWindowProvider();

	/**
	 * Dock control
	 */
	private CControl dockControl;

	/**
	 * Primary content area - one used for SessionEditor
	 */
	private CContentArea rootArea;

	/**
	 * Dockables
	 */
	private Map<String, CDockablePerspective> dockables;

	/**
	 * Views
	 */
	private final Map<String, EditorView> registeredViews =
			Collections.synchronizedMap(new HashMap<String, EditorView>());

	private final Map<String, JComponent> dynamicViews =
			Collections.synchronizedMap(new HashMap<>());

	/**
	 * View names by category
	 */
	private final Map<EditorViewCategory, List<String>> viewsByCategory =
			Collections.synchronizedMap(new TreeMap<EditorViewCategory, List<String>>());

	/**
	 * Weak reference to editor
	 */
	private final WeakReference<SessionEditor> editorRef;

	/**
	 * Editor view extension points
	 */
	private List<IPluginExtensionPoint<EditorView>> extPts;

	public DefaultEditorViewModel(SessionEditor editor) {
		super();

		editorRef = new WeakReference<SessionEditor>(editor);
		editor.addWindowListener(windowChangeListener);
		getDockControl();
	}

	private CControl getDockControl() {
		if(dockControl == null) {
			dockControl = new CControl( windows );
			windows.add(getEditor());
			setupDockControl();
			rootArea = dockControl.createContentArea( "root" );
		}
		return dockControl;
	}

	private void setupDockControl() {
		// theme
		dockControl.setTheme(ThemeMap.KEY_FLAT_THEME);

		// fix accelerators on non-mac systems
		if(!OSInfo.isMacOs()) {
			// fix accelerators for non-mac systems
			dockControl.putProperty( CControl.KEY_MAXIMIZE_CHANGE, KeyStroke.getKeyStroke( KeyEvent.VK_M, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK ) );
			dockControl.putProperty( CControl.KEY_GOTO_EXTERNALIZED, KeyStroke.getKeyStroke( KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK ) );
			dockControl.putProperty( CControl.KEY_GOTO_NORMALIZED, KeyStroke.getKeyStroke( KeyEvent.VK_N, InputEvent.CTRL_MASK | InputEvent.SHIFT_DOWN_MASK ) );
		}

		// setup factory
		dockControl.addSingleDockableFactory(dockableFilter, dockFactory);

		// fix title colours using substance theme on windows/linux
		final TitleColorBridge bridge = new TitleColorBridge();
		final ColorManager colorManager = dockControl.getController().getColors();
		colorManager.publish(Priority.CLIENT, TitleColor.KIND_TITLE_COLOR, bridge);
		colorManager.publish(Priority.CLIENT, TitleColor.KIND_FLAP_BUTTON_COLOR, bridge);
	}

	private SessionEditor getEditor() {
		return editorRef.get();
	}

	private List<IPluginExtensionPoint<EditorView>> getExtensionPoints() {
		if(extPts == null) {
			extPts = PluginManager.getInstance().getExtensionPoints(EditorView.class);
		}
		return extPts;
	}

	private Map<String, CDockablePerspective> getDockables() {
		if(dockables == null) {
			dockables = Collections.synchronizedMap(new TreeMap<String, CDockablePerspective>());
			setupDockables();
		}
		return dockables;
	}

	private void setupDockables() {
		for(IPluginExtensionPoint<EditorView> extPt:getExtensionPoints()) {
			final EditorViewInfo viewInfo = extPt.getClass().getAnnotation(EditorViewInfo.class);
			if(viewInfo == null) continue; // should never happen
			final String dockableName = viewInfo.name();
			List<String> categoryDockables = viewsByCategory.get(viewInfo.category());
			if(categoryDockables == null) {
				categoryDockables = new ArrayList<String>();
				viewsByCategory.put(viewInfo.category(), categoryDockables);
			}
			categoryDockables.add(dockableName);
			final SingleCDockablePerspective dockable =
					new SingleCDockablePerspective(dockableName);
			dockables.put(dockableName, dockable);
		}
	}

	private CDockable getViewDockable(String viewName) {
		CDockable retVal = null;
		final CControlRegister register = dockControl.getRegister();
		for(CDockable currentDockable:register.getDockables()) {
			if(currentDockable.intern().getTitleText().equals(viewName)) {
				retVal = currentDockable;
				break;
			}
		}
		return retVal;
	}

	@Override
	public Container getRoot() {
		return rootArea;
	}

	@Override
	public EditorView getView(String viewName) {

		EditorView retVal = registeredViews.get(viewName);
		if(retVal == null) {
			// attempt to load editor view
			for(IPluginExtensionPoint<EditorView> extPt:extPts) {
				final EditorViewInfo pluginAnnotation = extPt.getClass().getAnnotation(EditorViewInfo.class);
				if(pluginAnnotation != null && pluginAnnotation.name().equals(viewName)) {
					final IPluginExtensionFactory<EditorView> viewFactory = extPt.getFactory();
					try {
						retVal = viewFactory.createObject(getEditor());
						registeredViews.put(viewName, retVal);
					} catch (Exception e) {
						LOGGER.error( e.getLocalizedMessage(),
								e);
					}
					break;
				}
			}
		}
		return retVal;
	}

	@Override
	public JComponent getDynamicView(String viewName) {
		JComponent retVal = null;
		if(dynamicViews.containsKey(viewName)) {
			retVal = dynamicViews.get(viewName);
		}
		return retVal;
	}

	@Override
	public Action getCloseAction(String viewName) {
		final CDockable dockable = getViewDockable(viewName);
		if(dockable != null) {
			final CloseActionFactory factory = dockControl.getController().getProperties().get( CControl.CLOSE_ACTION_FACTORY );
			final CAction closeAct = factory.create(dockControl, dockable);

			final CActionWrapper wrapper = new CActionWrapper(dockable, closeAct);
			wrapper.putValue(CActionWrapper.NAME, "Close");
			return wrapper;
		}
		return null;
	}

	@Override
	public ImageIcon getViewIcon(String viewName) {
		final EditorView registeredView = registeredViews.get(viewName);
		if(registeredView != null) {
			return registeredView.getIcon();
		} else {
			for(IPluginExtensionPoint<EditorView> extPt:extPts) {
				final EditorViewInfo pluginAnnotation = extPt.getClass().getAnnotation(EditorViewInfo.class);
				if(pluginAnnotation != null && pluginAnnotation.name().equals(viewName)) {
					final String iconName = pluginAnnotation.icon();
					final ImageIcon icon = IconManager.getInstance().getIcon(iconName, IconSize.SMALL);
					return icon;
				}
			}
		}
		return null;
	}

	@Override
	public Set<String> getViewNames() {
		return getDockables().keySet();
	}

	@Override
	public Map<EditorViewCategory, List<String>> getViewsByCategory() {
		if(this.dockables == null) {
			getDockables();
		}
		return this.viewsByCategory;
	}

	@Override
	public boolean isShowing(String viewName) {
		boolean retVal = false;
		final CControlRegister register = dockControl.getRegister();
		for(CDockable currentDockable:register.getDockables()) {
			if(currentDockable.intern().getTitleText().equals(viewName)) {
				retVal = currentDockable.isVisible();
			}
		}
		return retVal;
	}

	@Override
	public void cleanup() {

		for(int i = 0; i < dockControl.getCDockableCount(); i++) {
			final CDockable dockable = dockControl.getCDockable(i);
			dockable.removeCDockableLocationListener(dockableLocationListener);
			dockControl.removeDockable((SingleCDockable)dockable);
		}

		dockControl.getController().kill();
		dockControl = null;

		registeredViews.clear();
		dockables.clear();

		windows.remove(getEditor());
	}

	@Override
	public void showView(String viewName) {
		if(isShowing(viewName)) {
			CDockable dockable = getViewDockable(viewName);
			if(dockable != null) {
				dockControl.getController().setAtLeastFocusedDockable(dockable.intern(), null);
			}
			return;
		}

		SingleCDockable dockable = dockControl.getSingleDockable(viewName);
		if(dockable == null) {
			final SingleCDockableFactory factory = dockControl.getSingleDockableFactory(viewName);
			dockable = factory.createBackup(viewName);
		}

		if(dockable != null) {
			dockControl.addDockable(dockable);

			final CommonModuleFrame focusedWindow = CommonModuleFrame.getCurrentFrame();
			CContentArea contentArea = rootArea;
			if(focusedWindow instanceof AccessoryWindow) {
				final AccessoryWindow accWin = (AccessoryWindow)focusedWindow;
				contentArea = accWin.getArea();
			}

			final CDockable focusedDockable = dockControl.getFocusedCDockable();
			if(focusedDockable != null) {
				dockable.setLocationsAside(focusedDockable);
			} else {
				final CLocation location = CLocation.base( contentArea ).normal();
				dockable.setLocation(location);
			}
			dockable.setVisible(true);

			savePreviousPerspective();

			getEditor().setJMenuBar(MenuManager.createWindowMenuBar(getEditor()));
			for(AccessoryWindow accWin:accessoryWindows) {
				accWin.setJMenuBar(MenuManager.createWindowMenuBar(accWin));
			}
		}
	}

	@Override
	public void hideView(String viewName) {
		if(!isShowing(viewName)) return;


	}

	@Override
	public void showDynamicFloatingDockable(String title, JComponent comp,
			int x, int y, int w, int h) {
		final DynamicViewFactory factory = new DynamicViewFactory(comp);
		final SingleCDockable dockable = factory.createBackup(title);

		dockControl.addDockable(dockable);
		dockControl.getLocationManager().setLocation(dockable.intern(), CLocation.external(x, y, w, h));
		dynamicViews.put(title, comp);
	}

	/**
	 * Delete given perspective.
	 *
	 * @param perspective
	 */
	public void onDeletePerspective(RecordEditorPerspective perspective) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(getEditor());
		props.setTitle("Delete Layout");
		props.setHeader("Delete Layout");
		props.setMessage("Delete layout '" + perspective.getName() + "'?");
		props.setOptions(MessageDialogProperties.okCancelOptions);
		props.setRunAsync(false);

		final int retVal = NativeDialogs.showMessageDialog(props);
		if(retVal == 0) {
			RecordEditorPerspective.deletePerspective(perspective);
			removePrespective(perspective);
		}
	}

	@Override
	public void setupWindows(RecordEditorPerspective editorPerspective) {
		final AccessoryWindow[] windows = accessoryWindows.toArray(new AccessoryWindow[0]);
		for(AccessoryWindow window:windows) {
			window.setVisible(false);
			accessoryWindows.remove(window);
			dockControl.removeStationContainer(window.getArea());
			window.dispose();
		}

		try(InputStream is = editorPerspective.getLocation().openStream()) {
			if(is != null) {
				final XElement xele = XIO.readUTF(is);

				final XElement boundsEle = xele.getElement("bounds");
				if(boundsEle != null) {
					int x = boundsEle.getAttribute("x").getInt();
					int y = boundsEle.getAttribute("y").getInt();
					int width = boundsEle.getAttribute("width").getInt();
					int height = boundsEle.getAttribute("height").getInt();
					final XAttribute extendedStateAttr = boundsEle.getAttribute("extendedState");
					int extendedState = JFrame.NORMAL;
					if(extendedStateAttr != null) {
						extendedState = extendedStateAttr.getInt();
					}

					if(width >= 0 && height >= 0) {
						getEditor().setSize(width, height);
					}
					getEditor().setLocation(x, y);
					getEditor().setExtendedState(extendedState);
				} else {
					if(!getEditor().isVisible())
						getEditor().cascadeWindow(CommonModuleFrame.getCurrentFrame());
				}

				final XElement windowsEle = xele.getElement("windows");
				if(windowsEle != null) {
					for(int i = 0; i < windowsEle.getElementCount(); i++) {
						final XElement winEle = windowsEle.getElement(i);

						final String uuid = winEle.getAttribute("uid").getString();

						final AccessoryWindow window = (AccessoryWindow)createAccessoryWindow(
								UUID.fromString(uuid));
						int x = winEle.getAttribute("x").getInt();
						int y = winEle.getAttribute("y").getInt();
						int width = winEle.getAttribute("width").getInt();
						int height = winEle.getAttribute("height").getInt();
						final XAttribute extendedStateAttr = winEle.getAttribute("extendedState");
						int extendedState = JFrame.NORMAL;
						if(extendedStateAttr != null) {
							extendedState = extendedStateAttr.getInt();
						}

						if(width >= 0 && height >= 0) {
							window.setSize(width, height);
						}
						window.setLocation(x, y);
						window.setExtendedState(extendedState);
						window.setVisible(true);
					}
				}
			}
		} catch (IOException e) {

		}
	}

	public void loadPerspective(RecordEditorPerspective editorPerspective) {
		setupWindows(editorPerspective);
		applyPerspective(editorPerspective);
	}

	@Override
	public void applyPerspective(RecordEditorPerspective editorPerspective) {
		CPerspective perspective = null;
		try(InputStream is = editorPerspective.getLocation().openStream()) {
			if(is != null) {
				final XElement xele = XIO.readUTF(is);
				perspective = dockControl.getPerspectives().readXML( xele );
			}
			dockControl.getPerspectives().setPerspective( editorPerspective.getName(), perspective);
			perspective.storeLocations();
			dockControl.load(editorPerspective.getName());

			getEditor().setJMenuBar(MenuManager.createWindowMenuBar(getEditor()));
			for(AccessoryWindow accWin:accessoryWindows) {
				accWin.setJMenuBar(MenuManager.createWindowMenuBar(accWin));
			}
		} catch (IOException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void savePerspective(RecordEditorPerspective editorPerspective) {
		if(dockControl == null) return;
		final CPerspective perspective = dockControl.getPerspectives().getPerspective(true);
		if(perspective != null) {
			try {
				final XElement root = new XElement("root");

				// add position of window as attributes
				final XElement rootBoundsEle = root.addElement("bounds");
				writeBoundsInfo(rootBoundsEle, getEditor());

				final XElement accessoryWindowsEle = root.addElement("windows");
				for(AccessoryWindow window:accessoryWindows) {
					final XElement winEle = accessoryWindowsEle.addElement("window");
					final XAttribute uuid = new XAttribute("uid");
					uuid.setString(window.uuid.toString());
					winEle.addAttribute(uuid);

					writeBoundsInfo(winEle, window);
				}

				dockControl.getPerspectives().writeXML(root, perspective);

				final File f = new File(editorPerspective.getLocation().toURI());
				XIO.writeUTF(root, new FileOutputStream(f));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeBoundsInfo(XElement rootBoundsEle, JFrame frame) {
		final XAttribute xAttr = new XAttribute("x");
		xAttr.setInt(frame.getX());
		rootBoundsEle.addAttribute(xAttr);

		final XAttribute yAttr = new XAttribute("y");
		yAttr.setInt(frame.getY());
		rootBoundsEle.addAttribute(yAttr);

		final XAttribute widthAttr = new XAttribute("width");
		widthAttr.setInt(frame.getWidth());
		rootBoundsEle.addAttribute(widthAttr);

		final XAttribute heightAttr = new XAttribute("height");
		heightAttr.setInt(frame.getHeight());
		rootBoundsEle.addAttribute(heightAttr);

		final XAttribute extendedStateAttr = new XAttribute("extendedState");
		extendedStateAttr.setInt(frame.getExtendedState());
		rootBoundsEle.addAttribute(extendedStateAttr);
	}

	private void savePreviousPerspective() {
		// XXX Only save previous perspective when running as
		// a 'Session Editor' window
		if(!getEditor().getTitle().startsWith("Session Editor")) return;

		final File prevPerspetiveFile = new File(RecordEditorPerspective.PERSPECTIVES_FOLDER,
				RecordEditorPerspective.LAST_USED_PERSPECTIVE_NAME + ".xml");
		try {
			final RecordEditorPerspective prevPerspective =
					new RecordEditorPerspective(RecordEditorPerspective.LAST_USED_PERSPECTIVE_NAME,
							prevPerspetiveFile.toURI().toURL());
			savePerspective(prevPerspective);
		} catch (MalformedURLException e1) {
			LOGGER.error(e1.getLocalizedMessage(), e1);
		}
	}

	@Override
	public void removePrespective(RecordEditorPerspective editorPerspective) {
		dockControl.getPerspectives().removePerspective(editorPerspective.getName());
	}

	// filter for dockable factory
	private final Filter<String> dockableFilter = new Filter<String>() {

		@Override
		public boolean includes(String item) {
			return getDockables().containsKey(item);
		}

	};

	// dockable factory
	private final SingleCDockableFactory dockFactory = new SingleCDockableFactory() {

		@Override
		public SingleCDockable createBackup(String id) {
			SingleCDockable retVal = null;
			final EditorView editorView = getView(id);
			if(editorView != null) {
				retVal = new EditorViewDockable(editorView.getName(), editorView, new CAction[0]);
				retVal.addCDockableLocationListener(dockableLocationListener);
			}
			return retVal;
		}

	};

	/**
	 * View factory for dynamically added dockables.
	 */
	private class DynamicViewFactory implements SingleCDockableFactory {

		private JComponent content;

		public DynamicViewFactory(JComponent content) {
			this.content = content;
		}

		@Override
		public SingleCDockable createBackup(String arg0) {
			final String title = arg0;
			final DefaultSingleCDockable retVal = new DefaultSingleCDockable(title, null, title, content, new CAction[0]);
			retVal.setCloseable(true);
			retVal.addCDockableStateListener(new CDockableStateListener() {

				@Override
				public void visibilityChanged(CDockable arg0) {
					if(!arg0.isVisible()) {
						dockControl.removeDockable(retVal);
						dynamicViews.remove(title);
					}
				}

				@Override
				public void extendedModeChanged(CDockable arg0, ExtendedMode arg1) {
				}
			});
			return retVal;
		}

	}

	// dockable wrapper for editor views
	private class EditorViewDockable extends DefaultSingleCDockable {

		private final WeakReference<EditorView> viewRef;

		public EditorViewDockable(String id, EditorView editorView, CAction[] actions) {
			super(id, editorView.getIcon(), editorView.getName(), editorView, actions);
			super.setCloseable(true);

			this.addVetoClosingListener(new CVetoClosingListener() {

				@Override
				public void closing(CVetoClosingEvent arg0) {

				}

				@Override
				public void closed(CVetoClosingEvent arg0) {
					getView().onClose();
				}

			});

			final SimpleButtonAction externalizeAct = new SimpleButtonAction();
			externalizeAct.setText("Open view in new window");
			externalizeAct.setIcon(IconManager.getInstance().getIcon("actions/externalize-to-window", IconSize.SMALL));
			externalizeAct.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					final AccessoryWindow window = (AccessoryWindow)createAccessoryWindow(UUID.randomUUID());
					window.getArea().getCenter().drop(EditorViewDockable.this.intern());
					window.pack();
					window.setVisible(true);
				}

			});

			final DefaultDockActionSource actionSource = new DefaultDockActionSource(
					new LocationHint( LocationHint.DOCKABLE, LocationHint.RIGHT ));
			actionSource.add(externalizeAct);
			super.intern().setActionOffers(actionSource);


			viewRef = new WeakReference<EditorView>(editorView);
		}

		@SuppressWarnings("unused")
		public EditorView getView() {
			return viewRef.get();
		}

	}

	// wrapp class for CActions
	private class CActionWrapper extends AbstractAction {

		private static final long serialVersionUID = -4913295698177388752L;

		// dockable
		private final CDockable dockable;

		// action
		private final CAction action;

		public CActionWrapper(CDockable dockable, CAction action) {
			super();
			this.dockable = dockable;
			this.action = action;

			if(action instanceof CDecorateableAction) {
				final CDecorateableAction<?> decAct = (CDecorateableAction<?>)action;
				putValue(NAME, decAct.getText());
				putValue(SMALL_ICON, decAct.getIcon());
				putValue(ACCELERATOR_KEY, decAct.getAccelerator());
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			action.intern().trigger(dockable.intern());
		}

	}

	private class TitleColorBridge implements ColorBridge {

		@Override
		public void add(String id, DockColor uiValue) {
		}

		@Override
		public void remove(String id, DockColor uiValue) {
		}

		@Override
		public void set(String id, Color value, DockColor uiValue) {
			switch(id) {
			case "title.flap.active.text":
			case "title.active.text":
				uiValue.set(UIManager.getColor("activeCaptionText"));
				break;

			case "title.flap.inactive.text":
			case "title.inactive.text":
				uiValue.set(UIManager.getColor("inactiveCaptionText"));
				break;

			case "title.flap.active.left":
			case "title.active.left":
				uiValue.set(UIManager.getColor("activeCaption"));
				break;

			case "title.flap.active.right":
			case "title.active.right":
				uiValue.set(UIManager.getColor("activeCaption"));
				break;

			case "title.flap.inactive.left":
			case "title.inactive.left":
				uiValue.set(UIManager.getColor("control"));
				break;

			case "title.flap.inactive.right":
			case "title.inactive.right":
				uiValue.set(UIManager.getColor("control"));
				break;
			}
		}
	}

	@Override
	public void setupViewMenu(MenuElement ele) {
		final Map<EditorViewCategory, List<String>> viewsByCategory =
				getViewsByCategory();
		for(EditorViewCategory category:viewsByCategory.keySet()) {
			final JMenuItem categoryItem = new JMenuItem("-- " + category.title + " --");
			categoryItem.setEnabled(false);
			if(ele.getComponent() instanceof JMenu) {
				final JMenu menu = (JMenu)ele;
				menu.add(categoryItem);
			} else if(ele.getComponent() instanceof JPopupMenu) {
				final JPopupMenu menu = (JPopupMenu)ele;
				menu.add(categoryItem);
			}

			for(String view:viewsByCategory.get(category)) {
				final PhonUIAction toggleViewAct = new PhonUIAction(view, this, "showView", view);
				toggleViewAct.putValue(PhonUIAction.SMALL_ICON, getViewIcon(view));

				JComponent viewItem = new JMenuItem(toggleViewAct);

				if(isShowing(view)) {
					JMenu menu = getView(view).getMenu();
					if(menu != null) {
						menu.addSeparator();
					} else {
						menu = new JMenu();
					}
					menu.setText(toggleViewAct.getValue(PhonUIAction.NAME).toString());
					menu.setIcon(getViewIcon(view));
					final Action closeAct = getCloseAction(view);
					menu.add(closeAct);

					viewItem = menu;
				}

				if(ele.getComponent() instanceof JMenu) {
					final JMenu menu = (JMenu)ele;
					menu.add(viewItem);
				} else if(ele.getComponent() instanceof JPopupMenu) {
					final JPopupMenu menu = (JPopupMenu)ele;
					menu.add(viewItem);
				}
			}
		}
	}

	@Override
	public void setupPerspectiveMenu(MenuElement menuElement) {
		final JMenu layoutMenu = new JMenu("Load layout");
		ImageIcon loadLayoutIcon = IconManager.getInstance().getIcon("actions/layout-content", IconSize.SMALL);
		layoutMenu.setIcon(loadLayoutIcon);
		layoutMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				layoutMenu.removeAll();
				setupLayoutMenu(layoutMenu);
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		if(menuElement instanceof JPopupMenu)
			((JPopupMenu)menuElement).add(layoutMenu);
		else if(menuElement instanceof JMenu)
			((JMenu)menuElement).add(layoutMenu);

		final JMenu deleteMenu = new JMenu("Delete layout");
		ImageIcon deleteLayoutIcon = IconManager.getInstance().getIcon("actions/layout-delete", IconSize.SMALL);
		deleteMenu.setIcon(deleteLayoutIcon);
		deleteMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent arg0) {
				setupDeleteLayoutMenu(deleteMenu);
			}

			@Override
			public void menuDeselected(MenuEvent arg0) {
			}

			@Override
			public void menuCanceled(MenuEvent arg0) {
			}
		});

		if(menuElement instanceof JPopupMenu)
			((JPopupMenu)menuElement).add(deleteMenu);
		else if(menuElement instanceof JMenu)
			((JMenu)menuElement).add(deleteMenu);

		// save current layout
		final PhonUIAction saveLayoutAct = new PhonUIAction(this, "onSaveLayout");
		ImageIcon saveLayoutIcon = IconManager.getInstance().getIcon("actions/layout-add", IconSize.SMALL);
		saveLayoutAct.putValue(PhonUIAction.SMALL_ICON, saveLayoutIcon);
		saveLayoutAct.putValue(PhonUIAction.NAME, "Save current layout...");
		saveLayoutAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save current layout as a preset.");
		final JMenuItem saveLayoutItem = new JMenuItem(saveLayoutAct);

		if(menuElement instanceof JPopupMenu)
			((JPopupMenu)menuElement).add(saveLayoutItem);
		else if(menuElement instanceof JMenu)
			((JMenu)menuElement).add(saveLayoutItem);
	}

	/**
	 * Adds a menu item for all available editor perspectives.
	 *
	 * @param menu
	 */
	@Override
	public void setupLayoutMenu(MenuElement menu) {
		if(menu.getComponent() instanceof JMenu) {
			((JMenu)menu).removeAll();
		} else if(menu.getComponent() instanceof JPopupMenu) {
			((JPopupMenu)menu).removeAll();
		}

		final Consumer<RecordEditorPerspective> addPerspectiveToMenu = (editorPerspective) -> {
			final PhonUIAction showPerspectiveAct = new PhonUIAction(this, "loadPerspective", editorPerspective);
			showPerspectiveAct.putValue(PhonUIAction.NAME, editorPerspective.getName());
			showPerspectiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Load perspective: " + editorPerspective.getName());
			final JMenuItem showPerspectiveItem = new JMenuItem(showPerspectiveAct);

			if(menu.getComponent() instanceof JMenu) {
				final JMenu m = (JMenu)menu;
				m.add(showPerspectiveItem);
			} else if(menu.getComponent() instanceof JPopupMenu) {
				final JPopupMenu m = (JPopupMenu)menu;
				m.add(showPerspectiveItem);
			}
		};

		final Iterator<RecordEditorPerspective> stockItr = RecordEditorPerspective.getStockPerspectives().iterator();
		while(stockItr.hasNext()) {
			addPerspectiveToMenu.accept(stockItr.next());
		}


		final PhonUIAction showPerspectivesFolderAct = new PhonUIAction(this, "onShowLayoutFolder");
		showPerspectivesFolderAct.putValue(PhonUIAction.NAME, "-- User Library --");
		showPerspectivesFolderAct.putValue(PhonUIAction.SHORT_DESCRIPTION, RecordEditorPerspective.PERSPECTIVES_FOLDER.getAbsolutePath());
		final JMenuItem showFolderItem = new JMenuItem(showPerspectivesFolderAct);
		showFolderItem.setFont(showFolderItem.getFont().deriveFont(Font.BOLD));
		if(menu.getComponent() instanceof JMenu) {
			((JMenu)menu).addSeparator();
			((JMenu)menu).add(showFolderItem);
		} else if(menu.getComponent() instanceof JPopupMenu) {
			((JPopupMenu)menu).addSeparator();
			((JPopupMenu)menu).add(showFolderItem);
		}


		final Iterator<RecordEditorPerspective> userItr = RecordEditorPerspective.getUserPerspectives().iterator();
		while(userItr.hasNext()) {
			addPerspectiveToMenu.accept(userItr.next());
		}
	}

	public void onShowLayoutFolder() {
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(RecordEditorPerspective.PERSPECTIVES_FOLDER);
			} catch (IOException e) {
				LogUtil.warning(e);
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

	/**
	 * Add a menu to delete user-defined editor perspectives.
	 *
	 * @param ele
	 */
	private void setupDeleteLayoutMenu(MenuElement ele) {
		if(ele.getComponent() instanceof JMenu) {
			((JMenu)ele).removeAll();
		} else if(ele.getComponent() instanceof JPopupMenu) {
			((JPopupMenu)ele).removeAll();
		}
		for(RecordEditorPerspective editorPerspective:RecordEditorPerspective.availablePerspectives()) {
			try {
				final File perspectiveFile = new File(editorPerspective.getLocation().toURI());
				if(perspectiveFile.canWrite()) {
					// add delete item
					final PhonUIAction delPerspectiveAct =
							new PhonUIAction(this, "onDeletePerspective", editorPerspective);
					delPerspectiveAct.putValue(PhonUIAction.NAME, editorPerspective.getName());
					delPerspectiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Delete layout " + editorPerspective.getName());
					final JMenuItem delPerspectiveItem = new JMenuItem(delPerspectiveAct);

					if(ele.getComponent() instanceof JMenu) {
						final JMenu menu = (JMenu)ele;
						menu.add(delPerspectiveItem);
					} else if(ele.getComponent() instanceof JPopupMenu) {
						final JPopupMenu menu = (JPopupMenu)ele;
						menu.add(delPerspectiveItem);
					}
				}
			} catch (URISyntaxException e) {

			} catch (IllegalArgumentException e) {
				// thrown when URI is not heirarchical (i.e., is in a jar)
			}
		}
	}

	public void onSaveLayout() {
		// get a perspective name
		final String layoutName = JOptionPane.showInputDialog("Enter layout name:");

		final MessageDialogProperties props = new MessageDialogProperties();
		props.setTitle("Unable to save layout");
		props.setHeader("Unable to save layout");
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setOptions(MessageDialogProperties.okOptions);
		if(layoutName == null || layoutName.trim().length() == 0) {
			props.setMessage("You must enter a layout name");
			NativeDialogs.showMessageDialog(props);
			return;
		}
		if(RecordEditorPerspective.getPerspective(layoutName) != null) {
			props.setMessage("Layout named " + layoutName + " already exists");
			NativeDialogs.showMessageDialog(props);
			return;
		}

		final File perspectiveFile = new File(RecordEditorPerspective.PERSPECTIVES_FOLDER, layoutName + ".xml");
		try {
			final RecordEditorPerspective perspective = new RecordEditorPerspective(layoutName, perspectiveFile.toURI().toURL());
			savePerspective(perspective);
		} catch (MalformedURLException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
	}

	private final WindowListener windowChangeListener = new WindowListener() {

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			savePreviousPerspective();
		}

		@Override
		public void windowClosed(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}
	};

	private final CDockableLocationListener dockableLocationListener = new CDockableLocationListener() {

		@Override
		public void changed(CDockableLocationEvent event) {
			savePreviousPerspective();
		}

	};

	public CommonModuleFrame createAccessoryWindow(UUID uuid) {
		final AccessoryWindow retVal = new AccessoryWindow(uuid);
		return retVal;
	}

	private List<AccessoryWindow> accessoryWindows = new ArrayList<AccessoryWindow>();
	private class AccessoryWindow extends CommonModuleFrame {

		private static final long serialVersionUID = -8637239684975912272L;

		private CContentArea contentArea;

		private UUID uuid;

		public AccessoryWindow() {
			this(UUID.randomUUID());
		}

		public AccessoryWindow(UUID uuid) {
			super();

			this.uuid = uuid;

			setWindowName("(" + (accessoryWindows.size()+1) + ") " + getEditor().getWindowName());

			setShowInWindowMenu(false);
			setParentFrame(getEditor());

			putExtension(Project.class, getEditor().getProject());
			putExtension(UndoManager.class, getEditor().getUndoManager());

			accessoryWindows.add(this);

			addWindowListener(new WindowListener() {

				@Override
				public void windowOpened(WindowEvent e) {
				}

				@Override
				public void windowIconified(WindowEvent e) {
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
				}

				@Override
				public void windowClosing(WindowEvent e) {
					accessoryWindows.remove(AccessoryWindow.this);
					dockControl.removeStationContainer(getArea());
				}

				@Override
				public void windowClosed(WindowEvent e) {
					accessoryWindows.remove(AccessoryWindow.this);
					if(dockControl != null)
						dockControl.removeStationContainer(getArea());
				}

				@Override
				public void windowActivated(WindowEvent e) {

				}
			});
			init();
		}

		@Override
		public void setJMenuBar(JMenuBar menuBar) {
			getEditor().setupMenu(menuBar);
			super.setJMenuBar(menuBar);
		}

		@Override
		public String getTitle() {
			final Session session = getEditor().getSession();
			String retVal = "(" + (accessoryWindows.indexOf(this)+1) + ") Session Editor";
			if(session != null) {
				retVal += " : " + session.getCorpus() + "." + session.getName();
				if(getEditor().hasUnsavedChanges())
					retVal += "*";
			}
			return retVal;
		}


		private void init() {
			setLayout(new BorderLayout());

			SessionEditorToolbar toolbar = new SessionEditorToolbar(getEditor());

			add(toolbar, BorderLayout.NORTH);

			contentArea = dockControl.createContentArea(uuid.toString());

			add(contentArea, BorderLayout.CENTER);
		}

		public CContentArea getArea() {
			return this.contentArea;
		}

	}

}
