package shared;

import java.io.Serializable;

/**
 * Holds x, y information for a point. Couldn't use default java points, as doubles are required.
 */
public class Point implements Serializable {
    private double x;
    private double y;
    private int centroid_id;

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

    public int getCentroid_id(){ return this.centroid_id; }

    public void setCentroid_id(int id){ this.centroid_id = id; }
}
