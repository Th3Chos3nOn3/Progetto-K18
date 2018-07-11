package eu.newton.ui.planes.helpers;

import javafx.scene.chart.NumberAxis;

public final class Mapper {
    
    private NumberAxis xAxis, yAxis;

    public Mapper(NumberAxis xAxis, NumberAxis yAxis) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    /**
     * Map a x-plane coordinate to a x-pane coordinate
     * @param cartesianX    x-plane coordinate
     * @return  x-pane coordinates
     */
    public double mapToPaneX(double cartesianX, double paneWidth) {
        double convertedX = cartesianX / getAxisRange(xAxis) * paneWidth;

        return convertedX + paneWidth / 2;
    }

    /**
     * Map a y-plane coordinate to a y-pane coordinate
     * @param cartesianY    y-plane coordinate
     * @return  y-pane coordinates
     */
    public double mapToPaneY(double cartesianY, double paneHeight) {
        double convertedY = - (cartesianY / getAxisRange(yAxis) * paneHeight);

        return convertedY + paneHeight / 2;
    }

    /**
     * Map a x-pane coordinate to a x-plane coordinate
     * @param paneX x-pane coordinate
     * @return  x-plane coordinate
     */
    public double mapToCartesianX(double paneX, double paneWidth) {
        double centeredX = paneX - paneWidth / 2;

        return centeredX / paneWidth * getAxisRange(xAxis);
    }

    /**
     * Map a y-pane coordinate to a y-plane coordinate
     * @param paneY y-pane coordinate
     * @return  y-plane coordinate
     */
    public double mapToCartesianY(double paneY, double paneHeight) {
        double centeredY = paneY - paneHeight / 2;

        return (centeredY / paneHeight * getAxisRange(yAxis));
    }

    public double getAxisRange(NumberAxis axis) {
        return axis.getUpperBound() - axis.getLowerBound();
    }

}
