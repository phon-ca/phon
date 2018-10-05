/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
