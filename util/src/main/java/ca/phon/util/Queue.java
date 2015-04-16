/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.util;

import java.util.Collection;
import java.util.Vector;

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
