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

/** A worker client that helps calculate the first phase of the k-means calculation.
 *
 */
public class Client {
    public int weight = 1;
    private static ArrayList<Point> AssignedPoints = new ArrayList<>();
    private static ArrayList<Point> Centroids = new ArrayList<>();

    private static int control_transmit_port = 10010;
    private static int control_return_port = 10011;
    private static int collector_port = 10001;
    private static String collector_ip = "localhost";

    static String SERVERIP = "127.0.0.1";
    private static int client_uid;
    private static ZMQ.Socket client_sub;
    private static ZMQ.Socket client_req;
    private static ZMQ.Socket client_taskboard;
    private static ZMQ.Socket collector_upload;
    private static ZMQ.Context zmq_context;

    /** Runs the client.
     *
     * @param args Arguments given at the command line.
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        PointGroup recieved_centroids = null;
        PointGroup MyPoint_Group;
        zmq_context = ZMQ.context(4);
        connect_to_socket(SERVERIP);
        String task_board_IP;
        byte[] server_msg;

        while (true) {
            server_msg = client_sub.recv();
            String uid_rep_string = new String(server_msg, ZMQ.CHARSET);
            String[] reply_msg = uid_rep_string.split(" ");

            if (reply_msg[1].equals("PHASEONEREADY")) {
                //delay self to allow Coordinator to setup
                Thread.sleep(1000);
                task_board_IP = reply_msg[2];
                String task_board_port = reply_msg[3];
                client_taskboard = zmq_context.socket(SocketType.PULL);
                client_taskboard.connect("tcp://" + task_board_IP + ":" + task_board_port);
                client_taskboard.setBacklog(3);

                collector_upload = zmq_context.socket(SocketType.PUSH);
                collector_upload.connect("tcp://" + collector_ip + ":" + collector_port);

                break;
            }
        }
        server_msg = null;
        System.out.println("Requesting Centroids");

        String centroid_request = client_uid + " CENTROIDS_UPDATE";
        client_req.send(centroid_request.getBytes(ZMQ.CHARSET), 0);
        byte[] centroid_raw_bytes = client_req.recv();
        System.out.println("Received Centroids!");
        ByteArrayInputStream Input_Byte_Converter = new ByteArrayInputStream(centroid_raw_bytes);
        try {
            ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
            recieved_centroids = (PointGroup) Byte_Translator.readObject();
        } catch (Exception e) {
            System.err.println("Error Parsing Centroids");
        }

        while (true) {
            server_msg = client_taskboard.recv(ZMQ.DONTWAIT);
            try {
                Input_Byte_Converter = new ByteArrayInputStream(server_msg);
                ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
                MyPoint_Group = (PointGroup) Byte_Translator.readObject();
                System.out.print("Recieved Points from Coordinator...");
                MyPoint_Group = KMeans.processPointGroup(MyPoint_Group, recieved_centroids);
                System.out.println("Finished Processing Points");

                //Sends to Collector (Phase 2)
                ByteArrayOutputStream collector_byte_stream = new ByteArrayOutputStream();
                ObjectOutput collector_converter = new ObjectOutputStream(collector_byte_stream);
                collector_converter.writeObject(MyPoint_Group);
                collector_converter.flush();
                byte[] uploaded_points = collector_byte_stream.toByteArray();
                //uploads the points to the collector, doesn't stop since the collector may have a queue
                collector_upload.send(uploaded_points, ZMQ.DONTWAIT); //TODO: Make sure group gets sent eventually

            } catch (Exception e) {
                //System.err.println("Error Parsing Point Given");
            }

            try {
                //Check for messages from control
                server_msg = client_sub.recv(ZMQ.DONTWAIT);
                if (server_msg != null) {
                    String message = new String(server_msg, ZMQ.CHARSET);
                    String[] msg_parts = message.split(" ");
                    //checks if theres an update to the centroids on the Broadcast Frequency
                    if (msg_parts[1].equals("CENTROIDS")) { //TODO: make sure centroids are sent as expected
                        Input_Byte_Converter = new ByteArrayInputStream(server_msg);
                        ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
                        recieved_centroids = (PointGroup) Byte_Translator.readObject();
                    } else if (msg_parts[1].equals("DONE")) {
                        //the iteration is complete, wait for the next one
                        //break;
                    } else if (msg_parts[1].equals("CENTROID_UPDATE")){

                        System.out.println("Requesting Centroid Update");

                        centroid_request = client_uid + " CENTROIDS_UPDATE";
                        client_req.send(centroid_request.getBytes(ZMQ.CHARSET), 0);
                        centroid_raw_bytes = client_req.recv();
                        System.out.println("Received Centroids!");
                        Input_Byte_Converter = new ByteArrayInputStream(centroid_raw_bytes);
                        try {
                            ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
                            recieved_centroids = (PointGroup) Byte_Translator.readObject();
                        } catch (Exception e) {
                            System.err.println("Error Parsing Centroids");
                        }

                    }
                }
            } catch (Exception e){
                System.out.println("Error Parsing Broadcast Message");
            }
        }
        //client_taskboard.close();
        //System.out.println("Completed Iteration");
    }

    /** Setup all the socket connections for when the worker client connects to the
     * {@link server.Coordinator network coordinator}
     *
     * @param ip The ip of the {@link server.Coordinator coordinator} that the worker is connecting to.
     */
    public static void connect_to_socket(String ip) {
        try {
            client_sub = zmq_context.socket(SocketType.SUB);
            client_sub.connect("tcp://" + ip + ":" + control_transmit_port);
            // setting up the broadcast channel
            client_sub.subscribe("BROADCAST");
            // setting up the client upload socket
            client_sub.subscribe(Integer.toString(client_uid));

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