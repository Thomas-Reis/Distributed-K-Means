package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.HashMap;

public class Coordinator implements Runnable {

    private static int control_transmit_port = 10000;
    private static int control_return_port = 10001;
    //private static int task_transmit_port = 10010;
    //private static int task_return_port = 10011;

    private static int zmq_iothreads = 2;
    private static int minimum_clients = 2;

    private int newest_id = 0;
    private int phase_one_id = -1;
    private int phase_two_id = -1;

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

            byte[] message_raw = this.control_return.recv();
            String message = new String(message_raw, ZMQ.CHARSET);
            String[] message_chunks = message.split(" ");

            //New user joining the network, send them an id
            if (message.equals("-1 JOIN")) {
                String response = (this.newest_id++) + " GOOD";
                this.control_return.send(response.getBytes(ZMQ.CHARSET));
            }

            //Message from PhaseOne
            if (Integer.parseInt(message_chunks[0]) == this.phase_one_id && this.phase_one_id != -1) {

                //Check if finished
                if (message_chunks[1].equals("DONE")) {
                    //Send the count to phase 2
                    this.control_transmit.send(this.phase_two_id + " COUNT " + message_chunks[2]);
                }



            }



            // --- END OF MESSAGE HANDLING ---

            //Enforce minimum client amount before starting K-Means distribution
            if (this.newest_id < minimum_clients) {
                this.control_transmit.send("BROADCAST LOWCLIENT".getBytes(ZMQ.CHARSET));
                continue;
            }

            if (this.phase_one_id == -1 && this.phase_two_id == -1) {

            }

        }
    }
}
