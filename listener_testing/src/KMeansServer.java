import java.rmi.Naming;

public class KMeansServer {
    public static void main(String[] args) {
        // Set the security policy
        System.setProperty("java.security.policy","policy.txt");
        System.setSecurityManager(new SecurityManager());
        try {
            KMeansImplementation k = new KMeansImplementation();
            Naming.rebind(k.SERVICENAME, k);
            System.out.println("Published in RMI registry, ready...");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}