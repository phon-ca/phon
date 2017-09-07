/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

package ca.phon.app.session.editor;

import java.lang.reflect.*;
import java.util.logging.*;

import javax.swing.SwingUtilities;

import ca.phon.worker.PhonWorker;

/**
 * Action class for editor events.  This class will call
 * the specified method of the given object when an event occurs.
 * The specified method must take a single parameter of type EditorEvent.
 *
 * The action will be executed on the calling thread by default.
 * To invoke the action on the AWT event queue use the method
 * setRunOnEDT(true)
 */
public class DelegateEditorAction implements EditorAction {
	
	private final static Logger LOGGER = Logger.getLogger(DelegateEditorAction.class.getName());

	/** The delegate class (static method) */
	private Class<?> clazz;

	/** The delegate object (non-static method) */
	private Object object;

	/** The method name we are looking for */
	private String methodId;
	
	/**
	 * Constructors
	 */
	public DelegateEditorAction(Object delegate, String methodId) {
		super();

		this.object = delegate;
		this.methodId = methodId;
	}

	public DelegateEditorAction(Class<?> clazz, String methodId) {
		super();

		this.clazz = clazz;
		this.methodId = methodId;
	}

	/**
	 * If both clazz and object are defined - we default to static.
	 * @return
	 */
	public final boolean isStaticAction() {
		boolean retVal = true;

		if(object != null)
			retVal = false;

		return retVal;
	}
	
	@Override
	public void eventOccured(EditorEvent ee) {
		try {
			final Method m = getMethod();
			
			final DelegateRunner runner = new DelegateRunner(ee, m);
			
			// choose where/who to run the action
			final RunOnEDT runOnEDT = m.getAnnotation(RunOnEDT.class);
			if(runOnEDT != null) {
				boolean waitForFinish = runOnEDT.invokeAndWait();
				if(waitForFinish) {
					SwingUtilities.invokeAndWait(runner);
				} else {
					SwingUtilities.invokeLater(runner);
				}
			} else {
				final RunInBackground runInBackground = m.getAnnotation(RunInBackground.class);
				if(runInBackground != null && runInBackground.newThread()) {
					final PhonWorker tempWorker = PhonWorker.createWorker();
					tempWorker.setFinishWhenQueueEmpty(true);
					tempWorker.invokeLater(runner);
					tempWorker.start();
				} else {
					runner.run();
				}
			}
		} catch (NoSuchMethodException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
			
	}

	/*
	 * Get the delegate method reference.
	 */
	private Method getMethod() throws NoSuchMethodException {
		Class<?> clazz = null;

		if(isStaticAction()) {
			clazz = this.clazz;
		} else {
			clazz = this.object.getClass();
		}

		if(clazz == null)
			throw new NullPointerException("Class not found");

		Method retVal =
				clazz.getMethod(methodId, EditorEvent.class);
		return retVal;
	}
	
	/**
	 * Runnable for event action
	 */
	private class DelegateRunner implements Runnable {
		
		private EditorEvent ee;
		
		private Method method;
		
		public DelegateRunner(EditorEvent ee, Method method) {
			super();
			this.ee = ee;
			this.method = method;
		}
		
		@Override
		public void run() {
			try {
				if(isStaticAction()) {
					method.invoke(null, ee);
				} else {
					method.invoke(object, ee);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
	}

}
