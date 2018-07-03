package ca.phon.script.params;

public class PatternScriptParam extends StringScriptParam {
	
	public final static String FORMAT_PROP = PatternScriptParam.class.getName() + ".format";
	private String format = "text/plain";
	
	public final static String MIN_ROWS_PROP = PatternScriptParam.class.getName() + ".minRows";
	private int minRows = 1;
	
	public final static String MAX_ROWS_PROP = PatternScriptParam.class.getName() + ".maxRows";
	private int maxRows = 1;
	
	public final static String VISIBLE_ROWS_PROP = PatternScriptParam.class.getName() + ".visibleRows";
	private int visibleRows = 1;
	
	public final static String ERROR_LINE_PROP = PatternScriptParam.class.getName() + ".errLine";
	private int errLine = -1;
	
	public final static String ERROR_CHAR_PROP = PatternScriptParam.class.getName() + ".errChar";
	private int errChar = -1;
	
	public PatternScriptParam(String id, String desc, String defaultValue, String format, int minRows, int maxRows) {
		super(id, desc, defaultValue);
		
		setParamType("pattern");
				
		setFormat(format);
		setMinRows(minRows);
		setMaxRows(maxRows);
	}
	
	public void setMinRows(int visibleRows) {
		int oldVal = this.minRows;
		this.minRows = visibleRows;
		propSupport.firePropertyChange(MIN_ROWS_PROP, oldVal, this.minRows);
	}
	
	public int getMinRows() {
		return this.minRows;
	}
	
	public void setMaxRows(int maxRows) {
		int oldVal = this.maxRows;
		this.maxRows = maxRows;
		propSupport.firePropertyChange(MAX_ROWS_PROP, oldVal, this.maxRows);
	}
	
	public int getMaxRows() {
		return this.maxRows;
	}
	
	public void setVisibleRows(int visibleRows) {
		int oldVal = this.visibleRows;
		this.visibleRows = visibleRows;
		propSupport.firePropertyChange(VISIBLE_ROWS_PROP, oldVal, this.visibleRows);
	}
	
	public int getVisibleRows() {
		return this.visibleRows;
	}
	
	public void setFormat(String format) {
		String oldVal = this.format;
		this.format = format;
		propSupport.firePropertyChange(FORMAT_PROP, oldVal, this.format);
	}
	
	public String getFormat() {
		return this.format;
	}
	
	public int getErrLine() {
		return this.errLine;
	}
	
	public void setErrLine(int errLine) {
		int oldVal = this.errLine;
		this.errLine = errLine;
		propSupport.firePropertyChange(ERROR_LINE_PROP, oldVal, errLine);
	}
	
	public int getErrChar() {
		return this.errChar;
	}
	
	public void setErrChar(int errChar) {
		int oldVal = this.errChar;
		this.errChar = errChar;
		propSupport.firePropertyChange(ERROR_CHAR_PROP, oldVal, errChar);
	}
	
	@Override
	public String getStringRepresentation() {
		final StringBuffer buffer = new StringBuffer();
		
		String id = super.getParamIds().iterator().next();
		buffer.append("{pattern, ");
		buffer.append(id + ", ");
		buffer.append("\"" + getDefaultValue(id) + "\", ");
		buffer.append("\"" + getParamDesc() + "\", ");
		buffer.append("\"" + format + "\", " + minRows);
		buffer.append("}");
		
		return buffer.toString();
	}

}
