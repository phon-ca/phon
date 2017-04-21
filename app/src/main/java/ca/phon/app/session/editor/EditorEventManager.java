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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonWorker;

/**
 * <p>Handles passing of internal EditorEvents.  Events can be dispatched on the AWT event
 * thread or on a background thread.</p>
 * 
 * <p>Events are placed into a queue until they can be processed.  Events are processed
 * using a background thread.</p>
 * 
 */
public class EditorEventManager {

	private final static Logger LOGGER = Logger.getLogger(EditorEventManager.class.getName());
	
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
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
				
				if(event != null) {
					synchronized (actionMap) {
						for(EditorAction action:getActionsForEvent(event.getEventName())) {
							action.eventOccured(event);
						}
					}
				}
			}
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	};
}
