public class Point implements Position {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public int compareTo(Position o) {
        int compareX = Double.compare(x, o.getX());
        return compareX != 0 ? compareX : Double.compare(y, o.getY());
    }

    @Override
    public String toString() {
        return String.format("(%.1f;%.1f)", x, y);
    }
}
