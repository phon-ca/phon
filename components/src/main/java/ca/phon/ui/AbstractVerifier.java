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
package ca.phon.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JComponent;

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
