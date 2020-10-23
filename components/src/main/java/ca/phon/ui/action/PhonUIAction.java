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
package ca.phon.ui.action;

import java.awt.event.*;
import java.lang.ref.*;
import java.lang.reflect.*;

import javax.swing.*;

import ca.phon.worker.*;

/**
 * <p>Swing action for Phon controls.  Allows for easy
 * calling of UI action code.  This class uses reflection
 * to find a specified method with the following signature:
 * <br/>
 * <CODE>(Lca/phon/gui/action/PhonActionEvent;)V</CODE>.
 * If a method accepting a {@link PhonActionEvent} is not found,
 * this class will look for a method accepting no parameters
 * (if {@link #object} is <code>null</code>) or a method
 * accepting a single parameter with a type to which
 * {@link #object} is assignable.
 * </p>
 *
 * <p>By default actions will run on the EDT.  To allow
 * for background execution of actions, call <code>setRunInBackground(true)</code>.
 * To specify a thread for this action to run on use the
 * <code>setWorkerThread</code> method (otherwise a new thread is
 * created).</p>
 *
 */
public class PhonUIAction extends AbstractAction {

	private static final long serialVersionUID = -3566788828631450043L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonUIAction.class
			.getName());

	/** The delegate class (static method) */
	private Class<?> clazz;

	/** The delegate object (non-static method) */
	private WeakReference<?> objectRef;

	/** The method name we are looking for */
	private String methodId;

	/** Are we running our action in the background? (def: false) */
	private boolean runInBackground = false;

	/** The worker thread for background actions */
	private PhonWorker workerThread = null;

	/** Data provided to method during events */
	private Object data = null;

	/**
	 * Constructors - pre java 9
	 */
	public PhonUIAction(Object delegate, String methodId) {
		this(delegate, methodId, null);
	}

	public PhonUIAction(Object delegate, String methodId, Object data) {
		super();

		this.objectRef = new WeakReference<>(delegate);
		this.methodId = methodId;
		this.data = data;
	}

	public PhonUIAction(String text,
			Object delegate, String methodId) {
		this(text, delegate, methodId, null);
	}

	public PhonUIAction(String text,
			Object delegate, String methodId, Object data) {
		super(text);

		this.objectRef = new WeakReference<>(delegate);
		this.methodId = methodId;
		this.data = data;
	}

	public PhonUIAction(String text, ImageIcon icon,
			Object delegate, String methodId) {
		super(text, icon);

		this.objectRef = new WeakReference<>(delegate);
		this.methodId = methodId;
	}

	public PhonUIAction(Class<?> clazz, String methodId) {
		this(clazz, methodId, null);
	}

	public PhonUIAction(Class<?> clazz, String methodId, Object data) {
		super();

		this.clazz = clazz;
		this.methodId = methodId;
		this.data = data;
	}

	public PhonUIAction(String text,
			Class<?> clazz, String methodId) {
		super(text);

		this.clazz = clazz;
		this.methodId = methodId;
	}

	public PhonUIAction(String text, ImageIcon icon,
			Class<?> clazz, String methodId) {
		super(text, icon);

		this.clazz = clazz;
		this.methodId = methodId;
	}

	/**
	 * If both clazz and object are defined - we default to static.
	 * @return
	 */
	public boolean isStaticAction() {
		boolean retVal = true;

		if(objectRef != null)
			retVal = objectRef.get() == null;

		return retVal;
	}

	/**
	 * Should we run in the background
	 */
	public boolean isRunInBackground() {
		return this.runInBackground;
	}

	public void setRunInBackground(boolean v) {
		this.runInBackground = v;
	}

	/**
	 * Background thread
	 */
	public PhonWorker getWorkerThread() {
		return this.workerThread;
	}

	public void setWorkerThread(PhonWorker th) {
		this.workerThread = th;
	}

	/**
	 * Data
	 */
	public Object getData() {
		return this.data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// create a new PhonActionEvent
		final PhonActionEvent evt = new PhonActionEvent(ae, data);

		Runnable toRun = new Runnable() { @Override
		public void run() {
		try {
			Method m = getMethod();

			Object[] params = new Object[0];
			if(m.getParameterTypes().length > 0) {
				Class<?> paramType = m.getParameterTypes()[0];
				if(paramType == PhonActionEvent.class) {
					params = new Object[]{evt};
				} else if(data != null) {
					params = new Object[]{data};
				}
			}
			if(isStaticAction())
				m.invoke(null, params);
			else
				m.invoke(objectRef.get(), params);

		} catch (InvocationTargetException e) {
			LOGGER.error( e.toString(), e);

			if(evt.getActionEvent().getSource() instanceof JComponent) {
				JComponent comp = (JComponent)evt.getActionEvent().getSource();
//				PhonUtilities.showComponentMessage(comp, e.getCause().getMessage());
			}
		} catch (Exception e) {
			LOGGER.error( e.toString(), e);

			if(evt.getActionEvent().getSource() instanceof JComponent) {
				JComponent comp = (JComponent)evt.getActionEvent().getSource();
//				PhonUtilities.showComponentMessage(comp, e.getMessage());
			}
		}
		} };

		if(isRunInBackground()) {
			PhonWorker bgThread = null;
			if(getWorkerThread() != null) {
				bgThread = getWorkerThread();
			} else {
				bgThread = PhonWorker.createWorker();
				bgThread.setFinishWhenQueueEmpty(true);
			}
			bgThread.invokeLater(toRun);
			if(getWorkerThread() == null)
				bgThread.start();
		} else {
			toRun.run();
		}
	}

	/**
	 * Find the action method.
	 * @return the method to call - or <code>null</code> if not
	 * found.
	 *
	 */
	private Method getMethod() {
		Class<?> clazz = null;

		if(isStaticAction()) {
			clazz = this.clazz;
		} else {
			clazz = this.objectRef.get().getClass();
		}

		if(clazz == null)
			throw new NullPointerException("Class not found");

		Method retVal = null;

		try {
			// look for a method that accepts a PhonActionEvent first
			retVal =
					clazz.getMethod(methodId, PhonActionEvent.class);
		} catch (NoSuchMethodException ex) {
				if(data != null) {
					Class<?> dataType = data.getClass();
					try {
						retVal = clazz.getMethod(methodId, new Class[]{dataType});
					} catch (NoSuchMethodException ex2) {
						if(dataType == Boolean.class) {
							dataType = boolean.class;
						} else if(dataType == Character.class) {
							dataType = char.class;
						} else if(dataType == Integer.class) {
							dataType = int.class;
						} else if(dataType == Short.class) {
							dataType = short.class;
						} else if(dataType == Long.class) {
							dataType = long.class;
						} else if(dataType == Float.class) {
							dataType = float.class;
						} else if(dataType == Double.class) {
							dataType = double.class;
						} else if(dataType == Byte.class) {
							dataType = byte.class;
						}

						try {
							retVal = clazz.getMethod(methodId, new Class[]{dataType});
						} catch (NoSuchMethodException ex3) {
							LOGGER.error( ex3.getLocalizedMessage(), ex);
						}
					}
				} else {
					try {
						// now look for a method with no parameters
						retVal =
								clazz.getMethod(methodId, new Class[0]);
					} catch (NoSuchMethodException ex2) {
						LOGGER.error( ex2.getLocalizedMessage(), ex);
					}
				}
		}
		return retVal;
	}
}
