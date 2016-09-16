/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
