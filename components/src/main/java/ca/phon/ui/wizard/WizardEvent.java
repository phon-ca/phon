package ca.phon.ui.wizard;

public class WizardEvent {
	
	public static enum WizardEventType {
		GOTO_STEP,
		FINISHED,
		STEP_ADDED,
		STEP_REMOVED
	};
	
	private WizardEventType type;
	
	private WizardFrame source;
	
	private int stepIdx;
	
	public static WizardEvent createGotoStepEvent(WizardFrame wizard, int stepIdx) {
		return new WizardEvent(WizardEventType.GOTO_STEP, wizard, stepIdx);
	}
	
	public static WizardEvent createFinishedEvent(WizardFrame wizard) {
		return new WizardEvent(WizardEventType.FINISHED, wizard, wizard.numberOfSteps()-1);
	}
	
	public WizardEvent(WizardEventType type, WizardFrame wizard, int stepIdx) {
		super();
		this.source = wizard;
		this.stepIdx = stepIdx;
		this.type = type;
	}

	public WizardEventType getType() {
		return type;
	}

	public void setType(WizardEventType type) {
		this.type = type;
	}

	public WizardFrame getSource() {
		return source;
	}

	public void setSource(WizardFrame source) {
		this.source = source;
	}

	public int getStep() {
		return stepIdx;
	}

	public void setStep(int stepIdx) {
		this.stepIdx = stepIdx;
	}

}
