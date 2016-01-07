package ca.phon.app.opgraph.editor.library;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;

import ca.gedge.opgraph.library.NodeData;

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
