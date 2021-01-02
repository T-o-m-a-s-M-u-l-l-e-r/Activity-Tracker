package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import custom_fxml.CustomTab;
import custom_fxml.TrendChart;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import launch.Main;
import model.Interpret.ProgramData;

public class TrendsTab extends CustomTab implements Initializable {

	private enum ChartType {
		AverageDayTrend, AverageWeekTrend;

		public Node getChart(String programName) {
			switch (this) {
			case AverageWeekTrend:
				return getAverageWeekChart(programName);
			case AverageDayTrend:
				return getAverageDayChart(programName);
			default:
				return null;
			}
		}

	}

	@FXML
	private ComboBox comboBox_program;
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
		String programName = ((Pair<String, String>)comboBox_program.getValue()).getKey();
		setup(programName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		callbackHashtable = new Hashtable<String, Callback<Integer, Node>>();
		chartHashtable = new Hashtable<String, Hashtable<ChartType, Node>>();
		
		comboBox_program.getItems().addAll(programPairNameList);

		comboBox_program.setValue(programPairNameList.get(0));
		setup(programPairNameList.get(0).getKey());
		
		comboBox_program.setConverter(new StringConverter<Pair<String, String>>() {
			  @Override
			  public String toString(Pair<String, String> obj) {
			    if (obj == null)
			    {
			      return "";
			    }
			    else 
			    {
			      return obj.getValue();
			    }
			  }

			  @Override
			  public Pair<String, String> fromString(String s)
			  {
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
						getListView().setId("trendsTab_listView");
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
		pagination_charts.setPageCount(ChartType.values().length);
	}

	private static ArrayList<Data<Number, Number>> pointsToData(ArrayList<Point2D> pointList) {
		ArrayList<Data<Number, Number>> dataList = new ArrayList<Data<Number, Number>>();

		for (Point2D point : pointList) {
			dataList.add(new Data<Number, Number>(point.getX(), point.getY()));
		}

		return dataList;
	}

	private void setup(String programName) {
		ProgramData program = null;
		for (Pair<String, String> pair : interpret.getNameList()) {
			if (pair.getKey().equals(programName)) {
				program = interpret.getProgram(pair.getKey());
				break;
			}
		}

		pagination_charts.setPageFactory(getProgramCallback(programName));

		imageView_programIcon.setImage(SwingFXUtils.toFXImage(program.getIcon(), null));
	}

	private Callback<Integer, Node> getProgramCallback(String programName) {
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
						chart = type.getChart(programName);
						charts.put(type, chart);
					}

					return chart;
				}
			};

			callbackHashtable.put(programName, programCallback);
			return programCallback;
		}
	}

	private static Node getAverageDayChart(String programName) {
		int day = 1;

		TrendChart chart = new TrendChart();
		NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		xAxis.setTickUnit(1);
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(24);
		xAxis.setAutoRanging(false);
		ArrayList<Data<Number, Number>> dataList = new ArrayList<Data<Number, Number>>();
		Series<Number, Number> series = new Series<Number, Number>();
		ArrayList<Point2D> points = interpret.getAverageDayTrend(interpret.getProgram(programName), day);
		dataList.addAll(pointsToData(points));
		series.getData().addAll(dataList);
		chart.getData().add(series);
		chart.setLegendVisible(false);

		chart.setTitle("Average day usage");
		System.out.println("AVERAGE_DAY_CHART");
		return chart;
	}

	static class TestNode extends AnchorPane implements Initializable {
		@FXML
		private ChoiceBox<String> choiceBox;
		@FXML
		private TrendChart chart;
		private String programName;

		public TestNode(String programName) {
			this.programName = programName;
			String path = "fxml/test.fxml";
			FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getClassLoader().getResource(path));
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);

			try {
				fxmlLoader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void initialize(URL location, ResourceBundle resources) {
			choiceBox.setItems(FXCollections.observableArrayList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
			choiceBox.setValue("None");
			change(String.valueOf(choiceBox.getValue()));
			choiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					change(String.valueOf(choiceBox.getValue()));
				}

			});

			
		}
		
		public void change(String dayOfTheWeek) {
			int day = 0;
			for (String dayString : choiceBox.getItems()) {
				day++;
				if (dayString.equals(dayOfTheWeek)) {
					break;
				}
				
			}
			chart.getData().clear();
			NumberAxis xAxis = (NumberAxis) chart.getXAxis();
			xAxis.setTickUnit(1);
			xAxis.setLowerBound(0);
			xAxis.setUpperBound(24);
			xAxis.setAutoRanging(false);
			ArrayList<Data<Number, Number>> dataList = new ArrayList<Data<Number, Number>>();
			Series<Number, Number> series = new Series<Number, Number>();
			ArrayList<Point2D> points = interpret.getAverageDayTrend(interpret.getProgram(programName), day);
			dataList.addAll(pointsToData(points));
			series.getData().addAll(dataList);
			chart.getData().add(series);
			chart.setLegendVisible(false);
			
			
			
		}

	}

	private static Node getAverageWeekChart(String programName) {
		TrendChart chart = new TrendChart();
		NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		ArrayList<Data<Number, Number>> dataList = new ArrayList<Data<Number, Number>>();
		Series<Number, Number> series = new Series<Number, Number>();
		ArrayList<Point2D> points = interpret.getAverageDayTrend(interpret.getProgram(programName), 1);
		dataList.addAll(pointsToData(points));
		series.getData().addAll(dataList);
		chart.getData().add(series);
		chart.setLegendVisible(false);

		chart.setTitle("Average week usage");

		return new TestNode(programName);
	}

}
