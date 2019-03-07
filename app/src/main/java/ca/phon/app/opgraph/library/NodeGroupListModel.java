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
package ca.phon.app.opgraph.library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;

import ca.phon.opgraph.library.NodeData;

/**
 *
 */
public class NodeGroupListModel extends AbstractListModel<NodeData> {

	private static final long serialVersionUID = 7555354396826758697L;
	
	private final ArrayList<NodeData> data = new ArrayList<>();
	
	private List<NodeData> filteredData = null;
	
	private String filter = null;

	public NodeGroupListModel() {
		super();
	}
	
	public NodeGroupListModel(List<NodeData> data) {
		super();
		this.data.addAll(data);
	}

	@Override
	public int getSize() {
		if(filteredData != null) {
			return filteredData.size();
		} else {
			return data.size();
		}
	}

	public String getFilter() {
		return this.filter;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
		if(this.filter == null) {
			this.filteredData = null;
		} else {
			this.filteredData =
				this.data.stream()
					.filter( (e) -> e.name.toLowerCase().contains(filter.toLowerCase()) 
							|| e.description.toLowerCase().contains(filter.toLowerCase()) )
					.collect(Collectors.toList());
		}
		super.fireContentsChanged(this, 0, data.size());
	}
	
	@Override
	public NodeData getElementAt(int index) {
		NodeData retVal = null;
		if(filteredData != null) {
			retVal = filteredData.get(index);
		} else {
			retVal = data.get(index);
		}
		return retVal;
	}

}
