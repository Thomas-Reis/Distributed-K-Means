import java.util.ArrayList;

public class Client {
    public static void main(String[] args) {
        ArrayList<Point> centroids = new ArrayList<>();
        centroids.add(new Point(0, 0));
        centroids.add(new Point(1, 0));
        centroids.add(new Point(2, 0));

        Point p1 = new Point(3, 10);
        Point p2 = new Point(2, 10);
        Point p3 = new Point(0.25, 10);

        ArrayList<ArrayList<Point>> client1 = new ArrayList<>();
        ArrayList<ArrayList<Point>> client2 = new ArrayList<>();
        ArrayList<ArrayList<Point>> client3 = new ArrayList<>();
    }
}