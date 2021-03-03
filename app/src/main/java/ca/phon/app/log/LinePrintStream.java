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
package ca.phon.app.log;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Fires a property change event when a new line is printed to
 * the stream.
 * 
 */
public class LinePrintStream extends PrintStream {
	
	private final PropertyChangeSupport propertySupport = 
			new PropertyChangeSupport(this);
	
	public final static String LINE_APPENDED_PROP = 
			LinePrintStream.class.getName() + ".numLines";
	
	int numLines = 0;

	public LinePrintStream(OutputStream out) {
		super(out);
	}

	@Override
	public void write(int b) {
		super.write(b);
		if(b == '\n') {
			int prevLines = numLines;
			propertySupport.firePropertyChange(LINE_APPENDED_PROP, prevLines, ++numLines);
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}
	
	public int getNumLines() {
		return this.numLines;
	}
	
}
