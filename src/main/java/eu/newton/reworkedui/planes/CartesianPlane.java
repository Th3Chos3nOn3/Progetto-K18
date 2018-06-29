package eu.newton.reworkedui.planes;

import eu.newton.MathematicalFunction;
import eu.newton.reworkedui.functionmanager.IFunctionManager;
import eu.newton.reworkedui.functionmanager.IObserver;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Cartesian plane where to draw math functions.
 */
public class CartesianPlane extends Pane implements IObserver {

    private static final int STD_POINT_DENSITY = 500;
    private static final double STD_TICK_DENSITY = 10;

    private final IFunctionManager functionManager;
    private Group sheet;

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    private double axisTickDensity;
    private int pointDensity;

    public CartesianPlane(IFunctionManager functionManager, double xLow, double xHi, double yLow, double yHi) {
        this(functionManager, xLow, xHi, yLow, yHi, STD_TICK_DENSITY, STD_POINT_DENSITY);
    }

    public CartesianPlane(
            IFunctionManager functionManager,
            double xLow, double xHi,
            double yLow, double yHi,
            double axisTickDensity,
            int pointsDensity) {

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
     * Plot all the functions managed by the functionManager
     */
    public void plot() {
        double step0 = (Math.abs(xAxis.getUpperBound()) + Math.abs(xAxis.getLowerBound())) / pointDensity;
        double maxStep = step0;         // 1/500 larghezza grafico
        double minStep = step0 / 100;   // 1/5000 larghezza grafico
        double disc = maxStep * 10;     // 5*maxStep<disc<10*maxStep; disc -> discrimina tra discontinuità e non discontinuità

        for (MathematicalFunction f : functionManager.getFunctions()) {
            double xa = xAxis.getLowerBound();

            if (f != null) {

                double ya, dya, xb, yb, dyb;
                double newStep = step0;
                while (xa < xAxis.getUpperBound()) {
                    xb = xa + newStep;
                    ya = f.evaluate(xa);
                    dya = f.evaluate(xa);
                    yb = f.evaluate(xb);
                    dyb = f.evaluate(xb);

                    double distab = Math.sqrt(Math.pow(xa - xb, 2) + Math.pow(ya - yb, 2));

                    if (distab < disc) plotSegment(xa, ya, xb, yb, Color.RED);

                    newStep = this.computeNewStep(dya,dyb,maxStep,minStep,newStep);
                    xa = xb;
                }
            }
        }
    }

    private double computeNewStep(double dya, double dyb, double maxStep, double minStep, double oldStep){
        double newStep = 0;

        dya = Math.abs(dya);
        dyb = Math.abs(dyb);

        //scambia dya con dyb nel caso dya < dyb
        if (dya < dyb) {
            double temp = dya;

            dya = dyb;
            dyb = temp;
        }

        //da qui in poi dya sempre maggiore di dyb
        if (dya!=0 && dyb!=0) {
            newStep = oldStep*(dyb/dya);

            if (newStep < minStep) {
                newStep = minStep;
            } else if (newStep > maxStep) {
                newStep = maxStep;
            }

        } else {

            if (dya==0 && dyb==0) newStep = maxStep;
            if (dya==0 && dyb!=0) newStep = maxStep;
            if (dya!=0 && dyb==0) newStep = minStep;

        }

        return newStep;
    }

    private void plotSegment(double x0, double y0, double x1, double y1, Color color) {
        Line line = new Line(mapToPaneX(x0), mapToPaneY(y0), mapToPaneX(x1), mapToPaneY(y1));
        line.setStrokeWidth(2);
        line.setStroke(color);
        sheet.getChildren().add(line);
    }

    private double mapToPaneX(double cartesianX) {
        double convertedX = cartesianX / getAxisRange(xAxis) * getWidth();

        return convertedX + getWidth() / 2;
    }

    private double mapToPaneY(double cartesianY) {
        double convertedY = - (cartesianY / getAxisRange(yAxis) * getHeight());

        return convertedY + getHeight() / 2;
    }

    private double mapToCartesianX(double paneX) {
        double centeredX = paneX - getWidth() / 2;

        return centeredX / getWidth() * getAxisRange(xAxis);
    }

    private double mapToCartesianY(double paneY) {
        double centeredY = paneY - getHeight() / 2;

        return (centeredY / getHeight() * getAxisRange(yAxis));
    }

    private double getAxisRange(NumberAxis axis) {
        return axis.getUpperBound() - axis.getLowerBound();
    }

    private void sheetRefresh() {
        sheet.getChildren().clear();
        plot();
    }

    @Override
    public void update() {
        sheetRefresh();
    }
}
