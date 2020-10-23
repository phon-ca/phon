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
package ca.phon.util;

import java.util.*;

/**
 * Basic (FIFO) Queue class based on java.util.Vector.
 * 
 * Class is threadsafe.
 * 
 *
 */
public class Queue<E> extends Vector<E> {

	/** Constructors from superclass */
	public Queue() {
		super();
	}

	public Queue(Collection<E> arg0) {
		super(arg0);
	}

	public Queue(int arg0, int arg1) {
		super(arg0, arg1);
	}

	public Queue(int arg0) {
		super(arg0);
	}
	
	/** Basic queue methods */
	
	/**
	 * Queues an object.
	 * 
	 * @param obj the object to add to the queue
	 */
	public synchronized void queue(E obj) {
		addElement(obj);
	}
	
	/**
	 * De-queues an object.  This method
	 * returns the next object to be returned.
	 * 
	 * @return Object the next object in the queue
	 * @throw EmptyQueueException if the queue is empty
	 */
	public synchronized E dequeue() 
		throws EmptyQueueException {
		
		if(this.isEmpty())
			throw new EmptyQueueException("No elements left in Queue!");
		
		E retVal = peek();
		
		// remove from queue
		this.removeElementAt(0);
		
		return retVal;
	}
	
	/**
	 * Peeks at the next object in the queue, without
	 * removing it.
	 * 
	 * @return Object the next element in the queue
	 * @throw EmptyQueueException if the queue is empty
	 */
	public synchronized E peek()
		throws EmptyQueueException {
		
		if(this.isEmpty())
			throw new EmptyQueueException("No elements left in Queue!");
		
		return this.get(0);
	}	
}
