import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FormationLine {
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
