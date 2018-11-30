package server;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import shared.PointGroup;

import java.io.*;
import java.util.HashMap;

public class Coordinator implements Runnable {
    ;
    private static String PHASEONEIP = "127.0.0.1";
    private static boolean PHASEONEACTIVE;
    //private static int task_transmit_port = 10000;
    //private static int task_return_port = 10001;
    private static int control_transmit_port = 10010;
    private static int control_return_port = 10011;

    private HashMap<Integer, Integer> client_score_map = new HashMap<>();

    private static int zmq_iothreads = 2;
    private static int minimum_clients = 2;

    private int newest_id = 0;
    private int phase_one_id = 100;
    private int phase_two_id = 200;
    private PointGroup Recv_Centroids;

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

    public static PointGroup convert_to_PointGroup(byte[] msg) {
        try {
            ByteArrayInputStream Input_Byte_Converter = new ByteArrayInputStream(msg);
            ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
            return (PointGroup) Byte_Translator.readObject();
        } catch (Exception e) {
            System.err.print("Unable to Convert to PointGroup");
            return null;
        }
    }

    @Override
    public void run() {
        while (true) {

            byte[] message_raw = this.control_return.recv();
            String message = new String(message_raw, ZMQ.CHARSET);
            String[] message_chunks = message.split(" ");

            if (message_chunks[1].equals("CENTROIDS_UPDATE")){
                System.out.println("Client requesting a Centroid Update");

                byte[] centroid_bytes;

                //Convert the cluster to a byte array
                ByteArrayOutputStream byte_stream = new ByteArrayOutputStream();
                try (ObjectOutput converter = new ObjectOutputStream(byte_stream)) {
                    converter.writeObject(Recv_Centroids);
                    converter.flush();
                    centroid_bytes = byte_stream.toByteArray();
                } catch (IOException ex) {
                    centroid_bytes = new byte[]{0};
                }

                this.control_return.send(centroid_bytes);
            }

            //New user joining the network, send them an id
            if (message.equals("-1 JOIN")) {
                System.out.println("Recieved a Client, Assigned ID " + this.newest_id);
                String response = (this.newest_id++) + " GOOD";
                this.control_return.send(response.getBytes(ZMQ.CHARSET));
                if (PHASEONEACTIVE){
                    control_transmit.send("BROADCAST PHASEONEREADY " + PHASEONEIP +" 10000");
                }
            }

            //Message from PhaseOne
            if (Integer.parseInt(message_chunks[0]) == this.phase_one_id && this.phase_one_id != -1) {
                if (message_chunks[1].equals("DONE")) { //P1 finished
                    //tell the clients to stop calculations
                    this.control_transmit.send("BROADCAST DONE");
                    //Send the count to phase 2
                    this.control_transmit.send(this.phase_two_id + " COUNT " + message_chunks[2]);
                } else if (message_chunks[1].equals("START")) { //P1 starting
                    //TODO: What should we do when PhaseOne starts?
                    this.control_transmit.send("BROADCAST PHASEONEREADY " + PHASEONEIP +" 10000 ");
                    this.control_return.send("OK");
                    byte[] centroid_return = this.control_return.recv();
                    ByteArrayInputStream Input_Byte_Converter = new ByteArrayInputStream(centroid_return);
                    try {
                        ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
                        Recv_Centroids = (PointGroup) Byte_Translator.readObject();
                        this.control_return.send("SUCCESS");
                        PHASEONEACTIVE = true;
                    } catch(Exception e){
                        System.err.println("Error Parsing Centroids");
                    }
                }
            }

            //Message from PhaseTwo
            if (Integer.parseInt(message_chunks[0]) == this.phase_two_id) {
                if (message_chunks[1].equals("START")) { //Starting
                    //TODO: What do when PhaseTwo starts?
                } else if (message_chunks[1].equals("SCORE")) { //Sent score for client
                    this.incrementClientScore(Integer.parseInt(message_chunks[2]), Integer.parseInt(message_chunks[3]));
                } else if (message_chunks[1].equals("DONE")) { //Finished
                    //TODO: Do something to clean up the thing
                } else if (message_chunks[1].equals("COLLECTOR_CENTROID_UPDATE")){
                    //client's should no longer be listening since all points are out so its safe to use the Socket
                    this.control_return.send("OK");
                    byte[] centroid_return = this.control_return.recv();
                    ByteArrayInputStream Input_Byte_Converter = new ByteArrayInputStream(centroid_return);
                    try {
                        ObjectInputStream Byte_Translator = new ObjectInputStream(Input_Byte_Converter);
                        Recv_Centroids = (PointGroup) Byte_Translator.readObject();
                        this.control_return.send("SUCCESS");
                    } catch(Exception e) {
                        System.err.println("Error Parsing Centroids");
                    }

                    //Broadcast that a Centroid Update is Ready and phase 2 will begin soon
                    this.control_transmit.send("BROADCAST CENTROID_UPDATE");
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
