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

import java.util.*;

import javax.swing.*;

import ca.phon.extensions.*;

/**
 * A single step in a wizard.
 *
 */
public class WizardStep extends JComponent implements IExtendable {
	
	private static final long serialVersionUID = 759706453682993593L;
	
	private final ExtensionSupport extSupport = new ExtensionSupport(WizardStep.class, this);
	
	/* Natigation */
	private int prevStep = -1;
	private int nextStep = -1;
	
	private String title = "";

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
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
	
}
