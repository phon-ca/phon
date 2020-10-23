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
package ca.phon.syllabifier.basic;

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.syllabifier.*;
import ca.phon.syllabifier.basic.io.*;
import ca.phon.util.*;

/**
 * 
 * Class to perform syllabification based on a
 * syllabifier definition.
 * 
 *
 */
public class BasicSyllabifier implements Syllabifier, IExtendable {
	
	public final static String TRACK_STAGES_PROP = BasicSyllabifier.class.getName() + ".trackStages";
	private final boolean trackStages = PrefHelper.getBoolean(TRACK_STAGES_PROP, PrefHelper.getBoolean("phon.debug", Boolean.FALSE));
	
	@Extension(IPAElement.class)
	public static class SyllabifierStageResults {
		public final Map<String, String> stages = new LinkedHashMap<String, String>();
	}
	
	private final ExtensionSupport extSupport = new ExtensionSupport(BasicSyllabifier.class, this);

	private final SyllabifierDef def;
	
	private final List<SyllabifierStage> stages = new ArrayList<SyllabifierStage>();
	
	/** Constructor */
	BasicSyllabifier(SyllabifierDef def) {
		super();
		
		extSupport.initExtensions();
		this.def = def;
		
		compile();
	}
	
	private void compile() {
		final SonorityScale scale = new SonorityScale(def.getSonorityScale());
		stages.add(scale);
		
		for(StageType st:def.getStage()) {
			final SyllabifierStage stage = new Stage(st);
			stages.add(stage);
		}
	}
	
	public SyllabifierDef getDefinition() {
		return def;
	}
	
	public SonorityValues getSonorityScale() {
		return def.getSonorityScale();
	}

	@Override
	public String getName() {
		return def.getName();
	}

	@Override
	public Language getLanguage() {
		return Language.parseLanguage(def.getLanguage());
	}

	@Override
	public void syllabify(List<IPAElement> phones) {
		Map<String, String> stageValues = null;
		if(trackStages) {
			final SyllabifierStageResults stageResults = new SyllabifierStageResults();
			putExtension(SyllabifierStageResults.class, stageResults);
			stageValues = stageResults.stages;
		}
		
		for(SyllabifierStage stage:stages) {
			if(stage.repeatWhileChanges()) {
				int repeatCount = 0;
				while(stage.run(phones)) {
					if(trackStages) {
						final String stageName = stage.getName() + "_" + repeatCount;
						stageValues.put(stageName, (new IPATranscript(phones)).toString(true));
					}
				}
			} else {
				stage.run(phones);
				if(trackStages) {
					stageValues.put(stage.getName(), (new IPATranscript(phones)).toString(true));
				}
			}
		}
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public boolean equals(Object b) {
		return (b instanceof BasicSyllabifier && ((BasicSyllabifier)b).getLanguage().equals(getLanguage()));
	}
	
}
