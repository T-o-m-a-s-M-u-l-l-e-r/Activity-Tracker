package controllers.history_tab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.format.DateTimeFormat;

import controllers.TrendsTab;
import custom_fxml.TrendChart;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import javafx.util.StringConverter;
import launch.Main;
import model.Interpret;
import model.Interpret.ProgramData;

public class AverageWeekChart extends AnchorPane {
	public static final String Y_AXIS_LABEL = "Average number of hours spent";
	public static final List<String> DAYS_OF_WEEK = Arrays.asList(new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"});
	private Interpret interpret;
	private ProgramData program;
	@FXML
	private TrendChart chart;

	public AverageWeekChart() {
		FXMLLoader fxmlLoader;
		try {
			fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(Main.AVERAGE_WEEK_CHART_FXML_PATH));
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

		NumberAxis xAxis = (NumberAxis) chart.getXAxis();
		xAxis.setTickUnit(1);
		xAxis.setLowerBound(1);
		xAxis.setUpperBound(7);
		xAxis.setAutoRanging(false);
		
		chart.setLegendVisible(false);
		chart.setTitle("Average week usage");
		
		xAxis.setTickLabelFormatter(new StringConverter<Number>() {
	        @Override
	        public String toString(Number object) {
	        	int value = (int)Math.round((Double)object);
	            return DAYS_OF_WEEK.get(value-1);
	        }

	        @Override
	        public Number fromString(String string) {
	            return DAYS_OF_WEEK.indexOf(string);
	        }
	    });

		chart.getYAxis().setLabel(Y_AXIS_LABEL);
		
		initData();
	}
	
	public static org.joda.time.DateTime toJodaTime(java.time.LocalDate javaTime) {
		org.joda.time.DateTime jodaTime = DateTimeFormat.forPattern("dd-M-y").parseDateTime(String.format("%d-%d-%d",
				javaTime.getDayOfMonth(), javaTime.getMonth().getValue(), javaTime.getYear()));
		return jodaTime;
	}
	
	@FXML
	public void onAction(ActionEvent event) {
		initData();
	}
	
	public void initData() {
		if (program != null) {
		chart.getData().clear();
		
		ArrayList<Pair<String, Double>> days = interpret.getAverageWeekTrend(program);
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		
		int i = 1;
		for (Pair<String, Double> pair : days) {
			points.add(new Point2D(i, pair.getValue()));
			i++;
		}
		
		ArrayList<Data<Number, Number>> dataList = new ArrayList<Data<Number, Number>>();
		Series<Number, Number> series = new Series<Number, Number>();
		dataList.addAll(TrendsTab.pointsToData(points));
		series.getData().addAll(dataList);
		chart.getData().add(series);
		}
		
	}

}
