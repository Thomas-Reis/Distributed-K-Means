import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KMeansInterface extends Remote {
    public final static String SERVICENAME = "KMeansService";
    public void sendMessage(String s) throws RemoteException;
    public ClientImplementation sendClient(ClientImplementation client) throws RemoteException;
}
