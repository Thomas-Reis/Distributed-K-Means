import java.rmi.Naming;

public class KMeansClient {
    public static void main(String[] args) {
        ClientImplementation me = new ClientImplementation();
        try {
            KMeansInterface f = (KMeansInterface) Naming.lookup(KMeansInterface.SERVICENAME);
            f.sendMessage("test");
            f.sendClient(me);
        } catch(Exception e) {
            System.err.println("Remote exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
