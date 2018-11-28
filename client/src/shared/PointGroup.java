package shared;

import java.io.Serializable;
import java.util.ArrayList;

public class PointGroup implements Serializable {

    private ArrayList<Point> points;
    private double score;

    PointGroup(ArrayList<Point> points) {
        this.points = points;
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


}
