package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;
import shared.KMeans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class PhaseOne implements Runnable {

    //private ClientLink origin;
    private DatabaseHelper db;

    private String uid;
    private int clusters_sent = 0;
    private int K = 5;
    private int iteration_num = 1;
    private int clusterid = 0;

    private byte[] next_transmission;
    private boolean transmitted;
    private int redundant_sends_left;
    private int iter_num = 1;

    private ZMQ.Socket task_transmit_socket;
    private ZMQ.Socket control_socket;
    private ZMQ.Socket control_return;

    //How many points to include in a group
    private int group_size;

    //How many users to send the message to
    private int redundant_calculations;

    //Keep Centroids Local
    PointGroup iteration_centroids;

    //PORTS USED:
    //Task_publish = 10000
    //Task_return = 10001
    //Control_publish = 10010
    //Control_return = 10011

    public static void main(String[] args) {
        ZMQ.Context zmq_context = ZMQ.context(6);
        DatabaseHelper db = new DatabaseHelper("root", "", "localhost", 3306,
                "kmeans", DatabaseHelper.DatabaseType.MYSQL, "points", "id",
                "loc_x", "loc_y", "last_seen",
                "centroid","centroids", "id",
                "centroid_number","iteration", "loc_x",
                "loc_y");
        int ClusterSize = 20;
        int Redundant_Calcs = 3;
        PhaseOne init = new PhaseOne(zmq_context, db, "100", Redundant_Calcs, ClusterSize);
        init.run();
    }


    PhaseOne(ZMQ.Context zmq_context, DatabaseHelper db, String uid, int redundant_calculations, int group_size) {
        this.db = db;

        this.group_size = group_size;
        this.redundant_calculations = redundant_calculations;
        this.uid = uid;


        //Setup the transmission socket
        this.task_transmit_socket = zmq_context.socket(SocketType.PUSH);
        this.task_transmit_socket.bind("tcp://*:10000");
        this.task_transmit_socket.setBacklog(this.redundant_calculations);

        //Setup the control downlink
        this.control_socket = zmq_context.socket(SocketType.SUB);
        this.control_socket.connect("tcp://localhost:10010");
        this.control_socket.subscribe("BROADCAST");
        this.control_socket.subscribe(this.uid);

        //Setup the control uplink
        this.control_return = zmq_context.socket(SocketType.REQ);
        this.control_return.connect("tcp://localhost:10011");

        GenCentroids();
    }

    public void GenCentroids() {
        iteration_centroids = new PointGroup(db.getStartingCentroids(K), this.uid + " CENTROID " + iteration_num);

    }


    private void attemptTransmitPointGroup() {
        System.out.print("Outputting Points...");
        //this.transmitted = this.task_transmit_socket.send(this.next_transmission, ZMQ.DONTWAIT);
        this.transmitted = this.task_transmit_socket.send(this.next_transmission);
        if (this.transmitted) {
            this.redundant_sends_left--;
        }
        System.out.println("Complete!");
    }

    private void getNextGroup() throws IndexOutOfBoundsException {
        if (this.transmitted) {

            PointGroup nextCluster = new PointGroup(this.db.getPoints(this.group_size), Integer.toString(clusterid++));
            if (nextCluster.getPoints().size() == 0) {
                throw new IndexOutOfBoundsException(); // Out of data, stop the loop
            }
            byte[] message;

            //Convert the cluster to a byte array
            ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
            try (ObjectOutput converter = new ObjectOutputStream(byte_stream)) {
                converter.writeObject(nextCluster);
                converter.flush();
                message = byte_stream.toByteArray();
            } catch (IOException ex) {
                message = new byte[]{0};
            }

            //Make sure it converted properly
            if (message.length != 1) {

                System.out.println("Set the PointGroup");
                this.redundant_sends_left = this.redundant_calculations;
                this.next_transmission = message;

            }
        }
    }

    @Override
    public void run() {
        //Let the Coordinator know we've started
        this.control_return.send((this.uid + " START").getBytes(ZMQ.CHARSET));
        this.control_return.recv();
        byte[] msg_bytes;
        //Convert the Centroids to a byte array to transmit
        ByteArrayOutputStream centroid_byte_stream = new ByteArrayOutputStream();
        try (ObjectOutput centroid_converter = new ObjectOutputStream(centroid_byte_stream)) {
            centroid_converter.writeObject(iteration_centroids);
            centroid_converter.flush();
            msg_bytes = centroid_byte_stream.toByteArray();
        } catch (IOException ex) {
            msg_bytes = new byte[]{0};
        }
        if (msg_bytes.length != 1) {
            System.out.println("Sending Centroids");
            this.control_return.send(msg_bytes, ZMQ.DONTWAIT);
            this.control_return.recv();
        }
        this.transmitted = true;

        clusterid = 0;
        while (true) {

            //If we've completely sent the last PointGroup
            if (this.redundant_sends_left == 0) {
                //Get the next group, break if no data left
                try {
                    this.getNextGroup();
                } catch (IndexOutOfBoundsException ex) {
                    if (iter_num > 1) {
                        this.control_return.recv(ZMQ.DONTWAIT);
                    }
                    //All the points have been distributed for this iteration
                    this.control_return.send((this.uid + " DONE " + this.clusters_sent).getBytes(ZMQ.CHARSET));
                    //listens in on the control socket until the iteration is complete and its okay to redistribute
                    while(true){
                        byte[] broadcast_msg = control_socket.recv();
                        String message = new String(broadcast_msg, ZMQ.CHARSET);
                        String[] msg_parts = message.split(" ");
                        if(msg_parts[1].equals("CENTROID_UPDATE")){
                            break;
                        }
                    }
                    //Do things for next iteration
                    clusterid = 0;
                    this.transmitted = true;
                    this.redundant_sends_left = 0;
                    iter_num++;
                    this.db.reset();
                }
            }

            //If there's something to send
            if (this.redundant_sends_left != 0) {
                attemptTransmitPointGroup();
            }
        }


        /*
        //Clean up the sockets
        this.task_transmit_socket.close();
        this.control_socket.close();

        //Let the Coordinator know we've finished
        this.control_return.send((this.uid + " DONE " + this.clusters_sent).getBytes(ZMQ.CHARSET));

        //Close the coordinator socket & die
        this.control_return.close();
        return;
        */
    }
}



