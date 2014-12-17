package ca.phon.media.sampled;

import java.awt.Color;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class TestSampledView {
	
	public static void main(String[] args) {
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
				view.setForeground(Color.white);
				view.setChannelColor(Channel.LEFT, Color.decode("#FFFF00"));
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
