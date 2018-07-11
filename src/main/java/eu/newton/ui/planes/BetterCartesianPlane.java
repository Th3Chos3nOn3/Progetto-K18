package eu.newton.ui.planes;

import eu.newton.IMathFunction;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.functionmanager.IObserver;
import eu.newton.ui.planes.helpers.DragManager;
import eu.newton.ui.planes.helpers.Mapper;
import eu.newton.ui.planes.helpers.MathExceptionFilter;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.math.BigDecimal;

// TODO: fix fucking slow plotting
// TODO: update while drag (when it will be faster)
public class BetterCartesianPlane extends LineChart<Number, Number> implements IObserver {

    private static final int STD_POINT_DENSITY = 50;
    private static final double MAX_ANGLE = Math.atan(Double.MIN_VALUE);

    private static final double ZOOM_FACTOR = 0.01;
    private static final double DRAG_MOVE_FACTOR = .5;

    private final IFunctionManager<BigDecimal> functionManager;

    private final NumberAxis xAxis, yAxis;

    private final MathExceptionFilter<BigDecimal> filter;
    private final Mapper mapper;
    private final DragManager planeDragManager;
    private final DragManager paneDragManager;

    public BetterCartesianPlane(IFunctionManager<BigDecimal> functionManager, double xLow, double xHi, double yLow, double yHi) {
        super(new NumberAxis(xLow, xHi, 1), new NumberAxis(yLow, yHi, 1));

        this.functionManager = functionManager;
        functionManager.addObserver(this);

        xAxis = (NumberAxis) this.getXAxis();
        yAxis = (NumberAxis) this.getYAxis();

        filter = new MathExceptionFilter<>();
        mapper = new Mapper(xAxis, yAxis);
        planeDragManager = new DragManager();
        paneDragManager = new DragManager();

        setOnScroll(scroll -> {

            double zoomSign = Math.signum(scroll.getDeltaY());

            double centerX = mapper.mapToCartesianX(scroll.getX(), getWidth());
            double centerY = mapper.mapToCartesianY(scroll.getY(), getHeight());

            double leftIncrement = -1 * computeDistance(xAxis.getLowerBound(), centerX) * ZOOM_FACTOR * zoomSign;
            double rightIncrement = 1 * computeDistance(centerX, xAxis.getUpperBound()) * ZOOM_FACTOR * zoomSign;
            double bottomIncrement = -1 * computeDistance(yAxis.getLowerBound(), centerY) * ZOOM_FACTOR * zoomSign;
            double topIncrement = +1 * computeDistance(centerY, yAxis.getUpperBound()) * ZOOM_FACTOR * zoomSign;

            xAxis.setLowerBound(xAxis.getLowerBound() + leftIncrement);
            xAxis.setUpperBound(xAxis.getUpperBound() + rightIncrement);
            yAxis.setLowerBound(yAxis.getLowerBound() + bottomIncrement);
            yAxis.setUpperBound(yAxis.getUpperBound() + topIncrement);

        });

        setOnMousePressed(click -> {

            planeDragManager.setAnchor(mapper.mapToCartesianX(click.getX(), getWidth()), mapper.mapToCartesianY(click.getY(), getHeight()));
            paneDragManager.setAnchor(click.getX(), click.getY());

            setOnMouseDragged(drag -> {

                double dragX = drag.getX();
                double dragY = drag.getY();

                planeDragManager.setDrag(mapper.mapToCartesianX(dragX, getWidth()), mapper.mapToCartesianY(dragY, getHeight()));
                paneDragManager.setDrag(dragX, dragY);

                double planeMoveX = planeDragManager.moveX() * DRAG_MOVE_FACTOR;
                double planeMoveY = planeDragManager.moveY() * DRAG_MOVE_FACTOR;

                xAxis.setLowerBound(xAxis.getLowerBound() - planeMoveX);
                xAxis.setUpperBound(xAxis.getUpperBound() - planeMoveX);
                yAxis.setLowerBound(yAxis.getLowerBound() + planeMoveY);
                yAxis.setUpperBound(yAxis.getUpperBound() + planeMoveY);

                planeDragManager.setAnchor(mapper.mapToCartesianX(dragX, getWidth()), mapper.mapToCartesianY(dragY, getHeight()));
                paneDragManager.setAnchor(dragX, dragY);
            });

        });

        this.getYAxis().setSide(Side.RIGHT);
        this.setLegendVisible(false);
        this.setAnimated(false);

        getStylesheets().add(getClass().getResource("/stylesheets/betterCartesianPlaneStylesheet.css").toExternalForm());

    }

    @Override
    public void update() {
        refresh();
    }

    @Override
    public void resize(double v, double v1) {
        super.resize(v, v1);
    }

    private void refresh() {
        getData().clear();
        plot();
    }

    private void plot() {

        double step = (Math.abs(xAxis.getUpperBound()) + Math.abs(xAxis.getLowerBound())) / STD_POINT_DENSITY;

        for (IMathFunction<BigDecimal> f : functionManager.getFunctions()) {

            Series series = new Series();

            // TODO: fix null pointer exception
            double prevX = xAxis.getLowerBound();
            double prevY = filter.eval(f, BigDecimal.valueOf(prevX)).doubleValue();

            series.getData().add(new Data<>(prevX, prevY));

            for (double x = prevX + step; x <= xAxis.getUpperBound(); x += step) {

                // TODO: fix null pointer exception
                double y = filter.eval(f, BigDecimal.valueOf(x)).doubleValue();

                BigDecimal zero = filter.zero(f, BigDecimal.valueOf(xAxis.getLowerBound()), BigDecimal.valueOf(xAxis.getUpperBound()));
                BigDecimal minX = filter.min(f, BigDecimal.valueOf(xAxis.getLowerBound()), BigDecimal.valueOf(xAxis.getUpperBound()));
                BigDecimal maxX = filter.max(f, BigDecimal.valueOf(xAxis.getLowerBound()), BigDecimal.valueOf(xAxis.getUpperBound()));

                plotPoint(zero, BigDecimal.ZERO, Color.GRAY);

                BigDecimal minY = filter.eval(f, minX);
                plotPoint(minX, minY, Color.BLUE);

                BigDecimal maxY = filter.eval(f, maxX);
                plotPoint(maxX, maxY, Color.GREEN);

                series.getData().add(new Data<>(x, y));

            }

            this.getData().add(series);
        }

    }

    private void plotPoint(BigDecimal x, BigDecimal y, Color color) {
        if (x != null && y != null) {

            Data d = new Data(x, y);
            d.setNode(new Circle(5, color));

            Series s = new Series();
            s.getData().add(d);

            this.getData().add(s);
        }
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
