package server;

public class PhaseTwo implements Runnable {
    private boolean busy;
    private ClientLink origin;

    PhaseTwo(PhaseOne phase_one) {
        this.busy = false;
        this.origin = phase_one.getOrigin();
    }


    @Override
    public void run() {
        //Set ourselves busy
        synchronized(this) { this.busy = true; }


        //Run the thing
    }

    public boolean isBusy() {
        synchronized (this) { return this.busy; }
    }

    public ClientLink getOrigin() {
        synchronized (this) { return this.origin; }
    }
}
