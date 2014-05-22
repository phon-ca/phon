package ca.phon.app.session.editor.view.find_and_replace;

import ca.phon.util.Range;

public class FindExpr {
	
	public enum SearchType {
		PLAIN,
		REGEX,
		PHONEX;
	}
	
	private SearchType type;
	
	private boolean caseSensitive;
	
	private String expr;
	
	public FindExpr() {
		this(SearchType.PLAIN, "", true);
	}
	
	public FindExpr(String expr) {
		this(SearchType.PLAIN, expr, true);
	}
	
	public FindExpr(String expr, boolean caseSensitive) {
		this(SearchType.PLAIN, expr, caseSensitive);
	}
	
	public FindExpr(SearchType type, String expr, boolean caseSensitive) {
		super();
		this.type = type;
		this.expr = expr;
		this.caseSensitive = caseSensitive;
	}

	public SearchType getType() {
		return type;
	}

	public void setType(SearchType type) {
		this.type = type;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String getExpr() {
		return expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}
	
	public Range find(Object obj) {
		return new Range(0, 0, true);
	}
	
}
