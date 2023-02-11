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
package ca.phon.app.opgraph.report.tree;

import java.util.*;

/**
 * Tree node for report templates.  
 * 
 */
public abstract class ReportTreeNode implements Iterable<ReportTreeNode> {

	private String title;
	
	private ReportTreeNode parent;
	
	private List<ReportTreeNode> children = new ArrayList<>();

	private List<ReportTreeListener> listeners = new ArrayList<>();
	
	public ReportTreeNode() {
		this("");
	}
	
	public ReportTreeNode(String title) {
		super();
		setTitle(title);
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public ReportTreeNode getParent() {
		return this.parent;
	}
	
	public void setParent(ReportTreeNode parent) {
		this.parent = parent;
	}
	
	public List<ReportTreeNode> getChildren() {
		return Collections.unmodifiableList(this.children);
	}
	
	public int size() {
		return children.size();
	}

	public boolean contains(ReportTreeNode o) {
		return children.contains(o);
	}

	public boolean add(ReportTreeNode e) {
		e.setParent(this);
		children.add(e);
		int index = children.size() - 1;
		findRoot().fireNodeAdded(this, index, e);
		return true;
	}

	public boolean remove(ReportTreeNode o) {
		((ReportTreeNode)o).setParent(null);
		int index = children.indexOf(o);
		if(index >= 0) {
			children.remove(o);
			findRoot().fireNodeRemoved(this, index, o);
			return true;
		}
		return false;
	}

	public void clear() {
		children.clear();
	}

	public void add(int index, ReportTreeNode element) {
		element.setParent(this);
		children.add(index, element);
		findRoot().fireNodeAdded(this, index, element);
	}

	public ReportTreeNode remove(int index) {
		ReportTreeNode retVal = children.remove(index);
		retVal.setParent(null);
		findRoot().fireNodeRemoved(this, index, retVal);
		return retVal;
	}

	@Override
	public Iterator<ReportTreeNode> iterator() {
		return children.iterator();
	}

	public int getLevel() {
		int retVal = 0;
		
		ReportTreeNode node = this;
		while(node.getParent() != null) {
			node = node.getParent();
			++retVal;
		}
		
		return retVal;
	}
	
	public ReportTreePath getPath() {
		ReportTreePath path = new ReportTreePath(Collections.singleton(this).toArray(new ReportTreeNode[0]));
		
		ReportTreeNode parent = getParent();
		while(parent != null) {
			path = path.pathWithNewParent(parent);
			parent = parent.getParent();
		}
		
		return path;
	}

	public ReportTreeNode findRoot() {
		ReportTreeNode parent = getParent();
		ReportTreeNode retVal = this;
		while(parent != null) {
			retVal = parent;
			parent = parent.getParent();
		}
		return retVal;
	}

	public void addReportTreeListener(ReportTreeListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeReportTreeListener(ReportTreeListener listener) {
		listeners.remove(listener);
	}

	public List<ReportTreeListener> getListeners() {
		return Collections.unmodifiableList(listeners);
	}

	public void fireNodeAdded(ReportTreeNode parent, int index, ReportTreeNode child) {
		getListeners().forEach((l) -> l.reportNodeAdded(parent, index, child));
	}

	public void fireNodeRemoved(ReportTreeNode parent, int index, ReportTreeNode child) {
		getListeners().forEach((l) -> l.reportNodeRemoved(parent, index, child));
	}
	
	/**
	 * Return the markdown template code for this report node.
	 * 
	 * @return
	 */
	public abstract String getReportTemplateBlock();

}
