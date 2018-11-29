package client;

import org.zeromq.ZContext;
import shared.Point;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;

public class Client {
    public int weight = 1;
    private static ArrayList<Point> AssignedPoints = new ArrayList<>();
    private static ArrayList<Point> Centroids = new ArrayList<>();

    private static int control_transmit_port = 10010;
    private static int control_return_port = 10011;

    static String SERVERIP = "127.0.0.1";
    private static int client_uid;
    static int PORT = 5555;
    private static ZMQ.Socket client_sub;
    private static ZMQ.Socket client_req;
    private static ZMQ.Socket client_taskboard;
    private static ZMQ.Context zmq_context;

    public static void main(String[] args) throws InterruptedException {
        zmq_context = ZMQ.context(3);
        connect_to_socket(SERVERIP);

        while(true) {
            byte[] server_msg = client_sub.recv();
            String uid_rep_string = new String(server_msg, ZMQ.CHARSET);
            String[] reply_msg = uid_rep_string.split(" ");
            if (reply_msg[1].equals("PHASEONEREADY")) {
                String task_board_IP = reply_msg[2];
                String task_board_port = reply_msg[3];
                client_taskboard = zmq_context.socket(SocketType.PULL);
                client_taskboard.connect("tcp://" + task_board_IP + ":" + task_board_port);
                server_msg = client_taskboard.recv();
                System.out.println("Recieved Points from Coordinator");
                try {
                    ByteArrayInputStream Input_Byte_Converter = new ByteArrayInputStream(server_msg);
                    ObjectInputStream is = new ObjectInputStream(Input_Byte_Converter);
                    PointGroup MyPoint_Group = (PointGroup) is.readObject();
                    System.out.println("Break Here");
                    ArrayList<Point> MyPoints = MyPoint_Group.getPoints();
                    //TODO pass points into K-Means Method provided by Bradman
                    //RETURNS ARRAY LIST OF POINTS BACK
                    //Create a new point group or reuse them (clear it if i do)
                    //PointGroup.addPointsToList(Arraylist Thing)
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-300);
                } finally {
                    client_taskboard.close();
                }
            }
            System.out.println("Break Here");
        }
    }

    public static void connect_to_socket(String ip){
        try {
            client_req = zmq_context.socket(SocketType.REQ);
            client_req.connect("tcp://" + ip + ":" + control_return_port);
            String tmp_msg = "-1 JOIN";
            client_req.send(tmp_msg.getBytes(ZMQ.CHARSET), 0);
            byte[] uid_rep = client_req.recv();
            System.out.println("Connecting to Coordinator");
            String uid_rep_string = new String(uid_rep, ZMQ.CHARSET);
            String[] reply_msg = uid_rep_string.split(" ");
            if(reply_msg[1].equals("GOOD")) {
                client_uid = Integer.parseInt(reply_msg[0]);
            }
            System.out.println("Assigned client ID " + client_uid);

            client_sub = zmq_context.socket(SocketType.SUB);
            client_sub.connect("tcp://" + ip + ":" + control_transmit_port);
            // setting up the broadcast channel
            client_sub.subscribe("BROADCAST");
            // setting up the client upload socket
            client_sub.subscribe(Integer.toString(client_uid));



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