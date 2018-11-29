package shared;

import java.io.Serializable;

/**
 * Holds x, y information for a point. Couldn't use default java points, as doubles are required.
 */
public class Point implements Serializable {
    private double x;
    private double y;
    private int centroid_id;
    private int row_id; // The row id of the point in the database

    /**
     * Creates a new point with a given x, y value.
     * @param x The x value of the new point.
     * @param y The y value of the new point.
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Creates a new point with a given x and y value, as well as with the row id of the point in the database.
     *
     * @param x The x value of the new point.
     * @param y The y value of the new point.
     * @param row_id The id of the point in the database (primary key).
     */
    public Point(double x, double y, int row_id) {
        this.x = x;
        this.y = y;
        this.row_id = row_id;
    }

    public int getRow_id() {
        return row_id;
    }

    public void setRow_id(int row_id) {
        this.row_id = row_id;
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
