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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Report tree used in OpGraph documents useful for creating
 * report templates.
 * 
 */
public class ReportTree {

	private final ReportTreeNode root;
	
	public ReportTree() {
		this(new SectionHeaderNode(""));
	}
	
	public ReportTree(ReportTreeNode root) {
		this.root = root;
	}
	
	public ReportTreeNode getRoot() {
		return this.root;
	}

	public void addReportTreeListener(ReportTreeListener listener) {
		getRoot().addReportTreeListener(listener);
	}

	public void removeReportTreeListener(ReportTreeListener listener) {
		getRoot().removeReportTreeListener(listener);
	}

	public List<ReportTreeListener> getListeners() {
		return getRoot().getListeners();
	}
	
	public String getReportTemplate() {
		final StringBuffer buffer = new StringBuffer();
		append(buffer, root);
		return buffer.toString();
	}
		
	private void append(StringBuffer buffer, ReportTreeNode node) {
		buffer.append(node.getReportTemplateBlock());
		buffer.append("\n");
		for(ReportTreeNode reportNode:node.getChildren())
			append(buffer, reportNode);
	}

	public ReportTree createFilteredTree(Predicate<ReportTreeNode> nodePredicate) {
		final ReportTreeNode clonedRoot = scanTree(getRoot(), nodePredicate);
		return new ReportTree(clonedRoot);
	}

	private ReportTreeNode scanTree(ReportTreeNode node, Predicate<ReportTreeNode> nodePredicate) {
		if(nodePredicate.test(node)) {
			final ReportTreeNode clonedNode = node.cloneWithoutChildren();
			for(ReportTreeNode childNode:node.getChildren()) {
				final ReportTreeNode clonedChildNode = scanTree(childNode, nodePredicate);
				if(clonedChildNode != null)
					clonedNode.add(clonedChildNode);
			}
			return clonedNode;
		} else {
			return null;
		}
	}

	public void forEachNode(Consumer<ReportTreeNode> consumer) {
		forEach(root, consumer);
	}

	private void forEach(ReportTreeNode node, Consumer<ReportTreeNode> consumer) {
		consumer.accept(node);
		for(ReportTreeNode child:node.getChildren())
			forEach(child, consumer);
	}

}
