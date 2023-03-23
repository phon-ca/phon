package ca.phon.app.session.editor.view.timeline.actions;

import ca.phon.app.log.LogUtil;
import ca.phon.app.session.editor.undo.RecordSegmentEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.timeline.TimelineView;
import ca.phon.media.LongSound;
import ca.phon.session.Record;
import ca.phon.session.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * Distribute unsegmented records evenly across the space between existing records while maintaining order
 */
public class DistributeRecordsAction extends TimelineAction {

	public final static String TXT = "Distribute unsegmented records";

	public final static String DESC = "Distribute unsegmented records evenly across the space between existing records while maintaining order";

	public DistributeRecordsAction(TimelineView view) {
		super(view);

		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		if(!getView().getEditor().getMediaModel().isSessionAudioAvailable()) return;

		float currentStart = 0.0f;
		Session session = getView().getEditor().getSession();
		DistributeRecordTreeNode recordTreeNode = new DistributeRecordTreeNode();
		for(int rIdx = 0; rIdx < session.getRecordCount(); rIdx++) {
			insertRecord(recordTreeNode, rIdx, session.getRecord(rIdx));
		}

		try {
			LongSound sessionAudio = getView().getEditor().getMediaModel().getSharedSessionAudio();
			float maxValue = sessionAudio.length() * 1000.0f;

			getView().getEditor().getUndoSupport().beginUpdate();
			distributeZeroLengthRecords(recordTreeNode, 0.0f, maxValue);
			getView().getEditor().getUndoSupport().endUpdate();
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}

	private void distributeZeroLengthRecords(DistributeRecordTreeNode node, float startValue, float maxValue) {
		if(node.zeroRecord != null) {
			float rightEdge = maxValue;
			if(node.nonzeroRecord != null) {
				MediaSegment rightEdgeSeg = node.nonzeroRecord.value.getMediaSegment();
				rightEdge = rightEdgeSeg.getStartValue();
			}

			// collect zero-length records from this node
			List<Record> zeroLengthRecords = new ArrayList<>();
			DistributeRecordTreeNode currentNode = node;
			while(currentNode != null) {
				if(currentNode.zeroRecord != null)
					zeroLengthRecords.add(currentNode.zeroRecord.value);
				currentNode = currentNode.zeroRecord;
			}

			if(zeroLengthRecords.size() > 0) {
				float length = rightEdge - startValue;
				float recordLen = length / zeroLengthRecords.size();

				SessionFactory factory = SessionFactory.newFactory();
				float currentStart = startValue;
				for(int i = 0; i < zeroLengthRecords.size(); i++) {
					Record r = zeroLengthRecords.get(i);
					MediaSegment seg = factory.createMediaSegment();
					seg.setStartValue(currentStart);
					seg.setEndValue(currentStart + recordLen);

					currentStart += recordLen;

					RecordSegmentEdit segEdit = new RecordSegmentEdit(getView().getEditor(), r, seg);
					getView().getEditor().getUndoSupport().postEdit(segEdit);
				}
			}
		}
		if(node.nonzeroRecord != null) {
			MediaSegment leftEdgeSeg = node.nonzeroRecord.value.getMediaSegment();
			distributeZeroLengthRecords(node.nonzeroRecord, leftEdgeSeg.getEndValue(), maxValue);
		}
	}

	private void insertRecord(DistributeRecordTreeNode node, int recordIndex, Record record) {
		MediaSegment seg = record.getMediaSegment();
		boolean isZero = seg.getStartValue() == 0.0f && seg.getEndValue() == 0.0f;

		if(isZero) {
			if(node.nonzeroRecord != null) {
				insertRecord(node.nonzeroRecord, recordIndex,  record);
			} else {
				if(node.zeroRecord == null) {
					node.zeroRecord = new DistributeRecordTreeNode();
					node.zeroRecord.value = record;
					node.zeroRecord.recordIndex = recordIndex;
				} else {
					insertRecord(node.zeroRecord, recordIndex, record);
				}
			}
		} else {
			if(node.nonzeroRecord == null) {
				node.nonzeroRecord = new DistributeRecordTreeNode();
				node.nonzeroRecord.value = record;
				node.nonzeroRecord.recordIndex = recordIndex;
			} else {
				insertRecord(node.nonzeroRecord, recordIndex, record);
			}
		}
	}

	private static class DistributeRecordTreeNode {
		DistributeRecordTreeNode zeroRecord;
		DistributeRecordTreeNode nonzeroRecord;
		int recordIndex;
		Record value;
	}

}
