package shared;

import java.io.Serializable;

/**
 * Holds x, y information for a point. Couldn't use default java points, as doubles are required.
 */
public class Point implements Serializable {
    private double x;
    private double y;
    private double centroid_x;
    private double centroid_y;

    /**
     * Creates a new point with a given x, y value.
     * @param x The x value of the new point.
     * @param y The y value of the new point.
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getCentroidX() {
        return this.centroid_x;
    }

    public double getCentroidY() {
        return this.centroid_y;
    }

    public void setCentroidX(double x) {
        this.centroid_x = x;
    }

    public void setCentroidY(double y) {
        this.centroid_y = y;
    }
}
