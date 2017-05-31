import java.util.Arrays;
import java.util.Collections;
import java.util.OptionalDouble;


public class CmeansWithHeuristic {
    private int numberOfClusters;
    private static final int FUZZYNESS_COEF = 2;
    private final double eps;
    private final int numberOfPoints;

    private final int[] points;
    private double[][] fuzzyPartitionMatrix;
    private double[][] newFuzzyPartitionMatrix;
    private double[] clusterCenters;
    private double[][] distances;

    public static int testCountClusters(int[] inPoints) {
        int points[] = inPoints.clone();
        Arrays.sort(points);
        System.out.println("points:\n" + Arrays.toString(points));

        int range = points[points.length - 1] - points[0];
        System.out.println("range = " + range);

        int[] delta = new int[points.length - 1];
        for (int i = 0; i < delta.length; i++) {
            delta[i] = points[i + 1] - points[i];
        }
        System.out.println("delta:\n" + Arrays.toString(delta));

        int[] sortedDelta = Arrays.copyOf(delta, delta.length);
        Arrays.sort(sortedDelta);
        reverseArray(sortedDelta);

        System.out.println("sorted delta:\n" + Arrays.toString(sortedDelta));

        int[] delta2 = new int[sortedDelta.length - 1];
        for (int i = 0; i < delta2.length; i++) {
            delta2[i] = sortedDelta[i] - sortedDelta[i + 1];
        }
        System.out.println("delta2:\n" + Arrays.toString(delta2));

        int max = 0;
        int maxIndex = 0;
        for (int i = 0; i < delta2.length; i++) {
            if (delta2[i] >= max) {
                max = delta2[i];
                maxIndex = i;
            }
        }
        //if (maxIndex == 0) maxIndex = 1;

        System.out.println("Number of lines using max delta2 = " + (maxIndex + 2));

        int range2 = range / 2;
        int countMoreThanRange2 = (int) Arrays.stream(delta).filter(x -> x >= range2).count();
        System.out.println("range2 = " + range2 + "\nMore than range2 = " + countMoreThanRange2);
        int range3 = range / 3;
        int countMoreThanRange3 = (int) Arrays.stream(delta).filter(x -> x >= range3).count();
        System.out.println("range3 = " + range3 + "\nMore than range3 = " + countMoreThanRange3);
        int range4 = range / 4;
        int countMoreThanRange4 = (int) Arrays.stream(delta).filter(x -> x >= range4).count();
        System.out.println("range4 = " + range4 + "\nMore than range4 = " + countMoreThanRange4);
        int range45 = (int) (range / 4.5);
        int countMoreThanRange45 = (int) Arrays.stream(delta).filter(x -> x >= range45).count();
        System.out.println("range4.5 = " + range45 + "\nMore than range4.5 = " + countMoreThanRange45);
        int range5 = range / 5;
        int countMoreThanRange5 = (int) Arrays.stream(delta).filter(x -> x >= range5).count();
        System.out.println("range5 = " + range5 + "\nMore than range5 = " + countMoreThanRange5);

        double[] delta2mul = new double[sortedDelta.length - 1];
        for (int i = 0; i < delta2mul.length; i++) {
            if (sortedDelta[i + 1] != 0)
                delta2mul[i] = (double) sortedDelta[i] / sortedDelta[i + 1];
            else
                delta2mul[i] = -1;
        }
        System.out.println("delta2mul:\n" + Arrays.toString(delta2mul));

        double max1 = 0;
        int maxIndex1 = 0;
        for (int i = 0; i < delta2mul.length; i++) {
            if (delta2mul[i] >= max1) {
                max1 = delta2mul[i];
                maxIndex1 = i;
            }
        }

        System.out.println("Number of lines using max delta2mul = " + (maxIndex1 + 2));

        System.out.printf("===========================");
        return -1;
    }

    public static int countClustersUsingRange5(int[] points) {
        int[] sortedPoints = Arrays.copyOf(points, points.length);
        Arrays.sort(sortedPoints);

        int range = sortedPoints[sortedPoints.length - 1] - sortedPoints[0];
        double range5 = range / 5.0;

        int[] delta = new int[sortedPoints.length - 1];
        for (int i = 0; i < delta.length; i++) {
            delta[i] = sortedPoints[i + 1] - sortedPoints[i];
        }

        return (int) Arrays.stream(delta).filter(x -> x >= range5).count();
    }

    public static int countClustersUsingDelta2(int[] points) {
        int[] sortedPoints = Arrays.copyOf(points, points.length);
        Arrays.sort(sortedPoints);

        int[] sortedDelta = new int[sortedPoints.length - 1];
        for (int i = 0; i < sortedDelta.length; i++) {
            sortedDelta[i] = sortedPoints[i + 1] - sortedPoints[i];
        }
        Arrays.sort(sortedDelta);
        reverseArray(sortedDelta);

        int[] delta2 = new int[sortedDelta.length - 1];
        for (int i = 0; i < delta2.length; i++) {
            delta2[i] = sortedDelta[i] - sortedDelta[i + 1];
        }

        int max = 0;
        int maxIndex = 0;
        for (int i = 1; i < delta2.length; i++) {
            if (delta2[i] >= max) {
                max = delta2[i];
                maxIndex = i;
            }
        }
        if (/*maxIndex < 1 || */maxIndex > 3) maxIndex = 1;

        System.out.println(maxIndex + 2);
        return maxIndex + 2;
    }

    public static int[] getCenters(int[] points, int numClust) {
        int[] sortedPoints = Arrays.copyOf(points, points.length);
        Arrays.sort(sortedPoints);

        int[] delta = new int[sortedPoints.length - 1];
        for (int i = 0; i < delta.length; i++) {
            delta[i] = sortedPoints[i + 1] - sortedPoints[i];
        }

        int[] maxIndexes = new int[numClust - 1];
        for (int i = 0; i < maxIndexes.length; i++) {
            int max = 0;
            for (int j = 0; j < delta.length; j++) {
                if (delta[j] >= max) {
                    max = delta[j];
                    maxIndexes[i] = j;
                }
            }
            delta[maxIndexes[i]] = -1;
        }

        Arrays.sort(maxIndexes);

        int[] centers = new int[numClust];
        for (int i = 0; i < maxIndexes.length; i++) {
            centers[i] = sortedPoints[maxIndexes[i]];
        }
        centers[numClust - 1] = sortedPoints[maxIndexes[numClust - 2] + 1];
        return centers;
    }


    private static void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    public double[] getClusterCenters() {
        return clusterCenters;
    }

    public CmeansWithHeuristic(/*int numberOfClusters, */double eps, int[] points) {
//        this.numberOfClusters = numberOfClusters;
        this.eps = eps;
        this.points = points;
        this.numberOfPoints = points.length;
//        fuzzyPartitionMatrix = new double[numberOfPoints][numberOfClusters];
//        newFuzzyPartitionMatrix = null;
//        clusterCenters = new double[numberOfClusters];
//        distances = new double[numberOfPoints][numberOfClusters];
    }

    private void init() {
       /* Arrays.sort(points);

        System.out.println("Points:");
        System.out.println(Arrays.toString(points));

        int[] delta = new int[points.length - 1];

        for (int i = 0; i < delta.length; i++) {
            delta[i] = points[i + 1] - points[i];
        }

        System.out.println("Delta:");
        System.out.println(Arrays.toString(delta));

        Arrays.sort(delta);

        System.out.println("Sorted Delta:");
        System.out.println(Arrays.toString(delta));

        System.out.println("Range = " + (points[points.length - 1] - points[0]));

        double average = Arrays.stream(delta).average().orElse(0);
        System.out.println("Average delta = " + average);

        double threshold = average * 1.5;
        System.out.println("Threshold = " + threshold);
        long countLines = Arrays.stream(delta).filter(x -> x >= threshold).count() + 1;
        System.out.println("Number of lines = " + countLines);

        numberOfClusters = (int) countLines;*/

        fuzzyPartitionMatrix = new double[numberOfPoints][numberOfClusters];
        newFuzzyPartitionMatrix = null;
        clusterCenters = new double[numberOfClusters];
        distances = new double[numberOfPoints][numberOfClusters];

        for (int i = 0; i < numberOfPoints; i++) {
            double max = 1;
            for (int j = 0; j < numberOfClusters - 1; j++) {
                fuzzyPartitionMatrix[i][j] = Math.random() % max;
                max -= fuzzyPartitionMatrix[i][j];
            }
            fuzzyPartitionMatrix[i][numberOfClusters - 1] = max;
        }
    }

    private void calculateCenters() {
        for (int i = 0; i < numberOfClusters; i++) {
            double A = 0;
            double B = 0;
            for (int j = 0; j < numberOfPoints; j++) {
                double temp = Math.pow(fuzzyPartitionMatrix[j][i], FUZZYNESS_COEF);
                A += temp * points[j];
                B += temp;
            }
            clusterCenters[i] = A / B;
        }
    }

    private void calculateDistance() {
        for (int i = 0; i < numberOfPoints; i++) {
            for (int j = 0; j < numberOfClusters; j++) {
                distances[i][j] = Math.abs(points[i] - clusterCenters[j]);
            }
        }
    }

    private void calculateMatrix() {
        newFuzzyPartitionMatrix = new double[numberOfPoints][numberOfClusters];
        for (int i = 0; i < numberOfPoints; i++) {
            double sum = 0;
            for (int j = 0; j < numberOfClusters; j++) {
                sum += 1 / Math.pow(distances[i][j], 2);

            }

            for (int j = 0; j < numberOfClusters; j++) {
                newFuzzyPartitionMatrix[i][j] = 1 / Math.pow(Math.pow(distances[i][j], 2) * sum, 1 / (FUZZYNESS_COEF - 1));
            }
        }

    }

    private boolean isTermination() {
        for (int i = 0; i < numberOfPoints; i++) {
            for (int j = 0; j < numberOfClusters; j++) {
                if (Math.abs(fuzzyPartitionMatrix[i][j] - newFuzzyPartitionMatrix[i][j]) > eps) {
                    return false;
                }
            }
        }
        return true;
    }

    public void calculation() {
        init();

        do {
            if (newFuzzyPartitionMatrix != null) {
                fuzzyPartitionMatrix = newFuzzyPartitionMatrix;
            }
            calculateCenters();
            calculateDistance();
            calculateMatrix();
        }
        while (!isTermination());

        printCenters();
        //printDistance();
        printMatrix();

        System.out.println("=============================");

    }

    private void printCenters() {
        System.out.println("Cluster centers:");
        for (double clusterCenter : clusterCenters) {
            System.out.printf("%.1f  ", clusterCenter);
        }
        System.out.println();
        System.out.println();
    }

    private void printDistance() {
        System.out.println("Distances to clusters:");
        for (double[] points : distances) {
            for (double distFromPointToCluster : points) {
                System.out.printf("%.1f  ", distFromPointToCluster);
            }
            System.out.println();
        }
        System.out.println();
    }

    private void printMatrix() {
        System.out.println("Fuzzy Partition Matrix:");
        for (double[] points : newFuzzyPartitionMatrix) {
            for (double fuzzuOfPoint : points) {
                System.out.printf("%.1f  ", fuzzuOfPoint);
            }
            System.out.println();
        }
        System.out.println();
    }

}
