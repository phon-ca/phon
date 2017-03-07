package ca.phon.app.opgraph.editor;

import ca.gedge.opgraph.OpGraph;

/**
 * Instantiator for {@link OpgraphEditor} editor models.
 * 
 */
@FunctionalInterface
public interface EditorModelInstantiator {
	
	/**
	 * Create model with the given arguments.
	 * 
	 * @param args
	 * @return
	 */
	public OpgraphEditorModel createModel(OpGraph graph);

}
