/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.app.session.editor.view.segmentation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import ca.phon.util.MsFormatter;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Simple component which displays two labels for
 * the start/end time of a segment.  Start time can be
 * locked/unlocked as necessary for segmentation.
 *
 */
public class SegmentLabel extends JComponent {

	/**
	 * Segment window (0 means use last end time)
	 */
	private int segmentWindow = 0;
	
	/**
	 * Last segment end value
	 */
	private volatile long startLockTime = -1L;

	/**
	 * Current time
	 */
	private volatile long currentTime = 0L;

	private final ImageIcon lockedIcon = 
			IconManager.getInstance().getIcon("actions/lock", IconSize.XSMALL);
	private final ImageIcon unlockedIcon =
			IconManager.getInstance().getIcon("actions/unlock", IconSize.XSMALL);
	
	/*
	 * UI
	 */
	private JLabel startTimeLbl;
	private JLabel currentTimeLbl;

	public SegmentLabel() {
		super();

		init();
	}

	private void init() {
		setLayout(new FlowLayout(FlowLayout.LEADING));

		
		startTimeLbl = new JLabel();
		startTimeLbl.setIcon(unlockedIcon);
		startTimeLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		startTimeLbl.setToolTipText("Click to lock start time to current time");
		startTimeLbl.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent evt) {
				if(startLockTime >= 0) {
					lockSegmentStartTime(-1L);
				} else {
					lockSegmentStartTime(getCurrentTime());
				}
			}

		});

		currentTimeLbl = new JLabel();

		add(startTimeLbl);
		add(new JLabel(" - "));
		add(currentTimeLbl);
	}

	public void setSegmentWindow(int window) {
		this.segmentWindow = window;
		refreshLabels();
	}

	/**
	 * Lock segment start time to the given value.
	 * If time < 0, start time lock is disabled.
	 * @param time
	 */
	public void lockSegmentStartTime(long time) {
		this.startLockTime = time;
		refreshLabels();
	}

	/**
	 * Seg current time
	 */
	public void setCurrentTime(long time) {
		this.currentTime = time;
		refreshLabels();
	}

	public long getCurrentTime() {
		return this.currentTime;
	}

	public long getStartTime() {
		long startTime =
				(segmentWindow > 0 ? currentTime - segmentWindow :
					startLockTime);
		startTime = Math.max(0, startTime);
		if(startTime < startLockTime)
			startTime = startLockTime;
		return startTime;
	}

	public void refreshLabels() {
		//call _refreshLabels on the EDT
		Runnable run = new Runnable() {
			@Override
			public void run() {
				_refreshLabels();
			}
		};
		SwingUtilities.invokeLater(run);
	}

	private void _refreshLabels() {
		String currentTimeTxt =
				MsFormatter.msToDisplayString(currentTime);

		long startTime = getStartTime();
		String startTimeTxt =
				MsFormatter.msToDisplayString(startTime);

		startTimeLbl.setText(startTimeTxt);
		currentTimeLbl.setText(currentTimeTxt);

		if(startTime > currentTime) {
			startTimeLbl.setForeground(Color.red);
		} else {
			if(startLockTime >= 0) {
				startTimeLbl.setForeground(Color.blue);
				String newTxt =
						"<html><u>" + startTimeTxt + "</u></html>";
				startTimeLbl.setText(newTxt);
				startTimeLbl.setIcon(lockedIcon);
				startTimeLbl.setToolTipText("Click to unlock start time");
			} else {
				startTimeLbl.setForeground(Color.black);
				startTimeLbl.setIcon(unlockedIcon);
				startTimeLbl.setToolTipText("Click to lock start time to current time");
			}
		}
	}

}
