package ca.phon.query.script.params;

import ca.phon.script.params.*;

import java.util.*;
import java.util.stream.Collectors;

public class TierListScriptParam extends ScriptParam {
	
	public static final String PROMPT_PROP = StringScriptParam.class.getName() + ".promptText";
	
	public static final String VALIDATE_PROP = StringScriptParam.class.getName() + ".validate";
	
	public static final String TOOLTIP_TEXT_PROP = StringScriptParam.class.getName() + ".tooltipText";
	
	private String promptText = new String();
	
	private boolean validate = true;
	
	private boolean required = false;
	
	private String tooltipText = null;

	public TierListScriptParam(String id, String desc, String defaultValue) {
		super();
		
		setParamType("tierset");
		
		setParamDesc(desc);
		setValue(id, null);
		setDefaultValue(id, defaultValue);
	}
	
	public Set<String> tierSet() {
		var tierVal = super.getValue(super.getParamId());
		if(tierVal == null) {
			return Set.of();
		} else {
			Set<String> retVal = new LinkedHashSet<>();
			Arrays.stream(tierVal.toString().split(","))
				.forEach( (s) -> { if(s.trim().length() > 0) retVal.add(s.trim()); } );
			return retVal;
		}
	}
	
	public void addTier(String tiername) {
		Set<String> tierSet = tierSet();
		if(!tierSet.contains(tiername)) {
			setTiers(tierSet);
		}
	}
	
	public void removeTier(String tiername) {
		Set<String> tierSet = tierSet();
		if(tierSet.remove(tiername)) {
			setTiers(tierSet);
		}
	}

	public void setTiers(Collection<String> tierList) {
		Set<String> tierSet = new LinkedHashSet<>(tierList);
		super.setValue(super.getParamId(), tierSet.stream().collect(Collectors.joining(",")));
	}

	public void setPrompt(String text) {
		String oldText = promptText;
		promptText = text;
		super.propSupport.firePropertyChange(PROMPT_PROP, oldText, text);
	}
	
	public String getPrompt() {
		return this.promptText;
	}

	public boolean isValidate() {
		return (isRequired() ? getValue(getParamId()).toString().length() > 0 && validate : validate);
	}

	public boolean isRequired() {
		return this.required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setValidate(boolean validate) {
		boolean old = this.validate;
		this.validate = validate;
		super.propSupport.firePropertyChange(VALIDATE_PROP, old, this.validate);
	}
	
	public void setTooltipText(String tooltipText) {
		String oldVal = this.tooltipText;
		this.tooltipText = tooltipText;
		super.propSupport.firePropertyChange(TOOLTIP_TEXT_PROP, oldVal, this.tooltipText);
	}
	
	public String getTooltipText() {
		return this.tooltipText;
	}
	
	@Override
	public String getStringRepresentation() {
		return "{tierset " + super.getValue(super.getParamId()) + "}";
	}
	
}
