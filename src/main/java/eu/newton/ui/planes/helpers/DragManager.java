package eu.newton.ui.planes.helpers;

public final class DragManager {

    private double anchorX, anchorY;
    private double dragX, dragY;

    public DragManager() {
    }

    public void setAnchor(double x, double y) {
        anchorX = x;
        anchorY = y;
    }

    public void setDrag(double x, double y) {
        dragX = x;
        dragY = y;
    }

    public double moveX() {
        return dragX - anchorX;
    }

    public double moveY() {
        return dragY - anchorY;
    }
}
