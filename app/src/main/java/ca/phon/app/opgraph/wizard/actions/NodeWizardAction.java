package ca.phon.app.opgraph.wizard.actions;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.opgraph.wizard.NodeWizard;

public abstract class NodeWizardAction extends HookableAction {

    private final NodeWizard nodeWizard;

    public NodeWizardAction(NodeWizard nodeWizard) {
        super();

        this.nodeWizard = nodeWizard;
    }

    public NodeWizard getNodeWizard() {
        return this.nodeWizard;
    }

}
