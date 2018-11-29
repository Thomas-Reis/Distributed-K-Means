package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class PhaseTwo implements Runnable {
    private DatabaseHelper db;

    private String uid;
    private int expected_points = Integer.MAX_VALUE;
    private int points_received = 0;

    private ZMQ.Socket control_socket;
    private ZMQ.Socket control_return;
    private ZMQ.Socket task_receive_socket;

    private HashMap<String, PointGroup> point_groups_received = new HashMap<>();

    //PORTS USED:
    //Task_publish = 10000
    //Task_return = 10001
    //Control_publish = 10010
    //Control_return = 10011


    PhaseTwo(ZMQ.Context zmq_context, DatabaseHelper db, String uid) {
        this.uid = uid;

        this.db = db;

        //Setup the transmission socket
        this.task_receive_socket = zmq_context.socket(SocketType.PULL);
        this.task_receive_socket.setBacklog(3); //Allow only 3 messages on local queue
        this.task_receive_socket.bind("tcp://*:10001");

        //Setup the control downlink
        this.control_socket = zmq_context.socket(SocketType.SUB);
        this.control_socket.connect("tcp://localhost:10010");
        this.control_socket.subscribe("BROADCAST");
        this.control_socket.subscribe(this.uid);

        //Setup the control uplink
        this.control_return = zmq_context.socket(SocketType.REQ);
        this.control_return.connect("tcp://localhost:10011");

    }


    @Override
    public void run() {

        //Let the Coordinator know we've started
        this.control_return.send((this.uid +" START").getBytes(ZMQ.CHARSET));

        int iteration = 1;
        // TODO iteration number has been hardcoded to 10
        while (iteration < 10) {
            this.control_return.send((this.uid + " ITERATION " + iteration).getBytes(ZMQ.CHARSET));

            //Check for messages from control
            byte[] control = this.control_socket.recv(ZMQ.DONTWAIT);
            if (control != null) {
                String message =  new String(control, ZMQ.CHARSET);
                String[] msg_parts = message.split(" ");

                //TODO: Process the message

                if (msg_parts[1].equals("COUNT"))
                { this.expected_points = Integer.parseInt(msg_parts[2]); }

            }

            //Check for clusters from the workers
            byte[] cluster_raw = this.task_receive_socket.recv(ZMQ.DONTWAIT);
            if (cluster_raw != null) {

                PointGroup cluster;

                //Convert from bytes
                ByteArrayInputStream byte_stream = new ByteArrayInputStream(cluster_raw);
                try (ObjectInput converter = new ObjectInputStream(byte_stream)) {
                    cluster = (PointGroup) converter.readObject();
                } catch (IOException | ClassNotFoundException ex) { cluster = null; }

                //If cluster converted properly
                if (cluster != null) {
                    //TODO: Process the cluster
                    // TODO double check for tired Bradon
                    // Check if the uid has been received already
                    if (point_groups_received.containsKey(cluster.getUid())) {
                        // Get the value from the hashed map
                        PointGroup old_group = point_groups_received.get(cluster.getUid());
                        // Append the old group's points into the new cluster
                        cluster.addPointsToList(old_group.getPoints());
                        point_groups_received.replace(cluster.getUid(), cluster);
                    }
                    // Put the cluster group into the hash map if has already been received
                    else {
                        point_groups_received.put(cluster.getUid(), cluster);
                    }

                    //Update the Coordinator with the id score
                    this.control_return.send(this.uid + " SCORE " + cluster.getProcessedBy() + " " + 1);

                    //Increment the number of points received
                    this.points_received += cluster.getPoints().size();
                }
            }

            // Check if the number of points received is the expected number
            if (this.points_received >= this.expected_points) {
                // TODO perform the kmeans division on the set
                iteration++;
                // Check to see if return type has been met
                // TODO for now this will be simply if the number of iterations needed has been met. HARDCODED TO 10!
                if (iteration >= 10) {
                    // TODO Perform return type here
                }
            }
        }

        //Clean up our mess
        this.task_receive_socket.close();
        this.control_socket.close();

        //Let the Coordinator know we've finished
        this.control_return.send((this.uid + " DONE").getBytes(ZMQ.CHARSET));

        //Close the coordinator socket & die
        this.control_return.close();
        return;
    }
}
