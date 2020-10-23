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
package ca.phon.ui;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * Abstract verifier for text fields.
 *
 */
public abstract class AbstractVerifier extends InputVerifier implements KeyListener {
	
	/**
	 * List of verifier listeners
	 */
	private List<VerifierListener> listeners = new ArrayList<VerifierListener>();
	
	/**
	 * Add listener
	 */
	public void addVerificationListener(VerifierListener listener) {
		synchronized(listeners) {
			if(!listeners.contains(listener))
				listeners.add(listener);
		}
	}
	
	/**
	 * Remove listener
	 */
	public void removeVerificationListener(VerifierListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Fire events
	 */
	public void fireVerificationPassed(JComponent c) {
		VerifierListener[] ls;
		synchronized(listeners) {
			ls = listeners.toArray(new VerifierListener[0]);
		}
		for(VerifierListener listener:ls) {
			listener.verificationPassed(c);
		}
	}
	
	public void fireVerificationFailed(JComponent c) {
		VerifierListener[] ls;
		synchronized(listeners) {
			ls = listeners.toArray(new VerifierListener[0]);
		}
		for(VerifierListener listener:ls) {
			listener.verificationFailed(c);
		}
	}
	
	public void fireVerificationReset(JComponent c) {
		VerifierListener[] ls;
		synchronized(listeners) {
			ls = listeners.toArray(new VerifierListener[0]);
		}
		for(VerifierListener listener:ls) {
			listener.verificationReset(c);
		}
	}
	
	/**
	 * Perform actual verification.
	 * 
	 * @return true if verification passes, false otherwise
	 */
	public abstract boolean verification(JComponent c);

	@Override
	public boolean verify(JComponent input) {
		boolean retVal = verification(input);
		
		if(!retVal) {
			fireVerificationFailed(input);
		} else {
			fireVerificationPassed(input);
		}
		
		return retVal;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		fireVerificationReset((JComponent)e.getSource());
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}
