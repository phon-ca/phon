package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tier_management.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;
import ca.phon.ui.toast.*;
import ca.phon.util.icons.*;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;

public class DuplicateTierAction extends SessionEditorAction {

	private final static String CMD_NAME = "Duplicate tier...";

	private final static String SHORT_DESC = "Duplicate tier.";

	private final static ImageIcon ICON =
			IconManager.getInstance().getFontIcon("content_copy", IconSize.SMALL, UIManager.getColor("Button.foreground"));

	private int index = -1;

	private TierDescription td;

	public DuplicateTierAction(SessionEditor editor, TierDescription td) {
		this(editor, td, -1);
	}

	public DuplicateTierAction(SessionEditor editor, TierDescription td, int index) {
		super(editor);

		this.td = td;
		this.index = index;

		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, ICON);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		TierEditorDialog newTierDialog = new TierEditorDialog(true);
		TierInfoEditor tierEditor = newTierDialog.getTierEditor();
		newTierDialog.add(tierEditor);
		newTierDialog.setTitle("Duplicate Tier");
		newTierDialog.getHeader().setHeaderText("Duplicate Tier");
		newTierDialog.getHeader().setDescText("Create a new tier with the contents of " + td.getName());
		newTierDialog.setModal(true);
		newTierDialog.pack();

		if(newTierDialog.showDialog()) {
			final SessionEditor editor = getEditor();
			final Session session = editor.getSession();
			// get tier info
			String tierName = tierEditor.getTierName();
			tierName = StringUtils.strip(tierName);
			if(tierName.length() == 0) {
				return;
			}

			boolean tierExists = false;
			if(SystemTierType.isSystemTier(tierName)) {
				tierExists = true;
			} else {
				for(TierDescription td:session.getUserTiers()) {
					if(td.getName().equals(tierName)) {
						tierExists = true;
						break;
					}
				}
			}

			if(tierExists){
				final Toast toast = ToastFactory.makeToast("A tier with name " + tierEditor.getTierName() + " already exists.");
				toast.start(tierEditor);
				return;
			}

			// create tier
			final TierDescription tierDescription = tierEditor.createTierDescription();
			final TierViewItem tierViewItem = tierEditor.createTierViewItem();

			getEditor().getUndoSupport().beginUpdate();

			final AddTierEdit edit = new AddTierEdit(editor, tierDescription, tierViewItem, index);
			editor.getUndoSupport().postEdit(edit);

			for(Record r:getEditor().getSession().getRecords()) {
				Tier<TierData> existingTier = r.getTier(td.getName(), TierData.class);
				Tier<TierData> dupTier = r.getTier(tierDescription.getName(), TierData.class);

				TierData existingVal = existingTier.getValue();
				try {
					TierEdit<TierData> tierEdit = new TierEdit<>(getEditor(), dupTier, TierData.parseTierData(existingVal.toString()));
					getEditor().getUndoSupport().postEdit(tierEdit);
				} catch (ParseException pe) {}
			}

			getEditor().getUndoSupport().endUpdate();
		}
	}

}
