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
package ca.phon.ui.wizard;

public class WizardEvent {
	
	public static enum WizardEventType {
		GOTO_STEP,
		FINISHED,
		STEP_ADDED,
		STEP_REMOVED
	};
	
	private WizardEventType type;
	
	private WizardFrame source;
	
	private int stepIdx;
	
	public static WizardEvent createGotoStepEvent(WizardFrame wizard, int stepIdx) {
		return new WizardEvent(WizardEventType.GOTO_STEP, wizard, stepIdx);
	}
	
	public static WizardEvent createFinishedEvent(WizardFrame wizard) {
		return new WizardEvent(WizardEventType.FINISHED, wizard, wizard.numberOfSteps()-1);
	}
	
	public WizardEvent(WizardEventType type, WizardFrame wizard, int stepIdx) {
		super();
		this.source = wizard;
		this.stepIdx = stepIdx;
		this.type = type;
	}

	public WizardEventType getType() {
		return type;
	}

	public void setType(WizardEventType type) {
		this.type = type;
	}

	public WizardFrame getSource() {
		return source;
	}

	public void setSource(WizardFrame source) {
		this.source = source;
	}

	public int getStep() {
		return stepIdx;
	}

	public void setStep(int stepIdx) {
		this.stepIdx = stepIdx;
	}

}
