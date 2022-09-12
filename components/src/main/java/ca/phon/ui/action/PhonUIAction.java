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
package ca.phon.ui.action;

import java.awt.event.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.function.Consumer;

import javax.swing.*;

import ca.phon.worker.*;

/**
 * UI action calling {@link FunctionalInterface} methods.
 */
public class PhonUIAction<T> extends AbstractAction {

	private static final long serialVersionUID = -3566788828631450043L;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(PhonUIAction.class
			.getName());

	/** Are we running our action in the background? (def: false) */
	private boolean runInBackground = false;

	/** The worker thread for background actions */
	private PhonWorker workerThread = null;

	/** Data provided to method during events */
	private T data = null;

	private final Runnable runnable;

	private final Consumer<T> directConsumer;

	private final Consumer<PhonActionEvent<T>> eventConsumer;

	public static PhonUIAction<Void> runnable(Runnable runnable) {
		return new PhonUIAction<>(runnable, null, null, null);
	}

	public static <T> PhonUIAction<T> consumer(Consumer<T> consumer, T data) {
		return new PhonUIAction<>(null, consumer, null, data);
	}

	public static PhonUIAction<Void> eventConsumer(Consumer<PhonActionEvent<Void>> eventConsumer) {
		return eventConsumer(eventConsumer, null);
	}

	public static <T> PhonUIAction<T> eventConsumer(Consumer<PhonActionEvent<T>> eventConsumer, T data) {
		return new PhonUIAction<>(null, null, eventConsumer, data);
	}

	private PhonUIAction(Runnable runnable, Consumer<T> consumer, Consumer<PhonActionEvent<T>> eventConsumer, T data) {
		super();

		this.runnable = runnable;
		this.directConsumer = consumer;
		this.eventConsumer = eventConsumer;
		this.data = data;
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
	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
	}

	protected void executeRunnable(Runnable runnable) {
		if(isRunInBackground()) {
			if(getWorkerThread() != null) {
				getWorkerThread().invokeLater(runnable);
			} else {
				PhonWorker.invokeOnNewWorker(runnable);
			}
		} else {
			runnable.run();
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if(runnable != null) {
			executeRunnable(runnable);
		} else if(directConsumer != null) {
			executeRunnable(() -> directConsumer.accept(getData()));
		} else if(eventConsumer != null) {
			final PhonActionEvent<T> pae = new PhonActionEvent<>(ae, getData());
			executeRunnable(() -> eventConsumer.accept(pae));
		} else {
			throw new RuntimeException("No action method provided");
		}
	}

}
