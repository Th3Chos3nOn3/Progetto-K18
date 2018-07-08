package eu.newton.ui.planes;

import eu.newton.api.IDifferentiable;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.functionmanager.IObserver;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.math.BigDecimal;

/**
 * Cartesian plane where to draw math functions.
 */
public class CartesianPlane extends Pane implements IObserver {

    private static final int STD_POINT_DENSITY = 200;
    private static final double STD_TICK_DENSITY = 10;
    private static int CHECK_THRESHOLD = 10;
    private static double MAX_ANGLE = Math.atan(Double.MIN_VALUE);

    private final IFunctionManager<BigDecimal> functionManager;
    private Group sheet;

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    private double axisTickDensity;
    private int pointDensity;

    public CartesianPlane(IFunctionManager<BigDecimal> functionManager, double xLow, double xHi, double yLow, double yHi) {
        this(functionManager, xLow, xHi, yLow, yHi, STD_TICK_DENSITY, STD_POINT_DENSITY);
    }

    /**
     * @param functionManager    math functions manager
     * @param xLow  x axis lower bound
     * @param xHi   x axis upper bound
     * @param yLow  y axis lower bound
     * @param yHi   y axis upper bound
     * @param axisTickDensity   number of axis ticks
     * @param pointsDensity     number of drawn points for each canvas
     */
    public CartesianPlane(IFunctionManager<BigDecimal> functionManager, double xLow, double xHi, double yLow, double yHi, double axisTickDensity, int pointsDensity) {
        if ((xLow > xHi) || (yLow > yHi)) {
            throw new AssertionError("Low(s) must be less than Hi(s)");
        }

        this.functionManager = functionManager;
        functionManager.addObserver(this);

        sheet = new Group();

        this.axisTickDensity = axisTickDensity;
        this.pointDensity = pointsDensity;

        xAxis = new NumberAxis(xLow, xHi, (xHi - xLow) / this.axisTickDensity);
        xAxis.setSide(Side.BOTTOM);

        yAxis = new NumberAxis(yLow, yHi, (yHi - yLow) / this.axisTickDensity);
        yAxis.setSide(Side.RIGHT);

        // Axis binding to pane
        xAxis.layoutYProperty().bind(heightProperty().divide(2));
        xAxis.prefWidthProperty().bind(widthProperty());
        yAxis.layoutXProperty().bind(widthProperty().divide(2));
        yAxis.prefHeightProperty().bind(heightProperty());

        getChildren().addAll(xAxis, yAxis, sheet);

        // TODO: implement zoom
        // TODO: implement drag
    }

    @Override
    public void resize(double v, double v1) {
        super.resize(v, v1);
        sheetRefresh();
    }

    /**
     * Plot a dot on the plane
     * @param x x-plane coordinate
     * @param y y-plane coordinate
     */
    public void plot(double x, double y) {
        Circle circle = new Circle(mapToPaneX(x), mapToPaneY(y), 4, Color.GREEN);

        sheet.getChildren().add(circle);
    }

    /**
     * Plot all controller managed functions
     */
    public void plot() {
        double step = (Math.abs(xAxis.getUpperBound()) + Math.abs(xAxis.getLowerBound())) / pointDensity;

        for (IDifferentiable<BigDecimal> f : functionManager.getFunctions()) {

            if (f != null) {

                double previousX = xAxis.getLowerBound();
                double previousY = f.evaluate(BigDecimal.valueOf(previousX)).doubleValue();

                for (double x = previousX + step; x <= xAxis.getUpperBound(); x += step) {
                    double y1 = f.evaluate(BigDecimal.valueOf(x)).doubleValue();
                    if (!isVertical(previousX, previousY, x, y1)) {
                        plotSegment(previousX, previousY, x, y1, Color.RED);
                    } else {

                        for (int i = 0; i < CHECK_THRESHOLD; i++) {
                            double[] midPoints = splitSegment(previousX, x, i + 2);

                            if (!isVertical(f, midPoints)) {
                                // Probably not a discontinuity
                                plot(f, midPoints, Color.RED);
                                break;
                            }

                            // May be a discontinuity! Don't plot the segment
                        }
                    }

                    previousX = x;
                    previousY = y1;
                }

            }

        }

    }

    private double[] splitSegment(double x0, double x1, int splits) {
        if (x0 >= x1) {
            throw new AssertionError("x0 should be less than x1");
        }

        double[] points = new double[splits];
        double step = (x1 - x0) / splits;

        for (int i = 0; i < splits; i++) {
            points[i] = x0 + step * (i + 1);
        }

        return points;
    }

    /**
     * Plot a math function f given an array of points
     * @param f math function to be plotted
     * @param points    points to be evaluated by f
     * @param color function's color
     */
    private void plot(IDifferentiable<BigDecimal> f, double[] points, Color color) {
        if (points.length < 2) {
            throw new AssertionError("At least 2 points");
        }

        for (int i = 0; i < points.length - 1; i++) {
            plotSegment(
                    points[i],
                    f.evaluate(BigDecimal.valueOf(points[i])).doubleValue(),
                    points[i + 1], f.evaluate(BigDecimal.valueOf(points[i + 1])).doubleValue(),
                    color);
        }

    }

    /**
     * Plot the segment AB
     * @param x0    A(x0, y0)
     * @param y0    A(x0, y0)
     * @param x1    B(x1, y1)
     * @param y1    B(x1, y1)
     * @param color segment's color
     */
    private void plotSegment(double x0, double y0, double x1, double y1, Color color) {
        Line line = new Line(mapToPaneX(x0), mapToPaneY(y0), mapToPaneX(x1), mapToPaneY(y1));
        line.setStrokeWidth(2);
        line.setStroke(color);
        sheet.getChildren().add(line);
    }

    /**
     * Determine if a given segment AB may be considered vertical
     * @param x0    A(x0, y0)
     * @param y0    A(x0, y0)
     * @param x1    B(x1, y1)
     * @param y1    B(x1, y1)
     * @return  true if it is vertical;
     */
    private boolean isVertical(double x0, double y0, double x1, double y1) {
        if (y0 == y1) {
            return false;
        }

        double angle = Math.abs(Math.atan((y1 - y0) / (x1 - x0)));

        // TODO: find a suitable proportional rule for angle offset
        double step = Math.abs(x1 - x0);
        double angleOffset = (step < 1 ? 1 / Math.pow(step, 2) : Math.pow(step, 3));
        return !(MAX_ANGLE <= angle) || !(angle <= Math.atan(angleOffset));
    }

    /**
     * Determine if a given math function may be considered vertical
     * on a given set of points
     * @param f math function
     * @param points    points to be evaluated by f
     * @return  true if it is vertical
     */
    private boolean isVertical(IDifferentiable<BigDecimal> f, double[] points) {
        double prevX = points[0];
        double prevY = f.evaluate(BigDecimal.valueOf(prevX)).doubleValue();

        for (int i = 1; i < points.length; i++) {
            if (isVertical(prevX, prevY, points[i], f.evaluate(BigDecimal.valueOf(points[i])).doubleValue())) {
                return true;
            }

            prevX = points[i];
        }

        return false;
    }

    /**
     * Map a x-plane coordinate to a x-pane coordinate
     * @param cartesianX    x-plane coordinate
     * @return  x-pane coordinates
     */
    private double mapToPaneX(double cartesianX) {
        double convertedX = cartesianX / getAxisRange(xAxis) * getWidth();

        return convertedX + getWidth() / 2;
    }

    /**
     * Map a y-plane coordinate to a y-pane coordinate
     * @param cartesianY    y-plane coordinate
     * @return  y-pane coordinates
     */
    private double mapToPaneY(double cartesianY) {
        double convertedY = - (cartesianY / getAxisRange(yAxis) * getHeight());

        return convertedY + getHeight() / 2;
    }

    /**
     * Map a x-pane coordinate to a x-plane coordinate
     * @param paneX x-pane coordinate
     * @return  x-plane coordinate
     */
    private double mapToCartesianX(double paneX) {
        double centeredX = paneX - getWidth() / 2;

        return centeredX / getWidth() * getAxisRange(xAxis);
    }

    /**
     * Map a y-pane coordinate to a y-plane coordinate
     * @param paneY y-pane coordinate
     * @return  y-plane coordinate
     */
    private double mapToCartesianY(double paneY) {
        double centeredY = paneY - getHeight() / 2;

        return (centeredY / getHeight() * getAxisRange(yAxis));
    }

    private double getAxisRange(NumberAxis axis) {
        return axis.getUpperBound() - axis.getLowerBound();
    }

    /**
     * Refresh the math function graph drawing canvas
     */
    private void sheetRefresh() {
        sheet.getChildren().clear();
        plot();
    }

    @Override
    public void update() {
        sheetRefresh();
    }
}
