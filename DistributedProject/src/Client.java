import java.util.ArrayList;

public class Client {
    public int weight = 1;
    private static ArrayList<Point> AssignedPoints = new ArrayList<>();
    private static ArrayList<Point> Centroids = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        //OLD CODE
        Centroids.add(new Point(0, 0));
        Centroids.add(new Point(1, 0));
        Centroids.add(new Point(2, 0));

        Point p1 = new Point(3, 10);
        Point p2 = new Point(2, 10);
        Point p3 = new Point(0.25, 10);

        ArrayList<ArrayList<Point>> client1 = new ArrayList<>();
        ArrayList<ArrayList<Point>> client2 = new ArrayList<>();
        ArrayList<ArrayList<Point>> client3 = new ArrayList<>();

        //NEW CODE
        while(true){
            while(AssignedPoints.size() > 0){
                AssignedPoints.clear();
                System.out.println("Received Points and Cleared Them");
            }
            Thread.sleep(20);
        }
    }

    public static void send_update(ArrayList<Double> sum_per_centroid, ArrayList<Integer> points_per_centroid){
        //TODO update the sums and number of points per centroid to the server somehow
    }

    public void ReceiveCentroids(ArrayList<Point> updated_centroids){
        Centroids = updated_centroids;
    }

    public void ReceivePoints(ArrayList<Point> GivenPoints){
        //Adds all points sent from the server to the client's AssignedPoints list
        AssignedPoints.addAll(GivenPoints);
    }
}