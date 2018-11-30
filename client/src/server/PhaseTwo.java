package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PhaseTwo implements Runnable {
    private DatabaseHelper db;

    private String uid;
    private int expected_points = Integer.MAX_VALUE;
    private int points_received = 0;

    private int max_iterations;

    private ZMQ.Socket control_socket;
    private ZMQ.Socket control_return;
    private ZMQ.Socket task_receive_socket;

    // The hash maps that will store received point groups for redundancy checks
    private HashMap<String, PointGroup> point_groups_received = new HashMap<>();
    private HashMap<String, PointGroup> point_groups_received_collision = new HashMap<>();

    // The total Point Group used to hold the summations of the centroids
    private PointGroup total_point_group = new PointGroup(new ArrayList<>(), "TODO replace uid string");

    //PORTS USED:
    //Task_publish = 10000
    //Task_return = 10001
    //Control_publish = 10010
    //Control_return = 10011

    public static void main(String[] args){
        ZMQ.Context C = ZMQ.context(3);
        DatabaseHelper db_temp = new DatabaseHelper("root", "", "localhost", 3306,
                "kmeans", DatabaseHelper.DatabaseType.MYSQL, "points", "id",
                "loc_x", "loc_y", "last_seen", "centroids", "id",
                "centroid_number", "iteration", "loc_x",
                "loc_y");
        PhaseTwo init = new PhaseTwo(C,db_temp,"200", 10);
        init.run();
    }

    PhaseTwo(ZMQ.Context zmq_context, DatabaseHelper db, String uid, int iterations) {
        this.uid = uid;
        this.db = db;
        this.max_iterations = iterations;


        //Setup the transmission socket
        this.task_receive_socket = zmq_context.socket(SocketType.PULL);
        //this.task_receive_socket.setBacklog(3); //Allow only 3 messages on local queue
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
        //this.control_return.send((this.uid +" START").getBytes(ZMQ.CHARSET));

        // Loop through all iterations
        int iteration = 1;
        while (iteration <= max_iterations) {
            //this.control_return.send((this.uid + " ITERATION " + iteration).getBytes(ZMQ.CHARSET));

            /*
            //Check for messages from control
            byte[] control = this.control_socket.recv(ZMQ.DONTWAIT);
            if (control != null) {
                String message =  new String(control, ZMQ.CHARSET);
                String[] msg_parts = message.split(" ");

                //TODO: Process the message

                if (msg_parts[1].equals("COUNT"))
                { this.expected_points = Integer.parseInt(msg_parts[2]); }

            }
            */

            //Check for clusters from the workers
            byte[] cluster_raw = this.task_receive_socket.recv(ZMQ.DONTWAIT);
            if (cluster_raw != null) {

                // The received point group
                PointGroup new_point_group;
                //Convert from bytes
                ByteArrayInputStream byte_stream = new ByteArrayInputStream(cluster_raw);
                try (ObjectInput converter = new ObjectInputStream(byte_stream)) {
                    new_point_group = (PointGroup) converter.readObject();
                } catch (IOException | ClassNotFoundException ex) { new_point_group = null; }

                //If cluster converted properly
                if (new_point_group != null) {

                    // Check to see if the point is in the received point group for redundancy check
                    String received_id = new_point_group.getUid();
                    if (point_groups_received.containsKey(received_id)) {
                        // Holds if the received group passes redundancy checks
                        boolean redundancy_pass = false;
                        PointGroup old_group = point_groups_received.get(received_id);
                        // Check to see if the old group is equal to the received group
                        if (old_group.equals(new_point_group)) {
                            redundancy_pass = true;
                        }
                        else {
                            // Check if the point is in the collision map
                            if (point_groups_received_collision.containsKey(received_id)) {
                                old_group = point_groups_received_collision.get(received_id);
                                // Check to see if the old group is equal to the received group once again
                                if (old_group.equals(new_point_group)) {
                                    redundancy_pass = true;
                                }
                                else {
                                    // None of the values were found, so remove it from the hash map and do not process
                                    point_groups_received_collision.remove(received_id);
                                    point_groups_received.remove(received_id);
                                }
                            }
                            // If it wasn't, add it to the collision map
                            else {
                                point_groups_received_collision.put(received_id, new_point_group);
                            }
                        }
                        // If the redundancy pass is true, the point must now be processed
                        if (redundancy_pass) {
                            // Combine the point groups together
                            total_point_group.combinePointGroup(new_point_group);
                            // Update the database
                            db.updatePointsSeen(new_point_group.getPoints(), iteration);
                            // Increment the total number of points processed
                            points_received += new_point_group.getPoints().size();
                            // Clear the point list to free up memory
                            total_point_group.setPoints(new ArrayList<>());
                        }
                    }
                    // If it wasn't in the first hash map, it must be added to it so the redundancy can be checked.
                    else {
                        point_groups_received.put(received_id, new_point_group);
                    }

                    // TODO talk to Chris
                    //Update the Coordinator with the id score
                    this.control_return.send(this.uid + " SCORE " + new_point_group.getProcessedBy() + " " + 1);
                }
            }

            // Check if the number of points received is the expected number
            if (this.points_received >= this.expected_points) {
                // Recalculate the centroids
                PointGroup new_centroids = total_point_group.getNewCentroids();

                // Write the centroids into the database
                db.insertCentroids(new_centroids.getPoints(), iteration);

                // Increment the iteration, as this iteration is now completed
                iteration++;

                // Reset the points that have been received
                // TODO replace with a proper id
                total_point_group = new PointGroup(new ArrayList<>(), "TODO replace this");

                // Reset the number of points received
                points_received = 0;

                // If the iteration number has not been reached yet, the new centroids need to be sent to workers
                if (iteration <= max_iterations) {
                    // Sends the new centroids to the workers here
                    this.control_return.send(this.uid + "COLLECTOR_CENTROID_UPDATE");

                    byte[] msg_bytes;
                    //Convert the Centroids to a byte array to transmit
                    ByteArrayOutputStream centroid_byte_stream = new ByteArrayOutputStream();
                    try (ObjectOutput centroid_converter = new ObjectOutputStream(centroid_byte_stream)) {
                        centroid_converter.writeObject(new_centroids);
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
                }
                /* If the iteration has been reached, the coordinator should be signalled that the results have been
                found */
                else {
                    // TODO send the new
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
