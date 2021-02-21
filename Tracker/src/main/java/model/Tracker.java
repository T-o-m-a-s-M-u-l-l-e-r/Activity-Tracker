package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Tracker {
	public static final String SAVE_FILE_PATH = String.format("%s/log", System.getProperty("user.home"));
	private static int periodSec = 1;
	private static Timer timer = new Timer();
	public static File saveFile;
	public static DateTimeFormatter powershellFormatter;
	private static Tracker tracker;
	private static ArrayList<Program> programDatabase = new ArrayList<Program>();
	private static ArrayList<WindowProcess> lastProcessBundle = new ArrayList<WindowProcess>();
	private static DateTime lastCheck;

	private Tracker() {
		Process patternProcess;
		lastCheck = new DateTime();
		try {
			patternProcess = Runtime.getRuntime().exec("powershell (Get-culture).DateTimeFormat.FullDateTimePattern");
			BufferedReader patternReader = new BufferedReader(new InputStreamReader(patternProcess.getInputStream()));
			String powershellDatePattern = patternReader.readLine().replaceAll("dddd", "EEEE");
			powershellFormatter = DateTimeFormat.forPattern(powershellDatePattern);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Tracker getInstance() {
		if (tracker == null) {
			tracker = new Tracker();
		}

		return tracker;
	}

	public void start() throws InterruptedException {
		Thread loadThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Tracker.getInstance().loadData();
				} catch (NumberFormatException | IOException e) {
					e.printStackTrace();
				}
			}
		});

		loadThread.start();
		loadThread.join();

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				try {
					periodically();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, 0, periodSec * 1000);

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				try {
					saveData();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 0, 10 * 1000);
	}

	public void end() throws FileNotFoundException, UnsupportedEncodingException {
		timer.cancel();
		saveData();
	}

	private ArrayList<WindowProcess> getRunningProcesses() throws IOException {
		String properties = "";
		ArrayList<WindowProcess.Property> values = new ArrayList<WindowProcess.Property>(
				Arrays.asList(WindowProcess.Property.values()));
		values.remove(WindowProcess.Property.StartTime);

		for (int i = 0; i < values.size(); i++) {
			WindowProcess.Property property = values.get(i);
			properties += String.format("%s, ", property.toString());

		}
		String command = "powershell gps | where {$_.MainWindowTitle } | Format-List ` " + properties
				+ "@{Label = 'StartTime'; Expression = {$_.starttime.tostring((Get-culture).DateTimeFormat.FullDateTimePattern)}} | Ft -autosize | out-string -width 4096";
		Process process = Runtime.getRuntime().exec(command);
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		ArrayList<Hashtable<String, String>> dataBundle = bundleData(streamReader);
		ArrayList<WindowProcess> runningProcesses = new ArrayList<WindowProcess>();
		for (Hashtable<String, String> dict : dataBundle) {

			try {
				runningProcesses.add(new WindowProcess(dict));
			} catch (IllegalArgumentException e) {
				// Happens if I get a process without start time (like 'dwm')
				// Just ignore
			}
		}
		return runningProcesses;
	}

	private ArrayList<Hashtable<String, String>> bundleData(BufferedReader bufferedReader)
			throws NumberFormatException, IOException {
		ArrayList<Hashtable<String, String>> dataBundle = new ArrayList<Hashtable<String, String>>();
		String line;
		boolean start = false;
		boolean wasLastBlank = false;

		Hashtable<String, String> properties = new Hashtable<String, String>();

		while ((line = bufferedReader.readLine()) != null) {

			if (!start && !line.isEmpty()) {
				start = true;
			}

			if (start) {

				if (line.isBlank()) {
					if (!wasLastBlank) {
						dataBundle.add(new Hashtable<String, String>(properties));

						if (Program.default_icon_path.isBlank()
								&& properties.getOrDefault(WindowProcess.Property.Name.toString(), "")
										.equals("ApplicationFrameHost")) {
							Program.default_icon_path = properties.getOrDefault(WindowProcess.Property.Path.toString(),
									"C:\\WINDOWS\\system32\\ApplicationFrameHost.exe");
						}

						properties.clear();
						wasLastBlank = true;
					}
				} else {
					String[] split = readGpsLine(line);
					properties.put(split[0], split[1]);
					wasLastBlank = false;
				}

			}
		}

		return dataBundle;
	}

	private void updateRunningProcesses() throws IOException {
		ArrayList<WindowProcess> runningProcesses = getRunningProcesses();

		for (WindowProcess runningProcess : runningProcesses) {

			if (!programDatabase.contains(runningProcess)) {
				programDatabase.add(new Program(runningProcess.getDictionary(), false));
			} else {
				int programIndex = programDatabase.indexOf(runningProcess);
				int id = Integer.valueOf(runningProcess.getProperty(WindowProcess.Property.Id.toString()));
				programDatabase.get(programIndex).start(id, runningProcess.getStartTime());
			}

		}

		ArrayList<WindowProcess> notRunning = new ArrayList<WindowProcess>(lastProcessBundle);
		notRunning.removeAll(runningProcesses);

		for (WindowProcess process : notRunning) {

			for (Program program : programDatabase) {
				program.end(process.getId(), lastCheck);
			}
		}

		lastProcessBundle = new ArrayList<WindowProcess>(runningProcesses);
	}

	private void loadData() throws NumberFormatException, IOException {
		saveFile = new File(SAVE_FILE_PATH);

		if (saveFile.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(saveFile));
			ArrayList<Hashtable<String, String>> dictList = bundleData(reader);

			for (Hashtable<String, String> dict : dictList) {
				programDatabase.add(new Program(dict, true));
			}

		}

	}

	public Interpret getInterpret() {

		for (Program program : programDatabase) {
			program.cleanIcon();
		}

		return new Interpret(programDatabase);
	}

	private void saveData() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(saveFile, "UTF-8");
		for (Program program : programDatabase) {
			program.endAll(new DateTime());
			writer.write(program.toString());
		}
		writer.write("\n");
		writer.close();
	}

	private void periodically() throws IOException {
		updateRunningProcesses();
		lastCheck = new DateTime();
	}

	private String[] readGpsLine(String line) {
		String[] split = line.strip().split("\\s+:\\s+");

		if (split.length < 2) {
			String property = split[0].replaceAll(":", "").strip();
			split = new String[2];
			split[0] = property;
			split[1] = "";
		}

		return split;
	}

}
