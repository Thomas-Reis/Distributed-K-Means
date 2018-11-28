package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class PhaseTwo implements Runnable {
    private CentroidDatabaseConnection centroidDB;

    private String uid;
    private int expected_clusters = Integer.MAX_VALUE;
    private int clusters_received = 0;

    private ZMQ.Socket control_socket;
    private ZMQ.Socket control_return;
    private ZMQ.Socket task_receive_socket;

    //PORTS USED:
    //Task_publish = 10000
    //Task_return = 10001
    //Control_publish = 10010
    //Control_return = 10011


    PhaseTwo(ZMQ.Context zmq_context, CentroidDatabaseConnection centroidDB, String uid) {
        this.uid = uid;

        this.centroidDB = centroidDB;

        //Setup the transmission socket
        this.task_receive_socket = zmq_context.socket(SocketType.PULL);
        this.task_receive_socket.setBacklog(3); //Allow only 3 messages on local queue
        this.task_receive_socket.bind("tcp://*:10001");

        //Setup the control downlink
        this.control_socket = zmq_context.socket(SocketType.SUB);
        this.control_socket.connect("tcp://localhost:10010");
        this.control_socket.subscribe(this.uid);

        //Setup the control uplink
        this.control_return = zmq_context.socket(SocketType.REQ);
        this.control_return.connect("tcp://localhost:10011");

    }


    @Override
    public void run() {

        //Let the Coordinator know we've started
        this.control_return.send((this.uid +" START").getBytes(ZMQ.CHARSET));

        int step = 1;
        while (true) {
            this.control_return.send((this.uid +" STEP " + step).getBytes(ZMQ.CHARSET));

            //Check for messages from control
            byte[] control = this.control_socket.recv(ZMQ.DONTWAIT);
            if (control != null) {
                String message =  new String(control, ZMQ.CHARSET);
                String[] msg_parts = message.split(" ");

                //TODO: Process the message

                if (msg_parts[1].equals("COUNT"))
                { this.expected_clusters = Integer.parseInt(msg_parts[2]); }

            }

            //Check for clusters from the workers
            byte[] cluster_raw = this.task_receive_socket.recv(ZMQ.DONTWAIT);
            if (cluster_raw != null) {

                PointGroup cluster = null;

                //Convert from bytes
                ByteArrayInputStream byte_stream = new ByteArrayInputStream(cluster_raw);
                try (ObjectInput converter = new ObjectInputStream(byte_stream)) {
                    cluster = (PointGroup) converter.readObject();
                } catch (IOException | ClassNotFoundException ex) { cluster = null; }

                //If cluster converted properly
                if (cluster != null) {
                    //TODO: Process the cluster

                    //Increment the cluster count
                    this.clusters_received++;
                }
            }

            //Check end conditions
            //TODO: Implement end conditions
            if (this.clusters_received >= this.expected_clusters) {
                //TODO: Handle closing



                //Let the Coordinator know we've finished
                this.control_return.send((this.uid +" DONE").getBytes(ZMQ.CHARSET));

                //Clean up our mess
                this.task_receive_socket.close();
                this.control_socket.close();
                this.control_return.close();
                return;
            }

        }



    }
}
