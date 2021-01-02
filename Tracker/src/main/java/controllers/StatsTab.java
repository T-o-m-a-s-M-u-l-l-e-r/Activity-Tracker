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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import launch.Main;
import model.Interpret.ProgramData;

public class StatsTab extends CustomTab implements Initializable {

	private enum Stat {
		Time_spent, Average_session_duration, Times_opened;
		//Enum names are used as text in the label
		//Order of constants defines order of labels
		//Use underscore instead of space
		
		private String getData(ProgramData program, boolean total) {
			DateTime date = total?null:new DateTime().minusDays(14);
			
			switch (this) {
			case Times_opened:
				return String.valueOf(interpret.getTimesOpened(program, date));
			case Time_spent:
				return durationToString(interpret.getRuntime(program, date));
			case Average_session_duration:
				return durationToString(interpret.getAverageSessionDuration(program, date));
			default: return null;
			}
			
		}
		
		public Pair<String, String> getPair(ProgramData program) {
			String toString = toString().replaceAll("_", " ");
			return new Pair<String, String>(String.format("%s: %s", toString, getData(program, true)), String.format("%s: %s", toString, getData(program, false)));
		}
		
	}

	@FXML
	private ComboBox comboBox_program;
	@FXML
	private ImageView imageView_programIcon;
	@FXML
	private ListView<String> listView_total, listView_recent;

	public StatsTab() {
		super("Statistics", Main.STATS_TAB_FXML_PATH);
	}

	public static String durationToString(Duration duration) {
		int hours = (int) (duration.getStandardHours());
		int minutes = (int) (duration.getStandardMinutes() - hours * 60);
		String output = String.format("%dh, %dmin", hours, minutes);
		return output;
	}

	@FXML
	public void comboBoxAction(ActionEvent event) {
		String programName = ((Pair<String, String>) comboBox_program.getValue()).getKey();
		setup(programName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		comboBox_program.getItems().addAll(programPairNameList);

		comboBox_program.setValue(programPairNameList.get(0));
		setup(programPairNameList.get(0).getKey());

		comboBox_program.setConverter(new StringConverter<Pair<String, String>>() {
			@Override
			public String toString(Pair<String, String> obj) {
				if (obj == null) {
					return "";
				} else {
					return obj.getValue();
				}
			}

			@Override
			public Pair<String, String> fromString(String s) {
				return null;
			}
		});

		comboBox_program.setCellFactory(new Callback<ListView<Pair<String, String>>, ListCell<Pair<String, String>>>() {
			@Override
			public ListCell<Pair<String, String>> call(ListView<Pair<String, String>> p) {
				return new ListCell<Pair<String, String>>() {
					@Override
					protected void updateItem(Pair<String, String> item, boolean empty) {
						super.updateItem(item, empty);
						getListView().setId("statsTab_listView");
						if (item == null || empty) {
							setGraphic(null);
						} else {
							setText(item.getValue());
							ProgramData program = interpret.getProgram(item.getKey());
							Image icon = null;
							try {
								icon = SwingFXUtils.toFXImage(program.getIcon(), null);
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
		
		listView_total.setCellFactory(param -> new ListCell<String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item==null) {
                    setGraphic(null);
                    setText(null); 
                }else{

                    setMinWidth(param.getWidth());
                    setMaxWidth(param.getWidth());
                    setPrefWidth(param.getWidth());

                    setWrapText(true);

                    setText(item.toString());
                }
            }
        });
	}

	public void setup(String programName) {
		ProgramData program = interpret.getProgram(programName);

		imageView_programIcon.setImage(SwingFXUtils.toFXImage(program.getIcon(), null));
		listView_recent.getItems().clear();
		listView_total.getItems().clear();
		
		for (Stat stat : Stat.values()) {
			
			Pair<String, String> pair = stat.getPair(program);
			listView_total.getItems().add(pair.getKey());
			listView_recent.getItems().add(pair.getValue());
			
		}

//		String timeSpentTotal = durationToString(interpret.getRuntime(program, null));
//		label_total_timeSpent.setText(String.format(TIME_SPENT_FORMAT, timeSpentTotal));
//
//		String timeSpent2Weeks = durationToString(interpret.getRuntime(program, new DateTime().minusDays(14)));
//		label_2weeks_timeSpent.setText(String.format(TIME_SPENT_FORMAT, timeSpent2Weeks));
//
//		String averageTotalSession = durationToString(interpret.getAverageSessionDuration(program, null));
//		label_total_averageSession.setText(String.format(AVERAEG_SESSION_FORMAT, averageTotalSession));
//
//		String average2WeeksSession = durationToString(
//				interpret.getAverageSessionDuration(program, new DateTime().minusDays(14)));
//		label_2weeks_averageSession.setText(String.format(AVERAEG_SESSION_FORMAT, average2WeeksSession));
//
//		label_total_timesOpened.setText(String.format(TIMES_OPENED_FORMAT, interpret.getTimesOpened(program, null)));
//		label_2weeks_timesOpened.setText(
//				String.format(TIMES_OPENED_FORMAT, interpret.getTimesOpened(program, new DateTime().minusDays(14))));
	}

}
