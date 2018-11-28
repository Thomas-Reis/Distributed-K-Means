import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ArrayList;

public class DatabaseHelper {
    /** Used in specifying the type of database connection to make. Currently, only mysql is supported.
     *
     */
    public enum DatabaseType {
        mysql
    }

    private String username;
    private String password;
    private String server;
    private int port_number;
    private String db_name;
    private DatabaseType db_type;

    /** Creates a new database object
     *
     * @param username The username to use to connect to the database.
     * @param password The password to use to connect to the database.
     * @param server The server hosting the database.
     * @param port_number The port number used to connect to the databsae.
     * @param connection_type The type of database.
     */
    public DatabaseHelper(String username, String password, String server, int port_number, String db_name,
                          DatabaseType db_type) {
        this.username = username;
        this.password = password;
        this.server = server;
        this.port_number = port_number;
        this.db_name = db_name;
        this.db_type = db_type;
    }

    /** Gets a connection to the database
     *
     * @return The database connection.
     * @throws SQLException Thrown if there is an error making the connection.
     */
    public Connection getConnection() throws SQLException {
        // The connection object
        Connection conn = null;
        // The properties needed to connect to the database
        Properties properties = new Properties();
        properties.put("user", this.username);
        properties.put("password", this.password);

        // Check which type of db the user is connecting with
        if (this.db_type.equals(DatabaseType.mysql)) {
            conn = DriverManager.getConnection(
                    "jdbc:mysql://"
                            + this.server + ":"
                            + Integer.toString(this.port_number) + "/",
                    properties
            );
        }
        // Return the connection
        return conn;
    }

    public ArrayList<Point> getPoints(int start_row, int num_points) {
        try {
            // Get the connection
            Connection conn = getConnection();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
