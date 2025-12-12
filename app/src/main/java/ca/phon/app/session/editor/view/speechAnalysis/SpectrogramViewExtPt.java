/*
 * Copyright (C) 2012-2018 Gregory Hedlund
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
package ca.phon.app.session.editor.view.speechAnalysis;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;

@PhonPlugin(name="Spectrogram",version="0.1",minPhonVersion="1.7.0")
public class SpectrogramViewExtPt implements IPluginExtensionPoint<SpeechAnalysisTier> {

	@Override
	public Class<?> getExtensionType() {
		return SpeechAnalysisTier.class;
	}

	@Override
	public IPluginExtensionFactory<SpeechAnalysisTier> getFactory() {
		return factory;
	}

	private final IPluginExtensionFactory<SpeechAnalysisTier> factory = new IPluginExtensionFactory<SpeechAnalysisTier>() {
		
		@Override
		public SpeechAnalysisTier createObject(Object... args) {
			final SpeechAnalysisEditorView parent = SpeechAnalysisEditorView.class.cast(args[0]);
			return new SpectrogramView(parent);
		}
		
	};
	
}
