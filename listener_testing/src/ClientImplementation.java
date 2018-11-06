import java.io.Serializable;
import java.rmi.Remote;

public class ClientImplementation implements Remote, Serializable {
    public ClientImplementation() {
    }

    public void printTest(String s) {
        System.out.println(s);
    }
}
