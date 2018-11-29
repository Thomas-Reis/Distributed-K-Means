package server;

import client.Client;
import server.Listener;
import shared.ClientObject;
import shared.Point;

import java.util.ArrayList;
import java.util.Observable;

@Deprecated
public class Server extends Observable {
    private static  int X_MAXIMUM = 100;
    private static int X_MINIMUM = -100;
    private static int Y_MAXIMUM = 100;
    private static int Y_MINIMUM = -100;
    private static int POINTS_PER_HANDOUT = 20;

    public static ArrayList<ClientObject> client_list = new ArrayList<>();

    public static void main(String[] args){
        //TODO make a new thread to accept clients
        //TODO Remember to put all Clients that connect into client_list

        Listener client_listener = new Listener();

        while (client_list.size() < 1) {
            System.out.println(client_list);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
                break;
            }
        }

        //LOOP START
        //TODO select an "origin" client from the pool
        //TODO receive database info/queries to run
        ArrayList<Point> TotalPoints = new ArrayList<Point>();

        //Generates Random Points for Now
        for (int i=0; i < 100; i++) {
            TotalPoints.add(TestPoints());
        }
        //BEFORE ITERATION BEGINS

        //A point tracker to keep track of where the loop left off on assigning points
        int Point_Tracker = 0;
        int points_to_send = TotalPoints.size()/POINTS_PER_HANDOUT * client_list.size();
        //TODO assign rows to clients
        //This method may not need the TotalPoints array depending on how we query for the points
        Point_Tracker = AssignPoints(points_to_send, Point_Tracker, client_list, TotalPoints);
        //DURING ITERATION
        //TODO perform load balancing here
        //this means:
        //TODO check for client's requests for more data and assign them accordingly


        //AFTER ITERATION
        //TODO recalculate centroids using client.KMeans updateCentroidsAverage Method
        //TODO check if finishing conditions are met

        //AFTER ALL DATA IS PROCESSED
        //TODO return results to client somehow
        //LOOP BACK
    }

    //this method should only be ran after all points and their centroids have been returned
    private static ArrayList<Double> RecalculateCentroids(ArrayList<Double> sum_per_centroid, ArrayList<Integer> points_per_centroid){
        ArrayList<Double> Temp = new ArrayList<>();
        int k = sum_per_centroid.size();
        for (int i=0;i<k;i++) {
            Temp.add((sum_per_centroid.get(i) / points_per_centroid.get(i)));
        }
        return Temp;
    }

    private static int AssignPoints(int point_limit, int Point_Tracker, ArrayList<ClientObject> client_list, ArrayList<Point> TotalPoints){
        //the number of points to allocate this run of handouts
        int n = point_limit;
        ArrayList<Point> temp_assign_array = new ArrayList<>();

        //client_list is a list of all clients currently connected
        for (ClientObject client : client_list){
            //a formula for calculating how many points should go to each client
            //TODO change this formula quite a bit and add a client weight
            int client_points_amount = n/client_list.size();

            //to avoid index out of bounds errors we use this check
            if (Point_Tracker + client_points_amount > n){
                //assign all points remaining to the current client
                client_points_amount = n - Point_Tracker;
            }
            //Tells the client how many points it will recieve before sending that many points
            client.getOut().println(client_points_amount);
            for(int i=Point_Tracker;i < Point_Tracker + client_points_amount; i++) {
                //for now this simply allocates the points locally to each client
                //TODO find a way to Mark points as belonging to certain clients before allocation
                try {
                    //Sends the current point object to the client
                    client.getObj_Out().writeObject(TotalPoints.get(i));
                } catch (Exception e){
                    System.out.println("Unable to assign all points to client");
                    //TODO find a way to UnMark points as well
                    //unassigns the points from the point tracker
                    Point_Tracker -= client_points_amount;
                }
            }
            Point_Tracker += client_points_amount;
        }
        return Point_Tracker;
    }

    public static Point TestPoints(){
        double random_X = (Math.random() * ((X_MAXIMUM - X_MINIMUM) + 1)) + X_MINIMUM;
        double random_Y = (Math.random() * ((Y_MAXIMUM - Y_MINIMUM) + 1)) + Y_MINIMUM;
        Point GenPoint = new Point(random_X, random_Y);
        return GenPoint;
    }
}
