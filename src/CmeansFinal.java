import java.util.*;
import java.util.stream.Collectors;


public class CmeansFinal {

    private static final int DEFAULT_NUMBER_OF_LINES = 3;
    private static final int NUMBER_OF_FIELD_PLAYERS = 10;
    private static final int MAX_NUMBER_OF_LINES = 5;
    private static final double FUZZYNESS_COEF = 2;
    private static final double EPS = 0.01;
    private static final double EQUALS_ZERO = 0.001;

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
        double[] oldLines = getLines(sortedPoints, numberOfLines);

        double[][] distances = new double[sortedPoints.length][oldLines.length];
        double[][] fuzzyPartitionMatrix = new double[sortedPoints.length][oldLines.length];
        double[] newLines = null;

        do {
            if (newLines != null) {
                oldLines = newLines;
            }

            calculatingDistances(sortedPoints, distances, oldLines);

            calculatingFuzzyPartitionMatrix(distances, fuzzyPartitionMatrix);

            newLines = calculationNewLines(sortedPoints, fuzzyPartitionMatrix);

        } while (!isTermination(oldLines, newLines, EPS));

        roundingFuzzyPartitionMatrix(sortedPoints, fuzzyPartitionMatrix, newLines);

        return Formation.getFormation(positions, fuzzyPartitionMatrix, newLines);
    }

    private static void roundingFuzzyPartitionMatrix(double[] sortedPoints, double[][] fuzzyPartitionMatrix, double[] newLines) {
        for (int i = 0; i < fuzzyPartitionMatrix.length; i++) {
            int leftCluster = -1;
            for (int j = 0; j < newLines.length; j++) {
                if (Double.compare(newLines[j], sortedPoints[i]) <= 0) {
                    leftCluster = j;
                }
                else{
                    break;
                }
            }

            double sum = 0;
            for (int j = 0; j < fuzzyPartitionMatrix[i].length; j++) {
                if (j == leftCluster || j == leftCluster + 1) {
                    fuzzyPartitionMatrix[i][j] = round(fuzzyPartitionMatrix[i][j], 1);
                } else {
                    fuzzyPartitionMatrix[i][j] = 0;
                }
                sum += fuzzyPartitionMatrix[i][j];
            }

            if (Double.compare(sum, 1) < 0 && leftCluster != -1 && leftCluster != newLines.length - 1) {
                if (fuzzyPartitionMatrix[i][leftCluster] > fuzzyPartitionMatrix[i][leftCluster + 1]) {
                    fuzzyPartitionMatrix[i][leftCluster] = 1 - fuzzyPartitionMatrix[i][leftCluster + 1];
                } else {
                    fuzzyPartitionMatrix[i][leftCluster + 1] = 1 - fuzzyPartitionMatrix[i][leftCluster];
                }
            }
        }
    }

    private static double[] calculationNewLines(double[] sortedPoints, double[][] fuzzyPartitionMatrix) {
        double[] newLines = new double[fuzzyPartitionMatrix[0].length];
        for (int i = 0; i < fuzzyPartitionMatrix[0].length; i++) {
            double A = 0;
            double B = 0;
            for (int j = 0; j < fuzzyPartitionMatrix.length; j++) {
                double temp = Math.pow(fuzzyPartitionMatrix[j][i], FUZZYNESS_COEF);
                A += temp * sortedPoints[j];
                B += temp;
            }
            newLines[i] = A / B;
        }
        return newLines;
    }

    private static void calculatingDistances(double[] sortedPoints, double[][] distances, double[] oldLines) {
        for (int i = 0; i < distances.length; i++) {
            for (int j = 0; j < distances[i].length; j++) {
                distances[i][j] = Math.abs(sortedPoints[i] - oldLines[j]);
            }
        }
    }

    private static void calculatingFuzzyPartitionMatrix(double[][] distances, double[][] fuzzyPartitionMatrix) {
        label:
        for (int i = 0; i < fuzzyPartitionMatrix.length; i++) {
            double sum = 0;
            for (int j = 0; j < fuzzyPartitionMatrix[i].length; j++) {
                if (distances[i][j] > EQUALS_ZERO) {
                    sum += 1 / Math.pow(distances[i][j], 2 / (FUZZYNESS_COEF - 1));
                } else {
                    for (int k = 0; k < fuzzyPartitionMatrix[i].length; k++) {
                        fuzzyPartitionMatrix[i][k] = (k == j) ? 1.0 : 0.0;
                    }
                    continue label;
                }
            }

            for (int j = 0; j < fuzzyPartitionMatrix[i].length; j++) {
                fuzzyPartitionMatrix[i][j] = 1 / (Math.pow(distances[i][j], 2 / (FUZZYNESS_COEF - 1)) * sum);
            }
        }
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