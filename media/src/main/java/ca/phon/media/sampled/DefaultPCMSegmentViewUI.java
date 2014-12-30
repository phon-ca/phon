package ca.phon.media.sampled;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.joda.time.Seconds;

import ca.phon.media.sampled.actions.PlayAction;
import ca.phon.media.sampled.actions.PlaySegmentAction;
import ca.phon.media.sampled.actions.PlaySelectionAction;
import ca.phon.media.sampled.actions.SaveSegmentAction;
import ca.phon.media.sampled.actions.SaveSelectionAction;
import ca.phon.media.sampled.actions.SelectMixerAction;
import ca.phon.media.sampled.actions.SelectSegmentAction;
import ca.phon.media.sampled.actions.StopAction;
import ca.phon.media.sampled.actions.ToggleChannelVisible;
import ca.phon.media.sampled.actions.ToggleLoop;
import ca.phon.ui.nativedialogs.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;


/**
 * Default UI for PCM segment view.
 *
 */
public class DefaultPCMSegmentViewUI extends PCMSegmentViewUI {
	
	private PCMSegmentView view;
	
	private Rectangle2D contentRect;
	
	private double timeBarHeight = 0.0;
	
	private Map<Channel, SampledPainter> channelPainters = new HashMap<Channel, SampledPainter>();
	
	private Cursor magnifyCursor;
	
	public DefaultPCMSegmentViewUI(PCMSegmentView view) {
		super();
		this.view = view;
		
		final ImageIcon magnifyIcon = IconManager.getInstance().getIcon("actions/magnifier", IconSize.LARGE);
		magnifyCursor = Toolkit.getDefaultToolkit().createCustomCursor(magnifyIcon.getImage(), new Point(0,0), "magnify");
		
		install(view);
	}

	private void install(PCMSegmentView view) {
		setupKeymap(view);
		
		view.addKeyListener(keyListener);
		
		view.addMouseListener(mouseHandler);
		view.addMouseMotionListener(mouseHandler);
		view.addMouseWheelListener(mouseHandler);
		
		view.addPropertyChangeListener(PCMSegmentView.SEGMENT_START_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.SELECTION_LENGTH_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.SEGMENT_LENGTH_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.WINDOW_START_PROT, propListener);
		view.addPropertyChangeListener(PCMSegmentView.WINDOW_LENGTH_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.CURSOR_LOCATION_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.CHANNEL_VISIBLITY_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.CHANNEL_COLOR_PROP, propListener);
		view.addPropertyChangeListener(PCMSegmentView.PLAYBACK_MARKER_PROP, propListener);
		
		view.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	private void setupKeymap(PCMSegmentView view) {
		final ActionMap actionMap = view.getActionMap();
		final InputMap inputMap = view.getInputMap(JComponent.WHEN_FOCUSED);
		
		inputMap.put(SelectSegmentAction.KS, SelectSegmentAction.TXT);
		actionMap.put(SelectSegmentAction.TXT, new SelectSegmentAction(view));
		
		inputMap.put(PlayAction.KS, PlayAction.TXT);
		actionMap.put(PlayAction.TXT, new PlayAction(view));
		
		inputMap.put(StopAction.KS, StopAction.TXT);
		actionMap.put(StopAction.TXT, new StopAction(view));
		
		view.setActionMap(actionMap);
		view.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}
	
	private double calculateTimeBarHeight(Graphics g) {
		final FontMetrics fm = g.getFontMetrics(view.getFont());
		final String testString = "000:00.000";
		final Rectangle2D bounds = fm.getStringBounds(testString, g);
		
		return bounds.getHeight() + 
				 2 * (1 + 2); // border plus spacing
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		// draw background
		if(view.isOpaque()) {
			final Color bg = view.getBackground();
			g.setColor(bg);
			g.fillRect(0, 0, view.getWidth(), view.getHeight());
		}
		
		if(view.getSampled() == null) return;
		
		final Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		if(timeBarHeight == 0.0) {
			timeBarHeight = calculateTimeBarHeight(g2);
		}
		final Rectangle2D timeBarRect = new Rectangle2D.Double(0.0, 1.0, view.getWidth(), timeBarHeight);
		contentRect = new Rectangle2D.Double(0.0, timeBarHeight + 2, view.getWidth(),
				view.getHeight() - timeBarHeight);
		
		final Line2D topLine = new Line2D.Double(timeBarRect.getX(), timeBarRect.getY(),
				timeBarRect.getX() + timeBarRect.getWidth(), timeBarRect.getY());
		final Line2D btmLine = new Line2D.Double(timeBarRect.getX(), timeBarRect.getY() + timeBarRect.getHeight(),
				timeBarRect.getX() + timeBarRect.getWidth(), timeBarRect.getY() + timeBarRect.getHeight());
		
		final Color borderColor = view.getForeground();
		g2.setColor(borderColor);
		g2.draw(topLine);
		g2.draw(btmLine);

		int numVisibleChannels = view.getSampled().getNumberOfChannels();
		for(int i = 0; i < view.getSampled().getNumberOfChannels(); i++) {
			if(!view.isChannelVisible(Channel.values()[i])) --numVisibleChannels;
		}
		
		// draw excluded ranges
		final double pixelsPerSecond = (double)view.getWidth() / (double)view.getWindowLength();
		final float segStart = view.getSegmentStart();
		final float segLen = view.getSegmentLength();
		final float segEnd = segStart + segLen;
		
		final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2}, 0);
		g2.setStroke(dashed);
		if(segStart > 0 && segLen > 0) {
			// left excluded rect
			if(segStart > view.getWindowStart()) {
				final double x = 0.0;
				final double y = contentRect.getY();
				final double height = view.getHeight();
				final double width = (view.getSegmentStart() - view.getWindowStart()) * pixelsPerSecond;
				
				final Rectangle2D excludedRect = new Rectangle2D.Double(x, y, width, height);
				g2.setColor(view.getExcludedColor());
				g2.fill(excludedRect);
				
				final Line2D excludedBoundary = new Line2D.Double(x+width, y, x+width, height);
				g2.setColor(view.getExcludedColor().darker());
				g2.draw(excludedBoundary);
			}
			
			// right excluded rect
			if(segEnd < view.getWindowStart() + view.getWindowLength()) {
				final double height = view.getHeight();
				final double width = ((view.getWindowStart() + view.getWindowLength()) - segEnd) * pixelsPerSecond;
				final double x = view.getWidth() - width;
				final double y = contentRect.getY();
				
				final Rectangle2D excludedRect = new Rectangle2D.Double(x, y, width, height);
				g2.setColor(view.getExcludedColor());
				g2.fill(excludedRect);
				
				final Line2D excludedBoundary = new Line2D.Double(x, y, x, height);
				g2.setColor(view.getExcludedColor().darker());
				g2.draw(excludedBoundary);
			}
		}
		
		// draw selection
		if(view.getSelectionStart() > 0.0f &&
				view.getSelectionLength() > 0.0f) {
			final double startX = view.modelToView(view.getSelectionStart());
			final double endX = view.modelToView(view.getSelectionStart() + view.getSelectionLength());
			
			final Rectangle2D selRect = new Rectangle2D.Double(startX, contentRect.getY(),
					endX - startX, contentRect.getHeight());
			g2.setColor(view.getSelectionColor());
			g2.fill(selRect);
			
			
			// draw selection lines
			final Line2D startLine = new Line2D.Double(startX,
					contentRect.getY(), startX, contentRect.getY() + contentRect.getHeight()
					);
			final Line2D endLine = new Line2D.Double(endX, contentRect.getY(),
					endX, contentRect.getY() + contentRect.getHeight());
			g2.setColor(view.getSelectionColor().darker());
			g2.draw(startLine);
			g2.draw(endLine);
		}
		
		// draw channels
		final double channelHeight = contentRect.getHeight() / (double)numVisibleChannels;
		for(int i = 0, displayed = 0; i < view.getSampled().getNumberOfChannels(); i++) {
			if(!view.isChannelVisible(Channel.values()[i])) continue;
			
			final double y = contentRect.getY() + (displayed * channelHeight);
			final Rectangle2D channelRect = new Rectangle2D.Double(
					0, y, (double)view.getWidth(), channelHeight);
			SampledPainter painter = channelPainters.get(Channel.values()[i]);
			if(painter == null) {
				painter = new SampledPainter(i, view.getChannelColor(Channel.values()[i]));
				channelPainters.put(Channel.values()[i], painter);
			}
			painter.setWindowStart(view.getWindowStart());
			painter.setWindowLength(view.getWindowLength());
			painter.paint(view.getSampled(), g2, channelRect);
			
			displayed++;
		}
		
		// cursor
		if(!view.isPlaying() && view.getCursorPosition() >= 0) {
			g2.setStroke(dashed);
			final Line2D cursorLine = new Line2D.Double(
					view.getCursorPosition(), contentRect.getY(),
					view.getCursorPosition(), contentRect.getY() + contentRect.getHeight());
			g2.draw(cursorLine);
		}
		
		if(view.isPlaying() && view.getPlaybackMarker() > view.getWindowStart()
				&& view.getPlaybackMarker() < (view.getWindowStart() + view.getWindowLength())) {
			g2.setStroke(dashed);
			double playbackPos = view.modelToView(view.getPlaybackMarker());
			final Line2D playbackLine = new Line2D.Double(
					playbackPos, contentRect.getY(),
					playbackPos, contentRect.getY() + contentRect.getHeight());
			g2.draw(playbackLine);
		}
		
	}
	
	private void showContextMenu(Point p) {
		final JPopupMenu menu = new JPopupMenu();
		
		final JMenuItem selectSegAct = new JMenuItem(new SelectSegmentAction(view));
		selectSegAct.setEnabled(view.getSelectionStart() > 0.0f && view.getSelectionLength() > 0.0f);
		menu.add(selectSegAct);
		
		menu.addSeparator();
		for(int i = 0; i < view.getSampled().getNumberOfChannels(); i++) {
			final Channel ch = Channel.values()[i];
			final ToggleChannelVisible toggleAct = new ToggleChannelVisible(view, ch);
			final JCheckBoxMenuItem menuItm = new JCheckBoxMenuItem(toggleAct);
			menu.add(menuItm);
		}

		menu.addSeparator();
		if(!view.isPlaying()) {
			final JMenuItem playSegmentItem = new JMenuItem(new PlaySegmentAction(view));
			playSegmentItem.setEnabled(view.hasSegment());
			menu.add(playSegmentItem);
			final JMenuItem playSelectionItem = new JMenuItem(new PlaySelectionAction(view));
			playSelectionItem.setEnabled(view.hasSelection());
			menu.add(playSelectionItem);
			final JCheckBoxMenuItem loopItem = new JCheckBoxMenuItem(new ToggleLoop(view));
			menu.add(loopItem);
			// output device selection
			final JMenu mixerMenu = new JMenu("Output Device");
			final Info[] mixers = AudioSystem.getMixerInfo();
			for(Info mixerInfo:mixers) {
				final SelectMixerAction mixerAct = new SelectMixerAction(view, mixerInfo);
				mixerAct.putValue(SelectMixerAction.SELECTED_KEY,
						view.getMixerInfo() == mixerInfo);
				mixerMenu.add(new JCheckBoxMenuItem(mixerAct));
			}
			menu.add(mixerMenu);
		} else {
			menu.add(new StopAction(view));
		}
		
		menu.addSeparator();
		final JMenuItem saveSegmentItem = new JMenuItem(new SaveSegmentAction(view));
		saveSegmentItem.setEnabled(view.hasSegment());
		menu.add(saveSegmentItem);
		final JMenuItem saveSelectionItem = new JMenuItem(new SaveSelectionAction(view));
		saveSelectionItem.setEnabled(view.hasSelection());
		menu.add(saveSelectionItem);
		
		menu.show(view, p.x, p.y);
	}
	
	private final PropertyChangeListener propListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(!view.isValuesAdjusting()) {
				if(evt.getPropertyName().equals(PCMSegmentView.CURSOR_LOCATION_PROP)) {
					final int oldVal = (Integer)evt.getOldValue();
					final int newVal = (Integer)evt.getNewValue();
					
					final int x = Math.min(oldVal, newVal);
					final int xmax = Math.max(oldVal, newVal);
					final Rectangle clipRect = 
							new Rectangle(x-2, 0, xmax-x+4, view.getHeight());
					view.repaint(clipRect);
				} else if(evt.getPropertyName().equals(PCMSegmentView.PLAYBACK_MARKER_PROP)) {
					final float oldVal = (Float)evt.getOldValue();
					final float newVal = (Float)evt.getNewValue();
					
					final double oldX = view.modelToView(oldVal);
					final double newX = view.modelToView(newVal);
					final Rectangle clipRect = 
							new Rectangle((int)oldX, 0, (int)newX, view.getHeight());
					view.repaint(clipRect);
				} else {
					view.repaint();
				}
			}
		}
		
	};
	
	private final KeyListener keyListener = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				view.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
				view.setCursor(magnifyCursor);
			}
		}
	};
	
	private final MouseInputAdapter mouseHandler = new MouseInputAdapter() {
		
		private boolean isDraggingSelection = false;
		
		private boolean isDraggingPosition = false;
		private int dragStartX = 0;
		
		@Override
		public void mouseExited(MouseEvent e) {
			// clear cursor position
			view.setCursorPosition(-1);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(!isDraggingSelection) {
				view.setCursorPosition(e.getX());
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			view.requestFocus();
			if(OSInfo.isMacOs() && e.isPopupTrigger())
				showContextMenu(e.getPoint());
			if(e.getButton() != MouseEvent.BUTTON1) return;
			dragStartX = e.getX();
			if(e.getY() > contentRect.getY()) {
				final int x = e.getX();
				final float time = view.viewToModel(x);
				view.setValuesAdusting(true);
				view.setSelectionStart(time);
				view.setValuesAdusting(false);
				view.setSelectionLength(0.0f);
				view.setCursorPosition(-1);
				isDraggingSelection = true;
			} else {
				isDraggingPosition = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(!OSInfo.isMacOs() && e.isPopupTrigger()) {
				showContextMenu(e.getPoint());
			} else {
				isDraggingSelection = false;
				isDraggingPosition = false;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(isDraggingSelection) {
				final int x = e.getX();
				final float time = view.viewToModel(x);
				final float dragStartTime = view.viewToModel(dragStartX);
				
				final float startValue = 
						Math.min(time, dragStartTime);
				final float endValue = 
						Math.max(time, dragStartTime);
				final float len = endValue - startValue;
				
				view.setValuesAdusting(true);
				view.setSelectionStart(startValue);
				view.setValuesAdusting(false);
				view.setSelectionLength(len);
			} else if(isDraggingPosition) {
				final int x = e.getX();
				final int diff = x - dragStartX;
				final float change = diff * 0.1f;
				dragStartX = x;
				
				float newStart = view.getWindowStart() + change;
				if(newStart < view.getSampled().getStartTime()) newStart = view.getSampled().getStartTime();
				view.setWindowStart(newStart);
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if((e.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
				final int numTicks = e.getWheelRotation();
				
				final float zoomAmount = 0.25f * numTicks;
				float windowLength = view.getWindowLength() + zoomAmount;
				
				if(windowLength < 0.1f) {
					windowLength = 0.1f;
				} else if(windowLength > view.getSampled().getLength()) {
					windowLength = view.getSampled().getLength();
				}
				view.setWindowLength(windowLength);
			}
		}
		
	};
	
}
