package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class PhaseOne implements Runnable {

    //private ClientLink origin;
    private ClientDatabaseConnection clientDB;
    private CentroidDatabaseConnection centroidDB;

    private String uid;
    private int clusters_sent = 0;

    private ZMQ.Socket task_transmit_socket;
    private ZMQ.Socket control_socket;
    private ZMQ.Socket control_return;

    //How many points to include in a group
    private int group_size;

    //How many users to send the message to
    private int redundant_calculations;

    //PORTS USED:
    //Task_publish = 10000
    //Task_return = 10001
    //Control_publish = 10010
    //Control_return = 10011


    PhaseOne(ZMQ.Context zmq_context, ClientDatabaseConnection clientDB, CentroidDatabaseConnection centroidDB, String uid, int redundant_calculations, int group_size) {
        this.clientDB = clientDB;
        this.centroidDB = centroidDB;

        this.group_size = group_size;
        this.redundant_calculations = redundant_calculations;
        this.uid = uid;


        //Setup the transmission socket
        this.task_transmit_socket = zmq_context.socket(SocketType.PUSH);
        this.task_transmit_socket.bind("tcp://*:10000");

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

        while (this.clientDB.hasMore()) {
            PointGroup nextCluster = this.clientDB.getPoints(this.group_size);
            byte[] message;

            //Convert the cluster to a byte array
            ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
            try (ObjectOutput converter = new ObjectOutputStream(byte_stream)) {
                converter.writeObject(nextCluster);
                converter.flush();
                message = byte_stream.toByteArray();
            } catch (IOException ex) { message = new byte[] {0}; }

            //Make sure it converted properly
            if (message.length != 1) {
                //Send the message to as many users as specified
                for (int i=0; i < this.redundant_calculations; i++) {
                    this.task_transmit_socket.send(message);
                    this.clusters_sent++;
                }
            }
        }

        //Let the Coordinator know we've finished
        this.control_return.send((this.uid +" DONE " + this.clusters_sent).getBytes(ZMQ.CHARSET));

        //Clean up our mess
        this.task_transmit_socket.close();
        this.control_socket.close();
        this.control_return.close();
        return;
    }

}

