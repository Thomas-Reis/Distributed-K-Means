package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class PhaseTwo implements Runnable {
    private CentroidDatabaseConnection centroidDB;

    private String uid;
    private int redundant_calculations;

    private ZMQ.Socket control_socket;
    private ZMQ.Socket control_return;
    private ZMQ.Socket task_receive_socket;

    //PORTS USED:
    //Task_publish = 10000
    //Task_return = 10001
    //Control_publish = 10010
    //Control_return = 10011


    PhaseTwo(ZMQ.Context zmq_context, CentroidDatabaseConnection centroidDB, String uid, int redundant_connections) {
        this.uid = uid;
        this.redundant_calculations = redundant_connections;

        this.centroidDB = centroidDB;

        //Setup the transmission socket
        this.task_receive_socket = zmq_context.socket(SocketType.PULL);
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
            }

        }



    }
}
