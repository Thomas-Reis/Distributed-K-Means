package server;

import shared.Point;

import java.io.Serializable;
import java.sql.*;
import java.util.Properties;
import java.util.ArrayList;

public class DatabaseHelper implements Serializable {
    /** Used in specifying the type of database connection to make. Currently, only MYSQL is supported.
     *
     */
    public enum DatabaseType {
        MYSQL
    }

    // The offset to retrieve values from in the database.
    private int offset;

    // Database information
    private String username;
    private String password;
    private String server;
    private int port_number;
    private String db_name;
    private DatabaseType db_type;

    // The view/table information for the points set
    private String table_points_name;
    private String table_points_id;
    private String table_points_x;
    private String table_points_y;
    private String table_points_last_seen;
    private String table_points_owner;
    // The view/table information for the centroids
    private String table_centroids_name;
    private String table_centroids_id;
    private String table_centroids_number;
    private String table_centroids_iteration;
    private String table_centroids_x;
    private String table_centroids_y;

    /** Creates a new database object
     *
     * @param username The username to use to connect to the database.
     * @param password The password to use to connect to the database.
     * @param server The server url.
     * @param port_number The port to connect through.
     * @param db_name The name of the database.
     * @param db_type The type of database.
     * @param table_points_name The table/view name for the points table/view.
     * @param table_points_id The id column name of the points table/view.
     * @param table_points_x The x location column name of the points table/view.
     * @param tables_points_y The y location column name of the points table/view.
     * @param table_points_last_seen The column that holds the last iteration the point was seen.
     * @param table_points_owner The column that holds the centroid owner.
     * @param table_centroids_name The table name for the centroid table.
     * @param table_centroids_id The id column name of the centroid table.
     * @param table_centroids_number The column name for the centroid number in the centroid table.
     * @param table_centroids_iteration The iteration column name of the centroid table.
     * @param table_centroids_x The x location column name of the centroid table.
     * @param table_centroids_y The y location column name of the centroid table.
     */
    public DatabaseHelper(String username, String password, String server, int port_number, String db_name,
                          DatabaseType db_type, String table_points_name, String table_points_id, String table_points_x,
                          String tables_points_y, String table_points_last_seen, String table_points_owner,
                          String table_centroids_name, String table_centroids_id, String table_centroids_number,
                          String table_centroids_iteration, String table_centroids_x, String table_centroids_y) {
        this.offset = 0;
        this.username = username;
        this.password = password;
        this.server = server;
        this.port_number = port_number;
        this.db_name = db_name;
        this.db_type = db_type;
        this.table_points_name = table_points_name;
        this.table_points_id = table_points_id;
        this.table_points_x = table_points_x;
        this.table_points_y = tables_points_y;
        this.table_points_last_seen = table_points_last_seen;
        this.table_points_owner = table_points_owner;
        this.table_centroids_name = table_centroids_name;
        this.table_centroids_id = table_centroids_id;
        this.table_centroids_number = table_centroids_number;
        this.table_centroids_iteration = table_centroids_iteration;
        this.table_centroids_x = table_centroids_x;
        this.table_centroids_y = table_centroids_y;
    }

    /** Gets a connection to the database
     *
     * @return The database connection.
     * @throws SQLException Thrown if there is an error making the connection.
     */
    private Connection getConnection() throws SQLException {
        // The connection object
        Connection conn = null;
        // The properties needed to connect to the database
        Properties properties = new Properties();
        properties.put("user", this.username);
        properties.put("password", this.password);

        // Check which type of db the user is connecting with
        if (this.db_type.equals(DatabaseType.MYSQL)) {
            conn = DriverManager.getConnection(
                    "jdbc:MYSQL://"
                            + this.server + ":"
                            + Integer.toString(this.port_number) + "/"
                            + this.db_name,
                    properties
            );
        }
        // Return the connection
        return conn;
    }

    /** Gets a list of points from the database.
     *
     * @param num_points The number of points to grab.
     * @return An array list of points that were retrieved or null if an sql error occurred.
     */
    public ArrayList<Point> getPoints(int num_points) throws SQLException {
        // Create an array list of points to be returned
        ArrayList<Point> points = new ArrayList<>();
        Connection conn = null;
        try {
            // Get the connection
            conn = getConnection();
            if (db_type.equals(DatabaseType.MYSQL)) {
                // The sql to be done
                String sql = String.format("SELECT %s, %s, %s FROM %s ORDER BY %s LIMIT ? OFFSET ?;",
                        table_points_id, table_points_x, table_points_y, table_points_name, table_points_id);
                // Create the sql statement
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, num_points);
                statement.setInt(2, offset);
                // Performs the sql query
                ResultSet results = statement.executeQuery();
                results.beforeFirst();
                // Loop through the results
                while (results.next()) {
                    // Get the x and y values
                    int point_id = results.getInt(table_points_id);
                    float loc_x = results.getFloat(table_points_x);
                    float loc_y = results.getFloat(table_points_y);
                    // Create the point
                    Point point = new Point(loc_x, loc_y, point_id);
                    // Add the point to the array list
                    points.add(point);
                }
            }

            // Change the offset value based on the number of points that were retrieved.
            offset += points.size();


            // Return the points that were found

        } catch (SQLException e) {
            e.printStackTrace();
            points = null;
        } finally {
            if (conn != null)
            { conn.close(); }
        }
        return points;
    }

    /** Updates the database stating when a point has been seen in the given iteration.
     *
     * @param points_seen The list of points to mark as seen in the iteration.
     * @param iteration_number The iteration number that the point was seen in.
     * @return Whether or not the insertions all happened successfully.
     */
    public boolean updatePointsSeen(ArrayList<Point> points_seen, int iteration_number) throws SQLException {
        // The connection to the database
        Connection conn = null;
        boolean success;
        try {
            conn = getConnection();
            // The sql to update the point in the database
            String sql = String.format("UPDATE %s SET %s=?, %s=? WHERE %s=?",
                    table_points_name, table_points_last_seen, table_points_owner, table_points_id);
            // Loop through all given points
            for (Point point : points_seen) {
                // Prepare the statement
                PreparedStatement statement = conn.prepareStatement(sql);
                statement.setInt(1, iteration_number);
                statement.setInt(2, point.getCentroid_id());
                statement.setInt(3, point.getRow_id());
                // Execute the insert
                //statement.execute();
            }
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (conn != null)
            { conn.close(); }
        }
        return success;
    }

    /** Inserts the given centroids into the database with the given iteration number.
     *
     * @param centroids The list of centroids to insert, where their id is equal to their index + 1
     *                  (so index # starts at 1).
     * @param iteration The iteration of k-means that these centroids were calculated from.
     * @return Whether or not the points were successfully inserted.
     */
    public boolean insertCentroids(ArrayList<Point> centroids, int iteration) throws SQLException {
        // The connection to the database
        Connection conn = null;
        boolean success = true;
        try {
            // Get the connection
            conn = getConnection();
            if (db_type.equals(DatabaseType.MYSQL)) {
                // The sql to be done
                String sql = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?);",
                        table_centroids_name, table_centroids_number, table_centroids_iteration, table_centroids_x,
                        table_centroids_y);
                // Loop through all the centroids
                for (int i = 0; i < centroids.size(); i++) {
                    // The current centroid
                    Point current_centroid = centroids.get(i);
                    // Create the sql statement
                    PreparedStatement statement = conn.prepareStatement(sql);
                    statement.setInt(1, i + 1);
                    statement.setInt(2, iteration);
                    statement.setDouble(3, current_centroid.getX());
                    statement.setDouble(4, current_centroid.getY());
                    // Insert the point
                    statement.execute();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            success = false;
        } finally {
            if (conn != null)
            { conn.close(); }
        }
        return success;
    }

    /** Gets an ArrayList of points to use as starting centroids by randomly selecting k points from the database.
     *
     * @param k The number of centroids needed.
     * @return The ArrayList of points.
     */
    public ArrayList<Point> getStartingCentroids(int k) throws SQLException {
        // The connection to the database
        Connection conn = null;
        // The array list to return
        ArrayList<Point> centroids = new ArrayList<>();
        try {
            // Get the connection
            conn = getConnection();
            // The sql to be done
            String sql = String.format("SELECT %s, %s FROM %s ORDER BY RAND() LIMIT ?;",
                    table_points_x, table_points_y, table_points_name);
            // Prepare the sql statement
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, k);
            // Execute the query
            ResultSet results = statement.executeQuery();
            // Loop through the results
            results.beforeFirst();
            int id = 1;
            while (results.next()) {
                // Get the point data
                double x = results.getFloat(table_points_x);
                double y = results.getFloat(table_points_y);
                // Insert the point into the array list
                Point centroid = new Point(x, y);
                centroid.setCentroid_id(id++);
                centroids.add(centroid);
            }
            //return centroids;
        } catch (SQLException e) {
            e.printStackTrace();
            centroids = null;
        } finally {
            if (conn != null)
            { conn.close(); }
        }
        return centroids;
    }

    /** Reset the database for when a new iteration starts.
     *
     */
    public void reset() {
        this.offset = 0;
    }
}
