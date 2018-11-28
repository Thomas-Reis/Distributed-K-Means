package server;

import java.net.Socket;
import java.util.ArrayList;

public class ClientPool {

    private ArrayList<Socket> clients;

    public ClientPool(){
        //TODO: Instantiate the instance variables
    }


    /**
     * Registers a client to the pool
     * @param client_socket Socket established with the client
     * @return Whether the client was successfully registered
     */
    public boolean registerClient(Socket client_socket) {

        //TODO: Client verification
        //NOTE: Keep as socket, or convert back to url & reconnect later?

        this.clients.add(client_socket);

        return true;
    }

}
