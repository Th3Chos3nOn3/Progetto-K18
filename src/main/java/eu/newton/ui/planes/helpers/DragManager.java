package eu.newton.ui.planes.helpers;

import javafx.geometry.Point2D;

public final class DragManager {

    private Point2D anchor;
    private Point2D point;

    public DragManager() {
    }

    public void setAnchor(Point2D anchor) {
        this.anchor = anchor;
    }

    public void setPoint(Point2D point) {
        this.point = point;
    }

    public double moveX() {
        return anchor.getX() - point.getX();
    }

    public double moveY() {
        return anchor.getY() - point.getY();
    }
}
