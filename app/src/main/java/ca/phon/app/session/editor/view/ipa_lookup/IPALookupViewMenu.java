package ca.phon.app.session.editor.view.ipa_lookup;

import javax.swing.JMenu;

import ca.phon.app.session.editor.view.ipa_lookup.actions.AutoTranscribeCommand;
import ca.phon.app.session.editor.view.ipa_lookup.actions.ExportIPACommand;
import ca.phon.app.session.editor.view.ipa_lookup.actions.ImportIPACommand;

public class IPALookupViewMenu extends JMenu {
	
	private static final long serialVersionUID = 3248124841856311448L;

	private final IPALookupView lookupView;
	
	public IPALookupViewMenu(IPALookupView lookupView) {
		super();
		this.lookupView = lookupView;
		
		init();
	}
	
	private void init() {
		final ExportIPACommand exportAct = new ExportIPACommand(lookupView);
		add(exportAct);
		
		final ImportIPACommand importAct = new ImportIPACommand(lookupView);
		add(importAct);
		
		addSeparator();
		final AutoTranscribeCommand autoTranscribeAct = new AutoTranscribeCommand(lookupView);
		add(autoTranscribeAct);
	}

}
