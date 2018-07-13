package eu.newton.ui.planes;

import eu.newton.IMathFunction;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.functionmanager.IObserver;
import eu.newton.ui.planes.helpers.Map;
import eu.newton.ui.planes.helpers.MathExceptionFilter;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.math.BigDecimal;

/**
 * Cartesian plane where to draw math functions.
 */
public class CartesianPlane extends Pane implements IObserver {

    private static final int STD_POINT_DENSITY = 100;
    private static final double STD_TICK_DENSITY = 10;
    private static final int CHECK_THRESHOLD = 10;
    private static final double MAX_ANGLE = Math.atan(Double.MIN_VALUE);
    private static final int ZEROS__AND_EXTREMA_SPLITS = 5;

    private final IFunctionManager<BigDecimal> functionManager;

    private final Map map;

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    private double axisTickDensity;
    private int pointDensity;

    private final MathExceptionFilter<BigDecimal> filter;

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

        map = new Map(xAxis, yAxis);
        map.prefWidthProperty().bind(this.widthProperty());
        map.prefHeightProperty().bind(this.heightProperty());

        filter = new MathExceptionFilter<>();

        getChildren().addAll(map);

    }

    @Override
    public void update() {
        sheetRefresh();
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
    public void plotPoint(double x, double y, Color color) {
        map.plotPoint(x, y, color);
    }


    /**
     * Plot all controller managed functions, zeros and extrema
     */
    private void plot() {

        double step = computeDistance(xAxis.getLowerBound(), xAxis.getUpperBound()) / pointDensity;

        for (IMathFunction<BigDecimal> f : functionManager.getFunctions()) {

            if (f != null) {

                Double[][] zerosAndExtrema = findZerosAndExtrema(f, xAxis.getLowerBound(), xAxis.getUpperBound());

                // plot zeros
                for (int i = 0; i < zerosAndExtrema.length; i++) {

                    if (zerosAndExtrema[0][i] != null) {
                        plotPoint(zerosAndExtrema[0][i], 0, Color.GRAY);
                    }

                }

                // plot min if present
                if (zerosAndExtrema[1][0] != null) {
                    plotPoint(
                            zerosAndExtrema[1][0],
                            filter.eval(f, BigDecimal.valueOf(zerosAndExtrema[1][0])).doubleValue(),
                            Color.BLUE
                    );
                }

                // plot max if present
                if (zerosAndExtrema[2][0] != null) {
                    plotPoint(
                            zerosAndExtrema[2][0],
                            filter.eval(f, BigDecimal.valueOf(zerosAndExtrema[2][0])).doubleValue(),
                            Color.GREEN
                    );
                }

                plot(f, step);

            }

        }

        java.util.Map<IMathFunction<BigDecimal>, Integer> functions = functionManager.getDerivativeFunctions();

        for (IMathFunction<BigDecimal> f : functions.keySet()) {

            if (f != null) {

                plotDerivative(f, functions.get(f), step);
            }
        }

    }

    private void plot(IMathFunction<BigDecimal> f, double step) {

        double previousX = xAxis.getLowerBound();

        BigDecimal res0 = filter.eval(f, BigDecimal.valueOf(previousX));

        while (previousX < xAxis.getUpperBound() && res0 == null) {

            previousX += step;
            res0 = filter.eval(f, BigDecimal.valueOf(previousX));
        }

        if (res0 == null) {
            return;
        }

        double previousY = res0.doubleValue();

        for (double x = previousX + step; x <= xAxis.getUpperBound(); x += step) {

            BigDecimal res1 = filter.eval(f, BigDecimal.valueOf(x));

            if (res1 == null) {
                continue;
            }

            double y1 = res1.doubleValue();

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

    private void plotDerivative(IMathFunction<BigDecimal> f, int grade, double step) {

        if (grade <= 0) {
            return;
        }

        double previousX = xAxis.getLowerBound();

        BigDecimal res0 = filter.differ(f, BigDecimal.valueOf(previousX), grade);

        while (previousX < xAxis.getUpperBound() && res0 == null) {

            previousX += step;
            res0 = filter.differ(f, BigDecimal.valueOf(previousX), grade);
        }

        if (res0 == null) {
            return;
        }

        double previousY = res0.doubleValue();

        for (double x = previousX + step; x <= xAxis.getUpperBound(); x += step) {

            BigDecimal res1 = filter.differ(f, BigDecimal.valueOf(x), grade);

            if (res1 == null) {
                continue;
            }

            double y1 = res1.doubleValue();

            if (!isVertical(previousX, previousY, x, y1)) {

                plotSegment(previousX, previousY, x, y1, Color.VIOLET);

            } else {

                for (int i = 0; i < CHECK_THRESHOLD; i++) {
                    double[] midPoints = splitSegment(previousX, x, i + 2);

                    if (!isVertical(f, midPoints)) {
                        // Probably not a discontinuity
                        plotDerivative(f, grade, midPoints, Color.VIOLET);
                        break;
                    }

                    // May be a discontinuity! Don't plot the segment
                }
            }

            previousX = x;
            previousY = y1;
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
     * @param points    points to be filter.eval()d by f
     * @param color function's color
     */
    private void plot(IMathFunction<BigDecimal> f, double[] points, Color color) {

        if (points.length < 2) {
            throw new AssertionError("At least 2 points");
        }

        for (int i = 0; i < points.length - 1; i++) {

            double x0 = points[i];
            BigDecimal y0 = filter.eval(f, BigDecimal.valueOf(points[i]));
            double x1 = points[i + 1];
            BigDecimal y1 = filter.eval(f, BigDecimal.valueOf(points[i + 1]));

            if (y0 != null && y1 != null) {

                plotSegment(x0, y0.doubleValue(), x1, y1.doubleValue(), color);
            }

        }

    }

    private void plotDerivative(IMathFunction<BigDecimal> f, int grade, double[] points, Color color) {

        if (points.length < 2) {
            throw new AssertionError("At least 2 points");
        }

        for (int i = 0; i < points.length - 1; i++) {

            double x0 = points[i];
            BigDecimal y0 = filter.differ(f, BigDecimal.valueOf(points[i]), grade);
            double x1 = points[i + 1];
            BigDecimal y1 = filter.differ(f, BigDecimal.valueOf(points[i + 1]), grade);

            if (y0 != null && y1 != null) {

                plotSegment(x0, y0.doubleValue(), x1, y1.doubleValue(), color);
            }

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
        map.plotSegment(x0, y0, x1, y1, color);
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
        double step = computeDistance(x0, x1);
        double angleOffset = (step < 1 ? 1 / Math.pow(step, 2) : Math.pow(step, 3));
        return !(MAX_ANGLE <= angle) || !(angle <= Math.atan(angleOffset));
    }

    /**
     * Determine if a given math function may be considered vertical
     * on a given set of points
     * @param f math function
     * @param points    points to be filter.eval()d by f
     * @return  true if it is vertical
     */
    private boolean isVertical(IMathFunction<BigDecimal> f, double[] points) {
        double prevX = points[0];
        double prevY = filter.eval(f, BigDecimal.valueOf(prevX)).doubleValue();

        for (int i = 1; i < points.length; i++) {
            if (isVertical(prevX, prevY, points[i], filter.eval(f, BigDecimal.valueOf(points[i])).doubleValue())) {
                return true;
            }

            prevX = points[i];
        }

        return false;
    }

    /**
     * Refresh the math function graph drawing canvas
     */
    private void sheetRefresh() {
        map.refresh();
        plot();
    }

    private double computeDistance(double a, double b) {

        if (a == b) {

            return 0;

        } else if (a > b) {

            return a - b;

        } else {

            return b - a;

        }

    }

    private Double[][] findZerosAndExtrema(IMathFunction<BigDecimal> f, double a, double b) {

        double[] split = splitSegment(a, b, ZEROS__AND_EXTREMA_SPLITS);

        Double[][] matrix = new Double[3][split.length - 1];

        Double dMin = null;
        Double dMax = null;

        for (int i = 0; i < split.length - 1; i++) {

            BigDecimal zero = filter.zero(f, BigDecimal.valueOf(split[i]), BigDecimal.valueOf(split[i + 1]));
            BigDecimal min = filter.min(f, BigDecimal.valueOf(split[i]), BigDecimal.valueOf(split[i + 1]));
            BigDecimal max = filter.max(f, BigDecimal.valueOf(split[i]), BigDecimal.valueOf(split[i + 1]));

            if (zero != null) {
                matrix[0][i] = zero.doubleValue();
            }

            if (min != null) {

                double foundMin = min.doubleValue();

                if (dMin == null) {

                    dMin = foundMin;

                } else {

                    if (foundMin < dMin) {
                        dMin = foundMin;
                    }

                }
            }

            if (max != null) {

                double foundMax = max.doubleValue();

                if (dMax == null) {

                    dMax = foundMax;

                } else {

                    if (foundMax > dMax) {
                        dMax = foundMax;
                    }

                }
            }

        }

        if (dMin != null) {
            matrix[1][0] = dMin;
        }

        if (dMax != null) {
            matrix[2][0] = dMax;
        }

        return matrix;
    }

}