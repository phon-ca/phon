package ca.phon.app.session.editor.actions;

import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.tierManagement.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.tierdata.TierData;
import ca.phon.ui.toast.*;
import ca.phon.util.icons.*;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;

public class DuplicateTierAction extends SessionEditorAction {

	private final static String CMD_NAME = "Duplicate tier...";

	private final static String SHORT_DESC = "Duplicate tier.";

	private final static ImageIcon ICON =
			IconManager.getInstance().getFontIcon("content_copy", IconSize.SMALL, UIManager.getColor("Button.foreground"));

	private int index = -1;

	private String tierName = null;

	public DuplicateTierAction(SessionEditor editor, String tierName, int index) {
		this(editor.getSession(), editor.getEventManager(), editor.getUndoSupport(), tierName, index);
	}

	public DuplicateTierAction(Session session, EditorEventManager eventManager, SessionEditUndoSupport undoSupport, String tierName, int index) {
		super(session, eventManager, undoSupport);

		this.tierName = tierName;
		this.index = index;

		putValue(NAME, CMD_NAME);
		putValue(SMALL_ICON, ICON);
		putValue(SHORT_DESCRIPTION, SHORT_DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final SystemTierType systemTierType = SystemTierType.tierFromString(tierName);
		TierDescription existingTierDesc = systemTierType != null ?
				SessionFactory.newFactory().createTierDescription(systemTierType) : getSession().getUserTier(tierName);
		TierViewItem tvi = getSession().getTierView().stream().filter( (item) -> item.getTierName().equals(tierName) ).findFirst().orElse(null);
		if(existingTierDesc == null || tvi == null) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		TierEditorDialog newTierDialog = new TierEditorDialog(getSession(),true);
		TierInfoEditor tierEditor = newTierDialog.getTierEditor();
		tierEditor.setTierName(tierName + " Copy");
		tierEditor.setVisible(tvi.isVisible());
		tierEditor.setBlind(existingTierDesc.isBlind());
		tierEditor.setAligned(!existingTierDesc.isExcludeFromAlignment());
		tierEditor.setLocked(tvi.isTierLocked());
		tierEditor.setTierFont(tvi.getTierFont());
		tierEditor.setTierType(existingTierDesc.getDeclaredType());

		newTierDialog.add(tierEditor);
		newTierDialog.setTitle("Duplicate Tier");
		newTierDialog.getHeader().setHeaderText("Duplicate Tier");
		newTierDialog.getHeader().setDescText("Create a new tier with the contents of " + existingTierDesc.getName());
		newTierDialog.setModal(true);
		newTierDialog.pack();

		if(newTierDialog.showDialog()) {
			final Session session = getSession();
			// get tier info
			String newTierName = tierEditor.getTierName();
			newTierName = StringUtils.strip(newTierName);

			// create tier
			final TierDescription tierDescription = tierEditor.createTierDescription();
			final TierViewItem tierViewItem = tierEditor.createTierViewItem();

			getUndoSupport().beginUpdate();
			for(Record r:getSession().getRecords()) {
				Tier<?> existingTier = r.getTier(tierName);
				Tier<?> dupTier = SessionFactory.newFactory().createTier(tierDescription);
				if(existingTier != null) {
					dupTier.setText(existingTier.toString());
				}
				r.putTier(dupTier);
			}
			final AddTierEdit edit = new AddTierEdit(session, getEventManager(), tierDescription, tierViewItem, index);
			getUndoSupport().postEdit(edit);
			getUndoSupport().endUpdate();
		}
	}

}
