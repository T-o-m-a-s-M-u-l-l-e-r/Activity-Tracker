package controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import custom_fxml.CustomTab;
import custom_fxml.GanttChart;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import launch.Main;
import model.Interpret.ProgramData;

public class HistoryTab extends CustomTab implements Initializable {
	@FXML
	private DatePicker datePicker;
	@FXML
	private GanttChart<Number, ?> ganttChart;

	private CategoryAxis yAxis;
	private NumberAxis xAxis;

	public HistoryTab() {
		super("History", Main.HISTORY_TAB_FXML_PATH);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
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

		ganttChart.setTitle("Machine Monitoring");
		ganttChart.setLegendVisible(false);
		ganttChart.setBlockHeight(25);
		ganttChart.setAnimated(false);
		
		datePicker.setValue(java.time.LocalDate.now());
		
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add("chrome");
		nameList.add("notepad");
		
		initChartData(nameList);
	}

	public void initChartData(ArrayList<String> programNameList) {
		initChartData(programNameList, toJodaTime(datePicker.getValue()));
	}

	public void initChartData(ArrayList<String> programNameList, DateTime day) {
		ganttChart.getData().clear();
		
		ArrayList<XYChart.Series> seriesList = new ArrayList<XYChart.Series>();
		
		for (String programName : programNameList) {
			try {
			yAxis.getCategories().add(programName);
			} catch (UnsupportedOperationException e) {
				
			}
			ProgramData program = interpret.getProgram(programName);
			seriesList.add(getSeries(programName, interpret.getProgramHistory(program, day)));
		}

		for (XYChart.Series series : seriesList) {
			ganttChart.getData().add(series);
		}

//		yAxis.requestAxisLayout();
//		yAxis.autosize();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public XYChart.Series getSeries(String programName, ArrayList<Pair<Double, Double>> points) {
		XYChart.Series series = new XYChart.Series();
		series.getData().add(new XYChart.Data(0, programName, new GanttChart.ExtraData(24, "status-off")));

		for (Pair<Double, Double> pair : points) {
			XYChart.Data data = new XYChart.Data(pair.getKey(), programName,
					new GanttChart.ExtraData(pair.getValue(), "status-on"));
			series.getData().add(data);
		}

		return series;
	}

	// Event Listener on DatePicker[#datePicker].onAction
	@FXML
	public void onAction(ActionEvent event) {
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add("ApplicationFrameHost");
		nameList.add("farcry3_d3d11");
		nameList.add("chrome");
		
		initChartData(nameList);
	}

	public org.joda.time.DateTime toJodaTime(java.time.LocalDate javaTime) {
		org.joda.time.DateTime jodaTime = DateTimeFormat.forPattern("dd-M-y").parseDateTime(String.format("%d-%d-%d",
				javaTime.getDayOfMonth(), javaTime.getMonth().getValue(), javaTime.getYear()));
		return jodaTime;
	}
}
