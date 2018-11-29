package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

public class Coordinator implements Runnable {

    private static int control_transmit_port = 10000;
    private static int control_return_port = 10001;
    //private static int task_transmit_port = 10010;
    //private static int task_return_port = 10011;

    private static int zmq_iothreads = 2;

    private ZMQ.Socket control_transmit;
    private ZMQ.Socket control_return;
    private ZMQ.Context zmq_context;

    Coordinator() {

        this.zmq_context = ZMQ.context(zmq_iothreads);

        //Setup the control transmit port
        this.control_transmit = zmq_context.socket(SocketType.PUB);
        this.control_transmit.bind("tcp://*:" + control_transmit_port);

        this.control_return = zmq_context.socket(SocketType.REP);
        this.control_return.bind("tcp://*:" + control_return_port);
    }

    @Override
    public void run() {
        //TODO: Setup PhaseOne & PhaseTwo Systems

        while (true) {
            //Keep listening to messages until everyone dies
        }
    }
}
