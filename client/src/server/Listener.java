package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener {

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(5555);

            while (true) {
                Socket client = server.accept();

            }

        } catch (IOException ex) { System.out.println("Connection error! " + ex.getMessage()); }

    }

}
