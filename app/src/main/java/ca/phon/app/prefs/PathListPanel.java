/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.prefs;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * UI for editing a list of paths
 *
 */
public class PathListPanel extends JPanel {
	
	private static final long serialVersionUID = 3250575449792042295L;

	/* UI */
	private JButton addPathBtn;
	private JButton removePathBtn;
	private JButton moveUpBtn;
	private JButton moveDownBtn;
	
	private JList pathList;
	
	private List<String> paths;
	
	/** Property event fired when path list changes */
	public static final String PATH_LIST_CHANGED_PROP = "_path_list_changed_";
	
	/**
	 * Constructor
	 */
	public PathListPanel() {
		super();
		
		paths = new ArrayList<String>();
		
		init();
	}
	
	public PathListPanel(List<String> initialList) {
		super();
		
		paths = new ArrayList<String>();
		paths.addAll(initialList);
		
		init();
	}
	
	public List<String> getPaths() {
		return this.paths;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		FormLayout layout = new FormLayout(
				"fill:pref:grow, pref, pref, pref",
				"pref, pref, pref, pref, fill:pref:grow");
		CellConstraints cc = new CellConstraints();
		setLayout(layout);
		
		
		
		pathList = new JList(new PathListModel());
		pathList.setVisibleRowCount(6);
		JScrollPane pathScroller = new JScrollPane(pathList);
		
		add(pathScroller, cc.xywh(1, 2, 3, 4));
		add(getMoveUpButton(), cc.xy(4, 2));
		add(getMoveDownButton(), cc.xy(4, 3));
		add(getRemovePathButton(), cc.xy(2, 1));
		
		add(getAddPathButton(), cc.xy(3, 1));
	}
	
	/*
	 * Create buttons
	 */
	private JButton getAddPathButton() {
		if(addPathBtn == null) {
			ImageIcon addIcon = 
				IconManager.getInstance().getIcon("actions/list-add", IconSize.XSMALL);
			PhonUIAction addPathAct = new PhonUIAction(this, "onAddPath");
			addPathAct.putValue(Action.NAME, "Add folder...");
			addPathAct.putValue(Action.SHORT_DESCRIPTION, "Add folder to list...");
			addPathAct.putValue(Action.SMALL_ICON, addIcon);
			
			addPathBtn = new JButton(addPathAct);
			addPathBtn.setText("");
		}
		
		return addPathBtn;
	}
	
	private JButton getRemovePathButton() {
		if(removePathBtn == null) {
			ImageIcon removeIcon = 
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.XSMALL);
			PhonUIAction removePathAct = new PhonUIAction(this, "onRemovePath");
			removePathAct.putValue(Action.NAME, "Remove folder...");
			removePathAct.putValue(Action.SHORT_DESCRIPTION, "Remove selected folder from list...");
			removePathAct.putValue(Action.SMALL_ICON, removeIcon);
			
			removePathBtn = new JButton(removePathAct);
			removePathBtn.setText("");
		}
		
		return removePathBtn;
	}
	
	private JButton getMoveUpButton() {
		if(moveUpBtn == null) {
			ImageIcon upIcon =
				IconManager.getInstance().getIcon("actions/go-up", IconSize.XSMALL);
			PhonUIAction moveUpAct = new PhonUIAction(this, "onMovePathUp");
			moveUpAct.putValue(Action.NAME, "Move up");
			moveUpAct.putValue(Action.SHORT_DESCRIPTION, "Move selected folder up in the list");
			moveUpAct.putValue(Action.SMALL_ICON, upIcon);
			
			moveUpBtn = new JButton(moveUpAct);
			moveUpBtn.setText("");
		}
		
		return moveUpBtn;
	}
	
	private JButton getMoveDownButton() {
		if(moveDownBtn == null) {
			ImageIcon downIcon = 
				IconManager.getInstance().getIcon("actions/go-down", IconSize.XSMALL);
			PhonUIAction moveDownAct = new PhonUIAction(this, "onMovePathDown");
			moveDownAct.putValue(Action.NAME, "Move down");
			moveDownAct.putValue(Action.SHORT_DESCRIPTION, "Move selected folder down in the list");
			moveDownAct.putValue(Action.SMALL_ICON, downIcon);
			
			moveDownBtn = new JButton(moveDownAct);
			moveDownBtn.setText("");
		}
		
		return moveDownBtn;
	}
	
	/*
	 * Button actions
	 * 
	 */
	public void onAddPath(PhonActionEvent pae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setAllowMultipleSelection(true);
		props.setCanCreateDirectories(true);
		props.setTitle("Add paths...");
		props.setRunAsync(false);
		
		final List<String> newPaths = NativeDialogs.showOpenDialog(props);
		for(String newPath:newPaths) {
			if(paths.contains(newPath)) continue;
			
			// add path to end if nothing is selected, or at the
			// current selected position if there is one
			int selectedIdx = pathList.getSelectedIndex();
			
			String oldValue = paths.toString();
			
			if(selectedIdx >= 0)
				paths.add(selectedIdx, newPath);
			else
				paths.add(newPath);
			
			String newValue = paths.toString();
			firePropertyChange(PATH_LIST_CHANGED_PROP, oldValue, newValue);
		}

	}
	
	public void onRemovePath(PhonActionEvent pae) {
		int selectedIdx = pathList.getSelectedIndex();
		
		if(selectedIdx >= 0 && selectedIdx < paths.size()) {
			String oldValue = paths.toString();
			
			paths.remove(selectedIdx);
			
			String newValue = paths.toString();
			
			firePropertyChange(PATH_LIST_CHANGED_PROP, oldValue, newValue);
		}
	}
	
	public void onMovePathUp(PhonActionEvent pae) {
		int selectedIdx = pathList.getSelectedIndex();
		
		if(selectedIdx > 0 && selectedIdx < paths.size()) {
			String oldValue = paths.toString();
			
			String path = paths.remove(selectedIdx);
			paths.add(selectedIdx-1, path);
			
			String newValue = paths.toString();
			
			firePropertyChange(PATH_LIST_CHANGED_PROP, oldValue, newValue);
			pathList.setSelectedIndex(selectedIdx-1);
		}
	}
	
	public void onMovePathDown(PhonActionEvent pae) {
		int selectedIdx = pathList.getSelectedIndex();
		
		if(selectedIdx >= 0 && selectedIdx < paths.size()-1) {
			String oldValue = paths.toString();
			
			String path = paths.remove(selectedIdx);
			paths.add(selectedIdx+1, path);
			
			String newValue = paths.toString();
			
			firePropertyChange(PATH_LIST_CHANGED_PROP, oldValue, newValue);
			pathList.setSelectedIndex(selectedIdx+1);
		}
	}
	
	/**
	 * List model for paths
	 */
	private class PathListModel extends AbstractListModel implements PropertyChangeListener {

		public PathListModel() {
			PathListPanel.this.addPropertyChangeListener(this);
		}
		
		@Override
		public Object getElementAt(int arg0) {
			return paths.get(arg0);
		}

		@Override
		public int getSize() {
			return paths.size();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getPropertyName().equals(PATH_LIST_CHANGED_PROP)) {
				super.fireContentsChanged(this, 0, getSize());
			}
		}
		
	}
}
