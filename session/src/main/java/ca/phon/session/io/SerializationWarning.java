package ca.phon.session.io;

public class SerializationWarning extends Exception {

	private static final long serialVersionUID = -1355009758549617681L;

	private int record = -1;
	
	public SerializationWarning(Throwable cause) {
		this(-1, cause);
	}
	
	public SerializationWarning(int record, Throwable cause) {
		super(cause);
		
		this.record = record;
	}
	
	public int getRecord() {
		return this.record;
	}
	
	public void setRecord(int record) {
		this.record = record;
	}
	
}
