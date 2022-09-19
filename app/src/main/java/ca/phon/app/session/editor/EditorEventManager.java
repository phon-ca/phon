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

import ca.phon.worker.*;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

/**
 * <p>Handles passing of internal EditorEvents.  Events can be dispatched on the AWT event
 * thread or on a background thread.</p>
 * 
 * <p>Events are placed into a queue until they can be processed.  Events are processed
 * using a background thread.</p>
 * 
 */
public class EditorEventManager {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(EditorEventManager.class.getName());
	
	/**
	 * Event queue.  Events are placed in the queue until they
	 * can be processed.
	 */
	private final BlockingQueue<EditorEvent> eventQueue = new LinkedBlockingQueue<EditorEvent>();
	
	/**
	 * Event dispatch thread for the editor
	 */
	private final PhonWorker dispatchThread = PhonWorker.createWorker();
	
	/**
	 * Weak reference to {@link SessionEditor}
	 */
	private final WeakReference<SessionEditor> editorRef;
	
	/**
	 * action map
	 */
	private final Map<String, List<EditorAction>> actionMap = Collections.synchronizedMap(new HashMap<String, List<EditorAction>>());
	
	/**
	 * Constructor
	 * 
	 * @param editor
	 */
	public EditorEventManager(SessionEditor editor) {
		super();
		this.editorRef = new WeakReference<SessionEditor>(editor);
		dispatchThread.setName("EET (" + editor.getTitle() + ")");
	}
	
	/**
	 * Shutdown the even thread.
	 * 
	 * 
	 */
	public void shutdown() {
		eventQueue.clear();
		dispatchTask.shutdown();
	
		// necessary to finish the 'take()' method of blocking queue
		dispatchThread.interrupt();
		
		actionMap.clear();
		eventQueue.clear();
	}
	
	/**
	 * Get the session editor we are dispatching events for
	 */
	public SessionEditor getEditor() {
		return editorRef.get();
	}
	
	/**
	 * Queue the given event.
	 * 
	 * @param event
	 */
	public void queueEvent(EditorEvent ee) {
		try {
			eventQueue.put(ee);
		} catch (InterruptedException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		// start thread if necessary
		if(!dispatchThread.isAlive()) {
			dispatchThread.setFinishWhenQueueEmpty(true);
			dispatchThread.invokeLater(dispatchTask);
			dispatchThread.start();
		}
	}
	
	/**
	 * Register a handler for the given event name
	 * 
	 * @param eventName
	 * @param action
	 */
	public void registerActionForEvent(String eventName, EditorAction action) {
		synchronized (actionMap) {			
			List<EditorAction> handlers = actionMap.get(eventName);
			if(handlers == null) {
				handlers = new ArrayList<EditorAction>();
				actionMap.put(eventName, handlers);
			}
			handlers.add(action);
		}
	}
	
	/**
	 * Remove handler for the given event
	 * 
	 * @param eventName
	 * @param action
	 */
	public void removeActionForEvent(String eventName, EditorAction action) {
		synchronized (actionMap) {
			List<EditorAction> handlers = actionMap.get(eventName);
			if(handlers != null) {
				handlers.remove(action);
			}
		}
	}
	
	/**
	 * Retrieve the list of actions for the specified event
	 * 
	 * @param eventName
	 * 
	 * @return list of handlers for the event
	 */
	public List<EditorAction> getActionsForEvent(String eventName) {
		List<EditorAction> retVal = actionMap.get(eventName);
		if(retVal == null) 
			retVal = new ArrayList<EditorAction>();
		else 
			retVal = Collections.unmodifiableList(retVal);
		return retVal;			
	}
	
	/**
	 * Task for dispatching events
	 */
	private final PhonTask dispatchTask = new PhonTask() {

		@Override
		public void performTask() {
			super.setStatus(TaskStatus.RUNNING);
			
			while(!isShutdown()) {
				EditorEvent event = null;
				try {
					event = eventQueue.take();
				} catch (InterruptedException e) {
					if(!isShutdown()) {
						// an error only if we are not shutdown
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				}
				
				if(event != null) {
					synchronized (actionMap) {
						for(EditorAction action:getActionsForEvent(event.getEventName())) {
							action.eventOccurred(event);
						}
					}
				}
			}
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	};
}
