import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FormationLine {

    final private double center;
    final private List<FuzzyPosition> fuzzyPositions = new ArrayList<>();

    public FormationLine(double center) {
        this.center = center;
    }

    public void addFuzzyPosition(FuzzyPosition fuzzyPosition) {
        fuzzyPositions.add(fuzzyPosition);
    }

    public double getCenter() {
        return center;
    }

    public List<FuzzyPosition> getFuzzyPositions() {
        return fuzzyPositions;
    }

    @Override
    public String toString() {
        return fuzzyPositions.stream()
            .map(FuzzyPosition::toString)
            .collect(Collectors.joining(" ; ",
                                        String.format("%.1f", center) + " : { ",
                                        " }"));
    }
}
