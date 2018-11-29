package client;

import org.zeromq.ZContext;
import shared.Point;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import shared.KMeans;

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
    private static ZMQ.Socket centroid_board;
    private static ZMQ.Context zmq_context;

    public static void main(String[] args) throws InterruptedException {
        PointGroup recieved_centroids = null;
        PointGroup MyPoint_Group;
        zmq_context = ZMQ.context(3);
        connect_to_socket(SERVERIP);
        String task_board_IP;
        byte[] server_msg;

        while (true) {
            server_msg = client_sub.recv();
            String uid_rep_string = new String(server_msg, ZMQ.CHARSET);
            String[] reply_msg = uid_rep_string.split(" ");

            if (reply_msg[1].equals("PHASEONEREADY")) {
                task_board_IP = reply_msg[2];
                String task_board_port = reply_msg[3];
                client_taskboard = zmq_context.socket(SocketType.PULL);
                client_taskboard.connect("tcp://" + task_board_IP + ":" + task_board_port);
                client_taskboard.setBacklog(3);
                break;
            }
        }

        //Gets the Port for the Centroids on the task board's IP
        server_msg = client_sub.recv(ZMQ.DONTWAIT);
        if (server_msg != null) {
            String message = new String(server_msg, ZMQ.CHARSET);
            String[] msg_parts = message.split(" ");
            if (msg_parts[1].equals("CENTROIDSPORT")) {
                String centroid_port = msg_parts[2];
                centroid_board = zmq_context.socket(SocketType.SUB);
                centroid_board.connect("tcp://" + task_board_IP + ":" + centroid_port);
                System.out.println("Connected To Centroid Update Socket");
            }
        }

        //Loops requesting for centroids until they are posted to the centroids board broadcast
        while (recieved_centroids == null) {
            server_msg = centroid_board.recv(ZMQ.DONTWAIT);
            if (server_msg == null) {
                //Request for a centroid update broadcast
                String centroid_request = client_uid + " CENTROIDS_UPDATE";
                System.out.println("Requesting Centroids");
                client_req.send(centroid_request.getBytes(ZMQ.CHARSET), 0);
                recieved_centroids = convert_to_PointGroup(centroid_board.recv());
                //sleep to avoid spamming requests
                Thread.sleep(100);
            } else{
                System.out.println("Received Centroids");
                recieved_centroids = convert_to_PointGroup(server_msg);
            }
        }

        while (true) {
            server_msg = client_taskboard.recv();
            System.out.println("Recieved Points from Coordinator");
            try {
                MyPoint_Group = convert_to_PointGroup(server_msg);
                MyPoint_Group = KMeans.processPointGroup(MyPoint_Group, recieved_centroids);
                //TODO Send to Phase 2

                //Check for messages from control
                server_msg = client_sub.recv(ZMQ.DONTWAIT);
                if (server_msg != null) {
                    String message = new String(server_msg, ZMQ.CHARSET);
                    String[] msg_parts = message.split(" ");
                    //checks if theres an update to the centroids on the Broadcast Frequency
                    if (msg_parts[1].equals("CENTROIDS")) {
                        recieved_centroids = convert_to_PointGroup(server_msg);
                    } else if (msg_parts[1].equals("DONE")){
                        //TODO determine what to do when the iteration is complete
                        //the iteration is complete
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(-300);
            } finally {
                client_taskboard.close();
            }
        }
        System.out.println("Completed Iteration");
    }


    public static PointGroup convert_to_PointGroup(byte[] msg) {
        try {
            ByteArrayInputStream Input_Byte_Converter = new ByteArrayInputStream(msg);
            ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
            return (PointGroup) Byte_Translator.readObject();
        } catch (Exception e) {
            System.err.print("Unable to Convert to PointGroup");
            return null;
        } finally {
            return null;
        }
    }

    public static void connect_to_socket(String ip) {
        try {
            client_req = zmq_context.socket(SocketType.REQ);
            client_req.connect("tcp://" + ip + ":" + control_return_port);
            String tmp_msg = "-1 JOIN";
            client_req.send(tmp_msg.getBytes(ZMQ.CHARSET), 0);
            byte[] uid_rep = client_req.recv();
            System.out.println("Connecting to Coordinator");
            String uid_rep_string = new String(uid_rep, ZMQ.CHARSET);
            String[] reply_msg = uid_rep_string.split(" ");
            if (reply_msg[1].equals("GOOD")) {
                client_uid = Integer.parseInt(reply_msg[0]);
            }
            System.out.println("Assigned client ID " + client_uid);

            client_sub = zmq_context.socket(SocketType.SUB);
            client_sub.connect("tcp://" + ip + ":" + control_transmit_port);
            // setting up the broadcast channel
            client_sub.subscribe("BROADCAST");
            // setting up the client upload socket
            client_sub.subscribe(Integer.toString(client_uid));


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-200);
        }
    }

    public static void send_update(ArrayList<Double> sum_per_centroid, ArrayList<Integer> points_per_centroid) {
        //TODO update the sums and number of points per centroid to the server somehow
    }

    public static void ReceiveCentroids(ArrayList<Point> updated_centroids) {
        Centroids = updated_centroids;
    }

    //REDUNDANT METHOD
    public static void ReceivePoints(ArrayList<Point> GivenPoints) {
        //Adds all points sent from the server to the client's AssignedPoints list
        AssignedPoints.addAll(GivenPoints);
    }
}