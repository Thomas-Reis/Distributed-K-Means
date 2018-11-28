package server;

import shared.ClientObject;

import java.net.Socket;
import java.util.ArrayList;

public class ClientPool {

    public ClientPool(){
        //TODO: Instantiate the instance variables
    }


    /**
     * Registers a client to the pool
     * @param client_socket Socket established with the client
     * @return Whether the client was successfully registered
     */
    public boolean registerClient(Socket client_socket) {

        //TODO: client.Client verification
        //NOTE: Keep as socket, or convert back to url & reconnect later?
        //ClientObject tmp_client = new ClientObject(client_socket);

        return true;
    }

    public ClientLink getHighestScoringClient() {
        return null;
    }

}
