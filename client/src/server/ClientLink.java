package server;

public class ClientLink {

    private String uid = "TODO MAKE UNIQUE";

    ClientLink() {
        //Needs some input
    }

    public String getUID(){
        synchronized (this) { return this.uid; }
    }


//wtf

}
