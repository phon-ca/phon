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
package ca.phon.app.opgraph.editor;

import java.io.*;
import java.lang.annotation.*;

import ca.phon.app.opgraph.macro.*;
import ca.phon.opgraph.*;

/**
 * Instantiator for {@link OpgraphEditor} editor models.
 * 
 */
public interface EditorModelInstantiator {

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface EditorModelInstantiatorMenuInfo {
		
		public String name();
		
		public String tooltip() default "";
		
		public Class<? extends OpgraphEditorModel> modelType() default MacroOpgraphEditorModel.class;
		
	}
	
	/**
	 * Create a new graph from the default template.
	 * 
	 * @return new graph from template
	 * 
	 * @throws IOException 
	 */
	public OpGraph defaultTemplate() throws IOException;
	
	/**
	 * Create model with the given graph.
	 * 
	 * @return
	 */
	public OpgraphEditorModel createModel(OpGraph graph);

}
