import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class KMeansImplementation extends UnicastRemoteObject implements KMeansInterface {
    public KMeansImplementation() throws RemoteException {
        super();
    }

    @Override
    public void sendMessage(String s) {
        System.out.println(s);
    }

    @Override
    public ClientImplementation sendClient(ClientImplementation client) {
        client.printTest("hello");
        return client;
    }
}
