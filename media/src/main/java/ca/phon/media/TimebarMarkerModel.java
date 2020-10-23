package ca.phon.media;

import java.awt.*;
import java.util.*;

import javax.swing.*;

public class TimebarMarkerModel {
	
	private Collection<Marker> pointMarkers = new TreeSet<>();
	
	public Marker addMarker(float time) {
		return null;
	}
	
	public Marker addMarker(float time, Icon icon) {
		return null;
	}
	
	public Marker addMarker(float time, String label) {
		return null;
	}
	
	public Marker addMarker(float time, Icon icon, String label) {
		Marker marker = new Marker();
		marker.setTime(time);
		marker.setIcon(icon);
		marker.setLabel(label);
		return marker;
	}
	
	public static class Marker implements Comparable<Marker> {
		private float time;
		private Icon icon;
		private String label;
		
		public float getTime() {
			return time;
		}
		public void setTime(float time) {
			this.time = time;
		}
		public Icon getIcon() {
			return icon;
		}
		public void setIcon(Icon icon) {
			this.icon = icon;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		
		@Override
		public int compareTo(Marker o) {
			return Float.valueOf(time).compareTo(o.time);
		}
	}
	
	public static class Interval {
		private Marker startMarker;
		private Marker endMarker;
		private Paint backgroundPaint;
		
		public Marker getStartMarker() {
			return startMarker;
		}
		public void setStartMarker(Marker startMarker) {
			this.startMarker = startMarker;
		}
		public Marker getEndMarker() {
			return endMarker;
		}
		public void setEndMarker(Marker endMarker) {
			this.endMarker = endMarker;
		}
	}
	
}
