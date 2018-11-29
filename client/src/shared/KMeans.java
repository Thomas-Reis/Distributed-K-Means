package shared;

import shared.Point;

import java.lang.Math;
import java.util.ArrayList;

/**
 * Includes all of the functions necessary to perform k-means related calculations.
 */
public class KMeans {

    /** Calculates which centroid a given point belongs to.
     *
     * @param p The point to have classified in terms of which cluster it belongs to.
     * @param centroids_group The PointGroup that contains the centroids available for the classification.
     * @return The new point which has the centroid owner assigned to it.
     */
    public static Point getOwningCentroid(Point p, PointGroup centroids_group) {
        // The current owner of the given point, will be used to return owner
        int int_owner = -1;
        // The distance to the current closest point
        double double_closest_distance = Double.POSITIVE_INFINITY;
        // The distance of the point to the current centroid in the loop
        double double_current_distance;
        // Loop through all the centroids
        ArrayList<Point> centroids = centroids_group.getPoints();
        for (Point centroid: centroids) {
            // Check the distance to current centroid
            double_current_distance = Math.sqrt( Math.pow(centroid.getX() - p.getX(), 2)
                            + Math.pow(centroid.getY() - p.getY(), 2) );
            if (double_current_distance < double_closest_distance) {
                // assign the current centroid as the owner centroid
                int_owner = centroid.getCentroid_id();
                double_closest_distance = double_current_distance;
            }
        }
        // Set the point's owner
        p.setCentroid_id(int_owner);
        // Return the point
        return p;
    }

    /** Gets all the owning centroids in a point group, and returns the updated point group.
     *
     * @param points_group The PointGroup with the points to classify.
     * @param centroids_group The PointGroup with the centroids.
     * @return
     */
    public static PointGroup getOwningCentroids(PointGroup points_group, PointGroup centroids_group) {
        // Get the ArrayList of given points
        ArrayList<Point> given_points = points_group.getPoints();
        // The ArrayList of points that will be returned in the returned PointGroup
        ArrayList<Point> updated_points = new ArrayList<>();
        // Loop through the points
        for (Point point: given_points) {
            // Classify the point
            point = getOwningCentroid(point, centroids_group);
            // Add the point to the point ArrayList
            updated_points.add(point);
        }
        // Create the new point group and return it.
        // TODO create uid properly
        String uid = "uid";
        return new PointGroup(updated_points, uid);
    }

    /** Performs the addition necessary to calculate the average for the centroid calculation. Should be run by each
     * client with their list of owned points.
     *
     * @deprecated This is now done by PointGroups.
     * @param list_centroid_points A list containing the lists of points that belong to each centroid.
     *                             IMPORTANT: must contain a list for each centroid, even if there is no points in a
     *                             centroid!
     * @return A list of doubles arrays, where each item represents a centroid. The doubles will be structured as:
     *         x addition, y addition, number of points added. Will give a double[] of 0, 0, 0 if the centroid list was
     *         empty.
     */
    @Deprecated
    public static ArrayList<double[]> updateCentroidsAddition(ArrayList<ArrayList<Point>> list_centroid_points) {
        // The array list that will keep track of progress during the function to be returned later
        ArrayList<double[]> additions = new ArrayList<>();
        // Create a double[] for each centroid, to ensure they each have a double array when returned
        for (int i = 0; i < list_centroid_points.size(); i++) {
            additions.add(new double[] {0, 0, 0});
        }

        // The values that will be used to keep track of the centroid additions
        double[] doubles_centroid_additions = {0, 0, 0};
        // The current centroid point list
        ArrayList<Point> points_current_centroid;
        // Loop through each centroid list
        for (int c = 0; c < list_centroid_points.size(); c++) {
            // Set current centroid list
            points_current_centroid = list_centroid_points.get(c);
            // The current point being iterated in the next loop
            Point current_point;
            // Loop through all the points in the list
            for (int p = 0; p < points_current_centroid.size(); p++) {
                // The current point
                current_point = points_current_centroid.get(p);
                // Add the x value
                doubles_centroid_additions[0] += current_point.getX();
                // Add the y value
                doubles_centroid_additions[1] += current_point.getY();
            }
            // Set the total number of additions completed to the size of the centroid's list
            doubles_centroid_additions[2] = points_current_centroid.size();

            // Set the doubles to the correct position in the additions list
            additions.set(c, doubles_centroid_additions);
            // Reset the doubles back to 0 for the next centroid loop
            doubles_centroid_additions = new double[]{0, 0, 0};
        }
        // Return the list of doubles used in the addition
        return additions;
    }

    /** Performs the average component required to recalculate the centroid locations. Should be run on the main server.
     *
     * @deprecated This is now handled in PointGroup
     * @param client_additions An array list of array lists generated by the updateCentroidsAddition by each client
     * @return An array list of the updated centroids.
     */
    @Deprecated
    public static ArrayList<Point> updateCentroidsAverage(ArrayList<ArrayList<double[]>> client_additions) {
        // Temporary list that holds the total values before dividing and calculating the new centroids
        ArrayList<double[]> temp_total_centroid_additions = new ArrayList<>();
        // Initialize the temporary list to contain double arrays with values of 0 for all centroids
        //TODO find a way to initialize this once (without duplication)
        for (int i = 0; i < client_additions.get(0).size(); i++) {
            temp_total_centroid_additions.add(new double[] {0, 0, 0});
        }
        // Combines the individual client lists into one list
        for (int cl = 0; cl < client_additions.size(); cl++) {
            // The current client being handled in the loop
            ArrayList<double[]> current_client = client_additions.get(cl);
            // Loop through the centroids for the current client
            for (int ce = 0; ce < current_client.size(); ce++) {
                // Add together the temp total and the current client's values
                double[] current_additions = current_client.get(ce);
                double[] total_additions = temp_total_centroid_additions.get(ce);
                temp_total_centroid_additions.set(ce, new double[] {
                        current_additions[0] + total_additions[0],
                        current_additions[1] + total_additions[1],
                        current_additions[2] + total_additions[2],
                    });
            }
        }
        // Create the new centroid points by performing the division portion of averaging
        ArrayList<Point> updated_centroids = new ArrayList<>();
        // Loop through all the centroid double arrays to calculate the new centroid point
        for (int c = 0; c < temp_total_centroid_additions.size(); c++) {
            // Current centroid double array
            double[] current_centroid = temp_total_centroid_additions.get(c);
            /* Create the new point by calculating the average from the given double arrays with format x additions,
            y additions, total additions */
            Point centroid = new Point(current_centroid[0] / current_centroid[2], current_centroid[1] / current_centroid[2]);
            // Add the centroid to the list of updated centroids
            updated_centroids.add(centroid);
        }
        // Return the updated centroid list
        return updated_centroids;
    }
}
