package ca.phon.app.session.editor.view.ipa_lookup.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.view.ipa_lookup.IPALookupView;

/**
 * Base class for lookup view actions.
 */
public abstract class IPALookupViewAction extends AbstractAction {

	private static final long serialVersionUID = 7551272812334000000L;

	private final IPALookupView lookupView;
	
	public IPALookupViewAction(IPALookupView view) {
		super();
		this.lookupView = view;
	}
	
	public IPALookupView getLookupView() {
		return this.lookupView;
	}
	
}
