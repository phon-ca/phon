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
package ca.phon.app.html;

import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.db.xml.XMLQueryFactory;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.Range;

/**
 * JavaScript bridge for common Phon functions.
 */
public class JavaScriptBridge {
	
	private Project project;
	
	/**
	 * Get current project (from focused window) or <code>null</code> if not found.
	 */
	public static Project getCurrentProject() {
		CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
		if(cmf != null) {
			return cmf.getExtension(Project.class);
		}
		return null;
	}
	
	public JavaScriptBridge() {
		this(getCurrentProject());
	}
	
	public JavaScriptBridge(Project project) {
		this.project = project;
	}
	
	public Project getProject() {
		if(this.project == null) {
			this.project = getCurrentProject();
		}
		return this.project;
	}
	
	public void openSessionAtRecord(String corpus, String session, int recordIndex) {
		final EntryPointArgs args = new EntryPointArgs();
		args.put(EntryPointArgs.PROJECT_OBJECT, getProject());
		args.put(EntryPointArgs.CORPUS_NAME, corpus);
		args.put(EntryPointArgs.SESSION_NAME, session);
		args.put(SessionEditorEP.RECORD_INDEX_PROPERY, recordIndex);

		PluginEntryPointRunner.executePluginInBackground(SessionEditorEP.EP_NAME, args);
	}
	
	/**
	 * Open session with result values specified in the given javascript array.
	 * The array should be an array of values with order: 'tier name', group index, 'range'
	 * 
	 * <pre>
	 * e.g., [['IPA Target', 0, '(0..2)'],[...
	 * </pre>
	 * 
	 * @param corpus
	 * @param session
	 * @param recordIndex
	 * @param jsRvs
	 */
	public void openSessionAtRecordWithResultValues(String corpus, String session, int recordIndex, Object jsRvs) {
//		final EntryPointArgs args = new EntryPointArgs();
//		args.put(EntryPointArgs.PROJECT_OBJECT, getProject());
//		args.put(EntryPointArgs.CORPUS_NAME, corpus);
//		args.put(EntryPointArgs.SESSION_NAME, session);
//		args.put(SessionEditorEP.RECORD_INDEX_PROPERY, recordIndex);
//
//		final QueryFactory factory = new XMLQueryFactory();
//		final Result tempResult = factory.createResult();
//		tempResult.setRecordIndex(recordIndex);
//
//		// find result value using columnName - which should be the tier name
//		for(int i = 0; i < jsRvs.length(); i++) {
//			JSArray rvData = jsRvs.get(i).asArray();
//
//			String tierName = rvData.get(0).getStringValue();
//			int gIdx = (int)rvData.get(1).asNumber().getNumberValue();
//			Range range = Range.fromString(rvData.get(2).getStringValue());
//
//			ResultValue rv = factory.createResultValue();
//			rv.setTierName(tierName);
//			rv.setGroupIndex(gIdx);
//			rv.setRange(range);
//			tempResult.addResultValue(rv);
//		}
//		args.put(SessionEditorEP.RESULT_VALUES_PROPERTY, new Result[] { tempResult });
//
//		PluginEntryPointRunner.executePluginInBackground(SessionEditorEP.EP_NAME, args);
	}
	
}
