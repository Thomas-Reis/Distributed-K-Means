package client;

import shared.Point;

import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    public int weight = 1;
    private static ArrayList<Point> AssignedPoints = new ArrayList<>();
    private static ArrayList<Point> Centroids = new ArrayList<>();
    static String SERVERIP = "127.0.0.1";
    static int PORT = 5555;
    private static Socket client;
    //The keyboard input
    private static DataInputStream in;
    //The read line for the socket
    private static DataInputStream r;
    //The write line for the socket
    private static PrintStream w;
    private static ObjectOutputStream Obj_w;
    private static ObjectInputStream Obj_r;

    public static void main(String[] args) throws InterruptedException {
        //OLD CODE
        connect_to_socket(SERVERIP, PORT);

        try {
            int num_points = Integer.parseInt(r.readLine());
            System.out.println(num_points);
            for (int i=0;i<num_points;i++){
                Point tmp_point = (Point)Obj_r.readObject();
                AssignedPoints.add(tmp_point);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.exit(-100);
        }

        ArrayList<ArrayList<Point>> client1 = new ArrayList<>();
        ArrayList<ArrayList<Point>> client2 = new ArrayList<>();
        ArrayList<ArrayList<Point>> client3 = new ArrayList<>();
        //NEW CODE
        while(true){
            while(AssignedPoints.size() > 0){
                AssignedPoints.clear();
                System.out.println("Received Points and Cleared Them");
            }
            Thread.sleep(20);
        }
    }

    public static void connect_to_socket(String ip, int port){
        try {
            client = new Socket(ip, port);
            r=new DataInputStream(client.getInputStream());
            w=new PrintStream(client.getOutputStream());
            in=new DataInputStream(System.in);
            Obj_w = new ObjectOutputStream(client.getOutputStream());
            Obj_r = new ObjectInputStream(client.getInputStream());
        } catch (Exception e){
            e.printStackTrace();
            System.exit(-200);
        }
    }

    public static void send_update(ArrayList<Double> sum_per_centroid, ArrayList<Integer> points_per_centroid){
        //TODO update the sums and number of points per centroid to the server somehow
    }

    public static void ReceiveCentroids(ArrayList<Point> updated_centroids){
        Centroids = updated_centroids;
    }

    //REDUNDANT METHOD
    public static void ReceivePoints(ArrayList<Point> GivenPoints){
        //Adds all points sent from the server to the client's AssignedPoints list
        AssignedPoints.addAll(GivenPoints);
    }
}