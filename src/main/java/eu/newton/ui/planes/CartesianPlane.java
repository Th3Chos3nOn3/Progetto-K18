package eu.newton.ui.planes;

import eu.newton.IMathFunction;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.functionmanager.IObserver;
import eu.newton.ui.planes.helpers.DragManager;
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

    private static final int STD_POINT_DENSITY = 100;
    private static final double STD_TICK_DENSITY = 10;
    private static final int CHECK_THRESHOLD = 10;
    private static final double MAX_ANGLE = Math.atan(Double.MIN_VALUE);

    private static final double DRAG_MOVE_FACTOR = .5;
    private static final double ZOOM_FACTOR = 0.01;
    private static double MIN_AXIS_WIDTH = 0.08;

    private final IFunctionManager<BigDecimal> functionManager;

    private Group sheet;

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    private double axisTickDensity;
    private int pointDensity;

    private final DragManager planeDragManager;
    private final DragManager paneDragManager;

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

        planeDragManager = new DragManager();
        paneDragManager = new DragManager();

        getChildren().addAll(xAxis, yAxis, sheet);

        // TODO: fix tickMarks warning
        // TODO: implement zoom
        // TODO: fix zoom coordinates mapping (can't be fixed setting the scale of the sheet itself)
        setOnScroll(scroll -> {

            double zoomSign = Math.signum(scroll.getDeltaY());

            // Avoid swapping lower and upper bound error
            if ((computeDistance(xAxis.getLowerBound(), xAxis.getUpperBound()) <= MIN_AXIS_WIDTH ||
                    computeDistance(yAxis.getLowerBound(), yAxis.getUpperBound()) <= MIN_AXIS_WIDTH) && zoomSign > 0) {

                return;
            }

            double centerX = mapToCartesianX(scroll.getX());
            double centerY = mapToCartesianY(scroll.getY());

            double leftIncrement = computeDistance(xAxis.getLowerBound(), centerX) * ZOOM_FACTOR * zoomSign;
            double rightIncrement = computeDistance(centerX, xAxis.getUpperBound()) * ZOOM_FACTOR * zoomSign;
            double bottomIncrement = computeDistance(yAxis.getLowerBound(), centerY) * ZOOM_FACTOR * zoomSign;
            double topIncrement = computeDistance(centerY, yAxis.getUpperBound()) * ZOOM_FACTOR * zoomSign;

            xAxis.setLowerBound(xAxis.getLowerBound() + leftIncrement);
            xAxis.setUpperBound(xAxis.getUpperBound() - rightIncrement);
            yAxis.setLowerBound(yAxis.getLowerBound() + bottomIncrement);
            yAxis.setUpperBound(yAxis.getUpperBound() - topIncrement);

            sheetRefresh();

        });

        setOnMousePressed(click -> {

            System.err.println(mapToCartesianX(click.getX()) + " " + mapToCartesianY(click.getY()));

            planeDragManager.setAnchor(mapToCartesianX(click.getX()), mapToCartesianY(click.getY()));
            paneDragManager.setAnchor(click.getX(), click.getY());

            setOnMouseDragged(drag -> {

                double dragX = drag.getX();
                double dragY = drag.getY();

                planeDragManager.setDrag(mapToCartesianX(dragX), mapToCartesianY(dragY));
                paneDragManager.setDrag(dragX, dragY);

                double planeMoveX = planeDragManager.moveX() * DRAG_MOVE_FACTOR;
                double planeMoveY = planeDragManager.moveY() * DRAG_MOVE_FACTOR;

                xAxis.setLowerBound(xAxis.getLowerBound() - planeMoveX);
                xAxis.setUpperBound(xAxis.getUpperBound() - planeMoveX);
                yAxis.setLowerBound(yAxis.getLowerBound() - planeMoveY);
                yAxis.setUpperBound(yAxis.getUpperBound() - planeMoveY);

                double paneMoveX = paneDragManager.moveX() * DRAG_MOVE_FACTOR;
                double paneMoveY = paneDragManager.moveY() * DRAG_MOVE_FACTOR;

                // LOL, I have to move the sheet
                sheet.setTranslateX(sheet.getTranslateX() + paneMoveX);
                sheet.setTranslateY(sheet.getTranslateY() + paneMoveY);

                sheetRefresh();

                planeDragManager.setAnchor(mapToCartesianX(dragX), mapToCartesianY(dragY));
                paneDragManager.setAnchor(dragX, dragY);
            });

        });

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
        sheet.getChildren().add(new Circle(mapToPaneX(x), mapToPaneY(y), 4, color));
    }


    /**
     * Plot all controller managed functions, zeros and extrema
     */
    private void plot() {

        double step = computeDistance(xAxis.getLowerBound(), xAxis.getUpperBound()) / pointDensity;

        for (IMathFunction<BigDecimal> f : functionManager.getFunctions()) {

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
                                plotFunctions(f, midPoints, Color.RED);
                                break;
                            }

                             // May be a discontinuity! Don't plotFunctions the segment
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
    private void plotFunctions(IMathFunction<BigDecimal> f, double[] points, Color color) {
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
        double step = computeDistance(x0, x1);
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
    private boolean isVertical(IMathFunction<BigDecimal> f, double[] points) {
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
        double centeredY = - (paneY - getHeight() / 2);

        return centeredY / getHeight() * getAxisRange(yAxis);
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

    private double computeDistance(double a, double b) {

        if (a == b) {

            return 0;

        } else if (a > b) {

            return a - b;

        } else {

            return b - a;

        }

    }

}