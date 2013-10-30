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
	private final BlockingQueue<EditorEvent> eventQueue = new LinkedBlockingQueue<>();
	
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
		this.editorRef = new WeakReference<>(editor);
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
		List<EditorAction> handlers = actionMap.get(eventName);
		if(handlers == null) {
			handlers = new ArrayList<>();
			actionMap.put(eventName, handlers);
		}
		handlers.add(action);
	}
	
	/**
	 * Remove handler for the given event
	 * 
	 * @param eventName
	 * @param action
	 */
	public void removeActionForEvent(String eventName, EditorAction action) {
		List<EditorAction> handlers = actionMap.get(eventName);
		if(handlers != null) {
			handlers.remove(action);
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
			retVal = new ArrayList<>();
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
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
				
				if(event != null) {
					for(EditorAction action:getActionsForEvent(event.getEventName())) {
						action.eventOccured(event);
					}
				}
			}
			
			super.setStatus(TaskStatus.FINISHED);
		}
		
	};
}
