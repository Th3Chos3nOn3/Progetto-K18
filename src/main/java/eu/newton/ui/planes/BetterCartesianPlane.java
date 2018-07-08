package eu.newton.ui.planes;

import eu.newton.api.IDifferentiable;
import eu.newton.ui.functionmanager.IFunctionManager;
import eu.newton.ui.functionmanager.IObserver;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

import java.math.BigDecimal;

public class BetterCartesianPlane extends LineChart<Number, Number> implements IObserver {

    private static final int N_VERTICES = 200;
    private static final int PLOT_LOOP_TRESHOLD = 5;
    private static final double RAD_TO_DEG = 180 / Math.PI;
    private static final double MAX_ANGLE = 90;
    private static final double DELTA_ANGLE = 1;

    private final IFunctionManager<BigDecimal> functionManager;

    private final NumberAxis xAxis, yAxis;

    // TODO: add caching for plotted functions

    public BetterCartesianPlane(IFunctionManager<BigDecimal> functionManager, NumberAxis xAxis, NumberAxis yAxis) {

        super(xAxis, yAxis);

        this.functionManager = functionManager;
        functionManager.addObserver(this);

        this.xAxis = xAxis;
        this.yAxis = yAxis;

        // TODO: implement zoom

        // TODO: implement drag

        // TODO: Add .css file
        xAxis.setSide(Side.BOTTOM);
        yAxis.setSide(Side.RIGHT);
        this.setLegendVisible(false);
        this.setAnimated(false);

        getStylesheets().add(getClass().getResource("/stylesheets/betterCartesianPlaneStylesheet.css").toExternalForm());
    }

    // TODO: improve method
    public void plotManaged() {
        double step = (Math.abs(xAxis.getUpperBound()) + Math.abs(xAxis.getLowerBound())) / N_VERTICES;

        for (IDifferentiable<BigDecimal> f : functionManager.getFunctions()) {

            Series points = new Series();

            double previousX = xAxis.getLowerBound();
            double previousY = f.evaluate(BigDecimal.valueOf(previousX)).doubleValue();

            points.getData().add(point(previousX, previousY));

            for (double x = previousX + step; x <= xAxis.getUpperBound(); x += step) {

                double y = f.evaluate(BigDecimal.valueOf(x)).doubleValue();

                if (!isVertical(previousX, previousY, x, y)) {
                    points.getData().add(point(x, y));
                } else {
                    this.getData().add(points);
                    points = new Series();
                }

                previousX = x;
                previousY = y;
            }

            this.getData().add(points);

        }

    }

    @Override
    public void update() {
        getData().clear();
        plotManaged();
    }

    private <T> Data<T, T> point(T x, T y) {

        Data<T, T> point = new Data<>(x, y);

        return point;
    }

    private boolean isVertical(double x0, double y0, double x1, double y1) {

        double m =  (y0 - y1) / (x0 - x1);
        double degAngle = Math.abs(Math.atan(m) * RAD_TO_DEG);

        return MAX_ANGLE - degAngle < DELTA_ANGLE;
    }

}
