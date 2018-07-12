package eu.newton.ui.planes.helpers;

import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class Map extends Pane {

    private static final double DRAG_MOVE_FACTOR = .5;

    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    private final Translate translate;
    private final Scale scale;

    private final DragManager planeDragManager;
    private final DragManager paneDragManager;

    public Map(NumberAxis xAxis, NumberAxis yAxis) {

        this.xAxis = xAxis;
        xAxis.setSide(Side.BOTTOM);
        this.yAxis = yAxis;
        yAxis.setSide(Side.RIGHT);

        translate = new Translate();
        translate.setX(- 1280 / 2);
        translate.setY(- 720 / 2);

        scale = new Scale();
        scale.xProperty().bind(xAxis.upperBoundProperty().subtract(xAxis.lowerBoundProperty()).divide(this.widthProperty()));
        scale.yProperty().bind(yAxis.upperBoundProperty().subtract(yAxis.lowerBoundProperty()).divide(this.heightProperty()).negate());
        scale.setPivotX(0);
        scale.setPivotY(0);

        planeDragManager = new DragManager();
        paneDragManager = new DragManager();

        xAxis.layoutYProperty().bind(heightProperty().divide(2));
        xAxis.prefWidthProperty().bind(widthProperty());
        xAxis.setAnimated(false);

        yAxis.layoutXProperty().bind(widthProperty().divide(2));
        yAxis.prefHeightProperty().bind(heightProperty());
        yAxis.setAnimated(false);

        this.getChildren().addAll(xAxis, yAxis);

        setOnMouseClicked(click -> toCartesian(click.getX(), click.getY()));

        // DEBUG
        setOnMouseClicked(e -> {
            /*
            Point2D point = toCartesian(e.getX(), e.getY());
            System.err.println(point);

            Point2D p = toPane(0, 0);

            this.getChildren().add(new Circle(p.getX(), p.getY(), 5, Color.RED));
            */
        });


        // ZOOM
        // TODO: implement better zoom
        setOnScroll(scroll -> {

        });

        // DRAG
        // TODO: We have correct mapping, but inverted drag movement axis
        setOnMousePressed(click -> {

            Point2D paneAnchor = new Point2D(click.getX(), click.getY());
            Point2D planeAnchor = toCartesian(paneAnchor.getX(), paneAnchor.getY());

            paneDragManager.setAnchor(paneAnchor);
            planeDragManager.setAnchor(planeAnchor);

            setOnMouseDragged(drag -> {

                Point2D paneDrag = new Point2D(drag.getX(), drag.getY());
                Point2D planeDrag = toCartesian(paneDrag.getX(), paneDrag.getY());

                paneDragManager.setPoint(paneDrag);
                planeDragManager.setPoint(planeDrag);

                double planeMoveX = planeDragManager.moveX() * DRAG_MOVE_FACTOR;
                double planeMoveY = planeDragManager.moveY() * DRAG_MOVE_FACTOR;

                xAxis.setLowerBound(xAxis.getLowerBound() - planeMoveX);
                xAxis.setUpperBound(xAxis.getUpperBound() - planeMoveX);
                yAxis.setLowerBound(yAxis.getLowerBound() - planeMoveY);
                yAxis.setUpperBound(yAxis.getUpperBound() - planeMoveY);

                double paneMoveX = paneDragManager.moveX();
                double paneMoveY = paneDragManager.moveY();

                translate.setX(translate.getX() - (paneMoveX));
                translate.setY(translate.getY() - (paneMoveY));

                planeDragManager.setAnchor(planeDrag);
                paneDragManager.setAnchor(paneDrag);
            });

        });

    }

    public void refresh() {

        this.getChildren().clear();
        this.getChildren().addAll(xAxis, yAxis);
    }

    public void plotPoint(double x, double y, Color color) {

        Point2D point = toPane(x, y);

        Circle p = new Circle(point.getX(), point.getY(), 4, color);

        this.getChildren().add(p);
    }

    public void plotSegment(double x0, double y0, double x1, double y1, Color color) {

        Point2D p0 = toPane(x0, y0);
        Point2D p1 = toPane(x1, y1);

        Line segment = new Line(p0.getX(), p0.getY(), p1.getX(), p1.getY());
        segment.setStrokeWidth(2);
        segment.setStroke(color);

        this.getChildren().add(segment);
    }

    private Point2D toCartesian(Point2D p) {

        return toCartesian(p.getX(), p.getY());
    }

    private Point2D toCartesian(double x, double y) {

        Point2D centered = translate.transform(x, y);

        Point2D scaled = scale.transform(centered.getX(), centered.getY());

        return scaled;
    }

    private Point2D toPane(Point2D p) {

        return toPane(p.getX(), p.getY());
    }

    private Point2D toPane(double x, double y) {

        Point2D invTrans = null;

        try {

            Point2D invScaled = scale.inverseTransform(x, y);
            invTrans = translate.inverseTransform(invScaled);

        } catch (NonInvertibleTransformException e) {

            System.err.println("[ERROR]! unable to invert transform!");
        }

        return invTrans;
    }

}
