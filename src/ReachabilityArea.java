public class ReachabilityArea implements Position {
    final private double xCenter;
    final private double yCenter;
    final private double radius;
    final private double angle;
    final private double x;
    final private double y;

    public ReachabilityArea(double xCenter, double yCenter, double radius, double angle) {
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.radius = radius;
        this.angle = angle;
        double angleInRadians = angle * Math.PI / 180;
        x = xCenter + radius * Math.cos(angleInRadians);
        y = yCenter + radius * Math.sin(angleInRadians);
    }

    @Override
    public int compareTo(Position o) {
        int compareX = Double.compare(x, o.getX());
        return compareX != 0 ? compareX : Double.compare(y, o.getY());
    }

    @Override
    public String toString() {
        return String.format("(%.1f;%.1f)", getX(), getY());
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    public double getxCenter() {
        return xCenter;
    }

    public double getyCenter() {
        return yCenter;
    }

    public double getRadius() {
        return radius;
    }

    public double getAngle() {
        return angle;
    }
}
