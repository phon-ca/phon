package ca.phon.ipadictionary;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import ca.phon.ipa.IPATranscript;

/**
 * Rule for handling contractions.
 * 
 * Rules are in format
 * 
 *  A -> B
 *  
 * Where A is a set of conditions for matching
 * this rule and B is a list of instructions for
 * building the transcription.
 * 
 * A conditions can be matched for the left-hand
 * contraction, transcript of left-hand contraction
 * and same on right side.  Conditions can be specified
 * as plain text, regular expressions or phonex expressions.
 * 
 * B instructions an include any of the input values
 * or literal values.
 *
 */
public final class ContractionRule implements Serializable {
	
	private static final long serialVersionUID = 3962967640160708840L;

	public enum ConditionType {
		PLAIN,
		REGEX,
		PHONEX;
	}
	
	/* By default all expressions are regex with '.*' as the value. (i.e., match anything) */
	private String lhsExpr = ".*";
	private ConditionType lhsType = ConditionType.REGEX;
	
	private String rhsExpr = ".*";
	private ConditionType rhsType = ConditionType.REGEX;
	
	private String tlhsExpr = ".*";
	private ConditionType tlhsType = ConditionType.REGEX;
	
	private String trhsExpr = ".*";
	private ConditionType trhsType = ConditionType.REGEX;
	
	/**
	 *  V expression 
	 *
	 *	There are 4 variable fields allowed in the 'V' string:
	 *
	 *   1) ${LHS} | ${lhs}
	 *      Left-hand side ortho value
	 *   2) ${RHS} | ${rhs}
	 *   	Right-hand side ortho value
	 *   3) ${transcript:LHS} | ${transcript:lhs}
	 *   	Left-hand side IPA transcript
	 *   4) ${transcript:RHS} | ${transcript:rhs}
	 *   	Right-hand side IPA transcript
	 *   
	 *   Any other field starting with a '$' will trigger
	 *   an error.  Literal value strings can also be
	 *   part of the expression.
	 */
	private String vExpr;
	
	private transient List<VClause> tBuilder = 
		new ArrayList<VClause>();
	
	private class VClause {
		ValueClause type;
		String value;
	}
	
	private enum ValueClause {
		LHS,
		RHS,
		T_LHS,
		T_RHS,
		LITERAL;
		
		private static final String[] exprs = {
				"\\$\\{lhs\\}",
				"\\$\\{rhs\\}",
				"\\$\\{transcript:lhs\\}",
				"\\$\\{transcript:rhs\\}",
				"[^${}+]*"
		};
		
		public static ValueClause getClause(String str) {
			ValueClause retVal = ValueClause.LITERAL;
			for(int i = 0; i < exprs.length; i++) {
				String expr = exprs[i];
				if(str.matches(expr)) {
					retVal = ValueClause.values()[i];
					break;
				}
			}
			return retVal;
		}
	}
	
	public ContractionRule() {
		super();
	}
	

	public String getLhsExpr() {
		return lhsExpr;
	}


	public void setLhsExpr(String lhsExpr) {
		this.lhsExpr = lhsExpr;
	}


	public ConditionType getLhsType() {
		return lhsType;
	}


	public void setLhsType(ConditionType lhsType) {
		this.lhsType = lhsType;
	}


	public String getRhsExpr() {
		return rhsExpr;
	}


	public void setRhsExpr(String rhsExpr) {
		this.rhsExpr = rhsExpr;
	}


	public ConditionType getRhsType() {
		return rhsType;
	}


	public void setRhsType(ConditionType rhsType) {
		this.rhsType = rhsType;
	}


	public String getTlhsExpr() {
		return tlhsExpr;
	}


	public void setTlhsExpr(String tlhsExpr) {
		this.tlhsExpr = tlhsExpr;
	}


	public ConditionType getTlhsType() {
		return tlhsType;
	}


	public void setTlhsType(ConditionType tlhsType) {
		this.tlhsType = tlhsType;
	}


	public String getTrhsExpr() {
		return trhsExpr;
	}


	public void setTrhsExpr(String trhsExpr) {
		this.trhsExpr = trhsExpr;
	}


	public ConditionType getTrhsType() {
		return trhsType;
	}


	public void setTrhsType(ConditionType trhsType) {
		this.trhsType = trhsType;
	}


	public List<VClause> getTBuilder() {
		return tBuilder;
	}


	public void setTBuilder(List<VClause> builder) {
		tBuilder = builder;
	}


	public String getVExpr() {
		return vExpr;
	}

	public void setVExpr(String expr) {
		vExpr = expr;
		parseVExpr();
	}
	
	private void parseVExpr() {
		// split string on '+'
		String[] vparts = vExpr.split("\\+");
		tBuilder.clear();
		
		for(String part:vparts) {
			ValueClause type = ValueClause.getClause(StringUtils.strip(part));
			VClause clause = new VClause();
			clause.type = type;
			clause.value = StringUtils.strip(part);
			
			tBuilder.add(clause);
		}
	}

	/**
	 * Check to see if the given string matches our LHS+RHS 
	 * expressions.
	 * 
	 * @param lhs - lhs (ortho)
	 * @param rhs - rhs (ortho)
	 * @param tlhs - lhs (ipa)
	 * @param thrs - rhs (ipa)
	 */
	public boolean matches(String lhs, String rhs,
			String tlhs, String trhs) {
		boolean retVal = 
			checkExpr(lhsExpr, lhs, lhsType) &&
			checkExpr(rhsExpr, rhs, rhsType) &&
			checkExpr(tlhsExpr, tlhs, tlhsType) &&
			checkExpr(trhsExpr, trhs, trhsType);
		return retVal;
	}
	
	private boolean checkExpr(String expr, String value, ConditionType type) {
		boolean retVal = false;
		
		if(type == ConditionType.PHONEX) {
			try {
				final IPATranscript transcript = IPATranscript.parseIPATranscript(value);
				retVal = transcript.matches(expr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if(type == ConditionType.PLAIN) {
			retVal = value.equals(expr);
		} else if(type == ConditionType.REGEX) {
			retVal = value.matches(expr);
		}
		
		return retVal;
	}
	
	/**
	 * 
	 * @param lhs - lhs (ortho)
	 * @param rhs - rhs (ortho)
	 * @param tlhs - lhs (ipa)
	 * @param thrs - rhs (ipa)
	 * @return
	 */
	public String buildTranscript(String lhs, String rhs, String tlhs, String trhs) {
		String retVal = "";
		
		if(tBuilder == null) {
			tBuilder = new ArrayList<VClause>();
			parseVExpr();
		}
		
		if(matches(lhs, rhs, tlhs, trhs)) {
			for(VClause vclause:tBuilder) {
				if(vclause.type == ValueClause.LHS) {
					retVal += lhs;
				} else if(vclause.type == ValueClause.RHS) {
					retVal += rhs;
				} else if(vclause.type == ValueClause.T_LHS) {
					retVal += tlhs;
				} else if(vclause.type == ValueClause.T_RHS) {
					retVal += trhs;
				} else if(vclause.type == ValueClause.LITERAL) {
					retVal += vclause.value;
				} else {
					Logger.getLogger(getClass().getName()).warning("Unknown clause type");
				}
			}
		}
		
		return retVal;
	}
}
