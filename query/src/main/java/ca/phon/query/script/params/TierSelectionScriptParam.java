package ca.phon.query.script.params;

import ca.phon.script.params.*;

public class TierSelectionScriptParam extends ScriptParam {

	public static final String PROMPT_PROP = StringScriptParam.class.getName() + ".promptText";

	public static final String VALIDATE_PROP = StringScriptParam.class.getName() + ".validate";

	public static final String TOOLTIP_TEXT_PROP = StringScriptParam.class.getName() + ".tooltipText";

	private String promptText = new String();

	private boolean validate = true;

	private boolean required = false;

	private String tooltipText = null;

	public TierSelectionScriptParam(String id, String title, String def) {
		super();

		setParamType("tierselect");

		setParamDesc(title);
		setValue(id, null);
		setDefaultValue(id, def);
	}

	public String getTier() {
		return (String) this.getValue(getParamId());
	}

	public void setTier(String tier) {
		setValue(getParamId(), tier);
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
		return "{tierselect " + super.getValue(super.getParamId()) + "}";
	}

}
