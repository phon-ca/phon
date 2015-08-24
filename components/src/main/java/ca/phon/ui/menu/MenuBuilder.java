package ca.phon.ui.menu;

import java.awt.MenuContainer;
import java.lang.ref.WeakReference;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import ca.phon.util.Tuple;

/*
 * TODO finish javadoc
 */
/**
 * <p>Helper class for building menus.  Menus are 'addressed' using paths.  Paths are
 * alphanumeric sequences separated by '/'.  Each section of the path is path of the 
 * menu hierarchy, with the final item nameing the terminating menu/menu item.</p>
 * 
 * <p>Example paths:<br/>
 * <ul>
 * <li><code>File</code></br>
 * The file menu.
 * </li>
 * <li><code>View/Record Data/My Item</code><br/>
 * The item with name 'My Item' in the View/Record Data' menu.</li>
 * </ul>
 * </p>
 * 
 * <p>Menu builders may be attached to existing menus, in which case existing
 * menu items may be addressed using the scheme above.  Menu items are typically
 * added 'before' or 'after' existing items.</p>
 * 
 * 
 */
public final class MenuBuilder {
	
	private WeakReference<MenuElement> rootRef;
	
	public MenuBuilder() {
		this(new JMenuBar());
	}
	
	public MenuBuilder(MenuElement root) {
		super();
		
		this.rootRef = new WeakReference<>(root);
	}
	
	public MenuElement getRoot() {
		return rootRef.get();
	}
	
	/**
	 * <p>Add the specified menu item to the end of the last menu
	 * in the path.<br/>
	 * 
	 * @param menuPath
	 * @param item
	 * 
	 * @return the builder
	 */
	public MenuBuilder addItem(String path, JMenuItem menuItem) {
		final JMenu menu = getMenu(path, true);
		menu.add(path);
		
		return this;
	}

	/**
	 * Get the menu specified by path.  Creates menu if
	 * requested.
	 * 
	 * @param path
	 * @param createMenu
	 * @return the menu
	 */
	public JMenu getMenu(String path, boolean createMenu) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		
		
		return null;
	}
	
	public JMenu addMenu(String path, String text) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		final String name = path.substring(deepest.getObj1().length());

		JMenu ret = null;
		if(name.indexOf('/') == -1) {
			if(deepest.getObj2() instanceof JMenu) {
				ret = new JMenu(text);
				ret.setName(name);
				((JMenu)deepest.getObj2()).add(ret);
			} else if(deepest.getObj2() instanceof JPopupMenu) {
				ret = new JMenu(text);
				ret.setName(name);
				ret.setIcon(null);
				((JPopupMenu)deepest.getObj2()).add(ret);
			} else if(deepest.getObj2() instanceof JMenuBar) {
				ret = new JMenu(text);
				ret.setName(name);
				((JMenuBar)deepest.getObj2()).add(ret);
			}
		}
		return ret;
	}

	public JMenuItem addMenuItem(String path, Action action) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		final String name = path.substring(deepest.getObj1().length());

		JMenuItem ret = null;
		if(name.indexOf('/') == -1) {
			if(deepest.getObj2() instanceof JMenu) {
				ret = new JMenuItem(action);
				ret.setName(name);
				ret.setIcon(null);
				((JMenu)deepest.getObj2()).add(ret);
			} else if(deepest.getObj2() instanceof JPopupMenu) {
				ret = new JMenuItem(action);
				ret.setName(name);
				ret.setIcon(null);
				((JPopupMenu)deepest.getObj2()).add(ret);
			} else if(deepest.getObj2() instanceof JMenuBar) {
				ret = new JMenuItem(action);
				ret.setName(name);
				ret.setIcon(null);
				((JMenuBar)deepest.getObj2()).add(ret);
			}
		}
		return ret;
	}
	
	private Tuple<String, MenuElement> getDeepestMenuElement(MenuElement elem, String path) {
		int position = 0;
		if(elem != null && path != null) {
			final String [] components = path.split("/");
			int index = 0;

			// Go as deep as we can go
			while(index < components.length) {
				final int oldIndex = index;
				for(MenuElement subelem : elem.getSubElements()) {
					if(components[index].equals(subelem.getComponent().getName())) {
						position += components[index].length() + 1;
						++index;
						elem = subelem;
						break;
					}
				}

				// If we didn't move, stop
				if(index == oldIndex)
					break;
			}

			if(index == components.length)
				--position;
		}

		return new Tuple<String, MenuElement>(path.substring(0, position), elem);
	}
	
}
