/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.query.report;

import ca.phon.query.report.io.Group;
import ca.phon.query.report.io.ReportDesign;
import ca.phon.query.report.io.Section;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * Tree model for report settings.
 * 
 *
 */
public class ReportTreeModel implements TreeModel {
	
	private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	
	/** The report - also the root of our tree */
	private ReportDesign report;
	
	public ReportTreeModel(ReportDesign root) {
		super();
		
		this.report = root;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		synchronized(listeners) {
			listeners.add(l);
		}
	}

	@Override
	public Object getChild(Object parent, int index) {
		Object retVal = null;
		
		if(parent == report) {
			retVal = report.getReportSection().get(index).getValue();
			//retVal = report.getSection().get(index);
		} else {
			Section section = (Section)parent;
			if(section instanceof Group) {
				Group group = (Group)section;
				retVal = group.getGroupReportSection().get(index).getValue();
			}
		}
		
		return retVal;
	}

	@Override
	public int getChildCount(Object parent) {
		int retVal = 0;
		
		if(parent == report) {
			retVal = report.getReportSection().size();
		} else {
			// everything else is of type SectionType
			Section section = (Section)parent;
			if(section instanceof Group) {
				Group group  = (Group)section;
				retVal = group.getGroupReportSection().size();
			}
		}
		
		return retVal;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		int retVal = 0;
		
		if(parent == report) {
			Section section = (Section)child;
			for(int i = 0; i < report.getReportSection().size(); i++) {
				Section s = report.getReportSection().get(i).getValue();
				if(s == section) {
					retVal = i;
					break;
				}
			}
		} else {
			Section gSection = (Section)parent;
			if(gSection instanceof Group) {
				Section section = (Section)child;
				Group group = (Group)gSection;
				for(int i = 0; i < group.getGroupReportSection().size(); i++) {
					Section s = group.getGroupReportSection().get(i).getValue();
					if(s == section) {
						retVal = i;
						break;
					}
				}
			}
		}
		
		return retVal;
	}

	@Override
	public Object getRoot() {
		return report;
	}

	@Override
	public boolean isLeaf(Object node) {
		boolean retVal = false;
		
		if(node != report) {
			Section section = (Section)node;
			retVal = !(section instanceof Group);
		}
		
		return retVal;
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		synchronized(listeners) {
			listeners.remove(l);
		}
	}
	
	public void fireTreeChanged(TreePath tp) {
		TreeModelListener ls[] = new TreeModelListener[0];
		synchronized(listeners) {
			ls = listeners.toArray(new TreeModelListener[0]);
		}
		for(TreeModelListener l:ls) {
			l.treeStructureChanged(new TreeModelEvent(this, tp));
		}
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}


}
