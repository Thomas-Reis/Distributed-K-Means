package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener extends Thread{
    public static ClientPool current_pool;

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(5555);
            current_pool = new ClientPool();

            while (true) {
                Socket client = server.accept();
                current_pool.registerClient(client);
            }

        } catch (IOException ex) { System.out.println("Connection error! " + ex.getMessage()); }

    }

}
