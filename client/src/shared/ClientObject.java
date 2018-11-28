package shared; /**
 * Contains the class that holds all of the information for a client.
 * @author Bradon Lodwick
 * @version 1.0
 * @since 2018-09-30
 */

import java.net.*;
import java.io.*;

/**
 * Holds all information pertaining to an individual client.
 */
public class ClientObject {
    // transient is used to ignore fields when converting to json using Gson library
    private transient PrintWriter out;
    private transient BufferedReader in;
    private transient Socket socket;
    private transient ObjectOutputStream Obj_w;
    private transient ObjectInputStream Obj_r;

    public ClientObject() {
    }

    /**
     * Constructor for the new client.
     * @param c The socket of the client.
     */
    public ClientObject(Socket c) {
        this.socket = c;
        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.Obj_w = new ObjectOutputStream(this.socket.getOutputStream());
            this.Obj_r = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            try {
                System.out.println("Error while getting the socket streams: " + e);
                c.close();
            } catch (IOException ex) {
                System.out.println("Error while closing the client socket while handling another exception: " + ex);
            }
        }
    }

    public ObjectOutputStream getObj_Out(){
        return this.Obj_w;
    }

    public ObjectInputStream getObj_In(){
        return this.Obj_r;
    }

    /**
     * Gets the output PrintWriter of the client.
     * @return PrintWriter The output PrintWriter of the client.
     */
    public PrintWriter getOut() {
        return this.out;
    }

    /**
     * Gets the input BufferedReader of the client.
     * @return BufferedReader The input BufferedReader of the client. BufferedReader The input BufferedReader of the client.
     */
    public BufferedReader getIn() {
        return this.in;
    }

    /**
     * Gets the socket of the client.
     * @return Socket The socket of the client.
     */
    public Socket getSocket() {
        return this.socket;
    }
}