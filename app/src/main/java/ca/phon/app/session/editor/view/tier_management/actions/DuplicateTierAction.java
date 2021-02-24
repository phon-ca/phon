package ca.phon.app.session.editor.view.tier_management.actions;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddTierEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.tier_management.TierEditorDialog;
import ca.phon.app.session.editor.view.tier_management.TierInfoEditor;
import ca.phon.app.session.editor.view.tier_management.TierOrderingEditorView;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.ui.toast.Toast;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DuplicateTierAction extends TierManagementAction {

	private static final long serialVersionUID = -25622911440669271L;

	private final static String CMD_NAME = "Duplicate tier...";

	private final static String SHORT_DESC = "Duplicate tier.";

	private final static ImageIcon ICON =
			IconManager.getInstance().getIcon("actions/edit-copy", IconSize.SMALL);

	private int index = -1;

	private TierDescription td;

	public DuplicateTierAction(SessionEditor editor, TierOrderingEditorView view, TierDescription td) {
		this(editor, view, td, -1);
	}

	public DuplicateTierAction(SessionEditor editor, TierOrderingEditorView view, TierDescription td, int index) {
		super(editor, view);

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
		tierEditor.setGrouped(td.isGrouped());
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
				Tier<TierString> existingTier = r.getTier(td.getName(), TierString.class);
				Tier<TierString> dupTier = r.getTier(tierDescription.getName(), TierString.class);

				for(int gIdx = 0; gIdx < r.numberOfGroups(); gIdx++) {
					Object existingVal = (gIdx < existingTier.numberOfGroups() ? existingTier.getGroup(gIdx) : "");
					TierEdit<TierString> tierEdit = new TierEdit<>(getEditor(), dupTier, gIdx, new TierString(existingVal.toString()));
					getEditor().getUndoSupport().postEdit(tierEdit);
				}
			}

			getEditor().getUndoSupport().endUpdate();
		}
	}

}
