package controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ResourceBundle;

import controllers.history_tab.AverageDayChart;
import controllers.history_tab.AverageWeekChart;
import custom_fxml.CustomTab;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import launch.Main;
import model.Interpret.ProgramData;

public class TrendsTab extends CustomTab implements Initializable {

	private enum ChartType {
		AverageDayTrend, AverageWeekTrend;

		public Node getChart(ProgramData program) {
			switch (this) {
			case AverageWeekTrend:
				AverageWeekChart weekChart = new AverageWeekChart();
				weekChart.init(interpret, program);
				return weekChart;
			case AverageDayTrend:
				AverageDayChart dayChart = new AverageDayChart();
				dayChart.init(interpret, program);
				return dayChart;
			default:
				return null;
			}
		}

	}

	@FXML
	private ComboBox<ProgramData> comboBox_program;
	@FXML
	private ImageView imageView_programIcon;
	@FXML
	private Pagination pagination_charts;
	private Hashtable<String, Callback<Integer, Node>> callbackHashtable;
	private Hashtable<String, Hashtable<ChartType, Node>> chartHashtable;

	public TrendsTab() {
		super("Trends", Main.TRENDS_TAB_FXML_PATH);
	}

	@FXML
	public void comboBoxAction(ActionEvent event) {
		setup(comboBox_program.getValue());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		callbackHashtable = new Hashtable<String, Callback<Integer, Node>>();
		chartHashtable = new Hashtable<String, Hashtable<ChartType, Node>>();

		comboBox_program.getItems().addAll(programDatabase);
		if (programDatabase.size() > 0) {
			comboBox_program.setValue(programDatabase.get(0));
			setup(programDatabase.get(0));
		} else {
			setup(null);
		}

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
		pagination_charts.setPageCount(ChartType.values().length);
	}

	public static ArrayList<Data<Number, Number>> pointsToData(ArrayList<Point2D> pointList) {
		ArrayList<Data<Number, Number>> dataList = new ArrayList<Data<Number, Number>>();

		for (Point2D point : pointList) {
			dataList.add(new Data<Number, Number>(point.getX(), point.getY()));
		}

		return dataList;
	}

	private void setup(ProgramData program) {
		pagination_charts.setPageFactory(getProgramCallback(program));
		if (program != null) {
			imageView_programIcon.setImage(SwingFXUtils.toFXImage(program.getIcon(), null));
		}
	}

	private Callback<Integer, Node> getProgramCallback(ProgramData program) {
		String programName = program == null ? "N/A" : program.getName();

		if (callbackHashtable.containsKey(programName)) {
			return callbackHashtable.get(programName);
		} else {
			Callback<Integer, Node> programCallback = new Callback<Integer, Node>() {

				@Override
				public Node call(Integer pageIndex) {
					ChartType type = ChartType.values()[pageIndex];

					Hashtable<ChartType, Node> charts = chartHashtable.get(programName);
					Node chart;

					if (charts == null) {
						charts = new Hashtable<TrendsTab.ChartType, Node>();
						chartHashtable.put(programName, charts);
					}

					chart = charts.get(type);

					if (chart == null) {
						chart = type.getChart(program);
						charts.put(type, chart);
					}

					return chart;
				}
			};

			callbackHashtable.put(programName, programCallback);
			return programCallback;
		}
	}

}
