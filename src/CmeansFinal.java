import java.util.*;
import java.util.stream.Collectors;


public class CmeansFinal {

    public static final int DEFAULT_NUMBER_OF_LINES = 3;
    public static final int NUMBER_OF_FIELD_PLAYERS = 10;
    public static final int MAX_NUMBER_OF_LINES = 5;
    public static final double FUZZYNESS_COEF = 2;
    public static final double EPS = 0.01;
    public static final double EQUALS_ZERO = 0.001;

    public static Formation calculateFormation(List<Position> positions) {
        if (positions.size() > NUMBER_OF_FIELD_PLAYERS) {
            throw new RuntimeException("Number of field players cannot be more than " + NUMBER_OF_FIELD_PLAYERS);
        }

        Collections.sort(positions);

        double[] sortedPoints = positions.stream()
                .mapToDouble(Position::getX)
                .sorted()
                .toArray();

        int numberOfLines = countLinesUsingDelta2(sortedPoints);
        double[] newLines = getLines(sortedPoints, numberOfLines);

        double[][] distances = new double[sortedPoints.length][newLines.length];
        double[][] fuzzyPartitionMatrix = new double[sortedPoints.length][newLines.length];
        double[] oldLines;

        do {
            oldLines = Arrays.copyOf(newLines, newLines.length);

            for (int i = 0; i < sortedPoints.length; i++) {
                for (int j = 0; j < oldLines.length; j++) {
                    distances[i][j] = Math.abs(sortedPoints[i] - oldLines[j]);//TODO pow 2
                }
            }

            label:
            for (int i = 0; i < sortedPoints.length; i++) {
                double sum = 0;
                for (int j = 0; j < oldLines.length; j++) {
                    if (distances[i][j] > EQUALS_ZERO) {
                        sum += 1 / Math.pow(distances[i][j], 2 / (FUZZYNESS_COEF - 1));
                    } else {
                        for (int k = 0; k < oldLines.length; k++) {
                            fuzzyPartitionMatrix[i][k] = (k == j) ? 1.0 : 0.0;
                        }
                        continue label;
                    }
                }

                for (int j = 0; j < oldLines.length; j++) {
                    fuzzyPartitionMatrix[i][j] = 1 / (Math.pow(distances[i][j], 2 / (FUZZYNESS_COEF - 1)) * sum);
                }
            }

            for (int i = 0; i < newLines.length; i++) {
                double A = 0;
                double B = 0;
                for (int j = 0; j < sortedPoints.length; j++) {
                    double temp = Math.pow(fuzzyPartitionMatrix[j][i], FUZZYNESS_COEF);
                    A += temp * sortedPoints[j];
                    B += temp;
                }
                newLines[i] = A / B;
            }

        } while (!isTermination(oldLines, newLines, EPS));

        for (int i = 0; i < fuzzyPartitionMatrix.length; i++) {
            int firstCluster = -1;
            for (int j = 0; j < newLines.length; j++) {
                if (Double.compare(newLines[j], sortedPoints[i]) <= 0) {
                    firstCluster = j;
                }
            }

            double sum = 0;
            for (int j = 0; j < fuzzyPartitionMatrix[i].length; j++) {
                if (j == firstCluster || j == firstCluster + 1) {
                    fuzzyPartitionMatrix[i][j] = round(fuzzyPartitionMatrix[i][j], 1);
                } else {
                    fuzzyPartitionMatrix[i][j] = 0;
                }
                sum += fuzzyPartitionMatrix[i][j];
            }

            if (Double.compare(sum, 1) < 0) {
                if (firstCluster != -1 && firstCluster != newLines.length - 1) {
                    if (fuzzyPartitionMatrix[i][firstCluster] > fuzzyPartitionMatrix[i][firstCluster + 1]) {
                        fuzzyPartitionMatrix[i][firstCluster] = 1 - fuzzyPartitionMatrix[i][firstCluster + 1];
                    } else {
                        fuzzyPartitionMatrix[i][firstCluster + 1] = 1 - fuzzyPartitionMatrix[i][firstCluster];
                    }
                }
            }
        }

        Formation formation = new Formation();
        for (int i = 0; i < newLines.length; i++) {
            FormationLine line = new FormationLine(newLines[i]);
            for (int j = 0; j < sortedPoints.length; j++) {
                if (Double.compare(fuzzyPartitionMatrix[j][i], 0) > 0) {
                    line.addFuzzyPosition(new FuzzyPosition(positions.get(j), fuzzyPartitionMatrix[j][i]));
                }
            }
            formation.addLine(line);
        }

        for (int i = 0; i < newLines.length; i++) {
            System.out.printf("%.2f  ", newLines[i]);
        }
        System.out.println();
        System.out.println();

        return formation;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private static boolean isTermination(double[] oldLines, double[] newLines, double eps) {
        for (int i = 0; i < oldLines.length; i++) {
            if (Math.abs(oldLines[i] - newLines[i]) > eps) {
                return false;
            }
        }
        return true;
    }

    private static int countLinesUsingDelta2(double[] sortedPoints) {

        double[] sortedDelta = new double[sortedPoints.length - 1];
        for (int i = 0; i < sortedDelta.length; i++) {
            sortedDelta[i] = sortedPoints[i + 1] - sortedPoints[i];
        }
        Arrays.sort(sortedDelta);

        double[] delta2 = new double[sortedDelta.length - 1];
        for (int i = 0; i < delta2.length; i++) {
            delta2[i] = sortedDelta[i + 1] - sortedDelta[i];
        }

        double maxDelta2 = 0;
        int maxDelta2Index = 0;
        for (int i = 0; i < delta2.length - 1; i++) {
            if (delta2[i] >= maxDelta2) {
                maxDelta2 = delta2[i];
                maxDelta2Index = i;
            }
        }

        int numberOfLines = delta2.length - maxDelta2Index + 1;
        if (numberOfLines > MAX_NUMBER_OF_LINES) {
            return DEFAULT_NUMBER_OF_LINES;
        }
        return numberOfLines;
    }

    private static double[] getLines(double[] sortedPoints, int numberOfClusters) {

        double[] delta = new double[sortedPoints.length - 1];
        for (int i = 0; i < delta.length; i++) {
            delta[i] = sortedPoints[i + 1] - sortedPoints[i];
        }

        int maxDeltaIndex = 0;
        double[] centers = new double[numberOfClusters];
        for (int i = 0; i < centers.length; i++) {
            double curMaxDelta = -1;
            int curMaxDeltaIndex = 0;
            for (int j = 0; j < delta.length; j++) {
                if (delta[j] >= curMaxDelta) {
                    curMaxDelta = delta[j];
                    curMaxDeltaIndex = j;
                }
            }

            delta[curMaxDeltaIndex] = -1;

            if (i == 0) {
                maxDeltaIndex = curMaxDeltaIndex;
                centers[i] = sortedPoints[curMaxDeltaIndex];
                i++;
                centers[i] = sortedPoints[curMaxDeltaIndex + 1];
            } else {
                if (curMaxDeltaIndex > maxDeltaIndex) {
                    centers[i] = sortedPoints[curMaxDeltaIndex + 1];
                } else {
                    centers[i] = sortedPoints[curMaxDeltaIndex];
                }
            }
        }

        Arrays.sort(centers);
        return centers;
    }

}

class Formation implements Iterable<FormationLine> {
    private final List<FormationLine> formationLines = new ArrayList<>();

    public int numberOfLines() {
        return formationLines.size();
    }

    public FormationLine getLine(int index) {
        return formationLines.get(index);
    }

    public void addLine(FormationLine line) {
        formationLines.add(line);
    }

    @Override
    public Iterator<FormationLine> iterator() {
        return formationLines.iterator();
    }

    @Override
    public String toString() {
        return formationLines.stream()
                .map(FormationLine::toString)
                .collect(Collectors.joining("\n"));
    }
}

class FormationLine {
    final private double xCenter;
    final private List<FuzzyPosition> fuzzyPositions = new ArrayList<>();

    public FormationLine(double xCenter) {
        this.xCenter = xCenter;
    }

    public void addFuzzyPosition(FuzzyPosition fuzzyPosition) {
        fuzzyPositions.add(fuzzyPosition);
    }

    public double getxCenter() {
        return xCenter;
    }

    public List<FuzzyPosition> getFuzzyPositions() {
        return fuzzyPositions;
    }

    @Override
    public String toString() {
        return fuzzyPositions.stream()
                .map(FuzzyPosition::toString)
                .collect(Collectors.joining(" ; ", String.format("%.1f", xCenter) + " : { ", " }"));
    }
}

class FuzzyPosition {
    final private Position position;
    final private double fuzziness;

    public FuzzyPosition(Position position, double fuzziness) {
        this.position = position;
        this.fuzziness = fuzziness;
    }

    public Position getPosition() {
        return position;
    }

    public double getFuzziness() {
        return fuzziness;
    }

    @Override
    public String toString() {
        return position + String.format(" / %.1f", fuzziness);
    }
}

interface Position extends Comparable<Position> {
    double getX();

    double getY();
}

class ReachabilityArea implements Position {
    final private double xCenter;
    final private double yCenter;
    final private double radius;
    final private double angle;   //in graduses
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

class Point implements Position {
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
        return String.format("(%.2f;%.2f)", x, y);
    }
}
