package shared;

import java.io.Serializable;
import java.util.ArrayList;

public class PointGroup implements Serializable {

    private ArrayList<Point> points;
    private double score;
    private String uid;
    private String processed_by = "";

    PointGroup(ArrayList<Point> points, String uid) {
        this.points = points;
        this.uid = uid;
        this.score = 0;
    }

    public ArrayList<Point> getPoints() {
        return this.points;
    }

    public void addScore(double value) {
        this.score += value;
    }

    public double getScore() {
        return this.score;
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
