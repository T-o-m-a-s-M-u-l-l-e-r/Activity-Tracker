package controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import controllers.HistoryTab.SpecialComboBox.ProgramDataWrapper;
import custom_fxml.CustomTab;
import custom_fxml.GanttChart;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.StringConverter;
import launch.Main;
import model.Interpret.ProgramData;

public class HistoryTab extends CustomTab implements Initializable {
	public static final int MAX_NAME_LENGTH = 15;
	private static HashSet<ProgramData> selection;

	@FXML
	private DatePicker datePicker;
	@FXML
	private GanttChart<Number, ?> ganttChart;
	@FXML
	private SpecialComboBox specialComboBox;

	private CategoryAxis yAxis;
	private NumberAxis xAxis;

	public HistoryTab() {
		super("History", Main.HISTORY_TAB_FXML_PATH);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		specialComboBox.setHistoryTab(this);
		specialComboBox.setId("historyTab_comboBox");
		xAxis = (NumberAxis) ganttChart.getXAxis();
		yAxis = (CategoryAxis) ganttChart.getYAxis();
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(24);
		xAxis.setAutoRanging(false);
		xAxis.setLabel("");
		xAxis.setTickLabelFill(Color.CHOCOLATE);
		xAxis.setTickUnit(1);
		xAxis.setMinorTickCount(1);

		yAxis.setLabel("");
		yAxis.setTickLabelFill(Color.CHOCOLATE);
		yAxis.setTickLabelGap(10);

		ganttChart.setLegendVisible(false);
		ganttChart.setBlockHeight(25);
		ganttChart.setAnimated(false);

		datePicker.setValue(java.time.LocalDate.now());
		setChartTitle();

		selection = new HashSet<ProgramData>();
		specialComboBox.setItems(programDatabase);
		setDefaultSelection();

		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number object) {
				int value = (int) Math.round((Double) object);

				if (value > 9) {
					return String.format("%d:00", value);
				} else {
					return String.format("0%d:00", value);
				}

			}

			@Override
			public Number fromString(String string) {
				Pattern pattern = Pattern.compile("\\d+");
				Matcher matcher = pattern.matcher(string);
				return matcher.find() ? Integer.valueOf(matcher.group()) : 0;
			}
		});
	}

	public void setDefaultSelection() {
		HashSet<ProgramData> defaultSelection = new HashSet<ProgramData>();

		for (int i = 0; i < Math.min(SpecialComboBox.SELECTION_LIMIT, specialComboBox.getItems().size()); i++) {
			defaultSelection.add(programDatabase.get(i));
		}

		ObservableList<ProgramDataWrapper> items = specialComboBox.getItems();
		selection.addAll(defaultSelection);

		for (ProgramData program : selection) {
			int index = items.indexOf(program);

			if (index != -1) {
				items.get(index).checkProperty().set(true);
				addSeries(program);
			}

		}

	}

	public void setChartTitle() {
		LocalDate localDate = datePicker.getValue();
		DateTime jodaTime = toJodaTime(localDate);
		String title = jodaTime.toString(DateTimeFormat.fullDate());
		ganttChart.setTitle(title);
	}

	public void addSeries(ProgramData program) {
		DateTime time = toJodaTime(datePicker.getValue());
		String name = getValidName(program, MAX_NAME_LENGTH);

		for (String category : yAxis.getCategories()) {

			if (category.equals(name)) {
				return;
			}

		}

		XYChart.Series newSeries = getSeries(program, interpret.getProgramHistory(program, time));
		ganttChart.getData().add(newSeries);
	}

	public void removeSeries(ProgramData program) {
		String name = getValidName(program, MAX_NAME_LENGTH);

		for (Iterator<?> iterator = ganttChart.getData().iterator(); iterator.hasNext();) {
			Series<Number, ?> series = (Series<Number, ?>) iterator.next();
			if (series.getName() != null && series.getName().equals(name)) {
				iterator.remove();
				break;
			}
		}

	}

	public static String getValidName(ProgramData program, int maxLength) {
		String name = program.getPresentableName();

		if (name.length() > MAX_NAME_LENGTH) {
			name = program.getName();
		}

		if (name.length() > MAX_NAME_LENGTH) {
			name = name.substring(0, maxLength - 4) + "...";
		}

		return name;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static XYChart.Series getSeries(ProgramData program, ArrayList<Pair<Double, Double>> points) {
		String programName = getValidName(program, MAX_NAME_LENGTH);

		XYChart.Series series = new XYChart.Series();
		series.getData().add(new XYChart.Data(0, programName, new GanttChart.ExtraData(24, "status-off")));

		for (Pair<Double, Double> pair : points) {
			XYChart.Data data = new XYChart.Data(pair.getKey(), programName,
					new GanttChart.ExtraData(pair.getValue(), "status-on"));
			series.getData().add(data);
		}

		series.setName(programName);
		return series;
	}

	// Event Listener on DatePicker[#datePicker].onAction
	@FXML
	public void onAction(ActionEvent event) {
		ganttChart.getData().clear();
		yAxis.getCategories().clear();

		for (ProgramData program : selection) {
			addSeries(program);
		}

		setChartTitle();
	}

	public static org.joda.time.DateTime toJodaTime(java.time.LocalDate javaTime) {
		org.joda.time.DateTime jodaTime = DateTimeFormat.forPattern("dd-M-y").parseDateTime(String.format("%d-%d-%d",
				javaTime.getDayOfMonth(), javaTime.getMonth().getValue(), javaTime.getYear()));
		return jodaTime;
	}

	public static class SpecialComboBox extends ComboBox<ProgramDataWrapper> {
		private HistoryTab historyTab;
		private static final int SELECTION_LIMIT = 4;

		public SpecialComboBox() {
			setEditable(false);

			setCellFactory(c -> {
				ListCell<ProgramDataWrapper> cell = new ListCell<ProgramDataWrapper>() {
					@Override
					protected void updateItem(ProgramDataWrapper item, boolean empty) {
						super.updateItem(item, empty);

						if (empty || item == null) {
							setText(null);
							setGraphic(null);
						} else {
							CheckBox checkBox = new CheckBox();
							checkBox.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(ActionEvent event) {
									item.checkProperty().set(!item.getCheck());
								}
							});
							setGraphic(checkBox);
							checkBox.selectedProperty().bindBidirectional(item.checkProperty());
							setText(item.getProgramData().getPresentableName());
						}

					}
				};

				cell.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {

					ProgramDataWrapper item = cell.getItem();
					if (item != null) {

						boolean trySelect = !item.checkProperty().get();

						if (trySelect) {

							if (selection.size() < SELECTION_LIMIT) {
								selection.add(item.getProgramData());
								historyTab.addSeries(item.getProgramData());
								item.checkProperty().set(trySelect);
							}

						} else {
							item.checkProperty().set(trySelect);
							historyTab.removeSeries(item.getProgramData());
							selection.remove(item.getProgramData());
						}

					}

					getSelectionModel().clearSelection();

				});

				return cell;
			});

			setSkin(new Skinner(this));
		}

		public void setHistoryTab(HistoryTab historyTab) {
			this.historyTab = historyTab;
		}

		public void setItems(ArrayList<ProgramData> list) {
			ObservableList<ProgramDataWrapper> items = FXCollections.observableArrayList();

			for (ProgramData program : list) {
				items.add(new ProgramDataWrapper(program));
			}

			setItems(items);
		}

		public HashSet<ProgramData> getSelection() {
			return selection;
		}

		public class Skinner extends ComboBoxListViewSkin<ProgramDataWrapper> {

			public Skinner(SpecialComboBox control) {
				super((ComboBox) control);
				setHideOnClick(false);
			}

		}

		public class ProgramDataWrapper {
			private BooleanProperty check = new SimpleBooleanProperty(false);
			private ProgramData program;

			public ProgramDataWrapper(ProgramData program) {
				this.program = program;
			}

			public BooleanProperty checkProperty() {
				return check;
			}

			public Boolean getCheck() {
				return check.getValue();
			}

			public ProgramData getProgramData() {
				return program;
			}

			@Override
			public String toString() {
				return null;
			}

		}

	}
}
