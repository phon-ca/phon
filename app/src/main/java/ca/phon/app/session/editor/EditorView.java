package ca.phon.app.session.editor;

import java.lang.ref.WeakReference;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;

/**
 * A view in the {@link SessionEditor}.  Each editor view
 * displays a different view of the current session/record data.
 * 
 * 
 */
public abstract class EditorView extends JPanel implements IExtendable {
	
	private static final long serialVersionUID = 907723403385573953L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(EditorView.class, this);

	/**
	 * Preferred dock position
	 */
	private DockPosition preferredDockPosition = DockPosition.CENTER;
	
	/**
	 * Parent editor
	 * 
	 */
	private final WeakReference<SessionEditor> editorRef;
	
	public EditorView(SessionEditor editor) {
		super();
		editorRef = new WeakReference<SessionEditor>(editor);
		extSupport.initExtensions();
	}
	
	/**
	 * Get the parent editor
	 * 
	 * @return
	 */
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	/**
	 * View name
	 * 
	 * @return the view name
	 */
	public abstract String getName();
	
	/**
	 * View icon
	 * 
	 * @return view icon
	 */
	public abstract ImageIcon getIcon();
	
	/**
	 * Get the menu for the view (if any)
	 * 
	 * @return menu for the view or <code>null</code> if
	 *  this view does not have a menu
	 */
	public abstract JMenu getMenu();
	
	/**
	 * Gets the preferred dock position for the view
	 * 
	 * @return preferred dock position
	 */
	public DockPosition getPreferredDockPosition() {
		return this.preferredDockPosition;
	}
	
	/**
	 * Sets the preferred dock position for the view
	 * 
	 * @param dockPosition
	 */
	public void setPreferredDockPosition(DockPosition dockPosition) {
		this.preferredDockPosition = dockPosition;
	}

	public void initExtensions() {
		extSupport.initExtensions();
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
