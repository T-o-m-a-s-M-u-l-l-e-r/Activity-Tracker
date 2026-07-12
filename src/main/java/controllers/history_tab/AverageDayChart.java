package controllers.history_tab;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controllers.TrendsTab;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import launch.Main;
import model.Interpret;
import model.Interpret.ProgramData;

public class AverageDayChart extends AnchorPane {
	public static final String Y_AXIS_LABEL = "Days with program running";
	public static final String CHART_TITLE_FORMAT = "Average %s usage";
	private Interpret interpret;
	private ProgramData program;
	@FXML
	private AreaChart<Number, Number> chart;
	@FXML
	private ComboBox<String> comboBox;

	public AverageDayChart() {
		FXMLLoader fxmlLoader;
		try {
			fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(Main.AVERAGE_DAY_CHART_FXML_PATH));
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(Interpret interpret, ProgramData program) {
		this.interpret = interpret;
		this.program = program;

		comboBox.setItems(FXCollections.observableArrayList(AverageWeekChart.DAYS_OF_WEEK));
		comboBox.setValue(comboBox.getItems().get(0));
		
		chart.setAnimated(false);
		
		NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		xAxis.setTickUnit(1);
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(24);
		xAxis.setAutoRanging(false);

		NumberAxis yAxis = (NumberAxis) chart.getYAxis();
		yAxis.setUpperBound(100);
		yAxis.setLowerBound(0);
		yAxis.setLabel(Y_AXIS_LABEL);

		yAxis.setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number object) {
				return (int) ((double) (object)) + "%";
			}

			@Override
			public Number fromString(String string) {
				return Integer.valueOf(string.substring(0, string.length() - 1));
			}
		});

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

		chart.setLegendVisible(false);

		initData(true);
	}

	public void initData(boolean firstUse) {
		
		if (program != null) {
			
			int day = getDayIndex(comboBox.getValue());
			ArrayList<Point2D> interpretOutput = interpret.getAverageDayTrend(program, day);
			
			
			
			if (firstUse) {
				
				Series<Number, Number> series = new Series<Number, Number>();
				series.getData().addAll(TrendsTab.pointsToData(interpretOutput));
				chart.setData(FXCollections.observableArrayList(series));
				
			} else {
				
				int i = 0;
				Series<Number, Number> series = (Series)chart.getData().get(0);
				for(XYChart.Data<Number,Number> data : series.getData()) {
					data.setYValue(interpretOutput.get(i).getY());
		            i++;
		        }
				
			}
			
			
			chart.setTitle(String.format(CHART_TITLE_FORMAT, comboBox.getValue()));
		}
	}

	@FXML
	public void onAction(ActionEvent event) {
		initData(false);
	}

	public static int getDayIndex(String day) {
		return AverageWeekChart.DAYS_OF_WEEK.indexOf(day) + 1;
	}

}
