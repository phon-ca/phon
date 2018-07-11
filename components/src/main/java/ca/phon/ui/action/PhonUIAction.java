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
package ca.phon.ui.action;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import ca.phon.worker.PhonWorker;

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

	private final static Logger LOGGER = Logger.getLogger(PhonUIAction.class
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
	 * Constructors
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
			LOGGER.log(Level.SEVERE, e.toString(), e);

			if(evt.getActionEvent().getSource() instanceof JComponent) {
				JComponent comp = (JComponent)evt.getActionEvent().getSource();
//				PhonUtilities.showComponentMessage(comp, e.getCause().getMessage());
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);

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
							LOGGER.log(Level.SEVERE, ex3.getLocalizedMessage(), ex);
						}
					}
				} else {
					try {
						// now look for a method with no parameters
						retVal =
								clazz.getMethod(methodId, new Class[0]);
					} catch (NoSuchMethodException ex2) {
						LOGGER.log(Level.SEVERE, ex2.getLocalizedMessage(), ex);
					}
				}
		}
		return retVal;
	}
}
