package model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

import controllers.HistoryTab.SpecialComboBox.ProgramDataWrapper;
import javafx.geometry.Point2D;
import javafx.util.Pair;
import model.Program.Session;
import model.Program.SessionComparator;

public class Interpret {
	private ArrayList<ProgramData> dataDatabase;

	public Interpret(ArrayList<Program> programDatabase) {
		dataDatabase = new ArrayList<ProgramData>();

		for (Program program : programDatabase) {
			dataDatabase.add(new ProgramData(program.getDictionary(), program.getSessions(), program.getIcon()));
		}

		dataDatabase.sort(new ProgramDataTimesOpenedComparator(this, null));
	}
	
	public int getTimesOpened(ProgramData program, DateTime intervalStart) {
		ArrayList<Session> sessions = new ArrayList<Session>(program.getSessions());
		
		if (intervalStart == null) {
			return sessions.size();
		}
		
		int count = 0;
		
		for (Session session : sessions) {
			DateTime start = session.getStart();
			if (intervalStart.compareTo(start) <= 0) {
				count++;
			}
		}
		
		return count;
	}
	
	public Duration getRuntime(ProgramData program, DateTime intervalStart) {
		ArrayList<Session> sessions = new ArrayList<Session>(program.getSessions());
		Interval interval = intervalStart == null ? null : new Interval(intervalStart, new DateTime());
		Duration runtime = new Duration(0);
		
		for (Session session : sessions) {
			Interval sessionInterval = new Interval(session.getStart(), session.getEnd());
			if (intervalStart != null) {
				 sessionInterval = sessionInterval.overlap(interval);
			}
			if (sessionInterval != null) {
				runtime = runtime.plus(sessionInterval.toDuration());
			}
		}
		
		
		return runtime;
	}
	
	
	
	public ArrayList<Pair<Double, Double>> getProgramHistory(ProgramData program, DateTime day) {
		ArrayList<Pair<Double, Double>> output = new ArrayList<Pair<Double, Double>>();
		ArrayList<Interval> programIntervals = new ArrayList<Interval>(program.getSessionIntervals());
		DateTime dayStart = DateTimeFormat.forPattern("dd-M-y, HH:mm")
				.parseDateTime(String.format("%s-%s-%s, 00:00", day.getDayOfMonth(), day.getMonthOfYear(), day.getYear()));
		Interval sample = new Interval(dayStart, dayStart.plusDays(1));
		
		for (Interval inv : programIntervals) {
			
			Interval overlap = sample.overlap(inv);
			
			if (overlap != null) {
				DateTime overlapStart = overlap.getStart();
				double start = overlapStart.getMinuteOfDay()/60.0;
				Duration duration = new Duration(overlapStart, overlap.getEnd());
				double length = duration.getStandardMinutes()/60.0;
				Pair<Double, Double> pair = new Pair<Double, Double>(start, length);
				output.add(pair);
			}
			
			
		}
		
		
		return output;
	}
	
	public ArrayList<ProgramData> getDataDatabase() {
		return dataDatabase;
	}
	
	public Duration getAverageSessionDuration(ProgramData program, DateTime intervalStart) {
		ArrayList<Session> sessions = new ArrayList<Session>(program.getSessions());
		
		return getRuntime(program, intervalStart).dividedBy(sessions.size());
	}
	
	public Duration getTimeElapsedSinceOpen(ProgramData program) {
		Interval interval = program.getSessionIntervals().get(program.getSessionIntervals().size()-1);
		DateTime start = interval.getStart();
		
		return new Duration(start, new DateTime());
	}
	
	public ArrayList<Point2D> getAverageDayTrend(ProgramData program, int day) {
		ArrayList<Interval> sessions = new ArrayList<Interval>(program.getSessionIntervals());
		Hashtable<Integer, Integer> chartData = new Hashtable<Integer, Integer>();
		for (Interval session : sessions) {
			
			DateTime start = session.getStart();
			DateTime end = session.getEnd();
			
			if (start.getDayOfWeek() == end.getDayOfWeek() && end.getDayOfWeek() == day) {

				
				while (start.compareTo(end) <= 0) {
					int i = start.getHourOfDay();
					chartData.put(i, chartData.getOrDefault(i, 0)+1);
					start = start.minusHours(-1);
				}
				
			}
			
		}
		
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		
		for (int i = 0; i <= 24; i++) {
			points.add(new Point2D(i, chartData.getOrDefault(i, 0)));
		}
		
		return points;
	}

//	public ArrayList<Point2D> getProgramDistribution(int timeFrame, DateTime date) {
//		ArrayList<Point2D> points = new ArrayList<Point2D>();
//
//		String day = String.valueOf(date.getDayOfMonth());
//		String month = String.valueOf(date.getMonthOfYear());
//		String year = String.valueOf(date.getYear());
//		DateTime start = DateTimeFormat.forPattern("dd-M-y, HH:mm")
//				.parseDateTime(String.format("%s-%s-%s, 00:00", day, month, year));
//		DateTime end = start.minusDays(-1);
//
//		while (!start.isEqual(end)) {
//			double hour = start.getHourOfDay() + start.getMinuteOfHour() / 60.0;
//			double y = 0;
//			Interval pointInterval = new Interval(start.minusMinutes(15), start.minusMinutes(-15));
//
//			ProgramData chrome = null;
//			for (ProgramData data : dataDatabase) {
//				if (data.getDictionary().get(Property.Name.toString()).equals("chrome")) {
//					chrome = data;
//					break;
//				}
//			}
//
//			for (Interval interval : chrome.sessionIntervals) {
//
//				Interval overlap = interval.overlap(pointInterval);
//				Duration overlapDuration = overlap == null ? new Duration(0) : overlap.toDuration();
//				if (overlapDuration.getStandardMinutes() > 0) {
//					int overlapSec = overlapDuration.toStandardSeconds().getSeconds();
//					int pointIntervalSec = pointInterval.toDuration().toStandardSeconds().getSeconds();
//					y = Double.valueOf(overlapSec) / pointIntervalSec;
//				}
//
//			}
//
//			points.add(new Point2D(hour, y));
//			start = start.minusMinutes(-1);
//		}
//
//		return points;
//	}

//	public ArrayList<ProgramDistribution> getDailyDistribution(int timeFrame, DateTime date) {
//		ArrayList<ProgramDistribution> programs = new ArrayList<ProgramDistribution>();
//
//		for (ProgramData program : dataDatabase) {
//			ArrayList<Point2D> points = getProgramDistribution(timeFrame, date);
//			programs.add(new ProgramDistribution(program.getProperty(Property.Name.toString()), points));
//		}
//
//		return programs;
//	}

	private static ArrayList<Interval> mergeIntervalLists(ArrayList<Interval> inputList) {
		ArrayList<Interval> input = new ArrayList<Interval>(inputList);
		int size = input.size();

		do {

			size = input.size();
			input.sort(new IntervalComparator());

			for (int i = 0; i < input.size(); i++) {

				if (i == input.size() - 1) {
					break;
				} else {
					Interval smaller = input.get(i);
					Interval bigger = input.get(i + 1);

					if (canBeMerged(smaller, bigger)) {
						input.remove(smaller);
						input.remove(bigger);
						input.add(mergeIntervals(smaller, bigger));
						break;
					}
				}
			}

		} while (size != input.size());

		return input;
	}

	private static boolean canBeMerged(Interval smaller, Interval bigger) {
		boolean first = smaller.getStart().compareTo(bigger.getStart()) < 1;
		boolean second = bigger.getStart().compareTo(smaller.getEnd()) < 1;

		return first && second;
	}

	private static Interval mergeIntervals(Interval smaller, Interval bigger) {
		DateTime start = smaller.getStart().compareTo(bigger.getStart()) > 0 ? bigger.getStart() : smaller.getStart();
		DateTime end = smaller.getEnd().compareTo(bigger.getEnd()) < 0 ? bigger.getEnd() : smaller.getEnd();

		return new Interval(start, end);
	}
	
	public ArrayList<Pair<String, String>> getNameList() {
		ArrayList<Pair<String, String>> names = new ArrayList<Pair<String, String>>();
		for (ProgramData program : dataDatabase) {
			Pair<String, String> pair = new Pair<String, String>(program.getProperty(WindowProcess.Property.Name.toString()), program.getPresentableName());
			names.add(pair);
		}
		return names;
	}
	
	public ProgramData getProgram(String name) {
		for (ProgramData program : dataDatabase) {
    		if(program.getProperty("Name").equals(name)) {
    			return program;
    		}
    	}
		return null;
	}
	
	static class ProgramDataTimesOpenedComparator implements Comparator<ProgramData> {
		private Interpret interpret;
		private DateTime intervalStart;
		
		public ProgramDataTimesOpenedComparator(Interpret interpret, DateTime intervalStart) {
			this.interpret = interpret;
			this.intervalStart = intervalStart;
		}

		@Override
		public int compare(ProgramData o1, ProgramData o2) {
			Integer timesOpened1 = interpret.getTimesOpened(o1, intervalStart);
			Integer timesOpened2 = interpret.getTimesOpened(o2, intervalStart);
			
			return timesOpened1.compareTo(timesOpened2);
		}
		
	}

	static class IntervalComparator implements Comparator<Interval> {

		@Override
		public int compare(Interval o1, Interval o2) {
			DateTime start1 = o1.getStart();
			DateTime start2 = o2.getStart();

			return start1.compareTo(start2);
		}

	}

	public static class ProgramData {
		private Hashtable<String, String> propertyDictionary;
		private ArrayList<Interval> sessionIntervals = null;
		private ArrayList<Session> sessions;
		private BufferedImage icon;

		public ProgramData(Hashtable<String, String> propertyDictionary, ArrayList<Session> sessions, BufferedImage icon) {
			this.propertyDictionary = new Hashtable<String, String>(propertyDictionary);
			this.sessions = sessions;
			this.sessions.sort(new SessionComparator());
			this.icon = icon;
		}
		
		public String getPresentableName() {
			String presentable = "";
			
			if (presentable.isBlank()) {
				presentable += propertyDictionary.getOrDefault("Description", "");
			}
			
			if (presentable.isBlank()) {
				presentable += propertyDictionary.getOrDefault("Name", "");
			}
			
			if (presentable.isBlank()) {
				presentable += propertyDictionary.getOrDefault("Product", "");
			}
			
			return presentable;
		}
		
		@Override
		public String toString() {
			return getPresentableName();
		}

		public String getProperty(String key) {
			return propertyDictionary.get(key);
		}
		
		public String getName() {
			return propertyDictionary.getOrDefault("Name", "");
		}

		public Hashtable<String, String> getDictionary() {
			return propertyDictionary;
		}

		public BufferedImage getIcon() {
			return icon;
		}

		public ArrayList<Interval> getSessionIntervals() {
			if (sessionIntervals == null) {
				sessionIntervals = convertToInterval(sessions);
			}
			
			return sessionIntervals;
		}
		
		public ArrayList<Session> getSessions() {
			return sessions;
		}
		
		private ArrayList<Interval> convertToInterval(ArrayList<Session> sessions) {
			ArrayList<Interval> intervals = new ArrayList<Interval>();

			for (Session session : sessions) {
				intervals.add(new Interval(session.getStart(), session.getEnd()));
			}

			return mergeIntervalLists(intervals);
		}
		
		@Override
		public boolean equals(Object obj) {

			if (obj instanceof ProgramDataWrapper) {
				ProgramData program = ((ProgramDataWrapper)obj).getProgramData();
				return propertyDictionary.equals(program.getDictionary());
				
			}
			
			return super.equals(obj);
		}

	}
}
