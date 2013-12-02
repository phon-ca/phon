package ca.phon.app.session.editor;

import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CControlRegister;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.SingleCDockableFactory;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.perspective.CDockablePerspective;
import bibliothek.gui.dock.common.perspective.CPerspective;
import bibliothek.gui.dock.common.perspective.SingleCDockablePerspective;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.util.Filter;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.PluginManager;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;

public class DefaultEditorViewModel implements EditorViewModel {

	/**
	 * Dock control
	 */
	private CControl dockControl;
	
	/**
	 * Dockables
	 */
	private Map<String, CDockablePerspective> dockables;
	
	/**
	 * Views
	 */
	private final Map<String, EditorView> registeredViews =
			Collections.synchronizedMap(new HashMap<String, EditorView>());
	
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
		
		editorRef = new WeakReference<>(editor);
	}
	
	private CControl getDockControl() {
		if(dockControl == null) {
			dockControl = new CControl(getEditor());
			setupDockControl();
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
		
		// icons
		
		// setup factory
		dockControl.addSingleDockableFactory(dockableFilter, dockFactory);
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
				categoryDockables = new ArrayList<>();
				viewsByCategory.put(viewInfo.category(), categoryDockables);
			}
			categoryDockables.add(dockableName);
			final SingleCDockablePerspective dockable =
					new SingleCDockablePerspective(dockableName);
			dockables.put(dockableName, dockable);
		}
	}
	
	@Override
	public Container getRoot() {
		return getDockControl().getContentArea();
	}

	@Override
	public EditorView getView(String viewName) {

		EditorView retVal = registeredViews.get(viewName);
		if(retVal == null) {
			// attempt to load editor view
			for(IPluginExtensionPoint<EditorView> extPt:extPts) {
				final PhonPlugin pluginAnnotation = extPt.getClass().getAnnotation(PhonPlugin.class);
				if(pluginAnnotation != null && pluginAnnotation.name().equals(viewName)) {
					final IPluginExtensionFactory<EditorView> viewFactory = extPt.getFactory();
					try {
						retVal = viewFactory.createObject(getEditor());
						registeredViews.put(viewName, retVal);
					} catch (Exception e) {}
					break;
				}
			}
		}
		return retVal;
	}

	@Override
	public Set<String> getViewNames() {
		return getDockables().keySet();
	}
	
	@Override
	public Map<EditorViewCategory, List<String>> getViewsByCategory() {
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
	public void showView(String viewName) {
		if(isShowing(viewName)) return;
		
		SingleCDockable dockable = dockControl.getSingleDockable(viewName);
		if(dockable == null) {
			final SingleCDockableFactory factory = dockControl.getSingleDockableFactory(viewName);
			dockable = factory.createBackup(viewName);
		}
		
		final EditorView editorView = getView(viewName);
		
		if(dockable != null) {
			final DockPosition position = (editorView == null ? DockPosition.CENTER : editorView.getPreferredDockPosition());
			final CLocation location = locationFromPosition(position);
			dockControl.addDockable(dockable);
			
			switch(position) {
			case NORTH:
				dockControl.getContentArea().getNorth().add(dockable.intern());
				break;
				
			case SOUTH:
				dockControl.getContentArea().getSouth().add(dockable.intern());
				break;
				
			case CENTER:
				dockControl.getContentArea().getCenter().addDockable(dockable.intern());
				break;
				
			case WEST:
				dockControl.getContentArea().getWest().add(dockable.intern());
				break;
				
			case EAST:
				dockControl.getContentArea().getEast().add(dockable.intern());
				break;
				
			default:
				break;
			}
//			dockControl.getLocationManager().setLocation(dockable.intern(), location);
		}
	}

	@Override
	public void hideView(String viewName) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void showDynamicFloatingDockable(String title, JComponent comp,
			int x, int y, int w, int h) {
		final DynamicViewFactory factory = new DynamicViewFactory(comp);
		final SingleCDockable dockable = factory.createBackup(title);
		
		dockControl.addDockable(dockable);
		dockControl.getLocationManager().setLocation(dockable.intern(), CLocation.external(x, y, w, h));
	}
	
	private CLocation locationFromPosition(DockPosition position) {
		CLocation retVal = CLocation.base().normal();
		
		switch(position) {
		case CENTER:
			break;
			
		case EAST:
			retVal = CLocation.base().normalEast(DockPosition.EAST.getSize());
			break;
			
		case WEST:
			retVal = CLocation.base().normalWest(DockPosition.WEST.getSize());
			break;
			
		case NORTH:
			retVal = CLocation.base().normalNorth(DockPosition.NORTH.getSize());
			break;
			
		case SOUTH:
			retVal = CLocation.base().normalSouth(DockPosition.SOUTH.getSize());
			break;
			
		default:
			break;
		}
		
		return retVal;
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
			removePrespective(perspective);
			RecordEditorPerspective.deletePerspective(perspective);
		}
	}
	
	@Override
	public void applyPerspective(RecordEditorPerspective editorPerspective) {
		if(dockControl.getPerspectives().getPerspective(editorPerspective.getName()) == null) {
			try {
				final InputStream is = editorPerspective.getLocation().openStream();
				
				if(is != null) {
					final XElement xele = XIO.readUTF(is);
					final CPerspective perspective = dockControl.getPerspectives().readXML( xele );
					
					dockControl.getPerspectives().setPerspective( editorPerspective.getName(), perspective);
					perspective.storeLocations();
				}
			} catch (IOException e) {
				
			}
		}
		dockControl.load(editorPerspective.getName());
	}
	
	@Override
	public void savePerspective(RecordEditorPerspective editorPerspective) {
		final CPerspective perspective = dockControl.getPerspectives().getPerspective(true);
		if(perspective != null) {
			try {
				final XElement root = new XElement("root");
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
			viewRef = new WeakReference<EditorView>(editorView);
		}
		
		public EditorView getView() {
			return viewRef.get();
		}
		
	}
	
	
}
