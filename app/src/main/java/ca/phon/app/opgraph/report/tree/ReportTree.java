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
	
	public String getReportTemplate() {
		final StringBuffer buffer = new StringBuffer();
		
		for(ReportTreeNode reportNode:root.getChildren()) {
			append(buffer, reportNode);
		}
		
		return buffer.toString();
	}
		
	private void append(StringBuffer buffer, ReportTreeNode node) {
		buffer.append(node.getReportTemplateBlock());
		buffer.append("\n");
		for(ReportTreeNode reportNode:node.getChildren())
			append(buffer, reportNode);
	}
	
}
