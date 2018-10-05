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
package ca.phon.ui.menu;

import java.awt.Component;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;

import ca.phon.util.Tuple;

/**
 * <p>Helper class for building menus.  Menus items are 'addressed' using paths.  Paths are
 * alphanumeric sequences separated by '/'.  Each section of the path is path of the
 * menu hierarchy, with the final item naming the terminating menu/menu item.</p>
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
 * <p>When adding a {@link MenuElement}, location of the element may be specified in the path
 * by adding the '@' token followed by
 * <ul>
 * <li><code>^</code> - place new element at beginning of this menu</li>
 * <li><code>$</code> - place new element at end of the menu (default behaviour)</li>
 * <li><i>item name</i> - place after given item name (or end if not found)</li>
 * </ul>
 *
 * E.g., To add a new item to the beginning of the File menu, use the path
 * <code>File@^</code> in the path given to the {@link #addMenu(String, String)} and
 * {@link #addItem(String, Action)} methods.</p>
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

	public JMenu addMenu(String path, String text) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		final MenuElement elem = deepest.getObj2();
		int insertIdx = getInsertIndex(elem, deepest.getObj1());

		return addMenu(elem, insertIdx, text);
	}
	
	public JMenu getMenu(String path) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		if(deepest.getObj2() instanceof JMenu) {
			return (JMenu)deepest.getObj2();
		} else {
			return null;
		}
	}

	public JMenu addMenu(MenuElement elem, int insertIdx, String text) {
		JMenu ret = null;

		ret = new JMenu(text);
		ret.setName(text);
		if(elem instanceof JMenu) {
			if(insertIdx >= 0)
				((JMenu)elem).add(ret, insertIdx);
			else
				((JMenu)elem).add(ret);
		} else if(elem instanceof JPopupMenu) {
			if(insertIdx >= 0)
				((JPopupMenu)elem).add(ret, insertIdx);
			else
				((JPopupMenu)elem).add(ret);
		} else if(elem instanceof JMenuBar) {
			if(insertIdx >= 0)
				((JMenuBar)elem).add(ret, insertIdx);
			else
				((JMenuBar)elem).add(ret);
		}
		return ret;
	}

	public void addSeparator(String path, String sepName) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		final MenuElement elem = deepest.getObj2();

		int insertIdx = getInsertIndex(elem, deepest.getObj1());
		if(elem instanceof JMenu) {
			if(insertIdx < 0) {
				((JMenu)elem).addSeparator();
				insertIdx = ((JMenu)elem).getItemCount()-1;
			} else
				((JMenu)elem).insertSeparator(insertIdx);
			final Component comp = ((JMenu)elem).getMenuComponent(insertIdx);
			if(comp != null) comp.setName(sepName);
		} else if(elem instanceof JPopupMenu) {
			final JSeparator sep = new JPopupMenu.Separator();
			sep.setName(sepName);
			if(insertIdx < 0) {
				((JPopupMenu)elem).add(sep);
			} else
				((JPopupMenu)elem).insert(sep, insertIdx);
		}
	}

	public JMenuItem addItem(String path, String text) {
		JMenuItem retVal = new JMenuItem(text);
		addItem(path, retVal);
		return retVal;
	}

	public void addItem(String path, JMenuItem menuItem) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		final MenuElement elem = deepest.getObj2();

		int insertIdx = getInsertIndex(elem, deepest.getObj1());
		if(elem instanceof JMenu) {
			if(insertIdx >= 0)
				((JMenu)elem).add(menuItem, insertIdx);
			else
				((JMenu)elem).add(menuItem);
		} else if(elem instanceof JPopupMenu) {
			if(insertIdx >= 0)
				((JPopupMenu)elem).add(menuItem, insertIdx);
			else
				((JPopupMenu)elem).add(menuItem);
		} else if(elem instanceof JMenuBar) {
			if(insertIdx >= 0)
				((JMenuBar)elem).add(menuItem, insertIdx);
			else
				((JMenuBar)elem).add(menuItem);
		}
	}

	public JMenuItem addItem(String path, Action action) {
		final JMenuItem  retVal = new JMenuItem(action);
		addItem(path, retVal);
		return retVal;
	}

	public void addItem(String path, MenuElement ele) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(getRoot(), path);
		final MenuElement elem = deepest.getObj2();
		int insertIdx = getInsertIndex(elem, deepest.getObj1());

		addItem(elem, insertIdx, ele);
	}

	public void addItem(MenuElement elem, int insertIdx, MenuElement menuItem) {
		if(elem instanceof JMenu) {
			if(insertIdx >= 0)
				((JMenu)elem).add(menuItem.getComponent(), insertIdx);
			else
				((JMenu)elem).add(menuItem.getComponent());
		} else if(elem instanceof JPopupMenu) {
			if(insertIdx >= 0)
				((JPopupMenu)elem).add(menuItem.getComponent(), insertIdx);
			else
				((JPopupMenu)elem).add(menuItem.getComponent());
		} else if(elem instanceof JMenuBar) {
			if(insertIdx >= 0)
				((JMenuBar)elem).add(menuItem.getComponent(), insertIdx);
			else
				((JMenuBar)elem).add(menuItem.getComponent());
		}
	}

	/**
	 * Append all subitems from the given {@link MenuElement} at
	 * the provied path.
	 *
	 * @param path
	 * @param menuEle
	 */
	public void appendSubItems(String path, MenuElement menu) {
		appendSubItems(getRoot(), path, menu);
	}

	public void appendSubItems(MenuElement parent, String path, MenuElement menu) {
		final Tuple<String, MenuElement> deepest = getDeepestMenuElement(parent, path);
		final MenuElement elem = deepest.getObj2();
		int insertIdx = getInsertIndex(elem, deepest.getObj1());

		for(MenuElement subelem : menu.getSubElements()) {
			addItem(elem, insertIdx, subelem);
			if(insertIdx >= 0) {
				++insertIdx;
			}
		}
	}

	private int getInsertIndex(MenuElement elem, String name) {
		int insertIdx = -1;
		if(name.indexOf('@') > 0) {
			String location = name.substring(name.lastIndexOf('@')+1);
			if(location.equals("^")) {
				// place at beginning
				insertIdx = 0;
			} else if(location.equals("$")) {
				// default, at end
				insertIdx = -1;
			} else if(location.matches("[0-9]+")) {
				insertIdx = Integer.parseInt(location);
			} else {
				// find index of referenced item in elem
				insertIdx = getItemIndex(elem, location)+1;
			}
		}
		return insertIdx;
	}

	private int getItemIndex(MenuElement elem, String itemName) {
		List<String> elements = new ArrayList<>();
		if(elem instanceof JMenu) {
			final JMenu menu = (JMenu)elem;
			for(int i = 0; i < menu.getItemCount(); i++) {
				final JMenuItem item = menu.getItem(i);
				if(item == null) {
					final Component menuComp = menu.getMenuComponent(i);
					elements.add(menuComp.getName());
				} else {
					elements.add(item.getText());
				}
			}
		} else if(elem instanceof JPopupMenu) {
			final JPopupMenu menu = (JPopupMenu)elem;
			for(int i = 0; i < menu.getComponentCount(); i++) {
				final Component menuComp = menu.getComponent(i);
				if(menuComp instanceof MenuElement)
					elements.add(getMenuElementText((MenuElement)menuComp));
				else if(menuComp instanceof JPopupMenu.Separator)
					elements.add(((JPopupMenu.Separator)menuComp).getName());
			}
		} else if(elem instanceof JMenuBar) {
			final JMenuBar menu = (JMenuBar)elem;
			for(int i = 0; i < menu.getMenuCount(); i++)
				elements.add(getMenuElementText(menu.getMenu(i)));
		}

		for(int i = 0; i < elements.size(); i++) {
			final String item = elements.get(i);
			if(item != null && item.equals(itemName)) {
				return i;
			}
		}
		return -1;
	}

	private String getMenuElementText(MenuElement elem) {
		String retVal = elem.getComponent().getName();

		if(elem instanceof JMenu) {
			retVal = ((JMenu)elem).getText();
		} else if(elem instanceof JMenuItem) {
			retVal = ((JMenuItem)elem).getText();
		} else if(elem instanceof JPopupMenu) {
			retVal = ((JPopupMenu)elem).getLabel();
		}
		if(retVal == null) {
			if(elem == getRoot())
				retVal = ".";
			else
				retVal = "";
		}

		return retVal;
	}

	private Tuple<String, MenuElement> getDeepestMenuElement(MenuElement elem, String path) {
		int position = 0;
		if(elem != null && path != null) {
			final String [] components = path.split("/");
			int index = 0;

			// Go as deep as we can go
			while(index < components.length) {
				final int oldIndex = index;
				String compTxt = components[index];
				if(compTxt.indexOf('@') > 0)
					compTxt = compTxt.substring(0, compTxt.lastIndexOf('@'));

				if(compTxt.equals(".") && elem == getRoot()) {
					elem = getRoot();
					position += components[index].length() + 1;
					++index;
					continue;
				}

				for(MenuElement subelem : elem.getSubElements()) {
					if(compTxt.equals(getMenuElementText(subelem))) {
						position += components[index].length() + 1;
						++index;
						elem = subelem;
						break;
					}
				}

				// If we didn't move, create new menu
				if(index == oldIndex) {
					int insertIdx = getInsertIndex(elem, components[index]);
					elem = addMenu(elem, insertIdx, compTxt);
					position += components[index].length() + 1;
					++index;
				}
			}

			if(index == components.length)
				--position;
		}

		return new Tuple<String, MenuElement>(path.substring(0, position), elem);
	}

}
