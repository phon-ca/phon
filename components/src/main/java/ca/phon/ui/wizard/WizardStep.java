/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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

import javax.swing.JComponent;

/**
 * A single step in a wizard.
 *
 */
public class WizardStep extends JComponent {
	
	private static final long serialVersionUID = 759706453682993593L;
	
	/* Natigation */
	private int prevStep = -1;
	private int nextStep = -1;

	public int getNextStep() {
		return nextStep;
	}

	public void setNextStep(int nextStep) {
		this.nextStep = nextStep;
	}

	public int getPrevStep() {
		return prevStep;
	}

	public void setPrevStep(int prevStep) {
		this.prevStep = prevStep;
	}
	
	public boolean validateStep() {
		return true;
	}
	
}
