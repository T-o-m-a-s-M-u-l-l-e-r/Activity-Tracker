package model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import me.marnic.jiconextract2.JIconExtract;

class Program {
	public static String default_icon_path = "";
	public static final String INPUT_OUTPUT_FORMAT = "dd-MM-y_HH:mm:ss";
	private Hashtable<String, String> propertyDictionary;
	private ArrayList<Session> sessions = new ArrayList<Session>();
	private Hashtable<Integer, DateTime> startTimes = new Hashtable<Integer, DateTime>();
	private BufferedImage icon;

	public Program(Hashtable<String, String> propertyDictionary, boolean loaded) {
		this.propertyDictionary = new Hashtable<String, String>(propertyDictionary);
		if (loaded) {
			sessions = parseSessions(propertyDictionary.get("Sessions"));
			this.propertyDictionary.remove("Sessions");
		} else {
			DateTime startTime = Tracker.powershellFormatter
					.parseDateTime(propertyDictionary.get(WindowProcess.Property.StartTime.toString()));
			int id = Integer.valueOf(propertyDictionary.get(WindowProcess.Property.Id.toString()));
			startTimes.put(id, startTime);
		}
		this.propertyDictionary.remove(WindowProcess.Property.Id.toString());
		this.propertyDictionary.remove(WindowProcess.Property.StartTime.toString());
		BufferedImage getIcon = JIconExtract.getIconForFile(128, 128,
				new File(getProperty(WindowProcess.Property.Path.toString())));

		icon = getIcon;
	}
	
	public void cleanIcon() {
		
		if(!default_icon_path.isBlank() && icon == null) {
			icon = JIconExtract.getIconForFile(128, 128, new File(default_icon_path));
		}
		
	}

	@Override
	public String toString() {
		String output = "";
		for (String key : propertyDictionary.keySet()) {
			output += String.format("%s : %s\n", key, getProperty(key));
		}

		output += String.format("%s : %s\n\n", "Sessions", sessionsToString());
		return output;
	}

	public String sessionsToString() {
		String output = "";
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(INPUT_OUTPUT_FORMAT);

		for (int i = 0; i < sessions.size(); i++) {
			Session period = sessions.get(i);
			String startDate = dateFormatter.print(period.start);
			String endDate = dateFormatter.print(period.end);
			output += String.format("%d - %s - %s", period.id, startDate, endDate);
			if (i != sessions.size() - 1) {
				output += ", ";
			}
		}

		return String.format("{%s}", output);
	}

	public ArrayList<Session> getSessions() {
		return sessions;
	}

	public ArrayList<Session> parseSessions(String input) {
		ArrayList<Session> sessions = new ArrayList<Session>();

		if (!input.isBlank()) {
			DateTimeFormatter dateFormatter = DateTimeFormat.forPattern(INPUT_OUTPUT_FORMAT);
			String[] sessionString = input.trim().replaceAll("[{}]", "").split("\\s?,\\s?");
			for (int i = 0; i < sessionString.length; i++) {
				String[] dates = sessionString[i].trim().split("\\s+-\\s+");
				int id = Integer.valueOf(dates[0]);
				DateTime startDate = dateFormatter.parseDateTime(dates[1]);
				DateTime endDate = dateFormatter.parseDateTime(dates[2]);
				sessions.add(new Session(id, startDate, endDate));
			}
		}

		return sessions;
	}

	public void start(int id, DateTime startTime) {
		startTimes.putIfAbsent(id, startTime);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WindowProcess) {
			WindowProcess compareTo = (WindowProcess) obj;
			WindowProcess.Property[] compareProperties = { WindowProcess.Property.Name, WindowProcess.Property.Company,
					WindowProcess.Property.Path, WindowProcess.Property.Product, WindowProcess.Property.Description };
			for (WindowProcess.Property property : compareProperties) {
				if (!compareProperty(compareTo, property.toString())) {
					return false;
				}
			}
			return true;
		} else {
			return super.equals(obj);
		}
	}

	private boolean compareProperty(WindowProcess other, String key) {
		return other.getProperty(key).equals(getProperty(key));
	}

	public String getProperty(String key) {
		return propertyDictionary.get(key);
	}

	public Hashtable<String, String> getDictionary() {
		return propertyDictionary;
	}

	public BufferedImage getIcon() {
		return icon;
	}

	public void end(int id, DateTime endTime) {
		DateTime startTime = startTimes.getOrDefault(id, null);

		if (startTime != null) {
			startTimes.remove(id);
			Session session = new Session(id, startTime, endTime);
			if (sessions.contains(session)) {
				int index = sessions.indexOf(session);
				sessions.remove(index);
			}
			sessions.add(session);
		}

	}

	public void endAll(DateTime endTime) {
		HashSet<Integer> keySet = new HashSet<Integer>(startTimes.keySet());

		for (Integer id : keySet) {
			end(id, endTime);
		}
	}

	static class SessionComparator implements Comparator<Session> {

		@Override
		public int compare(Session o1, Session o2) {
			return o1.compareTo(o2);
		}

	}

	static class Session implements Comparable {
		private DateTime start, end;
		private int id;

		public Session(int id, DateTime start, DateTime end) {
			this.id = id;
			this.start = start;
			this.end = end;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Session) {
				Session other = (Session) obj;
				return other.start.isEqual(start) && other.id == this.id;
			}
			return super.equals(obj);
		}

		public DateTime getStart() {
			return start;
		}

		public DateTime getEnd() {
			return end;
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof Session) {
				Session compareTo = (Session) o;
				DateTime startOther = compareTo.getStart();
				DateTime startSelf = getStart();
				return startSelf.compareTo(startOther);
			}

			return -2;
		}

	}

}