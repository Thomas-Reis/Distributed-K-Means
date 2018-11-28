package shared;

/**
 * Holds x, y information for a point. Couldn't use default java points, as doubles are required.
 */
public class Point {
    private double x;
    private double y;

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

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
