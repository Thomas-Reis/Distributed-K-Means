package server;

import shared.ClientObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Thread{
    public static ClientPool current_pool;

    public Listener(){
        this.start();
    }

    public void run(){

        try {
            ServerSocket server = new ServerSocket(5555);
            //current_pool = new ClientPool();

            while (true) {
                Socket client = server.accept();
                ClientObject tmp_client = new ClientObject(client);
                Server.client_list.add(tmp_client);
                //current_pool.registerClient(client);
            }

        } catch (IOException ex) { System.out.println("Connection error! " + ex.getMessage()); }

    }

}
