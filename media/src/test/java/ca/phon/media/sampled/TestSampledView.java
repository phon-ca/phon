/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.media.sampled;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestSampledView {
	
	public static void main(String[] args) throws IOException {
		final PCMSampled model = 
				new PCMSampled(new File("src/test/resources/DemoVideo.wav"));
		
		final Runnable onEDT = new Runnable() {
			
			@Override
			public void run() {
				final PCMSegmentView view = new PCMSegmentView(model);
				view.setOpaque(true);
				view.setBackground(Color.WHITE);
				view.setWindowStart(0.0f);
				view.setWindowLength(4.0f);
				view.setSegmentStart(0.5f);
				view.setSegmentLength(3.0f);
				view.setSelectionStart(1.0f);
				view.setSelectionLength(1.5f);
				
				view.setBackground(Color.black);
				view.setForeground(Color.decode("#FFFF66"));
				view.setSelectionColor(Color.decode("#4D4DFF"));
				view.setChannelColor(Channel.LEFT, Color.decode("#83F52C"));
				view.setChannelColor(Channel.RIGHT, Color.decode("#FF0066"));

				final JFrame f = new JFrame("Test");
				f.add(view);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setSize(500, 200);
				f.setVisible(true);
			}
		};
		SwingUtilities.invokeLater(onEDT);
	}

}
