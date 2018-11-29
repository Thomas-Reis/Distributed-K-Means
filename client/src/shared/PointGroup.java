package shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class PointGroup implements Serializable {

    private ArrayList<Point> points;
    private HashMap<Integer, Double> centroid_sums = new HashMap<>();
    private double group_sum;
    private String uid;
    private String processed_by = "";

    public PointGroup(ArrayList<Point> points, String uid) {
        this.points = points;
        this.uid = uid;
        this.group_sum = 0;
    }

    public ArrayList<Point> getPoints() {
        return this.points;
    }

    public void addScore(double value) {
        this.group_sum += value;
    }

    public double getGroupSum() {
        return this.group_sum;
    }

    public String getUid() {
        return this.uid;
    }

    public void setProcessedBy(String user_id) {
        if (this.processed_by.equals(""))
        { this.processed_by = user_id; }
    }

    public String getProcessedBy() {
        return this.processed_by;
    }

}
