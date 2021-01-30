package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import custom_fxml.CustomTab;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import launch.Main;
import model.Interpret.ProgramData;

public class StatsTab extends CustomTab implements Initializable {
	
	public static final String TIME_ELAPSED_FORMAT = "It has been %s since you last opened %s";
	public static final String AVERAGE_SESSION_FORMAT = "Average session duration: %s";
	public static final String TIMES_OPENED_FORMAT = "Times opened: %s";
	public static final String TIME_SPENT_FORMAT = "Time spent: %s";

	@FXML
	private Label label_timeSpent_total, label_averageSession_total, label_timesOpened_total, label_timeSpent_weeks,
			label_averageSession_weeks, label_timesOpened_weeks, label_timeElapsed;
	@FXML
	private ComboBox<ProgramData> comboBox_program;
	@FXML
	private ImageView imageView_programIcon;

	public StatsTab() {
		super("Statistics", Main.STATS_TAB_FXML_PATH);
	}

	public static String durationToString(Duration duration, boolean verbose) {

		int remainingHours = (int) (duration.getStandardHours());
		int minutes = (int) (duration.getStandardMinutes() - remainingHours * 60);
		
		if (verbose) {
			String output = "";

				int years = remainingHours/(365*24);
				remainingHours -= years*365*24;
				output += String.format("%d years", years);
			
				int months = remainingHours/(30*24);
				remainingHours -= months*30*24;
				output += String.format(", %d months", months);
			
				int days = remainingHours/(24);
				remainingHours -= days*24;
				output += String.format(", %d days", days);
			
				output += String.format(", %d hours", remainingHours);
				output += String.format(" and %d minutes", minutes);
			
			return output;
		} else {
			return String.format("%dh, %dmin", remainingHours, minutes);
		}
		
	}

	@FXML
	public void comboBoxAction(ActionEvent event) {
		setup(comboBox_program.getValue());
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		comboBox_program.getItems().addAll(programDatabase);
		comboBox_program.setValue(programDatabase.get(0));
		
		setup(programDatabase.get(0));

		comboBox_program.setCellFactory(new Callback<ListView<ProgramData>, ListCell<ProgramData>>() {
			@Override
			public ListCell<ProgramData> call(ListView<ProgramData> p) {
				return new ListCell<ProgramData>() {
					@Override
					protected void updateItem(ProgramData item, boolean empty) {
						super.updateItem(item, empty);
						getListView().setId("trendsTab_listView");
						if (item == null || empty) {
							setGraphic(null);
						} else {
							setText(item.getPresentableName());
							Image icon = null;
							try {
								icon = SwingFXUtils.toFXImage(item.getIcon(), null);
								ImageView iconImageView = new ImageView(icon);
								iconImageView.setFitHeight(35);
								iconImageView.setPreserveRatio(true);
								setGraphic(iconImageView);
							} catch (NullPointerException e) {
								setGraphic(null);
							}

						}
					}
				};
			}
		});
	}

	public void setup(ProgramData program) {
		imageView_programIcon.setImage(SwingFXUtils.toFXImage(program.getIcon(), null));
		
		String timeElapsed = durationToString(interpret.getTimeElapsedSinceOpen(program), true);
		label_timeElapsed.setText(String.format(TIME_ELAPSED_FORMAT, timeElapsed, program.getPresentableName()));

		String timeSpentTotal = durationToString(interpret.getRuntime(program, null), false);
		label_timeSpent_total.setText(String.format(TIME_SPENT_FORMAT, timeSpentTotal));

		String timeSpent2Weeks = durationToString(interpret.getRuntime(program, new DateTime().minusDays(14)), false);
		label_timeSpent_weeks.setText(String.format(TIME_SPENT_FORMAT, timeSpent2Weeks));

		String averageTotalSession = durationToString(interpret.getAverageSessionDuration(program, null), false);
		label_averageSession_total.setText(String.format(AVERAGE_SESSION_FORMAT, averageTotalSession));

		String average2WeeksSession = durationToString(
				interpret.getAverageSessionDuration(program, new DateTime().minusDays(14)), false);
		label_averageSession_weeks.setText(String.format(AVERAGE_SESSION_FORMAT, average2WeeksSession));

		label_timesOpened_total.setText(String.format(TIMES_OPENED_FORMAT, interpret.getTimesOpened(program, null)));
		label_timesOpened_weeks.setText(
				String.format(TIMES_OPENED_FORMAT, interpret.getTimesOpened(program, new DateTime().minusDays(14))));
	}

}
