package ca.phon.opgraph.editor;

import ca.gedge.opgraph.OpGraph;

@OpgraphEditorModelInfo(name="General", description="Empty graph with default context")
public class DefaultOpgraphEditorModel extends OpgraphEditorModel {

	public DefaultOpgraphEditorModel() {
		super();
	}

	public DefaultOpgraphEditorModel(OpGraph opgraph) {
		super(opgraph);
	}

}
