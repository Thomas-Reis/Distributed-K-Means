import java.util.ArrayList;
import java.util.Observable;


public class Server extends Observable {
    public static int X_MAXIMUM = 100;
    public static int X_MINIMUM = -100;
    public static int Y_MAXIMUM = 100;
    public static int Y_MINIMUM = -100;

    public static void main(String[] args){
        //TODO make a new thread to accept clients
        ArrayList<Client> client_list = new ArrayList<Client>();

        //LOOP START
        //TODO select an "origin" client from the pool
        //TODO receive database info/queries to run
        ArrayList<Point> TotalPoints = new ArrayList<Point>();

        //Generates Random Points for Now
        for (int i=0; i < 30; i++) {
            TotalPoints.add(TestPoints());
        }

        //set n to the total number of rows
        int n = 1;
        ArrayList<Point> client_point_array = new ArrayList<Point>();

        //BEFORE ITERATION BEGINS

        //A point tracker to keep track of where the loop left off on assigning points
        int Point_Tracker = 0;
        //TODO assign rows to clients
        //client_list is a list of all clients currently connected
        for (Client client : client_list){
            //a formula for calculating how many points should go to each client
            //TODO change this formula quite a bit
            int client_points_amount = n/client_list.size() * client.weight;

            //to avoid index out of bounds errors we use this check
            if (Point_Tracker + client_points_amount > TotalPoints.size()){
                //assign all points remaining to the current client
                client_points_amount = TotalPoints.size() - Point_Tracker;
            }
            for(int i=Point_Tracker;i < Point_Tracker + client_points_amount; i++) {
                //for now this simply allocates the points locally to each client
                client_point_array.add(TotalPoints.get(i));
                //TODO find a way to transfer these points over to the client rather than local allocation
            }
            Point_Tracker += client_points_amount;
        }

        //DURING ITERATION
        //TODO perform load balancing here
        //this means:
        //TODO check for client's requests for more data and assign them accordingly


        //AFTER ITERATION
        //TODO recalculate centroids using KMeans updateCentroidsAverage Method
        //TODO check if finishing conditions are met

        //AFTER ALL DATA IS PROCESSED
        //TODO return results to client somehow
        //LOOP BACK
    }

    public static Point TestPoints(){
        double random_X = (Math.random() * ((X_MAXIMUM - X_MINIMUM) + 1)) + X_MINIMUM;
        double random_Y = (Math.random() * ((Y_MAXIMUM - Y_MINIMUM) + 1)) + Y_MINIMUM;
        Point GenPoint = new Point(random_X, random_Y);
        return GenPoint;
    }
}
