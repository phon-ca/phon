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

	public boolean contains(Object o) {
		return children.contains(o);
	}

	public boolean add(ReportTreeNode e) {
		e.setParent(this);
		return children.add(e);
	}

	public boolean remove(Object o) {
		((ReportTreeNode)o).setParent(null);
		return children.remove(o);
	}

	public void clear() {
		children.clear();
	}

	public void add(int index, ReportTreeNode element) {
		element.setParent(this);
		children.add(index, element);
	}

	public ReportTreeNode remove(int index) {
		ReportTreeNode retVal = children.remove(index);
		retVal.setParent(null);
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
	
	/**
	 * Return the markdown template code for this report node.
	 * 
	 * @return
	 */
	public abstract String getReportTemplateBlock();

}
