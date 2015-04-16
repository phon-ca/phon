/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.app.autosave;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;

/**
 * Handles timer and performs actions for autosave of sessions.
 * 
 * Autosave timer interval is read from a user defined property.
 * Default is 10 mins.
 */
public class AutosaveManager {
	
	private final static Logger LOGGER = Logger.getLogger(AutosaveManager.class.getName());

	/**
	 * Autosave property
	 */
	public static final String AUTOSAVE_INTERVAL_PROP = AutosaveManager.class.getName() + ".interval";
	
	public static final String AUTOSAVE_PREFIX = "__autosave_";
	
	/**
	 * Default interval (seconds)
	 */
	public static final int defaultInterval = (5*60);
	
	/**
	 * Interval (miliseconds)
	 */
	private long interval = 0;
	
	/**
	 * Timer
	 */
	private Timer timer;
	
	/**
	 * singleton instance
	 */
	private static AutosaveManager _instance;
	
	/*
	 * Get the shared instance
	 */
	public static AutosaveManager getInstance() {
		if(_instance == null) {
			_instance = new AutosaveManager();
		//	_instance.setInterval(defaultInterval);
		}
		return _instance;
	}
	
	/**
	 * (Hidden) constructor
	 */
	private AutosaveManager() {
	}
	
	/**
	 * Set the timer interval in seconds.
	 */
	public void setInterval(int sec) {
		setInterval((long)(sec*1000));
	}
	
	/**
	 * Set the timer interval in miliseconds.
	 * Starts the timer if not started.
	 * 
	 * @param ms the interval for performing
	 * the autosave action.  Setting value to 0
	 * zero will stop the timer.
	 */
	public void setInterval(long ms) {
		interval = ms;
		
		if(interval == 0) {
			if(timer != null) {
				LOGGER.info("Stopping autosave manager.");
				timer.stop();
				timer = null;
			}
			return;
		}
		
		if(timer == null) {
			LOGGER.info("Starting autosave manager.");
			timer = new Timer((int)interval, new AutosaveAction());
			timer.start();
		} else {
			timer.setDelay((int)interval);
		}
		LOGGER.info("Autosaving every " + (ms/1000/60) + " minutes");
	}
	
	
	/**
	 * Autosave action
	 */
	private class AutosaveAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			LOGGER.info("Starting autosave at " + 
					Calendar.getInstance().getTime().toString() + "...");
			
			Runtime runtime = Runtime.getRuntime();
		
			// print out avail memory (may be useful)
			int freeMemory = (int)(runtime.freeMemory() / 1024);
			int totalMemory = (int)(runtime.totalMemory() / 1024);
			int usedMemory = (totalMemory - freeMemory);
			
			LOGGER.fine("Java heap: " + usedMemory + "Kb / "
					+ totalMemory + "Kb, " + (usedMemory * 100 / totalMemory)
					+ "%");
			
			// find all open sessions
			for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
				if(cmf instanceof SessionEditor) {
					final SessionEditor editor = (SessionEditor)cmf;
					
					if(editor.isModified()) {
						final Project project = editor.getProject();
						final Session session = editor.getSession();
						
						LOGGER.info("Autosaving session '" + 
								session.getCorpus() + "." + session.getName() + "'...");
						
						try {
							final UUID writeLock = project.getSessionWriteLock(session.getCorpus(), 
									AUTOSAVE_PREFIX + session.getName());
							project.saveSession(session.getCorpus(), AUTOSAVE_PREFIX + session.getName(), 
									session, writeLock);
							project.releaseSessionWriteLock(session.getCorpus(), AUTOSAVE_PREFIX + session.getName(), writeLock);
						} catch (IOException e1) {
							LOGGER.log(Level.SEVERE,
									e1.getLocalizedMessage(), e1);
						}
					}
				}
			}
			
			LOGGER.info(
					"Autosave action finished at " + Calendar.getInstance().getTime().toString());
		}
		
	}
}
