package ca.phon.app.opgraph.editor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ca.gedge.opgraph.OpGraph;
import ca.phon.app.opgraph.macro.MacroOpgraphEditorModel;

/**
 * Instantiator for {@link OpgraphEditor} editor models.
 * 
 */
@FunctionalInterface
public interface EditorModelInstantiator {

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface EditorModelInstantiatorMenuInfo {
		
		public String name();
		
		public String tooltip() default "";
		
		public Class<? extends OpgraphEditorModel> modelType() default MacroOpgraphEditorModel.class;
		
	}
	
	/**
	 * Create model with the given arguments.
	 * 
	 * @param args
	 * @return
	 */
	public OpgraphEditorModel createModel(OpGraph graph);

}
