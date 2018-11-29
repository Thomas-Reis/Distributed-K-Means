import java.util.Observable;

public class Server extends Observable {
    public static void main(String[] args){
        //TODO make a new thread to accept clients

        //LOOP START
        //TODO select an "origin" client from the pool
        //TODO receive database info/queries to run

        //BEFORE ITERATION BEGINS
        //TODO assign rows to clients

        //DURING ITERATION
        //TODO perform load balancing here
        //this means:
        //TODO check for client's requests for more data and assign them accordingly


        //AFTER ITERATION
        //TODO recalculate centroids using KMeans updateCentroidsAverage Method
        //TODO check if finishing conditions are met

        //AFTER ALL DATA IS PROCESSED
        //TODO return results to client somehow
        //LOOP BACK
    }
}
