package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.HashMap;

public class Coordinator implements Runnable {

    private static int control_transmit_port = 10000;
    private static int control_return_port = 10001;
    private static String PHASEONEIP = "127.0.0.1";
    //private static int task_transmit_port = 10010;
    //private static int task_return_port = 10011;

    private HashMap<Integer, Integer> client_score_map = new HashMap<>();

    private static int zmq_iothreads = 2;
    private static int minimum_clients = 2;

    private int newest_id = 0;
    private int phase_one_id = -1;
    private int phase_two_id = -1;

    private ZMQ.Socket control_transmit;
    private ZMQ.Socket control_return;
    private ZMQ.Context zmq_context;

    public static void main(String[] args){
        Coordinator init = new Coordinator();
        init.run();
    }

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

                if (message_chunks[1].equals("DONE")) { //P1 finished
                    //Send the count to phase 2
                    this.control_transmit.send(this.phase_two_id + " COUNT " + message_chunks[2]);
                } else if (message_chunks[1].equals("START")) { //P1 starting
                    //TODO: What should we do when PhaseOne starts?
                    control_transmit.send("BROADCAST PHASEONEREADY " + PHASEONEIP +" 10000");


                    //2nd chunk is command
                    //3rd chunk is additional data

                }

            }

            //Message from PhaseTwo
            if (Integer.parseInt(message_chunks[0]) == this.phase_two_id) {
                if (message_chunks[1].equals("START")) { //Starting
                    //TODO: What do when PhaseTwo starts?
                } else if (message_chunks[1].equals("SCORE")) { //Sent score for client
                    this.incrementClientScore(Integer.parseInt(message_chunks[2]), Integer.parseInt(message_chunks[3]));
                } else if (message_chunks[2].equals("DONE")) { //Finished
                    //TODO: Do something to clean up the thing
                }
            }



            // --- END OF MESSAGE HANDLING ---

            //Enforce minimum client amount before starting K-Means distribution
            //if (this.newest_id < minimum_clients) {
            //    this.control_transmit.send("BROADCAST LONELY".getBytes(ZMQ.CHARSET));
            //    continue;
            //}

            //Nothing's running
            if (this.phase_one_id == -1 && this.phase_two_id == -1) {
                if (this.client_score_map.isEmpty()) {
                    //TODO: Set origin to the first responding user
                } else {
                    //TODO: Set origin to the first responding high scoring user
                }
            }


        }
    }

    private void incrementClientScore(int client_id, int amount) {
        //Add it to the map
        if (client_score_map.containsKey(client_id)) { //If the client already has a score, score += value
            int current_score = client_score_map.get(client_id);
            client_score_map.replace(client_id, current_score + amount);

        } else { //Client doesn't have score yet, score = value
            client_score_map.put(client_id, amount);
        }
    }

}
