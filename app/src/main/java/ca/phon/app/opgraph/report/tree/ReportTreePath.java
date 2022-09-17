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
import java.util.stream.Collectors;

public class ReportTreePath implements Iterable<ReportTreeNode> {

	private final ReportTreeNode[] path;
	
	public ReportTreePath(ReportTreeNode[] path) {
		this.path = path;
	}
	
	public ReportTreeNode[] getPath() {
		return this.path;
	}
	
	public ReportTreeNode lastChild() {
		return (path.length > 0 ? path[path.length -1] : null);
	}
	
	public ReportTreePath pathByAppendingChild(ReportTreeNode node) {
		ReportTreeNode newPath[] = Arrays.copyOf(path, path.length+1);
		newPath[newPath.length-1] = node;
		return new ReportTreePath(newPath);
	}
	
	public ReportTreePath pathWithNewParent(ReportTreeNode parent) {
		ReportTreeNode newPath[] = new ReportTreeNode[path.length+1];
		newPath[0] = parent;
		for(int i = 1; i < newPath.length; i++) {
			newPath[i] = path[i-1];
		}
		return new ReportTreePath(newPath);
	}
	
	public ReportTreePath pathByRemovingRoot() {
		ReportTreeNode newPath[] = Arrays.copyOfRange(path, 1, path.length);
		return new ReportTreePath(newPath);
	}
	
	@Override
	public String toString() {
		return (path.length > 0
					? Arrays.stream(path)
							.map( ReportTreeNode::getTitle )
							.collect( Collectors.joining("/") )
					: "");
	}

	@Override
	public Iterator<ReportTreeNode> iterator() {
		return Arrays.asList(getPath()).iterator();
	}

}
