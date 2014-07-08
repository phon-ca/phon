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
