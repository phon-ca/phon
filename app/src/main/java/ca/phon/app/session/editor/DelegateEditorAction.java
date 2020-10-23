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

package ca.phon.app.session.editor;

import java.lang.reflect.*;

import javax.swing.*;

import ca.phon.worker.*;

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
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(DelegateEditorAction.class.getName());

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
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
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
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
	}

}
