package shared;

import java.util.ArrayList;

public class PointGroup {

    private ArrayList<Point> points;
    private double score;

    PointGroup(ArrayList<Point> points) {
        this.points = points;
        this.score = 0;
    }

    public void addScore(double value) {
        this.score += value;
    }

    public double getScore() {
        return this.score;
    }


}
