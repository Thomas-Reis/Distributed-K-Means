package shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PointGroup implements Serializable {

    private ArrayList<Point> points;
    // Holds the centroid additions, where double is the centroid addition values
    // (0 being x addition, 1 being y, 2 being total added)
    private HashMap<Integer, double[]> centroid_sums = new HashMap<>();
    private String uid;
    private String processed_by = "";

    public PointGroup(ArrayList<Point> points, String uid) {
        this.points = points;
        this.uid = uid;
    }

    public ArrayList<Point> getPoints() {
        return this.points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public String getUid() {
        return this.uid;
    }

    public void setProcessedBy(String user_id) {
        if (this.processed_by.equals(""))
        { this.processed_by = user_id; }
    }

    public HashMap<Integer, double[]> getCentroidSums() {
        return centroid_sums;
    }

    public void setCentroidSums(HashMap<Integer, double[]> centroid_sums) {
        this.centroid_sums = centroid_sums;
    }

    public String getProcessedBy() {
        return this.processed_by;
    }

    /** Adds a single point to the point group, performing the addition on the point set as well.
     *
     * @param point The point to add.
     */
    public void addPointToList(Point point) {
        // Check the point's centroid owner
        int owner = point.getCentroid_id();
        // Check if the centroid is in the hash map
        if (centroid_sums.containsKey(owner)) {
            // Add to the current sums
            double[] sum = centroid_sums.get(owner);
            sum[0] += point.getX();
            sum[1] += point.getY();
            sum[2]++;
            // Reset the sum in the hash map
            centroid_sums.replace(owner, sum);
        }
        else {
            // Create a new double array for additions
            double[] sum = new double[3];
            sum[0] = point.getX();
            sum[1] = point.getY();
            sum[2] = 1;
            // Set the value in the hash map
            centroid_sums.put(owner, sum);
        }
        // Add the point to the array list
        points.add(point);
    }

    /** Adds a set of points to the point group. Also performs the addition on the point set.
     *
     * @param new_points The new points to add.
     */
    public void addPointsToList(ArrayList<Point> new_points) {
        // Loop through all the points given
        for (Point point: new_points) {
            addPointToList(point);
        }
    }

    /** Combine the given point group with the point group that called the function.
     *
     * @param other The other point group to combine with this one.
     */
    public void combinePointGroup(PointGroup other) {
        // Get the other group's point group and their addition hash map
        ArrayList<Point> other_points = other.getPoints();
        HashMap<Integer, double[]> other_sums = other.getCentroidSums();
        // Add the points to the current group's
        points.addAll(other_points);
        // Add the hash maps together
        for (Map.Entry<Integer, double[]> current_sum: other_sums.entrySet()) {
            // Check if it is already in the hash map
            if (centroid_sums.containsKey(current_sum.getKey())) {
                // Get the current values and the new values
                double[] current = centroid_sums.get(current_sum.getKey());
                double[] value = current_sum.getValue();
                // Add the arrays together
                current[0] += value[0];
                current[1] += value[1];
                current[2] += value[2];
                // Set the centroid sum to the current sum
                centroid_sums.replace(current_sum.getKey(), current_sum.getValue());
            }
            else {
                // Simply put it in the hash map if it didn't exist already inside it
                centroid_sums.put(current_sum.getKey(), current_sum.getValue());
            }
        }
    }

    /** Gets a new point group of the centroids given. Uses the centroid_sums hash map in order to do so.
     *
     * @return The new point group of centroids.
     */
    public PointGroup getNewCentroids() {
        // The new array list that will hold the centroids
        ArrayList<Point> calculated_centroids = new ArrayList<>();
        for (Map.Entry<Integer, double[]> current_centroid_sum: centroid_sums.entrySet()) {
            // Perform the calculation for the centroid location
            double[] value = current_centroid_sum.getValue();
            double x = value[0] / value[2];
            double y = value[1] / value[2];
            // Create the point
            Point current_centroid = new Point(x, y);
            // Set the centroid to own itself, to keep track of it's own id
            current_centroid.setCentroid_id(current_centroid_sum.getKey());
            // Add the point to the array list
            calculated_centroids.add(current_centroid);
        }

        // Create the new uid for the centroid group
        String[] id_breakdown = uid.split(" ");
        // Increment the iteration number
        id_breakdown[2] = Integer.toString(Integer.parseInt(id_breakdown[2]) + 1);
        String new_uid = id_breakdown[0] + " CENTROID " + id_breakdown[2] ;
        // Create and return the point group
        return new PointGroup(calculated_centroids, new_uid);
    }

    public boolean equals(PointGroup pg) {
        // Loop through the hash map
        for (Map.Entry<Integer, double[]> centroid: centroid_sums.entrySet()) {
            if (pg.getCentroidSums().containsKey(centroid.getKey())) {
                // Get the current map entry
                double[] value1 = centroid.getValue();
                double[] value2 = pg.getCentroidSums().get(centroid.getKey());

                // If the values aren't equal, objects are not equal and should be returned as false
                for (int i = 0; i < value1.length && i < value2.length; i++) {
                    if (!(value1[i] == value2[i])) {
                        return false;
                    }
                }
            }
        }
        // If the entire hash map is walked through, it returns true
        return true;
    }
}
