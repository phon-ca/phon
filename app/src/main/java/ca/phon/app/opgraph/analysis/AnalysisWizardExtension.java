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
package ca.phon.app.opgraph.analysis;

import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.NodeWizardReportContext;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;

public class AnalysisWizardExtension extends WizardExtension {
	
	public AnalysisWizardExtension(OpGraph graph) {
		super(graph);
	}

	@Override
	public NodeWizard createWizard(Processor processor) {
		
		return new AnalysisWizard(
				"Analysis : " + (getWizardTitle() != null ? getWizardTitle() : "Unknown"), processor, super.getGraph());
	}

	@Override
	public void setupReportContext(NodeWizardReportContext context) {
		super.setupReportContext(context);
	}
	
}