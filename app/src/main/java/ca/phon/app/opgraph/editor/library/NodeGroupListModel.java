/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.opgraph.editor.library;

import java.util.*;
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
