import java.util.Arrays;
import java.util.stream.Collectors;


public class CmeansFromCenters {

    private final int numberOfClusters;
    private static final int FUZZYNESS_COEF = 2;
    private final double eps;
    private final int numberOfPoints;

    private final int[] points;
    private double[][] fuzzyPartitionMatrix;
    private double[][] newFuzzyPartitionMatrix;
    private final double[] clusterCenters;
    private final double[][] distances;

    public double[] getClusterCenters() {
        return clusterCenters;
    }

    public CmeansFromCenters(double[] centers, double eps, int[] points) {
        this.numberOfClusters = centers.length;// numberOfClusters;
        this.eps = eps;
        this.points = points;
        this.numberOfPoints = points.length;
        fuzzyPartitionMatrix = new double[numberOfPoints][numberOfClusters];
        newFuzzyPartitionMatrix = null;
        clusterCenters = centers;//new double[numberOfClusters];
        distances = new double[numberOfPoints][numberOfClusters];
    }

    private void init() {
        Arrays.sort(points);

        /*for (int i = 0; i < numberOfClusters; i++) {
            clusterCenters[i] = ThreadLocalRandom.current().nextInt(-52, 52);
        }*/
        calculateDistance();
        calculateMatrix();
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
        label:
        for (int i = 0; i < numberOfPoints; i++) {
            double sum = 0;
            for (int j = 0; j < numberOfClusters; j++) {
                if (distances[i][j] > 0.001) {
                    sum += 1 / Math.pow(distances[i][j], 2 / (FUZZYNESS_COEF - 1));
                } else {
                    for (int k = 0; k < numberOfClusters; k++) {
                        newFuzzyPartitionMatrix[i][k] = k == j ? 1.0 : 0.0;
                    }
                    continue label;
                }
            }

            for (int j = 0; j < numberOfClusters; j++) {
                newFuzzyPartitionMatrix[i][j] = 1 / (Math.pow(distances[i][j], 2 / (FUZZYNESS_COEF - 1)) * sum);
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

        System.out.println("Cmeans from centers");
        printPoints();
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

    private void printPoints() {
        System.out.println("Points:");
        System.out.println(Arrays.stream(points).mapToObj(Integer::toString).collect(Collectors.joining(" ")));
    }
}

