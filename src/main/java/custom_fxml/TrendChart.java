package custom_fxml;

import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.util.Pair;

public class TrendChart extends AreaChart {

	public TrendChart(@NamedArg("xAxis") Axis xAxis, @NamedArg("yAxis") Axis yAxis) {
		super(xAxis, yAxis);
	}

	@Override
	protected void layoutPlotChildren() {
		super.layoutPlotChildren();
		for (int seriesIndex = 0; seriesIndex < getDataSize(); seriesIndex++) {
			final XYChart.Series<Number, Number> series = (Series<Number, Number>) getData().get(seriesIndex);
			final Path seriesLine = (Path) ((Group) series.getNode()).getChildren().get(1);
			final Path fillPath = (Path) ((Group) series.getNode()).getChildren().get(0);
			smooth(seriesLine.getElements(), fillPath.getElements());
		}
	}

	private int getDataSize() {
		final ObservableList<XYChart.Series<Number, Number>> data = getData();
		return (data != null) ? data.size() : 0;
	}

	private static void smooth(ObservableList<PathElement> strokeElements, ObservableList<PathElement> fillElements) {
		final Point2D[] dataPoints = new Point2D[strokeElements.size()];
		for (int i = 0; i < strokeElements.size(); i++) {
			final PathElement element = strokeElements.get(i);
			if (element instanceof MoveTo) {
				final MoveTo move = (MoveTo) element;
				dataPoints[i] = new Point2D(move.getX(), move.getY());
			} else if (element instanceof LineTo) {
				final LineTo line = (LineTo) element;
				final double x = line.getX(), y = line.getY();
				dataPoints[i] = new Point2D(x, y);
			}
		}
		final double zeroY = ((MoveTo) fillElements.get(0)).getY();

		strokeElements.clear();
		fillElements.clear();
		Pair<Point2D[], Point2D[]> result = getCurveControlPoints(dataPoints);
		Point2D[] firstControlPoints = result.getKey();
		Point2D[] secondControlPoints = result.getValue();
		strokeElements.add(new MoveTo(dataPoints[0].getX(), dataPoints[0].getY()));
		fillElements.add(new MoveTo(dataPoints[0].getX(), zeroY));
		fillElements.add(new LineTo(dataPoints[0].getX(), dataPoints[0].getY()));
		for (int i = 1; i < dataPoints.length; i++) {
			final int ci = i - 1;

			strokeElements.add(new CubicCurveTo(firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
					secondControlPoints[ci].getX(), secondControlPoints[ci].getY(), dataPoints[i].getX(),
					dataPoints[i].getY()));
			fillElements.add(new CubicCurveTo(firstControlPoints[ci].getX(), firstControlPoints[ci].getY(),
					secondControlPoints[ci].getX(), secondControlPoints[ci].getY(), dataPoints[i].getX(),
					dataPoints[i].getY()));

		}
		fillElements.add(new LineTo(dataPoints[dataPoints.length - 1].getX(), zeroY));
		fillElements.add(new ClosePath());
	}

	public static Pair<Point2D[], Point2D[]> getCurveControlPoints(Point2D[] knots) {

		Point2D[] firstControlPoints, secondControlPoints;

		if (knots == null)
			return null;
		int n = knots.length - 1;
		if (n < 1)
			return null;
		if (n == 1) {
			firstControlPoints = new Point2D[1];

			double x1 = (2 * knots[0].getX() + knots[1].getX()) / 3;
			double y1 = (2 * knots[0].getY() + knots[1].getY()) / 3;
			firstControlPoints[0] = new Point2D(x1, y1);

			secondControlPoints = new Point2D[1];

			double x2 = 2 * firstControlPoints[0].getX() - knots[0].getX();
			double y2 = 2 * firstControlPoints[0].getY() - knots[0].getY();

			secondControlPoints[0] = new Point2D(x2, y2);
			return new Pair<Point2D[], Point2D[]>(firstControlPoints, secondControlPoints);
		}

		double[] rhs = new double[n];

		for (int i = 1; i < n - 1; ++i)
			rhs[i] = 4 * knots[i].getX() + 2 * knots[i + 1].getX();
		rhs[0] = knots[0].getX() + 2 * knots[1].getX();
		rhs[n - 1] = (8 * knots[n - 1].getX() + knots[n].getX()) / 2.0;

		double[] x = getFirstControlPoints(rhs);

		for (int i = 1; i < n - 1; ++i)
			rhs[i] = 4 * knots[i].getY() + 2 * knots[i + 1].getY();
		rhs[0] = knots[0].getY() + 2 * knots[1].getY();
		rhs[n - 1] = (8 * knots[n - 1].getY() + knots[n].getY()) / 2.0;
		double[] y = getFirstControlPoints(rhs);

		firstControlPoints = new Point2D[n];
		secondControlPoints = new Point2D[n];
		for (int i = 0; i < n; ++i) {
			firstControlPoints[i] = new Point2D(x[i], y[i]);
			if (i < n - 1)
				secondControlPoints[i] = new Point2D(2 * knots[i + 1].getX() - x[i + 1],
						2 * knots[i + 1].getY() - y[i + 1]);
			else
				secondControlPoints[i] = new Point2D((knots[n].getX() + x[n - 1]) / 2,
						(knots[n].getY() + y[n - 1]) / 2);
		}
		return new Pair<Point2D[], Point2D[]>(firstControlPoints, secondControlPoints);
	}

	private static double[] getFirstControlPoints(double[] rhs) {
		int n = rhs.length;
		double[] x = new double[n];
		double[] tmp = new double[n];

		double b = 2.0;
		x[0] = rhs[0] / b;
		for (int i = 1; i < n; i++) {
			tmp[i] = 1 / b;
			b = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
			x[i] = (rhs[i] - x[i - 1]) / b;
		}
		for (int i = 1; i < n; i++)
			x[n - i - 1] -= tmp[n - i] * x[n - i];

		return x;
	}

}
